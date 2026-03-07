# Build all Docker images required for E2E tests
# This script builds images with correct names for docker-compose-e2e.yml

Write-Host "=== Building Docker Images for E2E Tests ===" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"
$projectRoot = "C:\Users\user\Projects\parking-system"

# Function to build service image
function Build-ServiceImage {
    param (
        [string]$ServiceName,
        [string]$ServicePath
    )

    Write-Host "Building $ServiceName..." -ForegroundColor Yellow

    $fullPath = Join-Path $projectRoot $ServicePath

    if (Test-Path $fullPath) {
        Push-Location $fullPath

        # Build with Maven (skip tests for faster build)
        Write-Host "  Maven clean package..." -ForegroundColor Gray
        mvn clean package -DskipTests -q

        if ($LASTEXITCODE -ne 0) {
            Write-Host "  ❌ Maven build failed for $ServiceName" -ForegroundColor Red
            Pop-Location
            return $false
        }

        # Build Docker image
        Write-Host "  Building Docker image..." -ForegroundColor Gray
        docker build -t "${ServiceName}:latest" .

        if ($LASTEXITCODE -ne 0) {
            Write-Host "  ❌ Docker build failed for $ServiceName" -ForegroundColor Red
            Pop-Location
            return $false
        }

        Write-Host "  ✅ $ServiceName built successfully" -ForegroundColor Green
        Pop-Location
        return $true
    } else {
        Write-Host "  ❌ Path not found: $fullPath" -ForegroundColor Red
        return $false
    }
}

# Build all services
$services = @(
    @{Name="eureka-server"; Path="backend\eureka-server"},
    @{Name="api-gateway"; Path="backend\api-gateway"},
    @{Name="client-service"; Path="backend\client-service"},
    @{Name="gate-control-service"; Path="backend\gate-control-service"},
    @{Name="billing-service"; Path="backend\billing-service"},
    @{Name="reporting-service"; Path="backend\reporting-service"},
    @{Name="management-service"; Path="backend\management-service"}
)

$successCount = 0
$failCount = 0

foreach ($service in $services) {
    $result = Build-ServiceImage -ServiceName $service.Name -ServicePath $service.Path
    if ($result) {
        $successCount++
    } else {
        $failCount++
    }
    Write-Host ""
}

Write-Host "=== Build Summary ===" -ForegroundColor Cyan
Write-Host "✅ Success: $successCount" -ForegroundColor Green
Write-Host "❌ Failed: $failCount" -ForegroundColor Red
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "🎉 All images built successfully! Ready for E2E tests." -ForegroundColor Green
    Write-Host ""
    Write-Host "To run E2E tests:" -ForegroundColor Cyan
    Write-Host "  cd backend\e2e-tests" -ForegroundColor Gray
    Write-Host "  mvn test" -ForegroundColor Gray
} else {
    Write-Host "⚠️  Some images failed to build. Please check errors above." -ForegroundColor Yellow
    exit 1
}

