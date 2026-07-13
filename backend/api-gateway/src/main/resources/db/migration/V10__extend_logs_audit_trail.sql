-- Migration V10: Extend logs table with audit trail fields
-- Description: Add action, entity_type, entity_id, client_id, license_plate columns
--              to support structured business-event audit trails per entity.

ALTER TABLE logs ADD COLUMN IF NOT EXISTS action        VARCHAR(100);
ALTER TABLE logs ADD COLUMN IF NOT EXISTS entity_type   VARCHAR(50);
ALTER TABLE logs ADD COLUMN IF NOT EXISTS entity_id     BIGINT;
ALTER TABLE logs ADD COLUMN IF NOT EXISTS client_id     BIGINT;
ALTER TABLE logs ADD COLUMN IF NOT EXISTS license_plate VARCHAR(20);

-- Indexes for efficient history lookups
CREATE INDEX IF NOT EXISTS idx_logs_action        ON logs(action);
CREATE INDEX IF NOT EXISTS idx_logs_entity_type   ON logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_logs_entity_id     ON logs(entity_id);
CREATE INDEX IF NOT EXISTS idx_logs_client_id     ON logs(client_id);
CREATE INDEX IF NOT EXISTS idx_logs_license_plate ON logs(license_plate);
CREATE INDEX IF NOT EXISTS idx_logs_timestamp     ON logs(timestamp);
-- Composite: fast "client history in range"
CREATE INDEX IF NOT EXISTS idx_logs_client_ts     ON logs(client_id, timestamp DESC);
-- Composite: fast "plate history in range"
CREATE INDEX IF NOT EXISTS idx_logs_plate_ts      ON logs(license_plate, timestamp DESC);

COMMENT ON COLUMN logs.action        IS 'Business action name, e.g. CLIENT_CREATED, GATE_ENTRY';
COMMENT ON COLUMN logs.entity_type   IS 'Entity type: CLIENT, VEHICLE, SUBSCRIPTION, GATE, BILLING';
COMMENT ON COLUMN logs.entity_id     IS 'Primary key of the affected entity';
COMMENT ON COLUMN logs.client_id     IS 'ID of the related client (for history queries)';
COMMENT ON COLUMN logs.license_plate IS 'License plate of the vehicle (for vehicle history queries)';

