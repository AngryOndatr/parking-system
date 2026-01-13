# Reporting Service

Centralized logging and reporting service for the parking management system.

## Overview

The Reporting Service is responsible for:
- Collecting and persisting system logs from all microservices
- Providing query capabilities for log data
- Future reporting and analytics functionality

## Technology Stack

- **Spring Boot 3.5.8**
- **Spring Data JPA** - Database access
- **PostgreSQL** - Production database
- **H2** - Testing database
- **Eureka Client** - Service discovery
- **OpenAPI 3.0** - API specification and code generation
- **Hibernate** - ORM with JSON support

## API Endpoints

### POST /api/reporting/log
Create a new system log entry.

**Request Body:**
```json
{
  "timestamp": "2026-01-13T10:30:00Z",
  "level": "INFO",
  "service": "client-service",
  "message": "Client created successfully",
  "userId": 1,
  "meta": {
    "action": "CREATE",
    "clientId": 123
  }
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "timestamp": "2026-01-13T10:30:00Z",
  "level": "INFO",
  "service": "client-service",
  "message": "Client created successfully",
  "userId": 1,
  "meta": {
    "action": "CREATE",
    "clientId": 123
  }
}
```

### GET /api/reporting/logs
Retrieve all log entries.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "timestamp": "2026-01-13T10:30:00Z",
    "level": "INFO",
    "service": "client-service",
    "message": "Client created successfully",
    "userId": 1,
    "meta": {...}
  }
]
```

## Database Schema

The service uses the `logs` table:

| Column       | Type          | Description                        |
|-------------|---------------|------------------------------------|
| id          | BIGSERIAL     | Primary key                        |
| timestamp   | TIMESTAMP     | When the log entry was created     |
| log_level   | VARCHAR(50)   | Log level (INFO, WARN, ERROR, etc.)|
| service     | VARCHAR(100)  | Service that generated the log     |
| message     | TEXT          | Log message content                |
| user_id     | BIGINT        | Optional user ID                   |
| meta        | TEXT/JSON     | Additional metadata as JSON        |

## Architecture

### Domain Model
The service follows a layered architecture:
- **Entity Layer** (`parking-common`) - JPA entities
- **Domain Layer** - Business logic wrappers around entities
- **Service Layer** - Business logic and transaction management
- **Controller Layer** - REST API implementation
- **DTO Layer** - Generated from OpenAPI specification

### Key Components

#### LogDomain
Domain model wrapping the `Log` entity with business logic methods:
- `isValid()` - Validates required fields
- `isError()` - Checks if log level is ERROR
- `hasMeta()` - Checks if metadata exists

#### ReportingService
Business logic for log management:
- `createLog()` - Persist new log entries
- `getAllLogs()` - Retrieve all logs
- `getLogsByService()` - Filter by service name
- `getLogsByLevel()` - Filter by log level

#### LogMapper
Maps between DTOs and domain models, handling JsonNullable fields from OpenAPI generator.

## Configuration

### application.yml
```yaml
server:
  port: 8084

spring:
  application:
    name: reporting-service
  datasource:
    url: jdbc:postgresql://postgres:5432/parking_db
    username: parking_user
    password: parking_pass
  jpa:
    hibernate:
      ddl-auto: validate

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

### application-test.yml
Test profile uses H2 in-memory database and disables Eureka.

## Security & Authentication

### JWT Authentication

The Reporting Service uses JWT (JSON Web Token) authentication for all API endpoints:

- **Authentication Filter:** `JwtAuthenticationFilter` intercepts all requests
- **Token Validation:** `JwtTokenProvider` validates JWT tokens using HS512 algorithm
- **Security Config:** Spring Security requires authentication for all `/api/reporting/**` endpoints

**Important:** JWT secret must be **at least 64 characters** (512 bits) for HS512 algorithm.

#### JWT Configuration

**application.yml:**
```yaml
security:
  jwt:
    secret: ${JWT_SECRET:ParkingSystemSecretKey2025!VeryLongAndSecureKey123456789ForDevelopmentOnlyChangeInProduction}
```

**docker-compose.yml:**
```yaml
reporting-service:
  environment:
    JWT_SECRET: ParkingSystemSecretKey2025!VeryLongAndSecureKey123456789ForDevelopmentOnlyChangeInProduction
```

**⚠️ CRITICAL:** All services (api-gateway, client-service, reporting-service) **MUST** use the **SAME** JWT secret!

#### Authentication Flow

```
1. Client → API Gateway: Request with JWT in Authorization header
2. API Gateway: Validates JWT, proxies to reporting-service
3. Reporting Service:
   - JwtAuthenticationFilter extracts and validates token
   - Extracts username, role, userId from token
   - Sets Authentication in SecurityContext
4. Controller: Processes authenticated request
5. Response: Returns data to client
```

### Protected Endpoints

All reporting endpoints require JWT authentication:
- `POST /api/reporting/log` - Create log (requires valid JWT)
- `GET /api/reporting/logs` - List logs (requires valid JWT)
- `GET /api/reporting/logs?level=ERROR` - Filter logs (requires valid JWT)

### Public Endpoints

- `GET /actuator/health` - Health check (no authentication)
- `GET /actuator/prometheus` - Metrics (no authentication)

## Jackson Configuration

### JsonNullable Support

The service uses OpenAPI-generated models with `JsonNullable` fields. This requires special Jackson configuration:

**JacksonConfig.java:**
```java
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.registerModule(new JsonNullableModule());
        return objectMapper;
    }
}
```

**Required dependency:**
```xml
<dependency>
    <groupId>org.openapitools</groupId>
    <artifactId>jackson-databind-nullable</artifactId>
    <version>0.2.6</version>
</dependency>
```

**Without this configuration, POST requests will fail with:**
```
Cannot construct instance of org.openapitools.jackson.nullable.JsonNullable
```

## Running Locally

### Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL (or use Docker)

## Troubleshooting

### Common Issues

#### 1. JWT Secret Too Short

**Error:**
```
The verification key's size is 448 bits which is not secure enough for the HS512 algorithm
```

**Solution:** Ensure JWT secret is at least 64 characters (512 bits).

#### 2. JWT Signature Mismatch

**Error:**
```
JWT signature does not match locally computed signature
```

**Solution:** Verify that ALL services use the SAME `JWT_SECRET` in docker-compose.yml.

#### 3. JsonNullable Deserialization Error

**Error:**
```
Cannot construct instance of org.openapitools.jackson.nullable.JsonNullable
```

**Solution:** 
- Add `jackson-databind-nullable` dependency
- Create `JacksonConfig` with `JsonNullableModule` registration

#### 4. 403 Forbidden

**Error:** POST/GET requests return 403 Forbidden

**Possible Causes:**
- Missing JWT token in Authorization header
- Invalid or expired JWT token  
- JWT secret mismatch between services
- SecurityConfig blocking requests

**Solution:**
- Ensure JWT token is sent: `Authorization: Bearer <token>`
- Verify token is valid (login again if needed)
- Check JWT_SECRET consistency across all services
- Verify SecurityConfig allows `.authenticated()` users

#### 5. Spring Cloud Compatibility Warning

**Warning:**
```
Spring Boot [3.5.8] is not compatible with this Spring Cloud release train
```

**Solution:** Add to application.yml:
```yaml
spring:
  cloud:
    compatibility-verifier:
      enabled: false
```

### Debugging

#### Enable Debug Logging

**application.yml:**
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    com.parking.reporting_service.security: DEBUG
    com.parking.reporting_service.controller: DEBUG
```

#### Check JWT Token Claims

Use online JWT decoder (e.g., jwt.io) to inspect token contents:
- `sub` - username
- `role` - user role (ADMIN, USER, etc.)
- `userId` - numeric user ID
- `exp` - expiration timestamp

#### Verify Service Registration

Check Eureka dashboard: `http://localhost:8761/`
- Reporting Service should appear as "REPORTING-SERVICE"
- Status should be "UP"

## Running Locally

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Run Application
```bash
mvn spring-boot:run
```

The service will be available at `http://localhost:8084`

## Running with Docker

The service is included in the main docker-compose setup:

```bash
# From project root
docker-compose up reporting-service
```

## API Documentation

Interactive API documentation is available via Swagger UI:
- Local: `http://localhost:8084/swagger-ui.html`
- Docker: `http://reporting-service:8084/swagger-ui.html`

OpenAPI specification:
- `http://localhost:8084/v3/api-docs`

## Testing

### Unit Tests
- **ReportingServiceTest** - Service layer logic
- **ReportingControllerTest** - Controller endpoints with MockMvc

### Integration Tests
- **ReportingServiceIntegrationTest** - Full database integration

Run tests:
```bash
mvn test
```

## Development Guidelines

### Adding New Log Fields
1. Update `Log` entity in `parking-common`
2. Create Flyway migration in `api-gateway/src/main/resources/db/migration/`
3. Update `openapi.yaml` with new fields
4. Regenerate code: `mvn clean compile`
5. Update mapper and tests

### OpenAPI Code Generation
The project uses OpenAPI Generator to create:
- Controller interfaces (`ReportingApi`)
- DTO models (`LogRequest`, `LogResponse`)

Generated code is in `target/generated-sources/openapi/`

Controllers implement generated interfaces for contract-first development.

## Future Enhancements

- [ ] Add filtering and pagination to GET /logs endpoint
- [ ] Implement log aggregation and statistics
- [ ] Add real-time log streaming via WebSocket
- [ ] Create scheduled reports generation
- [ ] Add log retention policies
- [ ] Implement log search with Elasticsearch

## Monitoring

The service exposes actuator endpoints:
- `/actuator/health` - Health check
- `/actuator/info` - Service information
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics

## Troubleshooting

### Common Issues

**Issue:** Service fails to start - "Cannot connect to PostgreSQL"
**Solution:** Ensure PostgreSQL is running and accessible at the configured URL

**Issue:** Tests fail with JSONB errors
**Solution:** The entity uses JSON (not JSONB) for H2 compatibility in tests

**Issue:** OpenAPI code not generated
**Solution:** Run `mvn clean compile` to trigger code generation

## License

Part of the Parking Management System - Internal Use Only

