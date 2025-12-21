# JWT Token Management and Service Proxying - Session Summary

**Дата:** 2025-12-21
**Тип изменений:** JWT аутентификация между сервисами, API Gateway проксирование

## Реализованный функционал

### 1. JWT Token Interceptor для API Gateway

#### Создан `JwtRequestInterceptor.java`
- **Расположение:** `backend/api-gateway/src/main/java/com/parking/api_gateway/config/JwtRequestInterceptor.java`
- **Функционал:**
  - Автоматическое пересылание JWT токенов из входящих запросов в downstream сервисы
  - Форвардинг заголовков `X-Forwarded-For`, `User-Agent`
  - Извлечение IP-адреса клиента

#### Обновлён `RestTemplateConfig.java`
- Добавлен JWT interceptor в RestTemplate
- Настроены таймауты подключения (5 сек) и чтения (10 сек)

### 2. Proxy Controllers для всех микросервисов

Созданы контроллеры для проксирования запросов через API Gateway:

#### `BillingProxyController.java` (`/api/billing/*`)
- GET `/invoices` - получить все счета
- GET `/invoices/{id}` - получить счёт по ID
- POST `/invoices` - создать новый счёт
- PUT `/invoices/{id}` - обновить счёт
- DELETE `/invoices/{id}` - удалить счёт
- GET `/clients/{clientId}/invoices` - получить счета клиента

#### `GateControlProxyController.java` (`/api/gate/*`)
- POST `/entry` - открыть въездной шлагбаум
- POST `/exit` - открыть выездной шлагбаум
- GET `/events` - получить все события шлагбаума
- GET `/events/{id}` - получить событие по ID
- GET `/status` - получить статус шлагбаума
- GET `/clients/{clientId}/history` - история проездов клиента

#### `ManagementProxyController.java` (`/api/management/*`)
- GET `/spots` - получить все парковочные места
- GET `/spots/{id}` - получить место по ID
- POST `/spots` - создать новое место
- PUT `/spots/{id}` - обновить место
- DELETE `/spots/{id}` - удалить место
- GET `/spots/available` - получить свободные места
- GET `/spots/occupied` - получить занятые места
- POST `/spots/{spotId}/assign/{clientId}` - назначить место клиенту

#### `ReportingProxyController.java` (`/api/reports/*`)
- GET `/financial?startDate&endDate` - финансовый отчёт
- GET `/occupancy?startDate&endDate` - отчёт по занятости
- GET `/clients/{clientId}/usage?startDate&endDate` - использование парковки клиентом
- GET `/revenue?period` - отчёт по доходам
- GET `/parking-duration` - статистика по длительности парковки
- POST `/custom` - генерация пользовательского отчёта
- GET `/export/{reportId}?format` - экспорт отчёта (PDF/Excel)

### 3. JWT Security для Client Service

#### Создан `JwtTokenProvider.java`
- Валидация JWT токенов от API Gateway
- Извлечение данных пользователя (username, userId, role)
- Использует тот же секретный ключ, что и API Gateway

#### Создан `JwtAuthenticationFilter.java`
- Фильтр для валидации JWT токенов во входящих запросах
- Автоматическое создание Spring Security authentication
- Установка контекста безопасности для защищённых эндпоинтов

#### Создан `SecurityConfig.java`
- Spring Security конфигурация для Client Service
- Stateless сессии (SessionCreationPolicy.STATELESS)
- Публичный доступ к `/actuator/**`, `/api/health`
- Требование аутентификации для всех остальных эндпоинтов

### 4. Зависимости Maven

#### Добавлено в `client-service/pom.xml`:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### 5. Конфигурация

#### Обновлён `client-service/application.yml`:
```yaml
security:
  jwt:
    secret: ${JWT_SECRET:ParkingSystemSecretKey2025!VeryLongAndSecureKey123456789}
```

#### Обновлён `docker-compose.yml`:
- Добавлена переменная окружения `JWT_SECRET` для:
  - `client-service`
  - `api-gateway`
- Добавлены database credentials для `api-gateway`

## Архитектура

```
Client → API Gateway (port 8086)
           ↓ (JWT forwarding via JwtRequestInterceptor)
           ├→ Client Service (port 8081) - JWT validation
           ├→ Billing Service (port 8083)
           ├→ Gate Control Service (port 8082)
           ├→ Management Service (port 8084)
           └→ Reporting Service (port 8085)
```

## Безопасность

1. **Единый секретный ключ JWT** (`JWT_SECRET`) используется всеми сервисами
2. **Автоматическое пересылание токенов** через RestTemplate Interceptor
3. **Валидация токенов** в каждом микросервисе через JWT фильтры
4. **Stateless аутентификация** - токены не хранятся на сервере
5. **Role-based access control** - роли пользователей передаются в JWT claims

## Тестирование

### 1. Аутентификация через API Gateway:
```bash
# Login
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Response: {"accessToken":"eyJhbGc...", "refreshToken":"..."}
```

### 2. Запрос к Client Service через Gateway:
```bash
curl -X GET http://localhost:8086/api/clients \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Запрос к Billing Service:
```bash
curl -X GET http://localhost:8086/api/billing/invoices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Следующие шаги

1. **Реализовать JWT security** для остальных микросервисов:
   - billing-service
   - gate-control-service
   - management-service
   - reporting-service

2. **Добавить Role-based access control** в каждый контроллер:
   ```java
   @PreAuthorize("hasRole('ADMIN')")
   ```

3. **Настроить Circuit Breaker** (Resilience4j) для отказоустойчивости

4. **Добавить Redis** для кэширования и blacklist токенов

5. **Настроить Rate Limiting** для защиты от DDoS

## Файлы изменений

### Созданные файлы:
- `backend/api-gateway/src/main/java/com/parking/api_gateway/config/JwtRequestInterceptor.java`
- `backend/api-gateway/src/main/java/com/parking/api_gateway/controller/BillingProxyController.java`
- `backend/api-gateway/src/main/java/com/parking/api_gateway/controller/GateControlProxyController.java`
- `backend/api-gateway/src/main/java/com/parking/api_gateway/controller/ManagementProxyController.java`
- `backend/api-gateway/src/main/java/com/parking/api_gateway/controller/ReportingProxyController.java`
- `backend/client-service/src/main/java/com/parking/client_service/security/JwtTokenProvider.java`
- `backend/client-service/src/main/java/com/parking/client_service/security/JwtAuthenticationFilter.java`
- `backend/client-service/src/main/java/com/parking/client_service/security/SecurityConfig.java`

### Изменённые файлы:
- `backend/api-gateway/src/main/java/com/parking/api_gateway/config/RestTemplateConfig.java`
- `backend/client-service/pom.xml`
- `backend/client-service/src/main/resources/application.yml`
- `docker-compose.yml`

## Сборка и деплой

```bash
# Сборка сервисов
cd backend/client-service && mvn clean install -DskipTests
cd backend/api-gateway && mvn clean install -DskipTests

# Сборка Docker образов
docker-compose build client-service api-gateway

# Запуск контейнеров
docker-compose up -d

# Проверка статуса
docker ps
docker logs api-gateway
docker logs client-service
```

## Мониторинг

Все сервисы экспортируют метрики через Actuator:
- API Gateway: http://localhost:8086/actuator/health
- Client Service: http://localhost:8081/actuator/health

