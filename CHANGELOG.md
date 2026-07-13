# Changelog

All notable changes to the Parking System project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### In Progress

- Phase 4 planning and report features backlog

### Recently Completed
- ✅ **[Phase 3] Tech Debt: unify API path versioning across microservices** (Issue #81) — 2026-07-13
- ✅ **[Phase 3] Frontend multilingual support documentation and context sync** (Issue #84) — 2026-07-13
- ✅ **[Phase 3] Frontend: Billing Operator UI (payment processing)** (Issue #76) — 2026-07-13
- ✅ **[Phase 3] Frontend: Gate Emulation UI (vehicle entry and exit)** (Issue #75) — 2026-07-13
- ✅ **[Phase 3] Phase status updated to completed** — 2026-07-13
- ✅ **[Phase 3] Backend / client-service: Subscription CRUD endpoints (create, list, deactivate)** (Issue #83) — 2026-03-30
- ✅ **[Phase 3] Frontend: Responsive Layout Overhaul — mobile sidebar, adaptive grids, card views** (Issue #82) — 2026-03-30
- ✅ **[Phase 3] CORS wildcard for dynamic LAN IP** (Issue #79 fix) — 2026-03-09
- ✅ **[Phase 3] React frontend: project init, auth, base layout** (Issue #74) — 2026-03-08
- ✅ **[Phase 3] CORS configuration in api-gateway** (Issue #79) — 2026-03-08
- ✅ **[Phase 3] E2E test: subscriber full parking cycle** (Issue #73) — 2026-03-08
- ✅ **[Phase 3] Subscription check: real DB logic in client-service** (Issue #72) — 2026-03-08
- ✅ **[Phase 3] Add default OPERATOR user on application startup** (Issue #80) — 2026-03-08
- ✅ **[Phase 3] RBAC: role-based route protection in SecurityFilter** (Issue #78) — 2026-03-08

## [Issue #81] - 2026-07-13

### Changed — Tech Debt: unify API path versioning across microservices (Issue #81)

**Problem:** API path versioning was inconsistent (`/api/v1/gate/*`, `/api/v1/billing/*` vs
unversioned routes in other services), which increased maintenance overhead in RBAC,
OpenAPI contracts, frontend clients, tests, and docs.

#### `GateControlProxyController.java` / `BillingProxyController.java` (api-gateway)
- Proxy mappings migrated to unified non-versioned paths: `/api/gate/*` and `/api/billing/*`

#### `SecurityFilter.java` (api-gateway)
- RBAC route rules updated to match unified gate/billing paths without `/v1`

#### `openapi.yaml` (gate-control-service, billing-service)
- Endpoint paths normalized to non-versioned gateway contract

#### Frontend + tests + docs
- Frontend API clients, E2E/integration tests, scripts, and API docs aligned to unified paths

## [Issue #84] - 2026-07-13

### Added — Frontend multilingual support documentation and context sync (Issue #84)

**Problem:** multilingual support (EN/DE/UA/RU) existed in frontend code, but AI context docs
and project documentation were not fully synchronized with the implemented i18n architecture.

#### `.github/copilot-instructions.md` / `CLAUDE.md`
- Added i18n architecture notes and maintenance context for EN/DE/UA/RU support

#### `README.md` / `README_RU.md` / `frontend/README.md`
- Updated language-support documentation and translation workflow references

#### `docs/sessions/SESSION_DEVELOPMENT_2026-07-13_EN.md` / `docs/sessions/SESSION_DEVELOPMENT_2026-07-13_RU.md`
- Added session-level documentation snapshots for multilingual rollout updates

## [Issue #76] - 2026-07-13

### Added — Frontend: Billing Operator UI (payment processing) (Issue #76)

**Problem:** operators had no dedicated UI flow to check ticket payment status and complete
payments from the frontend.

#### `BillingPage.tsx` (frontend)
- Implemented ticket status lookup UI and payment submission flow for operator/admin roles
- Added response-state rendering for paid/unpaid and completed payment status

#### `billing.ts` (frontend API)
- Added billing endpoints integration for status-by-ticket and payment processing flows

## [Issue #75] - 2026-07-13

### Added — Frontend: Gate Emulation UI (vehicle entry and exit) (Issue #75)

**Problem:** there was no operator UI to emulate gate entry/exit and validate gate-control
behavior end-to-end from the frontend.

#### `GatePage.tsx` (frontend)
- Added entry/exit forms for operator flow with API-backed submission
- Added result rendering for ticket code and gate status outcomes

#### `gate.ts` (frontend API)
- Added gateway integrations for vehicle entry and exit operations

## [Issue #83] - 2026-03-30

### Added — Backend / client-service: Subscription CRUD endpoints (Issue #83)

**Problem:** subscriptions existed at DB/entity level, but there were no REST endpoints to
create, list, or deactivate subscriptions for operational use.

#### `SubscriptionController.java` (client-service)
- Added create/list/deactivate subscription endpoints

#### `SubscriptionService.java` / `SubscriptionRepository.java` / `SubscriptionMapper.java` (client-service)
- Implemented business logic, repository support, and DTO mapping for subscription lifecycle

#### Tests (client-service)
- Added/updated unit and controller tests for create, list, and deactivate behavior

## [Issue #82] - 2026-03-30

### Added — Frontend: Responsive Layout Overhaul (Issue #82)

**Problem:** desktop-oriented layout patterns caused poor usability on mobile and small tablets
(fixed sidebar, dense tables, non-adaptive action rows).

#### `AppLayout.tsx` / `PageHeader.tsx` (frontend)
- Implemented responsive layout shell and reusable adaptive page header patterns

#### `ReportingPage.tsx`, `BillingPage.tsx`, `ManagementPage.tsx`, `DashboardPage.tsx`, `ClientsPage.tsx`
- Migrated key screens to adaptive grid/card behavior with improved small-screen usability

## [Issue #74] - 2026-03-08

### Added — React frontend: project init, auth, and base layout (Issue #74)

**Problem:** project lacked frontend foundation for authenticated role-based user flows and
protected navigation.

#### Frontend foundation (React + TypeScript + Vite)
- Initialized app scaffold, routing, auth store, and API client integration
- Implemented login flow, protected routes, and role-based base layout/navigation


---

## [0.15.1] - 2026-03-09

### Fixed — CORS wildcard for dynamic LAN IP (Issue #79 fix)

**Problem:** `setAllowedOrigins()` in Spring Security does not support wildcard patterns —
`http://192.168.*` was silently ignored, so CORS preflight returned 403 whenever the
machine's DHCP address changed.

#### `SecurityConfiguration.java` (api-gateway)
- `setAllowedOrigins(origins)` → `setAllowedOriginPatterns(originPatterns)` — native Spring
  wildcard support (`http://192.168.*` matches any IP + any port in that subnet)
- Updated `@Value` default to include `http://192.168.*`

#### `CorsFilter.java` (api-gateway)
- Added `isOriginAllowed(origin)` with prefix-wildcard logic (entries ending with `*`
  are matched as prefix, e.g. `http://192.168.*` matches `http://192.168.1.42:5173`)
- Updated `@Value` default to include `http://192.168.*`

#### `application.yml` (api-gateway)
- Default `cors.allowed-origins` updated: `http://192.168.*` replaces hard-coded IP

#### `docker-compose.yml`
- `CORS_ALLOWED_ORIGINS` updated: removed `192.168.1.5:5173` / `192.168.1.5:3000`,
  replaced with single `http://192.168.*` wildcard — no more manual IP updates on DHCP change



## [0.15.0] - 2026-03-08

### Added — CORS configuration in api-gateway (Issue #79)

#### `CorsFilter.java` (new, api-gateway)
- Registered at `@Order(0)` — executes before `SecurityFilter` (`@Order(1)`)
- `ALLOWED_ORIGINS`: `http://localhost:5173` (Vite), `http://localhost:3000` (CRA)
- Sets headers: `Access-Control-Allow-Origin`, `Allow-Methods`, `Allow-Headers`, `Expose-Headers`, `Max-Age`
- `allowCredentials` intentionally **omitted** (`false`) — JWT stored in `localStorage`, sent via `Authorization` header; no cookies
- OPTIONS preflight: returns **HTTP 200 immediately**, does NOT invoke `filterChain.doFilter()` (SecurityFilter bypassed)

#### `SecurityFilter.java` (api-gateway)
- Added step **0.5**: bypass OPTIONS requests — `filterChain.doFilter()` called without JWT check, CORS headers already set by `CorsFilter`

#### `SecurityConfiguration.java` (api-gateway)
- Replaced `allowedOriginPatterns("*")` + `allowCredentials(true)` with exact origins `localhost:5173`, `localhost:3000` and `allowCredentials=false`
- Aligns Spring Security CORS config with `CorsFilter` as single source of truth

#### `CorsFilterTest.java` (new, 4 tests)
- `preflight_allowedOrigin_returns200WithCorsHeaders` — OPTIONS from `localhost:5173` → 200, correct headers, chain NOT invoked
- `preflight_port3000_returns200` — OPTIONS from `localhost:3000` → 200, chain NOT invoked
- `get_allowedOrigin_addsCorsHeadersAndPassesThrough` — GET from allowed origin → headers added, chain invoked
- `request_unknownOrigin_noCorsHeaders` — unknown origin → no CORS headers

#### Test Results
```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0  (CorsFilterTest)
api-gateway total: 12 tests, 0 failures
Full project: 177 tests, 0 failures, BUILD SUCCESS
```

---

## [0.14.0] - 2026-03-08

### Added — E2E test: subscriber full parking cycle (Issue #73)

#### `SubscriberE2ETest.java` (new, e2e-tests)
- Same structure as `OneTimeVisitorE2ETest` — spins up full stack via `docker-compose-e2e.yml`
- Pre-condition: `licensePlate=AA1234BB` seeded in `init.sql` with active ANNUAL subscription
- **Step 1** — `POST /api/v1/gate/entry`: expects `201`, `isSubscriber=true`, `gateStatus=OPENED`, `ticketCode=null`
- **Step 2** — `POST /api/v1/gate/exit` (no ticketCode): expects `200`, `paymentRequired=false`, `gateStatus=OPENED`
- **Step 3** — `GET /api/v1/billing/status?parkingEventId=N`: expects `404` (no payment record for subscriber)
- `waitForServices()` includes additional check for Client Service (`/api/v1/clients/subscriptions/check`)
- Discovered by surefire pattern `**/*E2ETest.java` — runs sequentially after `OneTimeVisitorE2ETest`
- No changes to `docker-compose-e2e.yml` — `client-service` and `CLIENT_SERVICE_URL` already configured

#### Run
```
mvn test -Pe2e
```

---

## [0.13.0] - 2026-03-08

### Added — Subscription check: real DB logic in client-service (Issue #72)

#### `Subscription.java` (new, parking-common)
- JPA entity mapped to existing `subscriptions` table
- Fields: `id`, `client` (ManyToOne → Client), `startDate`, `endDate`, `type`, `isActive`

#### `SubscriptionRepository.java` (new, client-service)
- JPQL query `findActiveByLicensePlate(licensePlate, now)`:
  traverses `vehicles → clients → subscriptions` with `isActive = true AND endDate > :now`

#### `SubscriptionCheckController.java` (client-service)
- Replaced hardcoded stub (always `false`) with real DB lookup via `SubscriptionRepository`
- Returns `isAccessGranted=true` + `subscriptionId` when active subscription found
- Returns `isAccessGranted=false` + `subscriptionId=null` when not found
- Added `@RequiredArgsConstructor` for constructor injection

#### Tests (new)
- `SubscriptionRepositoryTest` (`@DataJpaTest`, 3 tests):
  - `findActiveByLicensePlate_activeSubscription_returnsPresent` — plate AA1234BB → present
  - `findActiveByLicensePlate_expiredSubscription_returnsEmpty` — expired/inactive → empty
  - `findActiveByLicensePlate_unknownPlate_returnsEmpty` — unknown plate → empty
- `SubscriptionCheckControllerTest` (MockMvc, 2 tests):
  - `checkSubscription_activeSubscription_returnsAccessGranted` — mock returns subscription → 200 + `isAccessGranted=true`
  - `checkSubscription_noSubscription_returnsAccessDenied` — mock returns empty → 200 + `isAccessGranted=false`

#### Test Results
```
Tests run: 5 new (3 repo + 2 controller), Failures: 0
client-service total: 31 tests, 0 failures
Full project: 173 tests, 0 failures, BUILD SUCCESS
```

---

## [0.12.0] - 2026-03-08

### Added — Default OPERATOR user on startup (Issue #80)

#### `UserSecurityService.java` (api-gateway)
- Refactored `initializeDefaultUsers()`: replaced `countActiveUsers() == 0` check with
  per-user `existsByUsername()` guards — each user is created independently and idempotently
- Added `createDefaultOperatorUser()`:
  - `username=operator`, `password=ParkingOperator2025!` (BCrypt-hashed)
  - `role=OPERATOR`, `forcePasswordChange=true`, `enabled=true`
  - Logs a WARNING with the temporary password on first creation

#### `UserSecurityServiceDefaultUsersTest.java` (new)
- `createsBothUsersWhenDbIsEmpty` — verifies both admin + operator saved with correct roles and BCrypt hashes
- `doesNotCreateDuplicatesWhenUsersExist` — verifies `save()` is never called when both users exist
- `createsOnlyOperatorWhenAdminExists` — verifies partial creation is independent

#### Test Results
```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0  (UserSecurityServiceDefaultUsersTest)
Total project: 168 tests, 0 failures, BUILD SUCCESS
```

---

## [0.11.0] - 2026-03-08

### Added — RBAC: Role-Based Route Protection in SecurityFilter (Issue #78)

#### `SecurityFilter.java` (api-gateway)
- Added `ROUTE_ROLES` static map defining allowed roles per `METHOD:/path-prefix` rule:
  - `POST|PUT|DELETE /api/v1/gate/**` → `{OPERATOR, ADMIN}`
  - `POST|PUT|DELETE /api/v1/billing/**` → `{OPERATOR, ADMIN}`
  - `GET|POST|PUT|DELETE /api/clients/**` → `{ADMIN, MANAGER, OPERATOR}`
  - `POST|PUT|DELETE /api/management/**` → `{ADMIN, MANAGER}`
  - `GET|POST|PUT|DELETE /api/reporting/**` → `{ADMIN, MANAGER, OPERATOR}`
- Added **step 4.5** in `doFilterInternal` — RBAC check after JWT validation; returns **HTTP 403** with JSON body `{"error":"Forbidden","message":"Access denied: insufficient role for this operation"}` on role mismatch
- Added `isRoleAllowed(method, path, roleStr)` — package-private for unit-testing; first-match-wins prefix scan against `ROUTE_ROLES`
- **Fixed bug**: `validateJwtToken()` was reading wrong JWT claim names (`user_id` → `userId`, `roles` → `role`); request attribute renamed from `roles` to `role` accordingly
- Backward-compatible: public paths, Docker-bypass (whitelisted/internal IPs), and E2E profile (`SPRING_AUTOCONFIGURE_EXCLUDE`) are **not affected**

#### `SecurityFilterRbacTest.java` (new)
- `operatorDeniedOnManagementWriteRoute()` — pure unit test: `isRoleAllowed("POST", "/api/management/…", "OPERATOR")` → `false`
- `adminAllowedOnAllProtectedRoutes()` — pure unit test: ADMIN passes all 6 protected route patterns
- `operatorGets403OnManagementWrite()` — full HTTP pipeline: mocked JWT returning role=OPERATOR → filter returns 403
- `adminPassesManagementWrite()` — full HTTP pipeline: mocked JWT returning role=ADMIN → filter chain invoked → 200

#### Test Results
```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0  (SecurityFilterRbacTest)
Total project: 165 tests, 0 failures, BUILD SUCCESS
```
- ✅ Bug Fix: `BillingController.getPaymentStatus` — restored 404 for unknown events - 2026-03-07
- ✅ Bug Fix: `JacksonConfig` — removed `Jackson2ObjectMapperBuilder` dependency (broke `@DataJpaTest`) - 2026-03-07
- ✅ Bug Fix: `PaymentStatusResponse.remainingFee` — restored `BigDecimal` type - 2026-03-07
- ✅ Bug Fix: `BillingServiceClient` — parse `remainingFee` as `BigDecimal`, not `Double` - 2026-03-07
- ✅ Bug Fix: `GateServiceTest` — fix mock method (`checkPaymentStatusByTicket`) and remove unused stubs - 2026-03-07
- ✅ New script: `devops/run-e2e-tests.ps1` — standalone E2E runner with Docker health check - 2026-03-07
- ✅ E2E Test Implementation: OneTimeVisitorE2ETest (6-step full cycle) (Issue #70) - 2026-02-14
- ✅ Flyway Migration Fix: Moved gate_events table migration from gate-control-service to api-gateway (Issue #46) - 2026-02-04
- ✅ Eureka Service Discovery: Registered Billing and Gate Control Services - 2026-02-04
- ✅ Docker Compose: Enabled Gate Control and Billing Services - 2026-02-04
- ✅ E2E Test Setup: Testcontainers Configuration for Full Cycle Test - 2026-02-04

---

## [0.10.0] - 2026-03-07

### Fixed — Unit Tests & E2E Infrastructure (Issue #70: [Phase 2] E2E Test: Full Cycle for One-Time Visitor)

#### Bug Fixes

**billing-service**

- `BillingController.getPaymentStatus()` — re-throw `ParkingEventNotFoundException` instead of
  returning `200 OK`; `GlobalExceptionHandler` now correctly returns **404 Not Found**
- `JacksonConfig.objectMapper()` — remove dependency on `Jackson2ObjectMapperBuilder`
  (unavailable in `@DataJpaTest` slice); `ObjectMapper` created directly

**gate-control-service**

- `PaymentStatusResponse.remainingFee` — restored from `Double` back to `BigDecimal`
- `BillingServiceClient.checkPaymentStatus()` / `checkPaymentStatusByTicket()` — parse
  `remainingFee` via `new BigDecimal(value.toString())` to preserve decimal scale
- `GateServiceTest` — fixed mock: `checkPaymentStatus(id)` → `checkPaymentStatusByTicket(code)`;
  changed `remainingFee(0.0)` → `BigDecimal.ZERO`; removed unnecessary `findByTicketCode` stubs

#### Added

- `devops/run-e2e-tests.ps1` — standalone PowerShell E2E runner:
  - Docker daemon health check (with Windows named-pipe validation)
  - Maven auto-discovery (`PATH` + known install locations)
  - Flags: `-SkipBuild`, `-SkipDockerBuild`
  - Docker image existence validation before running tests
  - Coloured pass/fail banner with elapsed time

#### E2E Test Result
```
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0  (OneTimeVisitorE2ETest)
Time elapsed: 107.6 s
BUILD SUCCESS
```

Full project unit tests: **161 tests, 0 failures, BUILD SUCCESS**

---

## [0.9.5] - 2026-02-14

### Added — E2E Testing Infrastructure (Issue #70: [Phase 2] E2E Test: Full Cycle for One-Time Visitor)

#### `e2e-tests` module (новый)
- `OneTimeVisitorE2ETest` — full one-time visitor cycle (Entry → Payment → Exit)
  with Testcontainers + docker-compose orchestration
- `SimpleHealthCheckTest` — checks availability of all services
- `docker-compose-e2e.yml` — isolated test environment (9 services)
- `build-e2e-images.ps1` / `build-images.ps1` — Docker image build scripts
- `otel-collector-config.yaml` — OpenTelemetry Collector configuration for tests
- `logback-test.xml` — test logging configuration

#### Architecture Fixes (for E2E)
- `BillingController` — new endpoints `/api/v1/billing/pay-test` and `/api/v1/billing/status-by-ticket`
- `BillingService.recordPaymentByTicketCode()` — links payments by `ticketCode`
- `GateService.processExit()` — verifies payment via `checkPaymentStatusByTicket()`
- `BillingServiceClient.checkPaymentStatusByTicket()` — new WebClient method
- `E2ESecurityConfiguration` — `e2e-test` profile with relaxed security constraints
- `application-e2e-test.yml` — api-gateway configuration for E2E
- `SubscriptionCheckController` — new endpoint in client-service for E2E subscription checks

#### Database
- `database/init.sql` — synchronised with V8/V9 Flyway migration schema
- `V8__extend_parking_events_and_payments.sql` — nullable `vehicle_id` for guest visits
- `V9__create_gate_events_table.sql` — `gate_events` table moved to api-gateway

#### Windows / Testcontainers Fix
- Docker Engine config: `"min-api-version": "1.24"` — resolves `BadRequestException` on Docker discovery
- Documentation: `E2E_TESTCONTAINERS_WINDOWS_SETUP_EN.md`

#### E2E Documentation
- `backend/e2e-tests/README.md`
- `backend/e2e-tests/E2E_TESTING_GUIDE_EN.md`
- `backend/e2e-tests/QUICK_START_GUIDE_EN.md`
- `backend/e2e-tests/PORTS_ARCHITECTURE_EN.md`
- `backend/e2e-tests/E2E_TESTCONTAINERS_WINDOWS_SETUP_EN.md`

---

## [0.9.0] - 2026-01-26

### Added - Gate Control Service: Entry Decision Logic (Issue #49)

#### Issue #49: Implement Entry Decision Logic
- **Service Layer:**
  - `GateService` with core business logic for entry processing
  - `processEntry(String licensePlate)` method implementing decision tree:
    - **Subscriber path:** automatic gate opening without ticket generation
    - **Visitor path:** unique ticket generation with TICKET-{timestamp}-{random} format
  - Integration with `ClientServiceClient` for subscription validation
  - Integration with `GateEventRepository` for audit trail logging
- **DTO Layer:**
  - `EntryDecision` DTO with fields:
    - `action`: "OPEN" or "DENY" decision
    - `message`: User-friendly message for display
    - `ticketCode`: Generated ticket code (null for subscribers)
  - Lombok annotations for builder pattern support
- **Decision Logic:**
  - Call to Client Service to check subscription status
  - If `isAccessGranted=true`: 
    - Log ENTRY event with "Valid subscription" reason
    - Return OPEN decision without ticket
  - If `isAccessGranted=false`:
    - Generate unique ticket code
    - Log ENTRY event with "Ticket issued" reason and ticket code
    - Return OPEN decision with ticket code
  - All gate events saved with proper metadata (ENTRY-1 gate ID, timestamps)
- **Testing:**
  - 5 comprehensive unit tests with Mockito
  - Test scenarios:
    - Subscriber grants access without ticket
    - One-time visitor generates ticket and grants access
    - Client service called exactly once (both paths verified)
    - Multiple visitors receive unique tickets
  - Mock-based testing: ClientServiceClient and GateEventRepository mocked
  - ArgumentCaptor usage for verifying saved GateEvent details
  - **All tests passing** ✅
- **Architecture:**
  - Clean separation of concerns: Service -> Client -> Repository
  - Domain-driven design principles applied
  - Prepared for future REST endpoint integration

**Dependencies:**
- `ClientServiceClient` (Issue #48)
- `GateEventRepository` (Issue #46)
- `EntryDecision` DTO (new)

**Files Added:**
- `src/main/java/com/parking/gate_control_service/service/GateService.java`
- `src/main/java/com/parking/gate_control_service/dto/EntryDecision.java`
- `src/test/java/com/parking/gate_control_service/service/GateServiceTest.java`

**Next Steps:**
- Issue #50: Exit decision logic with billing integration
- Issue #51: REST endpoint POST /api/v1/gate/entry
- Issue #52: REST endpoint POST /api/v1/gate/exit

---

## [0.8.0] - 2026-01-26

### Added - Gate Control Service: Client Service Integration (Issue #48)

#### Issue #48: Implement Client Service Integration
- **Client Layer:**
  - `ClientServiceClient` for subscription validation via WebClient
  - `checkSubscription(String licensePlate)` method calling Client Service API
  - Fail-safe error handling: all errors result in access denial
  - GET request to `/api/v1/clients/subscriptions/check?licensePlate={plate}`
- **DTO Layer:**
  - `SubscriptionCheckResponse` DTO with fields:
    - `isAccessGranted`: Boolean flag for access decision
    - `clientId`: ID of the client (nullable)
    - `subscriptionId`: ID of the subscription (nullable)
- **Error Handling:**
  - 404 Not Found → access denied
  - 5xx Server Errors → access denied
  - Network/Timeout errors → access denied
  - All errors logged with SLF4J
- **Testing:**
  - 5 comprehensive unit tests with MockWebServer
  - Test scenarios:
    - Active subscription validation (200 OK)
    - Subscription not found (404)
    - Inactive subscription handling
    - Server error handling (500)
    - Network error/timeout handling
  - **All tests passing** ✅

**Dependencies:**
- WebClientConfig (Issue #47)

**Files Added:**
- `src/main/java/com/parking/gate_control_service/client/ClientServiceClient.java`
- `src/main/java/com/parking/gate_control_service/dto/SubscriptionCheckResponse.java`
- `src/test/java/com/parking/gate_control_service/client/ClientServiceClientTest.java`

---

## [0.7.0] - 2026-01-26

### Added - Gate Control Service: WebClient Configuration (Issue #47)

#### Issue #47: Setup WebClient for Inter-Service Communication
- **Configuration Layer:**
  - `WebClientConfig` with @Configuration for bean management
  - Four WebClient beans for microservices communication:
    - `clientServiceWebClient()` → Client Service
    - `billingServiceWebClient()` → Billing Service
    - `managementServiceWebClient()` → Management Service
    - `reportingServiceWebClient()` → Reporting Service
  - Base URL configuration via application.yml
  - Environment variable support for deployment flexibility
- **Application Configuration:**
  - `application.yml` updated with service URLs:
    - `services.client.url`: ${CLIENT_SERVICE_URL:http://localhost:8081}
    - `services.billing.url`: ${BILLING_SERVICE_URL:http://localhost:8082}
    - `services.management.url`: ${MANAGEMENT_SERVICE_URL:http://localhost:8083}
    - `services.reporting.url`: ${REPORTING_SERVICE_URL:http://localhost:8084}
- **Testing:**
  - Integration test verifying bean creation
  - All WebClient beans autowired and validated
  - **Test passing** ✅

**Files Added:**
- `src/main/java/com/parking/gate_control_service/config/WebClientConfig.java`
- `src/test/java/com/parking/gate_control_service/config/WebClientConfigTest.java`

**Files Modified:**
- `src/main/resources/application.yml`

---

## [0.6.0] - 2026-01-26

### Added - Gate Control Service: GateEvent Entity Implementation (Issue #46)

#### Issue #46: Create GateEvent JPA Entity and Repository
- **Entity Layer:**
  - `GateEvent` JPA entity mapping to `gate_events` table
  - Two enums for type safety:
    - `EventType`: ENTRY, EXIT, MANUAL_OPEN, ERROR
    - `Decision`: OPEN, DENY
  - Fields: id, eventType, licensePlate, ticketCode, gateId, decision, reason, timestamp, operatorId
  - `@PrePersist` method for automatic timestamp initialization
  - Lombok annotations for clean code (@Data, @NoArgsConstructor, @AllArgsConstructor)
- **Repository Layer:**
  - `GateEventRepository` extends JpaRepository
  - Custom query methods:
    - `findByLicensePlateOrderByTimestampDesc(String licensePlate)` - event history by license plate
    - `findByTimestampBetween(LocalDateTime start, LocalDateTime end)` - time-range queries
- **Database Migration:**
  - Flyway migration V9: `create_gate_events_table.sql`
  - Table with proper indexes for performance (license_plate, timestamp, gate_id)
  - Constraints: NOT NULL on critical fields, CHECK on enum values
- **Testing:**
  - 5 comprehensive integration tests with H2 in-memory database
  - Test coverage: save, findById, findByLicensePlate, findByTimestampBetween, enum validation
  - Test configuration: application-test.yml with H2 setup
  - **All tests passing** ✅
- **Architecture:**
  - Domain model pattern: Hibernate -> Domain model <- DTO (prepared for future implementation)
  - OpenAPI-first approach (ready for controller integration)

**Files:**
- Entity: `backend/gate-control-service/src/main/java/com/parking/gate_control_service/entity/GateEvent.java`
- Repository: `backend/gate-control-service/src/main/java/com/parking/gate_control_service/repository/GateEventRepository.java`
- Migration: `backend/gate-control-service/src/main/resources/db/migration/V9__create_gate_events_table.sql`
- Tests: `backend/gate-control-service/src/test/java/com/parking/gate_control_service/repository/GateEventRepositoryTest.java`

**Session Documentation:** [SESSION_DEVELOPMENT_2026-01-26_GATE_EVENT.md](./docs/sessions/SESSION_DEVELOPMENT_2026-01-26_GATE_EVENT.md)

---

## [0.5.0] - 2026-01-24

### Added - Billing Service: Complete REST API Implementation (Issues #34, #35, #36)

#### Issue #36: Payment Status Check Endpoint (/status)
- **REST API Endpoint:**
  - `GET /api/v1/billing/status?parkingEventId={id}` - comprehensive payment status check
  - Returns complete payment information including isPaid status and remaining fee
  - Support for both paid and unpaid tickets
  - History of all payment attempts (including failed/refunded)
- **Business Logic:**
  - `BillingService.getPaymentStatus()` - retrieve payment status with remaining fee calculation
  - Real-time fee calculation for unpaid tickets
  - Payment history tracking with all statuses (COMPLETED, FAILED, REFUNDED, PENDING)
- **Response DTO:**
  - PaymentStatusResponse: parkingEventId, isPaid, remainingFee, payments[]
  - PaymentInfo: paymentId, amount, status, paymentMethod, transactionId, paymentTime
- **Integration Tests:** 3 additional comprehensive tests
  - `getPaymentStatus_Success` - paid ticket status (isPaid=true, remainingFee=0.00)
  - `getPaymentStatus_Unpaid_WithRemainingFee` - unpaid ticket with remaining fee calculation
  - `getPaymentStatus_NotFound` - proper 404 handling for non-existent parking event
  - **All 10 integration tests passing** ✅
- **OpenAPI Documentation:**
  - Full specification with request/response examples for all scenarios
  - Documented error codes (400, 404, 500)
  - Examples: paid event, unpaid event, multiple payment attempts

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
  - `getPaymentStatus_Success` - payment status check (paid ticket)
  - `getPaymentStatus_Unpaid_WithRemainingFee` - unpaid ticket status ⭐ **NEW**
  - `getPaymentStatus_NotFound` - non-existent parking event ⭐ **NEW**
  - **All 10 integration tests passing** ✅

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
- **Test Coverage:** 57 tests total (19 repository + 28 service + 10 integration)
- **Code Quality:** All tests passing, proper exception handling, comprehensive logging
- **Documentation:** Full OpenAPI spec with examples, inline code documentation
- **Endpoints:** 3 REST endpoints (calculate, pay, status) - all fully functional
- **Status:** 🎉 Billing Service **100% COMPLETE** - Ready for Gate Control Service integration!

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
- **Total Tests in Billing Service:** 38 (18 repository + 18 service + 4 controller)
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
