package com.parking.billing.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * Test configuration for JPA entities.
 * Configures package scanning for test context.
 */
@TestConfiguration
@EntityScan(basePackages = {"com.parking.billing.entity"})
public class TestConfig {
}

