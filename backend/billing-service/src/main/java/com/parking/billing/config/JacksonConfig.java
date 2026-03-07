package com.parking.billing.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson configuration for JSON serialization/deserialization.
 * Registers JsonNullableModule to support OpenAPI JsonNullable types.
 */
@Configuration
public class JacksonConfig {

    /**
     * Configure ObjectMapper with JsonNullable module support.
     * Does not depend on Jackson2ObjectMapperBuilder so it works in all
     * Spring test slices (@DataJpaTest, @WebMvcTest, etc.).
     *
     * @return configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JsonNullableModule());
        return objectMapper;
    }
}

