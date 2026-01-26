package com.parking.gate_control_service.controller;

import com.parking.gate_control_service.dto.EntryDecision;
import com.parking.gate_control_service.generated.api.GateApi;
import com.parking.gate_control_service.generated.model.*;
import com.parking.gate_control_service.service.GateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;

/**
 * REST controller for gate operations.
 * Implements the GateApi interface generated from OpenAPI specification.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class GateController implements GateApi {

    private final GateService gateService;

    /**
     * Process vehicle entry through gate.
     * POST /api/v1/gate/entry
     *
     * @param entryRequest entry request with license plate and gate info
     * @return EntryResponse with parking event details and gate status
     */
    @Override
    public ResponseEntity<EntryResponse> processEntry(@Valid @RequestBody EntryRequest entryRequest) {
        log.info("🚀 [GATE CONTROLLER] POST /api/v1/gate/entry - License plate: {}, Gate: {}",
                entryRequest.getLicensePlate(), entryRequest.getGateId());

        // Process entry through service layer
        EntryDecision decision = gateService.processEntry(entryRequest.getLicensePlate());

        // Build response based on decision
        EntryResponse response = new EntryResponse();
        response.setParkingEventId(0L); // TODO: Will be set when integrating with parking events
        response.setLicensePlate(entryRequest.getLicensePlate());
        response.setEntryTime(OffsetDateTime.now());
        response.setIsSubscriber(decision.getTicketCode() == null);
        response.setMessage(decision.getMessage());
        response.setGateStatus(EntryResponse.GateStatusEnum.OPENED);

        // Set ticket code for non-subscribers using JsonNullable
        if (decision.getTicketCode() != null) {
            response.setTicketCode(JsonNullable.of(decision.getTicketCode()));
        } else {
            response.setTicketCode(JsonNullable.undefined());
        }

        log.info("✅ [GATE CONTROLLER] Entry processed - Action: {}, Subscriber: {}, Ticket: {}",
                decision.getAction(), response.getIsSubscriber(), decision.getTicketCode());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Process vehicle exit through gate.
     * POST /api/v1/gate/exit
     *
     * @param exitRequest exit request with license plate or ticket code
     * @return ExitResponse with parking event details, fee, and gate status
     */
    @Override
    public ResponseEntity<ExitResponse> processExit(@Valid @RequestBody ExitRequest exitRequest) {
        log.info("🚀 [GATE CONTROLLER] POST /api/v1/gate/exit - License plate: {}, Ticket: {}",
                exitRequest.getLicensePlate(), exitRequest.getTicketCode());

        // TODO: Implement exit logic in Phase 2
        throw new UnsupportedOperationException("Exit processing not yet implemented");
    }

    /**
     * Manual gate control by operator.
     * POST /api/v1/gate/control
     *
     * @param gateControlRequest gate control request with gate ID and action
     * @return GateControlResponse with gate status
     */
    @Override
    public ResponseEntity<GateControlResponse> manualControl(@Valid @RequestBody GateControlRequest gateControlRequest) {
        log.info("🚀 [GATE CONTROLLER] POST /api/v1/gate/control - Gate: {}, Action: {}",
                gateControlRequest.getGateId(), gateControlRequest.getAction());

        // TODO: Implement manual control logic in Phase 2
        throw new UnsupportedOperationException("Manual control not yet implemented");
    }
}
