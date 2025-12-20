# 📊 Observability Architecture Documentation
## Parking System OpenTelemetry & Monitoring Implementation

### 📋 Обзор

Система observability построена на стандартах OpenTelemetry с полным stack мониторинга включающим metrics, traces, и logs. Реализованы enterprise-grade observability practices с Grafana visualization, Prometheus metrics collection, и Jaeger distributed tracing.

---

## 🏗️ Observability Architecture

### Архитектурные принципы
- **Three Pillars of Observability** - Metrics, Traces, Logs
- **OpenTelemetry Standards** - Vendor-neutral instrumentation
- **Real-time Monitoring** - Live dashboards и alerting
- **Distributed Tracing** - End-to-end request visibility
- **Performance Optimization** - Proactive performance monitoring

### Компонентная архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                         │
│  ┌─────────────────┬─────────────────┬─────────────────────┐ │
│  │  API Gateway    │   Microservice  │   Microservice N   │ │
│  │  @WithSpan      │   Instrumented  │   Instrumented     │ │
│  └─────────────────┴─────────────────┴─────────────────────┘ │
└─────────────────────┬───────────────────────────────────────┘
                      │ OpenTelemetry SDKs
┌─────────────────────▼───────────────────────────────────────┐
│              OpenTelemetry Collector                        │
│  • Receive: OTLP, Prometheus, Jaeger                      │
│  • Process: Sampling, Filtering, Enrichment               │
│  • Export: Prometheus, Jaeger, Logging                    │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│               Storage & Visualization                       │
│  ┌─────────────────┬─────────────────┬─────────────────────┐ │
│  │   Prometheus    │     Jaeger      │      Grafana       │ │
│  │   (Metrics)     │    (Traces)     │   (Dashboards)     │ │
│  │   • TSDB        │   • Trace Store │   • Visualization  │ │
│  │   • Alerting    │   • Search      │   • Alerting       │ │
│  └─────────────────┴─────────────────┴─────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Observability Components

### 1. OpenTelemetry Configuration

**Местоположение:** `com.parking.api_gateway.observability.config.OpenTelemetryConfig`

**Key Features:**
- Auto-instrumentation для Spring WebMVC, JDBC, Redis
- Configurable sampling rates (default: 10%)
- Multiple exporters (OTLP, Jaeger)
- Resource detection и service identification

**Configuration:**
```java
@Configuration
@ConditionalOnProperty(value = "management.tracing.enabled", havingValue = "true")
public class OpenTelemetryConfig {
    
    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault()
            .merge(Resource.create(
                Attributes.builder()
                    .put(ResourceAttributes.SERVICE_NAME, serviceName)
                    .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
                    .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, environment)
                    .build()));

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(createSpanExporter()).build())
            .setResource(resource)
            .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();
    }
}
```

### 2. ObservabilityService - Custom Metrics & Traces

**Местоположение:** `com.parking.api_gateway.observability.service.ObservabilityService`

**Metrics Categories:**

#### Security Metrics
```java
// Counters
private final Counter loginAttempts;        // auth.login.attempts
private final Counter loginSuccess;         // auth.login.success  
private final Counter loginFailures;        // auth.login.failures
private final Counter rateLimitViolations;  // security.rate_limit.violations
private final Counter securityViolations;   // security.violations

// Gauges
private final AtomicInteger activeSessions; // sessions.active
private final AtomicInteger blockedIps;     // security.blocked_ips
```

#### Performance Metrics
```java
// Timers
private final Timer authenticationTimer;    // auth.duration
private final Timer jwtValidationTimer;     // jwt.validation.duration

// Request tracking
public void recordRequest(String method, String endpoint, int statusCode, long durationMs) {
    Timer.builder("http.requests")
        .tag("method", method)
        .tag("endpoint", endpoint)  
        .tag("status", String.valueOf(statusCode))
        .register(meterRegistry)
        .record(durationMs, TimeUnit.MILLISECONDS);
}
```

#### Business Metrics
```java
// Custom business metrics
@WithSpan("business.parking_operation")
public void recordParkingOperation(String operation, String result) {
    meterRegistry.counter("parking.operations",
        "operation", operation,
        "result", result)
        .increment();
}
```

### 3. Custom Tracing Implementation

**Automatic Instrumentation:**
- **HTTP Requests** - Spring WebMVC auto-instrumentation
- **Database Queries** - JDBC instrumentation with query details
- **Redis Operations** - Redis client instrumentation
- **Method Tracing** - @WithSpan annotation support

**Manual Tracing:**
```java
// Method-level tracing
@WithSpan("auth.login")
public AuthResponse login(AuthRequest request) {
    // Automatic span creation with method name
}

// Custom span creation
public void complexBusinessLogic() {
    Span span = tracer.spanBuilder("business.complex_operation")
        .setAttribute("user.id", userId)
        .setAttribute("operation.type", "parking_assignment")
        .startSpan();
    
    try {
        // Business logic
        span.setStatus(StatusCode.OK);
    } catch (Exception e) {
        span.setStatus(StatusCode.ERROR, e.getMessage());
        throw e;
    } finally {
        span.end();
    }
}
```

---

## 🔧 Infrastructure Components

### 1. Prometheus Configuration

**File:** `devops/observability/prometheus.yml`

**Scrape Targets:**
```yaml
scrape_configs:
  - job_name: 'parking-api-gateway'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    static_configs:
      - targets: ['api-gateway:8080']
    
  - job_name: 'otel-collector'
    static_configs:
      - targets: ['otel-collector:8888']
    metrics_path: '/metrics'
    scrape_interval: 15s
```

**Key Metrics Collected:**
- Application metrics from Spring Boot Actuator
- JVM metrics (memory, GC, threads)
- HTTP request metrics
- Database connection pool metrics
- Custom business metrics

### 2. OpenTelemetry Collector

**File:** `devops/observability/otel-collector-config.yml`

**Pipeline Configuration:**
```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:  
        endpoint: 0.0.0.0:4318

processors:
  batch:
    timeout: 1s
    send_batch_size: 1024
  
  resource:
    attributes:
      - action: insert
        key: service.namespace
        value: "parking-system"

exporters:
  jaeger:
    endpoint: jaeger:14250
    tls:
      insecure: true
  
  prometheus:
    endpoint: "0.0.0.0:8888"

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [resource, batch]
      exporters: [jaeger]
    
    metrics:
      receivers: [otlp, prometheus]
      processors: [resource, batch] 
      exporters: [prometheus]
```

### 3. Grafana Dashboard Configuration

**Dashboards:** `devops/observability/grafana/dashboards/parking-system-dashboard.json`

**Key Panels:**

#### Security Monitoring
```json
{
  "title": "Authentication Metrics",
  "targets": [
    {
      "expr": "auth_login_attempts_total",
      "legendFormat": "Login Attempts"
    },
    {
      "expr": "rate(auth_login_failures_total[5m])",
      "legendFormat": "Failed Logins/sec"
    }
  ]
}
```

#### Performance Monitoring
```json
{
  "title": "Response Times",
  "targets": [
    {
      "expr": "histogram_quantile(0.95, rate(auth_duration_seconds_bucket[5m]))",
      "legendFormat": "95th percentile"
    },
    {
      "expr": "histogram_quantile(0.50, rate(auth_duration_seconds_bucket[5m]))",
      "legendFormat": "50th percentile"  
    }
  ]
}
```

#### Infrastructure Monitoring
```json
{
  "title": "System Resources", 
  "targets": [
    {
      "expr": "process_cpu_usage",
      "legendFormat": "CPU Usage"
    },
    {
      "expr": "jvm_memory_used_bytes / jvm_memory_max_bytes",
      "legendFormat": "Memory Usage"
    }
  ]
}
```

---

## 📈 Metrics Collection Strategy

### 1. Application Metrics

**Spring Boot Actuator Integration:**
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,traces
  endpoint:
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

**Custom Metrics Implementation:**
```java
// Counter example
public void recordSecurityViolation(String type, String details, String ipAddress) {
    securityViolations.increment(
        Attributes.of(
            AttributeKey.stringKey("violation_type"), type,
            AttributeKey.stringKey("client_ip"), ipAddress
        )
    );
}

// Timer example  
public Timer.Sample startAuthenticationTimer() {
    return Timer.start(meterRegistry);
}

public void stopAuthenticationTimer(Timer.Sample sample, boolean successful) {
    sample.stop(authenticationTimer.tag("success", String.valueOf(successful)));
}

// Gauge example
Gauge.builder("sessions.active")
    .description("Number of active user sessions")
    .register(meterRegistry, activeSessions, AtomicInteger::get);
```

### 2. Infrastructure Metrics

**JVM Metrics (Auto-collected):**
- Heap/Non-heap memory usage
- Garbage collection statistics
- Thread pool status
- Class loading metrics

**Database Metrics:**
- HikariCP connection pool metrics
- Query execution times
- Transaction statistics
- Connection health

**Redis Metrics:**
- Connection pool status
- Command execution times  
- Memory usage
- Cache hit rates

---

## 🔍 Distributed Tracing Implementation

### 1. Trace Context Propagation

**HTTP Headers:**
```
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
tracestate: parking=p1,redis=r1
```

**Context Propagation Flow:**
1. **Request Ingress** - Extract trace context from HTTP headers
2. **Service Boundary** - Inject context into outbound calls
3. **Database Operations** - Automatic JDBC span creation
4. **Cache Operations** - Redis operation tracing
5. **Response Egress** - Trace ID in response headers

### 2. Span Attributes & Events

**Security Span Example:**
```java
Span span = tracer.spanBuilder("auth.login.success")
    .setAttribute("user.name", username)
    .setAttribute("client.ip", ipAddress) 
    .setAttribute("user.roles", String.join(",", roles))
    .setAttribute("session.id", sessionId)
    .startSpan();

// Add events to span
span.addEvent("password_validated");
span.addEvent("jwt_token_created");
span.addEvent("user_session_established");
```

**Performance Span Example:**
```java
@WithSpan("jwt.validation")
public Claims validateToken(String token) {
    Span currentSpan = Span.current();
    currentSpan.setAttribute("jwt.token_id", extractJti(token));
    currentSpan.setAttribute("jwt.issuer", extractIssuer(token));
    
    if (!isValid) {
        currentSpan.setStatus(StatusCode.ERROR, "Invalid token");
        currentSpan.recordException(new SecurityException("Token validation failed"));
    }
    
    return claims;
}
```

---

## 🎯 Monitoring Dashboards

### 1. Security Dashboard

**Key Metrics:**
- Authentication success/failure rates
- Active user sessions
- Security violations timeline
- Geographic login patterns
- Blocked IP addresses

**Alerting Rules:**
```promql
# High failed login rate
rate(auth_login_failures_total[5m]) > 10

# Unusual security violations
rate(security_violations_total[1m]) > 5

# Session anomalies
sessions_active > 1000
```

### 2. Performance Dashboard

**Key Metrics:**
- Request latency percentiles (50th, 95th, 99th)
- Request rate and error rate
- JVM performance (memory, GC)
- Database connection health

**SLA Monitoring:**
```promql
# Response time SLA (95% under 500ms)
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) < 0.5

# Availability SLA (99.9% uptime)
(sum(rate(http_requests_total{status!~"5.."}[5m])) / sum(rate(http_requests_total[5m]))) > 0.999
```

### 3. Business Dashboard

**Key Metrics:**
- Parking operations per hour
- User registration trends
- Revenue metrics
- System utilization patterns

---

## 🚀 Deployment & Configuration

### 1. Docker Compose Stack

**File:** `devops/docker-compose-observability.yml`

**Services:**
- **Prometheus** - Metrics collection and storage
- **Grafana** - Visualization and dashboards  
- **Jaeger** - Distributed tracing backend
- **OTEL Collector** - Telemetry data processing
- **API Gateway** - Instrumented application

### 2. Environment Configuration

**Production Variables:**
```bash
# OpenTelemetry
MANAGEMENT_TRACING_ENABLED=true
MANAGEMENT_TRACING_OTLP_ENDPOINT=http://otel-collector:4317
MANAGEMENT_TRACING_SAMPLING_PROBABILITY=0.1  # 10% sampling

# Metrics Export
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus

# Service Discovery
OTEL_SERVICE_NAME=parking-api-gateway
OTEL_SERVICE_VERSION=1.0.0
OTEL_RESOURCE_ATTRIBUTES=deployment.environment=production
```

### 3. Startup Scripts

**Windows PowerShell:** `devops/start-observability.ps1`
**Linux/macOS Bash:** `devops/start-observability.sh`

**Features:**
- Automated container orchestration
- Health checks и service readiness
- URL display для easy access
- Log aggregation commands

---

## 📊 Performance Optimization

### 1. Sampling Strategies

**Trace Sampling:**
```java
// Production: 10% sampling to reduce overhead
.sampling:
  probability: 0.1

// Development: 100% sampling for debugging  
.sampling:
  probability: 1.0

// Custom sampling logic
public class CustomSampler implements Sampler {
    @Override
    public SamplingResult shouldSample(Context context, String traceId, String name, SpanKind spanKind, Attributes attributes, List<Link> parentLinks) {
        // Sample 100% of error traces
        if (attributes.get(AttributeKey.stringKey("error")) != null) {
            return SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE);
        }
        
        // Sample 10% of normal traces
        return Math.abs(traceId.hashCode()) % 10 == 0 ?
            SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE) :
            SamplingResult.create(SamplingDecision.DROP);
    }
}
```

### 2. Resource Management

**Batch Processing:**
```yaml
# OTEL Collector batch configuration
processors:
  batch:
    timeout: 1s
    send_batch_size: 1024
    send_batch_max_size: 2048
```

**Memory Optimization:**
```yaml
# Memory limiter
processors:
  memory_limiter:
    limit_mib: 512
    spike_limit_mib: 128
```

---

## 🔍 Troubleshooting & Debugging

### 1. Trace Analysis

**Performance Issues:**
1. Search for slow traces in Jaeger (duration > 1s)
2. Identify bottleneck spans (database queries, external APIs)
3. Analyze span attributes for context
4. Correlate with metrics in Grafana

**Error Investigation:**
1. Filter traces with error status
2. Examine exception details in span events
3. Follow trace context across services
4. Check related logs via correlation ID

### 2. Metrics Investigation

**Dashboard Analysis:**
- **RED Metrics** - Rate, Errors, Duration
- **USE Metrics** - Utilization, Saturation, Errors  
- **Business Metrics** - Custom KPIs

**Query Examples:**
```promql
# Error rate spike investigation
rate(http_requests_total{status=~"5.."}[5m])

# Latency investigation
histogram_quantile(0.95, sum(rate(http_request_duration_seconds_bucket[5m])) by (le, endpoint))

# Resource utilization
jvm_memory_used_bytes / jvm_memory_max_bytes
```

---

## 📚 Best Practices Implemented

### 1. Observability Standards
- ✅ OpenTelemetry vendor-neutral instrumentation
- ✅ Semantic conventions for consistent naming
- ✅ Structured logging with correlation IDs
- ✅ Service topology mapping

### 2. Performance Considerations
- ✅ Configurable sampling rates
- ✅ Batch processing для efficiency
- ✅ Resource limits и memory management
- ✅ Async export для minimal latency impact

### 3. Security & Compliance
- ✅ No sensitive data in traces/metrics
- ✅ Configurable data retention policies
- ✅ Access control для monitoring tools
- ✅ Audit trail для observability changes

### 4. Operational Excellence
- ✅ Automated deployment scripts
- ✅ Health checks для all components
- ✅ Documentation и runbooks
- ✅ Disaster recovery procedures

---

## 🚀 Future Enhancements

### Planned Improvements
- **Log Correlation** - ELK stack integration
- **APM Integration** - Application Performance Monitoring
- **Custom Alerting** - PagerDuty/Slack integration
- **Cost Optimization** - Smart sampling strategies
- **Multi-Cloud** - Cross-provider monitoring

### Advanced Features
- **Anomaly Detection** - ML-based pattern recognition
- **Predictive Monitoring** - Proactive issue detection
- **Service Mesh** - Istio/Envoy integration
- **Chaos Engineering** - Resilience testing observability

---

**Observability система готова к enterprise-scale monitoring с comprehensive visibility! 📊✨**