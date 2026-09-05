# 🔒 Brute Force Protection: Описание и решение проблем

## 🚨 Проблема

**Симптом:**
```
AUDIT: Suspicious activity - User: unknown, IP: 172.18.0.1, 
Activity: Brute force detected, Details: Multiple failed authentication attempts
```

**Что происходит:**
API Gateway имеет встроенную защиту от brute force атак. После определенного количества неудачных попыток входа (по умолчанию 10), аккаунт временно блокируется.

---

## 🔍 Как работает защита

### Поля в таблице `users`:

```sql
failed_login_attempts INTEGER DEFAULT 0
account_non_locked BOOLEAN DEFAULT TRUE
account_locked_until TIMESTAMP
```

### Логика защиты:

1. При **успешном входе**: `failed_login_attempts = 0`
2. При **неудачном входе**: `failed_login_attempts += 1`
3. При **failed_login_attempts >= 10**: 
   - `account_non_locked = FALSE`
   - `account_locked_until = NOW() + 30 минут`
   - Возвращается **HTTP 423 Locked**

### Код в UserSecurityService:

```java
// После проверки пароля
if (!passwordMatches) {
    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
    
    if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
        user.setAccountNonLocked(false);
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
    }
    
    userRepository.save(user);
    throw new InvalidCredentialsException("Invalid credentials");
}
```

---

## ✅ Решение проблемы

### Вариант 1: Автоматический скрипт (Рекомендуется)

```powershell
cd C:\Users\user\Projects\parking-system\devops
.\reset-brute-force.ps1
```

Скрипт:
- ✓ Проверяет подключение к БД
- ✓ Показывает текущее состояние пользователей
- ✓ Сбрасывает счетчики для всех пользователей
- ✓ Разблокирует все аккаунты

### Вариант 2: Ручной SQL

```sql
-- Разблокировать всех пользователей
UPDATE users 
SET 
    failed_login_attempts = 0,
    account_non_locked = true,
    account_locked_until = NULL;

-- Проверка
SELECT username, failed_login_attempts, account_non_locked 
FROM users;
```

Выполнить через Docker:
```powershell
docker exec -it parking_db psql -U postgres -d parking_db -c "
UPDATE users SET failed_login_attempts = 0, account_non_locked = true, account_locked_until = NULL;
"
```

### Вариант 3: Разблокировать конкретного пользователя

```powershell
docker exec -it parking_db psql -U postgres -d parking_db -c "
UPDATE users 
SET failed_login_attempts = 0, account_non_locked = true, account_locked_until = NULL 
WHERE username = 'admin';
"
```

---

## 🧪 Проверка после разблокировки

### Полная проверка системы:

```powershell
cd C:\Users\user\Projects\parking-system\devops
.\check-system.ps1
```

Этот скрипт проверяет:
1. Статус Docker контейнеров
2. Доступность PostgreSQL
3. Наличие пользователей в БД
4. Сброс brute force защиты
5. Health API Gateway и Client Service
6. Тест авторизации admin/parking123

### Ручной тест авторизации:

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

**Ожидаемый результат:**
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

## 🛡️ Настройка защиты (для продакшена)

### Параметры в application.yml:

```yaml
security:
  brute-force:
    threshold: ${BRUTE_FORCE_THRESHOLD:10}                  # Количество попыток до блокировки
    lockout-minutes: ${BRUTE_FORCE_LOCKOUT_MINUTES:30}      # Время блокировки в минутах
    reset-after-success: true     # Сбросить счетчик после успешного входа
```

### Рекомендации для продакшена:

1. **Rate Limiting** на уровне API Gateway:
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

2. **CAPTCHA** после 3 неудачных попыток

3. **Email уведомления** о подозрительной активности

4. **IP Blacklist** для повторяющихся атак

5. **Мониторинг** через Prometheus/Grafana:
   ```promql
   rate(authentication_failures_total[5m]) > 10
   ```

---

## 📊 Мониторинг brute force атак

### Проверка логов API Gateway:

```powershell
docker logs api-gateway 2>&1 | Select-String "AUDIT|Brute force|Suspicious"
```

### SQL для анализа:

```sql
-- Топ пользователей с неудачными попытками
SELECT username, failed_login_attempts, account_non_locked, last_login_at
FROM users
WHERE failed_login_attempts > 0
ORDER BY failed_login_attempts DESC;

-- Заблокированные аккаунты
SELECT username, account_locked_until, failed_login_attempts
FROM users
WHERE account_non_locked = false;
```

### Метрики в Prometheus:

- `authentication_attempts_total` - общее количество попыток
- `authentication_failures_total` - неудачные попытки
- `authentication_lockouts_total` - количество блокировок
- `authentication_success_total` - успешные входы

---

## 🔄 Автоматическая разблокировка

Можно настроить cron job для автоматической разблокировки:

```bash
# Каждые 15 минут разблокировать истекшие блокировки
*/15 * * * * docker exec parking_db psql -U postgres -d parking_db -c "
UPDATE users 
SET account_non_locked = true, failed_login_attempts = 0
WHERE account_locked_until < NOW();
"
```

Или через Spring Scheduler в коде:

```java
@Scheduled(fixedRate = 900000) // 15 минут
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

## 📝 Скрипты для управления

### 1. Сброс brute force
```powershell
.\reset-brute-force.ps1
```

### 2. Проверка системы
```powershell
.\check-system.ps1
```

### 3. Тест авторизации
```powershell
.\test-auth.ps1
```

---

## 🚫 Частые ошибки

### 1. "Multiple failed authentication attempts"
**Причина:** Неверный пароль или username  
**Решение:** Проверить учетные данные в БД

### 2. "HTTP 423 Locked"
**Причина:** Аккаунт заблокирован после 5 неудачных попыток  
**Решение:** `.\reset-brute-force.ps1`

### 3. "User not found"
**Причина:** Пользователь не существует в БД  
**Решение:** Проверить `docker exec parking_db psql ... -c "SELECT * FROM users;"`

### 4. "Invalid credentials" (после сброса)
**Причина:** Хеш пароля в БД не совпадает  
**Решение:** Пересоздать пользователя с правильным BCrypt хешем

---

## 🎯 Резюме

**Защита от brute force** - это важная функция безопасности, но она может вызвать проблемы во время разработки и тестирования.

**Для разработки:**
- Используйте `reset-brute-force.ps1` для быстрого сброса
- Увеличьте `max-attempts` в конфигурации
- Уменьшите `lockout-duration`

**Для продакшена:**
- Оставьте защиту включенной
- Настройте мониторинг
- Добавьте email уведомления
- Рассмотрите CAPTCHA и rate limiting

---

**Создано:** 2025-12-21  
**Статус:** ✅ Решено
