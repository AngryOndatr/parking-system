# ============================================
#  Quick Rebuild and Restart API Gateway
# ============================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   REBUILD API GATEWAY" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Stop API Gateway container
Write-Host "Step 1: Stopping API Gateway container..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml stop api-gateway
Write-Host "OK`n" -ForegroundColor Green

# Step 2: Clear suspicious IPs from Redis (if needed)
Write-Host "Step 2: Clearing security cache in Redis..." -ForegroundColor Yellow
docker exec parking_redis redis-cli FLUSHDB
Write-Host "OK`n" -ForegroundColor Green

# Step 3: Rebuild API Gateway
Write-Host "Step 3: Rebuilding API Gateway service..." -ForegroundColor Yellow
Set-Location C:\Users\user\Projects\parking-system\backend\api-gateway
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Maven build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "OK`n" -ForegroundColor Green

# Step 4: Rebuild and start container
Write-Host "Step 4: Rebuilding and starting container..." -ForegroundColor Yellow
Set-Location C:\Users\user\Projects\parking-system
docker-compose -f docker-compose.yml up -d --build api-gateway
Write-Host "OK`n" -ForegroundColor Green

# Step 5: Wait for startup
Write-Host "Step 5: Waiting for API Gateway to start (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
Write-Host "OK`n" -ForegroundColor Green

# Step 6: Check health
Write-Host "Step 6: Checking API Gateway health..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/actuator/health" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "OK - API Gateway is healthy!" -ForegroundColor Green
    } else {
        Write-Host "WARNING: Unexpected status code: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "ERROR: Health check failed - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   REBUILD COMPLETE!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Check logs: docker logs api-gateway --tail 50" -ForegroundColor White
Write-Host "  2. Test auth: .\test-login.ps1" -ForegroundColor White
Write-Host "  3. Test proxy: .\test-client-service-via-gateway.ps1" -ForegroundColor White

