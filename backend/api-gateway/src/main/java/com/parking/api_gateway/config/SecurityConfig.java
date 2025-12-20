package com.parking.api_gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;

/**
 * Конфигурация безопасности для API Gateway
 * Полностью отключает Spring Security автоконфигурацию
 */
@Configuration
@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    ReactiveSecurityAutoConfiguration.class
})
public class SecurityConfig {
    // Пустой класс для отключения Security
}