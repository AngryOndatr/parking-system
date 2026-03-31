# Скрипт для запуска системы parking-system
# Использование: .\start-system.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Parking System - Docker Compose" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path $PSScriptRoot -Parent
$composeFile = "$projectRoot\docker-compose.yml"

Write-Host "Запуск всей системы (инфраструктура + сервисы)..." -ForegroundColor Green
docker-compose -f $composeFile up -d

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
Write-Host "  docker-compose -f $composeFile logs -f" -ForegroundColor White
Write-Host ""
Write-Host "Для остановки используйте:" -ForegroundColor Yellow
Write-Host "  .\stop-system.ps1" -ForegroundColor White
Write-Host "=====================================" -ForegroundColor Cyan

