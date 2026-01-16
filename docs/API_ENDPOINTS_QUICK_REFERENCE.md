# API Endpoints Quick Reference

**Last Updated:** 2026-01-16

## All Microservices Endpoints Summary

### Client Service (Port 8081) ⭐ IMPLEMENTED

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | /api/clients | Get all clients | 200 |
| POST | /api/clients | Create client | 201 |
| GET | /api/clients/{id} | Get client by ID | 200, 404 |
| GET | /api/clients/search?phone={phone} | Search by phone | 200, 404 |
| POST | /api/clients/{clientId}/vehicles | Add vehicle to client | 201, 404, 409 |
| GET | /api/vehicles | Get all vehicles | 200 |
| GET | /api/vehicles/{id} | Get vehicle by ID | 200, 404 |
| DELETE | /api/vehicles/{id} | Delete vehicle | 204, 404 |

### Billing Service (Port 8082) 📋 DOCUMENTED

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | /api/v1/billing/calculate | Calculate parking fee | 200, 400, 404, 500 |
| POST | /api/v1/billing/pay | Process payment | 201, 400, 404, 409, 500 |
| GET | /api/v1/billing/status?parkingEventId={id} | Get payment status | 200, 400, 404, 500 |

### Management Service (Port 8083) ⭐ IMPLEMENTED

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | /api/management/spots/available | Get all available spaces | 200, 500 |
| GET | /api/management/spots/available/lot/{lotId} | Get available by lot | 200, 404, 500 |
| GET | /api/management/spots | Get all spaces | 200, 500 |
| GET | /api/management/spots/available/count | Count available spaces | 200, 500 |
| GET | /api/management/spots/search?type=X&status=Y | Search spaces | 200, 400, 500 |

### Reporting Service (Port 8084) ⭐ IMPLEMENTED

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | /api/reporting/log | Create log entry | 201, 400, 500 |
| GET | /api/reporting/logs?level=X&service=Y&userId=Z&fromDate=A&toDate=B&limit=N | Get logs (filtered) | 200, 400, 500 |

### Gate Control Service (Port 8085) 📋 DOCUMENTED

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | /api/v1/gate/entry | Process vehicle entry | 201, 400, 403, 409, 500 |
| POST | /api/v1/gate/exit | Process vehicle exit | 200, 400, 404, 409, 500 |
| POST | /api/v1/gate/control | Manual gate control | 200, 400, 403, 404, 500 |

---

## OpenAPI Specifications Locations

| Service | OpenAPI Spec Path |
|---------|-------------------|
| Client Service | `backend/client-service/src/main/resources/openapi.yaml` |
| Billing Service | `backend/billing-service/src/main/resources/openapi.yaml` |
| Management Service | `backend/management-service/src/main/resources/openapi.yaml` |
| Reporting Service | `backend/reporting-service/src/main/resources/openapi.yaml` |
| Gate Control Service | `backend/gate-control-service/src/main/resources/openapi.yaml` |

---

## Status Legend

- ⭐ **IMPLEMENTED** - Service is running, endpoints tested and working
- 📋 **DOCUMENTED** - OpenAPI spec created, awaiting implementation
- ❌ **NOT STARTED** - No spec or implementation

---

## Full Documentation

See [api-contracts.md](./api-contracts.md) for complete endpoint documentation with:
- Request/response examples
- Error scenarios
- Testing examples
- Integration notes

