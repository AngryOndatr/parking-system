package com.parking.api_gateway.observability.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class ObservabilityService {
    
    private final MeterRegistry meterRegistry;
    @Autowired(required = false)
    private Tracer tracer;

    // Metrics counters
    private final Counter loginAttempts;
    private final Counter loginSuccess;
    private final Counter loginFailures;
    private final Counter rateLimitViolations;
    private final Counter securityViolations;
    private final Timer authenticationTimer;
    private final Timer jwtValidationTimer;
    
    // Gauges for current state
    private final AtomicInteger activeSessions = new AtomicInteger(0);
    private final AtomicInteger blockedIps = new AtomicInteger(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    
    public ObservabilityService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        // tracer may be absent in test/non-observability environments
        // this.tracer = tracer;

        // Initialize counters
        this.loginAttempts = Counter.builder("auth.login.attempts")
            .description("Total number of login attempts")
            .register(meterRegistry);
            
        this.loginSuccess = Counter.builder("auth.login.success")
            .description("Number of successful logins")
            .register(meterRegistry);
            
        this.loginFailures = Counter.builder("auth.login.failures")
            .description("Number of failed logins")
            .tag("reason", "invalid_credentials")
            .register(meterRegistry);
            
        this.rateLimitViolations = Counter.builder("security.rate_limit.violations")
            .description("Number of rate limit violations")
            .register(meterRegistry);
            
        this.securityViolations = Counter.builder("security.violations")
            .description("Number of security violations")
            .register(meterRegistry);
            
        this.authenticationTimer = Timer.builder("auth.duration")
            .description("Authentication processing time")
            .register(meterRegistry);
            
        this.jwtValidationTimer = Timer.builder("jwt.validation.duration")
            .description("JWT validation processing time")
            .register(meterRegistry);
        
        // Initialize gauges
        Gauge.builder("sessions.active", activeSessions, ai -> ai.get())
            .description("Number of active user sessions")
            .register(meterRegistry);

        Gauge.builder("security.blocked_ips", blockedIps, ai -> ai.get())
            .description("Number of currently blocked IP addresses")
            .register(meterRegistry);

        Gauge.builder("requests.total", totalRequests, al -> al.get())
            .description("Total number of requests processed")
            .register(meterRegistry);
    }
    
    // Authentication metrics
    public void recordLoginAttempt() {
        loginAttempts.increment();
    }
    
    public void recordLoginSuccess(String username, String ipAddress) {
        loginSuccess.increment();
        activeSessions.incrementAndGet();
        
        if (tracer != null) {
            Span span = tracer.spanBuilder("auth.login.success")
                .setAttribute("user.name", username)
                .setAttribute("client.ip", ipAddress)
                .startSpan();
            try {
                log.debug("Successful login recorded for user: {} from IP: {}", username, ipAddress);
            } finally {
                span.end();
            }
        } else {
            log.debug("Successful login recorded for user: {} from IP: {} (no tracer)", username, ipAddress);
        }
    }

    public void recordLoginFailure(String username, String ipAddress, String reason) {
        loginFailures.increment();

        if (tracer != null) {
            Span span = tracer.spanBuilder("auth.login.failure")
                .setAttribute("user.name", username)
                .setAttribute("client.ip", ipAddress)
                .setAttribute("failure.reason", reason)
                .startSpan();
            span.setStatus(StatusCode.ERROR, "Login failed: " + reason);
            try {
                log.warn("Failed login recorded for user: {} from IP: {}, reason: {}",
                        username, ipAddress, reason);
            } finally {
                span.end();
            }
        } else {
            log.warn("Failed login recorded for user: {} from IP: {}, reason: {} (no tracer)", username, ipAddress, reason);
        }
    }
    
    // Security metrics
    public void recordRateLimitViolation(String ipAddress, String endpoint) {
        rateLimitViolations.increment();

        if (tracer != null) {
            Span span = tracer.spanBuilder("security.rate_limit_violation")
                .setAttribute("client.ip", ipAddress)
                .setAttribute("endpoint", endpoint)
                .startSpan();
            span.setStatus(StatusCode.ERROR, "Rate limit exceeded");

            try {
                log.warn("Rate limit violation from IP: {} on endpoint: {}", ipAddress, endpoint);
            } finally {
                span.end();
            }
        } else {
            log.warn("Rate limit violation from IP: {} on endpoint: {} (no tracer)", ipAddress, endpoint);
        }
    }
    
    public void recordSecurityViolation(String type, String details, String ipAddress) {
        securityViolations.increment();

        if (tracer != null) {
            Span span = tracer.spanBuilder("security.violation")
                .setAttribute("violation.type", type)
                .setAttribute("violation.details", details)
                .setAttribute("client.ip", ipAddress)
                .startSpan();
            span.setStatus(StatusCode.ERROR, "Security violation: " + type);

            try {
                log.error("Security violation [{}] from IP: {} - {}", type, ipAddress, details);
            } finally {
                span.end();
            }
        } else {
            log.error("Security violation [{}] from IP: {} - {} (no tracer)", type, ipAddress, details);
        }
    }
    
    // Performance metrics
    @WithSpan("jwt.validation")
    public Timer.Sample startJwtValidationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopJwtValidationTimer(Timer.Sample sample, boolean successful) {
        sample.stop(Timer.builder("jwt.validation")
            .tag("success", String.valueOf(successful))
            .register(meterRegistry));
    }
    
    @WithSpan("auth.process")
    public Timer.Sample startAuthenticationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopAuthenticationTimer(Timer.Sample sample, boolean successful) {
        sample.stop(Timer.builder("auth.authentication")
            .tag("success", String.valueOf(successful))
            .register(meterRegistry));
    }
    
    // Request tracking
    public void recordRequest(String method, String endpoint, int statusCode, long durationMs) {
        totalRequests.incrementAndGet();
        
        Timer.builder("http.requests")
            .description("HTTP request duration")
            .tag("method", method)
            .tag("endpoint", endpoint)
            .tag("status", String.valueOf(statusCode))
            .register(meterRegistry)
            .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    
    // Session management
    public void recordSessionCreated() {
        activeSessions.incrementAndGet();
    }
    
    public void recordSessionDestroyed() {
        activeSessions.decrementAndGet();
    }
    
    public void updateBlockedIpsCount(int count) {
        blockedIps.set(count);
    }
    
    // Business metrics
    @WithSpan("business.parking_operation")
    public void recordParkingOperation(String operation, String result) {
        meterRegistry.counter("parking.operations", 
            "operation", operation,
            "result", result)
            .increment();
    }
    
    // Health check metrics
    public void recordHealthCheck(String component, boolean healthy) {
        meterRegistry.counter("health.checks",
            "component", component,
            "status", healthy ? "healthy" : "unhealthy")
            .increment();
    }
}

