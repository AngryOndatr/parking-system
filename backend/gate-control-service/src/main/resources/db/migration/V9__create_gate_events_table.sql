-- Migration: Create gate_events table
-- Version: V9
-- Description: Create gate_events table to log all gate operations

CREATE TABLE gate_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(20) NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    ticket_code VARCHAR(50),
    gate_id VARCHAR(20) NOT NULL,
    decision VARCHAR(10) NOT NULL,
    reason VARCHAR(500),
    timestamp TIMESTAMP NOT NULL,
    operator_id BIGINT,

    CONSTRAINT chk_event_type CHECK (event_type IN ('ENTRY', 'EXIT', 'MANUAL_OPEN', 'ERROR')),
    CONSTRAINT chk_decision CHECK (decision IN ('OPEN', 'DENY'))
);

-- Create indexes for common queries
CREATE INDEX idx_gate_events_license_plate ON gate_events(license_plate);
CREATE INDEX idx_gate_events_timestamp ON gate_events(timestamp);
CREATE INDEX idx_gate_events_gate_id ON gate_events(gate_id);

-- Add comment to table
COMMENT ON TABLE gate_events IS 'Logs all gate operations including entry, exit, manual operations and errors';
