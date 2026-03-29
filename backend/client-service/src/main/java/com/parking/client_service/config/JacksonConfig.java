package com.parking.client_service.config;

import com.fasterxml.jackson.databind.Module;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson configuration for Client Service.
 * Registers JsonNullableModule so that OpenAPI-generated models with JsonNullable fields
 * are serialized correctly (e.g., JsonNullable.of(1L) → 1, not {"present":true}).
 *
 * Without this module, Jackson uses default bean serialization for JsonNullable,
 * producing {"present":true} instead of the actual value — which breaks inter-service
 * communication with gate-control-service and other consumers.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Module jsonNullableModule() {
        return new JsonNullableModule();
    }
}

