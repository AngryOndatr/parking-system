package com.parking.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a parking space in a parking lot.
 * Corresponds to parking_spaces table in the database.
 */
@Entity
@Table(name = "parking_spaces")
@Data
@NoArgsConstructor
public class ParkingSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parking_lot_id", nullable = false)
    private Long parkingLotId;

    @Column(name = "space_number", nullable = false, length = 20)
    private String spaceNumber;

    @Column(name = "floor_level")
    private Integer floorLevel;

    @Column(name = "section", length = 50)
    private String section;

    @Column(name = "space_type", length = 50)
    private String spaceType; // STANDARD, HANDICAPPED, ELECTRIC, VIP, COMPACT, OVERSIZED

    @Column(name = "status", length = 20)
    private String status; // AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, OUT_OF_SERVICE

    @Column(name = "has_charger")
    private Boolean hasCharger;

    @Column(name = "charger_type", length = 50)
    private String chargerType;

    @Column(name = "length_cm")
    private Integer lengthCm;

    @Column(name = "width_cm")
    private Integer widthCm;

    @Column(name = "hourly_rate_override", precision = 10, scale = 2)
    private BigDecimal hourlyRateOverride;

    @Column(name = "daily_rate_override", precision = 10, scale = 2)
    private BigDecimal dailyRateOverride;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_occupied_at")
    private LocalDateTime lastOccupiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "AVAILABLE";
        }
        if (spaceType == null) {
            spaceType = "STANDARD";
        }
        if (hasCharger == null) {
            hasCharger = false;
        }
        if (floorLevel == null) {
            floorLevel = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

