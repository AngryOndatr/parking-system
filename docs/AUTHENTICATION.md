# Authentication (Canonical Reference)

> 🇷🇺 **Русская версия:** [AUTHENTICATION_RU.md](./AUTHENTICATION_RU.md)

**Last Updated:** 2026-08-10  
**Scope:** API Gateway authentication and JWT usage across the system

---

## 1. Overview

Parking System uses JWT authentication through **api-gateway** (`:8086`).

High-level flow:
1. `POST /api/auth/login` -> receive `accessToken` + `refreshToken`
2. Client sends `Authorization: Bearer <accessToken>` on protected API calls
3. When access token expires, client calls `POST /api/auth/refresh`
4. Logout is handled via token revocation endpoints

---

## 2. Auth Endpoints (api-gateway)

Base path: `/api/auth`

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/login` | Authenticate user and issue tokens |
| POST | `/api/auth/refresh` | Issue new access token from refresh token |
| POST | `/api/auth/logout` | Revoke current token |
| POST | `/api/auth/logout-all` | Revoke all user sessions |
| POST | `/api/auth/change-password` | Change password for authenticated user |
| GET | `/api/auth/profile` | Return current user profile |

Public endpoints (no JWT required):  
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- health checks

---

## 3. JWT Model (current implementation)

- **Algorithm:** `HS512`
- **Access token lifetime:** `1800s` (30 min)
- **Refresh token lifetime:** `43200s` (12 h)
- **Required env:** `JWT_SECRET` (64+ chars in production)

Access token includes claims such as:
- `sub` (username)
- `userId`
- `role`
- `iss`, `aud`, `iat`, `exp`
- `jti`

Example header:
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

Example payload (shape):
```json
{
  "sub": "admin",
  "userId": "1",
  "role": "ADMIN",
  "iss": "parking-system",
  "aud": "parking-system-api",
  "iat": 1700000000,
  "exp": 1700001800,
  "jti": "uuid-token-id"
}
```

---

## 4. Component Roles

### CorsFilter
- Runs before security checks.
- Handles CORS and answers preflight `OPTIONS` early.

### SecurityFilter
- Validates bearer token on protected endpoints.
- Builds security context from token claims.
- Enforces RBAC for route/method combinations.
- Applies rate limiting and brute-force protections.

### JwtTokenService
- Creates access and refresh tokens.
- Validates token signature/expiry.
- Supports token revocation/blacklist flows.

---

## 5. Usage Examples

### Login

```bash
curl -s -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}'
```

```powershell
$body = '{"username":"admin","password":"parking123"}'
$r = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
     -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
$token = ($r.Content | ConvertFrom-Json).accessToken
```

### Authorized request

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8086/api/clients
```

```powershell
Invoke-WebRequest -Uri "http://localhost:8086/api/clients" `
  -Headers @{Authorization="Bearer $token"} -UseBasicParsing
```

### Refresh

```bash
curl -X POST http://localhost:8086/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

---

## 6. Configuration

```yaml
jwt:
  algorithm: HS512
  access-token-expiration: 1800
  refresh-token-expiration: 43200
  secret: ${JWT_SECRET}
```

Production checklist:
- use strong `JWT_SECRET` (64+ chars)
- never commit secrets
- keep token lifetimes controlled via env
