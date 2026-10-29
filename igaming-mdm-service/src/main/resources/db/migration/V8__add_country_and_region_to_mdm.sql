-- V8: Proper country normalization via mdm_country reference table
-- Replaces the previous approach of raw VARCHAR country_name columns.
-- mdm_country is sport-agnostic: one England entry serves Football, Tennis, etc.

-- 1. Create canonical country reference table
CREATE TABLE IF NOT EXISTS mdm_country (
    id           SERIAL PRIMARY KEY,
    iso_code     VARCHAR(4)   UNIQUE,                    -- ISO 3166-1 alpha-2/alpha-3: "GB", "NG", "BH"
    canonical_name VARCHAR(128) NOT NULL,                -- "England", "Nigeria", "Bahrain"
    flag_code    VARCHAR(16),                            -- emoji / flag sprite key
    sofascore_id INTEGER      UNIQUE,                    -- SofaScore category ID if known
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_mdm_country_name
    ON mdm_country(lower(canonical_name));

CREATE INDEX IF NOT EXISTS idx_mdm_country_iso
    ON mdm_country(iso_code);

-- 2. Add country_id FK to mdm_tournament (nullable — international tournaments have no country)
ALTER TABLE mdm_tournament
    ADD COLUMN IF NOT EXISTS country_id INTEGER REFERENCES mdm_country(id);

CREATE INDEX IF NOT EXISTS idx_mdm_tournament_country_id
    ON mdm_tournament(country_id);

-- 3. Add country_id FK to mdm_team (nullable — international clubs, e.g. UEFA CL participants)
ALTER TABLE mdm_team
    ADD COLUMN IF NOT EXISTS country_id INTEGER REFERENCES mdm_country(id);

CREATE INDEX IF NOT EXISTS idx_mdm_team_country_id
    ON mdm_team(country_id);

-- 4. CONSTRAINT: prevent duplicate tournaments in same country
--    "Premier League" can exist only once per (sport, country).
--    NULLs allowed (international tournaments don't have country).
CREATE UNIQUE INDEX IF NOT EXISTS uk_mdm_tournament_sport_name_country
    ON mdm_tournament(sport_id, lower(canonical_name), country_id)
    WHERE country_id IS NOT NULL;

-- 5. Seed well-known countries from mdm_category slugs (best-effort)
INSERT INTO mdm_country (canonical_name, iso_code, sofascore_id)
SELECT DISTINCT
    initcap(replace(c.slug, '-', ' ')) AS canonical_name,
    NULL::VARCHAR,
    c.sofascore_id
FROM mdm_category c
WHERE c.slug NOT IN ('general', 'international', 'world', 'uefa', 'concacaf',
                     'afc', 'caf', 'conmebol', 'ofc', 'south-america', 'north-central-america',
                     'europe', 'africa', 'oceania', 'asia')
  AND c.slug IS NOT NULL
ON CONFLICT (lower(canonical_name)) DO UPDATE
    SET sofascore_id = EXCLUDED.sofascore_id
    WHERE mdm_country.sofascore_id IS NULL;

-- 6. Backfill tournament country_id from their existing category_id → mdm_country
UPDATE mdm_tournament t
SET country_id = mc.id
FROM mdm_category cat
JOIN mdm_country mc ON lower(mc.canonical_name) = lower(initcap(replace(cat.slug, '-', ' ')))
WHERE t.category_id = cat.id
  AND t.country_id IS NULL
  AND cat.slug NOT IN ('general', 'international', 'world', 'uefa', 'concacaf',
                       'afc', 'caf', 'conmebol', 'ofc');

-- 7. country_name VARCHAR columns stay as a read-cache / denorm for now
--    They get populated by backfill_country.py and serve as fallback
--    until all data is migrated to country_id.
--    The authoritative field is country_id; country_name is informational.

-- 8. Add nationality_country_id to mdm_player for nationality flags display.
--    citizenship_category_id is kept for backward compat / SofaScore reconciliation,
--    but nationality_country_id is the authoritative normalized field.
ALTER TABLE mdm_player
    ADD COLUMN IF NOT EXISTS nationality_country_id INTEGER REFERENCES mdm_country(id);

CREATE INDEX IF NOT EXISTS idx_mdm_player_nationality
    ON mdm_player(nationality_country_id);

-- 9. Backfill player nationality from existing citizenship_category_id → mdm_country
UPDATE mdm_player p
SET nationality_country_id = mc.id
FROM mdm_category cat
JOIN mdm_country mc ON lower(mc.canonical_name) = lower(initcap(replace(cat.slug, '-', ' ')))
WHERE p.citizenship_category_id = cat.id
  AND p.nationality_country_id IS NULL
  AND cat.slug NOT IN ('general', 'international', 'world', 'uefa', 'concacaf',
                       'afc', 'caf', 'conmebol', 'ofc');
