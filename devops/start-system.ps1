# Скрипт для запуска системы parking-system
# Использование: .\start-system.ps1 [infrastructure|services|all]

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("infrastructure", "services", "all")]
    [string]$Mode = "all"
)

$InfrastructureFile = "docker-compose.infrastructure.yml"
$ServicesFile = "docker-compose.services.yml"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Parking System - Docker Compose" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

function Start-Infrastructure {
    Write-Host "Запуск инфраструктуры (PostgreSQL, Redis, Eureka)..." -ForegroundColor Green
    docker-compose -f $InfrastructureFile up -d

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Инфраструктура запущена" -ForegroundColor Green
        Write-Host ""
        Write-Host "Ожидание готовности Eureka Server (30 секунд)..." -ForegroundColor Yellow
        Start-Sleep -Seconds 30
        Write-Host "✓ Eureka Server готов" -ForegroundColor Green
    } else {
        Write-Host "✗ Ошибка запуска инфраструктуры" -ForegroundColor Red
        exit 1
    }
}

function Start-Services {
    Write-Host "Запуск микросервисов..." -ForegroundColor Green
    docker-compose -f $ServicesFile up -d

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Микросервисы запущены" -ForegroundColor Green
    } else {
        Write-Host "✗ Ошибка запуска микросервисов" -ForegroundColor Red
        exit 1
    }
}

function Show-Status {
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Cyan
    Write-Host "  Статус контейнеров" -ForegroundColor Cyan
    Write-Host "=====================================" -ForegroundColor Cyan
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Cyan
    Write-Host "  Доступные endpoints" -ForegroundColor Cyan
    Write-Host "=====================================" -ForegroundColor Cyan
    Write-Host "Eureka Server:    http://localhost:8761" -ForegroundColor White
    Write-Host "API Gateway:      http://localhost:8086" -ForegroundColor White
    Write-Host "Client Service:   http://localhost:8081" -ForegroundColor White
    Write-Host ""
    Write-Host "Через Gateway (Service Discovery):" -ForegroundColor Yellow
    Write-Host "  Client Service: http://localhost:8086/client-service/actuator/health" -ForegroundColor White
    Write-Host ""
}

# Основная логика
switch ($Mode) {
    "infrastructure" {
        Start-Infrastructure
    }
    "services" {
        Start-Services
    }
    "all" {
        Start-Infrastructure
        Start-Services
    }
}

Show-Status

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Для просмотра логов используйте:" -ForegroundColor Yellow
Write-Host "  docker-compose -f $InfrastructureFile logs -f" -ForegroundColor White
Write-Host "  docker-compose -f $ServicesFile logs -f" -ForegroundColor White
Write-Host ""
Write-Host "Для остановки используйте:" -ForegroundColor Yellow
Write-Host "  .\stop-system.ps1" -ForegroundColor White
Write-Host "=====================================" -ForegroundColor Cyan


