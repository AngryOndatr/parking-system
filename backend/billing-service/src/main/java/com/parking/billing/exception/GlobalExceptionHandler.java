package com.parking.billing.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 * Handles all exceptions and converts them to proper HTTP responses with error details.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle ParkingEventNotFoundException - returns 404 Not Found
     */
    @ExceptionHandler(ParkingEventNotFoundException.class)
    protected ResponseEntity<Object> handleParkingEventNotFound(ParkingEventNotFoundException ex, WebRequest request) {
        log.error("Parking event not found: {}", ex.getMessage());
        ApiError apiError = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    /**
     * Handle TicketAlreadyPaidException - returns 400 Bad Request
     */
    @ExceptionHandler(TicketAlreadyPaidException.class)
    protected ResponseEntity<Object> handleTicketAlreadyPaid(TicketAlreadyPaidException ex, WebRequest request) {
        log.error("Ticket already paid: {}", ex.getMessage());
        ApiError apiError = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    /**
     * Handle InsufficientPaymentException - returns 400 Bad Request
     */
    @ExceptionHandler(InsufficientPaymentException.class)
    protected ResponseEntity<Object> handleInsufficientPayment(InsufficientPaymentException ex, WebRequest request) {
        log.error("Insufficient payment: {}", ex.getMessage());
        ApiError apiError = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    /**
     * Handle TariffNotFoundException - returns 404 Not Found
     */
    @ExceptionHandler(TariffNotFoundException.class)
    protected ResponseEntity<Object> handleTariffNotFound(TariffNotFoundException ex, WebRequest request) {
        log.error("Tariff not found: {}", ex.getMessage());
        ApiError apiError = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    /**
     * Handle IllegalArgumentException - returns 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());
        ApiError apiError = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    /**
     * Handle validation errors - returns 400 Bad Request
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String messages = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.error("Validation error: {}", messages);

        ApiError apiError = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                messages,
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.badRequest().body(apiError);
    }

    /**
     * Handle all other exceptions - returns 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        ApiError apiError = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
