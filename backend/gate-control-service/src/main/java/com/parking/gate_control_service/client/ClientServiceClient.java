package com.parking.gate_control_service.client;

import com.parking.gate_control_service.dto.SubscriptionCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Client for communication with Client Service.
 * Handles subscription validation and vehicle verification.
 */
@Slf4j
@Component
public class ClientServiceClient {

    private final WebClient clientServiceWebClient;

    public ClientServiceClient(@Qualifier("clientServiceWebClient") WebClient clientServiceWebClient) {
        this.clientServiceWebClient = clientServiceWebClient;
    }

    /**
     * Checks if a vehicle has an active subscription allowing access.
     *
     * @param licensePlate the vehicle's license plate
     * @return SubscriptionCheckResponse with access decision
     */
    public SubscriptionCheckResponse checkSubscription(String licensePlate) {
        log.info("Checking subscription for license plate: {}", licensePlate);

        try {
            SubscriptionCheckResponse response = clientServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/clients/subscriptions/check")
                            .queryParam("licensePlate", licensePlate)
                            .build())
                    .retrieve()
                    .bodyToMono(SubscriptionCheckResponse.class)
                    .block();

            log.info("Subscription check result for {}: accessGranted={}",
                    licensePlate, response != null ? response.getIsAccessGranted() : null);

            return response;
        } catch (WebClientResponseException.NotFound e) {
            log.warn("No subscription found for license plate: {}", licensePlate);
            return SubscriptionCheckResponse.builder()
                    .isAccessGranted(false)
                    .build();
        } catch (Exception e) {
            log.error("Error checking subscription for {}: {}", licensePlate, e.getMessage(), e);
            return SubscriptionCheckResponse.builder()
                    .isAccessGranted(false)
                    .build();
        }
    }
}
