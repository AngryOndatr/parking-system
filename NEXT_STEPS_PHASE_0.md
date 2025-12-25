# 🚀 NEXT STEPS: Complete Phase 0

**Current Status:** 85% Complete  
**Time to 100%:** 3-5 days

---

## Priority 1: GitHub Configuration (30 minutes)

### 1. Create GitHub Projects Kanban Board

**📚 Важное пояснение: Board vs Kanban**

В GitHub Projects есть путаница терминов:

**"Board" (Доска)** - это общее название для визуального представления проекта в GitHub Projects (новая версия, Projects Beta/v2). У Board есть разные **layouts/views**:
- 📊 **Table view** - табличное представление
- 📋 **Board view** - представление в виде колонок (это и есть **Kanban**)
- 📅 **Roadmap view** - временная шкала

**"Kanban"** - это **методология** управления проектом с колонками (Backlog → In Progress → Done). В GitHub Projects это реализуется через **Board view** (вид "доска" с колонками).

**🎯 ЧТО МЫ СОЗДАЕМ:**
Мы создаем **GitHub Project** с **Board view** (колонки), что и называется **Kanban-доской**.

**Правильный процесс:**

```bash
# On GitHub.com:
1. Go to your repository → "Projects" tab
2. Click "Link a project" → "New project"
3. You'll see options:
   
   📋 Board    - Kanban-style columns (ЭТО НАМ НУЖНО!)
   📊 Table    - Spreadsheet view
   🗺️ Roadmap  - Timeline view
   
4. Choose "Board" (это Kanban с колонками!)
5. Name: "Parking System Development"
6. Template: "Board" or "Team backlog"

# GitHub автоматически создаст колонки:
   - 📋 Todo
   - 🔨 In Progress  
   - ✅ Done

# Вы можете добавить/переименовать колонки:
   - 📋 Backlog      (новые идеи)
   - 📝 To Do        (готовы к работе)
   - 🔨 In Progress  (в работе)
   - 👀 Review       (на ревью)
   - ✅ Done         (завершено)
```

**🔍 Почему это важно:**
- **Board** в GitHub = **Kanban доска** в классическом понимании
- Это не два разных инструмента, а одно и то же
- В документации GitHub используют термин "Board", но это Kanban-методология
- GitHub Projects (старая версия) называлась "Project boards" (множественное число)
- GitHub Projects (новая версия, v2) просто называется "Projects" с Board/Table/Roadmap views

### 2. Configure Branch Protection
```bash
# On GitHub.com → Settings → Branches:
1. Add rule for 'main' branch:
   ✓ Require pull request before merging
   ✓ Require approvals (1)
   ✓ Require status checks to pass
   ✓ Require conversation resolution
   
2. Add rule for 'develop' branch:
   ✓ Require pull request before merging
   ✓ Require status checks to pass
```

### 3. Create main branch
```bash
git checkout develop
git checkout -b main
git push -u origin main

# Set main as default on GitHub
```

---

## Priority 2: Database Migration Setup (2-3 hours)

**📚 Важное пояснение:**

У вас **УЖЕ ЕСТЬ 8 таблиц** в `database/init.sql`:
- ✅ users, user_backup_codes (security)
- ✅ clients, vehicles (клиенты)
- ✅ subscriptions (подписки = тарифы)
- ✅ parking_events (сессии парковки)
- ✅ payments (платежи)
- ✅ logs (системные логи)

**Что нужно добавить: только 3 таблицы!**
- ❌ parking_lots (управление парковками)
- ❌ parking_spaces (парковочные места)
- ❌ bookings (бронирования)

**Зачем нужен Flyway/Liquibase?**

Database Migration - это управление изменениями БД через версионированные скрипты.

**Без миграций (сейчас):**
```
database/init.sql  ← один большой файл
```
- При изменении нужно пересоздавать всю БД
- Нет истории изменений
- Сложно работать в команде

**С миграциями (Flyway):**
```
db/migration/
├── V1__initial_schema.sql       # Текущие 8 таблиц
├── V2__add_parking_lots.sql     # Новая таблица
├── V3__add_parking_spaces.sql   # Новая таблица
└── V4__add_bookings.sql         # Новая таблица
```
- ✅ Автоматическое применение изменений
- ✅ История всех изменений
- ✅ Откат к любой версии
- ✅ Безопасное обновление production

📖 **Подробное объяснение:** см. [docs/DATABASE_MIGRATION_EXPLAINED.md](./docs/DATABASE_MIGRATION_EXPLAINED.md)

---

### 1. Add Flyway Dependency (5 minutes)

**Выберите один сервис для управления БД:**
- Рекомендую: API Gateway (центральная точка входа)
- Альтернатива: отдельный database-migrator сервис

```xml
<!-- В api-gateway/pom.xml или выбранном сервисе -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### 2. Configure Flyway (5 minutes)

```yaml
# api-gateway/src/main/resources/application.yml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true  # ВАЖНО! Для существующей БД
    baseline-version: 0
    baseline-description: "Existing schema"
    locations: classpath:db/migration
    schemas: public
    validate-on-migrate: true
```

### 3. Reorganize SQL Scripts (10 minutes)

```bash
# Создать структуру миграций
mkdir -p backend/api-gateway/src/main/resources/db/migration

# Скопировать текущий init.sql как V1
cp database/init.sql \
   backend/api-gateway/src/main/resources/db/migration/V1__initial_schema.sql
```

**Или если используете отдельный migrator сервис:**
```bash
mkdir -p backend/database-migrator/src/main/resources/db/migration
```

### 4. Create Missing Tables (1-2 hours)

**V2__add_parking_lots.sql** - Информация о парковках:
```sql
-- Parking lots management (несколько парковок в системе)
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
    -- ACTIVE, MAINTENANCE, CLOSED, FULL
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_parking_lot_status 
        CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'CLOSED', 'FULL'))
);

CREATE INDEX idx_parking_lots_name ON parking_lots(name);
CREATE INDEX idx_parking_lots_status ON parking_lots(status);
CREATE INDEX idx_parking_lots_city ON parking_lots(city);
```

**V3__add_parking_spaces.sql** - Конкретные места:
```sql
-- Individual parking spaces
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
    
    -- Charging (for electric spaces)
    has_charger BOOLEAN DEFAULT FALSE,
    charger_type VARCHAR(50),
    
    -- Dimensions
    length_cm INTEGER,
    width_cm INTEGER,
    
    -- Pricing (optional override from lot-level pricing)
    hourly_rate_override DECIMAL(10,2),
    daily_rate_override DECIMAL(10,2),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_occupied_at TIMESTAMP,
    
    UNIQUE(parking_lot_id, space_number),
    
    CONSTRAINT chk_space_type 
        CHECK (space_type IN ('STANDARD', 'HANDICAPPED', 'ELECTRIC', 
                              'VIP', 'COMPACT', 'OVERSIZED')),
    CONSTRAINT chk_space_status 
        CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 
                          'MAINTENANCE', 'OUT_OF_SERVICE'))
);

CREATE INDEX idx_parking_spaces_lot ON parking_spaces(parking_lot_id);
CREATE INDEX idx_parking_spaces_status ON parking_spaces(status);
CREATE INDEX idx_parking_spaces_type ON parking_spaces(space_type);
CREATE INDEX idx_parking_spaces_lot_status ON parking_spaces(parking_lot_id, status);
```

**V4__add_bookings.sql** - Система бронирования:
```sql
-- Reservations/bookings system
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
    cancelled_by VARCHAR(50), -- USER, ADMIN, SYSTEM
    cancellation_reason TEXT,
    refund_amount DECIMAL(10,2),
    
    -- Special requirements
    notes TEXT,
    special_requirements TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_booking_status 
        CHECK (status IN ('PENDING', 'CONFIRMED', 'ACTIVE', 'COMPLETED', 
                          'CANCELLED', 'NO_SHOW', 'EXPIRED')),
    CONSTRAINT chk_booking_time 
        CHECK (end_time > start_time),
    CONSTRAINT chk_cancelled_by 
        CHECK (cancelled_by IS NULL OR cancelled_by IN ('USER', 'ADMIN', 'SYSTEM'))
);

CREATE INDEX idx_bookings_client ON bookings(client_id);
CREATE INDEX idx_bookings_space ON bookings(parking_space_id);
CREATE INDEX idx_bookings_vehicle ON bookings(vehicle_id);
CREATE INDEX idx_bookings_code ON bookings(booking_code);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_time ON bookings(start_time, end_time);
CREATE INDEX idx_bookings_active ON bookings(status) 
    WHERE status IN ('PENDING', 'CONFIRMED', 'ACTIVE');
```

### 5. Test Migrations (30 minutes)

```bash
# 1. Остановить контейнеры и удалить volumes
cd devops
docker-compose down -v

# 2. Пересобрать сервисы
cd ../backend/api-gateway
mvn clean package -DskipTests

# 3. Запустить контейнеры
cd ../../devops
docker-compose up -d

# 4. Проверить логи Flyway
docker logs api-gateway 2>&1 | grep -i flyway

# Ожидаемый вывод:
# Flyway Community Edition by Redgate
# Database: jdbc:postgresql://parking_db:5432/parking_db
# Successfully validated 4 migrations
# Creating Schema History table "public"."flyway_schema_history"
# Current version of schema "public": << Empty Schema >>
# Migrating schema "public" to version "1 - initial schema"
# Migrating schema "public" to version "2 - add parking lots"
# Migrating schema "public" to version "3 - add parking spaces"
# Migrating schema "public" to version "4 - add bookings"
# Successfully applied 4 migrations to schema "public"
```

### 6. Verify Database (10 minutes)

```bash
# Подключиться к БД через pgAdmin или psql
docker exec -it parking_db psql -U postgres -d parking_db

# Проверить созданные таблицы
\dt

# Должны увидеть:
#  users
#  user_backup_codes
#  clients
#  vehicles
#  subscriptions
#  parking_events
#  payments
#  logs
#  parking_lots        ← НОВАЯ
#  parking_spaces      ← НОВАЯ
#  bookings            ← НОВАЯ

# Проверить историю миграций
SELECT * FROM flyway_schema_history;

# Должны увидеть записи V1, V2, V3, V4
```

---

## Priority 3: Complete Database Schema (ALREADY DONE! 🎉)

### ✅ Текущее состояние БД

**У вас УЖЕ ЕСТЬ 8 из 11 нужных таблиц!**

| Таблица | Статус | Назначение |
|---------|--------|------------|
| ✅ users | DONE | Пользователи системы (38 полей!) |
| ✅ user_backup_codes | DONE | 2FA backup codes |
| ✅ clients | DONE | Клиенты парковки |
| ✅ vehicles | DONE | Транспортные средства |
| ✅ subscriptions | DONE | Подписки (= tariffs) |
| ✅ parking_events | DONE | События парковки (= parking_sessions + access_logs) |
| ✅ payments | DONE | Платежи |
| ✅ logs | DONE | Системные логи |
| ❌ parking_lots | TODO | Управление парковками |
| ❌ parking_spaces | TODO | Парковочные места |
| ❌ bookings | TODO | Бронирования |

### 📋 Что означают существующие таблицы

**Таблицы, которые "уже есть" (но под другими названиями):**

1. **subscriptions** → выполняет роль **tariffs**
   - Хранит тарифные планы
   - Подписки клиентов

2. **parking_events** → объединяет **parking_sessions** + **access_logs**
   - События въезда/выезда
   - Активные сессии парковки
   - Логи доступа к воротам

3. **users** (с role='OPERATOR') → заменяет **operators**
   - Роль OPERATOR уже определена
   - Нет необходимости в отдельной таблице

### 🎯 Таблицы для создания в Priority 2

Смотри выше в **Priority 2** - там SQL для создания:
1. ❌ parking_lots - V2__add_parking_lots.sql
2. ❌ parking_spaces - V3__add_parking_spaces.sql  
3. ❌ bookings - V4__add_bookings.sql

**Только эти 3 таблицы нужно добавить!**

### 💡 Дополнительные таблицы (опционально, для будущего)

Если захотите расширить функциональность:

4. **tariffs** (отдельно от subscriptions)
```sql
CREATE TABLE tariffs (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lots(id),
    name VARCHAR(100) NOT NULL,
    tariff_type VARCHAR(50), -- HOURLY, DAILY, MONTHLY, SUBSCRIPTION
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    duration_minutes INTEGER,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

5. **operators_schedule** (расписание операторов)
```sql
CREATE TABLE operators_schedule (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    parking_lot_id BIGINT REFERENCES parking_lots(id),
    shift_date DATE NOT NULL,
    shift_start TIME NOT NULL,
    shift_end TIME NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED'
);
```

6. **maintenance_logs** (обслуживание оборудования)
```sql
CREATE TABLE maintenance_logs (
    id BIGSERIAL PRIMARY KEY,
    parking_space_id BIGINT REFERENCES parking_spaces(id),
    maintenance_type VARCHAR(50),
    description TEXT,
    scheduled_at TIMESTAMP,
    completed_at TIMESTAMP,
    performed_by BIGINT REFERENCES users(id),
    cost DECIMAL(10,2),
    status VARCHAR(20)
);
```

### ✅ ВЫВОД

**Priority 3 практически выполнен!**
- 8 из 11 таблиц уже созданы и работают ✅
- 3 таблицы будут добавлены в Priority 2 через Flyway ✅
- Дополнительные таблицы - по желанию, не критичны 🎯

**Общее время на Priority 3: 0 часов (уже сделано!)** 🎉

---

## Priority 4: Implement Services (3-5 days)

### Services to Implement:

1. ✅ **API Gateway** - COMPLETE
2. ✅ **Client Service** - COMPLETE  
3. ✅ **Eureka Server** - COMPLETE
4. ⚠️ **User Service** - 20% done, needs full CRUD
5. ⚠️ **Parking Service** - needs creation
6. ⚠️ **Booking Service** - needs creation
7. ⚠️ **Payment Service** - needs creation
8. ⚠️ **Billing Service** - basic structure, needs implementation
9. ⚠️ **Gate Control Service** - basic structure, needs implementation
10. ⚠️ **Management Service** - basic structure, needs implementation
11. ⚠️ **Reporting Service** - basic structure, needs implementation

### Implementation Order:
1. **User Service** (1 day) - Complete user management
2. **Parking Service** (1 day) - Parking lots and spaces
3. **Booking Service** (1 day) - Reservations
4. **Payment/Billing Service** (1 day) - Combined or separate
5. **Gate Control** (0.5 day) - Access control
6. **Management** (0.5 day) - Dashboard APIs
7. **Reporting** (0.5 day) - Log aggregation

---

## Bonus: CI/CD Setup (Optional, 1 day)

### GitHub Actions Workflows

1. **build.yml** - Build and test on PR
```yaml
name: Build and Test
on: [pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - run: mvn clean verify
```

2. **deploy.yml** - Deploy to staging/production
```yaml
name: Deploy
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: docker-compose build
      - run: docker-compose push
```

---

## 📋 Checklist for Phase 0 Completion

- [ ] GitHub Projects board created
- [ ] Branch protection configured
- [ ] Flyway/Liquibase setup
- [ ] All 8 tables created
- [ ] Migration scripts organized
- [ ] User Service complete
- [ ] Parking Service complete
- [ ] Booking Service complete
- [ ] Payment/Billing complete
- [ ] Gate Control complete
- [ ] Management complete
- [ ] Reporting complete
- [ ] Integration tests added
- [ ] CI/CD pipeline configured
- [ ] Documentation updated

---

**When complete, you'll have:**
- ✅ Professional GitHub repository
- ✅ Complete infrastructure
- ✅ Full database schema
- ✅ All 11 microservices implemented
- ✅ Automated CI/CD
- ✅ Ready for Phase 1 development!

🎯 **Let's complete Phase 0 and move to Phase 1!**

