package com.parking.gate_control_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient beans used for inter-service communication.
 * Each bean is configured to communicate with a specific microservice.
 */
@Configuration
public class WebClientConfig {

    @Value("${services.client.url}")
    private String clientServiceUrl;

    @Value("${services.billing.url}")
    private String billingServiceUrl;

    @Value("${services.management.url}")
    private String managementServiceUrl;

    @Value("${services.reporting.url}")
    private String reportingServiceUrl;

    /**
     * WebClient for communication with Client Service.
     * Used for vehicle and subscription verification.
     *
     * @return configured WebClient instance
     */
    @Bean
    public WebClient clientServiceWebClient() {
        return WebClient.builder()
                .baseUrl(clientServiceUrl)
                .build();
    }

    /**
     * WebClient for communication with Billing Service.
     * Used for payment verification and fee calculation.
     *
     * @return configured WebClient instance
     */
    @Bean
    public WebClient billingServiceWebClient() {
        return WebClient.builder()
                .baseUrl(billingServiceUrl)
                .build();
    }

    /**
     * WebClient for communication with Management Service.
     * Used for parking spot availability checks.
     *
     * @return configured WebClient instance
     */
    @Bean
    public WebClient managementServiceWebClient() {
        return WebClient.builder()
                .baseUrl(managementServiceUrl)
                .build();
    }

    /**
     * WebClient for communication with Reporting Service.
     * Used for logging gate events.
     *
     * @return configured WebClient instance
     */
    @Bean
    public WebClient reportingServiceWebClient() {
        return WebClient.builder()
                .baseUrl(reportingServiceUrl)
                .build();
    }
}
