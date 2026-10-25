-- =========================================================================
-- V4__optimize_mdm_indexes_and_storage.sql
-- Master Data Management (MDM) Storage & Index Performance Optimization
-- 1. Eliminates duplicate B-tree indexes on ingestion maps
-- 2. Sets fillfactor=85-90 for HOT (Heap-Only Tuples) update optimization
-- 3. Adds reverse lookup indexes for O(1) canonical entity resolution
-- =========================================================================

-- 1. Drop redundant duplicate indexes that duplicate UNIQUE constraints
DROP INDEX IF EXISTS idx_mdm_team_map_lookup;
DROP INDEX IF EXISTS idx_mdm_tourn_map_lookup;
DROP INDEX IF EXISTS idx_mdm_sport_map_lookup;
DROP INDEX IF EXISTS idx_mdm_team_sofascore;

-- 2. Configure fillfactors for HOT update optimization (prevents index bloat)
ALTER TABLE mdm_team SET (fillfactor = 90);
ALTER TABLE mdm_team_sofa_metadata SET (fillfactor = 85);
ALTER TABLE mdm_team_dna SET (fillfactor = 85);
ALTER TABLE mdm_team_translation SET (fillfactor = 85);
ALTER TABLE mdm_tournament SET (fillfactor = 90);
ALTER TABLE mdm_tournament_translation SET (fillfactor = 85);

-- 3. Reverse lookup indexes for fast reverse resolution by canonical ID
CREATE INDEX IF NOT EXISTS idx_mdm_team_map_team_id ON mdm_team_ingestion_map (canonical_team_id);
CREATE INDEX IF NOT EXISTS idx_mdm_tourn_map_tourn_id ON mdm_tournament_ingestion_map (canonical_tournament_id);
