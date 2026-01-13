# Client Service

Manages client information and vehicle registrations in the parking management system.

## Overview

The Client Service is responsible for:
- CRUD operations for client profiles
- CRUD operations for vehicles linked to clients
- Phone number and email uniqueness validation
- Client-vehicle relationship management

## Technology Stack

- **Spring Boot 3.5.8**
- **Spring Data JPA** - Database access
- **PostgreSQL** - Production database
- **H2** - Testing database
- **Eureka Client** - Service discovery
- **OpenAPI 3.0** - API specification and code generation
- **Hibernate** - ORM

## Features

✅ **Client Management (Issue #16)**
- Create, read, update, delete clients
- Unique phone number and email enforcement
- Full validation with appropriate HTTP status codes
- JWT-protected endpoints

✅ **Vehicle Management (Issue #17)**
- Create, read, update, delete vehicles
- Link vehicles to clients (client_id foreign key)
- Unique license plate enforcement
- Full validation with appropriate HTTP status codes

## API Endpoints

### Client Endpoints

All endpoints require JWT authentication via `Authorization: Bearer <token>` header.

#### POST /api/clients
Create a new client.

**Request Body:**
```json
{
  "fullName": "John Doe",
  "phoneNumber": "+380501234567",
  "email": "john.doe@example.com"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "fullName": "John Doe",
  "phoneNumber": "+380501234567",
  "email": "john.doe@example.com",
  "registeredAt": "2026-01-13T10:00:00Z"
}
```

**Validation:**
- Phone number must be unique (409 Conflict if duplicate)
- Email must be unique (409 Conflict if duplicate)
- All fields are required (400 Bad Request if missing)

---

#### GET /api/clients
Retrieve all clients.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "fullName": "John Doe",
    "phoneNumber": "+380501234567",
    "email": "john.doe@example.com",
    "registeredAt": "2026-01-13T10:00:00Z"
  }
]
```

---

#### GET /api/clients/{id}
Retrieve a specific client by ID.

**Response:** `200 OK`
```json
{
  "id": 1,
  "fullName": "John Doe",
  "phoneNumber": "+380501234567",
  "email": "john.doe@example.com",
  "registeredAt": "2026-01-13T10:00:00Z"
}
```

**Error:** `404 Not Found` if client doesn't exist

---

#### GET /api/clients/search
Search for a client by phone number.

**Query Parameters:**
- `phone` (required) - Phone number to search for

**Example:**
```bash
GET /api/clients/search?phone=%2B380501234567
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "fullName": "John Doe",
  "phoneNumber": "+380501234567",
  "email": "john.doe@example.com",
  "registeredAt": "2026-01-13T10:00:00Z"
}
```

**Error:** `404 Not Found` if client not found

---

#### PUT /api/clients
Update an existing client.

**Request Body:**
```json
{
  "id": 1,
  "fullName": "John Updated Doe",
  "phoneNumber": "+380501234567",
  "email": "john.new@example.com"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "fullName": "John Updated Doe",
  "phoneNumber": "+380501234567",
  "email": "john.new@example.com",
  "registeredAt": "2026-01-13T10:00:00Z"
}
```

**Error:** 
- `404 Not Found` if client doesn't exist
- `409 Conflict` if new phone/email conflicts with another client

---

#### DELETE /api/clients/{id}
Delete a client.

**Response:** `204 No Content`

**Error:** `404 Not Found` if client doesn't exist

---

### Vehicle Endpoints

#### POST /api/clients/{clientId}/vehicles
Create a new vehicle for a client.

**Path Parameters:**
- `clientId` - The ID of the client who owns the vehicle

**Request Body:**
```json
{
  "licensePlate": "AA1234BB",
  "model": "Toyota Camry",
  "color": "Blue"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "clientId": 1,
  "licensePlate": "AA1234BB",
  "model": "Toyota Camry",
  "color": "Blue",
  "registeredAt": "2026-01-13T10:00:00Z"
}
```

**Validation:**
- License plate must be unique (409 Conflict if duplicate)
- Client must exist (404 Not Found if invalid clientId)
- License plate is required (400 Bad Request if missing)

---

#### GET /api/vehicles
Retrieve all vehicles.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "clientId": 1,
    "licensePlate": "AA1234BB",
    "model": "Toyota Camry",
    "color": "Blue",
    "registeredAt": "2026-01-13T10:00:00Z"
  }
]
```

---

#### GET /api/vehicles/{id}
Retrieve a specific vehicle by ID.

**Response:** `200 OK`
```json
{
  "id": 1,
  "clientId": 1,
  "licensePlate": "AA1234BB",
  "model": "Toyota Camry",
  "color": "Blue",
  "registeredAt": "2026-01-13T10:00:00Z"
}
```

**Error:** `404 Not Found` if vehicle doesn't exist

---

#### GET /api/clients/{clientId}/vehicles
Retrieve all vehicles for a specific client.

**Path Parameters:**
- `clientId` - The ID of the client

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "clientId": 1,
    "licensePlate": "AA1234BB",
    "model": "Toyota Camry",
    "color": "Blue",
    "registeredAt": "2026-01-13T10:00:00Z"
  }
]
```

---

#### PUT /api/vehicles
Update an existing vehicle.

**Request Body:**
```json
{
  "id": 1,
  "clientId": 1,
  "licensePlate": "AA1234BB",
  "model": "Toyota Camry Updated",
  "color": "Red"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "clientId": 1,
  "licensePlate": "AA1234BB",
  "model": "Toyota Camry Updated",
  "color": "Red",
  "registeredAt": "2026-01-13T10:00:00Z"
}
```

**Error:**
- `404 Not Found` if vehicle doesn't exist
- `409 Conflict` if new license plate conflicts with another vehicle

---

#### DELETE /api/vehicles/{id}
Delete a vehicle.

**Response:** `204 No Content`

**Error:** `404 Not Found` if vehicle doesn't exist

---

## Examples via API Gateway

All requests go through API Gateway on port **8086**.

### Authentication

First, authenticate to get JWT token:

```bash
# curl
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'

# PowerShell
$authBody = @{
    username = "admin"
    password = "admin"
} | ConvertTo-Json

$authResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $authBody

$token = $authResponse.accessToken
```

### Client Examples

#### Create Client

```bash
# curl
curl -X POST http://localhost:8086/api/clients \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jane Smith",
    "phoneNumber": "+380679876543",
    "email": "jane.smith@example.com"
  }'

# PowerShell
$clientBody = @{
    fullName = "Jane Smith"
    phoneNumber = "+380679876543"
    email = "jane.smith@example.com"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8086/api/clients" `
  -Method POST `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body $clientBody
```

#### Get All Clients

```bash
# curl
curl -X GET http://localhost:8086/api/clients \
  -H "Authorization: Bearer $TOKEN"

# PowerShell
Invoke-RestMethod -Uri "http://localhost:8086/api/clients" `
  -Headers @{ Authorization = "Bearer $token" }
```

#### Search Client by Phone

```bash
# curl (note: + must be URL encoded as %2B)
curl -X GET "http://localhost:8086/api/clients/search?phone=%2B380679876543" \
  -H "Authorization: Bearer $TOKEN"

# PowerShell
$phone = "+380679876543"
$encodedPhone = [System.Web.HttpUtility]::UrlEncode($phone)
Invoke-RestMethod -Uri "http://localhost:8086/api/clients/search?phone=$encodedPhone" `
  -Headers @{ Authorization = "Bearer $token" }
```

### Vehicle Examples

#### Create Vehicle

```bash
# curl
curl -X POST http://localhost:8086/api/clients/1/vehicles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "BB5678CC",
    "model": "Honda Accord",
    "color": "White"
  }'

# PowerShell
$vehicleBody = @{
    licensePlate = "BB5678CC"
    model = "Honda Accord"
    color = "White"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8086/api/clients/1/vehicles" `
  -Method POST `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body $vehicleBody
```

#### Get All Vehicles

```bash
# curl
curl -X GET http://localhost:8086/api/vehicles \
  -H "Authorization: Bearer $TOKEN"

# PowerShell
Invoke-RestMethod -Uri "http://localhost:8086/api/vehicles" `
  -Headers @{ Authorization = "Bearer $token" }
```

#### Get Vehicles for Specific Client

```bash
# curl
curl -X GET http://localhost:8086/api/clients/1/vehicles \
  -H "Authorization: Bearer $TOKEN"

# PowerShell
Invoke-RestMethod -Uri "http://localhost:8086/api/clients/1/vehicles" `
  -Headers @{ Authorization = "Bearer $token" }
```

---

## Database Schema

### clients Table
```sql
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### vehicles Table
```sql
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    model VARCHAR(100),
    color VARCHAR(50),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);
```

---

## JWT Authentication

This service validates JWT tokens but does not issue them. Tokens are issued by the API Gateway.

### JWT Configuration

**application.yml:**
```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 hours
```

**Environment Variable:**
```bash
JWT_SECRET=ParkingSystemSecretKey2025!VeryLongAndSecureKey123456789ForDevelopmentOnlyChangeInProduction
```

### Protected Endpoints

All endpoints require valid JWT token with:
- Valid signature
- Not expired
- Contains username and role claims

### Public Endpoints

None - all Client Service endpoints are protected.

---

## Running Tests

### Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ClientServiceTest
mvn test -Dtest=VehicleServiceTest

# Run with coverage
mvn test jacoco:report
```

### Integration Tests (MockMvc)

```bash
# Controller tests
mvn test -Dtest=ClientControllerTest
mvn test -Dtest=VehicleControllerTest
```

### Test Coverage

**ClientServiceTest:**
- ✅ Create client (happy path)
- ✅ Get client by ID (happy path)
- ✅ Update client (happy path)
- ✅ Client not found (404)
- ✅ Duplicate phone number (409)
- ✅ Duplicate email (409)

**ClientControllerTest:**
- ✅ POST - valid request (201)
- ✅ POST - missing field (400)
- ✅ POST - duplicate phone (409)
- ✅ GET - not found (404)
- ✅ PUT - success (200)
- ✅ DELETE - success (204)

**VehicleServiceTest:**
- ✅ Create vehicle (happy path)
- ✅ Get vehicle by ID (happy path)
- ✅ Update vehicle (happy path)
- ✅ Vehicle not found (404)
- ✅ Duplicate license plate (409)

**VehicleControllerTest:**
- ✅ POST - valid request (201)
- ✅ POST - missing license plate (400)
- ✅ POST - duplicate plate (409)
- ✅ GET - not found (404)
- ✅ PUT - success (200)
- ✅ DELETE - success (204)

**Total:** 20+ test cases

---

## Configuration

### application.yml

```yaml
spring:
  application:
    name: client-service
  datasource:
    url: jdbc:postgresql://postgres:5432/parking_db
    username: ${DB_USERNAME:parking_user}
    password: ${DB_PASSWORD:parking_pass}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    prefer-ip-address: true

server:
  port: 8081

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

### Environment Variables

```bash
# Database
DB_USERNAME=parking_user
DB_PASSWORD=parking_pass

# JWT
JWT_SECRET=ParkingSystemSecretKey2025!VeryLongAndSecureKey123456789ForDevelopmentOnlyChangeInProduction

# Eureka
EUREKA_URI=http://eureka-server:8761/eureka/
```

---

## Architecture

### OpenAPI-First Approach

This service uses OpenAPI 3.0 specification for API contract definition:
- API contract: `src/main/resources/openapi.yaml`
- Auto-generated interfaces and DTOs during Maven build
- Controllers implement generated interfaces
- Ensures API contract consistency

### Domain Model

**Structure:** Hibernate Entity → Domain Model → DTO

1. **Hibernate Entity** (`parking-common`)
   - JPA entities: `Client`, `Vehicle`
   - Database mapping with annotations
   
2. **Domain Model** (service layer)
   - `ClientDomain`, `VehicleDomain`
   - Business logic layer
   - Wraps entities for service operations
   
3. **DTO** (controller layer)
   - Generated from OpenAPI specification
   - Request/Response models
   - Validation annotations

### Logging

```java
@Slf4j
public class ClientService {
    public ClientDomain createClient(ClientRequestDto dto) {
        log.info("Creating client: {}", dto.getFullName());
        // ...
        log.debug("Client created with ID: {}", client.getId());
    }
}
```

---

## Development

### Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL 16+ (or Docker)

### Local Development

```bash
# 1. Start PostgreSQL
docker run -d --name postgres \
  -e POSTGRES_DB=parking_db \
  -e POSTGRES_USER=parking_user \
  -e POSTGRES_PASSWORD=parking_pass \
  -p 5432:5432 \
  postgres:16-alpine

# 2. Set environment variables
export JWT_SECRET=ParkingSystemSecretKey2025!VeryLongAndSecureKey123456789ForDevelopmentOnlyChangeInProduction
export DB_USERNAME=parking_user
export DB_PASSWORD=parking_pass

# 3. Run service
mvn spring-boot:run
```

### Docker Build

```bash
# Build JAR
mvn clean package -DskipTests

# Build Docker image
docker build -t client-service:latest .

# Run container
docker run -d \
  -e JWT_SECRET=$JWT_SECRET \
  -e DB_USERNAME=parking_user \
  -e DB_PASSWORD=parking_pass \
  -p 8081:8081 \
  client-service:latest
```

---

## Troubleshooting

### Common Issues

**Issue:** JWT token validation fails (403 Forbidden)  
**Solution:** Ensure JWT_SECRET matches across all services

**Issue:** Duplicate phone number error  
**Solution:** Phone numbers must be unique - use different number or update existing client

**Issue:** Cannot find client by phone  
**Solution:** Ensure phone number is URL-encoded (+ becomes %2B)

**Issue:** Database connection refused  
**Solution:** Check PostgreSQL is running and credentials are correct

---

## Related Documentation

- **API Gateway Proxy Examples:** `docs/API_GATEWAY_PROXY_EXAMPLES.md`
- **Authentication Guide:** `docs/AUTHENTICATION.md`
- **Database Migration:** `docs/DATABASE_MIGRATION_EXPLAINED.md`
- **Vehicle CRUD Implementation:** `VEHICLE_CRUD_IMPLEMENTATION.md`

---

## Issues

- **Issue #16:** CLIENT-SVC — CRUD for CLIENTS ✅ Complete
- **Issue #17:** CLIENT-SVC — CRUD for VEHICLES ✅ Complete

---

**Service Port:** 8081  
**API Gateway Port:** 8086  
**Version:** 1.0.0  
**Last Updated:** 2026-01-13

