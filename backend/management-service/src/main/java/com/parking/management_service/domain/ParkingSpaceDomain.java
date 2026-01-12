package com.parking.management_service.domain;

import com.parking.common.entity.ParkingSpace;
import lombok.Getter;

/**
 * Domain model for ParkingSpace
 * Wraps the entity and provides business logic methods
 */
@Getter
public class ParkingSpaceDomain {

    private final ParkingSpace entity;

    public ParkingSpaceDomain(ParkingSpace entity) {
        this.entity = entity;
    }

    // Delegate getters to entity
    public Long getId() {
        return entity.getId();
    }

    public Long getParkingLotId() {
        return entity.getParkingLotId();
    }

    public String getSpaceNumber() {
        return entity.getSpaceNumber();
    }

    public Integer getFloorLevel() {
        return entity.getFloorLevel();
    }

    public String getSpaceType() {
        return entity.getSpaceType();
    }

    public String getStatus() {
        return entity.getStatus();
    }

    public Boolean getHasCharger() {
        return entity.getHasCharger();
    }

    // Business logic methods

    /**
     * Check if space is available
     */
    public boolean isAvailable() {
        return "AVAILABLE".equals(entity.getStatus());
    }

    /**
     * Check if space is occupied
     */
    public boolean isOccupied() {
        return "OCCUPIED".equals(entity.getStatus());
    }

    /**
     * Check if space has EV charger
     */
    public boolean hasElectricCharger() {
        return Boolean.TRUE.equals(entity.getHasCharger());
    }

    /**
     * Check if space is handicapped accessible
     */
    public boolean isHandicapped() {
        return "HANDICAPPED".equals(entity.getSpaceType());
    }
}

