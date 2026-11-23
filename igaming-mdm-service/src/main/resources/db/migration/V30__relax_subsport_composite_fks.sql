-- Flyway Migration V30: Relax sub-sport composite foreign keys in mdm_match and mdm_tournament_team
-- Replaces (entity_id, sport_id) composite foreign keys with (entity_id) references to primary keys,
-- allowing matches in sub-sports (e.g. cricket-women, football-women, basketball-women, 3x3)
-- to reference teams whose primary classification is the base parent sport.

-- 1. Fix mutant/misclassified team genders (e.g. Rote Raben Vilsbiburg)
UPDATE mdm_team SET gender = 'FEMALE' WHERE id = 122960 AND gender = 'MALE';

-- 2. Clean up any orphaned entries in mdm_tournament_team
DELETE FROM mdm_tournament_team tt WHERE NOT EXISTS (SELECT 1 FROM mdm_tournament t WHERE t.id = tt.tournament_id);

-- 3. Replace composite foreign keys on mdm_match with single-column primary key references
ALTER TABLE mdm_match DROP CONSTRAINT IF EXISTS fk_mdm_match_home_team;
ALTER TABLE mdm_match
    ADD CONSTRAINT fk_mdm_match_home_team FOREIGN KEY (home_team_id)
        REFERENCES mdm_team(id) ON DELETE RESTRICT NOT VALID;
ALTER TABLE mdm_match VALIDATE CONSTRAINT fk_mdm_match_home_team;

ALTER TABLE mdm_match DROP CONSTRAINT IF EXISTS fk_mdm_match_away_team;
ALTER TABLE mdm_match
    ADD CONSTRAINT fk_mdm_match_away_team FOREIGN KEY (away_team_id)
        REFERENCES mdm_team(id) ON DELETE RESTRICT NOT VALID;
ALTER TABLE mdm_match VALIDATE CONSTRAINT fk_mdm_match_away_team;

ALTER TABLE mdm_match DROP CONSTRAINT IF EXISTS fk_mdm_match_tournament;
ALTER TABLE mdm_match
    ADD CONSTRAINT fk_mdm_match_tournament FOREIGN KEY (tournament_id)
        REFERENCES mdm_tournament(id) ON DELETE RESTRICT NOT VALID;
ALTER TABLE mdm_match VALIDATE CONSTRAINT fk_mdm_match_tournament;

ALTER TABLE mdm_match DROP CONSTRAINT IF EXISTS fk_mdm_match_stage;
ALTER TABLE mdm_match
    ADD CONSTRAINT fk_mdm_match_stage FOREIGN KEY (stage_id)
        REFERENCES mdm_season_stage(id) ON DELETE SET NULL NOT VALID;
ALTER TABLE mdm_match VALIDATE CONSTRAINT fk_mdm_match_stage;

-- 4. Replace composite foreign keys on mdm_tournament_team with single-column primary key references
ALTER TABLE mdm_tournament_team DROP CONSTRAINT IF EXISTS fk_mdm_tourn_team_team;
ALTER TABLE mdm_tournament_team
    ADD CONSTRAINT fk_mdm_tourn_team_team FOREIGN KEY (team_id)
        REFERENCES mdm_team(id) ON DELETE CASCADE NOT VALID;
ALTER TABLE mdm_tournament_team VALIDATE CONSTRAINT fk_mdm_tourn_team_team;

ALTER TABLE mdm_tournament_team DROP CONSTRAINT IF EXISTS fk_mdm_tourn_team_tournament;
ALTER TABLE mdm_tournament_team
    ADD CONSTRAINT fk_mdm_tourn_team_tournament FOREIGN KEY (tournament_id)
        REFERENCES mdm_tournament(id) ON DELETE CASCADE NOT VALID;
ALTER TABLE mdm_tournament_team VALIDATE CONSTRAINT fk_mdm_tourn_team_tournament;

-- 5. Seed blueprint sport mappings for ski jumping, floorball/ball-hockey, skiing, and biathlon
INSERT INTO mdm_sport_ingestion_map (external_system_id, raw_key, canonical_sport_id)
SELECT s.id, k.raw_key, k.canonical_sport_id
FROM mdm_external_system s
CROSS JOIN (
    VALUES 
        ('прыжки с трамплина', 26),
        ('прыжки-с-трамплина', 26),
        ('прыжки на лыжах с трамплина', 26),
        ('лыжный спорт', 26),
        ('лыжные гонки', 26),
        ('горные лыжи', 26),
        ('ski jumping', 26),
        ('ski-jumping', 26),
        ('хоккейбол', 18),
        ('ball hockey', 18),
        ('ball-hockey', 18),
        ('флорбол', 18),
        ('floorball', 18),
        ('шахматы', 25),
        ('chess', 25),
        ('биатлон', 27),
        ('biathlon', 27)
) AS k(raw_key, canonical_sport_id)
ON CONFLICT (external_system_id, raw_key) DO NOTHING;

