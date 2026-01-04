# DevOps - Docker Compose Configuration

Эта директория содержит конфигурации Docker Compose для развертывания системы parking-system.

## Структура файлов

### Docker Compose файлы

1. **docker-compose.infrastructure.yml** - Базовая инфраструктура
   - PostgreSQL (база данных)
   - Redis (кеш и сессии)
   - Eureka Server (service discovery)

2. **docker-compose.services.yml** - Микросервисы приложения
   - API Gateway
   - Client Service
   - Gate Control Service
   - Billing Service
   - Management Service
   - Reporting Service

3. **docker-compose.yml** - Полная конфигурация (legacy, для обратной совместимости)

### Скрипты управления

- **full-rebuild.ps1** - ⭐ **ПОЛНАЯ ПЕРЕСБОРКА** системы с нуля + автотесты всех эндпойнтов
- **start-system.ps1** - Запуск системы (infrastructure/services/all)
- **stop-system.ps1** - Остановка системы (infrastructure/services/all)
- **check-system.ps1** - Проверка статуса всех сервисов
- **start-full-system.ps1** - Запуск полной системы со всеми сервисами

### Интерактивные инструменты

- **test-login.html** - 🌐 **ИНТЕРАКТИВНЫЙ ТЕСТЕР** всех API эндпойнтов (веб-интерфейс)

### Документация

- **[TEST_LOGIN_README.md](TEST_LOGIN_README.md)** - 🌐 Руководство по использованию веб-тестера API
- **[DOCKER_COMPOSE_USAGE.md](DOCKER_COMPOSE_USAGE.md)** - 📖 Подробная инструкция по использованию Docker Compose
- **[OBSERVABILITY_README.md](OBSERVABILITY_README.md)** - 📊 Настройка мониторинга и трейсинга

## Быстрый старт

### ⭐ Вариант 0: Полная пересборка с нуля (рекомендуется для первого запуска)

```powershell
# ПОЛНАЯ АВТОМАТИЧЕСКАЯ ПЕРЕСБОРКА + ТЕСТИРОВАНИЕ
.\full-rebuild.ps1
```

**Что делает:**
- Останавливает и удаляет все контейнеры
- Очищает старые Docker образы
- Пересобирает все сервисы с Maven
- Последовательно запускает всю инфраструктуру
- Инициализирует базу данных
- **Автоматически тестирует все 11 эндпойнтов API** ✅
- Показывает детальную статистику и логи

**Время выполнения:** ~3-4 минуты

📖 Подробнее: [FULL_REBUILD_QUICK_REF.md](FULL_REBUILD_QUICK_REF.md)

---

### 🌐 Интерактивное тестирование API

После запуска системы откройте в браузере:
```
test-login.html
```

**Возможности:**
- 🔐 Авторизация с JWT токенами
- 👥 Тестирование всех Client API эндпойнтов (5 шт.)
- 🚗 Тестирование всех Vehicle API эндпойнтов (7 шт.)
- ⚡ Автоматический полный тест за один клик (11 эндпойнтов)
- 🎨 Удобный интерфейс с вкладками и цветовой индикацией

📖 Подробнее: [TEST_LOGIN_README.md](TEST_LOGIN_README.md)

---

### Вариант 1: Использование скрипта (для повседневной работы)

```powershell
# Запуск всей системы
.\start-system.ps1

# Только инфраструктура
.\start-system.ps1 infrastructure

# Только сервисы (если инфраструктура уже запущена)
.\start-system.ps1 services

# Остановка
.\stop-system.ps1

# Остановка с удалением данных
.\stop-system.ps1 -RemoveVolumes
```

### Вариант 2: Ручной запуск

```powershell
# Создание сети (один раз)
docker network create parking-network

# Запуск инфраструктуры
docker-compose -f docker-compose.infrastructure.yml up -d

# Ожидание запуска Eureka (30 сек)
Start-Sleep -Seconds 30

# Запуск сервисов
docker-compose -f docker-compose.services.yml up -d

# Проверка статуса
docker ps
```

## Endpoints

После запуска доступны следующие endpoints:

- **Eureka Server**: http://localhost:8761
- **API Gateway**: http://localhost:8086
- **Client Service**: http://localhost:8081
- **Gate Control Service**: http://localhost:8082
- **Billing Service**: http://localhost:8083
- **Management Service**: http://localhost:8084
- **Reporting Service**: http://localhost:8085

### Проверка работоспособности

```powershell
# Eureka Dashboard
curl http://localhost:8761

# API Gateway Health
curl http://localhost:8086/actuator/health

# Client Service через Gateway
curl http://localhost:8086/client-service/actuator/health
```

## Troubleshooting

### Проблемы с подключением к Eureka

```powershell
# Проверка логов Eureka
docker logs eureka-server

# Проверка регистрации сервисов
curl http://localhost:8761
```

### Проблемы с проксированием через Gateway

```powershell
# Проверка логов Gateway
docker logs api-gateway

# Проверка маршрутов
curl http://localhost:8086/actuator/gateway/routes
```

### Полная перезагрузка

```powershell
# Остановить все с удалением volumes
.\stop-system.ps1 -RemoveVolumes

# Очистка Docker
docker system prune -f

# Пересборка и запуск
docker-compose -f docker-compose.infrastructure.yml build --no-cache
docker-compose -f docker-compose.services.yml build --no-cache
.\start-system.ps1
```

## Технологии

- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway
