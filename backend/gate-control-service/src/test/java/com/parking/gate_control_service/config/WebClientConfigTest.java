package com.parking.gate_control_service.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for WebClientConfig.
 * Verifies that all WebClient beans are properly configured and accessible.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("WebClientConfig Integration Tests")
class WebClientConfigTest {

    @Autowired
    private WebClient clientServiceWebClient;

    @Autowired
    private WebClient billingServiceWebClient;

    @Autowired
    private WebClient managementServiceWebClient;

    @Autowired
    private WebClient reportingServiceWebClient;

    @Test
    @DisplayName("Should inject clientServiceWebClient bean")
    void testClientServiceWebClient_NotNull() {
        // Then
        assertThat(clientServiceWebClient).isNotNull();
    }

    @Test
    @DisplayName("Should inject billingServiceWebClient bean")
    void testBillingServiceWebClient_NotNull() {
        // Then
        assertThat(billingServiceWebClient).isNotNull();
    }

    @Test
    @DisplayName("Should inject managementServiceWebClient bean")
    void testManagementServiceWebClient_NotNull() {
        // Then
        assertThat(managementServiceWebClient).isNotNull();
    }

    @Test
    @DisplayName("Should inject reportingServiceWebClient bean")
    void testReportingServiceWebClient_NotNull() {
        // Then
        assertThat(reportingServiceWebClient).isNotNull();
    }

    @Test
    @DisplayName("Should have all four WebClient beans configured")
    void testAllWebClientBeans_Configured() {
        // Then
        assertThat(clientServiceWebClient).isNotNull();
        assertThat(billingServiceWebClient).isNotNull();
        assertThat(managementServiceWebClient).isNotNull();
        assertThat(reportingServiceWebClient).isNotNull();
    }
}
