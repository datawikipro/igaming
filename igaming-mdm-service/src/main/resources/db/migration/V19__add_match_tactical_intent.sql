-- V19: Add pre-match tactical intent, tournament imperative, and dynamic tags to mdm_match

ALTER TABLE mdm_match 
    ADD COLUMN IF NOT EXISTS home_intent_imperative VARCHAR(64),
    ADD COLUMN IF NOT EXISTS home_intent_calculation VARCHAR(64),
    ADD COLUMN IF NOT EXISTS away_intent_imperative VARCHAR(64),
    ADD COLUMN IF NOT EXISTS away_intent_calculation VARCHAR(64),
    ADD COLUMN IF NOT EXISTS match_dynamic_tag VARCHAR(64),
    ADD COLUMN IF NOT EXISTS draw_satisfaction_home NUMERIC(3, 2) DEFAULT 0.33,
    ADD COLUMN IF NOT EXISTS draw_satisfaction_away NUMERIC(3, 2) DEFAULT 0.33,
    ADD COLUMN IF NOT EXISTS tactical_intent_json JSONB;

CREATE INDEX IF NOT EXISTS idx_mdm_match_dynamic_tag ON mdm_match(match_dynamic_tag);
CREATE INDEX IF NOT EXISTS idx_mdm_match_home_imperative ON mdm_match(home_intent_imperative);
CREATE INDEX IF NOT EXISTS idx_mdm_match_away_imperative ON mdm_match(away_intent_imperative);
