# FINAL SOLUTION - Apply working hashes and test

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  APPLYING VERIFIED WORKING HASHES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "These hashes are from a previous SUCCESSFUL session:" -ForegroundColor Yellow
Write-Host "  admin   -> parking123  ($2b$ hash)" -ForegroundColor White
Write-Host "  user    -> user1234    ($2b$ hash)" -ForegroundColor White
Write-Host "  manager -> manager123  ($2b$ hash)" -ForegroundColor White
Write-Host ""

Write-Host "[1/2] Applying hashes to database..." -ForegroundColor Yellow
$sqlFilePath = Join-Path (Split-Path $PSScriptRoot -Parent) "database\apply_working_hashes.sql"
Get-Content $sqlFilePath | docker exec -i parking_db psql -U postgres -d parking_db 2>&1 | Out-Null
Write-Host "  Done" -ForegroundColor Green

Write-Host "[2/2] Testing login..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

$body = '{"username":"admin","password":"parking123"}'

try {
    $response = Invoke-WebRequest `
        -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -UseBasicParsing `
        -ErrorAction Stop

    $json = $response.Content | ConvertFrom-Json

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "           SUCCESS!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Login successful with parking123!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Token received:" -ForegroundColor Cyan
    Write-Host "  $($json.accessToken.Substring(0,70))..." -ForegroundColor Gray
    Write-Host ""
    Write-Host "Working credentials:" -ForegroundColor Yellow
    Write-Host "  admin   -> parking123" -ForegroundColor White
    Write-Host "  user    -> user1234" -ForegroundColor White
    Write-Host "  manager -> manager123" -ForegroundColor White
    Write-Host ""
}
catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "           FAILED" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Last 15 log lines:" -ForegroundColor Cyan
    docker logs api-gateway --tail 15 2>&1 | Select-String "STEP|Password|Login|admin" | Select-Object -Last 10
    Write-Host ""
}

Write-Host "Script completed." -ForegroundColor Cyan
Write-Host ""

