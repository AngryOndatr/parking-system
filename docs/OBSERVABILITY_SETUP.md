# 🔍 Observability Stack для Parking System

## 📊 Компоненты

### 1. **Prometheus** (Metrics)
- **URL:** http://localhost:9090
- **Назначение:** Сбор и хранение метрик из микросервисов
- **Targets:** API Gateway, Client Service, Eureka Server, OTEL Collector

### 2. **Grafana** (Dashboards)
- **URL:** http://localhost:3000
- **Логин:** admin
- **Пароль:** admin123
- **Назначение:** Визуализация метрик и создание дашбордов

### 3. **Jaeger** (Distributed Tracing)
- **URL:** http://localhost:16686
- **Назначение:** Трассировка распределенных запросов между микросервисами
- **Порты:**
  - 16686 - Jaeger UI
  - 14250 - gRPC
  - 14268 - HTTP
  - 4317 - OTLP gRPC

### 4. **OpenTelemetry Collector** (Telemetry Pipeline)
- **Порты:**
  - 4317 - OTLP gRPC receiver (для приложений)
  - 4318 - OTLP HTTP receiver (для приложений)
  - 8889 - Prometheus metrics endpoint
- **Назначение:** Сбор, обработка и экспорт телеметрии (traces, metrics, logs)

### 5. **pgAdmin 4** (Database Management)
- **URL:** http://localhost:5050
- **Логин:** admin@parking.com
- **Пароль:** admin

## 🚀 Запуск Observability Stack

### Автоматический запуск (Рекомендуется)

```powershell
cd C:\Users\user\Projects\parking-system\devops

# Запуск только инфраструктуры
.\start-infrastructure.ps1

# Запуск всей системы (инфраструктура + сервисы)
.\start-full-system.ps1
```

### Ручной запуск

```powershell
cd C:\Users\user\Projects\parking-system

# Запуск инфраструктуры с observability
docker-compose -f docker-compose.yml up -d postgres redis eureka-server pgadmin prometheus grafana jaeger otel-collector

# Проверка статуса
docker-compose -f docker-compose.yml ps

# Запуск сервисов (они автоматически подключатся к OTEL Collector)
docker-compose -f docker-compose.yml up -d
```

## 🔧 Конфигурация сервисов

### API Gateway и Client Service

Сервисы настроены на отправку телеметрии в OTEL Collector:

```yaml
environment:
  MANAGEMENT_TRACING_ENABLED: "true"
  MANAGEMENT_OTLP_TRACING_ENDPOINT: http://parking_otel_collector:4318/v1/traces
  MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED: "true"
  MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE: health,info,metrics,prometheus
```

## 📈 Доступ к метрикам

### Prometheus Targets
Откройте http://localhost:9090/targets для просмотра всех подключенных сервисов:
- api-gateway:8080/actuator/prometheus
- client-service:8080/actuator/prometheus
- eureka-server:8761/actuator/prometheus
- parking_otel_collector:8889/metrics

### Примеры запросов в Prometheus

```promql
# CPU Usage
process_cpu_usage

# Memory Usage
jvm_memory_used_bytes

# HTTP Request Rate
rate(http_server_requests_seconds_count[5m])

# HTTP Request Duration (95th percentile)
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Active Sessions
spring_session_active_sessions

# Database Connection Pool
hikaricp_connections_active
```

## 🎯 Jaeger - Distributed Tracing

### Как использовать:
1. Откройте http://localhost:16686
2. В левом меню выберите сервис (например, `api-gateway`)
3. Нажмите "Find Traces"
4. Кликните на любой trace для детального просмотра

### Что можно отследить:
- Время выполнения каждого запроса
- Цепочка вызовов между микросервисами
- Ошибки и исключения
- Database queries
- Redis операции

## 📊 Grafana Dashboards

### Первый вход:
1. Откройте http://localhost:3000
2. Логин: `admin`, Пароль: `admin123`
3. (Опционально) Измените пароль

### Добавление Prometheus как Data Source:
1. Configuration → Data Sources → Add data source
2. Выберите "Prometheus"
3. URL: `http://parking_prometheus:9090`
4. Save & Test

### Рекомендуемые Dashboard ID для импорта:
- **4701** - JVM (Micrometer)
- **11378** - Spring Boot Statistics
- **12230** - PostgreSQL Database
- **11159** - Redis Dashboard

### Импорт dashboard:
1. Dashboard → Import
2. Введите ID (например, 4701)
3. Выберите Prometheus data source
4. Import

## 🔍 OpenTelemetry Collector Pipeline

```
┌─────────────────┐
│  API Gateway    │──┐
└─────────────────┘  │
                     │
┌─────────────────┐  │  ┌──────────────────┐   ┌──────────┐
│ Client Service  │──┼─→│ OTEL Collector   │──→│  Jaeger  │
└─────────────────┘  │  │  (4317/4318)     │   └──────────┘
                     │  └──────────────────┘
┌─────────────────┐  │           │
│ Other Services  │──┘           │
└─────────────────┘              ↓
                          ┌──────────────┐
                          │  Prometheus  │
                          └──────────────┘
```

## 🛠️ Troubleshooting

### OTEL Collector не запускается
```powershell
docker logs parking_otel_collector
```

**Типичные проблемы:**

1. **`checkInterval must be greater than zero`**
   ```yaml
   # Добавьте в memory_limiter:
   memory_limiter:
     check_interval: 1s
     limit_mib: 512
   ```

2. **`unknown type: "jaeger"` для exporters**
   ```yaml
   # Замените jaeger exporter на:
   exporters:
     otlphttp/jaeger:
       endpoint: http://parking_jaeger:14268
       tls:
         insecure: true
   ```

3. **`address already in use` для порта 8889**
   ```powershell
   # Остановите все контейнеры
   docker-compose -f ..\docker-compose.yml down
   
   # Проверьте, что порт свободен
   netstat -ano | Select-String "8889"
   
   # Запустите заново
   docker-compose -f ..\docker-compose.yml up -d
   ```

4. **`grpc: addrConn.createTransport failed` для Jaeger**
   - Проблема: OTEL Collector пытается подключиться к Jaeger через gRPC на порту 4317, но Jaeger не слушает этот порт по умолчанию
   - Решение: Используйте HTTP endpoint (`http://parking_jaeger:14268`) вместо gRPC

### Prometheus не видит targets
```powershell
# Проверьте connectivity
docker exec parking_prometheus wget -O- http://api-gateway:8080/actuator/prometheus
```

### Jaeger не показывает traces
1. Проверьте, что OTEL Collector работает: http://localhost:8889/metrics
2. Проверьте логи: `docker logs parking_otel_collector`
3. Убедитесь, что сервисы отправляют traces: проверьте переменные окружения

### Grafana не подключается к Prometheus
- URL должен быть `http://parking_prometheus:9090` (не localhost!)
- Используйте имена контейнеров, не localhost

## 📝 Полезные команды

```powershell
# Просмотр всех контейнеров observability
docker ps --filter "name=parking_" --format "table {{.Names}}`t{{.Status}}`t{{.Ports}}"

# Логи observability сервисов
docker-compose -f ..\docker-compose.yml logs -f prometheus grafana jaeger otel-collector

# Перезапуск observability stack
docker-compose -f ..\docker-compose.yml restart prometheus grafana jaeger otel-collector

# Остановка observability (но не БД и Redis)
docker stop parking_prometheus parking_grafana parking_jaeger parking_otel_collector parking_pgadmin

# Очистка volumes (ВНИМАНИЕ: удаляет все данные!)
docker-compose -f ..\docker-compose.yml down -v
```

## 🎓 Best Practices

1. **Метрики:**
   - Используйте Prometheus для долгосрочного хранения метрик
   - Создайте алерты на критические метрики (CPU, Memory, Error Rate)

2. **Трейсинг:**
   - Включайте трейсинг только для критических путей
   - Используйте sampling для production (не 100%)

3. **Dashboards:**
   - Создайте отдельные дашборды для каждого микросервиса
   - Используйте tags и labels для фильтрации

4. **Логи:**
   - Централизуйте логи через OTEL Collector
   - Добавьте correlation ID для связи логов и traces

## 🔐 Security Considerations

**Production рекомендации:**
- Измените пароли Grafana и pgAdmin
- Используйте TLS для OTEL Collector endpoints
- Ограничьте доступ к портам мониторинга
- Настройте authentication для Prometheus

---
**Дата:** 2025-12-21  
**Версия:** 1.0

