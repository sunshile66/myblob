package com.myblob.module.flight.repository;

import com.myblob.module.flight.entity.FlightRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRouteRepository extends JpaRepository<FlightRoute, Long> {

    Optional<FlightRoute> findByCallsign(String callsign);

    Page<FlightRoute> findByAirlineContainingIgnoreCase(String airline, Pageable pageable);

    Page<FlightRoute> findByChangeTypeNot(String changeType, Pageable pageable);

    @Query("SELECT DISTINCT f.airline FROM FlightRoute f WHERE f.airline IS NOT NULL ORDER BY f.airline")
    List<String> findAllAirlines();

    @Query("SELECT f FROM FlightRoute f WHERE f.changeType != 'NORMAL' ORDER BY f.updatedAt DESC")
    List<FlightRoute> findRecentChanges(Pageable pageable);

    @Query("SELECT f FROM FlightRoute f WHERE f.lastSeen > :cutoff ORDER BY f.callsign")
    List<FlightRoute> findActiveFlights(@Param("cutoff") LocalDateTime cutoff);

    void deleteByLastSeenBefore(LocalDateTime cutoff);
}
