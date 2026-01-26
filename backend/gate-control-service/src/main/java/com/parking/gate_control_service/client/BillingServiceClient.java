package com.parking.gate_control_service.client;

import com.parking.gate_control_service.dto.PaymentStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingServiceClient {
    private final WebClient billingServiceWebClient;

    public PaymentStatusResponse checkPaymentStatus(String ticketCode) {
        try {
            return billingServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/billing/status")
                            .queryParam("ticketCode", ticketCode)
                            .build())
                    .retrieve()
                    .bodyToMono(PaymentStatusResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Ticket not found: {}", ticketCode);
            return null;
        } catch (Exception e) {
            log.error("Error checking payment status for ticket {}: {}", ticketCode, e.getMessage());
            throw e;
        }
    }
}
