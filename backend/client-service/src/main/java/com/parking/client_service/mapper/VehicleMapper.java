package com.parking.client_service.mapper;

import com.parking.client_service.dto.VehicleRequestDto;
import com.parking.client_service.generated.model.VehicleCreateRequest;
import com.parking.client_service.generated.model.VehicleResponse;
import com.parking.client_service.generated.model.VehicleUpdateRequest;
import com.parking.common.entity.Client;
import com.parking.common.entity.Vehicle;
import org.springframework.stereotype.Component;

/**
 * Mapper for Vehicle entity and DTOs.
 */
@Component
public class VehicleMapper {

    /**
     * Map local VehicleRequestDto to Vehicle entity.
     */
    public Vehicle toEntity(VehicleRequestDto dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(dto.licensePlate());
        vehicle.setIsAllowed(dto.isAllowed());

        // Client will be set separately in service layer
        // vehicle.setClient(...)

        return vehicle;
    }

    /**
     * Map generated VehicleCreateRequest to Vehicle entity.
     */
    public Vehicle toEntity(VehicleCreateRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setIsAllowed(request.getIsAllowed() != null ? request.getIsAllowed() : true);

        // Client will be set separately in service layer based on clientId

        return vehicle;
    }

    /**
     * Map generated VehicleUpdateRequest to update existing Vehicle entity.
     */
    public void updateEntityFromRequest(Vehicle vehicle, VehicleUpdateRequest request) {
        if (request.getLicensePlate() != null) {
            vehicle.setLicensePlate(request.getLicensePlate());
        }
        if (request.getIsAllowed() != null) {
            vehicle.setIsAllowed(request.getIsAllowed());
        }
        // Client update will be handled separately if needed
    }

    /**
     * Map Vehicle entity to VehicleResponse.
     */
    public VehicleResponse toResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setLicensePlate(vehicle.getLicensePlate());
        response.setIsAllowed(vehicle.getIsAllowed());

        // Map client ID if client is present
        if (vehicle.getClient() != null) {
            response.setClientId(vehicle.getClient().getId());
        }

        return response;
    }
}

