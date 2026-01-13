# Management Service

## Overview
Management Service provides endpoints for parking space management and monitoring. It allows external systems (like information display boards) to retrieve current parking space availability information.

**Architecture**: OpenAPI-first approach with auto-generated contracts and models.

## Features
- ✅ List all available parking spaces
- ✅ Filter available spaces by parking lot
- ✅ Get total count of available spaces
- ✅ Search spaces by type and status
- ✅ RESTful API with auto-generated Swagger documentation
- ✅ Database integration with PostgreSQL
- ✅ Comprehensive logging
- ✅ Unit and integration tests
- ✅ OpenAPI-first design (contract-driven development)

## Technology Stack
- **Framework**: Spring Boot 3.5.8
- **Language**: Java 21
- **Database**: PostgreSQL (production), H2 (tests)
- **ORM**: Spring Data JPA / Hibernate
- **API Contract**: OpenAPI 3.0.3 (contract-first)
- **Code Generation**: OpenAPI Generator Maven Plugin 7.6.0
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Build Tool**: Maven

## OpenAPI-First Approach

This service uses **contract-first development** - the API is defined in `openapi.yaml` and all interfaces and models are auto-generated during build.

### API Contract
**File**: `src/main/resources/openapi.yaml`

Defines:
- 5 REST endpoints for parking space management
- `ParkingSpaceResponse` schema with enums and nullable fields
- Request/response examples
- Error responses

### Generated Code
Maven plugin generates:
- `ParkingSpaceApi` interface (controller implements this)
- `ParkingSpaceResponse` model (DTOs)
- Validation annotations
- Swagger documentation

Location: `target/generated-sources/openapi/`

### Modifying the API
1. Edit `src/main/resources/openapi.yaml`
2. Run `mvn generate-sources` to regenerate code
3. Update controller/mapper if needed
4. Update tests
5. Commit both yaml and updated code

📖 **See**: `OPENAPI_MIGRATION.md` for detailed documentation

## Database
Uses `parking_spaces` table from the shared database schema.
Entity defined in `parking-common` module for reusability across services.

## API Endpoints

### Get Available Parking Spaces
```http
GET /api/management/spots/available
```
Returns all currently available parking spaces.

**Response**: 200 OK
```json
[
  {
    "spaceId": 1,
    "lotId": 1,
    "spaceNumber": "A-01",
    "level": 0,
    "section": "North",
    "type": "STANDARD",
    "status": "AVAILABLE",
    "hasCharger": false,
    "chargerType": null,
    "lengthCm": 500,
    "widthCm": 250,
    "hourlyRateOverride": 5.00,
    "dailyRateOverride": 40.00,
    "lastOccupiedAt": null
  }
]
```

### Get Available Spaces by Parking Lot
```http
GET /api/management/spots/available/lot/{lotId}
```
Returns available parking spaces for a specific parking lot.

### Get All Parking Spaces
```http
GET /api/management/spots
```
Returns all parking spaces regardless of status.

### Get Available Spaces Count
```http
GET /api/management/spots/available/count
```
Returns the total number of available spaces.

**Response**: 200 OK
```json
42
```

### Search Parking Spaces
```http
GET /api/management/spots/search?type={type}&status={status}
```
Search for parking spaces by type and/or status.

**Query Parameters**:
- `type` (optional): Space type (STANDARD, HANDICAPPED, ELECTRIC, VIP, COMPACT, OVERSIZED)
- `status` (optional): Space status (AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, OUT_OF_SERVICE)

## Configuration

### application.yml (Production)
```yaml
spring:
  application: 
    name: management-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:parking_db}
    username: ${DB_USER:parking_user}
    password: ${DB_PASSWORD:parking_pass}
  jpa:
    hibernate:
      ddl-auto: validate  # Only validate, don't modify schema
server:
  port: ${SERVER_PORT:8083}
```

### application-test.yml (Tests)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
  jpa:
    hibernate:
      ddl-auto: create-drop  # Auto-create schema for tests
```

## Project Structure
```
management-service/
├── src/
│   ├── main/
│   │   ├── java/com/parking/management_service/
│   │   │   ├── controller/
│   │   │   │   └── ManagementController.java      # REST endpoints
│   │   │   ├── service/
│   │   │   │   └── ManagementService.java         # Business logic
│   │   │   ├── repository/
│   │   │   │   └── ParkingSpaceRepository.java    # Data access
│   │   │   ├── dto/
│   │   │   │   └── ParkingSpaceResponseDto.java   # Response model
│   │   │   ├── mapper/
│   │   │   │   └── ParkingSpaceMapper.java        # Entity ↔ DTO
│   │   │   └── ManagementServiceApplication.java  # Main class
│   │   └── resources/
│   │       ├── application.yml                     # Main config
│   │       └── application-test.yml                # Test config
│   └── test/
│       └── java/com/parking/management_service/
│           ├── controller/
│           │   └── ManagementControllerTest.java   # Controller tests
│           └── service/
│               └── ManagementServiceIntegrationTest.java # Integration tests
└── pom.xml
```

## Dependencies
- `parking-common` - Shared entities and utilities
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - Database access
- `spring-boot-starter-security` - Security features
- `postgresql` - PostgreSQL driver
- `springdoc-openapi-starter-webmvc-ui` - Swagger UI
- `h2` (test scope) - In-memory database for tests

## Building and Running

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

Or with environment variables:
```bash
DB_HOST=localhost DB_PORT=5432 DB_NAME=parking_db \
DB_USER=parking_user DB_PASSWORD=parking_pass \
SERVER_PORT=8083 mvn spring-boot:run
```

### Access Swagger UI
```
http://localhost:8083/swagger-ui.html
```

## Testing

### Unit Tests
- `ManagementControllerTest` - Tests REST endpoints with mocked service layer
- Uses MockMvc for HTTP request simulation

### Integration Tests  
- `ManagementServiceIntegrationTest` - Tests with real H2 database
- Tests database operations and data mapping
- Includes edge cases and filtering scenarios

**Test Coverage**:
- 7 controller unit tests
- 8 service integration tests
- 1 application context test
- **Total: 16 tests, all passing** ✅

## Logging
Service uses SLF4J with emoji indicators for easy log filtering:
- 🔍 - Data retrieval operations
- ✅ - Successful operations
- ⚠️ - Warnings
- ❌ - Errors
- 🔢 - Counting operations

**Log Levels**:
- INFO: Important operations (API calls, counts)
- DEBUG: Detailed operations (queries, intermediate steps)

## Architecture

### Layer Separation
```
Controller Layer (REST)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Entity Layer (parking-common)
    ↓
PostgreSQL Database
```

### Data Flow
```
HTTP Request
    ↓
ManagementController (@RestController)
    ↓
ManagementService (@Service, @Transactional)
    ↓
ParkingSpaceRepository (JpaRepository)
    ↓
ParkingSpace Entity (parking-common)
    ↓
parking_spaces table
```

## Security Considerations
- Uses Spring Security for authentication (to be configured)
- Database credentials via environment variables
- No sensitive data logged
- Read-only transactions for data retrieval

## Performance
- Uses `@Transactional(readOnly = true)` for optimized database reads
- Efficient JPA queries with method names
- Connection pooling via HikariCP
- Hibernate second-level cache disabled (simple queries don't benefit)

## Future Enhancements
- [ ] Add pagination support for large datasets
- [ ] Implement caching (Redis) for frequently accessed data
- [ ] Add real-time WebSocket updates for space availability
- [ ] Add metrics and monitoring (Micrometer)
- [ ] Add rate limiting for public API access
- [ ] Implement API versioning

## Related Issues
- #18 - MANAGEMENT-SVC — GET /available (list available parking spaces) ✅

## Contact
Part of the Parking Lot Management System microservices architecture.

