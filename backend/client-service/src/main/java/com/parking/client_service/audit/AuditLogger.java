package com.parking.client_service.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fire-and-forget audit logger.
 * Sends structured business-event records to the reporting-service asynchronously.
 * Failures are silently logged — they never affect the calling transaction.
 */
@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);
    private static final String SERVICE_NAME = "client-service";

    private final ObjectMapper objectMapper;

    @Value("${reporting.service.url:http://reporting-service:8084}")
    private String reportingServiceUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public AuditLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Log a business event asynchronously.
     *
     * @param action       event name, e.g. CLIENT_CREATED
     * @param entityType   entity type: CLIENT, VEHICLE, SUBSCRIPTION
     * @param entityId     primary key of the affected entity (nullable)
     * @param clientId     ID of the client this event belongs to (nullable)
     * @param licensePlate vehicle plate (nullable, for vehicle-related events)
     * @param message      human-readable description
     * @param extra        any additional key-value pairs to include in meta
     */
    public void audit(String action,
                      String entityType,
                      Long   entityId,
                      Long   clientId,
                      String licensePlate,
                      String message,
                      Map<String, Object> extra) {

        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> meta = new HashMap<>();
                meta.put("action", action);
                meta.put("entityType", entityType);
                if (entityId     != null) meta.put("entityId",     entityId);
                if (clientId     != null) meta.put("clientId",     clientId);
                if (licensePlate != null) meta.put("licensePlate", licensePlate);
                if (extra        != null) meta.putAll(extra);

                Map<String, Object> body = new HashMap<>();
                body.put("timestamp", Instant.now().toString());
                body.put("level",     "INFO");
                body.put("service",   SERVICE_NAME);
                body.put("message",   message);
                body.put("meta",      meta);

                String json = objectMapper.writeValueAsString(body);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(reportingServiceUrl + "/api/reporting/log"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .timeout(Duration.ofSeconds(5))
                        .build();

                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.debug("AUDIT [{}/{}] {}", entityType, entityId, message);

            } catch (Exception e) {
                log.warn("AUDIT send failed [{}/{}]: {}", entityType, entityId, e.getMessage());
            }
        });
    }

    /** Shorthand without extra fields */
    public void audit(String action, String entityType, Long entityId,
                      Long clientId, String licensePlate, String message) {
        audit(action, entityType, entityId, clientId, licensePlate, message, null);
    }
}

