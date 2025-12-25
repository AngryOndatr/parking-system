# Script to backup PostgreSQL database
# Usage: .\backup-db.ps1 [-OutputDir "path\to\backup"]

param(
    [Parameter(Mandatory=$false)]
    [string]$OutputDir = "..\backups",

    [Parameter(Mandatory=$false)]
    [string]$ContainerName = "parking_db",

    [Parameter(Mandatory=$false)]
    [string]$DatabaseName = "parking_db",

    [Parameter(Mandatory=$false)]
    [string]$Username = "postgres"
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PostgreSQL Database Backup" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

# Create backup directory if it doesn't exist
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
    Write-Host "[INFO] Created backup directory: $OutputDir" -ForegroundColor Yellow
}

# Generate timestamp
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = Join-Path $OutputDir "parking_db_backup_${timestamp}.sql"

Write-Host "Container: $ContainerName" -ForegroundColor White
Write-Host "Database:  $DatabaseName" -ForegroundColor White
Write-Host "Output:    $backupFile`n" -ForegroundColor White

# Check if container is running
$containerStatus = docker inspect -f '{{.State.Running}}' $ContainerName 2>$null

if ($containerStatus -ne "true") {
    Write-Host "[ERROR] Container '$ContainerName' is not running!" -ForegroundColor Red
    Write-Host "Start it with: docker-compose -f docker-compose.infrastructure.yml up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "Creating backup..." -ForegroundColor Yellow

try {
    # Create backup
    docker exec $ContainerName pg_dump -U $Username $DatabaseName > $backupFile

    if ($LASTEXITCODE -eq 0 -and (Test-Path $backupFile)) {
        $fileSize = (Get-Item $backupFile).Length / 1KB
        Write-Host "`n[SUCCESS] Backup created successfully!" -ForegroundColor Green
        Write-Host "File: $backupFile" -ForegroundColor Cyan
        Write-Host "Size: $([math]::Round($fileSize, 2)) KB" -ForegroundColor Cyan

        # List recent backups
        Write-Host "`n========================================" -ForegroundColor Cyan
        Write-Host "  Recent Backups" -ForegroundColor Yellow
        Write-Host "========================================" -ForegroundColor Cyan

        Get-ChildItem $OutputDir -Filter "parking_db_backup_*.sql" |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 5 |
            Format-Table Name,
                @{Label="Size (KB)"; Expression={[math]::Round($_.Length / 1KB, 2)}},
                @{Label="Created"; Expression={$_.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss")}} -AutoSize

        Write-Host "`n========================================" -ForegroundColor Cyan
        Write-Host "  Restore Instructions" -ForegroundColor Yellow
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "To restore this backup:" -ForegroundColor White
        Write-Host "  Get-Content `"$backupFile`" | docker exec -i $ContainerName psql -U $Username -d $DatabaseName" -ForegroundColor Cyan
        Write-Host "========================================`n" -ForegroundColor Cyan

    } else {
        Write-Host "`n[ERROR] Backup failed!" -ForegroundColor Red
        exit 1
    }

} catch {
    Write-Host "`n[ERROR] An error occurred during backup:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

