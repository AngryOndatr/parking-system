// Simple ApiError DTO for error responses
package com.parking.client_service.exception;

import java.time.OffsetDateTime;

public class ApiError {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;

    public ApiError() { this.timestamp = OffsetDateTime.now(); }

    public ApiError(int status, String error, String message) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    // getters and setters
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

