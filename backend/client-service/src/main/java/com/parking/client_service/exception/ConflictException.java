// Creating a small custom exception for HTTP 409 Conflict
package com.parking.client_service.exception;

public class ConflictException extends RuntimeException {
    public ConflictException() { super(); }
    public ConflictException(String message) { super(message); }
    public ConflictException(String message, Throwable cause) { super(message, cause); }
}

