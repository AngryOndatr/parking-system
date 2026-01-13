package com.parking.client_service.controller;

import com.parking.client_service.exception.ResourceNotFoundException;
import com.parking.client_service.generated.controller.VehicleApi;
import com.parking.client_service.generated.model.VehicleCreateRequest;
import com.parking.client_service.generated.model.VehicleResponse;
import com.parking.client_service.generated.model.VehicleUpdateRequest;
import com.parking.client_service.service.VehicleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for vehicle management operations.
 * Implements the generated VehicleApi interface from OpenAPI.
 */
@RestController
public class VehicleController implements VehicleApi {

    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Override
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        log.info("🚗 [VEHICLE CONTROLLER] GET /api/vehicles - User: {}", username);

        List<VehicleResponse> vehicles = vehicleService.findAllVehicles();
        log.info("✅ [VEHICLE CONTROLLER] Returning {} vehicles", vehicles.size());
        return ResponseEntity.ok(vehicles);
    }

    @Override
    public ResponseEntity<VehicleResponse> createVehicle(@Valid VehicleCreateRequest vehicleCreateRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        log.info("🚗 [VEHICLE CONTROLLER] POST /api/vehicles - User: {}", username);

        VehicleResponse created = vehicleService.createVehicle(vehicleCreateRequest);
        log.info("✅ [VEHICLE CONTROLLER] Vehicle created with id: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<VehicleResponse> getVehicleById(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        log.info("🚗 [VEHICLE CONTROLLER] GET /api/vehicles/{} - User: {}", id, username);

        return vehicleService.findVehicleById(id)
                .map(vehicle -> {
                    log.info("✅ [VEHICLE CONTROLLER] Vehicle found with id: {}", id);
                    return ResponseEntity.ok(vehicle);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ [VEHICLE CONTROLLER] Vehicle not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Override
    public ResponseEntity<VehicleResponse> updateVehicle(Long id, @Valid VehicleUpdateRequest vehicleUpdateRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        log.info("🚗 [VEHICLE CONTROLLER] PUT /api/vehicles/{} - User: {}", id, username);

        try {
            VehicleResponse updated = vehicleService.updateVehicle(id, vehicleUpdateRequest);
            log.info("✅ [VEHICLE CONTROLLER] Vehicle updated with id: {}", id);
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException e) {
            log.warn("⚠️ [VEHICLE CONTROLLER] Vehicle not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteVehicle(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        log.info("🚗 [VEHICLE CONTROLLER] DELETE /api/vehicles/{} - User: {}", id, username);

        try {
            vehicleService.deleteVehicle(id);
            log.info("✅ [VEHICLE CONTROLLER] Vehicle deleted with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.warn("⚠️ [VEHICLE CONTROLLER] Vehicle not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }


    @Override
    public ResponseEntity<List<VehicleResponse>> getVehiclesByClient(Long clientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        log.info("🚗 [VEHICLE CONTROLLER] GET /api/clients/{}/vehicles - User: {}", clientId, username);

        List<VehicleResponse> vehicles = vehicleService.findVehiclesByClientId(clientId);
        log.info("✅ [VEHICLE CONTROLLER] Returning {} vehicles for client {}", vehicles.size(), clientId);
        return ResponseEntity.ok(vehicles);
    }

    @Override
    public ResponseEntity<VehicleResponse> addVehicleToClient(Long clientId, @Valid com.parking.client_service.generated.model.VehicleRequest vehicleRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        log.info("🚗 [VEHICLE CONTROLLER] POST /api/clients/{}/vehicles - User: {}", clientId, username);

        // Create VehicleCreateRequest from VehicleRequest and clientId
        VehicleCreateRequest createRequest = new VehicleCreateRequest();
        createRequest.setLicensePlate(vehicleRequest.getLicensePlate());
        createRequest.setClientId(clientId);
        createRequest.setIsAllowed(true); // Default value

        VehicleResponse created = vehicleService.createVehicle(createRequest);
        log.info("✅ [VEHICLE CONTROLLER] Vehicle added to client {} with id: {}", clientId, created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

