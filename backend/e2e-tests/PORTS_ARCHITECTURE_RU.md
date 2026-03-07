# Архитектура портов микросервисов

## 📊 Таблица портов

| Сервис | Порт контейнера | Порт хоста | Примечание |
|--------|----------------|------------|------------|
| **Инфраструктура** | | | |
| postgres | 5432 | 5432 | Стандартный порт PostgreSQL |
| redis | 6379 | 6379 | Стандартный порт Redis |
| eureka-server | 8761 | 8761 | Стандартный порт Eureka |
| **Микросервисы (Spring Boot — все на 8080)** | | | |
| api-gateway | 8080 | 8086 | Стандартный Spring Boot |
| client-service | 8080 | 8081 | Стандартный Spring Boot |
| gate-control-service | 8080 | 8082 | Стандартный Spring Boot |
| billing-service | 8080 | 8083 | Стандартный Spring Boot |
| reporting-service | 8080 | 8084 | ✅ Унифицирован |
| management-service | 8080 | 8085 | ✅ Унифицирован |

**Все микросервисы теперь используют стандартный порт 8080 внутри контейнера.**

## 🔍 Почему все сервисы на порту 8080?

**Все** Spring Boot микросервисы используют **порт 8080 по умолчанию**:
- api-gateway
- client-service
- gate-control-service
- billing-service
- reporting-service ✅ (исправлено)
- management-service ✅ (исправлено)

В docker-compose они маппятся следующим образом:
```yaml
api-gateway:
  ports: "8086:8080"  # Хост 8086 → Контейнер 8080

reporting-service:
  ports: "8084:8080"  # Хост 8084 → Контейнер 8080 ✅

management-service:
  ports: "8085:8080"  # Хост 8085 → Контейнер 8080 ✅
```

### Преимущества унификации:
- ✅ Более простая конфигурация
- ✅ Единообразие
- ✅ Меньше путаницы при отладке
- ✅ Проще добавлять новые сервисы
- ✅ Стандартный подход Spring Boot

## ⚠️ Важно для межсервисного взаимодействия

Внутри Docker сети сервисы **всегда** общаются используя **внутренний порт контейнера**:

```yaml
# ✅ Правильно — ВСЕ сервисы на порту 8080
CLIENT_SERVICE_URL: http://client-service:8080
BILLING_SERVICE_URL: http://billing-service:8080
REPORTING_SERVICE_URL: http://reporting-service:8080
MANAGEMENT_SERVICE_URL: http://management-service:8080

# ❌ Неправильно (порты хоста используются только снаружи Docker)
REPORTING_SERVICE_URL: http://reporting-service:8084
MANAGEMENT_SERVICE_URL: http://management-service:8085
```

## 📚 Связанные документы
- `docker-compose-e2e.yml` — конфигурация портов для E2E тестов
- `application.yml` каждого сервиса — определения портов

