# Test database connection from host
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PostgreSQL Connection Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Connection parameters (same as application.yml defaults)
$dbHost = "localhost"
$dbPort = 5432
$dbName = "parking_db"
$dbUser = "postgres"
$dbPassword = "postgres"

Write-Host "Connection Parameters:" -ForegroundColor Yellow
Write-Host "  Host:     $dbHost" -ForegroundColor White
Write-Host "  Port:     $dbPort" -ForegroundColor White
Write-Host "  Database: $dbName" -ForegroundColor White
Write-Host "  User:     $dbUser" -ForegroundColor White
Write-Host ""

# Test 1: Check if port is accessible
Write-Host "[1/3] Testing port accessibility..." -ForegroundColor Cyan
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect($dbHost, $dbPort)
    $tcpClient.Close()
    Write-Host "✅ Port $dbPort is accessible on $dbHost" -ForegroundColor Green
} catch {
    Write-Host "❌ Port $dbPort is NOT accessible: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`n⚠️  SOLUTION: Make sure PostgreSQL container is running:" -ForegroundColor Yellow
    Write-Host "   docker ps | Select-String parking_db" -ForegroundColor Gray
    exit 1
}

# Test 2: Test PostgreSQL connection with psql
Write-Host "`n[2/3] Testing PostgreSQL authentication..." -ForegroundColor Cyan
$env:PGPASSWORD = $dbPassword

try {
    $result = docker exec parking_db psql -U $dbUser -d $dbName -c "SELECT version();" 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Successfully connected to database '$dbName'" -ForegroundColor Green
        Write-Host "   PostgreSQL version: $($result | Select-Object -First 1)" -ForegroundColor Gray
    } else {
        Write-Host "❌ Failed to connect to database" -ForegroundColor Red
        Write-Host "   Error: $result" -ForegroundColor Red
    }
} catch {
    Write-Host "⚠️  Could not test via psql: $_" -ForegroundColor Yellow
}

# Test 3: Check if database exists and has tables
Write-Host "`n[3/3] Checking database structure..." -ForegroundColor Cyan
try {
    $tables = docker exec parking_db psql -U $dbUser -d $dbName -c "\dt" 2>&1

    if ($tables -match "users") {
        Write-Host "✅ Database structure is correct" -ForegroundColor Green
        Write-Host "   Found tables: users, clients, logs, etc." -ForegroundColor Gray
    } else {
        Write-Host "⚠️  Tables not found or database not initialized" -ForegroundColor Yellow
        Write-Host "   Run: .\devops\init-db.ps1" -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️  Could not check database structure" -ForegroundColor Yellow
}

# Test 4: Verify users exist
Write-Host "`n[4/4] Checking test users..." -ForegroundColor Cyan
try {
    $users = docker exec parking_db psql -U $dbUser -d $dbName -tc "SELECT COUNT(*) FROM users;" 2>&1
    $userCount = ($users | Out-String).Trim()

    if ($userCount -match "\d+" -and [int]$userCount -gt 0) {
        Write-Host "✅ Found $userCount user(s) in database" -ForegroundColor Green
    } else {
        Write-Host "⚠️  No users found in database" -ForegroundColor Yellow
        Write-Host "   Run: .\devops\init-db.ps1" -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️  Could not check users" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Database is ready for API Gateway" -ForegroundColor Green
Write-Host "`nYou can now start API Gateway:" -ForegroundColor White
Write-Host "   • Run ApiGatewayApplication in IntelliJ IDEA" -ForegroundColor Gray
Write-Host "   • Or: mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""

