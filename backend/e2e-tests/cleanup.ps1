# Docker Cleanup Script
Write-Host "Cleaning Docker..." -ForegroundColor Cyan

$containers = docker ps -q
if ($containers) { docker stop $containers; docker rm $containers }

$allContainers = docker ps -aq
if ($allContainers) { docker rm $allContainers }

docker network prune -f

Write-Host "Waiting 15 seconds..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

$ports = @(5432, 6379, 8761, 4318)
foreach ($port in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conn) {
        Write-Host "Port $port in use!" -ForegroundColor Red
    } else {
        Write-Host "Port $port free" -ForegroundColor Green
    }
}

Write-Host "Cleanup done!" -ForegroundColor Green

