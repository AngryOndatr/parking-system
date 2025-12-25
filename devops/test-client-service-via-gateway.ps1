# Test Client Service access via API Gateway
# Encoding: UTF-8

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Client Service Access Test via API Gateway" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Login to get JWT token
Write-Host "Step 1: Getting JWT token..." -ForegroundColor Yellow

$loginBody = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -UseBasicParsing

    $loginData = $loginResponse.Content | ConvertFrom-Json
    $token = $loginData.accessToken

    if ($token) {
        Write-Host "OK: Token obtained successfully" -ForegroundColor Green
        Write-Host "   Token (first 30 chars): $($token.Substring(0, [Math]::Min(30, $token.Length)))..." -ForegroundColor Gray
    } else {
        Write-Host "ERROR: No token in response" -ForegroundColor Red
        Write-Host "Response: $($loginResponse.Content)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "ERROR: Failed to get token" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Test access to Client Service via Gateway
Write-Host "`nStep 2: Testing access to /api/clients via Gateway..." -ForegroundColor Yellow

try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Accept" = "application/json"
    }

    $clientsResponse = Invoke-WebRequest -Uri "http://localhost:8086/api/clients" `
        -Method GET `
        -Headers $headers `
        -UseBasicParsing

    Write-Host "OK: Received response with status: $($clientsResponse.StatusCode)" -ForegroundColor Green
    Write-Host "`nResponse data:" -ForegroundColor Cyan
    Write-Host $clientsResponse.Content -ForegroundColor White

} catch {
    Write-Host "ERROR: Failed to access Client Service" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red

    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }

    exit 1
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test completed successfully!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

