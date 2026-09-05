# Production Flyway Configuration - Quick Reference

## 🔒 Critical Safety Settings

```yaml
spring:
  flyway:
    clean-disabled: true              # ⚠️ CRITICAL: Prevents database wipe
    baseline-on-migrate: false        # ⚠️ Don't auto-baseline
    out-of-order: false               # ⚠️ Enforce sequential order
    validate-on-migrate: true         # ✅ Verify checksums
    ignore-missing-migrations: false  # ✅ Fail on missing
    ignore-pending-migrations: false  # ✅ Fail on pending
```

## 🎯 What Each Setting Does

| Setting | Production Value | Why |
|---------|-----------------|-----|
| `clean-disabled` | `true` | **CRITICAL** - Prevents `flyway clean` which drops all DB objects |
| `baseline-on-migrate` | `false` | Forces explicit baseline creation, prevents accidents |
| `out-of-order` | `false` | Requires migrations in order (V1→V2→V3), prevents confusion |
| `validate-on-migrate` | `true` | Verifies checksums, detects modified migrations |
| `ignore-missing-migrations` | `false` | Fails if migration files are missing |
| `ignore-pending-migrations` | `false` | Fails if migrations pending (forces review) |

## 🚀 Quick Start

### 1. Set Environment Variables

```bash
# REQUIRED
export SPRING_PROFILES_ACTIVE=production
export SPRING_DATASOURCE_PASSWORD=<SECURE_PASSWORD>
export JWT_SECRET=<64_CHAR_SECRET>
export SPRING_REDIS_PASSWORD=<REDIS_PASSWORD>

# OPTIONAL (have defaults)
export DB_POOL_SIZE=20
export JWT_ACCESS_TOKEN_EXPIRATION=1800
export RATE_LIMIT_MINUTE=30
export BRUTE_FORCE_THRESHOLD=10
export BRUTE_FORCE_LOCKOUT_MINUTES=30
```

### 2. Verify Configuration

```bash
# Check which profile is active
docker exec api-gateway env | grep SPRING_PROFILES_ACTIVE

# Verify Flyway settings (from logs)
docker logs api-gateway | grep -i flyway
```

### 3. Deploy

```bash
# Follow the complete process
# See: PRODUCTION_MIGRATION_PROCESS.md
```

## ⚠️ Common Mistakes to Avoid

### ❌ DON'T DO THIS

```yaml
# DANGEROUS for production:
spring:
  flyway:
    clean-disabled: false          # ❌ Allows database wipe!
    baseline-on-migrate: true      # ❌ Auto-baselines
    out-of-order: true             # ❌ Allows chaos
    validate-on-migrate: false     # ❌ No checksum verification
    
  jpa:
    hibernate:
      ddl-auto: update             # ❌ Auto-schema changes!
```

### ✅ DO THIS

```yaml
# SAFE for production:
spring:
  flyway:
    clean-disabled: true           # ✅ Protected
    baseline-on-migrate: false     # ✅ Explicit
    out-of-order: false            # ✅ Ordered
    validate-on-migrate: true      # ✅ Verified
    
  jpa:
    hibernate:
      ddl-auto: validate           # ✅ Validation only
```

## 📋 Pre-Deployment Checklist

- [ ] `application-production.yml` configured
- [ ] All environment variables set
- [ ] Database backup taken
- [ ] Migrations tested on staging
- [ ] Rollback plan documented
- [ ] Team notified
- [ ] Monitoring active

## 🔗 Related Documents

- **[Complete Production Config](../backend/api-gateway/src/main/resources/application-production.yml)** - Full configuration file
- **[Production Migration Process](PRODUCTION_MIGRATION_PROCESS.md)** - Step-by-step deployment guide
- **[Database README](../database/README.md)** - General migration guide
- **[Deployment Guide](DEPLOYMENT_GUIDE.md)** - Overall deployment guide

## 🆘 Emergency

If something goes wrong:

```bash
# 1. Stop immediately
docker-compose stop api-gateway

# 2. Check logs
docker logs api-gateway --tail 200

# 3. Check failed migrations
docker exec parking_db psql -U postgres -d parking_db \
  -c "SELECT * FROM flyway_schema_history WHERE success = false;"

# 4. Follow rollback procedure
# See: PRODUCTION_MIGRATION_PROCESS.md → Rollback Section
```

## 📞 Support

- **Documentation:** [PRODUCTION_MIGRATION_PROCESS.md](PRODUCTION_MIGRATION_PROCESS.md)
- **Troubleshooting:** [Database README](../database/README.md#troubleshooting)
