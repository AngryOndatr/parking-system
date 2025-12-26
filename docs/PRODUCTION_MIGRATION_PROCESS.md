# Production Migration Deployment Process

## 🎯 Overview

This document describes the safe process for applying Flyway database migrations in production environment.

---

## ⚠️ Prerequisites

Before starting production migration:

- [ ] **Backup completed** - Full database backup taken
- [ ] **Migrations tested** - All migrations verified on staging environment
- [ ] **Rollback plan** - Documented procedure to revert changes
- [ ] **Downtime window** - Scheduled maintenance window (if needed)
- [ ] **Team notification** - Development and operations teams informed
- [ ] **Monitoring ready** - Logging and alerting systems active

---

## 🔒 Safety Features in Production

### Flyway Configuration

```yaml
spring:
  flyway:
    # Safety Features
    baseline-on-migrate: false        # Don't create baseline automatically
    validate-on-migrate: true         # Verify checksums
    out-of-order: false               # Enforce sequential order
    clean-disabled: true              # Prevent accidental wipe
    ignore-missing-migrations: false  # Fail on missing migrations
    ignore-pending-migrations: false  # Fail on pending migrations
```

### What These Settings Do:

- **`baseline-on-migrate: false`** - Prevents Flyway from automatically creating a baseline on existing schemas. This forces explicit baseline creation.
- **`validate-on-migrate: true`** - Validates that applied migrations haven't been modified (checksum verification).
- **`clean-disabled: true`** - **CRITICAL**: Prevents `flyway clean` command, which would drop all database objects.
- **`out-of-order: false`** - Requires migrations to be applied in order (V1, V2, V3...).

---

## 📋 Production Deployment Checklist

### Phase 1: Pre-Deployment

1. **Review Migration Scripts**
   ```bash
   # List all pending migrations
   ls backend/api-gateway/src/main/resources/db/migration/
   ```

2. **Verify on Staging**
   ```bash
   # Apply migrations on staging first
   export SPRING_PROFILES_ACTIVE=staging
   docker-compose up api-gateway
   
   # Verify results
   docker exec parking_db psql -U postgres -d parking_db \
     -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank;"
   ```

3. **Create Database Backup**
   ```bash
   # Backup production database
   docker exec parking_db pg_dump -U postgres -Fc parking_db \
     > backup_prod_$(date +%Y%m%d_%H%M%S).dump
   
   # Verify backup
   ls -lh backup_prod_*.dump
   ```

4. **Document Current State**
   ```sql
   -- Record current migration state
   SELECT installed_rank, version, description, installed_on, success 
   FROM flyway_schema_history 
   ORDER BY installed_rank;
   
   -- Record table count
   SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public';
   
   -- Record row counts
   SELECT table_name, 
          (xpath('/row/cnt/text()', 
                 xml_count))[1]::text::int as row_count
   FROM (
     SELECT table_name, 
            query_to_xml(format('SELECT COUNT(*) AS cnt FROM %I.%I', 
                               table_schema, table_name), false, true, '') as xml_count
     FROM information_schema.tables
     WHERE table_schema = 'public'
   ) t;
   ```

### Phase 2: Deployment

5. **Set Production Environment**
   ```bash
   # Set environment variables
   export SPRING_PROFILES_ACTIVE=production
   export SPRING_DATASOURCE_PASSWORD=<SECURE_PASSWORD>
   export JWT_SECRET=<64_CHAR_SECRET>
   export SPRING_REDIS_PASSWORD=<REDIS_PASSWORD>
   ```

6. **Enable Maintenance Mode** (if applicable)
   ```bash
   # Put application in maintenance mode
   # This depends on your infrastructure (e.g., load balancer, API Gateway)
   ```

7. **Stop Application** (if required)
   ```bash
   # Stop API Gateway
   docker-compose stop api-gateway
   ```

8. **Apply Migrations**
   ```bash
   # Start API Gateway with production profile
   docker-compose up -d api-gateway
   
   # Monitor logs in real-time
   docker logs -f api-gateway
   ```

9. **Verify Migration Success**
   ```bash
   # Check Flyway history
   docker exec parking_db psql -U postgres -d parking_db \
     -c "SELECT * FROM flyway_schema_history WHERE success = true ORDER BY installed_rank DESC LIMIT 5;"
   
   # Check for failed migrations
   docker exec parking_db psql -U postgres -d parking_db \
     -c "SELECT * FROM flyway_schema_history WHERE success = false;"
   ```

### Phase 3: Verification

10. **Run Health Checks**
    ```bash
    # Application health
    curl http://api-gateway:8080/actuator/health
    
    # Database connectivity
    docker exec parking_db pg_isready -U postgres
    
    # Check tables
    docker exec parking_db psql -U postgres -d parking_db \
      -c "\dt" | wc -l
    ```

11. **Verify Data Integrity**
    ```sql
    -- Check foreign keys
    SELECT COUNT(*) 
    FROM information_schema.table_constraints 
    WHERE constraint_type = 'FOREIGN KEY';
    
    -- Check indexes
    SELECT COUNT(*) 
    FROM pg_indexes 
    WHERE schemaname = 'public';
    
    -- Verify critical tables exist
    SELECT table_name 
    FROM information_schema.tables 
    WHERE table_schema = 'public' 
    ORDER BY table_name;
    ```

12. **Test Application Functionality**
    ```bash
    # Test authentication
    curl -X POST http://api-gateway:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"username":"admin","password":"parking123"}'
    
    # Test protected endpoint
    curl -X GET http://api-gateway:8080/api/clients \
      -H "Authorization: Bearer <TOKEN>"
    ```

### Phase 4: Post-Deployment

13. **Disable Maintenance Mode**
    ```bash
    # Remove maintenance mode
    # (Infrastructure-specific)
    ```

14. **Monitor Application**
    ```bash
    # Monitor logs for errors
    docker logs api-gateway --tail 100 -f
    
    # Check error rates in monitoring system
    # (Prometheus, Grafana, etc.)
    ```

15. **Document Changes**
    ```bash
    # Create deployment log
    cat > deployment_log_$(date +%Y%m%d).md << EOF
    # Deployment $(date +%Y-%m-%d)
    
    ## Migrations Applied
    - V5__description.sql
    - V6__description.sql
    
    ## Results
    - Status: Success
    - Duration: XX minutes
    - Issues: None
    
    ## Verification
    - Health checks: PASSED
    - Data integrity: VERIFIED
    - Application tests: PASSED
    EOF
    ```

---

## 🚨 Rollback Procedure

### If Migration Fails

1. **Immediately stop deployment**
   ```bash
   docker-compose stop api-gateway
   ```

2. **Check error logs**
   ```bash
   docker logs api-gateway --tail 200
   ```

3. **Identify failed migration**
   ```sql
   SELECT * FROM flyway_schema_history 
   WHERE success = false 
   ORDER BY installed_rank DESC 
   LIMIT 1;
   ```

4. **Assess damage**
   ```sql
   -- Check what was changed
   SELECT tablename, schemaname 
   FROM pg_tables 
   WHERE schemaname = 'public' 
   ORDER BY tablename;
   ```

### Rollback Options

#### Option A: Fix and Retry (Minor Issue)

If the issue is minor (e.g., syntax error):

1. **Remove failed migration record**
   ```sql
   DELETE FROM flyway_schema_history 
   WHERE version = 'X' AND success = false;
   ```

2. **Fix the migration SQL file**
   ```bash
   # Edit the file
   vim backend/api-gateway/src/main/resources/db/migration/VX__description.sql
   ```

3. **Rebuild and retry**
   ```bash
   cd backend
   mvn clean install
   docker-compose up --build api-gateway
   ```

#### Option B: Manual Rollback (Partial Application)

If migration partially applied:

1. **Write compensating migration**
   ```sql
   -- VX_1__rollback_VX.sql
   -- Undo changes from VX
   DROP TABLE IF EXISTS new_table;
   ALTER TABLE existing_table DROP COLUMN IF EXISTS new_column;
   ```

2. **Apply compensating migration**
   ```bash
   docker exec parking_db psql -U postgres -d parking_db \
     -f /path/to/rollback.sql
   ```

#### Option C: Full Database Restore (Critical Failure)

If database is corrupted:

1. **Stop all services**
   ```bash
   docker-compose down
   ```

2. **Restore from backup**
   ```bash
   # Restore database
   docker exec -i parking_db pg_restore -U postgres \
     -d parking_db --clean --if-exists \
     < backup_prod_YYYYMMDD_HHMMSS.dump
   ```

3. **Verify restoration**
   ```sql
   SELECT * FROM flyway_schema_history 
   ORDER BY installed_rank DESC 
   LIMIT 5;
   ```

4. **Restart services**
   ```bash
   docker-compose up -d
   ```

---

## 📊 Monitoring During Migration

### Key Metrics to Watch

1. **Database Connections**
   ```sql
   SELECT count(*) 
   FROM pg_stat_activity 
   WHERE datname = 'parking_db';
   ```

2. **Active Queries**
   ```sql
   SELECT pid, usename, state, query 
   FROM pg_stat_activity 
   WHERE datname = 'parking_db' 
   AND state != 'idle';
   ```

3. **Lock Monitoring**
   ```sql
   SELECT 
     locktype, 
     relation::regclass, 
     mode, 
     granted 
   FROM pg_locks 
   WHERE NOT granted;
   ```

4. **Migration Duration**
   ```bash
   # Start time
   date +%s > migration_start.txt
   
   # After migration
   START=$(cat migration_start.txt)
   END=$(date +%s)
   DURATION=$((END - START))
   echo "Migration took $DURATION seconds"
   ```

---

## 🔍 Post-Migration Validation

### Automated Tests

```bash
# Run integration tests
cd backend
mvn verify -Dspring.profiles.active=production

# Run smoke tests
curl -f http://api-gateway:8080/actuator/health || exit 1
curl -f http://api-gateway:8080/actuator/info || exit 1
```

### Manual Validation

```sql
-- Verify all migrations applied
SELECT COUNT(*) FROM flyway_schema_history WHERE success = true;

-- Expected result: Total number of migrations

-- Verify no failed migrations
SELECT COUNT(*) FROM flyway_schema_history WHERE success = false;

-- Expected result: 0

-- Verify checksums match
SELECT version, checksum 
FROM flyway_schema_history 
ORDER BY installed_rank;

-- Compare with your local checksums
```

---

## 📝 Common Issues and Solutions

### Issue: "Checksum mismatch"

**Cause:** Migration file was modified after being applied.

**Solution:**
```sql
-- DO NOT modify applied migrations!
-- Instead, create a new migration to fix the issue
```

### Issue: "Out of order migration detected"

**Cause:** Migration with lower version number added after higher versions applied.

**Solution:**
```yaml
# In development only, you can enable:
spring:
  flyway:
    out-of-order: true

# In production: Renumber the migration to be after existing ones
```

### Issue: "Migration failed with SQL syntax error"

**Cause:** SQL syntax error in migration file.

**Solution:**
1. Fix the SQL file
2. Remove failed migration record from flyway_schema_history
3. Retry migration

### Issue: "Connection timeout during migration"

**Cause:** Long-running migration blocking connections.

**Solution:**
- Increase timeout settings
- Break migration into smaller chunks
- Consider applying during low-traffic period

---

## 🎓 Best Practices

### DO

- ✅ Always backup before migration
- ✅ Test on staging first
- ✅ Apply migrations during maintenance window
- ✅ Monitor during and after migration
- ✅ Have rollback plan ready
- ✅ Keep migrations small and focused
- ✅ Document all changes

### DON'T

- ❌ Modify applied migrations
- ❌ Skip testing on staging
- ❌ Apply untested migrations to production
- ❌ Use `flyway clean` in production
- ❌ Mix DDL and DML in same migration
- ❌ Apply migrations during peak hours
- ❌ Forget to backup

---

## 📞 Emergency Contacts

During production migration, have these contacts ready:

- **Database Admin:** [Contact info]
- **DevOps Lead:** [Contact info]
- **Development Lead:** [Contact info]
- **On-Call Engineer:** [Contact info]

---

## 📚 References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Database README](../database/README.md)
- [Deployment Guide](DEPLOYMENT_GUIDE.md)
- [PostgreSQL Backup/Restore](https://www.postgresql.org/docs/current/backup.html)

