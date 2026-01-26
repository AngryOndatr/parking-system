package com.parking.gate_control_service.mapper;

import com.parking.gate_control_service.domain.GateEventDomain;
import com.parking.gate_control_service.entity.GateEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Mapper for converting between entity, domain models and DTOs for Gate Control Service.
 */
@Component
public class GateControlMapper {

    /**
     * Convert GateEvent entity to domain model.
     *
     * @param entity the gate event entity
     * @return domain model
     */
    public GateEventDomain toDomain(GateEvent entity) {
        if (entity == null) {
            return null;
        }
        return GateEventDomain.from(entity);
    }

    /**
     * Convert domain model to entity.
     *
     * @param domain the gate event domain model
     * @return entity
     */
    public GateEvent toEntity(GateEventDomain domain) {
        if (domain == null) {
            return null;
        }
        return domain.toEntity();
    }

    /**
     * Create a new GateEvent entity from parameters.
     *
     * @param eventType the event type
     * @param licensePlate the license plate
     * @param ticketCode the ticket code (optional)
     * @param gateId the gate ID
     * @param decision the gate decision
     * @param reason the reason for the decision
     * @param operatorId the operator ID (optional)
     * @return new GateEvent entity
     */
    public GateEvent createGateEvent(
            GateEvent.EventType eventType,
            String licensePlate,
            String ticketCode,
            String gateId,
            GateEvent.Decision decision,
            String reason,
            Long operatorId
    ) {
        GateEvent event = new GateEvent();
        event.setEventType(eventType);
        event.setLicensePlate(licensePlate);
        event.setTicketCode(ticketCode);
        event.setGateId(gateId);
        event.setDecision(decision);
        event.setReason(reason);
        event.setOperatorId(operatorId);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    /**
     * Convert LocalDateTime to OffsetDateTime.
     *
     * @param localDateTime the local date time
     * @return offset date time with system default zone
     */
    public OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneOffset.systemDefault()).toOffsetDateTime();
    }

    /**
     * Convert OffsetDateTime to LocalDateTime.
     *
     * @param offsetDateTime the offset date time
     * @return local date time
     */
    public LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.atZoneSameInstant(ZoneOffset.systemDefault()).toLocalDateTime();
    }
}
