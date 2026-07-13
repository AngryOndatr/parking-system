# Скрипт для запуска инфраструктуры Parking System

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Parking System Infrastructure Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path $PSScriptRoot -Parent
$composeFile = "$projectRoot\docker-compose.yml"

# Шаг 1: Остановка всех контейнеров
Write-Host "[1/4] Остановка существующих контейнеров..." -ForegroundColor Yellow
docker-compose -f $composeFile down 2>$null
Start-Sleep -Seconds 3

# Шаг 2: Очистка сети
Write-Host "[2/4] Очистка Docker сетей..." -ForegroundColor Yellow
docker network prune -f 2>$null
Start-Sleep -Seconds 2

# Шаг 3: Запуск инфраструктуры
Write-Host "[3/4] Запуск инфраструктуры (PostgreSQL, Redis, Eureka, Observability)..." -ForegroundColor Yellow
docker-compose -f $composeFile up -d postgres redis eureka-server pgadmin prometheus grafana jaeger otel-collector

# Ожидание запуска
Write-Host "[4/4] Ожидание запуска контейнеров (30 секунд)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Проверка статуса
Write-Host ""
docker ps --format "table {{.Names}}`t{{.Status}}`t{{.Ports}}"
Write-Host ""

# Проверка проблемных контейнеров
Write-Host "Проверка OTEL Collector..." -ForegroundColor Cyan
$otelStatus = docker ps --filter "name=parking_otel_collector" --format "{{.Status}}"
if ($otelStatus -like "*Up*") {
    Write-Host "✓ OTEL Collector запущен" -ForegroundColor Green
} else {
    Write-Host "✗ OTEL Collector не запущен. Проверяем логи:" -ForegroundColor Red
    docker logs parking_otel_collector --tail 20
}
Write-Host ""

Write-Host "Проверка Jaeger..." -ForegroundColor Cyan
$jaegerStatus = docker ps --filter "name=parking_jaeger" --format "{{.Status}}"
if ($jaegerStatus -like "*Up*") {
    Write-Host "✓ Jaeger запущен" -ForegroundColor Green
} else {
    Write-Host "✗ Jaeger не запущен" -ForegroundColor Red
}
Write-Host ""

Write-Host "Проверка PostgreSQL..." -ForegroundColor Cyan
$dbStatus = docker ps --filter "name=parking_db" --format "{{.Status}}"
if ($dbStatus -like "*healthy*") {
    Write-Host "✓ PostgreSQL healthy" -ForegroundColor Green
} else {
    Write-Host "○ PostgreSQL starting..." -ForegroundColor Yellow
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Доступные сервисы:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PostgreSQL:       localhost:5433" -ForegroundColor White
Write-Host "pgAdmin:          http://localhost:5050" -ForegroundColor White
Write-Host "Redis:            localhost:6379" -ForegroundColor White
Write-Host "Eureka:           http://localhost:8761" -ForegroundColor White
Write-Host "Prometheus:       http://localhost:9090" -ForegroundColor White
Write-Host "Grafana:          http://localhost:3000 (admin/admin)" -ForegroundColor White
Write-Host "Jaeger UI:        http://localhost:16686" -ForegroundColor White
Write-Host "OTEL Collector:   http://localhost:4317 (gRPC)" -ForegroundColor White
Write-Host "                  http://localhost:4318 (HTTP)" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Для запуска всей системы выполните:" -ForegroundColor Green
Write-Host "docker-compose -f $composeFile up -d" -ForegroundColor Yellow
