package com.myblob.module.flight.repository;

import com.myblob.module.flight.entity.FlightRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRouteRepository extends JpaRepository<FlightRoute, Long> {

    @Query("SELECT f FROM FlightRoute f WHERE f.callsign = :callsign AND f.deleted = false")
    Optional<FlightRoute> findByCallsign(@Param("callsign") String callsign);

    @Query("SELECT f FROM FlightRoute f WHERE f.callsign IN :callsigns AND f.deleted = false")
    List<FlightRoute> findByCallsignIn(@Param("callsigns") List<String> callsigns);

    @Query("SELECT f FROM FlightRoute f WHERE LOWER(f.airline) LIKE LOWER(CONCAT('%', :airline, '%')) AND f.deleted = false")
    Page<FlightRoute> findByAirlineContainingIgnoreCase(@Param("airline") String airline, Pageable pageable);

    @Query("SELECT f FROM FlightRoute f WHERE f.changeType != :changeType AND f.deleted = false")
    Page<FlightRoute> findByChangeTypeNot(@Param("changeType") String changeType, Pageable pageable);

    @Query("SELECT DISTINCT f.airline FROM FlightRoute f WHERE f.airline IS NOT NULL AND f.deleted = false ORDER BY f.airline")
    List<String> findAllAirlines();

    @Query("SELECT f FROM FlightRoute f WHERE f.changeType != 'NORMAL' AND f.deleted = false ORDER BY f.updatedAt DESC")
    List<FlightRoute> findRecentChanges(Pageable pageable);

    @Query("SELECT f FROM FlightRoute f WHERE f.lastSeen > :cutoff AND f.deleted = false ORDER BY f.callsign")
    List<FlightRoute> findActiveFlights(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("UPDATE FlightRoute f SET f.changeType = 'CANCELLED', f.status = 'not-seen' " +
           "WHERE f.lastSeen < :cutoff AND f.changeType != 'CANCELLED' AND f.deleted = false")
    int markStaleFlightsAsCancelled(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("DELETE FROM FlightRoute f WHERE f.lastSeen < :cutoff AND f.deleted = false")
    void deleteByLastSeenBefore(@Param("cutoff") LocalDateTime cutoff);
}
