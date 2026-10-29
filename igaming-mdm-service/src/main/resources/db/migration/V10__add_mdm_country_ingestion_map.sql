-- =========================================================================
-- V10__add_mdm_country_ingestion_map.sql
-- Master Data Management (MDM): Bookmaker Country Ingestion Mapping
-- =========================================================================

CREATE TABLE IF NOT EXISTS mdm_country_ingestion_map (
    id BIGSERIAL PRIMARY KEY,
    external_system_id INT NOT NULL REFERENCES mdm_external_system(id) ON DELETE CASCADE,
    raw_key VARCHAR(255) NOT NULL,
    canonical_country_id INT NOT NULL REFERENCES mdm_country(id) ON DELETE CASCADE,
    raw_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_mdm_country_map UNIQUE (external_system_id, raw_key)
);

CREATE INDEX IF NOT EXISTS idx_mdm_country_map_lookup ON mdm_country_ingestion_map(external_system_id, raw_key);
CREATE INDEX IF NOT EXISTS idx_mdm_country_map_country ON mdm_country_ingestion_map(canonical_country_id);
