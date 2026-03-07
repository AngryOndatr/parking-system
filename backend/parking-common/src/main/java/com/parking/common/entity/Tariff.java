package com.parking.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tariff Entity
 * Represents parking tariff configuration for different parking types.
 * Maps to 'tariffs' table in the database.
 */
@Entity
@Table(name = "tariffs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of tariff: ONE_TIME, DAILY, NIGHT, VIP
     */
    @Column(name = "tariff_type", nullable = false, unique = true, length = 50)
    @NotNull(message = "Tariff type cannot be null")
    private String tariffType;

    /**
     * Hourly rate in the local currency
     */
    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Hourly rate cannot be null")
    @DecimalMin(value = "0.0", message = "Hourly rate must be greater than or equal to 0")
    private BigDecimal hourlyRate;

    /**
     * Daily rate in the local currency (optional, for daily tariffs)
     */
    @Column(name = "daily_rate", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Daily rate must be greater than or equal to 0")
    private BigDecimal dailyRate;

    /**
     * Description of the tariff
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Whether the tariff is currently active
     */
    @Column(name = "is_active", nullable = false)
    @NotNull(message = "isActive flag cannot be null")
    private Boolean isActive = true;

    /**
     * Timestamp when the tariff was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the tariff was last updated
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Set timestamps before persisting
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Update timestamp before updating
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

