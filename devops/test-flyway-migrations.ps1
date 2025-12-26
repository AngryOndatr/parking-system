# ============================================
#   TEST FLYWAY MIGRATIONS
# ============================================

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   FLYWAY MIGRATIONS TEST" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Step 1: Check if database is ready
Write-Host "Step 1: Checking database connection..." -ForegroundColor Yellow
$dbCheck = docker exec parking_db psql -U postgres -d parking_db -c "\l" 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Database is accessible" -ForegroundColor Green
} else {
    Write-Host "✗ Database connection failed" -ForegroundColor Red
    exit 1
}

# Step 2: Count all tables
Write-Host "`nStep 2: Counting tables..." -ForegroundColor Yellow
$tableCount = docker exec parking_db psql -U postgres -d parking_db -t -A -c "SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public';" 2>&1
Write-Host "  Total tables: $tableCount" -ForegroundColor White

# Step 3: Check new tables existence
Write-Host "`nStep 3: Checking new tables..." -ForegroundColor Yellow

$tables = @(
    "flyway_schema_history",
    "parking_lots",
    "parking_spaces",
    "bookings"
)

foreach ($table in $tables) {
    $exists = docker exec parking_db psql -U postgres -d parking_db -t -A -c "SELECT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = '$table');" 2>&1
    if ($exists -match "t") {
        Write-Host "  ✓ $table" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $table NOT FOUND" -ForegroundColor Red
    }
}

# Step 4: Check Flyway history
Write-Host "`nStep 4: Checking Flyway migration history..." -ForegroundColor Yellow
$migrationCount = docker exec parking_db psql -U postgres -d parking_db -t -A -c "SELECT COUNT(*) FROM flyway_schema_history;" 2>&1
Write-Host "  Total migrations: $migrationCount" -ForegroundColor White

# Get migration details
Write-Host "`n  Migration details:" -ForegroundColor Cyan
$migrations = docker exec parking_db psql -U postgres -d parking_db -t -A -F"|" -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;" 2>&1

foreach ($migration in $migrations) {
    if ($migration -and $migration.Trim()) {
        $parts = $migration.Split("|")
        if ($parts.Length -ge 4) {
            $rank = $parts[0].Trim()
            $version = $parts[1].Trim()
            $desc = $parts[2].Trim()
            $success = $parts[3].Trim()

            if ($success -eq "t") {
                Write-Host "    ✓ V$version : $desc" -ForegroundColor Green
            } else {
                Write-Host "    ✗ V$version : $desc (FAILED)" -ForegroundColor Red
            }
        }
    }
}

# Step 5: Check foreign keys
Write-Host "`nStep 5: Checking foreign keys..." -ForegroundColor Yellow
$fkCount = docker exec parking_db psql -U postgres -d parking_db -t -A -c "SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_type = 'FOREIGN KEY';" 2>&1
Write-Host "  Total foreign keys: $fkCount" -ForegroundColor White

# Step 6: Check indexes
Write-Host "`nStep 6: Checking indexes..." -ForegroundColor Yellow
$indexCount = docker exec parking_db psql -U postgres -d parking_db -t -A -c "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public';" 2>&1
Write-Host "  Total indexes: $indexCount" -ForegroundColor White

# Summary
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   SUMMARY" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Tables:     $tableCount" -ForegroundColor White
Write-Host "Migrations: $migrationCount" -ForegroundColor White
Write-Host "FKs:        $fkCount" -ForegroundColor White
Write-Host "Indexes:    $indexCount" -ForegroundColor White
Write-Host "============================================`n" -ForegroundColor Cyan

if ($migrationCount -ge 4) {
    Write-Host "ALL TESTS PASSED!" -ForegroundColor Green
} else {
    Write-Host "Expected 4+ migrations, found $migrationCount" -ForegroundColor Yellow
}

