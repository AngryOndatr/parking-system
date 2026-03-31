package com.parking.common.domain;

import com.parking.common.entity.ParkingSpace;
import com.parking.common.entity.Subscription;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain wrapper around JPA Subscription entity.
 * Encapsulates business logic (expiry check, deactivation) while
 * delegating persistence state to the underlying entity.
 */
public class SubscriptionDomain {

    private final Subscription entity;

    public SubscriptionDomain(Subscription entity) {
        this.entity = Objects.requireNonNull(entity, "subscription entity must not be null");
    }

    public Subscription getEntity() {
        return entity;
    }

    // ── Identity ──────────────────────────────────────────────────

    public Long getId() {
        return entity.getId();
    }

    // ── Dates ─────────────────────────────────────────────────────

    public LocalDateTime getStartDate() {
        return entity.getStartDate();
    }

    public void setStartDate(LocalDateTime startDate) {
        entity.setStartDate(startDate);
    }

    public LocalDateTime getEndDate() {
        return entity.getEndDate();
    }

    public void setEndDate(LocalDateTime endDate) {
        entity.setEndDate(endDate);
    }

    // ── Type ──────────────────────────────────────────────────────

    public String getType() {
        return entity.getType();
    }

    public void setType(String type) {
        entity.setType(type);
    }

    // ── Status ────────────────────────────────────────────────────

    public Boolean getIsActive() {
        return entity.getIsActive();
    }

    // ── Parking Space ─────────────────────────────────────────────

    public ParkingSpace getParkingSpace() {
        return entity.getParkingSpace();
    }

    public void setParkingSpace(ParkingSpace space) {
        entity.setParkingSpace(space);
    }

    // ── Business logic ────────────────────────────────────────────

    /**
     * Returns true when the subscription has passed its endDate.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(entity.getEndDate());
    }

    /**
     * Soft-deactivates the subscription.
     * Mutates the underlying entity — caller must persist afterwards.
     */
    public void deactivate() {
        entity.setIsActive(false);
    }
}

