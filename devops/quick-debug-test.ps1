# Quick Debug Test - API Gateway Authentication
# Encoding: UTF-8
Write-Host "`n=== QUICK DEBUG TEST ===" -ForegroundColor Cyan

# Test 1: Login
Write-Host "`n1. Testing login..." -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"parking123"}'

try {
    $loginResp = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -UseBasicParsing

    Write-Host "   Status: $($loginResp.StatusCode)" -ForegroundColor Green
    $loginData = $loginResp.Content | ConvertFrom-Json
    $token = $loginData.accessToken
    Write-Host "   Token: $($token.Substring(0,20))..." -ForegroundColor Green
} catch {
    Write-Host "   FAILED: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Access protected endpoint
Write-Host "`n2. Testing /api/clients with JWT token..." -ForegroundColor Yellow
Write-Host "   Token being sent: Bearer $($token.Substring(0,20))..." -ForegroundColor Gray

try {
    $clientsResp = Invoke-WebRequest -Uri "http://localhost:8086/api/clients" `
        -Method GET `
        -Headers @{
            "Authorization" = "Bearer $token"
            "Accept" = "application/json"
        } `
        -UseBasicParsing

    Write-Host "   Status: $($clientsResp.StatusCode)" -ForegroundColor Green
    Write-Host "   Response: $($clientsResp.Content)" -ForegroundColor White

} catch {
    Write-Host "   FAILED!" -ForegroundColor Red
    Write-Host "   Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red

    # Try to get response body
    try {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response: $responseBody" -ForegroundColor Red
    } catch {
        Write-Host "   (No response body available)" -ForegroundColor Gray
    }
}

# Test 3: Check logs
Write-Host "`n3. Checking recent API Gateway logs..." -ForegroundColor Yellow
docker logs api-gateway --tail 20

Write-Host "`n=== TEST COMPLETE ===" -ForegroundColor Cyan

