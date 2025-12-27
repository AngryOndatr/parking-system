# Фаза 1: План GitHub Issues

Этот документ содержит структурированную декомпозицию задач Фазы 1 для создания GitHub Issues.

## Epic: Фаза 1 - Базовый Backend (3 Недели)

**Описание Epic:**
Реализация CRUD-операций и подключения к БД для Client Service, Management Service и Reporting Service.

---

## Issue 1: Client Service - CRUD операции для Vehicle

**Заголовок:** Реализация CRUD endpoints для Vehicle в Client Service

**Метки:** `enhancement`, `backend`, `client-service`, `phase-1`

**Описание:**
Реализовать полные CRUD операции для сущности Vehicle в Client Service.

**Критерии приемки:**
- [ ] Создан VehicleRepository с необходимыми query методами
- [ ] VehicleService реализует бизнес-логику для CRUD операций
- [ ] VehicleController предоставляет REST endpoints:
  - `GET /api/clients/{clientId}/vehicles` - Список всех машин клиента
  - `POST /api/clients/{clientId}/vehicles` - Создание новой машины
  - `GET /api/clients/{clientId}/vehicles/{vehicleId}` - Получение машины по ID
  - `PUT /api/clients/{clientId}/vehicles/{vehicleId}` - Обновление машины
  - `DELETE /api/clients/{clientId}/vehicles/{vehicleId}` - Удаление машины
- [ ] Реализована корректная обработка ошибок
- [ ] Написаны unit тесты для service слоя
- [ ] Написаны интеграционные тесты для controller

**Зависимости:** Нет

**Оценка:** 5 story points (3 дня)

---

## Issue 2: Client Service - Endpoint проверки абонемента

**Заголовок:** Реализация endpoint для проверки абонемента в Client Service

**Метки:** `enhancement`, `backend`, `client-service`, `phase-1`

**Описание:**
Реализовать endpoint для проверки наличия активного абонемента у клиента.

**Критерии приемки:**
- [ ] Создана сущность Subscription в parking-common
- [ ] Создан SubscriptionRepository с query для активного абонемента
- [ ] SubscriptionService реализует логику валидации абонемента
- [ ] SubscriptionController предоставляет endpoint:
  - `GET /api/clients/{clientId}/subscriptions/check`
- [ ] Ответ включает детали абонемента, если он активен
- [ ] Написаны unit тесты
- [ ] Написаны интеграционные тесты

**Зависимости:** Нет

**Оценка:** 3 story points (2 дня)

---

## Issue 3: Management Service - Основная реализация

**Заголовок:** Реализация Management Service с отслеживанием доступности парковочных мест

**Метки:** `enhancement`, `backend`, `management-service`, `phase-1`, `new-service`

**Описание:**
Создать Management Service для отслеживания доступности парковочных мест и предоставления management API.

**Критерии приемки:**
- [ ] Созданы сущности ParkingLot и ParkingSpace в parking-common
- [ ] Создана структура сервиса с Spring Boot
- [ ] Добавлена конфигурация БД (PostgreSQL)
- [ ] Настроена регистрация Eureka client
- [ ] Добавлен SecurityConfig
- [ ] Созданы репозитории: ParkingLotRepository, ParkingSpaceRepository
- [ ] ManagementService реализует бизнес-логику
- [ ] ManagementController предоставляет endpoints:
  - `GET /api/management/available` - Получить доступные места
  - `GET /api/management/summary` - Получить сводку по доступности
  - `POST /api/management/spaces/{id}/update` - Обновить статус места
- [ ] Создан Dockerfile
- [ ] Написаны unit тесты
- [ ] Написаны интеграционные тесты

**Зависимости:** Нет

**Оценка:** 8 story points (5 дней)

---

## Issue 4: Reporting Service - Основная реализация

**Заголовок:** Реализация Reporting Service для логирования системы

**Метки:** `enhancement`, `backend`, `reporting-service`, `phase-1`, `new-service`

**Описание:**
Создать Reporting Service для сбора и хранения системных логов с API для получения данных.

**Критерии приемки:**
- [ ] Создана сущность Log в parking-common
- [ ] Создана структура сервиса с Spring Boot
- [ ] Добавлена конфигурация БД (PostgreSQL)
- [ ] Настроена регистрация Eureka client
- [ ] Добавлен SecurityConfig
- [ ] Создан LogRepository с запросами по временным диапазонам
- [ ] ReportingService реализует логику логирования
- [ ] ReportingController предоставляет endpoints:
  - `POST /api/reporting/log` - Создать лог
  - `GET /api/reporting/logs` - Получить логи с фильтрами
  - `GET /api/reporting/logs/errors` - Получить недавние ошибки
- [ ] Создан Dockerfile
- [ ] Написаны unit тесты
- [ ] Написаны интеграционные тесты

**Зависимости:** Нет

**Оценка:** 8 story points (5 дней)

---

## Issue 5: Docker Compose - Интеграция сервисов

**Заголовок:** Добавить Management и Reporting сервисы в Docker Compose

**Метки:** `infrastructure`, `docker`, `phase-1`

**Описание:**
Обновить docker-compose.yml для включения новых сервисов Management и Reporting.

**Критерии приемки:**
- [ ] Добавлен контейнер management-service с:
  - Корректным маппингом портов (8083:8083)
  - Настроенным подключением к БД
  - Настроенным подключением к Eureka
  - Настроенным health check
  - Настроенным OpenTelemetry tracing
- [ ] Добавлен контейнер reporting-service с:
  - Корректным маппингом портов (8084:8084)
  - Настроенным подключением к БД
  - Настроенным подключением к Eureka
  - Настроенным health check
  - Настроенным OpenTelemetry tracing
- [ ] Сервисы запускаются с `docker-compose up`
- [ ] Сервисы успешно регистрируются в Eureka
- [ ] Health checks проходят

**Зависимости:** Issue 3, Issue 4

**Оценка:** 2 story points (1 день)

---

## Issue 6: API Gateway - Настройка маршрутизации

**Заголовок:** Настроить маршруты API Gateway для Management и Reporting сервисов

**Метки:** `enhancement`, `backend`, `api-gateway`, `phase-1`

**Описание:**
Добавить конфигурацию маршрутизации в API Gateway для перенаправления запросов к Management и Reporting сервисам.

**Критерии приемки:**
- [ ] Добавлены маршруты для Management Service:
  - `/api/management/**` → management-service
- [ ] Добавлены маршруты для Reporting Service:
  - `/api/reporting/**` → reporting-service
- [ ] Service discovery работает через Eureka
- [ ] Настроен load balancing
- [ ] Настроены timeout и retry policies
- [ ] Маршруты протестированы вручную
- [ ] Обновлена документация

**Зависимости:** Issue 3, Issue 4, Issue 5

**Оценка:** 3 story points (2 дня)

---

## Issue 7: Интеграционное тестирование - E2E сценарии

**Заголовок:** Создать интеграционные тесты для функциональности Фазы 1

**Метки:** `testing`, `integration-test`, `phase-1`

**Описание:**
Создать комплексные интеграционные тесты, покрывающие всю функциональность Фазы 1.

**Тестовые сценарии:**
- [ ] CRUD операции Vehicle через API Gateway
- [ ] Проверка абонемента через API Gateway
- [ ] Запросы доступности парковочных мест
- [ ] Обновление статуса парковочных мест
- [ ] Создание и получение логов
- [ ] Service discovery и load balancing
- [ ] Подключение к БД для всех сервисов
- [ ] Сценарии обработки ошибок

**Зависимости:** Issue 1, Issue 2, Issue 3, Issue 4, Issue 6

**Оценка:** 5 story points (3 дня)

---

## Issue 8: OpenAPI Документация

**Заголовок:** Добавить OpenAPI спецификации для новых endpoints

**Метки:** `documentation`, `api`, `phase-1`

**Описание:**
Создать OpenAPI/Swagger документацию для всех новых API endpoints.

**Критерии приемки:**
- [ ] OpenAPI спецификация для Vehicle endpoints
- [ ] OpenAPI спецификация для Subscription endpoint
- [ ] OpenAPI спецификация для Management Service endpoints
- [ ] OpenAPI спецификация для Reporting Service endpoints
- [ ] Включены примеры request/response
- [ ] Задокументированы ответы с ошибками
- [ ] Swagger UI доступен для каждого сервиса
- [ ] Создана Postman коллекция

**Зависимости:** Issue 1, Issue 2, Issue 3, Issue 4

**Оценка:** 3 story points (2 дня)

---

## Issue 9: Мониторинг и Наблюдаемость

**Заголовок:** Настроить мониторинг и дашборды для сервисов Фазы 1

**Метки:** `observability`, `monitoring`, `phase-1`

**Описание:**
Настроить сбор метрик Prometheus и дашборды Grafana для новых сервисов.

**Критерии приемки:**
- [ ] Prometheus собирает метрики Management Service
- [ ] Prometheus собирает метрики Reporting Service
- [ ] Создан дашборд Grafana для Management Service:
  - Доступные парковочные места во времени
  - Распределение статусов мест
  - Время ответа API
- [ ] Создан дашборд Grafana для Reporting Service:
  - Объем логов по уровням
  - Частота ошибок во времени
  - Время ответа API
- [ ] OpenTelemetry traces видны в Jaeger
- [ ] Настроены алерты для критических метрик

**Зависимости:** Issue 3, Issue 4, Issue 5

**Оценка:** 5 story points (3 дня)

---

## Issue 10: Обновление документации

**Заголовок:** Обновить документацию проекта для Фазы 1

**Метки:** `documentation`, `phase-1`

**Описание:**
Обновить README и другую документацию с учетом изменений Фазы 1.

**Критерии приемки:**
- [ ] README.md обновлен:
  - Описания новых сервисов
  - Новые API endpoints
  - Обновленная диаграмма архитектуры
  - Обновленный quick start guide
- [ ] PHASE_1_IMPLEMENTATION_SUMMARY.md проверен и финализирован
- [ ] Обновлен developer guide
- [ ] Обновлен deployment guide с новыми сервисами
- [ ] Обновлена документация переменных окружения

**Зависимости:** Все остальные issues

**Оценка:** 2 story points (1 день)

---

## Итого

**Общая оценка:** 44 story points (~4-5 недель с запасом)

**Разбивка по категориям:**
- Backend разработка: 24 points (Issues 1-4)
- Инфраструктура: 5 points (Issues 5-6)
- Тестирование: 5 points (Issue 7)
- Документация: 5 points (Issues 8, 10)
- Наблюдаемость: 5 points (Issue 9)

**Критический путь:**
1. Issues 1-4 можно выполнять параллельно
2. Issue 5 зависит от Issues 3-4
3. Issue 6 зависит от Issue 5
4. Issue 7 зависит от всех issues реализации
5. Issues 8-10 можно выполнять параллельно после реализации

**Рекомендуемая структура спринтов:**
- **Спринт 1 (Неделя 1):** Issues 1, 2, 3, 4
- **Спринт 2 (Неделя 2):** Issues 5, 6, 8
- **Спринт 3 (Неделя 3):** Issues 7, 9, 10

---

## Создание Issues в GitHub

Для создания этих issues в GitHub:

1. Перейдите в ваш репозиторий
2. Нажмите на вкладку "Issues"
3. Нажмите "New Issue"
4. Скопируйте заголовок и описание из каждого issue выше
5. Добавьте указанные метки
6. Установите оценку (если используете story points)
7. Добавьте к milestone (например, "Phase 1 - Basic Backend")
8. Назначьте на членов команды

Альтернативно, можно использовать GitHub CLI для программного создания issues:

```bash
gh issue create --title "ЗАГОЛОВОК" --body "ОПИСАНИЕ" --label "label1,label2"
```

Или использовать REST API GitHub для пакетного создания issues.
