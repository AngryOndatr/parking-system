param (
    [switch]$Prod
)

$scriptPath = Join-Path $PSScriptRoot 'full-rebuild.ps1'
& $scriptPath -Prod:$Prod
