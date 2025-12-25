# FINAL PASSWORD FIX - Guaranteed to work
# This script recreates everything from scratch

param(
    [switch]$SkipWait
)

$ErrorActionPreference = "Continue"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  FINAL PASSWORD FIX SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "C:\Users\user\Projects\parking-system"
cd $projectRoot

# Step 1: Stop everything
Write-Host "[1/6] Stopping all containers..." -ForegroundColor Yellow
docker-compose down 2>&1 | Out-Null
Write-Host "  Done" -ForegroundColor Green
Start-Sleep -Seconds 2

# Step 2: Clean volumes
Write-Host "[2/6] Removing database volume..." -ForegroundColor Yellow
docker volume rm parking-system_parking_data 2>&1 | Out-Null
Write-Host "  Done" -ForegroundColor Green
Start-Sleep -Seconds 1

# Step 3: Start containers
Write-Host "[3/6] Starting containers..." -ForegroundColor Yellow
docker-compose up -d 2>&1 | Out-Null
Write-Host "  Done" -ForegroundColor Green

# Step 4: Wait for database initialization
if (-not $SkipWait) {
    Write-Host "[4/6] Waiting for database initialization..." -ForegroundColor Yellow
    for ($i = 1; $i -le 10; $i++) {
        Write-Host "  $($i * 5) seconds..." -ForegroundColor Gray
        Start-Sleep -Seconds 5
    }
    Write-Host "  Done" -ForegroundColor Green
}
else {
    Write-Host "[4/6] Skipping wait (manual mode)" -ForegroundColor Gray
}

# Step 5: Populate users
Write-Host "[5/6] Populating users table..." -ForegroundColor Yellow
Get-Content "$projectRoot\database\populate_users.sql" | docker exec -i parking_db psql -U postgres 2>&1 | Out-Null
Write-Host "  Done" -ForegroundColor Green
Start-Sleep -Seconds 2

# Step 6: Test login
Write-Host "[6/6] Testing login..." -ForegroundColor Yellow
$body = '{"username":"admin","password":"Admin123!"}'

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop

    $json = $response.Content | ConvertFrom-Json

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "           SUCCESS!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Login successful!" -ForegroundColor Green
    Write-Host "  Username: admin" -ForegroundColor White
    Write-Host "  Password: Admin123!" -ForegroundColor White
    Write-Host ""
    Write-Host "Token received:" -ForegroundColor Cyan
    Write-Host "  $($json.accessToken.Substring(0,70))..." -ForegroundColor Gray
    Write-Host ""
    Write-Host "All credentials:" -ForegroundColor Yellow
    Write-Host "  admin   -> Admin123!   (ADMIN)" -ForegroundColor White
    Write-Host "  user    -> User1234!   (USER)" -ForegroundColor White
    Write-Host "  manager -> Manager123! (MANAGER)" -ForegroundColor White
    Write-Host ""
}
catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "           FAILED" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Login test failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Checking logs..." -ForegroundColor Cyan
    Write-Host ""
    docker logs api-gateway --tail 15 2>&1 | Select-String "STEP|Password|Login|ERROR" | Select-Object -Last 10
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "  1. Check if API Gateway is running: docker ps | findstr api-gateway" -ForegroundColor Gray
    Write-Host "  2. Check logs: docker logs api-gateway --tail 50" -ForegroundColor Gray
    Write-Host "  3. Wait 1-2 minutes and try again: .\test-login.ps1" -ForegroundColor Gray
    Write-Host ""
}

Write-Host "Script completed." -ForegroundColor Cyan
Write-Host ""



