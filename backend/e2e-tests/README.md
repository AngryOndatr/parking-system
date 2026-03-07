﻿# E2E Tests - Parking System
End-to-end integration tests for the Parking System using **Testcontainers**.
**Status:** ✅ **Ready for Use** (Updated 2026-02-14)
---
## 🚀 Quick Start (3 Steps)
### 1. Docker Setup (One-time for Windows 11 Home)
```bash
Docker Desktop → Settings → Docker Engine
Add: "min-api-version": "1.24"
Apply & Restart
```
### 2. Build Images
```powershell
.\build-e2e-images.ps1
```
### 3. Run Tests
```powershell
mvn test
```
---
## 📋 Prerequisites
- ✅ **Docker Desktop** running
- ✅ **Java 21** installed
- ✅ **Maven 3.6+** installed
- ✅ **Docker Engine** configured (see step 1 above)
---
## 🧪 Test Scenarios
### OneTimeVisitorE2ETest
**Scenario:** Complete parking cycle for a one-time visitor (no subscription)
**Steps:**
1. **Entry** → Vehicle enters, receives ticket
   - `POST /api/v1/gate/entry`
   - Returns: `ticketCode`, `parkingEventId`
   - Gate opens
2. **Exit Attempt (Before Payment)** → Denied
   - `POST /api/v1/gate/exit`
   - Returns: `paymentRequired: true`
   - Gate remains closed
3. **Payment Status Check** → Unpaid
   - `GET /api/v1/billing/status?parkingEventId={id}`
   - Returns: `isPaid: false`
4. **Payment** → Process payment
   - `POST /api/v1/billing/pay-test` (test endpoint)
   - Payload: `ticketCode`, `licensePlate`, `amount`
   - Returns: Payment confirmation
5. **Payment Status Verify** → Paid
   - `GET /api/v1/billing/status?parkingEventId={id}`
   - Returns: `isPaid: true`
6. **Exit** → Success
   - `POST /api/v1/gate/exit`
   - Returns: `gateStatus: OPENED`, message: "Thank you for your payment. Goodbye!"
**Duration:** ~2 minutes
**Expected Result:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
---
## 🏗️ Architecture
### Services Under Test
1. **eureka-server** - Service discovery
2. **api-gateway** - API Gateway with routing
3. **postgres** - Database
4. **redis** - Session storage
5. **gate-control-service** - Entry/exit logic
6. **billing-service** - Payment processing
7. **client-service** - Subscription checks
8. **management-service** - System management
9. **reporting-service** - Analytics
### Key Features
- ✅ **Isolated environment** - No conflicts with running services
- ✅ **Full integration** - All microservices communicate
- ✅ **Database migrations** - Uses init.sql for schema
- ✅ **No security** - Disabled for E2E tests (faster execution)
- ✅ **Automatic cleanup** - Containers removed after test
---
## 📚 Additional Documentation

| Document | 🇬🇧 English | 🇷🇺 Русский |
|----------|-------------|-------------|
| Detailed guide | [E2E_TESTING_GUIDE_EN.md](E2E_TESTING_GUIDE_EN.md) | [E2E_TESTING_GUIDE_RU.md](E2E_TESTING_GUIDE_RU.md) |
| Quick reference | [QUICK_START_GUIDE_EN.md](QUICK_START_GUIDE_EN.md) | [QUICK_START_GUIDE_RU.md](QUICK_START_GUIDE_RU.md) |
| Windows setup | [E2E_TESTCONTAINERS_WINDOWS_SETUP_EN.md](E2E_TESTCONTAINERS_WINDOWS_SETUP_EN.md) | [E2E_TESTCONTAINERS_WINDOWS_SETUP_RU.md](E2E_TESTCONTAINERS_WINDOWS_SETUP_RU.md) |
| Port architecture | [PORTS_ARCHITECTURE_EN.md](PORTS_ARCHITECTURE_EN.md) | [PORTS_ARCHITECTURE_RU.md](PORTS_ARCHITECTURE_RU.md) |
---
## 🐛 Troubleshooting
### Docker not found
**Error:** `Could not find a valid Docker environment`
**Solution:** Add `"min-api-version": "1.24"` to Docker Engine settings.
### Port conflicts
**Error:** `Bind for 0.0.0.0:8761 failed: port is already allocated`
**Solution:** Stop running containers:
```powershell
docker-compose down
docker ps -a --filter "name=parking" -q | ForEach-Object { docker rm -f $_ }
```
### Image not found
**Error:** `Image 'billing-service:latest' not found`
**Solution:** Build images:
```powershell
.\build-e2e-images.ps1
```
---
**For questions or issues, see the detailed guides in the documentation folder.**
