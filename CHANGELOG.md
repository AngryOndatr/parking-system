# Changelog

All notable changes to the Parking System project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### In Progress
- Phase 2: Gate Control Service implementation (Issue #36)

### Recently Completed
- ✅ Billing Service: Payment Recording API (Issues #34, #35) - 2026-01-24

---

## [0.5.0] - 2026-01-24

### Added - Billing Service: Complete REST API Implementation (Issues #34, #35)

#### Issue #35: Payment Recording Endpoint (/pay)
- **REST API Endpoints:**
  - `POST /api/v1/billing/pay` - payment recording with transaction ID generation
  - Payment validation: insufficient amount detection, duplicate payment prevention
  - Proper HTTP status codes: 201 Created, 400 Bad Request, 409 Conflict
- **DTOs:**
  - PaymentRequest: parkingEventId, amount, paymentMethod, operatorId (optional)
  - PaymentResponse: paymentId, parkingEventId, amount, status, paymentMethod, transactionId, paymentTime
  - PaymentStatusResponse: parkingEventId, isPaid
- **Exception Handling:**
  - GlobalExceptionHandler for InsufficientPaymentException (400)
  - TicketAlreadyPaidException handling (409 Conflict)
  - Proper error responses with timestamp, status, error, message, path
- **Integration Tests:** 6 comprehensive tests
  - `processPayment_Success` - successful payment recording
  - `processPayment_Error400_InsufficientAmount` - validation for insufficient payment
  - `calculateFee_Success_TwoHours` - fee calculation for 2 hours
  - `calculateFee_Success_OneAndHalfHours_RoundsUp` - rounding up logic verification
  - `calculateFee_Error404_TicketNotFound` - parking event not found handling
  - `calculateFee_Error400_TicketAlreadyPaid` - duplicate payment prevention
  - `getPaymentStatus_Success` - payment status check
  - **All tests passing** ✅

#### Issue #34: Fee Calculation API (/calculate)
- **REST API Endpoints:**
  - `POST /api/v1/billing/calculate` - parking fee calculation
  - `GET /api/v1/billing/status?parkingEventId={id}` - payment status check
- **OpenAPI-First Implementation:**
  - BillingController implements BillingApi interface (generated from openapi.yaml)
  - Full OpenAPI 3.0.3 specification with examples and detailed descriptions
  - Swagger UI integration for API documentation and testing
- **DTOs with Validation:**
  - FeeCalculationRequest: parkingEventId, entryTime, exitTime, tariffType, isSubscriber
  - FeeCalculationResponse: parkingEventId, durationMinutes, baseFee, discount, totalFee, tariffApplied, calculatedAt
  - Validation annotations: @NotNull, @NotBlank, ISO 8601 datetime formats
- **BillingMapper Enhancements:**
  - `toFeeCalculationResponse()` - maps calculation results to response DTO
  - `toPaymentResponse()` - maps Payment entity to response DTO
  - `toPaymentStatusResponse()` - maps payment status to response DTO
  - `toPaymentMethod()` - converts enum between OpenAPI and domain models
- **Exception Handling:**
  - GlobalExceptionHandler with @RestControllerAdvice
  - ParkingEventNotFoundException (404), TicketAlreadyPaidException (400)
  - Structured error responses: ErrorResponse with timestamp, status, error, message, path

### Technical Details
- **Architecture:** OpenAPI-first, Hibernate -> Domain Model <- DTO pattern
- **Test Coverage:** 53 tests total (18 repository + 28 service + 6 integration + 1 smoke test)
- **Code Quality:** All tests passing, proper exception handling, comprehensive logging
- **Documentation:** Full OpenAPI spec with examples, inline code documentation
- **Status:** 🎉 Billing Service **COMPLETE** - Ready for Phase 3 integration!

---

### Added - Billing Service: Complete Entity & Service Layer (Issues #32, #33)

#### Issue #33: Fee Calculation & Payment Processing
- **Service Layer:** BillingService implementation
  - `calculateFee()` - parking fee calculation with hourly rate and rounding up
  - `recordPayment()` - payment recording with validation and unique transaction ID
  - `isTicketPaid()` - ticket payment status check
- **Domain Models:** ParkingEventDomain, PaymentDomain, TariffDomain
- **Mapper:** BillingMapper for Entity <-> DTO transformation
  - `toFeeCalculationResponse()` - fee calculation response mapping
  - `toPaymentResponse()` - payment response mapping
  - `toPaymentStatusResponse()` - payment status mapping
- **Controller:** BillingController (OpenAPI-first approach)
  - `POST /api/billing/calculate` - fee calculation endpoint
  - `POST /api/billing/payment` - payment processing endpoint
  - `GET /api/billing/payment/status/{parkingEventId}` - payment status check
- **Exceptions:** Custom business exceptions
  - ParkingEventNotFoundException
  - TicketAlreadyPaidException
  - InsufficientPaymentException
  - TariffNotFoundException
- **Tests:** Comprehensive unit tests (20 new tests)
  - BillingServiceTest: 16 tests covering all service methods
  - BillingControllerTest: 4 tests for REST endpoints
  - All tests passing with ~90% coverage

#### Issue #32: ParkingEvent & Payment Entities with Repositories
- **JPA Entities:**
  - ParkingEvent: maps to parking_events table with entry/exit tracking
    - Fields: id, vehicleId, licensePlate, ticketCode, entryTime, exitTime, entryMethod, exitMethod, spotId, isSubscriber, createdAt
    - Enums: EntryMethod (SCAN, MANUAL), ExitMethod (SCAN, MANUAL, AUTO)
    - @PrePersist for automatic timestamps
  - Payment: maps to payments table with transaction management
    - Fields: id, parkingEventId, amount, paymentTime, paymentMethod, status, transactionId, operatorId, createdAt
    - Enums: PaymentMethod (CARD, CASH, MOBILE_PAY), PaymentStatus (PENDING, COMPLETED, FAILED, REFUNDED)
    - @PrePersist for automatic timestamps
- **Repositories:**
  - ParkingEventRepository: custom queries for ticket lookup and active sessions
  - PaymentRepository: custom queries for payment status tracking
- **Tests:** Repository integration tests (18 new tests)
  - ParkingEventRepositoryTest: 8 tests for CRUD and custom queries
  - PaymentRepositoryTest: 10 tests for payment operations and status checks
  - All tests passing with H2 in-memory database

### Technical Improvements
- Implemented Hibernate -> Domain Model <- DTO architecture
- OpenAPI-first approach with generated API interfaces
- Comprehensive error handling with custom exceptions
- Transaction ID generation for payment tracking (format: TRX-{timestamp}-{random})

### Documentation
- Session development logs for 2026-01-17 and 2026-01-18
- Updated project progress (Phase 2: 50% complete)
- Architecture documentation for multi-layer design

### Statistics
- **Total Tests in Billing Service:** 38 (18 repository + 16 service + 4 controller)
- **Overall Test Count:** 52+ across all services
- **Code Added:** ~2000+ lines (including tests)
- **Test Coverage:** Service ~95%, Controller ~85%, Repository ~90%

---

## [0.3.1] - 2026-01-17

### Added - Billing Service: ParkingEvent & Payment Entities (Issue #32)
- **Entities:** ParkingEvent and Payment JPA entities
  - ParkingEvent: vehicleId, licensePlate, ticketCode, entry/exitTime, methods, spotId, isSubscriber
  - Payment: parkingEventId, amount, paymentTime, paymentMethod, status, transactionId, operatorId
- **Enums:** 
  - EntryMethod (SCAN, MANUAL)
  - ExitMethod (SCAN, MANUAL, AUTO)
  - PaymentMethod (CARD, CASH, MOBILE_PAY)
  - PaymentStatus (PENDING, COMPLETED, FAILED, REFUNDED)
- **Repositories:** ParkingEventRepository and PaymentRepository
  - Custom query methods for ticket lookup and payment status
  - Integration tests (18 tests, all passing)
- **Tests:** Repository integration tests with proper configuration
  - Fixed @ContextConfiguration setup
  - Removed @EnableJpaRepositories annotation conflicts

### Documentation
- Updated project phases documentation (Russian & English)
- Removed "Latest Updates" section from root README
- Added PROJECT_PHASES_EN.md

---

## [0.3.0] - 2026-01-16

### Added - Billing Service: Tariff Entity Layer (Issue #31)
- **Entity:** Tariff JPA entity with complete validation
  - `@NotNull` constraints on required fields
  - `@DecimalMin("0.0")` on rates
  - Lombok annotations for boilerplate reduction
  - `@PrePersist` for automatic timestamp management
- **Repository:** TariffRepository with custom queries
  - `findByTariffTypeAndIsActiveTrue(String)` - active tariff lookup
  - `existsByTariffType(String)` - uniqueness check
- **Tests:** Repository integration tests (`@DataJpaTest`)
  - Test save and find by ID
  - Test findByTariffTypeAndIsActiveTrue
  - All tests passing (13 tests)
- **Build:** OpenAPI code generation configured
  - billing-service: DTO generation enabled
  - gate-control-service: DTO generation enabled
  - Maven compilation successful

### Fixed
- Gate Control Service OpenAPI code generation issues
- Maven compilation errors in billing-service

---

## [0.2.0] - 2026-01-16

### Added - Phase 2: Database Extensions & API Contracts (Issues #24-26)

#### Database - TARIFFS Table (Issue #24)
- **Migration V7:** `V7__create_tariffs_table.sql`
  - Table structure: id, tariff_type (UNIQUE), hourly_rate, daily_rate, description, is_active, timestamps
  - Index: `idx_tariffs_type_active` on (tariff_type, is_active)
  - Seed data: 4 base tariffs (ONE_TIME, DAILY, NIGHT, VIP)
  - Idempotent migration with proper constraints
- **Documentation:** Database README updated with tariffs schema
- **Cleanup:** Removed duplicate `users_security_migration.sql`

#### Database - Extended PARKING_EVENTS & PAYMENTS (Issue #25)
- **Migration V8:** `V8__extend_parking_events_and_payments.sql`
  - **PARKING_EVENTS extended:**
    - `license_plate VARCHAR(20) NOT NULL`
    - `entry_method VARCHAR(20) CHECK IN ('SCAN', 'MANUAL')`
    - `exit_method VARCHAR(20) CHECK IN ('SCAN', 'MANUAL', 'AUTO')`
    - `is_subscriber BOOLEAN DEFAULT FALSE`
    - `created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`
  - **PAYMENTS extended:**
    - `status VARCHAR(20) DEFAULT 'COMPLETED' CHECK IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')`
    - `transaction_id VARCHAR(100) UNIQUE`
    - `operator_id BIGINT FK to users ON DELETE SET NULL`
    - `created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`
  - **Indexes:** 9 new indexes for performance
    - `idx_parking_events_ticket`, `idx_parking_events_entry_time`, `idx_parking_events_license`
    - `idx_payments_parking_event`, `idx_payments_transaction`
  - **Constraints:** Partial unique index - only one COMPLETED payment per parking event
  - **Data Migration:** Existing parking_events backfilled with license_plate from vehicles table
- **Verification:** PowerShell script `devops/verify-v8-migration.ps1`

#### API Contracts - Billing & Gate Control (Issue #26)
- **OpenAPI Specs:** Created OpenAPI 3.0.3 specifications
  - `backend/billing-service/src/main/resources/openapi.yaml` (3 endpoints)
  - `backend/gate-control-service/src/main/resources/openapi.yaml` (3 endpoints)
- **Documentation:** Complete API contracts guide
  - `docs/api-contracts.md` with 36+ code examples
  - Request/response examples for all endpoints
  - Error handling documentation (all status codes)
  - PowerShell and curl testing examples
- **Configuration:** Swagger UI ready (contract-first approach)

### Changed
- Database schema extended for Phase 2 business logic
- OpenAPI code generation enabled in billing-service and gate-control-service

---

## [0.1.0] - 2026-01-13

### Added - Phase 1: Tests, Documentation & Proxy Verification (Issues #21-22)

#### API Gateway Proxy Verification (Issue #21)
- **Scripts:** Cross-platform smoke test scripts
  - `devops/test-proxy.ps1` (PowerShell, 269 lines)
  - `devops/test-proxy.sh` (Bash, 270 lines)
- **Tests:** 11 automated proxy tests across all services
  - Management Service: 4 endpoint tests
  - Reporting Service: 5 endpoint tests
  - Client Service: 2 endpoint tests
- **Verification:** JWT token forwarding tested and verified
- **Documentation:**
  - `docs/API_GATEWAY_PROXY_EXAMPLES.md` (36 code examples)
  - `devops/README.md` updated with testing guide

#### Tests & Documentation Complete (Issue #22)
- **Tests:** Comprehensive test coverage (46+ test cases)
  - Client Service: 20+ tests (unit + MockMvc integration)
  - Management Service: 14+ tests (integration)
  - Reporting Service: 12+ tests (unit + integration)
  - Happy path + negative case coverage
- **Documentation:** Service-level README files (1,371 lines total)
  - `backend/client-service/README.md` (630 lines)
  - `backend/management-service/README.md` (307 lines)
  - `backend/reporting-service/README.md` (434 lines)
- **Examples:** API examples for all endpoints (curl + PowerShell)
- **Scripts:** Local test scripts operational
- **Report:** `docs/reports/ISSUE_22_STATUS_REPORT.md`

#### Reporting Service JWT Authentication (Issue #19)
- **Endpoints:**
  - `POST /api/reporting/log` - Create log entries (JWT protected)
  - `GET /api/reporting/logs` - Retrieve logs with filters (JWT protected)
- **Security:** JWT Authentication integrated
  - `JwtAuthenticationFilter`, `JwtTokenProvider`, `SecurityConfig`
  - Unified JWT secret across all microservices (768 bits, HS512 compliant)
- **Dependencies:** Jackson JsonNullable support for OpenAPI models
- **Tests:** Comprehensive test coverage

### Fixed
- JWT signature mismatch (unified secrets in docker-compose.yml)
- JWT key too short (upgraded to 96 characters, 768 bits)
- Jackson JsonNullable deserialization error (added jackson-databind-nullable module)

---

## [0.0.5] - 2026-01-12

### Added - Phase 1: Backend CRUD Implementation (Issues #16-18)

#### Client Service - Full CRUD (Issue #16)
- **Endpoints:** Complete CRUD for Clients entity
  - `POST /api/v1/clients` - Create client
  - `GET /api/v1/clients` - List all clients
  - `GET /api/v1/clients/{id}` - Get client by ID
  - `PUT /api/v1/clients/{id}` - Update client
  - `DELETE /api/v1/clients/{id}` - Delete client
- **Design:** OpenAPI-first with generated interfaces
- **Validation:** Comprehensive validation and error handling
- **Tests:** Unit and integration tests
- **Security:** JWT-protected endpoints via API Gateway

#### Client Service - Vehicles Management (Issue #17)
- **Endpoints:** Full CRUD for Vehicles linked to Clients
  - `POST /api/v1/clients/{id}/vehicles` - Add vehicle to client
  - `GET /api/v1/clients/{id}/vehicles` - List client's vehicles
  - `GET /api/v1/clients/{id}/vehicles/{vehicleId}` - Get vehicle details
  - `PUT /api/v1/clients/{id}/vehicles/{vehicleId}` - Update vehicle
  - `DELETE /api/v1/clients/{id}/vehicles/{vehicleId}` - Remove vehicle
- **Features:**
  - License plate uniqueness enforcement
  - Client-Vehicle relationship management
- **Tests:** Comprehensive test coverage

#### Management Service - Parking Spaces API (Issue #18)
- **Endpoints:**
  - `GET /api/v1/management/spots/available` - List available spaces
  - `GET /api/v1/management/spots/available/count` - Count available spaces
  - `GET /api/v1/management/spots/available/lot/{id}` - Available spaces by lot
  - `GET /api/v1/management/spots/search` - Search with filters (type, status)
- **Data:** Test data migration with 23 parking spaces
- **Integration:** API Gateway proxy endpoints configured

---

## [0.0.4] - 2025-12-26

### Added - Flyway Database Migrations
- **Configuration:** Flyway integrated into API Gateway
- **Migrations:** 5 migrations created (V1-V5)
  - V1: Initial schema (users, roles, clients)
  - V2: Parking lots table
  - V3: Parking spaces table
  - V4: Bookings table
  - V5: Test data
- **Documentation:**
  - `database/README.md` - Comprehensive database documentation
  - `docs/DEPLOYMENT_GUIDE.md` - Deployment guide with migration instructions
- **Scripts:** Test scripts for migration verification

---

## [0.0.3] - 2025-12-25

### Added - Infrastructure & Foundation (Phase 0 Complete)
- **API Gateway:**
  - JWT authentication and security features
  - Route mapping and load balancing
  - CORS handling
  - Rate limiting and brute force protection
- **Microservices:**
  - Eureka Server for service discovery
  - Client Service (basic structure)
  - Management Service (basic structure)
  - Reporting Service (basic structure)
  - Billing Service (basic structure)
  - Gate Control Service (basic structure)
- **Database:**
  - PostgreSQL setup and configuration
  - Redis for caching and sessions
- **Observability:**
  - Prometheus for metrics
  - Grafana for dashboards
  - Jaeger for distributed tracing
  - OpenTelemetry integration
- **Docker:**
  - docker-compose.yml for all services
  - docker-compose.infrastructure.yml for infrastructure
  - docker-compose.services.yml for microservices
  - docker-compose.observability.yml for monitoring
- **Security:**
  - BCrypt password hashing
  - JWT token generation and validation
  - Role-based access control (ADMIN, OPERATOR, USER)

### Documentation
- `docs/sessions/SESSION_DEVELOPMENT_2025-12-25_EN.md` - Complete development log
- `docs/SECURITY_ARCHITECTURE.md` - Security architecture documentation
- `docs/OBSERVABILITY_SETUP.md` - Observability setup guide
- `devops/README.md` - DevOps scripts documentation

---

## [0.0.2] - 2025-12-20

### Added - Initial Setup
- **Repository:** GitHub repository created
- **Project Structure:**
  - Backend services directory structure
  - Frontend directory structure
  - DevOps scripts directory
  - Documentation directory
- **Build Configuration:**
  - Maven parent POM
  - Service-level POMs
  - Spring Boot dependencies
- **GitHub:**
  - GitHub Projects Kanban board setup
  - Branch protection rules (main, develop)
  - Issue templates

---

## [0.0.1] - 2025-12-15

### Added - Project Initialization
- **Planning:** Project roadmap defined
- **Architecture:** Microservices architecture designed
- **Database:** ER diagram created
- **Documentation:** Initial README.md

---

## Legend

- **Added:** New features
- **Changed:** Changes in existing functionality
- **Deprecated:** Soon-to-be removed features
- **Removed:** Removed features
- **Fixed:** Bug fixes
- **Security:** Security improvements

---

**[View Current Status](./docs/PROJECT_PHASES.md)** | **[Session Logs](./docs/sessions/)** | **[Reports](./docs/reports/)**

