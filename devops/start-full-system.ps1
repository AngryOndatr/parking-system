# Полный запуск Parking System (Infrastructure + Services)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Parking System Full Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $PSScriptRoot

# Запуск инфраструктуры
Write-Host "[1/3] Запуск инфраструктуры..." -ForegroundColor Yellow
& .\start-infrastructure.ps1

Write-Host ""
Write-Host "[2/3] Ожидание готовности инфраструктуры (15 секунд)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Запуск сервисов
Write-Host "[3/3] Запуск микросервисов (API Gateway, Client Service)..." -ForegroundColor Yellow
docker-compose -f docker-compose.services.yml up -d

Write-Host ""
Write-Host "Ожидание запуска сервисов (30 секунд)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Статус всех контейнеров:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
docker ps --format "table {{.Names}}\t{{.Status}}"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Проверка сервисов:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Проверка API Gateway
Write-Host "`nAPI Gateway:" -ForegroundColor Cyan
$apiStatus = docker logs api-gateway 2>&1 | Select-String "Started ApiGatewayApplication"
if ($apiStatus) {
    Write-Host "✓ API Gateway запущен (http://localhost:8086)" -ForegroundColor Green
} else {
    Write-Host "○ API Gateway еще запускается..." -ForegroundColor Yellow
    docker logs api-gateway --tail 10
}

# Проверка Client Service
Write-Host "`nClient Service:" -ForegroundColor Cyan
$clientStatus = docker logs client-service 2>&1 | Select-String "Started ClientServiceApplication"
if ($clientStatus) {
    Write-Host "✓ Client Service запущен (http://localhost:8081)" -ForegroundColor Green
} else {
    Write-Host "○ Client Service еще запускается..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Тест получения JWT токена:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nОжидание готовности API Gateway..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

$body = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body
    Write-Host "✓ Успешно получен JWT токен!" -ForegroundColor Green
    Write-Host "Token: $($response.accessToken.Substring(0,50))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Ошибка получения токена: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Проверьте логи API Gateway:" -ForegroundColor Yellow
    Write-Host "docker logs api-gateway --tail 30" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Полезные команды:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Просмотр логов API Gateway:" -ForegroundColor White
Write-Host "  docker logs api-gateway -f" -ForegroundColor Gray
Write-Host ""
Write-Host "Просмотр логов Client Service:" -ForegroundColor White
Write-Host "  docker logs client-service -f" -ForegroundColor Gray
Write-Host ""
Write-Host "Перезапуск сервисов:" -ForegroundColor White
Write-Host "  docker-compose -f docker-compose.services.yml restart" -ForegroundColor Gray
Write-Host ""
Write-Host "Остановка всей системы:" -ForegroundColor White
Write-Host "  .\stop-system.ps1" -ForegroundColor Gray
Write-Host ""

