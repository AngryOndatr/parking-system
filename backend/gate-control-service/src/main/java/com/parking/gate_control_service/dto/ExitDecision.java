package com.parking.gate_control_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExitDecision {
    private String action; // "OPEN" or "DENY"
    private String message;
}
