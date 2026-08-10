# Аутентификация (канонический документ)

> 🌐 **English version:** [AUTHENTICATION.md](./AUTHENTICATION.md)

**Обновлено:** 2026-08-10  
**Область:** Аутентификация API Gateway и использование JWT в системе

---

## 1. Обзор

Parking System использует JWT-аутентификацию через **api-gateway** (`:8086`).

Поток высокого уровня:
1. `POST /api/auth/login` -> получение `accessToken` и `refreshToken`
2. Клиент отправляет `Authorization: Bearer <accessToken>` на защищённые API
3. Когда access token истекает, клиент вызывает `POST /api/auth/refresh`
4. Выход из системы выполняется через эндпоинты отзыва токенов

---

## 2. Auth-эндпоинты (api-gateway)

Базовый путь: `/api/auth`

| Метод | Эндпоинт | Назначение |
|---|---|---|
| POST | `/api/auth/login` | Аутентификация пользователя и выдача токенов |
| POST | `/api/auth/refresh` | Выдача нового access token по refresh token |
| POST | `/api/auth/logout` | Отзыв текущего токена |
| POST | `/api/auth/logout-all` | Отзыв всех сессий пользователя |
| POST | `/api/auth/change-password` | Смена пароля аутентифицированного пользователя |
| GET | `/api/auth/profile` | Профиль текущего пользователя |

Публичные эндпоинты (без JWT):
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- health checks

---

## 3. JWT-модель (текущая реализация)

- **Алгоритм:** `HS512`
- **Время жизни access token:** `1800s` (30 мин)
- **Время жизни refresh token:** `43200s` (12 ч)
- **Обязательная env-переменная:** `JWT_SECRET` (64+ символов в production)

Access token содержит такие claims:
- `sub` (username)
- `userId`
- `role`
- `iss`, `aud`, `iat`, `exp`
- `jti`

Пример header:
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

Пример payload (форма):
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

## 4. Роли компонентов

### CorsFilter
- Выполняется до security-проверок.
- Обрабатывает CORS и завершает preflight `OPTIONS` заранее.

### SecurityFilter
- Валидирует bearer token на защищённых эндпоинтах.
- Формирует security context из token claims.
- Применяет RBAC для комбинаций route/method.
- Применяет rate limiting и brute-force protection.

### JwtTokenService
- Создаёт access и refresh токены.
- Валидирует подпись/срок действия токенов.
- Поддерживает отзыв токенов/blacklist-сценарии.

---

## 5. Примеры использования

### Логин

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

### Авторизованный запрос

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8086/api/clients
```

```powershell
Invoke-WebRequest -Uri "http://localhost:8086/api/clients" `
  -Headers @{Authorization="Bearer $token"} -UseBasicParsing
```

### Обновление токена

```bash
curl -X POST http://localhost:8086/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

---

## 6. Конфигурация

```yaml
jwt:
  algorithm: HS512
  access-token-expiration: 1800
  refresh-token-expiration: 43200
  secret: ${JWT_SECRET}
```

Production checklist:
- использовать сильный `JWT_SECRET` (64+ символов)
- не коммитить секреты
- управлять TTL токенов через env

