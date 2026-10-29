-- V9: Register SofaScore in mdm_external_system and seed sport ingestion map
INSERT INTO mdm_external_system (code, name, is_active)
VALUES ('sofascore', 'SofaScore', true)
ON CONFLICT (code) DO NOTHING;

-- Seed sport mappings for sofascore
INSERT INTO mdm_sport_ingestion_map (external_system_id, raw_key, canonical_sport_id, created_at)
SELECT s.id, m.slug, m.id, now()
FROM mdm_external_system s
CROSS JOIN mdm_sport m
WHERE s.code = 'sofascore'
ON CONFLICT (external_system_id, raw_key) DO NOTHING;

INSERT INTO mdm_sport_ingestion_map (external_system_id, raw_key, canonical_sport_id, created_at)
SELECT s.id, lower(m.domain_key), m.id, now()
FROM mdm_external_system s
CROSS JOIN mdm_sport m
WHERE s.code = 'sofascore'
ON CONFLICT (external_system_id, raw_key) DO NOTHING;

INSERT INTO mdm_sport_ingestion_map (external_system_id, raw_key, canonical_sport_id, created_at)
SELECT s.id, upper(m.domain_key), m.id, now()
FROM mdm_external_system s
CROSS JOIN mdm_sport m
WHERE s.code = 'sofascore'
ON CONFLICT (external_system_id, raw_key) DO NOTHING;
