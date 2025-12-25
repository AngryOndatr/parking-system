# Полное исправление проблемы 403 Forbidden и перезапуск client-service

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Исправление 403 Forbidden" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$ErrorActionPreference = "Continue"

# Шаг 1: Остановить client-service
Write-Host "1️⃣  Остановка client-service..." -ForegroundColor Yellow
docker-compose -f docker-compose.services.yml stop client-service
docker-compose -f docker-compose.services.yml rm -f client-service
Write-Host "✅ Остановлен" -ForegroundColor Green
Start-Sleep -Seconds 2

# Шаг 2: Проверить исправления в коде
Write-Host "`n2️⃣  Проверка исправлений в коде..." -ForegroundColor Yellow
$filterPath = "..\backend\client-service\src\main\java\com\parking\client_service\security\JwtAuthenticationFilter.java"
if (Test-Path $filterPath) {
    $content = Get-Content $filterPath -Raw
    if ($content -match "Skip JWT validation for public endpoints") {
        Write-Host "✅ JwtAuthenticationFilter исправлен" -ForegroundColor Green
    } else {
        Write-Host "❌ JwtAuthenticationFilter НЕ исправлен!" -ForegroundColor Red
        Write-Host "   Требуется добавить проверку публичных endpoints" -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "❌ Файл не найден: $filterPath" -ForegroundColor Red
    exit 1
}

# Шаг 3: Проверить docker-compose.services.yml
Write-Host "`n3️⃣  Проверка docker-compose.services.yml..." -ForegroundColor Yellow
$composePath = "docker-compose.services.yml"
$composeContent = Get-Content $composePath -Raw
if ($composeContent -match '"8081:8081"') {
    Write-Host "✅ Маппинг портов правильный (8081:8081)" -ForegroundColor Green
} else {
    Write-Host "⚠️  Маппинг портов может быть неправильным" -ForegroundColor Yellow
}

# Шаг 4: Пересборка Maven
Write-Host "`n4️⃣  Пересборка Maven проекта..." -ForegroundColor Yellow
Set-Location ..
$buildOutput = mvn clean install -DskipTests -pl backend/client-service -am 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Maven сборка успешна" -ForegroundColor Green
} else {
    Write-Host "❌ Maven сборка провалилась!" -ForegroundColor Red
    Write-Host $buildOutput
    exit 1
}
Set-Location devops

# Шаг 5: Пересборка Docker образа
Write-Host "`n5️⃣  Пересборка Docker образа..." -ForegroundColor Yellow
docker-compose -f docker-compose.services.yml build --no-cache client-service
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Docker образ пересобран" -ForegroundColor Green
} else {
    Write-Host "❌ Ошибка при сборке Docker образа!" -ForegroundColor Red
    exit 1
}

# Шаг 6: Запуск контейнера
Write-Host "`n6️⃣  Запуск client-service..." -ForegroundColor Yellow
docker-compose -f docker-compose.services.yml up -d client-service
Start-Sleep -Seconds 3
Write-Host "✅ Контейнер запущен" -ForegroundColor Green

# Шаг 7: Ожидание инициализации
Write-Host "`n7️⃣  Ожидание инициализации (30 секунд)..." -ForegroundColor Yellow
for ($i = 30; $i -gt 0; $i--) {
    Write-Host "`r   $i секунд..." -NoNewline -ForegroundColor Cyan
    Start-Sleep -Seconds 1
}
Write-Host "`r   Готово!           " -ForegroundColor Green

# Шаг 8: Проверка логов
Write-Host "`n8️⃣  Проверка логов запуска..." -ForegroundColor Yellow
$logs = docker logs client-service --tail 10 2>&1
if ($logs -match "Started ClientServiceApplication") {
    Write-Host "✅ Сервис успешно запущен" -ForegroundColor Green
} else {
    Write-Host "⚠️  Сервис может быть еще не готов" -ForegroundColor Yellow
    Write-Host "   Последние логи:" -ForegroundColor White
    docker logs client-service --tail 20
}

# Шаг 9: Тестирование
Write-Host "`n9️⃣  Тестирование endpoints..." -ForegroundColor Yellow

# Тест 1: Прямой доступ
Write-Host "`n   Тест 1: http://localhost:8081/actuator/health" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -TimeoutSec 10
    if ($response.status -eq "UP") {
        Write-Host "   ✅ Прямой доступ работает!" -ForegroundColor Green
        Write-Host "      Status: $($response.status)" -ForegroundColor White
    } else {
        Write-Host "   ⚠️  Статус: $($response.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ❌ Ошибка: $($_.Exception.Message)" -ForegroundColor Red
}

# Тест 2: Через Gateway
Write-Host "`n   Тест 2: http://localhost:8086/client-service/actuator/health" -ForegroundColor Cyan
Start-Sleep -Seconds 2
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8086/client-service/actuator/health" -TimeoutSec 10
    if ($response.status -eq "UP") {
        Write-Host "   ✅ Доступ через Gateway работает!" -ForegroundColor Green
        Write-Host "      Status: $($response.status)" -ForegroundColor White
    } else {
        Write-Host "   ⚠️  Статус: $($response.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ❌ Ошибка: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`n   Проверьте:" -ForegroundColor Yellow
    Write-Host "   1. API Gateway запущен" -ForegroundColor White
    Write-Host "   2. Client Service зарегистрирован в Eureka: http://localhost:8761" -ForegroundColor White
}

# Тест 3: Регистрация в Eureka
Write-Host "`n   Тест 3: Регистрация в Eureka" -ForegroundColor Cyan
try {
    $eureka = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps/CLIENT-SERVICE" -Method Get -ErrorAction Stop
    Write-Host "   ✅ CLIENT-SERVICE зарегистрирован в Eureka" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️  CLIENT-SERVICE не найден в Eureka (может потребоваться время)" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Процесс завершен!" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Если проблема сохраняется:" -ForegroundColor Yellow
Write-Host "1. Проверьте логи: docker logs client-service" -ForegroundColor White
Write-Host "2. Проверьте Eureka: http://localhost:8761" -ForegroundColor White
Write-Host "3. Проверьте API Gateway логи: docker logs api-gateway`n" -ForegroundColor White

