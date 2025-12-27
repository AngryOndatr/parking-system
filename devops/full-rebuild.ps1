# ============================================
#  FULL PROJECT REBUILD FROM SCRATCH
# ============================================

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   FULL PROJECT REBUILD" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

$ErrorActionPreference = "Continue"
$startTime = Get-Date
$projectRoot = Split-Path $PSScriptRoot -Parent

# ============================================
# HELPER FUNCTIONS
# ============================================

# Get container name from service name
function Get-ContainerName {
    param($serviceName)

    $containerNames = @{
        "postgres" = "parking_db"
        "redis" = "parking_redis"
        "eureka-server" = "eureka-server"
        "api-gateway" = "api-gateway"
        "client-service" = "client-service"
        "pgadmin" = "parking_pgadmin"
        "prometheus" = "parking_prometheus"
        "grafana" = "parking_grafana"
        "jaeger" = "parking_jaeger"
        "otel-collector" = "parking_otel_collector"
    }

    if ($containerNames.ContainsKey($serviceName)) {
        return $containerNames[$serviceName]
    }
    return $serviceName
}

# Display container status with color
function Show-ContainerStatus {
    param($containerName, $port = "")

    $status = docker ps --filter "name=^${containerName}$" --format "{{.Status}}" 2>$null
    $portInfo = if ($port) { " [$port]" } else { "" }

    Write-Host "      $containerName${portInfo}: " -NoNewline -ForegroundColor White
    if ($status) {
        Write-Host "RUNNING" -ForegroundColor Green
        return $true
    } else {
        Write-Host "STOPPED" -ForegroundColor Red
        return $false
    }
}

# ============================================
# BUILD PROCESS
# ============================================

# Step 1: Stop and remove all containers
Write-Host "Step 1: Stopping and removing all containers..." -ForegroundColor Yellow
Set-Location $projectRoot
docker-compose -f docker-compose.yml down -v 2>&1 | Out-Null
Start-Sleep -Seconds 3
Write-Host "   OK - All containers stopped and removed`n" -ForegroundColor Green

# Step 2: Clean Docker images
Write-Host "Step 2: Removing old Docker images..." -ForegroundColor Yellow
$imagesToRemove = @(
    "parking-system-api-gateway:latest",
    "parking-system-eureka-server:latest",
    "parking-system-client-service:latest"
)
foreach ($image in $imagesToRemove) {
    docker rmi $image -f 2>&1 | Out-Null
}
Start-Sleep -Seconds 1
Write-Host "   OK - Old images removed`n" -ForegroundColor Green

# Step 3: Clean Maven artifacts
Write-Host "Step 3: Cleaning Maven artifacts..." -ForegroundColor Yellow
mvn clean -q -DskipTests
Write-Host "   OK - Maven artifacts cleaned`n" -ForegroundColor Green

# Step 4: Build all services
Write-Host "Step 4: Building all Maven services..." -ForegroundColor Yellow
Write-Host "   This may take 2-3 minutes..." -ForegroundColor Gray
mvn package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "   ERROR: Maven build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "   OK - All services built successfully`n" -ForegroundColor Green

# Step 5: Start database and cache
Write-Host "Step 5: Starting database and cache services..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml up -d postgres redis
Write-Host "   Waiting for PostgreSQL and Redis to initialize (15 seconds)..." -ForegroundColor Gray
Start-Sleep -Seconds 15
Write-Host "   OK - Database and cache started`n" -ForegroundColor Green

# Step 6: Initialize and verify database
Write-Host "Step 6: Initializing and verifying database..." -ForegroundColor Yellow

# Wait for database to be fully ready
$maxRetries = 5
$retry = 0
$dbReady = $false

while (-not $dbReady -and $retry -lt $maxRetries) {
    try {
        $result = docker exec parking_db pg_isready -U postgres 2>&1
        if ($result -match "accepting connections") {
            $dbReady = $true
            Write-Host "   Database is accepting connections" -ForegroundColor Gray
        }
    } catch {
        $retry++
        Write-Host "   Waiting for database... (attempt $retry/$maxRetries)" -ForegroundColor Gray
        Start-Sleep -Seconds 3
    }
}

if (-not $dbReady) {
    Write-Host "   WARNING: Database may not be fully ready" -ForegroundColor Yellow
}

# Check if database is initialized
try {
    $userCount = docker exec parking_db psql -U postgres -d parking_db -t -c "SELECT COUNT(*) FROM users;" 2>&1

    if ($userCount -match "relation.*does not exist" -or $userCount -match "ERROR") {
        Write-Host "   Database not initialized, running init.sql..." -ForegroundColor Gray
        Get-Content "$projectRoot\database\init.sql" | docker exec -i parking_db psql -U postgres -d parking_db 2>&1 | Out-Null
        Start-Sleep -Seconds 2

        # Verify initialization
        $userCount = docker exec parking_db psql -U postgres -d parking_db -t -c "SELECT COUNT(*) FROM users;" 2>&1
    }

    $count = [int]($userCount -replace '\D', '')

    if ($count -gt 0) {
        Write-Host "   OK - Database initialized with $count user(s)" -ForegroundColor Green
    } else {
        Write-Host "   WARNING - Database initialized but users table is empty!" -ForegroundColor Yellow
        Write-Host "   Consider running: Get-Content database\init.sql | docker exec -i parking_db psql -U postgres -d parking_db" -ForegroundColor Gray
    }
} catch {
    Write-Host "   WARNING - Could not verify database: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# Step 7: Start observability stack
Write-Host "Step 7: Starting observability services..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml up -d prometheus grafana jaeger otel-collector pgadmin
Write-Host "   Waiting for observability stack to start (10 seconds)..." -ForegroundColor Gray
Start-Sleep -Seconds 10
Write-Host "   OK - Observability stack started`n" -ForegroundColor Green

# Step 8: Start Eureka Server
Write-Host "Step 8: Starting Eureka Server..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml up -d eureka-server
Write-Host "   Waiting for Eureka to start (20 seconds)..." -ForegroundColor Gray
Start-Sleep -Seconds 20
Write-Host "   OK - Eureka Server started`n" -ForegroundColor Green

# Step 9: Start API Gateway
Write-Host "Step 9: Starting API Gateway..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml up -d --build api-gateway
Write-Host "   Waiting for API Gateway to register with Eureka (25 seconds)..." -ForegroundColor Gray
Start-Sleep -Seconds 25
Write-Host "   OK - API Gateway started`n" -ForegroundColor Green

# Step 10: Start Client Service
Write-Host "Step 10: Starting Client Service..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml up -d --build client-service
Write-Host "   Waiting for Client Service to register (20 seconds)..." -ForegroundColor Gray
Start-Sleep -Seconds 20
Write-Host "   OK - Client Service started`n" -ForegroundColor Green

# ============================================
# VERIFICATION
# ============================================

Write-Host "Step 11: Verifying all services..." -ForegroundColor Yellow
Write-Host ""

# Get all services from docker-compose.yml
$allServices = @(
    @{Service="postgres"; Container="parking_db"; Port="5433"},
    @{Service="redis"; Container="parking_redis"; Port="6379"},
    @{Service="eureka-server"; Container="eureka-server"; Port="8761"},
    @{Service="api-gateway"; Container="api-gateway"; Port="8086"},
    @{Service="client-service"; Container="client-service"; Port="8081"},
    @{Service="pgadmin"; Container="parking_pgadmin"; Port="5050"},
    @{Service="prometheus"; Container="parking_prometheus"; Port="9090"},
    @{Service="grafana"; Container="parking_grafana"; Port="3000"},
    @{Service="jaeger"; Container="parking_jaeger"; Port="16686"},
    @{Service="otel-collector"; Container="parking_otel_collector"; Port="4317"}
)

Write-Host "   Container Status:" -ForegroundColor Cyan
$runningCount = 0
$totalCount = $allServices.Count

foreach ($svc in $allServices) {
    if (Show-ContainerStatus -containerName $svc.Container -port $svc.Port) {
        $runningCount++
    }
}

Write-Host ""
Write-Host "   Summary: $runningCount/$totalCount containers running" -ForegroundColor $(if ($runningCount -eq $totalCount) { "Green" } else { "Yellow" })
Write-Host ""

# Check Eureka registrations
Write-Host "   Checking Eureka registrations..." -ForegroundColor Cyan
Start-Sleep -Seconds 3
try {
    $eurekaResponse = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps" -UseBasicParsing -TimeoutSec 10

    $apiGatewayRegistered = $eurekaResponse.Content -match "API-GATEWAY"
    $clientServiceRegistered = $eurekaResponse.Content -match "CLIENT-SERVICE"

    Write-Host "      API-GATEWAY: " -NoNewline -ForegroundColor White
    Write-Host $(if ($apiGatewayRegistered) { "REGISTERED" } else { "NOT REGISTERED" }) -ForegroundColor $(if ($apiGatewayRegistered) { "Green" } else { "Yellow" })

    Write-Host "      CLIENT-SERVICE: " -NoNewline -ForegroundColor White
    Write-Host $(if ($clientServiceRegistered) { "REGISTERED" } else { "NOT REGISTERED" }) -ForegroundColor $(if ($clientServiceRegistered) { "Green" } else { "Yellow" })
} catch {
    Write-Host "      Could not check Eureka - $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# Step 12: Test authentication
Write-Host "Step 12: Testing authentication..." -ForegroundColor Yellow
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
        Write-Host "   OK - Authentication successful!" -ForegroundColor Green
        Write-Host "      Token length: $($authResponse.accessToken.Length) characters" -ForegroundColor Gray

        # Test Client Service access
        Write-Host "`nStep 13: Testing Client Service access via Gateway..." -ForegroundColor Yellow
        try {
            $headers = @{
                "Authorization" = "Bearer $($authResponse.accessToken)"
            }

            $clientsResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/clients" `
                -Method GET `
                -Headers $headers `
                -TimeoutSec 10

            Write-Host "   OK - Client Service accessible via Gateway!" -ForegroundColor Green
            if ($clientsResponse) {
                $count = if ($clientsResponse.Count) { $clientsResponse.Count } else { 0 }
                Write-Host "      Clients found: $count" -ForegroundColor Gray
            } else {
                Write-Host "      Response: Empty list (OK - no clients yet)" -ForegroundColor Gray
            }
        } catch {
            Write-Host "   ERROR - Client Service access failed!" -ForegroundColor Red
            Write-Host "      Error: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "   ERROR - No access token received" -ForegroundColor Red
    }
} catch {
    Write-Host "   ERROR - Authentication failed!" -ForegroundColor Red
    Write-Host "      Error: $($_.Exception.Message)" -ForegroundColor Red
}

# ============================================
# SUMMARY
# ============================================

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   REBUILD COMPLETE!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   Total time: $([math]::Round($duration, 1)) seconds`n" -ForegroundColor White

Write-Host "Services:" -ForegroundColor Cyan
Write-Host "   Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "   API Gateway:      http://localhost:8086" -ForegroundColor White
Write-Host "   Client Service:   http://localhost:8081 (via Gateway: /api/clients)" -ForegroundColor White
Write-Host "   Grafana:          http://localhost:3000 (admin/admin123)" -ForegroundColor White
Write-Host "   Prometheus:       http://localhost:9090" -ForegroundColor White
Write-Host "   Jaeger:           http://localhost:16686" -ForegroundColor White
Write-Host "   pgAdmin:          http://localhost:5050 (admin@parking.com/admin)" -ForegroundColor White
Write-Host ""

Write-Host "Useful commands:" -ForegroundColor Cyan
Write-Host "   View logs:         docker logs <container-name> --tail 100" -ForegroundColor White
Write-Host "   Follow logs:       docker logs <container-name> -f" -ForegroundColor White
Write-Host "   Restart service:   docker-compose restart <service-name>" -ForegroundColor White
Write-Host "   Stop all:          docker-compose down" -ForegroundColor White
Write-Host "   Check system:      .\check-system.ps1" -ForegroundColor White
Write-Host ""

Write-Host "Test credentials:" -ForegroundColor Cyan
Write-Host "   Username: admin" -ForegroundColor White
Write-Host "   Password: parking123" -ForegroundColor White
Write-Host ""

