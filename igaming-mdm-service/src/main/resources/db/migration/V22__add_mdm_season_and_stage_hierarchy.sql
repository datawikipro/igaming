-- =========================================================================
-- V22__add_mdm_season_and_stage_hierarchy.sql
-- 4-Level MDM Hierarchy: Tournament (League) -> Season -> Season Stage -> Match
-- Polymorphic bracket support (Swiss, Double/Single Elimination, GSL, Round Robin)
-- Match-level format validation: best_of (Bo1, Bo3, Bo5, Bo7)
-- =========================================================================

-- 1. MDM Season Table
CREATE TABLE IF NOT EXISTS mdm_season (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    sport_id INT NOT NULL,
    slug VARCHAR(191) NOT NULL,
    name VARCHAR(255) NOT NULL,
    year INT,
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uk_mdm_season_tourn_slug UNIQUE (tournament_id, slug),
    CONSTRAINT uk_mdm_season_id_sport UNIQUE (id, sport_id),
    CONSTRAINT fk_mdm_season_tournament FOREIGN KEY (tournament_id, sport_id)
        REFERENCES mdm_tournament (id, sport_id) ON DELETE CASCADE,
    CONSTRAINT fk_mdm_season_sport FOREIGN KEY (sport_id)
        REFERENCES mdm_sport (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_mdm_season_tourn ON mdm_season(tournament_id);
CREATE INDEX IF NOT EXISTS idx_mdm_season_active ON mdm_season(status);

-- 2. MDM Season Stage Table (Swiss, Double Elim, Single Elim, GSL, Round Robin)
CREATE TABLE IF NOT EXISTS mdm_season_stage (
    id BIGSERIAL PRIMARY KEY,
    season_id BIGINT NOT NULL,
    sport_id INT NOT NULL,
    slug VARCHAR(191) NOT NULL,
    name VARCHAR(255) NOT NULL,
    stage_type VARCHAR(64) NOT NULL DEFAULT 'SINGLE_ELIMINATION',
    default_best_of INT NOT NULL DEFAULT 3,
    stage_order INT NOT NULL DEFAULT 1,
    is_playoff BOOLEAN NOT NULL DEFAULT FALSE,
    is_grand_final BOOLEAN NOT NULL DEFAULT FALSE,
    bracket_meta_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uk_mdm_stage_season_slug UNIQUE (season_id, slug),
    CONSTRAINT uk_mdm_stage_id_sport UNIQUE (id, sport_id),
    CONSTRAINT fk_mdm_stage_season FOREIGN KEY (season_id, sport_id)
        REFERENCES mdm_season (id, sport_id) ON DELETE CASCADE,
    CONSTRAINT fk_mdm_stage_sport FOREIGN KEY (sport_id)
        REFERENCES mdm_sport (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_mdm_stage_season ON mdm_season_stage(season_id);
CREATE INDEX IF NOT EXISTS idx_mdm_stage_type ON mdm_season_stage(stage_type);

-- 3. Match format & stage link
ALTER TABLE mdm_match
    ADD COLUMN IF NOT EXISTS stage_id BIGINT,
    ADD COLUMN IF NOT EXISTS best_of INT NOT NULL DEFAULT 3;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_mdm_match_stage'
    ) THEN
        ALTER TABLE mdm_match
            ADD CONSTRAINT fk_mdm_match_stage FOREIGN KEY (stage_id, sport_id)
                REFERENCES mdm_season_stage (id, sport_id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_mdm_match_stage_id ON mdm_match(stage_id);

-- 4. Initial Backfill: Default Season and Stage for existing tournaments
INSERT INTO mdm_season (tournament_id, sport_id, slug, name, year, status)
SELECT t.id, t.sport_id, 'default', 'Regular Season', EXTRACT(YEAR FROM NOW())::int, 'ACTIVE'
FROM mdm_tournament t
ON CONFLICT (tournament_id, slug) DO NOTHING;

INSERT INTO mdm_season_stage (season_id, sport_id, slug, name, stage_type, default_best_of, stage_order)
SELECT s.id, s.sport_id, 'main-event', 'Main Event', 'SINGLE_ELIMINATION', 3, 1
FROM mdm_season s
ON CONFLICT (season_id, slug) DO NOTHING;

-- 5. Backfill best_of:
-- Non-series sports (Football, Ice Hockey, Basketball, Cyber-Football etc.) are single-match games (best_of = 1)
UPDATE mdm_match
SET best_of = 1
WHERE sport_id IN (1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 37, 41, 42);

-- Esports series formatting from stage/round
UPDATE mdm_match
SET best_of = 1
WHERE (stage ILIKE '%bo1%' OR round ILIKE '%bo1%' OR stage ILIKE '%best of 1%' OR round ILIKE '%best of 1%');

UPDATE mdm_match
SET best_of = 5
WHERE (stage ILIKE '%bo5%' OR round ILIKE '%bo5%' OR stage ILIKE '%best of 5%' OR round ILIKE '%best of 5%');

UPDATE mdm_match
SET best_of = 7
WHERE (stage ILIKE '%bo7%' OR round ILIKE '%bo7%' OR stage ILIKE '%best of 7%' OR round ILIKE '%best of 7%');
