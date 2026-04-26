-- iGaming Source Database Maintenance Script
-- Use these commands to stabilize and optimize Postgres performance.

-- 1. Cleaning up "Dead Rows" (Bloat)
-- Run this to reclaim space and update statistics. 
-- Note: ANALYZE is fast. VACUUM (without FULL) is online and doesn't lock the table.
VACUUM ANALYZE match_cache;
VACUUM ANALYZE match_factor;

-- 2. Performance Indexing for Loader
-- The Loader frequently queries matches by status and updatedAt.
-- This composite index will significantly speed up "findAndLockOnlyNewPendingMatches".
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_match_cache_status_updated_at 
ON match_cache (status, updated_at);

-- 3. Optimization for Factor retrieval
-- Ensure match_id is indexed for fast joins and updates.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_match_factor_match_id 
ON match_factor (match_id);

-- 4. Deep Cleanup (Maintenance Window Required)
-- If the tables are extremely bloated (e.g. 50%+ dead rows), 
-- you might need VACUUM FULL. WARNING: This LOCKS the table completely.
-- VACUUM FULL match_factor;
-- VACUUM FULL match_cache;

-- 5. Redis-Offloading Cleanup
-- If app.persistence.use-redis-factors=true is enabled, 
-- you can truncate match_factor to reclaim massive amounts of space.
-- TRUNCATE TABLE match_factor;

-- 6. Monitor Statistics
-- Run this to see the current bloat level (requires pgstattuple extension)
-- SELECT * FROM pgstattuple('match_cache');
