# API Gateway

Central entry point for all Parking System API calls. Handles JWT authentication, RBAC, CORS, rate limiting, and proxying to downstream microservices.

## Responsibilities

- **JWT Authentication** — HS512 tokens, 30 min access / 12 h refresh
- **RBAC** — role-based route protection via `SecurityFilter`
- **CORS** — `CorsFilter` with wildcard `http://192.168.*` for LAN access
- **Rate limiting** — 60 req/min per IP (Redis-backed)
- **Brute-force protection** — lockout after 10 failed login attempts
- **Flyway** — manages all DB schema migrations (V0–V9)
- **Proxy controllers** — transparent forwarding to downstream services

## Auth Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Authenticate, returns JWT + refresh token |
| POST | `/api/auth/refresh` | Exchange refresh token for new access token |
| POST | `/api/auth/logout` | Invalidate session |

## Security Architecture

```
Request
  │
  ├─ CorsFilter (@Order(0))          — preflight → 200 immediately
  │
  └─ SecurityFilter (@Order(1))
       ├─ STEP 0: Docker internal IP? → bypass
       ├─ STEP 1: Public path? (/api/auth/**, /actuator/**)  → pass
       ├─ STEP 2: JWT present? → validate (jjwt 0.12.6 / HS512)
       ├─ STEP 3: Token blacklist? (Redis) → 401
       ├─ STEP 4: Rate limit? (Redis, 60/min) → 429
       └─ STEP 5: RBAC role check → 403 on mismatch
```

### RBAC Route Permissions

| Route pattern | Required roles |
|---------------|---------------|
| `POST/PUT/DELETE /api/v1/gate/*` | OPERATOR, ADMIN |
| `POST/PUT/DELETE /api/v1/billing/*` | OPERATOR, ADMIN |
| `GET/POST/PUT/DELETE /api/clients/*` | ADMIN, MANAGER, OPERATOR |
| write ops `/api/management/*` | ADMIN, MANAGER |
| `GET /api/reporting/*` | ADMIN, MANAGER, OPERATOR |

## Test Configuration

The project uses a test profile to isolate unit tests from external systems.  
Config: `src/test/resources/application-test.yml`

What it does:
- Disables Flyway during tests (no real Postgres needed)
- Uses in-memory H2 datasource
- Excludes OpenTelemetry auto-configuration
- Sets Hibernate to `create-drop` with H2 dialect

Why:
- Keeps tests hermetic and fast
- No external dependency flakiness in CI
- No database containers needed for unit/smoke tests

For tests against real Postgres/OTLP — use the `e2e` Maven profile with Testcontainers.

## Key Configuration (application.yml)

```yaml
jwt:
  access-token-expiration: 1800     # 30 minutes
  refresh-token-expiration: 43200   # 12 hours
  secret: ${JWT_SECRET}             # HS512, min 64 chars

app:
  rate-limiting:
    enabled: true
    max-requests-per-minute: ${RATE_LIMIT_MINUTE:60}
  brute-force-threshold: ${BRUTE_FORCE_THRESHOLD:10}

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000,http://192.168.*}
```

## Test Credentials

| Username | Password | Role |
|----------|----------|------|
| `admin` | `parking123` | ADMIN |
| `operator` | `parking123` | OPERATOR |
| `manager` | `manager123` | MANAGER |

Seeded by `database/init.sql`. Also ensured on startup by `UserSecurityService.initializeDefaultUsers()`.
