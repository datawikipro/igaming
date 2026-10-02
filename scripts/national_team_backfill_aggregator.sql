-- =============================================================================
-- Backfill: national team detection in aggregator match_record table
-- Task: #78 [team-national-detect]
-- Author: AI (plane-243ca9d1)
-- Date: 2026-10-02
--
-- Applies to: aggregator PostgreSQL database (igaming_aggregator)
-- Requires: detect_country_iso(), is_national_league(), is_club_team() functions
--           already created via national_team_backfill.sql in bookmaker DB OR
--           run the functions below first.
-- =============================================================================

-- Step 1: DDL — add columns to match_record
ALTER TABLE match_record ADD COLUMN IF NOT EXISTS is_national_home BOOLEAN DEFAULT NULL;
ALTER TABLE match_record ADD COLUMN IF NOT EXISTS is_national_away BOOLEAN DEFAULT NULL;
ALTER TABLE match_record ADD COLUMN IF NOT EXISTS home_country_iso VARCHAR(10) DEFAULT NULL;
ALTER TABLE match_record ADD COLUMN IF NOT EXISTS away_country_iso VARCHAR(10) DEFAULT NULL;

-- Step 2: Reuse same helper functions (paste from national_team_backfill.sql if not already created)
-- (Functions detect_country_iso, is_national_league, is_club_team should exist)

-- Step 3: Backfill match_record
-- team1_name / team2_name / league_name are assumed column names in match_record
-- Adjust if actual columns differ (check: \d match_record)

-- Pass 1: League-based detection (high confidence)
UPDATE match_record mr
SET
    is_national_home = TRUE,
    is_national_away = TRUE,
    home_country_iso = detect_country_iso(mr.team1_name),
    away_country_iso = detect_country_iso(mr.team2_name)
WHERE
    is_national_home IS NULL
    AND mr.league_name IS NOT NULL
    AND is_national_league(mr.league_name);

-- Pass 2: Team-name-based detection
UPDATE match_record mr
SET
    is_national_home = CASE WHEN NOT is_club_team(mr.team1_name) AND detect_country_iso(mr.team1_name) IS NOT NULL THEN TRUE ELSE FALSE END,
    is_national_away = CASE WHEN NOT is_club_team(mr.team2_name) AND detect_country_iso(mr.team2_name) IS NOT NULL THEN TRUE ELSE FALSE END,
    home_country_iso = CASE WHEN NOT is_club_team(mr.team1_name) AND detect_country_iso(mr.team1_name) IS NOT NULL THEN detect_country_iso(mr.team1_name) ELSE NULL END,
    away_country_iso = CASE WHEN NOT is_club_team(mr.team2_name) AND detect_country_iso(mr.team2_name) IS NOT NULL THEN detect_country_iso(mr.team2_name) ELSE NULL END
WHERE
    is_national_home IS NULL;

-- Pass 3: Default to FALSE for unrecognized
UPDATE match_record
SET
    is_national_home = FALSE,
    is_national_away = FALSE
WHERE
    is_national_home IS NULL
    OR is_national_away IS NULL;

-- Step 4: Statistics
SELECT
    COUNT(*) AS total_matches,
    COUNT(*) FILTER (WHERE is_national_home = TRUE) AS national_home_count,
    COUNT(*) FILTER (WHERE is_national_away = TRUE) AS national_away_count,
    COUNT(*) FILTER (WHERE is_national_home = TRUE AND is_national_away = TRUE) AS both_national,
    COUNT(DISTINCT home_country_iso) FILTER (WHERE home_country_iso IS NOT NULL) AS unique_home_iso_codes,
    COUNT(DISTINCT away_country_iso) FILTER (WHERE away_country_iso IS NOT NULL) AS unique_away_iso_codes
FROM match_record;
