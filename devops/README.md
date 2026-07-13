# DevOps — Docker Compose Configuration

This directory contains Docker Compose configurations for deploying the parking-system.

## File Structure

### Docker Compose Files

**`../docker-compose.yml`** (project root) — ⭐ **Primary** — full stack including observability  
All infrastructure + all services + Prometheus + Grafana + Jaeger + OTel Collector + pgAdmin

### Management Scripts

- **full-rebuild.ps1** — ⭐ **FULL REBUILD** from scratch + automated endpoint tests
- **start-system.ps1** — Start the system (infrastructure/services/all)
- **stop-system.ps1** — Stop the system (infrastructure/services/all)
- **check-system.ps1** — Check the status of all services
- **start-full-system.ps1** — Start the full system with all services
- **run-e2e-tests.ps1** — Run E2E tests (with Docker health check)

### Test Scripts

- **test-proxy.ps1** — 🧪 **API GATEWAY PROXY TESTS** — smoke tests for all proxy endpoints
- **test-proxy.sh** — 🧪 Bash version of proxy smoke tests (Linux/Mac)
- **test-login.html** — 🌐 Interactive web API tester with UI

### Interactive Tools

- **test-login.html** — 🌐 **INTERACTIVE TESTER** for all API endpoints (web interface)
  - **Client Management**: CRUD for clients, search by phone
  - **Vehicle Management**: CRUD for vehicles, link to clients
  - **Parking Space Management**: View available spots, search by parameters, count
  - **Quick Test Suite**: Automated testing of all endpoints in one click
  - JWT authentication support
  - Response visualisation with syntax highlighting

### Documentation

| Document | 🇬🇧 English | 🇷🇺 Русский |
|----------|-------------|-------------|
| Web API tester guide | [TEST_LOGIN_README_EN.md](TEST_LOGIN_README_EN.md) | [TEST_LOGIN_README_RU.md](TEST_LOGIN_README_RU.md) |
| Monitoring & tracing | [OBSERVABILITY_README_EN.md](OBSERVABILITY_README_EN.md) | [OBSERVABILITY_README_RU.md](OBSERVABILITY_README_RU.md) |

## Quick Start

### ⭐ Option 0: Full rebuild from scratch (recommended for first run)

```powershell
# FULL AUTOMATED REBUILD + TESTING
.\full-rebuild.ps1
```

**What it does:**
- Stops and removes all containers
- Cleans up old Docker images
- Rebuilds all services with Maven
- Starts the full infrastructure sequentially
- Initialises the database
- **Automatically tests all API endpoints** ✅
- Displays detailed statistics and logs

**Estimated time:** ~3–4 minutes

---

### 🧪 Running E2E Tests

```powershell
# Full build + E2E tests
.\run-e2e-tests.ps1

# Skip Maven build (images already built)
.\run-e2e-tests.ps1 -SkipBuild

# Skip Docker image build only
.\run-e2e-tests.ps1 -SkipDockerBuild
```

---

### 🌐 Interactive API Testing

After starting the system, open in a browser:
```
test-login.html
```

**Features:**
- 🔐 JWT authentication
- 👥 All Client API endpoints (5)
- 🚗 All Vehicle API endpoints (7)
- ⚡ Automated full test in one click (11 endpoints)
- 🎨 Tab-based interface with colour-coded status

---

### Option 1: Using a script (daily workflow)

```powershell
# Start the full system
.\start-system.ps1

# Stop
.\stop-system.ps1

# Stop and remove data
.\stop-system.ps1 -RemoveVolumes
```

### Option 2: Manual startup

```powershell
# From project root
docker-compose -f docker-compose.yml up -d

# Check status
docker ps
```

## Endpoints

After startup the following endpoints are available:

- **Eureka Server**: http://localhost:8761
- **API Gateway**: http://localhost:8086
- **Client Service**: http://localhost:8081
- **Gate Control Service**: http://localhost:8082
- **Billing Service**: http://localhost:8083
- **Management Service**: http://localhost:8084
- **Reporting Service**: http://localhost:8087
- **Grafana**: http://localhost:3000
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686
- **pgAdmin**: http://localhost:5050
- **React Frontend** (dev): http://localhost:5173

### Health Checks

```powershell
# Eureka Dashboard
curl http://localhost:8761

# API Gateway Health
curl http://localhost:8086/actuator/health

# Client Service via Gateway
curl http://localhost:8086/client-service/actuator/health
```

## Troubleshooting

### Eureka connection issues

```powershell
# Check Eureka logs
docker logs eureka-server

# Check registered services
curl http://localhost:8761
```

### Gateway proxy issues

```powershell
# Check Gateway logs
docker logs api-gateway

# Check routes
curl http://localhost:8086/actuator/gateway/routes
```

---

## 🧪 API Gateway Proxy Testing

### Automated Smoke Tests

For a quick check of all proxy endpoints:

**PowerShell (Windows):**
```powershell
.\test-proxy.ps1
```

**Bash (Linux/Mac):**
```bash
chmod +x test-proxy.sh
./test-proxy.sh
```

**What is tested:**
- ✅ Management Service proxy (4 endpoints)
  - GET /api/management/spots/available
  - GET /api/management/spots/available/count
  - GET /api/management/spots
  - GET /api/management/spots/search
- ✅ Reporting Service proxy (5 endpoints)
  - POST /api/reporting/log
  - GET /api/reporting/logs (with various filters)
- ✅ Client Service proxy (2 endpoints)
  - GET /api/clients
  - GET /api/vehicles

**Output:**
```
🧪 API Gateway Proxy Smoke Tests
============================================================
🔐 Step 1: Authenticating...
✅ Authentication successful

📦 Step 2: Testing Management Service Proxy
✅ Management Service Results: 4 passed, 0 failed

📊 Step 3: Testing Reporting Service Proxy
✅ Reporting Service Results: 5 passed, 0 failed

👥 Step 4: Testing Client Service Proxy
✅ Client Service Results: 2 passed, 0 failed

📋 Test Summary
Total: 11/11 passed

✅ All proxy tests passed!
```

### Manual Testing

curl and PowerShell examples for all endpoints:

📖 **See:** [API_GATEWAY_PROXY_EXAMPLES.md](../docs/API_GATEWAY_PROXY_EXAMPLES.md)

---

### Full Reset

```powershell
# Stop all with volume removal
.\stop-system.ps1 -RemoveVolumes

# Clean Docker
docker system prune -f

# Rebuild and start
.\full-rebuild.ps1
```

## Technologies

- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway
