# Полная разблокировка и тест авторизации

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Разблокировка аккаунта и очистка Redis" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 1. Разблокировка в БД
Write-Host "[1/4] Разблокировка аккаунта admin в PostgreSQL..." -ForegroundColor Yellow
docker exec parking_db psql -U postgres -d parking_db -c "
UPDATE users
SET
    failed_login_attempts = 0,
    account_non_locked = true,
    account_locked_until = NULL,
    enabled = true
WHERE username = 'admin';
" > $null 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Аккаунт разблокирован" -ForegroundColor Green
} else {
    Write-Host "  ✗ Ошибка разблокировки" -ForegroundColor Red
}

# 2. Проверка статуса аккаунта
Write-Host "`n[2/4] Проверка статуса аккаунта..." -ForegroundColor Yellow
$accountStatus = docker exec parking_db psql -U postgres -d parking_db -t -c "
SELECT
    username,
    failed_login_attempts,
    account_non_locked,
    enabled
FROM users
WHERE username = 'admin';
"

Write-Host "  $accountStatus" -ForegroundColor Gray

# 3. Очистка Redis (блокировки IP)
Write-Host "`n[3/4] Очистка блокировок IP в Redis..." -ForegroundColor Yellow
docker exec parking_redis redis-cli FLUSHALL > $null 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Redis очищен (все IP разблокированы)" -ForegroundColor Green
} else {
    Write-Host "  ✗ Ошибка очистки Redis" -ForegroundColor Red
}

# 4. Проверка API Gateway
Write-Host "`n[4/4] Проверка статуса API Gateway..." -ForegroundColor Yellow
$apiStatus = docker ps --filter "name=api-gateway" --format "{{.Status}}"

if ($apiStatus -like "*Up*") {
    Write-Host "  ✓ API Gateway запущен" -ForegroundColor Green
} else {
    Write-Host "  ✗ API Gateway не запущен" -ForegroundColor Red
    Write-Host "  Запуск API Gateway..." -ForegroundColor Yellow
    docker-compose -f docker-compose.services.yml up -d api-gateway
    Write-Host "  Ожидание (30 секунд)..." -ForegroundColor Gray
    Start-Sleep -Seconds 30
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Тест авторизации" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$body = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

Write-Host "Отправка запроса на http://localhost:8086/api/auth/login..." -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 10

    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  ✓ АВТОРИЗАЦИЯ УСПЕШНА!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""

    Write-Host "Информация о пользователе:" -ForegroundColor Cyan
    Write-Host "  Username:  $($response.user.username)" -ForegroundColor White
    Write-Host "  Role:      $($response.user.role)" -ForegroundColor White
    Write-Host "  Email:     $($response.user.email)" -ForegroundColor White
    Write-Host "  Enabled:   $($response.user.enabled)" -ForegroundColor White

    Write-Host "`nAccess Token (первые 70 символов):" -ForegroundColor Cyan
    $tokenPreview = $response.accessToken.Substring(0, [Math]::Min(70, $response.accessToken.Length))
    Write-Host "  $tokenPreview..." -ForegroundColor Gray

    Write-Host "`nSession Info:" -ForegroundColor Cyan
    Write-Host "  Login Time:   $($response.loginTime)" -ForegroundColor White
    Write-Host "  IP Address:   $($response.ipAddress)" -ForegroundColor White

    # Сохранение токена в переменную окружения
    $env:JWT_TOKEN = $response.accessToken
    Write-Host "`n✓ Токен сохранен в переменную `$env:JWT_TOKEN" -ForegroundColor Green

    Write-Host ""
    Write-Host "Для использования токена в следующих запросах:" -ForegroundColor Yellow
    Write-Host '  $headers = @{ "Authorization" = "Bearer $env:JWT_TOKEN" }' -ForegroundColor Gray
    Write-Host '  Invoke-RestMethod -Uri "http://localhost:8081/api/clients" -Headers $headers' -ForegroundColor Gray

} catch {
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "  ✗ ОШИБКА АВТОРИЗАЦИИ" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    Write-Host ""

    Write-Host "Статус: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host "Ошибка: $($_.Exception.Message)" -ForegroundColor Red

    Write-Host "`nПроверка логов API Gateway (последние 20 строк):" -ForegroundColor Yellow
    docker logs api-gateway --tail 20 2>&1 | Select-String "ERROR|WARN|Failed|Executing|Suspicious" | ForEach-Object {
        if ($_ -match "ERROR") {
            Write-Host "  $_" -ForegroundColor Red
        } elseif ($_ -match "WARN") {
            Write-Host "  $_" -ForegroundColor Yellow
        } else {
            Write-Host "  $_" -ForegroundColor Gray
        }
    }

    Write-Host "`nВозможные причины:" -ForegroundColor Yellow
    Write-Host "  1. API Gateway еще не полностью запустился (подождите 30-60 секунд)" -ForegroundColor Gray
    Write-Host "  2. Ошибка 'Executing an update/delete query' все еще присутствует" -ForegroundColor Gray
    Write-Host "  3. Проблема с подключением к PostgreSQL" -ForegroundColor Gray
    Write-Host "  4. IP все еще заблокирован в Redis" -ForegroundColor Gray

    Write-Host "`nКоманды для диагностики:" -ForegroundColor Yellow
    Write-Host "  docker logs api-gateway --tail 50" -ForegroundColor Gray
    Write-Host "  docker exec parking_redis redis-cli KEYS '*'" -ForegroundColor Gray
    Write-Host "  docker exec parking_db psql -U postgres -d parking_db -c 'SELECT * FROM users WHERE username=''admin'';'" -ForegroundColor Gray
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Завершено" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

