# Скрипт для проверки авторизации в API Gateway
# Цель: Убедиться, что можем авторизоваться с логином и паролем администратора

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Проверка авторизации API Gateway" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Шаг 1: Проверка запущен ли API Gateway
Write-Host "[1/5] Проверка статуса API Gateway..." -ForegroundColor Yellow
$apiGatewayRunning = docker ps --filter "name=api-gateway" --format "{{.Names}}"

if ($apiGatewayRunning -eq "api-gateway") {
    Write-Host "✓ API Gateway контейнер запущен" -ForegroundColor Green
} else {
    Write-Host "✗ API Gateway не запущен. Запускаем..." -ForegroundColor Red
    docker-compose -f docker-compose.services.yml up -d api-gateway
    Write-Host "Ожидание запуска (30 секунд)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
}

# Шаг 2: Проверка логов API Gateway
Write-Host "`n[2/5] Проверка логов API Gateway..." -ForegroundColor Yellow
$startupLog = docker logs api-gateway 2>&1 | Select-String "Started ApiGatewayApplication"
if ($startupLog) {
    Write-Host "✓ API Gateway успешно запустился" -ForegroundColor Green
} else {
    Write-Host "○ API Gateway еще запускается или есть ошибки" -ForegroundColor Yellow
    Write-Host "`nПоследние 20 строк логов:" -ForegroundColor Gray
    docker logs api-gateway --tail 20
}

# Шаг 3: Проверка health endpoint
Write-Host "`n[3/5] Проверка health endpoint..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:8086/actuator/health" -TimeoutSec 5
    Write-Host "✓ Health endpoint отвечает: $($healthResponse.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ Health endpoint недоступен: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Возможные причины:" -ForegroundColor Yellow
    Write-Host "  1. API Gateway еще не полностью запустился (подождите 1-2 минуты)" -ForegroundColor Gray
    Write-Host "  2. Порт 8086 занят другим процессом" -ForegroundColor Gray
    Write-Host "  3. Ошибка в конфигурации" -ForegroundColor Gray
    exit 1
}

# Шаг 4: Проверка пользователей в БД
Write-Host "`n[4/5] Проверка пользователей в базе данных..." -ForegroundColor Yellow
$dbCheck = docker exec parking_db psql -U postgres -d parking_db -c "SELECT username, email, user_role, enabled FROM users;" 2>&1
if ($dbCheck -match "admin") {
    Write-Host "✓ Пользователь 'admin' найден в базе данных" -ForegroundColor Green
} else {
    Write-Host "✗ Пользователь 'admin' не найден в базе данных" -ForegroundColor Red
    Write-Host "Результат запроса к БД:" -ForegroundColor Gray
    Write-Host $dbCheck
    exit 1
}

# Шаг 5: Попытка авторизации
Write-Host "`n[5/5] Попытка авторизации с admin/parking123..." -ForegroundColor Yellow

$loginRequest = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

Write-Host "`nОтправка запроса на http://localhost:8086/api/auth/login" -ForegroundColor Gray
Write-Host "Тело запроса: $loginRequest" -ForegroundColor Gray
Write-Host ""

try {
    $authResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginRequest `
        -TimeoutSec 10

    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✓ АВТОРИЗАЦИЯ УСПЕШНА!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""

    if ($authResponse.accessToken) {
        Write-Host "Access Token (первые 50 символов):" -ForegroundColor Cyan
        Write-Host $authResponse.accessToken.Substring(0, [Math]::Min(50, $authResponse.accessToken.Length)) -ForegroundColor Gray
        Write-Host "..." -ForegroundColor Gray
    }

    if ($authResponse.refreshToken) {
        Write-Host "`nRefresh Token (первые 50 символов):" -ForegroundColor Cyan
        Write-Host $authResponse.refreshToken.Substring(0, [Math]::Min(50, $authResponse.refreshToken.Length)) -ForegroundColor Gray
        Write-Host "..." -ForegroundColor Gray
    }

    if ($authResponse.user) {
        Write-Host "`nИнформация о пользователе:" -ForegroundColor Cyan
        Write-Host "  Username: $($authResponse.user.username)" -ForegroundColor White
        Write-Host "  Role: $($authResponse.user.role)" -ForegroundColor White
        Write-Host "  Email: $($authResponse.user.email)" -ForegroundColor White
    }

    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "Компонент, генерирующий токен: API Gateway (JwtTokenService)" -ForegroundColor Cyan
    Write-Host "Путь: backend/api-gateway/src/main/java/com/parking/api_gateway/security/service/JwtTokenService.java" -ForegroundColor Gray
    Write-Host "========================================" -ForegroundColor Green

} catch {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "✗ ОШИБКА АВТОРИЗАЦИИ" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""

    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "HTTP Status Code: $statusCode" -ForegroundColor Yellow
    Write-Host "Ошибка: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""

    # Попытка получить тело ошибки
    try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        $reader.Close()
        Write-Host "Тело ответа:" -ForegroundColor Yellow
        Write-Host $errorBody -ForegroundColor Gray
    } catch {
        Write-Host "Не удалось прочитать тело ответа" -ForegroundColor Gray
    }

    Write-Host "`nВозможные причины ошибки $statusCode" ":" -ForegroundColor Yellow

    switch ($statusCode) {
        401 {
            Write-Host "  1. Неверный логин или пароль" -ForegroundColor Gray
            Write-Host "  2. Пользователь не найден в базе данных" -ForegroundColor Gray
            Write-Host "  3. Пароль в БД не совпадает с хешем BCrypt" -ForegroundColor Gray
        }
        423 {
            Write-Host "  1. Аккаунт заблокирован (account_locked)" -ForegroundColor Gray
            Write-Host "  2. Превышено количество попыток входа" -ForegroundColor Gray
        }
        500 {
            Write-Host "  1. Ошибка в коде сервера" -ForegroundColor Gray
            Write-Host "  2. Проблема подключения к базе данных" -ForegroundColor Gray
            Write-Host "  3. Ошибка генерации JWT токена" -ForegroundColor Gray
        }
        default {
            Write-Host "  Непредвиденная ошибка. Проверьте логи API Gateway" -ForegroundColor Gray
        }
    }

    Write-Host "`nПроверка логов API Gateway:" -ForegroundColor Yellow
    Write-Host "docker logs api-gateway --tail 50 | Select-String `"authentication|login|error`"" -ForegroundColor Gray
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Проверка завершена" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

