-- ******************************************************
-- Migration: V4__add_bookings.sql
-- Purpose: Add bookings table for parking space reservation system
-- Database: PostgreSQL
-- Created: 2025-12-26
-- Issue: #6
-- ******************************************************

-- ============================================================
-- SECTION 1: BOOKINGS TABLE
-- ============================================================

-- -----------------------------------------------------
-- Table: BOOKINGS
-- Purpose: Manage parking space reservations and bookings
-- Features: Client reservations, vehicle tracking, pricing,
--           payment integration, check-in/out, cancellation handling
-- -----------------------------------------------------
CREATE TABLE bookings (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Foreign Keys
    client_id BIGINT NOT NULL,
    parking_space_id BIGINT NOT NULL,
    vehicle_id BIGINT,

    -- Booking Details
    booking_code VARCHAR(20) UNIQUE NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,

    -- Status
    status VARCHAR(20) DEFAULT 'PENDING',

    -- Pricing
    estimated_cost DECIMAL(10,2),
    final_cost DECIMAL(10,2),

    -- Payment
    payment_id BIGINT,
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

    -- Special Requirements
    notes TEXT,
    special_requirements TEXT,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_booking_client
        FOREIGN KEY (client_id)
        REFERENCES clients(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_booking_space
        FOREIGN KEY (parking_space_id)
        REFERENCES parking_spaces(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_booking_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_booking_time
        CHECK (end_time > start_time),

    CONSTRAINT chk_booking_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'EXPIRED')),

    CONSTRAINT chk_booking_estimated_cost
        CHECK (estimated_cost IS NULL OR estimated_cost >= 0),

    CONSTRAINT chk_booking_final_cost
        CHECK (final_cost IS NULL OR final_cost >= 0),

    CONSTRAINT chk_booking_prepaid_amount
        CHECK (prepaid_amount IS NULL OR prepaid_amount >= 0),

    CONSTRAINT chk_booking_refund_amount
        CHECK (refund_amount IS NULL OR refund_amount >= 0),

    CONSTRAINT chk_booking_prepaid
        CHECK (
            (prepaid = FALSE AND prepaid_amount IS NULL) OR
            (prepaid = TRUE AND prepaid_amount IS NOT NULL AND prepaid_amount > 0)
        ),

    CONSTRAINT chk_booking_checked_in
        CHECK (checked_in_at IS NULL OR checked_in_at >= start_time),

    CONSTRAINT chk_booking_checked_out
        CHECK (
            checked_out_at IS NULL OR
            (checked_in_at IS NOT NULL AND checked_out_at >= checked_in_at)
        ),

    CONSTRAINT chk_booking_cancelled
        CHECK (
            (cancelled_at IS NULL AND cancelled_by IS NULL AND cancellation_reason IS NULL) OR
            (cancelled_at IS NOT NULL AND cancelled_by IS NOT NULL)
        )
);

-- ============================================================
-- SECTION 2: INDEXES FOR OPTIMIZATION
-- ============================================================

-- Index for finding all bookings by client
CREATE INDEX idx_bookings_client ON bookings(client_id);

-- Index for finding all bookings for a parking space
CREATE INDEX idx_bookings_space ON bookings(parking_space_id);

-- Index for finding bookings by vehicle
CREATE INDEX idx_bookings_vehicle ON bookings(vehicle_id)
WHERE vehicle_id IS NOT NULL;

-- Unique index for booking code lookups
CREATE UNIQUE INDEX idx_bookings_code ON bookings(booking_code);

-- Index for filtering by status
CREATE INDEX idx_bookings_status ON bookings(status);

-- Composite index for time-based queries
CREATE INDEX idx_bookings_time ON bookings(start_time, end_time);

-- Partial index for active bookings
CREATE INDEX idx_bookings_active ON bookings(parking_space_id, start_time, end_time)
WHERE status IN ('PENDING', 'CONFIRMED', 'ACTIVE');

-- Index for payment-related queries
CREATE INDEX idx_bookings_payment ON bookings(payment_id)
WHERE payment_id IS NOT NULL;

-- Composite index for space availability checks
CREATE INDEX idx_bookings_space_time ON bookings(parking_space_id, start_time, end_time, status);

-- Index for cancelled bookings
CREATE INDEX idx_bookings_cancelled ON bookings(cancelled_at)
WHERE cancelled_at IS NOT NULL;

-- ============================================================
-- SECTION 3: COMMENTS
-- ============================================================

-- Table comment
COMMENT ON TABLE bookings IS 'Parking space reservations and bookings';

-- Column comments
COMMENT ON COLUMN bookings.id IS 'Unique identifier for booking';
COMMENT ON COLUMN bookings.client_id IS 'Reference to the client making the booking';
COMMENT ON COLUMN bookings.parking_space_id IS 'Reference to the reserved parking space';
COMMENT ON COLUMN bookings.vehicle_id IS 'Reference to the vehicle (optional)';
COMMENT ON COLUMN bookings.booking_code IS 'Unique booking code for reference';
COMMENT ON COLUMN bookings.start_time IS 'Booking start date and time';
COMMENT ON COLUMN bookings.end_time IS 'Booking end date and time';
COMMENT ON COLUMN bookings.status IS 'Status: PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED, NO_SHOW, EXPIRED';
COMMENT ON COLUMN bookings.estimated_cost IS 'Estimated cost calculated at booking time';
COMMENT ON COLUMN bookings.final_cost IS 'Final cost after completion';
COMMENT ON COLUMN bookings.payment_id IS 'Reference to payment record';
COMMENT ON COLUMN bookings.prepaid IS 'Indicates if booking was prepaid';
COMMENT ON COLUMN bookings.prepaid_amount IS 'Amount paid in advance';
COMMENT ON COLUMN bookings.checked_in_at IS 'Actual check-in timestamp';
COMMENT ON COLUMN bookings.checked_out_at IS 'Actual check-out timestamp';
COMMENT ON COLUMN bookings.cancelled_at IS 'Cancellation timestamp';
COMMENT ON COLUMN bookings.cancelled_by IS 'Who cancelled (CLIENT, ADMIN, SYSTEM)';
COMMENT ON COLUMN bookings.cancellation_reason IS 'Reason for cancellation';
COMMENT ON COLUMN bookings.refund_amount IS 'Amount refunded after cancellation';
COMMENT ON COLUMN bookings.notes IS 'Internal notes';
COMMENT ON COLUMN bookings.special_requirements IS 'Special requirements from client';

-- ============================================================
-- MIGRATION COMPLETE
-- ============================================================
-- Schema Version: V4
-- Table Created: bookings
-- Foreign Keys: 3 (clients RESTRICT, parking_spaces RESTRICT, vehicles SET NULL)
-- Indexes Created: 10
-- Constraints: 9 CHECK constraints + 1 UNIQUE + 3 FK
-- ============================================================

