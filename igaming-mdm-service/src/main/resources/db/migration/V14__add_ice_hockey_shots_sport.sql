-- =========================================================================
-- V14__add_ice_hockey_shots_sport.sql
-- Add ICE_HOCKEY_SHOTS sport taxonomy, auto-satellite triggers & data cleanup
-- =========================================================================

-- 1. Ensure ICE_HOCKEY_SHOTS sport exists in mdm_sport
INSERT INTO mdm_sport (id, slug, parent_id, domain_key, is_esports, is_team_sport, sort_order)
VALUES 
    (44, 'ice-hockey-shots', 2, 'ICE_HOCKEY_SHOTS', FALSE, TRUE, 131)
ON CONFLICT (id) DO NOTHING;

SELECT setval('mdm_sport_id_seq', GREATEST(44, (SELECT COALESCE(MAX(id), 0) FROM mdm_sport)));

-- 2. Clean up and normalize mutant Senior Hockey teams in sport_id = 2
-- Avtomobilist
UPDATE mdm_team 
SET canonical_name = 'Автомобилист', slug = 'avtomobilist', normalized_signature = 'AVTOMOBILIST', updated_at = NOW()
WHERE id = 161138 AND sport_id = 2;

-- Avangard
UPDATE mdm_team 
SET canonical_name = 'Авангард', slug = 'avangard', normalized_signature = 'AVANGARD', updated_at = NOW()
WHERE id = 161149 AND sport_id = 2;

-- Sibir
UPDATE mdm_team 
SET canonical_name = 'Сибирь', slug = 'sibir', normalized_signature = 'SIBIR', updated_at = NOW()
WHERE id = 161137 AND sport_id = 2;

-- Barys
UPDATE mdm_team 
SET canonical_name = 'Барыс', slug = 'barys', normalized_signature = 'BARYS', updated_at = NOW()
WHERE id = 161139 AND sport_id = 2;

-- Detroit Red Wings
UPDATE mdm_team 
SET canonical_name = 'Детройт Ред Уингс', slug = 'detroyt-red-uings', normalized_signature = 'DETROYT_RED_WINGS', updated_at = NOW()
WHERE id = 161210 AND sport_id = 2;

-- 3. Create missing Senior Hockey teams in sport_id = 2 if absent
INSERT INTO mdm_team (
    sport_id, category_id, slug, canonical_name, is_national, is_individual,
    gender, age_category, normalized_signature, country_code, country_name, country_id, popularity_rank, source_origin
)
SELECT 
    2, NULL, 'spartak-moscow', 'Спартак Москва', FALSE, FALSE,
    'MALE', 'SENIOR', 'SPARTAK_MOSCOW', 'RU', 'Russia', 769, 100, 'NATIVE_LOCAL'
WHERE NOT EXISTS (SELECT 1 FROM mdm_team WHERE sport_id = 2 AND slug = 'spartak-moscow');

INSERT INTO mdm_team (
    sport_id, category_id, slug, canonical_name, is_national, is_individual,
    gender, age_category, normalized_signature, country_code, country_name, country_id, popularity_rank, source_origin
)
SELECT 
    2, NULL, 'traktor', 'Трактор', FALSE, FALSE,
    'MALE', 'SENIOR', 'TRAKTOR', 'RU', 'Russia', 769, 100, 'NATIVE_LOCAL'
WHERE NOT EXISTS (SELECT 1 FROM mdm_team WHERE sport_id = 2 AND slug = 'traktor');

-- 5. Ensure mdm_organization exists for all Senior Male Ice Hockey teams & link organization_id
INSERT INTO mdm_organization (canonical_name, slug, country_id, country_name, logo_url)
SELECT DISTINCT ON (t.slug) 
    t.canonical_name, t.slug, t.country_id, t.country_name, t.logo_url
FROM mdm_team t
WHERE t.sport_id = 2
  AND t.age_category = 'SENIOR'
  AND t.gender = 'MALE'
  AND t.canonical_name NOT ILIKE '%броск%'
  AND t.canonical_name NOT ILIKE '%shots%'
  AND t.canonical_name NOT ILIKE '%удален%'
  AND t.canonical_name NOT ILIKE '%вбрас%'
  AND NOT EXISTS (SELECT 1 FROM mdm_organization o WHERE o.slug = t.slug)
ORDER BY t.slug, t.id ASC
ON CONFLICT DO NOTHING;

UPDATE mdm_team t
SET organization_id = o.id
FROM mdm_organization o
WHERE t.sport_id = 2
  AND t.organization_id IS NULL
  AND t.slug = o.slug;

-- 6. Trigger: Auto-generate Shots on Goal satellite team for Senior Male Ice Hockey teams
CREATE OR REPLACE FUNCTION trg_mdm_team_auto_hockey_shots_satellite()
RETURNS TRIGGER AS $$
DECLARE
    v_shots_slug VARCHAR(191);
    v_shots_name VARCHAR(255);
    v_shots_sig VARCHAR(255);
BEGIN
    IF NEW.sport_id = 2 AND NEW.age_category = 'SENIOR' AND NEW.gender = 'MALE' 
       AND NEW.canonical_name NOT ILIKE '%броск%' 
       AND NEW.canonical_name NOT ILIKE '%shots%'
       AND NEW.canonical_name NOT ILIKE '%удален%' 
       AND NEW.canonical_name NOT ILIKE '%вбрас%' THEN
        v_shots_slug := NEW.slug || '-shots';
        v_shots_name := NEW.canonical_name || ' (Броски в створ)';
        v_shots_sig := 'HOCKEY_SHOTS_' || NEW.normalized_signature;
        
        INSERT INTO mdm_team (
            sport_id, category_id, slug, canonical_name, is_national, is_individual,
            gender, age_category, normalized_signature, logo_url, country_code,
            country_name, country_id, popularity_rank, source_origin, organization_id
        )
        VALUES (
            44, NEW.category_id, v_shots_slug, v_shots_name, NEW.is_national, NEW.is_individual,
            'MALE', 'SENIOR', v_shots_sig, NEW.logo_url, NEW.country_code,
            NEW.country_name, NEW.country_id, NEW.popularity_rank, 'NATIVE_LOCAL', NEW.organization_id
        )
        ON CONFLICT (sport_id, slug) DO UPDATE
        SET organization_id = EXCLUDED.organization_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_mdm_team_auto_hockey_shots_satellite_ai ON mdm_team;
CREATE TRIGGER trg_mdm_team_auto_hockey_shots_satellite_ai
    AFTER INSERT ON mdm_team
    FOR EACH ROW
    EXECUTE FUNCTION trg_mdm_team_auto_hockey_shots_satellite();

-- 7. Populate Shots on Goal satellite teams in sport_id = 44 for all existing Senior Hockey teams
INSERT INTO mdm_team (
    sport_id, category_id, slug, canonical_name, is_national, is_individual,
    gender, age_category, normalized_signature, logo_url, country_code,
    country_name, country_id, popularity_rank, source_origin, organization_id
)
SELECT 
    44, t.category_id, t.slug || '-shots', t.canonical_name || ' (Броски в створ)', t.is_national, t.is_individual,
    'MALE', 'SENIOR', 'HOCKEY_SHOTS_' || t.normalized_signature, t.logo_url, t.country_code,
    t.country_name, t.country_id, t.popularity_rank, 'NATIVE_LOCAL', t.organization_id
FROM mdm_team t
WHERE t.sport_id = 2
  AND t.age_category = 'SENIOR'
  AND t.gender = 'MALE'
  AND t.canonical_name NOT ILIKE '%броск%'
  AND t.canonical_name NOT ILIKE '%shots%'
  AND t.canonical_name NOT ILIKE '%удален%'
  AND t.canonical_name NOT ILIKE '%вбрас%'
  AND NOT EXISTS (
      SELECT 1 FROM mdm_team c WHERE c.sport_id = 44 AND c.slug = t.slug || '-shots'
  )
ON CONFLICT (sport_id, slug) DO NOTHING;

-- 8. Create canonical Tournaments for Shots on Goal
INSERT INTO mdm_tournament (sport_id, slug, canonical_name, popularity_rank)
SELECT 44, 'khl-shots', 'КХЛ. Броски в створ', 100
WHERE NOT EXISTS (SELECT 1 FROM mdm_tournament WHERE sport_id = 44 AND slug = 'khl-shots');

INSERT INTO mdm_tournament (sport_id, slug, canonical_name, popularity_rank)
SELECT 44, 'nhl-shots', 'НХЛ. Броски в створ', 100
WHERE NOT EXISTS (SELECT 1 FROM mdm_tournament WHERE sport_id = 44 AND slug = 'nhl-shots');

INSERT INTO mdm_tournament (sport_id, slug, canonical_name, popularity_rank)
SELECT 44, 'vhl-shots', 'ВХЛ. Броски в створ', 50
WHERE NOT EXISTS (SELECT 1 FROM mdm_tournament WHERE sport_id = 44 AND slug = 'vhl-shots');

-- 9. Register sport ingestion mappings for football corners (43) and hockey shots (44) across all external systems
INSERT INTO mdm_sport_ingestion_map (external_system_id, raw_key, canonical_sport_id)
SELECT es.id, k.raw_key, k.canonical_sport_id
FROM mdm_external_system es
CROSS JOIN (
    VALUES 
        ('FOOTBALL_CORNERS', 43),
        ('football-corners', 43),
        ('CORNERS', 43),
        ('corners', 43),
        ('угловые', 43),
        ('ICE_HOCKEY_SHOTS', 44),
        ('ice-hockey-shots', 44),
        ('SHOTS', 44),
        ('shots', 44),
        ('shots on goal', 44),
        ('броски', 44),
        ('броски в створ', 44)
) AS k(raw_key, canonical_sport_id)
ON CONFLICT (external_system_id, raw_key) DO UPDATE
SET canonical_sport_id = EXCLUDED.canonical_sport_id;

-- 10. Repoint bookmaker raw ingestion mappings for shots on goal to sport_id = 44 satellite teams
-- Repoint Avangard shots
UPDATE mdm_team_ingestion_map
SET canonical_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'avangard-shots' LIMIT 1)
WHERE raw_name IN ('Авангард (Броски)', 'Авангард(броски)', 'Авангард (броски в створ)');

-- Repoint Avtomobilist shots
UPDATE mdm_team_ingestion_map
SET canonical_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'avtomobilist-shots' LIMIT 1)
WHERE raw_name IN ('Автомобилист(броски)', 'Автомобилист (броски в створ)');

-- Repoint Sibir shots
UPDATE mdm_team_ingestion_map
SET canonical_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'sibir-shots' LIMIT 1)
WHERE raw_name IN ('Сибирь(броски)', 'Сибирь (броски в створ)');

-- Repoint Barys shots
UPDATE mdm_team_ingestion_map
SET canonical_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'barys-shots' LIMIT 1)
WHERE raw_name IN ('Барыс (Броски)', 'Барыс(броски)', 'Барыс (броски в створ)');

-- Repoint Spartak shots
UPDATE mdm_team_ingestion_map
SET canonical_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'spartak-moscow-shots' LIMIT 1)
WHERE raw_name IN ('Спартак Москва (Броски)', 'Спартак(броски)');

-- Repoint Traktor shots
UPDATE mdm_team_ingestion_map
SET canonical_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'traktor-shots' LIMIT 1)
WHERE raw_name IN ('Трактор (Броски)', 'Трактор(броски)');

-- Repoint Philadelphia shots
UPDATE mdm_team_ingestion_map
SET canonical_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'philadelphia-flyers-3699-shots' LIMIT 1)
WHERE raw_name IN ('Филадельфия (Броски)', 'Филадельфия(броски)');

-- Repoint Calgary shots
UPDATE mdm_team_ingestion_map
SET canonical_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'calgary-flames-3679-shots' LIMIT 1)
WHERE raw_name IN ('Калгари (Броски)', 'Калгари(броски)');

-- 10. Reclassify stat matches to sport_id = 44
-- Match 1344583: Ak Bars vs Avangard shots
UPDATE mdm_match
SET sport_id = 44,
    tournament_id = (SELECT id FROM mdm_tournament WHERE sport_id = 44 AND slug = 'khl-shots' LIMIT 1),
    home_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'ak-bars-kazan-3947-shots' LIMIT 1),
    away_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'avangard-shots' LIMIT 1),
    updated_at = NOW()
WHERE id = 1344583;

-- Match 1345705: Edmonton Oilers vs Calgary shots
UPDATE mdm_match
SET sport_id = 44,
    tournament_id = (SELECT id FROM mdm_tournament WHERE sport_id = 44 AND slug = 'nhl-shots' LIMIT 1),
    home_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'edmonton-oilers-3686-shots' LIMIT 1),
    away_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'calgary-flames-3679-shots' LIMIT 1),
    updated_at = NOW()
WHERE id = 1345705;

-- Match 1359394: Severstal vs Spartak shots
UPDATE mdm_match
SET sport_id = 44,
    tournament_id = (SELECT id FROM mdm_tournament WHERE sport_id = 44 AND slug = 'khl-shots' LIMIT 1),
    home_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'severstal-cherepovets-3943-shots' LIMIT 1),
    away_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'spartak-moscow-shots' LIMIT 1),
    updated_at = NOW()
WHERE id = 1359394;

-- Match 1365904: Washington vs Philadelphia shots
UPDATE mdm_match
SET sport_id = 44,
    tournament_id = (SELECT id FROM mdm_tournament WHERE sport_id = 44 AND slug = 'nhl-shots' LIMIT 1),
    home_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'washington-capitals-3691-shots' LIMIT 1),
    away_team_id = (SELECT id FROM mdm_team WHERE sport_id = 44 AND slug = 'philadelphia-flyers-3699-shots' LIMIT 1),
    updated_at = NOW()
WHERE id = 1365904;

-- Delete garbage cross-sport mutant match 1358953 (CSKA Sofia vs Traktor (Броски) in football)
DELETE FROM mdm_match WHERE id = 1358953;
