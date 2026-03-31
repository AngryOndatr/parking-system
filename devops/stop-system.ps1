# Script to stop parking-system
# Usage: .\stop-system.ps1 [-RemoveVolumes]

param(
    [Parameter(Mandatory=$false)]
    [switch]$RemoveVolumes = $false
)

$projectRoot = Split-Path $PSScriptRoot -Parent
$composeFile = "$projectRoot\docker-compose.yml"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Parking System - Stopping" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Stopping all containers..." -ForegroundColor Yellow
if ($RemoveVolumes) {
    docker-compose -f $composeFile down -v
} else {
    docker-compose -f $composeFile down
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] System stopped" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Failed to stop system" -ForegroundColor Red
}

Write-Host ""
if ($RemoveVolumes) {
    Write-Host "[WARNING] Volumes were removed. Database data is lost." -ForegroundColor Red
}

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Container status:" -ForegroundColor Yellow
docker ps -a --filter "name=parking" --format "table {{.Names}}`t{{.Status}}"
Write-Host "=====================================" -ForegroundColor Cyan
