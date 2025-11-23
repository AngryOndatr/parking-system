-- ******************************************************
-- Файл: init.sql
-- Назначение: Инициализация схемы базы данных для Parking Lot Management System
-- База данных: PostgreSQL
-- ******************************************************

-- Отключение внешних ключей на время очистки, если схема уже существует
-- Это полезно для повторного запуска скрипта (например, в тестовом режиме)
SET session_replication_role = 'replica';

-- 1. Очистка (DROP) существующих таблиц
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS parking_events;
DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS clients;
-- DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS logs;

-- Включение внешних ключей обратно
SET session_replication_role = 'origin';

-- ******************************************************
-- 2. СОЗДАНИЕ ТАБЛИЦ (JPA-сущности из parking-common)
-- ******************************************************

-- Таблица: CLIENTS (Клиенты - владельцы абонементов)
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    registered_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- Таблица: VEHICLES (Транспортные средства)
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(50) UNIQUE NOT NULL,
    client_id BIGINT,
    is_allowed BOOLEAN NOT NULL DEFAULT TRUE,

    -- Внешний ключ на таблицу CLIENTS
    CONSTRAINT fk_vehicle_client
        FOREIGN KEY (client_id)
        REFERENCES clients (id)
);

-- Таблица: SUBSCRIPTIONS (Абонементы)
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type VARCHAR(50) NOT NULL, -- например, 'MONTHLY', 'ANNUAL', 'DAY_TIME'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Внешний ключ на таблицу CLIENTS
    CONSTRAINT fk_subscription_client
        FOREIGN KEY (client_id)
        REFERENCES clients (id)
);

-- ******************************************************
-- 3. СОЗДАНИЕ ТАБЛИЦ (Специфичные для микросервисов)
-- ******************************************************

-- Таблица: PARKING_EVENTS (Журнал въездов/выездов)
CREATE TABLE parking_events (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    entry_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    exit_time TIMESTAMP WITHOUT TIME ZONE,
    spot_id BIGINT, -- Если используем резервирование конкретного места
    ticket_code VARCHAR(100) UNIQUE, -- Для разовых посетителей
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,

    -- Внешний ключ на таблицу VEHICLES
    CONSTRAINT fk_event_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles (id)
);

-- Таблица: PAYMENTS (Учет оплаты разовой парковки)
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    parking_event_id BIGINT UNIQUE NOT NULL, -- Одно событие - одна оплата
    amount NUMERIC(10, 2) NOT NULL,
    payment_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- 'CASH', 'CARD'

    -- Внешний ключ на событие парковки
    CONSTRAINT fk_payment_event
        FOREIGN KEY (parking_event_id)
        REFERENCES parking_events (id)
);

-- Таблица: USERS (Администраторы и операторы системы)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL -- 'ADMIN', 'OPERATOR'
);

-- Таблица: LOGS (Журнал системных действий и действий операторов)
CREATE TABLE logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    log_level VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    user_id BIGINT,

    -- Внешний ключ на пользователя (если действие совершено оператором)
    CONSTRAINT fk_log_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);


-- ******************************************************
-- 4. ИНИЦИАЛИЗАЦИЯ ДАННЫМИ (Для тестирования)
-- ******************************************************

-- Тестовые клиенты
INSERT INTO clients (full_name, phone_number, email, registered_at) VALUES
('Абонент Тест', '+380501112233', 'test@parking.com', NOW()),
('Разовый Гость', '+380679998877', 'guest@parking.com', NOW());

-- Тестовые автомобили
-- ID 1: Привязан к абоненту (для Сценария 1)
INSERT INTO vehicles (client_id, license_plate, is_allowed) VALUES
(1, 'AA1234BB', TRUE),
(2, 'BB5678CC', TRUE);

-- Тестовый абонемент (действителен год)
INSERT INTO subscriptions (client_id, start_date, end_date, type, is_active) VALUES
(1, NOW(), NOW() + INTERVAL '1 year', 'ANNUAL', TRUE);

-- Тестовые пользователи системы
INSERT INTO users (username, password_hash, user_role) VALUES
('admin_user', 'hashed_password_admin', 'ADMIN'),
('operator_1', 'hashed_password_oper', 'OPERATOR');
