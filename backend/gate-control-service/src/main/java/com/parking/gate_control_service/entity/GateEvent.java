package com.parking.gate_control_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a gate operation event.
 * Maps to gate_events table in the database.
 * Logs all gate operations including entry, exit, manual operations and errors.
 */
@Entity
@Table(name = "gate_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GateEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "ticket_code", length = 50)
    private String ticketCode;

    @Column(name = "gate_id", nullable = false, length = 20)
    private String gateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 10)
    private Decision decision;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * Enum representing the type of gate event.
     */
    public enum EventType {
        ENTRY,
        EXIT,
        MANUAL_OPEN,
        ERROR
    }

    /**
     * Enum representing the gate decision.
     */
    public enum Decision {
        OPEN,
        DENY
    }

    /**
     * Set default timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
