package com.parking.gate_control_service.client;

import com.parking.gate_control_service.dto.PaymentStatusResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BillingServiceClientTest {
    private static MockWebServer mockWebServer;
    private static BillingServiceClient billingServiceClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        billingServiceClient = new BillingServiceClient(webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void checkPaymentStatus_Paid_ReturnsIsPaidTrue() {
        String ticketCode = "TICKET-123";
        String responseJson = "{\"isPaid\":true,\"remainingFee\":0}";
        mockWebServer.enqueue(new MockResponse().setBody(responseJson).addHeader("Content-Type", "application/json"));

        PaymentStatusResponse response = billingServiceClient.checkPaymentStatus(ticketCode);
        assertThat(response).isNotNull();
        assertThat(response.getIsPaid()).isTrue();
        assertThat(response.getRemainingFee()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void checkPaymentStatus_NotPaid_ReturnsIsPaidFalse() {
        String ticketCode = "TICKET-456";
        String responseJson = "{\"isPaid\":false,\"remainingFee\":15.5}";
        mockWebServer.enqueue(new MockResponse().setBody(responseJson).addHeader("Content-Type", "application/json"));

        PaymentStatusResponse response = billingServiceClient.checkPaymentStatus(ticketCode);
        assertThat(response).isNotNull();
        assertThat(response.getIsPaid()).isFalse();
        assertThat(response.getRemainingFee()).isEqualTo(new BigDecimal("15.5"));
    }

    @Test
    void checkPaymentStatus_TicketNotFound_ReturnsNull() {
        String ticketCode = "TICKET-404";
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        PaymentStatusResponse response = billingServiceClient.checkPaymentStatus(ticketCode);
        assertThat(response).isNull();
    }
}
