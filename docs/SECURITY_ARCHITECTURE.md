# 🔐 Security Architecture Documentation
## Parking System API Gateway Security Implementation

### 📋 Обзор

Система безопасности API Gateway построена на многоуровневой архитектуре с использованием современных стандартов безопасности. Реализованы comprehensive security measures включая JWT authentication, rate limiting, brute force protection и comprehensive auditing.

---

## 🏗️ Архитектура безопасности

### Архитектурные принципы
- **Defense in Depth** - многоуровневая защита
- **Zero Trust** - проверка каждого запроса  
- **Fail Secure** - безопасное поведение при ошибках
- **Least Privilege** - минимальные необходимые права
- **Audit Everything** - комплексное логирование

### 📌 Принятые архитектурные решения (Phase 4, 2026-08-10)
Ниже зафиксированы решения, согласованные для следующей итерации архитектуры (статус: planned, в процессе реализации):

1. **JWT trust model hardening:** переход от общего HS512 `JWT_SECRET` к модели RS256/JWKS с ротацией ключей и явной политикой жизненного цикла ключей.
2. **Service-to-service auth standard:** единый подход для межсервисной авторизации: user-token relay для request-driven flow и service-token для фоновых задач.
3. **Strict token validation:** обязательная валидация `iss/aud/exp/nbf/jti` и унификация негативных auth-сценариев (401/403 + аудируемые причины отказа).
4. **Data isolation in shared PostgreSQL:** переход к schema-per-service и отдельным DB-пользователям с правами least-privilege; cross-service доступ к данным — через API, не через прямой SQL.
5. **Edge/resilience/concurrency hardening:** фиксированные правила для timeout/retry/circuit-breaker и покрытие race-condition сценариев в gate/billing/capacity потоках.

Связанные задачи в backlog: #125, #126, #127, #128, #129, #130, #131, #132, #133.

### Компонентная архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Request                           │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│               SecurityFilter                                │
│  • Rate Limiting                                           │
│  • Brute Force Protection                                  │
│  • IP Blocking                                             │
│  • JWT Validation                                          │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│              AuthController                                 │
│  • Login/Logout                                           │
│  • Token Refresh                                           │
│  • User Management                                         │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│             Security Services                               │
│  ┌─────────────────┬─────────────────┬─────────────────────┐ │
│  │  JwtTokenService│UserSecuritySvc │ SecurityAuditService│ │
│  └─────────────────┴─────────────────┴─────────────────────┘ │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│              Data Layer                                     │
│  ┌─────────────────┬─────────────────────────────────────────┐ │
│  │   PostgreSQL    │              Redis                    │ │
│  │ UserSecurity    │        Sessions/Cache               │ │
│  │   Repository    │         Blacklist                   │ │
│  └─────────────────┴─────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Компоненты системы безопасности

### 1. SecurityFilter - Точка входа безопасности

**Местоположение:** `com.parking.api_gateway.security.filter.SecurityFilter`

**Ключевые функции:**
- Rate limiting (60 req/min, 1000 req/hour)
- Brute force detection (10 failed attempts → block IP)
- JWT token validation
- Request/response logging
- IP blocking management

**Алгоритм обработки:**
```java
1. Extract client IP from headers (X-Forwarded-For, X-Real-IP)
2. Check rate limits (per IP, per minute/hour)
3. Check if IP is blocked (suspicious activity)
4. Validate public paths (auth endpoints, health checks)
5. For protected endpoints: validate JWT token
6. Add user context to request attributes
7. Record metrics and audit logs
```

**Rate Limiting Implementation:**
```java
private final Map<String, RateLimitInfo> rateLimitCache = new ConcurrentHashMap<>();

// RateLimitInfo contains:
- List<LocalDateTime> requestTimestamps // Sliding window
- AtomicInteger failedAttempts         // Brute force counter
```

### 2. JwtTokenService - JWT Management

**Местоположение:** `com.parking.api_gateway.security.service.JwtTokenService`

**Архитектура JWT:**
- **Algorithm:** HMAC-SHA512 
- **Key Length:** 256-bit minimum
- **Access Token:** 30 minutes (configurable)
- **Refresh Token:** 12 hours (configurable)
- **Blacklist Support:** Redis-based token revocation

**Token Structure:**
```json
{
  "header": {
    "alg": "HS512",
    "typ": "JWT"
  },
  "payload": {
    "sub": "username",
    "userId": "12345",
    "role": "ADMIN",
    "iss": "parking-system",
    "iat": 1703123456,
    "exp": 1703127056,
    "jti": "unique-token-id",
    "ipAddress": "192.168.1.1",
    "userAgentHash": "sha256-hash"
  }
}
```

**Security Features:**
- **Token Blacklisting:** Redis-based revocation list
- **IP Binding:** Tokens tied to originating IP
- **User Agent Validation:** Basic session hijacking protection
- **Automatic Cleanup:** Expired tokens removed via scheduled task

### 3. UserSecurityEntity - Comprehensive User Model

**Местоположение:** `com.parking.api_gateway.security.entity.UserSecurityEntity`

**Database Schema (50+ security fields):**
```sql
CREATE TABLE user_security_entities (
    -- Core Identity
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    
    -- Account Status
    account_enabled BOOLEAN DEFAULT true,
    account_locked BOOLEAN DEFAULT false,
    account_expired BOOLEAN DEFAULT false,
    credentials_expired BOOLEAN DEFAULT false,
    
    -- Authentication Tracking
    failed_login_attempts INTEGER DEFAULT 0,
    last_failed_login TIMESTAMP,
    last_successful_login TIMESTAMP,
    last_login_ip VARCHAR(45),
    
    -- Password Management
    password_last_changed TIMESTAMP,
    password_reset_token VARCHAR(100),
    password_reset_expires TIMESTAMP,
    force_password_change BOOLEAN DEFAULT false,
    
    -- Multi-Factor Authentication
    two_factor_enabled BOOLEAN DEFAULT false,
    two_factor_secret VARCHAR(32),
    backup_codes TEXT[],
    
    -- Session Management
    concurrent_sessions_allowed INTEGER DEFAULT 3,
    current_session_count INTEGER DEFAULT 0,
    
    -- Security Policies
    password_policy_id BIGINT,
    role_assignments TEXT[],
    permissions TEXT[],
    
    -- Audit Trail
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(50),
    
    -- Compliance
    gdpr_consent_date TIMESTAMP,
    data_retention_date TIMESTAMP,
    
    -- Advanced Security
    security_questions JSONB,
    trusted_devices JSONB,
    login_history JSONB,
    security_alerts TEXT[]
);
```

### 4. SecurityAuditService - Comprehensive Auditing

**Местоположение:** `com.parking.api_gateway.security.service.SecurityAuditService`

**Audit Categories:**
- **Authentication Events:** Login/logout attempts, successes, failures
- **Authorization Events:** Permission checks, role changes
- **Security Violations:** Rate limiting, brute force, suspicious activity
- **Data Access:** Sensitive data read/write operations
- **Administrative Actions:** User management, configuration changes

**Audit Log Format:**
```json
{
  "timestamp": "2025-12-20T19:30:15.123Z",
  "event_type": "SECURITY_VIOLATION",
  "severity": "HIGH",
  "user_id": "12345",
  "username": "testuser",
  "client_ip": "192.168.1.100",
  "user_agent": "Mozilla/5.0...",
  "action": "RATE_LIMIT_EXCEEDED",
  "details": {
    "endpoint": "/api/auth/login",
    "attempts_in_window": 65,
    "limit": 60,
    "window_minutes": 1
  },
  "outcome": "BLOCKED",
  "session_id": "abc123def456"
}
```

---

## ⚙️ Configuration Management

### Production Security Configuration

**Environment Variables (обязательные для prod):**
```bash
# JWT Security
JWT_SECRET=<64-character-cryptographically-strong-secret>
JWT_ACCESS_TOKEN_EXPIRATION=1800     # 30 minutes
JWT_REFRESH_TOKEN_EXPIRATION=43200   # 12 hours

# Database Security
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/parking_production
SPRING_DATASOURCE_USERNAME=parking_prod_user
SPRING_DATASOURCE_PASSWORD=<strong-db-password>
HIBERNATE_DDL_AUTO=validate          # NEVER create-drop in production

# Redis Security
SPRING_REDIS_PASSWORD=<strong-redis-password>
SPRING_REDIS_HOST=prod-redis-cluster
SPRING_REDIS_PORT=6380

# Security Policies
RATE_LIMITING_ENABLED=true
RATE_LIMIT_MINUTE=30                # More restrictive for prod
RATE_LIMIT_HOUR=500                 # More restrictive for prod
BRUTE_FORCE_THRESHOLD=5             # More restrictive for prod
BRUTE_FORCE_LOCKOUT_MINUTES=30

# Logging
SECURITY_LOGGING_LEVEL=WARN         # Don't log sensitive data
SECURITY_AUDIT_ENABLED=true
```

### Spring Security Integration

**SecurityConfiguration:** `com.parking.api_gateway.security.config.SecurityConfiguration`

**Production Security Headers:**
```java
.headers(headers -> headers
    .frameOptions().deny()                    // Prevent clickjacking
    .contentTypeOptions().and()               // Prevent MIME sniffing
    .httpStrictTransportSecurity(hstsConfig -> hstsConfig
        .maxAgeInSeconds(31536000)           // 1 year HSTS
        .includeSubdomains(true)
        .preload(true)
    )
    .referrerPolicy(STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
)
```

---

## 🔐 Security Features Implementation

### 1. Rate Limiting Algorithm

**Sliding Window Implementation:**
```java
public boolean checkRateLimit(String clientIp, String path) {
    LocalDateTime now = LocalDateTime.now();
    RateLimitInfo info = rateLimitCache.computeIfAbsent(clientIp, k -> new RateLimitInfo());
    
    // Clean old entries (sliding window)
    info.requestTimestamps.removeIf(timestamp -> 
        ChronoUnit.MINUTES.between(timestamp, now) > 60);
    
    // Check minute limit
    long recentRequests = info.requestTimestamps.stream()
        .filter(timestamp -> ChronoUnit.MINUTES.between(timestamp, now) < 1)
        .count();
    
    if (recentRequests >= MAX_REQUESTS_PER_MINUTE) {
        recordViolation(clientIp, "RATE_LIMIT_MINUTE");
        return false;
    }
    
    info.requestTimestamps.add(now);
    return true;
}
```

### 2. Brute Force Protection

**Progressive Blocking Strategy:**
```java
private void incrementFailedAttempts(String clientIp) {
    RateLimitInfo info = rateLimitCache.computeIfAbsent(clientIp, k -> new RateLimitInfo());
    int failures = info.failedAttempts.incrementAndGet();
    
    if (failures >= BRUTE_FORCE_THRESHOLD) {
        // Block IP for 1 hour
        suspiciousIps.put(clientIp, LocalDateTime.now());
        auditService.logSuspiciousActivity("unknown", clientIp, 
            "Brute force detected", "Multiple failed authentication attempts");
    }
}
```

### 3. JWT Token Validation

**Multi-layer Validation:**
```java
public Claims validateAccessToken(String token, String clientIpAddress) {
    // 1. Check blacklist
    if (isTokenBlacklisted(token)) {
        throw new SecurityException("Token is blacklisted");
    }
    
    // 2. Parse and validate JWT
    Claims claims = Jwts.parser()
        .setSigningKey(signingKey)
        .requireIssuer(issuer)
        .build()
        .parseClaimsJws(token)
        .getBody();
    
    // 3. Validate IP binding (if enabled)
    String tokenIp = claims.get("ipAddress", String.class);
    if (tokenIp != null && !tokenIp.equals(clientIpAddress)) {
        throw new SecurityException("Token IP mismatch");
    }
    
    // 4. Additional custom validations
    validateUserStatus(claims.get("userId", Long.class));
    
    return claims;
}
```

---

## 📊 Security Metrics & Monitoring

### Key Security Metrics
- **Authentication Rate:** Successful vs failed login attempts
- **Token Usage:** Active tokens, refresh patterns, blacklist size  
- **Rate Limiting:** Violations per IP, endpoint abuse patterns
- **Brute Force:** Attack patterns, blocked IPs, geographic distribution
- **Session Management:** Active sessions, concurrent users, session duration

### Security Alerting Triggers
- Failed login rate > 20/minute
- New IP brute force detection
- Mass token blacklisting events
- Unusual authentication patterns
- High rate limiting violations

---

## 🚨 Security Incident Response

### Automated Responses
1. **IP Blocking:** Automatic blocking after threshold violations
2. **Token Revocation:** Suspicious activity triggers mass revocation
3. **Account Locking:** Multiple failure patterns lock accounts
4. **Session Termination:** Security violations end active sessions

### Manual Investigation Tools
- Detailed audit logs with correlation IDs
- User activity timelines
- Geographic access patterns
- Device fingerprinting data
- Session forensics

---

## 🔒 Production Security Checklist

### Pre-Deployment Requirements
- [ ] Strong JWT secret (64+ characters) set via environment variable
- [ ] Database credentials secured and rotated
- [ ] Redis authentication enabled
- [ ] Rate limiting enabled with production values
- [ ] HTTPS/TLS configured with valid certificates
- [ ] Security headers configured
- [ ] Audit logging enabled
- [ ] Monitoring and alerting set up

### Runtime Security Validation
- [ ] JWT tokens expire correctly
- [ ] Rate limiting blocks excessive requests
- [ ] IP blocking works for suspicious activity
- [ ] Audit logs capture all security events
- [ ] Health checks don't leak sensitive information
- [ ] Error messages don't expose internal details

---

## 📚 Security Best Practices Implemented

### Authentication Security
- ✅ Strong password policies enforced
- ✅ JWT with short expiration times
- ✅ Secure token storage and transmission
- ✅ IP binding for session security
- ✅ Comprehensive audit logging

### Authorization Security  
- ✅ Role-based access control (RBAC)
- ✅ Least privilege principles
- ✅ Permission-based endpoint protection
- ✅ Session-based authorization

### Infrastructure Security
- ✅ Database connection security
- ✅ Redis authentication
- ✅ Network isolation capabilities
- ✅ Secure configuration management
- ✅ Environment-specific settings

### Monitoring & Response
- ✅ Real-time security metrics
- ✅ Automated threat response
- ✅ Comprehensive audit trails
- ✅ Security incident tracking

---

## 🚀 Future Security Enhancements

### Planned Improvements
- **Multi-Factor Authentication (MFA)** - TOTP/SMS integration
- **Device Fingerprinting** - Enhanced session security
- **Geographic Restrictions** - Location-based access control
- **Advanced Threat Detection** - ML-based anomaly detection
- **SAML/OAuth2 Integration** - Enterprise SSO support

### Scalability Considerations
- **Distributed Rate Limiting** - Redis-based coordination
- **JWT Signing Key Rotation** - Automated key management
- **Audit Log Archival** - Long-term compliance storage
- **Cross-Service Security** - Microservice security mesh

---

**Система безопасности готова к production использованию с enterprise-grade security features! 🔐✨**