# Database Documentation

## Overview

The Parking System uses PostgreSQL database with Flyway for version-controlled schema migrations. All migrations are located in `backend/api-gateway/src/main/resources/db/migration/`.

---

## Migration Structure

### Existing Migrations

| Version | File | Description | Tables | Status |
|---------|------|-------------|--------|--------|
| V0 | `V0__baseline.sql` | Baseline migration | N/A | ✅ |
| V1 | `V1__initial_schema.sql` | Initial schema | users, user_backup_codes, clients, vehicles, subscriptions, parking_events, payments, logs | ✅ |
| V2 | `V2__add_parking_lots.sql` | Parking facilities management | parking_lots | ✅ |
| V3 | `V3__add_parking_spaces.sql` | Individual parking spaces | parking_spaces | ✅ |
| V4 | `V4__add_bookings.sql` | Reservation system | bookings | ✅ |
| V5 | `V5__insert_test_parking_data.sql` | Test data for development | N/A (inserts) | ✅ |
| V6 | `V6__extend_logs_table.sql` | Extend logs table | logs (service, meta columns) | ✅ |

**Latest Migration (V6 - 2026-01-13):**
- Added `service` column to logs table (VARCHAR(100)) - tracks originating microservice
- Added `meta` column to logs table (TEXT/JSON) - stores additional metadata
- Added indexes on `service` and `log_level` for query performance
- Required for Reporting Service (Issue #19)

### Test Data (V5)

**Parking Lot:**
- Name: Downtown Parking
- Total Capacity: 100 spaces
- Available: 15 spaces (initially)
- Location: Kyiv, Ukraine

**Parking Spaces (23 total):**
- Section A: 7 standard spaces (including 2 handicapped)
- Section B: 4 electric charging spaces (Type 2, DC Fast, Tesla)
- Section C: 3 VIP spaces (with rate overrides)
- Section D: 4 compact spaces
- Section E: 3 oversized spaces (underground -1)
- Section F: 2 maintenance/out-of-service spaces

**Space Distribution by Status:**
- 15 AVAILABLE
- 4 OCCUPIED
- 2 RESERVED
- 2 MAINTENANCE/OUT_OF_SERVICE

### Total Tables: 13

**Core Tables:**
- `users` - User authentication and security (38 fields)
- `user_backup_codes` - 2FA backup codes
- `clients` - Customer profiles
- `vehicles` - Vehicle registration
- `subscriptions` - Subscription plans
- `parking_events` - Entry/exit events
- `payments` - Payment transactions
- `logs` - System audit logs

**New Tables (via Flyway):**
- `parking_lots` - Parking facilities
- `parking_spaces` - Individual spaces
- `bookings` - Space reservations

---

## Creating New Migration

### Step-by-Step Guide

1. **Create Migration File**
   ```bash
   # Navigate to migrations directory
   cd backend/api-gateway/src/main/resources/db/migration/
   
   # Create new migration file
   touch V5__your_description.sql
   ```

2. **Write Migration SQL**
   - Only include UP migration (Flyway doesn't support DOWN)
   - Use idempotent SQL where possible
   - Add comments for complex logic
   
   Example:
   ```sql
   -- V5__add_notifications.sql
   -- Purpose: Add notifications table for user alerts
   
   CREATE TABLE notifications (
       id BIGSERIAL PRIMARY KEY,
       user_id BIGINT NOT NULL REFERENCES users(id),
       message TEXT NOT NULL,
       read BOOLEAN DEFAULT FALSE,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   
   -- Indexes for performance
   CREATE INDEX idx_notifications_user ON notifications(user_id);
   CREATE INDEX idx_notifications_unread ON notifications(user_id, read) 
       WHERE read = FALSE;
   ```

3. **Test Locally**
   ```powershell
   # Rebuild containers with new migration
   cd devops
   .\full-rebuild.ps1
   
   # Verify migration
   .\test-flyway-migrations.ps1
   ```

4. **Verify in Database**
   ```sql
   -- Check Flyway history
   SELECT * FROM flyway_schema_history ORDER BY installed_rank;
   
   -- Verify table exists
   \dt
   ```

5. **Commit to Git**
   ```bash
   git add backend/api-gateway/src/main/resources/db/migration/V5__*.sql
   git commit -m "[DB Migration] V5 - Add notifications table"
   git push
   ```

---

## Naming Convention

### Format
```
V{NUMBER}__{DESCRIPTION}.sql
```

### Rules

1. **Prefix**: `V` (uppercase, mandatory)
2. **Version Number**: Sequential integer (1, 2, 3, ...)
   - Never reuse numbers
   - Never skip numbers
   - Use consecutive numbering
3. **Separator**: `__` (double underscore)
4. **Description**: 
   - Use snake_case
   - Be brief but descriptive
   - Use action verbs (add, create, update, remove)

### ✅ Good Examples
```
V1__initial_schema.sql
V2__add_parking_lots.sql
V3__add_parking_spaces.sql
V4__add_bookings.sql
V5__add_notifications_table.sql
V6__add_index_to_users.sql
V7__alter_payments_add_currency.sql
V8__create_audit_triggers.sql
```

### ❌ Bad Examples
```
V1_initial_schema.sql        # Single underscore
v2__add_parking_lots.sql     # Lowercase 'v'
V2.1__hotfix.sql             # Decimal version
V3__AddParkingSpaces.sql     # CamelCase
V5__migration.sql            # Non-descriptive
migration_001.sql            # Wrong format
```

---

## Flyway Commands

### Via Docker (Development)

```powershell
# Check migration status
docker exec api-gateway java -jar /app/flyway/flyway-commandline.jar info

# Run migrations
docker-compose restart api-gateway

# Repair failed migration
docker exec parking_db psql -U postgres -d parking_db -c "DELETE FROM flyway_schema_history WHERE success = false;"
```

### Via Application Properties

Flyway runs automatically on application startup if:
```yaml
spring:
  flyway:
    enabled: true
```

---

## Configuration

### Development (application-development.yml)

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true      # Allow baseline on existing schema
    baseline-version: 0
    baseline-description: "Existing schema"
    locations: classpath:db/migration
    schemas: public
    validate-on-migrate: true
    out-of-order: false
```

### Production (application-production.yml)

```yaml
spring:
  flyway:
    # Core Settings
    enabled: true
    
    # Safety Features - CRITICAL for production!
    baseline-on-migrate: false     # Don't auto-baseline
    validate-on-migrate: true      # Verify checksums
    clean-disabled: true           # Prevent database wipe!
    out-of-order: false            # Enforce sequential order
    
    # Validation
    ignore-missing-migrations: false
    ignore-pending-migrations: false
    ignore-future-migrations: false
    
    # Configuration
    locations: classpath:db/migration
    schemas: public
    batch: true
    connect-retries: 3
```

**📖 Complete Production Configuration:** See `application-production.yml`

**📖 Production Deployment Process:** [PRODUCTION_MIGRATION_PROCESS.md](../docs/PRODUCTION_MIGRATION_PROCESS.md)

---

## Best Practices

### ✅ DO

- **Keep migrations small** - One logical change per migration
- **Test thoroughly** - Test on dev before committing
- **Use transactions** - Wrap changes in BEGIN/COMMIT
- **Add indexes** - Create indexes for foreign keys
- **Document complex logic** - Add comments
- **Use constraints** - Add CHECK, UNIQUE, NOT NULL
- **Version control** - Commit migrations to Git
- **Sequential numbering** - Never skip version numbers

### ❌ DON'T

- **Modify existing migrations** - Create new migration instead
- **Delete migrations** - Flyway tracks by checksum
- **Use dynamic SQL** - Avoid procedures/functions if possible
- **Forget rollback plan** - Document how to undo changes
- **Skip testing** - Always test locally first
- **Use DROP TABLE** - Extremely dangerous in production

---

## Troubleshooting

### Migration Failed

```sql
-- Check failed migration
SELECT * FROM flyway_schema_history WHERE success = false;

-- Fix the SQL file, then repair
DELETE FROM flyway_schema_history WHERE version = 'X' AND success = false;

-- Restart application
docker-compose restart api-gateway
```

### Checksum Mismatch

```sql
-- Update checksum (only if you know what you're doing!)
UPDATE flyway_schema_history 
SET checksum = {new_checksum} 
WHERE version = 'X';
```

### Reset Database (Development Only!)

```powershell
# ⚠️ DANGER: This deletes all data!
docker-compose down -v
docker-compose up -d
```

---

## Migration Testing

### Automated Tests

```powershell
# Run test script
cd devops
.\test-flyway-migrations.ps1
```

### Manual Verification

```sql
-- 1. Check all migrations
SELECT installed_rank, version, description, type, success 
FROM flyway_schema_history 
ORDER BY installed_rank;

-- 2. Verify table count
SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public';

-- 3. Check foreign keys
SELECT 
    tc.table_name, 
    kcu.column_name, 
    ccu.table_name AS foreign_table_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY';

-- 4. List all indexes
SELECT tablename, indexname, indexdef 
FROM pg_indexes 
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
```

---

## Initial Setup Files

### init.sql
Legacy initialization script. **Only used for baseline**, not for new tables.

Contains:
- Initial 8 tables schema
- Users with BCrypt passwords
- Sample data

**Note:** New tables should be added via Flyway migrations (V2+), not in init.sql.

### users_security_migration.sql
Backup script for user security data migration. Not used in normal flow.

---

## Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- Project Migration Scripts: `backend/api-gateway/src/main/resources/db/migration/`
- Test Script: `devops/test-flyway-migrations.ps1`

---

## Questions?

For migration issues or questions:
1. Check `flyway_schema_history` table
2. Review API Gateway logs: `docker logs api-gateway`
3. Run test script: `.\test-flyway-migrations.ps1`
4. Check database logs: `docker logs parking_db`

