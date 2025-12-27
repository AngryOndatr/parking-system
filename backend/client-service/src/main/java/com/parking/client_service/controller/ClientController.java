package com.parking.client_service.controller;

import com.parking.client_service.generated.model.ClientRequest;
import com.parking.client_service.generated.model.ClientResponse;
import com.parking.client_service.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@Slf4j
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // --- 1. CRUD:  ---
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@RequestBody ClientRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [CLIENT CONTROLLER] POST /api/clients - User: {}", auth != null ? auth.getName() : "anonymous");

        ClientResponse response = clientService.createClient(request);
        log.info("✅ [CLIENT CONTROLLER] Client created with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // --- 2. CRUD:  ---
    @GetMapping
    public List<ClientResponse> getAllClients() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [CLIENT CONTROLLER] GET /api/clients - User: {}", auth != null ? auth.getName() : "anonymous");
        log.info("🔐 [CLIENT CONTROLLER] Authentication present: {}", auth != null);
        log.info("🔐 [CLIENT CONTROLLER] Authentication details: {}", auth);

        List<ClientResponse> clients = clientService.findAllClients();
        log.info("✅ [CLIENT CONTROLLER] Returning {} clients", clients.size());
        return clients;
    }

    // --- 3. CRUD:  ---
    @GetMapping("/{id}")
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

    // --- 4.  ---
    @GetMapping("/search")
    public ResponseEntity<ClientResponse> getClientByPhone(@RequestParam String phone) {
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
}