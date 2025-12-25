# Update all password references in the project
# From: parking123, user1234, manager123
# To: Admin123!, User1234!, Manager123!

Write-Host "`n=== Password Update Script ===" -ForegroundColor Cyan
Write-Host "Updating all password references to new standards..." -ForegroundColor Yellow
Write-Host ""

$rootPath = "C:\Users\user\Projects\parking-system"
$filesUpdated = 0

# Define replacements
$replacements = @(
    @{ Old = 'parking123'; New = 'Admin123!' },
    @{ Old = 'user1234'; New = 'User1234!' },
    @{ Old = 'manager123'; New = 'Manager123!' }
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
Write-Host "New password standards:" -ForegroundColor Yellow
Write-Host "  admin   -> Admin123!   (9 chars)" -ForegroundColor White
Write-Host "  user    -> User1234!   (9 chars)" -ForegroundColor White
Write-Host "  manager -> Manager123! (11 chars)" -ForegroundColor White
Write-Host ""

