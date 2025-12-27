# Parking System Observability Stack Startup Script for Windows

Write-Host "🚀 Starting Parking System with Observability Stack..." -ForegroundColor Green

# Create necessary directories
if (!(Test-Path ".\observability\grafana\dashboards")) {
    New-Item -ItemType Directory -Path ".\observability\grafana\dashboards" -Force | Out-Null
}
if (!(Test-Path ".\observability\grafana\provisioning")) {
    New-Item -ItemType Directory -Path ".\observability\grafana\provisioning" -Force | Out-Null
}

# Stop any running containers
Write-Host "🛑 Stopping existing containers..." -ForegroundColor Yellow
docker-compose -f docker-compose-observability.yml down

# Clean up volumes if requested
if ($args[0] -eq "--clean") {
    Write-Host "🧹 Cleaning up volumes..." -ForegroundColor Yellow
    docker volume prune -f
}

# Start the observability stack
Write-Host "📊 Starting observability services..." -ForegroundColor Cyan
docker-compose -f docker-compose-observability.yml up -d prometheus grafana jaeger otel-collector

# Wait for services to be healthy
Write-Host "⏳ Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Start database and cache
Write-Host "🗄️ Starting database and cache..." -ForegroundColor Cyan
docker-compose -f docker-compose-observability.yml up -d postgres-db redis-cache

# Wait for database to be ready
Write-Host "⏳ Waiting for database to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Build and start API Gateway
Write-Host "🏗️ Building API Gateway..." -ForegroundColor Cyan
Push-Location ..\backend\api-gateway
mvn clean package -DskipTests
Pop-Location

Write-Host "🚪 Starting API Gateway..." -ForegroundColor Cyan
docker-compose -f docker-compose-observability.yml up -d api-gateway

# Show status
Write-Host "📋 Container Status:" -ForegroundColor Green
docker ps --format "table {{.Names}}`t{{.Status}}`t{{.Ports}}"

Write-Host ""
Write-Host "🎉 Observability Stack is ready!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Access URLs:" -ForegroundColor Cyan
Write-Host "   • Grafana Dashboard: http://localhost:3000 (admin/admin123)" -ForegroundColor White
Write-Host "   • Prometheus: http://localhost:9090" -ForegroundColor White
Write-Host "   • Jaeger Tracing: http://localhost:16686" -ForegroundColor White
Write-Host "   • API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "   • Health Check: http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "   • Metrics: http://localhost:8080/actuator/prometheus" -ForegroundColor White
Write-Host ""
Write-Host "📈 Default Grafana Dashboard will be available after first login" -ForegroundColor Yellow
Write-Host "🔍 To view traces, make some API calls to generate traffic" -ForegroundColor Yellow
Write-Host ""
Write-Host "💡 Useful commands:" -ForegroundColor Cyan
Write-Host "   • View logs: docker-compose -f docker-compose-observability.yml logs -f [service-name]" -ForegroundColor White
Write-Host "   • Stop all: docker-compose -f docker-compose-observability.yml down" -ForegroundColor White
Write-Host "   • Restart API: docker-compose -f docker-compose-observability.yml restart api-gateway" -ForegroundColor White

Write-Host ""
Write-Host "✨ Test the setup with:" -ForegroundColor Magenta
Write-Host "curl http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "curl http://localhost:8080/actuator/prometheus" -ForegroundColor White