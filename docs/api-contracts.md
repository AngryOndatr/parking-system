# API Contracts Documentation

**Version:** 2.0.0  
**Last Updated:** 2026-01-16  
**Coverage:** All 5 microservices  
**Status:** Phase 1 Complete ⭐ | Phase 2 Documented 📋

---

## Table of Contents

### Implemented Services (Phase 1) ⭐
- [Client Service API](#client-service-api) - Port 8081
- [Management Service API](#management-service-api) - Port 8083  
- [Reporting Service API](#reporting-service-api) - Port 8084

### Documented Services (Phase 2) 📋
- [Billing Service API](#billing-service-api) - Port 8082
- [Gate Control Service API](#gate-control-service-api) - Port 8083

### Reference
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Service Ports](#service-ports)

---

## Overview

This document provides comprehensive API documentation for all microservices in the Parking System.

### Design Principles

- **Contract-First:** OpenAPI 3.0.3 specifications define APIs before implementation
- **RESTful:** Standard HTTP methods and status codes
- **JSON:** All request/response bodies use JSON format
- **ISO 8601:** Timestamps in ISO 8601 format (UTC)
- **Validation:** Input validation with clear error messages
- **Idempotency:** POST operations generate consistent results

### Service Ports

| Service | Port | Status | Base URL (Docker) |
|---------|------|--------|-------------------|
| Client Service | 8081 | ⭐ IMPLEMENTED | http://client-service:8081 |
| Billing Service | 8082 | 📋 DOCUMENTED | http://billing-service:8082 |
| Management Service | 8083 | ⭐ IMPLEMENTED | http://management-service:8083 |
| Reporting Service | 8084 | ⭐ IMPLEMENTED | http://reporting-service:8084 |
| Gate Control Service | 8085 | 📋 DOCUMENTED | http://gate-control-service:8085 |

### Authentication

All endpoints require JWT authentication via API Gateway (port 8080/8086) except:
- Internal service-to-service calls
- Public endpoints (marked explicitly)

---

## Client Service API ⭐

**Status:** IMPLEMENTED (Phase 1)  
**Port:** 8081  
**Base URL:** http://localhost:8081 | http://client-service:8081  
**OpenAPI Spec:** `backend/client-service/src/main/resources/openapi.yaml`

### Description
Manages clients (subscription owners) and their vehicles. Central service for registering and identifying subscribers.

### Endpoints

#### Clients

##### GET /api/clients
Get list of all registered clients.

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "fullName": "John Doe",
    "phoneNumber": "+380501234567",
    "email": "john@example.com",
    "registeredAt": "2026-01-01T10:00:00Z"
  }
]
```

##### POST /api/clients
Register a new client.

**Request:**
```json
{
  "fullName": "John Doe",
  "phoneNumber": "+380501234567",
  "email": "john@example.com"
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "fullName": "John Doe",
  "phoneNumber": "+380501234567",
  "email": "john@example.com",
  "registeredAt": "2026-01-01T10:00:00Z"
}
```

**Errors:**
- 400 - Invalid phone/email format
- 409 - Phone/email already exists

##### GET /api/clients/{id}
Get client by ID.

**Response 200 OK:**
```json
{
  "id": 1,
  "fullName": "John Doe",
  "phoneNumber": "+380501234567",
  "email": "john@example.com",
  "registeredAt": "2026-01-01T10:00:00Z"
}
```

**Errors:**
- 404 - Client not found

##### GET /api/clients/search
Search clients by phone number.

**Query Parameters:**
- `phone` (required) - Phone number to search

**Example:** `GET /api/clients/search?phone=%2B380501234567`

**Response 200 OK:**
```json
{
  "id": 1,
  "fullName": "John Doe",
  "phoneNumber": "+380501234567",
  "email": "john@example.com",
  "registeredAt": "2026-01-01T10:00:00Z"
}
```

**Errors:**
- 404 - Client not found

#### Vehicles

##### POST /api/clients/{clientId}/vehicles
Add vehicle to client.

**Request:**
```json
{
  "licensePlate": "ABC-1234",
  "vehicleType": "CAR",
  "model": "Toyota Camry",
  "color": "Black"
}
```

**Response 201 Created:**
```json
{
  "id": 10,
  "clientId": 1,
  "licensePlate": "ABC-1234",
  "vehicleType": "CAR",
  "model": "Toyota Camry",
  "color": "Black",
  "registeredAt": "2026-01-01T10:00:00Z"
}
```

**Errors:**
- 400 - Invalid request
- 404 - Client not found
- 409 - License plate already exists

##### GET /api/vehicles
Get all vehicles.

**Response 200 OK:**
```json
[
  {
    "id": 10,
    "clientId": 1,
    "licensePlate": "ABC-1234",
    "vehicleType": "CAR",
    "model": "Toyota Camry",
    "color": "Black",
    "registeredAt": "2026-01-01T10:00:00Z"
  }
]
```

##### GET /api/vehicles/{id}
Get vehicle by ID.

##### DELETE /api/vehicles/{id}
Delete vehicle.

**Response:** 204 No Content

**Errors:**
- 404 - Vehicle not found

---

## Management Service API ⭐

**Status:** IMPLEMENTED (Phase 1)  
**Port:** 8083  
**Base URL:** http://localhost:8083 | http://management-service:8083  
**OpenAPI Spec:** `backend/management-service/src/main/resources/openapi.yaml`

### Description
Parking space management and monitoring. Real-time information about parking space availability.

### Endpoints

##### GET /api/management/spots/available
Get all available parking spaces.

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "lotId": 1,
    "spaceNumber": "A-001",
    "spaceType": "STANDARD",
    "status": "AVAILABLE",
    "level": 1
  }
]
```

##### GET /api/management/spots/available/lot/{lotId}
Get available spaces by parking lot.

**Path Parameters:**
- `lotId` (required) - Parking lot ID

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "lotId": 1,
    "spaceNumber": "A-001",
    "spaceType": "STANDARD",
    "status": "AVAILABLE",
    "level": 1
  }
]
```

**Errors:**
- 404 - Lot not found

##### GET /api/management/spots
Get all parking spaces (regardless of status).

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "lotId": 1,
    "spaceNumber": "A-001",
    "spaceType": "STANDARD",
    "status": "AVAILABLE",
    "level": 1
  },
  {
    "id": 2,
    "lotId": 1,
    "spaceNumber": "A-002",
    "spaceType": "DISABLED",
    "status": "OCCUPIED",
    "level": 1
  }
]
```

##### GET /api/management/spots/available/count
Get count of available parking spaces.

**Response 200 OK:**
```json
{
  "count": 42
}
```

##### GET /api/management/spots/search
Search parking spaces by type and status.

**Query Parameters:**
- `type` (optional) - Space type: STANDARD, VIP, DISABLED, ELECTRIC_CHARGING
- `status` (optional) - Status: AVAILABLE, OCCUPIED, RESERVED, OUT_OF_SERVICE

**Example:** `GET /api/management/spots/search?type=VIP&status=AVAILABLE`

**Response 200 OK:**
```json
[
  {
    "id": 15,
    "lotId": 1,
    "spaceNumber": "V-001",
    "spaceType": "VIP",
    "status": "AVAILABLE",
    "level": 2
  }
]
```

---

## Reporting Service API ⭐

**Status:** IMPLEMENTED (Phase 1)  
**Port:** 8084  
**Base URL:** http://localhost:8084 | http://reporting-service:8084  
**OpenAPI Spec:** `backend/reporting-service/src/main/resources/openapi.yaml`

### Description
System logging and reporting. Centralized log storage and retrieval.

### Endpoints

##### POST /api/reporting/log
Create system log entry.

**Request:**
```json
{
  "timestamp": "2026-01-16T12:00:00Z",
  "level": "INFO",
  "service": "client-service",
  "userId": 1,
  "message": "Client registered successfully",
  "meta": {
    "clientId": 123,
    "action": "CREATE"
  }
}
```

**Response 201 Created:**
```json
{
  "id": 9876,
  "timestamp": "2026-01-16T12:00:00Z",
  "level": "INFO",
  "service": "client-service",
  "userId": 1,
  "message": "Client registered successfully"
}
```

**Errors:**
- 400 - Invalid request

##### GET /api/reporting/logs
Get log entries with optional filters.

**Query Parameters:**
- `level` (optional) - Log level: INFO, WARN, ERROR, DEBUG, TRACE
- `service` (optional) - Service name
- `userId` (optional) - User ID
- `fromDate` (optional) - Start date (ISO 8601)
- `toDate` (optional) - End date (ISO 8601)
- `limit` (optional) - Max results (default: 100)

**Example:** `GET /api/reporting/logs?level=ERROR&service=client-service&limit=50`

**Response 200 OK:**
```json
[
  {
    "id": 9876,
    "timestamp": "2026-01-16T12:00:00Z",
    "level": "ERROR",
    "service": "client-service",
    "userId": 1,
    "message": "Database connection failed",
    "meta": {
      "error": "Connection timeout"
    }
  }
]
```

---

## Billing Service API 📋

**Status:** DOCUMENTED (Phase 2)  
**Port:** 8082  
**Base URL:** http://localhost:8082 | http://billing-service:8082  
**OpenAPI Spec:** `backend/billing-service/src/main/resources/openapi.yaml`

### Description
Parking fee calculation and payment processing. Integrates with parking events, tariffs, and payment systems.

### Endpoints


### POST /api/v1/billing/calculate

Calculate parking fee for a parking event.

**Request Body:**

```json
{
  "parkingEventId": 12345,
  "entryTime": "2026-01-16T10:00:00Z",
  "exitTime": "2026-01-16T12:00:00Z",
  "tariffType": "ONE_TIME",
  "isSubscriber": false,
  "subscriptionId": null
}
```

**Request Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| parkingEventId | integer(int64) | ✅ | ID of the parking event |
| entryTime | string(date-time) | ✅ | Entry timestamp (ISO 8601) |
| exitTime | string(date-time) | ✅ | Exit timestamp (ISO 8601) |
| tariffType | enum | ✅ | Tariff type: `ONE_TIME`, `DAILY`, `NIGHT`, `VIP` |
| isSubscriber | boolean | ✅ | Whether vehicle has active subscription |
| subscriptionId | integer(int64) | ❌ | Subscription ID (if isSubscriber is true) |

**Success Response (200 OK):**

```json
{
  "parkingEventId": 12345,
  "durationMinutes": 120,
  "baseFee": 100.00,
  "discount": 0.00,
  "totalFee": 100.00,
  "tariffApplied": "ONE_TIME",
  "calculatedAt": "2026-01-16T12:00:00Z"
}
```

**Error Responses:**

- **400 Bad Request:** Invalid request parameters
- **404 Not Found:** Parking event or tariff not found
- **500 Internal Server Error:** Server error

**Example with Subscriber Discount:**

Request:
```json
{
  "parkingEventId": 12346,
  "entryTime": "2026-01-16T08:00:00Z",
  "exitTime": "2026-01-16T18:00:00Z",
  "tariffType": "DAILY",
  "isSubscriber": true,
  "subscriptionId": 789
}
```

Response:
```json
{
  "parkingEventId": 12346,
  "durationMinutes": 600,
  "baseFee": 500.00,
  "discount": 100.00,
  "totalFee": 400.00,
  "tariffApplied": "DAILY",
  "calculatedAt": "2026-01-16T18:00:00Z"
}
```

---

### POST /api/v1/billing/pay

Process payment for a parking event.

**Request Body:**

```json
{
  "parkingEventId": 12345,
  "amount": 100.00,
  "paymentMethod": "CARD",
  "transactionId": "TXN-20260116-001",
  "operatorId": null
}
```

**Request Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| parkingEventId | integer(int64) | ✅ | ID of the parking event |
| amount | number(double) | ✅ | Payment amount (min: 0.01) |
| paymentMethod | enum | ✅ | Payment method: `CARD`, `CASH`, `MOBILE_PAY` |
| transactionId | string | ❌ | External payment system transaction ID (for CARD/MOBILE_PAY) |
| operatorId | integer(int64) | ❌ | Operator ID (for CASH/manual payments) |

**Success Response (201 Created):**

```json
{
  "paymentId": 9876,
  "parkingEventId": 12345,
  "amount": 100.00,
  "status": "COMPLETED",
  "paymentMethod": "CARD",
  "transactionId": "TXN-20260116-001",
  "paymentTime": "2026-01-16T12:05:00Z"
}
```

**Error Responses:**

- **400 Bad Request:** Invalid payment request (e.g., amount <= 0)
- **404 Not Found:** Parking event not found
- **409 Conflict:** Payment already exists for this parking event
- **500 Internal Server Error:** Server error

**Example - Cash Payment:**

Request:
```json
{
  "parkingEventId": 12346,
  "amount": 400.00,
  "paymentMethod": "CASH",
  "transactionId": null,
  "operatorId": 5
}
```

Response:
```json
{
  "paymentId": 9877,
  "parkingEventId": 12346,
  "amount": 400.00,
  "status": "COMPLETED",
  "paymentMethod": "CASH",
  "transactionId": null,
  "paymentTime": "2026-01-16T12:10:00Z"
}
```

---

### GET /api/v1/billing/status

Get payment status for a parking event.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| parkingEventId | integer(int64) | ✅ | ID of the parking event |

**Example Request:**

```
GET /api/v1/billing/status?parkingEventId=12345
```

**Success Response (200 OK) - Paid:**

```json
{
  "parkingEventId": 12345,
  "isPaid": true,
  "payments": [
    {
      "paymentId": 9876,
      "amount": 100.00,
      "status": "COMPLETED",
      "paymentMethod": "CARD",
      "transactionId": "TXN-20260116-001",
      "paymentTime": "2026-01-16T12:05:00Z"
    }
  ]
}
```

**Success Response (200 OK) - Unpaid:**

```json
{
  "parkingEventId": 12346,
  "isPaid": false,
  "payments": []
}
```

**Success Response (200 OK) - Multiple Attempts:**

```json
{
  "parkingEventId": 12347,
  "isPaid": true,
  "payments": [
    {
      "paymentId": 9877,
      "amount": 150.00,
      "status": "FAILED",
      "paymentMethod": "CARD",
      "transactionId": "TXN-20260116-002",
      "paymentTime": "2026-01-16T13:00:00Z"
    },
    {
      "paymentId": 9878,
      "amount": 150.00,
      "status": "COMPLETED",
      "paymentMethod": "CASH",
      "transactionId": null,
      "paymentTime": "2026-01-16T13:10:00Z"
    }
  ]
}
```

**Error Responses:**

- **400 Bad Request:** Missing or invalid parkingEventId
- **404 Not Found:** Parking event not found
- **500 Internal Server Error:** Server error

---


