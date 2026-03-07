package com.parking.gate_control_service.domain;

import com.parking.gate_control_service.entity.GateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for GateEventDomain.
 */
@DisplayName("GateEventDomain Unit Tests")
class GateEventDomainTest {

    @Test
    @DisplayName("Should create domain model from entity")
    void testFrom_Success() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.ENTRY,
                "ABC123",
                "TICKET001",
                "ENTRY-1",
                GateEvent.Decision.OPEN,
                "Valid subscription",
                null
        );

        // When
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getEntity()).isEqualTo(entity);
        assertThat(domain.getLicensePlate()).isEqualTo("ABC123");
        assertThat(domain.getTicketCode()).isEqualTo("TICKET001");
    }

    @Test
    @DisplayName("Should throw exception when creating from null entity")
    void testFrom_NullEntity_ThrowsException() {
        // When/Then
        assertThatThrownBy(() -> GateEventDomain.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("Should correctly identify entry event")
    void testIsEntry() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.ENTRY,
                "ABC123",
                null,
                "ENTRY-1",
                GateEvent.Decision.OPEN,
                "OK",
                null
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain.isEntry()).isTrue();
        assertThat(domain.isExit()).isFalse();
        assertThat(domain.isManualOpen()).isFalse();
        assertThat(domain.isError()).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify exit event")
    void testIsExit() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.EXIT,
                "ABC123",
                "TICKET001",
                "EXIT-1",
                GateEvent.Decision.OPEN,
                "Payment confirmed",
                null
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain.isExit()).isTrue();
        assertThat(domain.isEntry()).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify manual open event")
    void testIsManualOpen() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.MANUAL_OPEN,
                "ABC123",
                null,
                "ENTRY-1",
                GateEvent.Decision.OPEN,
                "Operator override",
                123L
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain.isManualOpen()).isTrue();
        assertThat(domain.isEntry()).isFalse();
        assertThat(domain.hasOperatorInvolvement()).isTrue();
    }

    @Test
    @DisplayName("Should correctly identify error event")
    void testIsError() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.ERROR,
                "ABC123",
                null,
                "ENTRY-1",
                GateEvent.Decision.DENY,
                "Scanner malfunction",
                null
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain.isError()).isTrue();
        assertThat(domain.wasDenied()).isTrue();
    }

    @Test
    @DisplayName("Should correctly identify OPEN decision")
    void testWasOpened() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.ENTRY,
                "ABC123",
                null,
                "ENTRY-1",
                GateEvent.Decision.OPEN,
                "OK",
                null
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain.wasOpened()).isTrue();
        assertThat(domain.wasDenied()).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify DENY decision")
    void testWasDenied() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.ENTRY,
                "ABC123",
                null,
                "ENTRY-1",
                GateEvent.Decision.DENY,
                "No subscription",
                null
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain.wasDenied()).isTrue();
        assertThat(domain.wasOpened()).isFalse();
    }

    @Test
    @DisplayName("Should detect operator involvement")
    void testHasOperatorInvolvement_WithOperator() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.MANUAL_OPEN,
                "ABC123",
                null,
                "ENTRY-1",
                GateEvent.Decision.OPEN,
                "Manual override",
                456L
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain.hasOperatorInvolvement()).isTrue();
        assertThat(domain.getOperatorId()).isEqualTo(456L);
    }

    @Test
    @DisplayName("Should detect no operator involvement")
    void testHasOperatorInvolvement_WithoutOperator() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.ENTRY,
                "ABC123",
                null,
                "ENTRY-1",
                GateEvent.Decision.OPEN,
                "Automatic",
                null
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain.hasOperatorInvolvement()).isFalse();
        assertThat(domain.getOperatorId()).isNull();
    }

    @Test
    @DisplayName("Should return correct entity from toEntity()")
    void testToEntity() {
        // Given
        GateEvent entity = createGateEvent(
                GateEvent.EventType.ENTRY,
                "ABC123",
                "TICKET001",
                "ENTRY-1",
                GateEvent.Decision.OPEN,
                "OK",
                null
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // When
        GateEvent returnedEntity = domain.toEntity();

        // Then
        assertThat(returnedEntity).isSameAs(entity);
    }

    @Test
    @DisplayName("Should get all properties correctly")
    void testGetters() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        GateEvent entity = new GateEvent(
                1L,
                GateEvent.EventType.EXIT,
                "XYZ789",
                "TICKET999",
                "EXIT-2",
                GateEvent.Decision.OPEN,
                "Payment completed",
                timestamp,
                789L
        );
        GateEventDomain domain = GateEventDomain.from(entity);

        // Then
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getEventType()).isEqualTo(GateEvent.EventType.EXIT);
        assertThat(domain.getLicensePlate()).isEqualTo("XYZ789");
        assertThat(domain.getTicketCode()).isEqualTo("TICKET999");
        assertThat(domain.getGateId()).isEqualTo("EXIT-2");
        assertThat(domain.getDecision()).isEqualTo(GateEvent.Decision.OPEN);
        assertThat(domain.getReason()).isEqualTo("Payment completed");
        assertThat(domain.getTimestamp()).isEqualTo(timestamp);
        assertThat(domain.getOperatorId()).isEqualTo(789L);
    }

    // Helper method
    private GateEvent createGateEvent(
            GateEvent.EventType eventType,
            String licensePlate,
            String ticketCode,
            String gateId,
            GateEvent.Decision decision,
            String reason,
            Long operatorId
    ) {
        GateEvent event = new GateEvent();
        event.setEventType(eventType);
        event.setLicensePlate(licensePlate);
        event.setTicketCode(ticketCode);
        event.setGateId(gateId);
        event.setDecision(decision);
        event.setReason(reason);
        event.setTimestamp(LocalDateTime.now());
        event.setOperatorId(operatorId);
        return event;
    }
}
