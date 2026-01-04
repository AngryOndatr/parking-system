feat(client-service): implement CRUD for vehicles (#17)

## Summary
Implemented full CRUD functionality for vehicles in client-service with proper integration through API Gateway. All endpoints are working and tested.

---

## Features

### Backend - Client Service
**Vehicle CRUD Operations:**
- POST /api/vehicles - Create new vehicle
- GET /api/vehicles - List all vehicles  
- GET /api/vehicles/{id} - Get vehicle by ID
- PUT /api/vehicles/{id} - Update vehicle
- DELETE /api/vehicles/{id} - Delete vehicle

**Client-Vehicle Operations:**
- GET /api/clients/{clientId}/vehicles - Get client's vehicles
- POST /api/clients/{clientId}/vehicles - Add vehicle to client

**Technical Implementation:**
- VehicleController implements VehicleApi (OpenAPI generated)
- VehicleService with full CRUD logic and validation
- VehicleRepository extending JpaRepository
- Vehicle entity from parking-common module
- Proper error handling (404, 409 for conflicts)
- Comprehensive logging with SLF4J
- Domain model pattern (VehicleDomain wrapper)
- DTO mapping (VehicleRequest/Response)

### Backend - API Gateway
**Added VehicleProxyController:**
- Complete proxy implementation for all Vehicle endpoints
- Proper header forwarding and JWT propagation
- Detailed logging for debugging
- Error handling and status code forwarding

**Extended ClientProxyController:**
- Added GET /api/clients/{clientId}/vehicles proxy method
- Added POST /api/clients/{clientId}/vehicles proxy method
- URL encoding for query parameters (phone search fix)

### Frontend
**Added Vehicle Testing Interface in test-login.html:**
- Vehicle tab with all CRUD operations
- Create vehicle form with validation
- Get/Update/Delete vehicle by ID
- List all vehicles
- Client's vehicles section
- Add vehicle to client functionality
- Real-time JSON editing
- Result display with syntax highlighting

---

## Fixes

### fix(api-gateway): phone search encoding issue
**Problem:** Symbol `+` in phone numbers was decoded as space

**Root Cause:** 
- API Gateway: `+` not encoded when proxying to client-service
- Client Service: Received `%2B` but Spring didn't decode automatically

**Solution:**
- API Gateway: Manual URLEncoder.encode() for phone parameter
- Client Service: Manual URLDecoder.decode() in controller
- Result: +380501112233 correctly passes through the chain

### fix(api-gateway): missing proxy routes
**Problem:** Multiple 404/405 errors for various endpoints

**Fixed:**
- PUT /api/clients/{id} - Added proxy method
- GET /api/clients/search - Added with proper encoding
- GET /api/clients/{clientId}/vehicles - Added proxy method
- POST /api/clients/{clientId}/vehicles - Added proxy method
- All Vehicle endpoints - Created VehicleProxyController

### fix(frontend): JSON format issues
**Problem:** Frontend sent firstName/lastName, API expected fullName
**Solution:** Updated all JSON templates to use fullName as per OpenAPI spec

### fix(frontend): missing ID validation
**Problem:** Empty ID fields caused 404 errors
**Solution:** Added JavaScript validation functions for all ID-dependent operations

---

## Code Changes

### New Files
- VehicleController.java (client-service) - Implements VehicleApi interface
- VehicleService.java (client-service) - Business logic layer
- VehicleRepository.java (client-service) - JPA repository interface
- VehicleDomain.java (client-service) - Domain model wrapper
- VehicleProxyController.java (api-gateway) - Complete proxy for Vehicle endpoints

### Modified Files
- ClientProxyController.java (api-gateway) - Added updateClient(), searchClientByPhone(), getClientVehicles(), addVehicleToClient()
- ClientController.java (client-service) - Added phone parameter URL decoding
- test-login.html - Added Vehicles tab UI and functions
- openapi.yaml (client-service) - Phone parameter required: false, separated VehicleApi
- application-test.properties (multiple services) - Added H2 compatibility settings

### Cleanup
- Removed test endpoint /api/clients/find-by-phone (was for debugging)
- Removed all troubleshooting .md files (13 files)
- Removed UriEncodingTest.java test utility

---

## Testing

### Unit Tests
- VehicleServiceTest - Service layer tests with Mockito
- VehicleControllerTest - MockMvc tests for all endpoints
- All tests pass with H2 in-memory database

### Integration Tests (via test-login.html)
- Create vehicle with valid data → 201 Created
- Create vehicle with duplicate license plate → 409 Conflict
- Get vehicle by ID → 200 OK or 404 Not Found
- Update vehicle → 200 OK
- Delete vehicle → 204 No Content
- List all vehicles → 200 OK
- Get client's vehicles → 200 OK
- Add vehicle to client → 201 Created

---

## Technical Details

### URL Encoding Solution
```java
// API Gateway - Encode before sending
String encodedPhone = URLEncoder.encode(phone, StandardCharsets.UTF_8);
// Result: +380... → %2B380...

// Client Service - Decode when receiving  
String decodedPhone = URLDecoder.decode(phone, StandardCharsets.UTF_8);
// Result: %2B380... → +380...
```

### Domain Model Pattern
```java
public class VehicleDomain {
    private Vehicle entity;
    // Business logic methods
    public boolean canBeDeleted() { ... }
    public void updateFrom(VehicleRequest request) { ... }
}
```

---

## Statistics

**Lines of Code:**
- VehicleController: ~120 lines
- VehicleService: ~200 lines  
- VehicleRepository: ~15 lines
- VehicleDomain: ~80 lines
- VehicleProxyController: ~200 lines
- ClientProxyController updates: +150 lines
- test-login.html updates: +200 lines
- Tests: ~300 lines

**Total:** ~1,265 lines of new/modified code

**Files Changed:** 15 files
**Files Created:** 7 files
**Files Deleted:** 14 files (cleanup)

---

## Deployment

### Build Commands
```bash
# Client Service
cd backend/client-service
mvn clean install -DskipTests

# API Gateway  
cd backend/api-gateway
mvn clean install -DskipTests

# Docker
cd devops
docker-compose build client-service api-gateway
docker-compose up -d client-service api-gateway
```

**Database:** No migrations needed - entities already exist in parking-common
**Configuration:** No environment variables added

---

## Acceptance Criteria

- [x] Endpoints: POST, GET, GET/{id}, PUT/{id}, DELETE/{id} for vehicles
- [x] Vehicle persisted to vehicles table with client_id foreign key
- [x] Unique license_plate enforced (409 Conflict on duplicate)
- [x] Validation errors returned properly (400 Bad Request)
- [x] Unit tests for create and list happy paths
- [x] Integration with API Gateway
- [x] Frontend testing interface
- [x] Logging and error handling
- [x] Documentation updated

---

## Related Issues

Closes #17
Related to #16 - both Client Service CRUD operations now complete

---

BREAKING CHANGE: None

