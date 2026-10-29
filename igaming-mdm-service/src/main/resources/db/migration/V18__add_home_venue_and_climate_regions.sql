-- V18: Add home_venue_id to mdm_team and geo_region to mdm_country for climate/season Graph embeddings

-- 1. Add home_venue_id to mdm_team
ALTER TABLE mdm_team ADD COLUMN IF NOT EXISTS home_venue_id BIGINT REFERENCES mdm_venue(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_mdm_team_home_venue ON mdm_team(home_venue_id);

-- 2. Add geo_region to mdm_country
ALTER TABLE mdm_country ADD COLUMN IF NOT EXISTS geo_region VARCHAR(64) DEFAULT 'GLOBAL';
CREATE INDEX IF NOT EXISTS idx_mdm_country_geo_region ON mdm_country(geo_region);

-- 3. Populate macro-climate regions for graph embeddings
UPDATE mdm_country SET geo_region = 'SOUTHEAST_ASIA' WHERE iso_code IN ('TH', 'VN', 'MY', 'ID', 'SG', 'PH', 'KH', 'MM', 'LA');
UPDATE mdm_country SET geo_region = 'MIDDLE_EAST'    WHERE iso_code IN ('SA', 'AE', 'QA', 'KW', 'BH', 'OM', 'IQ', 'IR', 'JO', 'LB');
UPDATE mdm_country SET geo_region = 'NORDIC_COLD'    WHERE iso_code IN ('NO', 'SE', 'FI', 'IS', 'EE', 'LV', 'LT');
UPDATE mdm_country SET geo_region = 'BRITISH_ISLES'  WHERE iso_code IN ('GB', 'IE');
UPDATE mdm_country SET geo_region = 'MEDITERRANEAN'  WHERE iso_code IN ('ES', 'IT', 'GR', 'PT', 'TR', 'CY', 'MT');
UPDATE mdm_country SET geo_region = 'EAST_EUROPE'    WHERE iso_code IN ('RU', 'UA', 'BY', 'KZ', 'AM', 'GE', 'AZ', 'UZ');
UPDATE mdm_country SET geo_region = 'WEST_EUROPE'    WHERE iso_code IN ('FR', 'DE', 'NL', 'BE', 'AT', 'CH', 'PL', 'CZ', 'SK', 'HU');
UPDATE mdm_country SET geo_region = 'SA_HIGHLAND'    WHERE iso_code IN ('BO', 'EC', 'PE', 'CO');
UPDATE mdm_country SET geo_region = 'SA_TEMPERATE'   WHERE iso_code IN ('BR', 'AR', 'UY', 'CL', 'PY');
UPDATE mdm_country SET geo_region = 'NORTH_AMERICA'  WHERE iso_code IN ('US', 'CA', 'MX');
UPDATE mdm_country SET geo_region = 'AFRICA_NORTH'   WHERE iso_code IN ('EG', 'MA', 'TN', 'DZ', 'LY');
UPDATE mdm_country SET geo_region = 'AFRICA_TROPIC'  WHERE iso_code IN ('NG', 'GH', 'CI', 'CM', 'SN', 'CD', 'KE', 'ZA');

-- 4. Initial bind of home_venue_id based on matches already extracted
WITH venue_ranks AS (
    SELECT home_team_id, venue_id,
           ROW_NUMBER() OVER(PARTITION BY home_team_id ORDER BY count(*) DESC) as rn
    FROM mdm_match
    WHERE venue_id IS NOT NULL AND home_team_id IS NOT NULL
    GROUP BY home_team_id, venue_id
)
UPDATE mdm_team t
SET home_venue_id = vr.venue_id
FROM venue_ranks vr
WHERE t.id = vr.home_team_id AND vr.rn = 1 AND t.home_venue_id IS NULL;
