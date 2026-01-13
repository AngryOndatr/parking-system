Markdown
# 🅿️ Parking System - Microservices Architecture

Modern parking lot management system built on microservices architecture using Spring Boot, Spring Cloud, and Docker.

## 🆕 Latest Updates

### 2026-01-13 - Phase 1 Complete: Tests & Documentation (Issue #22) ✅

✅ **Tests & Documentation - Phase 1 Complete** (Issue #22)
- ✅ Comprehensive test coverage (46+ test cases)
  - Client Service: 20+ tests (unit + MockMvc integration)
  - Management Service: 14+ tests (integration)
  - Reporting Service: 12+ tests (unit + integration)
- ✅ Service-level documentation complete
  - **NEW**: Client Service README (630+ lines)
  - Management Service README (307 lines)
  - Reporting Service README (434 lines)
- ✅ API examples for all endpoints (curl + PowerShell)
- ✅ Happy path + negative case coverage
- ✅ Local test scripts operational
- 📖 **Status Report**: [ISSUE_22_STATUS_REPORT.md](./docs/reports/ISSUE_22_STATUS_REPORT.md)
- 📖 **Client Service**: [README.md](./backend/client-service/README.md)

### 2026-01-13 - API Gateway Proxy Testing Infrastructure (Issue #21) ✅

✅ **API Gateway Proxy Verification Complete** (Issue #21)
- ✅ PowerShell smoke test script (test-proxy.ps1, 269 lines)
- ✅ Bash smoke test script (test-proxy.sh, 270 lines)
- ✅ 11 automated tests across all services
- ✅ Comprehensive API documentation (36 code examples)
- ✅ Management Service proxy verified (4 endpoints)
- ✅ Reporting Service proxy verified (5 endpoints)
- ✅ Client Service proxy verified (2 endpoints)
- ✅ JWT token forwarding tested
- ✅ Cross-platform support (Windows/Linux/Mac)
- 📖 **API Examples**: [API_GATEWAY_PROXY_EXAMPLES.md](./docs/API_GATEWAY_PROXY_EXAMPLES.md)
- 📖 **Testing Guide**: [devops/README.md](./devops/README.md)

### 2026-01-13 - Reporting Service JWT Authentication Complete (Issue #19) ✅

✅ **Reporting Service - Complete with JWT Authentication** (Issue #19)
- ✅ POST /api/reporting/log - Create log entries (JWT protected)
- ✅ GET /api/reporting/logs - Retrieve logs with filters (JWT protected)
- ✅ JWT Authentication integrated (JwtAuthenticationFilter, JwtTokenProvider, SecurityConfig)
- ✅ Jackson JsonNullable support for OpenAPI models
- ✅ Unified JWT secret across all microservices (768 bits, HS512 compliant)
- ✅ JSON metadata support with proper deserialization
- ✅ Comprehensive test coverage
- 🔧 **FIXED**: JWT signature mismatch (unified secrets in docker-compose.yml)
- 🔧 **FIXED**: JWT key too short (upgraded to 96 characters)
- 🔧 **FIXED**: Jackson JsonNullable deserialization error (added module)
- 📖 **Session Log**: [SESSION_DEVELOPMENT_2026-01-13.md](./docs/sessions/SESSION_DEVELOPMENT_2026-01-13.md)

### 2026-01-12 - Phase 1: Backend CRUD Implementation Started 🚀

✅ **Client Service - Full CRUD Complete** (Issue #16)
- ✅ Complete CRUD endpoints for Clients entity
- ✅ OpenAPI-first design with generated interfaces
- ✅ Comprehensive validation and error handling
- ✅ Unit and integration tests
- ✅ JWT-protected endpoints via API Gateway
- 📖 **Details:** [Phase 1 Week 1 Report](./docs/reports/PHASE_1_WEEK_1_REPORT.md#issue-16)

✅ **Client Service - Vehicles Management Complete** (Issue #17)
- ✅ Full CRUD for Vehicles linked to Clients
- ✅ License plate uniqueness enforcement
- ✅ Client-Vehicle relationship management
- ✅ Comprehensive test coverage
- 📖 **Details:** [Phase 1 Week 1 Report](./docs/reports/PHASE_1_WEEK_1_REPORT.md#issue-17)

✅ **Management Service - Parking Spaces API Complete** (Issue #18)
- ✅ GET /available - List all available parking spaces
- ✅ GET /available/count - Count of available spaces
- ✅ GET /available/lot/{id} - Available spaces by lot
- ✅ GET /search - Search with filters (type, status)
- ✅ Test data migration with 23 parking spaces
- ✅ API Gateway proxy endpoints configured
- 📖 **Details:** [Phase 1 Week 1 Report](./docs/reports/PHASE_1_WEEK_1_REPORT.md#issue-18)

### 2025-12-26 - Flyway Database Migrations

✅ **Flyway Database Migrations Implemented**
- ✅ Flyway configured and integrated into API Gateway
- ✅ 5 migrations created (V1-V5): initial schema, parking_lots, parking_spaces, bookings, test data
- ✅ Comprehensive database documentation created
- ✅ Deployment guide with migration instructions
- ✅ Test scripts for migration verification
- 📖 **Migration Guide:** [Database README](./database/README.md)
- 📖 **Deployment Guide:** [DEPLOYMENT_GUIDE.md](./docs/DEPLOYMENT_GUIDE.md)

### 2025-12-25 - Initial Setup

✅ **Infrastructure & Foundation**
- ✅ API Gateway with JWT authentication and security features
- ✅ Complete microservices implementation (Eureka, API Gateway, Client Service)
- ✅ PostgreSQL and Redis working and accessible
- ✅ Observability stack integrated (Prometheus, Grafana, Jaeger, OpenTelemetry)
- ✅ Docker Compose configuration optimized
- ✅ Correct password hashes configured (BCrypt)
- 📖 Complete development documentation: [SESSION_DEVELOPMENT_2025-12-25_EN.md](./docs/sessions/SESSION_DEVELOPMENT_2025-12-25_EN.md)

## 📈 Project Status & Roadmap

### Phase 0: Infrastructure & Foundation ✅ COMPLETE

| Task | Description | Status | Completion |
|------|-------------|--------|------------|
| 0.1 | **GitHub Setup** | ✅ Complete | 100% |
| 0.2 | **Docker Compose** | ✅ Complete | 100% |
| 0.3 | **PostgreSQL DDL** | ✅ Complete | 100% |
| 0.4 | **Spring Boot Services** | ✅ Complete | 100% |

📖 **Phase 0 Summary:** [PHASE_0_SUMMARY.md](./docs/reports/PHASE_0_SUMMARY.md)

### Phase 1: Basic Backend (In Progress - Week 1/3) 🔄

**Goal:** Implement CRUD operations and database connectivity for core services.

| Service | Task | Status | Issue |
|---------|------|--------|-------|
| **Client Service** | ✅ CRUD for CLIENTS | Complete | #16 |
| **Client Service** | ✅ CRUD for VEHICLES | Complete | #17 |
| **Client Service** | ⏳ GET /check (subscription) | Pending | - |
| **Management Service** | ✅ GET /available | Complete | #18 |
| **Management Service** | ⏳ POST /update (status) | Pending | - |
| **Reporting Service** | ✅ POST /log | Complete | #19 |
| **Database** | ✅ Flyway migrations | Complete | #20 |

**Progress:** 5/7 tasks complete (71%)

**What's Done:**
- ✅ Complete Client entity CRUD with validation
- ✅ Complete Vehicle entity CRUD with client linking
- ✅ Parking space availability queries (list, count, filter)
- ✅ Reporting service with JWT authentication and logging
- ✅ Database migrations verified (parking_spaces, logs tables)
- ✅ OpenAPI-first design pattern established
- ✅ Test data migrations (23 parking spaces)
- ✅ Comprehensive test coverage

**Next Steps:**
- ⏳ Implement subscription check endpoint
- ⏳ Implement parking space status update

**Week 1 Achievements:**
- 5 major issues completed (#16, #17, #18, #19, #20)
- 3 microservices enhanced (client-service, management-service, reporting-service)
- 15+ endpoints implemented and tested
- Production-ready OpenAPI contracts
- Database schema validated and documented

### Phase 1: Basic Backend (Ready to Start) 🟡

**Scope:** Implement core backend functionality for client management, parking space management, and basic reporting.

**Key Deliverables:**
- ✅ Issue templates prepared and ready for creation
- ⏳ CLIENT-SVC: Full CRUD for clients and vehicles + subscription check
- ⏳ MANAGEMENT-SVC: Parking space availability and status updates
- ⏳ REPORTING-SVC: System logging endpoint
- ⏳ Integration testing and API documentation

📖 **Issue Templates:** 
- [PHASE_1_COMPLETE_GUIDE.md](./docs/PHASE_1_COMPLETE_GUIDE.md) — **Start here!** Complete implementation guide
- [PHASE_1_ISSUES.md](./docs/PHASE_1_ISSUES.md) — Detailed issue templates
- [PHASE_1_ISSUES_QUICK_REF.md](./docs/PHASE_1_ISSUES_QUICK_REF.md) — Quick reference guide

**Automated Scripts:**
- `devops/create-phase1-issues.sh` (Linux/Mac)
- `devops/create-phase1-issues.ps1` (Windows)

**Timeline:** 3 weeks | **Issues:** Epic + 9 implementation tasks

## 🏗️ System Architecture

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

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21+
- Maven 3.8+

### Launch System
```bash
# Clone repository
git clone <repository-url>
cd parking-system

# Start all services
docker-compose up -d

# Check status
docker-compose ps
```

### Service Access
- **API Gateway**: http://localhost:8086
- **Eureka Server**: http://localhost:8761
- **Client Service**: http://localhost:8081 (via Gateway)
- **Management Service**: http://localhost:8083 (via Gateway)
- **Test Interface**: [devops/test-login.html](./devops/test-login.html) - Browser-based API tester
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686
- **pgAdmin**: http://localhost:5050 (admin@parking.com/admin)
- **PostgreSQL**: localhost:5433 (parking_db/postgres/postgres)

## 🔧 Microservices

### 1. API Gateway (Port 8086)
- Centralized entry point
- JWT authentication and authorization
- Request routing to microservices
- Security features:
  - Rate limiting (100 req/min per IP)
  - Brute force protection (5 failed attempts)
  - Suspicious IP detection
  - Security audit logging
- CORS and basic security
- Monitoring and metrics

📖 **Documentation**: See SESSION_DEVELOPMENT_2025-12-25_EN.md

### 2. Client Service (Port 8081)
- Client and vehicle management
- CRUD operations for clients and vehicles
- PostgreSQL database integration
- JWT authentication via API Gateway
- OpenAPI 3.0 specification

**Client Endpoints** (via API Gateway):
- `POST /api/clients` - Create client
- `GET /api/clients` - List all clients
- `GET /api/clients/{id}` - Get client by ID
- `PUT /api/clients/{id}` - Update client
- `DELETE /api/clients/{id}` - Delete client
- `GET /api/clients/search?phone={phone}` - Search by phone

**Vehicle Endpoints** (via API Gateway):
- `POST /api/clients/{clientId}/vehicles` - Create vehicle
- `GET /api/vehicles` - List all vehicles
- `GET /api/vehicles/{id}` - Get vehicle by ID
- `PUT /api/vehicles/{id}` - Update vehicle
- `DELETE /api/vehicles/{id}` - Delete vehicle

📖 **Implementation:** Issues #16, #17

### 3. Management Service (Port 8083)
- Parking space management and monitoring
- Real-time availability tracking
- Search and filtering capabilities
- PostgreSQL database integration
- OpenAPI 3.0 specification

**Parking Space Endpoints** (via API Gateway):
- `GET /api/management/spots` - List all parking spaces
- `GET /api/management/spots/available` - List available spaces
- `GET /api/management/spots/available/count` - Count available spaces
- `GET /api/management/spots/available/lot/{lotId}` - Available spaces by lot
- `GET /api/management/spots/search?type={type}&status={status}` - Search with filters

**Supported Space Types:**
- STANDARD, HANDICAPPED, ELECTRIC, VIP, COMPACT, OVERSIZED

**Supported Statuses:**
- AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, OUT_OF_SERVICE

📖 **Implementation:** Issue #18

### 4. Service Registry (Port 8761)
- Eureka Server for service discovery
- Microservice registration and discovery
- Health checks and monitoring

### 5. Observability Stack
- **Prometheus** (Port 9090) - Metrics collection
- **Grafana** (Port 3000) - Dashboards and visualization
- **Jaeger** (Port 16686) - Distributed tracing
- **OpenTelemetry Collector** (Port 4317/4318) - Telemetry collection

### 6. Database Management
- **PostgreSQL 16** (Port 5433) - Main database
- **pgAdmin 4** (Port 5050) - Database management UI
- **Redis 7** (Port 6379) - Caching and session storage

### 7. Planned Services
- **User Service** - System user management
- **Parking Service** - Extended parking lot management
- **Booking Service** - Parking space reservations
- **Payment Service** - Payment processing
- **Billing Service** - Billing and tariff plans
- **Gate Control Service** - Parking gate management
- **Reporting Service** - Reports and analytics

## 📊 Technology Stack

### Backend
- **Java 21** - Main programming language
- **Spring Boot 3.5.8** - Microservices framework
- **Spring Cloud 2025.0.0** - Microservices architecture
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **JWT (jjwt 0.12.6)** - Token-based authentication
- **MapStruct** - DTO to Entity mapping
- **Lombok** - Boilerplate code reduction

### Infrastructure
- **Docker & Docker Compose** - Containerization
- **PostgreSQL 16** - Main database
- **Redis 7** - Caching and session storage
- **Eureka Server** - Service Registry
- **Spring Cloud Gateway** - API Gateway
- **Maven** - Build system

### Observability
- **Prometheus** - Metrics collection
- **Grafana** - Monitoring dashboards
- **Jaeger** - Distributed tracing
- **OpenTelemetry** - Telemetry instrumentation

### Documentation & Testing
- **OpenAPI 3 / Swagger UI** - API documentation
- **JUnit 5** - Unit testing
- **Spring Boot Test** - Integration testing

## 🗄️ Database

### PostgreSQL Configuration
- **Database**: `parking_db`
- **Username**: `postgres`
- **Password**: `postgres`
- **Port**: `5433` (Docker), `5432` (local)

### Flyway Migrations

Database schema is managed using **Flyway** for version-controlled migrations.

**Migration Files:** `backend/api-gateway/src/main/resources/db/migration/`

| Version | File | Description | Tables |
|---------|------|-------------|--------|
| V0 | `V0__baseline.sql` | Baseline | - |
| V1 | `V1__initial_schema.sql` | Core schema | 8 tables |
| V2 | `V2__add_parking_lots.sql` | Parking facilities | parking_lots |
| V3 | `V3__add_parking_spaces.sql` | Parking spaces | parking_spaces |
| V4 | `V4__add_bookings.sql` | Reservations | bookings |

**Quick Commands:**
```powershell
# Test migrations
cd devops
.\test-flyway-migrations.ps1

# View migration history
docker exec parking_db psql -U postgres -d parking_db -c "SELECT * FROM flyway_schema_history;"
```

📖 **Complete Guide:** [Database README](./database/README.md)

### Data Schema
```sql
-- Users (for authentication)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    user_role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked_until TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    -- ... + 30 additional security fields
);

-- Clients
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    registered_at TIMESTAMP DEFAULT NOW()
);

-- Vehicles
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

## 🔑 Test Credentials

### For Development and Testing:

| Username | Password | Role | Email |
|----------|----------|------|-------|
| **admin** | `parking123` | ADMIN | admin@parking.com |
| **user** | `user1234` | USER | user@parking.com |
| **manager** | `manager123` | MANAGER | manager@parking.com |

**⚠️ IMPORTANT:** These credentials are for development only! For production, use strong passwords and environment variables.

### Quick Authentication Test:
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

## 🔧 Configuration

### Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/parking_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT Security
JWT_SECRET=<YOUR_64_CHAR_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION=3600
JWT_REFRESH_TOKEN_EXPIRATION=604800

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=development
```

### Docker Compose Services
```yaml
services:
  # Database
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: parking_db
      POSTGRES_USER: postgres  
      POSTGRES_PASSWORD: postgres
    ports: ["5433:5432"]

  # Cache
  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  # Service Registry
  eureka-server:
    build: ./backend/eureka-server
    ports: ["8761:8761"]

  # API Gateway  
  api-gateway:
    build: ./backend/api-gateway
    ports: ["8086:8080"]
    depends_on: [eureka-server, postgres, redis]

  # Client Service
  client-service:
    build: ./backend/client-service
    ports: ["8081:8080"] 
    depends_on: [postgres, eureka-server]
```

## 📚 Documentation

### Development History
- **[Session Logs](./docs/sessions/)** - Detailed development session logs
  - [2025-12-25 - Project Setup](./docs/sessions/SESSION_DEVELOPMENT_2025-12-25_EN.md)
  - [2026-01-03 - Issue #16 Client CRUD](./docs/sessions/SESSION_DEVELOPMENT_2026-01-03_EN.md)
  - [2026-01-04 - Issue #17 Vehicle CRUD](./docs/sessions/SESSION_DEVELOPMENT_2026-01-04_EN.md)
  - [2026-01-11 - Issue #18 Management API (Day 1)](./docs/sessions/SESSION_DEVELOPMENT_2026-01-11_EN.md)
  - [2026-01-12 - Issue #18 Finalization (Day 2)](./docs/sessions/SESSION_DEVELOPMENT_2026-01-12_EN.md)

### Phase Reports
- **[Phase 0 Summary](./docs/reports/PHASE_0_SUMMARY.md)** - Infrastructure foundation completion
- **[Phase 1 Week 1 Report](./docs/reports/PHASE_1_WEEK_1_REPORT.md)** - Basic backend CRUD progress

### Architecture and Security
- **[Authentication Architecture](./docs/AUTHENTICATION.md)** - JWT authentication system
- **[Security Architecture](./docs/SECURITY_ARCHITECTURE.md)** - Security features and components
- **[Observability Setup](./docs/OBSERVABILITY_SETUP.md)** - Monitoring and tracing

### Database & Deployment
- **[Database README](./database/README.md)** - Database schema and Flyway migrations guide
- **[Deployment Guide](./docs/DEPLOYMENT_GUIDE.md)** - Production deployment instructions
- **[Production Migration Process](./docs/PRODUCTION_MIGRATION_PROCESS.md)** - Safe migration deployment to production
- **[Production Config Quick Ref](./docs/PRODUCTION_CONFIG_QUICK_REF.md)** - Production Flyway configuration
- **[Migration Tasks](./docs/DATABASE_MIGRATION_TASKS_EN.md)** - Database migration task breakdown

### Project Management & Issues
- **[Phase 1 Complete Guide](./docs/PHASE_1_COMPLETE_GUIDE.md)** - 📖 **Complete Phase 1 implementation guide** (start here!)
- **[Phase 1 Issues](./docs/PHASE_1_ISSUES.md)** - Detailed GitHub issue templates for Phase 1 backend
- **[Phase 1 Quick Reference](./docs/PHASE_1_ISSUES_QUICK_REF.md)** - Quick guide for creating Phase 1 issues
- **[Issue Creation Guide](./docs/QUICK_GUIDE_ISSUES.md)** - General guide for creating GitHub issues
- **Automated Scripts**: `devops/create-phase1-issues.sh` and `devops/create-phase1-issues.ps1`

### Specialized Documentation
- **[DevOps README](./devops/README.md)** - Deployment instructions and scripts
- **[API Documentation](./docs/API-Gateway-Developer-Guide.md)** - API Gateway guide

## 🧪 Testing

### Automated Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Test all modules
mvn clean test -f pom.xml
```

### Manual Testing

#### Test Authentication
```bash
# Get JWT token
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}'

# Use token to access protected endpoint
curl -X GET http://localhost:8086/api/clients \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Health Checks
```bash
# API Gateway health
curl http://localhost:8086/actuator/health

# Client Service health
curl http://localhost:8081/actuator/health

# Eureka dashboard
open http://localhost:8761
```

### PowerShell Scripts for Testing
Located in `devops/` folder:
- `check-system.ps1` - Complete system health check
- `test-auth.ps1` - Authentication testing
- `test-client-service-via-gateway.ps1` - Proxy testing
- `full-rebuild.ps1` - Full rebuild and test

## 📚 API Documentation

### Interactive Documentation
- **Swagger UI**: http://localhost:8086/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8086/v3/api-docs

### Developer Guides
- **[API Gateway Developer Guide](./docs/API-Gateway-Developer-Guide.md)** - Complete guide for API Gateway
- **[Authentication Components](./docs/AUTHENTICATION_COMPONENTS.md)** - JWT authentication system
- **[Brute Force Protection](./docs/BRUTE_FORCE_PROTECTION.md)** - Security features

### Postman Collection
Available in `/docs` folder for easy API testing.

## 🎯 System Features

### Core Services
1. **`client-service`**: Manages client database and subscriptions, verifies their validity
2. **`gate-control-service`**: Receives events from scanners, decides on vehicle admission, manages barrier gates
3. **`billing-service`**: Calculates parking session costs and records payments
4. **`management-service`**: Tracks available parking spaces, provides API for information displays
5. **`reporting-service`**: Collects system logs and generates reports

### Functional Requirements
* **Automatic Mode:** Free access for subscribers (license plate recognition). Ticket issuance/payment for one-time visitors
* **Manual Mode:** Operator control of entry/exit and fee calculation (fallback for automation failure)
* **Logging:** Complete log of arrivals/departures and audit trail of operator/administrator actions
* **Security:** Role-based authentication and authorization (`ADMIN`, `OPERATOR`) using Spring Security

## 🛠️ Running the Project

### Using Docker Compose
Quick deployment of the entire stack (PostgreSQL, all microservices, and Frontend).

**Prerequisites:** Docker and Docker Compose installed.

1. **Build the Images:**
   ```bash
   # Build all Java services
   cd backend
   mvn clean install
   
   # Build Docker images
   cd ..
   docker-compose build
   ```

2. **Start the Services:**
   ```bash
   docker-compose up -d
   ```

3. **Verify Services:**
   ```bash
   # Check container status
   docker-compose ps
   
   # Check logs
   docker-compose logs -f api-gateway
   ```

### Default Access Points

| Service | Address | Credentials |
| :--- | :--- | :--- |
| **API Gateway** | `http://localhost:8086` | admin/parking123 |
| **Eureka Server** | `http://localhost:8761` | - |
| **Frontend Web UI** | `http://localhost:3000` | - |
| **PostgreSQL** | `localhost:5433` | postgres/postgres |
| **pgAdmin** | `http://localhost:5050` | admin@parking.com/admin |

## 💻 Development

### Project Structure

```
parking-system/
├── backend/              # Spring Boot microservices
│   ├── api-gateway/      # API Gateway with JWT auth
│   ├── client-service/   # Client management
│   ├── eureka-server/    # Service discovery
│   └── ...               # Other services
├── frontend/             # React web interface
├── devops/               # Docker files and scripts
│   ├── *.ps1             # PowerShell automation scripts
│   └── observability/    # Prometheus, Grafana configs
├── database/             # SQL scripts
│   └── init.sql          # Database initialization
└── docs/                 # Documentation
```

### Development Workflow

1. **Start Infrastructure:**
   ```bash
   cd devops
   docker-compose -f docker-compose.infrastructure.yml up -d
   ```

2. **Run Services Locally:**
   ```bash
   cd backend/api-gateway
   mvn spring-boot:run
   ```

3. **Run Tests:**
   ```bash
   mvn test
   ```

### Running Tests

To run all Unit and Integration tests:
```bash
cd backend
./mvnw test
```

## 🔒 Production Security

### Critical Environment Variables for Production:

```bash
# JWT Security (MUST be 64+ characters)
JWT_SECRET=<GENERATE_STRONG_64_CHAR_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION=1800   # 30 minutes
JWT_REFRESH_TOKEN_EXPIRATION=43200  # 12 hours

# Database with strong credentials
SPRING_DATASOURCE_PASSWORD=<STRONG_DB_PASSWORD_32_CHARS+>

# Redis with authentication
SPRING_REDIS_PASSWORD=<STRONG_REDIS_PASSWORD>

# Production profile
SPRING_PROFILES_ACTIVE=production

# Rate limiting (more restrictive for production)
RATE_LIMIT_MINUTE=30
BRUTE_FORCE_THRESHOLD=5
```

**📖 Complete security documentation:** See "Production Readiness" section in `SESSION_DEVELOPMENT_2025-12-25_EN.md`

### Security Best Practices
- Use environment variables for all secrets
- Enable HTTPS/TLS in production
- Implement proper logging and monitoring
- Regular security audits
- Keep dependencies updated

## 📝 Future Enhancements

### Short-term
- [ ] Integration with message broker (Kafka/RabbitMQ)
- [ ] WebSocket support for real-time notifications
- [ ] Frontend application (React/Angular)
- [ ] Complete all microservices implementation

### Mid-term
- [ ] Multiple subscription types (day/night, limited entry)
- [ ] Mobile application (iOS/Android)
- [ ] Advanced reporting and analytics
- [ ] CI/CD pipeline (GitHub Actions)

### Long-term
- [ ] Cloud deployment (AWS/GCP/Azure)
- [ ] Kubernetes orchestration
- [ ] Multi-language support (i18n)
- [ ] AI-powered parking optimization

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For questions and support:
- 📧 Email: support@parking-system.com
- 💬 Issues: [GitHub Issues](https://github.com/your-repo/parking-system/issues)
- 📖 Documentation: See `/docs` folder

---

**Made with ❤️ using Spring Boot and Docker**
