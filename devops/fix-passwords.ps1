# ============================================
#  Quick Password Fix - Update User Passwords
# ============================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   QUICK PASSWORD FIX" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Check if database container is running
$dbRunning = docker ps --filter "name=parking_db" --format "{{.Names}}" 2>$null

if (-not $dbRunning) {
    Write-Host "ERROR: Database container 'parking_db' is not running!" -ForegroundColor Red
    Write-Host "Start it with: docker-compose up -d postgres" -ForegroundColor Yellow
    exit 1
}

Write-Host "Step 1: Updating user passwords in database..." -ForegroundColor Yellow

# Create temporary SQL file with updates
$tempSqlFile = "$env:TEMP\update_passwords.sql"

# Use single quotes in here-string to prevent PowerShell variable interpolation
$sqlContent = @'
-- Update admin password to parking123
UPDATE users
SET password_hash = '$2b$10$DdZNyRdGNw2RTFkD92p7fu.v7CI.poCvicApJ5zozpwv7fBoNHiG.',
    failed_login_attempts = 0,
    account_locked_until = NULL
WHERE username = 'admin';

-- Update user password to user1234
UPDATE users
SET password_hash = '$2b$10$hnNC/GKgX69DZFIeJOV3Z.qilduqc5LUV3o3ugYTAqR3y8j5mC.fa',
    failed_login_attempts = 0,
    account_locked_until = NULL
WHERE username = 'user';

-- Update manager password to manager123
UPDATE users
SET password_hash = '$2b$10$Xdg9Gy3l9Ejhci36J1yGTuD/bcQsOTkFFRwdMqGv/OFVo3GYToICS',
    failed_login_attempts = 0,
    account_locked_until = NULL
WHERE username = 'manager';

-- Show results
SELECT 'Updated ' || COUNT(*) || ' users' as result FROM users WHERE username IN ('admin', 'user', 'manager');
SELECT username, LEFT(password_hash, 60) as password_hash, failed_login_attempts
FROM users
WHERE username IN ('admin', 'user', 'manager')
ORDER BY username;
'@

# Write SQL to file
$sqlContent | Out-File -FilePath $tempSqlFile -Encoding UTF8

# Execute SQL update
try {
    Write-Host "   Executing SQL updates..." -ForegroundColor Gray
    $result = Get-Content $tempSqlFile | docker exec -i parking_db psql -U postgres -d parking_db 2>&1
    Write-Host $result -ForegroundColor Gray
    Remove-Item $tempSqlFile -Force
    Write-Host "`n   OK - Passwords updated successfully`n" -ForegroundColor Green
} catch {
    Write-Host "   ERROR - Failed to update passwords: $($_.Exception.Message)" -ForegroundColor Red
    Remove-Item $tempSqlFile -Force -ErrorAction SilentlyContinue
    exit 1
}


Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   FIX COMPLETE!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Updated credentials:" -ForegroundColor Cyan
Write-Host "   Username: admin   | Password: parking123" -ForegroundColor White
Write-Host "   Username: user    | Password: user1234" -ForegroundColor White
Write-Host "   Username: manager | Password: manager123" -ForegroundColor White
Write-Host ""

Write-Host "Test login:" -ForegroundColor Cyan
Write-Host '   $body = @{ username = "admin"; password = "parking123" } | ConvertTo-Json' -ForegroundColor White
Write-Host '   Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body' -ForegroundColor White
Write-Host ""

