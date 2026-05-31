package com.myblob.module.flight.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "flight_route", indexes = {
        @Index(name = "idx_flight_callsign", columnList = "callsign"),
        @Index(name = "idx_flight_airline", columnList = "airline"),
        @Index(name = "idx_flight_change_type", columnList = "change_type"),
        @Index(name = "idx_flight_updated_at", columnList = "updated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FlightRoute extends BaseEntity {

    @Column(name = "callsign", length = 20)
    private String callsign;

    @Column(name = "flight_number", length = 20)
    private String flightNumber;

    @Column(name = "airline", length = 100)
    private String airline;

    @Column(name = "airline_code", length = 10)
    private String airlineCode;

    @Column(name = "origin_airport", length = 10)
    private String originAirport;

    @Column(name = "origin_city", length = 100)
    private String originCity;

    @Column(name = "destination_airport", length = 10)
    private String destinationAirport;

    @Column(name = "destination_city", length = 100)
    private String destinationCity;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "scheduled";

    /**
     * Change type for highlighting:
     * NEW = new route, CANCELLED = cancelled route,
     * DELAYED = time change, NORMAL = no change
     */
    @Column(name = "change_type", length = 20)
    @Builder.Default
    private String changeType = "NORMAL";

    @Column(name = "altitude", nullable = true)
    private Double altitude;

    @Column(name = "velocity", nullable = true)
    private Double velocity;

    @Column(name = "latitude", nullable = true)
    private Double latitude;

    @Column(name = "longitude", nullable = true)
    private Double longitude;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "icao24", length = 10)
    private String icao24;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
}
