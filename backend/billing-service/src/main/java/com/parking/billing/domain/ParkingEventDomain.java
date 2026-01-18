package com.parking.billing.domain;

import com.parking.billing.entity.ParkingEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Domain model wrapping ParkingEvent entity.
 * Contains business logic for parking event operations.
 */
@Getter
@AllArgsConstructor
public class ParkingEventDomain {

    private final ParkingEvent entity;

    /**
     * Calculate parking duration in hours (rounded up).
     *
     * @param exitTime the exit time to calculate duration until
     * @return duration in hours, rounded up (minimum 1 hour)
     */
    public long calculateDurationInHours(LocalDateTime exitTime) {
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit time cannot be null");
        }

        LocalDateTime entryTime = entity.getEntryTime();
        if (entryTime == null) {
            throw new IllegalStateException("Entry time is not set");
        }

        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }

        Duration duration = Duration.between(entryTime, exitTime);
        long minutes = duration.toMinutes();

        // Round up to the nearest hour
        long hours = (minutes + 59) / 60;

        // Minimum charge is 1 hour
        return Math.max(hours, 1);
    }

    /**
     * Calculate parking fee based on hourly rate.
     *
     * @param exitTime the exit time
     * @param hourlyRate the hourly rate
     * @return calculated fee
     */
    public BigDecimal calculateFee(LocalDateTime exitTime, BigDecimal hourlyRate) {
        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Hourly rate must be non-negative");
        }

        long hours = calculateDurationInHours(exitTime);
        return hourlyRate.multiply(BigDecimal.valueOf(hours))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Check if this is a subscriber parking event.
     *
     * @return true if subscriber, false otherwise
     */
    public boolean isSubscriber() {
        return Boolean.TRUE.equals(entity.getIsSubscriber());
    }

    /**
     * Get the ticket code.
     *
     * @return ticket code
     */
    public String getTicketCode() {
        return entity.getTicketCode();
    }

    /**
     * Get the parking event ID.
     *
     * @return event ID
     */
    public Long getId() {
        return entity.getId();
    }

    /**
     * Get the entry time.
     *
     * @return entry time
     */
    public LocalDateTime getEntryTime() {
        return entity.getEntryTime();
    }

    /**
     * Get the exit time.
     *
     * @return exit time
     */
    public LocalDateTime getExitTime() {
        return entity.getExitTime();
    }

    /**
     * Set the exit time.
     *
     * @param exitTime exit time to set
     */
    public void setExitTime(LocalDateTime exitTime) {
        entity.setExitTime(exitTime);
    }
}

