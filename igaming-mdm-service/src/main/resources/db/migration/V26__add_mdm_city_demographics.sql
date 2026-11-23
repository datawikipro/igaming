-- V26: Add mdm_city demographic & geographic reference table (GeoNames cities15000)
-- Used strictly as an odds-quality / public-bias / travel-context lookup (NOT raw ML outcome feature).

CREATE TABLE IF NOT EXISTS mdm_city (
    id BIGSERIAL PRIMARY KEY,
    geonames_id BIGINT UNIQUE,
    canonical_name VARCHAR(255) NOT NULL,
    ascii_name VARCHAR(255) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    population BIGINT NOT NULL DEFAULT 0,
    is_capital BOOLEAN NOT NULL DEFAULT FALSE,
    feature_code VARCHAR(16),
    latitude NUMERIC(10, 6),
    longitude NUMERIC(10, 6),
    elevation_m INTEGER,
    timezone VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mdm_city_country_pop ON mdm_city(country_code, population DESC);
CREATE INDEX IF NOT EXISTS idx_mdm_city_country_ascii ON mdm_city(country_code, LOWER(ascii_name));
CREATE INDEX IF NOT EXISTS idx_mdm_city_capital ON mdm_city(country_code) WHERE is_capital = TRUE;

ALTER TABLE mdm_venue ADD COLUMN IF NOT EXISTS city_id BIGINT REFERENCES mdm_city(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_mdm_venue_city_id ON mdm_venue(city_id);

ALTER TABLE mdm_team ADD COLUMN IF NOT EXISTS home_city_id BIGINT REFERENCES mdm_city(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_mdm_team_home_city_id ON mdm_team(home_city_id);
