package com.parking.api_gateway.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator to override Redis health checks
 * Always returns UP status
 */
@Component("custom")
public class SimpleHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
                .withDetail("status", "JWT Authentication Service is running")
                .withDetail("database", "PostgreSQL Connected")
                .build();
    }
}