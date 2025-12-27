package com.parking.management_service.service;

import com.parking.common.entity.ParkingLot;
import com.parking.common.entity.ParkingSpace;
import com.parking.management_service.repository.ParkingLotRepository;
import com.parking.management_service.repository.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementService {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ParkingLotRepository parkingLotRepository;

    /**
     * Get available parking spaces
     */
    @Transactional(readOnly = true)
    public List<ParkingSpace> getAvailableSpaces() {
        log.info("Fetching all available parking spaces");
        return parkingSpaceRepository.findByStatus("AVAILABLE");
    }

    /**
     * Get available parking spaces by parking lot
     */
    @Transactional(readOnly = true)
    public List<ParkingSpace> getAvailableSpacesByLot(Long parkingLotId) {
        log.info("Fetching available spaces for parking lot: {}", parkingLotId);
        return parkingSpaceRepository.findByParkingLotIdAndStatus(parkingLotId, "AVAILABLE");
    }

    /**
     * Get availability summary for all parking lots
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAvailabilitySummary() {
        log.info("Fetching availability summary");
        
        List<ParkingLot> parkingLots = parkingLotRepository.findAll();
        Map<String, Object> summary = new HashMap<>();
        
        int totalSpaces = 0;
        int totalAvailable = 0;
        
        for (ParkingLot lot : parkingLots) {
            Long availableCount = parkingSpaceRepository.countAvailableSpacesByParkingLotId(lot.getId());
            totalSpaces += lot.getTotalSpaces();
            totalAvailable += availableCount.intValue();
            
            // Update the parking lot's available spaces count
            lot.setAvailableSpaces(availableCount.intValue());
        }
        
        summary.put("totalParkingLots", parkingLots.size());
        summary.put("totalSpaces", totalSpaces);
        summary.put("totalAvailable", totalAvailable);
        summary.put("totalOccupied", totalSpaces - totalAvailable);
        summary.put("parkingLots", parkingLots);
        
        return summary;
    }

    /**
     * Update parking space status
     */
    @Transactional
    public ParkingSpace updateSpaceStatus(Long spaceId, String newStatus) {
        log.info("Updating parking space {} to status: {}", spaceId, newStatus);
        
        ParkingSpace space = parkingSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Parking space not found with id: " + spaceId));
        
        String oldStatus = space.getStatus();
        space.setStatus(newStatus);
        space.setUpdatedAt(LocalDateTime.now());
        
        if ("OCCUPIED".equals(newStatus)) {
            space.setLastOccupiedAt(LocalDateTime.now());
        }
        
        ParkingSpace updated = parkingSpaceRepository.save(space);
        log.info("Parking space {} status updated from {} to {}", spaceId, oldStatus, newStatus);
        
        // Update parking lot's available spaces count
        updateParkingLotAvailableCount(space.getParkingLot().getId());
        
        return updated;
    }

    /**
     * Update parking lot's available spaces count
     */
    @Transactional
    public void updateParkingLotAvailableCount(Long parkingLotId) {
        log.info("Updating available count for parking lot: {}", parkingLotId);
        
        ParkingLot lot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new RuntimeException("Parking lot not found with id: " + parkingLotId));
        
        Long availableCount = parkingSpaceRepository.countAvailableSpacesByParkingLotId(parkingLotId);
        lot.setAvailableSpaces(availableCount.intValue());
        lot.setUpdatedAt(LocalDateTime.now());
        
        // Update status to FULL if no spaces available
        if (availableCount == 0) {
            lot.setStatus("FULL");
        } else if ("FULL".equals(lot.getStatus())) {
            lot.setStatus("ACTIVE");
        }
        
        parkingLotRepository.save(lot);
        log.info("Parking lot {} now has {} available spaces", parkingLotId, availableCount);
    }
}
