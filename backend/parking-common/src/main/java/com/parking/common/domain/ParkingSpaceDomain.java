package com.parking.common.domain;

import com.parking.common.entity.ParkingSpace;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain wrapper around JPA ParkingSpace entity.
 * This class delegates business-facing accessors to the underlying JPA entity.
 * Use it when you want a domain object but keep the single source of truth in the entity.
 */
public class ParkingSpaceDomain {

    private final ParkingSpace entity;

    public ParkingSpaceDomain(ParkingSpace parkingSpace) {
        this.entity = Objects.requireNonNull(parkingSpace, "parkingSpace entity must not be null");
    }

    public ParkingSpace getEntity() {
        return entity;
    }

    public Long getId() {
        return entity.getId();
    }

    public void setId(Long id) {
        entity.setId(id);
    }

    public Long getParkingLotId() {
        return entity.getParkingLotId();
    }

    public void setParkingLotId(Long parkingLotId) {
        entity.setParkingLotId(parkingLotId);
    }

    public String getSpaceNumber() {
        return entity.getSpaceNumber();
    }

    public void setSpaceNumber(String spaceNumber) {
        entity.setSpaceNumber(spaceNumber);
    }

    public Integer getFloorLevel() {
        return entity.getFloorLevel();
    }

    public void setFloorLevel(Integer floorLevel) {
        entity.setFloorLevel(floorLevel);
    }

    public String getSection() {
        return entity.getSection();
    }

    public void setSection(String section) {
        entity.setSection(section);
    }

    public String getSpaceType() {
        return entity.getSpaceType();
    }

    public void setSpaceType(String spaceType) {
        entity.setSpaceType(spaceType);
    }

    public String getStatus() {
        return entity.getStatus();
    }

    public void setStatus(String status) {
        entity.setStatus(status);
    }

    public Boolean getHasCharger() {
        return entity.getHasCharger();
    }

    public void setHasCharger(Boolean hasCharger) {
        entity.setHasCharger(hasCharger);
    }

    public String getChargerType() {
        return entity.getChargerType();
    }

    public void setChargerType(String chargerType) {
        entity.setChargerType(chargerType);
    }

    public Integer getLengthCm() {
        return entity.getLengthCm();
    }

    public void setLengthCm(Integer lengthCm) {
        entity.setLengthCm(lengthCm);
    }

    public Integer getWidthCm() {
        return entity.getWidthCm();
    }

    public void setWidthCm(Integer widthCm) {
        entity.setWidthCm(widthCm);
    }

    public BigDecimal getHourlyRateOverride() {
        return entity.getHourlyRateOverride();
    }

    public void setHourlyRateOverride(BigDecimal hourlyRateOverride) {
        entity.setHourlyRateOverride(hourlyRateOverride);
    }

    public BigDecimal getDailyRateOverride() {
        return entity.getDailyRateOverride();
    }

    public void setDailyRateOverride(BigDecimal dailyRateOverride) {
        entity.setDailyRateOverride(dailyRateOverride);
    }

    public LocalDateTime getCreatedAt() {
        return entity.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return entity.getUpdatedAt();
    }

    public LocalDateTime getLastOccupiedAt() {
        return entity.getLastOccupiedAt();
    }

    public void setLastOccupiedAt(LocalDateTime lastOccupiedAt) {
        entity.setLastOccupiedAt(lastOccupiedAt);
    }

    /**
     * Business logic: Check if the space is currently available for parking.
     */
    public boolean isAvailable() {
        return "AVAILABLE".equals(getStatus());
    }

    /**
     * Business logic: Check if the space supports electric vehicle charging.
     */
    public boolean supportsEVCharging() {
        return Boolean.TRUE.equals(getHasCharger()) && getChargerType() != null;
    }

    /**
     * Business logic: Mark the space as occupied.
     */
    public void markAsOccupied() {
        setStatus("OCCUPIED");
        setLastOccupiedAt(LocalDateTime.now());
    }

    /**
     * Business logic: Mark the space as available.
     */
    public void markAsAvailable() {
        setStatus("AVAILABLE");
    }

    /**
     * Business logic: Check if the space is suitable for a specific vehicle type.
     */
    public boolean isSuitableFor(String vehicleType) {
        // Add business logic for determining suitability
        // For now, simple check
        if ("ELECTRIC".equals(vehicleType)) {
            return supportsEVCharging();
        }
        return true; // All spaces suitable for standard vehicles
    }

    @Override
    public String toString() {
        return "ParkingSpaceDomain{" +
                "id=" + getId() +
                ", spaceNumber='" + getSpaceNumber() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", type='" + getSpaceType() + '\'' +
                ", lotId=" + getParkingLotId() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpaceDomain that = (ParkingSpaceDomain) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

