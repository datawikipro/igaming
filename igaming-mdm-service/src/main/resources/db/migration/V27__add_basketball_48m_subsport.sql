-- =========================================================================
-- V27__add_basketball_48m_subsport.sql
-- Add 48-minute Professional Basketball (NBA, CBA China, PBA Philippines)
-- =========================================================================

INSERT INTO mdm_sport (id, slug, parent_id, domain_key, is_esports, is_team_sport, sort_order)
VALUES 
    (57, 'basketball-48m', 3, 'BASKETBALL_48M', FALSE, TRUE, 125)
ON CONFLICT (id) DO NOTHING;

SELECT setval('mdm_sport_id_seq', GREATEST(57, (SELECT COALESCE(MAX(id), 0) FROM mdm_sport)));
