# Database Migration Quick Reference

## 📚 Documentation Links

- **[Database README](../database/README.md)** - Complete migration guide
- **[Deployment Guide](../docs/DEPLOYMENT_GUIDE.md)** - Production deployment
- **[Migration Tasks](../docs/DATABASE_MIGRATION_TASKS_EN.md)** - Task breakdown

## 🚀 Quick Commands

### Check Migration Status
```powershell
# PowerShell
cd devops
.\test-flyway-migrations.ps1

# Or view history directly
docker exec parking_db psql -U postgres -d parking_db -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank;"
```

### Create New Migration

```powershell
# 1. Create file with next version number
cd backend/api-gateway/src/main/resources/db/migration
New-Item -Path "V5__your_description.sql" -ItemType File

# 2. Edit the file with your SQL
notepad V5__your_description.sql

# 3. Test the migration
cd ../../../../../../../devops
.\full-rebuild.ps1

# 4. Verify
.\test-flyway-migrations.ps1
```

### Naming Convention
```
Format: V{NUMBER}__{DESCRIPTION}.sql

✅ Good:
- V1__initial_schema.sql
- V2__add_parking_lots.sql
- V5__add_notifications_table.sql
- V6__add_index_to_users.sql

❌ Bad:
- V1_initial_schema.sql (single underscore)
- v2__add_table.sql (lowercase 'v')
- V2.1__hotfix.sql (decimal version)
```

## 📋 Existing Migrations

| # | File | Description | Tables Added |
|---|------|-------------|--------------|
| 0 | V0__baseline.sql | Baseline marker | - |
| 1 | V1__initial_schema.sql | Core schema | 8 tables |
| 2 | V2__add_parking_lots.sql | Parking facilities | parking_lots |
| 3 | V3__add_parking_spaces.sql | Parking spaces | parking_spaces |
| 4 | V4__add_bookings.sql | Reservations | bookings |

## 🔧 Troubleshooting

### Migration Failed?
```sql
-- Check what failed
docker exec parking_db psql -U postgres -d parking_db -c "SELECT * FROM flyway_schema_history WHERE success = false;"

-- Remove failed record
docker exec parking_db psql -U postgres -d parking_db -c "DELETE FROM flyway_schema_history WHERE version = 'X' AND success = false;"

-- Restart service
docker-compose restart api-gateway
```

### Reset Database (Development Only!)
```powershell
# ⚠️ WARNING: Deletes all data!
docker-compose down -v
docker-compose up -d
```

## 📖 Full Documentation

For detailed information, see:
- [Database README](../database/README.md)
- [Deployment Guide](../docs/DEPLOYMENT_GUIDE.md)

