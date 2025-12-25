Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  API Gateway Login Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Testing with credentials:" -ForegroundColor Yellow
Write-Host "  Username: admin" -ForegroundColor White
Write-Host "  Password: parking123" -ForegroundColor White
Write-Host ""

$body = '{"username":"admin","password":"parking123"}'

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body

    Write-Host "SUCCESS! JWT Token received:" -ForegroundColor Green
    Write-Host "  Access Token: $($response.accessToken.Substring(0, 50))..." -ForegroundColor White
    if ($response.refreshToken) {
        Write-Host "  Refresh Token: $($response.refreshToken.Substring(0, 50))..." -ForegroundColor White
    }
    Write-Host ""

    # Test with token
    Write-Host "Testing API call with token..." -ForegroundColor Yellow
    $headers = @{ Authorization = "Bearer $($response.accessToken)" }
    $clients = Invoke-RestMethod -Uri "http://localhost:8086/api/clients" `
        -Method GET `
        -Headers $headers

    Write-Host "SUCCESS! Clients retrieved:" -ForegroundColor Green
    $clients | Format-Table -AutoSize

} catch {
    Write-Host "FAILED!" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Cyan

