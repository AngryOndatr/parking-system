package com.parking.management_service.service;

import com.parking.common.entity.ParkingSpace;
import com.parking.management_service.generated.model.ParkingSpaceResponse;
import com.parking.management_service.repository.ParkingSpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for ManagementService.
 * Tests actual database operations with H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ManagementServiceIntegrationTest {

    @Autowired
    private ParkingSpaceService parkingSpaceService;

    @Autowired
    private ParkingSpaceRepository parkingSpaceRepository;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        parkingSpaceRepository.deleteAll();
    }

    @Test
    void getAvailableSpaces_WhenNoSpaces_ReturnsEmptyList() {
        // Act
        List<ParkingSpaceResponse> result = parkingSpaceService.getAvailableSpaces();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getAvailableSpaces_WhenAvailableSpacesExist_ReturnsOnlyAvailable() {
        // Arrange
        ParkingSpace available1 = createParkingSpace(1L, "A-01", "AVAILABLE", "STANDARD", 0);
        ParkingSpace available2 = createParkingSpace(1L, "A-02", "AVAILABLE", "ELECTRIC", 0);
        ParkingSpace occupied = createParkingSpace(1L, "A-03", "OCCUPIED", "STANDARD", 0);

        parkingSpaceRepository.saveAll(List.of(available1, available2, occupied));

        // Act
        List<ParkingSpaceResponse> result = parkingSpaceService.getAvailableSpaces();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ParkingSpaceResponse::getStatus)
                .containsOnly(ParkingSpaceResponse.StatusEnum.AVAILABLE);
        assertThat(result)
                .extracting(ParkingSpaceResponse::getSpaceNumber)
                .containsExactlyInAnyOrder("A-01", "A-02");
    }

    @Test
    void getAvailableSpacesByLot_ReturnsOnlySpacesForSpecificLot() {
        // Arrange
        ParkingSpace lot1Space1 = createParkingSpace(1L, "L1-A-01", "AVAILABLE", "STANDARD", 0);
        ParkingSpace lot1Space2 = createParkingSpace(1L, "L1-A-02", "AVAILABLE", "STANDARD", 0);
        ParkingSpace lot2Space = createParkingSpace(2L, "L2-A-01", "AVAILABLE", "STANDARD", 0);

        parkingSpaceRepository.saveAll(List.of(lot1Space1, lot1Space2, lot2Space));

        // Act
        List<ParkingSpaceResponse> result = parkingSpaceService.getAvailableSpacesByLot(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ParkingSpaceResponse::getLotId)
                .containsOnly(1L);
        assertThat(result)
                .extracting(ParkingSpaceResponse::getSpaceNumber)
                .containsExactlyInAnyOrder("L1-A-01", "L1-A-02");
    }

    private ParkingSpace createParkingSpace(Long lotId, String spaceNumber,
                                            String status, String type, Integer level) {
        ParkingSpace space = new ParkingSpace();
        space.setParkingLotId(lotId);
        space.setSpaceNumber(spaceNumber);
        space.setStatus(status);
        space.setSpaceType(type);
        space.setFloorLevel(level);
        space.setHasCharger(false);
        return space;
    }

    @Test
    void countAvailableSpaces_ReturnsCorrectCount() {
        // Arrange
        ParkingSpace available1 = createParkingSpace(1L, "A-01", "AVAILABLE", "STANDARD", 0);
        ParkingSpace available2 = createParkingSpace(1L, "A-02", "AVAILABLE", "STANDARD", 0);
        ParkingSpace occupied = createParkingSpace(1L, "A-03", "OCCUPIED", "STANDARD", 0);

        parkingSpaceRepository.saveAll(List.of(available1, available2, occupied));

        // Act
        long count = parkingSpaceService.getAvailableSpacesCount();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void getAllSpaces_ReturnsAllSpacesRegardlessOfStatus() {
        // Arrange
        ParkingSpace available = createParkingSpace(1L, "A-01", "AVAILABLE", "STANDARD", 0);
        ParkingSpace occupied = createParkingSpace(1L, "A-02", "OCCUPIED", "STANDARD", 0);
        ParkingSpace maintenance = createParkingSpace(1L, "A-03", "MAINTENANCE", "STANDARD", 0);

        parkingSpaceRepository.saveAll(List.of(available, occupied, maintenance));

        // Act
        List<ParkingSpaceResponse> result = parkingSpaceService.getAllSpaces();

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(ParkingSpaceResponse::getStatus)
                .containsExactlyInAnyOrder(
                        ParkingSpaceResponse.StatusEnum.AVAILABLE,
                        ParkingSpaceResponse.StatusEnum.OCCUPIED,
                        ParkingSpaceResponse.StatusEnum.MAINTENANCE
                );
    }

    @Test
    void getSpacesByTypeAndStatus_FiltersCorrectly() {
        // Arrange
        ParkingSpace standardAvailable = createParkingSpace(1L, "S-01", "AVAILABLE", "STANDARD", 0);
        ParkingSpace electricAvailable = createParkingSpace(1L, "E-01", "AVAILABLE", "ELECTRIC", 0);
        ParkingSpace electricOccupied = createParkingSpace(1L, "E-02", "OCCUPIED", "ELECTRIC", 0);

        parkingSpaceRepository.saveAll(List.of(standardAvailable, electricAvailable, electricOccupied));

        // Act
        List<ParkingSpaceResponse> result =
                parkingSpaceService.searchSpaces("ELECTRIC", "AVAILABLE");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ParkingSpaceResponse.TypeEnum.ELECTRIC);
        assertThat(result.get(0).getStatus()).isEqualTo(ParkingSpaceResponse.StatusEnum.AVAILABLE);
        assertThat(result.get(0).getSpaceNumber()).isEqualTo("E-01");
    }

    @Test
    void getAvailableSpaces_MapsAllFieldsCorrectly() {
        // Arrange
        ParkingSpace space = createParkingSpace(1L, "A-01", "AVAILABLE", "ELECTRIC", 1);
        space.setSection("North");
        space.setHasCharger(true);
        space.setChargerType("Type 2");
        space.setLengthCm(500);
        space.setWidthCm(250);
        space.setHourlyRateOverride(new BigDecimal("5.50"));
        space.setDailyRateOverride(new BigDecimal("45.00"));

        parkingSpaceRepository.save(space);

        // Act
        List<ParkingSpaceResponse> result = parkingSpaceService.getAvailableSpaces();

        // Assert
        assertThat(result).hasSize(1);
        ParkingSpaceResponse dto = result.get(0);
        assertThat(dto.getSpaceNumber()).isEqualTo("A-01");
        assertThat(dto.getLevel()).isEqualTo(1);
        assertThat(dto.getSection().get()).isEqualTo("North"); // JsonNullable
        assertThat(dto.getType()).isEqualTo(ParkingSpaceResponse.TypeEnum.ELECTRIC); // Enum
        assertThat(dto.getHasCharger()).isTrue();
        assertThat(dto.getChargerType().get()).isEqualTo("Type 2"); // JsonNullable
        assertThat(dto.getLengthCm().get()).isEqualTo(500); // JsonNullable
        assertThat(dto.getWidthCm().get()).isEqualTo(250); // JsonNullable
        assertThat(dto.getHourlyRateOverride().get()).isEqualTo(5.50); // JsonNullable
        assertThat(dto.getDailyRateOverride().get()).isEqualTo(45.00); // JsonNullable
    }
}

