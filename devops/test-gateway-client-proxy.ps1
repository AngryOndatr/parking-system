# Test API Gateway proxy to Client Service with JWT

Write-Host "`n=== Testing API Gateway Proxy ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Get JWT token
Write-Host "[1/3] Getting JWT token..." -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"parking123"}'

try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -UseBasicParsing `
        -ErrorAction Stop

    $tokenData = $loginResponse.Content | ConvertFrom-Json
    $token = $tokenData.accessToken

    Write-Host "  Token received: $($token.Substring(0,30))..." -ForegroundColor Green
}
catch {
    Write-Host "  Failed to get token: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    exit 1
}

Write-Host ""

# Step 2: Test /api/clients WITHOUT token
Write-Host "[2/3] Testing /api/clients WITHOUT token..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/clients" `
        -Method GET `
        -UseBasicParsing `
        -ErrorAction Stop

    Write-Host "  Unexpected success (should require auth)" -ForegroundColor Yellow
    Write-Host "  Status: $($response.StatusCode)" -ForegroundColor Gray
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 200) {
        Write-Host "  No auth required (public endpoint)" -ForegroundColor Green
    }
    elseif ($statusCode -eq 401) {
        Write-Host "  401 Unauthorized (auth required - expected)" -ForegroundColor Green
    }
    else {
        Write-Host "  Status: $statusCode" -ForegroundColor Yellow
    }
}

Write-Host ""

# Step 3: Test /api/clients WITH token
Write-Host "[3/3] Testing /api/clients WITH JWT token..." -ForegroundColor Yellow

try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }

    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/clients" `
        -Method GET `
        -Headers $headers `
        -UseBasicParsing `
        -ErrorAction Stop

    Write-Host ""
    Write-Host "  SUCCESS!" -ForegroundColor Green
    Write-Host "  Status: $($response.StatusCode)" -ForegroundColor White
    Write-Host "  Response:" -ForegroundColor Cyan
    $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 5 | Write-Host -ForegroundColor Gray
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host ""
    Write-Host "  FAILED!" -ForegroundColor Red
    Write-Host "  Status: $statusCode" -ForegroundColor Yellow

    if ($statusCode -eq 403) {
        Write-Host "  403 Forbidden - Check Security configuration" -ForegroundColor Yellow
    }
    elseif ($statusCode -eq 401) {
        Write-Host "  401 Unauthorized - Token invalid or expired" -ForegroundColor Yellow
    }
    else {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
Write-Host ""

