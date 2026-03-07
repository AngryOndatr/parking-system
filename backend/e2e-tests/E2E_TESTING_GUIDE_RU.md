# E2E Tests - Руководство по запуску

## Обзор

E2E (End-to-End) тесты проверяют полный жизненный цикл парковочной системы, используя
Testcontainers для запуска всех микросервисов внутри Docker контейнеров.

## Предварительные требования

1. **Docker Desktop** должен быть запущен
2. **Java 21** установлена
3. **Maven 3.6+** установлен
4. Все Docker образы собраны (см. раздел "Сборка образов")

## Важно для Windows 11 Home Edition

### Проблема с Docker API

На Windows 11 Home Edition может возникнуть ошибка:
```
Could not find a valid Docker environment
```

**Решение:** Добавить настройку минимальной версии API в конфигурацию Docker Engine.

1. Откройте Docker Desktop
2. Settings → Docker Engine
3. Добавьте в JSON конфигурацию:
```json
{
  "min-api-version": "1.24",
  ...другие настройки...
}
```
4. Нажмите "Apply & Restart"

## Сборка Docker образов

Перед запуском тестов необходимо собрать все Docker образы:

```powershell
# Из корня проекта или из backend/e2e-tests
.\build-e2e-images.ps1
```

Скрипт:
- Собирает все микросервисы через Maven
- Создаёт Docker образы с правильными именами
- Проверяет успешность сборки

### Ручная сборка (если нужно)

```powershell
cd backend\eureka-server
mvn clean package -DskipTests
docker build -t eureka-server:latest .

cd ..\api-gateway
mvn clean package -DskipTests
docker build -t api-gateway:latest .

# Аналогично для остальных сервисов...
```

## Запуск E2E тестов

### Вариант 1: Maven (рекомендуется)

```powershell
cd backend\e2e-tests
mvn test
```

### Вариант 2: IntelliJ IDEA

1. Откройте `OneTimeVisitorE2ETest.java`
2. Правая кнопка мыши на классе или методе теста
3. Выберите "Run 'OneTimeVisitorE2ETest'"

**Важно:** Убедитесь, что в IntelliJ IDEA:
- Выбран JDK 21 для проекта
- Docker Desktop запущен

## Архитектура E2E тестов

### Используемые технологии

- **Testcontainers** — управление Docker контейнерами
- **JUnit 5** — тестовый фреймворк
- **RestAssured** — HTTP клиент для тестирования REST API
- **Awaitility** — ожидание готовности сервисов

### Структура теста

```
OneTimeVisitorE2ETest
├── @BeforeAll setup()                   - Запуск Docker Compose окружения
├── @Test oneTimeVisitorFullCycle()      - Полный сценарий парковки
│   ├── Шаг 1: Въезд (генерация тикета)
│   ├── Шаг 2: Попытка выезда без оплаты (должен быть отказ)
│   ├── Шаг 3: Проверка статуса оплаты
│   ├── Шаг 4: Оплата
│   ├── Шаг 5: Повторная проверка статуса
│   └── Шаг 6: Успешный выезд
└── @AfterAll tearDown()                 - Остановка контейнеров
```

### Docker Compose конфигурация

`docker-compose-e2e.yml` включает:
- PostgreSQL (база данных)
- Redis (кэш и blacklist токенов)
- Eureka Server (service discovery)
- API Gateway (точка входа)
- 5 микросервисов (client, gate-control, billing, reporting, management)

## Конфигурация E2E тестов

### Отключённые компоненты

В тестовом окружении отключены:
- ✅ **Spring Security** — аутентификация/авторизация отключены
- ✅ **OpenTelemetry** — трейсинг и метрики отключены
- ✅ **Flyway** — DB миграции отключены (используется JPA DDL auto)

Настраивается через профиль `e2e-test` в `application-e2e-test.yml`.

### Порты

Все сервисы используют внутренние порты (expose only), кроме api-gateway,
который маппится на случайный порт хоста Testcontainers.

## Устранение неполадок

### Проблема: "Container did not start correctly"

**Причина:** Порты уже заняты другими контейнерами

**Решение:**
```powershell
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)
mvn test
```

### Проблема: "Image not found"

**Причина:** Docker образы не собраны

**Решение:**
```powershell
.\build-e2e-images.ps1
```

### Проблема: "api-gateway is unhealthy"

**Причина:** OpenTelemetry пытается подключиться к отсутствующему коллектору

**Решение:** Убедитесь, что `application-e2e-test.yml` содержит:
```yaml
otel:
  sdk:
    disabled: true
  traces:
    exporter: none
```

## Добавление новых E2E тестов

1. Создайте новый класс в `src/test/java/com/parking/e2e/`
2. Используйте ту же структуру с `@BeforeAll`, `@Test`, `@AfterAll`
3. Переиспользуйте настройку `DockerComposeContainer` из `OneTimeVisitorE2ETest`
4. Добавьте сценарии тестирования через RestAssured

