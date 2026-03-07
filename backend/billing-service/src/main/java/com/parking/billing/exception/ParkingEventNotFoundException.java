package com.parking.billing.exception;

/**
 * Exception thrown when a parking event is not found.
 */
public class ParkingEventNotFoundException extends RuntimeException {

    public ParkingEventNotFoundException(String ticketCode) {
        super("Parking event not found with ticket code: " + ticketCode);
    }
}

