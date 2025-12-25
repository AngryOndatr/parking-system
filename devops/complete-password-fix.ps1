# Complete password fix script
# Stops containers, recreates database with correct hashes, and tests login

Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "   PASSWORD FIX SCRIPT" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "C:\Users\user\Projects\parking-system"
cd $projectRoot

# Step 1: Stop containers
Write-Host "[1/5] Stopping containers..." -ForegroundColor Yellow
docker-compose down 2>&1 | Out-Null
Write-Host "  Done" -ForegroundColor Green
Start-Sleep -Seconds 2

# Step 2: Remove volumes (clean slate)
Write-Host "[2/5] Removing old database..." -ForegroundColor Yellow
docker volume rm parking-system_parking_data 2>&1 | Out-Null
Write-Host "  Done" -ForegroundColor Green

# Step 3: Start containers
Write-Host "[3/5] Starting containers..." -ForegroundColor Yellow
docker-compose up -d 2>&1 | Out-Null
Write-Host "  Done" -ForegroundColor Green

# Step 4: Wait for services
Write-Host "[4/5] Waiting for services (40 seconds)..." -ForegroundColor Yellow
for ($i = 1; $i -le 8; $i++) {
    Write-Host "  $($i*5) seconds..." -ForegroundColor Gray
    Start-Sleep -Seconds 5
}
Write-Host "  Done" -ForegroundColor Green

# Step 5: Test login
Write-Host "[5/5] Testing login with Admin123!..." -ForegroundColor Yellow
$body = '{"username":"admin","password":"Admin123!"}'

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -UseBasicParsing `
        -ErrorAction Stop

    $json = $response.Content | ConvertFrom-Json

    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host "   SUCCESS!" -ForegroundColor Green
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Login successful with credentials:" -ForegroundColor White
    Write-Host "  Username: admin" -ForegroundColor Cyan
    Write-Host "  Password: Admin123!" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Token received:" -ForegroundColor White
    Write-Host "  $($json.accessToken.Substring(0,60))..." -ForegroundColor Gray
    Write-Host ""

} catch {
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Red
    Write-Host "   FAILED" -ForegroundColor Red
    Write-Host "=====================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Checking logs..." -ForegroundColor Yellow
    docker logs api-gateway --tail 10
    Write-Host ""
}

Write-Host "All credentials:" -ForegroundColor Cyan
Write-Host "  admin   -> Admin123!   (ADMIN)" -ForegroundColor White
Write-Host "  user    -> User1234!   (USER)" -ForegroundColor White
Write-Host "  manager -> Manager123! (MANAGER)" -ForegroundColor White
Write-Host ""

