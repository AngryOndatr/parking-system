# E2E Tests - Parking System
End-to-end integration tests for the Parking System using **Testcontainers**.
**Status:** ✅ **Ready for Use** (Updated 2026-03-09)
---
## 🚀 Quick Start

### Run via devops script (recommended)
```powershell
cd devops
.\run-e2e-tests.ps1
```

### Run via Maven
```powershell
cd backend
mvn test -Pe2e
```
---
## 📋 Prerequisites
- ✅ **Docker Desktop** running
- ✅ **Java 21** installed
- ✅ **Maven 3.6+** installed
- ✅ Docker images built (`docker-compose build` from project root)
---
## 🧪 Test Scenarios

### OneTimeVisitorE2ETest (Issue #70)
**Scenario:** Complete parking cycle for a one-time visitor (no subscription)

**Steps:**
1. **Entry** → `POST /api/v1/gate/entry` → returns `ticketCode`, `isSubscriber=false`
2. **Exit Attempt** → `POST /api/v1/gate/exit` → `paymentRequired: true`
3. **Payment Status** → `GET /api/v1/billing/status-by-ticket?ticketCode={code}` → `isPaid: false`
4. **Payment** → `POST /api/v1/billing/pay-test`
5. **Exit** → `POST /api/v1/gate/exit` → `gateStatus: OPENED`

**Duration:** ~2 minutes

---

### SubscriberE2ETest (Issue #73)
**Scenario:** Complete parking cycle for a subscriber (plate AA1234BB, seeded in init.sql)

**Steps:**
1. **Entry** → `POST /api/v1/gate/entry` with `licensePlate=AA1234BB`
   - Returns: `isSubscriber=true`, `gateStatus=OPENED`, no ticketCode
2. **Exit** → `POST /api/v1/gate/exit` → `paymentRequired=false`, `gateStatus=OPENED`
3. **Verify no payment** → `GET /api/v1/billing/status-by-ticket` → 404

**Duration:** ~1 minute

**Expected result for both tests:**
```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
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
- ✅ **Database seeded** - Uses init.sql (includes subscriber AA1234BB)
- ✅ **No security** - Disabled for E2E tests (SPRING_AUTOCONFIGURE_EXCLUDE)
- ✅ **Automatic cleanup** - Containers removed after test
- ✅ **Surefire discovery** - Both tests found via `**/*E2ETest.java` pattern

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
**Solution:** Ensure Docker Desktop is running.

### Port conflicts
**Error:** `Bind for 0.0.0.0:8761 failed: port is already allocated`
**Solution:** Stop running containers:
```powershell
docker-compose down
docker ps -a --filter "name=parking" -q | ForEach-Object { docker rm -f $_ }
```

### Image not found
**Error:** `Image 'billing-service:latest' not found`
**Solution:** Build images from project root:
```powershell
mvn clean install -DskipTests
docker-compose build
```
