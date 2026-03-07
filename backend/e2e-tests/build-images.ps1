# Build all Docker images for E2E tests
# Run this from project root: C:\Users\user\Projects\parking-system

Write-Host "Building all Docker images for E2E tests..." -ForegroundColor Green

$services = @(
    @{Name="eureka-server"; Path="./backend/eureka-server"},
    @{Name="api-gateway"; Path="./backend/api-gateway"},
    @{Name="client-service"; Path="./backend/client-service"},
    @{Name="gate-control-service"; Path="./backend/gate-control-service"},
    @{Name="billing-service"; Path="./backend/billing-service"},
    @{Name="reporting-service"; Path="./backend/reporting-service"},
    @{Name="management-service"; Path="./backend/management-service"}
)

foreach ($service in $services) {
    Write-Host "`nBuilding $($service.Name)..." -ForegroundColor Cyan
    docker build -t "$($service.Name):latest" $service.Path
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to build $($service.Name)" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ $($service.Name) built successfully" -ForegroundColor Green
}

Write-Host "`nAll images built successfully!" -ForegroundColor Green
Write-Host "`nYou can now run E2E tests with:" -ForegroundColor Yellow
Write-Host "  cd backend/e2e-tests" -ForegroundColor Yellow
Write-Host "  mvn clean test" -ForegroundColor Yellow
