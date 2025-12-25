#!/usr/bin/env pwsh
# Debug 403 Error - Complete Analysis
# Run this script to identify and fix the 403 Forbidden error

$ErrorActionPreference = "Continue"

Write-Host "`n" -NoNewline
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  API Gateway 403 Debug Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check container status
Write-Host "STEP 1: Checking Container Status" -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Gray
$apiGateway = docker ps --filter "name=api-gateway" --format "{{.Status}}"
if ($apiGateway -match "Up") {
    Write-Host "OK: API Gateway is running" -ForegroundColor Green
    Write-Host "   Status: $apiGateway" -ForegroundColor Gray
} else {
    Write-Host "ERROR: API Gateway is not running!" -ForegroundColor Red
    Write-Host "   Starting it now..." -ForegroundColor Yellow
    docker-compose up -d api-gateway
    Start-Sleep -Seconds 20
}

# Step 2: Test Login
Write-Host "`nSTEP 2: Testing Login" -ForegroundColor Yellow
Write-Host "----------------------" -ForegroundColor Gray

$loginBody = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop

    $token = $loginResponse.accessToken
    Write-Host "OK: Login successful" -ForegroundColor Green
    Write-Host "   Token (first 40 chars): $($token.Substring(0, [Math]::Min(40, $token.Length)))..." -ForegroundColor Gray
    Write-Host "   Token length: $($token.Length) characters" -ForegroundColor Gray

} catch {
    Write-Host "ERROR: Login failed!" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red

    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "   Status Code: $statusCode" -ForegroundColor Red
    }

    Write-Host "`nCannot proceed without token. Exiting." -ForegroundColor Red
    exit 1
}

# Step 3: Test /api/clients with token
Write-Host "`nSTEP 3: Testing /api/clients Access" -ForegroundColor Yellow
Write-Host "------------------------------------" -ForegroundColor Gray

$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
    "User-Agent" = "Debug-Script/1.0"
}

Write-Host "Sending request to: http://localhost:8086/api/clients" -ForegroundColor Gray
Write-Host "Authorization header: Bearer $($token.Substring(0,20))..." -ForegroundColor Gray

try {
    $clientsResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/clients" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop

    Write-Host "SUCCESS! /api/clients is accessible" -ForegroundColor Green
    Write-Host "   Response:" -ForegroundColor Cyan
    $clientsResponse | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor White

} catch {
    Write-Host "FAILED: Cannot access /api/clients" -ForegroundColor Red

    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "   Status Code: $statusCode" -ForegroundColor Red

        # Try to get response body
        try {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $responseBody = $reader.ReadToEnd()
            if ($responseBody) {
                Write-Host "   Response Body: $responseBody" -ForegroundColor Red
            }
        } catch {
            # Ignore if can't read response
        }
    }

    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 4: Analyze logs
Write-Host "`nSTEP 4: Analyzing API Gateway Logs" -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Gray

$logs = docker logs api-gateway --tail 100 2>&1

# Look for security filter messages
$securityLogs = $logs | Select-String -Pattern "Security filter|🔍|🔑|🔒|✅|❌" -Context 0,2

if ($securityLogs) {
    Write-Host "Found Security Filter logs:" -ForegroundColor Cyan
    $securityLogs | ForEach-Object { Write-Host $_.Line -ForegroundColor White }
} else {
    Write-Host "WARNING: No Security Filter logs found!" -ForegroundColor Red
    Write-Host "This might mean:" -ForegroundColor Yellow
    Write-Host "  - Old code is still running (rebuild needed)" -ForegroundColor Yellow
    Write-Host "  - Logs level is too high" -ForegroundColor Yellow
    Write-Host "  - Request didn't reach the security filter" -ForegroundColor Yellow
}

# Look for recent requests to /api/clients
Write-Host "`nLooking for /api/clients requests..." -ForegroundColor Cyan
$clientRequests = $logs | Select-String -Pattern "/api/clients|ClientProxyController"

if ($clientRequests) {
    Write-Host "Found /api/clients activity:" -ForegroundColor Green
    $clientRequests | Select-Object -Last 5 | ForEach-Object { Write-Host $_.Line -ForegroundColor White }
} else {
    Write-Host "WARNING: No /api/clients requests in logs!" -ForegroundColor Red
    Write-Host "This means requests are being blocked before reaching the controller" -ForegroundColor Yellow
}

# Look for JWT validation logs
Write-Host "`nLooking for JWT validation..." -ForegroundColor Cyan
$jwtLogs = $logs | Select-String -Pattern "JWT|token|validation|Bearer"

if ($jwtLogs) {
    Write-Host "Found JWT-related logs:" -ForegroundColor Green
    $jwtLogs | Select-Object -Last 10 | ForEach-Object { Write-Host $_.Line -ForegroundColor White }
} else {
    Write-Host "No JWT validation logs found" -ForegroundColor Yellow
}

# Summary
Write-Host "`n" -NoNewline
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SUMMARY & RECOMMENDATIONS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nDiagnostics:" -ForegroundColor Yellow
Write-Host "  Container Status: " -NoNewline
Write-Host "OK" -ForegroundColor Green

Write-Host "  Login: " -NoNewline
if ($token) {
    Write-Host "OK" -ForegroundColor Green
} else {
    Write-Host "FAILED" -ForegroundColor Red
}

Write-Host "  /api/clients Access: " -NoNewline
if ($clientsResponse) {
    Write-Host "OK" -ForegroundColor Green
} else {
    Write-Host "FAILED (403)" -ForegroundColor Red
}

Write-Host "  Security Filter Logs: " -NoNewline
if ($securityLogs) {
    Write-Host "Found" -ForegroundColor Green
} else {
    Write-Host "Not Found" -ForegroundColor Red
}

Write-Host "`nNext Steps:" -ForegroundColor Yellow

if (-not $securityLogs) {
    Write-Host "  1. Docker image may not have latest code" -ForegroundColor Red
    Write-Host "     Run: docker-compose down && docker-compose up -d --build" -ForegroundColor White
}

if ($clientsResponse) {
    Write-Host "  PROBLEM SOLVED! /api/clients is now accessible!" -ForegroundColor Green
} else {
    Write-Host "  2. Check Security Filter configuration" -ForegroundColor Red
    Write-Host "     File: backend/api-gateway/.../SecurityFilter.java" -ForegroundColor White
    Write-Host "  3. Verify JWT token validation logic" -ForegroundColor Red
    Write-Host "  4. Check Spring Security configuration" -ForegroundColor Red
}

Write-Host "`n" -NoNewline
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

