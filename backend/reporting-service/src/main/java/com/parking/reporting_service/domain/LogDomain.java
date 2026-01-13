package com.parking.reporting_service.domain;

import com.parking.common.entity.Log;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Domain model for Log.
 * Wraps the Log entity and provides business logic methods.
 */
public class LogDomain {

    private final Log entity;

    // Constructors
    public LogDomain() {
        this.entity = new Log();
    }

    public LogDomain(Log entity) {
        this.entity = entity;
    }

    public LogDomain(LocalDateTime timestamp, String logLevel, String service, String message, Long userId, Map<String, Object> meta) {
        this.entity = new Log(timestamp, logLevel, service, message, userId, meta);
    }

    // Access to entity
    public Log getEntity() {
        return entity;
    }

    // Business logic methods (delegate to entity)
    public Long getId() {
        return entity.getId();
    }

    public LocalDateTime getTimestamp() {
        return entity.getTimestamp();
    }

    public void setTimestamp(LocalDateTime timestamp) {
        entity.setTimestamp(timestamp);
    }

    public String getLogLevel() {
        return entity.getLogLevel();
    }

    public void setLogLevel(String logLevel) {
        entity.setLogLevel(logLevel);
    }

    public String getMessage() {
        return entity.getMessage();
    }

    public void setMessage(String message) {
        entity.setMessage(message);
    }

    public Long getUserId() {
        return entity.getUserId();
    }

    public void setUserId(Long userId) {
        entity.setUserId(userId);
    }

    public String getService() {
        return entity.getService();
    }

    public void setService(String service) {
        entity.setService(service);
    }

    public Map<String, Object> getMeta() {
        return entity.getMeta();
    }

    public void setMeta(Map<String, Object> meta) {
        entity.setMeta(meta);
    }

    /**
     * Business logic: Validates that required fields are present
     */
    public boolean isValid() {
        return entity.getTimestamp() != null
                && entity.getLogLevel() != null && !entity.getLogLevel().isBlank()
                && entity.getMessage() != null && !entity.getMessage().isBlank();
    }

    /**
     * Business logic: Check if this is an error-level log
     */
    public boolean isError() {
        return "ERROR".equalsIgnoreCase(entity.getLogLevel());
    }

    /**
     * Business logic: Check if this log has metadata
     */
    public boolean hasMeta() {
        return entity.getMeta() != null && !entity.getMeta().isEmpty();
    }

    @Override
    public String toString() {
        return "LogDomain{" +
                "entity=" + entity +
                '}';
    }
}

