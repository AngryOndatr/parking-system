-- ******************************************************
-- Migration: V11__add_parking_space_to_subscription.sql
-- Purpose: Link subscriptions to reserved parking spaces
-- Database: PostgreSQL
-- Created: 2026-03-30
-- ******************************************************

-- Add FK column: nullable — not every subscription reserves a specific spot
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS parking_space_id BIGINT;

-- Foreign key to parking_spaces
ALTER TABLE subscriptions
    ADD CONSTRAINT fk_subscription_space
        FOREIGN KEY (parking_space_id)
        REFERENCES parking_spaces(id)
        ON DELETE SET NULL;

-- Index for quick lookup of reservations per space
CREATE INDEX IF NOT EXISTS idx_subscriptions_space ON subscriptions(parking_space_id);

