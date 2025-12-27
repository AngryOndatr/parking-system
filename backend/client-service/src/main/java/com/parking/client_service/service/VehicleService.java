package com.parking.client_service.service;

import com.parking.common.entity.Client;
import com.parking.common.entity.Vehicle;
import com.parking.client_service.repository.ClientRepository;
import com.parking.client_service.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public Vehicle createVehicle(Long clientId, Vehicle vehicle) {
        log.info("Creating vehicle for client ID: {}", clientId);
        
        // Validate client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));
        
        // Check if license plate already exists
        if (vehicleRepository.findByLicensePlate(vehicle.getLicensePlate()).isPresent()) {
            throw new RuntimeException("Vehicle with license plate already exists: " + vehicle.getLicensePlate());
        }
        
        vehicle.setClient(client);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created with ID: {}", savedVehicle.getId());
        return savedVehicle;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByClientId(Long clientId) {
        log.info("Fetching vehicles for client ID: {}", clientId);
        return vehicleRepository.findByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public Optional<Vehicle> getVehicleById(Long vehicleId) {
        log.info("Fetching vehicle with ID: {}", vehicleId);
        return vehicleRepository.findById(vehicleId);
    }

    @Transactional
    public Vehicle updateVehicle(Long clientId, Long vehicleId, Vehicle vehicleUpdate) {
        log.info("Updating vehicle ID: {} for client ID: {}", vehicleId, clientId);
        
        Vehicle existingVehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
        
        // Verify the vehicle belongs to the client
        if (!existingVehicle.getClient().getId().equals(clientId)) {
            throw new RuntimeException("Vehicle does not belong to client");
        }
        
        // Update fields
        if (vehicleUpdate.getLicensePlate() != null) {
            // Check if new license plate already exists
            Optional<Vehicle> existingByPlate = vehicleRepository.findByLicensePlate(vehicleUpdate.getLicensePlate());
            if (existingByPlate.isPresent() && !existingByPlate.get().getId().equals(vehicleId)) {
                throw new RuntimeException("License plate already in use");
            }
            existingVehicle.setLicensePlate(vehicleUpdate.getLicensePlate());
        }
        
        if (vehicleUpdate.getIsAllowed() != null) {
            existingVehicle.setIsAllowed(vehicleUpdate.getIsAllowed());
        }
        
        Vehicle updated = vehicleRepository.save(existingVehicle);
        log.info("Vehicle updated: {}", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteVehicle(Long clientId, Long vehicleId) {
        log.info("Deleting vehicle ID: {} for client ID: {}", vehicleId, clientId);
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
        
        // Verify the vehicle belongs to the client
        if (!vehicle.getClient().getId().equals(clientId)) {
            throw new RuntimeException("Vehicle does not belong to client");
        }
        
        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted: {}", vehicleId);
    }
}
