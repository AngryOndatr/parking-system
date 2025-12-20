#!/bin/bash

# Parking System Observability Stack Startup Script

echo "🚀 Starting Parking System with Observability Stack..."

# Create necessary directories
mkdir -p ./observability/grafana/{dashboards,provisioning}

# Stop any running containers
echo "🛑 Stopping existing containers..."
docker-compose -f docker-compose-observability.yml down

# Clean up volumes if requested
if [ "$1" == "--clean" ]; then
    echo "🧹 Cleaning up volumes..."
    docker volume prune -f
fi

# Start the observability stack
echo "📊 Starting observability services..."
docker-compose -f docker-compose-observability.yml up -d prometheus grafana jaeger otel-collector

# Wait for services to be healthy
echo "⏳ Waiting for services to start..."
sleep 10

# Start database and cache
echo "🗄️ Starting database and cache..."
docker-compose -f docker-compose-observability.yml up -d postgres-db redis-cache

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 15

# Build and start API Gateway
echo "🏗️ Building API Gateway..."
cd ../backend/api-gateway
mvn clean package -DskipTests
cd ../../devops

echo "🚪 Starting API Gateway..."
docker-compose -f docker-compose-observability.yml up -d api-gateway

# Show status
echo "📋 Container Status:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "🎉 Observability Stack is ready!"
echo ""
echo "📊 Access URLs:"
echo "   • Grafana Dashboard: http://localhost:3000 (admin/admin123)"
echo "   • Prometheus: http://localhost:9090"
echo "   • Jaeger Tracing: http://localhost:16686"
echo "   • API Gateway: http://localhost:8080"
echo "   • Health Check: http://localhost:8080/actuator/health"
echo "   • Metrics: http://localhost:8080/actuator/prometheus"
echo ""
echo "📈 Default Grafana Dashboard will be available after first login"
echo "🔍 To view traces, make some API calls to generate traffic"
echo ""
echo "💡 Useful commands:"
echo "   • View logs: docker-compose -f docker-compose-observability.yml logs -f [service-name]"
echo "   • Stop all: docker-compose -f docker-compose-observability.yml down"
echo "   • Restart API: docker-compose -f docker-compose-observability.yml restart api-gateway"