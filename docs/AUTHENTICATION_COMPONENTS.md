# 🔐 Компоненты авторизации и генерации JWT токенов в Parking System

## 📍 Где происходит генерация токенов?

**Компонент:** API Gateway  
**Местоположение:** `backend/api-gateway`

### Цепочка обработки запроса на авторизацию:

```
1. HTTP POST /api/auth/login
   ↓
2. AuthController.login() 
   ↓ 
3. UserSecurityService.authenticateUser()
   ↓
4. JwtTokenService.createAccessToken()
   ↓
5. JwtTokenService.createRefreshToken()
   ↓
6. Возврат AuthResponse с токенами
```

---

## 📂 Ключевые файлы

### 1. **AuthController.java** 
`backend/api-gateway/src/main/java/com/parking/api_gateway/security/controller/AuthController.java`

**Роль:** REST контроллер для эндпоинтов аутентификации

**Эндпоинты:**
- `POST /api/auth/login` - авторизация пользователя
- `POST /api/auth/refresh` - обновление access token
- `POST /api/auth/logout` - выход из системы

**Ключевой метод:**
```java
@PostMapping("/login")
public Mono<ResponseEntity<AuthResponse>> login(
    @Valid @RequestBody AuthRequest request,
    HttpServletRequest httpRequest) {
    
    return userSecurityService.authenticateUser(request, clientIpAddress, userAgent)
        .map(authResponse -> {
            // Генерация JWT токенов
            String accessToken = jwtTokenService.createAccessToken(...);
            String refreshToken = jwtTokenService.createRefreshToken(...);
            
            authResponse.setAccessToken(accessToken);
            authResponse.setRefreshToken(refreshToken);
            
            return ResponseEntity.ok(authResponse);
        });
}
```

---

### 2. **JwtTokenService.java**
`backend/api-gateway/src/main/java/com/parking/api_gateway/security/service/JwtTokenService.java`

**Роль:** Сервис генерации, валидации и управления JWT токенами

**Основные методы:**

#### `createAccessToken()`
Генерирует Access Token со следующими claims:
- `jti` - уникальный ID токена (UUID)
- `sub` - username
- `userId` - ID пользователя
- `role` - роль пользователя (ADMIN, MANAGER, OPERATOR)
- `email` - email
- `firstName`, `lastName` - имя и фамилия
- `ipAddress` - IP адрес клиента
- `userAgentHash` - хеш User-Agent
- `tokenType` - "ACCESS"

**Время жизни:** 30 минут (1800 секунд)

#### `createRefreshToken()`
Генерирует Refresh Token:
- Минимальные claims (только userId, username)
- **Время жизни:** 12 часов (43200 секунд)

#### `validateToken()`
Проверяет валидность токена:
- Signature
- Expiration
- Blacklist (в Redis)

---

### 3. **UserSecurityService.java**
`backend/api-gateway/src/main/java/com/parking/api_gateway/security/service/UserSecurityService.java`

**Роль:** Бизнес-логика аутентификации

**Основные проверки:**
1. Поиск пользователя в БД по username
2. Проверка enabled (пользователь активен?)
3. Проверка account_non_locked (не заблокирован?)
4. Проверка пароля (BCrypt)
5. Защита от brute force (failed_login_attempts)
6. Обновление статистики входа (last_login_at, login_count)

---

### 4. **UserSecurityEntity.java**
`backend/api-gateway/src/main/java/com/parking/api_gateway/security/entity/UserSecurityEntity.java`

**Роль:** JPA Entity пользователя с security полями

**Ключевые поля для авторизации:**
```java
private String username;
private String password; // BCrypt hash
private Role role; // ADMIN, MANAGER, OPERATOR
private Boolean enabled;
private Boolean accountNonLocked;
private Integer failedLoginAttempts;
private LocalDateTime accountLockedUntil;
```

---

### 5. **UserSecurityRepository.java**
`backend/api-gateway/src/main/java/com/parking/api_gateway/security/repository/UserSecurityRepository.java`

**Роль:** JPA Repository для работы с пользователями

**Методы:**
```java
Optional<UserSecurityEntity> findByUsername(String username);
Optional<UserSecurityEntity> findByEmail(String email);
```

---

## 🔑 Конфигурация JWT

Находится в `application.yml` или переменных окружения:

```yaml
security:
  jwt:
    secret: ${JWT_SECRET:ParkingSystemSecretKey2025!VeryLongAndSecureKey123456789}
    access-token-expiration: 1800  # 30 минут
    refresh-token-expiration: 43200  # 12 часов
    issuer: parking-system
```

**В Docker:**
```yaml
environment:
  JWT_SECRET: "your-secret-key-min-64-characters-long-for-production-use-only-12345"
```

---

## 📝 Формат AuthRequest

```json
{
  "username": "admin",
  "password": "parking123"
}
```

---

## 📤 Формат AuthResponse

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "sessionTimeoutMinutes": 480,
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@parking.com",
    "role": "ADMIN",
    "firstName": "System",
    "lastName": "Administrator"
  }
}
```

---

## 🛠️ Как проверить авторизацию?

### Вариант 1: PowerShell скрипт
```powershell
cd C:\Users\user\Projects\parking-system\devops
.\test-auth.ps1
```

### Вариант 2: curl
```powershell
$body = '{"username":"admin","password":"parking123"}'
curl.exe -X POST http://localhost:8086/api/auth/login `
  -H "Content-Type: application/json" `
  -d $body
```

### Вариант 3: Invoke-RestMethod
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

---

## 🔍 Отладка проблем с авторизацией

### 1. Проверить, запущен ли API Gateway
```powershell
docker ps --filter "name=api-gateway"
docker logs api-gateway --tail 30
```

### 2. Проверить health endpoint
```powershell
curl http://localhost:8086/actuator/health
```

### 3. Проверить пользователей в БД
```powershell
docker exec -it parking_db psql -U postgres -d parking_db `
  -c "SELECT id, username, email, user_role, enabled, account_non_locked FROM users;"
```

### 4. Проверить логи AuthController
```powershell
docker logs api-gateway 2>&1 | Select-String "Login attempt|authentication|AuthController"
```

### 5. Проверить переменную JWT_SECRET
```powershell
docker exec api-gateway printenv | Select-String "JWT"
```

---

## ⚠️ Типичные проблемы

### 401 Unauthorized
**Причины:**
1. Неверный username или password
2. Пользователь не найден в БД
3. Хеш пароля в БД не совпадает с введенным
4. `enabled = false` в таблице users

**Решение:**
```powershell
# Проверить пароль в БД
docker exec -it parking_db psql -U postgres -d parking_db `
  -c "SELECT username, password_hash, enabled FROM users WHERE username='admin';"
```

### 423 Locked
**Причины:**
1. `account_non_locked = false`
2. Превышено количество попыток входа (failed_login_attempts > 5)
3. `account_locked_until` еще не истекло

**Решение:**
```sql
-- Разблокировать аккаунт
UPDATE users SET account_non_locked = true, failed_login_attempts = 0, account_locked_until = NULL 
WHERE username = 'admin';
```

### 500 Internal Server Error
**Причины:**
1. Проблема с подключением к PostgreSQL
2. Ошибка в JwtTokenService (неправильный SECRET)
3. Redis недоступен (если используется)

**Решение:**
```powershell
# Проверить подключение к БД
docker exec api-gateway curl -f http://parking_db:5432 || echo "DB not accessible"

# Проверить логи
docker logs api-gateway --tail 50
```

---

## 📊 Архитектура аутентификации

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /api/auth/login
       │ {username, password}
       ▼
┌──────────────────────────────┐
│      API Gateway              │
│  (Port 8086)                 │
│                              │
│  ┌────────────────────────┐  │
│  │  AuthController        │  │
│  │  /api/auth/login       │  │
│  └───────┬────────────────┘  │
│          │                   │
│          ▼                   │
│  ┌────────────────────────┐  │
│  │ UserSecurityService    │  │
│  │ - Find user in DB      │  │
│  │ - Check password       │  │
│  │ - Update login stats   │  │
│  └───────┬────────────────┘  │
│          │                   │
│          ▼                   │
│  ┌────────────────────────┐  │
│  │  JwtTokenService       │  │
│  │ - createAccessToken()  │  │
│  │ - createRefreshToken() │  │
│  └───────┬────────────────┘  │
│          │                   │
└──────────┼───────────────────┘
           │
           ▼
    ┌─────────────┐
    │  PostgreSQL │
    │  (users)    │
    └─────────────┘
```

---

**Создано:** 2025-12-21  
**Версия API Gateway:** 0.0.1-SNAPSHOT
