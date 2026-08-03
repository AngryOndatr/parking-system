# System Management Script
param (
    [switch]$Prod
)

$ComposeFiles = if ($Prod) {
    "-f docker-compose.yml -f docker-compose.prod.yml --env-file devops\.env.prod"
} else {
    "-f docker-compose.yml -f docker-compose.override.yml --env-file devops\.env.dev"
}

Write-Host "Executing start-observability.ps1 ( mode)..." -ForegroundColor Cyan
Invoke-Expression "docker-compose $ComposeFiles up -d"
