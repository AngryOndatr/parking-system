package com.parking.client_service.controller;

import com.parking.common.entity.Vehicle;
import com.parking.client_service.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/vehicles")
@Slf4j
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(
            @PathVariable Long clientId,
            @RequestBody Vehicle vehicle) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [VEHICLE CONTROLLER] POST /api/clients/{}/vehicles - User: {}", 
                clientId, auth != null ? auth.getName() : "anonymous");
        
        try {
            Vehicle created = vehicleService.createVehicle(clientId, vehicle);
            log.info("✅ [VEHICLE CONTROLLER] Vehicle created with ID: {}", created.getId());
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            log.error("❌ [VEHICLE CONTROLLER] Error creating vehicle: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getVehiclesByClient(@PathVariable Long clientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [VEHICLE CONTROLLER] GET /api/clients/{}/vehicles - User: {}", 
                clientId, auth != null ? auth.getName() : "anonymous");
        
        List<Vehicle> vehicles = vehicleService.getVehiclesByClientId(clientId);
        log.info("✅ [VEHICLE CONTROLLER] Returning {} vehicles", vehicles.size());
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<Vehicle> getVehicleById(
            @PathVariable Long clientId,
            @PathVariable Long vehicleId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [VEHICLE CONTROLLER] GET /api/clients/{}/vehicles/{} - User: {}", 
                clientId, vehicleId, auth != null ? auth.getName() : "anonymous");
        
        return vehicleService.getVehicleById(vehicleId)
                .map(vehicle -> {
                    log.info("✅ [VEHICLE CONTROLLER] Vehicle found with ID: {}", vehicleId);
                    return ResponseEntity.ok(vehicle);
                })
                .orElseGet(() -> {
                    log.warn("❌ [VEHICLE CONTROLLER] Vehicle not found with ID: {}", vehicleId);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Long clientId,
            @PathVariable Long vehicleId,
            @RequestBody Vehicle vehicle) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [VEHICLE CONTROLLER] PUT /api/clients/{}/vehicles/{} - User: {}", 
                clientId, vehicleId, auth != null ? auth.getName() : "anonymous");
        
        try {
            Vehicle updated = vehicleService.updateVehicle(clientId, vehicleId, vehicle);
            log.info("✅ [VEHICLE CONTROLLER] Vehicle updated with ID: {}", updated.getId());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("❌ [VEHICLE CONTROLLER] Error updating vehicle: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable Long clientId,
            @PathVariable Long vehicleId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [VEHICLE CONTROLLER] DELETE /api/clients/{}/vehicles/{} - User: {}", 
                clientId, vehicleId, auth != null ? auth.getName() : "anonymous");
        
        try {
            vehicleService.deleteVehicle(clientId, vehicleId);
            log.info("✅ [VEHICLE CONTROLLER] Vehicle deleted with ID: {}", vehicleId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("❌ [VEHICLE CONTROLLER] Error deleting vehicle: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
