# ============================================================
# API Gateway Proxy Smoke Tests
# Purpose: Verify API Gateway correctly proxies requests to services
# Usage: .\test-proxy.ps1
# ============================================================

$ErrorActionPreference = "Continue"
$API_GATEWAY = "http://localhost:8086"
$TOKEN = $null

Write-Host "`nAPI Gateway Proxy Smoke Tests" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Gray
Write-Host "Gateway URL: $API_GATEWAY`n" -ForegroundColor White

# ============================================================
# Step 1: Authentication
# ============================================================
Write-Host "`n[STEP 1] Authenticating..." -ForegroundColor Yellow

$loginBody = @{
    username = "admin"
    password = "parking123"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$API_GATEWAY/api/auth/login" `
        -Method POST `
        -Body $loginBody `
        -ContentType "application/json"

    $TOKEN = $authResponse.accessToken

    if ($TOKEN) {
        Write-Host "[OK] Authentication successful" -ForegroundColor Green
        Write-Host "   Token length: $($TOKEN.Length) characters`n" -ForegroundColor Gray
    } else {
        Write-Host "[ERROR] Authentication failed - no token received" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "[ERROR] Authentication failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# ============================================================
# Helper Function: Test Endpoint
# ============================================================
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Description,
        [object]$Body = $null,
        [bool]$RequiresAuth = $true
    )

    Write-Host "Testing: $Description" -ForegroundColor Cyan
    Write-Host "  $Method $Endpoint" -ForegroundColor Gray

    try {
        $headers = @{
            "Content-Type" = "application/json"
        }

        if ($RequiresAuth -and $TOKEN) {
            $headers["Authorization"] = "Bearer $TOKEN"
        }

        $params = @{
            Uri = "$API_GATEWAY$Endpoint"
            Method = $Method
            Headers = $headers
        }

        if ($Body) {
            $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
        }

        $response = Invoke-RestMethod @params

        Write-Host "  [OK] Status: 200 OK" -ForegroundColor Green

        if ($response -is [array]) {
            Write-Host "  [INFO] Response: Array with $($response.Count) items" -ForegroundColor Gray
        }
        elseif ($response -is [PSCustomObject]) {
            Write-Host "  [INFO] Response: Object with $($response.PSObject.Properties.Count) properties" -ForegroundColor Gray
        }
        else {
            Write-Host "  [INFO] Response: $($response.GetType().Name)" -ForegroundColor Gray
        }

        Write-Host ""
        return $true
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorMessage = $_.Exception.Message

        Write-Host "  [ERROR] Status: $statusCode" -ForegroundColor Red
        Write-Host "  [ERROR] Error: $errorMessage" -ForegroundColor Red
        Write-Host ""

        return $false
    }
}

# ============================================================
# Step 2: Management Service Proxy Tests
# ============================================================
Write-Host "`n[STEP 2] Testing Management Service Proxy" -ForegroundColor Yellow
Write-Host "-" * 60 -ForegroundColor Gray

$managementTests = @(
    @{
        Method = "GET"
        Endpoint = "/api/management/spots/available"
        Description = "Get available parking spots"
    },
    @{
        Method = "GET"
        Endpoint = "/api/management/spots/available/count"
        Description = "Get available spots count"
    },
    @{
        Method = "GET"
        Endpoint = "/api/management/spots"
        Description = "Get all parking spots"
    },
    @{
        Method = "GET"
        Endpoint = "/api/management/spots/search?type=STANDARD`&status=AVAILABLE"
        Description = "Search spots by type and status"
    }
)

$managementPassed = 0
$managementFailed = 0

foreach ($test in $managementTests) {
    if (Test-Endpoint -Method $test.Method -Endpoint $test.Endpoint -Description $test.Description) {
        $managementPassed++
    } else {
        $managementFailed++
    }
}

Write-Host "Management Service Results: $managementPassed passed, $managementFailed failed" -ForegroundColor $(if ($managementFailed -eq 0) { "Green" } else { "Yellow" })

# ============================================================
# Step 3: Reporting Service Proxy Tests
# ============================================================
Write-Host "`n[STEP 3] Testing Reporting Service Proxy" -ForegroundColor Yellow
Write-Host "-" * 60 -ForegroundColor Gray

# Test 3.1: Create Log Entry (POST)
$logBody = @{
    timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    level = "INFO"
    service = "test-proxy-script"
    message = "Smoke test log entry"
    userId = 1
    meta = @{
        test = $true
        timestamp = (Get-Date).ToString("o")
    }
}

$reportingTests = @(
    @{
        Method = "POST"
        Endpoint = "/api/reporting/log"
        Description = "Create log entry"
        Body = $logBody
    },
    @{
        Method = "GET"
        Endpoint = "/api/reporting/logs"
        Description = "Get all logs"
    },
    @{
        Method = "GET"
        Endpoint = "/api/reporting/logs?level=INFO"
        Description = "Get logs by level (INFO)"
    },
    @{
        Method = "GET"
        Endpoint = "/api/reporting/logs?service=test-proxy-script"
        Description = "Get logs by service"
    },
    @{
        Method = "GET"
        Endpoint = "/api/reporting/logs?limit=5"
        Description = "Get logs with limit"
    }
)

$reportingPassed = 0
$reportingFailed = 0

foreach ($test in $reportingTests) {
    if (Test-Endpoint -Method $test.Method -Endpoint $test.Endpoint -Description $test.Description -Body $test.Body) {
        $reportingPassed++
    } else {
        $reportingFailed++
    }
}

Write-Host "Reporting Service Results: $reportingPassed passed, $reportingFailed failed" -ForegroundColor $(if ($reportingFailed -eq 0) { "Green" } else { "Yellow" })

# ============================================================
# Step 4: Client Service Proxy Tests (existing)
# ============================================================
Write-Host "`n[STEP 4] Testing Client Service Proxy" -ForegroundColor Yellow
Write-Host "-" * 60 -ForegroundColor Gray

$clientTests = @(
    @{
        Method = "GET"
        Endpoint = "/api/clients"
        Description = "Get all clients"
    },
    @{
        Method = "GET"
        Endpoint = "/api/vehicles"
        Description = "Get all vehicles"
    }
)

$clientPassed = 0
$clientFailed = 0

foreach ($test in $clientTests) {
    if (Test-Endpoint -Method $test.Method -Endpoint $test.Endpoint -Description $test.Description) {
        $clientPassed++
    } else {
        $clientFailed++
    }
}

Write-Host "Client Service Results: $clientPassed passed, $clientFailed failed" -ForegroundColor $(if ($clientFailed -eq 0) { "Green" } else { "Yellow" })

# ============================================================
# Final Summary
# ============================================================
Write-Host "`n" + ("=" * 60) -ForegroundColor Gray
Write-Host "[TEST SUMMARY]" -ForegroundColor Cyan
Write-Host ("=" * 60) -ForegroundColor Gray

$totalPassed = $managementPassed + $reportingPassed + $clientPassed
$totalFailed = $managementFailed + $reportingFailed + $clientFailed
$totalTests = $totalPassed + $totalFailed

Write-Host "`nManagement Service: $managementPassed/$($managementTests.Count) passed" -ForegroundColor $(if ($managementFailed -eq 0) { "Green" } else { "Yellow" })
Write-Host "Reporting Service:  $reportingPassed/$($reportingTests.Count) passed" -ForegroundColor $(if ($reportingFailed -eq 0) { "Green" } else { "Yellow" })
Write-Host "Client Service:     $clientPassed/$($clientTests.Count) passed" -ForegroundColor $(if ($clientFailed -eq 0) { "Green" } else { "Yellow" })
Write-Host ("-" * 60) -ForegroundColor Gray
Write-Host "Total:              $totalPassed/$totalTests passed" -ForegroundColor $(if ($totalFailed -eq 0) { "Green" } else { "Yellow" })

if ($totalFailed -eq 0) {
    Write-Host "`n[SUCCESS] All proxy tests passed!" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "`nSome tests failed. Check the output above for details." -ForegroundColor Yellow
    exit 1
}

