package com.parking.gate_control_service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.gate_control_service.dto.SubscriptionCheckResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ClientServiceClient using MockWebServer.
 */
@DisplayName("ClientServiceClient Tests")
class ClientServiceClientTest {

    private MockWebServer mockWebServer;
    private ClientServiceClient clientServiceClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        clientServiceClient = new ClientServiceClient(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Check subscription - Success with active subscription")
    void checkSubscription_Success_ActiveSubscription() throws Exception {
        // Arrange
        String licensePlate = "ABC123";
        SubscriptionCheckResponse expectedResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(true)
                .clientId(100L)
                .subscriptionId(1L)
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .addHeader("Content-Type", "application/json"));

        // Act
        SubscriptionCheckResponse response = clientServiceClient.checkSubscription(licensePlate);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsAccessGranted()).isTrue();
        assertThat(response.getClientId()).isEqualTo(100L);
        assertThat(response.getSubscriptionId()).isEqualTo(1L);

        // Verify request
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/api/v1/clients/subscriptions/check");
        assertThat(request.getPath()).contains("licensePlate=" + licensePlate);
    }

    @Test
    @DisplayName("Check subscription - 404 Not Found returns access denied")
    void checkSubscription_NotFound_ReturnsAccessDenied() throws Exception {
        // Arrange
        String licensePlate = "XYZ789";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\":\"Subscription not found\"}")
                .addHeader("Content-Type", "application/json"));

        // Act
        SubscriptionCheckResponse response = clientServiceClient.checkSubscription(licensePlate);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsAccessGranted()).isFalse();
        assertThat(response.getClientId()).isNull();
        assertThat(response.getSubscriptionId()).isNull();

        // Verify request was made
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/api/v1/clients/subscriptions/check");
        assertThat(request.getPath()).contains("licensePlate=" + licensePlate);
    }

    @Test
    @DisplayName("Check subscription - No active subscription returns access denied")
    void checkSubscription_NoActiveSubscription_ReturnsAccessDenied() throws Exception {
        // Arrange
        String licensePlate = "DEF456";
        SubscriptionCheckResponse expectedResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(false)
                .clientId(200L)
                .subscriptionId(null)
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .addHeader("Content-Type", "application/json"));

        // Act
        SubscriptionCheckResponse response = clientServiceClient.checkSubscription(licensePlate);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsAccessGranted()).isFalse();
        assertThat(response.getClientId()).isEqualTo(200L);
        assertThat(response.getSubscriptionId()).isNull();
    }

    @Test
    @DisplayName("Check subscription - Server error returns access denied")
    void checkSubscription_ServerError_ReturnsAccessDenied() throws Exception {
        // Arrange
        String licensePlate = "GHI789";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\":\"Internal server error\"}"));

        // Act
        SubscriptionCheckResponse response = clientServiceClient.checkSubscription(licensePlate);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsAccessGranted()).isFalse();
        assertThat(response.getClientId()).isNull();
        assertThat(response.getSubscriptionId()).isNull();
    }

    @Test
    @DisplayName("Check subscription - Network timeout returns access denied")
    void checkSubscription_Timeout_ReturnsAccessDenied() throws IOException {
        // Arrange
        String licensePlate = "JKL012";
        // Don't enqueue any response - this will cause a timeout/error

        mockWebServer.shutdown(); // Force connection error

        // Act
        SubscriptionCheckResponse response = clientServiceClient.checkSubscription(licensePlate);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsAccessGranted()).isFalse();
    }
}
