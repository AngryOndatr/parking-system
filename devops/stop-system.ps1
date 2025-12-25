# Script to stop parking-system
# Usage: .\stop-system.ps1 [infrastructure|services|all]

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("infrastructure", "services", "all")]
    [string]$Mode = "all",

    [Parameter(Mandatory=$false)]
    [switch]$RemoveVolumes = $false
)

$InfrastructureFile = "docker-compose.infrastructure.yml"
$ServicesFile = "docker-compose.services.yml"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Parking System - Stopping" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$volumeFlag = if ($RemoveVolumes) { "-v" } else { "" }

function Stop-Services {
    Write-Host "Stopping microservices..." -ForegroundColor Yellow
    if ($RemoveVolumes) {
        docker-compose -f $ServicesFile down -v
    } else {
        docker-compose -f $ServicesFile down
    }

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Services stopped" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Failed to stop services" -ForegroundColor Red
    }
}

function Stop-Infrastructure {
    Write-Host "Stopping infrastructure..." -ForegroundColor Yellow
    if ($RemoveVolumes) {
        docker-compose -f $InfrastructureFile down -v
    } else {
        docker-compose -f $InfrastructureFile down
    }

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Infrastructure stopped" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Failed to stop infrastructure" -ForegroundColor Red
    }
}

# Main logic (stop services first, then infrastructure)
switch ($Mode) {
    "infrastructure" {
        Stop-Infrastructure
    }
    "services" {
        Stop-Services
    }
    "all" {
        Stop-Services
        Stop-Infrastructure
    }
}

Write-Host ""
if ($RemoveVolumes) {
    Write-Host "[WARNING] Volumes were removed. Database data is lost." -ForegroundColor Red
}

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Container status:" -ForegroundColor Yellow
docker ps -a --filter "name=parking" --format "table {{.Names}}\t{{.Status}}"
Write-Host "=====================================" -ForegroundColor Cyan



