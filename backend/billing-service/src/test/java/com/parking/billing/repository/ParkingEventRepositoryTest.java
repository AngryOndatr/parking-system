package com.parking.billing.repository;

import com.parking.billing.config.TestConfig;
import com.parking.billing.entity.ParkingEvent;
import com.parking.billing_service.BillingServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ParkingEventRepository.
 * Uses @DataJpaTest for lightweight JPA testing with in-memory database.
 */
@DataJpaTest
@ContextConfiguration(classes = {BillingServiceApplication.class})
@Import(TestConfig.class)
@ActiveProfiles("test")
@DisplayName("ParkingEvent Repository Tests")
class ParkingEventRepositoryTest {

    @Autowired
    private ParkingEventRepository parkingEventRepository;

    private ParkingEvent testEvent;

    @BeforeEach
    void setUp() {
        parkingEventRepository.deleteAll();

        testEvent = new ParkingEvent();
        testEvent.setVehicleId(100L);
        testEvent.setLicensePlate("ABC123");
        testEvent.setTicketCode("TICKET-001");
        testEvent.setEntryTime(LocalDateTime.now());
        testEvent.setEntryMethod(ParkingEvent.EntryMethod.SCAN);
        testEvent.setSpotId(1L);
        testEvent.setIsSubscriber(false);
    }

    @Test
    @DisplayName("Should save and find parking event by ID")
    void testSaveAndFindById() {
        // When
        ParkingEvent saved = parkingEventRepository.save(testEvent);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getEntryTime()).isNotNull();

        Optional<ParkingEvent> found = parkingEventRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLicensePlate()).isEqualTo("ABC123");
    }

    @Test
    @DisplayName("Should find parking event by ticket code")
    void testFindByTicketCode() {
        // Given
        parkingEventRepository.save(testEvent);

        // When
        Optional<ParkingEvent> found = parkingEventRepository.findByTicketCode("TICKET-001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getLicensePlate()).isEqualTo("ABC123");
    }

    @Test
    @DisplayName("Should find active parking events by license plate")
    void testFindByLicensePlateAndExitTimeIsNull() {
        // Given - create event without exit time
        parkingEventRepository.save(testEvent);

        // Create another event with exit time (should not be returned)
        ParkingEvent exitedEvent = new ParkingEvent();
        exitedEvent.setLicensePlate("ABC123");
        exitedEvent.setTicketCode("TICKET-002");
        exitedEvent.setEntryTime(LocalDateTime.now().minusHours(2));
        exitedEvent.setExitTime(LocalDateTime.now().minusHours(1));
        exitedEvent.setIsSubscriber(false);
        parkingEventRepository.save(exitedEvent);

        // When
        List<ParkingEvent> activeEvents = parkingEventRepository
                .findByLicensePlateAndExitTimeIsNull("ABC123");

        // Then
        assertThat(activeEvents).hasSize(1);
        assertThat(activeEvents.get(0).getTicketCode()).isEqualTo("TICKET-001");
        assertThat(activeEvents.get(0).getExitTime()).isNull();
    }

    @Test
    @DisplayName("Should find parking events within time range")
    void testFindByEntryTimeBetween() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        testEvent.setEntryTime(now);
        parkingEventRepository.save(testEvent);

        // Create event outside range
        ParkingEvent oldEvent = new ParkingEvent();
        oldEvent.setLicensePlate("XYZ999");
        oldEvent.setTicketCode("TICKET-OLD");
        oldEvent.setEntryTime(now.minusDays(10));
        oldEvent.setIsSubscriber(false);
        parkingEventRepository.save(oldEvent);

        // When
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);
        List<ParkingEvent> eventsInRange = parkingEventRepository
                .findByEntryTimeBetween(start, end);

        // Then
        assertThat(eventsInRange).hasSize(1);
        assertThat(eventsInRange.get(0).getTicketCode()).isEqualTo("TICKET-001");
    }

    @Test
    @DisplayName("Should check if parking event exists by ticket code")
    void testExistsByTicketCode() {
        // Given
        parkingEventRepository.save(testEvent);

        // When & Then
        assertThat(parkingEventRepository.existsByTicketCode("TICKET-001")).isTrue();
        assertThat(parkingEventRepository.existsByTicketCode("NON-EXISTENT")).isFalse();
    }

    @Test
    @DisplayName("Should update parking event with exit time")
    void testUpdateExitTime() {
        // Given
        ParkingEvent saved = parkingEventRepository.save(testEvent);
        assertThat(saved.getExitTime()).isNull();

        // When
        saved.setExitTime(LocalDateTime.now());
        saved.setExitMethod(ParkingEvent.ExitMethod.SCAN);
        ParkingEvent updated = parkingEventRepository.save(saved);

        // Then
        assertThat(updated.getExitTime()).isNotNull();
        assertThat(updated.getExitMethod()).isEqualTo(ParkingEvent.ExitMethod.SCAN);
    }

    @Test
    @DisplayName("Should handle subscriber flag correctly")
    void testSubscriberFlag() {
        // Given - subscriber event
        testEvent.setIsSubscriber(true);
        ParkingEvent saved = parkingEventRepository.save(testEvent);

        // When
        Optional<ParkingEvent> found = parkingEventRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getIsSubscriber()).isTrue();
    }

    @Test
    @DisplayName("Should store all entry and exit method enums")
    void testEntryAndExitMethodEnums() {
        // Test SCAN entry
        testEvent.setEntryMethod(ParkingEvent.EntryMethod.SCAN);
        ParkingEvent saved1 = parkingEventRepository.save(testEvent);
        assertThat(saved1.getEntryMethod()).isEqualTo(ParkingEvent.EntryMethod.SCAN);

        // Test MANUAL entry
        testEvent.setId(null);
        testEvent.setTicketCode("TICKET-002");
        testEvent.setEntryMethod(ParkingEvent.EntryMethod.MANUAL);
        testEvent.setExitMethod(ParkingEvent.ExitMethod.MANUAL);
        ParkingEvent saved2 = parkingEventRepository.save(testEvent);
        assertThat(saved2.getEntryMethod()).isEqualTo(ParkingEvent.EntryMethod.MANUAL);
        assertThat(saved2.getExitMethod()).isEqualTo(ParkingEvent.ExitMethod.MANUAL);

        // Test AUTO exit
        testEvent.setId(null);
        testEvent.setTicketCode("TICKET-003");
        testEvent.setExitMethod(ParkingEvent.ExitMethod.AUTO);
        ParkingEvent saved3 = parkingEventRepository.save(testEvent);
        assertThat(saved3.getExitMethod()).isEqualTo(ParkingEvent.ExitMethod.AUTO);
    }
}

