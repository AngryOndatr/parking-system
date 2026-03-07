package com.parking.gate_control_service.controller;

import com.parking.gate_control_service.dto.EntryDecision;
import com.parking.gate_control_service.dto.ExitDecision;
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
        // EntryRequest: licensePlate and gateId are plain String (generated), spotId/operatorId are JsonNullable
        String licensePlate = entryRequest.getLicensePlate() != null ? entryRequest.getLicensePlate() : "";
        String gateId = entryRequest.getGateId() != null ? entryRequest.getGateId() : null;

        log.info("🚀 [GATE CONTROLLER] POST /api/v1/gate/entry - License plate: {}, Gate: {}", licensePlate, gateId);

        EntryDecision decision = gateService.processEntry(licensePlate);

        EntryResponse response = new EntryResponse();
        // set parkingEventId from decision if present
        response.setParkingEventId(decision.getParkingEventId());
        response.setLicensePlate(licensePlate);
        response.setEntryTime(OffsetDateTime.now());
        response.setIsSubscriber(decision.getTicketCode() == null);
        response.setMessage(decision.getMessage() == null ? "" : decision.getMessage());
        response.setGateStatus(EntryResponse.GateStatusEnum.OPENED);

        // ticketCode is JsonNullable in response model
        if (decision.getTicketCode() != null) {
            response.setTicketCode(JsonNullable.of(decision.getTicketCode()));
        } else {
            response.setTicketCode(JsonNullable.undefined());
        }

        log.info("✅ [GATE CONTROLLER] Entry processed - Action: {}, Subscriber: {}, Ticket: {}", decision.getAction(), response.getIsSubscriber(), decision.getTicketCode());

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
        try {
            log.info("🚪 [GATE CONTROLLER] POST /api/v1/gate/exit - RECEIVED REQUEST");
            log.info("🔍 [RAW] exitRequest class: {}", exitRequest != null ? exitRequest.getClass().getName() : "null");
            log.info("🔍 [RAW] exitRequest toString: {}", exitRequest);

            if (exitRequest == null) {
                log.error("❌ exitRequest is NULL!");
                throw new IllegalArgumentException("Exit request cannot be null");
            }

            log.debug("Exit request object fields:");
            log.debug("  - ticketCode field: {}", exitRequest.getTicketCode());
            log.debug("  - licensePlate field: {}", exitRequest.getLicensePlate());
            log.debug("  - exitMethod: {}", exitRequest.getExitMethod());
            log.debug("  - gateId: {}", exitRequest.getGateId());

            // ExitRequest: licensePlate and ticketCode are JsonNullable (generated)
            String ticketCode = null;
            if (exitRequest.getTicketCode() != null && exitRequest.getTicketCode().isPresent()) {
                ticketCode = exitRequest.getTicketCode().get();
                log.debug("✅ Extracted ticket code: {}", ticketCode);
            } else {
                log.debug("⚠️  No ticket code in request (null or not present)");
            }

            String licensePlate = null;
            if (exitRequest.getLicensePlate() != null && exitRequest.getLicensePlate().isPresent()) {
                licensePlate = exitRequest.getLicensePlate().get();
                log.debug("✅ Extracted license plate: {}", licensePlate);
            } else {
                log.debug("⚠️  No license plate in request (null or not present)");
            }

            log.info("🚀 [GATE CONTROLLER] Processing exit - License plate: {}, Ticket: {}", licensePlate, ticketCode);

            ExitDecision decision = gateService.processExit(ticketCode, licensePlate);
            log.info("✅ [GATE CONTROLLER] Exit decision received: action={}, message={}",
                decision.getAction(), decision.getMessage());

            ExitResponse response = new ExitResponse();
            // set parkingEventId if present in decision
            response.setParkingEventId(decision.getParkingEventId());
            response.setLicensePlate(licensePlate == null ? "" : licensePlate);
            response.setEntryTime(OffsetDateTime.now().minusHours(2));
            response.setExitTime(OffsetDateTime.now());
            response.setDurationMinutes(120);
            response.setFee(0.0);
            response.setIsPaid("OPEN".equals(decision.getAction()));
            response.setPaymentRequired("DENY".equals(decision.getAction()));
            response.setMessage(decision.getMessage() == null ? "" : decision.getMessage());
            response.setGateStatus(ExitResponse.GateStatusEnum.OPENED);

            log.info("✅ [GATE CONTROLLER] Returning exit response with status OK");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("❌ [GATE CONTROLLER] Error processing exit request: {} - {}",
                e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
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
        // GateControlRequest fields are plain (gateId, action, operatorId, reason)
        String action = gateControlRequest.getAction() != null ? gateControlRequest.getAction().name() : null;
        Long operatorId = gateControlRequest.getOperatorId() != null ? gateControlRequest.getOperatorId() : null;
        String gateId = gateControlRequest.getGateId() != null ? gateControlRequest.getGateId() : null;

        log.info("🚀 [GATE CONTROLLER] POST /api/v1/gate/control - Gate: {}, Action: {}", gateId, action);

        gateService.processManualControl(gateId, action, operatorId, gateControlRequest.getReason() != null ? gateControlRequest.getReason() : null);

        GateControlResponse response = new GateControlResponse();
        response.setGateId(gateId);
        if (gateControlRequest.getAction() != null) {
            response.setAction(GateControlResponse.ActionEnum.valueOf(gateControlRequest.getAction().name()));
        }
        response.setStatus(GateControlResponse.StatusEnum.OPENED);
        response.setOperatorId(operatorId);
        response.setTimestamp(OffsetDateTime.now());
        response.setMessage(gateControlRequest.getReason() != null ? gateControlRequest.getReason() : "Manual action performed");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
