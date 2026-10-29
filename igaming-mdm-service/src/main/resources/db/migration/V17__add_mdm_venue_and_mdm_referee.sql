-- V17: Add mdm_venue, mdm_venue_sport_profile, mdm_referee, and link them to mdm_match & mdm_team

-- 1. Venue Table (Stadium, Court, Arena)
CREATE TABLE IF NOT EXISTS mdm_venue (
    id                   BIGSERIAL PRIMARY KEY,
    slug                 VARCHAR(191) NOT NULL UNIQUE,
    canonical_name       VARCHAR(255) NOT NULL,
    city                 VARCHAR(128),
    country_code         VARCHAR(8),
    latitude             NUMERIC(9, 6),
    longitude            NUMERIC(9, 6),
    altitude_m           INTEGER DEFAULT 0,
    capacity             INTEGER DEFAULT 0,
    is_indoor            BOOLEAN NOT NULL DEFAULT false,
    has_retractable_roof BOOLEAN NOT NULL DEFAULT false,
    sofascore_venue_id   BIGINT UNIQUE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_mdm_venue_slug ON mdm_venue(slug);
CREATE INDEX IF NOT EXISTS idx_mdm_venue_country ON mdm_venue(country_code);
CREATE INDEX IF NOT EXISTS idx_mdm_venue_city ON mdm_venue(city);

-- 2. Venue Sport Profile (Sport-specific parameters: surfaces, dimensions, CPI, etc.)
CREATE TABLE IF NOT EXISTS mdm_venue_sport_profile (
    id                   BIGSERIAL PRIMARY KEY,
    venue_id             BIGINT NOT NULL REFERENCES mdm_venue(id) ON DELETE CASCADE,
    sport_id             INTEGER NOT NULL REFERENCES mdm_sport(id) ON DELETE RESTRICT,
    surface_category     VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    surface_detail       VARCHAR(64),
    court_pace_index     NUMERIC(4, 1),
    pitch_length_m       NUMERIC(5, 2),
    pitch_width_m        NUMERIC(5, 2),
    rink_type            VARCHAR(32),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_mdm_venue_sport UNIQUE (venue_id, sport_id)
);

CREATE INDEX IF NOT EXISTS idx_mdm_venue_profile_sport ON mdm_venue_sport_profile(sport_id, surface_category);

-- 3. Referee Table
CREATE TABLE IF NOT EXISTS mdm_referee (
    id                   BIGSERIAL PRIMARY KEY,
    sport_id             INTEGER NOT NULL REFERENCES mdm_sport(id) ON DELETE RESTRICT,
    slug                 VARCHAR(191) NOT NULL UNIQUE,
    canonical_name       VARCHAR(255) NOT NULL,
    first_name           VARCHAR(128),
    last_name            VARCHAR(128),
    country_code         VARCHAR(8),
    country_name         VARCHAR(128),
    sofascore_referee_id BIGINT UNIQUE,
    yellow_cards_per_game NUMERIC(4, 2) DEFAULT 0,
    red_cards_per_game   NUMERIC(4, 2) DEFAULT 0,
    fouls_per_game       NUMERIC(4, 2) DEFAULT 0,
    penalties_per_game   NUMERIC(4, 2) DEFAULT 0,
    strictness_tier      VARCHAR(32) NOT NULL DEFAULT 'MODERATE',
    total_matches        INTEGER DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_mdm_referee_sport ON mdm_referee(sport_id);
CREATE INDEX IF NOT EXISTS idx_mdm_referee_slug ON mdm_referee(slug);
CREATE INDEX IF NOT EXISTS idx_mdm_referee_strictness ON mdm_referee(strictness_tier);

-- 4. Foreign Keys to mdm_match
ALTER TABLE mdm_match ADD COLUMN IF NOT EXISTS venue_id BIGINT REFERENCES mdm_venue(id) ON DELETE SET NULL;
ALTER TABLE mdm_match ADD COLUMN IF NOT EXISTS referee_id BIGINT REFERENCES mdm_referee(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_mdm_match_venue ON mdm_match(venue_id);
CREATE INDEX IF NOT EXISTS idx_mdm_match_referee ON mdm_match(referee_id);

-- 5. Team Format in mdm_team (SOLO, DOUBLES, SQUAD)
ALTER TABLE mdm_team ADD COLUMN IF NOT EXISTS team_format VARCHAR(16) NOT NULL DEFAULT 'SQUAD';
UPDATE mdm_team SET team_format = 'SOLO' WHERE is_individual = true;
CREATE INDEX IF NOT EXISTS idx_mdm_team_format ON mdm_team(team_format);
