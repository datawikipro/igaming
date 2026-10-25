-- =========================================================================
-- V2__init_mdm_match.sql
-- Master Data Management (MDM) Match Entity Schema with Composite Foreign Keys
-- Guarantees cross-sport integrity (no football teams in hockey matches)
-- =========================================================================

-- 1. Ensure composite unique constraints on parent entities for composite FK referencing
ALTER TABLE mdm_tournament 
    ADD CONSTRAINT uk_mdm_tournament_id_sport UNIQUE (id, sport_id);

ALTER TABLE mdm_team 
    ADD CONSTRAINT uk_mdm_team_id_sport UNIQUE (id, sport_id);

-- 2. Master Match Table
CREATE TABLE IF NOT EXISTS mdm_match (
    id BIGSERIAL PRIMARY KEY,
    sport_id INT NOT NULL REFERENCES mdm_sport(id) ON DELETE RESTRICT,
    tournament_id BIGINT NOT NULL,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    sofascore_id BIGINT UNIQUE,
    source_origin VARCHAR(32) NOT NULL DEFAULT 'SOFASCORE',
    created_by_source VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED',
    score1 VARCHAR(16),
    score2 VARCHAR(16),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Composite Foreign Keys guaranteeing sport integrity
    CONSTRAINT fk_mdm_match_tournament FOREIGN KEY (tournament_id, sport_id) 
        REFERENCES mdm_tournament(id, sport_id) ON DELETE RESTRICT,
    CONSTRAINT fk_mdm_match_home_team FOREIGN KEY (home_team_id, sport_id) 
        REFERENCES mdm_team(id, sport_id) ON DELETE RESTRICT,
    CONSTRAINT fk_mdm_match_away_team FOREIGN KEY (away_team_id, sport_id) 
        REFERENCES mdm_team(id, sport_id) ON DELETE RESTRICT,
        
    -- Business integrity constraints
    CONSTRAINT ck_mdm_match_distinct_teams CHECK (home_team_id <> away_team_id),
    CONSTRAINT ck_mdm_match_origin CHECK (source_origin IN ('SOFASCORE', 'BOOKMAKER', 'OPERATOR')),
    CONSTRAINT ck_mdm_match_status CHECK (status IN ('SCHEDULED', 'LIVE', 'IN_PLAY', 'FINISHED', 'CANCELLED', 'POSTPONED', 'INTERRUPTED'))
);

-- 3. High-Performance Indexes for O(1) Match Resolution and Blueprint queries
CREATE INDEX IF NOT EXISTS idx_mdm_match_sofascore ON mdm_match(sofascore_id);
CREATE INDEX IF NOT EXISTS idx_mdm_match_teams_time ON mdm_match(sport_id, home_team_id, away_team_id, start_time);
CREATE INDEX IF NOT EXISTS idx_mdm_match_reverse_teams_time ON mdm_match(sport_id, away_team_id, home_team_id, start_time);
CREATE INDEX IF NOT EXISTS idx_mdm_match_active ON mdm_match(status, start_time);
CREATE INDEX IF NOT EXISTS idx_mdm_match_tourn_time ON mdm_match(tournament_id, start_time);
