# Deployment Guide

## Table of Contents
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Flyway Migrations](#flyway-migrations)
- [Environment Configuration](#environment-configuration)
- [Docker Deployment](#docker-deployment)
- [Health Checks](#health-checks)
- [Troubleshooting](#troubleshooting)

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
# Start infrastructure (Postgres, Redis, Eureka)
cd devops
docker-compose -f docker-compose.infrastructure.yml up -d
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
- user_backup_codes
- clients
- vehicles
- subscriptions
- parking_events
- payments
- logs

**Additional tables** (from V2-V4 migrations):
- parking_lots
- parking_spaces
- bookings

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
Detect Pending Migrations (V1, V2, V3, V4...)
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
| `V0__baseline.sql` | Baseline marker |
| `V1__initial_schema.sql` | Core 8 tables |
| `V2__add_parking_lots.sql` | Parking facilities |
| `V3__add_parking_spaces.sql` | Individual spaces |
| `V4__add_bookings.sql` | Reservation system |

### Configuration

**Development** (`application-development.yml`):
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true   # Allow existing schema
    baseline-version: 0
    locations: classpath:db/migration
    schemas: public
```

**Production** (`application-production.yml`):
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: false  # Strict mode
    validate-on-migrate: true   # Verify checksums
    clean-disabled: true        # Prevent accidents
```

### Verify Migrations

```powershell
# Run test script
cd devops
.\test-flyway-migrations.ps1
```

Or manually:
```sql
SELECT installed_rank, version, description, success 
FROM flyway_schema_history 
ORDER BY installed_rank;
```

Expected output:
```
 installed_rank | version | description           | success 
----------------+---------+-----------------------+---------
              1 | 0       | baseline              | t
              2 | 1       | initial schema        | t
              3 | 2       | add parking lots      | t
              4 | 3       | add parking spaces    | t
              5 | 4       | add bookings          | t
```

---

## Environment Configuration

### Environment Variables

**Database:**
```bash
DB_HOST=parking_db
DB_PORT=5432
DB_NAME=parking_db
DB_USER=postgres
DB_PASSWORD=postgres
```

**Redis:**
```bash
REDIS_HOST=parking_redis
REDIS_PORT=6379
REDIS_PASSWORD=
```

**Eureka:**
```bash
EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
```

**Security:**
```bash
JWT_SECRET=ParkingSystemSecretKey2025VeryLongAndSecureKey123456789
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000
```

### Application Profiles

- `development` - Local development with relaxed security
- `production` - Strict security, no auto-schema changes

Switch profile:
```yaml
spring:
  profiles:
    active: development  # or production
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
cd devops
docker-compose -f docker-compose.infrastructure.yml up -d
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
docker-compose -f docker-compose.services.yml up -d
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

Check Eureka dashboard to ensure:
- ✅ API-GATEWAY is UP
- ✅ CLIENT-SERVICE is UP

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
2. Verify migrations: `.\test-flyway-migrations.ps1`
3. Review health checks
4. Check GitHub Issues

