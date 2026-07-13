package com.parking.gate_control_service.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient beans used for inter-service communication.
 * Each bean is configured to communicate with a specific microservice.
 *
 * NOTE: The ObjectMapper for WebClient is created locally (NOT exposed as a @Bean)
 * so it does not override Spring Boot's auto-configured ObjectMapper used by Spring MVC.
 * Spring Boot's default ObjectMapper already registers JavaTimeModule, Jdk8Module,
 * and sets FAIL_ON_UNKNOWN_PROPERTIES=false — all of which are required for
 * proper serialization of OffsetDateTime in OpenAPI-generated response models.
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
     * Creates an ObjectMapper configured for WebClient inter-service communication.
     * This is intentionally NOT a @Bean — it is only used internally by WebClient
     * ExchangeStrategies so that Spring Boot's default ObjectMapper remains active
     * for Spring MVC request/response handling.
     *
     * @return configured ObjectMapper instance for WebClient
     */
    private ObjectMapper createWebClientObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new org.openapitools.jackson.nullable.JsonNullableModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    /**
     * ExchangeStrategies configured with JsonNullable-aware ObjectMapper.
     * Used by all WebClient instances.
     *
     * @return configured ExchangeStrategies
     */
    @Bean
    public ExchangeStrategies jsonNullableExchangeStrategies() {
        ObjectMapper objectMapper = createWebClientObjectMapper();
        return ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                })
                .build();
    }

    /**
     * WebClient for communication with Client Service.
     * Used for vehicle and subscription verification.
     *
     * @param exchangeStrategies configured exchange strategies
     * @return configured WebClient instance
     */
    @Bean
    public WebClient clientServiceWebClient(ExchangeStrategies exchangeStrategies) {
        return WebClient.builder()
                .baseUrl(clientServiceUrl)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    /**
     * WebClient for communication with Billing Service.
     * Used for payment verification and fee calculation.
     *
     * @param exchangeStrategies configured exchange strategies
     * @return configured WebClient instance
     */
    @Bean
    public WebClient billingServiceWebClient(ExchangeStrategies exchangeStrategies) {
        return WebClient.builder()
                .baseUrl(billingServiceUrl)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    /**
     * WebClient for communication with Management Service.
     * Used for parking spot availability checks.
     *
     * @param exchangeStrategies configured exchange strategies
     * @return configured WebClient instance
     */
    @Bean
    public WebClient managementServiceWebClient(ExchangeStrategies exchangeStrategies) {
        return WebClient.builder()
                .baseUrl(managementServiceUrl)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    /**
     * WebClient for communication with Reporting Service.
     * Used for logging gate events.
     *
     * @param exchangeStrategies configured exchange strategies
     * @return configured WebClient instance
     */
    @Bean
    public WebClient reportingServiceWebClient(ExchangeStrategies exchangeStrategies) {
        return WebClient.builder()
                .baseUrl(reportingServiceUrl)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
}
