#!/bin/bash

# Phase 1 GitHub Issues Creation Script
# This script creates all Phase 1 issues using GitHub CLI
# Prerequisites: gh CLI installed and authenticated

set -e

echo "🚀 Phase 1 Issues Creation Script"
echo "=================================="
echo ""
echo "This script will create:"
echo "  - 1 Epic issue"
echo "  - 9 implementation issues"
echo ""
echo "Prerequisites:"
echo "  - GitHub CLI (gh) installed"
echo "  - Authenticated with: gh auth login"
echo "  - Milestone 'Phase 1 (3 weeks)' created"
echo "  - All required labels created (see docs/PHASE_1_ISSUES.md)"
echo ""

read -p "Do you want to continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 1
fi

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo "❌ Error: GitHub CLI (gh) is not installed."
    echo "Install from: https://cli.github.com/"
    exit 1
fi

# Check if authenticated
if ! gh auth status &> /dev/null; then
    echo "❌ Error: Not authenticated with GitHub CLI."
    echo "Run: gh auth login"
    exit 1
fi

echo ""
echo "Creating issues..."
echo ""

# Create Epic
echo "📊 Creating Epic: Phase 1 — Basic Backend..."
EPIC_BODY='## 🎯 Goal
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
- [ ] CLIENT-SVC: CRUD for CLIENTS
- [ ] CLIENT-SVC: CRUD for VEHICLES
- [ ] CLIENT-SVC: GET /check
- [ ] MANAGEMENT-SVC: GET /available
- [ ] MANAGEMENT-SVC: POST /update
- [ ] REPORTING-SVC: POST /log
- [ ] DB: Flyway migrations for spaces/logs
- [ ] Integration: API Gateway proxy checks
- [ ] Tests & Docs'

EPIC_NUM=$(gh issue create \
    --title "Phase 1 — Basic Backend (3 weeks)" \
    --body "$EPIC_BODY" \
    --label "epic,phase-1,backend,postgres,high-priority" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Epic created: #$EPIC_NUM"
echo ""

# Issue #1
echo "📝 Creating Issue #1: CLIENT-SVC — CRUD for CLIENTS..."
ISSUE1_BODY='## 📄 Summary
Implement full CRUD for clients in client-service. Use existing Client entity in parking-common. Ensure JWT-protected endpoints work via API Gateway.

## ✅ Acceptance Criteria
- [ ] Endpoints implemented: POST /api/clients, GET /api/clients, GET /api/clients/{id}, PUT /api/clients/{id}, DELETE /api/clients/{id}
- [ ] Data persisted to `clients` table (Postgres)
- [ ] Validation: unique phone_number/email handling and appropriate HTTP codes
- [ ] Unit tests for create and get-by-id happy paths

## 📁 Files to Modify/Create
- Controller: backend/client-service/src/main/java/com/parking/client_service/controller/ClientController.java
- Service: backend/client-service/src/main/java/com/parking/client_service/service/ClientService.java
- Repository: backend/client-service/src/main/java/com/parking/client_service/repository/ClientRepository.java
- Mapper/DTO: backend/client-service/src/main/java/com/parking/client_service/mapper/ClientMapper.java
- Common entity: backend/parking-common/src/main/java/com/parking/common/entity/Client.java

## ⏱️ Estimate
2–3 days

## 🔗 Dependencies
- Epic: #'$EPIC_NUM

ISSUE1_NUM=$(gh issue create \
    --title "CLIENT-SVC — CRUD for CLIENTS" \
    --body "$ISSUE1_BODY" \
    --label "feature,client-service,backend,postgres,needs-tests" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Issue #1 created: #$ISSUE1_NUM"
echo ""

# Issue #2
echo "📝 Creating Issue #2: CLIENT-SVC — CRUD for VEHICLES..."
ISSUE2_BODY='## 📄 Summary
Implement vehicle management endpoints in client-service. Use Vehicle entity in parking-common. Support linking a vehicle to a client.

## ✅ Acceptance Criteria
- [ ] Endpoints implemented: POST /api/vehicles, GET /api/vehicles, GET /api/vehicles/{id}, PUT /api/vehicles/{id}, DELETE /api/vehicles/{id}
- [ ] Vehicle persisted to `vehicles` table with `client_id` foreign key
- [ ] Unique `license_plate` enforced and validation errors returned properly
- [ ] Unit tests for create and list happy paths

## 📁 Files to Create/Modify
- NEW Controller: backend/client-service/src/main/java/com/parking/client_service/controller/VehicleController.java
- NEW Service: backend/client-service/src/main/java/com/parking/client_service/service/VehicleService.java
- NEW Repository: backend/client-service/src/main/java/com/parking/client_service/repository/VehicleRepository.java
- Use entity: backend/parking-common/src/main/java/com/parking/common/entity/Vehicle.java

## ⏱️ Estimate
2–3 days

## 🔗 Dependencies
- Issue #'$ISSUE1_NUM': CLIENT-SVC CRUD for CLIENTS'

ISSUE2_NUM=$(gh issue create \
    --title "CLIENT-SVC — CRUD for VEHICLES" \
    --body "$ISSUE2_BODY" \
    --label "feature,client-service,backend,postgres,needs-tests" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Issue #2 created: #$ISSUE2_NUM"
echo ""

# Issue #3
echo "📝 Creating Issue #3: CLIENT-SVC — GET /check..."
ISSUE3_BODY='## 📄 Summary
Add endpoint to check subscription/permission status for vehicles or clients. Returns whether entry is allowed.

## ✅ Acceptance Criteria
- [ ] Endpoint: GET /api/clients/check?licensePlate={plate} (or ?clientId={id})
- [ ] Response JSON: { "allowed": true|false, "clientId": number|null, "vehicleId": number|null }
- [ ] 200 OK when vehicle/client found; 404 when not found
- [ ] Logic uses Vehicle.isAllowed and client relationship
- [ ] Unit/integration test for allowed and not-allowed cases

## 📁 Files to Modify
- Controller: backend/client-service/src/main/java/com/parking/client_service/controller/ClientController.java
- Service: backend/client-service/src/main/java/com/parking/client_service/service/ClientService.java
- Repository: backend/client-service/src/main/java/com/parking/client_service/repository/ClientRepository.java

## ⏱️ Estimate
1 day

## 🔗 Dependencies
- Issue #'$ISSUE2_NUM': CLIENT-SVC CRUD for VEHICLES'

ISSUE3_NUM=$(gh issue create \
    --title "CLIENT-SVC — GET /check (subscription check)" \
    --body "$ISSUE3_BODY" \
    --label "feature,client-service,backend,postgres,needs-tests" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Issue #3 created: #$ISSUE3_NUM"
echo ""

# Issue #4
echo "📝 Creating Issue #4: MANAGEMENT-SVC — GET /available..."
ISSUE4_BODY='## 📄 Summary
Implement endpoint in management-service to list currently available parking spaces. Read directly from DB table (parking_spaces).

## ✅ Acceptance Criteria
- [ ] Endpoint: GET /api/management/spots/available
- [ ] Response: JSON array of available spaces with spaceId, lotId, status, level
- [ ] Uses JPA repository to read parking_spaces table
- [ ] Returns 200 with empty array if none available
- [ ] Integration test for DB read

## 📁 Files to Create
- NEW Controller: backend/management-service/src/main/java/com/parking/management_service/controller/ManagementController.java
- NEW Service: backend/management-service/src/main/java/com/parking/management_service/service/ManagementService.java
- NEW Repository: backend/management-service/src/main/java/com/parking/management_service/repository/ParkingSpaceRepository.java
- Entity: backend/parking-common/src/main/java/com/parking/common/entity/ParkingSpace.java

## ⏱️ Estimate
2 days

## 🔗 Dependencies
- Flyway V3 migration (parking_spaces table already exists)'

ISSUE4_NUM=$(gh issue create \
    --title "MANAGEMENT-SVC — GET /available (list available parking spaces)" \
    --body "$ISSUE4_BODY" \
    --label "feature,management-service,backend,postgres,needs-tests" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Issue #4 created: #$ISSUE4_NUM"
echo ""

# Issue #5
echo "📝 Creating Issue #5: MANAGEMENT-SVC — POST /update..."
ISSUE5_BODY='## 📄 Summary
Implement endpoint for updating parking space status (occupied/available) in management-service.

## ✅ Acceptance Criteria
- [ ] Endpoint: POST /api/management/spots/update
- [ ] Accepts JSON: { "spaceId": number, "status": "AVAILABLE"|"OCCUPIED"|"RESERVED" }
- [ ] Validates input and updates DB record
- [ ] Returns 200 with updated resource or 400 on invalid input
- [ ] Handles concurrent updates (optimistic locking recommended)
- [ ] Integration test for update behavior

## 📁 Files to Create/Modify
- Controller: backend/management-service/src/main/java/com/parking/management_service/controller/ManagementController.java
- Service: backend/management-service/src/main/java/com/parking/management_service/service/ManagementService.java
- Repository: backend/management-service/src/main/java/com/parking/management_service/repository/ParkingSpaceRepository.java

## ⏱️ Estimate
1–2 days

## 🔗 Dependencies
- Issue #'$ISSUE4_NUM': MANAGEMENT-SVC GET /available'

ISSUE5_NUM=$(gh issue create \
    --title "MANAGEMENT-SVC — POST /update (update parking space status)" \
    --body "$ISSUE5_BODY" \
    --label "feature,management-service,backend,postgres,needs-tests" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Issue #5 created: #$ISSUE5_NUM"
echo ""

# Issue #6
echo "📝 Creating Issue #6: REPORTING-SVC — POST /log..."
ISSUE6_BODY='## 📄 Summary
Add an internal API in reporting-service to receive log records and persist them to a logs table.

## ✅ Acceptance Criteria
- [ ] Endpoint: POST /api/reporting/log
- [ ] Accepts JSON: { "timestamp": ISO8601, "level": "INFO|WARN|ERROR", "service": "string", "message": "string", "meta": {...} }
- [ ] Inserts record into logs table with proper columns and timestamp
- [ ] Returns 201 Created with persisted id
- [ ] Unit/integration test for insert

## 📁 Files to Create
- NEW Controller: backend/reporting-service/src/main/java/com/parking/reporting_service/controller/ReportingController.java
- NEW Service: backend/reporting-service/src/main/java/com/parking/reporting_service/service/ReportingService.java
- NEW Repository: backend/reporting-service/src/main/java/com/parking/reporting_service/repository/LogRepository.java
- NEW Entity: backend/reporting-service/src/main/java/com/parking/reporting_service/entity/Log.java

## ⏱️ Estimate
1–2 days

## 🔗 Dependencies
- Flyway V1 migration (logs table already exists)'

ISSUE6_NUM=$(gh issue create \
    --title "REPORTING-SVC — POST /log (persist system logs)" \
    --body "$ISSUE6_BODY" \
    --label "feature,reporting-service,backend,postgres,needs-tests" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Issue #6 created: #$ISSUE6_NUM"
echo ""

# Issue #7
echo "📝 Creating Issue #7: DB — Flyway migrations..."
ISSUE7_BODY='## 📄 Summary
Ensure Flyway migrations include creation of parking_spaces and logs tables. Verify existing migrations or add new ones if needed.

## ✅ Acceptance Criteria
- [ ] Migration files verified (V3 for parking_spaces, V1 for logs)
- [ ] Migrations idempotent and follow project conventions
- [ ] Running flyway creates the tables successfully
- [ ] DB schema matches entities used by services

## 📁 Files to Check
- V3__add_parking_spaces.sql ✅ Already exists
- V1__initial_schema.sql ✅ Already contains logs table
- Create V5__update_logs_table.sql if additional columns needed

## ⏱️ Estimate
1 day (verification + any needed updates)

## 🔗 Dependencies
- Epic: #'$EPIC_NUM

ISSUE7_NUM=$(gh issue create \
    --title "DB — Flyway migrations for parking_spaces and logs (if missing)" \
    --body "$ISSUE7_BODY" \
    --label "infra,database,flyway,backend" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Issue #7 created: #$ISSUE7_NUM"
echo ""

# Issue #8
echo "📝 Creating Issue #8: Integration — API Gateway proxy..."
ISSUE8_BODY='## 📄 Summary
Confirm API Gateway correctly proxies requests to the new management and reporting endpoints.

## ✅ Acceptance Criteria
- [ ] GET /api/management/spots/available routes to management-service
- [ ] POST /api/reporting/log routes to reporting-service
- [ ] All endpoints return expected responses
- [ ] README updated with curl/PowerShell examples
- [ ] Optional: Script added under devops/ for smoke tests

## 📁 Files to Check/Modify
- Gateway: backend/api-gateway/src/main/java/com/parking/api_gateway/controller/ManagementProxyController.java
- Gateway: backend/api-gateway/src/main/java/com/parking/api_gateway/controller/ReportingProxyController.java
- Script: devops/test-proxy.sh or devops/test-proxy.ps1
- Docs: README.md

## ⏱️ Estimate
1 day

## 🔗 Dependencies
- Issue #'$ISSUE4_NUM', #'$ISSUE5_NUM', #'$ISSUE6_NUM

ISSUE8_NUM=$(gh issue create \
    --title "Integration — Verify API Gateway proxying for new endpoints" \
    --body "$ISSUE8_BODY" \
    --label "integration,api-gateway,devops,testing" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Issue #8 created: #$ISSUE8_NUM"
echo ""

# Issue #9
echo "📝 Creating Issue #9: Tests & Docs..."
ISSUE9_BODY='## 📄 Summary
Add unit/integration tests and update README/docs for new endpoints. Provide example requests and expected responses.

## ✅ Acceptance Criteria
- [ ] Each service has unit/integration tests: happy path + one negative case
- [ ] README updated with examples for new endpoints via API Gateway
- [ ] Postman collection or curl/PowerShell examples in docs
- [ ] CI job runs tests locally (if CI exists)

## 📁 Files to Modify/Create
- Tests: backend/client-service/src/test/...
- Tests: backend/management-service/src/test/...
- Tests: backend/reporting-service/src/test/...
- Docs: README.md or service-level README
- Scripts: devops/ for quick verification

## ⏱️ Estimate
1–2 days

## 🔗 Dependencies
- All previous issues (#'$ISSUE1_NUM'-#'$ISSUE8_NUM')'

ISSUE9_NUM=$(gh issue create \
    --title "Tests & Docs — Minimal tests and API docs for Phase 1" \
    --body "$ISSUE9_BODY" \
    --label "tests,docs,backend,ci" \
    --milestone "Phase 1 (3 weeks)" \
    --json number -q .number)

echo "✅ Issue #9 created: #$ISSUE9_NUM"
echo ""

echo "=================================="
echo "✅ All issues created successfully!"
echo ""
echo "Summary:"
echo "  - Epic: #$EPIC_NUM"
echo "  - Issue #1 (CLIENT-SVC CRUD Clients): #$ISSUE1_NUM"
echo "  - Issue #2 (CLIENT-SVC CRUD Vehicles): #$ISSUE2_NUM"
echo "  - Issue #3 (CLIENT-SVC /check): #$ISSUE3_NUM"
echo "  - Issue #4 (MANAGEMENT-SVC /available): #$ISSUE4_NUM"
echo "  - Issue #5 (MANAGEMENT-SVC /update): #$ISSUE5_NUM"
echo "  - Issue #6 (REPORTING-SVC /log): #$ISSUE6_NUM"
echo "  - Issue #7 (DB Migrations): #$ISSUE7_NUM"
echo "  - Issue #8 (Integration): #$ISSUE8_NUM"
echo "  - Issue #9 (Tests & Docs): #$ISSUE9_NUM"
echo ""
echo "Next steps:"
echo "  1. Review issues on GitHub"
echo "  2. Add issues to project board if needed"
echo "  3. Start with Issue #$ISSUE1_NUM"
echo ""
