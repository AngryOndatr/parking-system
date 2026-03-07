# E2E Tests - Running Guide

## Overview

E2E (End-to-End) tests verify the complete parking system lifecycle using Testcontainers to launch
all microservices inside Docker containers.

## Prerequisites

1. **Docker Desktop** must be running
2. **Java 21** installed
3. **Maven 3.6+** installed
4. All Docker images built (see "Building Images" section)

## Important for Windows 11 Home Edition

### Docker API Issue

On Windows 11 Home Edition you may encounter:
```
Could not find a valid Docker environment
```

**Fix:** Add the minimum API version setting to Docker Engine configuration.

1. Open Docker Desktop
2. Settings → Docker Engine
3. Add the following to the JSON config:
```json
{
  "min-api-version": "1.24",
  ...other settings...
}
```
4. Click "Apply & Restart"

## Building Docker Images

Build all Docker images before running the tests:

```powershell
# From the project root or from backend/e2e-tests
.\build-e2e-images.ps1
```

This script:
- Builds all microservices with Maven
- Creates Docker images with the correct names
- Verifies that the build succeeded

### Manual Build (if needed)

```powershell
cd backend\eureka-server
mvn clean package -DskipTests
docker build -t eureka-server:latest .

cd ..\api-gateway
mvn clean package -DskipTests
docker build -t api-gateway:latest .

# Repeat for all services...
```

## Running E2E Tests

### Option 1: Maven (recommended)

```powershell
cd backend\e2e-tests
mvn test
```

### Option 2: IntelliJ IDEA

1. Open `OneTimeVisitorE2ETest.java`
2. Right-click on the class or test method
3. Select "Run 'OneTimeVisitorE2ETest'"

**Important:** Make sure IntelliJ IDEA has:
- JDK 21 selected for the project
- Docker Desktop running

## E2E Test Architecture

### Technologies Used

- **Testcontainers** — Docker container management
- **JUnit 5** — testing framework
- **RestAssured** — HTTP client for REST API testing
- **Awaitility** — waiting for service readiness

### Test Structure

```
OneTimeVisitorE2ETest
├── @BeforeAll setup()                   - Starts Docker Compose environment
├── @Test oneTimeVisitorFullCycle()      - Full parking cycle scenario
│   ├── Step 1: Entry (ticket generation)
│   ├── Step 2: Exit without payment (should be denied)
│   ├── Step 3: Check payment status
│   ├── Step 4: Process payment
│   ├── Step 5: Re-check payment status
│   └── Step 6: Successful exit
└── @AfterAll tearDown()                 - Stops containers
```

### Docker Compose Configuration

`docker-compose-e2e.yml` includes:
- PostgreSQL (database)
- Redis (cache and token blacklist)
- Eureka Server (service discovery)
- API Gateway (entry point)
- 5 microservices (client, gate-control, billing, reporting, management)

## E2E Test Configuration

### Disabled Components

The following are disabled in the E2E test environment:
- ✅ **Spring Security** — authentication/authorisation disabled
- ✅ **OpenTelemetry** — tracing and metrics disabled
- ✅ **Flyway** — DB migrations disabled (using JPA DDL auto)

Configured via the `e2e-test` profile in `application-e2e-test.yml`.

### Ports

All services use internal ports (expose only), except api-gateway which is mapped to a random host
port by Testcontainers.

## Troubleshooting

### Problem: "Container did not start correctly"

**Cause:** Ports already occupied by other containers

**Fix:**
```powershell
# Stop all containers
docker stop $(docker ps -aq)

# Remove containers
docker rm $(docker ps -aq)

# Run the test again
mvn test
```

### Problem: "Image not found"

**Cause:** Docker images not built

**Fix:**
```powershell
.\build-e2e-images.ps1
```

### Problem: "api-gateway is unhealthy"

**Cause:** OpenTelemetry tries to connect to a missing collector

**Fix:** Verify that `application-e2e-test.yml` contains:
```yaml
otel:
  sdk:
    disabled: true
  traces:
    exporter: none
```

### Problem: Test hangs on "Waiting for services"

**Cause:** Services cannot start or health check fails

**Fix:**
```powershell
# Check container logs
docker logs <container_id>

# Or run compose manually for debugging
cd backend\e2e-tests
docker-compose -f docker-compose-e2e.yml up
```

## API Endpoints (for manual testing)

When Testcontainers is running, the API Gateway is available at `http://localhost:<random-port>`.

### Gate Control Service

```bash
# Entry
POST /api/v1/gate/entry
{
  "licensePlate": "ABC-1234",
  "entryMethod": "SCAN",
  "gateId": "ENTRY-1"
}

# Exit
POST /api/v1/gate/exit
{
  "licensePlate": "ABC-1234",
  "ticketCode": "TKT-...",
  "exitMethod": "SCAN",
  "gateId": "EXIT-1"
}
```

### Billing Service

```bash
# Get status
GET /api/v1/billing/status?parkingEventId=123

# Pay
POST /api/v1/billing/pay
{
  "parkingEventId": 123,
  "amount": 100.0,
  "paymentMethod": "CARD",
  "transactionId": "TXN-..."
}
```

## Adding New E2E Tests

1. Create a new class in `src/test/java/com/parking/e2e/`
2. Use the same structure with `@BeforeAll`, `@Test`, `@AfterAll`
3. Reuse the `DockerComposeContainer` setup from `OneTimeVisitorE2ETest`
4. Add test scenarios using RestAssured

Example:
```java
@Test
void subscriberParkingCycle() {
    // Create a client
    // Add a vehicle
    // Create a subscription
    // Enter
    // Exit (no payment — subscription is active)
}
```

## Useful Commands

```powershell
# Check running containers
docker ps

# Check logs
docker logs <container_name>

# Stop all
docker stop $(docker ps -aq)

# Remove everything (use with caution!)
docker system prune -a --volumes

# Check images
docker images | findstr "parking\|eureka\|api-gateway"

# Run a single test
mvn test -Dtest=OneTimeVisitorE2ETest#oneTimeVisitorFullCycle
```

## Additional Resources

- OpenAPI contracts: `docs/api-contracts.md`
- Security architecture: `docs/SECURITY_ARCHITECTURE.md`
- Deployment guide: `docs/DEPLOYMENT_GUIDE.md`
