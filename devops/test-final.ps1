# test-final.ps1
# Финальный тест системы после ребилда

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ФИНАЛЬНЫЙ ТЕСТ PARKING SYSTEM" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Проверка контейнеров
Write-Host "1️⃣  Проверка контейнеров..." -ForegroundColor Yellow
$containers = docker ps --format "{{.Names}}\t{{.Status}}"
Write-Host $containers
Write-Host ""

# 2. Проверка Eureka
Write-Host "2️⃣  Проверка Eureka Dashboard..." -ForegroundColor Yellow
try {
    $eureka = Invoke-WebRequest -Uri "http://localhost:8761" -Method GET -TimeoutSec 5 -UseBasicParsing
    Write-Host "   ✓ Eureka доступна: http://localhost:8761" -ForegroundColor Green
    Write-Host "   Статус: $($eureka.StatusCode)" -ForegroundColor White
} catch {
    Write-Host "   ✗ Eureka недоступна" -ForegroundColor Red
}
Write-Host ""

# 3. Проверка API Gateway Health
Write-Host "3️⃣  Проверка API Gateway..." -ForegroundColor Yellow
try {
    $gateway = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5 -SkipHttpErrorCheck
    Write-Host "   URL: http://localhost:8080/actuator/health" -ForegroundColor White
    Write-Host "   Статус: $($gateway.StatusCode)" -ForegroundColor White
    if ($gateway.StatusCode -eq 200) {
        Write-Host "   ✓ API Gateway работает!" -ForegroundColor Green
    } elseif ($gateway.StatusCode -in @(401, 403)) {
        Write-Host "   ✓ API Gateway работает (требует аутентификацию)" -ForegroundColor Green
    }
} catch {
    Write-Host "   ✗ API Gateway недоступен: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 4. Тест аутентификации
Write-Host "4️⃣  Тест аутентификации (user/user123)..." -ForegroundColor Yellow
try {
    $body = @{
        username = "user"
        password = "user123"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 10

    if ($response.token) {
        Write-Host "   ✓ Аутентификация успешна!" -ForegroundColor Green
        Write-Host "   Токен (первые 50 символов): $($response.token.Substring(0, [Math]::Min(50, $response.token.Length)))..." -ForegroundColor White
        $token = $response.token

        # 5. Тест доступа к Client Service через Gateway
        Write-Host ""
        Write-Host "5️⃣  Тест доступа к Client Service через Gateway..." -ForegroundColor Yellow
        try {
            $headers = @{
                "Authorization" = "Bearer $token"
            }

            $clients = Invoke-RestMethod -Uri "http://localhost:8080/api/clients" `
                -Method GET `
                -Headers $headers `
                -TimeoutSec 10

            Write-Host "   ✓ Доступ к Client Service успешен!" -ForegroundColor Green
            Write-Host "   Получено клиентов: $($clients.Count)" -ForegroundColor White
        } catch {
            $statusCode = $_.Exception.Response.StatusCode.Value__
            Write-Host "   ✗ Ошибка доступа к Client Service" -ForegroundColor Red
            Write-Host "   Статус: $statusCode" -ForegroundColor Yellow
            Write-Host "   Ошибка: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ✗ Токен не получен!" -ForegroundColor Red
    }
} catch {
    $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.Value__ } else { "N/A" }
    Write-Host "   ✗ Ошибка аутентификации" -ForegroundColor Red
    Write-Host "   Статус: $statusCode" -ForegroundColor Yellow
    Write-Host "   Ошибка: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ТЕСТИРОВАНИЕ ЗАВЕРШЕНО" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Полезные ссылки:" -ForegroundColor Yellow
Write-Host "   Eureka Dashboard:  http://localhost:8761" -ForegroundColor Gray
Write-Host "   API Gateway:       http://localhost:8080" -ForegroundColor Gray
Write-Host "   Prometheus:        http://localhost:9090" -ForegroundColor Gray
Write-Host "   Grafana:           http://localhost:3000" -ForegroundColor Gray
Write-Host "   Jaeger:            http://localhost:16686" -ForegroundColor Gray
Write-Host "   pgAdmin:           http://localhost:5050" -ForegroundColor Gray
Write-Host ""

