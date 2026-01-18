package com.parking.billing.mapper;

import com.parking.billing.entity.Payment;
import com.parking.billing_service.generated.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * Mapper for converting between domain/entity models and OpenAPI-generated DTOs.
 */
@Component
public class BillingMapper {

    /**
     * Convert to FeeCalculationResponse.
     *
     * @param parkingEventId parking event ID
     * @param entryTime entry time
     * @param exitTime exit time
     * @param totalFee calculated total fee
     * @param tariffType tariff type applied
     * @return FeeCalculationResponse
     */
    public FeeCalculationResponse toFeeCalculationResponse(
            Long parkingEventId,
            OffsetDateTime entryTime,
            LocalDateTime exitTime,
            BigDecimal totalFee,
            String tariffType
    ) {
        LocalDateTime entryLocalTime = entryTime.atZoneSameInstant(ZoneOffset.systemDefault()).toLocalDateTime();

        // Calculate duration in minutes
        long durationMinutes = ChronoUnit.MINUTES.between(entryLocalTime, exitTime);

        // For this implementation, base fee equals total fee (no discounts yet)
        double baseFee = totalFee.doubleValue();
        double discount = 0.0;

        return new FeeCalculationResponse(
                parkingEventId,
                (int) durationMinutes,
                baseFee,
                discount,
                totalFee.doubleValue(),
                tariffType,
                OffsetDateTime.now()
        );
    }

    /**
     * Convert PaymentRequest.PaymentMethodEnum to Payment.PaymentMethod.
     *
     * @param paymentMethod OpenAPI payment method enum
     * @return entity payment method enum
     */
    public Payment.PaymentMethod toPaymentMethod(PaymentRequest.PaymentMethodEnum paymentMethod) {
        return switch (paymentMethod) {
            case CARD -> Payment.PaymentMethod.CARD;
            case CASH -> Payment.PaymentMethod.CASH;
            case MOBILE_PAY -> Payment.PaymentMethod.MOBILE_PAY;
        };
    }

    /**
     * Convert Payment entity to PaymentResponse.
     *
     * @param payment payment entity
     * @return PaymentResponse
     */
    public PaymentResponse toPaymentResponse(Payment payment) {
        OffsetDateTime paymentTime = payment.getPaymentTime() != null ?
                payment.getPaymentTime().atZone(ZoneOffset.systemDefault()).toOffsetDateTime() :
                OffsetDateTime.now();

        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getParkingEventId(),
                payment.getAmount().doubleValue(),
                toPaymentResponseStatus(payment.getStatus()),
                toPaymentResponseMethod(payment.getPaymentMethod()),
                paymentTime
        );

        // Set optional transactionId using JsonNullable
        if (payment.getTransactionId() != null) {
            response.setTransactionId(org.openapitools.jackson.nullable.JsonNullable.of(payment.getTransactionId()));
        }

        return response;
    }

    /**
     * Convert Payment.PaymentMethod to PaymentResponse.PaymentMethodEnum.
     *
     * @param paymentMethod entity payment method
     * @return OpenAPI payment method enum
     */
    private PaymentResponse.PaymentMethodEnum toPaymentResponseMethod(Payment.PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        return switch (paymentMethod) {
            case CARD -> PaymentResponse.PaymentMethodEnum.CARD;
            case CASH -> PaymentResponse.PaymentMethodEnum.CASH;
            case MOBILE_PAY -> PaymentResponse.PaymentMethodEnum.MOBILE_PAY;
        };
    }

    /**
     * Convert Payment.PaymentStatus to PaymentResponse.StatusEnum.
     *
     * @param status entity payment status
     * @return OpenAPI payment status enum
     */
    private PaymentResponse.StatusEnum toPaymentResponseStatus(Payment.PaymentStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> PaymentResponse.StatusEnum.PENDING;
            case COMPLETED -> PaymentResponse.StatusEnum.COMPLETED;
            case FAILED -> PaymentResponse.StatusEnum.FAILED;
            case REFUNDED -> PaymentResponse.StatusEnum.REFUNDED;
        };
    }

    /**
     * Convert to PaymentStatusResponse.
     *
     * @param parkingEventId parking event ID
     * @param isPaid whether the ticket is paid
     * @return PaymentStatusResponse
     */
    public PaymentStatusResponse toPaymentStatusResponse(Long parkingEventId, boolean isPaid) {
        // Create response with required parameters (empty payments list for now)
        return new PaymentStatusResponse(
                parkingEventId,
                isPaid,
                new java.util.ArrayList<>()  // Empty list, can be populated later with actual payment history
        );
    }
}

