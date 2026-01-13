-- Migration: Add service and meta columns to logs table
-- Version: V6
-- Description: Extend logs table with service name and metadata JSON fields

-- Add service column to store the originating service name
ALTER TABLE logs ADD COLUMN IF NOT EXISTS service VARCHAR(100);

-- Add meta column to store additional JSON metadata
ALTER TABLE logs ADD COLUMN IF NOT EXISTS meta TEXT;

-- Add index on service for faster filtering
CREATE INDEX IF NOT EXISTS idx_logs_service ON logs(service);

-- Add index on log_level for faster filtering
CREATE INDEX IF NOT EXISTS idx_logs_level ON logs(log_level);

-- Comment on new columns
COMMENT ON COLUMN logs.service IS 'Name of the service that generated this log entry';
COMMENT ON COLUMN logs.meta IS 'Additional metadata stored as JSON';

