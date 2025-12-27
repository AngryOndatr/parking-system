# Diagnostic script for API Gateway issues

Write-Host "`n=== API Gateway Diagnostics ===" -ForegroundColor Cyan
Write-Host ""

# 1. Check container status
Write-Host "[1/5] Checking container status..." -ForegroundColor Yellow
$containers = docker ps -a --format "{{.Names}}\t{{.Status}}" | Select-String "api-gateway"
if ($containers) {
    Write-Host $containers -ForegroundColor White
} else {
    Write-Host "  No api-gateway container found!" -ForegroundColor Red
}
Write-Host ""

# 2. Check if port is listening
Write-Host "[2/5] Checking if port 8086 is listening..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/actuator/health" -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
    Write-Host "  Port 8086 is UP - Status: $($response.StatusCode)" -ForegroundColor Green
}
catch {
    Write-Host "  Port 8086 not responding!" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host ""

# 3. Check Eureka registration
Write-Host "[3/5] Checking Eureka registration..." -ForegroundColor Yellow
try {
    $eureka = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps" -UseBasicParsing -ErrorAction Stop
    if ($eureka.Content -match "API-GATEWAY") {
        Write-Host "  API-GATEWAY is registered in Eureka" -ForegroundColor Green
    } else {
        Write-Host "  API-GATEWAY NOT found in Eureka!" -ForegroundColor Red
    }
}
catch {
    Write-Host "  Cannot connect to Eureka" -ForegroundColor Red
}
Write-Host ""

# 4. Check recent logs
Write-Host "[4/5] Recent API Gateway logs..." -ForegroundColor Yellow
$logs = docker logs api-gateway --tail 20 2>&1
if ($logs) {
    $logs | ForEach-Object {
        if ($_ -match "ERROR|WARN") {
            Write-Host "  $_" -ForegroundColor Yellow
        }
        elseif ($_ -match "Started|SUCCESS") {
            Write-Host "  $_" -ForegroundColor Green
        }
        else {
            Write-Host "  $_" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "  No logs available" -ForegroundColor Red
}
Write-Host ""

# 5. Test login endpoint
Write-Host "[5/5] Testing login endpoint..." -ForegroundColor Yellow
$body = '{"username":"admin","password":"parking123"}'
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -UseBasicParsing `
        -TimeoutSec 5 `
        -ErrorAction Stop

    Write-Host "  Login endpoint WORKS!" -ForegroundColor Green
    Write-Host "  Status: $($response.StatusCode)" -ForegroundColor Gray
}
catch {
    Write-Host "  Login endpoint FAILED!" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Response.StatusCode)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=== Diagnostics Complete ===" -ForegroundColor Cyan
Write-Host ""

# Recommendations
Write-Host "Recommendations:" -ForegroundColor Yellow
Write-Host "  1. If container not found: docker-compose up -d api-gateway" -ForegroundColor White
Write-Host "  2. If port not responding: Check container logs for errors" -ForegroundColor White
Write-Host "  3. If not in Eureka: Check EUREKA_CLIENT_SERVICEURL_DEFAULTZONE env var" -ForegroundColor White
Write-Host "  4. If login fails: Check database connection" -ForegroundColor White
Write-Host ""

