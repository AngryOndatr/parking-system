# Script to initialize database with test users
# Usage: .\init-db.ps1

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Database Initialization" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Loading test users into database..." -ForegroundColor Yellow

$sqlFile = "..\database\insert_users.sql"

if (-not (Test-Path $sqlFile)) {
    Write-Host "[ERROR] SQL file not found: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "SQL file: $sqlFile" -ForegroundColor White

try {
    # Load SQL content
    $sqlContent = Get-Content $sqlFile -Raw

    # Execute SQL in database
    $sqlContent | docker exec -i parking_db psql -U postgres -d parking_db

    Write-Host "`n[SUCCESS] Test users loaded!" -ForegroundColor Green

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  Available Users" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan

    # Show loaded users
    docker exec parking_db psql -U postgres -d parking_db -c "SELECT username, user_role, created_at FROM users ORDER BY username;"

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  Credentials" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "admin    / parking123  (ADMIN)" -ForegroundColor Green
    Write-Host "user     / user123     (USER)" -ForegroundColor Green
    Write-Host "manager  / manager123  (ADMIN)" -ForegroundColor Green

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  Next Step: Get JWT Token" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Run: .\get-token.ps1" -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan

} catch {
    Write-Host "`n[ERROR] Failed to initialize database!" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host "`nMake sure PostgreSQL container is running:" -ForegroundColor Yellow
    Write-Host "docker ps | grep parking_db" -ForegroundColor Cyan
    exit 1
}

