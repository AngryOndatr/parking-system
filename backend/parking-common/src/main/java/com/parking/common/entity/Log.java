package com.parking.common.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * JPA Entity for system logs.
 * Maps to the 'logs' table in the database.
 */
@Entity
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "log_level", nullable = false, length = 50)
    private String logLevel;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "service", length = 100)
    private String service;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "json")
    private Map<String, Object> meta;

    // Constructors
    public Log() {
    }

    public Log(LocalDateTime timestamp, String logLevel, String message, Long userId) {
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.message = message;
        this.userId = userId;
    }

    public Log(LocalDateTime timestamp, String logLevel, String service, String message, Long userId, Map<String, Object> meta) {
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.service = service;
        this.message = message;
        this.userId = userId;
        this.meta = meta;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", logLevel='" + logLevel + '\'' +
                ", service='" + service + '\'' +
                ", message='" + message + '\'' +
                ", userId=" + userId +
                ", meta=" + meta +
                '}';
    }
}

