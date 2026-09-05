# 🔒 Brute Force Protection: Description and Troubleshooting

## 🚨 Problem

**Symptom:**
```
AUDIT: Suspicious activity - User: unknown, IP: 172.18.0.1,
Activity: Brute force detected, Details: Multiple failed authentication attempts
```

**What is happening:**
API Gateway has built-in brute force protection. After a configurable number of failed login attempts (default: 10), the account is temporarily locked.

---

## 🔍 How Protection Works

### Fields in the `users` table:

```sql
failed_login_attempts INTEGER DEFAULT 0
account_non_locked BOOLEAN DEFAULT TRUE
account_locked_until TIMESTAMP
```

### Protection logic:

1. On **successful login**: `failed_login_attempts = 0`
2. On **failed login**: `failed_login_attempts += 1`
3. When **failed_login_attempts >= 10**:
   - `account_non_locked = FALSE`
   - `account_locked_until = NOW() + 30 minutes`
   - Returns **HTTP 423 Locked**

### Code in UserSecurityService:

> ⚙️ Thresholds `bruteForceThreshold` and `lockoutDurationMinutes` are injected via `@Value` and can be overridden with env vars `BRUTE_FORCE_THRESHOLD` and `BRUTE_FORCE_LOCKOUT_MINUTES` (see `application.yml`).

```java
// After password check
if (!passwordMatches) {
    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

    if (user.getFailedLoginAttempts() >= bruteForceThreshold) {
        user.setAccountNonLocked(false);
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
    }

    userRepository.save(user);
    throw new InvalidCredentialsException("Invalid credentials");
}
```

---

## ✅ Troubleshooting

### Option 1: Automated Script (Recommended)

```powershell
cd C:\Users\user\Projects\parking-system\devops
.\reset-brute-force.ps1
```

The script:
- ✓ Checks DB connection
- ✓ Shows current user state
- ✓ Resets counters for all users
- ✓ Unlocks all accounts

### Option 2: Manual SQL

```sql
-- Unlock all users
UPDATE users
SET
    failed_login_attempts = 0,
    account_non_locked = true,
    account_locked_until = NULL;

-- Verification
SELECT username, failed_login_attempts, account_non_locked
FROM users;
```

Run via Docker:
```powershell
docker exec -it parking_db psql -U postgres -d parking_db -c "
UPDATE users SET failed_login_attempts = 0, account_non_locked = true, account_locked_until = NULL;
"
```

### Option 3: Unlock a Specific User

```powershell
docker exec -it parking_db psql -U postgres -d parking_db -c "
UPDATE users
SET failed_login_attempts = 0, account_non_locked = true, account_locked_until = NULL
WHERE username = 'admin';
"
```

---

## 🧪 Post-Unlock Verification

### Full System Check:

```powershell
cd C:\Users\user\Projects\parking-system\devops
.\check-system.ps1
```

This script checks:
1. Docker container status
2. PostgreSQL availability
3. Users present in DB
4. Brute force protection reset
5. API Gateway and Client Service health
6. Authentication test with admin/parking123

### Manual Auth Test:

```powershell
$body = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

Write-Host "Access Token: $($response.accessToken)"
```

**Expected result:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "username": "admin",
    "role": "ADMIN",
    "email": "admin@parking.com"
  }
}
```

---

## 🛡️ Production Configuration

### application.yml parameters:

```yaml
security:
  brute-force:
    threshold: ${BRUTE_FORCE_THRESHOLD:10}                  # Number of attempts before lockout
    lockout-minutes: ${BRUTE_FORCE_LOCKOUT_MINUTES:30}   # Lockout duration in minutes
    reset-after-success: true     # Reset counter on successful login
```

### Production Recommendations:

1. **Rate Limiting** at API Gateway level:
   ```yaml
   spring:
     cloud:
       gateway:
         routes:
           - id: auth-route
             uri: lb://auth-service
             filters:
               - name: RequestRateLimiter
                 args:
                   redis-rate-limiter.replenishRate: 10
                   redis-rate-limiter.burstCapacity: 20
   ```

2. **CAPTCHA** after 3 failed attempts

3. **Email notifications** for suspicious activity

4. **IP Blacklist** for repeated attacks

5. **Monitoring** via Prometheus/Grafana:
   ```promql
   rate(authentication_failures_total[5m]) > 10
   ```

---

## 📊 Monitoring Brute Force Attacks

### Check API Gateway logs:

```powershell
docker logs api-gateway 2>&1 | Select-String "AUDIT|Brute force|Suspicious"
```

### SQL for Analysis:

```sql
-- Top users by failed attempts
SELECT username, failed_login_attempts, account_non_locked, last_login_at
FROM users
WHERE failed_login_attempts > 0
ORDER BY failed_login_attempts DESC;

-- Locked accounts
SELECT username, account_locked_until, failed_login_attempts
FROM users
WHERE account_non_locked = false;
```

### Prometheus Metrics:

- `authentication_attempts_total` - total number of attempts
- `authentication_failures_total` - failed attempts
- `authentication_lockouts_total` - number of lockouts
- `authentication_success_total` - successful logins

---

## 🔄 Automatic Unlock

A cron job can be configured for automatic unlock:

```bash
# Every 15 minutes, unlock expired lockouts
*/15 * * * * docker exec parking_db psql -U postgres -d parking_db -c "
UPDATE users
SET account_non_locked = true, failed_login_attempts = 0
WHERE account_locked_until < NOW();
"
```

Or via Spring Scheduler in code:

```java
@Scheduled(fixedRate = 900000) // 15 minutes
public void unlockExpiredAccounts() {
    LocalDateTime now = LocalDateTime.now();
    List<UserSecurityEntity> lockedUsers = userRepository
        .findByAccountNonLockedAndAccountLockedUntilBefore(false, now);

    lockedUsers.forEach(user -> {
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
    });

    userRepository.saveAll(lockedUsers);
    log.info("Unlocked {} expired accounts", lockedUsers.size());
}
```

---

## 📝 Management Scripts

### 1. Reset brute force
```powershell
.\reset-brute-force.ps1
```

### 2. System check
```powershell
.\check-system.ps1
```

### 3. Auth test
```powershell
.\test-auth.ps1
```

---

## 🚫 Common Errors

### 1. "Multiple failed authentication attempts"
**Cause:** Wrong password or username
**Solution:** Verify credentials in DB

### 2. "HTTP 423 Locked"
**Cause:** Account locked after failed attempts
**Solution:** `.\reset-brute-force.ps1`

### 3. "User not found"
**Cause:** User does not exist in DB
**Solution:** Check `docker exec parking_db psql ... -c "SELECT * FROM users;"`

### 4. "Invalid credentials" (after reset)
**Cause:** Password hash in DB doesn't match
**Solution:** Recreate user with correct BCrypt hash

---

## 🎯 Summary

**Brute force protection** is an important security feature but can cause issues during development and testing.

**For development:**
- Use `reset-brute-force.ps1` for quick reset
- Increase `max-attempts` in config
- Reduce `lockout-duration`

**For production:**
- Keep protection enabled
- Configure monitoring
- Add email notifications
- Consider CAPTCHA and rate limiting

---

**Created:** 2025-12-21
**Status:** ✅ Resolved
