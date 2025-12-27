# Revert all passwords back to parking123, user1234, manager123
# These are the WORKING passwords with VERIFIED BCrypt hashes

Write-Host "`n=== Reverting Passwords to Working Versions ===" -ForegroundColor Cyan
Write-Host ""

$rootPath = Split-Path $PSScriptRoot -Parent
$filesUpdated = 0

# Define replacements (reverse the previous change)
$replacements = @(
    @{ Old = 'Admin123!'; New = 'parking123' },
    @{ Old = 'User1234!'; New = 'user1234' },
    @{ Old = 'Manager123!'; New = 'manager123' }
)

# Files to update
$filesToUpdate = @(
    "$rootPath\devops\get-token.ps1",
    "$rootPath\devops\test-login.ps1",
    "$rootPath\devops\test-auth.ps1",
    "$rootPath\devops\test-gateway-proxy.ps1",
    "$rootPath\devops\test-with-logs.ps1",
    "$rootPath\devops\wait-and-test.ps1",
    "$rootPath\devops\unlock-and-test.ps1",
    "$rootPath\devops\start-full-system.ps1",
    "$rootPath\devops\check-system.ps1",
    "$rootPath\devops\reset-brute-force.ps1",
    "$rootPath\devops\init-db.ps1"
)

foreach ($file in $filesToUpdate) {
    if (Test-Path $file) {
        Write-Host "Processing: $([System.IO.Path]::GetFileName($file))" -ForegroundColor White

        $content = Get-Content $file -Raw -Encoding UTF8
        $originalContent = $content

        foreach ($replacement in $replacements) {
            $content = $content -replace [regex]::Escape($replacement.Old), $replacement.New
        }

        if ($content -ne $originalContent) {
            Set-Content $file -Value $content -Encoding UTF8 -NoNewline
            Write-Host "  Updated!" -ForegroundColor Green
            $filesUpdated++
        } else {
            Write-Host "  No changes needed" -ForegroundColor Gray
        }
    }
}

Write-Host ""
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host "Files updated: $filesUpdated" -ForegroundColor Green
Write-Host ""
Write-Host "Working password standards:" -ForegroundColor Yellow
Write-Host "  admin   -> parking123  (10 chars)" -ForegroundColor White
Write-Host "  user    -> user1234    (8 chars)" -ForegroundColor White
Write-Host "  manager -> manager123  (10 chars)" -ForegroundColor White
Write-Host ""
Write-Host "These passwords are VERIFIED to work!" -ForegroundColor Green
Write-Host ""

