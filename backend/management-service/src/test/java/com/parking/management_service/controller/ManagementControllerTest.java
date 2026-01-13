package com.parking.management_service.controller;

import com.parking.management_service.generated.model.ParkingSpaceResponse;
import com.parking.management_service.service.ParkingSpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ManagementController.
 * Tests controller behavior with mocked service layer.
 */
@ExtendWith(MockitoExtension.class)
class ManagementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ParkingSpaceService parkingSpaceService;

    @InjectMocks
    private ParkingSpaceController parkingSpaceController;

    private ParkingSpaceResponse testSpace;

    @BeforeEach
    void setUp() {
        testSpace = createTestSpace(1L, 1L, "A-01", 0, "North", "STANDARD", "AVAILABLE",
                false, null, 500, 250, 5.00, 40.00);

        this.mockMvc = MockMvcBuilders.standaloneSetup(parkingSpaceController).build();
    }

    @Test
    void getAvailableSpaces_ReturnsListOfSpaces() throws Exception {
        // Arrange
        ParkingSpaceResponse space2 = createTestSpace(2L, 1L, "A-02", 0, "North", "ELECTRIC",
                "AVAILABLE", true, "Type 2", 500, 250, null, null);

        List<ParkingSpaceResponse> spaces = Arrays.asList(testSpace, space2);

        when(parkingSpaceService.getAvailableSpaces()).thenReturn(spaces);

        // Act & Assert
        mockMvc.perform(get("/api/management/spots/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].spaceId").value(1))
                .andExpect(jsonPath("$[0].spaceNumber").value("A-01"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[1].spaceNumber").value("A-02"))
                .andExpect(jsonPath("$[1].hasCharger").value(true));
    }

    @Test
    void getAvailableSpaces_WhenNoSpaces_ReturnsEmptyArray() throws Exception {
        // Arrange
        when(parkingSpaceService.getAvailableSpaces()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/management/spots/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAvailableSpacesByLot_ReturnsSpacesForLot() throws Exception {
        // Arrange
        List<ParkingSpaceResponse> spaces = List.of(testSpace);
        when(parkingSpaceService.getAvailableSpacesByLot(1L)).thenReturn(spaces);

        // Act & Assert
        mockMvc.perform(get("/api/management/spots/available/lot/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].lotId").value(1))
                .andExpect(jsonPath("$[0].spaceNumber").value("A-01"));
    }

    @Test
    void getAllSpaces_ReturnsAllSpaces() throws Exception {
        // Arrange
        ParkingSpaceResponse occupiedSpace = createTestSpace(2L, 1L, "A-02", 0, "North",
                "STANDARD", "OCCUPIED", false, null, 500, 250, null, null);

        List<ParkingSpaceResponse> spaces = Arrays.asList(testSpace, occupiedSpace);

        when(parkingSpaceService.getAllSpaces()).thenReturn(spaces);

        // Act & Assert
        mockMvc.perform(get("/api/management/spots"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[1].status").value("OCCUPIED"));
    }

    @Test
    void getAvailableSpacesCount_ReturnsCount() throws Exception {
        // Arrange
        when(parkingSpaceService.getAvailableSpacesCount()).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/management/spots/available/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("5"));
    }

    @Test
    void searchSpaces_WithTypeAndStatus_ReturnsMatchingSpaces() throws Exception {
        // Arrange
        List<ParkingSpaceResponse> spaces = List.of(testSpace);
        when(parkingSpaceService.searchSpaces("STANDARD", "AVAILABLE"))
                .thenReturn(spaces);

        // Act & Assert
        mockMvc.perform(get("/api/management/spots/search")
                        .param("type", "STANDARD")
                        .param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("STANDARD"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void searchSpaces_WithoutParameters_CallsServiceWithNullValues() throws Exception {
        // Arrange
        when(parkingSpaceService.searchSpaces(null, null))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/management/spots/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Helper method to create test ParkingSpaceResponse with proper types.
     */
    private ParkingSpaceResponse createTestSpace(Long spaceId, Long lotId, String spaceNumber,
                                                 Integer level, String section, String type,
                                                 String status, Boolean hasCharger, String chargerType,
                                                 Integer lengthCm, Integer widthCm,
                                                 Double hourlyRate, Double dailyRate) {
        ParkingSpaceResponse response = new ParkingSpaceResponse();
        response.setSpaceId(spaceId);
        response.setLotId(lotId);
        response.setSpaceNumber(spaceNumber);
        response.setLevel(level);
        response.setSection(section != null ? JsonNullable.of(section) : JsonNullable.undefined());
        response.setType(ParkingSpaceResponse.TypeEnum.fromValue(type));
        response.setStatus(ParkingSpaceResponse.StatusEnum.fromValue(status));
        response.setHasCharger(hasCharger);
        response.setChargerType(chargerType != null ? JsonNullable.of(chargerType) : JsonNullable.undefined());
        response.setLengthCm(lengthCm != null ? JsonNullable.of(lengthCm) : JsonNullable.undefined());
        response.setWidthCm(widthCm != null ? JsonNullable.of(widthCm) : JsonNullable.undefined());
        response.setHourlyRateOverride(hourlyRate != null ? JsonNullable.of(hourlyRate) : JsonNullable.undefined());
        response.setDailyRateOverride(dailyRate != null ? JsonNullable.of(dailyRate) : JsonNullable.undefined());
        response.setLastOccupiedAt(JsonNullable.undefined());
        return response;
    }
}

