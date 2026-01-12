-- ******************************************************
-- Migration: V5__insert_test_parking_data.sql
-- Purpose: Insert test data for parking lots and parking spaces
-- Database: PostgreSQL
-- Created: 2026-01-12
-- Issue: #18
-- ******************************************************

-- ============================================================
-- SECTION 1: INSERT PARKING LOTS
-- ============================================================

-- Insert test parking lot if it doesn't exist
-- Using INSERT only if no parking lot with this name exists
INSERT INTO parking_lots (name, address, total_spaces, available_spaces, opens_at, closes_at, latitude, longitude, status, is_24_hours, city)
SELECT 'Downtown Parking', '123 Main St', 100, 15, '06:00:00', '23:00:00', 50.4501, 30.5234, 'ACTIVE', FALSE, 'Kyiv'
WHERE NOT EXISTS (SELECT 1 FROM parking_lots WHERE name = 'Downtown Parking');

-- Get the parking lot ID
DO $$
DECLARE
    lot_id BIGINT;
BEGIN
    SELECT id INTO lot_id FROM parking_lots WHERE name = 'Downtown Parking' LIMIT 1;

    -- Insert parking spaces only if they don't exist yet
    IF NOT EXISTS (SELECT 1 FROM parking_spaces WHERE parking_lot_id = lot_id) THEN

        -- ============================================================
        -- SECTION 2: INSERT PARKING SPACES
        -- ============================================================

        -- Ground Floor - Section A (Standard spaces)
        INSERT INTO parking_spaces (parking_lot_id, space_number, floor_level, section, space_type, status, has_charger, length_cm, width_cm)
        VALUES
            (lot_id, 'A-01', 0, 'A', 'STANDARD', 'AVAILABLE', FALSE, 500, 250),
            (lot_id, 'A-02', 0, 'A', 'STANDARD', 'AVAILABLE', FALSE, 500, 250),
            (lot_id, 'A-03', 0, 'A', 'STANDARD', 'OCCUPIED', FALSE, 500, 250),
            (lot_id, 'A-04', 0, 'A', 'STANDARD', 'AVAILABLE', FALSE, 500, 250),
            (lot_id, 'A-05', 0, 'A', 'STANDARD', 'AVAILABLE', FALSE, 500, 250),
            (lot_id, 'A-06', 0, 'A', 'HANDICAPPED', 'AVAILABLE', FALSE, 600, 350),
            (lot_id, 'A-07', 0, 'A', 'HANDICAPPED', 'AVAILABLE', FALSE, 600, 350);

        -- Ground Floor - Section B (Electric charging spaces)
        INSERT INTO parking_spaces (parking_lot_id, space_number, floor_level, section, space_type, status, has_charger, charger_type, length_cm, width_cm)
        VALUES
            (lot_id, 'B-01', 0, 'B', 'ELECTRIC', 'AVAILABLE', TRUE, 'Type 2', 550, 270),
            (lot_id, 'B-02', 0, 'B', 'ELECTRIC', 'OCCUPIED', TRUE, 'Type 2', 550, 270),
            (lot_id, 'B-03', 0, 'B', 'ELECTRIC', 'AVAILABLE', TRUE, 'DC Fast', 550, 270),
            (lot_id, 'B-04', 0, 'B', 'ELECTRIC', 'RESERVED', TRUE, 'Tesla', 550, 270);

        -- Level 1 - Section C (VIP spaces with rate override)
        INSERT INTO parking_spaces (parking_lot_id, space_number, floor_level, section, space_type, status, has_charger, hourly_rate_override, daily_rate_override, length_cm, width_cm)
        VALUES
            (lot_id, 'C-01', 1, 'C', 'VIP', 'AVAILABLE', FALSE, 10.00, 80.00, 600, 300),
            (lot_id, 'C-02', 1, 'C', 'VIP', 'AVAILABLE', FALSE, 10.00, 80.00, 600, 300),
            (lot_id, 'C-03', 1, 'C', 'VIP', 'OCCUPIED', FALSE, 10.00, 80.00, 600, 300);

        -- Level 1 - Section D (Compact spaces)
        INSERT INTO parking_spaces (parking_lot_id, space_number, floor_level, section, space_type, status, has_charger, length_cm, width_cm)
        VALUES
            (lot_id, 'D-01', 1, 'D', 'COMPACT', 'AVAILABLE', FALSE, 450, 220),
            (lot_id, 'D-02', 1, 'D', 'COMPACT', 'AVAILABLE', FALSE, 450, 220),
            (lot_id, 'D-03', 1, 'D', 'COMPACT', 'AVAILABLE', FALSE, 450, 220),
            (lot_id, 'D-04', 1, 'D', 'COMPACT', 'OCCUPIED', FALSE, 450, 220);

        -- Underground Level -1 - Section E (Oversized spaces)
        INSERT INTO parking_spaces (parking_lot_id, space_number, floor_level, section, space_type, status, has_charger, length_cm, width_cm)
        VALUES
            (lot_id, 'E-01', -1, 'E', 'OVERSIZED', 'AVAILABLE', FALSE, 700, 350),
            (lot_id, 'E-02', -1, 'E', 'OVERSIZED', 'AVAILABLE', FALSE, 700, 350),
            (lot_id, 'E-03', -1, 'E', 'OVERSIZED', 'RESERVED', FALSE, 700, 350);

        -- Maintenance spaces
        INSERT INTO parking_spaces (parking_lot_id, space_number, floor_level, section, space_type, status, has_charger)
        VALUES
            (lot_id, 'F-01', 0, 'F', 'STANDARD', 'MAINTENANCE', FALSE),
            (lot_id, 'F-02', 0, 'F', 'STANDARD', 'OUT_OF_SERVICE', FALSE);

        RAISE NOTICE 'Successfully inserted % parking spaces for Downtown Parking', (SELECT COUNT(*) FROM parking_spaces WHERE parking_lot_id = lot_id);
    ELSE
        RAISE NOTICE 'Parking spaces already exist for Downtown Parking, skipping insert';
    END IF;
END $$;

-- ============================================================
-- SECTION 3: VERIFICATION
-- ============================================================

-- Display summary
DO $$
DECLARE
    lot_count INTEGER;
    space_count INTEGER;
    available_count INTEGER;
    occupied_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO lot_count FROM parking_lots;
    SELECT COUNT(*) INTO space_count FROM parking_spaces;
    SELECT COUNT(*) INTO available_count FROM parking_spaces WHERE status = 'AVAILABLE';
    SELECT COUNT(*) INTO occupied_count FROM parking_spaces WHERE status = 'OCCUPIED';

    RAISE NOTICE '=================================================';
    RAISE NOTICE 'Migration V5 completed successfully';
    RAISE NOTICE 'Parking Lots: %', lot_count;
    RAISE NOTICE 'Total Parking Spaces: %', space_count;
    RAISE NOTICE 'Available Spaces: %', available_count;
    RAISE NOTICE 'Occupied Spaces: %', occupied_count;
    RAISE NOTICE '=================================================';
END $$;

-- ============================================================
-- MIGRATION COMPLETE
-- ============================================================
-- Schema Version: V5
-- Test Data Created: 1 parking lot, 23 parking spaces
-- Available Spaces: 15
-- Occupied Spaces: 4
-- Reserved Spaces: 2
-- Maintenance/Out of Service: 2
-- ============================================================

