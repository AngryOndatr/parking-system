# E2E Testcontainers Setup on Windows 11 Home

## Проблема
При запуске E2E тестов с Testcontainers на Windows 11 Home Edition получали ошибку:
`Could not find a valid Docker environment`.

## Решение

### 1. Конфигурация Docker Desktop
Добавить в Docker Engine settings (Settings → Docker Engine) следующую строку:
```json
{
  "min-api-version": "1.24"
}
```

Это решает проблему с ошибкой `Status 400: BadRequestException` при подключении Testcontainers к Docker.

**Важно:** После изменения настроек перезапустите Docker Desktop.

### 2. Конфигурация Testcontainers
Создать файл `C:\Users\<username>\.testcontainers.properties` с содержимым:
```properties
# Конфигурация Testcontainers для Windows с Docker Desktop
docker.host=npipe:////./pipe/docker_engine

# Переиспользование контейнеров для ускорения тестов (опционально)
testcontainers.reuse.enable=false

# Конфигурация Ryuk (автоматическая очистка)
testcontainers.ryuk.disabled=false

# Таймауты
testcontainers.docker.client.timeout.duration=60s
testcontainers.pull.pause.timeout=120s
```

### 3. API Gateway Routes
Обновлены прокси-контроллеры в API Gateway для использования `/api/v1/*` вместо `/api/*`:
- `GateControlProxyController`: `/api/gate/*` → `/api/v1/gate/*`
- `BillingProxyController`: `/api/billing/*` → `/api/v1/billing/*` (добавлены методы calculate, pay, status)

### 4. Docker Compose Dependencies
Удалена циклическая зависимость в `docker-compose-e2e.yml`:
- `client-service` больше не зависит от `api-gateway`
- Все сервисы зависят только от базовых сервисов (postgres, redis, eureka-server)

### 5. OpenTelemetry Configuration
Добавлен профиль `e2e-test` в `application.yml` для полного отключения OpenTelemetry:
```yaml
spring:
  config:
    activate:
      on-profile: e2e-test
  autoconfigure:
    exclude:
      - org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpAutoConfiguration
      - org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryAutoConfiguration

management:
  tracing:
    enabled: false
```

В `docker-compose-e2e.yml` используется профиль: `SPRING_PROFILES_ACTIVE: "prod-security,e2e-test"`

### 6. Testcontainers Timeouts
Увеличены timeouts в тесте:
- Startup timeout для API Gateway: 3 минуты → 5 минут
- Добавлен параметр `.withLocalCompose(false)` для использования контейнеризованного docker-compose
- Увеличен timeout ожидания сервисов до 5 минут

## Известные проблемы

### API Gateway не запускается в Testcontainers
**Симптом**: `Aborting attempt to link to container api-gateway as it is not running`

**Возможные причины**:
1. OpenTelemetry пытается подключиться к коллектору при инициализации
2. Конфликт версий зависимостей OpenTelemetry
3. Недостаточный timeout для healthcheck

**Текущий статус**: Решено. API Gateway успешно проходит healthcheck.

## Рекомендации
1. Убедитесь, что Docker Desktop запущен перед запуском тестов
2. Убедитесь, что все образы сервисов собраны локально
3. При изменении кода сервисов пересобирайте соответствующие Docker образы
4. Используйте `mvn clean test -Dtest=OneTimeVisitorE2ETest` для запуска конкретного теста
5. Для отладки используйте `docker-compose -f docker-compose-e2e.yml up`

## Отладка

### Проверка Docker
```powershell
docker info
docker version
docker images | Select-String -Pattern "api-gateway|client-service|gate-control-service"
```

### Ручной запуск docker-compose
```powershell
cd backend\e2e-tests
docker-compose -f docker-compose-e2e.yml up -d postgres redis eureka-server
Start-Sleep -Seconds 30
docker-compose -f docker-compose-e2e.yml up api-gateway
docker-compose -f docker-compose-e2e.yml ps
docker-compose -f docker-compose-e2e.yml down -v
```

## Статус
1. ✅ Решена проблема с подключением Testcontainers к Docker
2. ✅ Обновлены API routes в прокси-контроллерах
3. ✅ Удалены циклические зависимости
4. ✅ Добавлен профиль для отключения OpenTelemetry
5. ✅ API Gateway успешно проходит healthcheck — E2E тест зелёный

## Полезные ссылки
- https://java.testcontainers.org/on_failure.html
- https://java.testcontainers.org/features/configuration/

