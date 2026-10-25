-- =========================================================================
-- V3__init_mdm_tournament_team.sql
-- Master Data Management (MDM) Tournament Roster & League Participants
-- Prevents cross-tier team collisions (e.g. 3rd division clubs in top leagues)
-- =========================================================================

CREATE TABLE IF NOT EXISTS mdm_tournament_team (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    sport_id INT NOT NULL REFERENCES mdm_sport(id) ON DELETE RESTRICT,
    season VARCHAR(32),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Composite Foreign Keys guaranteeing sport integrity
    CONSTRAINT fk_mdm_tourn_team_tournament FOREIGN KEY (tournament_id, sport_id)
        REFERENCES mdm_tournament(id, sport_id) ON DELETE CASCADE,
    CONSTRAINT fk_mdm_tourn_team_team FOREIGN KEY (team_id, sport_id)
        REFERENCES mdm_team(id, sport_id) ON DELETE CASCADE,

    -- Unique participant constraint per tournament & season
    CONSTRAINT uk_mdm_tournament_team UNIQUE (tournament_id, team_id)
);

CREATE INDEX IF NOT EXISTS idx_mdm_tourn_team_lookup ON mdm_tournament_team(tournament_id, team_id);
CREATE INDEX IF NOT EXISTS idx_mdm_team_tourn_lookup ON mdm_tournament_team(team_id, tournament_id);
CREATE INDEX IF NOT EXISTS idx_mdm_tourn_team_active ON mdm_tournament_team(tournament_id, is_active);
