# Development Session — Bug Fixes & E2E Tests Green
## Session Date: 2026-03-07
## Issue: [Phase 2] E2E Test: Full Cycle for One-Time Visitor (billing/gate/integration) [#70](https://github.com/[org]/parking-system/issues/70)

---

## 🎯 Objective

Fix failing unit tests across the project and get the full build (unit + E2E) to **BUILD SUCCESS**
as part of Issue #70 — E2E full cycle for one-time parking visitor.

---

## ✅ Achievements

### All Tests Green
| Module | Tests | Status |
|---|---|---|
| api-gateway | 1 | ✅ |
| client-service | 26 | ✅ |
| billing-service | **55** | ✅ |
| gate-control-service | **47** | ✅ |
| management-service | 15 | ✅ |
| reporting-service | 16 | ✅ |
| e2e-tests | **1** | ✅ |
| **Total** | **161** | ✅ **BUILD SUCCESS** |

---

## 🐛 Bugs Fixed

### 1. `BillingController.getPaymentStatus` — 404 vs 200 (billing-service)

**Test**: `BillingControllerIntegrationTest.getPaymentStatus_NotFound:301`  
**Error**: `Expected status code <404> but was <200>`

**Root cause**: A previous change swallowed `ParkingEventNotFoundException` in `getPaymentStatus()`
and returned `200 OK` with `isPaid=false` (introduced for E2E compatibility).
This broke the correct HTTP contract.

**Fix** (`BillingController.java`):
```java
// Before — swallowed the exception:
} catch (ParkingEventNotFoundException e) {
    return ResponseEntity.ok(buildUnpaidResponse(parkingEventId)); // ← WRONG
}

// After — re-throws so GlobalExceptionHandler returns 404:
} catch (ParkingEventNotFoundException e) {
    throw e; // GlobalExceptionHandler → 404 Not Found
}
```

**Note**: E2E payment flow uses `/api/v1/billing/pay-test` and
`/api/v1/billing/status-by-ticket` endpoints — they are not affected.

---

### 2. `JacksonConfig` — `@DataJpaTest` slice failure (billing-service)

**Error**: `NoSuchBeanDefinitionException: Jackson2ObjectMapperBuilder`  
**Root cause**: `JacksonConfig.objectMapper()` depended on `Jackson2ObjectMapperBuilder`,
which is only available in web context. `@DataJpaTest` slices don't load it.

**Fix** (`JacksonConfig.java`): Create `ObjectMapper` directly without builder dependency:
```java
// Before:
@Bean
public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) { ... }

// After:
@Bean
public ObjectMapper objectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    om.registerModule(new JsonNullableModule());
    return om;
}
```

---

### 3. `PaymentStatusResponse.remainingFee` — `Double` vs `BigDecimal` (gate-control-service)

**Tests**: `BillingServiceClientTest` — `expected: BigDecimal@... but was: Double@...`  
**Root cause**: Field `remainingFee` was changed from `BigDecimal` to `Double`
(comment: *"Changed from BigDecimal to Double to match billing-service API"*).
But both the tests and `GateService` comparison logic expected `BigDecimal`.

**Fix** (`PaymentStatusResponse.java`):
```java
// Restored:
private BigDecimal remainingFee;
```

---

### 4. `BillingServiceClient` — parses fee as `Double` (gate-control-service)

**Root cause**: Both `checkPaymentStatus()` and `checkPaymentStatusByTicket()` methods
parsed the JSON `remainingFee` field as `Double` via `doubleValue()`.

**Fix** (`BillingServiceClient.java`): Parse via `BigDecimal` string constructor:
```java
// Before:
remainingFee = ((Number) remainingFeeObj).doubleValue();  // Double

// After:
remainingFee = new BigDecimal(remainingFeeObj.toString()); // BigDecimal — preserves scale
```

---

### 5. `GateServiceTest` — wrong mock + wrong type (gate-control-service)

**Tests**: `processExit_OneTimeVisitor_Paid_ExitAllowed`,
`processExit_OneTimeVisitor_NotPaid_ExitDenied`

**Two problems**:

a) Tests mocked `billingServiceClient.checkPaymentStatus(parkingEventId)`,
   but `GateService.processExit()` actually calls
   `billingServiceClient.checkPaymentStatusByTicket(ticketCode)`.
   Mock never fired → service got `null` → always returned `DENY`.

b) Tests set `remainingFee(0.0)` / `remainingFee(20.0)` — compile error after
   `PaymentStatusResponse.remainingFee` was restored to `BigDecimal`.

c) Unnecessary `gateEventRepository.findByTicketCode()` stubs caused
   `UnnecessaryStubbingException` in strict Mockito mode.

**Fix** (`GateServiceTest.java`):
```java
// Before (wrong method mocked, wrong type):
when(billingServiceClient.checkPaymentStatus(parkingEventId)).thenReturn(paymentStatus);
.remainingFee(0.0)

// After (correct method, correct type, no unused stubs):
when(billingServiceClient.checkPaymentStatusByTicket(ticketCode)).thenReturn(paymentStatus);
.remainingFee(BigDecimal.ZERO)
```

---

### 6. New script `devops/run-e2e-tests.ps1`

Created a standalone PowerShell script for running E2E tests with:
- Docker daemon health check (incl. named-pipe availability check)
- Maven auto-discovery
- Optional Maven build (`-SkipBuild`)
- Optional Docker image build (`-SkipDockerBuild`)
- Docker image existence validation
- Coloured pass/fail banner with elapsed time

**Usage**:
```powershell
# Full build + E2E:
.\devops\run-e2e-tests.ps1

# Only run E2E (images already built):
.\devops\run-e2e-tests.ps1 -SkipBuild

# Skip Docker build only:
.\devops\run-e2e-tests.ps1 -SkipDockerBuild
```

---

## 📊 E2E Test — Final Run

```
=== Step 1: Vehicle Entry ===         ✅ 201 Created  (ticket generated)
=== Step 2: Attempt Exit w/o Payment === ✅ 200 DENY  (payment required)
=== Step 3: Check Payment Status ===  ✅ 200 isPaid=false
=== Step 4: Process Payment ===       ✅ 201 Created  (CARD payment)
=== Step 5: Verify Payment Status === ✅ 200 isPaid=true
=== Step 6: Successful Exit ===       ✅ 200 OPEN     (gate opened)

[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Time elapsed: 107.642 s
[INFO] BUILD SUCCESS
```

---

## 📝 Files Changed

### billing-service
| File | Change |
|---|---|
| `controller/BillingController.java` | Re-throw `ParkingEventNotFoundException` in `getPaymentStatus()` |
| `config/JacksonConfig.java` | Remove `Jackson2ObjectMapperBuilder` dependency |

### gate-control-service
| File | Change |
|---|---|
| `dto/PaymentStatusResponse.java` | Restore `remainingFee` to `BigDecimal` |
| `client/BillingServiceClient.java` | Parse `remainingFee` as `BigDecimal` (both methods) |
| `test/.../GateServiceTest.java` | Fix mock method, fix type, remove unused stubs |

### devops
| File | Change |
|---|---|
| `run-e2e-tests.ps1` | **New** — standalone E2E runner script |

---

## 🔄 Next Steps

- ✅ Issue #70: All changes implemented — unit tests + E2E infrastructure green
- 🔲 Issue #70: Commit & close issue
- 🔲 Subscriber E2E test scenario
- 🔲 CI/CD integration (GitHub Actions) — run E2E on PR
- 🔲 Payment refund test scenario

