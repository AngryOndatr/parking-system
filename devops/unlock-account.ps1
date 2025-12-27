# Unlock Account Script

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Account Unlock Utility" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "This script will:" -ForegroundColor Yellow
Write-Host "  1. Clear failed login attempts" -ForegroundColor White
Write-Host "  2. Remove account lock" -ForegroundColor White
Write-Host "  3. Clear Redis cache" -ForegroundColor White
Write-Host ""

# Unlock admin account in database
Write-Host "🔓 Unlocking admin account..." -ForegroundColor Cyan
$result = docker exec parking_db psql -U postgres -d parking_db -c "UPDATE users SET failed_login_attempts = 0, account_locked_until = NULL, account_non_locked = true WHERE username = 'admin';" 2>&1

if ($result -match "UPDATE 1") {
    Write-Host "✅ Admin account unlocked" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to unlock account" -ForegroundColor Red
    Write-Host $result -ForegroundColor Gray
    exit 1
}

# Clear Redis
Write-Host "`n🧹 Clearing Redis cache..." -ForegroundColor Cyan
docker exec parking_redis redis-cli FLUSHALL 2>&1 | Out-Null
Write-Host "✅ Redis cleared" -ForegroundColor Green

# Verify
Write-Host "`n🔍 Verifying account status..." -ForegroundColor Cyan
$status = docker exec parking_db psql -U postgres -d parking_db -c "SELECT username, failed_login_attempts, account_non_locked, account_locked_until FROM users WHERE username = 'admin';" 2>&1

Write-Host $status -ForegroundColor White

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  ✅ Account Unlocked Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`n📝 You can now login with:" -ForegroundColor Cyan
Write-Host "  Username: admin" -ForegroundColor White
Write-Host "  Password: password123" -ForegroundColor White
Write-Host ""

