# 📋 Phase 1 — Basic Backend Issues

> **Ready-to-use issue templates in English for GitHub**
>
> This document contains the Epic and all child issues for Phase 1 implementation.
> Each issue includes: goal, acceptance criteria, files to change, estimates, labels, and dependencies.

---

## 🎯 How to Use This Document

### Option A: Create Issues Manually
1. Go to GitHub → Issues → New issue
2. Copy the title and body from each section below
3. Add the specified labels
4. Set milestone to "Phase 1 (3 weeks)"
5. Link dependencies as noted

### Option B: Use GitHub CLI (Faster)
**Automated Scripts Available!**

We provide ready-to-use scripts that create all issues automatically:

**Linux/Mac:**
```bash
cd devops
./create-phase1-issues.sh
```

**Windows (PowerShell):**
```powershell
cd devops
.\create-phase1-issues.ps1
```

**Manual CLI commands:**
```bash
# Authenticate if needed
gh auth login

# See individual commands at end of document
```

### Option C: Use GitHub Copilot
- Share this document with GitHub Copilot
- Ask it to create the issues for you

---

## 📊 Epic: Phase 1 — Basic Backend

### Title
```
Phase 1 — Basic Backend (3 weeks)
```

### Body
```markdown
## 🎯 Goal
Implement core backend functionality required for Phase 1: CRUD for clients & vehicles, subscription check endpoint, management endpoints for parking spaces, reporting log endpoint, and necessary DB migrations and tests. This Epic groups all related implementation tasks and tracks Phase 1 progress.

## 📦 Scope

### client-service
- CRUD for clients and vehicles
- GET /check (subscription verification)

### management-service
- GET /available (list available parking spaces)
- POST /update (update parking space status)

### reporting-service
- POST /log (persist to logs table)

### Database
- Flyway migrations for new tables (if missing)
- Ensure parking_spaces and logs tables exist

### Testing & Integration
- Minimal unit/integration tests
- API Gateway proxy verification

## ✅ Acceptance Criteria
- [ ] All child issues implemented and linked to this Epic
- [ ] Flyway migrations run successfully and create required tables
- [ ] Basic integration test verifies API Gateway proxying for new endpoints
- [ ] Each service has at least one unit or integration test for happy path

## 📝 Checklist
- [ ] CLIENT-SVC: CRUD for CLIENTS (#1)
- [ ] CLIENT-SVC: CRUD for VEHICLES (#2)
- [ ] CLIENT-SVC: GET /check (#3)
- [ ] MANAGEMENT-SVC: GET /available (#4)
- [ ] MANAGEMENT-SVC: POST /update (#5)
- [ ] REPORTING-SVC: POST /log (#6)
- [ ] DB: Flyway migrations for spaces/logs (#7)
- [ ] Integration: API Gateway proxy checks (#8)
- [ ] Tests & Docs (#9)

## 🏷️ Labels
`epic` `phase-1` `backend` `postgres` `high-priority`

## 📅 Milestone
Phase 1 (3 weeks)
```

---

## 📝 Issue #1: CLIENT-SVC — CRUD for CLIENTS

### Title
```
CLIENT-SVC — CRUD for CLIENTS
```

### Body
```markdown
## 📄 Summary
Implement full CRUD for clients in client-service. Use existing Client entity in parking-common. Ensure JWT-protected endpoints work via API Gateway.

## ✅ Acceptance Criteria
- [ ] Endpoints implemented:
  - POST /api/clients (create new client)
  - GET /api/clients (list all clients)
  - GET /api/clients/{id} (get client by id)
  - PUT /api/clients/{id} (update client)
  - DELETE /api/clients/{id} (delete client)
- [ ] Data persisted to `clients` table (Postgres)
- [ ] Validation: unique phone_number/email handling and appropriate HTTP codes
- [ ] Unit tests for create and get-by-id happy paths

## 📁 Files to Modify/Create
- Controller: [ClientController.java](../backend/client-service/src/main/java/com/parking/client_service/controller/ClientController.java) (existing)
- Service: [ClientService.java](../backend/client-service/src/main/java/com/parking/client_service/service/ClientService.java) (existing)
- Repository: [ClientRepository.java](../backend/client-service/src/main/java/com/parking/client_service/repository/ClientRepository.java) (existing)
- Mapper/DTO: [ClientMapper.java](../backend/client-service/src/main/java/com/parking/client_service/mapper/ClientMapper.java) (existing)
- Common entity (use): [Client.java](../backend/parking-common/src/main/java/com/parking/common/entity/Client.java)

## ⏱️ Estimate
2–3 days

## 🏷️ Labels
`feature` `client-service` `backend` `postgres` `needs-tests`

## 🔗 Dependencies
- Epic: Phase 1 — Basic Backend
- DB migrations (if schema missing)
```

---

## 📝 Issue #2: CLIENT-SVC — CRUD for VEHICLES

### Title
```
CLIENT-SVC — CRUD for VEHICLES
```

### Body
```markdown
## 📄 Summary
Implement vehicle management endpoints in client-service. Use Vehicle entity in parking-common. Support linking a vehicle to a client.

## ✅ Acceptance Criteria
- [ ] Endpoints implemented:
  - POST /api/vehicles (create new vehicle)
  - GET /api/vehicles (list all vehicles)
  - GET /api/vehicles/{id} (get vehicle by id)
  - PUT /api/vehicles/{id} (update vehicle)
  - DELETE /api/vehicles/{id} (delete vehicle)
- [ ] Vehicle persisted to `vehicles` table with `client_id` foreign key linking to `clients`
- [ ] Unique `license_plate` enforced and validation errors returned properly
- [ ] Unit tests for create and list happy paths

## 📁 Files to Create/Modify
- **New** controller: `backend/client-service/src/main/java/com/parking/client_service/controller/VehicleController.java`
- **New** service: `backend/client-service/src/main/java/com/parking/client_service/service/VehicleService.java`
- **New** repository: `backend/client-service/src/main/java/com/parking/client_service/repository/VehicleRepository.java`
- Use entity: [Vehicle.java](../backend/parking-common/src/main/java/com/parking/common/entity/Vehicle.java)
- Add DTOs or reuse generated models if available under `com.parking.client_service.generated.model`

## ⏱️ Estimate
2–3 days

## 🏷️ Labels
`feature` `client-service` `backend` `postgres` `needs-tests`

## 🔗 Dependencies
- Issue #1: CLIENT-SVC CRUD for CLIENTS
```

---

## 📝 Issue #3: CLIENT-SVC — GET /check (subscription check)

### Title
```
CLIENT-SVC — GET /check (subscription check)
```

### Body
```markdown
## 📄 Summary
Add endpoint to check subscription/permission status for vehicles or clients. Returns whether entry is allowed.

## ✅ Acceptance Criteria
- [ ] Endpoint: `GET /api/clients/check?licensePlate={plate}` (or `?clientId={id}`)
- [ ] Response JSON: `{ "allowed": true|false, "clientId": number|null, "vehicleId": number|null }`
- [ ] 200 OK when vehicle/client found; 404 when not found
- [ ] Logic: uses `Vehicle.isAllowed` and client relationship
- [ ] Unit/integration test for allowed and not-allowed cases

## 📁 Files to Modify
- Controller: [ClientController.java](../backend/client-service/src/main/java/com/parking/client_service/controller/ClientController.java) (add route)
- Service: [ClientService.java](../backend/client-service/src/main/java/com/parking/client_service/service/ClientService.java) (add method)
- Repository: [ClientRepository.java](../backend/client-service/src/main/java/com/parking/client_service/repository/ClientRepository.java) and new VehicleRepository if needed

## ⏱️ Estimate
1 day

## 🏷️ Labels
`feature` `client-service` `backend` `postgres` `needs-tests`

## 🔗 Dependencies
- Issue #2: CLIENT-SVC CRUD for VEHICLES
```

---

## 📝 Issue #4: MANAGEMENT-SVC — GET /available (list available parking spaces)

### Title
```
MANAGEMENT-SVC — GET /available (list available parking spaces)
```

### Body
```markdown
## 📄 Summary
Implement endpoint in management-service to list currently available parking spaces. Read directly from DB table (parking_spaces).

## ✅ Acceptance Criteria
- [ ] Endpoint: `GET /api/management/spots/available`
- [ ] Response: JSON array of available spaces: `{ "spaceId", "lotId", "status", "level" ... }` (fields per DB)
- [ ] Uses JPA repository to read `parking_spaces` table
- [ ] Returns 200 with empty array if none available
- [ ] Integration test for DB read

## 📁 Files to Create
- **New** Controller: `backend/management-service/src/main/java/com/parking/management_service/controller/ManagementController.java`
- **New** Service: `backend/management-service/src/main/java/com/parking/management_service/service/ManagementService.java`
- **New** Repository: `backend/management-service/src/main/java/com/parking/management_service/repository/ParkingSpaceRepository.java`
- Entity: either add to `backend/parking-common/src/main/java/com/parking/common/entity/ParkingSpace.java` or create in management-service (recommend: parking-common)

## ⏱️ Estimate
2 days

## 🏷️ Labels
`feature` `management-service` `backend` `postgres` `needs-tests`

## 🔗 Dependencies
- Flyway migrations creating `parking_spaces` table (already exists via V3 migration)
```

---

## 📝 Issue #5: MANAGEMENT-SVC — POST /update (update parking space status)

### Title
```
MANAGEMENT-SVC — POST /update (update parking space status)
```

### Body
```markdown
## 📄 Summary
Implement endpoint for updating parking space status (occupied/available) in management-service.

## ✅ Acceptance Criteria
- [ ] Endpoint: `POST /api/management/spots/update`
- [ ] Accepts JSON body: `{ "spaceId": number, "status": "AVAILABLE"|"OCCUPIED"|"RESERVED" }`
- [ ] Validates input and updates DB record
- [ ] Returns 200 with updated resource or 400 on invalid input
- [ ] Handles concurrent updates (optimistic locking recommended)
- [ ] Integration test for update behavior

## 📁 Files to Create/Modify
- Controller: [ManagementController.java](../backend/management-service/src/main/java/com/parking/management_service/controller/ManagementController.java)
- Service: [ManagementService.java](../backend/management-service/src/main/java/com/parking/management_service/service/ManagementService.java)
- Repository/Entity: `backend/management-service/.../ParkingSpaceRepository.java` and entity in parking-common or management-service

## ⏱️ Estimate
1–2 days

## 🏷️ Labels
`feature` `management-service` `backend` `postgres` `needs-tests`

## 🔗 Dependencies
- Issue #4: MANAGEMENT-SVC GET /available
```

---

## 📝 Issue #6: REPORTING-SVC — POST /log (persist system logs)

### Title
```
REPORTING-SVC — POST /log (persist system logs)
```

### Body
```markdown
## 📄 Summary
Add an internal API in reporting-service to receive log records and persist them to a logs table.

## ✅ Acceptance Criteria
- [ ] Endpoint: `POST /api/reporting/log`
- [ ] Accepts JSON: `{ "timestamp": ISO8601, "level": "INFO|WARN|ERROR", "service": "string", "message": "string", "meta": {...} }`
- [ ] Inserts record into `logs` table with proper columns and timestamp
- [ ] Returns 201 Created with persisted id
- [ ] Unit/integration test for insert

## 📁 Files to Create
- **New** Controller: `backend/reporting-service/src/main/java/com/parking/reporting_service/controller/ReportingController.java`
- **New** Service: `backend/reporting-service/src/main/java/com/parking/reporting_service/service/ReportingService.java`
- **New** Repository: `backend/reporting-service/src/main/java/com/parking/reporting_service/repository/LogRepository.java`
- **New** Entity: `backend/reporting-service/src/main/java/com/parking/reporting_service/entity/Log.java` or place entity in parking-common if reused

## ⏱️ Estimate
1–2 days

## 🏷️ Labels
`feature` `reporting-service` `backend` `postgres` `needs-tests`

## 🔗 Dependencies
- Flyway migrations for `logs` table (already exists via V1 migration)
```

---

## 📝 Issue #7: DB — Flyway migrations for parking_spaces and logs (if missing)

### Title
```
DB — Flyway migrations for parking_spaces and logs (if missing)
```

### Body
```markdown
## 📄 Summary
Ensure Flyway migrations include creation of parking_spaces and logs tables. Add migration files into the Flyway folder where migrations run (backend/api-gateway/src/main/resources/db/migration/ or service-specific migrations if configured).

## ✅ Acceptance Criteria
- [ ] Migration files verified or created (e.g., V5__add_parking_spaces.sql, V6__add_logs.sql if needed)
- [ ] Migrations idempotent and follow project conventions
- [ ] Running flyway (dev environment) creates the tables
- [ ] DB schema matches entities used by services

## 📁 Files to Check/Create
- [V3__add_parking_spaces.sql](../backend/api-gateway/src/main/resources/db/migration/V3__add_parking_spaces.sql) ✅ **Already exists**
- [V1__initial_schema.sql](../backend/api-gateway/src/main/resources/db/migration/V1__initial_schema.sql) ✅ **Already contains logs table**
- If additional columns needed, create new migration: `backend/api-gateway/src/main/resources/db/migration/V5__update_logs_table.sql`

## ⏱️ Estimate
1 day (verification + any needed updates)

## 🏷️ Labels
`infra` `database` `flyway` `backend`

## 🔗 Dependencies
- Epic: Phase 1 — Basic Backend
- Services that rely on these tables

## 📝 Notes
Based on repository inspection:
- ✅ `parking_spaces` table already exists (V3 migration)
- ✅ `logs` table already exists (V1 migration)
- This task mainly involves verification and potential schema updates if needed
```

---

## 📝 Issue #8: Integration — Verify API Gateway proxying for new endpoints

### Title
```
Integration — Verify API Gateway proxying for new endpoints
```

### Body
```markdown
## 📄 Summary
Confirm API Gateway correctly proxies requests to the new management and reporting endpoints (gateway controllers exist but need verification).

## ✅ Acceptance Criteria
- [ ] Requests to API Gateway endpoints are routed correctly:
  - `GET /api/management/spots/available` → management-service
  - `POST /api/reporting/log` → reporting-service
- [ ] All endpoints return expected responses
- [ ] README updated with example curl/PowerShell commands for each route
- [ ] Script added under `devops/` to run quick smoke tests (optional)

## 📁 Files to Check/Modify
- Gateway proxy controllers:
  - [ManagementProxyController.java](../backend/api-gateway/src/main/java/com/parking/api_gateway/controller/ManagementProxyController.java) (may need creation)
  - [ReportingProxyController.java](../backend/api-gateway/src/main/java/com/parking/api_gateway/controller/ReportingProxyController.java) (may need creation)
- Devops script: `devops/test-proxy.sh` or `devops/test-proxy.ps1`
- Documentation: [README.md](../README.md)

## ⏱️ Estimate
1 day (can be parallel)

## 🏷️ Labels
`integration` `api-gateway` `devops` `testing`

## 🔗 Dependencies
- Issue #4: MANAGEMENT-SVC GET /available
- Issue #5: MANAGEMENT-SVC POST /update
- Issue #6: REPORTING-SVC POST /log
```

---

## 📝 Issue #9: Tests & Docs — Minimal tests and API docs for Phase 1

### Title
```
Tests & Docs — Minimal tests and API docs for Phase 1
```

### Body
```markdown
## 📄 Summary
Add unit/integration tests and update README/docs for new endpoints. Provide example requests and expected responses.

## ✅ Acceptance Criteria
- [ ] Each service has at least one unit/integration test for new endpoints:
  - Happy path test
  - One negative case test
- [ ] README updated with examples for using the new endpoints via API Gateway
- [ ] Postman/collection snippet or curl/PowerShell examples included in docs
- [ ] CI job runs tests locally (if CI exists)

## 📁 Files to Modify/Create
- Tests under:
  - `backend/client-service/src/test/...`
  - `backend/management-service/src/test/...`
  - `backend/reporting-service/src/test/...`
- Docs:
  - [README.md](../README.md) (root) or service-level README
  - Devops scripts under `devops/` for quick verification

## ⏱️ Estimate
1–2 days

## 🏷️ Labels
`tests` `docs` `backend` `ci`

## 🔗 Dependencies
- All previous issues (#1-#8)
```

---

## 🚀 GitHub CLI Commands

If you want to create all issues using GitHub CLI:

```bash
# Epic
gh issue create \
  --title "Phase 1 — Basic Backend (3 weeks)" \
  --label "epic,phase-1,backend,postgres,high-priority" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/epic_phase1.md

# Issue #1
gh issue create \
  --title "CLIENT-SVC — CRUD for CLIENTS" \
  --label "feature,client-service,backend,postgres,needs-tests" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/issue_01_client_crud.md

# Issue #2
gh issue create \
  --title "CLIENT-SVC — CRUD for VEHICLES" \
  --label "feature,client-service,backend,postgres,needs-tests" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/issue_02_vehicle_crud.md

# Issue #3
gh issue create \
  --title "CLIENT-SVC — GET /check (subscription check)" \
  --label "feature,client-service,backend,postgres,needs-tests" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/issue_03_check_endpoint.md

# Issue #4
gh issue create \
  --title "MANAGEMENT-SVC — GET /available (list available parking spaces)" \
  --label "feature,management-service,backend,postgres,needs-tests" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/issue_04_get_available.md

# Issue #5
gh issue create \
  --title "MANAGEMENT-SVC — POST /update (update parking space status)" \
  --label "feature,management-service,backend,postgres,needs-tests" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/issue_05_update_space.md

# Issue #6
gh issue create \
  --title "REPORTING-SVC — POST /log (persist system logs)" \
  --label "feature,reporting-service,backend,postgres,needs-tests" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/issue_06_log_endpoint.md

# Issue #7
gh issue create \
  --title "DB — Flyway migrations for parking_spaces and logs (if missing)" \
  --label "infra,database,flyway,backend" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/issue_07_migrations.md

# Issue #8
gh issue create \
  --title "Integration — Verify API Gateway proxying for new endpoints" \
  --label "integration,api-gateway,devops,testing" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/issue_08_gateway_proxy.md

# Issue #9
gh issue create \
  --title "Tests & Docs — Minimal tests and API docs for Phase 1" \
  --label "tests,docs,backend,ci" \
  --milestone "Phase 1 (3 weeks)" \
  --body-file docs/issues/issue_09_tests_docs.md
```

---

## 📋 Required Labels

Before creating issues, ensure these labels exist in your repository:

| Label | Color | Description |
|-------|-------|-------------|
| `epic` | `#3E4B9E` | Epic issue grouping multiple related issues |
| `phase-1` | `#0E8A16` | Phase 1 work items |
| `backend` | `#1D76DB` | Backend service work |
| `postgres` | `#336791` | PostgreSQL database work |
| `high-priority` | `#D73A4A` | High priority items |
| `feature` | `#A2EEEF` | New feature implementation |
| `client-service` | `#FBCA04` | Client service related |
| `management-service` | `#FBCA04` | Management service related |
| `reporting-service` | `#FBCA04` | Reporting service related |
| `needs-tests` | `#D4C5F9` | Needs test coverage |
| `infra` | `#0366D6` | Infrastructure work |
| `database` | `#0366D6` | Database work |
| `flyway` | `#0366D6` | Flyway migrations |
| `integration` | `#C5DEF5` | Integration work |
| `api-gateway` | `#FBCA04` | API Gateway related |
| `devops` | `#F9D0C4` | DevOps work |
| `testing` | `#D4C5F9` | Testing work |
| `tests` | `#D4C5F9` | Test implementation |
| `docs` | `#0075CA` | Documentation |
| `ci` | `#0075CA` | CI/CD work |

---

## 📅 Estimated Timeline

| Week | Focus | Issues |
|------|-------|--------|
| Week 1 | Client Service Implementation | #1, #2, #3 |
| Week 2 | Management & Reporting Services | #4, #5, #6, #7 |
| Week 3 | Integration & Testing | #8, #9 |

**Total Estimate:** ~13-18 days of development work

---

## ✅ Checklist for Issue Creation

- [ ] Create "Phase 1 (3 weeks)" milestone in GitHub
- [ ] Create all required labels in GitHub
- [ ] Create Epic issue
- [ ] Create Issue #1 and link to Epic
- [ ] Create Issue #2 and link to Epic + Issue #1
- [ ] Create Issue #3 and link to Epic + Issue #2
- [ ] Create Issue #4 and link to Epic
- [ ] Create Issue #5 and link to Epic + Issue #4
- [ ] Create Issue #6 and link to Epic
- [ ] Create Issue #7 and link to Epic
- [ ] Create Issue #8 and link to Epic + Issues #4, #5, #6
- [ ] Create Issue #9 and link to Epic + All previous issues
- [ ] Add all issues to project board (if using GitHub Projects)

---

**Document Status:** ✅ Ready for issue creation
**Last Updated:** 2025-12-29
**Related Documents:** [QUICK_GUIDE_ISSUES.md](./QUICK_GUIDE_ISSUES.md), [DATABASE_MIGRATION_TASKS_EN.md](./DATABASE_MIGRATION_TASKS_EN.md)
