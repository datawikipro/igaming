-- =========================================================================
-- V13__add_organization_hierarchy.sql
-- Add Sports Organization / Club Entity & Auto-Satellite Management
-- =========================================================================

-- 1. Ensure FOOTBALL_CORNERS sport exists
INSERT INTO mdm_sport (id, slug, parent_id, domain_key, is_esports, is_team_sport, sort_order)
VALUES 
    (43, 'football-corners', 1, 'FOOTBALL_CORNERS', FALSE, TRUE, 130)
ON CONFLICT (id) DO NOTHING;

SELECT setval('mdm_sport_id_seq', GREATEST(43, (SELECT COALESCE(MAX(id), 0) FROM mdm_sport)));

-- 2. Master Sports Organization (Club / Franchise / Esports Org)
CREATE TABLE IF NOT EXISTS mdm_organization (
    id BIGSERIAL PRIMARY KEY,
    canonical_name VARCHAR(255) NOT NULL,
    slug VARCHAR(191) NOT NULL,
    country_id INT REFERENCES mdm_country(id) ON DELETE RESTRICT,
    country_name VARCHAR(128),
    city VARCHAR(128),
    foundation_year INT,
    logo_url VARCHAR(512),
    academy_tier VARCHAR(32) NOT NULL DEFAULT 'STANDARD',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mdm_organization_slug ON mdm_organization(slug);
CREATE INDEX IF NOT EXISTS idx_mdm_organization_country ON mdm_organization(country_id);
CREATE INDEX IF NOT EXISTS idx_mdm_organization_name ON mdm_organization(lower(canonical_name));

-- 3. Link mdm_team to mdm_organization
ALTER TABLE mdm_team 
    ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES mdm_organization(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_mdm_team_organization ON mdm_team(organization_id);

-- 4. Initial Population of mdm_organization from existing Senior Male Football Teams
INSERT INTO mdm_organization (canonical_name, slug, country_id, country_name, logo_url)
SELECT DISTINCT ON (t.slug) 
    t.canonical_name, t.slug, t.country_id, t.country_name, t.logo_url
FROM mdm_team t
WHERE t.sport_id = 1
  AND t.age_category = 'SENIOR'
  AND t.gender = 'MALE'
  AND NOT EXISTS (SELECT 1 FROM mdm_organization o WHERE o.slug = t.slug)
ORDER BY t.slug, t.id ASC
ON CONFLICT DO NOTHING;

-- Link existing football teams to their new organizations
UPDATE mdm_team t
SET organization_id = o.id
FROM mdm_organization o
WHERE t.organization_id IS NULL
  AND t.slug = o.slug;

-- 5. Trigger: Auto-assign or create mdm_organization on new mdm_team insert
CREATE OR REPLACE FUNCTION trg_mdm_team_auto_organization()
RETURNS TRIGGER AS $$
DECLARE
    v_org_id BIGINT;
    v_base_slug VARCHAR(191);
    v_base_name VARCHAR(255);
BEGIN
    IF NEW.organization_id IS NULL THEN
        -- Strip youth or women suffix to find base club identity
        v_base_slug := regexp_replace(NEW.slug, '-(?:u1[5-9]|u2[0-3]|youth|women|fem)$', '', 'i');
        v_base_name := regexp_replace(NEW.canonical_name, '\\s*\\((?:U1[5-9]|U2[0-3]|Youth|Women|W)\\)$', '', 'i');
        
        SELECT id INTO v_org_id 
        FROM mdm_organization 
        WHERE slug = v_base_slug 
           OR lower(canonical_name) = lower(v_base_name)
        LIMIT 1;
        
        IF v_org_id IS NULL THEN
            INSERT INTO mdm_organization (canonical_name, slug, country_id, country_name, logo_url)
            VALUES (v_base_name, v_base_slug, NEW.country_id, NEW.country_name, NEW.logo_url)
            RETURNING id INTO v_org_id;
        END IF;
        
        NEW.organization_id := v_org_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_mdm_team_auto_organization_bi ON mdm_team;
CREATE TRIGGER trg_mdm_team_auto_organization_bi
    BEFORE INSERT ON mdm_team
    FOR EACH ROW
    EXECUTE FUNCTION trg_mdm_team_auto_organization();

-- 6. Trigger: Auto-generate Corners satellite team for Senior Male Football teams
CREATE OR REPLACE FUNCTION trg_mdm_team_auto_corners_satellite()
RETURNS TRIGGER AS $$
DECLARE
    v_corners_slug VARCHAR(191);
    v_corners_name VARCHAR(255);
    v_corners_sig VARCHAR(255);
BEGIN
    IF NEW.sport_id = 1 AND NEW.age_category = 'SENIOR' AND NEW.gender = 'MALE' THEN
        v_corners_slug := NEW.slug || '-corners';
        v_corners_name := NEW.canonical_name || ' (Corners)';
        v_corners_sig := 'CORNERS_' || NEW.normalized_signature;
        
        INSERT INTO mdm_team (
            sport_id, category_id, slug, canonical_name, is_national, is_individual,
            gender, age_category, normalized_signature, logo_url, country_code,
            country_name, country_id, popularity_rank, source_origin, organization_id
        )
        VALUES (
            43, NEW.category_id, v_corners_slug, v_corners_name, NEW.is_national, NEW.is_individual,
            'MALE', 'SENIOR', v_corners_sig, NEW.logo_url, NEW.country_code,
            NEW.country_name, NEW.country_id, NEW.popularity_rank, 'NATIVE_LOCAL', NEW.organization_id
        )
        ON CONFLICT (sport_id, slug) DO UPDATE
        SET organization_id = EXCLUDED.organization_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_mdm_team_auto_corners_satellite_ai ON mdm_team;
CREATE TRIGGER trg_mdm_team_auto_corners_satellite_ai
    AFTER INSERT ON mdm_team
    FOR EACH ROW
    EXECUTE FUNCTION trg_mdm_team_auto_corners_satellite();

-- 7. Populate Corners satellite teams for all currently existing Senior Male Football teams
INSERT INTO mdm_team (
    sport_id, category_id, slug, canonical_name, is_national, is_individual,
    gender, age_category, normalized_signature, logo_url, country_code,
    country_name, country_id, popularity_rank, source_origin, organization_id
)
SELECT 
    43, t.category_id, t.slug || '-corners', t.canonical_name || ' (Corners)', t.is_national, t.is_individual,
    'MALE', 'SENIOR', 'CORNERS_' || t.normalized_signature, t.logo_url, t.country_code,
    t.country_name, t.country_id, t.popularity_rank, 'NATIVE_LOCAL', t.organization_id
FROM mdm_team t
WHERE t.sport_id = 1
  AND t.age_category = 'SENIOR'
  AND t.gender = 'MALE'
  AND NOT EXISTS (
      SELECT 1 FROM mdm_team c WHERE c.sport_id = 43 AND c.slug = t.slug || '-corners'
  )
ON CONFLICT (sport_id, slug) DO NOTHING;
