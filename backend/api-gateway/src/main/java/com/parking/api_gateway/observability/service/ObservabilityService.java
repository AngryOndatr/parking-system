package com.parking.api_gateway.observability.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.extension.annotations.WithSpan;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObservabilityService {
    
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;
    
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
    
    public ObservabilityService(MeterRegistry meterRegistry, Tracer tracer) {
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;
        
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
        Gauge.builder("sessions.active")
            .description("Number of active user sessions")
            .register(meterRegistry, activeSessions, AtomicInteger::get);
            
        Gauge.builder("security.blocked_ips")
            .description("Number of currently blocked IP addresses")
            .register(meterRegistry, blockedIps, AtomicInteger::get);
            
        Gauge.builder("requests.total")
            .description("Total number of requests processed")
            .register(meterRegistry, totalRequests, AtomicLong::get);
    }
    
    // Authentication metrics
    public void recordLoginAttempt() {
        loginAttempts.increment();
    }
    
    public void recordLoginSuccess(String username, String ipAddress) {
        loginSuccess.increment();
        activeSessions.incrementAndGet();
        
        Span span = tracer.spanBuilder("auth.login.success")
            .setAttribute("user.name", username)
            .setAttribute("client.ip", ipAddress)
            .startSpan();
        
        try {
            log.debug("Successful login recorded for user: {} from IP: {}", username, ipAddress);
        } finally {
            span.end();
        }
    }
    
    public void recordLoginFailure(String username, String ipAddress, String reason) {
        loginFailures.increment(
            Attributes.of(
                AttributeKey.stringKey("reason"), reason,
                AttributeKey.stringKey("username"), username,
                AttributeKey.stringKey("client_ip"), ipAddress
            )
        );
        
        Span span = tracer.spanBuilder("auth.login.failure")
            .setAttribute("user.name", username)
            .setAttribute("client.ip", ipAddress)
            .setAttribute("failure.reason", reason)
            .setStatus(StatusCode.ERROR, "Login failed: " + reason)
            .startSpan();
        
        try {
            log.warn("Failed login recorded for user: {} from IP: {}, reason: {}", 
                    username, ipAddress, reason);
        } finally {
            span.end();
        }
    }
    
    // Security metrics
    public void recordRateLimitViolation(String ipAddress, String endpoint) {
        rateLimitViolations.increment(
            Attributes.of(
                AttributeKey.stringKey("client_ip"), ipAddress,
                AttributeKey.stringKey("endpoint"), endpoint
            )
        );
        
        Span span = tracer.spanBuilder("security.rate_limit_violation")
            .setAttribute("client.ip", ipAddress)
            .setAttribute("endpoint", endpoint)
            .setStatus(StatusCode.ERROR, "Rate limit exceeded")
            .startSpan();
        
        try {
            log.warn("Rate limit violation from IP: {} on endpoint: {}", ipAddress, endpoint);
        } finally {
            span.end();
        }
    }
    
    public void recordSecurityViolation(String type, String details, String ipAddress) {
        securityViolations.increment(
            Attributes.of(
                AttributeKey.stringKey("violation_type"), type,
                AttributeKey.stringKey("client_ip"), ipAddress
            )
        );
        
        Span span = tracer.spanBuilder("security.violation")
            .setAttribute("violation.type", type)
            .setAttribute("violation.details", details)
            .setAttribute("client.ip", ipAddress)
            .setStatus(StatusCode.ERROR, "Security violation: " + type)
            .startSpan();
        
        try {
            log.error("Security violation [{}] from IP: {} - {}", type, ipAddress, details);
        } finally {
            span.end();
        }
    }
    
    // Performance metrics
    @WithSpan("jwt.validation")
    public Timer.Sample startJwtValidationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopJwtValidationTimer(Timer.Sample sample, boolean successful) {
        sample.stop(jwtValidationTimer.tag("success", String.valueOf(successful)));
    }
    
    @WithSpan("auth.process")
    public Timer.Sample startAuthenticationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopAuthenticationTimer(Timer.Sample sample, boolean successful) {
        sample.stop(authenticationTimer.tag("success", String.valueOf(successful)));
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