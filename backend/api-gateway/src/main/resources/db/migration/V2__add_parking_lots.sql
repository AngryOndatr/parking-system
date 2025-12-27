-- ******************************************************
-- Migration: V2__add_parking_lots.sql
-- Purpose: Add parking_lots table for managing multiple parking facilities
-- Database: PostgreSQL
-- Created: 2025-12-26
-- Issue: #4
-- ******************************************************

-- ============================================================
-- SECTION 1: PARKING_LOTS TABLE
-- ============================================================

-- -----------------------------------------------------
-- Table: PARKING_LOTS
-- Purpose: Manage multiple parking facilities/locations
-- Features: Location tracking, capacity management,
--           operation hours, status monitoring
-- -----------------------------------------------------
CREATE TABLE parking_lots (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Basic Information
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    description TEXT,

    -- Location Information
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    city VARCHAR(100),
    postal_code VARCHAR(20),

    -- Capacity Management
    total_spaces INTEGER NOT NULL DEFAULT 0,
    available_spaces INTEGER NOT NULL DEFAULT 0,

    -- Operation Hours
    opens_at TIME,
    closes_at TIME,
    is_24_hours BOOLEAN DEFAULT FALSE,

    -- Contact Information
    phone VARCHAR(50),
    email VARCHAR(100),

    -- Status Management
    status VARCHAR(20) DEFAULT 'ACTIVE',

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_parking_lot_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'FULL')),
    CONSTRAINT chk_parking_lot_spaces CHECK (available_spaces >= 0 AND available_spaces <= total_spaces),
    CONSTRAINT chk_parking_lot_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT chk_parking_lot_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

-- ============================================================
-- SECTION 2: INDEXES FOR OPTIMIZATION
-- ============================================================

-- Index for searching by name
CREATE INDEX idx_parking_lots_name ON parking_lots(name);

-- Index for filtering by status
CREATE INDEX idx_parking_lots_status ON parking_lots(status);

-- Index for searching by city
CREATE INDEX idx_parking_lots_city ON parking_lots(city);

-- Index for geolocation queries (if using spatial searches in future)
CREATE INDEX idx_parking_lots_location ON parking_lots(latitude, longitude);

-- Index for finding available parking lots
CREATE INDEX idx_parking_lots_available ON parking_lots(status, available_spaces)
WHERE status = 'ACTIVE' AND available_spaces > 0;

-- ============================================================
-- SECTION 3: COMMENTS
-- ============================================================

-- Table comment
COMMENT ON TABLE parking_lots IS 'Stores information about parking facilities/locations';

-- Column comments
COMMENT ON COLUMN parking_lots.id IS 'Unique identifier for parking lot';
COMMENT ON COLUMN parking_lots.name IS 'Name of the parking facility';
COMMENT ON COLUMN parking_lots.total_spaces IS 'Total number of parking spaces in this lot';
COMMENT ON COLUMN parking_lots.available_spaces IS 'Current number of available spaces';
COMMENT ON COLUMN parking_lots.is_24_hours IS 'Indicates if the parking lot operates 24/7';
COMMENT ON COLUMN parking_lots.status IS 'Current operational status: ACTIVE, INACTIVE, MAINTENANCE, FULL';
COMMENT ON COLUMN parking_lots.latitude IS 'GPS latitude coordinate (-90 to 90)';
COMMENT ON COLUMN parking_lots.longitude IS 'GPS longitude coordinate (-180 to 180)';

-- ============================================================
-- MIGRATION COMPLETE
-- ============================================================
-- Schema Version: V2
-- Table Created: parking_lots
-- Indexes Created: 5
-- Constraints: 4 CHECK constraints
-- ============================================================

