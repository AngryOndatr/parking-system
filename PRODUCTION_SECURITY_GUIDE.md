# Production Deployment Security Checklist

## 🚨 CRITICAL: Required Environment Variables

### Database Security
```bash
# PostgreSQL with strong credentials
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db-host:5432/parking_production
SPRING_DATASOURCE_USERNAME=parking_prod_user
SPRING_DATASOURCE_PASSWORD=<STRONG_DB_PASSWORD_32_CHARS+>

# Use "validate" for production - NEVER create-drop!
HIBERNATE_DDL_AUTO=validate
```

### JWT Security (CRITICAL!)
```bash
# JWT Secret MUST be 64+ characters, cryptographically strong
JWT_SECRET=<GENERATE_STRONG_64_CHAR_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION=1800   # 30 minutes
JWT_REFRESH_TOKEN_EXPIRATION=43200  # 12 hours
JWT_ISSUER=parking-system-prod
```

### Redis Security
```bash
# Redis with authentication
SPRING_REDIS_HOST=prod-redis-host
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=<STRONG_REDIS_PASSWORD>
SPRING_REDIS_TIMEOUT=2000
```

### Profile & Logging
```bash
# Use production profile
SPRING_PROFILES_ACTIVE=prod-security

# Production logging levels
LOGGING_LEVEL=INFO
SECURITY_LOGGING_LEVEL=WARN
```

### Rate Limiting & Security
```bash
RATE_LIMITING_ENABLED=true
RATE_LIMIT_MINUTE=30              # More restrictive for prod
RATE_LIMIT_HOUR=500               # More restrictive for prod  
BRUTE_FORCE_THRESHOLD=5           # More restrictive for prod
```

## 🔒 Pre-Production Security Actions

### 1. Generate Strong Secrets
```bash
# Generate JWT secret (64+ chars)
openssl rand -hex 32

# Generate Redis password
openssl rand -base64 32

# Generate DB password
openssl rand -base64 24
```

### 2. Database Migration (CRITICAL!)
```sql
-- Run database migrations BEFORE deployment
-- Create production tables with proper indexes
-- Set up backup strategy
-- Configure connection pooling
```

### 3. TLS/SSL Configuration
- Configure HTTPS with valid certificates
- Enable HSTS headers (already in code)
- Configure secure cookie settings
- Set up reverse proxy (nginx/Apache) with SSL termination

### 4. Infrastructure Security
- Network isolation (private subnets)
- Firewall rules (only necessary ports)
- Load balancer configuration
- DDoS protection
- VPC/security groups

### 5. Monitoring & Alerting
- Set up log aggregation (ELK/Splunk)
- Configure security event alerts
- Monitor authentication failures
- Track rate limiting violations
- Database connection monitoring

## ✅ Production Readiness Checklist

- [ ] All environment variables set with strong values
- [ ] Database schema created with proper migrations
- [ ] TLS/SSL certificates configured
- [ ] Redis with authentication enabled
- [ ] Load balancer configured
- [ ] Monitoring and alerting set up
- [ ] Backup strategy implemented
- [ ] Security testing completed
- [ ] Penetration testing performed
- [ ] Code review completed
- [ ] Documentation updated

## 🚨 Production No-Go Conditions

❌ **DO NOT DEPLOY IF:**
- Using default/weak passwords
- JWT secret is in code or < 64 characters
- Hibernate DDL is not "validate"
- Debug logging is enabled
- No TLS/SSL configured
- No monitoring set up
- No backup strategy

## 📋 Quick Production Start

```bash
# 1. Set all required environment variables
export JWT_SECRET="$(openssl rand -hex 32)"
export SPRING_REDIS_PASSWORD="$(openssl rand -base64 32)"
# ... set all other variables

# 2. Run database migrations
./mvnw flyway:migrate

# 3. Start with production profile
java -jar api-gateway.jar --spring.profiles.active=prod-security
```

## 🔧 Post-Deployment Verification

```bash
# Check health endpoint
curl -k https://your-domain/actuator/health

# Verify security headers
curl -I https://your-domain/api/auth/login

# Test rate limiting
# Should get 429 after configured limits

# Verify JWT expiration
# Tokens should expire according to configuration
```