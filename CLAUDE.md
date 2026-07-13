# CLAUDE.md — Parking Lot Management System

## Project Overview

Microservices-based parking management system.  
**Backend:** Java 21 + Spring Boot 3.2.8 (Maven multi-module)  
**Frontend:** React 19 + TypeScript + Vite + Tailwind CSS

---

## Repository Structure

```
parking-system/
├── backend/
│   ├── api-gateway/          # JWT auth, reverse proxy, Flyway migrations, Redis brute-force
│   ├── client-service/       # Clients, vehicles, subscriptions (OpenAPI-first)
│   ├── billing-service/      # Fee calculation, payments (OpenAPI-first)
│   ├── gate-control-service/ # Entry/exit gate decisions
│   ├── management-service/   # Parking spaces CRUD
│   ├── reporting-service/    # Audit logs / system logs
│   ├── eureka-server/        # Service discovery
│   └── parking-common/       # Shared JPA entities (Client, Vehicle, Subscription, Tariff, Log)
├── frontend/                 # React SPA
├── database/
│   └── init.sql              # Initial schema + seed data
└── devops/                   # Docker Compose files and PowerShell scripts
```

---

## Service Ports

| Service              | Internal port | External port |
|----------------------|---------------|---------------|
| api-gateway          | 8080          | **8086**      |
| client-service       | 8081          | 8081          |
| gate-control-service | 8080          | 8082          |
| billing-service      | 8080          | 8083          |
| reporting-service    | 8080          | 8084          |
| eureka-server        | 8761          | 8761          |
| PostgreSQL           | 5432          | **5433**      |
| Redis                | 6379          | 6379          |
| pgAdmin              | 80            | 5050          |

> Frontend dev server runs on **5173** and proxies `/api` → `http://127.0.0.1:8086`.

---

## Build & Run Commands

### Backend (from project root)

```powershell
# Build all modules, skip tests
mvn clean package -DskipTests

# Run unit tests for all services
mvn test

# Run E2E tests (requires running Docker stack)
mvn test -Pe2e

# Build & test a single service
mvn test -pl backend/billing-service
mvn test -pl backend/gate-control-service
```

### Frontend

```powershell
cd frontend
npm install
npm run dev      # dev server on :5173
npm run build    # production build
npm run lint     # ESLint check
```

### Docker (from devops/)

```powershell
# Full system (infra + services + observability)
docker compose -f docker-compose.yml up --build -d          # root docker-compose.yml


# Full rebuild from scratch
.\devops\full-rebuild.ps1
```

---

## Architecture Decisions

### Shared Database
All services connect to a **single PostgreSQL database** (`parking_db`).  
Tables: `users`, `clients`, `vehicles`, `subscriptions`, `parking_events`, `payments`, `gate_events`, `logs`, `tariffs`.

### CORS Configuration
- Handled by `CorsFilter` (`@Order(0)`) — runs **before** `SecurityFilter` (`@Order(1)`)
- OPTIONS preflight returns **HTTP 200 immediately**, SecurityFilter is bypassed
- `allowCredentials` is intentionally **`false`** — JWT stored in `localStorage`, sent via `Authorization` header (no cookies)
- Allowed origins: `http://localhost:5173`, `http://localhost:3000`, `http://192.168.*` (wildcard covers entire LAN, DHCP-safe)
- Wildcard pattern matching via `setAllowedOriginPatterns()` (not `setAllowedOrigins()` — the latter silently ignores wildcards)

### RBAC (Role-Based Access Control)
Implemented in `SecurityFilter` (`api-gateway`). First-match-wins prefix scan against `ROUTE_ROLES` map:

| Route pattern | Allowed roles |
|---------------|---------------|
| `POST\|PUT\|DELETE /api/gate/**` | OPERATOR, ADMIN |
| `POST\|PUT\|DELETE /api/billing/**` | OPERATOR, ADMIN |
| `GET\|POST\|PUT\|DELETE /api/clients/**` | ADMIN, MANAGER, OPERATOR |
| `POST\|PUT\|DELETE /api/management/**` | ADMIN, MANAGER |
| `GET\|POST\|PUT\|DELETE /api/reporting/**` | ADMIN, MANAGER, OPERATOR |

Returns **HTTP 403** with `{"error":"Forbidden","message":"Access denied: insufficient role for this operation"}` on mismatch.

### JWT Configuration
```yaml
jwt:
  algorithm: HS512
  access-token-expiration: 1800     # 30 minutes
  refresh-token-expiration: 43200   # 12 hours
  secret: ${JWT_SECRET}             # minimum 64 characters
```
Claim names in token payload: `userId`, `role` (not `user_id` / `roles`).

### Rate Limiting & Brute-Force Protection
- **Rate limit:** 60 requests/min per IP (env: `RATE_LIMIT_MINUTE`)
- **Brute-force lockout:** after 10 failed login attempts (env: `BRUTE_FORCE_THRESHOLD`)
- Counters stored in **Redis**

### Service-to-Service Communication
Services call each other via HTTP using URLs from environment variables:
- `CLIENT_SERVICE_URL` — gate-control → client-service
- `BILLING_SERVICE_URL` — gate-control → billing-service
- `REPORTING_SERVICE_URL` — services → reporting-service (audit)

### Flyway Migrations
Only **api-gateway** runs Flyway migrations (creates/alters the shared schema).  
Other services use `spring.jpa.hibernate.ddl-auto=validate`.  
Migration scripts live in `backend/api-gateway/src/main/resources/db/migration/`.

**Current migration state:**

| Version | File | Description |
|---------|------|-------------|
| V0 | `V0__Baseline.sql` | Baseline marker |
| V1 | `V1__initial_schema.sql` | users, clients, vehicles, subscriptions, logs |
| V2 | `V2__add_parking_lots.sql` | Parking lot facilities |
| V3 | `V3__add_parking_spaces.sql` | Individual parking spaces |
| V4 | `V4__add_bookings.sql` | Reservation system |
| V5 | `V5__insert_test_parking_data.sql` | Dev/test seed data |
| V6 | `V6__extend_logs_table.sql` | Extended audit fields |
| V7 | `V7__create_tariffs_table.sql` | Billing tariffs |
| V8 | `V8__extend_parking_events_and_payments.sql` | license_plate, entry/exit_method, is_subscriber, transaction_id |
| V9 | `V9__create_gate_events_table.sql` | gate_events table |
| V10 | `V10__extend_logs_audit_trail.sql` | action, entity_type, entity_id, client_id, license_plate in logs |
| V11 | `V11__subscription_parking_space.sql` | parking_space_id in subscriptions |

> **Next migration:** V12. Always add new scripts to api-gateway; never alter an existing migration file.

```powershell
# View migration history in running DB
docker exec parking_db psql -U postgres -d parking_db -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;"
```

### OpenAPI-First Code Generation
`billing-service`, `client-service`, `reporting-service`, and `management-service` generate  
Java controller interfaces and model classes from `src/main/resources/openapi.yaml` at build time.  
Controllers implement the generated `*Api` interface — **do not edit generated classes directly**.

Generated sources land in `target/generated-sources/openapi/`:

| Service | Implements | Generated package |
|---------|-----------|-------------------|
| `client-service` | `ClientApi`, `VehicleApi`, `SubscriptionApi` | `generated.controller` |
| `gate-control-service` | `GateApi` | `generated.api` |
| `billing-service` | `BillingApi` | `generated.api` |
| `management-service` | `ManagementApi` | `generated.api` |
| `reporting-service` | `ReportingApi` | `generated.controller` |

> `api-gateway` does **not** use OpenAPI-first — it is infrastructure (auth + proxy), not a business API.

---

## Testing Rules

### Unit Tests (`@ExtendWith(MockitoExtension.class)` + `@InjectMocks`)

**Critical:** any service that injects `AuditLogger` requires a `@Mock` for it in the test,  
otherwise Mockito leaves the field `null` and all calls to `auditLogger.audit(...)` throw NPE.

Services that use `AuditLogger`:
- `BillingService` → `BillingServiceTest` ✅ has `@Mock AuditLogger`
- `GateService` → `GateServiceTest` ✅ has `@Mock AuditLogger`
- `ClientService` → `ClientServiceTest` ✅ has `@Mock AuditLogger`
- `SubscriptionService` → `SubscriptionServiceTest` ✅ has `@Mock AuditLogger`
- `VehicleService` → `VehicleServiceTest` ✅ has `@Mock AuditLogger`

**Template for new service unit tests:**
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock private MyRepository myRepository;
    @Mock private AuditLogger auditLogger;   // always required if service uses it
    @InjectMocks private MyService myService;
}
```

### Integration Tests (`@SpringBootTest`)
Use `@MockBean` instead of `@Mock`. Spring context auto-wires real beans.  
Active profile: `test` (H2 or Testcontainers depending on the service).

### E2E Tests (`backend/e2e-tests/`)
- Run via `mvn test -Pe2e`
- Use Testcontainers + `docker-compose-e2e.yml`
- Require Docker daemon running and all service images pre-built
- `SubscriberE2ETest` — verifies subscriber gate cycle (entry → exit → billing returns 404)
- `OneTimeVisitorE2ETest` — verifies visitor cycle (entry → payment → exit)

---

## Frontend Architecture

### Directory Layout

```
frontend/src/
├── api/          # Axios API client functions (one file per domain)
│   ├── auth.ts, billing.ts, clients.ts, gate.ts
│   ├── management.ts, reporting.ts, spaces.ts, subscriptions.ts
├── components/   # Reusable UI components (shadcn/Radix primitives)
├── layouts/      # AppLayout (nav sidebar + outlet)
├── i18n/
│   └── translations.ts # EN/DE/UA/RU translation dictionaries + typed helper
├── lib/
│   └── apiClient.ts   # Axios instance with JWT interceptor + JsonNullable unwrapper
├── pages/        # Route-level page components
├── store/
│   └── authStore.ts   # Zustand store (JWT token, user info)
│   └── languageContext.tsx # i18n language context + localStorage persistence
└── types/        # Shared TypeScript types
```

### Key Libraries
- **Routing:** React Router v6
- **Server state:** TanStack Query v5
- **Client state:** Zustand
- **HTTP:** Axios (baseURL `/api`, JWT interceptor)
- **UI:** Radix UI primitives + Tailwind CSS + `clsx`/`tailwind-merge`
- **Icons:** lucide-react

### API Base URL
Vite dev proxy rewrites `/api` → `http://127.0.0.1:8086/api`.  
All `apiClient` calls use relative paths: `apiClient.get('/v1/clients')`.

### JsonNullable
Backend uses `org.openapitools.jackson.nullable.JsonNullable` for optional fields.  
`apiClient.ts` includes a response interceptor that automatically unwraps `JsonNullable`  
wrappers so the frontend never sees `{ present: true, value: "..." }` objects.

### Internationalization (i18n)
- Frontend UI supports **EN**, **DE**, **UA** (`uk` locale code), and **RU**
- Translation source of truth: `frontend/src/i18n/translations.ts` (`Language = 'en' | 'de' | 'uk' | 'ru'`)
- `LanguageProvider` from `frontend/src/store/languageContext.tsx` wraps the app in `frontend/src/App.tsx`
- Selected language is persisted in `localStorage` under key: `parking-system-language`
- Language switchers are available on **LoginPage** and in **AppLayout** sidebar
- Pages use `useLanguage().t(key, params)` for runtime text translation

---

## Common Pitfalls

| Pitfall | Solution |
|---------|----------|
| `auditLogger` is `null` in unit tests | Add `@Mock private AuditLogger auditLogger;` |
| Billing returns 200 for subscriber's `parkingEventId` | `billing-service/BillingController.getPaymentStatus()` checks `parkingEventRepository.existsById()` first → 404 if not found |
| Gate event IDs ≠ billing ParkingEvent IDs | They are independent BIGSERIAL sequences in different tables |
| PostgreSQL port | Use **5433** (not 5432) — local port-mapped to avoid conflict |
| E2E tests fail with "services not ready" | Run `.\devops\full-rebuild.ps1` or wait for all healthchecks to pass |
| `ddl-auto=validate` fails after schema change | Add Flyway migration script in api-gateway, increment version (V12, V13, …) |
| Frontend `/api` calls fail locally | Ensure api-gateway is running on port 8086 |

---

## Seed Data (init.sql)

| Entity | Data |
|--------|------|
| Users | `admin` / `parking123`, `user` / `user1234`, `manager` / `manager123`, `operator` / `operator123` |
| Clients | ID 1 — "Абонент Тест" (+380501112233), ID 2 — "Разовый Гость" |
| Vehicles | ID 1 — `AA1234BB` (client 1), ID 2 — `BB5678CC` (client 2) |
| Subscriptions | ID 1 — ANNUAL, active, NOW → NOW+1year (for plate `AA1234BB`) |
| Tariffs | ONE_TIME — 50.00 UAH/hour |

---

## Observability Stack (optional)

Start with: `docker compose -f devops/docker-compose-observability.yml up -d`

| Tool | URL |
|------|-----|
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |
| Jaeger (traces) | http://localhost:16686 |
| pgAdmin | http://localhost:5050 (admin@parking.com / admin) |
| Eureka dashboard | http://localhost:8761 |

---

## API Endpoints Reference

### Auth (api-gateway — no JWT required)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Authenticate, returns `accessToken` + `refreshToken` |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Invalidate session |

### Client Service (`/api/clients/**`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/clients` | Create client |
| GET | `/api/clients` | List all clients |
| GET | `/api/clients/{id}` | Get by ID |
| GET | `/api/clients/search?phone={phone}` | Search by phone |
| PUT | `/api/clients/{id}` | Update client |
| DELETE | `/api/clients/{id}` | Delete client |
| POST | `/api/clients/{id}/vehicles` | Add vehicle to client |
| GET | `/api/clients/{id}/vehicles` | List client's vehicles |
| PUT | `/api/vehicles/{id}` | Update vehicle |
| DELETE | `/api/vehicles/{id}` | Delete vehicle |
| GET | `/api/v1/clients/subscriptions/check?licensePlate={plate}` | Check active subscription (used by gate-control) |

### Gate Control Service (`/api/gate/**`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/gate/entry` | Vehicle entry — returns ticket or subscriber pass |
| POST | `/api/gate/exit` | Vehicle exit — checks payment, opens gate |
| POST | `/api/gate/control` | Manual gate open/close (OPERATOR) |

### Billing Service (`/api/billing/**`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/billing/calculate` | Calculate fee (entryTime, exitTime, tariffType) |
| POST | `/api/billing/pay` | Record payment |
| GET | `/api/billing/status?parkingEventId={id}` | Payment status — **404 if no billing record** |
| GET | `/api/billing/status-by-ticket?ticketCode={code}` | Payment status by ticket |
| POST | `/api/billing/pay-test` | Simplified payment for E2E/testing |

### Management Service (`/api/management/**`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/management/spots` | All parking spaces |
| GET | `/api/management/spots/available` | Available spaces |
| GET | `/api/management/spots/available/count` | Count available |
| GET | `/api/management/spots/available/lot/{lotId}` | Available by lot |
| GET | `/api/management/spots/search?type={t}&status={s}` | Filtered search |

**Space types:** `STANDARD`, `HANDICAPPED`, `ELECTRIC`, `VIP`, `COMPACT`, `OVERSIZED`  
**Space statuses:** `AVAILABLE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`, `OUT_OF_SERVICE`

### Reporting Service (`/api/reporting/**`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/reporting/log` | Write audit log entry |
| GET | `/api/reporting/logs` | Retrieve logs (filters: level, service, userId, date range) |

---

## Key Environment Variables

```bash
# JWT (MUST be 64+ chars in production)
JWT_SECRET=<YOUR_64_CHAR_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION=1800    # seconds (30 min)
JWT_REFRESH_TOKEN_EXPIRATION=43200  # seconds (12 h)

# CORS (comma-separated, supports wildcards)
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000,http://192.168.*

# Rate limiting & brute-force
RATE_LIMIT_MINUTE=60
BRUTE_FORCE_THRESHOLD=10

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://parking_db:5432/parking_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Redis
SPRING_REDIS_HOST=parking_redis
SPRING_REDIS_PORT=6379

# Service URLs (used by gate-control-service)
CLIENT_SERVICE_URL=http://client-service:8081
BILLING_SERVICE_URL=http://billing-service:8080
REPORTING_SERVICE_URL=http://reporting-service:8080

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

---

## DevOps Scripts (`devops/`)

| Script | Purpose |
|--------|---------|
| `full-rebuild.ps1` | ⭐ Full rebuild from scratch + automated endpoint tests |
| `start-all.ps1` | Start full system |
| `stop-system.ps1` | Stop all containers |
| `check-system.ps1` | Health check all services |
| `run-e2e-tests.ps1` | E2E runner with Docker health check (`-SkipBuild`, `-SkipDockerBuild` flags) |
| `backup-db.ps1` | Backup PostgreSQL |
| `unlock-account.ps1` | Unlock brute-force locked user |
| `reset-brute-force.ps1` | Reset failed login counter |
| `test-proxy.ps1` | Smoke tests for all proxy endpoints |

---

## Quick Auth Test

```powershell
# PowerShell — get JWT token
$body = '{"username":"admin","password":"parking123"}'
$r = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
     -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
$token = ($r.Content | ConvertFrom-Json).accessToken

# Use token
Invoke-WebRequest -Uri "http://localhost:8086/api/clients" `
     -Headers @{Authorization="Bearer $token"} -UseBasicParsing
```

```bash
# curl — get JWT token
TOKEN=$(curl -s -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}' | jq -r .accessToken)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8086/api/clients
```
