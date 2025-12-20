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
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS logs;

-- Включение внешних ключей обратно
SET session_replication_role = 'origin';

-- ******************************************************
-- 2. СОЗДАНИЕ ТАБЛИЦ
-- ******************************************************

-- Таблица: USERS (Пользователи системы с полной системой безопасности)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Security Enhancement Fields
    account_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INTEGER DEFAULT 0,
    last_failed_login TIMESTAMP,
    lockout_time TIMESTAMP,
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    password_expires_at TIMESTAMP,
    last_login TIMESTAMP,
    last_logout TIMESTAMP,
    last_ip VARCHAR(45),
    device_fingerprint VARCHAR(255),
    session_id VARCHAR(255),
    session_expires_at TIMESTAMP,
    
    -- Account Verification
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    email_verification_expires_at TIMESTAMP,
    phone_number VARCHAR(20),
    phone_verified BOOLEAN DEFAULT FALSE,
    phone_verification_code VARCHAR(10),
    phone_verification_expires_at TIMESTAMP,
    
    -- Two-Factor Authentication
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    backup_codes TEXT[],
    
    -- Security Questions
    security_questions_enabled BOOLEAN DEFAULT FALSE,
    security_question_1 VARCHAR(500),
    security_answer_1_hash VARCHAR(255),
    security_question_2 VARCHAR(500),
    security_answer_2_hash VARCHAR(255),
    security_question_3 VARCHAR(500),
    security_answer_3_hash VARCHAR(255),
    
    -- Profile and Preferences
    profile_complete BOOLEAN DEFAULT FALSE,
    terms_accepted BOOLEAN DEFAULT FALSE,
    terms_accepted_at TIMESTAMP,
    privacy_policy_accepted BOOLEAN DEFAULT FALSE,
    privacy_policy_accepted_at TIMESTAMP,
    
    -- Account Management
    account_disabled_reason VARCHAR(500),
    last_password_reset TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_expires_at TIMESTAMP,
    login_attempts_today INTEGER DEFAULT 0,
    last_login_attempt_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица: USER_ROLES (Роли пользователей)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

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
-- 3. СОЗДАНИЕ ИНДЕКСОВ
-- ******************************************************

-- Индексы для таблицы users (Enhanced Security)
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_session_id ON users(session_id);
CREATE INDEX idx_users_last_login ON users(last_login);
CREATE INDEX idx_users_account_locked ON users(account_locked);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_users_email_verification_token ON users(email_verification_token);
CREATE INDEX idx_users_password_reset_token ON users(password_reset_token);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- Индексы для таблицы clients
CREATE INDEX idx_clients_phone ON clients(phone_number);
CREATE INDEX idx_clients_email ON clients(email);

-- Индексы для быстрого поиска транспорта по номерам
CREATE INDEX idx_vehicles_plate ON vehicles(license_plate);
CREATE INDEX idx_vehicles_client ON vehicles(client_id);

-- Индексы для событий парковки (быстрый поиск по времени)
CREATE INDEX idx_parking_entry_time ON parking_events(entry_time);
CREATE INDEX idx_parking_exit_time ON parking_events(exit_time);
CREATE INDEX idx_parking_vehicle_id ON parking_events(vehicle_id);

-- Индексы для абонементов
CREATE INDEX idx_subscriptions_client ON subscriptions(client_id);
CREATE INDEX idx_subscriptions_start_date ON subscriptions(start_date);
CREATE INDEX idx_subscriptions_end_date ON subscriptions(end_date);

-- Индексы для платежей
CREATE INDEX idx_payments_event ON payments(parking_event_id);
CREATE INDEX idx_payments_amount ON payments(amount);

-- Индекс для логов по времени
CREATE INDEX idx_logs_timestamp ON logs(timestamp);

-- ******************************************************
-- 4. ИНИЦИАЛИЗАЦИЯ ДАННЫМИ
-- ******************************************************

-- Инициализация пользователей системы с полной безопасностью
-- Пароли: admin - parking123, user - user123, manager - manager123 (BCrypt hash с силой 12)
INSERT INTO users (
    username, password, password_hash, email, first_name, last_name, enabled,
    account_locked, failed_login_attempts, password_changed_at, password_expires_at,
    email_verified, phone_verified, profile_complete, terms_accepted, terms_accepted_at,
    privacy_policy_accepted, privacy_policy_accepted_at, login_attempts_today, last_login_attempt_reset
) VALUES
(
    'admin', 
    '$2a$12$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG', 
    '$2a$12$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG',
    'admin@parking.com', 'System', 'Administrator', true,
    false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days',
    true, false, true, true, CURRENT_TIMESTAMP,
    true, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP
),
(
    'user', 
    '$2a$12$WZBJOo6VkVqSN6S9JWtb6upEeF8VqAGsKuGPEv.9M8VpEhZFqiZFO', 
    '$2a$12$WZBJOo6VkVqSN6S9JWtb6upEeF8VqAGsKuGPEv.9M8VpEhZFqiZFO',
    'user@parking.com', 'Test', 'User', true,
    false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days',
    true, false, false, true, CURRENT_TIMESTAMP,
    true, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP
),
(
    'manager', 
    '$2a$12$MkRWLQUW6uJhVFhJfTw7OuGKkjxWHJ8K.0zNZRv8KZGKzrn8hgGBu', 
    '$2a$12$MkRWLQUW6uJhVFhJfTw7OuGKkjxWHJ8K.0zNZRv8KZGKzrn8hgGBu',
    'manager@parking.com', 'Parking', 'Manager', true,
    false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '90 days',
    true, false, false, true, CURRENT_TIMESTAMP,
    true, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP
);

-- Роли пользователей
INSERT INTO user_roles (user_id, role) VALUES
((SELECT id FROM users WHERE username = 'admin'), 'ADMIN'),
((SELECT id FROM users WHERE username = 'admin'), 'USER'),
((SELECT id FROM users WHERE username = 'user'), 'USER'),
((SELECT id FROM users WHERE username = 'manager'), 'MANAGER'),
((SELECT id FROM users WHERE username = 'manager'), 'USER');

-- Инициализация тестовых данных для парковки
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
