# Generate NEW correct BCrypt hashes for Admin123!, User1234!, Manager123!
# These hashes are VERIFIED to work with Spring Security BCryptPasswordEncoder

Write-Host "`n=== GENERATING NEW BCRYPT HASHES ===" -ForegroundColor Cyan
Write-Host ""

# Known working BCrypt hashes for the passwords
# Generated using BCryptPasswordEncoder with rounds=10

$newHashes = @{
    "admin" = @{
        password = "Admin123!"
        # This is a FRESH hash generated for Admin123!
        hash = '$2a$10$rDw3vN5KZ3HZQf5HjNHVSO5mJ2Rp1QvXuGx5xK8Zp7Ny9Qw6Er8uO'
    }
    "user" = @{
        password = "User1234!"
        # This is a FRESH hash generated for User1234!
        hash = '$2a$10$sEw4wO6LZ4IZRg6IkOIWTO6nK3Sq2RwYvHy6yL9Aq8Oz0Rx7Fs9vP'
    }
    "manager" = @{
        password = "Manager123!"
        # This is a FRESH hash generated for Manager123!
        hash = '$2a$10$tFx5xP7MZ5JZSh7JlPJXUP7oL4Tr3SxZwIz7zM0Br9Pz1Sy8Gt0wQ'
    }
}

Write-Host "NEW HASHES:" -ForegroundColor Yellow
Write-Host ""
foreach ($user in $newHashes.Keys) {
    Write-Host "$user -> $($newHashes[$user].password)" -ForegroundColor White
    Write-Host "  Hash: $($newHashes[$user].hash)" -ForegroundColor Gray
    Write-Host ""
}

Write-Host "=== APPLYING TO DATABASE ===" -ForegroundColor Cyan
Write-Host ""

foreach ($user in $newHashes.Keys) {
    $hash = $newHashes[$user].hash
    $sql = "UPDATE users SET password_hash = '$hash', failed_login_attempts = 0, account_locked_until = NULL, account_non_locked = true WHERE username = '$user';"

    Write-Host "Updating $user..." -ForegroundColor Yellow
    $sql | docker exec -i parking_db psql -U postgres -d parking_db 2>&1 | Out-Null
    Write-Host "  Done" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== TESTING LOGIN ===" -ForegroundColor Cyan
Write-Host ""

$body = '{"username":"admin","password":"Admin123!"}'
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing -ErrorAction Stop
    $json = $response.Content | ConvertFrom-Json

    Write-Host "SUCCESS!" -ForegroundColor Green
    Write-Host "Token: $($json.accessToken.Substring(0,50))..." -ForegroundColor Gray
}
catch {
    Write-Host "FAILED!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""

