-- =========================================================================
-- V5__add_subsports_taxonomy.sql
-- Add sub-sports taxonomy for domain and relational isolation
-- =========================================================================

INSERT INTO mdm_sport (id, slug, domain_key, is_esports, is_team_sport, sort_order)
VALUES 
    (29, 'basketball-3x3', 'BASKETBALL_3X3', FALSE, TRUE, 120),
    (30, 'short-hockey', 'SHORT_HOCKEY', FALSE, TRUE, 121),
    (31, 'beach-soccer', 'BEACH_SOCCER', FALSE, TRUE, 122),
    (32, 'beach-volleyball', 'BEACH_VOLLEYBALL', FALSE, TRUE, 123)
ON CONFLICT (id) DO NOTHING;

SELECT setval('mdm_sport_id_seq', GREATEST(32, (SELECT COALESCE(MAX(id), 0) FROM mdm_sport)));
