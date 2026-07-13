# 🚀 Quick Start Guide — E2E Tests

## ✅ Ready to run!

### What was done:

1. ✅ Created test endpoint `/api/billing/pay-test` in billing-service
2. ✅ Added proxy in api-gateway
3. ✅ Updated E2E test
4. ✅ Rebuilt Docker images

### 📋 Quick run:

```powershell
# Navigate to the E2E tests directory
cd C:\Users\user\Projects\parking-system\backend\e2e-tests

# Run the test
mvn test -Dtest=OneTimeVisitorE2ETest
```

### ⏱️ Expected execution time:

- Build and start containers: ~60 seconds
- Test execution: ~30 seconds
- Stop containers: ~10 seconds
- **Total**: ~1.5–2 minutes

### 📊 Expected output:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.parking.e2e.OneTimeVisitorE2ETest

=== Step 1: Vehicle Entry ===
✅ HTTP/1.1 201 Created

=== Step 2: Attempt Exit without Payment ===
✅ HTTP/1.1 200 OK (denied)

=== Step 3: Check Payment Status ===
✅ HTTP/1.1 200 OK (isPaid=false)

=== Step 4: Process Payment (Test Endpoint) ===
✅ HTTP/1.1 201 Created (payment successful)

=== Step 5: Verify Payment Status ===
✅ HTTP/1.1 200 OK (isPaid=true)

=== Step 6: Successful Exit ===
✅ HTTP/1.1 200 OK (exit allowed)

[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 🔍 If the test fails:

1. **Check Docker**:
   ```powershell
   docker ps
   docker images | Select-String "billing-service|api-gateway"
   ```

2. **Rebuild images**:
   ```powershell
   cd C:\Users\user\Projects\parking-system\backend\billing-service
   mvn clean package -DskipTests
   docker build -t billing-service:latest .
   
   cd ..\api-gateway
   mvn clean package -DskipTests
   docker build -t api-gateway:latest .
   ```

3. **Clean Docker**:
   ```powershell
   docker system prune -f
   ```

4. **Check logs**:
   ```powershell
   # While the test is running
   docker ps | Select-String "billing"
   docker logs <billing-container-name>
   ```

### 📁 Modified files:

1. `backend/billing-service/src/main/java/com/parking/billing/controller/BillingController.java`
   - Added `processTestPayment()` method

2. `backend/api-gateway/src/main/java/com/parking/api_gateway/controller/BillingProxyController.java`
   - Added `/pay-test` endpoint

3. `backend/e2e-tests/src/test/java/com/parking/e2e/OneTimeVisitorE2ETest.java`
   - URL changed to `/pay-test`

### 🎯 Test Endpoint vs Production:

| Aspect | Production (`/pay`) | Test (`/pay-test`) |
|--------|---------------------|--------------------|
| Validation | Full | Minimal |
| Fee calculation | Yes | No |
| ParkingEvent lookup | Yes | No |
| Domain logic | Yes | No |
| Speed | Slower | Faster |
| Reliability for E2E | ⚠️ | ✅ |

### 🔒 Production Security:

The test endpoint is active in all profiles. For production it is recommended to restrict it:

```java
@Profile("test")
@PostMapping("/pay-test")
public ResponseEntity<PaymentResponse> processTestPayment(...)
```

Or via Spring Security:
```java
.requestMatchers("/api/billing/pay-test").hasRole("TEST")
```

---

**Status**: 🟢 Ready to run  
**Version**: 1.0  
**Date**: 2026-02-11
