-- =========================================================================
-- V11__add_source_id_and_unmapped_queue.sql
-- Master Data Management (MDM): Bookmaker Source ID & LLM Decoding Queue
-- =========================================================================

-- 1. Add source_id to tournament ingestion map
ALTER TABLE mdm_tournament_ingestion_map
    ADD COLUMN IF NOT EXISTS source_id VARCHAR(128);

CREATE INDEX IF NOT EXISTS idx_mdm_tourn_map_source_id
    ON mdm_tournament_ingestion_map(external_system_id, source_id);

-- 2. Add source_id to team ingestion map
ALTER TABLE mdm_team_ingestion_map
    ADD COLUMN IF NOT EXISTS source_id VARCHAR(128);

CREATE INDEX IF NOT EXISTS idx_mdm_team_map_source_id
    ON mdm_team_ingestion_map(external_system_id, source_id);

-- 3. Create unmapped decoding queue for LLM agent workers
CREATE TABLE IF NOT EXISTS mdm_unmapped_queue (
    id BIGSERIAL PRIMARY KEY,
    external_system_code VARCHAR(64) NOT NULL,
    entity_type VARCHAR(32) NOT NULL, -- 'TOURNAMENT' or 'TEAM'
    raw_key VARCHAR(255) NOT NULL,
    source_id VARCHAR(128),
    raw_name VARCHAR(255),
    sport_id INT,
    sport_slug VARCHAR(64),
    country_id INT,
    country_name VARCHAR(128),
    context_parent_key VARCHAR(255),
    context_parent_name VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'RESOLVED', 'IGNORED'
    resolution_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_mdm_unmapped_queue UNIQUE (external_system_code, entity_type, raw_key)
);

CREATE INDEX IF NOT EXISTS idx_mdm_unmapped_queue_status ON mdm_unmapped_queue (status, entity_type);
CREATE INDEX IF NOT EXISTS idx_mdm_unmapped_queue_lookup ON mdm_unmapped_queue (external_system_code, entity_type, raw_key);
