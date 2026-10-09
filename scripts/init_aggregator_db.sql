-- =============================================================================
-- SmartBet.guru - Aggregator Database Performance & Initialization Script
-- Idempotent script for fresh installs, cluster redeployments, or migrations.
-- =============================================================================

-- 1. Create RAM tablespace for high-throughput odds & alerts tables
DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_tablespace WHERE spcname = 'ram_tablespace') THEN
      CREATE TABLESPACE ram_tablespace LOCATION '/dev/shm/ramdata';
   END IF;
END
$$;

-- 2. Database-level Performance Tuning
ALTER DATABASE igaming_aggregator SET jit = off;
ALTER DATABASE igaming_aggregator SET synchronous_commit = off;

-- 3. Extensions
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- 4. Target Indexes for odds_actual (Prevents 200ms+ sequential table scans)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_odds_actual_bm_ext_id 
    ON odds_actual (bookmaker, external_event_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_odds_actual_updated_at 
    ON odds_actual (updated_at);

-- 5. Target Indexes for normalization_request
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_norm_req_status_created 
    ON normalization_request (status, created_at);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_norm_req_pending_created 
    ON normalization_request (created_at DESC) 
    WHERE status IN ('PENDING', 'FAILED', 'NOT_VALID');

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_norm_req_odds_type 
    ON normalization_request (created_at ASC) 
    WHERE status IN ('PENDING', 'FAILED', 'NOT_VALID') AND type = 'ODDS_TYPE';

-- 6. Target Indexes for odds_refresh_queue
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orq_source_ext 
    ON odds_refresh_queue (bet_source_id, external_event_id);

-- 7. Target Indexes for match_record
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_match_start_time 
    ON match_record (start_time);

-- 8. Hot Table Autovacuum Tuning for odds_actual (reduces bloat and IO spikes)
ALTER TABLE odds_actual SET (
    autovacuum_vacuum_scale_factor = 0.05,
    autovacuum_vacuum_threshold = 10000,
    autovacuum_vacuum_cost_limit = 1000,
    autovacuum_vacuum_cost_delay = 2
);

ANALYZE odds_actual;
ANALYZE normalization_request;
ANALYZE match_record;
