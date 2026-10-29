-- Add stage and round columns to mdm_match
ALTER TABLE mdm_match ADD COLUMN IF NOT EXISTS stage VARCHAR(64);
ALTER TABLE mdm_match ADD COLUMN IF NOT EXISTS round VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_mdm_match_stage ON mdm_match(stage);
CREATE INDEX IF NOT EXISTS idx_mdm_match_round ON mdm_match(round);
