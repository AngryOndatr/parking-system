# 📊 Parking System Observability Stack

Полный observability stack с OpenTelemetry, Prometheus, Grafana и Jaeger для мониторинга Parking System.

## 🚀 Быстрый старт

### Windows (PowerShell)
```powershell
cd devops
.\start-observability.ps1
```

### Linux/MacOS (Bash)
```bash
cd devops
chmod +x start-observability.sh
./start-observability.sh
```

## 🎯 Компоненты

### 📈 **Grafana Dashboard** - http://localhost:3000
- **Логин**: admin / admin123
- Визуализация метрик реального времени
- Dashboards для security, performance, business metrics
- Alerts и notifications

### 🔍 **Prometheus Metrics** - http://localhost:9090  
- Сбор метрик с API Gateway
- Targets: API Gateway, PostgreSQL, Redis
- PromQL запросы для анализа

### 🕵️ **Jaeger Tracing** - http://localhost:16686
- Distributed tracing для запросов
- Performance profiling
- Dependency mapping

### 🔄 **OpenTelemetry Collector** - Порт 4317/4318
- Универсальный сбор телеметрии
- Обработка traces, metrics, logs
- Export в Prometheus и Jaeger

## 📊 Метрики мониторинга

### 🔐 Security Metrics
- `auth_login_attempts_total` - Общее количество попыток входа
- `auth_login_success_total` - Успешные входы
- `auth_login_failures_total` - Неудачные входы
- `security_rate_limit_violations_total` - Нарушения rate limit
- `security_violations_total` - Нарушения безопасности
- `sessions_active` - Активные сессии
- `security_blocked_ips` - Заблокированные IP

### ⚡ Performance Metrics  
- `auth_duration_seconds` - Время аутентификации
- `jwt_validation_duration_seconds` - Время валидации JWT
- `http_requests_total` - HTTP запросы
- `http_request_duration_seconds` - Длительность HTTP запросов

### 🏗️ Infrastructure Metrics
- `hikaricp_connections_*` - Database connection pool
- `jvm_memory_*` - JVM memory usage
- `process_cpu_usage` - CPU utilization
- `system_disk_*` - Disk usage

## 🎛️ Custom Traces

### Автоматические трассы:
- HTTP requests через Spring WebMVC
- Database queries через JDBC
- Redis operations

### Кастомные трассы:
```java
@WithSpan("auth.login")
public void login() {
    // Автоматически создает span
}

// Или вручную:
Span span = tracer.spanBuilder("custom.operation").startSpan();
try {
    // Business logic
} finally {
    span.end();
}
```

## 🔧 Конфигурация

### Environment Variables для Production:
```bash
# OpenTelemetry
MANAGEMENT_TRACING_ENABLED=true
MANAGEMENT_TRACING_OTLP_ENDPOINT=http://your-otel-collector:4317
MANAGEMENT_TRACING_SAMPLING_PROBABILITY=0.1

# Metrics
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
```

## 📋 Полезные команды

### Проверить метрики:
```bash
curl http://localhost:8080/actuator/prometheus | grep auth_
curl http://localhost:8080/actuator/health
```

### Генерировать трафик для тестирования:
```bash
# Успешный логин
curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpassword"}' \
  http://localhost:8080/api/auth/login

# Rate limit тестирование (много запросов)
for i in {1..100}; do curl http://localhost:8080/actuator/health; done
```

### Docker команды:
```bash
# Просмотр логов
docker-compose -f docker-compose-observability.yml logs -f grafana
docker-compose -f docker-compose-observability.yml logs -f api-gateway

# Перезапуск сервиса
docker-compose -f docker-compose-observability.yml restart api-gateway

# Остановка всех сервисов
docker-compose -f docker-compose-observability.yml down

# Очистка volumes
docker-compose -f docker-compose-observability.yml down -v
```

## 🎨 Grafana Dashboards

### Основные панели:
1. **Authentication Overview** - логины, ошибки, активные сессии
2. **Security Monitoring** - нарушения, заблокированные IP, rate limiting  
3. **Performance Metrics** - response times, throughput, errors
4. **Infrastructure** - CPU, memory, database connections
5. **Business Metrics** - парковочные операции, использование

### Создание кастомных dashboards:
1. Откройте http://localhost:3000
2. Login: admin / admin123  
3. Create → Dashboard
4. Add Panel с PromQL queries

## 🔍 Jaeger Tracing

### Просмотр трассировок:
1. Откройте http://localhost:16686
2. Service: api-gateway
3. Operation: выберите конкретную операцию
4. Find Traces

### Trace информация:
- **Duration** - общее время выполнения
- **Services** - задействованные сервисы  
- **Spans** - отдельные операции
- **Tags** - метаданные (user_id, IP, etc.)
- **Logs** - события внутри spans

## 🚨 Alerting (Future Enhancement)

### Примеры правил для Prometheus:
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
- **Prometheus**: Настроить retention policy
- **Grafana**: Использовать external database (MySQL/PostgreSQL)
- **Jaeger**: Настроить Elasticsearch storage
- **OTEL Collector**: Horizontal scaling с load balancing

### Security:
- Настроить authentication для Grafana
- Secure endpoints с reverse proxy
- Network isolation для internal services
- Encrypt traffic между компонентами

### Performance:
- Оптимизировать sampling rate для traces (0.1 = 10%)
- Настроить metric retention periods  
- Use recording rules в Prometheus
- Index optimization для Jaeger

## 🎉 Результат

После запуска у вас будет:
- ✅ Real-time мониторинг безопасности и производительности
- ✅ Distributed tracing для debugging
- ✅ Comprehensive dashboards 
- ✅ Infrastructure monitoring
- ✅ Готовность к production alerting
- ✅ Observability best practices

**Система готова к production мониторингу!** 🚀