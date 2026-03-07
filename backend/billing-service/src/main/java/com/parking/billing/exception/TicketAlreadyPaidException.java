package com.parking.billing.exception;

/**
 * Exception thrown when attempting to pay for an already paid ticket.
 */
public class TicketAlreadyPaidException extends RuntimeException {

    public TicketAlreadyPaidException(String ticketCode) {
        super("Ticket already paid: " + ticketCode);
    }
}

