# Скрипт для запуска всей системы

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Запуск Parking System" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$projectRoot = Split-Path $PSScriptRoot -Parent
$composeFile = "$projectRoot\docker-compose.yml"

# Шаг 1: Остановка старых контейнеров
Write-Host "🛑 Остановка старых контейнеров..." -ForegroundColor Yellow
docker-compose -f $composeFile down 2>&1 | Out-Null
Write-Host "✅ Старые контейнеры остановлены`n" -ForegroundColor Green

# Шаг 2: Запуск инфраструктуры
Write-Host "🚀 Запуск инфраструктуры..." -ForegroundColor Cyan
docker-compose -f $composeFile up -d postgres redis eureka-server pgadmin
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Ошибка запуска инфраструктуры!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Инфраструктура запущена`n" -ForegroundColor Green

# Шаг 3: Ожидание готовности БД
Write-Host "🔧 Ожидание готовности базы данных..." -ForegroundColor Cyan
Start-Sleep -Seconds 10
Write-Host "✅ База данных готова`n" -ForegroundColor Green

# Шаг 4: Запуск сервисов
Write-Host "🚀 Запуск сервисов..." -ForegroundColor Cyan
docker-compose -f $composeFile up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Ошибка запуска сервисов!" -ForegroundColor Red
    Write-Host "Проверьте логи: docker-compose -f $composeFile logs" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Сервисы запущены`n" -ForegroundColor Green

# Шаг 5: Проверка статуса
Write-Host "📊 Статус контейнеров:" -ForegroundColor Cyan
docker ps --format "table {{.Names}}`t{{.Status}}`t{{.Ports}}" | Select-String -Pattern "parking|eureka|api-gateway|client-service|NAMES"

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  ✅ Система запущена!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`n📍 Доступные сервисы:" -ForegroundColor Cyan
Write-Host "  • Eureka Dashboard:    http://localhost:8761" -ForegroundColor White
Write-Host "  • API Gateway:         http://localhost:8086" -ForegroundColor White
Write-Host "  • Client Service:      http://localhost:8081" -ForegroundColor White
Write-Host "  • PostgreSQL:          localhost:5433" -ForegroundColor White
Write-Host "  • Redis:               localhost:6379" -ForegroundColor White
Write-Host "  • Prometheus:          http://localhost:9090" -ForegroundColor White
Write-Host "  • Grafana:             http://localhost:3000" -ForegroundColor White
Write-Host "  • Jaeger:              http://localhost:16686" -ForegroundColor White
Write-Host "  • pgAdmin:             http://localhost:5050" -ForegroundColor White
Write-Host ""
