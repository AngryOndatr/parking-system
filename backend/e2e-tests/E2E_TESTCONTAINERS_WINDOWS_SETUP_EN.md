# E2E Testcontainers Setup on Windows 11 Home

## Problem
When running E2E tests with Testcontainers on Windows 11 Home Edition, the following error occurred:
`Could not find a valid Docker environment`.

## Solution

### 1. Docker Desktop Configuration
Add the following line to Docker Engine settings (Settings → Docker Engine):
```json
{
  "min-api-version": "1.24"
}
```

This resolves the `Status 400: BadRequestException` error when Testcontainers tries to connect to Docker.

**Important:** Restart Docker Desktop after applying this change.

### 2. Testcontainers Configuration
Create the file `C:\Users\<username>\.testcontainers.properties` with the following content:
```properties
# Testcontainers configuration for Windows with Docker Desktop
docker.host=npipe:////./pipe/docker_engine

# Enable container reuse for faster test runs (optional)
testcontainers.reuse.enable=false

# Ryuk configuration (automatic cleanup)
testcontainers.ryuk.disabled=false

# Timeouts
testcontainers.docker.client.timeout.duration=60s
testcontainers.pull.pause.timeout=120s
```

### 3. API Gateway Routes
Updated proxy controllers in API Gateway to use `/api/v1/*` instead of `/api/*` to match service contracts:
- `GateControlProxyController`: `/api/gate/*` → `/api/v1/gate/*`
- `BillingProxyController`: `/api/billing/*` → `/api/v1/billing/*` (added calculate, pay, status methods)

### 4. Docker Compose Dependencies
Removed circular dependency in `docker-compose-e2e.yml`:
- `client-service` no longer depends on `api-gateway`
- All services depend only on base services (postgres, redis, eureka-server)

### 5. OpenTelemetry Configuration
Added `e2e-test` profile in `application.yml` to fully disable OpenTelemetry:
```yaml
spring:
  config:
    activate:
      on-profile: e2e-test
  autoconfigure:
    exclude:
      - org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpAutoConfiguration
      - org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryAutoConfiguration

management:
  tracing:
    enabled: false
```

Used in `docker-compose-e2e.yml` as: `SPRING_PROFILES_ACTIVE: "prod-security,e2e-test"`

### 6. Testcontainers Timeouts
Increased timeouts in the test:
- Startup timeout for API Gateway: 3 minutes → 5 minutes
- Added `.withLocalCompose(false)` to use containerised docker-compose
- Increased service wait timeout to 5 minutes

## Known Issues

### API Gateway does not start in Testcontainers
**Symptom**: `Aborting attempt to link to container api-gateway as it is not running`

**Possible causes**:
1. OpenTelemetry tries to connect to the collector on initialisation
2. OpenTelemetry dependency version conflict
3. Insufficient timeout for the health check

**Current status**: Resolved. API Gateway passes the health check successfully.

## Recommendations
1. Make sure Docker Desktop is running before executing tests
2. Make sure all service images are built locally
3. Rebuild the relevant Docker images after changing service code
4. Use `mvn clean test -Dtest=OneTimeVisitorE2ETest` to run a specific test
5. For debugging, use `docker-compose -f docker-compose-e2e.yml up` for manual startup

## Debugging

### Check Docker
```powershell
# Check Docker connection
docker info

# Check API version
docker version

# Check image availability
docker images | Select-String -Pattern "api-gateway|client-service|gate-control-service"
```

### Manual docker-compose startup
```powershell
cd backend\e2e-tests

# Start base services
docker-compose -f docker-compose-e2e.yml up -d postgres redis eureka-server

# Wait 30 seconds
Start-Sleep -Seconds 30

# Start api-gateway and watch logs
docker-compose -f docker-compose-e2e.yml up api-gateway

# Check status
docker-compose -f docker-compose-e2e.yml ps

# Stop and remove
docker-compose -f docker-compose-e2e.yml down -v
```

### Check container logs
```powershell
# Find the container
docker ps -a | Select-String "api-gateway"

# View logs
docker logs <container_id>
```

## Status
1. ✅ Resolved: Testcontainers Docker connection issue
2. ✅ Resolved: API routes updated in proxy controllers
3. ✅ Resolved: Circular dependencies removed
4. ✅ Resolved: OpenTelemetry profile added for E2E
5. ✅ Resolved: API Gateway health check passes — E2E test is fully green

## Useful Links
- https://java.testcontainers.org/on_failure.html
- https://java.testcontainers.org/features/configuration/
- https://docs.docker.com/engine/api/
- https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.observability

