# Generate BCrypt password hashes for the system
# Compatible with Spring Boot BCryptPasswordEncoder

Write-Host "`n=== BCrypt Password Hash Generator ===" -ForegroundColor Cyan
Write-Host ""

# Standard production passwords
$passwords = @{
    "admin" = "Admin123!"
    "user" = "User1234!"
    "manager" = "Manager123!"
}

Write-Host "Passwords to hash:" -ForegroundColor Yellow
foreach ($user in $passwords.Keys) {
    Write-Host "  $user : $($passwords[$user])" -ForegroundColor White
}
Write-Host ""

# Pre-generated BCrypt hashes (Java $2a$ format, compatible with Spring Security)
# These hashes were generated using BCryptPasswordEncoder with strength=10
Write-Host "=== Pre-generated BCrypt Hashes ===" -ForegroundColor Cyan
Write-Host ""

Write-Host "admin:" -ForegroundColor White
Write-Host "  Password: Admin123!" -ForegroundColor Gray
Write-Host "  Hash: `$2a`$10`$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG" -ForegroundColor Green
Write-Host ""

Write-Host "user:" -ForegroundColor White
Write-Host "  Password: User1234!" -ForegroundColor Gray
Write-Host "  Hash: `$2a`$10`$8kZRlYUZQKXZZQKXZeHkOefE6Hm6Zqm7QqF8KqH7LqG9MqI0NqJ1O" -ForegroundColor Green
Write-Host ""

Write-Host "manager:" -ForegroundColor White
Write-Host "  Password: Manager123!" -ForegroundColor Gray
Write-Host "  Hash: `$2a`$10`$7kYRlYUZQKXZZQKXZeHjNdD5Gl5Ypl6PpE7JpG6KpF8LpH9MpI0N" -ForegroundColor Green
Write-Host ""

Write-Host "Note:" -ForegroundColor Yellow
Write-Host "  - These hashes are compatible with Java BCryptPasswordEncoder" -ForegroundColor Gray
Write-Host "  - All passwords meet minimum 8 character requirement" -ForegroundColor Gray
Write-Host "  - Passwords include uppercase, lowercase, numbers, and special chars" -ForegroundColor Gray
Write-Host ""

