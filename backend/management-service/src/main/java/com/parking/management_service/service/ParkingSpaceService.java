package com.parking.management_service.service;

import com.parking.common.entity.ParkingSpace;
import com.parking.management_service.domain.ParkingSpaceDomain;
import com.parking.management_service.generated.model.ParkingSpaceResponse;
import com.parking.management_service.mapper.ParkingSpaceMapper;
import com.parking.management_service.repository.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing parking spaces
 * Provides business logic for parking space operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ParkingSpaceService {

    private final ParkingSpaceRepository repository;
    private final ParkingSpaceMapper mapper;

    /**
     * Get all available parking spaces
     */
    public List<ParkingSpaceResponse> getAvailableSpaces() {
        log.debug("Finding all available parking spaces");
        List<ParkingSpace> spaces = repository.findByStatus("AVAILABLE");
        return spaces.stream()
                .map(ParkingSpaceDomain::new)
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available parking spaces for a specific lot
     */
    public List<ParkingSpaceResponse> getAvailableSpacesByLot(Long lotId) {
        log.debug("Finding available spaces for lot: {}", lotId);
        List<ParkingSpace> spaces = repository.findByParkingLotIdAndStatus(lotId, "AVAILABLE");
        return spaces.stream()
                .map(ParkingSpaceDomain::new)
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all parking spaces regardless of status
     */
    public List<ParkingSpaceResponse> getAllSpaces() {
        log.debug("Finding all parking spaces");
        List<ParkingSpace> spaces = repository.findAll();
        return spaces.stream()
                .map(ParkingSpaceDomain::new)
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get count of available parking spaces
     */
    public long getAvailableSpacesCount() {
        log.debug("Counting available parking spaces");
        return repository.countByStatus("AVAILABLE");
    }

    /**
     * Search parking spaces by type and/or status
     */
    public List<ParkingSpaceResponse> searchSpaces(String type, String status) {
        log.debug("Searching spaces - type: {}, status: {}", type, status);

        List<ParkingSpace> spaces;

        if (type != null && status != null) {
            spaces = repository.findBySpaceTypeAndStatus(type, status);
        } else if (type != null) {
            spaces = repository.findBySpaceType(type);
        } else if (status != null) {
            spaces = repository.findByStatus(status);
        } else {
            spaces = repository.findAll();
        }

        return spaces.stream()
                .map(ParkingSpaceDomain::new)
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}

