# ============================================
#  QUICK RESTART (without Maven rebuild)
# ============================================
# This script restarts Docker containers using existing JAR files
# Use this after Maven build is complete

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   QUICK RESTART (no Maven build)" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

$ErrorActionPreference = "Continue"
$startTime = Get-Date
$projectRoot = Split-Path $PSScriptRoot -Parent

Write-Host "This script will:" -ForegroundColor Gray
Write-Host "  1. Stop all running containers" -ForegroundColor Gray
Write-Host "  2. Rebuild Docker images from existing JARs" -ForegroundColor Gray
Write-Host "  3. Start all services in optimal order" -ForegroundColor Gray
Write-Host ""

# Step 1: Stop all containers
Write-Host "Step 1: Stopping all containers..." -ForegroundColor Yellow
Set-Location $projectRoot
docker-compose -f docker-compose.yml down 2>&1 | Out-Null
Write-Host "   OK - All containers stopped`n" -ForegroundColor Green

# Step 2: Rebuild Docker images only
Write-Host "Step 2: Rebuilding Docker images..." -ForegroundColor Yellow
Write-Host "   Building images for all services..." -ForegroundColor Gray
docker-compose -f docker-compose.yml build --parallel
Write-Host "   OK - All images rebuilt`n" -ForegroundColor Green

# Step 3: Start infrastructure
Write-Host "Step 3: Starting infrastructure (DB, Redis, Observability)..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml up -d postgres redis prometheus grafana jaeger otel-collector pgadmin
Start-Sleep -Seconds 10
Write-Host "   OK - Infrastructure started`n" -ForegroundColor Green

# Step 4: Start Eureka
Write-Host "Step 4: Starting Eureka Server..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml up -d eureka-server
Write-Host "   Waiting for Eureka to become healthy..." -ForegroundColor Gray

$maxWait = 60
$waited = 0
$eurekaHealthy = $false

while (-not $eurekaHealthy -and $waited -lt $maxWait) {
    Start-Sleep -Seconds 2
    $waited += 2
    $status = docker inspect --format='{{.State.Health.Status}}' eureka-server 2>$null
    if ($status -eq "healthy") {
        $eurekaHealthy = $true
        Write-Host "   Eureka is healthy (${waited}s)" -ForegroundColor Gray
    }
}

if (-not $eurekaHealthy) {
    Write-Host "   WARNING: Eureka not healthy" -ForegroundColor Yellow
}
Write-Host "   OK - Eureka Server started`n" -ForegroundColor Green

# Step 5: Start API Gateway
Write-Host "Step 5: Starting API Gateway..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml up -d api-gateway
Write-Host "   Waiting for API Gateway to become healthy..." -ForegroundColor Gray

$maxWait = 60
$waited = 0
$apiHealthy = $false

while (-not $apiHealthy -and $waited -lt $maxWait) {
    Start-Sleep -Seconds 2
    $waited += 2
    $status = docker inspect --format='{{.State.Health.Status}}' api-gateway 2>$null
    if ($status -eq "healthy") {
        $apiHealthy = $true
        Write-Host "   API Gateway is healthy (${waited}s)" -ForegroundColor Gray
    }
}

if (-not $apiHealthy) {
    Write-Host "   WARNING: API Gateway not healthy" -ForegroundColor Yellow
}
Write-Host "   OK - API Gateway started`n" -ForegroundColor Green

# Step 6: Start all microservices
Write-Host "Step 6: Starting all microservices..." -ForegroundColor Yellow
docker-compose -f docker-compose.yml up -d client-service gate-control-service billing-service management-service reporting-service

Write-Host "   Waiting for all services to become healthy (max 90s)..." -ForegroundColor Gray

$services = @("client-service", "gate-control-service", "billing-service", "management-service", "reporting-service")
$maxWait = 90
$waited = 0

while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 5
    $waited += 5

    $healthyCount = 0
    foreach ($svc in $services) {
        $status = docker inspect --format='{{.State.Health.Status}}' $svc 2>$null
        if ($status -eq "healthy") {
            $healthyCount++
        }
    }

    Write-Host "   Progress: $healthyCount/$($services.Count) services healthy (waited ${waited}s)" -ForegroundColor Gray

    if ($healthyCount -eq $services.Count) {
        break
    }
}

Write-Host "   OK - Microservices started`n" -ForegroundColor Green

# Final status
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   QUICK RESTART COMPLETE" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

$elapsed = (Get-Date) - $startTime
Write-Host "Total time: $($elapsed.ToString('mm\:ss'))" -ForegroundColor Green
Write-Host ""
Write-Host "Services:" -ForegroundColor Cyan
Write-Host "  Eureka:     http://localhost:8761" -ForegroundColor White
Write-Host "  API Gateway: http://localhost:8086" -ForegroundColor White
Write-Host "  Grafana:    http://localhost:3000" -ForegroundColor White
Write-Host "  Jaeger:     http://localhost:16686" -ForegroundColor White
Write-Host "  PgAdmin:    http://localhost:5050" -ForegroundColor White
Write-Host ""

