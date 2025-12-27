-- ******************************************************
-- Migration: V1__initial_schema.sql
-- Purpose: Initial database schema for Parking System
-- Database: PostgreSQL
-- Created: 2025-12-26
-- ******************************************************

-- ============================================================
-- SECTION 1: CORE TABLES
-- ============================================================

-- -----------------------------------------------------
-- Table: USERS
-- Purpose: User accounts with security features
-- Entity: UserSecurityEntity.java
-- -----------------------------------------------------
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    user_role VARCHAR(20) NOT NULL DEFAULT 'USER',

    -- Account Status
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,

    -- Password Security
    password_reset_token VARCHAR(255),
    password_reset_expires_at TIMESTAMP,
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    force_password_change BOOLEAN NOT NULL DEFAULT FALSE,

    -- Brute Force Protection
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    account_locked_until TIMESTAMP,

    -- Login Tracking
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(255),
    current_login_at TIMESTAMP,
    current_login_ip VARCHAR(255),
    login_count INTEGER NOT NULL DEFAULT 0,

    -- Two-Factor Authentication
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_secret VARCHAR(255),

    -- Session Management
    active_sessions_limit INTEGER NOT NULL DEFAULT 3,
    last_password_check_at TIMESTAMP,

    -- Security Questions
    security_question_hash VARCHAR(255),
    security_answer_hash VARCHAR(255),

    -- Terms & Privacy
    terms_accepted_at TIMESTAMP,
    privacy_accepted_at TIMESTAMP,

    -- Additional Metadata
    user_agent_hash VARCHAR(255),
    preferred_language VARCHAR(255) DEFAULT 'en',
    timezone VARCHAR(255) DEFAULT 'UTC',
    avatar_url VARCHAR(255),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Soft Delete Support
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT fk_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(id),

    -- Role validation constraint
    CONSTRAINT chk_user_role CHECK (user_role IN ('USER', 'MANAGER', 'ADMIN', 'OPERATOR', 'SECURITY_ADMIN'))
);

-- -----------------------------------------------------
-- Table: USER_BACKUP_CODES
-- Purpose: Two-Factor Authentication backup codes
-- -----------------------------------------------------
CREATE TABLE user_backup_codes (
    user_id BIGINT NOT NULL,
    backup_code VARCHAR(255) NOT NULL,
    CONSTRAINT fk_backup_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- SECTION 2: PARKING BUSINESS TABLES
-- ============================================================

-- -----------------------------------------------------
-- Table: CLIENTS
-- Purpose: Subscription holders
-- -----------------------------------------------------
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    registered_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- -----------------------------------------------------
-- Table: VEHICLES
-- Purpose: Vehicle registry with client association
-- -----------------------------------------------------
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(50) UNIQUE NOT NULL,
    client_id BIGINT,
    is_allowed BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_vehicle_client
        FOREIGN KEY (client_id)
        REFERENCES clients (id)
);

-- -----------------------------------------------------
-- Table: SUBSCRIPTIONS
-- Purpose: Parking subscriptions management
-- -----------------------------------------------------
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'MONTHLY', 'ANNUAL', 'DAY_TIME'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_subscription_client
        FOREIGN KEY (client_id)
        REFERENCES clients (id)
);

-- ============================================================
-- SECTION 3: EVENT TRACKING TABLES
-- ============================================================

-- -----------------------------------------------------
-- Table: PARKING_EVENTS
-- Purpose: Entry/exit log for all vehicles
-- -----------------------------------------------------
CREATE TABLE parking_events (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    entry_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    exit_time TIMESTAMP WITHOUT TIME ZONE,
    spot_id BIGINT, -- Reserved spot ID (if applicable)
    ticket_code VARCHAR(100) UNIQUE, -- For one-time visitors
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_event_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles (id)
);

-- -----------------------------------------------------
-- Table: PAYMENTS
-- Purpose: Payment records for parking events
-- -----------------------------------------------------
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    parking_event_id BIGINT UNIQUE NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    payment_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- 'CASH', 'CARD'

    CONSTRAINT fk_payment_event
        FOREIGN KEY (parking_event_id)
        REFERENCES parking_events (id)
);

-- -----------------------------------------------------
-- Table: LOGS
-- Purpose: System and operator action audit log
-- -----------------------------------------------------
CREATE TABLE logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    log_level VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    user_id BIGINT,

    CONSTRAINT fk_log_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

-- ============================================================
-- SECTION 4: INDEXES FOR PERFORMANCE
-- ============================================================

-- Indexes for USERS table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(user_role);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_users_account_non_locked ON users(account_non_locked);
CREATE INDEX idx_users_last_login_at ON users(last_login_at);
CREATE INDEX idx_users_email_verified ON users(email_verified);
CREATE INDEX idx_users_password_reset_token ON users(password_reset_token);

-- Indexes for USER_BACKUP_CODES
CREATE INDEX idx_backup_codes_user_id ON user_backup_codes(user_id);

-- Indexes for CLIENTS table
CREATE INDEX idx_clients_phone ON clients(phone_number);
CREATE INDEX idx_clients_email ON clients(email);

-- Indexes for VEHICLES table
CREATE INDEX idx_vehicles_plate ON vehicles(license_plate);
CREATE INDEX idx_vehicles_client ON vehicles(client_id);

-- Indexes for PARKING_EVENTS table
CREATE INDEX idx_parking_entry_time ON parking_events(entry_time);
CREATE INDEX idx_parking_exit_time ON parking_events(exit_time);
CREATE INDEX idx_parking_vehicle_id ON parking_events(vehicle_id);

-- Indexes for SUBSCRIPTIONS table
CREATE INDEX idx_subscriptions_client ON subscriptions(client_id);
CREATE INDEX idx_subscriptions_start_date ON subscriptions(start_date);
CREATE INDEX idx_subscriptions_end_date ON subscriptions(end_date);

-- Indexes for PAYMENTS table
CREATE INDEX idx_payments_event ON payments(parking_event_id);
CREATE INDEX idx_payments_amount ON payments(amount);

-- Indexes for LOGS table
CREATE INDEX idx_logs_timestamp ON logs(timestamp);

-- ============================================================
-- MIGRATION COMPLETE
-- ============================================================
-- Schema Version: V1
-- Tables Created: 8 (users, user_backup_codes, clients, vehicles,
--                    subscriptions, parking_events, payments, logs)
-- Indexes Created: 23
-- ============================================================

