package com.parking.client_service.service;

import com.parking.client_service.dto.VehicleRequestDto;
import com.parking.client_service.exception.ConflictException;
import com.parking.client_service.exception.ResourceNotFoundException;
import com.parking.client_service.generated.model.VehicleCreateRequest;
import com.parking.client_service.generated.model.VehicleResponse;
import com.parking.client_service.generated.model.VehicleUpdateRequest;
import com.parking.client_service.mapper.VehicleMapper;
import com.parking.client_service.repository.ClientRepository;
import com.parking.client_service.repository.VehicleRepository;
import com.parking.common.domain.VehicleDomain;
import com.parking.common.entity.Client;
import com.parking.common.entity.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;
    private final VehicleMapper vehicleMapper;

    public VehicleService(VehicleRepository vehicleRepository,
                         ClientRepository clientRepository,
                         VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.clientRepository = clientRepository;
        this.vehicleMapper = vehicleMapper;
    }

    @Transactional
    public VehicleResponse createVehicle(VehicleRequestDto requestDto) {
        log.info("Creating new vehicle with license plate: {}", requestDto.licensePlate());

        // Check if license plate already exists
        if (vehicleRepository.findByLicensePlate(requestDto.licensePlate()).isPresent()) {
            log.warn("Failed to create vehicle - license plate already in use: {}", requestDto.licensePlate());
            throw new ConflictException("License plate already in use");
        }

        // Verify client exists
        Client client = clientRepository.findById(requestDto.clientId())
                .orElseThrow(() -> {
                    log.warn("Failed to create vehicle - client not found with id: {}", requestDto.clientId());
                    return new ResourceNotFoundException("Client not found with id: " + requestDto.clientId());
                });

        // Map DTO -> Entity
        Vehicle vehicle = vehicleMapper.toEntity(requestDto);
        vehicle.setClient(client);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully created vehicle with id: {}, license plate: {}, for client: {}",
                savedVehicle.getId(), savedVehicle.getLicensePlate(), client.getId());

        return vehicleMapper.toResponse(savedVehicle);
    }

    @Transactional
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        log.info("Creating new vehicle (via generated API) with license plate: {}", request.getLicensePlate());

        // Check if license plate already exists
        if (vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            log.warn("Failed to create vehicle - license plate already in use: {}", request.getLicensePlate());
            throw new ConflictException("License plate already in use");
        }

        // Verify client exists
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> {
                    log.warn("Failed to create vehicle - client not found with id: {}", request.getClientId());
                    return new ResourceNotFoundException("Client not found with id: " + request.getClientId());
                });

        // Map generated request -> Entity
        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle.setClient(client);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully created vehicle with id: {}, license plate: {}, for client: {}",
                savedVehicle.getId(), savedVehicle.getLicensePlate(), client.getId());

        return vehicleMapper.toResponse(savedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAllVehicles() {
        log.debug("Retrieving all vehicles");
        List<VehicleResponse> vehicles = vehicleRepository.findAll().stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
        log.debug("Found {} vehicles", vehicles.size());
        return vehicles;
    }

    @Transactional(readOnly = true)
    public Optional<VehicleResponse> findVehicleById(Long id) {
        log.debug("Searching for vehicle with id: {}", id);
        Optional<VehicleResponse> result = vehicleRepository.findById(id)
                .map(vehicleMapper::toResponse);
        if (result.isPresent()) {
            log.debug("Vehicle found with id: {}", id);
        } else {
            log.debug("Vehicle not found with id: {}", id);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> findVehiclesByClientId(Long clientId) {
        log.debug("Retrieving vehicles for client id: {}", clientId);
        List<VehicleResponse> vehicles = vehicleRepository.findByClientId(clientId).stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
        log.debug("Found {} vehicles for client id: {}", vehicles.size(), clientId);
        return vehicles;
    }

    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequestDto requestDto) {
        log.info("Updating vehicle with id: {}", id);

        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to update - vehicle not found with id: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with id: " + id);
                });

        // Wrap entity in domain for business operations
        VehicleDomain domain = new VehicleDomain(existing);

        // Check license plate uniqueness (if changing)
        if (!objectsEqual(domain.getLicensePlate(), requestDto.licensePlate())) {
            log.debug("License plate change detected for vehicle id: {}", id);
            vehicleRepository.findByLicensePlate(requestDto.licensePlate()).ifPresent(v -> {
                log.warn("Failed to update vehicle {} - license plate already in use: {}", id, requestDto.licensePlate());
                throw new ConflictException("License plate already in use");
            });
        }

        // Check if client exists (if changing)
        if (!objectsEqual(domain.getClient() != null ? domain.getClient().getId() : null, requestDto.clientId())) {
            log.debug("Client change detected for vehicle id: {}", id);
            Client newClient = clientRepository.findById(requestDto.clientId())
                    .orElseThrow(() -> {
                        log.warn("Failed to update vehicle {} - client not found with id: {}", id, requestDto.clientId());
                        return new ResourceNotFoundException("Client not found with id: " + requestDto.clientId());
                    });
            domain.setClient(newClient);
        }

        // Apply changes via domain
        domain.setLicensePlate(requestDto.licensePlate());
        if (requestDto.isAllowed() != null) {
            domain.setIsAllowed(requestDto.isAllowed());
        }

        Vehicle saved = vehicleRepository.save(domain.getEntity());
        log.info("Successfully updated vehicle with id: {}, license plate: {}", saved.getId(), saved.getLicensePlate());

        return vehicleMapper.toResponse(saved);
    }

    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request) {
        log.info("Updating vehicle (via generated API) with id: {}", id);

        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to update - vehicle not found with id: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with id: " + id);
                });

        VehicleDomain domain = new VehicleDomain(existing);

        // Check license plate uniqueness (if changing)
        if (request.getLicensePlate() != null &&
            !objectsEqual(domain.getLicensePlate(), request.getLicensePlate())) {
            log.debug("License plate change detected for vehicle id: {}", id);
            vehicleRepository.findByLicensePlate(request.getLicensePlate()).ifPresent(v -> {
                log.warn("Failed to update vehicle {} - license plate already in use: {}", id, request.getLicensePlate());
                throw new ConflictException("License plate already in use");
            });
            domain.setLicensePlate(request.getLicensePlate());
        }

        // Check if client exists (if changing)
        if (request.getClientId() != null &&
            !objectsEqual(domain.getClient() != null ? domain.getClient().getId() : null, request.getClientId())) {
            log.debug("Client change detected for vehicle id: {}", id);
            Client newClient = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> {
                        log.warn("Failed to update vehicle {} - client not found with id: {}", id, request.getClientId());
                        return new ResourceNotFoundException("Client not found with id: " + request.getClientId());
                    });
            domain.setClient(newClient);
        }

        // Apply isAllowed change if provided
        if (request.getIsAllowed() != null) {
            domain.setIsAllowed(request.getIsAllowed());
        }

        Vehicle saved = vehicleRepository.save(domain.getEntity());
        log.info("Successfully updated vehicle with id: {}, license plate: {}", saved.getId(), saved.getLicensePlate());

        return vehicleMapper.toResponse(saved);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with id: {}", id);

        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to delete - vehicle not found with id: {}", id);
                    return new ResourceNotFoundException("Vehicle not found with id: " + id);
                });

        vehicleRepository.delete(existing);
        log.info("Successfully deleted vehicle with id: {}", id);
    }

    // Helper method for null-safe equality
    private boolean objectsEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}

