Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Тест после полной пересборки" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Проверка статуса контейнеров..." -ForegroundColor Yellow
docker ps --format "table {{.Names}}\t{{.Status}}" | Select-String "api-gateway|client-service|eureka"

Write-Host "`n🧪 Тестирование логина..." -ForegroundColor Cyan
Write-Host "Credentials: user / user123" -ForegroundColor Gray
Write-Host ""

$body = '{"username":"user","password":"user123"}'

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -ErrorAction Stop

    Write-Host "🎉🎉🎉 SUCCESS! JWT TOKEN RECEIVED! 🎉🎉🎉" -ForegroundColor Green
    Write-Host "`nToken (first 70 chars):" -ForegroundColor Cyan
    Write-Host $response.token.Substring(0, [Math]::Min(70, $response.token.Length)) -ForegroundColor White

    Write-Host "`n✅ Аутентификация работает!" -ForegroundColor Green
    Write-Host "`n📝 Рабочие credentials:" -ForegroundColor Yellow
    Write-Host "  Username: user" -ForegroundColor White
    Write-Host "  Password: user123" -ForegroundColor White

} catch {
    Write-Host "❌ ОШИБКА: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Yellow

    Write-Host "`n📋 Последние логи API Gateway:" -ForegroundColor Cyan
    docker logs api-gateway --tail 10 2>&1 | Select-String "ERROR|FAILED|SUCCESS" | Select-Object -Last 5
}

Write-Host "`n========================================" -ForegroundColor Cyan

