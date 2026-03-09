# 🅿️ Parking System - Microservices Architecture

> 🇷🇺 **Русская версия:** [README_RU.md](./README_RU.md)

Modern parking lot management system built on microservices architecture using Spring Boot, Spring Cloud, and Docker.

## 🆕 Latest Updates

> **Showing last commit only.** Full history: [CHANGELOG.md](./CHANGELOG.md)

### 2026-03-09 — CORS wildcard for dynamic LAN IP (Issue #79 fix) ✅

- 🔧 **`SecurityConfiguration.java`**: `setAllowedOrigins()` → `setAllowedOriginPatterns()` — wildcard `http://192.168.*` support
- 🔧 **`CorsFilter.java`**: added `isOriginAllowed()` with wildcard logic, updated default `@Value`
- 🔧 **`application.yml`** + **`docker-compose.yml`**: hardcoded IP removed, `http://192.168.*` covers entire home LAN regardless of DHCP
- ✅ React frontend at `http://192.168.1.X:5173` works with any dynamic IP without config changes

---

## 📈 Project Status & Roadmap

### Current Status: Phase 3 — In Progress 🔄

```
Phase 0: ████████████████████ 100% ✅ COMPLETE
Phase 1: ████████████████████ 100% ✅ COMPLETE
Phase 2: ████████████████████ 100% ✅ COMPLETE
Phase 3: ████████████░░░░░░░░  60% 🔄 IN PROGRESS
Phase 4: ░░░░░░░░░░░░░░░░░░░░   0% ⏳ PENDING
Phase 5: ░░░░░░░░░░░░░░░░░░░░   0% ⏳ PENDING
```

### 📋 Project Phases Overview

| Phase | Duration | Status | Progress | Description |
|-------|----------|--------|----------|-------------|
| **Phase 0** | 1 week | ✅ Complete | 100% | Infrastructure & Foundation |
| **Phase 1** | 3 weeks | ✅ Complete | 100% | Basic Backend (CRUD & DB) |
| **Phase 2** | 2 weeks | ✅ Complete | 100% | Core Business Logic |
| **Phase 3** | 2 weeks | 🔄 In Progress | 60% | Security, CORS & React Frontend |
| **Phase 4** | 3 weeks | ⏳ Pending | 0% | Reports & Advanced E2E |
| **Phase 5** | 1 week | ⏳ Pending | 0% | Finalization & Deployment |

📖 **Detailed roadmap:** [PROJECT_PHASES.md](./docs/PROJECT_PHASES.md)

### 🎯 Phase 3 Progress

**Completed:**
- ✅ Issue #78 — RBAC: role-based route protection in SecurityFilter
- ✅ Issue #80 — Default OPERATOR user on application startup
- ✅ Issue #72 — Subscription check: real DB logic in client-service
- ✅ Issue #73 — E2E test: subscriber full parking cycle
- ✅ Issue #79 — CORS configuration in api-gateway (wildcard LAN support)
- ✅ Issue #74 — React frontend: project init, auth, role-based layout

**In Progress / Next:**
- 🔄 Issue #76 — Gate Control UI page
- 🔄 Issue #77 — Clients UI page
- ⏳ Issue #75 — Reports UI page

### 📊 Quick Stats

| Metric | Value |
|--------|-------|
| **Microservices** | 7 backend + 1 frontend |
| **Unit/Integration Tests** | 177+ passing |
| **E2E Tests** | 2 scenarios (visitor + subscriber) |
| **API Endpoints** | 20+ (via API Gateway) |
| **DB Migrations** | 10 (V0–V9) |
| **OpenAPI specs** | 5 services (OpenAPI-first) |

---

## 🏗️ System Architecture

```
  Browser / Mobile              React Frontend (Vite dev :5173)
        │                                  │
        │             proxy /api/*         │
        └──────────────────────────────────┘
                               │
                               ▼
                  ┌────────────────────────┐
                  │      API Gateway       │  :8086
                  │                        │
                  │  • JWT auth (HS512)    │
                  │  • RBAC (SecurityFilter│
                  │  • CORS (CorsFilter)   │
                  │  • Rate limit 60/min   │
                  │  • Brute-force (10x)   │
                  │  • Flyway migrations   │
                  └────────────┬───────────┘
                               │
            ┌──────────────────┼──────────────────┐
            │                  │                  │
            ▼                  ▼                  ▼
┌───────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│  client-service   │ │gate-control-svc  │ │  billing-service │
│     :8081         │ │     :8082        │ │     :8083        │
│                   │ │                  │ │                  │
│ • Clients CRUD    │ │ • POST /entry    │ │ • GET  /status   │
│ • Vehicles CRUD   │ │ • POST /exit     │ │ • POST /pay-test │
│ • Subscriptions   │ │ • POST /control  │ │                  │
│ • Sub.check API   │ │   (→ client :8081│ │                  │
└───────────────────┘ └──────────────────┘ └──────────────────┘

┌───────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│management-service │ │reporting-service │ │  eureka-server   │
│     :8084         │ │     :8087        │ │     :8761        │
│                   │ │                  │ │                  │
│ • GET  /spots     │ │ • POST /log      │ │ • Service        │
│ • GET  /available │ │ • GET  /logs     │ │   registry       │
│ • GET  /search    │ │                  │ │                  │
└───────────────────┘ └──────────────────┘ └──────────────────┘
            │                  │
            └──────────┬───────┘
                       ▼
        ┌──────────────────────────┐
        │   PostgreSQL :5433       │
        │   Redis       :6379      │
        └──────────────────────────┘

  ─────────────── Observability ────────────────
  Prometheus :9090 → Grafana :3000
  Jaeger     :16686
  OTel Collector :4317/:4318
  pgAdmin    :5050
```

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21+
- Maven 3.8+
- Node.js 20+ (for frontend dev)

### Launch System (Backend)
```bash
# Clone repository
git clone <repository-url>
cd parking-system

# Build all services
mvn clean install -DskipTests

# Start all containers
docker-compose up -d

# Check status
docker-compose ps
```

### Launch Frontend (Dev)
```bash
cd frontend
npm install
npm run dev
# Opens at http://localhost:5173
# Proxies /api/* → http://localhost:8086
```

### Service Access
| Service | Address | Notes |
|---------|---------|-------|
| **React Frontend** | http://localhost:5173 | Vite dev server |
| **API Gateway** | http://localhost:8086 | Entry point for all API calls |
| **Eureka Server** | http://localhost:8761 | Service registry UI |
| **Grafana** | http://localhost:3000 | admin / admin123 |
| **Prometheus** | http://localhost:9090 | Metrics |
| **Jaeger** | http://localhost:16686 | Distributed tracing |
| **pgAdmin** | http://localhost:5050 | admin@parking.com / admin |
| **PostgreSQL** | localhost:5433 | parking_db / postgres / postgres |
| **Test Interface** | [devops/test-login.html](./devops/test-login.html) | Browser-based API tester |

---

## 🔧 Microservices

### 1. API Gateway (Port 8086)
Centralized entry point for all API calls. All requests from frontend pass through here.

- **JWT authentication** — HS512, 30 min access / 12 h refresh tokens
- **RBAC** — role-based route protection in `SecurityFilter`
- **CORS** — wildcard `http://192.168.*` covers any LAN IP (DHCP-safe)
- **Rate limiting** — 60 req/min per IP
- **Brute-force protection** — lockout after 10 failed attempts
- **Flyway** — manages all DB schema migrations (V0–V9)
- **Service discovery** — routes to microservices via Eureka

**Auth Endpoints:**
- `POST /api/auth/login` — authenticate, returns JWT
- `POST /api/auth/refresh` — refresh access token
- `POST /api/auth/logout` — invalidate session

### 2. Client Service (Port 8081)
Client, vehicle, and subscription management. OpenAPI-first (`src/main/resources/openapi.yaml` → generated interfaces).

**Client Endpoints** (via API Gateway):
- `POST   /api/clients` — create client
- `GET    /api/clients` — list all clients
- `GET    /api/clients/{id}` — get by ID
- `GET    /api/clients/search?phone={phone}` — search by phone
- `PUT    /api/clients/{id}` — update client
- `DELETE /api/clients/{id}` — delete client

**Vehicle Endpoints:**
- `POST   /api/clients/{clientId}/vehicles` — add vehicle to client
- `GET    /api/vehicles/{id}` — get vehicle by ID
- `PUT    /api/vehicles/{id}` — update vehicle
- `DELETE /api/vehicles/{id}` — delete vehicle

**Subscription Endpoints:**
- `GET    /api/v1/clients/subscriptions/check?licensePlate={plate}` — check active subscription (used by gate-control)

### 3. Gate Control Service (Port 8082)
Handles physical gate events. OpenAPI-first. Calls client-service to check subscriptions.

- `POST /api/v1/gate/entry` — vehicle entry (returns ticket or subscriber pass)
- `POST /api/v1/gate/exit` — vehicle exit (calculates fee)
- `POST /api/v1/gate/control` — manual gate control (OPEN/CLOSE)

### 4. Billing Service (Port 8083)
Parking fee calculation and payment recording.

- `GET  /api/v1/billing/status-by-ticket?ticketCode={code}` — payment status
- `POST /api/v1/billing/pay-test` — test payment endpoint

### 5. Management Service (Port 8084)
Parking space inventory and availability. OpenAPI-first.

- `GET /api/management/spots` — all parking spaces
- `GET /api/management/spots/available` — available spaces
- `GET /api/management/spots/available/count` — count available
- `GET /api/management/spots/available/lot/{lotId}` — available by lot
- `GET /api/management/spots/search?type={type}&status={status}` — filtered search

**Space Types:** STANDARD, HANDICAPPED, ELECTRIC, VIP, COMPACT, OVERSIZED  
**Statuses:** AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, OUT_OF_SERVICE

### 6. Reporting Service (Port 8087)
System event logging and audit trail. OpenAPI-first.

- `POST /api/reporting/log` — log event from any service
- `GET  /api/reporting/logs` — retrieve logs (filters: level, service, userId, date range)

### 7. Eureka Server (Port 8761)
Service registry — all microservices register here for discovery.

### 8. Observability Stack
- **Prometheus** (:9090) — metrics collection from all services
- **Grafana** (:3000) — dashboards (admin/admin123)
- **Jaeger** (:16686) — distributed tracing
- **OpenTelemetry Collector** (:4317/:4318) — telemetry aggregation

### 9. Infrastructure
- **PostgreSQL 16** (:5433) — main database, schema managed by Flyway
- **Redis 7** (:6379) — JWT blacklist, rate-limit counters, session cache
- **pgAdmin 4** (:5050) — database UI

---

## 📊 Technology Stack

### Backend
| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 21 | Main language |
| **Spring Boot** | 3.2.8 | Microservices framework |
| **Spring Cloud** | 2023.0.3 | Eureka, LoadBalancer |
| **Spring Security** | (Boot managed) | Auth & RBAC |
| **Spring Data JPA** | (Boot managed) | DB operations |
| **Flyway** | (Boot managed) | DB migrations |
| **JWT (jjwt)** | 0.12.6 | Token auth |
| **MapStruct** | 1.5.5 | DTO mapping |
| **Lombok** | 1.18.34 | Boilerplate reduction |
| **OpenAPI Generator** | (Maven plugin) | OpenAPI-first codegen |

### Frontend
| Technology | Version | Purpose |
|-----------|---------|---------|
| **React** | 19 | UI framework |
| **TypeScript** | — | Type safety |
| **Vite** | — | Build tool + dev proxy |
| **Tailwind CSS** | 3 | Styling |
| **Radix UI** | — | shadcn/ui components |
| **React Router** | 6 | Client-side routing |
| **TanStack Query** | 5 | Server state management |
| **Zustand** | 4 | Client state (auth store) |
| **Axios** | — | HTTP client |

### Infrastructure
- **Docker & Docker Compose** — containerization
- **PostgreSQL 16** — main database
- **Redis 7** — caching & session storage
- **OpenTelemetry** — distributed tracing instrumentation

---

## 🗄️ Database

### PostgreSQL Configuration
- **Database**: `parking_db`
- **Username**: `postgres`
- **Password**: `postgres`
- **Port**: `5433` (host) / `5432` (inside Docker network)

### Flyway Migrations

Schema managed by **Flyway** (runs on api-gateway startup).  
Files: `backend/api-gateway/src/main/resources/db/migration/`

| Version | File | Description |
|---------|------|-------------|
| V0 | `V0__Baseline.sql` | Baseline marker |
| V1 | `V1__initial_schema.sql` | Core tables: users, clients, vehicles, subscriptions, logs |
| V2 | `V2__add_parking_lots.sql` | Parking lot facilities |
| V3 | `V3__add_parking_spaces.sql` | Individual parking spaces |
| V4 | `V4__add_bookings.sql` | Reservation system |
| V5 | `V5__insert_test_parking_data.sql` | Seed data for dev/testing |
| V6 | `V6__extend_logs_table.sql` | Extended logging fields |
| V7 | `V7__create_tariffs_table.sql` | Billing tariffs |
| V8 | `V8__extend_parking_events_and_payments.sql` | Parking events & payments |
| V9 | `V9__create_gate_events_table.sql` | Gate hardware events |

```powershell
# View migration history
docker exec parking_db psql -U postgres -d parking_db -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;"
```

### Core Schema (abbreviated)
```sql
-- System users (authentication)
users (id, username, password_hash, user_role, enabled, failed_login_attempts, ...)

-- Parking clients
clients (id, full_name, phone_number, email, registered_at)

-- Client vehicles
vehicles (id, client_id→clients, license_plate, is_allowed, ...)

-- Subscriptions
subscriptions (id, client_id→clients, start_date, end_date, type, is_active)

-- Parking spaces
parking_lots (id, name, address, capacity, ...)
parking_spaces (id, lot_id→parking_lots, space_number, type, status)

-- Events & billing
parking_events (id, license_plate, entry_time, exit_time, ticket_code, ...)
payments (id, event_id→parking_events, amount, paid_at, ...)
gate_events (id, gate_id, action, result, timestamp, ...)
tariffs (id, name, price_per_hour, ...)
```

---

## 🔑 Test Credentials

Users are seeded by `database/init.sql` (via Docker init) and additionally ensured by `UserSecurityService.initializeDefaultUsers()` on startup.

| Username | Password | Role | Notes |
|----------|----------|------|-------|
| **admin** | `parking123` | ADMIN | Full access |
| **operator** | `parking123` | OPERATOR | Gate & billing ops |
| **manager** | `manager123` | MANAGER | Reports & management |

**⚠️ Development only.** Use strong passwords and env vars in production.

### Quick Auth Test
```powershell
# PowerShell
$body = '{"username":"admin","password":"parking123"}'
$r = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
$r.Content | ConvertFrom-Json | Select-Object accessToken, user
```
```bash
# curl
curl -s -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}' | jq .accessToken
```

---

## 🔒 Security & RBAC

### Route Permissions (SecurityFilter)

| Route | Required Roles |
|-------|---------------|
| `POST/PUT/DELETE /api/v1/gate/*` | OPERATOR, ADMIN |
| `POST/PUT/DELETE /api/v1/billing/*` | OPERATOR, ADMIN |
| `GET/POST/PUT/DELETE /api/clients/*` | ADMIN, MANAGER, OPERATOR |
| `write ops /api/management/*` | ADMIN, MANAGER |
| `GET /api/reporting/*` | ADMIN, MANAGER, OPERATOR |

### JWT Configuration
```yaml
jwt:
  access-token-expiration: 1800    # 30 minutes
  refresh-token-expiration: 43200  # 12 hours
  secret: ${JWT_SECRET}            # HS512, min 64 chars
```

### Rate Limiting
- 60 requests/min per IP (configurable via `RATE_LIMIT_MINUTE`)
- Brute-force lockout after 10 failed login attempts (`BRUTE_FORCE_THRESHOLD`)

---

## 🧪 Testing

### E2E Tests
```powershell
# Run both E2E scenarios (requires Docker services running)
cd devops
.\run-e2e-tests.ps1

# Or directly with Maven
cd backend
mvn test -Pe2e
```

**E2E Scenarios:**
- ✅ `OneTimeVisitorE2ETest` — Entry → ticket → payment → exit
- ✅ `SubscriberE2ETest` — Entry with active subscription → free exit

### Unit & Integration Tests
```bash
# Run all unit + integration tests
mvn clean test

# Single module
cd backend/api-gateway
mvn test
```

**Test counts (approximate):**
- api-gateway: ~80 tests
- client-service: ~26 tests
- gate-control-service: ~30 tests
- billing-service: ~20 tests
- management-service: ~21 tests
- **Total: 177+ tests**

### Manual Testing
```bash
# Health checks
curl http://localhost:8086/actuator/health
curl http://localhost:8081/actuator/health

# Authenticated request
TOKEN=$(curl -s -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}' | jq -r .accessToken)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8086/api/clients
```

---

## 🔧 Configuration

### Key Environment Variables
```bash
# JWT (MUST be 64+ chars in production)
JWT_SECRET=<YOUR_64_CHAR_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION=1800    # seconds
JWT_REFRESH_TOKEN_EXPIRATION=43200  # seconds

# CORS (comma-separated, supports wildcards)
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000,http://192.168.*

# Rate limiting
RATE_LIMIT_MINUTE=60
BRUTE_FORCE_THRESHOLD=10

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://parking_db:5432/parking_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Redis
SPRING_REDIS_HOST=parking_redis
SPRING_REDIS_PORT=6379

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

### Production Security Checklist
- [ ] Change all default passwords
- [ ] Set `JWT_SECRET` to a 64+ char random string
- [ ] Set `SPRING_DATASOURCE_PASSWORD` to a strong password
- [ ] Set `SPRING_REDIS_PASSWORD`
- [ ] Enable HTTPS/TLS
- [ ] Set `SPRING_PROFILES_ACTIVE=production`
- [ ] Restrict `CORS_ALLOWED_ORIGINS` to your domain

---

## 💻 Development

### Project Structure
```
parking-system/
├── backend/
│   ├── api-gateway/          # JWT auth, RBAC, CORS, Flyway, proxy
│   ├── client-service/       # Clients, vehicles, subscriptions
│   ├── gate-control-service/ # Gate entry/exit/control
│   ├── billing-service/      # Fees & payments
│   ├── management-service/   # Parking space inventory
│   ├── reporting-service/    # Audit logs & reports
│   ├── eureka-server/        # Service registry
│   ├── parking-common/       # Shared DTOs/utils
│   └── e2e-tests/            # End-to-end test suite
├── frontend/                 # React 19 + Vite + TypeScript
│   └── src/
│       ├── api/              # Axios service clients
│       ├── pages/            # Route pages
│       ├── components/       # Shared UI components
│       ├── layouts/          # AppLayout with sidebar
│       ├── store/            # Zustand auth store
│       └── types/            # TypeScript types
├── database/
│   └── init.sql              # DB init (runs once on first Docker start)
├── devops/                   # PowerShell scripts, Docker configs
│   └── observability/        # Prometheus, Grafana, OTel configs
└── docs/                     # Architecture & API documentation
```

### OpenAPI-First Services

Five business services follow the **OpenAPI-first** approach — `openapi.yaml` is the source of truth, Maven plugin generates Java interfaces, controllers implement them:

| Service | Spec | Generated package |
|---------|------|-------------------|
| `client-service` | `src/main/resources/openapi.yaml` | `generated.controller` |
| `gate-control-service` | `src/main/resources/openapi.yaml` | `generated.api` |
| `billing-service` | `src/main/resources/openapi.yaml` | `generated.api` |
| `management-service` | `src/main/resources/openapi.yaml` | `generated.api` |
| `reporting-service` | `src/main/resources/openapi.yaml` | `generated.controller` |

Generated interfaces reside in `target/generated-sources/openapi/`. Controllers implement these interfaces (`implements GateApi`, `implements BillingApi`, etc.).

> **`api-gateway`** does **not** use OpenAPI-first — it is infrastructure, not a business API. Its controllers are either auth endpoints (`AuthController`) or transparent proxy controllers that forward requests to downstream services.

### Development Workflow
```bash
# 1. Start infrastructure only
docker-compose up -d postgres redis eureka-server

# 2. Run a service locally (hot-reload)
cd backend/api-gateway
mvn spring-boot:run

# 3. Frontend dev server (proxies /api/* to :8086)
cd frontend
npm run dev
```

---

## 📚 Documentation

### Architecture & Security
- [Authentication Architecture](./docs/AUTHENTICATION.md)
- [Security Architecture](./docs/SECURITY_ARCHITECTURE.md)
- [Observability Setup](./docs/OBSERVABILITY_SETUP.md)
- [API Contracts](./docs/api-contracts.md)

### Database & Deployment
- [Database README](./database/README.md)
- [Deployment Guide](./docs/DEPLOYMENT_GUIDE.md)
- [Production Config Quick Ref](./docs/PRODUCTION_CONFIG_QUICK_REF.md)

### DevOps Scripts (`devops/`)
| Script | Purpose |
|--------|---------|
| `start-all.ps1` | Start full system |
| `stop-system.ps1` | Stop all containers |
| `full-rebuild.ps1` | Rebuild all images and restart |
| `run-e2e-tests.ps1` | Run E2E test suite |
| `check-system.ps1` | Health check all services |
| `backup-db.ps1` | Backup PostgreSQL |
| `unlock-account.ps1` | Unlock brute-force locked user |
| `reset-brute-force.ps1` | Reset failed login counter |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

---

**Made with ❤️ using Spring Boot and React**
