package com.parking.gate_control_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a gate entry decision.
 * Contains the action to take (OPEN or DENY), a message, and optional ticket code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryDecision {

    /**
     * Action to take: "OPEN" or "DENY"
     */
    private String action;

    /**
     * Message explaining the decision
     */
    private String message;

    /**
     * Ticket code if generated for one-time visitor (null for subscribers)
     */
    private String ticketCode;
}
