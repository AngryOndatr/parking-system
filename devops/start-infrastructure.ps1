# Скрипт для запуска и проверки инфраструктуры Parking System

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Parking System Infrastructure Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Переход в директорию devops
Set-Location "C:\Users\user\Projects\parking-system\devops"

# Шаг 1: Остановка всех контейнеров
Write-Host "[1/5] Остановка существующих контейнеров..." -ForegroundColor Yellow
docker-compose -f docker-compose.infrastructure.yml down 2>$null
docker-compose -f docker-compose.services.yml down 2>$null
Start-Sleep -Seconds 3

# Шаг 2: Очистка сети
Write-Host "[2/5] Очистка Docker сетей..." -ForegroundColor Yellow
docker network prune -f 2>$null
Start-Sleep -Seconds 2

# Шаг 3: Запуск инфраструктуры
Write-Host "[3/5] Запуск инфраструктуры (PostgreSQL, Redis, Eureka, Observability)..." -ForegroundColor Yellow
docker-compose -f docker-compose.infrastructure.yml up -d

# Ожидание запуска
Write-Host "[4/5] Ожидание запуска контейнеров (30 секунд)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Шаг 4: Проверка статуса
Write-Host "[5/5] Проверка статуса контейнеров..." -ForegroundColor Yellow
Write-Host ""
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
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
Write-Host "PostgreSQL:       http://localhost:5432" -ForegroundColor White
Write-Host "pgAdmin:          http://localhost:5050" -ForegroundColor White
Write-Host "Redis:            http://localhost:6379" -ForegroundColor White
Write-Host "Eureka:           http://localhost:8761" -ForegroundColor White
Write-Host "Prometheus:       http://localhost:9090" -ForegroundColor White
Write-Host "Grafana:          http://localhost:3000 (admin/admin123)" -ForegroundColor White
Write-Host "Jaeger UI:        http://localhost:16686" -ForegroundColor White
Write-Host "OTEL Collector:   http://localhost:4317 (gRPC)" -ForegroundColor White
Write-Host "                  http://localhost:4318 (HTTP)" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Для запуска сервисов выполните:" -ForegroundColor Green
Write-Host "docker-compose -f docker-compose.services.yml up -d" -ForegroundColor Yellow

