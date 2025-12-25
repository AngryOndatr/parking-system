# Script to restore PostgreSQL database from backup
# Usage: .\restore-db.ps1 -BackupFile "path\to\backup.sql"

param(
    [Parameter(Mandatory=$true)]
    [string]$BackupFile,

    [Parameter(Mandatory=$false)]
    [string]$ContainerName = "parking_db",

    [Parameter(Mandatory=$false)]
    [string]$DatabaseName = "parking_db",

    [Parameter(Mandatory=$false)]
    [string]$Username = "postgres",

    [Parameter(Mandatory=$false)]
    [switch]$Force = $false
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PostgreSQL Database Restore" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

# Check if backup file exists
if (-not (Test-Path $BackupFile)) {
    Write-Host "[ERROR] Backup file not found: $BackupFile" -ForegroundColor Red
    Write-Host "`nAvailable backups:" -ForegroundColor Yellow

    $backupDir = "..\backups"
    if (Test-Path $backupDir) {
        Get-ChildItem $backupDir -Filter "parking_db_backup_*.sql" |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 10 |
            Format-Table Name,
                @{Label="Size (KB)"; Expression={[math]::Round($_.Length / 1KB, 2)}},
                @{Label="Created"; Expression={$_.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss")}} -AutoSize
    }
    exit 1
}

# Check if container is running
$containerStatus = docker inspect -f '{{.State.Running}}' $ContainerName 2>$null

if ($containerStatus -ne "true") {
    Write-Host "[ERROR] Container '$ContainerName' is not running!" -ForegroundColor Red
    Write-Host "Start it with: docker-compose -f docker-compose.infrastructure.yml up -d" -ForegroundColor Yellow
    exit 1
}

$fileSize = (Get-Item $BackupFile).Length / 1KB
Write-Host "Container: $ContainerName" -ForegroundColor White
Write-Host "Database:  $DatabaseName" -ForegroundColor White
Write-Host "Backup:    $BackupFile" -ForegroundColor White
Write-Host "Size:      $([math]::Round($fileSize, 2)) KB`n" -ForegroundColor White

# Warning
if (-not $Force) {
    Write-Host "⚠️  WARNING: This will OVERWRITE the current database!" -ForegroundColor Red
    Write-Host "All existing data will be REPLACED with backup data.`n" -ForegroundColor Red

    $confirmation = Read-Host "Type 'yes' to continue"

    if ($confirmation -ne 'yes') {
        Write-Host "`nRestore cancelled." -ForegroundColor Yellow
        exit 0
    }
}

Write-Host "`nRestoring database..." -ForegroundColor Yellow

try {
    # Drop existing connections
    Write-Host "[1/3] Terminating active connections..." -ForegroundColor Cyan
    docker exec $ContainerName psql -U $Username -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$DatabaseName' AND pid <> pg_backend_pid();" | Out-Null

    # Drop and recreate database
    Write-Host "[2/3] Recreating database..." -ForegroundColor Cyan
    docker exec $ContainerName psql -U $Username -d postgres -c "DROP DATABASE IF EXISTS $DatabaseName;" 2>&1 | Out-Null
    docker exec $ContainerName psql -U $Username -d postgres -c "CREATE DATABASE $DatabaseName;" 2>&1 | Out-Null

    # Restore from backup
    Write-Host "[3/3] Restoring data from backup..." -ForegroundColor Cyan
    Get-Content $BackupFile | docker exec -i $ContainerName psql -U $Username -d $DatabaseName 2>&1 | Out-Null

    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n[SUCCESS] Database restored successfully!" -ForegroundColor Green

        # Show statistics
        Write-Host "`n========================================" -ForegroundColor Cyan
        Write-Host "  Database Statistics" -ForegroundColor Yellow
        Write-Host "========================================" -ForegroundColor Cyan

        docker exec $ContainerName psql -U $Username -d $DatabaseName -c "
            SELECT
                schemaname,
                tablename,
                (SELECT COUNT(*) FROM pg_catalog.pg_tables WHERE schemaname = 'public') as total_tables
            FROM pg_tables
            WHERE schemaname = 'public'
            LIMIT 1;
        "

        Write-Host "`n========================================" -ForegroundColor Cyan

    } else {
        Write-Host "`n[ERROR] Restore failed!" -ForegroundColor Red
        exit 1
    }

} catch {
    Write-Host "`n[ERROR] An error occurred during restore:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

