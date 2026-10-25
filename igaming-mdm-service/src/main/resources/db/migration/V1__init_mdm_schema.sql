-- =========================================================================
-- V1__init_mdm_schema.sql
-- Master Data Management (MDM) Taxonomy & Ingestion Schema
-- =========================================================================

-- 1. Sports (Виды спорта)
CREATE TABLE IF NOT EXISTS mdm_sport (
    id SERIAL PRIMARY KEY,
    slug VARCHAR(64) NOT NULL UNIQUE,
    parent_id INT REFERENCES mdm_sport(id) ON DELETE RESTRICT,
    domain_key VARCHAR(64) NOT NULL,
    is_esports BOOLEAN NOT NULL DEFAULT FALSE,
    is_team_sport BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 100,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mdm_sport_domain_key ON mdm_sport(domain_key);

-- 2. Categories / Countries (Категории, Регионы, Страны)
CREATE TABLE IF NOT EXISTS mdm_category (
    id SERIAL PRIMARY KEY,
    sport_id INT NOT NULL REFERENCES mdm_sport(id) ON DELETE RESTRICT,
    slug VARCHAR(128) NOT NULL,
    code VARCHAR(16),
    sofascore_id BIGINT UNIQUE,
    flag_code VARCHAR(16),
    source_origin VARCHAR(32) NOT NULL DEFAULT 'SOFASCORE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_mdm_category_origin CHECK (source_origin IN ('SOFASCORE', 'NATIVE_LOCAL', 'OPERATOR')),
    CONSTRAINT uk_mdm_category_sport_slug UNIQUE (sport_id, slug)
);

CREATE INDEX idx_mdm_category_sport ON mdm_category(sport_id);
CREATE INDEX idx_mdm_category_sofascore ON mdm_category(sofascore_id);

-- 3. Tournaments / Leagues (Турниры, Лиги)
CREATE TABLE IF NOT EXISTS mdm_tournament (
    id BIGSERIAL PRIMARY KEY,
    sport_id INT NOT NULL REFERENCES mdm_sport(id) ON DELETE RESTRICT,
    category_id INT REFERENCES mdm_category(id) ON DELETE RESTRICT,
    slug VARCHAR(191) NOT NULL,
    sofascore_id BIGINT UNIQUE,
    canonical_name VARCHAR(255) NOT NULL,
    tier INT NOT NULL DEFAULT 3,
    gender VARCHAR(16) NOT NULL DEFAULT 'MALE',
    age_category VARCHAR(16) NOT NULL DEFAULT 'SENIOR',
    is_cup BOOLEAN NOT NULL DEFAULT FALSE,
    popularity_rank INT NOT NULL DEFAULT 1000,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    logo_url VARCHAR(512),
    source_origin VARCHAR(32) NOT NULL DEFAULT 'SOFASCORE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_mdm_tournament_gender CHECK (gender IN ('MALE', 'FEMALE', 'MIXED')),
    CONSTRAINT ck_mdm_tournament_age CHECK (age_category IN ('SENIOR', 'U23', 'U21', 'U19', 'U17', 'U16', 'RESERVE')),
    CONSTRAINT ck_mdm_tournament_origin CHECK (source_origin IN ('SOFASCORE', 'NATIVE_LOCAL', 'OPERATOR')),
    CONSTRAINT uk_mdm_tournament_sport_cat_slug UNIQUE (sport_id, category_id, slug)
);

CREATE INDEX idx_mdm_tournament_sport_cat ON mdm_tournament(sport_id, category_id);
CREATE INDEX idx_mdm_tournament_sofascore ON mdm_tournament(sofascore_id);
CREATE INDEX idx_mdm_tournament_age_gender ON mdm_tournament(age_category, gender);

-- 4. Teams / Clubs / Athletes (Команды, Клубы, Персональные спортсмены)
CREATE TABLE IF NOT EXISTS mdm_team (
    id BIGSERIAL PRIMARY KEY,
    sport_id INT NOT NULL REFERENCES mdm_sport(id) ON DELETE RESTRICT,
    category_id INT REFERENCES mdm_category(id) ON DELETE RESTRICT,
    slug VARCHAR(191) NOT NULL,
    sofascore_id BIGINT UNIQUE,
    canonical_name VARCHAR(255) NOT NULL,
    is_national BOOLEAN NOT NULL DEFAULT FALSE,
    is_individual BOOLEAN NOT NULL DEFAULT FALSE,
    gender VARCHAR(16) NOT NULL DEFAULT 'MALE',
    age_category VARCHAR(16) NOT NULL DEFAULT 'SENIOR',
    normalized_signature VARCHAR(255) NOT NULL,
    logo_url VARCHAR(512),
    popularity_rank INT NOT NULL DEFAULT 1000,
    source_origin VARCHAR(32) NOT NULL DEFAULT 'SOFASCORE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_mdm_team_gender CHECK (gender IN ('MALE', 'FEMALE', 'MIXED')),
    CONSTRAINT ck_mdm_team_age CHECK (age_category IN ('SENIOR', 'U23', 'U21', 'U19', 'U17', 'U16', 'RESERVE')),
    CONSTRAINT ck_mdm_team_origin CHECK (source_origin IN ('SOFASCORE', 'NATIVE_LOCAL', 'OPERATOR')),
    CONSTRAINT uk_mdm_team_sport_slug UNIQUE (sport_id, slug)
);

CREATE INDEX idx_mdm_team_sport ON mdm_team(sport_id);
CREATE INDEX idx_mdm_team_sofascore ON mdm_team(sofascore_id);
CREATE INDEX idx_mdm_team_signature ON mdm_team(sport_id, normalized_signature);
CREATE INDEX idx_mdm_team_age_gender ON mdm_team(age_category, gender, is_national);

-- 5. Team Metadata Satellite (Сателлит: Метаданные команды)
CREATE TABLE IF NOT EXISTS mdm_team_sofa_metadata (
    team_id BIGINT PRIMARY KEY REFERENCES mdm_team(id) ON DELETE CASCADE,
    coach_name VARCHAR(128),
    venue_name VARCHAR(191),
    venue_city VARCHAR(128),
    venue_capacity INT,
    sofascore_rating NUMERIC(4,2),
    form_recent VARCHAR(16),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Team DNA Satellite (Сателлит: Тактический профиль и ДНК команды)
CREATE TABLE IF NOT EXISTS mdm_team_dna (
    team_id BIGINT PRIMARY KEY REFERENCES mdm_team(id) ON DELETE CASCADE,
    primary_formation VARCHAR(32),
    play_style VARCHAR(64),
    pressing_intensity NUMERIC(5,2),
    possession_avg_pct NUMERIC(5,2),
    aerial_threat_score NUMERIC(5,2),
    defense_vulnerability_score NUMERIC(5,2),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. Players (Игроки / Спортсмены)
CREATE TABLE IF NOT EXISTS mdm_player (
    id BIGSERIAL PRIMARY KEY,
    sport_id INT NOT NULL REFERENCES mdm_sport(id) ON DELETE RESTRICT,
    slug VARCHAR(191) NOT NULL,
    sofascore_id BIGINT UNIQUE,
    first_name VARCHAR(128),
    last_name VARCHAR(128),
    common_name VARCHAR(191) NOT NULL,
    birth_date DATE,
    citizenship_category_id INT REFERENCES mdm_category(id) ON DELETE SET NULL,
    primary_position VARCHAR(32),
    photo_url VARCHAR(512),
    gender VARCHAR(16) NOT NULL DEFAULT 'MALE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_mdm_player_gender CHECK (gender IN ('MALE', 'FEMALE', 'MIXED')),
    CONSTRAINT uk_mdm_player_sport_slug UNIQUE (sport_id, slug)
);

CREATE INDEX idx_mdm_player_sport ON mdm_player(sport_id);
CREATE INDEX idx_mdm_player_sofascore ON mdm_player(sofascore_id);
CREATE INDEX idx_mdm_player_names ON mdm_player(last_name, first_name);

-- 8. Player Archetype Satellite (Сателлит: Тактический архетип игрока для AI-Аналитика)
CREATE TABLE IF NOT EXISTS mdm_player_archetype (
    player_id BIGINT PRIMARY KEY REFERENCES mdm_player(id) ON DELETE CASCADE,
    archetype VARCHAR(64),
    overall_rating NUMERIC(4,2),
    is_joker_candidate BOOLEAN NOT NULL DEFAULT FALSE,
    preferred_foot VARCHAR(16),
    pace_rating NUMERIC(4,2),
    shooting_rating NUMERIC(4,2),
    passing_rating NUMERIC(4,2),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mdm_player_archetype_joker ON mdm_player_archetype(is_joker_candidate);

-- 9. Team Membership (Связь Игрок-Команда: клуб + сборная одновременно)
CREATE TABLE IF NOT EXISTS mdm_team_membership (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL REFERENCES mdm_player(id) ON DELETE CASCADE,
    team_id BIGINT NOT NULL REFERENCES mdm_team(id) ON DELETE CASCADE,
    membership_type VARCHAR(32) NOT NULL,
    jersey_number INT,
    squad_role VARCHAR(32) NOT NULL DEFAULT 'ROTATION',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    contract_until DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_mdm_membership_type CHECK (membership_type IN ('CLUB_PRIMARY', 'NATIONAL_TEAM', 'ON_LOAN', 'YOUTH_ACADEMY')),
    CONSTRAINT ck_mdm_squad_role CHECK (squad_role IN ('STARTER', 'ROTATION', 'RESERVE')),
    CONSTRAINT uk_mdm_player_team_membership UNIQUE (player_id, team_id, membership_type)
);

CREATE INDEX idx_mdm_membership_player ON mdm_team_membership(player_id);
CREATE INDEX idx_mdm_membership_team ON mdm_team_membership(team_id);
CREATE INDEX idx_mdm_membership_active ON mdm_team_membership(team_id, is_active);

-- 10. External Systems & Ingestion Mappings (Пакеты ингестии краулеров)
CREATE TABLE IF NOT EXISTS mdm_external_system (
    id SERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS mdm_sport_ingestion_map (
    id BIGSERIAL PRIMARY KEY,
    external_system_id INT NOT NULL REFERENCES mdm_external_system(id) ON DELETE CASCADE,
    raw_key VARCHAR(128) NOT NULL,
    canonical_sport_id INT NOT NULL REFERENCES mdm_sport(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_mdm_sport_map UNIQUE (external_system_id, raw_key)
);

CREATE INDEX idx_mdm_sport_map_lookup ON mdm_sport_ingestion_map(external_system_id, raw_key);

CREATE TABLE IF NOT EXISTS mdm_tournament_ingestion_map (
    id BIGSERIAL PRIMARY KEY,
    external_system_id INT NOT NULL REFERENCES mdm_external_system(id) ON DELETE CASCADE,
    raw_key VARCHAR(255) NOT NULL,
    canonical_tournament_id BIGINT NOT NULL REFERENCES mdm_tournament(id) ON DELETE CASCADE,
    raw_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_mdm_tournament_map UNIQUE (external_system_id, raw_key)
);

CREATE INDEX idx_mdm_tourn_map_lookup ON mdm_tournament_ingestion_map(external_system_id, raw_key);

CREATE TABLE IF NOT EXISTS mdm_team_ingestion_map (
    id BIGSERIAL PRIMARY KEY,
    external_system_id INT NOT NULL REFERENCES mdm_external_system(id) ON DELETE CASCADE,
    raw_key VARCHAR(255) NOT NULL,
    canonical_team_id BIGINT NOT NULL REFERENCES mdm_team(id) ON DELETE CASCADE,
    raw_name VARCHAR(255),
    is_verified BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_mdm_team_map UNIQUE (external_system_id, raw_key)
);

CREATE INDEX idx_mdm_team_map_lookup ON mdm_team_ingestion_map(external_system_id, raw_key);

-- 11. Multilingual Translations (Локализация)
CREATE TABLE IF NOT EXISTS mdm_tournament_translation (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES mdm_tournament(id) ON DELETE CASCADE,
    lang VARCHAR(8) NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_mdm_tourn_trans UNIQUE (tournament_id, lang)
);

CREATE TABLE IF NOT EXISTS mdm_team_translation (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES mdm_team(id) ON DELETE CASCADE,
    lang VARCHAR(8) NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_mdm_team_trans UNIQUE (team_id, lang)
);
