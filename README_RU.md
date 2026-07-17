# 🅿️ Parking System — Микросервисная архитектура

> 🌐 **English version:** [README.md](./README.md)

Современная система управления парковкой на основе микросервисной архитектуры с использованием Spring Boot, Spring Cloud, Docker и React.

## 🆕 Последние обновления

> **Показан последний коммит.** Полная история: [CHANGELOG.md](./CHANGELOG.md)

### 2026-07-13 — Мультиязычный UI фронтенда (EN/DE/UA/RU) ✅

- 🌍 Добавлены централизованные словари i18n в `frontend/src/i18n/translations.ts`
- 🔧 Добавлен `LanguageProvider` + `useLanguage()` в `frontend/src/store/languageContext.tsx`
- 💾 Выбранный язык сохраняется в `localStorage` (`parking-system-language`)
- 🧭 Переключатель языка добавлен на странице логина и в боковом меню приложения
- ✅ Переведены основные страницы UI (dashboard, clients, subscriptions, management, billing, gate, reporting, simulator)

### 2026-03-09 — CORS wildcard для динамического LAN IP (Issue #79 fix) ✅

- 🔧 **`SecurityConfiguration.java`**: `setAllowedOrigins()` → `setAllowedOriginPatterns()` — поддержка wildcard `http://192.168.*`
- 🔧 **`CorsFilter.java`**: добавлен `isOriginAllowed()` с логикой wildcard
- 🔧 **`application.yml`** + **`docker-compose.yml`**: убраны хардкод IP, `http://192.168.*` покрывает весь домашний LAN независимо от DHCP
- ✅ React-фронтенд на `http://192.168.1.X:5173` работает с любым динамическим IP без изменений конфига

---

## 📈 Статус проекта и дорожная карта

### Текущий статус: Фаза 3 — Завершена ✅

| Фаза | Прогресс | Статус |
|------|----------|--------|
| Фаза 0 | 100% | ✅ Завершена |
| Фаза 1 | 100% | ✅ Завершена |
| Фаза 2 | 100% | ✅ Завершена |
| Фаза 3 | 100% | ✅ Завершена |
| Фаза 4 | 0% | ⏳ Ожидается |
| Фаза 5 | 0% | ⏳ Ожидается |

### 📋 Обзор фаз проекта

| Фаза | Длительность | Статус | Прогресс | Описание |
|------|-------------|--------|----------|----------|
| **Фаза 0** | 1 неделя | ✅ Завершена | 100% | Инфраструктура и основа |
| **Фаза 1** | 3 недели | ✅ Завершена | 100% | Базовый бэкенд (CRUD и БД) |
| **Фаза 2** | 2 недели | ✅ Завершена | 100% | Основная бизнес-логика |
| **Фаза 3** | 2 недели | ✅ Завершена | 100% | Безопасность, CORS и React-фронтенд |
| **Фаза 4** | 3 недели | ⏳ Ожидается | 0% | Отчёты и расширенные E2E |
| **Фаза 5** | 1 неделя | ⏳ Ожидается | 0% | Финализация и деплой |

📖 **Детальная дорожная карта:** [PROJECT_PHASES.md](./docs/PROJECT_PHASES.md)

### 🎯 Завершение Фазы 3

**Завершено:**
- ✅ Issue #78 — RBAC: ролевая защита маршрутов в SecurityFilter
- ✅ Issue #80 — Дефолтный пользователь OPERATOR при старте приложения
- ✅ Issue #72 — Проверка подписки: реальная логика в client-service
- ✅ Issue #73 — E2E-тест: полный цикл парковки абонента
- ✅ Issue #79 — CORS в api-gateway (wildcard поддержка LAN)
- ✅ Issue #74 — React-фронтенд: инициализация, аутентификация, ролевой макет

**Статус:**
- ✅ Скоуп Фазы 3 полностью завершён
- ⏳ Следующий фокус: Фаза 4 (отчёты и расширенные E2E)

### 📊 Быстрая статистика

| Метрика | Значение |
|---------|---------|
| **Микросервисов** | 7 бэкенд + 1 фронтенд |
| **Юнит/интеграционных тестов** | 177+ |
| **E2E-тестов** | 2 сценария (разовый + абонент) |
| **API-эндпоинтов** | 20+ (через API Gateway) |
| **Миграций БД** | 12 (V0–V11) |
| **OpenAPI-спецификации** | 5 сервисов (OpenAPI-first) |
| **Языков UI** | 4 (EN, DE, UA, RU) |

---

## 🏗️ Архитектура системы

| Компонент | Порт | Назначение |
|-----------|------|------------|
| React Frontend (Vite dev) | 5173 | UI-клиент, проксирует `/api/*` в API Gateway |
| API Gateway | 8086 | JWT, RBAC, CORS, rate limit, brute-force защита, Flyway |
| client-service | 8081 | Клиенты, ТС, подписки, проверка абонентов |
| gate-control-service | 8082 | Въезд/выезд/ручное управление шлагбаумом |
| billing-service | 8083 | Расчёт стоимости и обработка платежей |
| management-service | 8084 | Управление парковочными местами |
| reporting-service | 8087 | Логи и отчёты |
| eureka-server | 8761 | Service discovery |
| PostgreSQL | 5433 | Основная база данных |
| Redis | 6379 | Кэш, счётчики rate-limit/brute-force |
| Prometheus | 9090 | Сбор метрик |
| Grafana | 3000 | Дашборды |
| Jaeger | 16686 | Трейсинг |
| OTel Collector | 4317/4318 | Сбор телеметрии |
| pgAdmin | 5050 | UI для PostgreSQL |

---

## 🚀 Быстрый старт

### Предварительные требования
- Docker & Docker Compose
- Java 21+
- Maven 3.8+
- Node.js 20+ (для фронтенда)

### Запуск системы (бэкенд)
```bash
# Клонировать репозиторий
git clone <repository-url>
cd parking-system

# Собрать все сервисы
mvn clean install -DskipTests

# Запустить все контейнеры
docker-compose up -d

# Проверить статус
docker-compose ps
```

### Запуск фронтенда (dev)
```bash
cd frontend
npm install
npm run dev
# Открывается на http://localhost:5173
# Проксирует /api/* → http://localhost:8086
```
 
---

## ⚙️ CI/CD (GitHub Actions)

В репозитории добавлены workflow GitHub Actions в `.github/workflows/` для автоматизации CI и CD:

- `ci.yml` — CI: запускается на push и PR; выполняет unit-тесты бэкенда (Java 21 / Maven) и проверки фронтенда (ESLint + TypeScript/Vite build). В workflow задаётся тестовый `JWT_SECRET` для сценариев, где требуется JWT.
- `cd.yml` — сборка и пуш Docker-образов в GitHub Container Registry (GHCR). Триггер — push в `main` и теги вида `v*.*.*`. Сначала собираются JAR, затем образы для каждого сервиса и фронтенда. Job с `deploy` включён, но закомментирован — включайте его после настройки секретов.
- `e2e.yml` — E2E тесты (ручной запуск `workflow_dispatch`). Собирает образы для E2E и запускает Testcontainers-based тесты. Требует раннер с доступом к Docker.

Важные замечания перед включением deploy:

- `cd.yml` использует `docker/login-action` и `GITHUB_TOKEN` для пуша в GHCR. `GITHUB_TOKEN` предоставляется Actions автоматически; убедитесь, что разрешения репозитория позволяют запись в пакеты.
- Для включения закомментированного `deploy`-job добавьте секреты в GitHub → Settings → Secrets: `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_KEY`, `DEPLOY_PATH`.
- Никогда не коммитьте приватные ключи и другие секреты. Удалите локальные файлы вроде `ssh key.txt` и добавьте их в `.gitignore`.

Локальные аналоги часто выполняемых CI шагов:

```powershell
# Запустить unit-тесты бэкенда
mvn clean test

# Собрать JAR (вход для сборки Docker)
mvn clean package -DskipTests

# Собрать образ фронтенда локально
docker build -t parking-frontend:local frontend

# Собрать образ для бэкенд-сервиса (пример)
docker build -t parking-api-gateway:local backend/api-gateway
```
### Доступ к сервисам
| Сервис | Адрес | Примечание |
|--------|-------|-----------|
| **React Frontend** | http://localhost:5173 | Vite dev server |
| **API Gateway** | http://localhost:8086 | Точка входа для всех API |
| **Eureka Server** | http://localhost:8761 | UI реестра сервисов |
| **Grafana** | http://localhost:3000 | admin / admin123 |
| **Prometheus** | http://localhost:9090 | Метрики |
| **Jaeger** | http://localhost:16686 | Distributed tracing |
| **pgAdmin** | http://localhost:5050 | admin@parking.com / admin |
| **PostgreSQL** | localhost:5433 | parking_db / postgres / postgres |
| **Тестовый интерфейс** | [devops/test-login.html](./devops/test-login.html) | Браузерный API-тестер |

---

## 🔧 Микросервисы

### 1. API Gateway (порт 8086)
- **JWT аутентификация** — HS512, 30 мин access / 12 ч refresh
- **RBAC** — ролевая защита маршрутов в `SecurityFilter`
- **CORS** — wildcard `http://192.168.*` (DHCP-safe)
- **Ограничение запросов** — 60 req/min на IP
- **Защита от брутфорса** — блокировка после 10 неудачных попыток
- **Flyway** — управляет миграциями БД (V0–V11)

### 2. Client Service (порт 8081)
OpenAPI-first. Управление клиентами, ТС и подписками.

- `POST/GET/PUT/DELETE /api/clients` — CRUD клиентов
- `GET /api/clients/search?phone=` — поиск по телефону
- `POST /api/clients/{id}/vehicles` — добавить ТС
- `GET/PUT/DELETE /api/vehicles/{id}` — CRUD ТС
- `GET /api/v1/clients/subscriptions/check?licensePlate=` — проверка подписки (используется gate-control)

### 3. Gate Control Service (порт 8082)
OpenAPI-first. Управление физическими шлагбаумами.

- `POST /api/gate/entry` — въезд (возвращает тикет или пропуск абонента)
- `POST /api/gate/exit` — выезд (расчёт стоимости)
- `POST /api/gate/control` — ручное управление (OPEN/CLOSE)

### 4. Billing Service (порт 8083)
Расчёт стоимости парковки и фиксация платежей.

- `GET /api/billing/status-by-ticket?ticketCode=` — статус оплаты
- `POST /api/billing/pay-test` — тестовый эндпоинт оплаты

### 5. Management Service (порт 8084)
OpenAPI-first. Управление парковочными местами.

- `GET /api/management/spots` — все места
- `GET /api/management/spots/available` — доступные места
- `GET /api/management/spots/available/count` — количество доступных
- `GET /api/management/spots/available/lot/{lotId}` — по парковке
- `GET /api/management/spots/search?type=&status=` — поиск с фильтрами

### 6. Reporting Service (порт 8087)
OpenAPI-first. Централизованное логирование и аудит.

- `POST /api/reporting/log` — записать событие
- `GET /api/reporting/logs` — получить логи (фильтры: level, service, userId, дата)

### 7. Eureka Server (порт 8761)
Реестр сервисов — все микросервисы регистрируются здесь.

### 8. Стек наблюдаемости
- **Prometheus** (:9090) — сбор метрик
- **Grafana** (:3000) — дашборды (admin/admin123)
- **Jaeger** (:16686) — distributed tracing
- **OTel Collector** (:4317/:4318) — агрегация телеметрии

---

## 📊 Стек технологий

### Бэкенд
| Технология | Версия | Назначение |
|-----------|--------|-----------|
| **Java** | 21 | Основной язык |
| **Spring Boot** | 3.2.8 | Фреймворк микросервисов |
| **Spring Cloud** | 2023.0.3 | Eureka, LoadBalancer |
| **Spring Security** | (Boot managed) | Auth & RBAC |
| **JWT (jjwt)** | 0.12.6 | Токен-аутентификация |
| **Flyway** | (Boot managed) | Миграции БД |
| **MapStruct** | 1.5.5 | Маппинг DTO |
| **Lombok** | 1.18.34 | Сокращение шаблонного кода |
| **OpenAPI Generator** | 7.6.0 | OpenAPI-first codegen |

### Фронтенд
| Технология | Версия | Назначение |
|-----------|--------|-----------|
| **React** | 19 | UI-фреймворк |
| **TypeScript** | — | Типизация |
| **Vite** | — | Сборка + dev proxy |
| **Tailwind CSS** | 3 | Стилизация |
| **Radix UI** | — | shadcn/ui компоненты |
| **React Router** | 6 | Клиентский роутинг |
| **TanStack Query** | 5 | Серверный стейт |
| **Zustand** | 4 | Клиентский стейт (auth) |
| **Axios** | — | HTTP клиент |

---

## 🗄️ База данных

### Конфигурация PostgreSQL
- **База данных**: `parking_db`
- **Пользователь**: `postgres`
- **Пароль**: `postgres`
- **Порт**: `5433` (хост) / `5432` (внутри Docker)

### Flyway-миграции

| Версия | Файл | Описание |
|--------|------|----------|
| V0 | `V0__Baseline.sql` | Базовая линия |
| V1 | `V1__initial_schema.sql` | Основные таблицы (users, clients, vehicles, ...) |
| V2 | `V2__add_parking_lots.sql` | Парковочные объекты |
| V3 | `V3__add_parking_spaces.sql` | Парковочные места |
| V4 | `V4__add_bookings.sql` | Бронирования |
| V5 | `V5__insert_test_parking_data.sql` | Тестовые данные (вкл. абонент AA1234BB) |
| V6 | `V6__extend_logs_table.sql` | Расширение логов |
| V7 | `V7__create_tariffs_table.sql` | Тарифы |
| V8 | `V8__extend_parking_events_and_payments.sql` | Расширение событий/платежей |
| V9 | `V9__create_gate_events_table.sql` | Лог событий шлагбаума |
| V10 | `V10__extend_logs_audit_trail.sql` | Расширенные поля аудита в logs |
| V11 | `V11__add_parking_space_to_subscription.sql` | `parking_space_id` в subscriptions |

---

## 🔑 Тестовые учётные данные

Пользователи создаются через `database/init.sql` и дополнительно обеспечиваются `UserSecurityService.initializeDefaultUsers()` при старте.

| Пользователь | Пароль | Роль | Примечание |
|-------------|--------|------|-----------|
| **admin** | `parking123` | ADMIN | Полный доступ |
| **operator** | `operator123` | OPERATOR | Шлагбаум и биллинг |
| **manager** | `manager123` | MANAGER | Отчёты и управление |

**⚠️ Только для разработки.** В продакшене используйте надёжные пароли и переменные окружения.

---

## 🔒 Безопасность и RBAC

### Разрешения по маршрутам (SecurityFilter)

| Маршрут | Требуемые роли |
|---------|---------------|
| `POST/PUT/DELETE /api/gate/*` | OPERATOR, ADMIN |
| `POST/PUT/DELETE /api/billing/*` | OPERATOR, ADMIN |
| `GET/POST/PUT/DELETE /api/clients/*` | ADMIN, MANAGER, OPERATOR |
| write ops `/api/management/*` | ADMIN, MANAGER |
| `GET/POST/PUT/DELETE /api/reporting/*` | ADMIN, MANAGER, OPERATOR |

### Конфигурация JWT
```yaml
jwt:
  access-token-expiration: 1800    # 30 минут
  refresh-token-expiration: 43200  # 12 часов
  secret: ${JWT_SECRET}            # HS512, мин 64 символа
```

---

## 🧪 Тестирование

### E2E-тесты
```powershell
cd devops
.\run-e2e-tests.ps1

# Или напрямую через Maven
cd backend
mvn test -Pe2e
```

**E2E-сценарии:**
- ✅ `OneTimeVisitorE2ETest` — Въезд → тикет → оплата → выезд
- ✅ `SubscriberE2ETest` — Въезд абонента → бесплатный выезд

### Юнит и интеграционные тесты
```bash
mvn clean test
```

**Количество тестов:**
- api-gateway: ~80
- client-service: ~26
- gate-control-service: ~30
- billing-service: ~20
- management-service: ~21
- **Итого: 177+ тестов**

---

## 📚 Документация

### Архитектура и безопасность
- [Аутентификация](./docs/AUTHENTICATION.md)
- [Архитектура безопасности](./docs/SECURITY_ARCHITECTURE.md)
- [Настройка наблюдаемости](./docs/OBSERVABILITY_SETUP.md)
- [API-контракты](./docs/api-contracts.md)

### База данных и деплой
- [Database README](./database/README.md)
- [Руководство по деплою](./docs/DEPLOYMENT_GUIDE.md)
- [Быстрый справочник по конфигурации](./docs/PRODUCTION_CONFIG_QUICK_REF.md)

### DevOps-скрипты (`devops/`)
| Скрипт | Назначение |
|--------|-----------|
| `start-all.ps1` | Запуск всей системы |
| `stop-system.ps1` | Остановка контейнеров |
| `full-rebuild.ps1` | Полная пересборка и перезапуск |
| `run-e2e-tests.ps1` | Запуск E2E-тестов |
| `check-system.ps1` | Проверка состояния всех сервисов |
| `backup-db.ps1` | Резервное копирование PostgreSQL |
| `unlock-account.ps1` | Разблокировка заблокированного пользователя |
| `reset-brute-force.ps1` | Сброс счётчика неудачных попыток |

---

## 🤝 Участие в разработке

1. Форкните репозиторий
2. Создайте ветку (`git checkout -b feature/МояФункция`)
3. Зафиксируйте изменения (`git commit -m 'feat: Add some feature'`)
4. Отправьте в ветку (`git push origin feature/МояФункция`)
5. Откройте Pull Request

## 📄 Лицензия

Проект лицензирован по лицензии MIT.

---

**Сделано с ❤️ с использованием Spring Boot и React**
