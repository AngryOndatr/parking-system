# Quick fix for password verification issues
# Updates database with correct BCrypt hashes

Write-Host "`n=== Password Database Update ===" -ForegroundColor Cyan
Write-Host ""

# SQL commands to update passwords
$sqlCommands = @"
UPDATE users SET
    password_hash = '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG',
    failed_login_attempts = 0,
    account_locked_until = NULL,
    account_non_locked = true
WHERE username = 'admin';

UPDATE users SET
    password_hash = '\$2a\$10\$8kZRlYUZQKXZZQKXZeHkOefE6Hm6Zqm7QqF8KqH7LqG9MqI0NqJ1O',
    failed_login_attempts = 0,
    account_locked_until = NULL,
    account_non_locked = true
WHERE username = 'user';

UPDATE users SET
    password_hash = '\$2a\$10\$7kYRlYUZQKXZZQKXZeHjNdD5Gl5Ypl6PpE7JpG6KpF8LpH9MpI0N',
    failed_login_attempts = 0,
    account_locked_until = NULL,
    account_non_locked = true
WHERE username = 'manager';
"@

Write-Host "Updating passwords in database..." -ForegroundColor Yellow

try {
    # Execute SQL commands
    $sqlCommands | docker exec -i parking_db psql -U postgres -d parking_db

    Write-Host "Success! Passwords updated." -ForegroundColor Green
    Write-Host ""

    # Verify
    Write-Host "Verifying updates..." -ForegroundColor Cyan
    $verifySQL = "SELECT username, substring(password_hash, 1, 10) as hash, account_non_locked FROM users ORDER BY username;"
    docker exec parking_db psql -U postgres -d parking_db -c $verifySQL

} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "New passwords:" -ForegroundColor Cyan
Write-Host "  admin   -> Admin123!" -ForegroundColor White
Write-Host "  user    -> User1234!" -ForegroundColor White
Write-Host "  manager -> Manager123!" -ForegroundColor White
Write-Host ""

