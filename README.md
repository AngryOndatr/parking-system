Markdown
# 🅿️ Parking System - Микросервисная архитектура

Современная система управления парковочными местами, построенная на микросервисной архитектуре с использованием Spring Boot, Spring Cloud и Docker.

## 🆕 Последние обновления (2025-12-20)

✅ **Система полностью развернута и работает!**

- ✅ Все 7 микросервисов собраны и запущены в Docker
- ✅ API Gateway с механизмом проксирования к Client Service
- ✅ PostgreSQL и Redis работают и доступны
- ✅ Исправлены ошибки сборки и кодировки
- 📖 Полная документация изменений: [SESSION_CHANGES_2025-12-20.md](./docs/SESSION_CHANGES_2025-12-20.md)
- 📊 Отчет о тестировании проксирования: [API_GATEWAY_PROXY_TEST_REPORT.md](./docs/API_GATEWAY_PROXY_TEST_REPORT.md)

## 🏗️ Архитектура системы

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│    Frontend     │───▶│   API Gateway    │───▶│  Microservices  │
│                 │    │                  │    │                 │
│ React/Angular   │    │ • Route Mapping  │    │ • Client Svc    │
│ Mobile App      │    │ • Load Balancer  │    │ • User Svc      │
│ Admin Panel     │    │ • CORS Handler   │    │ • Parking Svc   │
└─────────────────┘    │ • Monitoring     │    │ • Booking Svc   │
                       └──────────────────┘    │ • Payment Svc   │
                                │              │ • Billing Svc   │
                                ▼              │ • Gate Ctrl Svc │
                       ┌──────────────────┐    │ • Management    │
                       │ Service Registry │    │ • Reporting     │
                       │  (Eureka Server) │    └─────────────────┘
                       └──────────────────┘             │
                                                        ▼
                                               ┌─────────────────┐
                                               │   PostgreSQL    │
                                               │    Database     │
                                               └─────────────────┘
```

## 🚀 Быстрый старт

### Предварительные требования
- Docker & Docker Compose
- Java 21+
- Maven 3.8+

### Запуск системы
```bash
# Клонирование репозитория
git clone <repository-url>
cd parking-system

# Запуск всех сервисов
docker-compose up -d

# Проверка статуса
docker-compose ps
```

### Доступ к сервисам
- **API Gateway**: http://localhost:8086
- **Eureka Server**: http://localhost:8761
- **Client Service**: http://localhost:8081
- **PostgreSQL**: localhost:5432 (parking_db/postgres/postgres)

## ️ Микросервисы

### 1. API Gateway (Port 8086)
- Централизованная точка входа
- Маршрутизация запросов к микросервисам
- CORS и базовая безопасность
- Мониторинг и метрики

📖 **Документация**: [API Gateway Developer Guide](./docs/API-Gateway-Developer-Guide.md)

### 2. Client Service (Port 8081)
- Управление клиентами и их транспортными средствами
- CRUD операции для клиентов
- Интеграция с базой данных PostgreSQL
- HTTP Basic аутентификация

**Endpoints**:
- `GET /api/clients` - Список клиентов
- `POST /api/clients` - Создание клиента
- `GET /api/clients/{id}` - Получение клиента
- `PUT /api/clients/{id}` - Обновление клиента
- `DELETE /api/clients/{id}` - Удаление клиента

### 3. Service Registry (Port 8761)
- Eureka Server для service discovery
- Регистрация и обнаружение микросервисов
- Health checks и мониторинг

### 4. Планируемые сервисы
- **User Service** - Управление пользователями системы
- **Parking Service** - Управление парковками и местами
- **Booking Service** - Бронирование парковочных мест
- **Payment Service** - Обработка платежей
- **Billing Service** - Биллинг и тарифные планы
- **Gate Control Service** - Управление воротами парковки
- **Management Service** - Административные функции
- **Reporting Service** - Отчеты и аналитика

## 📊 Технологический стек

### Backend
- **Java 21** - Основной язык программирования
- **Spring Boot 3.5.8** - Фреймворк для микросервисов
- **Spring Cloud 2025.0.0** - Микросервисная архитектура
- **Spring Data JPA** - Работа с базой данных
- **MapStruct** - Маппинг между DTO и Entity
- **Lombok** - Уменьшение boilerplate кода

### Infrastructure
- **Docker & Docker Compose** - Контейнеризация
- **PostgreSQL 16** - Основная база данных
- **Eureka Server** - Service Registry
- **Spring Cloud Gateway** - API Gateway
- **Maven** - Система сборки

### Documentation & Testing
- **OpenAPI 3 / Swagger UI** - API документация
- **JUnit 5** - Unit тестирование
- **Spring Boot Test** - Integration тестирование

## 🗄️ База данных

### PostgreSQL Configuration
- **Database**: `parking_db`
- **Username**: `postgres`
- **Password**: `postgres`
- **Port**: `5432`

### Схема данных
```sql
-- Клиенты
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    registered_at TIMESTAMP DEFAULT NOW()
);

-- Транспортные средства  
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    make VARCHAR(100),
    model VARCHAR(100),
    color VARCHAR(50),
    vehicle_type VARCHAR(50)
);
```

## 🔧 Конфигурация

### Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/parking_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Application
SERVER_PORT=8080
```

### Docker Compose Services
```yaml
services:
  # Service Registry
  eureka-server:
    image: steeltoeoss/eureka-server:latest
    ports: ["8761:8761"]

  # Database
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: parking_db
      POSTGRES_USER: postgres  
      POSTGRES_PASSWORD: postgres
    ports: ["5432:5432"]

  # API Gateway  
  api-gateway:
    build: ./backend/api-gateway
    ports: ["8086:8080"]
    depends_on: [eureka-server]

  # Client Service
  client-service:
    build: ./backend/client-service
    ports: ["8081:8080"] 
    depends_on: [postgres, eureka-server]
```

## 📚 Документация

### Архитектура и безопасность
- **[API Gateway Developer Guide](./docs/API-Gateway-Developer-Guide.md)** - Разработчикам API Gateway

### Специализированная документация
- **[Database README](./database/README.md)** - Настройка и схема базы данных
- **[DevOps README](./devops/README.md)** - Инструкции по развертыванию

## 🧪 Тестирование

### Автоматические тесты
```bash
# Unit тесты
mvn test

# Integration тесты
mvn verify

# Тестирование всех модулей
mvn clean test -f pom.xml
```

### Ручное тестирование

#### HTTP файлы для тестирования
- [`client-service-test.http`](./client-service-test.http) - Client Service API

#### Примеры запросов
```bash
# Health check
curl http://localhost:8086/actuator/health

# Получение клиентов
curl -X GET http://localhost:8086/api/clients
```

## 📚 Документация

### API Documentation
- **Swagger UI**: http://localhost:8086/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8086/v3/api-docs
- **Postman Collection**: [Доступна в папке /docs](./docs/)

### Developer Guides
- [API Gateway Developer Guide](./docs/API-Gateway-Developer-Guide.md)
- [Client Service Documentation](./docs/Client-Service.md)
- [Database Schema](./docs/Database-Schema.md)

1.  **`client-service`**: Manages the client database and subscriptions, and verifies their validity.
2.  **`gate-control-service`**: Receives events from scanners, decides on vehicle admission, and manages the barrier gates (via an emulator).
3.  **`billing-service`**: Calculates the cost for one-time parking sessions and records payments.
4.  **`management-service`**: Tracks available parking spaces and provides an API for the external information display board.
5.  **`	`**: Collects and stores all system logs, and generates reports.

## 🔑 Functional Requirements

* **Automatic Mode:** Free access for subscribers (via license plate recognition). Ticket issuance/payment for one-time visitors.
* **Manual Mode:** Ability for operators to manually control entry/exit and calculate fees (in case of automation failure).
* **Logging:** A log of all arrivals/departures, and an audit trail of operator/administrator actions.
* **Security:** Role-based authentication and authorization (`ADMIN`, `OPERATOR`) using Spring Security.

## 🛠️ Running the Project (Docker Compose)

Use Docker Compose for the quick deployment of the entire stack (PostgreSQL, all microservices, and Frontend).

**Prerequisites:** Docker and Docker Compose must be installed.

1.  **Build the Images:**
    ```bash
    # Build all Java services and the React interface
    ./mvnw clean install  # or the corresponding command for your build tool
    docker-compose build
    ```
2.  **Start the Services:**
    ```bash
    docker-compose up -d
    ```

### Default Access Points:

| Service | Address |
| :--- | :--- |
| **Backend API Gateway** (if implemented) | `http://localhost:8080` |
| **Frontend Web UI** | `http://localhost:3000` |
| **PostgreSQL** | `localhost:5432` |

## 💻 Development and Testing

### Folder Structure

-   `backend/`: Spring Boot microservice code.
-   `frontend/`: React web interface code.
-   `devops/`: Dockerfiles and `docker-compose.yml`.
-   `database/`: Migration scripts (e.g., Flyway or Liquibase).

### Running Tests

To run all Unit and Integration tests:
```bash
cd backend
./mvnw test

📝 Future Enhancements
Integration with a message broker (Kafka/RabbitMQ) for asynchronous communication.
Implementation of various subscription types (day/night, limited entry count).
Cloud deployment (AWS/GCP/Azure).
