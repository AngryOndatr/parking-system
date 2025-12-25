# Скрипт для запуска системы с проверкой готовности Eureka
# Usage: .\start-with-health-check.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting Parking System with Health Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Функция проверки готовности Eureka
function Test-EurekaHealth {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8761/actuator/health" -TimeoutSec 2 -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            $content = $response.Content | ConvertFrom-Json
            return $content.status -eq "UP"
        }
    }
    catch {
        return $false
    }
    return $false
}

# 1. Остановка всех контейнеров
Write-Host "1. Stopping existing containers..." -ForegroundColor Yellow
docker-compose -f docker-compose.infrastructure.yml -f docker-compose.services.yml down
Write-Host "   OK: Containers stopped" -ForegroundColor Green
Write-Host ""

# 2. Запуск инфраструктуры
Write-Host "2. Starting infrastructure (PostgreSQL, Redis, Eureka)..." -ForegroundColor Yellow
docker-compose -f docker-compose.infrastructure.yml up -d
Write-Host "   OK: Infrastructure started" -ForegroundColor Green
Write-Host ""

# 3. Ожидание готовности Eureka с проверкой
Write-Host "3. Waiting for Eureka Server to be ready..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$isReady = $false

while ($attempt -lt $maxAttempts -and -not $isReady) {
    $attempt++
    Write-Host "   Attempt $attempt/$maxAttempts..." -NoNewline

    $isReady = Test-EurekaHealth

    if ($isReady) {
        Write-Host " READY!" -ForegroundColor Green
    } else {
        Write-Host " waiting..." -ForegroundColor Gray
        Start-Sleep -Seconds 3
    }
}

if (-not $isReady) {
    Write-Host "ERROR: Eureka did not start in $($maxAttempts * 3) seconds" -ForegroundColor Red
    Write-Host "   Check logs: docker logs eureka-server" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# 4. Дополнительная пауза для полной инициализации
Write-Host "4. Additional pause for full initialization (10 sec)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10
Write-Host "   OK: Ready" -ForegroundColor Green
Write-Host ""

# 5. Запуск сервисов
Write-Host "5. Starting microservices..." -ForegroundColor Yellow
docker-compose -f docker-compose.services.yml up -d
Write-Host "   OK: Services started" -ForegroundColor Green
Write-Host ""

# 6. Ожидание регистрации сервисов в Eureka
Write-Host "6. Waiting for service registration in Eureka (30 sec)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
Write-Host ""

# 7. Проверка статуса
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  System Status Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Проверка контейнеров
Write-Host "Running containers:" -ForegroundColor Cyan
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Where-Object { $_ -match "parking|eureka|api-gateway|client-service" }
Write-Host ""

# Проверка Eureka
Write-Host "Eureka Server:" -ForegroundColor Cyan
try {
    $eureka = Invoke-WebRequest -Uri "http://localhost:8761/actuator/health" -UseBasicParsing
    Write-Host "   OK: http://localhost:8761 - AVAILABLE" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: http://localhost:8761 - NOT AVAILABLE" -ForegroundColor Red
}
Write-Host ""

# Проверка API Gateway
Write-Host "API Gateway:" -ForegroundColor Cyan
try {
    $gateway = Invoke-WebRequest -Uri "http://localhost:8086/actuator/health" -UseBasicParsing
    Write-Host "   OK: http://localhost:8086 - AVAILABLE" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: http://localhost:8086 - NOT AVAILABLE" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "System started successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Useful links:" -ForegroundColor Yellow
Write-Host "  - Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "  - API Gateway:      http://localhost:8086" -ForegroundColor White
Write-Host "  - Prometheus:       http://localhost:9090" -ForegroundColor White
Write-Host "  - Grafana:          http://localhost:3000" -ForegroundColor White
Write-Host "  - Jaeger:           http://localhost:16686" -ForegroundColor White
Write-Host ""
Write-Host "To view logs:" -ForegroundColor Yellow
Write-Host "  docker logs -f api-gateway" -ForegroundColor White
Write-Host "  docker logs -f client-service" -ForegroundColor White
Write-Host ""

