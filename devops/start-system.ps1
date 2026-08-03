# Скрипт для запуска системы parking-system
# Использование:
#   .\start-system.ps1          # dev (default) - override ports + pgAdmin + devops\.env.dev
#   .\start-system.ps1 -Prod    # production - devops\.env.prod, no override, restart policy

param(
    [Parameter(Mandatory=$false)]
    [switch]$Prod = $false
)

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Parking System - Docker Compose" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path $PSScriptRoot -Parent
$composeFile = "$projectRoot\docker-compose.yml"

if ($Prod) {
    $overlayFile = "$projectRoot\docker-compose.prod.yml"
    $envFile = "$projectRoot\devops\.env.prod"
    if (-not (Test-Path $envFile)) {
        Write-Host "[ERROR] $envFile not found. Copy devops\.env.prod.example to devops\.env.prod and fill in real secrets first." -ForegroundColor Red
        exit 1
    }
    Write-Host "Запуск в PRODUCTION конфигурации..." -ForegroundColor Yellow
} else {
    $overlayFile = "$projectRoot\docker-compose.override.yml"
    $envFile = "$projectRoot\devops\.env.dev"
    Write-Host "Запуск в DEVELOPMENT конфигурации..." -ForegroundColor Green
}

Write-Host "Запуск всей системы (инфраструктура + сервисы)..." -ForegroundColor Green
docker-compose -f $composeFile -f $overlayFile --env-file $envFile up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Система запущена" -ForegroundColor Green
} else {
    Write-Host "✗ Ошибка запуска системы" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Статус контейнеров" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
docker ps --format "table {{.Names}}`t{{.Status}}`t{{.Ports}}"

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Доступные endpoints" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Eureka Server:    http://localhost:8761" -ForegroundColor White
Write-Host "API Gateway:      http://localhost:8086" -ForegroundColor White
Write-Host "Client Service:   http://localhost:8081" -ForegroundColor White
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Для просмотра логов используйте:" -ForegroundColor Yellow
Write-Host "  docker-compose -f $composeFile -f $overlayFile --env-file $envFile logs -f" -ForegroundColor White
Write-Host ""
Write-Host "Для остановки используйте:" -ForegroundColor Yellow
Write-Host "  .\stop-system.ps1" -ForegroundColor White
Write-Host "=====================================" -ForegroundColor Cyan

