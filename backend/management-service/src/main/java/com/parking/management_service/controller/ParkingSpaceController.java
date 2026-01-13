package com.parking.management_service.controller;

import com.parking.management_service.generated.controller.ParkingSpaceApi;
import com.parking.management_service.generated.model.ParkingSpaceResponse;
import com.parking.management_service.service.ParkingSpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Parking Space Management
 * Implements OpenAPI-generated ParkingSpaceApi interface
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ParkingSpaceController implements ParkingSpaceApi {

    private final ParkingSpaceService parkingSpaceService;

    @Override
    public ResponseEntity<List<ParkingSpaceResponse>> getAvailableSpaces() {
        log.info("📍 [GET /api/management/spots/available] Getting all available parking spaces");
        List<ParkingSpaceResponse> spaces = parkingSpaceService.getAvailableSpaces();
        log.info("✅ [GET /api/management/spots/available] Found {} available spaces", spaces.size());
        return ResponseEntity.ok(spaces);
    }

    @Override
    public ResponseEntity<List<ParkingSpaceResponse>> getAvailableSpacesByLot(Long lotId) {
        log.info("📍 [GET /api/management/spots/available/lot/{}] Getting available spaces for lot", lotId);
        List<ParkingSpaceResponse> spaces = parkingSpaceService.getAvailableSpacesByLot(lotId);
        log.info("✅ [GET /api/management/spots/available/lot/{}] Found {} available spaces", lotId, spaces.size());
        return ResponseEntity.ok(spaces);
    }

    @Override
    public ResponseEntity<List<ParkingSpaceResponse>> getAllSpaces() {
        log.info("📍 [GET /api/management/spots] Getting all parking spaces");
        List<ParkingSpaceResponse> spaces = parkingSpaceService.getAllSpaces();
        log.info("✅ [GET /api/management/spots] Found {} total spaces", spaces.size());
        return ResponseEntity.ok(spaces);
    }

    @Override
    public ResponseEntity<Long> getAvailableSpacesCount() {
        log.info("📍 [GET /api/management/spots/available/count] Getting count of available spaces");
        long count = parkingSpaceService.getAvailableSpacesCount();
        log.info("✅ [GET /api/management/spots/available/count] Available: {}", count);
        return ResponseEntity.ok(count);
    }

    @Override
    public ResponseEntity<List<ParkingSpaceResponse>> searchSpaces(String type, String status) {
        log.info("📍 [GET /api/management/spots/search] Searching spaces - type: {}, status: {}", type, status);
        List<ParkingSpaceResponse> spaces = parkingSpaceService.searchSpaces(type, status);
        log.info("✅ [GET /api/management/spots/search] Found {} matching spaces", spaces.size());
        return ResponseEntity.ok(spaces);
    }
}

