package com.parking.client_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Local DTO for Vehicle requests with validation annotations.
 * Maps to generated VehicleCreateRequest/VehicleUpdateRequest from OpenAPI.
 */
public record VehicleRequestDto(
        @NotBlank(message = "License plate is required")
        String licensePlate,

        @NotNull(message = "Client ID is required")
        Long clientId,

        Boolean isAllowed
) {
    public VehicleRequestDto {
        // Default value for isAllowed if null
        if (isAllowed == null) {
            isAllowed = true;
        }
    }
}

