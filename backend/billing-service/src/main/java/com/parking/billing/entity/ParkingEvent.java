package com.parking.billing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a parking event (entry/exit session).
 * Maps to parking_events table in the database.
 */
@Entity
@Table(name = "parking_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "ticket_code", unique = true, length = 50)
    private String ticketCode;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_method", length = 20)
    private EntryMethod entryMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "exit_method", length = 20)
    private ExitMethod exitMethod;

    @Column(name = "spot_id")
    private Long spotId;

    @Column(name = "is_subscriber", nullable = false)
    private Boolean isSubscriber = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enum representing the method of entry.
     */
    public enum EntryMethod {
        SCAN,
        MANUAL
    }

    /**
     * Enum representing the method of exit.
     */
    public enum ExitMethod {
        SCAN,
        MANUAL,
        AUTO
    }

    /**
     * Set default values before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (entryTime == null) {
            entryTime = LocalDateTime.now();
        }
        if (isSubscriber == null) {
            isSubscriber = false;
        }
    }
}

