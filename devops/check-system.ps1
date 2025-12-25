# ============================================
#  Quick System Check
# ============================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   SYSTEM STATUS CHECK" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. Check Docker containers
Write-Host "1. Docker Containers:" -ForegroundColor Yellow
$containers = @(
    @{Name="parking_db"; Port="5433"},
    @{Name="parking_redis"; Port="6379"},
    @{Name="eureka-server"; Port="8761"},
    @{Name="api-gateway"; Port="8086"},
    @{Name="client-service"; Port="8081"}
)

foreach ($container in $containers) {
    $status = docker ps --filter "name=$($container.Name)" --format "{{.Status}}" 2>$null
    Write-Host "   $($container.Name) [$($container.Port)]:" -NoNewline -ForegroundColor White
    if ($status) {
        Write-Host " RUNNING" -ForegroundColor Green
    } else {
        Write-Host " STOPPED" -ForegroundColor Red
    }
}
Write-Host ""

# 2. Check Eureka Dashboard
Write-Host "2. Eureka Dashboard:" -ForegroundColor Yellow
try {
    $eureka = Invoke-WebRequest -Uri "http://localhost:8761" -UseBasicParsing -TimeoutSec 5
    Write-Host "   Status: " -NoNewline -ForegroundColor White
    Write-Host "ACCESSIBLE" -ForegroundColor Green
    Write-Host "   URL: http://localhost:8761" -ForegroundColor Gray
} catch {
    Write-Host "   Status: " -NoNewline -ForegroundColor White
    Write-Host "NOT ACCESSIBLE" -ForegroundColor Red
}
Write-Host ""

# 3. Check Service Registrations
Write-Host "3. Eureka Service Registrations:" -ForegroundColor Yellow
try {
    $eurekaApps = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps" -UseBasicParsing -TimeoutSec 5

    if ($eurekaApps.Content -match "API-GATEWAY") {
        Write-Host "   API-GATEWAY: " -NoNewline -ForegroundColor White
        Write-Host "REGISTERED" -ForegroundColor Green
    } else {
        Write-Host "   API-GATEWAY: " -NoNewline -ForegroundColor White
        Write-Host "NOT REGISTERED" -ForegroundColor Yellow
    }

    if ($eurekaApps.Content -match "CLIENT-SERVICE") {
        Write-Host "   CLIENT-SERVICE: " -NoNewline -ForegroundColor White
        Write-Host "REGISTERED" -ForegroundColor Green
    } else {
        Write-Host "   CLIENT-SERVICE: " -NoNewline -ForegroundColor White
        Write-Host "NOT REGISTERED" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Cannot check registrations" -ForegroundColor Red
}
Write-Host ""

# 4. Check API Gateway Health
Write-Host "4. API Gateway Health:" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8086/actuator/health" -TimeoutSec 5
    Write-Host "   Status: " -NoNewline -ForegroundColor White
    if ($health.status -eq "UP") {
        Write-Host "UP" -ForegroundColor Green
    } else {
        Write-Host "$($health.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Status: " -NoNewline -ForegroundColor White
    Write-Host "DOWN" -ForegroundColor Red
}
Write-Host ""

# 5. Test Authentication
Write-Host "5. Authentication Test:" -ForegroundColor Yellow
try {
    $body = @{
        username = "admin"
        password = "parking123"
    } | ConvertTo-Json

    $authResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 10

    if ($authResponse.accessToken) {
        Write-Host "   Login: " -NoNewline -ForegroundColor White
        Write-Host "SUCCESS" -ForegroundColor Green
        Write-Host "   Token length: $($authResponse.accessToken.Length) chars" -ForegroundColor Gray

        # 6. Test Client Service Access
        Write-Host "`n6. Client Service Access via Gateway:" -ForegroundColor Yellow
        try {
            $headers = @{
                "Authorization" = "Bearer $($authResponse.accessToken)"
            }

            $clientsResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/clients" `
                -Method GET `
                -Headers $headers `
                -TimeoutSec 10

            Write-Host "   Access: " -NoNewline -ForegroundColor White
            Write-Host "SUCCESS" -ForegroundColor Green
            if ($clientsResponse) {
                Write-Host "   Clients found: $($clientsResponse.Count)" -ForegroundColor Gray
            } else {
                Write-Host "   Response: Empty list (OK)" -ForegroundColor Gray
            }
        } catch {
            Write-Host "   Access: " -NoNewline -ForegroundColor White
            Write-Host "FAILED - $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "   Login: " -NoNewline -ForegroundColor White
        Write-Host "FAILED - No token received" -ForegroundColor Red
    }
} catch {
    Write-Host "   Login: " -NoNewline -ForegroundColor White
    Write-Host "FAILED - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   CHECK COMPLETE!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Quick links:" -ForegroundColor Cyan
Write-Host "   Eureka:     http://localhost:8761" -ForegroundColor White
Write-Host "   API Docs:   http://localhost:8086/api/docs" -ForegroundColor White
Write-Host "   Swagger:    http://localhost:8086/swagger-ui.html" -ForegroundColor White
Write-Host ""

Write-Host "Commands:" -ForegroundColor Cyan
Write-Host "   View logs:  docker logs <container-name> --tail 100" -ForegroundColor White
Write-Host "   Restart:    docker-compose restart <service-name>" -ForegroundColor White
Write-Host "   Stop all:   docker-compose down" -ForegroundColor White
Write-Host ""

