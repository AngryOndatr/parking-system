# Phase 1 GitHub Issues Creation Script (PowerShell)
# This script creates all Phase 1 issues using GitHub CLI
# Prerequisites: gh CLI installed and authenticated

$ErrorActionPreference = "Stop"

Write-Host "🚀 Phase 1 Issues Creation Script" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will create:"
Write-Host "  - 1 Epic issue"
Write-Host "  - 9 implementation issues"
Write-Host ""
Write-Host "Prerequisites:"
Write-Host "  - GitHub CLI (gh) installed"
Write-Host "  - Authenticated with: gh auth login"
Write-Host "  - Milestone 'Phase 1 (3 weeks)' created"
Write-Host "  - All required labels created (see docs/PHASE_1_ISSUES.md)"
Write-Host ""

$continue = Read-Host "Do you want to continue? (y/n)"
if ($continue -ne "y" -and $continue -ne "Y") {
    Write-Host "Aborted." -ForegroundColor Yellow
    exit 0
}

# Check if gh CLI is installed
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Error: GitHub CLI (gh) is not installed." -ForegroundColor Red
    Write-Host "Install from: https://cli.github.com/" -ForegroundColor Yellow
    exit 1
}

# Check if authenticated
$authStatus = & gh auth status 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error: Not authenticated with GitHub CLI." -ForegroundColor Red
    Write-Host "Run: gh auth login" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Creating issues..." -ForegroundColor Green
Write-Host ""

# Create Epic
Write-Host "📊 Creating Epic: Phase 1 — Basic Backend..." -ForegroundColor Cyan
$epicBody = @"
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
- [ ] CLIENT-SVC: CRUD for CLIENTS
- [ ] CLIENT-SVC: CRUD for VEHICLES
- [ ] CLIENT-SVC: GET /check
- [ ] MANAGEMENT-SVC: GET /available
- [ ] MANAGEMENT-SVC: POST /update
- [ ] REPORTING-SVC: POST /log
- [ ] DB: Flyway migrations for spaces/logs
- [ ] Integration: API Gateway proxy checks
- [ ] Tests & Docs
"@

$epicNum = & gh issue create `
    --title "Phase 1 — Basic Backend (3 weeks)" `
    --body $epicBody `
    --label "epic,phase-1,backend,postgres,high-priority" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Epic created: #$epicNum" -ForegroundColor Green
Write-Host ""

# Issue #1
Write-Host "📝 Creating Issue #1: CLIENT-SVC — CRUD for CLIENTS..." -ForegroundColor Cyan
$issue1Body = @"
## 📄 Summary
Implement full CRUD for clients in client-service. Use existing Client entity in parking-common. Ensure JWT-protected endpoints work via API Gateway.

## ✅ Acceptance Criteria
- [ ] Endpoints implemented: POST /api/clients, GET /api/clients, GET /api/clients/{id}, PUT /api/clients/{id}, DELETE /api/clients/{id}
- [ ] Data persisted to ``clients`` table (Postgres)
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
- Epic: #$epicNum
"@

$issue1Num = & gh issue create `
    --title "CLIENT-SVC — CRUD for CLIENTS" `
    --body $issue1Body `
    --label "feature,client-service,backend,postgres,needs-tests" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Issue #1 created: #$issue1Num" -ForegroundColor Green
Write-Host ""

# Issue #2
Write-Host "📝 Creating Issue #2: CLIENT-SVC — CRUD for VEHICLES..." -ForegroundColor Cyan
$issue2Body = @"
## 📄 Summary
Implement vehicle management endpoints in client-service. Use Vehicle entity in parking-common. Support linking a vehicle to a client.

## ✅ Acceptance Criteria
- [ ] Endpoints implemented: POST /api/vehicles, GET /api/vehicles, GET /api/vehicles/{id}, PUT /api/vehicles/{id}, DELETE /api/vehicles/{id}
- [ ] Vehicle persisted to ``vehicles`` table with ``client_id`` foreign key
- [ ] Unique ``license_plate`` enforced and validation errors returned properly
- [ ] Unit tests for create and list happy paths

## 📁 Files to Create/Modify
- NEW Controller: backend/client-service/src/main/java/com/parking/client_service/controller/VehicleController.java
- NEW Service: backend/client-service/src/main/java/com/parking/client_service/service/VehicleService.java
- NEW Repository: backend/client-service/src/main/java/com/parking/client_service/repository/VehicleRepository.java
- Use entity: backend/parking-common/src/main/java/com/parking/common/entity/Vehicle.java

## ⏱️ Estimate
2–3 days

## 🔗 Dependencies
- Issue #$issue1Num : CLIENT-SVC CRUD for CLIENTS
"@

$issue2Num = & gh issue create `
    --title "CLIENT-SVC — CRUD for VEHICLES" `
    --body $issue2Body `
    --label "feature,client-service,backend,postgres,needs-tests" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Issue #2 created: #$issue2Num" -ForegroundColor Green
Write-Host ""

# Issue #3
Write-Host "📝 Creating Issue #3: CLIENT-SVC — GET /check..." -ForegroundColor Cyan
$issue3Body = @"
## 📄 Summary
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
- Issue #$issue2Num : CLIENT-SVC CRUD for VEHICLES
"@

$issue3Num = & gh issue create `
    --title "CLIENT-SVC — GET /check (subscription check)" `
    --body $issue3Body `
    --label "feature,client-service,backend,postgres,needs-tests" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Issue #3 created: #$issue3Num" -ForegroundColor Green
Write-Host ""

# Issue #4
Write-Host "📝 Creating Issue #4: MANAGEMENT-SVC — GET /available..." -ForegroundColor Cyan
$issue4Body = @"
## 📄 Summary
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
- Flyway V3 migration (parking_spaces table already exists)
"@

$issue4Num = & gh issue create `
    --title "MANAGEMENT-SVC — GET /available (list available parking spaces)" `
    --body $issue4Body `
    --label "feature,management-service,backend,postgres,needs-tests" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Issue #4 created: #$issue4Num" -ForegroundColor Green
Write-Host ""

# Issue #5
Write-Host "📝 Creating Issue #5: MANAGEMENT-SVC — POST /update..." -ForegroundColor Cyan
$issue5Body = @"
## 📄 Summary
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
- Issue #$issue4Num : MANAGEMENT-SVC GET /available
"@

$issue5Num = & gh issue create `
    --title "MANAGEMENT-SVC — POST /update (update parking space status)" `
    --body $issue5Body `
    --label "feature,management-service,backend,postgres,needs-tests" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Issue #5 created: #$issue5Num" -ForegroundColor Green
Write-Host ""

# Issue #6
Write-Host "📝 Creating Issue #6: REPORTING-SVC — POST /log..." -ForegroundColor Cyan
$issue6Body = @"
## 📄 Summary
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
- Flyway V1 migration (logs table already exists)
"@

$issue6Num = & gh issue create `
    --title "REPORTING-SVC — POST /log (persist system logs)" `
    --body $issue6Body `
    --label "feature,reporting-service,backend,postgres,needs-tests" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Issue #6 created: #$issue6Num" -ForegroundColor Green
Write-Host ""

# Issue #7
Write-Host "📝 Creating Issue #7: DB — Flyway migrations..." -ForegroundColor Cyan
$issue7Body = @"
## 📄 Summary
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
- Epic: #$epicNum
"@

$issue7Num = & gh issue create `
    --title "DB — Flyway migrations for parking_spaces and logs (if missing)" `
    --body $issue7Body `
    --label "infra,database,flyway,backend" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Issue #7 created: #$issue7Num" -ForegroundColor Green
Write-Host ""

# Issue #8
Write-Host "📝 Creating Issue #8: Integration — API Gateway proxy..." -ForegroundColor Cyan
$issue8Body = @"
## 📄 Summary
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
- Issue #$issue4Num , #$issue5Num , #$issue6Num
"@

$issue8Num = & gh issue create `
    --title "Integration — Verify API Gateway proxying for new endpoints" `
    --body $issue8Body `
    --label "integration,api-gateway,devops,testing" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Issue #8 created: #$issue8Num" -ForegroundColor Green
Write-Host ""

# Issue #9
Write-Host "📝 Creating Issue #9: Tests & Docs..." -ForegroundColor Cyan
$issue9Body = @"
## 📄 Summary
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
- All previous issues (#$issue1Num -#$issue8Num )
"@

$issue9Num = & gh issue create `
    --title "Tests & Docs — Minimal tests and API docs for Phase 1" `
    --body $issue9Body `
    --label "tests,docs,backend,ci" `
    --milestone "Phase 1 (3 weeks)" `
    --json number -q .number

Write-Host "✅ Issue #9 created: #$issue9Num" -ForegroundColor Green
Write-Host ""

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "✅ All issues created successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  - Epic: #$epicNum"
Write-Host "  - Issue #1 (CLIENT-SVC CRUD Clients): #$issue1Num"
Write-Host "  - Issue #2 (CLIENT-SVC CRUD Vehicles): #$issue2Num"
Write-Host "  - Issue #3 (CLIENT-SVC /check): #$issue3Num"
Write-Host "  - Issue #4 (MANAGEMENT-SVC /available): #$issue4Num"
Write-Host "  - Issue #5 (MANAGEMENT-SVC /update): #$issue5Num"
Write-Host "  - Issue #6 (REPORTING-SVC /log): #$issue6Num"
Write-Host "  - Issue #7 (DB Migrations): #$issue7Num"
Write-Host "  - Issue #8 (Integration): #$issue8Num"
Write-Host "  - Issue #9 (Tests & Docs): #$issue9Num"
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Review issues on GitHub"
Write-Host "  2. Add issues to project board if needed"
Write-Host "  3. Start with Issue #$issue1Num"
Write-Host ""
