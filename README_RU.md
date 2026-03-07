# 🅿️ Parking System — Микросервисная архитектура

> 🌐 **English version:** [README.md](./README.md)

Современная система управления парковкой на основе микросервисной архитектуры с использованием Spring Boot, Spring Cloud и Docker.

## 🆕 Последние обновления

> **Показаны последние 3 обновления.** Полная история: [CHANGELOG.md](./CHANGELOG.md) | [Логи сессий](./docs/sessions/)

### 2026-03-07 — Исправления багов, все тесты зелёные (Issue #70: [Фаза 2] E2E-тест: полный цикл разового посетителя) ✅

✅ **Полная сборка: 161 юнит-тест + 1 E2E-тест — BUILD SUCCESS**
- 🐛 **billing-service**: `getPaymentStatus` — исправлен ответ 404 для неизвестных событий
- 🐛 **billing-service**: `JacksonConfig` — исправлена несовместимость со слайсом `@DataJpaTest`
- 🐛 **gate-control-service**: `PaymentStatusResponse.remainingFee` — восстановлен тип `BigDecimal`
- 🐛 **gate-control-service**: `BillingServiceClient` — разбор fee как `BigDecimal` (не `Double`)
- 🐛 **gate-control-service**: `GateServiceTest` — исправлен mock (`checkPaymentStatusByTicket`), удалены лишние стабы
- 🛠️ **Новый скрипт**: `devops/run-e2e-tests.ps1` — автономный запуск E2E с проверкой Docker
- 📖 **Лог сессии**: [docs/sessions/SESSION_DEVELOPMENT_2026-03-07.md](./docs/sessions/SESSION_DEVELOPMENT_2026-03-07.md)

### 2026-02-14 — Инфраструктура E2E-тестов завершена (Issue #70: [Фаза 2] E2E-тест: полный цикл разового посетителя) ✅

✅ **E2E-тесты — сценарий разового посетителя** (Issue #70)
- ✅ `OneTimeVisitorE2ETest` — полный цикл парковки (Въезд → Оплата → Выезд)
- ✅ Интеграция Testcontainers с оркестрацией docker-compose
- ✅ 9 микросервисов, протестированных в изолированном окружении
- ✅ Время теста: ~2 минуты | Успешность: 100%
- ✅ Поддержка Windows 11 Home Edition (исправление совместимости Docker API)
- ✅ Автоматический скрипт сборки: `build-e2e-images.ps1`
- 📊 **Покрытие тестами**: въездной шлагбаум, обработка платежей, выездной шлагбаум, service discovery
- 🏗️ **Улучшения архитектуры**:
  - Платёжный поток на основе тикета (ticketCode как натуральный ключ)
  - Синхронизация схемы БД (init.sql обновлён под миграции V8/V9)
  - Новые эндпоинты: `/api/v1/billing/pay-test`, `/api/v1/billing/status-by-ticket`
  - Поддержка nullable `vehicle_id` (разовые посетители без регистрации)
- 📖 **Документация**: [backend/e2e-tests/README.md](./backend/e2e-tests/README.md)
- 📖 **Лог сессии**: [docs/sessions/SESSION_DEVELOPMENT_2026-02-14.md](./docs/sessions/SESSION_DEVELOPMENT_2026-02-14.md)
- 🎯 **Следующие шаги**: сценарий для абонента, тесты управления подписками

### 2026-01-27 — Gate Control Service: выезд и ручное управление (Issue #52) ✅

✅ **Gate Control Service — эндпоинты выезда и ручного управления** (Issue #52)
- ✅ Эндпоинт POST /api/v1/gate/exit с OpenAPI-first реализацией
- ✅ Эндпоинт POST /api/v1/gate/control для ручного управления оператором
- ✅ DTO ExitRequest и ManualControlRequest с валидацией
- ✅ DTO ExitDecision и ManualControlResponse для ответов
- ✅ GateService.processExit с интеграцией BillingService (проверка оплаты)
- ✅ Ручное управление логирует действия оператора в Reporting Service и GateEvent (MANUAL_OPEN)
- ✅ Интеграционные тесты: платный/неоплаченный выезд и ручное управление — **ВСЕ ПРОЙДЕНЫ** ✅
- 📖 **Контроллер:** [GateController.java](./backend/gate-control-service/src/main/java/com/parking/gate_control_service/controller/GateController.java)
- 📖 **Тесты:** [GateControllerIntegrationTest.java](./backend/gate-control-service/src/test/java/com/parking/gate_control_service/controller/GateControllerIntegrationTest.java)
- 🎯 **Следующие шаги:** нет — Фаза 2 завершена!

---

## 📈 Статус проекта и дорожная карта

### Текущий статус: Фаза 2 — ЗАВЕРШЕНА 🚀

```
Фаза 0: ████████████████████ 100% ✅ ЗАВЕРШЕНА
Фаза 1: ████████████████████ 100% ✅ ЗАВЕРШЕНА
Фаза 2: ████████████████████ 100% ✅ ЗАВЕРШЕНА
Фаза 3: ░░░░░░░░░░░░░░░░░░░░   0% ⏳ ОЖИДАЕТСЯ
Фаза 4: ░░░░░░░░░░░░░░░░░░░░   0% ⏳ ОЖИДАЕТСЯ
Фаза 5: ░░░░░░░░░░░░░░░░░░░░   0% ⏳ ОЖИДАЕТСЯ
```

### 📋 Обзор фаз проекта

| Фаза | Длительность | Статус | Прогресс | Описание |
|------|-------------|--------|----------|----------|
| **Фаза 0** | 1 неделя | ✅ Завершена | 100% | Инфраструктура и основа |
| **Фаза 1** | 3 недели | ✅ Завершена | 100% | Базовый бэкенд (CRUD и БД) |
| **Фаза 2** | 2 недели | ✅ Завершена | 100% | Основная бизнес-логика |
| **Фаза 3** | 2 недели | ⏳ Ожидается | 0% | Интеграция и безопасность |
| **Фаза 4** | 3 недели | ⏳ Ожидается | 0% | Фронтенд, отчёты и E2E |
| **Фаза 5** | 1 неделя | ⏳ Ожидается | 0% | Финализация и деплой |

📖 **Детальная дорожная карта:** [PROJECT_PHASES.md](./docs/PROJECT_PHASES.md)

### 📊 Быстрая статистика

| Метрика | Значение |
|---------|---------|
| **Всего задач (Issues)** | 70 |
| **Закрытых задач** | 53 (76%) |
| **Микросервисов** | 9 |
| **Фаза 1** | ✅ 100% завершена |
| **Фаза 2** | ✅ 100% завершена |
| **E2E-тесты** | ✅ Реализованы |
| **API-эндпоинтов** | 54+ |
| **Тестов** | 100+ (юнит + интеграционные + E2E) |
| **Миграций БД** | 9 (V0–V9) |
| **Покрытие кода** | ~90% в среднем |

### Последние достижения

**2026-02-14 — E2E-инфраструктура тестирования (Issue #70)**
- ✅ OneTimeVisitorE2ETest — тест полного цикла парковки
- ✅ Интеграция Testcontainers с docker-compose
- ✅ 9 микросервисов в изолированном окружении
- ✅ Время теста: ~2 минуты | Успешность: 100%
- ✅ Поддержка Windows 11 Home Edition
- ✅ Платёжный поток на основе тикета
- ✅ Синхронизация схемы БД (init.sql обновлён под V8/V9)
- ✅ Новые эндпоинты: `/api/v1/billing/pay-test`, `/api/v1/billing/status-by-ticket`
- 📖 **Тест:** [OneTimeVisitorE2ETest.java](./backend/e2e-tests/src/test/java/com/parking/e2e/OneTimeVisitorE2ETest.java)
- 📖 **Документация:** [backend/e2e-tests/README.md](./backend/e2e-tests/README.md)

### Следующие шаги

**Ближайшие задачи:**
1. Реализовать Gate Control Service (Issue #37) — POST /entry, POST /exit, GET /status
2. Добавить межсервисное взаимодействие (Issue #38) — Billing ↔ Gate Control

**В ближайшие 2 недели:**
1. Завершить Фазу 2: реализация бизнес-логики
2. Межсервисная коммуникация через WebClient
3. Начать Фазу 3: безопасность и интеграция (JWT, Spring Security)

---

## 🏗️ Архитектура системы

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│    Фронтенд     │───▶│   API Gateway    │───▶│  Микросервисы  │
│                 │    │                  │    │                 │
│ React/Angular   │    │ • Маршрутизация  │    │ • Client Svc    │
│ Мобильное прил. │    │ • Балансировка   │    │ • User Svc      │
│ Панель админа   │    │ • CORS           │    │ • Parking Svc   │
└─────────────────┘    │ • Мониторинг     │    │ • Booking Svc   │
                       └──────────────────┘    │ • Billing Svc   │
                                │              │ • Gate Ctrl Svc │
                                ▼              │ • Management    │
                       ┌──────────────────┐    │ • Reporting     │
                       │ Service Registry │    └─────────────────┘
                       │  (Eureka Server) │             │
                       └──────────────────┘             ▼
                                               ┌─────────────────┐
                                               │   PostgreSQL    │
                                               │    База данных  │
                                               └─────────────────┘
```

---

## 🚀 Быстрый старт

### Предварительные требования
- Docker & Docker Compose
- Java 21+
- Maven 3.8+

### Запуск системы
```bash
# Клонировать репозиторий
git clone <repository-url>
cd parking-system

# Запустить все сервисы
docker-compose up -d

# Проверить статус
docker-compose ps
```

### Доступ к сервисам
- **API Gateway**: http://localhost:8086
- **Eureka Server**: http://localhost:8761
- **Client Service**: http://localhost:8081 (через Gateway)
- **Management Service**: http://localhost:8083 (через Gateway)
- **Тестовый интерфейс**: [devops/test-login.html](./devops/test-login.html) — браузерный API-тестер
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686
- **pgAdmin**: http://localhost:5050 (admin@parking.com/admin)
- **PostgreSQL**: localhost:5433 (parking_db/postgres/postgres)

---

## 🔧 Микросервисы

### 1. API Gateway (порт 8086)
- Централизованная точка входа
- JWT-аутентификация и авторизация
- Маршрутизация запросов к микросервисам
- Функции безопасности:
  - Ограничение частоты запросов (100 req/min на IP)
  - Защита от брутфорса (5 неудачных попыток)
  - Обнаружение подозрительных IP
  - Аудит безопасности
- CORS и базовая безопасность
- Мониторинг и метрики

### 2. Client Service (порт 8081)
- Управление клиентами и транспортными средствами
- CRUD-операции для клиентов и ТС
- Интеграция с PostgreSQL
- JWT-аутентификация через API Gateway
- Спецификация OpenAPI 3.0

**Эндпоинты клиентов** (через API Gateway):
- `POST /api/clients` — создать клиента
- `GET /api/clients` — список всех клиентов
- `GET /api/clients/{id}` — получить клиента по ID
- `PUT /api/clients/{id}` — обновить клиента
- `DELETE /api/clients/{id}` — удалить клиента
- `GET /api/clients/search?phone={phone}` — поиск по телефону

**Эндпоинты транспортных средств** (через API Gateway):
- `POST /api/clients/{clientId}/vehicles` — добавить ТС
- `GET /api/vehicles` — список всех ТС
- `GET /api/vehicles/{id}` — получить ТС по ID
- `PUT /api/vehicles/{id}` — обновить ТС
- `DELETE /api/vehicles/{id}` — удалить ТС

### 3. Management Service (порт 8083)
- Управление и мониторинг парковочных мест
- Отслеживание доступности в реальном времени
- Поиск и фильтрация
- Интеграция с PostgreSQL
- Спецификация OpenAPI 3.0

**Эндпоинты парковочных мест** (через API Gateway):
- `GET /api/management/spots` — все парковочные места
- `GET /api/management/spots/available` — доступные места
- `GET /api/management/spots/available/count` — количество доступных
- `GET /api/management/spots/available/lot/{lotId}` — доступные по парковке
- `GET /api/management/spots/search?type={type}&status={status}` — поиск с фильтрами

**Типы мест:** STANDARD, HANDICAPPED, ELECTRIC, VIP, COMPACT, OVERSIZED

**Статусы мест:** AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, OUT_OF_SERVICE

### 4. Service Registry (порт 8761)
- Eureka Server для service discovery
- Регистрация и обнаружение микросервисов
- Проверки состояния (health checks)

### 5. Стек наблюдаемости
- **Prometheus** (порт 9090) — сбор метрик
- **Grafana** (порт 3000) — дашборды и визуализация
- **Jaeger** (порт 16686) — распределённая трассировка
- **OpenTelemetry Collector** (порт 4317/4318) — сбор телеметрии

### 6. Управление базой данных
- **PostgreSQL 16** (порт 5433) — основная БД
- **pgAdmin 4** (порт 5050) — UI для управления БД
- **Redis 7** (порт 6379) — кэш и хранилище сессий

---

## 📊 Стек технологий

### Бэкенд
- **Java 21** — основной язык программирования
- **Spring Boot 3.2.x** — фреймворк для микросервисов
- **Spring Cloud 2023.0.x** — микросервисная архитектура
- **Spring Security** — аутентификация и авторизация
- **Spring Data JPA** — работа с БД
- **JWT (jjwt 0.12.6)** — токен-based аутентификация
- **MapStruct** — маппинг DTO ↔ Entity
- **Lombok** — сокращение шаблонного кода

### Инфраструктура
- **Docker & Docker Compose** — контейнеризация
- **PostgreSQL 16** — основная БД
- **Redis 7** — кэш и хранилище сессий
- **Eureka Server** — Service Registry
- **Spring Cloud Gateway** — API Gateway
- **Maven** — система сборки

### Наблюдаемость
- **Prometheus** — сбор метрик
- **Grafana** — мониторинговые дашборды
- **Jaeger** — распределённая трассировка
- **OpenTelemetry** — инструментирование телеметрии

### Документация и тестирование
- **OpenAPI 3 / Swagger UI** — документация API
- **JUnit 5** — юнит-тестирование
- **Spring Boot Test** — интеграционное тестирование
- **Testcontainers** — E2E-тестирование

---

## 🗄️ База данных

### Конфигурация PostgreSQL
- **База данных**: `parking_db`
- **Пользователь**: `postgres`
- **Пароль**: `postgres`
- **Порт**: `5433` (Docker), `5432` (локально)

### Flyway-миграции

Схема БД управляется через **Flyway** с версионным контролем.

**Файлы миграций:** `backend/api-gateway/src/main/resources/db/migration/`

| Версия | Файл | Описание | Таблицы |
|--------|------|----------|---------|
| V0 | `V0__baseline.sql` | Базовая линия | — |
| V1 | `V1__initial_schema.sql` | Основная схема | 8 таблиц |
| V2 | `V2__add_parking_lots.sql` | Парковочные объекты | parking_lots |
| V3 | `V3__add_parking_spaces.sql` | Парковочные места | parking_spaces |
| V4 | `V4__add_bookings.sql` | Бронирования | bookings |
| V7 | `V7__add_tariffs.sql` | Тарифы | tariffs |
| V8 | `V8__extend_parking_events_and_payments.sql` | Расширение событий/платежей | — |
| V9 | `V9__create_gate_events_table.sql` | События шлагбаума | gate_events |

**Быстрые команды:**
```powershell
# Просмотреть историю миграций
docker exec parking_db psql -U postgres -d parking_db -c "SELECT * FROM flyway_schema_history;"
```

📖 **Полное руководство:** [Database README](./database/README.md)

---

## 🔑 Тестовые учётные данные

### Для разработки и тестирования:

| Пользователь | Пароль | Роль | Email |
|-------------|--------|------|-------|
| **admin** | `parking123` | ADMIN | admin@parking.com |
| **user** | `user1234` | USER | user@parking.com |
| **manager** | `manager123` | MANAGER | manager@parking.com |

**⚠️ ВАЖНО:** Эти учётные данные предназначены только для разработки! В продакшене используйте надёжные пароли и переменные окружения.

### Быстрая проверка аутентификации:
```powershell
# PowerShell
$body = @{ username = "admin"; password = "parking123" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body
```

```bash
# Bash/cURL
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}'
```

---

## 🔧 Конфигурация

### Переменные окружения
```bash
# База данных
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/parking_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT безопасность
JWT_SECRET=<ВАШ_64_СИМВОЛЬНЫЙ_СЕКРЕТ>
JWT_ACCESS_TOKEN_EXPIRATION=3600
JWT_REFRESH_TOKEN_EXPIRATION=604800

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Приложение
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=development
```

---

## 📚 Документация

### История разработки
- **[Логи сессий](./docs/sessions/)** — подробные логи сессий разработки

| Дата | Тема | 🇷🇺 Русский | 🇬🇧 English |
|------|------|-------------|-------------|
| 2026-03-07 | Bug Fixes & All Tests Green | [RU](./docs/sessions/SESSION_DEVELOPMENT_2026-03-07.md) | *(тот же файл)* |
| 2026-02-14 | E2E Testing Implementation | [RU](./docs/sessions/SESSION_DEVELOPMENT_2026-02-14.md) | *(тот же файл)* |
| 2026-02-04 | — | [RU](./docs/sessions/SESSION_DEVELOPMENT_2026-02-04_RU.md) | [EN](./docs/sessions/SESSION_DEVELOPMENT_2026-02-04_EN.md) |
| 2026-01-27 | Gate Control: Exit & Manual | [RU](./docs/sessions/SESSION_DEVELOPMENT_2026-01-27_RU.md) | [EN](./docs/sessions/SESSION_DEVELOPMENT_2026-01-27_EN.md) |
| 2026-01-26 | Gate Control: Entry & Logic | [RU](./docs/sessions/SESSION_DEVELOPMENT_2026-01-26_RU.md) | [EN](./docs/sessions/SESSION_DEVELOPMENT_2026-01-26_EN.md) |
| 2026-01-24 | Billing Service REST API | [RU](./docs/sessions/SESSION_DEVELOPMENT_2026-01-24_RU.md) | [EN](./docs/sessions/SESSION_DEVELOPMENT_2026-01-24_EN.md) |
| 2026-01-18 | Billing Service Complete | — | [EN](./docs/sessions/SESSION_DEVELOPMENT_2026-01-18_EN.md) |
| 2026-01-16 | Phase 2 DB & API Contracts | — | [EN](./docs/sessions/SESSION_DEVELOPMENT_2026-01-16_EN.md) |
| 2026-01-13 | — | — | [EN](./docs/sessions/SESSION_DEVELOPMENT_2026-01-13_EN.md) |
| 2026-01-12 | Management API Day 2 | — | [EN](./docs/sessions/SESSION_DEVELOPMENT_2026-01-12_EN.md) |
| 2026-01-11 | Management API Day 1 | — | [EN](./docs/sessions/SESSION_DEVELOPMENT_2026-01-11_EN.md) |

### E2E-тесты

| Документ | 🇷🇺 Русский | 🇬🇧 English |
|----------|-------------|-------------|
| Руководство по запуску | [E2E_TESTING_GUIDE_RU.md](./backend/e2e-tests/E2E_TESTING_GUIDE_RU.md) | [E2E_TESTING_GUIDE_EN.md](./backend/e2e-tests/E2E_TESTING_GUIDE_EN.md) |
| Быстрый старт | [QUICK_START_GUIDE_RU.md](./backend/e2e-tests/QUICK_START_GUIDE_RU.md) | [QUICK_START_GUIDE_EN.md](./backend/e2e-tests/QUICK_START_GUIDE_EN.md) |
| Настройка Windows | [E2E_TESTCONTAINERS_WINDOWS_SETUP_RU.md](./backend/e2e-tests/E2E_TESTCONTAINERS_WINDOWS_SETUP_RU.md) | [E2E_TESTCONTAINERS_WINDOWS_SETUP_EN.md](./backend/e2e-tests/E2E_TESTCONTAINERS_WINDOWS_SETUP_EN.md) |
| Архитектура портов | [PORTS_ARCHITECTURE_RU.md](./backend/e2e-tests/PORTS_ARCHITECTURE_RU.md) | [PORTS_ARCHITECTURE_EN.md](./backend/e2e-tests/PORTS_ARCHITECTURE_EN.md) |

### DevOps

| Документ | 🇷🇺 Русский | 🇬🇧 English |
|----------|-------------|-------------|
| Скрипты деплоя | [README.md](./devops/README.md) | *(тот же файл)* |
| Веб-тестер API | [TEST_LOGIN_README_RU.md](./devops/TEST_LOGIN_README_RU.md) | [TEST_LOGIN_README_EN.md](./devops/TEST_LOGIN_README_EN.md) |
| Наблюдаемость | [OBSERVABILITY_README_RU.md](./devops/OBSERVABILITY_README_RU.md) | [OBSERVABILITY_README_EN.md](./devops/OBSERVABILITY_README_EN.md) |
| Исправление скриптов | [README_SCRIPT_FIX_RU.md](./devops/README_SCRIPT_FIX_RU.md) | [README_SCRIPT_FIX_EN.md](./devops/README_SCRIPT_FIX_EN.md) |

### Архитектура и безопасность
- **[Аутентификация](./docs/AUTHENTICATION.md)** — JWT-система аутентификации
- **[Архитектура безопасности](./docs/SECURITY_ARCHITECTURE.md)** — компоненты безопасности
- **[Настройка наблюдаемости](./docs/OBSERVABILITY_SETUP.md)** — мониторинг и трассировка
- **[Защита от брутфорса](./docs/BRUTE_FORCE_PROTECTION.md)** — защита от подбора паролей

### База данных и деплой
- **[Database README](./database/README.md)** — схема БД и руководство по Flyway
- **[Руководство по деплою](./docs/DEPLOYMENT_GUIDE.md)** — инструкции по развёртыванию в продакшене
- **[API-контракты](./docs/api-contracts.md)** — спецификации OpenAPI

---

## 🧪 Тестирование

### E2E-тесты (End-to-End)
```powershell
# Предварительные требования
cd backend/e2e-tests
.\build-e2e-images.ps1  # Собрать Docker-образы

# Запустить тесты
mvn test

# Запустить конкретный тест
mvn test -Dtest=OneTimeVisitorE2ETest

# Или использовать готовый скрипт (из devops/)
.\run-e2e-tests.ps1
```

**Покрытие тестами:**
- ✅ Полный цикл разового посетителя (Въезд → Оплата → Выезд)
- ✅ Интеграция всех 9 микросервисов
- ✅ Операции с БД (PostgreSQL)
- ✅ Service discovery (Eureka)
- ✅ Маршрутизация через API Gateway

📖 **Документация**: [backend/e2e-tests/README.md](./backend/e2e-tests/README.md)

### Юнит и интеграционные тесты
```bash
# Юнит-тесты
mvn test

# Интеграционные тесты
mvn verify

# Тесты всех модулей
mvn clean test -f pom.xml
```

### Ручное тестирование

#### Проверка аутентификации
```bash
# Получить JWT-токен
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}'

# Использовать токен для обращения к защищённому эндпоинту
curl -X GET http://localhost:8086/api/clients \
  -H "Authorization: Bearer ВАШ_JWT_ТОКЕН"
```

#### Проверки состояния (Health Checks)
```bash
# API Gateway
curl http://localhost:8086/actuator/health

# Client Service
curl http://localhost:8081/actuator/health

# Панель Eureka
open http://localhost:8761
```

### PowerShell-скрипты для тестирования
Находятся в папке `devops/`:
- `check-system.ps1` — полная проверка состояния системы
- `full-rebuild.ps1` — полная пересборка и тест
- `run-e2e-tests.ps1` — запуск E2E-тестов с проверкой Docker
- `quick-restart.ps1` — быстрый перезапуск без пересборки Maven

---

## 📚 API-документация

### Интерактивная документация
- **Swagger UI**: http://localhost:8086/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8086/v3/api-docs

### Постман-коллекция
Доступна в папке `/docs` для удобного тестирования API.

---

## 🎯 Функциональные возможности системы

### Основные сервисы
1. **`client-service`**: управляет базой клиентов и подписками, проверяет их действительность
2. **`gate-control-service`**: получает события от сканеров, принимает решение о пропуске ТС, управляет шлагбаумами
3. **`billing-service`**: рассчитывает стоимость парковочной сессии и фиксирует платежи
4. **`management-service`**: отслеживает свободные парковочные места, предоставляет API для информационных табло
5. **`reporting-service`**: собирает системные логи и формирует отчёты

### Функциональные требования
* **Автоматический режим:** свободный проезд для абонентов (распознавание номера). Выдача тикета/оплата для разовых посетителей
* **Ручной режим:** управление въездом/выездом оператором с расчётом стоимости (резервный режим при сбое автоматики)
* **Логирование:** полный журнал въездов/выездов и аудит-трейл действий операторов/администраторов
* **Безопасность:** ролевая аутентификация и авторизация (`ADMIN`, `OPERATOR`) через Spring Security

---

## 🛠️ Запуск проекта

### Через Docker Compose
Быстрое развёртывание всего стека (PostgreSQL, все микросервисы, фронтенд).

**Требования:** Docker и Docker Compose должны быть установлены.

1. **Сборка образов:**
   ```bash
   # Собрать все Java-сервисы
   cd backend
   mvn clean install

   # Собрать Docker-образы
   cd ..
   docker-compose build
   ```

2. **Запуск сервисов:**
   ```bash
   docker-compose up -d
   ```

3. **Проверка:**
   ```bash
   # Статус контейнеров
   docker-compose ps

   # Логи
   docker-compose logs -f api-gateway
   ```

### Точки доступа по умолчанию

| Сервис | Адрес | Учётные данные |
| :--- | :--- | :--- |
| **API Gateway** | `http://localhost:8086` | admin/parking123 |
| **Eureka Server** | `http://localhost:8761` | — |
| **Frontend Web UI** | `http://localhost:3000` | — |
| **PostgreSQL** | `localhost:5433` | postgres/postgres |
| **pgAdmin** | `http://localhost:5050` | admin@parking.com/admin |

---

## 🔒 Безопасность в продакшене

### Критически важные переменные окружения для продакшена:

```bash
# JWT (ОБЯЗАТЕЛЬНО 64+ символов)
JWT_SECRET=<СГЕНЕРИРУЙТЕ_НАДЁЖНЫЙ_64_СИМВОЛЬНЫЙ_СЕКРЕТ>
JWT_ACCESS_TOKEN_EXPIRATION=1800   # 30 минут
JWT_REFRESH_TOKEN_EXPIRATION=43200  # 12 часов

# БД с надёжными учётными данными
SPRING_DATASOURCE_PASSWORD=<НАДЁЖНЫЙ_ПАРОЛЬ_БД_32+ символа>

# Redis с аутентификацией
SPRING_REDIS_PASSWORD=<НАДЁЖНЫЙ_ПАРОЛЬ_REDIS>

# Продакшен-профиль
SPRING_PROFILES_ACTIVE=production

# Ограничение запросов (строже для продакшена)
RATE_LIMIT_MINUTE=30
BRUTE_FORCE_THRESHOLD=5
```

### Лучшие практики безопасности
- Используйте переменные окружения для всех секретов
- Включите HTTPS/TLS в продакшене
- Настройте правильное логирование и мониторинг
- Регулярные аудиты безопасности
- Обновляйте зависимости

---

## 📝 Планы развития

### Краткосрочные
- [ ] Интеграция с брокером сообщений (Kafka/RabbitMQ)
- [ ] Поддержка WebSocket для уведомлений в реальном времени
- [ ] Фронтенд-приложение (React/Angular)
- [ ] Завершение реализации всех микросервисов

### Среднесрочные
- [ ] Несколько типов подписок (день/ночь, ограниченный въезд)
- [ ] Мобильное приложение (iOS/Android)
- [ ] Расширенные отчёты и аналитика
- [ ] CI/CD-пайплайн (GitHub Actions)

### Долгосрочные
- [ ] Облачное развёртывание (AWS/GCP/Azure)
- [ ] Оркестрация Kubernetes
- [ ] Поддержка нескольких языков (i18n)
- [ ] Оптимизация парковки на основе ИИ

---

## 🤝 Участие в разработке

Приветствуются любые вклады в проект! Не стесняйтесь создавать Pull Request.

1. Сделайте форк репозитория
2. Создайте ветку для функциональности (`git checkout -b feature/МояФункция`)
3. Зафиксируйте изменения (`git commit -m 'Добавил МоюФункцию'`)
4. Отправьте в ветку (`git push origin feature/МояФункция`)
5. Откройте Pull Request

---

## 📄 Лицензия

Проект лицензирован по лицензии MIT — см. файл LICENSE.

## 📞 Поддержка

По вопросам и поддержке:
- 📧 Email: support@parking-system.com
- 💬 Issues: [GitHub Issues](https://github.com/your-repo/parking-system/issues)
- 📖 Документация: папка `/docs`

---

**Сделано с ❤️ с использованием Spring Boot и Docker**

