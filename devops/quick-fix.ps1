# Quick Fix and Test for 403 Forbidden + Eureka Registration
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Quick Fix: 403 Forbidden + Eureka" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
# Step 1: Stop services
Write-Host "`nШаг 1: Остановка сервисов..." -ForegroundColor Yellow
docker-compose -f docker-compose.services.yml stop api-gateway client-service
docker-compose -f docker-compose.services.yml rm -f api-gateway client-service
Write-Host "Готово`n" -ForegroundColor Green
# Step 2: Maven rebuild
Write-Host "Шаг 2: Пересборка Maven (api-gateway + client-service)..." -ForegroundColor Yellow
Set-Location ..
mvn clean install -DskipTests -pl backend/api-gateway,backend/client-service -am
Set-Location devops
Write-Host "Готово`n" -ForegroundColor Green
# Step 3: Docker rebuild
Write-Host "Шаг 3: Пересборка Docker..." -ForegroundColor Yellow
docker-compose -f docker-compose.services.yml build --no-cache api-gateway client-service
Write-Host "Готово`n" -ForegroundColor Green
# Step 4: Start services
Write-Host "Шаг 4: Запуск сервисов..." -ForegroundColor Yellow
docker-compose -f docker-compose.services.yml up -d api-gateway client-service
Write-Host "Готово`n" -ForegroundColor Green
# Step 5: Wait for startup
Write-Host "Шаг 5: Ожидание 30 секунд для запуска и регистрации в Eureka..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
Write-Host "Готово`n" -ForegroundColor Green
# Step 6: Check Eureka registration
Write-Host "Шаг 6: Проверка регистрации в Eureka..." -ForegroundColor Yellow
try {
    $eurekaApps = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{Accept="application/json"}
    $registeredServices = $eurekaApps.applications.application.name
    Write-Host "Зарегистрированные сервисы в Eureka:" -ForegroundColor Cyan
    $registeredServices | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
    if ($registeredServices -contains "API-GATEWAY" -or $registeredServices -contains "api-gateway") {
        Write-Host "[OK] API Gateway зарегистрирован" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] API Gateway НЕ зарегистрирован!" -ForegroundColor Red
    }
    if ($registeredServices -contains "CLIENT-SERVICE" -or $registeredServices -contains "client-service") {
        Write-Host "[OK] Client Service зарегистрирован" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] Client Service НЕ зарегистрирован!" -ForegroundColor Red
    }
} catch {
    Write-Host "[FAIL] Не удалось получить список сервисов из Eureka" -ForegroundColor Red
}
Write-Host ""
# Step 7: Testing
Write-Host "Шаг 7: Тестирование..." -ForegroundColor Yellow
Write-Host "`nПрямой доступ к Client Service:" -ForegroundColor Cyan
try {
    $resp1 = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health"
    Write-Host "[OK] Статус = $($resp1.status)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host "`nДоступ к API Gateway:" -ForegroundColor Cyan
try {
    $resp2 = Invoke-RestMethod -Uri "http://localhost:8086/actuator/health"
    Write-Host "[OK] Статус = $($resp2.status)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host "`nПрокси через Gateway (без токена - ожидаем 401/403):" -ForegroundColor Cyan
try {
    $resp3 = Invoke-RestMethod -Uri "http://localhost:8086/api/clients"
    Write-Host "[НЕОЖИДАННО] Нет ошибки: $resp3" -ForegroundColor Yellow
} catch {
    if ($_.Exception.Response.StatusCode -eq 401 -or $_.Exception.Response.StatusCode -eq 403) {
        Write-Host "[ОЖИДАЕМО] $($_.Exception.Response.StatusCode) - требуется аутентификация" -ForegroundColor Yellow
    } else {
        Write-Host "[FAIL] $($_.Exception.Message)" -ForegroundColor Red
    }
}
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " Завершено" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""