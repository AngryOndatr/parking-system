# Management Service - Architecture Overview

## Complete Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    API REQUEST (JSON)                        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Controller (ManagementController)                          │
│  - Implements ParkingSpaceApi (generated from OpenAPI)      │
│  - Receives ParkingSpaceResponse DTOs                       │
│  - Delegates to Service layer                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Service (ManagementService)                                │
│  - Business logic layer                                     │
│  - Works with ParkingSpaceDomain objects                    │
│  - Wraps entities in domain                                 │
│  - Maps domain → DTO via Mapper                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Domain (ParkingSpaceDomain)                                │
│  - Wraps ParkingSpace entity                                │
│  - Business logic methods:                                  │
│    * isAvailable()                                          │
│    * markAsOccupied()                                       │
│    * supportsEVCharging()                                   │
│  - Single source of truth: underlying entity                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Repository (ParkingSpaceRepository)                        │
│  - Spring Data JPA interface                                │
│  - Query methods (findByStatus, etc.)                       │
│  - Returns/saves ParkingSpace entities                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Entity (ParkingSpace)                                      │
│  - JPA/Hibernate entity                                     │
│  - Mapped to parking_spaces table                           │
│  - Database concerns only                                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Database (PostgreSQL / H2)                                 │
│  - parking_spaces table                                     │
└─────────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### 1. API Layer (Controller)
**File**: `ManagementController.java`
**Responsibility**: HTTP communication
- Handle HTTP requests
- Validate input (via annotations)
- Call service methods
- Return HTTP responses
- **Does NOT contain business logic**

### 2. Service Layer
**File**: `ManagementService.java`
**Responsibility**: Business orchestration
- Coordinate operations
- Wrap entities in domain objects
- Apply business logic via domain
- Map domain → DTO
- **Contains orchestration, delegates logic to domain**

### 3. Domain Layer
**File**: `ParkingSpaceDomain.java` (in parking-common)
**Responsibility**: Business rules
- Encapsulate business logic
- State transitions (mark as occupied, etc.)
- Business validations
- Calculated properties
- **Contains actual business logic**

### 4. Repository Layer
**File**: `ParkingSpaceRepository.java`
**Responsibility**: Data access
- Database queries
- CRUD operations
- Custom query methods
- **No business logic**

### 5. Entity Layer
**File**: `ParkingSpace.java` (in parking-common)
**Responsibility**: Database mapping
- JPA annotations
- Column mappings
- Relationships
- **No business logic**

## Key Benefits

### Separation of Concerns
```
Controller → "How to communicate?"
Service    → "How to orchestrate?"
Domain     → "What are the rules?"
Repository → "How to persist?"
Entity     → "How to map to DB?"
```

### Testability
```java
// Test domain logic without database
@Test
void testBusinessLogic() {
    ParkingSpace entity = new ParkingSpace();
    ParkingSpaceDomain domain = new ParkingSpaceDomain(entity);
    
    domain.markAsOccupied();
    
    assertEquals("OCCUPIED", domain.getStatus());
    assertNotNull(domain.getLastOccupiedAt());
}
```

### Maintainability
- Business logic in one place (Domain)
- Easy to find and modify rules
- Changes don't ripple across layers

### Consistency
Same pattern across all services:
- client-service → ClientDomain
- management-service → ParkingSpaceDomain
- (future) vehicle-service → VehicleDomain

## Example Usage

### Service Layer
```java
@Transactional(readOnly = true)
public List<ParkingSpaceResponse> getAvailableSpaces() {
    // 1. Get entities from database
    List<ParkingSpace> entities = repository.findByStatus("AVAILABLE");
    
    // 2. Wrap in domain objects
    List<ParkingSpaceDomain> domains = entities.stream()
        .map(ParkingSpaceDomain::new)
        .collect(Collectors.toList());
    
    // 3. Apply business logic if needed
    // domains.forEach(d -> d.someBusinessLogic());
    
    // 4. Map to DTOs
    return domains.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
}
```

### Domain Layer
```java
public class ParkingSpaceDomain {
    private final ParkingSpace entity;
    
    // Business logic
    public void markAsOccupied() {
        setStatus("OCCUPIED");
        setLastOccupiedAt(LocalDateTime.now());
    }
    
    public boolean isAvailable() {
        return "AVAILABLE".equals(getStatus());
    }
    
    // Delegates to entity
    public String getStatus() {
        return entity.getStatus();
    }
}
```

## Future Extensions

### Smart Allocation
```java
public boolean isSuitableForVehicle(Vehicle vehicle) {
    // Business logic for space allocation
    if (vehicle.isElectric() && !supportsEVCharging()) {
        return false;
    }
    return canAccommodateSize(vehicle.getLength(), vehicle.getWidth());
}
```

### Pricing Logic
```java
public BigDecimal calculatePricing(Duration parkingDuration) {
    // Complex pricing logic
    if (parkingDuration.toHours() > 24) {
        return calculateDailyRate(parkingDuration);
    }
    return calculateHourlyRate(parkingDuration);
}
```

### Maintenance Scheduling
```java
public boolean needsMaintenance() {
    // Business rule for maintenance
    return Duration.between(getLastMaintenanceAt(), 
                           LocalDateTime.now())
                   .toDays() > 30;
}
```

---

**Architecture Status**: ✅ Implemented and documented
**Next Step**: Build and test the complete flow

