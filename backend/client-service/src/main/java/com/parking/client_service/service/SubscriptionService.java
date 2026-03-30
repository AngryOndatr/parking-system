package com.parking.client_service.service;

import com.parking.client_service.exception.ConflictException;
import com.parking.client_service.exception.ResourceNotFoundException;
import com.parking.client_service.generated.model.SubscriptionRequest;
import com.parking.client_service.generated.model.SubscriptionResponse;
import com.parking.client_service.mapper.SubscriptionMapper;
import com.parking.client_service.repository.ClientRepository;
import com.parking.client_service.repository.SubscriptionRepository;
import com.parking.common.domain.SubscriptionDomain;
import com.parking.common.entity.Client;
import com.parking.common.entity.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SubscriptionMapper subscriptionMapper;

    /**
     * Create a new subscription for the given client.
     *
     * @throws ResourceNotFoundException if the client does not exist
     * @throws IllegalArgumentException  if endDate is not after startDate
     * @throws ConflictException         if an active subscription of the same type already exists
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

        // 4. Persist via domain model
        Subscription entity = subscriptionMapper.toEntity(request, client);
        SubscriptionDomain domain = subscriptionMapper.toDomain(entity); // wrap for potential pre-save logic
        Subscription saved = subscriptionRepository.save(domain.getEntity());

        log.info("Subscription created id={} for clientId={}", saved.getId(), clientId);
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
     *
     * @throws ResourceNotFoundException if no subscription with the given id exists
     */
    @Transactional
    public void deactivateSubscription(Long subscriptionId) {
        log.info("Deactivating subscription id={}", subscriptionId);

        Subscription entity = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + subscriptionId));

        // Delegate mutation to domain model
        SubscriptionDomain domain = subscriptionMapper.toDomain(entity);
        domain.deactivate();                           // entity.isActive = false
        subscriptionRepository.save(domain.getEntity());

        log.info("Subscription id={} deactivated", subscriptionId);
    }
}

