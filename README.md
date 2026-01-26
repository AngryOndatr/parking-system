Markdown
# 🅿️ Parking System - Microservices Architecture

Modern parking lot management system built on microservices architecture using Spring Boot, Spring Cloud, and Docker.

## 🆕 Latest Updates

> **Показаны последние 3 обновления.** Полная история: [CHANGELOG.md](./CHANGELOG.md) | [Session Logs](./docs/sessions/)

### 2026-01-26 - Gate Control Service: Entry REST Endpoint (Issue #50) ✅

✅ **Gate Control Service - Entry REST API Complete** (Issue #50)
- ✅ POST /api/v1/gate/entry endpoint with OpenAPI-first implementation
- ✅ GateController implementing GateApi interface from OpenAPI specification
- ✅ EntryRequest DTO with validation (license plate pattern, entry method, gate ID)
- ✅ EntryResponse DTO with JsonNullable for optional ticket code
- ✅ 5 comprehensive integration tests - **ALL PASSING** ✅
  - Subscriber entry returns 201 without ticket
  - One-time visitor returns 201 with unique ticket code
  - Invalid license plate format returns 400
  - Missing required fields returns 400
  - Manual entry with operator ID succeeds
- ✅ TestSecurityConfig for integration tests (permits all requests in test profile)
- ✅ Full request/response validation with proper HTTP status codes
- ✅ SLF4J logging with emojis for all operations
- 📖 **Controller:** [GateController.java](./backend/gate-control-service/src/main/java/com/parking/gate_control_service/controller/GateController.java)
- 📖 **Tests:** [GateControllerIntegrationTest.java](./backend/gate-control-service/src/test/java/com/parking/gate_control_service/controller/GateControllerIntegrationTest.java)
- 📖 **OpenAPI:** [openapi.yaml](./backend/gate-control-service/src/main/resources/openapi.yaml)
- 🎯 **Next Steps:** Exit decision logic and REST endpoint

### 2026-01-26 - Gate Control Service: Entry Decision Logic (Issue #49) ✅

✅ **Gate Control Service - Entry Decision Logic** (Issue #49)
- ✅ GateService with processEntry(licensePlate) decision logic
- ✅ EntryDecision DTO with action (OPEN/DENY), message, and ticket code
- ✅ Subscriber path: automatic gate opening without ticket generation
- ✅ Visitor path: unique ticket generation (TICKET-{timestamp}-{random})
- ✅ GateEvent logging for all entry decisions (ENTRY, OPEN/DENY with reason)
- ✅ 5 comprehensive unit tests - **ALL PASSING** ✅
  - Subscriber grants access without ticket
  - One-time visitor generates ticket and grants access
  - Client service called exactly once for both paths
  - Multiple visitors receive unique tickets
- ✅ Integration with ClientServiceClient and GateEventRepository
- 📖 **Service:** [GateService.java](./backend/gate-control-service/src/main/java/com/parking/gate_control_service/service/GateService.java)
- 📖 **DTO:** [EntryDecision.java](./backend/gate-control-service/src/main/java/com/parking/gate_control_service/dto/EntryDecision.java)
- 📖 **Tests:** [GateServiceTest.java](./backend/gate-control-service/src/test/java/com/parking/gate_control_service/service/GateServiceTest.java)

### 2026-01-26 - Gate Control Service: Client Service Integration (Issue #48) ✅

✅ **Gate Control Service - Client Service Integration** (Issue #48)
- ✅ ClientServiceClient with WebClient for subscription validation
- ✅ SubscriptionCheckResponse DTO with access grant status
- ✅ Fail-safe error handling: all errors result in access denial
- ✅ 5 comprehensive unit tests with MockWebServer - **ALL PASSING** ✅
  - Active subscription validation (200 OK)
  - Subscription not found handling (404)
  - Inactive subscription handling
  - Server error handling (500)
  - Network error/timeout handling
- ✅ SLF4J logging for all requests and errors
- ✅ Integration with existing WebClientConfig
- 📖 **Client:** [ClientServiceClient.java](./backend/gate-control-service/src/main/java/com/parking/gate_control_service/client/ClientServiceClient.java)
- 📖 **DTO:** [SubscriptionCheckResponse.java](./backend/gate-control-service/src/main/java/com/parking/gate_control_service/dto/SubscriptionCheckResponse.java)
- 📖 **Tests:** [ClientServiceClientTest.java](./backend/gate-control-service/src/test/java/com/parking/gate_control_service/client/ClientServiceClientTest.java)

### 2026-01-26 - Gate Control Service: GateEvent Entity Implementation (Issue #46) ✅

✅ **Gate Control Service - Entity Layer** (Issue #46)
- ✅ GateEvent JPA entity with two enums (EventType: ENTRY/EXIT/MANUAL_OPEN/ERROR, Decision: OPEN/DENY)
- ✅ GateEventRepository with custom query methods (findByLicensePlateOrderByTimestampDesc, findByTimestampBetween)
- ✅ Flyway migration V9: gate_events table with indexes and constraints
- ✅ 5 comprehensive integration tests - **ALL PASSING** ✅
- ✅ Test configuration with H2 in-memory database
- ✅ Domain model architecture: Hibernate -> Domain model <- DTO
- 📖 **Entity:** [GateEvent.java](./backend/gate-control-service/src/main/java/com/parking/gate_control_service/entity/GateEvent.java)
- 📖 **Repository:** [GateEventRepository.java](./backend/gate-control-service/src/main/java/com/parking/gate_control_service/repository/GateEventRepository.java)
- 📖 **Tests:** [GateEventRepositoryTest.java](./backend/gate-control-service/src/test/java/com/parking/gate_control_service/repository/GateEventRepositoryTest.java)
- 🎯 **Next Steps:** WebClient configuration for inter-service communication (Issue #47)

### 2026-01-24 - Billing Service: Payment Status & Recording Endpoints (Issues #34, #35, #36) ✅

✅ **Billing Service - Complete REST API Implementation** (Issues #34, #35, #36)
- ✅ POST /api/v1/billing/calculate endpoint - fee calculation with OpenAPI validation
- ✅ POST /api/v1/billing/pay endpoint - payment recording with transaction ID generation
- ✅ GET /api/v1/billing/status endpoint - comprehensive payment status check with remaining fee calculation
- ✅ OpenAPI-first REST controller implementing BillingApi interface
- ✅ FeeCalculationRequest/Response, PaymentRequest/Response, PaymentStatusResponse DTOs
- ✅ BillingMapper for comprehensive DTO <-> Entity <-> Domain transformations
- ✅ Global exception handler with proper HTTP status codes (400, 404, 409, 500)
- ✅ Payment validation: insufficient amount detection, duplicate payment prevention
- ✅ Payment status tracking: support for paid/unpaid tickets with history of all payment attempts
- ✅ 10 integration tests covering all success/error scenarios - **ALL PASSING** ✅
- ✅ Full OpenAPI documentation available via Swagger UI
- 📊 **Total Test Coverage:** 57 tests (19 repository + 28 service + 10 integration) - **100% passing**
- 📖 **Controller:** [BillingController.java](./backend/billing-service/src/main/java/com/parking/billing/controller/BillingController.java)
- 📖 **Tests:** [BillingControllerIntegrationTest.java](./backend/billing-service/src/test/java/com/parking/billing/controller/BillingControllerIntegrationTest.java)
- 🎉 **Status:** Phase 2 Billing Service **100% COMPLETE** - Ready for Gate Control Service integration!

### 2026-01-18 - Billing Service: Complete Implementation (Issues #32, #33) ✅

✅ **Billing Service - Service Layer Complete** (Issue #33)
- ✅ BillingService with fee calculation logic (hourly rate + rounding up)
- ✅ Payment recording with validation and unique transaction ID generation (TRX-{timestamp}-{random})
- ✅ Domain models (ParkingEventDomain, PaymentDomain, TariffDomain)
- ✅ BillingMapper for Entity <-> DTO transformation
- ✅ Custom exceptions (ParkingEventNotFound, TicketAlreadyPaid, InsufficientPayment, TariffNotFound)
- ✅ 28 unit tests (BillingService + mapper) - all passing
- 📊 **Test Coverage:** Service ~95%, Repository ~90%
- 📖 **Service:** [BillingService.java](./backend/billing-service/src/main/java/com/parking/billing/service/BillingService.java)

### 2026-01-18 - Billing Service: ParkingEvent & Payment Entities (Issue #32) ✅

✅ **Billing Service - Entity & Repository Layer** (Issue #32)
- ✅ ParkingEvent entity with entry/exit tracking and method enums (SCAN, MANUAL, AUTO)
- ✅ Payment entity with status tracking (PENDING, COMPLETED, FAILED, REFUNDED) and transaction management
- ✅ ParkingEventRepository with custom queries (findByTicketCode, findByLicensePlateAndExitTimeIsNull, findByEntryTimeBetween)
- ✅ PaymentRepository with payment status queries (findByParkingEventIdAndStatus, findByTransactionId)
- ✅ 18 repository integration tests - all green
- ✅ @PrePersist hooks for automatic timestamp initialization
- 📖 **Entities:** [ParkingEvent.java](./backend/billing-service/src/main/java/com/parking/billing/entity/ParkingEvent.java), [Payment.java](./backend/billing-service/src/main/java/com/parking/billing/entity/Payment.java)
- 📖 **Repositories:** [ParkingEventRepository.java](./backend/billing-service/src/main/java/com/parking/billing/repository/ParkingEventRepository.java), [PaymentRepository.java](./backend/billing-service/src/main/java/com/parking/billing/repository/PaymentRepository.java)

### 2026-01-16 - Phase 2: Database Extensions & API Contracts (Issues #24-26) ✅

✅ **Database - TARIFFS & Extended Tables** (Issues #24, #25)
- ✅ V7 migration: TARIFFS table with 4 seed tariffs (ONE_TIME, DAILY, NIGHT, VIP)
- ✅ V8 migration: Extended PARKING_EVENTS (license_plate, entry/exit_method, is_subscriber)
- ✅ V8 migration: Extended PAYMENTS (status, transaction_id, operator_id)
- ✅ 9 new indexes for performance optimization
- 📖 **Migration Details**: [database/README.md](./database/README.md)

✅ **API Contracts - Billing & Gate Control** (Issue #26)
- ✅ OpenAPI 3.0.3 specification for Billing Service (3 endpoints)
- ✅ OpenAPI 3.0.3 specification for Gate Control Service (3 endpoints)
- 📖 **API Contracts:** [docs/api-contracts.md](./docs/api-contracts.md)


---

## 📈 Project Status & Roadmap

### Current Status: Phase 2 - Week 7 of 12 🚀

```
Фаза 0: ████████████████████ 100% ✅ ЗАВЕРШЕНА
Фаза 1: ████████████████████ 100% ✅ ЗАВЕРШЕНА
Фаза 2: ███████████████████░  93% 🔄 В ПРОЦЕССЕ (Gate Control Integration!)
Фаза 3: ░░░░░░░░░░░░░░░░░░░░   0% ⏳ ОЖИДАЕТ
Фаза 4: ░░░░░░░░░░░░░░░░░░░░   0% ⏳ ОЖИДАЕТ
Фаза 5: ░░░░░░░░░░░░░░░░░░░░   0% ⏳ ОЖИДАЕТ

Общий прогресс: ██████████████░ 68% (32/35 задач)
```

### 📋 Project Phases Overview

| Phase | Duration | Status | Progress | Description |
|-------|----------|--------|----------|-------------|
| **Phase 0** | 1 week | ✅ Complete | 100% | Infrastructure & Foundation |
| **Phase 1** | 3 weeks | ✅ Complete | 100% | Basic Backend (CRUD & DB) |
| **Phase 2** | 2 weeks | 🔄 In Progress | 90% | Core Business Logic (Gate Control Started!) |
| **Phase 3** | 2 weeks | ⏳ Pending | 0% | Integration & Security |
| **Phase 4** | 3 weeks | ⏳ Pending | 0% | Frontend, Reports & E2E |
| **Phase 5** | 1 week | ⏳ Pending | 0% | Finalization & Deployment |

📖 **Детальная дорожная карта:** [PROJECT_PHASES.md](./docs/PROJECT_PHASES.md)

### 🎯 Current Sprint Goals (Week 6)

**Phase 2 - Core Business Logic:** 🔄 **В ПРОЦЕССЕ (90%)**

**Current Focus: Gate Control Service**
- ✅ Issue #46: GateEvent entity & repository - **COMPLETED**
- 🔄 Issue #47: WebClient configuration for inter-service calls - **IN PROGRESS**
- ⏳ Issue #48: Entry decision logic service
- ⏳ Issue #49: Exit decision logic service

**Completed This Week:**
- ✅ GateEvent JPA entity with enums (EventType, Decision)
- ✅ GateEventRepository with custom queries
- ✅ Flyway migration V9: gate_events table
- ✅ 5 repository integration tests

**Next Tasks:**
1. Configure WebClient beans for Client, Billing, Management, Reporting services (Issue #47)
2. Implement entry decision logic with subscription validation (Issue #48)
3. Implement exit decision logic with payment verification (Issue #49)
- ✅ Client Service: CRUD + subscription check
- ✅ Management Service: available spots + status update
- ✅ Reporting Service: log storage
- ✅ API Gateway proxy verification
- ✅ Tests & documentation

**Phase 2 - Core Business Logic:** 🔄 **В ПРОЦЕССЕ (90%)**
- ✅ TARIFFS table migration (Issue #24)
- ✅ PARKING_EVENTS & PAYMENTS extensions (Issue #25)
- ✅ API Contracts documentation (Issue #26)
- ✅ Tariff entity implementation (Issue #31)
- ✅ ParkingEvent & Payment entities (Issue #32)
- ✅ Billing Service: fee calculation & payment processing (Issue #33)
- ✅ Billing Service: fee calculation endpoint /calculate (Issue #34)
- ✅ Billing Service: payment recording endpoint /pay (Issue #35)
- ✅ Billing Service: payment status endpoint /status (Issue #36) ⭐ **NEW**
- ⏳ Gate Control Service: entry/exit logic (Issue #37)
- ⏳ Inter-service communication (Issue #38)

### 📊 Quick Stats

| Metric | Value |
|--------|-------|
| **Total Issues** | 35 |
| **Closed Issues** | 30 (86%) |
| **Microservices** | 9 |
| **Phase 1** | ✅ 100% Complete |
| **Phase 2** | 🔄 85% In Progress |
| **API Endpoints** | 54+ |
| **Tests** | 100+ |
| **DB Migrations** | 8 |
| **Code Coverage** | ~90% avg |

### Recent Achievements

**2026-01-24 - Billing Service REST API Complete (Issues #34, #35, #36)**
- ✅ OpenAPI-first BillingController implementing BillingApi interface
- ✅ POST /api/v1/billing/calculate - fee calculation endpoint
- ✅ POST /api/v1/billing/pay - payment recording endpoint
- ✅ GET /api/v1/billing/status - comprehensive payment status endpoint with remaining fee ⭐ **NEW**
- ✅ FeeCalculationRequest/Response, PaymentRequest/Response, PaymentStatusResponse DTOs
- ✅ BillingMapper enhancements for comprehensive transformations
- ✅ GlobalExceptionHandler with proper HTTP status codes (400, 404, 409, 500)
- ✅ 10 integration tests - all passing (7 core + 3 for status)
- ✅ Payment status tracking with history of all payment attempts
- 📊 **Total Test Coverage:** 57 tests (100% passing)
- 🎉 **Billing Service 100% COMPLETE** - Ready for Gate Control integration!

**2026-01-18 - Billing Service Complete (Issues #32, #33)**
- ✅ ParkingEvent & Payment JPA entities with @PrePersist hooks
- ✅ Repositories with custom queries (18 tests)
- ✅ BillingService with fee calculation & payment logic
- ✅ Domain models & mapper implementation
- ✅ Custom exceptions for business logic errors
- ✅ 38 new unit tests (all passing) - Total: 80+ tests
- 📊 Coverage: Service ~95%, Controller ~85%, Repository ~90%

**2026-01-16 - Phase 2 Database & API Contracts (Issues #24-26)**
- ✅ V7 migration: TARIFFS table with 4 seed tariffs
- ✅ V8 migration: Extended PARKING_EVENTS & PAYMENTS
- ✅ OpenAPI 3.0.3 specs for Billing & Gate Control

### Next Steps

**Immediate (This Week):**
1. ✅ ~~Complete Billing Service implementation (Issues #32, #33, #34, #35, #36)~~ - DONE
2. Implement Gate Control Service (Issue #37) - POST /entry, POST /exit, GET /status
3. Add inter-service communication (Issue #38) - Billing <-> Gate Control

**Upcoming (Next 2 Weeks):**
1. Complete Phase 2: Business logic implementation
2. Service-to-service communication with WebClient
3. Begin Phase 3: Security & Integration (JWT, Spring Security)

## 🏗️ System Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│    Frontend     │───▶│   API Gateway    │───▶│  Microservices  │
│                 │    │                  │    │                 │
│ React/Angular   │    │ • Route Mapping  │    │ • Client Svc    │
│ Mobile App      │    │ • Load Balancer  │    │ • User Svc      │
│ Admin Panel     │    │ • CORS Handler   │    │ • Parking Svc   │
└─────────────────┘    │ • Monitoring     │    │ • Booking Svc   │
                       └──────────────────┘    │ • Payment Svc   │
                                │              │ • Billing Svc   │
                                ▼              │ • Gate Ctrl Svc │
                       ┌──────────────────┐    │ • Management    │
                       │ Service Registry │    │ • Reporting     │
                       │  (Eureka Server) │    └─────────────────┘
                       └──────────────────┘             │
                                                        ▼
                                               ┌─────────────────┐
                                               │   PostgreSQL    │
                                               │    Database     │
                                               └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21+
- Maven 3.8+

### Launch System
```bash
# Clone repository
git clone <repository-url>
cd parking-system

# Start all services
docker-compose up -d

# Check status
docker-compose ps
```

### Service Access
- **API Gateway**: http://localhost:8086
- **Eureka Server**: http://localhost:8761
- **Client Service**: http://localhost:8081 (via Gateway)
- **Management Service**: http://localhost:8083 (via Gateway)
- **Test Interface**: [devops/test-login.html](./devops/test-login.html) - Browser-based API tester
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686
- **pgAdmin**: http://localhost:5050 (admin@parking.com/admin)
- **PostgreSQL**: localhost:5433 (parking_db/postgres/postgres)

## 🔧 Microservices

### 1. API Gateway (Port 8086)
- Centralized entry point
- JWT authentication and authorization
- Request routing to microservices
- Security features:
  - Rate limiting (100 req/min per IP)
  - Brute force protection (5 failed attempts)
  - Suspicious IP detection
  - Security audit logging
- CORS and basic security
- Monitoring and metrics

📖 **Documentation**: See SESSION_DEVELOPMENT_2025-12-25_EN.md

### 2. Client Service (Port 8081)
- Client and vehicle management
- CRUD operations for clients and vehicles
- PostgreSQL database integration
- JWT authentication via API Gateway
- OpenAPI 3.0 specification

**Client Endpoints** (via API Gateway):
- `POST /api/clients` - Create client
- `GET /api/clients` - List all clients
- `GET /api/clients/{id}` - Get client by ID
- `PUT /api/clients/{id}` - Update client
- `DELETE /api/clients/{id}` - Delete client
- `GET /api/clients/search?phone={phone}` - Search by phone

**Vehicle Endpoints** (via API Gateway):
- `POST /api/clients/{clientId}/vehicles` - Create vehicle
- `GET /api/vehicles` - List all vehicles
- `GET /api/vehicles/{id}` - Get vehicle by ID
- `PUT /api/vehicles/{id}` - Update vehicle
- `DELETE /api/vehicles/{id}` - Delete vehicle

📖 **Implementation:** Issues #16, #17

### 3. Management Service (Port 8083)
- Parking space management and monitoring
- Real-time availability tracking
- Search and filtering capabilities
- PostgreSQL database integration
- OpenAPI 3.0 specification

**Parking Space Endpoints** (via API Gateway):
- `GET /api/management/spots` - List all parking spaces
- `GET /api/management/spots/available` - List available spaces
- `GET /api/management/spots/available/count` - Count available spaces
- `GET /api/management/spots/available/lot/{lotId}` - Available spaces by lot
- `GET /api/management/spots/search?type={type}&status={status}` - Search with filters

**Supported Space Types:**
- STANDARD, HANDICAPPED, ELECTRIC, VIP, COMPACT, OVERSIZED

**Supported Statuses:**
- AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, OUT_OF_SERVICE

📖 **Implementation:** Issue #18

### 4. Service Registry (Port 8761)
- Eureka Server for service discovery
- Microservice registration and discovery
- Health checks and monitoring

### 5. Observability Stack
- **Prometheus** (Port 9090) - Metrics collection
- **Grafana** (Port 3000) - Dashboards and visualization
- **Jaeger** (Port 16686) - Distributed tracing
- **OpenTelemetry Collector** (Port 4317/4318) - Telemetry collection

### 6. Database Management
- **PostgreSQL 16** (Port 5433) - Main database
- **pgAdmin 4** (Port 5050) - Database management UI
- **Redis 7** (Port 6379) - Caching and session storage

### 7. Planned Services
- **User Service** - System user management
- **Parking Service** - Extended parking lot management
- **Booking Service** - Parking space reservations
- **Payment Service** - Payment processing
- **Billing Service** - Billing and tariff plans
- **Gate Control Service** - Parking gate management
- **Reporting Service** - Reports and analytics

## 📊 Technology Stack

### Backend
- **Java 21** - Main programming language
- **Spring Boot 3.5.8** - Microservices framework
- **Spring Cloud 2025.0.0** - Microservices architecture
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **JWT (jjwt 0.12.6)** - Token-based authentication
- **MapStruct** - DTO to Entity mapping
- **Lombok** - Boilerplate code reduction

### Infrastructure
- **Docker & Docker Compose** - Containerization
- **PostgreSQL 16** - Main database
- **Redis 7** - Caching and session storage
- **Eureka Server** - Service Registry
- **Spring Cloud Gateway** - API Gateway
- **Maven** - Build system

### Observability
- **Prometheus** - Metrics collection
- **Grafana** - Monitoring dashboards
- **Jaeger** - Distributed tracing
- **OpenTelemetry** - Telemetry instrumentation

### Documentation & Testing
- **OpenAPI 3 / Swagger UI** - API documentation
- **JUnit 5** - Unit testing
- **Spring Boot Test** - Integration testing

## 🗄️ Database

### PostgreSQL Configuration
- **Database**: `parking_db`
- **Username**: `postgres`
- **Password**: `postgres`
- **Port**: `5433` (Docker), `5432` (local)

### Flyway Migrations

Database schema is managed using **Flyway** for version-controlled migrations.

**Migration Files:** `backend/api-gateway/src/main/resources/db/migration/`

| Version | File | Description | Tables |
|---------|------|-------------|--------|
| V0 | `V0__baseline.sql` | Baseline | - |
| V1 | `V1__initial_schema.sql` | Core schema | 8 tables |
| V2 | `V2__add_parking_lots.sql` | Parking facilities | parking_lots |
| V3 | `V3__add_parking_spaces.sql` | Parking spaces | parking_spaces |
| V4 | `V4__add_bookings.sql` | Reservations | bookings |

**Quick Commands:**
```powershell
# Test migrations
cd devops
.\test-flyway-migrations.ps1

# View migration history
docker exec parking_db psql -U postgres -d parking_db -c "SELECT * FROM flyway_schema_history;"
```

📖 **Complete Guide:** [Database README](./database/README.md)

### Data Schema
```sql
-- Users (for authentication)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    user_role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked_until TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    -- ... + 30 additional security fields
);

-- Clients
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    registered_at TIMESTAMP DEFAULT NOW()
);

-- Vehicles
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    make VARCHAR(100),
    model VARCHAR(100),
    color VARCHAR(50),
    vehicle_type VARCHAR(50)
);
```

## 🔑 Test Credentials

### For Development and Testing:

| Username | Password | Role | Email |
|----------|----------|------|-------|
| **admin** | `parking123` | ADMIN | admin@parking.com |
| **user** | `user1234` | USER | user@parking.com |
| **manager** | `manager123` | MANAGER | manager@parking.com |

**⚠️ IMPORTANT:** These credentials are for development only! For production, use strong passwords and environment variables.

### Quick Authentication Test:
```powershell
# PowerShell
$body = @{ username = "admin"; password = "parking123" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body
```

```bash
# Bash/cURL
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}'
```

## 🔧 Configuration

### Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/parking_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT Security
JWT_SECRET=<YOUR_64_CHAR_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION=3600
JWT_REFRESH_TOKEN_EXPIRATION=604800

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=development
```

### Docker Compose Services
```yaml
services:
  # Database
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: parking_db
      POSTGRES_USER: postgres  
      POSTGRES_PASSWORD: postgres
    ports: ["5433:5432"]

  # Cache
  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  # Service Registry
  eureka-server:
    build: ./backend/eureka-server
    ports: ["8761:8761"]

  # API Gateway  
  api-gateway:
    build: ./backend/api-gateway
    ports: ["8086:8080"]
    depends_on: [eureka-server, postgres, redis]

  # Client Service
  client-service:
    build: ./backend/client-service
    ports: ["8081:8080"] 
    depends_on: [postgres, eureka-server]
```

## 📚 Documentation

### Development History
- **[Session Logs](./docs/sessions/)** - Detailed development session logs
  - [2025-12-25 - Project Setup](./docs/sessions/SESSION_DEVELOPMENT_2025-12-25_EN.md)
  - [2026-01-03 - Issue #16 Client CRUD](./docs/sessions/SESSION_DEVELOPMENT_2026-01-03_EN.md)
  - [2026-01-04 - Issue #17 Vehicle CRUD](./docs/sessions/SESSION_DEVELOPMENT_2026-01-04_EN.md)
  - [2026-01-11 - Issue #18 Management API (Day 1)](./docs/sessions/SESSION_DEVELOPMENT_2026-01-11_EN.md)
  - [2026-01-12 - Issue #18 Finalization (Day 2)](./docs/sessions/SESSION_DEVELOPMENT_2026-01-12_EN.md)

### Phase Reports
- **[Phase 0 Summary](./docs/reports/PHASE_0_SUMMARY.md)** - Infrastructure foundation completion
- **[Phase 1 Week 1 Report](./docs/reports/PHASE_1_WEEK_1_REPORT.md)** - Basic backend CRUD progress

### Architecture and Security
- **[Authentication Architecture](./docs/AUTHENTICATION.md)** - JWT authentication system
- **[Security Architecture](./docs/SECURITY_ARCHITECTURE.md)** - Security features and components
- **[Observability Setup](./docs/OBSERVABILITY_SETUP.md)** - Monitoring and tracing

### Database & Deployment
- **[Database README](./database/README.md)** - Database schema and Flyway migrations guide
- **[Deployment Guide](./docs/DEPLOYMENT_GUIDE.md)** - Production deployment instructions
- **[Production Migration Process](./docs/PRODUCTION_MIGRATION_PROCESS.md)** - Safe migration deployment to production
- **[Production Config Quick Ref](./docs/PRODUCTION_CONFIG_QUICK_REF.md)** - Production Flyway configuration
- **[Migration Tasks](./docs/DATABASE_MIGRATION_TASKS_EN.md)** - Database migration task breakdown

### Specialized Documentation
- **[DevOps README](./devops/README.md)** - Deployment instructions and scripts
- **[API Documentation](./docs/API-Gateway-Developer-Guide.md)** - API Gateway guide

## 🧪 Testing

### Automated Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Test all modules
mvn clean test -f pom.xml
```

### Manual Testing

#### Test Authentication
```bash
# Get JWT token
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}'

# Use token to access protected endpoint
curl -X GET http://localhost:8086/api/clients \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Health Checks
```bash
# API Gateway health
curl http://localhost:8086/actuator/health

# Client Service health
curl http://localhost:8081/actuator/health

# Eureka dashboard
open http://localhost:8761
```

### PowerShell Scripts for Testing
Located in `devops/` folder:
- `check-system.ps1` - Complete system health check
- `test-auth.ps1` - Authentication testing
- `test-client-service-via-gateway.ps1` - Proxy testing
- `full-rebuild.ps1` - Full rebuild and test

## 📚 API Documentation

### Interactive Documentation
- **Swagger UI**: http://localhost:8086/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8086/v3/api-docs

### Developer Guides
- **[API Gateway Developer Guide](./docs/API-Gateway-Developer-Guide.md)** - Complete guide for API Gateway
- **[Authentication Components](./docs/AUTHENTICATION_COMPONENTS.md)** - JWT authentication system
- **[Brute Force Protection](./docs/BRUTE_FORCE_PROTECTION.md)** - Security features

### Postman Collection
Available in `/docs` folder for easy API testing.

## 🎯 System Features

### Core Services
1. **`client-service`**: Manages client database and subscriptions, verifies their validity
2. **`gate-control-service`**: Receives events from scanners, decides on vehicle admission, manages barrier gates
3. **`billing-service`**: Calculates parking session costs and records payments
4. **`management-service`**: Tracks available parking spaces, provides API for information displays
5. **`reporting-service`**: Collects system logs and generates reports

### Functional Requirements
* **Automatic Mode:** Free access for subscribers (license plate recognition). Ticket issuance/payment for one-time visitors
* **Manual Mode:** Operator control of entry/exit and fee calculation (fallback for automation failure)
* **Logging:** Complete log of arrivals/departures and audit trail of operator/administrator actions
* **Security:** Role-based authentication and authorization (`ADMIN`, `OPERATOR`) using Spring Security

## 🛠️ Running the Project

### Using Docker Compose
Quick deployment of the entire stack (PostgreSQL, all microservices, and Frontend).

**Prerequisites:** Docker and Docker Compose installed.

1. **Build the Images:**
   ```bash
   # Build all Java services
   cd backend
   mvn clean install
   
   # Build Docker images
   cd ..
   docker-compose build
   ```

2. **Start the Services:**
   ```bash
   docker-compose up -d
   ```

3. **Verify Services:**
   ```bash
   # Check container status
   docker-compose ps
   
   # Check logs
   docker-compose logs -f api-gateway
   ```

### Default Access Points

| Service | Address | Credentials |
| :--- | :--- | :--- |
| **API Gateway** | `http://localhost:8086` | admin/parking123 |
| **Eureka Server** | `http://localhost:8761` | - |
| **Frontend Web UI** | `http://localhost:3000` | - |
| **PostgreSQL** | `localhost:5433` | postgres/postgres |
| **pgAdmin** | `http://localhost:5050` | admin@parking.com/admin |

## 💻 Development

### Project Structure

```
parking-system/
├── backend/              # Spring Boot microservices
│   ├── api-gateway/      # API Gateway with JWT auth
│   ├── client-service/   # Client management
│   ├── eureka-server/    # Service discovery
│   └── ...               # Other services
├── frontend/             # React web interface
├── devops/               # Docker files and scripts
│   ├── *.ps1             # PowerShell automation scripts
│   └── observability/    # Prometheus, Grafana configs
├── database/             # SQL scripts
│   └── init.sql          # Database initialization
└── docs/                 # Documentation
```

### Development Workflow

1. **Start Infrastructure:**
   ```bash
   cd devops
   docker-compose -f docker-compose.infrastructure.yml up -d
   ```

2. **Run Services Locally:**
   ```bash
   cd backend/api-gateway
   mvn spring-boot:run
   ```

3. **Run Tests:**
   ```bash
   mvn test
   ```

### Running Tests

To run all Unit and Integration tests:
```bash
cd backend
./mvnw test
```

## 🔒 Production Security

### Critical Environment Variables for Production:

```bash
# JWT Security (MUST be 64+ characters)
JWT_SECRET=<GENERATE_STRONG_64_CHAR_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION=1800   # 30 minutes
JWT_REFRESH_TOKEN_EXPIRATION=43200  # 12 hours

# Database with strong credentials
SPRING_DATASOURCE_PASSWORD=<STRONG_DB_PASSWORD_32_CHARS+>

# Redis with authentication
SPRING_REDIS_PASSWORD=<STRONG_REDIS_PASSWORD>

# Production profile
SPRING_PROFILES_ACTIVE=production

# Rate limiting (more restrictive for production)
RATE_LIMIT_MINUTE=30
BRUTE_FORCE_THRESHOLD=5
```

**📖 Complete security documentation:** See "Production Readiness" section in `SESSION_DEVELOPMENT_2025-12-25_EN.md`

### Security Best Practices
- Use environment variables for all secrets
- Enable HTTPS/TLS in production
- Implement proper logging and monitoring
- Regular security audits
- Keep dependencies updated

## 📝 Future Enhancements

### Short-term
- [ ] Integration with message broker (Kafka/RabbitMQ)
- [ ] WebSocket support for real-time notifications
- [ ] Frontend application (React/Angular)
- [ ] Complete all microservices implementation

### Mid-term
- [ ] Multiple subscription types (day/night, limited entry)
- [ ] Mobile application (iOS/Android)
- [ ] Advanced reporting and analytics
- [ ] CI/CD pipeline (GitHub Actions)

### Long-term
- [ ] Cloud deployment (AWS/GCP/Azure)
- [ ] Kubernetes orchestration
- [ ] Multi-language support (i18n)
- [ ] AI-powered parking optimization

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For questions and support:
- 📧 Email: support@parking-system.com
- 💬 Issues: [GitHub Issues](https://github.com/your-repo/parking-system/issues)
- 📖 Documentation: See `/docs` folder

---

**Made with ❤️ using Spring Boot and Docker**
