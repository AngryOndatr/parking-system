# 🗂️ Database Migration Tasks - Decomposition for GitHub Issues

**Date:** 2025-12-25  
**Goal:** Implement Flyway for database migration management  
**Total Time:** 2-3 hours  
**Priority:** HIGH

---

## 📋 TASKS FOR GITHUB ISSUES

### Issue #1: Setup Flyway Dependency and Configuration

**Title:** `[DB Migration] Setup Flyway dependency and configuration`

**Description:**
```markdown
## 🎯 Goal
Add Flyway to the project for database migration management.

## 📝 Tasks
- [ ] Add Flyway dependencies to `api-gateway/pom.xml`
- [ ] Configure Flyway in `application.yml` (development profile)
- [ ] Configure Flyway in `application-production.yml` (production profile)
- [ ] Add `baseline-on-migrate: true` parameter for existing DB
- [ ] Create directory `src/main/resources/db/migration`

## 📦 Dependencies
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

## ⚙️ Configuration
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    baseline-description: "Existing schema"
    locations: classpath:db/migration
    schemas: public
    validate-on-migrate: true
```

## ✅ Acceptance Criteria
- Flyway added to dependencies
- Configuration in `application.yml` is set up
- Migration directory created
- Service starts successfully

## ⏱️ Estimate
15 minutes

## 🏷️ Labels
`database`, `migration`, `flyway`, `priority-high`
```

---

### Issue #2: Create Initial Schema Migration (V1)

**Title:** `[DB Migration] Create V1__initial_schema.sql migration`

**Description:**
```markdown
## 🎯 Goal
Create initial V1 migration with existing database schema.

## 📝 Tasks
- [ ] Copy content from `database/init.sql`
- [ ] Create file `V1__initial_schema.sql` in `db/migration/`
- [ ] Remove DROP TABLE commands (not needed in migrations)
- [ ] Verify SQL syntax correctness
- [ ] Add comments to sections

## 📂 File
`backend/api-gateway/src/main/resources/db/migration/V1__initial_schema.sql`

## 📊 Tables in V1
1. users (38 fields)
2. user_backup_codes
3. clients (5 fields)
4. vehicles (7 fields)
5. subscriptions
6. parking_events
7. payments
8. logs

## ⚠️ Important
- Remove `SET session_replication_role` commands
- Remove all DROP TABLE commands
- Keep only CREATE TABLE and CREATE INDEX

## ✅ Acceptance Criteria
- File V1__initial_schema.sql created
- Contains all 8 existing tables
- SQL syntax is correct
- No DROP commands

## ⏱️ Estimate
20 minutes

## 🏷️ Labels
`database`, `migration`, `sql`, `priority-high`

## 🔗 Dependencies
Requires: #1
```

---

### Issue #3: Create Parking Lots Migration (V2)

**Title:** `[DB Migration] Create V2__add_parking_lots.sql migration`

**Description:**
```markdown
## 🎯 Goal
Add `parking_lots` table for managing multiple parking facilities.

## 📝 Tasks
- [ ] Create file `V2__add_parking_lots.sql`
- [ ] Define parking_lots table structure
- [ ] Add all necessary fields
- [ ] Create indexes for optimization
- [ ] Add constraints and checks

## 📊 Table Structure
```sql
CREATE TABLE parking_lots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    description TEXT,
    
    -- Location
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    
    -- Capacity
    total_spaces INTEGER NOT NULL DEFAULT 0,
    available_spaces INTEGER NOT NULL DEFAULT 0,
    
    -- Operation hours
    opens_at TIME,
    closes_at TIME,
    is_24_hours BOOLEAN DEFAULT FALSE,
    
    -- Contact
    phone VARCHAR(50),
    email VARCHAR(100),
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 📇 Indexes
- idx_parking_lots_name
- idx_parking_lots_status
- idx_parking_lots_city

## ✅ Acceptance Criteria
- File V2__add_parking_lots.sql created
- Table has all necessary fields
- Indexes created
- Constraints added (CHECK for status)

## ⏱️ Estimate
30 minutes

## 🏷️ Labels
`database`, `migration`, `sql`, `feature`, `priority-high`

## 🔗 Dependencies
Requires: #2
```

---

### Issue #4: Create Parking Spaces Migration (V3)

**Title:** `[DB Migration] Create V3__add_parking_spaces.sql migration`

**Description:**
```markdown
## 🎯 Goal
Add `parking_spaces` table for managing individual parking spaces.

## 📝 Tasks
- [ ] Create file `V3__add_parking_spaces.sql`
- [ ] Define parking_spaces table structure
- [ ] Add foreign key to parking_lots
- [ ] Create indexes for optimization
- [ ] Add constraints for types and statuses

## 📊 Table Structure
```sql
CREATE TABLE parking_spaces (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id) ON DELETE CASCADE,
    
    -- Identification
    space_number VARCHAR(20) NOT NULL,
    floor_level INTEGER DEFAULT 0,
    section VARCHAR(50),
    
    -- Type
    space_type VARCHAR(50) DEFAULT 'STANDARD',
    -- STANDARD, HANDICAPPED, ELECTRIC, VIP, COMPACT, OVERSIZED
    
    -- Status
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    -- AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, OUT_OF_SERVICE
    
    -- Electric charging
    has_charger BOOLEAN DEFAULT FALSE,
    charger_type VARCHAR(50),
    
    -- Dimensions
    length_cm INTEGER,
    width_cm INTEGER,
    
    -- Pricing overrides
    hourly_rate_override DECIMAL(10,2),
    daily_rate_override DECIMAL(10,2),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_occupied_at TIMESTAMP,
    
    UNIQUE(parking_lot_id, space_number)
);
```

## 📇 Indexes
- idx_parking_spaces_lot
- idx_parking_spaces_status
- idx_parking_spaces_type
- idx_parking_spaces_lot_status (composite)

## ✅ Acceptance Criteria
- File V3__add_parking_spaces.sql created
- Foreign key to parking_lots works
- Unique constraint on (parking_lot_id, space_number)
- All indexes created
- CHECK constraints for space_type and status

## ⏱️ Estimate
30 minutes

## 🏷️ Labels
`database`, `migration`, `sql`, `feature`, `priority-high`

## 🔗 Dependencies
Requires: #3
```

---

### Issue #5: Create Bookings Migration (V4)

**Title:** `[DB Migration] Create V4__add_bookings.sql migration`

**Description:**
```markdown
## 🎯 Goal
Add `bookings` table for parking space reservation system.

## 📝 Tasks
- [ ] Create file `V4__add_bookings.sql`
- [ ] Define bookings table structure
- [ ] Add foreign keys to clients, parking_spaces, vehicles
- [ ] Create indexes for search optimization
- [ ] Add constraints for validation

## 📊 Table Structure
```sql
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    
    -- References
    client_id BIGINT NOT NULL REFERENCES clients(id),
    parking_space_id BIGINT NOT NULL REFERENCES parking_spaces(id),
    vehicle_id BIGINT REFERENCES vehicles(id),
    
    -- Booking details
    booking_code VARCHAR(20) UNIQUE NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING',
    -- PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED, NO_SHOW, EXPIRED
    
    -- Pricing
    estimated_cost DECIMAL(10,2),
    final_cost DECIMAL(10,2),
    
    -- Payment
    payment_id BIGINT REFERENCES payments(id),
    prepaid BOOLEAN DEFAULT FALSE,
    prepaid_amount DECIMAL(10,2),
    
    -- Check-in/out
    checked_in_at TIMESTAMP,
    checked_out_at TIMESTAMP,
    
    -- Cancellation
    cancelled_at TIMESTAMP,
    cancelled_by VARCHAR(50),
    cancellation_reason TEXT,
    refund_amount DECIMAL(10,2),
    
    -- Special requirements
    notes TEXT,
    special_requirements TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 📇 Indexes
- idx_bookings_client
- idx_bookings_space
- idx_bookings_vehicle
- idx_bookings_code (UNIQUE)
- idx_bookings_status
- idx_bookings_time (composite: start_time, end_time)
- idx_bookings_active (partial: WHERE status IN (...))

## ✅ Acceptance Criteria
- File V4__add_bookings.sql created
- Foreign keys work correctly
- Unique constraint on booking_code
- CHECK constraint on end_time > start_time
- All indexes created, including partial index

## ⏱️ Estimate
40 minutes

## 🏷️ Labels
`database`, `migration`, `sql`, `feature`, `priority-high`

## 🔗 Dependencies
Requires: #4
```

---

### Issue #6: Test Flyway Migrations

**Title:** `[DB Migration] Test Flyway migrations on clean database`

**Description:**
```markdown
## 🎯 Goal
Test all migrations on a clean database.

## 📝 Tasks
- [ ] Stop all containers
- [ ] Remove volumes with database data
- [ ] Rebuild services with Maven
- [ ] Start containers
- [ ] Check Flyway logs
- [ ] Verify all tables creation
- [ ] Check flyway_schema_history table

## 🧪 Test Commands
```bash
# Stop and clean
docker-compose down -v

# Rebuild
cd backend/api-gateway
mvn clean package -DskipTests

# Start
cd ../../devops
docker-compose up -d

# Check logs
docker logs api-gateway 2>&1 | grep -i flyway

# Check database
docker exec -it parking_db psql -U postgres -d parking_db -c "\dt"
docker exec -it parking_db psql -U postgres -d parking_db -c "SELECT * FROM flyway_schema_history;"
```

## ✅ Expected Result
```
Successfully validated 4 migrations
Creating Schema History table "public"."flyway_schema_history"
Migrating schema "public" to version "1 - initial schema"
Migrating schema "public" to version "2 - add parking lots"
Migrating schema "public" to version "3 - add parking spaces"
Migrating schema "public" to version "4 - add bookings"
Successfully applied 4 migrations to schema "public"
```

## 📊 Checks
- [ ] All 11 tables created (8 old + 3 new)
- [ ] flyway_schema_history contains 4 records
- [ ] Indexes created
- [ ] Foreign keys work
- [ ] Service starts without errors

## ✅ Acceptance Criteria
- Migrations applied automatically
- All tables created
- No errors in logs
- flyway_schema_history contains correct records

## ⏱️ Estimate
30 minutes

## 🏷️ Labels
`database`, `migration`, `testing`, `priority-high`

## 🔗 Dependencies
Requires: #5
```

---

### Issue #7: Update Documentation for Flyway

**Title:** `[Documentation] Update database documentation for Flyway migrations`

**Description:**
```markdown
## 🎯 Goal
Update project documentation with Flyway migration information.

## 📝 Tasks
- [ ] Update `database/README.md`
- [ ] Add instructions for creating new migrations
- [ ] Document naming convention (V{number}__{description}.sql)
- [ ] Add Flyway command examples
- [ ] Update `docs/DEPLOYMENT_GUIDE.md` (if exists)

## 📄 README.md Content
```markdown
# Database Migrations

## Structure
- `V1__initial_schema.sql` - Initial schema (8 tables)
- `V2__add_parking_lots.sql` - parking_lots table
- `V3__add_parking_spaces.sql` - parking_spaces table
- `V4__add_bookings.sql` - bookings table

## Creating New Migration
1. Create file: `V{N}__{description}.sql`
2. Write SQL (UP migration only)
3. Test on dev environment
4. Commit to git

## Naming Convention
- Prefix: `V` (version)
- Number: sequential (1, 2, 3...)
- Separator: `__` (double underscore)
- Description: snake_case, brief

Examples:
- `V1__initial_schema.sql`
- `V2__add_parking_lots.sql`
- `V3__add_index_to_users.sql`
```

## ✅ Acceptance Criteria
- README.md updated
- Migration creation instructions added
- Naming convention documented
- Command examples added

## ⏱️ Estimate
20 minutes

## 🏷️ Labels
`documentation`, `database`, `priority-medium`

## 🔗 Dependencies
Requires: #6
```

---

### Issue #8: Configure Flyway for Production

**Title:** `[DB Migration] Configure Flyway for production environment`

**Description:**
```markdown
## 🎯 Goal
Configure Flyway for safe migration application in production.

## 📝 Tasks
- [ ] Create `application-production.yml`
- [ ] Configure strict migration validation
- [ ] Disable baseline-on-migrate for production
- [ ] Configure migration logging
- [ ] Add checksum verification
- [ ] Document deployment process

## ⚙️ Production Configuration
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: false  # Disable for production!
    validate-on-migrate: true
    out-of-order: false
    locations: classpath:db/migration
    schemas: public
    clean-disabled: true  # Protection from accidental cleanup
```

## 📋 Production Deployment Process
1. Backup database
2. Verify migrations on staging
3. Apply migrations in production
4. Verify status
5. Rollback plan (if needed)

## ✅ Acceptance Criteria
- Production configuration created
- Protection from clean operations enabled
- Strict validation enabled
- Deployment process documented

## ⏱️ Estimate
30 minutes

## 🏷️ Labels
`database`, `migration`, `production`, `priority-medium`

## 🔗 Dependencies
Requires: #6
```

---

## 📊 SUMMARY

### Total Issues: 8

| # | Name | Estimate | Priority | Dependencies |
|---|------|----------|----------|--------------|
| 1 | Setup Flyway | 15 min | HIGH | - |
| 2 | V1 Initial Schema | 20 min | HIGH | #1 |
| 3 | V2 Parking Lots | 30 min | HIGH | #2 |
| 4 | V3 Parking Spaces | 30 min | HIGH | #3 |
| 5 | V4 Bookings | 40 min | HIGH | #4 |
| 6 | Test Migrations | 30 min | HIGH | #5 |
| 7 | Update Docs | 20 min | MEDIUM | #6 |
| 8 | Production Config | 30 min | MEDIUM | #6 |

### Total Time: 3 hours 35 minutes

### Distribution by Kanban Columns:

**📋 Backlog:**
- #7 Update Documentation
- #8 Configure Production

**📝 To Do:**
- #1 Setup Flyway
- #2 V1 Initial Schema
- #3 V2 Parking Lots
- #4 V3 Parking Spaces
- #5 V4 Bookings
- #6 Test Migrations

**🔨 In Progress:** (empty)

**👀 Review:** (empty)

**✅ Done:** (empty)

---

## 🏷️ Labels to Create on GitHub

```
database
migration
flyway
sql
feature
testing
documentation
production
priority-high
priority-medium
```

---

## 📝 How to Use This File

### 1. Creating Issues on GitHub

For each task (#1-#8):
1. Open GitHub → Issues → New issue
2. Copy Title
3. Copy Description
4. Add Labels
5. Assign to yourself (or developer)
6. Add to Project (Parking System Development)

### 2. Alternative - Bulk Creation via GitHub CLI

```bash
# Issue #1
gh issue create --title "[DB Migration] Setup Flyway dependency and configuration" \
  --body-file issue1.md \
  --label "database,migration,flyway,priority-high"

# Issue #2
gh issue create --title "[DB Migration] Create V1__initial_schema.sql migration" \
  --body-file issue2.md \
  --label "database,migration,sql,priority-high"

# ... and so on
```

### 3. Automation (optional)

You can create a PowerShell script for automatic creation of all issues.

---

## ✅ Checklist Before Starting

- [ ] Create all Labels on GitHub
- [ ] Create GitHub Project Board
- [ ] Create all 8 Issues
- [ ] Add Issues to Project
- [ ] Set dependencies between issues
- [ ] Assign executor
- [ ] Start with Issue #1

---

**🎯 Ready to use! You can create Issues on GitHub!**

