# Deployment Guide

## Table of Contents
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Flyway Migrations](#flyway-migrations)
- [Environment Configuration](#environment-configuration)
- [Docker Deployment](#docker-deployment)
- [Health Checks](#health-checks)
- [Troubleshooting](#troubleshooting)

> 📖 **For Production Migration Process:** See [PRODUCTION_MIGRATION_PROCESS.md](./PRODUCTION_MIGRATION_PROCESS.md)

---

## Prerequisites

### Required Software
- Docker Desktop (v20.10+)
- Docker Compose (v2.0+)
- Java 21
- Maven 3.9+
- PostgreSQL 17 (via Docker)
- Git

### Optional Tools
- pgAdmin 4 (database management)
- Postman (API testing)

---

## Database Setup

### 1. Start Database Container

```powershell
# Start infrastructure (Postgres, Redis, Eureka) — run from project root
docker-compose -f docker-compose.yml up -d postgres redis eureka-server
```

### 2. Verify Database

```powershell
# Check database status
docker ps | findstr parking_db

# Connect to database
docker exec -it parking_db psql -U postgres -d parking_db

# List tables
\dt
```

### 3. Database Initialization

Database is initialized automatically via **Flyway migrations** when API Gateway starts.

**Initial tables** (from V1__initial_schema.sql):
- users
- clients
- vehicles
- subscriptions
- parking_events
- payments
- logs

**Additional tables** (from V2–V11 migrations):
- parking_lots (V2)
- parking_spaces (V3)
- bookings (V4)
- tariffs (V7)
- gate_events (V9)

---

## Flyway Migrations

### Overview

Flyway handles all database schema changes automatically on application startup.

### Migration Flow

```
Application Startup
    ↓
Flyway Initialization
    ↓
Check flyway_schema_history
    ↓
Detect Pending Migrations (V1 through V11)
    ↓
Execute Migrations Sequentially
    ↓
Update flyway_schema_history
    ↓
Application Ready
```

### Migration Files

Located in: `backend/api-gateway/src/main/resources/db/migration/`

| File | Description |
|------|-------------|
| `V0__Baseline.sql` | Baseline marker |
| `V1__initial_schema.sql` | Core tables: users, clients, vehicles, subscriptions, parking_events, payments, logs |
| `V2__add_parking_lots.sql` | Parking lot facilities |
| `V3__add_parking_spaces.sql` | Individual parking spaces |
| `V4__add_bookings.sql` | Reservation system |
| `V5__insert_test_parking_data.sql` | Dev/test seed data |
| `V6__extend_logs_table.sql` | Extended audit fields in logs |
| `V7__create_tariffs_table.sql` | Billing tariffs |
| `V8__extend_parking_events_and_payments.sql` | license_plate, entry/exit_method, is_subscriber, transaction_id |
| `V9__create_gate_events_table.sql` | gate_events table |
| `V10__extend_logs_audit_trail.sql` | action, entity_type, entity_id, client_id, license_plate in logs |
| `V11__add_parking_space_to_subscription.sql` | parking_space_id in subscriptions |

> **Next migration:** V12. Add new scripts to `backend/api-gateway/src/main/resources/db/migration/` only — **never edit an existing migration file**.

### Configuration

**Default (`application.yml`):**
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 1
    baseline-description: "Existing schema from init.sql"
    locations: classpath:db/migration
    schemas: public
    validate-on-migrate: true
```

**Production (`application-production.yml`):**
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: false  # Strict mode — no auto-baseline in production
    baseline-version: 0
    validate-on-migrate: true   # Verify checksums
    clean-disabled: true        # Prevent accidental database wipe
    out-of-order: false
    ignore-missing-migrations: false
```

### Verify Migrations

```powershell
# View migration history via Docker
docker exec parking_db psql -U postgres -d parking_db -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;"
```

Or via psql:
```sql
SELECT installed_rank, version, description, success 
FROM flyway_schema_history 
ORDER BY installed_rank;
```

Expected output (V0–V11):
```
 installed_rank | version | description                             | success 
----------------+---------+-----------------------------------------+---------
              1 | 0       | Baseline                                | t
              2 | 1       | initial schema                          | t
              3 | 2       | add parking lots                        | t
              4 | 3       | add parking spaces                      | t
              5 | 4       | add bookings                            | t
              6 | 5       | insert test parking data                | t
              7 | 6       | extend logs table                       | t
              8 | 7       | create tariffs table                    | t
              9 | 8       | extend parking events and payments      | t
             10 | 9       | create gate events table                | t
             11 | 10      | extend logs audit trail                 | t
             12 | 11      | add parking space to subscription       | t
```

---

## Environment Configuration

### Environment Variables

**Database:**
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://parking_db:5432/parking_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

**Redis:**
```bash
SPRING_REDIS_HOST=parking_redis
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=
```

**Eureka:**
```bash
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

**JWT (seconds-based, minimum 64-char secret for production):**
```bash
JWT_SECRET=<YOUR_64_CHAR_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION=1800    # 30 minutes
JWT_REFRESH_TOKEN_EXPIRATION=43200  # 12 hours
```

**Rate limiting & brute-force:**
```bash
RATE_LIMIT_MINUTE=60
BRUTE_FORCE_THRESHOLD=10
```

**Service-to-service URLs (gate-control-service):**
```bash
CLIENT_SERVICE_URL=http://client-service:8081
BILLING_SERVICE_URL=http://billing-service:8080
REPORTING_SERVICE_URL=http://reporting-service:8080
```

### Application Profiles

- `prod-security` — default, used in all environments (JWT auth + rate limiting active)
- `production` — additional production hardening (stricter Flyway, no SQL logging)

Active profile is set via:
```bash
SPRING_PROFILES_ACTIVE=prod-security          # default
SPRING_PROFILES_ACTIVE=prod-security,production  # production deployment
```

---

## Docker Deployment

### Full System Rebuild

```powershell
cd devops
.\full-rebuild.ps1
```

This script:
1. ✅ Stops and removes all containers
2. ✅ Removes volumes
3. ✅ Cleans Maven build
4. ✅ Rebuilds all services
5. ✅ Starts infrastructure (Postgres, Redis, Eureka)
6. ✅ Waits for database readiness
7. ✅ Initializes database (Flyway migrations)
8. ✅ Starts application services
9. ✅ Runs health checks
10. ✅ Verifies migrations

### Step-by-Step Deployment

#### 1. Build Services

```powershell
# Clean and build
cd backend
mvn clean install -DskipTests
```

#### 2. Start Infrastructure

```powershell
# From project root
docker-compose -f docker-compose.yml up -d postgres redis eureka-server pgadmin
```

Wait for services:
```powershell
# Check infrastructure health
docker ps
docker logs eureka-server
docker logs parking_db
```

#### 3. Start Application Services

```powershell
docker-compose -f docker-compose.yml up -d
```

#### 4. Verify Deployment

```powershell
# Check all containers
docker ps

# Check logs
docker logs api-gateway
docker logs client-service

# Test health endpoints
curl http://localhost:8086/actuator/health
curl http://localhost:8081/actuator/health
```

### Quick Commands

```powershell
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f [service-name]

# Restart specific service
docker-compose restart api-gateway

# Rebuild and restart
docker-compose up -d --build api-gateway
```

---

## CI/CD и GitHub Actions

В репозитории присутствует папка `.github/workflows/` с готовыми workflow для CI, CD и E2E:

- `ci.yml` — CI: выполняется на push и pull_request. Запускает `mvn test` (все модульные тесты) и для фронтенда — `npm ci`, `npm run lint`, `npm run build`.
- `cd.yml` — сборка образов и пуш в GHCR: сначала один `mvn clean package -DskipTests`, затем параллельная сборка/пуш Docker-образов для каждого сервиса и фронтенда. Триггер: push в `main` и семантические теги (`v*.*.*`). В job-е предусмотрен закомментированный `deploy` шаг по SSH — включайте его только после настройки секретов.
- `e2e.yml` — ручной запуск E2E (`workflow_dispatch`) с построением образов и запуском Testcontainers-based тестов. Используется для тяжёлых интеграционных проверок и требует runner с доступом к Docker.

Что нужно знать и настроить:

- Для пуша в GitHub Container Registry workflow использует `GITHUB_TOKEN`. GitHub Actions предоставляет этот токен автоматически — проверьте, что в настройках репозитория разрешена запись пакетов (`Settings → Actions → General` и `Settings → Packages`).
- Для автоматического deploy-а (раскомментированного job-а в `cd.yml`) добавьте в репозиторий секреты (`Settings → Secrets`):
  - `DEPLOY_HOST` — адрес сервера (IP или DNS);
  - `DEPLOY_USER` — SSH-пользователь;
  - `DEPLOY_SSH_KEY` — приватный SSH-ключ (в виде value, не файл);
  - `DEPLOY_PATH` — путь на сервере, где находится docker-compose и .env;
  Адреса и ключи не храните в репозитории в открытом виде.

- `ci.yml` задаёт тестовый `JWT_SECRET` для CI: это значение используется в тестах, где ожидается минимум 64 символов для HS512. Не используйте этот тестовый секрет в продакшене.

Рекомендации безопасности и workflow hygiene:

- Никогда не коммитьте приватные ключи и секреты. Удалите локальные файлы вроде `ssh key.txt` и добавьте соответствующие паттерны в `.gitignore`.
- Если вы хотите запускать `e2e.yml` в GitHub Actions, убедитесь, что runner имеет достаточные ресурсы и доступ к Docker (Testcontainers требует Docker socket или подходящий сервис).
- Предпочтительнее использовать GHCR для хранения артефактов образов и управлять релизами через теги (semver). `cd.yml` уже формирует теги: branch, sha, semver и `latest` для `main`.

Как включить deploy (шаги):

1. Добавьте секреты в GitHub (`DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_KEY`, `DEPLOY_PATH`).
2. Проверьте, что `docker compose` и `docker` настроены на сервере назначения и что `DEPLOY_USER` имеет права запускать docker-compose.
3. Раскомментируйте `deploy` job в `.github/workflows/cd.yml` и адаптируйте скрипт развертывания при необходимости (например, обновление `.env` с новыми тегами образов).
4. Push в `main` или создайте релиз тегом `vX.Y.Z` — workflow соберёт образы и запустит deploy.


---

## Health Checks

### Infrastructure Health

**Eureka Dashboard:**
```
http://localhost:8761
```

**Database:**
```bash
docker exec parking_db pg_isready -U postgres
```

**Redis:**
```bash
docker exec parking_redis redis-cli ping
```

### Application Health

**API Gateway:**
```bash
curl http://localhost:8086/actuator/health
```

**Client Service:**
```bash
curl http://localhost:8081/actuator/health
```

### Service Registration

Check Eureka dashboard to ensure all services are UP:
- ✅ API-GATEWAY (port 8086)
- ✅ CLIENT-SERVICE (port 8081)
- ✅ GATE-CONTROL-SERVICE (port 8082)
- ✅ BILLING-SERVICE (port 8083)
- ✅ MANAGEMENT-SERVICE (port 8084)
- ✅ REPORTING-SERVICE (port 8087)

---

## Troubleshooting

### Database Issues

**Problem:** Flyway migration failed

```sql
-- Check failed migrations
SELECT * FROM flyway_schema_history WHERE success = false;

-- Remove failed migration record
DELETE FROM flyway_schema_history WHERE version = 'X' AND success = false;

-- Restart API Gateway
docker-compose restart api-gateway
```

**Problem:** Database not ready

```powershell
# Wait for database
Start-Sleep -Seconds 10

# Check database logs
docker logs parking_db
```

**Problem:** Connection refused

```powershell
# Verify database is running
docker ps | findstr parking_db

# Check network
docker network ls
docker network inspect parking-system_parking-network
```

### Service Issues

**Problem:** Service not starting

```powershell
# Check logs
docker logs api-gateway --tail 100

# Check configuration
docker exec api-gateway env | findstr SPRING
```

**Problem:** Service not registered in Eureka

```powershell
# Check Eureka client configuration
docker logs api-gateway | findstr Eureka

# Verify network connectivity
docker exec api-gateway ping eureka-server
```

**Problem:** Authentication failing

```powershell
# Check users in database
docker exec parking_db psql -U postgres -d parking_db -c "SELECT id, username, enabled FROM users;"

# Verify password hash
docker exec parking_db psql -U postgres -d parking_db -c "SELECT username, password FROM users WHERE username='admin';"
```

### Migration Issues

**Problem:** Checksum mismatch

```sql
-- Check migration checksums
SELECT version, checksum, description 
FROM flyway_schema_history;

-- Solution: Don't modify existing migrations!
-- Create new migration instead
```

**Problem:** Missing migration file

```bash
# Verify migration files exist
ls backend/api-gateway/src/main/resources/db/migration/
```

**Problem:** Out of order migrations

```sql
-- Flyway enforces order
-- Solution: Use correct sequential numbering (V5, V6, V7...)
```

---

## Production Deployment Checklist

### Pre-Deployment

- [ ] All tests passing
- [ ] Migrations tested on staging
- [ ] Backup production database
- [ ] Review migration scripts
- [ ] Check resource limits
- [ ] Update environment variables
- [ ] SSL/TLS certificates ready

### Deployment Steps

1. **Backup Database**
   ```bash
   docker exec parking_db pg_dump -U postgres parking_db > backup_$(date +%Y%m%d).sql
   ```

2. **Stop Services** (if needed)
   ```bash
   docker-compose stop api-gateway client-service
   ```

3. **Pull Latest Changes**
   ```bash
   git pull origin main
   ```

4. **Build New Version**
   ```bash
   mvn clean install -DskipTests
   docker-compose build
   ```

5. **Start Services**
   ```bash
   docker-compose up -d
   ```

6. **Monitor Migrations**
   ```bash
   docker logs api-gateway -f
   ```

7. **Verify Health**
   ```bash
   curl http://localhost:8086/actuator/health
   ```

### Post-Deployment

- [ ] All services UP in Eureka
- [ ] Health checks passing
- [ ] Migrations successful
- [ ] API endpoints responding
- [ ] Monitor logs for errors
- [ ] Test critical flows

---

## Rollback Plan

### If Deployment Fails

1. **Stop new version**
   ```bash
   docker-compose down
   ```

2. **Restore database** (if migrations ran)
   ```bash
   docker exec -i parking_db psql -U postgres parking_db < backup_YYYYMMDD.sql
   ```

3. **Revert code**
   ```bash
   git checkout [previous-commit]
   docker-compose up -d
   ```

4. **Verify services**
   ```bash
   curl http://localhost:8086/actuator/health
   ```

---

## Performance Tuning

### Database

```sql
-- Enable query logging
ALTER DATABASE parking_db SET log_statement = 'all';

-- Analyze slow queries
SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;

-- Vacuum and analyze
VACUUM ANALYZE;
```

### JVM Settings

```yaml
# In docker-compose.yml
environment:
  JAVA_OPTS: >
    -Xms512m 
    -Xmx2048m 
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=200
```

---

## Monitoring

### Logs

```powershell
# All services
docker-compose logs -f

# Specific service
docker logs api-gateway -f --tail 100

# Database logs
docker logs parking_db -f
```

### Metrics

Access Prometheus metrics:
```
http://localhost:8086/actuator/prometheus
```

### Health Endpoints

```
http://localhost:8086/actuator/health
http://localhost:8086/actuator/info
http://localhost:8086/actuator/metrics
```

---

## Security

### Production Checklist

- [ ] Change default passwords
- [ ] Use strong JWT secret
- [ ] Enable HTTPS
- [ ] Configure CORS properly
- [ ] Enable rate limiting
- [ ] Set up firewall rules
- [ ] Use secrets management
- [ ] Enable audit logging

### Environment Secrets

**Never commit:**
- Database passwords
- JWT secrets
- API keys
- Certificates

Use:
- Docker secrets
- Environment variables
- External secret managers (AWS Secrets Manager, Azure Key Vault)

---

## Additional Resources

- [Database Documentation](../database/README.md)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)

---

## Support

For deployment issues:
1. Check logs: `docker logs [service-name]`
2. Verify migrations: `docker exec parking_db psql -U postgres -d parking_db -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;"`
3. Review health checks
4. Check GitHub Issues

