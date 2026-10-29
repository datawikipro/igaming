-- Add coverage tier and protocol indicators to mdm_tournament
ALTER TABLE mdm_tournament ADD COLUMN IF NOT EXISTS ml_eligible BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE mdm_tournament ADD COLUMN IF NOT EXISTS has_official_reports BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE mdm_tournament ADD COLUMN IF NOT EXISTS has_player_stats BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE mdm_tournament ADD COLUMN IF NOT EXISTS coverage_tier VARCHAR(32) NOT NULL DEFAULT 'TIER_0_SCRATCH';

CREATE INDEX IF NOT EXISTS idx_mdm_tournament_ml_eligible ON mdm_tournament(ml_eligible);
CREATE INDEX IF NOT EXISTS idx_mdm_tournament_coverage_tier ON mdm_tournament(coverage_tier);
