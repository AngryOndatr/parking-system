package com.parking.client_service.service;

import com.parking.client_service.audit.AuditLogger;
import com.parking.client_service.exception.ConflictException;
import com.parking.client_service.exception.ResourceNotFoundException;
import com.parking.client_service.generated.model.SubscriptionRequest;
import com.parking.client_service.generated.model.SubscriptionResponse;
import com.parking.client_service.mapper.SubscriptionMapper;
import com.parking.client_service.repository.ClientRepository;
import com.parking.client_service.repository.ParkingSpaceRepository;
import com.parking.client_service.repository.SubscriptionRepository;
import com.parking.common.domain.SubscriptionDomain;
import com.parking.common.entity.Client;
import com.parking.common.entity.ParkingSpace;
import com.parking.common.entity.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for subscription management.
 *
 * Architecture:
 *   OpenAPI DTO  →  SubscriptionMapper  →  Subscription entity
 *   Subscription entity  →  SubscriptionDomain  (business logic)
 *   SubscriptionDomain / entity  →  SubscriptionMapper  →  OpenAPI DTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ClientRepository clientRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final AuditLogger auditLogger;

    /**
     * Create a new subscription for the given client.
     * If {@code request.parkingSpaceId} is provided the space is reserved (status → RESERVED).
     *
     * @throws ResourceNotFoundException if the client or parking space does not exist
     * @throws IllegalArgumentException  if endDate is not after startDate
     * @throws ConflictException         if an active subscription of the same type already exists
     *                                   or the requested parking space is not AVAILABLE
     */
    @Transactional
    public SubscriptionResponse createSubscription(Long clientId, SubscriptionRequest request) {
        log.info("Creating subscription for clientId={}, type={}", clientId, request.getType());

        // 1. Verify client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + clientId));

        // 2. Business rule: endDate must be strictly after startDate
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }

        // 3. One active subscription per type per client
        String typeValue = request.getType().getValue();
        if (subscriptionRepository.existsByClientIdAndTypeAndIsActiveTrue(clientId, typeValue)) {
            throw new ConflictException(
                    "Client " + clientId + " already has an active " + typeValue + " subscription");
        }

        // 4. Optionally reserve a parking space
        ParkingSpace reservedSpace = null;
        if (request.getParkingSpaceId() != null) {
            final Long spaceId = request.getParkingSpaceId();
            reservedSpace = parkingSpaceRepository.findById(spaceId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parking space not found with id: " + spaceId));
            if (!"AVAILABLE".equals(reservedSpace.getStatus())) {
                throw new ConflictException(
                        "Parking space " + reservedSpace.getSpaceNumber()
                                + " is not available (current status: " + reservedSpace.getStatus() + ")");
            }
            reservedSpace.setStatus("RESERVED");
            reservedSpace.setUpdatedAt(LocalDateTime.now());
            parkingSpaceRepository.save(reservedSpace);
            log.info("Reserved parking space id={} number={} for clientId={}",
                    reservedSpace.getId(), reservedSpace.getSpaceNumber(), clientId);
        }

        // 5. Persist via domain model
        Subscription entity = subscriptionMapper.toEntity(request, client);
        entity.setParkingSpace(reservedSpace);
        SubscriptionDomain domain = subscriptionMapper.toDomain(entity);
        Subscription saved = subscriptionRepository.save(domain.getEntity());

        log.info("Subscription created id={} for clientId={}", saved.getId(), clientId);

        String spaceInfo = reservedSpace != null
                ? ", spaceId=" + reservedSpace.getId() + ", space=" + reservedSpace.getSpaceNumber()
                : "";
        auditLogger.audit("SUBSCRIPTION_CREATED", "SUBSCRIPTION", saved.getId(),
                clientId, null,
                "Subscription created: type=" + typeValue + ", clientId=" + clientId
                        + ", from=" + request.getStartDate() + " to " + request.getEndDate() + spaceInfo);

        return subscriptionMapper.toResponse(saved);
    }

    /**
     * Return all subscriptions for a client, ordered newest-first.
     *
     * @throws ResourceNotFoundException if the client does not exist
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByClient(Long clientId) {
        log.debug("Listing subscriptions for clientId={}", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }

        return subscriptionRepository
                .findByClientIdOrderByStartDateDesc(clientId)
                .stream()
                .map(subscriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Soft-delete: sets isActive=false on the subscription.
     * If a parking space was reserved, it is released back to AVAILABLE.
     *
     * @throws ResourceNotFoundException if no subscription with the given id exists
     */
    @Transactional
    public void deactivateSubscription(Long subscriptionId) {
        log.info("Deactivating subscription id={}", subscriptionId);

        Subscription entity = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + subscriptionId));

        Long clientId = entity.getClient() != null ? entity.getClient().getId() : null;
        String type = entity.getType();

        // Release reserved parking space if any
        if (entity.getParkingSpace() != null) {
            ParkingSpace space = entity.getParkingSpace();
            log.info("Releasing parking space id={} number={} back to AVAILABLE",
                    space.getId(), space.getSpaceNumber());
            space.setStatus("AVAILABLE");
            space.setUpdatedAt(LocalDateTime.now());
            parkingSpaceRepository.save(space);
        }

        // Delegate mutation to domain model
        SubscriptionDomain domain = subscriptionMapper.toDomain(entity);
        domain.deactivate();                           // entity.isActive = false
        subscriptionRepository.save(domain.getEntity());

        log.info("Subscription id={} deactivated", subscriptionId);

        auditLogger.audit("SUBSCRIPTION_DEACTIVATED", "SUBSCRIPTION", subscriptionId,
                clientId, null,
                "Subscription deactivated: id=" + subscriptionId + ", type=" + type
                        + ", clientId=" + clientId);
    }
}
