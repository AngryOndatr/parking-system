package com.parking.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// NOTE:
// Test configuration properties were moved to
// src/test/resources/application-test.properties
// This file disables Flyway and OpenTelemetry for test profile,
// uses an in-memory H2 database and instructs Hibernate to create-drop schema.
// Keeping these settings in a properties file keeps the test annotation clean
// and makes it easier to manage multiple test properties in future.

@SpringBootTest
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
