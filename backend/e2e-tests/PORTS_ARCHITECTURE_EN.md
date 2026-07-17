# E2E Port Architecture

This file documents ports used by `backend/e2e-tests/docker-compose-e2e.yml`.

## Port table (E2E stack)

| Service | Container port | Host port |
|---------|----------------|-----------|
| postgres | 5432 | not published (`expose` only) |
| redis | 6379 | not published (`expose` only) |
| eureka-server | 8761 | not published (`expose` only) |
| api-gateway | 8080 | not published (`expose` only) |
| client-service | 8081 | not published (`expose` only) |
| gate-control-service | 8080 | not published (`expose` only) |
| billing-service | 8080 | not published (`expose` only) |
| reporting-service | 8080 | not published (`expose` only) |
| management-service | 8080 | not published (`expose` only) |

## Inter-service URLs in E2E

- `CLIENT_SERVICE_URL=http://client-service:8081`
- `BILLING_SERVICE_URL=http://billing-service:8080`
- `REPORTING_SERVICE_URL=http://reporting-service:8080` (default/service env where used)

## Notes

- E2E containers communicate only via internal Docker network addresses.
- API calls in E2E tests should target service/container names, not `localhost` host ports.
