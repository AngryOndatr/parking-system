package com.parking.gate_control_service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * Test configuration for JPA entities.
 * Configures package scanning for test context.
 */
@TestConfiguration
@EntityScan(basePackages = {"com.parking.gate_control_service.entity"})
public class TestConfig {
}
