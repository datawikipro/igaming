-- Migration V24: Add visual assets provenance columns to mdm_team and mdm_player

ALTER TABLE mdm_team
    ADD COLUMN IF NOT EXISTS logo_source VARCHAR(32) DEFAULT 'OFFICIAL',
    ADD COLUMN IF NOT EXISTS is_official_logo BOOLEAN DEFAULT TRUE;

ALTER TABLE mdm_player
    ADD COLUMN IF NOT EXISTS photo_source VARCHAR(32) DEFAULT 'OFFICIAL',
    ADD COLUMN IF NOT EXISTS is_official_photo BOOLEAN DEFAULT TRUE;

COMMENT ON COLUMN mdm_team.logo_source IS 'Provenance source for team logo (OFFICIAL, EXTERNAL_SEARCH, GENERATED)';
COMMENT ON COLUMN mdm_team.is_official_logo IS 'Flag indicating whether the logo is verified from authoritative provider';

COMMENT ON COLUMN mdm_player.photo_source IS 'Provenance source for player portrait (OFFICIAL, EXTERNAL_SEARCH, GENERATED)';
COMMENT ON COLUMN mdm_player.is_official_photo IS 'Flag indicating whether the photo is verified from authoritative provider';
