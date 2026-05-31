package com.myblob.module.flight.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.flight.entity.FlightRoute;
import com.myblob.module.flight.repository.FlightRouteRepository;
import com.myblob.module.flight.service.FlightTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightRouteRepository flightRouteRepository;
    private final FlightTrackingService flightTrackingService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<PageResponse<FlightRoute>>> getFlights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String airline,
            @RequestParam(required = false) String changeType) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<FlightRoute> result;

        if (airline != null && !airline.isEmpty()) {
            result = flightRouteRepository.findByAirlineContainingIgnoreCase(airline, pageable);
        } else if (changeType != null && !changeType.isEmpty()) {
            result = flightRouteRepository.findByChangeTypeNot("NORMAL", pageable);
        } else {
            result = flightRouteRepository.findAll(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    @GetMapping("/changes/")
    public ResponseEntity<ApiResponse<List<FlightRoute>>> getRecentChanges(
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<FlightRoute> changes = flightRouteRepository.findRecentChanges(pageable);
        return ResponseEntity.ok(ApiResponse.success(changes));
    }

    @GetMapping("/airlines/")
    public ResponseEntity<ApiResponse<List<String>>> getAirlines() {
        return ResponseEntity.ok(ApiResponse.success(flightTrackingService.getAirlines()));
    }

    @PostMapping("/fetch/")
    public ResponseEntity<ApiResponse<Integer>> triggerFetch() {
        int count = flightTrackingService.fetchAndStoreFlights();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
