package com.parking.client_service.mapper;

import com.parking.client_service.generated.model.SubscriptionRequest;
import com.parking.client_service.generated.model.SubscriptionResponse;
import com.parking.common.domain.SubscriptionDomain;
import com.parking.common.entity.Client;
import com.parking.common.entity.Subscription;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Manual mapper for Subscription entity / domain / generated DTOs.
 * Follows the same manual-mapping pattern used by VehicleMapper.
 */
@Component
public class SubscriptionMapper {

    // ── Entity → Domain ───────────────────────────────────────────

    public SubscriptionDomain toDomain(Subscription entity) {
        return new SubscriptionDomain(entity);
    }

    // ── SubscriptionRequest → Entity ──────────────────────────────

    /**
     * Creates a new Subscription entity from the OpenAPI-generated request DTO.
     * Client association and isActive default are set here.
     */
    public Subscription toEntity(SubscriptionRequest request, Client client) {
        Subscription entity = new Subscription();
        entity.setClient(client);
        entity.setType(request.getType().getValue());
        entity.setStartDate(toLocalDateTime(request.getStartDate()));
        entity.setEndDate(toLocalDateTime(request.getEndDate()));
        entity.setIsActive(true);
        return entity;
    }

    // ── Entity → SubscriptionResponse ─────────────────────────────

    public SubscriptionResponse toResponse(Subscription entity) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(entity.getId());
        if (entity.getClient() != null) {
            response.setClientId(entity.getClient().getId());
        }
        response.setType(entity.getType());
        response.setStartDate(toOffsetDateTime(entity.getStartDate()));
        response.setEndDate(toOffsetDateTime(entity.getEndDate()));
        response.setIsActive(entity.getIsActive());
        return response;
    }

    // ── Domain → SubscriptionResponse ─────────────────────────────

    public SubscriptionResponse toResponse(SubscriptionDomain domain) {
        return toResponse(domain.getEntity());
    }

    // ── DateTime converters ───────────────────────────────────────

    private LocalDateTime toLocalDateTime(OffsetDateTime odt) {
        if (odt == null) return null;
        return odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}

