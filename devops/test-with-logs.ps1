# Тест авторизации с мониторингом логов в реальном времени

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Тест авторизации с детальными логами" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Проверка API Gateway
Write-Host "Проверка готовности API Gateway..." -ForegroundColor Yellow
$maxAttempts = 10
$attempt = 0
$isReady = $false

while ($attempt -lt $maxAttempts -and -not $isReady) {
    $attempt++
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8086/actuator/health" -TimeoutSec 3 -ErrorAction Stop
        if ($health.status -eq "UP") {
            Write-Host "✓ API Gateway готов" -ForegroundColor Green
            $isReady = $true
        }
    } catch {
        Write-Host "  [$attempt/$maxAttempts] Ожидание..." -ForegroundColor Gray
        Start-Sleep -Seconds 5
    }
}

if (-not $isReady) {
    Write-Host "✗ API Gateway не готов" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Отправка запроса авторизации" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$body = '{"username":"admin","password":"parking123"}'

Write-Host "Отправка: POST http://localhost:8086/api/auth/login" -ForegroundColor Gray
Write-Host "Body: $body" -ForegroundColor Gray
Write-Host ""

# Запуск мониторинга логов в фоне
$logJob = Start-Job -ScriptBlock {
    Start-Sleep -Seconds 1
    docker logs api-gateway -f --tail 50 2>&1 | Select-String "STEP|ERROR|SUCCESS|FAILED|handleFailedLogin|lockAccountTemporarily|recordSuccessfulLogin"
}

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 10

    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  ✓ УСПЕХ!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Username: $($response.user.username)" -ForegroundColor Cyan
    Write-Host "Role:     $($response.user.role)" -ForegroundColor Cyan
    Write-Host "Token:    $($response.accessToken.Substring(0,60))..." -ForegroundColor Gray

} catch {
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "  ✗ ОШИБКА" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host "Error:  $($_.Exception.Message)" -ForegroundColor Red
}

# Подождать и остановить мониторинг логов
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Детальные логи API Gateway" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Start-Sleep -Seconds 2
Stop-Job $logJob
$logs = Receive-Job $logJob
Remove-Job $logJob

if ($logs) {
    $logs | ForEach-Object {
        $line = $_.ToString()
        if ($line -match "ERROR|FAILED") {
            Write-Host $line -ForegroundColor Red
        } elseif ($line -match "SUCCESS|✓") {
            Write-Host $line -ForegroundColor Green
        } elseif ($line -match "STEP|🔐|🔍|🔒|🔑|📅|📝|🎫") {
            Write-Host $line -ForegroundColor Cyan
        } elseif ($line -match "WARN|⚠") {
            Write-Host $line -ForegroundColor Yellow
        } else {
            Write-Host $line -ForegroundColor Gray
        }
    }
} else {
    Write-Host "Нет логов с детализацией. Показываю последние 30 строк:" -ForegroundColor Yellow
    docker logs api-gateway --tail 30 2>&1 | ForEach-Object {
        $line = $_.ToString()
        if ($line -match "ERROR") {
            Write-Host $line -ForegroundColor Red
        } elseif ($line -match "WARN") {
            Write-Host $line -ForegroundColor Yellow
        } else {
            Write-Host $line -ForegroundColor Gray
        }
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Завершено" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

