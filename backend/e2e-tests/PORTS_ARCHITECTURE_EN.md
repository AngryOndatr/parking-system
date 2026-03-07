# Microservice Port Architecture

## 📊 Port Reference Table

| Service | Container port | Host port | Note |
|---------|---------------|-----------|------|
| **Infrastructure** | | | |
| postgres | 5432 | 5432 | Standard PostgreSQL port |
| redis | 6379 | 6379 | Standard Redis port |
| eureka-server | 8761 | 8761 | Standard Eureka port |
| **Microservices (Spring Boot — all on 8080)** | | | |
| api-gateway | 8080 | 8086 | Standard Spring Boot |
| client-service | 8080 | 8081 | Standard Spring Boot |
| gate-control-service | 8080 | 8082 | Standard Spring Boot |
| billing-service | 8080 | 8083 | Standard Spring Boot |
| reporting-service | 8080 | 8084 | ✅ Unified |
| management-service | 8080 | 8085 | ✅ Unified |

**All microservices now use the standard port 8080 inside the container.**

## 🔍 Why are all services on port 8080?

### Standard Approach (unified)
**All** Spring Boot microservices use **port 8080 by default**:
- api-gateway
- client-service
- gate-control-service
- billing-service
- reporting-service ✅ (fixed)
- management-service ✅ (fixed)

In docker-compose they are mapped like this:
```yaml
api-gateway:
  ports: "8086:8080"  # Host 8086 → Container 8080

client-service:
  ports: "8081:8080"  # Host 8081 → Container 8080

gate-control-service:
  ports: "8082:8080"  # Host 8082 → Container 8080

billing-service:
  ports: "8083:8080"  # Host 8083 → Container 8080

reporting-service:
  ports: "8084:8080"  # Host 8084 → Container 8080 ✅

management-service:
  ports: "8085:8080"  # Host 8085 → Container 8080 ✅
```

### Benefits of unification:
- ✅ Simpler configuration
- ✅ Consistency
- ✅ Less confusion during debugging
- ✅ Easier to add new services
- ✅ Standard Spring Boot approach

## ⚠️ Important for inter-service communication

Inside the Docker network, services **always** communicate using the **internal container port**:

```yaml
# ✅ Correct — ALL services on port 8080
CLIENT_SERVICE_URL: http://client-service:8080
BILLING_SERVICE_URL: http://billing-service:8080
REPORTING_SERVICE_URL: http://reporting-service:8080
MANAGEMENT_SERVICE_URL: http://management-service:8080
GATE_CONTROL_SERVICE_URL: http://gate-control-service:8080

# ❌ Wrong (host ports are only used outside Docker)
CLIENT_SERVICE_URL: http://client-service:8081
BILLING_SERVICE_URL: http://billing-service:8083
REPORTING_SERVICE_URL: http://reporting-service:8084
MANAGEMENT_SERVICE_URL: http://management-service:8085
```

## 🎯 Recommendations

### For new services:
1. **Use the standard port 8080** inside the container
2. Map to a unique host port: `808X:8080`
3. This simplifies configuration and avoids confusion

### For existing non-standard ports:
1. Do not change without a good reason (breaking change)
2. Document the reason for the non-standard port
3. Make sure all places that use the URL know the correct port

## 📝 Verifying Ports

### Inside the container:
```bash
docker exec <container_name> netstat -tlnp | grep LISTEN
# or
docker exec <container_name> ss -tlnp | grep LISTEN
```

### From application.yml:
```bash
# reporting-service
grep "port:" backend/reporting-service/src/main/resources/application.yml

# management-service
grep "SERVER_PORT" backend/management-service/src/main/resources/application.yml
```

### Health check uses the internal port:
```yaml
healthcheck:
  test: ["CMD-SHELL", "wget --spider --quiet http://localhost:8080/actuator/health"]
  #                                                           ^^^^ internal container port (all services on 8080)
```

## 🔧 Changing the port

### Option 1: Via environment variable (recommended)
```yaml
# application.yml
server:
  port: ${SERVER_PORT:8080}

# docker-compose.yml
environment:
  SERVER_PORT: 8084
```

### Option 2: Hard-coded value in application.yml
```yaml
# application.yml
server:
  port: 8084
```

### Option 3: Spring profiles
```yaml
# application.yml
---
spring:
  config:
    activate:
      on-profile: docker
server:
  port: 8084
```

## 📚 Related Documents
- `docker-compose-e2e.yml` — Port configuration for E2E tests
- `application.yml` of each service — Port definitions
