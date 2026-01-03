package com.parking.client_service.controller;

import com.parking.client_service.generated.controller.ApiApi;
import com.parking.client_service.generated.model.ClientRequest;
import com.parking.client_service.generated.model.ClientResponse;
import com.parking.client_service.generated.model.VehicleRequest;
import com.parking.client_service.generated.model.VehicleResponse;
import com.parking.client_service.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@Slf4j
public class ClientController implements ApiApi {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // --- 1. CRUD:  ---
    @Override
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [CLIENT CONTROLLER] POST /api/clients - User: {}", auth != null ? auth.getName() : "anonymous");

        ClientResponse response = clientService.createClient(request);
        log.info("✅ [CLIENT CONTROLLER] Client created with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // --- 2. CRUD:  ---
    @Override
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [CLIENT CONTROLLER] GET /api/clients - User: {}", auth != null ? auth.getName() : "anonymous");
        log.info("🔐 [CLIENT CONTROLLER] Authentication present: {}", auth != null);
        log.info("🔐 [CLIENT CONTROLLER] Authentication details: {}", auth);

        List<ClientResponse> clients = clientService.findAllClients();
        log.info("✅ [CLIENT CONTROLLER] Returning {} clients", clients.size());
        return ResponseEntity.ok(clients);
    }

    // --- 3. CRUD:  ---
    @Override
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [CLIENT CONTROLLER] GET /api/clients/{} - User: {}", id, auth != null ? auth.getName() : "anonymous");

        return clientService.findClientById(id)
                .map(client -> {
                    log.info("✅ [CLIENT CONTROLLER] Client found with ID: {}", id);
                    return ResponseEntity.ok(client);
                })
                .orElseGet(() -> {
                    log.warn("❌ [CLIENT CONTROLLER] Client not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // --- 4. Vehicles: ---
    @Override
    @PostMapping(value = "/{clientId}/vehicles", consumes = "application/json", produces = "application/json")
    public ResponseEntity<VehicleResponse> addVehicleToClient(@PathVariable("clientId") Long clientId, @Valid @RequestBody VehicleRequest vehicleRequest) {
        // Not implemented yet in client-service; return 501 Not Implemented.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    @GetMapping(value = "/{clientId}/vehicles", produces = "application/json")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByClient(@PathVariable("clientId") Long clientId) {
        // Not implemented yet; return 501 Not Implemented.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // --- 5. Search by phone (OpenAPI name: searchClientByPhone) ---
    @Override
    @GetMapping(value = "/search", produces = "application/json")
    public ResponseEntity<ClientResponse> searchClientByPhone(@RequestParam(value = "phone", required = true) String phone) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [CLIENT CONTROLLER] GET /api/clients/search?phone={} - User: {}", phone, auth != null ? auth.getName() : "anonymous");

        return clientService.findClientByPhoneNumber(phone)
                .map(client -> {
                    log.info("✅ [CLIENT CONTROLLER] Client found with phone: {}", phone);
                    return ResponseEntity.ok(client);
                })
                .orElseGet(() -> {
                    log.warn("❌ [CLIENT CONTROLLER] Client not found with phone: {}", phone);
                    return ResponseEntity.notFound().build();
                });
    }

    // --- 6. Update ---
    @Override
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.updateClient(id, request);
        return ResponseEntity.ok(response);
    }

    // --- 7. Delete (extra, not part of generated interface) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}