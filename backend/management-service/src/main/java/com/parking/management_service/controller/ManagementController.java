package com.parking.management_service.controller;

import com.parking.common.entity.ParkingSpace;
import com.parking.management_service.service.ManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/management")
@Slf4j
@RequiredArgsConstructor
public class ManagementController {

    private final ManagementService managementService;

    /**
     * GET /api/management/available
     * Returns all available parking spaces
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableSpaces(
            @RequestParam(required = false) Long parkingLotId) {
        
        log.info("🚀 [MANAGEMENT CONTROLLER] GET /api/management/available - parkingLotId: {}", parkingLotId);
        
        List<ParkingSpace> availableSpaces;
        
        if (parkingLotId != null) {
            availableSpaces = managementService.getAvailableSpacesByLot(parkingLotId);
        } else {
            availableSpaces = managementService.getAvailableSpaces();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", availableSpaces.size());
        response.put("spaces", availableSpaces);
        
        log.info("✅ [MANAGEMENT CONTROLLER] Returning {} available spaces", availableSpaces.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/management/summary
     * Returns availability summary for all parking lots
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getAvailabilitySummary() {
        log.info("🚀 [MANAGEMENT CONTROLLER] GET /api/management/summary");
        
        Map<String, Object> summary = managementService.getAvailabilitySummary();
        
        log.info("✅ [MANAGEMENT CONTROLLER] Returning availability summary");
        return ResponseEntity.ok(summary);
    }

    /**
     * POST /api/management/spaces/{id}/update
     * Updates the status of a parking space
     */
    @PostMapping("/spaces/{id}/update")
    public ResponseEntity<ParkingSpace> updateSpaceStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String newStatus = request.get("status");
        log.info("🚀 [MANAGEMENT CONTROLLER] POST /api/management/spaces/{}/update - status: {}", id, newStatus);
        
        if (newStatus == null || newStatus.isEmpty()) {
            log.error("❌ [MANAGEMENT CONTROLLER] Status is required");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            ParkingSpace updated = managementService.updateSpaceStatus(id, newStatus);
            log.info("✅ [MANAGEMENT CONTROLLER] Space status updated successfully");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("❌ [MANAGEMENT CONTROLLER] Error updating space status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
