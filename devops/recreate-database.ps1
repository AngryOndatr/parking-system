# ============================================
#  RECREATE DATABASE WITH CORRECT PASSWORDS
# ============================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   DATABASE RECREATION" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$projectRoot = "C:\Users\user\Projects\parking-system"

# Step 1: Stop and remove postgres container
Write-Host "Step 1: Stopping and removing postgres container..." -ForegroundColor Yellow
Set-Location $projectRoot
docker-compose stop postgres 2>&1 | Out-Null
docker-compose rm -f postgres 2>&1 | Out-Null
Write-Host "   OK - Container stopped and removed`n" -ForegroundColor Green

# Step 2: Remove volume
Write-Host "Step 2: Removing database volume..." -ForegroundColor Yellow
docker volume rm parking-system_parking_data -f 2>&1 | Out-Null
Write-Host "   OK - Volume removed`n" -ForegroundColor Green

# Step 3: Start new postgres container
Write-Host "Step 3: Starting new postgres container..." -ForegroundColor Yellow
docker-compose up -d postgres
Start-Sleep -Seconds 3
Write-Host "   Container started, waiting for initialization (15 seconds)..." -ForegroundColor Gray
Start-Sleep -Seconds 15
Write-Host "   OK - Postgres started`n" -ForegroundColor Green

# Step 4: Verify database is ready
Write-Host "Step 4: Verifying database is ready..." -ForegroundColor Yellow
$maxRetries = 5
$ready = $false

for ($i = 1; $i -le $maxRetries; $i++) {
    try {
        $result = docker exec parking_db pg_isready -U postgres 2>&1
        if ($result -match "accepting connections") {
            $ready = $true
            Write-Host "   OK - Database is ready`n" -ForegroundColor Green
            break
        }
    } catch {
        Write-Host "   Attempt $i/$maxRetries - waiting..." -ForegroundColor Gray
        Start-Sleep -Seconds 3
    }
}

if (-not $ready) {
    Write-Host "   ERROR - Database not ready after $maxRetries attempts!" -ForegroundColor Red
    exit 1
}

# Step 5: Verify users in database
Write-Host "Step 5: Checking users in database..." -ForegroundColor Yellow
$users = docker exec parking_db psql -U postgres -d parking_db -t -c "SELECT username, LEFT(password_hash, 60), user_role FROM users ORDER BY username;" 2>&1

if ($users -match "admin") {
    Write-Host "   Users found in database:" -ForegroundColor Green
    Write-Host $users -ForegroundColor Gray

    # Verify password hashes are correct
    Write-Host "`n   Verifying password hashes..." -ForegroundColor Yellow

    $adminHash = docker exec parking_db psql -U postgres -d parking_db -t -c "SELECT password_hash FROM users WHERE username='admin';" 2>&1
    $expectedAdminHash = '$2b$10$DdZNyRdGNw2RTFkD92p7fu.v7CI.poCvicApJ5zozpwv7fBoNHiG.'

    if ($adminHash -match [regex]::Escape($expectedAdminHash)) {
        Write-Host "   ✅ Admin password hash is CORRECT (parking123)" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Admin password hash is WRONG!" -ForegroundColor Red
        Write-Host "      Expected: $expectedAdminHash" -ForegroundColor Yellow
        Write-Host "      Got:      $adminHash" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ⚠️  No users found - database may not be initialized" -ForegroundColor Yellow
    Write-Host "   Raw output: $users" -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   DATABASE READY!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Credentials:" -ForegroundColor Cyan
Write-Host "   admin    -> parking123  (ADMIN)" -ForegroundColor White
Write-Host "   user     -> user1234    (USER)" -ForegroundColor White
Write-Host "   manager  -> manager123  (MANAGER)" -ForegroundColor White
Write-Host ""

Write-Host "Next: Test authentication" -ForegroundColor Cyan
Write-Host '   $body = @{ username = "admin"; password = "parking123" } | ConvertTo-Json' -ForegroundColor White
Write-Host '   Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body' -ForegroundColor White
Write-Host ""

