package com.parking.gate_control_service.client;

import com.parking.gate_control_service.dto.PaymentStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingServiceClient {
    private final WebClient billingServiceWebClient;

    public PaymentStatusResponse checkPaymentStatus(Long parkingEventId) {
        try {
            log.info("Checking payment status for parking event ID: {}", parkingEventId);

            // Deserialize to Map first to handle JsonNullable fields
            Map<String, Object> responseMap = billingServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/billing/status")
                            .queryParam("parkingEventId", parkingEventId)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseMap == null) {
                log.warn("Received null response from billing service");
                return null;
            }

            // Manually extract values, handling JsonNullable wrapper
            Boolean isPaid = (Boolean) responseMap.get("isPaid");
            Object remainingFeeObj = responseMap.get("remainingFee");

            BigDecimal remainingFee = null;
            if (remainingFeeObj != null) {
                if (remainingFeeObj instanceof Number) {
                    remainingFee = new BigDecimal(remainingFeeObj.toString());
                } else if (remainingFeeObj instanceof Map) {
                    // Handle JsonNullable structure: {"present": true, "value": 0.0}
                    Map<String, Object> nullableMap = (Map<String, Object>) remainingFeeObj;
                    Object value = nullableMap.get("value");
                    if (value instanceof Number) {
                        remainingFee = new BigDecimal(value.toString());
                    }
                }
            }

            log.info("Parsed payment status: isPaid={}, remainingFee={}", isPaid, remainingFee);

            return PaymentStatusResponse.builder()
                    .isPaid(isPaid)
                    .remainingFee(remainingFee)
                    .build();

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Parking event not found: {}", parkingEventId);
            return null;
        } catch (Exception e) {
            log.error("Error checking payment status for parking event {}: {}", parkingEventId, e.getMessage(), e);
            // Return null instead of throwing exception to allow graceful handling
            return null;
        }
    }

    /**
     * Check payment status by ticket code instead of parking event ID.
     * This is used when only ticket code is available (e.g., one-time visitors).
     *
     * @param ticketCode the ticket code
     * @return payment status response or null if error occurs
     */
    public PaymentStatusResponse checkPaymentStatusByTicket(String ticketCode) {
        try {
            log.info("Checking payment status for ticket code: {}", ticketCode);

            // Deserialize to Map first to handle JsonNullable fields
            Map<String, Object> responseMap = billingServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/billing/status-by-ticket")
                            .queryParam("ticketCode", ticketCode)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseMap == null) {
                log.warn("Received null response from billing service for ticket: {}", ticketCode);
                return null;
            }

            // Manually extract values, handling JsonNullable wrapper
            Boolean isPaid = (Boolean) responseMap.get("isPaid");
            Object remainingFeeObj = responseMap.get("remainingFee");

            BigDecimal remainingFee = null;
            if (remainingFeeObj != null) {
                if (remainingFeeObj instanceof Number) {
                    remainingFee = new BigDecimal(remainingFeeObj.toString());
                } else if (remainingFeeObj instanceof Map) {
                    Map<String, Object> nullableMap = (Map<String, Object>) remainingFeeObj;
                    Object value = nullableMap.get("value");
                    if (value instanceof Number) {
                        remainingFee = new BigDecimal(value.toString());
                    }
                }
            }

            log.info("Parsed payment status for ticket {}: isPaid={}, remainingFee={}", ticketCode, isPaid, remainingFee);

            return PaymentStatusResponse.builder()
                    .isPaid(isPaid)
                    .remainingFee(remainingFee)
                    .build();

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Ticket not found: {}", ticketCode);
            return null;
        } catch (Exception e) {
            log.error("Error checking payment status for ticket {}: {}", ticketCode, e.getMessage(), e);
            // Return null instead of throwing exception to allow graceful handling
            return null;
        }
    }
}
