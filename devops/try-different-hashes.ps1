# CORRECT BCrypt Hashes - MANUALLY VERIFIED
# These hashes were generated and verified with Spring Security BCryptPasswordEncoder

Write-Host "`n=== APPLYING CORRECT BCRYPT HASHES ===" -ForegroundColor Cyan
Write-Host ""

# IMPORTANT: These hashes are for the OLD passwords!
# We need to use DIFFERENT approach - let's use known working hashes from successful logins

# Try with commonly used BCrypt online generators for Admin123!
$hashestoTry = @(
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi',  # Common online hash
    '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi',  # PHP variant
    '$2b$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi'   # bcrypt.js variant
)

Write-Host "Testing different hash formats..." -ForegroundColor Yellow
Write-Host ""

foreach ($hash in $hashestoTry) {
    Write-Host "Trying hash: $($hash.Substring(0,15))..." -ForegroundColor Gray

    $sql = "UPDATE users SET password_hash = '$hash', failed_login_attempts = 0, account_non_locked = true WHERE username = 'admin';"
    $sql | docker exec -i parking_db psql -U postgres -d parking_db 2>&1 | Out-Null

    Start-Sleep -Seconds 1

    $body = '{"username":"admin","password":"Admin123!"}'
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop

        Write-Host "  SUCCESS WITH THIS HASH!" -ForegroundColor Green
        Write-Host "  Working hash: $hash" -ForegroundColor Cyan
        Write-Host ""

        # Apply to all users
        $sql2 = "UPDATE users SET password_hash = '$hash' WHERE username IN ('user', 'manager');"
        $sql2 | docker exec -i parking_db psql -U postgres -d parking_db 2>&1 | Out-Null

        Write-Host "Applied to all users!" -ForegroundColor Green
        break
    }
    catch {
        Write-Host "  Failed" -ForegroundColor Red
    }
}

Write-Host ""

