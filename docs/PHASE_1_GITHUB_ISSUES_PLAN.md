# Phase 1: GitHub Issues Plan

This document provides a structured breakdown of tasks for Phase 1 implementation into GitHub Issues.

## Epic: Phase 1 - Basic Backend Implementation (3 Weeks)

**Epic Description:**
Implement CRUD operations and database connectivity for Client Service, Management Service, and Reporting Service.

---

## Issue 1: Client Service - Vehicle CRUD Operations

**Title:** Implement Vehicle CRUD endpoints in Client Service

**Labels:** `enhancement`, `backend`, `client-service`, `phase-1`

**Description:**
Implement full CRUD operations for Vehicle entity in the Client Service.

**Acceptance Criteria:**
- [ ] VehicleRepository created with necessary query methods
- [ ] VehicleService implements business logic for CRUD operations
- [ ] VehicleController exposes REST endpoints:
  - `GET /api/clients/{clientId}/vehicles` - List all vehicles for a client
  - `POST /api/clients/{clientId}/vehicles` - Create new vehicle
  - `GET /api/clients/{clientId}/vehicles/{vehicleId}` - Get vehicle by ID
  - `PUT /api/clients/{clientId}/vehicles/{vehicleId}` - Update vehicle
  - `DELETE /api/clients/{clientId}/vehicles/{vehicleId}` - Delete vehicle
- [ ] Proper error handling implemented
- [ ] Unit tests written for service layer
- [ ] Integration tests written for controller

**Dependencies:** None

**Estimate:** 5 story points (3 days)

---

## Issue 2: Client Service - Subscription Check Endpoint

**Title:** Implement subscription check endpoint in Client Service

**Labels:** `enhancement`, `backend`, `client-service`, `phase-1`

**Description:**
Implement endpoint to check if a client has an active subscription.

**Acceptance Criteria:**
- [ ] Subscription entity created in parking-common
- [ ] SubscriptionRepository created with active subscription query
- [ ] SubscriptionService implements subscription validation logic
- [ ] SubscriptionController exposes endpoint:
  - `GET /api/clients/{clientId}/subscriptions/check`
- [ ] Response includes subscription details if active
- [ ] Unit tests written
- [ ] Integration tests written

**Dependencies:** None

**Estimate:** 3 story points (2 days)

---

## Issue 3: Management Service - Core Implementation

**Title:** Implement Management Service with parking space availability tracking

**Labels:** `enhancement`, `backend`, `management-service`, `phase-1`, `new-service`

**Description:**
Create Management Service to track parking space availability and provide management APIs.

**Acceptance Criteria:**
- [ ] ParkingLot and ParkingSpace entities created in parking-common
- [ ] Service structure created with Spring Boot
- [ ] Database configuration added (PostgreSQL)
- [ ] Eureka client registration configured
- [ ] SecurityConfig added
- [ ] Repositories created: ParkingLotRepository, ParkingSpaceRepository
- [ ] ManagementService implements business logic
- [ ] ManagementController exposes endpoints:
  - `GET /api/management/available` - Get available spaces
  - `GET /api/management/summary` - Get availability summary
  - `POST /api/management/spaces/{id}/update` - Update space status
- [ ] Dockerfile created
- [ ] Unit tests written
- [ ] Integration tests written

**Dependencies:** None

**Estimate:** 8 story points (5 days)

---

## Issue 4: Reporting Service - Core Implementation

**Title:** Implement Reporting Service for system logging

**Labels:** `enhancement`, `backend`, `reporting-service`, `phase-1`, `new-service`

**Description:**
Create Reporting Service to collect and store system logs with retrieval APIs.

**Acceptance Criteria:**
- [ ] Log entity created in parking-common
- [ ] Service structure created with Spring Boot
- [ ] Database configuration added (PostgreSQL)
- [ ] Eureka client registration configured
- [ ] SecurityConfig added
- [ ] LogRepository created with time-range queries
- [ ] ReportingService implements logging logic
- [ ] ReportingController exposes endpoints:
  - `POST /api/reporting/log` - Create log entry
  - `GET /api/reporting/logs` - Get logs with filters
  - `GET /api/reporting/logs/errors` - Get recent errors
- [ ] Dockerfile created
- [ ] Unit tests written
- [ ] Integration tests written

**Dependencies:** None

**Estimate:** 8 story points (5 days)

---

## Issue 5: Docker Compose - Service Integration

**Title:** Add Management and Reporting services to Docker Compose

**Labels:** `infrastructure`, `docker`, `phase-1`

**Description:**
Update docker-compose.yml to include the new Management and Reporting services.

**Acceptance Criteria:**
- [ ] management-service container added with:
  - Correct port mapping (8083:8083)
  - Database connection configured
  - Eureka connection configured
  - Health check configured
  - OpenTelemetry tracing configured
- [ ] reporting-service container added with:
  - Correct port mapping (8084:8084)
  - Database connection configured
  - Eureka connection configured
  - Health check configured
  - OpenTelemetry tracing configured
- [ ] Services can be started with `docker-compose up`
- [ ] Services register with Eureka successfully
- [ ] Health checks pass

**Dependencies:** Issue 3, Issue 4

**Estimate:** 2 story points (1 day)

---

## Issue 6: API Gateway - Route Configuration

**Title:** Configure API Gateway routes for Management and Reporting services

**Labels:** `enhancement`, `backend`, `api-gateway`, `phase-1`

**Description:**
Add routing configuration in API Gateway to forward requests to Management and Reporting services.

**Acceptance Criteria:**
- [ ] Routes added for Management Service:
  - `/api/management/**` → management-service
- [ ] Routes added for Reporting Service:
  - `/api/reporting/**` → reporting-service
- [ ] Service discovery working through Eureka
- [ ] Load balancing configured
- [ ] Timeout and retry policies configured
- [ ] Routes tested manually
- [ ] Documentation updated

**Dependencies:** Issue 3, Issue 4, Issue 5

**Estimate:** 3 story points (2 days)

---

## Issue 7: Integration Testing - End-to-End Scenarios

**Title:** Create integration tests for Phase 1 functionality

**Labels:** `testing`, `integration-test`, `phase-1`

**Description:**
Create comprehensive integration tests covering all Phase 1 functionality.

**Test Scenarios:**
- [ ] Vehicle CRUD operations through API Gateway
- [ ] Subscription check through API Gateway
- [ ] Parking space availability queries
- [ ] Parking space status updates
- [ ] Log creation and retrieval
- [ ] Service discovery and load balancing
- [ ] Database connectivity for all services
- [ ] Error handling scenarios

**Dependencies:** Issue 1, Issue 2, Issue 3, Issue 4, Issue 6

**Estimate:** 5 story points (3 days)

---

## Issue 8: OpenAPI Documentation

**Title:** Add OpenAPI specifications for new endpoints

**Labels:** `documentation`, `api`, `phase-1`

**Description:**
Create OpenAPI/Swagger documentation for all new API endpoints.

**Acceptance Criteria:**
- [ ] OpenAPI spec for Vehicle endpoints
- [ ] OpenAPI spec for Subscription endpoint
- [ ] OpenAPI spec for Management Service endpoints
- [ ] OpenAPI spec for Reporting Service endpoints
- [ ] Request/response examples included
- [ ] Error responses documented
- [ ] Swagger UI accessible for each service
- [ ] Postman collection created

**Dependencies:** Issue 1, Issue 2, Issue 3, Issue 4

**Estimate:** 3 story points (2 days)

---

## Issue 9: Monitoring and Observability

**Title:** Set up monitoring and dashboards for Phase 1 services

**Labels:** `observability`, `monitoring`, `phase-1`

**Description:**
Configure Prometheus metrics collection and Grafana dashboards for new services.

**Acceptance Criteria:**
- [ ] Prometheus scraping Management Service metrics
- [ ] Prometheus scraping Reporting Service metrics
- [ ] Grafana dashboard created for Management Service:
  - Available parking spaces over time
  - Space status distribution
  - API response times
- [ ] Grafana dashboard created for Reporting Service:
  - Log volume by level
  - Error rate over time
  - API response times
- [ ] OpenTelemetry traces visible in Jaeger
- [ ] Alerts configured for critical metrics

**Dependencies:** Issue 3, Issue 4, Issue 5

**Estimate:** 5 story points (3 days)

---

## Issue 10: Documentation Update

**Title:** Update project documentation for Phase 1

**Labels:** `documentation`, `phase-1`

**Description:**
Update README and other documentation to reflect Phase 1 changes.

**Acceptance Criteria:**
- [ ] README.md updated with:
  - New service descriptions
  - New API endpoints
  - Updated architecture diagram
  - Updated quick start guide
- [ ] PHASE_1_IMPLEMENTATION_SUMMARY.md reviewed and finalized
- [ ] Developer guide updated
- [ ] Deployment guide updated with new services
- [ ] Environment variable documentation updated

**Dependencies:** All other issues

**Estimate:** 2 story points (1 day)

---

## Summary

**Total Estimated Effort:** 44 story points (~4-5 weeks with buffer)

**Breakdown by Category:**
- Backend Development: 24 points (Issues 1-4)
- Infrastructure: 5 points (Issues 5-6)
- Testing: 5 points (Issue 7)
- Documentation: 5 points (Issues 8, 10)
- Observability: 5 points (Issue 9)

**Critical Path:**
1. Issues 1-4 can be done in parallel
2. Issue 5 depends on Issues 3-4
3. Issue 6 depends on Issue 5
4. Issue 7 depends on all implementation issues
5. Issues 8-10 can be done in parallel after implementation

**Recommended Sprint Structure:**
- **Sprint 1 (Week 1):** Issues 1, 2, 3, 4
- **Sprint 2 (Week 2):** Issues 5, 6, 8
- **Sprint 3 (Week 3):** Issues 7, 9, 10

---

## Creating Issues in GitHub

To create these issues in GitHub:

1. Navigate to your repository
2. Click on "Issues" tab
3. Click "New Issue"
4. Copy the title and description from each issue above
5. Add the specified labels
6. Set the estimate (if using story points)
7. Add to a milestone (e.g., "Phase 1 - Basic Backend")
8. Assign to team members as appropriate

Alternatively, you can use GitHub CLI to create issues programmatically:

```bash
gh issue create --title "TITLE" --body "DESCRIPTION" --label "label1,label2"
```

Or use GitHub's REST API to batch-create issues.
