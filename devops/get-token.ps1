# Script to get JWT token from API Gateway
# Usage: .\get-token.ps1 [username] [password]

param(
    [Parameter(Mandatory=$false)]
    [string]$Username = "admin",

    [Parameter(Mandatory=$false)]
    [string]$Password = "parking123"
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Getting JWT Token" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nUsername: $Username" -ForegroundColor Yellow
Write-Host "API Gateway: http://localhost:8086" -ForegroundColor Yellow

# Prepare request body
$body = @{
    username = $Username
    password = $Password
} | ConvertTo-Json

Write-Host "`nSending login request..." -ForegroundColor White

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -UseBasicParsing `
        -ErrorAction Stop

    $auth = $response.Content | ConvertFrom-Json

    Write-Host "`n[SUCCESS] Token received!" -ForegroundColor Green
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  User Information" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Username: $($auth.user.username)" -ForegroundColor White
    Write-Host "Role:     $($auth.user.role)" -ForegroundColor White
    Write-Host "User ID:  $($auth.user.id)" -ForegroundColor White

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  Tokens" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Session Timeout: $($auth.sessionTimeoutMinutes) minutes" -ForegroundColor White

    Write-Host "`nAccess Token (first 100 chars):" -ForegroundColor Yellow
    $tokenPreview = if ($auth.accessToken.Length -gt 100) {
        $auth.accessToken.Substring(0, 100) + "..."
    } else {
        $auth.accessToken
    }
    Write-Host $tokenPreview -ForegroundColor Cyan

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  Saving to Variables" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan

    # Save tokens to global variables
    $global:token = $auth.accessToken
    $global:refreshToken = $auth.refreshToken

    Write-Host "[OK] Access token saved to:`$token" -ForegroundColor Green
    Write-Host "[OK] Refresh token saved to: `$refreshToken" -ForegroundColor Green

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  Usage Examples" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan

    Write-Host "`n1. Test with Client Service (direct):" -ForegroundColor White
    Write-Host @'
Invoke-WebRequest -Uri "http://localhost:8081/api/clients" -Headers @{"Authorization" = "Bearer $token"} -UseBasicParsing
'@ -ForegroundColor Cyan

    Write-Host "`n2. Test with API Gateway (recommended):" -ForegroundColor White
    Write-Host @'
Invoke-WebRequest -Uri "http://localhost:8086/client-service/api/clients" -Headers @{"Authorization" = "Bearer $token"} -UseBasicParsing
'@ -ForegroundColor Cyan

    Write-Host "`n========================================" -ForegroundColor Cyan

    return $auth

} catch {
    Write-Host "`n[ERROR] Failed to get token!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red

    if ($_.Exception.Response) {
        try {
            $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            $reader.Close()
            Write-Host "`nServer Response:" -ForegroundColor Yellow
            Write-Host $errorBody -ForegroundColor Red
        } catch {
            Write-Host "Could not read error response" -ForegroundColor Yellow
        }
    }

    Write-Host "`nPossible issues:" -ForegroundColor Yellow
    Write-Host "1. API Gateway is not running (check: docker ps | grep api-gateway)" -ForegroundColor White
    Write-Host "2. Wrong username/password" -ForegroundColor White
    Write-Host "3. Database connection issue" -ForegroundColor White
    Write-Host "`nCheck logs: docker logs api-gateway --tail 50" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    exit 1
}

