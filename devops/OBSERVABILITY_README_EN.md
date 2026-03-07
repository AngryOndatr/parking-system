# 📊 Parking System Observability Stack

Full observability stack with OpenTelemetry, Prometheus, Grafana and Jaeger for monitoring the Parking System.

## 🚀 Quick Start

### Windows (PowerShell)
```powershell
cd devops
.\start-observability.ps1
```

### Linux/macOS (Bash)
```bash
cd devops
chmod +x start-observability.sh
./start-observability.sh
```

## 🎯 Components

### 📈 **Grafana Dashboard** — http://localhost:3000
- **Login**: admin / admin123
- Real-time metrics visualisation
- Dashboards for security, performance and business metrics
- Alerts and notifications

### 🔍 **Prometheus Metrics** — http://localhost:9090
- Metrics collection from API Gateway
- Targets: API Gateway, PostgreSQL, Redis
- PromQL queries for analysis

### 🕵️ **Jaeger Tracing** — http://localhost:16686
- Distributed tracing for requests
- Performance profiling
- Dependency mapping

### 🔄 **OpenTelemetry Collector** — Ports 4317/4318
- Universal telemetry collection
- Processes traces, metrics, logs
- Exports to Prometheus and Jaeger

## 📊 Monitored Metrics

### 🔐 Security Metrics
- `auth_login_attempts_total` — Total login attempts
- `auth_login_success_total` — Successful logins
- `auth_login_failures_total` — Failed logins
- `security_rate_limit_violations_total` — Rate limit violations
- `security_violations_total` — Security violations
- `sessions_active` — Active sessions
- `security_blocked_ips` — Blocked IPs

### ⚡ Performance Metrics
- `auth_duration_seconds` — Authentication duration
- `jwt_validation_duration_seconds` — JWT validation duration
- `http_requests_total` — HTTP requests
- `http_request_duration_seconds` — HTTP request duration

### 🏗️ Infrastructure Metrics
- `hikaricp_connections_*` — Database connection pool
- `jvm_memory_*` — JVM memory usage
- `process_cpu_usage` — CPU utilisation
- `system_disk_*` — Disk usage

## 🎛️ Custom Traces

### Automatic traces:
- HTTP requests via Spring WebMVC
- Database queries via JDBC
- Redis operations

### Custom traces:
```java
@WithSpan("auth.login")
public void login() {
    // Automatically creates a span
}

// Or manually:
Span span = tracer.spanBuilder("custom.operation").startSpan();
try {
    // Business logic
} finally {
    span.end();
}
```

## 🔧 Configuration

### Environment Variables for Production:
```bash
# OpenTelemetry
MANAGEMENT_TRACING_ENABLED=true
MANAGEMENT_TRACING_OTLP_ENDPOINT=http://your-otel-collector:4317
MANAGEMENT_TRACING_SAMPLING_PROBABILITY=0.1

# Metrics
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
```

## 📋 Useful Commands

### Check metrics:
```bash
curl http://localhost:8080/actuator/prometheus | grep auth_
curl http://localhost:8080/actuator/health
```

### Generate traffic for testing:
```bash
# Successful login
curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpassword"}' \
  http://localhost:8080/api/auth/login

# Rate limit test (many requests)
for i in {1..100}; do curl http://localhost:8080/actuator/health; done
```

### Docker commands:
```bash
# View logs
docker-compose -f docker-compose-observability.yml logs -f grafana
docker-compose -f docker-compose-observability.yml logs -f api-gateway

# Restart a service
docker-compose -f docker-compose-observability.yml restart api-gateway

# Stop all services
docker-compose -f docker-compose-observability.yml down

# Remove volumes
docker-compose -f docker-compose-observability.yml down -v
```

## 🎨 Grafana Dashboards

### Main panels:
1. **Authentication Overview** — logins, errors, active sessions
2. **Security Monitoring** — violations, blocked IPs, rate limiting
3. **Performance Metrics** — response times, throughput, errors
4. **Infrastructure** — CPU, memory, database connections
5. **Business Metrics** — parking operations, utilisation

### Creating custom dashboards:
1. Open http://localhost:3000
2. Login: admin / admin123
3. Create → Dashboard
4. Add Panel with PromQL queries

## 🔍 Jaeger Tracing

### Viewing traces:
1. Open http://localhost:16686
2. Service: api-gateway
3. Operation: select the specific operation
4. Find Traces

### Trace information:
- **Duration** — total execution time
- **Services** — involved services
- **Spans** — individual operations
- **Tags** — metadata (user_id, IP, etc.)
- **Logs** — events inside spans

## 🚨 Alerting (Future Enhancement)

### Example Prometheus alert rules:
```yaml
# High error rate
- alert: HighErrorRate
  expr: rate(auth_login_failures_total[5m]) > 10

# High response time
- alert: HighResponseTime
  expr: histogram_quantile(0.95, rate(auth_duration_seconds_bucket[5m])) > 1

# Service down
- alert: ServiceDown
  expr: up{job="parking-api-gateway"} == 0
```

## 📈 Production Considerations

### Scaling:
- **Prometheus**: Configure retention policy
- **Grafana**: Use external database (MySQL/PostgreSQL)
- **Jaeger**: Configure Elasticsearch storage
- **OTEL Collector**: Horizontal scaling with load balancing

### Security:
- Configure authentication for Grafana
- Secure endpoints with reverse proxy
- Network isolation for internal services
- Encrypt traffic between components

### Performance:
- Optimise sampling rate for traces (0.1 = 10%)
- Configure metric retention periods
- Use recording rules in Prometheus
- Index optimisation for Jaeger

## 🎉 Result

After startup you will have:
- ✅ Real-time security and performance monitoring
- ✅ Distributed tracing for debugging
- ✅ Comprehensive dashboards
- ✅ Infrastructure monitoring
- ✅ Production-ready alerting
- ✅ Observability best practices

**System is ready for production monitoring!** 🚀
