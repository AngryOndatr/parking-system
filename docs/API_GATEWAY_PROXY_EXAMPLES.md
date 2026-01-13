# API Gateway Proxy Examples

This document provides curl and PowerShell examples for testing API Gateway proxy endpoints.

---

## Table of Contents

- [Authentication](#authentication)
- [Management Service Endpoints](#management-service-endpoints)
- [Reporting Service Endpoints](#reporting-service-endpoints)
- [Client Service Endpoints](#client-service-endpoints)

---

## Authentication

All endpoints (except `/api/auth/*`) require JWT authentication.

### Login

**PowerShell:**
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
    -Method POST `
    -Body '{"username":"admin","password":"parking123"}' `
    -ContentType "application/json"

$token = $response.accessToken
```

**curl:**
```bash
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}'

# Extract token (use jq if available)
TOKEN=$(curl -s -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}' | jq -r '.accessToken')
```

---

## Management Service Endpoints

### Get Available Parking Spots

**PowerShell:**
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Invoke-RestMethod -Uri "http://localhost:8086/api/management/spots/available" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET http://localhost:8086/api/management/spots/available \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
[
  {
    "id": 1,
    "spaceNumber": "A-001",
    "floorLevel": 0,
    "section": "A",
    "spaceType": "STANDARD",
    "status": "AVAILABLE",
    "hasCharger": false
  }
]
```

---

### Get Available Spots Count

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/management/spots/available/count" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET http://localhost:8086/api/management/spots/available/count \
  -H "Authorization: Bearer $TOKEN"
```

**Response Example:**
```json
{
  "count": 15
}
```

---

### Get All Parking Spots

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/management/spots" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET http://localhost:8086/api/management/spots \
  -H "Authorization: Bearer $TOKEN"
```

---

### Search Spots by Type and Status

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/management/spots/search?type=STANDARD&status=AVAILABLE" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET "http://localhost:8086/api/management/spots/search?type=STANDARD&status=AVAILABLE" \
  -H "Authorization: Bearer $TOKEN"
```

---

### Get Available Spots by Parking Lot

**PowerShell:**
```powershell
$lotId = 1
Invoke-RestMethod -Uri "http://localhost:8086/api/management/spots/available/lot/$lotId" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET http://localhost:8086/api/management/spots/available/lot/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Reporting Service Endpoints

### Create Log Entry

**PowerShell:**
```powershell
$logBody = @{
    timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    level = "INFO"
    service = "test-script"
    message = "Test log entry"
    userId = 1
    meta = @{
        test = $true
        source = "api-test"
    }
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8086/api/reporting/log" `
    -Method POST `
    -Headers $headers `
    -Body $logBody
```

**curl:**
```bash
curl -X POST http://localhost:8086/api/reporting/log \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "timestamp": "2026-01-13T12:00:00Z",
    "level": "INFO",
    "service": "test-script",
    "message": "Test log entry",
    "userId": 1,
    "meta": {
      "test": true,
      "source": "api-test"
    }
  }'
```

**Response Example:**
```json
{
  "id": 42,
  "timestamp": "2026-01-13T12:00:00Z",
  "level": "INFO",
  "service": "test-script",
  "message": "Test log entry",
  "userId": 1
}
```

---

### Get All Logs

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/reporting/logs" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET http://localhost:8086/api/reporting/logs \
  -H "Authorization: Bearer $TOKEN"
```

---

### Get Logs with Filters

**Filter by Level:**

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/reporting/logs?level=ERROR" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET "http://localhost:8086/api/reporting/logs?level=ERROR" \
  -H "Authorization: Bearer $TOKEN"
```

---

**Filter by Service:**

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/reporting/logs?service=client-service" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET "http://localhost:8086/api/reporting/logs?service=client-service" \
  -H "Authorization: Bearer $TOKEN"
```

---

**Filter by User ID:**

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/reporting/logs?userId=1" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET "http://localhost:8086/api/reporting/logs?userId=1" \
  -H "Authorization: Bearer $TOKEN"
```

---

**Limit Results:**

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/reporting/logs?limit=10" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET "http://localhost:8086/api/reporting/logs?limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

**Combined Filters:**

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/reporting/logs?level=INFO&service=test-script&limit=5" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET "http://localhost:8086/api/reporting/logs?level=INFO&service=test-script&limit=5" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Client Service Endpoints

### Get All Clients

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/clients" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET http://localhost:8086/api/clients \
  -H "Authorization: Bearer $TOKEN"
```

---

### Create Client

**PowerShell:**
```powershell
$clientBody = @{
    fullName = "John Doe"
    email = "john.doe@example.com"
    phoneNumber = "+380501234567"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8086/api/clients" `
    -Method POST `
    -Headers $headers `
    -Body $clientBody
```

**curl:**
```bash
curl -X POST http://localhost:8086/api/clients \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+380501234567"
  }'
```

---

### Get All Vehicles

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/vehicles" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET http://localhost:8086/api/vehicles \
  -H "Authorization: Bearer $TOKEN"
```

---

### Get Client's Vehicles

**PowerShell:**
```powershell
$clientId = 1
Invoke-RestMethod -Uri "http://localhost:8086/api/clients/$clientId/vehicles" `
    -Method GET `
    -Headers $headers
```

**curl:**
```bash
curl -X GET http://localhost:8086/api/clients/1/vehicles \
  -H "Authorization: Bearer $TOKEN"
```

---

## Running Smoke Tests

Use the provided test scripts to verify all proxy endpoints:

**PowerShell (Windows):**
```powershell
cd devops
.\test-proxy.ps1
```

**Bash (Linux/Mac):**
```bash
cd devops
chmod +x test-proxy.sh
./test-proxy.sh
```

The scripts will:
1. Authenticate as admin
2. Test Management Service proxy endpoints
3. Test Reporting Service proxy endpoints
4. Test Client Service proxy endpoints
5. Display a summary of results

---

## Troubleshooting

### Common Issues

**401 Unauthorized:**
- Token expired or invalid
- Re-authenticate to get a new token

**404 Not Found:**
- Service not running
- Check Docker containers: `docker ps`
- Start services: `docker-compose up -d`

**500 Internal Server Error:**
- Backend service error
- Check service logs: `docker logs <service-name>`

**Connection Refused:**
- API Gateway not running
- Check: `docker ps | grep api-gateway`
- Start: `docker-compose up -d api-gateway`

---

## Service URLs

When running in Docker:

- **API Gateway:** http://localhost:8086
- **Client Service:** http://client-service:8081 (internal)
- **Management Service:** http://management-service:8083 (internal)
- **Reporting Service:** http://reporting-service:8084 (internal)

All external requests go through API Gateway on port 8086.

---

**Last Updated:** 2026-01-13  
**Related Issue:** #21

