package com.parking.gate_control_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the response from Client Service for subscription validation.
 * Contains information about whether access should be granted based on subscription status.
 *
 * Uses @JsonIgnoreProperties to safely handle extra fields returned by client-service
 * (e.g., licensePlate, message) that this DTO doesn't need.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionCheckResponse {

    /**
     * Indicates whether the vehicle has access based on subscription.
     */
    private Boolean isAccessGranted;

    /**
     * Client ID if subscription exists (nullable).
     */
    private Long clientId;

    /**
     * Subscription ID if active subscription exists (nullable).
     * Plain Long — client-service now serializes JsonNullable properly via JsonNullableModule.
     */
    private Long subscriptionId;
}
