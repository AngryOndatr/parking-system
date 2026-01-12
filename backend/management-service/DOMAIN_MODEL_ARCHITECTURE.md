# Domain Model Architecture for Management Service

## Summary
Management Service has been refactored to use **Domain Model** pattern, following the same architecture as client-service. Business logic now works with domain objects that wrap JPA entities.

## Architecture Pattern

### Three-Layer Data Model

1. **Entity Layer** (JPA / Hibernate)
   - Location: `parking-common/entity/ParkingSpace.java`
   - Responsibility: Database mapping, persistence
   - Direct database interaction via Spring Data JPA

2. **Domain Layer** (Business Logic)
   - Location: `parking-common/domain/ParkingSpaceDomain.java`
   - Responsibility: Business logic, domain rules
   - Wraps entity, provides business methods

3. **DTO Layer** (API Contract)
   - Location: Generated from OpenAPI
   - Responsibility: API communication
   - `ParkingSpaceResponse` for responses

### Data Flow

```
Request (DTO) 
    ↓
Controller
    ↓
Service (works with Domain)
    ↓
Repository (returns Entity)
    ↓
Entity → Domain (wrapped)
    ↓
Domain → DTO (mapped)
    ↓
Response (DTO)
```

## Created Files

### ParkingSpaceDomain
**Location**: `backend/parking-common/src/main/java/com/parking/common/domain/ParkingSpaceDomain.java`

**Purpose**: Domain wrapper around ParkingSpace entity

**Key Features**:
- Delegates all getters/setters to underlying entity
- Provides business logic methods:
  - `isAvailable()` - check if space is available
  - `supportsEVCharging()` - check EV charging support
  - `markAsOccupied()` - business logic for occupation
  - `markAsAvailable()` - business logic for release
  - `isSuitableFor(vehicleType)` - suitability check
- Maintains single source of truth (entity)
- Implements `equals()`, `hashCode()`, `toString()`

**Example Usage**:
```java
ParkingSpace entity = repository.findById(id);
ParkingSpaceDomain domain = new ParkingSpaceDomain(entity);

// Business logic in domain
if (domain.isAvailable() && domain.supportsEVCharging()) {
    domain.markAsOccupied();
    repository.save(domain.getEntity());
}
```

## Modified Files

### ManagementService
**Changes**:
- Now works with `ParkingSpaceDomain` instead of `ParkingSpace` entity
- Wraps repository results in domain objects
- Business logic uses domain methods
- Maps domain to DTO for responses

**Before**:
```java
List<ParkingSpace> spaces = repository.findByStatus("AVAILABLE");
return spaces.stream()
    .map(mapper::toResponse)
    .collect(Collectors.toList());
```

**After**:
```java
List<ParkingSpace> entities = repository.findByStatus("AVAILABLE");
List<ParkingSpaceDomain> domains = entities.stream()
    .map(ParkingSpaceDomain::new)
    .collect(Collectors.toList());
return domains.stream()
    .map(mapper::toResponse)
    .collect(Collectors.toList());
```

### ParkingSpaceMapper
**Changes**:
- Signature changed: `toResponse(ParkingSpace entity)` → `toResponse(ParkingSpaceDomain domain)`
- Maps from domain model, not directly from entity
- All field access through domain methods

**Benefit**: Mapper is decoupled from JPA layer

### Tests
**No changes needed!**
- Tests create entities directly via repository (test data setup)
- Service uses domain internally (encapsulated)
- Tests verify behavior, not implementation

## Benefits

### 1. Separation of Concerns
- **Entity**: Database concerns only
- **Domain**: Business logic only
- **DTO**: API contract only

### 2. Business Logic Centralization
All business rules in one place:
```java
public void markAsOccupied() {
    setStatus("OCCUPIED");
    setLastOccupiedAt(LocalDateTime.now());
}
```

### 3. Testability
- Can test business logic without database
- Can mock domain objects
- Clear boundaries between layers

### 4. Consistency
- Same pattern as `ClientDomain` in client-service
- Same pattern as `VehicleDomain` in client-service
- Easy to understand across services

### 5. Future-Proof
- Easy to add business logic without touching entity
- Can add validation in domain
- Can add calculated fields in domain

## Example Business Logic in Domain

### Check Availability
```java
public boolean isAvailable() {
    return "AVAILABLE".equals(getStatus());
}
```

### EV Charging Support
```java
public boolean supportsEVCharging() {
    return Boolean.TRUE.equals(getHasCharger()) && 
           getChargerType() != null;
}
```

### State Transitions
```java
public void markAsOccupied() {
    setStatus("OCCUPIED");
    setLastOccupiedAt(LocalDateTime.now());
}

public void markAsAvailable() {
    setStatus("AVAILABLE");
}
```

### Business Rules
```java
public boolean isSuitableFor(String vehicleType) {
    if ("ELECTRIC".equals(vehicleType)) {
        return supportsEVCharging();
    }
    return true; // All spaces suitable for standard
}
```

## Comparison with Client Service

### ClientDomain (existing)
```java
public class ClientDomain {
    private final Client entity;
    
    public ClientDomain(Client client) {
        this.entity = client;
    }
    
    public String getFullName() {
        return entity.getFullName();
    }
    
    // ... other delegating methods
}
```

### ParkingSpaceDomain (new)
```java
public class ParkingSpaceDomain {
    private final ParkingSpace entity;
    
    public ParkingSpaceDomain(ParkingSpace parkingSpace) {
        this.entity = parkingSpace;
    }
    
    public String getSpaceNumber() {
        return entity.getSpaceNumber();
    }
    
    // ... other delegating methods
    // + business logic methods
}
```

**Same pattern!** ✅

## Migration Checklist

- [x] Created `ParkingSpaceDomain` in parking-common
- [x] Added business logic methods to domain
- [x] Updated `ManagementService` to use domain
- [x] Updated `ParkingSpaceMapper` to map from domain
- [x] Verified tests still pass (no changes needed)
- [x] Documented architecture and benefits

## Future Enhancements

### Additional Business Logic (can be added to domain)
- Space reservation logic
- Price calculation based on duration
- Space compatibility with vehicle size
- Maintenance scheduling
- Usage statistics
- Smart allocation algorithms

### Example:
```java
public class ParkingSpaceDomain {
    // ...existing code...
    
    public boolean canAccommodateVehicle(int vehicleLength, int vehicleWidth) {
        return getLengthCm() >= vehicleLength && 
               getWidthCm() >= vehicleWidth;
    }
    
    public BigDecimal calculateCost(Duration duration) {
        // Business logic for cost calculation
        long hours = duration.toHours();
        if (hours > 24) {
            return getDailyRateOverride().multiply(
                BigDecimal.valueOf(hours / 24)
            );
        }
        return getHourlyRateOverride().multiply(
            BigDecimal.valueOf(hours)
        );
    }
}
```

## Build Instructions

1. Build parking-common first (contains domain model):
```bash
cd backend/parking-common
mvn clean install
```

2. Build management-service:
```bash
cd backend/management-service
mvn clean install
```

## Summary

The domain model architecture provides:
- ✅ Clear separation of concerns
- ✅ Centralized business logic
- ✅ Better testability
- ✅ Consistency across services
- ✅ Future-proof design

All business logic is now in the domain layer, where it belongs! 🎉

