package com.parking.billing.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Standard API error response DTO.
 * Used to provide consistent error information to API clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ApiError(int status, String error, String message) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
