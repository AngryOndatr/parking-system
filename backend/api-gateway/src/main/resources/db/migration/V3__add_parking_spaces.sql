-- ******************************************************
-- Migration: V3__add_parking_spaces.sql
-- Purpose: Add parking_spaces table for managing individual parking spaces
-- Database: PostgreSQL
-- Created: 2025-12-26
-- Issue: #5
-- ******************************************************

-- ============================================================
-- SECTION 1: PARKING_SPACES TABLE
-- ============================================================

-- -----------------------------------------------------
-- Table: PARKING_SPACES
-- Purpose: Manage individual parking spaces within parking lots
-- Features: Space identification, type management,
--           status tracking, EV charging support, pricing overrides
-- -----------------------------------------------------
CREATE TABLE parking_spaces (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Foreign Key to parking_lots
    parking_lot_id BIGINT NOT NULL,

    -- Identification
    space_number VARCHAR(20) NOT NULL,
    floor_level INTEGER DEFAULT 0,
    section VARCHAR(50),

    -- Space Type
    space_type VARCHAR(50) DEFAULT 'STANDARD',

    -- Status
    status VARCHAR(20) DEFAULT 'AVAILABLE',

    -- Electric Charging
    has_charger BOOLEAN DEFAULT FALSE,
    charger_type VARCHAR(50),

    -- Dimensions (in centimeters)
    length_cm INTEGER,
    width_cm INTEGER,

    -- Pricing Overrides (optional - overrides parking lot default rates)
    hourly_rate_override DECIMAL(10,2),
    daily_rate_override DECIMAL(10,2),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_occupied_at TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_parking_space_lot
        FOREIGN KEY (parking_lot_id)
        REFERENCES parking_lots(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_parking_space_number
        UNIQUE (parking_lot_id, space_number),

    CONSTRAINT chk_parking_space_type
        CHECK (space_type IN ('STANDARD', 'HANDICAPPED', 'ELECTRIC', 'VIP', 'COMPACT', 'OVERSIZED')),

    CONSTRAINT chk_parking_space_status
        CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE', 'OUT_OF_SERVICE')),

    CONSTRAINT chk_parking_space_floor
        CHECK (floor_level >= -10 AND floor_level <= 50),

    CONSTRAINT chk_parking_space_dimensions
        CHECK (
            (length_cm IS NULL AND width_cm IS NULL) OR
            (length_cm > 0 AND width_cm > 0)
        ),

    CONSTRAINT chk_parking_space_charger
        CHECK (
            (has_charger = FALSE AND charger_type IS NULL) OR
            (has_charger = TRUE AND charger_type IS NOT NULL)
        ),

    CONSTRAINT chk_parking_space_hourly_rate
        CHECK (hourly_rate_override IS NULL OR hourly_rate_override >= 0),

    CONSTRAINT chk_parking_space_daily_rate
        CHECK (daily_rate_override IS NULL OR daily_rate_override >= 0)
);

-- ============================================================
-- SECTION 2: INDEXES FOR OPTIMIZATION
-- ============================================================

-- Index for finding all spaces in a parking lot
CREATE INDEX idx_parking_spaces_lot ON parking_spaces(parking_lot_id);

-- Index for filtering by status
CREATE INDEX idx_parking_spaces_status ON parking_spaces(status);

-- Index for filtering by space type
CREATE INDEX idx_parking_spaces_type ON parking_spaces(space_type);

-- Composite index for finding available spaces in a specific lot
CREATE INDEX idx_parking_spaces_lot_status ON parking_spaces(parking_lot_id, status)
WHERE status IN ('AVAILABLE', 'RESERVED');

-- Index for finding spaces with EV chargers
CREATE INDEX idx_parking_spaces_charger ON parking_spaces(parking_lot_id, has_charger)
WHERE has_charger = TRUE;

-- Index for section-based searches
CREATE INDEX idx_parking_spaces_section ON parking_spaces(parking_lot_id, section)
WHERE section IS NOT NULL;

-- Index for floor-level searches
CREATE INDEX idx_parking_spaces_floor ON parking_spaces(parking_lot_id, floor_level);

-- ============================================================
-- SECTION 3: COMMENTS
-- ============================================================

-- Table comment
COMMENT ON TABLE parking_spaces IS 'Individual parking spaces within parking facilities';

-- Column comments
COMMENT ON COLUMN parking_spaces.id IS 'Unique identifier for parking space';
COMMENT ON COLUMN parking_spaces.parking_lot_id IS 'Reference to the parent parking lot';
COMMENT ON COLUMN parking_spaces.space_number IS 'Space number/identifier (e.g., A-101, B-25)';
COMMENT ON COLUMN parking_spaces.floor_level IS 'Floor level: 0=ground, negative=underground, positive=above ground';
COMMENT ON COLUMN parking_spaces.section IS 'Section/zone within parking lot (e.g., North, A, VIP)';
COMMENT ON COLUMN parking_spaces.space_type IS 'Type: STANDARD, HANDICAPPED, ELECTRIC, VIP, COMPACT, OVERSIZED';
COMMENT ON COLUMN parking_spaces.status IS 'Status: AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, OUT_OF_SERVICE';
COMMENT ON COLUMN parking_spaces.has_charger IS 'Indicates if space has EV charging station';
COMMENT ON COLUMN parking_spaces.charger_type IS 'Type of charger (e.g., Level2, DC-Fast, Tesla)';
COMMENT ON COLUMN parking_spaces.length_cm IS 'Length of parking space in centimeters';
COMMENT ON COLUMN parking_spaces.width_cm IS 'Width of parking space in centimeters';
COMMENT ON COLUMN parking_spaces.hourly_rate_override IS 'Override hourly rate for this specific space';
COMMENT ON COLUMN parking_spaces.daily_rate_override IS 'Override daily rate for this specific space';
COMMENT ON COLUMN parking_spaces.last_occupied_at IS 'Timestamp when space was last occupied';

-- ============================================================
-- MIGRATION COMPLETE
-- ============================================================
-- Schema Version: V3
-- Table Created: parking_spaces
-- Foreign Keys: 1 (parking_lots CASCADE)
-- Indexes Created: 7
-- Constraints: 8 CHECK constraints + 1 UNIQUE + 1 FK
-- ============================================================

