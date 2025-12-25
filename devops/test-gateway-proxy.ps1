# Test API Gateway Proxying

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  API Gateway Proxy Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Get JWT token
Write-Host "Step 1: Getting JWT token..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -TimeoutSec 10

    $token = $loginResponse.token
    Write-Host "SUCCESS: JWT token received" -ForegroundColor Green
    Write-Host "   Token: $($token.Substring(0, [Math]::Min(50, $token.Length)))..." -ForegroundColor Gray
} catch {
    Write-Host "FAILED: Error getting token" -ForegroundColor Red
    Write-Host "   Message: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Gray
    exit 1
}

# Step 2: Request WITHOUT token (expect 403)
Write-Host "`nStep 2: Request WITHOUT token (expecting 403)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/clients" `
        -Method GET `
        -UseBasicParsing `
        -TimeoutSec 10

    Write-Host "UNEXPECTED: Request passed without token!" -ForegroundColor Yellow
    Write-Host "   Status: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "SUCCESS: Got expected 403 (authentication required)" -ForegroundColor Green
    } else {
        Write-Host "UNEXPECTED: Got status code $statusCode instead of 403" -ForegroundColor Red
    }
}

# Step 3: Request WITH token
Write-Host "`nStep 3: Request WITH token..." -ForegroundColor Yellow
try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $response = Invoke-RestMethod -Uri "http://localhost:8086/api/clients" `
        -Method GET `
        -Headers $headers `
        -ContentType "application/json" `
        -TimeoutSec 10

    Write-Host "SUCCESS: Request successful!" -ForegroundColor Green
    Write-Host "   Clients found: $($response.Count)" -ForegroundColor Gray

    if ($response.Count -gt 0) {
        Write-Host "`nFirst client:" -ForegroundColor Cyan
        $response[0] | Format-List | Out-String | Write-Host -ForegroundColor White
    } else {
        Write-Host "   (list is empty)" -ForegroundColor Gray
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "FAILED: Request error: $statusCode" -ForegroundColor Red
    Write-Host "   Message: $($_.Exception.Message)" -ForegroundColor Gray

    # Try to read response body
    try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response body: $responseBody" -ForegroundColor Gray
    } catch {}
}

# Step 4: Direct request to Client Service (bypassing API Gateway)
Write-Host "`nStep 4: Direct request to Client Service..." -ForegroundColor Yellow
try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/clients" `
        -Method GET `
        -Headers $headers `
        -ContentType "application/json" `
        -TimeoutSec 10

    Write-Host "SUCCESS: Direct request successful!" -ForegroundColor Green
    Write-Host "   Clients found: $($response.Count)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "FAILED: Direct request error: $statusCode" -ForegroundColor Red
    Write-Host "   Message: $($_.Exception.Message)" -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test Complete" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

