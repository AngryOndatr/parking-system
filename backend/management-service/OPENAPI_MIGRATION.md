# OpenAPI Integration in Management Service

## Summary
Management Service has been migrated to OpenAPI-first approach, similar to client-service. All API contracts are now defined in `openapi.yaml` and code is generated automatically during build.

## Changes Made

### 1. OpenAPI Specification
**File**: `src/main/resources/openapi.yaml`

- Defined all 5 endpoints with full documentation
- Created `ParkingSpaceResponse` schema with all fields
- Added enums for `type` and `status` fields
- Used `nullable` properties with proper JsonNullable support
- Defined common error responses

### 2. Maven Configuration
**File**: `pom.xml`

Added dependencies:
- `spring-boot-starter-validation` - for Jakarta Bean Validation
- `jackson-databind-nullable` - for JsonNullable support
- `lombok` - for code generation
- `jakarta.annotation-api` - for generated annotations

Added plugins:
- `openapi-generator-maven-plugin` (v7.6.0) - generates interfaces and models
- `maven-compiler-plugin` - configured with Lombok annotation processor
- `maven-clean-plugin` - cleans generated sources

Configuration:
```xml
<generatorName>spring</generatorName>
<modelPackage>com.parking.management_service.generated.model</modelPackage>
<apiPackage>com.parking.management_service.generated.controller</apiPackage>
<configOptions>
    <useJakartaEe>true</useJakartaEe>
    <interfaceOnly>true</interfaceOnly>
    <useTags>true</useTags>
    <useBeanValidation>true</useBeanValidation>
    <skipDefaultInterface>true</skipDefaultInterface>
</configOptions>
```

### 3. Generated Code Structure

The plugin generates:
```
target/generated-sources/openapi/
├── com/parking/management_service/generated/
│   ├── controller/
│   │   └── ParkingSpaceApi.java        # Interface to implement
│   └── model/
│       ├── ParkingSpaceResponse.java   # DTO with builders
│       ├── ApiError.java                # Error response
│       └── ...enums...
```

### 4. Controller Implementation
**File**: `ManagementController.java`

```java
@RestController
public class ManagementController implements ParkingSpaceApi {
    // Implements all methods from generated interface
    // No need for @RequestMapping, @GetMapping, etc. - comes from interface
}
```

### 5. Updated Mapper
**File**: `ParkingSpaceMapper.java`

Key changes:
- Maps to generated `ParkingSpaceResponse` instead of custom DTO
- Handles `JsonNullable<T>` wrappers for nullable fields
- Converts String to Enum types (Type Enum, StatusEnum)
- Converts `BigDecimal` → `Double` for rates
- Converts `LocalDateTime` → `OffsetDateTime` with UTC zone

Example:
```java
response.setSection(entity.getSection() != null ? 
        JsonNullable.of(entity.getSection()) : JsonNullable.undefined());

response.setType(ParkingSpaceResponse.TypeEnum.fromValue(entity.getSpaceType()));
```

### 6. Updated Service Layer
**File**: `ManagementService.java`

- Changed return types from `ParkingSpaceResponseDto` to `ParkingSpaceResponse`
- Uses mapper method `toResponse()` instead of `toResponseDto()`
- No business logic changes

### 7. Updated Tests

#### ManagementControllerTest
- Created helper method `createTestSpace()` to build test objects with proper types
- All test DTOs use `JsonNullable` and Enum types correctly

#### ManagementServiceIntegrationTest
- Updated assertions to compare Enum values instead of Strings
- Example: `ParkingSpaceResponse.StatusEnum.AVAILABLE` instead of `"AVAILABLE"`

## API Contract Changes

### Nullable Fields
Fields marked as `nullable: true` in OpenAPI are wrapped in `JsonNullable<T>`:
- `section`
- `chargerType`
- `lengthCm` / `widthCm`
- `hourlyRateOverride` / `dailyRateOverride`
- `lastOccupiedAt`

### Enum Types
String fields with defined values become Enums:
- `type` → `ParkingSpaceResponse.TypeEnum`
- `status` → `ParkingSpaceResponse.StatusEnum`

## Build Process

1. **Clean** - removes old generated code
2. **Generate Sources** - OpenAPI generator creates interfaces and models
3. **Compile** - compiles both hand-written and generated code
4. **Test** - runs unit and integration tests
5. **Package** - creates JAR with all code

```bash
mvn clean install
```

## Benefits

✅ **Contract-First Development**
- API contract is the source of truth
- Frontend and backend can develop in parallel
- Changes are visible in Git diffs

✅ **Auto-Generated Documentation**
- Swagger UI automatically updated
- No manual Swagger annotations needed
- Accessible at `/swagger-ui.html`

✅ **Type Safety**
- Enums prevent invalid values
- JsonNullable distinguishes null vs undefined
- Compile-time verification

✅ **Consistency**
- Same approach as client-service
- Standardized across microservices
- Easier to maintain

✅ **Less Boilerplate**
- No manual DTO creation
- No manual controller annotations
- Mapper focuses on business logic only

## Swagger UI

Access auto-generated documentation:
```
http://localhost:8083/swagger-ui.html
```

Features:
- Interactive API testing
- Schema definitions
- Example requests/responses
- Try-it-out functionality

## Migration Checklist

- [x] Created `openapi.yaml` with all endpoints
- [x] Added OpenAPI generator plugin to `pom.xml`
- [x] Added required dependencies (validation, jackson-databind-nullable)
- [x] Updated controller to implement generated interface
- [x] Updated mapper to work with generated models
- [x] Updated service to return generated DTOs
- [x] Fixed all unit tests
- [x] Fixed all integration tests
- [x] Removed old manual DTO class
- [x] Verified build and tests pass
- [x] Documented changes

## Test Results

```
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
✅ All tests passing
```

- ManagementControllerTest: 7 tests
- ManagementServiceApplicationTests: 1 test
- ManagementServiceIntegrationTest: 8 tests

## Next Steps

1. Update API Gateway routing to management-service endpoints
2. Add security configuration (JWT validation)
3. Add integration tests with running Docker containers
4. Consider adding pagination for list endpoints
5. Add rate limiting configuration

## Related Files

- `src/main/resources/openapi.yaml` - API specification
- `backend/management-service/pom.xml` - Maven configuration
- `ManagementController.java` - Controller implementation
- `ParkingSpaceMapper.java` - Entity to DTO mapper
- `ManagementService.java` - Business logic
- `README.md` - Service documentation (updated)

## Compatibility

- ✅ Compatible with existing database schema
- ✅ No breaking changes to API responses
- ✅ JSON serialization works as before
- ✅ All existing tests updated and passing

---

**Migration completed successfully** ✅
All code uses OpenAPI-generated contracts, following the same pattern as client-service.

