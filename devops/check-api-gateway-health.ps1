# Complete API Gateway Health Check

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  API GATEWAY COMPLETE HEALTH CHECK" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$issues = 0
$checks = 0

# 1. Container Running
Write-Host "[1/6] Container Status..." -ForegroundColor Yellow
$checks++
$containerStatus = docker ps --filter "name=api-gateway" --format "{{.Status}}" 2>&1
if ($containerStatus -like "*Up*") {
    Write-Host "  [OK] Container is running" -ForegroundColor Green
    Write-Host "  Status: $containerStatus" -ForegroundColor Gray
} else {
    Write-Host "  [ERROR] Container is NOT running!" -ForegroundColor Red
    $issues++
}
Write-Host ""

# 2. Port Accessible
Write-Host "[2/6] Port 8086 Accessibility..." -ForegroundColor Yellow
$checks++
try {
    $health = Invoke-WebRequest -Uri "http://localhost:8086/actuator/health" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    Write-Host "  [OK] Port 8086 is accessible" -ForegroundColor Green
    Write-Host "  Health: $($health.Content)" -ForegroundColor Gray
} catch {
    Write-Host "  [ERROR] Port 8086 NOT accessible!" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
    $issues++
}
Write-Host ""

# 3. Eureka Registration
Write-Host "[3/6] Eureka Registration..." -ForegroundColor Yellow
$checks++
try {
    $eureka = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps/API-GATEWAY" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    if ($eureka.StatusCode -eq 200) {
        Write-Host "  [OK] Registered in Eureka" -ForegroundColor Green
        if ($eureka.Content -match "UP") {
            Write-Host "  Status: UP" -ForegroundColor Green
        } else {
            Write-Host "  [WARNING] Status: NOT UP" -ForegroundColor Yellow
            $issues++
        }
    }
} catch {
    Write-Host "  [ERROR] NOT registered in Eureka!" -ForegroundColor Red
    $issues++
}
Write-Host ""

# 4. Authentication Works
Write-Host "[4/6] Authentication Endpoint..." -ForegroundColor Yellow
$checks++
$loginBody = '{"username":"admin","password":"parking123"}'
try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -UseBasicParsing `
        -TimeoutSec 5 `
        -ErrorAction Stop

    Write-Host "  [OK] Login endpoint works" -ForegroundColor Green
    $token = ($loginResponse.Content | ConvertFrom-Json).accessToken
    if ($token) {
        Write-Host "  Token: $($token.Substring(0,30))..." -ForegroundColor Gray
    } else {
        Write-Host "  Token: received but empty" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  [ERROR] Login endpoint FAILED!" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
    $issues++
    $token = $null
}
Write-Host ""

# 5. Proxy Logs Visible
Write-Host "[5/6] Checking for Proxy Logs..." -ForegroundColor Yellow
$checks++
$recentLogs = docker logs api-gateway --tail 50 --since 5m 2>&1 | Out-String
if ($recentLogs -match "ClientProxy|Proxying") {
    Write-Host "  [OK] Proxy activity visible in logs" -ForegroundColor Green
    $proxyLines = $recentLogs -split "`n" | Select-String "ClientProxy|Proxying" | Select-Object -Last 3
    $proxyLines | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
} else {
    Write-Host "  WARNING: No proxy activity in recent logs" -ForegroundColor Yellow
    Write-Host "  (This might be OK if no requests were made)" -ForegroundColor Gray
}
Write-Host ""

# 6. Proxy Functionality
if ($token) {
    Write-Host "[6/6] Testing Proxy to Client Service..." -ForegroundColor Yellow
    $checks++
    try {
        $headers = @{ "Authorization" = "Bearer $token" }
        $proxyResponse = Invoke-WebRequest -Uri "http://localhost:8086/api/clients" `
            -Method GET `
            -Headers $headers `
            -UseBasicParsing `
            -TimeoutSec 5 `
            -ErrorAction Stop

        Write-Host "  [OK] Proxy works! Status: $($proxyResponse.StatusCode)" -ForegroundColor Green
        Write-Host "  Response length: $($proxyResponse.Content.Length) bytes" -ForegroundColor Gray
    } catch {
        Write-Host "  [ERROR] Proxy FAILED!" -ForegroundColor Red
        Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Gray
        $issues++
    }
} else {
    Write-Host "[6/6] Skipping proxy test (no token)" -ForegroundColor Gray
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Checks performed: $checks" -ForegroundColor White
Write-Host "Issues found: $issues" -ForegroundColor $(if ($issues -eq 0) { "Green" } else { "Red" })
Write-Host ""

if ($issues -eq 0) {
    Write-Host "[OK] ALL CHECKS PASSED!" -ForegroundColor Green
    Write-Host ""
    Write-Host "API Gateway is healthy and working correctly." -ForegroundColor White
} else {
    Write-Host "[WARNING] ISSUES DETECTED!" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Recommended actions:" -ForegroundColor White
    Write-Host "  1. Check logs: docker logs api-gateway --tail 100" -ForegroundColor Gray
    Write-Host "  2. Restart: docker-compose restart api-gateway" -ForegroundColor Gray
    Write-Host "  3. Rebuild: docker-compose build --no-cache api-gateway" -ForegroundColor Gray
    Write-Host "  4. Check Eureka: http://localhost:8761" -ForegroundColor Gray
}
Write-Host ""

