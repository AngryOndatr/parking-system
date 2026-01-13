package com.parking.reporting_service.mapper;

import com.parking.reporting_service.domain.LogDomain;
import com.parking.reporting_service.generated.model.LogRequest;
import com.parking.reporting_service.generated.model.LogResponse;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Mapper for converting between DTOs and Domain models.
 */
@Component
public class LogMapper {

    /**
     * Convert LogRequest DTO to LogDomain
     */
    public LogDomain toDomain(LogRequest request) {
        if (request == null) {
            return null;
        }

        LocalDateTime timestamp = request.getTimestamp() != null
                ? request.getTimestamp().toLocalDateTime()
                : LocalDateTime.now();

        // Handle JsonNullable fields
        String service = unwrapJsonNullable(request.getService());
        Long userId = unwrapJsonNullable(request.getUserId());
        Map<String, Object> meta = unwrapJsonNullable(request.getMeta());

        return new LogDomain(
                timestamp,
                request.getLevel().getValue(),
                service,
                request.getMessage(),
                userId,
                meta
        );
    }

    /**
     * Convert LogDomain to LogResponse DTO
     */
    public LogResponse toResponse(LogDomain domain) {
        if (domain == null) {
            return null;
        }

        LogResponse response = new LogResponse();
        response.setId(domain.getId());

        // Convert LocalDateTime to OffsetDateTime for API response
        if (domain.getTimestamp() != null) {
            response.setTimestamp(domain.getTimestamp().atOffset(ZoneOffset.UTC));
        }

        response.setLevel(domain.getLogLevel());
        response.setService(wrapJsonNullable(domain.getService()));
        response.setMessage(domain.getMessage());

        if (domain.getUserId() != null) {
            response.setUserId(JsonNullable.of(domain.getUserId()));
        }

        response.setMeta(wrapJsonNullable(domain.getMeta()));

        return response;
    }

    /**
     * Unwrap JsonNullable value, returning null if undefined or null
     */
    private <T> T unwrapJsonNullable(JsonNullable<T> nullable) {
        if (nullable == null || !nullable.isPresent()) {
            return null;
        }
        return nullable.get();
    }

    /**
     * Wrap value in JsonNullable
     */
    private <T> JsonNullable<T> wrapJsonNullable(T value) {
        return value == null ? JsonNullable.undefined() : JsonNullable.of(value);
    }
}

