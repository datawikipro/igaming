-- =========================================================================
-- V6__add_esports_disciplines_taxonomy.sql
-- Separate esports into dedicated game disciplines under parent esports (id=6)
-- Prevents cross-game collisions (CS2 vs Dota 2 vs LoL vs Valorant)
-- =========================================================================

INSERT INTO mdm_sport (id, slug, parent_id, domain_key, is_esports, is_team_sport, sort_order)
VALUES 
    (33, 'cs2', 6, 'CS2', TRUE, TRUE, 130),
    (34, 'dota2', 6, 'DOTA2', TRUE, TRUE, 131),
    (35, 'league-of-legends', 6, 'LEAGUE_OF_LEGENDS', TRUE, TRUE, 132),
    (36, 'valorant', 6, 'VALORANT', TRUE, TRUE, 133),
    (37, 'cyber-football', 6, 'CYBER_FOOTBALL', TRUE, TRUE, 134),
    (38, 'rainbow-six', 6, 'RAINBOW_SIX', TRUE, TRUE, 135),
    (39, 'starcraft', 6, 'STARCRAFT', TRUE, FALSE, 136),
    (40, 'mobile-legends', 6, 'MOBILE_LEGENDS', TRUE, TRUE, 137),
    (41, 'cyber-basketball', 6, 'CYBER_BASKETBALL', TRUE, TRUE, 138),
    (42, 'cyber-hockey', 6, 'CYBER_HOCKEY', TRUE, TRUE, 139)
ON CONFLICT (id) DO UPDATE SET
    slug = EXCLUDED.slug,
    parent_id = EXCLUDED.parent_id,
    domain_key = EXCLUDED.domain_key,
    is_esports = EXCLUDED.is_esports,
    is_team_sport = EXCLUDED.is_team_sport,
    sort_order = EXCLUDED.sort_order;

SELECT setval('mdm_sport_id_seq', GREATEST(42, (SELECT COALESCE(MAX(id), 0) FROM mdm_sport)));
