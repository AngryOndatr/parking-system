# Аутентификация в Parking System

## Обзор

Система использует **JWT (JSON Web Token)** аутентификацию через API Gateway. Client-service и другие микросервисы не имеют собственной аутентификации - они проверяют JWT токены, выданные API Gateway.

## Учетные данные для тестирования

В базе данных созданы следующие тестовые пользователи:

### 1. Администратор
```
Username: admin
Password: parking123
Role: ADMIN
```

### 2. Обычный пользователь
```
Username: user
Password: user123
Role: USER
```

### 3. Менеджер (администратор)
```
Username: manager
Password: manager123
Role: ADMIN
```

## Как получить JWT токен

### Шаг 1: Отправить запрос на login

**Endpoint:** `POST http://localhost:8086/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "username": "admin",
  "password": "parking123"
}
```

**Пример с curl (для Linux/Mac или Git Bash в Windows):**
```bash
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}'
```

**⚠️ ВАЖНО для PowerShell:**
В PowerShell `curl` - это алиас для `Invoke-WebRequest`, который имеет другой синтаксис!

**Вариант 1: Используйте Invoke-WebRequest (рекомендуется):**
```powershell
$body = '{"username":"admin","password":"parking123"}'
Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body `
    -UseBasicParsing
```

**Вариант 2: Используйте настоящий curl.exe:**
```powershell
curl.exe -X POST http://localhost:8086/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"admin\",\"password\":\"parking123\"}'
```

**Вариант 3: Более читаемый PowerShell:**
```powershell
$body = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body `
    -UseBasicParsing

$auth = $response.Content | ConvertFrom-Json
$token = $auth.accessToken
Write-Host "Access Token: $token"
```

### Шаг 2: Ответ содержит токены

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN"
  },
  "sessionTimeoutMinutes": 480
}
```

- **accessToken** - используется для доступа к API (срок: 30 минут)
- **refreshToken** - используется для обновления access token (срок: 12 часов)

## Как использовать JWT токен

### Для прямого доступа к Client Service

**Endpoint:** `GET http://localhost:8081/api/clients`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Пример с curl (Linux/Mac/Git Bash):**
```bash
curl http://localhost:8081/api/clients \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Пример с PowerShell:**
```powershell
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
Invoke-WebRequest -Uri "http://localhost:8081/api/clients" `
    -Headers @{"Authorization" = "Bearer $token"} `
    -UseBasicParsing
```

**Или с curl.exe в PowerShell:**
```powershell
curl.exe http://localhost:8081/api/clients `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Через API Gateway (рекомендуется)

**Endpoint:** `GET http://localhost:8086/client-service/api/clients`

API Gateway автоматически проксирует запросы к Client Service через Eureka Service Discovery.

**Пример (Linux/Mac/Git Bash):**
```bash
curl http://localhost:8086/client-service/api/clients \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**PowerShell:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8086/client-service/api/clients" `
    -Headers @{"Authorization" = "Bearer $token"} `
    -UseBasicParsing
```

## Обновление токена

Когда access token истекает (через 30 минут), используйте refresh token для получения нового:

**Endpoint:** `POST http://localhost:8086/api/auth/refresh`

**Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Ответ:**
```json
{
  "accessToken": "NEW_ACCESS_TOKEN",
  "refreshToken": "NEW_REFRESH_TOKEN"
}
```

## Public Endpoints (без аутентификации)

Следующие endpoints доступны без токена:

- `GET http://localhost:8081/actuator/health` - health check
- `GET http://localhost:8086/actuator/health` - health check API Gateway
- `POST http://localhost:8086/api/auth/login` - login endpoint

## Swagger UI

Swagger UI доступен по адресу:
- **Client Service:** http://localhost:8081/swagger-ui.html
- **API Gateway:** http://localhost:8086/swagger-ui.html

В Swagger UI можно авторизоваться:
1. Получить access token через `/api/auth/login`
2. Нажать кнопку "Authorize" в Swagger UI
3. Ввести: `Bearer YOUR_ACCESS_TOKEN`
4. Нажать "Authorize"

## Полный пример: Получение списка клиентов

### 1. Получить токен
```powershell
$loginBody = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

$loginResponse = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody `
    -UseBasicParsing

$auth = $loginResponse.Content | ConvertFrom-Json
$token = $auth.accessToken
```

### 2. Использовать токен для запроса
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

$clients = Invoke-WebRequest -Uri "http://localhost:8086/client-service/api/clients" `
    -Headers $headers `
    -UseBasicParsing

Write-Host $clients.Content
```

## Troubleshooting

### 401 Unauthorized
- Проверьте, что токен не истек (access token живет 30 минут)
- Проверьте формат заголовка: `Authorization: Bearer TOKEN` (слово "Bearer" обязательно)
- Используйте refresh token для получения нового access token

### 403 Forbidden
- У пользователя недостаточно прав для этого действия
- Войдите под пользователем с ролью ADMIN

### 423 Account Locked
- Учетная запись заблокирована после нескольких неудачных попыток входа
- Подождите или обратитесь к администратору

## Безопасность

- **JWT Secret** настраивается через переменную окружения `JWT_SECRET`
- Пароли хешируются с помощью BCrypt
- Access token истекает через 30 минут
- Refresh token истекает через 12 часов
- Реализована защита от брутфорса
- Все действия аудируются

## Переменные окружения

```yaml
# API Gateway
JWT_SECRET: "your-secret-key-min-64-characters-long-for-production-use-only-12345"
JWT_ACCESS_TOKEN_EXPIRATION: 1800  # 30 минут
JWT_REFRESH_TOKEN_EXPIRATION: 43200  # 12 часов

# Client Service
JWT_SECRET: "ParkingSystemSecretKey2025!VeryLongAndSecureKey123456789"
```

**Важно:** В production используйте надежный JWT_SECRET длиной минимум 64 символа!

