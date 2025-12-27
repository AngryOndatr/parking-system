# 🗂️ Database Migration Tasks - Декомпозиция для GitHub Issues

**Дата:** 2025-12-25  
**Цель:** Внедрение Flyway для управления миграциями БД  
**Общее время:** 2-3 часа  
**Приоритет:** HIGH

---

## 📋 ЗАДАЧИ ДЛЯ GITHUB ISSUES

### Issue #1: Setup Flyway Dependency and Configuration

**Title:** `[DB Migration] Setup Flyway dependency and configuration`

**Description:**
```markdown
## 🎯 Цель
Добавить Flyway в проект для управления миграциями базы данных.

## 📝 Задачи
- [ ] Добавить Flyway dependencies в `api-gateway/pom.xml`
- [ ] Настроить Flyway в `application.yml` (development profile)
- [ ] Настроить Flyway в `application-production.yml` (production profile)
- [ ] Добавить параметр `baseline-on-migrate: true` для существующей БД
- [ ] Создать директорию `src/main/resources/db/migration`

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
- Flyway добавлен в зависимости
- Конфигурация в `application.yml` настроена
- Директория для миграций создана
- Сервис успешно запускается

## ⏱️ Estimate
15 минут

## 🏷️ Labels
`database`, `migration`, `flyway`, `priority-high`
```

---

### Issue #2: Create Initial Schema Migration (V1)

**Title:** `[DB Migration] Create V1__initial_schema.sql migration`

**Description:**
```markdown
## 🎯 Цель
Создать начальную миграцию V1 с существующей схемой БД.

## 📝 Задачи
- [ ] Скопировать содержимое `database/init.sql`
- [ ] Создать файл `V1__initial_schema.sql` в `db/migration/`
- [ ] Удалить команды DROP TABLE (не нужны в миграциях)
- [ ] Проверить корректность SQL синтаксиса
- [ ] Добавить комментарии к секциям

## 📂 Файл
`backend/api-gateway/src/main/resources/db/migration/V1__initial_schema.sql`

## 📊 Таблицы в V1
1. users (38 полей)
2. user_backup_codes
3. clients (5 полей)
4. vehicles (7 полей)
5. subscriptions
6. parking_events
7. payments
8. logs

## ⚠️ Важно
- Убрать `SET session_replication_role` команды
- Убрать все DROP TABLE команды
- Оставить только CREATE TABLE и CREATE INDEX

## ✅ Acceptance Criteria
- Файл V1__initial_schema.sql создан
- Содержит все 8 существующих таблиц
- SQL синтаксис корректен
- Нет DROP команд

## ⏱️ Estimate
20 минут

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
## 🎯 Цель
Добавить таблицу `parking_lots` для управления несколькими парковками.

## 📝 Задачи
- [ ] Создать файл `V2__add_parking_lots.sql`
- [ ] Определить структуру таблицы parking_lots
- [ ] Добавить все необходимые поля
- [ ] Создать индексы для оптимизации
- [ ] Добавить constraints и проверки

## 📊 Структура таблицы
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

## 📇 Индексы
- idx_parking_lots_name
- idx_parking_lots_status
- idx_parking_lots_city

## ✅ Acceptance Criteria
- Файл V2__add_parking_lots.sql создан
- Таблица имеет все необходимые поля
- Индексы созданы
- Constraints добавлены (CHECK для status)

## ⏱️ Estimate
30 минут

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
## 🎯 Цель
Добавить таблицу `parking_spaces` для управления конкретными парковочными местами.

## 📝 Задачи
- [ ] Создать файл `V3__add_parking_spaces.sql`
- [ ] Определить структуру таблицы parking_spaces
- [ ] Добавить foreign key к parking_lots
- [ ] Создать индексы для оптимизации
- [ ] Добавить constraints для типов и статусов

## 📊 Структура таблицы
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

## 📇 Индексы
- idx_parking_spaces_lot
- idx_parking_spaces_status
- idx_parking_spaces_type
- idx_parking_spaces_lot_status (composite)

## ✅ Acceptance Criteria
- Файл V3__add_parking_spaces.sql создан
- Foreign key к parking_lots работает
- Unique constraint на (parking_lot_id, space_number)
- Все индексы созданы
- CHECK constraints для space_type и status

## ⏱️ Estimate
30 минут

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
## 🎯 Цель
Добавить таблицу `bookings` для системы бронирования парковочных мест.

## 📝 Задачи
- [ ] Создать файл `V4__add_bookings.sql`
- [ ] Определить структуру таблицы bookings
- [ ] Добавить foreign keys к clients, parking_spaces, vehicles
- [ ] Создать индексы для оптимизации поиска
- [ ] Добавить constraints для валидации

## 📊 Структура таблицы
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

## 📇 Индексы
- idx_bookings_client
- idx_bookings_space
- idx_bookings_vehicle
- idx_bookings_code (UNIQUE)
- idx_bookings_status
- idx_bookings_time (composite: start_time, end_time)
- idx_bookings_active (partial: WHERE status IN (...))

## ✅ Acceptance Criteria
- Файл V4__add_bookings.sql создан
- Foreign keys работают корректно
- Unique constraint на booking_code
- CHECK constraint на end_time > start_time
- Все индексы созданы, включая partial index

## ⏱️ Estimate
40 минут

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
## 🎯 Цель
Протестировать все миграции на чистой базе данных.

## 📝 Задачи
- [ ] Остановить все контейнеры
- [ ] Удалить volumes с данными БД
- [ ] Пересобрать сервисы с Maven
- [ ] Запустить контейнеры
- [ ] Проверить логи Flyway
- [ ] Проверить создание всех таблиц
- [ ] Проверить таблицу flyway_schema_history

## 🧪 Тестовые команды
```bash
# Остановить и очистить
docker-compose down -v

# Пересборка
cd backend/api-gateway
mvn clean package -DskipTests

# Запуск
cd ../../devops
docker-compose up -d

# Проверка логов
docker logs api-gateway 2>&1 | grep -i flyway

# Проверка БД
docker exec -it parking_db psql -U postgres -d parking_db -c "\dt"
docker exec -it parking_db psql -U postgres -d parking_db -c "SELECT * FROM flyway_schema_history;"
```

## ✅ Ожидаемый результат
```
Successfully validated 4 migrations
Creating Schema History table "public"."flyway_schema_history"
Migrating schema "public" to version "1 - initial schema"
Migrating schema "public" to version "2 - add parking lots"
Migrating schema "public" to version "3 - add parking spaces"
Migrating schema "public" to version "4 - add bookings"
Successfully applied 4 migrations to schema "public"
```

## 📊 Проверки
- [ ] Все 11 таблиц созданы (8 старых + 3 новых)
- [ ] flyway_schema_history содержит 4 записи
- [ ] Индексы созданы
- [ ] Foreign keys работают
- [ ] Сервис запускается без ошибок

## ✅ Acceptance Criteria
- Миграции применяются автоматически
- Все таблицы созданы
- Нет ошибок в логах
- flyway_schema_history содержит правильные записи

## ⏱️ Estimate
30 минут

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
## 🎯 Цель
Обновить документацию проекта с информацией о Flyway миграциях.

## 📝 Задачи
- [ ] Обновить `database/README.md`
- [ ] Добавить инструкцию по созданию новых миграций
- [ ] Документировать naming convention (V{number}__{description}.sql)
- [ ] Добавить примеры команд Flyway
- [ ] Обновить `docs/DEPLOYMENT_GUIDE.md` (если есть)

## 📄 Содержание README.md
```markdown
# Database Migrations

## Структура
- `V1__initial_schema.sql` - Начальная схема (8 таблиц)
- `V2__add_parking_lots.sql` - Таблица parking_lots
- `V3__add_parking_spaces.sql` - Таблица parking_spaces
- `V4__add_bookings.sql` - Таблица bookings

## Создание новой миграции
1. Создать файл: `V{N}__{description}.sql`
2. Написать SQL (только UP миграция)
3. Протестировать на dev окружении
4. Commit в git

## Naming Convention
- Префикс: `V` (версия)
- Номер: последовательный (1, 2, 3...)
- Разделитель: `__` (два подчеркивания)
- Описание: snake_case, краткое

Примеры:
- `V1__initial_schema.sql`
- `V2__add_parking_lots.sql`
- `V3__add_index_to_users.sql`
```

## ✅ Acceptance Criteria
- README.md обновлен
- Инструкция по созданию миграций добавлена
- Naming convention документирован
- Примеры команд добавлены

## ⏱️ Estimate
20 минут

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
## 🎯 Цель
Настроить Flyway для безопасного применения миграций в production.

## 📝 Задачи
- [ ] Создать `application-production.yml`
- [ ] Настроить строгую валидацию миграций
- [ ] Отключить baseline-on-migrate для production
- [ ] Настроить логирование миграций
- [ ] Добавить проверку checksums
- [ ] Документировать процесс деплоя

## ⚙️ Production конфигурация
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: false  # Отключить для production!
    validate-on-migrate: true
    out-of-order: false
    locations: classpath:db/migration
    schemas: public
    clean-disabled: true  # Защита от случайной очистки
```

## 📋 Production deployment процесс
1. Backup базы данных
2. Проверка миграций на staging
3. Применение миграций в production
4. Проверка статуса
5. Rollback plan (если нужно)

## ✅ Acceptance Criteria
- Production конфигурация создана
- Защита от clean операций включена
- Strict validation включена
- Deployment процесс документирован

## ⏱️ Estimate
30 минут

## 🏷️ Labels
`database`, `migration`, `production`, `priority-medium`

## 🔗 Dependencies
Requires: #6
```

---

## 📊 SUMMARY

### Всего Issues: 8

| # | Название | Estimate | Приоритет | Dependencies |
|---|----------|----------|-----------|--------------|
| 1 | Setup Flyway | 15 min | HIGH | - |
| 2 | V1 Initial Schema | 20 min | HIGH | #1 |
| 3 | V2 Parking Lots | 30 min | HIGH | #2 |
| 4 | V3 Parking Spaces | 30 min | HIGH | #3 |
| 5 | V4 Bookings | 40 min | HIGH | #4 |
| 6 | Test Migrations | 30 min | HIGH | #5 |
| 7 | Update Docs | 20 min | MEDIUM | #6 |
| 8 | Production Config | 30 min | MEDIUM | #6 |

### Общее время: 3 часа 35 минут

### Распределение по Kanban колонкам:

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

**🔨 In Progress:** (пусто)

**👀 Review:** (пусто)

**✅ Done:** (пусто)

---

## 🏷️ Labels для создания на GitHub

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

## 📝 Как использовать этот файл

### 1. Создание Issues на GitHub

Для каждой задачи (#1-#8):
1. Открыть GitHub → Issues → New issue
2. Скопировать Title
3. Скопировать Description
4. Добавить Labels
5. Assign себе (или разработчику)
6. Add to Project (Parking System Development)

### 2. Альтернатива - массовое создание через GitHub CLI

```bash
# Issue #1
gh issue create --title "[DB Migration] Setup Flyway dependency and configuration" \
  --body-file issue1.md \
  --label "database,migration,flyway,priority-high"

# Issue #2
gh issue create --title "[DB Migration] Create V1__initial_schema.sql migration" \
  --body-file issue2.md \
  --label "database,migration,sql,priority-high"

# ... и так далее
```

### 3. Автоматизация (опционально)

Можно создать PowerShell скрипт для автоматического создания всех issues.

---

## ✅ Чеклист перед началом

- [ ] Создать все Labels на GitHub
- [ ] Создать GitHub Project Board
- [ ] Создать все 8 Issues
- [ ] Добавить Issues в Project
- [ ] Установить dependencies между issues
- [ ] Назначить исполнителя
- [ ] Начать с Issue #1

---

**🎯 Готово к использованию! Можно создавать Issues на GitHub!**

