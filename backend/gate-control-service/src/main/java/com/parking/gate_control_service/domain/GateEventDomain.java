package com.parking.gate_control_service.domain;

import com.parking.gate_control_service.entity.GateEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Domain model wrapping GateEvent entity.
 * Contains business logic for gate event operations.
 */
@Getter
@AllArgsConstructor
public class GateEventDomain {

    private final GateEvent entity;

    /**
     * Check if this is an entry event.
     *
     * @return true if event type is ENTRY, false otherwise
     */
    public boolean isEntry() {
        return entity.getEventType() == GateEvent.EventType.ENTRY;
    }

    /**
     * Check if this is an exit event.
     *
     * @return true if event type is EXIT, false otherwise
     */
    public boolean isExit() {
        return entity.getEventType() == GateEvent.EventType.EXIT;
    }

    /**
     * Check if this is a manual open event.
     *
     * @return true if event type is MANUAL_OPEN, false otherwise
     */
    public boolean isManualOpen() {
        return entity.getEventType() == GateEvent.EventType.MANUAL_OPEN;
    }

    /**
     * Check if this is an error event.
     *
     * @return true if event type is ERROR, false otherwise
     */
    public boolean isError() {
        return entity.getEventType() == GateEvent.EventType.ERROR;
    }

    /**
     * Check if the gate decision was to open.
     *
     * @return true if decision is OPEN, false otherwise
     */
    public boolean wasOpened() {
        return entity.getDecision() == GateEvent.Decision.OPEN;
    }

    /**
     * Check if the gate decision was to deny.
     *
     * @return true if decision is DENY, false otherwise
     */
    public boolean wasDenied() {
        return entity.getDecision() == GateEvent.Decision.DENY;
    }

    /**
     * Check if this event had operator involvement.
     *
     * @return true if operator ID is present, false otherwise
     */
    public boolean hasOperatorInvolvement() {
        return entity.getOperatorId() != null;
    }

    /**
     * Get the gate event ID.
     *
     * @return event ID
     */
    public Long getId() {
        return entity.getId();
    }

    /**
     * Get the event type.
     *
     * @return event type
     */
    public GateEvent.EventType getEventType() {
        return entity.getEventType();
    }

    /**
     * Get the license plate.
     *
     * @return license plate
     */
    public String getLicensePlate() {
        return entity.getLicensePlate();
    }

    /**
     * Get the ticket code (may be null).
     *
     * @return ticket code or null
     */
    public String getTicketCode() {
        return entity.getTicketCode();
    }

    /**
     * Get the gate ID.
     *
     * @return gate ID
     */
    public String getGateId() {
        return entity.getGateId();
    }

    /**
     * Get the decision.
     *
     * @return gate decision
     */
    public GateEvent.Decision getDecision() {
        return entity.getDecision();
    }

    /**
     * Get the reason for the decision.
     *
     * @return reason
     */
    public String getReason() {
        return entity.getReason();
    }

    /**
     * Get the timestamp.
     *
     * @return event timestamp
     */
    public LocalDateTime getTimestamp() {
        return entity.getTimestamp();
    }

    /**
     * Get the operator ID (may be null).
     *
     * @return operator ID or null
     */
    public Long getOperatorId() {
        return entity.getOperatorId();
    }

    /**
     * Create a domain model from an entity.
     *
     * @param entity the gate event entity
     * @return domain model wrapping the entity
     */
    public static GateEventDomain from(GateEvent entity) {
        if (entity == null) {
            throw new IllegalArgumentException("GateEvent entity cannot be null");
        }
        return new GateEventDomain(entity);
    }

    /**
     * Get the wrapped entity.
     *
     * @return the gate event entity
     */
    public GateEvent toEntity() {
        return entity;
    }
}
