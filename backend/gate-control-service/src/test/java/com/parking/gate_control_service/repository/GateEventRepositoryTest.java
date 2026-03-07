package com.parking.gate_control_service.repository;

import com.parking.gate_control_service.GateControlServiceApplication;
import com.parking.gate_control_service.config.TestConfig;
import com.parking.gate_control_service.entity.GateEvent;
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
 * Integration tests for GateEventRepository.
 * Uses @DataJpaTest for lightweight JPA testing with in-memory database.
 */
@DataJpaTest
@ContextConfiguration(classes = {GateControlServiceApplication.class})
@Import(TestConfig.class)
@ActiveProfiles("test")
@DisplayName("GateEvent Repository Tests")
class GateEventRepositoryTest {

    @Autowired
    private GateEventRepository gateEventRepository;

    private GateEvent testEvent;

    @BeforeEach
    void setUp() {
        gateEventRepository.deleteAll();

        testEvent = new GateEvent();
        testEvent.setEventType(GateEvent.EventType.ENTRY);
        testEvent.setLicensePlate("ABC123");
        testEvent.setTicketCode("TICKET-001");
        testEvent.setGateId("ENTRY-1");
        testEvent.setDecision(GateEvent.Decision.OPEN);
        testEvent.setReason("Valid subscription");
        testEvent.setTimestamp(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should save and find gate event by ID")
    void testSaveAndFindById() {
        // When
        GateEvent saved = gateEventRepository.save(testEvent);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTimestamp()).isNotNull();

        Optional<GateEvent> found = gateEventRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLicensePlate()).isEqualTo("ABC123");
        assertThat(found.get().getEventType()).isEqualTo(GateEvent.EventType.ENTRY);
        assertThat(found.get().getDecision()).isEqualTo(GateEvent.Decision.OPEN);
    }

    @Test
    @DisplayName("Should find gate events by license plate ordered by timestamp desc")
    void testFindByLicensePlateOrderByTimestampDesc() {
        // Given - create multiple events for same license plate
        LocalDateTime now = LocalDateTime.now();

        GateEvent event1 = new GateEvent();
        event1.setEventType(GateEvent.EventType.ENTRY);
        event1.setLicensePlate("ABC123");
        event1.setGateId("ENTRY-1");
        event1.setDecision(GateEvent.Decision.OPEN);
        event1.setTimestamp(now.minusHours(2));
        gateEventRepository.save(event1);

        GateEvent event2 = new GateEvent();
        event2.setEventType(GateEvent.EventType.EXIT);
        event2.setLicensePlate("ABC123");
        event2.setGateId("EXIT-1");
        event2.setDecision(GateEvent.Decision.OPEN);
        event2.setTimestamp(now.minusHours(1));
        gateEventRepository.save(event2);

        GateEvent event3 = new GateEvent();
        event3.setEventType(GateEvent.EventType.ENTRY);
        event3.setLicensePlate("ABC123");
        event3.setGateId("ENTRY-1");
        event3.setDecision(GateEvent.Decision.OPEN);
        event3.setTimestamp(now);
        gateEventRepository.save(event3);

        // Event for different license plate (should not be returned)
        GateEvent otherEvent = new GateEvent();
        otherEvent.setEventType(GateEvent.EventType.ENTRY);
        otherEvent.setLicensePlate("XYZ999");
        otherEvent.setGateId("ENTRY-1");
        otherEvent.setDecision(GateEvent.Decision.OPEN);
        otherEvent.setTimestamp(now);
        gateEventRepository.save(otherEvent);

        // When
        List<GateEvent> events = gateEventRepository.findByLicensePlateOrderByTimestampDesc("ABC123");

        // Then
        assertThat(events).hasSize(3);
        assertThat(events.get(0).getTimestamp()).isAfter(events.get(1).getTimestamp());
        assertThat(events.get(1).getTimestamp()).isAfter(events.get(2).getTimestamp());
        assertThat(events.get(0).getEventType()).isEqualTo(GateEvent.EventType.ENTRY);
        assertThat(events.get(1).getEventType()).isEqualTo(GateEvent.EventType.EXIT);
    }

    @Test
    @DisplayName("Should find gate events within time range")
    void testFindByTimestampBetween() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        testEvent.setTimestamp(now);
        gateEventRepository.save(testEvent);

        // Create event outside range (too old)
        GateEvent oldEvent = new GateEvent();
        oldEvent.setEventType(GateEvent.EventType.EXIT);
        oldEvent.setLicensePlate("XYZ999");
        oldEvent.setGateId("EXIT-1");
        oldEvent.setDecision(GateEvent.Decision.OPEN);
        oldEvent.setTimestamp(now.minusDays(10));
        gateEventRepository.save(oldEvent);

        // Create event outside range (too new)
        GateEvent futureEvent = new GateEvent();
        futureEvent.setEventType(GateEvent.EventType.ENTRY);
        futureEvent.setLicensePlate("DEF456");
        futureEvent.setGateId("ENTRY-1");
        futureEvent.setDecision(GateEvent.Decision.DENY);
        futureEvent.setTimestamp(now.plusDays(10));
        gateEventRepository.save(futureEvent);

        // When
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);
        List<GateEvent> eventsInRange = gateEventRepository.findByTimestampBetween(start, end);

        // Then
        assertThat(eventsInRange).hasSize(1);
        assertThat(eventsInRange.get(0).getLicensePlate()).isEqualTo("ABC123");
    }

    @Test
    @DisplayName("Should handle different event types")
    void testDifferentEventTypes() {
        // Given & When
        GateEvent entryEvent = new GateEvent();
        entryEvent.setEventType(GateEvent.EventType.ENTRY);
        entryEvent.setLicensePlate("ABC123");
        entryEvent.setGateId("ENTRY-1");
        entryEvent.setDecision(GateEvent.Decision.OPEN);
        gateEventRepository.save(entryEvent);

        GateEvent exitEvent = new GateEvent();
        exitEvent.setEventType(GateEvent.EventType.EXIT);
        exitEvent.setLicensePlate("ABC123");
        exitEvent.setGateId("EXIT-1");
        exitEvent.setDecision(GateEvent.Decision.OPEN);
        gateEventRepository.save(exitEvent);

        GateEvent manualEvent = new GateEvent();
        manualEvent.setEventType(GateEvent.EventType.MANUAL_OPEN);
        manualEvent.setLicensePlate("ABC123");
        manualEvent.setGateId("ENTRY-1");
        manualEvent.setDecision(GateEvent.Decision.OPEN);
        manualEvent.setOperatorId(100L);
        gateEventRepository.save(manualEvent);

        GateEvent errorEvent = new GateEvent();
        errorEvent.setEventType(GateEvent.EventType.ERROR);
        errorEvent.setLicensePlate("ABC123");
        errorEvent.setGateId("EXIT-1");
        errorEvent.setDecision(GateEvent.Decision.DENY);
        errorEvent.setReason("Scanner malfunction");
        gateEventRepository.save(errorEvent);

        // Then
        List<GateEvent> allEvents = gateEventRepository.findAll();
        assertThat(allEvents).hasSize(4);

        assertThat(allEvents.stream()
                .filter(e -> e.getEventType() == GateEvent.EventType.ENTRY)
                .count()).isEqualTo(1);

        assertThat(allEvents.stream()
                .filter(e -> e.getEventType() == GateEvent.EventType.EXIT)
                .count()).isEqualTo(1);

        assertThat(allEvents.stream()
                .filter(e -> e.getEventType() == GateEvent.EventType.MANUAL_OPEN)
                .count()).isEqualTo(1);

        assertThat(allEvents.stream()
                .filter(e -> e.getEventType() == GateEvent.EventType.ERROR)
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle different decisions")
    void testDifferentDecisions() {
        // Given & When
        GateEvent openEvent = new GateEvent();
        openEvent.setEventType(GateEvent.EventType.ENTRY);
        openEvent.setLicensePlate("ABC123");
        openEvent.setGateId("ENTRY-1");
        openEvent.setDecision(GateEvent.Decision.OPEN);
        openEvent.setReason("Valid subscription");
        gateEventRepository.save(openEvent);

        GateEvent denyEvent = new GateEvent();
        denyEvent.setEventType(GateEvent.EventType.ENTRY);
        denyEvent.setLicensePlate("XYZ999");
        denyEvent.setGateId("ENTRY-1");
        denyEvent.setDecision(GateEvent.Decision.DENY);
        denyEvent.setReason("Payment required");
        gateEventRepository.save(denyEvent);

        // Then
        List<GateEvent> allEvents = gateEventRepository.findAll();
        assertThat(allEvents).hasSize(2);

        assertThat(allEvents.stream()
                .filter(e -> e.getDecision() == GateEvent.Decision.OPEN)
                .count()).isEqualTo(1);

        assertThat(allEvents.stream()
                .filter(e -> e.getDecision() == GateEvent.Decision.DENY)
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should save event with nullable fields")
    void testSaveWithNullableFields() {
        // Given
        GateEvent event = new GateEvent();
        event.setEventType(GateEvent.EventType.MANUAL_OPEN);
        event.setLicensePlate("ABC123");
        event.setGateId("ENTRY-1");
        event.setDecision(GateEvent.Decision.OPEN);
        // ticketCode, reason, operatorId are null

        // When
        GateEvent saved = gateEventRepository.save(event);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTicketCode()).isNull();
        assertThat(saved.getReason()).isNull();
        assertThat(saved.getOperatorId()).isNull();
        assertThat(saved.getTimestamp()).isNotNull(); // Should be set by @PrePersist
    }

    @Test
    @DisplayName("Should auto-set timestamp with @PrePersist")
    void testPrePersistTimestamp() {
        // Given
        GateEvent event = new GateEvent();
        event.setEventType(GateEvent.EventType.ENTRY);
        event.setLicensePlate("ABC123");
        event.setGateId("ENTRY-1");
        event.setDecision(GateEvent.Decision.OPEN);
        // timestamp is null

        // When
        GateEvent saved = gateEventRepository.save(event);

        // Then
        assertThat(saved.getTimestamp()).isNotNull();
        assertThat(saved.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(saved.getTimestamp()).isAfter(LocalDateTime.now().minusMinutes(1));
    }
}
