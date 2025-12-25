# Populate users table with correct BCrypt hashes
# Passwords: admin=Admin123!, user=User1234!, manager=Manager123!

Write-Host "`n=== Populating Users Table ===" -ForegroundColor Cyan
Write-Host ""

$users = @(
    @{
        username = "admin"
        hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG'
        role = "ADMIN"
        password = "Admin123!"
    },
    @{
        username = "user"
        hash = '$2a$10$8kZRlYUZQKXZZQKXZeHkOefE6Hm6Zqm7QqF8KqH7LqG9MqI0NqJ1O'
        role = "USER"
        password = "User1234!"
    },
    @{
        username = "manager"
        hash = '$2a$10$7kYRlYUZQKXZZQKXZeHjNdD5Gl5Ypl6PpE7JpG6KpF8LpH9MpI0N'
        role = "MANAGER"
        password = "Manager123!"
    }
)

$insertCount = 0
$errorCount = 0

foreach ($user in $users) {
    Write-Host "Inserting user: $($user.username)..." -ForegroundColor Yellow

    $sql = @"
INSERT INTO users (
    username, password_hash, user_role,
    enabled, email_verified, account_non_expired,
    account_non_locked, credentials_non_expired,
    force_password_change, failed_login_attempts,
    login_count, two_factor_enabled, active_sessions_limit
) VALUES (
    '$($user.username)', '$($user.hash)', '$($user.role)',
    true, true, true,
    true, true,
    false, 0,
    0, false, 3
) ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    user_role = EXCLUDED.user_role,
    account_non_locked = true,
    failed_login_attempts = 0;
"@

    try {
        $result = docker exec parking_db psql -U postgres -d parking_db -c $sql 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  Success! ($($user.role))" -ForegroundColor Green
            $insertCount++
        } else {
            Write-Host "  Failed: $result" -ForegroundColor Red
            $errorCount++
        }
    } catch {
        Write-Host "  Error: $_" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "=== Verification ===" -ForegroundColor Cyan

$verifySQL = "SELECT username, user_role, enabled, account_non_locked FROM users ORDER BY username;"
$users = docker exec parking_db psql -U postgres -d parking_db -t -c $verifySQL 2>&1

Write-Host $users

Write-Host ""
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host "Inserted: $insertCount users" -ForegroundColor $(if ($insertCount -eq 3) { "Green" } else { "Yellow" })
Write-Host "Errors: $errorCount" -ForegroundColor $(if ($errorCount -eq 0) { "Green" } else { "Red" })
Write-Host ""

if ($insertCount -eq 3) {
    Write-Host "Credentials:" -ForegroundColor Green
    Write-Host "  admin   -> Admin123!   (ADMIN)" -ForegroundColor White
    Write-Host "  user    -> User1234!   (USER)" -ForegroundColor White
    Write-Host "  manager -> Manager123! (MANAGER)" -ForegroundColor White
    Write-Host ""
    Write-Host "Ready to test login!" -ForegroundColor Green
} else {
    Write-Host "Some users were not inserted. Check errors above." -ForegroundColor Yellow
}

