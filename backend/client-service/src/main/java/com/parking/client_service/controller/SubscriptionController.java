package com.parking.client_service.controller;

import com.parking.client_service.generated.controller.SubscriptionManagementApi;
import com.parking.client_service.generated.model.SubscriptionRequest;
import com.parking.client_service.generated.model.SubscriptionResponse;
import com.parking.client_service.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for subscription management.
 * Implements the OpenAPI-generated {@link SubscriptionManagementApi} interface.
 *
 * Endpoints:
 *   POST   /api/clients/{clientId}/subscriptions  — create
 *   GET    /api/clients/{clientId}/subscriptions  — list
 *   DELETE /api/clients/subscriptions/{id}        — deactivate (soft delete)
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController implements SubscriptionManagementApi {

    private final SubscriptionService subscriptionService;

    @Override
    public ResponseEntity<SubscriptionResponse> createSubscription(
            Long clientId,
            @Valid SubscriptionRequest subscriptionRequest) {

        log.info("📋 [SUBSCRIPTION] POST /api/clients/{}/subscriptions type={}",
                clientId, subscriptionRequest.getType());

        SubscriptionResponse response =
                subscriptionService.createSubscription(clientId, subscriptionRequest);

        log.info("✅ [SUBSCRIPTION] Created id={} for clientId={}", response.getId(), clientId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByClient(Long clientId) {
        log.info("📋 [SUBSCRIPTION] GET /api/clients/{}/subscriptions", clientId);

        List<SubscriptionResponse> list =
                subscriptionService.getSubscriptionsByClient(clientId);

        log.info("✅ [SUBSCRIPTION] Returning {} subscriptions for clientId={}", list.size(), clientId);
        return ResponseEntity.ok(list);
    }

    @Override
    public ResponseEntity<Void> deactivateSubscription(Long id) {
        log.info("📋 [SUBSCRIPTION] DELETE /api/clients/subscriptions/{}", id);

        subscriptionService.deactivateSubscription(id);

        log.info("✅ [SUBSCRIPTION] Deactivated id={}", id);
        return ResponseEntity.noContent().build();
    }
}
