-- Phase 2 / Issue #24: create tariffs table and seed initial data
-- Idempotent: uses IF NOT EXISTS and ON CONFLICT

CREATE TABLE IF NOT EXISTS tariffs (
    id BIGSERIAL PRIMARY KEY,
    tariff_type VARCHAR(50) NOT NULL UNIQUE,
    hourly_rate DECIMAL(10,2) NOT NULL,
    daily_rate DECIMAL(10,2),
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tariffs_type_active ON tariffs (tariff_type, is_active);

INSERT INTO tariffs (tariff_type, hourly_rate, daily_rate, description, is_active)
VALUES
    ('ONE_TIME', 50.00, NULL, 'One-time parking tariff', TRUE),
    ('DAILY', 30.00, 500.00, 'Daily parking tariff', TRUE),
    ('NIGHT', 20.00, 300.00, 'Night parking tariff', TRUE),
    ('VIP', 0.00, 1500.00, 'VIP parking tariff', TRUE)
ON CONFLICT (tariff_type) DO NOTHING;

