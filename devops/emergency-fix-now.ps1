# Emergency Password Update - Run this NOW
# Updates passwords in running database

Write-Host "`nEmergency Password Update" -ForegroundColor Red
Write-Host "=========================" -ForegroundColor Red
Write-Host ""

$updates = @"
UPDATE users SET password_hash = '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG', failed_login_attempts = 0, account_locked_until = NULL, account_non_locked = true WHERE username = 'admin';
UPDATE users SET password_hash = '\$2a\$10\$8kZRlYUZQKXZZQKXZeHkOefE6Hm6Zqm7QqF8KqH7LqG9MqI0NqJ1O', failed_login_attempts = 0, account_locked_until = NULL, account_non_locked = true WHERE username = 'user';
UPDATE users SET password_hash = '\$2a\$10\$7kYRlYUZQKXZZQKXZeHjNdD5Gl5Ypl6PpE7JpG6KpF8LpH9MpI0N', failed_login_attempts = 0, account_locked_until = NULL, account_non_locked = true WHERE username = 'manager';
"@

Write-Host "Updating passwords..." -ForegroundColor Yellow
$updates | docker exec -i parking_db psql -U postgres -d parking_db 2>&1 | Out-Null

Write-Host "Done!" -ForegroundColor Green
Write-Host ""
Write-Host "Testing login..." -ForegroundColor Cyan

$body = '{"username":"admin","password":"Admin123!"}'
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing -ErrorAction Stop
    $json = $response.Content | ConvertFrom-Json

    Write-Host ""
    Write-Host "SUCCESS!" -ForegroundColor Green
    Write-Host "Token: $($json.accessToken.Substring(0,50))..." -ForegroundColor Gray
    Write-Host ""
    Write-Host "Credentials:" -ForegroundColor Yellow
    Write-Host "  admin   -> Admin123!" -ForegroundColor White
    Write-Host "  user    -> User1234!" -ForegroundColor White
    Write-Host "  manager -> Manager123!" -ForegroundColor White
    Write-Host ""
}
catch {
    Write-Host ""
    Write-Host "STILL FAILED!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Last logs:" -ForegroundColor Cyan
    docker logs api-gateway --tail 10 2>&1 | Select-String "STEP|Password"
    Write-Host ""
}

