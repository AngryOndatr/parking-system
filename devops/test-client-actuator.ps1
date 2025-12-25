# Тест доступности client-service actuator endpoints

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Client Service - Actuator Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Проверка 1: Контейнер запущен?
Write-Host "1️⃣  Проверка статуса контейнера..." -ForegroundColor Yellow
$container = docker ps --filter "name=client-service" --format "{{.Names}}: {{.Status}}"
if ($container) {
    Write-Host "✅ Контейнер запущен: $container" -ForegroundColor Green
} else {
    Write-Host "❌ Контейнер не найден или остановлен!" -ForegroundColor Red
    Write-Host "`nЗапустите контейнер:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose.services.yml up -d client-service" -ForegroundColor White
    exit 1
}

Start-Sleep -Seconds 2

# Проверка 2: Прямой доступ к health endpoint
Write-Host "`n2️⃣  Проверка /actuator/health (прямой доступ)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing -TimeoutSec 10
    Write-Host "✅ Успех! Статус: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Ответ: $($response.Content)" -ForegroundColor White
} catch {
    Write-Host "❌ Ошибка: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`nПроверьте логи:" -ForegroundColor Yellow
    Write-Host "  docker logs client-service --tail 50" -ForegroundColor White
}

Start-Sleep -Seconds 2

# Проверка 3: Доступ через API Gateway
Write-Host "`n3️⃣  Проверка через API Gateway..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/client-service/actuator/health" -UseBasicParsing -TimeoutSec 10
    Write-Host "✅ Успех! Статус: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Ответ: $($response.Content)" -ForegroundColor White
} catch {
    Write-Host "❌ Ошибка: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`nПроверьте:" -ForegroundColor Yellow
    Write-Host "  1. API Gateway запущен: docker ps --filter 'name=api-gateway'" -ForegroundColor White
    Write-Host "  2. Client Service зарегистрирован в Eureka: http://localhost:8761" -ForegroundColor White
}

Start-Sleep -Seconds 2

# Проверка 4: Регистрация в Eureka
Write-Host "`n4️⃣  Проверка регистрации в Eureka..." -ForegroundColor Yellow
try {
    $eureka = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps/CLIENT-SERVICE" -Method Get -ErrorAction Stop
    if ($eureka) {
        Write-Host "✅ CLIENT-SERVICE зарегистрирован в Eureka" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  CLIENT-SERVICE не найден в Eureka" -ForegroundColor Yellow
    Write-Host "   Подождите 30 секунд и попробуйте снова" -ForegroundColor White
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Тест завершен" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

