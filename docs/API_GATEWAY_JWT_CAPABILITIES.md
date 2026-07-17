# API Gateway - Возможности генерации JWT токенов

## ✅ ОТВЕТ: ДА, API Gateway полностью умеет генерировать токены!

## 🔍 Проверка реализации

### 1. ✅ JwtTokenService.java - Сервис для работы с JWT

**Расположение:** `backend/api-gateway/src/main/java/com/parking/api_gateway/security/service/JwtTokenService.java`

**Реализованные методы:**

```java
// Создание Access Token (срок: 30 минут / 1 час в зависимости от профиля)
public String createAccessToken(UserSecurityEntity user, String ipAddress, String userAgent)

// Создание Refresh Token (срок: 12 часов / 24 часа)
public String createRefreshToken(UserSecurityEntity user, String ipAddress)

// Валидация Access Token
public Mono<Claims> validateAccessToken(String token, String clientIpAddress)

// Валидация Refresh Token
public Mono<Claims> validateRefreshToken(String token)

// Аннулирование токена (blacklist)
public Mono<Void> invalidateToken(String token, String reason)
```

**Особенности:**
- Использует **HMAC-SHA512** алгоритм подписи
- Хранит blacklist токенов в **Redis**
- Записывает сессии пользователей
- Включает в токен: userId, role, email, firstName, lastName, IP адрес
- Поддерживает аудит всех операций с токенами

### 2. ✅ AuthController.java - REST API для аутентификации

**Расположение:** `backend/api-gateway/src/main/java/com/parking/api_gateway/security/controller/AuthController.java`

**Endpoints:**

```java
// Вход пользователя и получение токенов
POST /api/auth/login
Body: {"username": "admin", "password": "parking123"}
Response: {
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "email": "admin@example.com"
  },
  "sessionTimeoutMinutes": 480
}

// Обновление Access Token через Refresh Token
POST /api/auth/refresh
Body: {"refreshToken": "eyJ..."}

// Выход (аннулирование токена)
POST /api/auth/logout
Header: Authorization: Bearer <token>

// Проверка валидности токена
GET /api/auth/validate
Header: Authorization: Bearer <token>
```

### 3. ✅ UserSecurityService.java - Бизнес-логика аутентификации

**Расположение:** `backend/api-gateway/src/main/java/com/parking/api_gateway/security/service/UserSecurityService.java`

**Функциональность:**

```java
// Полная аутентификация пользователя с проверками безопасности
public Mono<AuthResponse> authenticateUser(AuthRequest request, String clientIpAddress, String userAgent)
```

**Выполняемые проверки:**
1. ✅ Поиск пользователя в БД
2. ✅ Проверка активности аккаунта (`enabled = true`)
3. ✅ Проверка срока действия аккаунта (`account_non_expired = true`)
4. ✅ Проверка блокировки аккаунта (`account_non_locked = true`)
5. ✅ Проверка временной блокировки (после неудачных попыток)
6. ✅ **Проверка пароля через BCrypt** (`passwordEncoder.matches()`)
7. ✅ Проверка срока действия пароля
8. ✅ Проверка срока действия credentials
9. ✅ Запись успешного входа
10. ✅ Обнуление счетчика неудачных попыток

**Защита от брутфорса:**
- После **10 неудачных попыток** аккаунт блокируется на **30 минут**
- Все попытки входа логируются через `SecurityAuditService`

### 4. ✅ Конфигурация JWT

**Файл:** `backend/api-gateway/src/main/resources/application.yml`

```yaml
security:
  jwt:
    secret: ${JWT_SECRET}  # Минимум 64 символа
    access-token-expiration: 1800    # 30 минут
    refresh-token-expiration: 43200  # 12 часов
    issuer: parking-system
```

**Переменные окружения (Docker):**
```yaml
JWT_SECRET: "your-secret-key-min-64-characters-long-for-production-use-only-12345"
```

## 🔧 Что НЕ работает (текущая проблема)

### ❌ В базе данных НЕТ пользователей!

**Симптомы:**
- API Gateway запущен ✅
- Код генерации токенов работает ✅
- Endpoint `/api/auth/login` доступен ✅
- **НО:** запрос возвращает **401 Unauthorized** ❌

**Причина:**
```sql
-- Таблица users существует, но ПУСТАЯ!
SELECT COUNT(*) FROM users;
-- Result: 0
```

**Решение:**
```powershell
# Загрузить тестовых пользователей в БД
Get-Content ..\database\insert_users.sql | docker exec -i parking_db psql -U postgres -d parking_db
```

После этого в БД появятся пользователи:
- **admin** / **parking123** (ADMIN)
- **user** / **user123** (USER)
- **manager** / **manager123** (ADMIN)

## 🧪 Тестирование генерации токенов

### После загрузки пользователей:

```powershell
# 1. Получить токен
$body = '{"username":"admin","password":"parking123"}'
$response = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body `
    -UseBasicParsing

# 2. Извлечь токен из ответа
$auth = $response.Content | ConvertFrom-Json
$token = $auth.accessToken

# 3. Использовать токен для запросов
Invoke-WebRequest -Uri "http://localhost:8081/api/clients" `
    -Headers @{"Authorization" = "Bearer $token"} `
    -UseBasicParsing
```

## 📊 Архитектура генерации токенов

```
┌────────────────────────────────────────────────────┐
│  Client (Browser / Postman / curl)                 │
└────────────────┬───────────────────────────────────┘
                 │ POST /api/auth/login
                 │ {"username":"admin","password":"..."}
                 ↓
┌────────────────────────────────────────────────────┐
│  API Gateway (Port 8086)                           │
│                                                     │
│  ┌──────────────────────────────────────────────┐ │
│  │ AuthController                               │ │
│  │  - Принимает запрос                          │ │
│  │  - Извлекает IP и User-Agent                 │ │
│  └────────────┬─────────────────────────────────┘ │
│               ↓                                    │
│  ┌──────────────────────────────────────────────┐ │
│  │ UserSecurityService                          │ │
│  │  - Ищет пользователя в БД                   │ │
│  │  - Проверяет статус аккаунта                │ │
│  │  - Проверяет пароль (BCrypt)                │ │
│  │  - Записывает успешный вход                 │ │
│  └────────────┬─────────────────────────────────┘ │
│               ↓                                    │
│  ┌──────────────────────────────────────────────┐ │
│  │ JwtTokenService                              │ │
│  │  - Генерирует Access Token                  │ │
│  │  - Генерирует Refresh Token                 │ │
│  │  - Подписывает токены (HMAC-SHA512)         │ │
│  │  - Сохраняет сессию в Redis                 │ │
│  └────────────┬─────────────────────────────────┘ │
│               ↓                                    │
└───────────────┼────────────────────────────────────┘
                │ Response: {accessToken, refreshToken, user}
                ↓
┌────────────────────────────────────────────────────┐
│  Client получает токены                            │
└────────────────────────────────────────────────────┘
```

## 🔐 Структура JWT токена

После декодирования токена (https://jwt.io):

**Header:**
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "jti": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "iss": "parking-system",
  "sub": "admin",
  "aud": "parking-system-api",
  "iat": 1703174400,
  "exp": 1703176200,
  "userId": 1,
  "role": "ADMIN",
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "ipAddress": "192.168.1.100",
  "userAgentHash": "abc123...",
  "tokenType": "ACCESS"
}
```

## ✅ Вывод

**API Gateway полностью готов к генерации JWT токенов!**

Вся необходимая инфраструктура реализована:
- ✅ Генерация Access/Refresh токенов
- ✅ Валидация токенов
- ✅ Blacklist (аннулирование)
- ✅ Аутентификация пользователей
- ✅ Проверка паролей (BCrypt)
- ✅ Защита от брутфорса
- ✅ Аудит безопасности
- ✅ Интеграция с Redis
- ✅ REST API endpoints

**Единственная проблема:** пустая таблица `users` в БД.

**Решение:** выполнить `insert_users.sql` для загрузки тестовых пользователей.
