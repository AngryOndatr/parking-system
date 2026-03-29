package com.parking.client_service.controller;

import com.parking.client_service.generated.controller.SubscriptionApi;
import com.parking.client_service.generated.model.SubscriptionCheckResponse;
import com.parking.client_service.repository.SubscriptionRepository;
import com.parking.common.entity.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Implements the OpenAPI-generated SubscriptionApi interface.
 * Returns real DB result: isAccessGranted=true when an active subscription
 * exists for the given license plate.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class SubscriptionCheckController implements SubscriptionApi {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public ResponseEntity<SubscriptionCheckResponse> checkSubscription(String licensePlate) {

        log.info("🔍 [SUBSCRIPTION CHECK] Checking subscription for license plate: {}", licensePlate);

        Optional<Subscription> subscription =
                subscriptionRepository.findActiveByLicensePlate(licensePlate);

        SubscriptionCheckResponse response = new SubscriptionCheckResponse();
        response.setLicensePlate(licensePlate);

        if (subscription.isPresent()) {
            response.setIsAccessGranted(true);
            response.setSubscriptionId(JsonNullable.of(subscription.get().getId()));
            response.setMessage("Active subscription found");
            log.info("✅ [SUBSCRIPTION CHECK] Active subscription #{} found for {}",
                    subscription.get().getId(), licensePlate);
        } else {
            response.setIsAccessGranted(false);
            response.setSubscriptionId(JsonNullable.undefined());
            response.setMessage("No active subscription found");
            log.info("ℹ️ [SUBSCRIPTION CHECK] No active subscription for {}", licensePlate);
        }

        return ResponseEntity.ok(response);
    }
}

