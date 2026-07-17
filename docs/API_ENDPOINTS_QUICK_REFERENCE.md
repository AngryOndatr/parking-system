# API Endpoints Quick Reference

**Last Updated:** 2026-07-17

## Runtime ports (project root `docker-compose.yml`)

| Service | Internal port | External port |
|---------|---------------|---------------|
| api-gateway | 8080 | 8086 |
| client-service | 8081 | 8081 |
| gate-control-service | 8080 | 8082 |
| billing-service | 8080 | 8083 |
| management-service | 8083 | 8084 |
| reporting-service | 8080 | 8087 |

> External client calls should go through API Gateway: `http://localhost:8086/api/...`

## Endpoint summary (all implemented)

### Client Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/clients | Get all clients |
| POST | /api/clients | Create client |
| GET | /api/clients/{id} | Get client by ID |
| GET | /api/clients/search?phone={phone} | Search by phone |
| PUT | /api/clients/{id} | Update client |
| DELETE | /api/clients/{id} | Delete client |
| POST | /api/clients/{clientId}/vehicles | Add vehicle to client |
| GET | /api/clients/{clientId}/vehicles | Get client vehicles |
| GET | /api/vehicles | Get all vehicles |
| GET | /api/vehicles/{id} | Get vehicle by ID |
| PUT | /api/vehicles/{id} | Update vehicle |
| DELETE | /api/vehicles/{id} | Delete vehicle |
| GET | /api/v1/clients/subscriptions/check?licensePlate={plate} | Subscription check (service-to-service) |

### Gate Control Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/gate/entry | Process vehicle entry |
| POST | /api/gate/exit | Process vehicle exit |
| POST | /api/gate/control | Manual gate control |

### Billing Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/billing/calculate | Calculate parking fee |
| POST | /api/billing/pay | Process payment |
| GET | /api/billing/status?parkingEventId={id} | Get payment status |
| GET | /api/billing/status-by-ticket?ticketCode={code} | Get payment status by ticket |
| POST | /api/billing/pay-test | Test payment endpoint |

### Management Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/management/spots/available | Get all available spaces |
| GET | /api/management/spots/available/lot/{lotId} | Get available spaces by lot |
| GET | /api/management/spots | Get all spaces |
| GET | /api/management/spots/available/count | Count available spaces |
| GET | /api/management/spots/search?type=X&status=Y | Search spaces |

### Reporting Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/reporting/log | Create log entry |
| GET | /api/reporting/logs | Get logs (supports filters) |

## OpenAPI specification locations

| Service | OpenAPI spec path |
|---------|-------------------|
| Client Service | `backend/client-service/src/main/resources/openapi.yaml` |
| Billing Service | `backend/billing-service/src/main/resources/openapi.yaml` |
| Management Service | `backend/management-service/src/main/resources/openapi.yaml` |
| Reporting Service | `backend/reporting-service/src/main/resources/openapi.yaml` |
| Gate Control Service | `backend/gate-control-service/src/main/resources/openapi.yaml` |
