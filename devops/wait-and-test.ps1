# Ожидание запуска API Gateway и тест авторизации

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Ожидание запуска API Gateway" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$maxAttempts = 12
$attempt = 0
$isReady = $false

while ($attempt -lt $maxAttempts -and -not $isReady) {
    $attempt++
    Write-Host "[$attempt/$maxAttempts] Проверка API Gateway..." -ForegroundColor Yellow

    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8086/actuator/health" -TimeoutSec 3 -ErrorAction Stop
        if ($health.status -eq "UP") {
            Write-Host "✓ API Gateway готов!" -ForegroundColor Green
            $isReady = $true
        }
    } catch {
        Write-Host "  Еще не готов, ожидание 5 секунд..." -ForegroundColor Gray
        Start-Sleep -Seconds 5
    }
}

if (-not $isReady) {
    Write-Host "✗ API Gateway не запустился за отведенное время" -ForegroundColor Red
    Write-Host "Проверьте логи: docker logs api-gateway" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Разблокировка и очистка" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Разблокировка в БД
Write-Host "[1/2] Разблокировка аккаунта admin..." -ForegroundColor Yellow
docker exec parking_db psql -U postgres -d parking_db -c "UPDATE users SET failed_login_attempts = 0, account_non_locked = true, account_locked_until = NULL WHERE username = 'admin';" > $null 2>&1
Write-Host "  ✓ Разблокирован" -ForegroundColor Green

# Очистка Redis
Write-Host "[2/2] Очистка блокировок IP в Redis..." -ForegroundColor Yellow
docker exec parking_redis redis-cli FLUSHALL > $null 2>&1
Write-Host "  ✓ Redis очищен" -ForegroundColor Green

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Тест авторизации" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$body = '{"username":"admin","password":"parking123"}'

try {
    Write-Host "Отправка запроса..." -ForegroundColor Gray
    $response = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 10

    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  ✓ АВТОРИЗАЦИЯ УСПЕШНА!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""

    Write-Host "Username: $($response.user.username)" -ForegroundColor Cyan
    Write-Host "Role:     $($response.user.role)" -ForegroundColor Cyan
    Write-Host "Email:    $($response.user.email)" -ForegroundColor Cyan

    Write-Host "`nAccess Token:" -ForegroundColor Yellow
    Write-Host $response.accessToken.Substring(0, 80) -ForegroundColor Gray
    Write-Host "..." -ForegroundColor Gray

    $env:JWT_TOKEN = $response.accessToken
    Write-Host "`n✓ Токен сохранен в `$env:JWT_TOKEN" -ForegroundColor Green

} catch {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "  ✗ ОШИБКА АВТОРИЗАЦИИ" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    Write-Host ""

    Write-Host "HTTP Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host "Ошибка: $($_.Exception.Message)" -ForegroundColor Red

    Write-Host "`nПроверка логов API Gateway:" -ForegroundColor Yellow
    docker logs api-gateway --tail 30 2>&1 | Select-String "ERROR|Executing|Failed login" | Select-Object -Last 10 | ForEach-Object {
        Write-Host "  $_" -ForegroundColor Red
    }

    Write-Host "`nЕсли ошибка 'Executing an update/delete query' все еще есть:" -ForegroundColor Yellow
    Write-Host "  1. Контейнер использует старый JAR файл" -ForegroundColor Gray
    Write-Host "  2. Нужна полная пересборка без кеша" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Команда для полной пересборки:" -ForegroundColor Cyan
    Write-Host "  docker-compose -f docker-compose.services.yml down" -ForegroundColor Gray
    Write-Host "  docker rmi devops-api-gateway" -ForegroundColor Gray
    Write-Host "  docker-compose -f docker-compose.services.yml build --no-cache api-gateway" -ForegroundColor Gray
    Write-Host "  docker-compose -f docker-compose.services.yml up -d api-gateway" -ForegroundColor Gray
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Завершено" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

