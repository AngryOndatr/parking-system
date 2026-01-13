package com.parking.common.domain;

import com.parking.common.entity.Client;
import com.parking.common.entity.Vehicle;

/**
 * Domain model for Vehicle business logic.
 * Wraps the Vehicle entity to provide business-level operations.
 */
public class VehicleDomain {

    private final Vehicle entity;

    public VehicleDomain(Vehicle vehicle) {
        this.entity = vehicle;
    }

    public Vehicle getEntity() {
        return entity;
    }

    // Business-level getters (delegate to entity)
    public Long getId() {
        return entity.getId();
    }

    public String getLicensePlate() {
        return entity.getLicensePlate();
    }

    public Client getClient() {
        return entity.getClient();
    }

    public Boolean getIsAllowed() {
        return entity.getIsAllowed();
    }

    // Business-level setters (delegate to entity)
    public void setLicensePlate(String licensePlate) {
        entity.setLicensePlate(licensePlate);
    }

    public void setClient(Client client) {
        entity.setClient(client);
    }

    public void setIsAllowed(Boolean isAllowed) {
        entity.setIsAllowed(isAllowed);
    }

    // Business logic methods can be added here
    // Example: validation, status changes, etc.
}

