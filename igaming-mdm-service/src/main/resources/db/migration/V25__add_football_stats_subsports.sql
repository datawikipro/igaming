-- =========================================================================
-- V25__add_football_stats_subsports.sql
-- Add Football Statistical Sub-Sports: Yellow Cards, Fouls, Offsides, Goal Kicks
-- =========================================================================

-- 1. Ensure statistical sub-sports exist in mdm_sport
INSERT INTO mdm_sport (id, slug, parent_id, domain_key, is_esports, is_team_sport, sort_order)
VALUES 
    (46, 'football-yellow-cards', 1, 'FOOTBALL_YELLOW_CARDS', FALSE, TRUE, 132),
    (47, 'football-fouls', 1, 'FOOTBALL_FOULS', FALSE, TRUE, 133),
    (48, 'football-offsides', 1, 'FOOTBALL_OFFSIDES', FALSE, TRUE, 134),
    (49, 'football-goal-kicks', 1, 'FOOTBALL_GOAL_KICKS', FALSE, TRUE, 135)
ON CONFLICT (id) DO NOTHING;

SELECT setval('mdm_sport_id_seq', GREATEST(49, (SELECT COALESCE(MAX(id), 0) FROM mdm_sport)));

-- 2. Trigger: Auto-generate Statistical Satellite Teams for Senior Male Football teams
CREATE OR REPLACE FUNCTION trg_mdm_team_auto_football_stats_satellites()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.sport_id = 1 AND NEW.age_category = 'SENIOR' AND NEW.gender = 'MALE' 
       AND NEW.canonical_name NOT ILIKE '%углов%'
       AND NEW.canonical_name NOT ILIKE '%corners%'
       AND NEW.canonical_name NOT ILIKE '%жк%'
       AND NEW.canonical_name NOT ILIKE '%желт%'
       AND NEW.canonical_name NOT ILIKE '%фол%'
       AND NEW.canonical_name NOT ILIKE '%офсайд%'
       AND NEW.canonical_name NOT ILIKE '%удар%от%ворот%' THEN

        -- 46: Yellow Cards
        INSERT INTO mdm_team (
            sport_id, category_id, slug, canonical_name, is_national, is_individual,
            gender, age_category, normalized_signature, logo_url, country_code,
            country_name, country_id, popularity_rank, source_origin, organization_id
        )
        VALUES (
            46, NEW.category_id, NEW.slug || '-yellow-cards', NEW.canonical_name || ' (ЖК)', NEW.is_national, NEW.is_individual,
            'MALE', 'SENIOR', 'YELLOW_CARDS_' || NEW.normalized_signature, NEW.logo_url, NEW.country_code,
            NEW.country_name, NEW.country_id, NEW.popularity_rank, 'NATIVE_LOCAL', NEW.organization_id
        )
        ON CONFLICT (sport_id, slug) DO UPDATE
        SET organization_id = EXCLUDED.organization_id;

        -- 47: Fouls
        INSERT INTO mdm_team (
            sport_id, category_id, slug, canonical_name, is_national, is_individual,
            gender, age_category, normalized_signature, logo_url, country_code,
            country_name, country_id, popularity_rank, source_origin, organization_id
        )
        VALUES (
            47, NEW.category_id, NEW.slug || '-fouls', NEW.canonical_name || ' (Фолы)', NEW.is_national, NEW.is_individual,
            'MALE', 'SENIOR', 'FOULS_' || NEW.normalized_signature, NEW.logo_url, NEW.country_code,
            NEW.country_name, NEW.country_id, NEW.popularity_rank, 'NATIVE_LOCAL', NEW.organization_id
        )
        ON CONFLICT (sport_id, slug) DO UPDATE
        SET organization_id = EXCLUDED.organization_id;

        -- 48: Offsides
        INSERT INTO mdm_team (
            sport_id, category_id, slug, canonical_name, is_national, is_individual,
            gender, age_category, normalized_signature, logo_url, country_code,
            country_name, country_id, popularity_rank, source_origin, organization_id
        )
        VALUES (
            48, NEW.category_id, NEW.slug || '-offsides', NEW.canonical_name || ' (Офсайды)', NEW.is_national, NEW.is_individual,
            'MALE', 'SENIOR', 'OFFSIDES_' || NEW.normalized_signature, NEW.logo_url, NEW.country_code,
            NEW.country_name, NEW.country_id, NEW.popularity_rank, 'NATIVE_LOCAL', NEW.organization_id
        )
        ON CONFLICT (sport_id, slug) DO UPDATE
        SET organization_id = EXCLUDED.organization_id;

        -- 49: Goal Kicks
        INSERT INTO mdm_team (
            sport_id, category_id, slug, canonical_name, is_national, is_individual,
            gender, age_category, normalized_signature, logo_url, country_code,
            country_name, country_id, popularity_rank, source_origin, organization_id
        )
        VALUES (
            49, NEW.category_id, NEW.slug || '-goal-kicks', NEW.canonical_name || ' (Удары от ворот)', NEW.is_national, NEW.is_individual,
            'MALE', 'SENIOR', 'GOAL_KICKS_' || NEW.normalized_signature, NEW.logo_url, NEW.country_code,
            NEW.country_name, NEW.country_id, NEW.popularity_rank, 'NATIVE_LOCAL', NEW.organization_id
        )
        ON CONFLICT (sport_id, slug) DO UPDATE
        SET organization_id = EXCLUDED.organization_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_mdm_team_auto_football_stats_satellites_ai ON mdm_team;
CREATE TRIGGER trg_mdm_team_auto_football_stats_satellites_ai
    AFTER INSERT ON mdm_team
    FOR EACH ROW
    EXECUTE FUNCTION trg_mdm_team_auto_football_stats_satellites();

-- 3. Register ingestion mappings across all external systems
INSERT INTO mdm_sport_ingestion_map (external_system_id, raw_key, canonical_sport_id)
SELECT es.id, k.raw_key, k.canonical_sport_id
FROM mdm_external_system es
CROSS JOIN (
    VALUES 
        -- 46: Yellow Cards
        ('FOOTBALL_YELLOW_CARDS', 46),
        ('football-yellow-cards', 46),
        ('YELLOW_CARDS', 46),
        ('yellow-cards', 46),
        ('yellow cards', 46),
        ('bookings', 46),
        ('cards', 46),
        ('жк', 46),
        ('ж.к.', 46),
        ('желтые карточки', 46),
        ('карточки', 46),
        ('футбол жк', 46),
        ('футбол (жк)', 46),
        ('cartons jaunes', 46),
        ('tarjetas amarillas', 46),

        -- 47: Fouls
        ('FOOTBALL_FOULS', 47),
        ('football-fouls', 47),
        ('FOULS', 47),
        ('fouls', 47),
        ('фолы', 47),
        ('нарушения', 47),
        ('футбол фолы', 47),
        ('футбол (фолы)', 47),
        ('fautes', 47),
        ('faltas', 47),

        -- 48: Offsides
        ('FOOTBALL_OFFSIDES', 48),
        ('football-offsides', 48),
        ('OFFSIDES', 48),
        ('offsides', 48),
        ('офсайды', 48),
        ('вне игры', 48),
        ('футбол офсайды', 48),
        ('футбол (офсайды)', 48),
        ('hors-jeu', 48),
        ('fueras de juego', 48),

        -- 49: Goal Kicks
        ('FOOTBALL_GOAL_KICKS', 49),
        ('football-goal-kicks', 49),
        ('GOAL_KICKS', 49),
        ('goal-kicks', 49),
        ('goal kicks', 49),
        ('удары от ворот', 49),
        ('от ворот', 49),
        ('футбол удары от ворот', 49),
        ('футбол (удары от ворот)', 49),
        ('saques de puerta', 49),
        ('six mètres', 49)
) AS k(raw_key, canonical_sport_id)
ON CONFLICT (external_system_id, raw_key) DO UPDATE
SET canonical_sport_id = EXCLUDED.canonical_sport_id;
