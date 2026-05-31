package com.myblob.module.flight.service;

import com.myblob.module.flight.entity.FlightRoute;
import com.myblob.module.flight.repository.FlightRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightTrackingService {

    private final FlightRouteRepository flightRouteRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // OpenSky Network API - free, no API key required
    private static final String OPENSKY_API = "https://opensky-network.org/api/states/all";

    // Rate limiting: 10 seconds minimum between requests (anonymous)
    private static final long MIN_REQUEST_INTERVAL_MS = 10_000;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_BACKOFF_MS = 15_000; // 15s base backoff
    private volatile long lastRequestTime = 0;

    // Known airline callsign prefixes for mapping
    private static final Map<String, String> AIRLINE_MAP = new LinkedHashMap<>();
    static {
        AIRLINE_MAP.put("UAE", "Emirates");
        AIRLINE_MAP.put("QTR", "Qatar Airways");
        AIRLINE_MAP.put("ETD", "Etihad Airways");
        AIRLINE_MAP.put("DLH", "Lufthansa");
        AIRLINE_MAP.put("BAW", "British Airways");
        AIRLINE_MAP.put("AFR", "Air France");
        AIRLINE_MAP.put("KLM", "KLM");
        AIRLINE_MAP.put("RYR", "Ryanair");
        AIRLINE_MAP.put("EZY", "easyJet");
        AIRLINE_MAP.put("SIA", "Singapore Airlines");
        AIRLINE_MAP.put("CPA", "Cathay Pacific");
        AIRLINE_MAP.put("ANA", "All Nippon Airways");
        AIRLINE_MAP.put("JAL", "Japan Airlines");
        AIRLINE_MAP.put("KAL", "Korean Air");
        AIRLINE_MAP.put("THY", "Turkish Airlines");
        AIRLINE_MAP.put("DAL", "Delta Air Lines");
        AIRLINE_MAP.put("UAL", "United Airlines");
        AIRLINE_MAP.put("AAL", "American Airlines");
        AIRLINE_MAP.put("SWA", "Southwest Airlines");
        AIRLINE_MAP.put("JBU", "JetBlue Airways");
        AIRLINE_MAP.put("QFA", "Qantas");
        AIRLINE_MAP.put("VIR", "Virgin Atlantic");
        AIRLINE_MAP.put("ACA", "Air Canada");
        AIRLINE_MAP.put("AAR", "Asiana Airlines");
        AIRLINE_MAP.put("THA", "Thai Airways");
        AIRLINE_MAP.put("HVN", "Vietnam Airlines");
        AIRLINE_MAP.put("MAS", "Malaysia Airlines");
        AIRLINE_MAP.put("PAL", "Philippine Airlines");
        AIRLINE_MAP.put("IGO", "IndiGo");
        AIRLINE_MAP.put("SAS", "SAS Scandinavian");
        AIRLINE_MAP.put("SWR", "Swiss International");
        AIRLINE_MAP.put("SAS", "Scandinavian Airlines");
        AIRLINE_MAP.put("FIN", "Finnair");
        AIRLINE_MAP.put("IBE", "Iberia");
        AIRLINE_MAP.put("TAP", "TAP Air Portugal");
        AIRLINE_MAP.put("ETH", "Ethiopian Airlines");
        AIRLINE_MAP.put("SAA", "South African Airways");
        AIRLINE_MAP.put("ELY", "El Al");
        AIRLINE_MAP.put("GFA", "Gulf Air");
        AIRLINE_MAP.put("OAI", "Oman Air");
        AIRLINE_MAP.put("RJA", "Royal Jordanian");
    }

    /**
     * Fetch current flight states from OpenSky Network.
     * Filter for known international airlines only.
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public int fetchAndStoreFlights() {
        try {
            log.info("Fetching flight data from OpenSky Network...");

            // Enforce rate limiting: wait if needed
            enforceRateLimit();

            // Fetch with retry + exponential backoff
            Map<String, Object> response = fetchWithRetry();
            if (response == null || response.get("states") == null) {
                log.warn("OpenSky API returned no data after {} retries", MAX_RETRIES);
                return 0;
            }

            List<List<Object>> states = (List<List<Object>>) response.get("states");
            int saved = 0;
            int now = (int) (System.currentTimeMillis() / 1000);

            for (List<Object> state : states) {
                if (state.size() < 17) continue;

                String callsign = state.get(1) != null ? state.get(1).toString().trim() : "";
                if (callsign.isEmpty()) continue;

                // Check if this is a known airline
                String prefix3 = callsign.length() >= 3 ? callsign.substring(0, 3) : "";
                String airline = AIRLINE_MAP.get(prefix3);
                if (airline == null) continue; // Skip non-airline flights

                String icao24 = state.get(0) != null ? state.get(0).toString() : "";
                String country = state.get(2) != null ? state.get(2).toString() : "";
                Double longitude = state.get(5) instanceof Number ? ((Number) state.get(5)).doubleValue() : null;
                Double latitude = state.get(6) instanceof Number ? ((Number) state.get(6)).doubleValue() : null;
                Double altitude = state.get(7) instanceof Number ? ((Number) state.get(7)).doubleValue() : null;
                Double velocity = state.get(9) instanceof Number ? ((Number) state.get(9)).doubleValue() : null;
                Object lastContactObj = state.get(4);
                int lastContact = lastContactObj instanceof Number ? ((Number) lastContactObj).intValue() : now;

                // Save or update flight
                Optional<FlightRoute> existing = flightRouteRepository.findByCallsign(callsign);
                FlightRoute flight;
                String changeType;

                if (existing.isPresent()) {
                    flight = existing.get();
                    changeType = "NORMAL";
                } else {
                    flight = FlightRoute.builder()
                            .callsign(callsign)
                            .flightNumber(callsign)
                            .airline(airline)
                            .airlineCode(prefix3)
                            .status("in-flight")
                            .changeType("NEW")
                            .build();
                    changeType = "NEW";
                }

                flight.setIcao24(icao24);
                flight.setCountry(country);
                flight.setLatitude(latitude);
                flight.setLongitude(longitude);
                flight.setAltitude(altitude);
                flight.setVelocity(velocity);
                flight.setLastSeen(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(lastContact), ZoneId.systemDefault()));
                flight.setChangeType(changeType);

                flightRouteRepository.save(flight);
                saved++;
            }

            // Mark flights not seen in 2 hours as potentially cancelled
            LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
            List<FlightRoute> staleFlights = flightRouteRepository.findActiveFlights(cutoff);
            for (FlightRoute f : staleFlights) {
                if (f.getLastSeen() != null && f.getLastSeen().isBefore(cutoff) && !"CANCELLED".equals(f.getChangeType())) {
                    f.setChangeType("CANCELLED");
                    f.setStatus("not-seen");
                    flightRouteRepository.save(f);
                }
            }

            log.info("Flight tracking: saved/updated {} flights from {} total states", saved, states.size());
            return saved;

        } catch (Exception e) {
            log.error("Flight tracking fetch failed: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Enforce minimum interval between API requests (10s for anonymous access).
     */
    private void enforceRateLimit() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        if (elapsed < MIN_REQUEST_INTERVAL_MS && lastRequestTime > 0) {
            long waitMs = MIN_REQUEST_INTERVAL_MS - elapsed;
            log.debug("Rate limiting: waiting {}ms before OpenSky request", waitMs);
            try { Thread.sleep(waitMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    /**
     * Fetch OpenSky data with retry and exponential backoff.
     * Uses direct HTTP connection for better control over timeouts.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchWithRetry() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                URL url = URI.create(OPENSKY_API).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "MyBlob-FlightTracker/1.0");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15_000);
                conn.setReadTimeout(30_000);

                int status = conn.getResponseCode();
                if (status == 429 || status == 503) {
                    long backoff = RETRY_BACKOFF_MS * attempt;
                    log.warn("OpenSky rate limited (HTTP {}), retrying in {}s (attempt {}/{})",
                            status, backoff / 1000, attempt, MAX_RETRIES);
                    Thread.sleep(backoff);
                    continue;
                }
                if (status != 200) {
                    log.warn("OpenSky HTTP error: {} (attempt {}/{})", status, attempt, MAX_RETRIES);
                    if (attempt < MAX_RETRIES) Thread.sleep(RETRY_BACKOFF_MS * attempt);
                    continue;
                }

                try (InputStream is = conn.getInputStream()) {
                    return objectMapper.readValue(is, Map.class);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Flight fetch interrupted");
                return null;
            } catch (Exception e) {
                log.warn("OpenSky fetch attempt {}/{} failed: {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_BACKOFF_MS * attempt); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); return null;
                    }
                }
            }
        }
        return null;
    }

    @Scheduled(fixedRate = 3600000, initialDelay = 60000) // Every hour, first run after 60s
    public void scheduledFetch() {
        fetchAndStoreFlights();
    }

    @Scheduled(cron = "0 0 4 * * ?") // Daily at 4 AM
    @Transactional
    public void cleanupOldFlights() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        flightRouteRepository.deleteByLastSeenBefore(cutoff);
        log.info("Cleaned up flight data older than 7 days");
    }

    public List<String> getAirlines() {
        return flightRouteRepository.findAllAirlines();
    }
}
