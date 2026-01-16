-- Phase 2 / Issue #25: Extend PARKING_EVENTS and PAYMENTS tables
-- Adds new columns and constraints according to ER diagram requirements

-- ============================================================
-- SECTION 1: EXTEND PARKING_EVENTS TABLE
-- ============================================================

-- Add new columns to parking_events
ALTER TABLE parking_events
    ADD COLUMN IF NOT EXISTS license_plate VARCHAR(20),
    ADD COLUMN IF NOT EXISTS entry_method VARCHAR(20),
    ADD COLUMN IF NOT EXISTS exit_method VARCHAR(20),
    ADD COLUMN IF NOT EXISTS is_subscriber BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Make license_plate NOT NULL (after adding column with data migration if needed)
-- Note: In production, you would first populate license_plate from vehicles table before making it NOT NULL
-- For now, we'll update existing records to have license_plate from vehicles
UPDATE parking_events pe
SET license_plate = v.license_plate
FROM vehicles v
WHERE pe.vehicle_id = v.id
  AND pe.license_plate IS NULL;

-- Now make it NOT NULL
ALTER TABLE parking_events
    ALTER COLUMN license_plate SET NOT NULL;

-- Add CHECK constraints for entry_method and exit_method
ALTER TABLE parking_events
    ADD CONSTRAINT chk_entry_method CHECK (entry_method IN ('SCAN', 'MANUAL') OR entry_method IS NULL),
    ADD CONSTRAINT chk_exit_method CHECK (exit_method IN ('SCAN', 'MANUAL', 'AUTO') OR exit_method IS NULL);

-- Modify vehicle_id to allow NULL (for guests without vehicle record)
ALTER TABLE parking_events
    ALTER COLUMN vehicle_id DROP NOT NULL;

-- Drop old FK constraint and recreate with ON DELETE SET NULL
ALTER TABLE parking_events
    DROP CONSTRAINT IF EXISTS fk_event_vehicle;

ALTER TABLE parking_events
    ADD CONSTRAINT fk_event_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles (id)
        ON DELETE SET NULL;

-- Add FK to parking_spaces (spot_id)
ALTER TABLE parking_events
    DROP CONSTRAINT IF EXISTS fk_event_spot;

ALTER TABLE parking_events
    ADD CONSTRAINT fk_event_spot
        FOREIGN KEY (spot_id)
        REFERENCES parking_spaces (id)
        ON DELETE SET NULL;

-- Set default for entry_time (already NOT NULL in V1)
ALTER TABLE parking_events
    ALTER COLUMN entry_time SET DEFAULT CURRENT_TIMESTAMP;

-- Create indexes for parking_events
CREATE INDEX IF NOT EXISTS idx_parking_events_ticket ON parking_events (ticket_code);
CREATE INDEX IF NOT EXISTS idx_parking_events_entry_time ON parking_events (entry_time);
CREATE INDEX IF NOT EXISTS idx_parking_events_license ON parking_events (license_plate);

-- ============================================================
-- SECTION 2: EXTEND PAYMENTS TABLE
-- ============================================================

-- Add new columns to payments
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'COMPLETED',
    ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS operator_id BIGINT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add CHECK constraint for payment_method
ALTER TABLE payments
    DROP CONSTRAINT IF EXISTS chk_payment_method;

ALTER TABLE payments
    ADD CONSTRAINT chk_payment_method CHECK (payment_method IN ('CARD', 'CASH', 'MOBILE_PAY'));

-- Add CHECK constraint for status
ALTER TABLE payments
    ADD CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'));

-- Add CHECK constraint for amount >= 0
ALTER TABLE payments
    ADD CONSTRAINT chk_payment_amount CHECK (amount >= 0);

-- Add UNIQUE constraint on transaction_id
ALTER TABLE payments
    ADD CONSTRAINT uniq_transaction_id UNIQUE (transaction_id);

-- Drop old UNIQUE constraint on parking_event_id (allow multiple payments per event, but only one COMPLETED)
ALTER TABLE payments
    DROP CONSTRAINT IF EXISTS payments_parking_event_id_key;

-- Add FK to users (operator_id) with ON DELETE SET NULL
ALTER TABLE payments
    ADD CONSTRAINT fk_payment_operator
        FOREIGN KEY (operator_id)
        REFERENCES users (id)
        ON DELETE SET NULL;

-- Recreate FK to parking_events with ON DELETE CASCADE
ALTER TABLE payments
    DROP CONSTRAINT IF EXISTS fk_payment_event;

ALTER TABLE payments
    ADD CONSTRAINT fk_payment_event
        FOREIGN KEY (parking_event_id)
        REFERENCES parking_events (id)
        ON DELETE CASCADE;

-- Create indexes for payments
CREATE INDEX IF NOT EXISTS idx_payments_parking_event ON payments (parking_event_id);
CREATE INDEX IF NOT EXISTS idx_payments_transaction ON payments (transaction_id);

-- Add UNIQUE constraint: only one COMPLETED payment per parking_event_id
-- Using partial index for this constraint (PostgreSQL feature)
CREATE UNIQUE INDEX IF NOT EXISTS uniq_completed_payment_per_event
    ON payments (parking_event_id)
    WHERE status = 'COMPLETED';

-- ============================================================
-- SECTION 3: CLEANUP OLD COLUMNS (OPTIONAL)
-- ============================================================

-- Remove is_paid column from parking_events (now tracked in payments table)
-- Commented out for safety - can be done in a future migration after verification
-- ALTER TABLE parking_events DROP COLUMN IF EXISTS is_paid;

-- ============================================================
-- SECTION 4: COMMENTS FOR DOCUMENTATION
-- ============================================================

COMMENT ON TABLE parking_events IS 'Parking session records with entry/exit tracking';
COMMENT ON COLUMN parking_events.license_plate IS 'Vehicle license plate (required for all events)';
COMMENT ON COLUMN parking_events.ticket_code IS 'Unique ticket code for one-time visitors';
COMMENT ON COLUMN parking_events.entry_method IS 'Method of entry: SCAN or MANUAL';
COMMENT ON COLUMN parking_events.exit_method IS 'Method of exit: SCAN, MANUAL, or AUTO';
COMMENT ON COLUMN parking_events.is_subscriber IS 'TRUE if vehicle has active subscription';

COMMENT ON TABLE payments IS 'Payment records for parking events';
COMMENT ON COLUMN payments.status IS 'Payment status: PENDING, COMPLETED, FAILED, or REFUNDED';
COMMENT ON COLUMN payments.transaction_id IS 'External payment system transaction ID';
COMMENT ON COLUMN payments.operator_id IS 'User who processed the payment (if manual)';

