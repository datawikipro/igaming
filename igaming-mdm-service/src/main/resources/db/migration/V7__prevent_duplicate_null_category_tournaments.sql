-- =========================================================================
-- V7__prevent_duplicate_null_category_tournaments.sql
-- Prevent duplicate tournaments when category_id IS NULL
-- In PostgreSQL UNIQUE(sport_id, category_id, slug) does not constrain NULLs
-- because NULL != NULL. This partial index enforces uniqueness for null category.
-- =========================================================================

CREATE UNIQUE INDEX IF NOT EXISTS uk_mdm_tournament_sport_slug_null_cat 
ON mdm_tournament (sport_id, slug) 
WHERE category_id IS NULL;
