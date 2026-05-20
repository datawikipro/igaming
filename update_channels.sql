-- SQL Script to update telegram_channels table
ALTER TABLE telegram_channels ADD COLUMN IF NOT EXISTS low_profit_threshold DOUBLE PRECISION DEFAULT 5.0;
ALTER TABLE telegram_channels ADD COLUMN IF NOT EXISTS low_profit_interval_minutes INT DEFAULT 1;
ALTER TABLE telegram_channels ADD COLUMN IF NOT EXISTS high_profit_interval_minutes INT DEFAULT 60;
ALTER TABLE telegram_channels ADD COLUMN IF NOT EXISTS min_freshness_minutes INT DEFAULT 1;

-- Update defaults for existing rows
UPDATE telegram_channels SET low_profit_threshold = 5.0 WHERE low_profit_threshold IS NULL;
UPDATE telegram_channels SET low_profit_interval_minutes = 1 WHERE low_profit_interval_minutes IS NULL;
UPDATE telegram_channels SET high_profit_interval_minutes = 60 WHERE high_profit_interval_minutes IS NULL;
UPDATE telegram_channels SET min_freshness_minutes = 1 WHERE min_freshness_minutes IS NULL;
