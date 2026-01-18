package com.parking.billing.exception;

/**
 * Exception thrown when a tariff is not found.
 */
public class TariffNotFoundException extends RuntimeException {

    public TariffNotFoundException(String tariffType) {
        super("Tariff not found for type: " + tariffType);
    }
}

