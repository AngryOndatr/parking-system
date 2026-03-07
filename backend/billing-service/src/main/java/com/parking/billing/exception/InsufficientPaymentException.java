package com.parking.billing.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when payment amount is insufficient.
 */
public class InsufficientPaymentException extends RuntimeException {

    public InsufficientPaymentException(BigDecimal expected, BigDecimal provided) {
        super(String.format("Insufficient payment amount. Expected: %.2f, Provided: %.2f",
            expected.doubleValue(), provided.doubleValue()));
    }

    public InsufficientPaymentException(String message) {
        super(message);
    }
}
