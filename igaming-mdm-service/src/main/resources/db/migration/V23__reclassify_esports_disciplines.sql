BEGIN;

-- 1. Drop composite foreign keys temporarily for batch reclassification
ALTER TABLE mdm_match DROP CONSTRAINT IF EXISTS fk_mdm_match_tournament;
ALTER TABLE mdm_match DROP CONSTRAINT IF EXISTS fk_mdm_match_home_team;
ALTER TABLE mdm_match DROP CONSTRAINT IF EXISTS fk_mdm_match_away_team;
ALTER TABLE mdm_match DROP CONSTRAINT IF EXISTS fk_mdm_match_stage;
ALTER TABLE mdm_tournament_team DROP CONSTRAINT IF EXISTS fk_mdm_tourn_team_tournament;
ALTER TABLE mdm_tournament_team DROP CONSTRAINT IF EXISTS fk_mdm_tourn_team_team;
ALTER TABLE mdm_season_stage DROP CONSTRAINT IF EXISTS fk_mdm_stage_season;
ALTER TABLE mdm_season DROP CONSTRAINT IF EXISTS fk_mdm_season_tournament;

-- 2. Create classification mapping table
DROP TABLE IF EXISTS tmp_tourn_target_sport;
CREATE TEMP TABLE tmp_tourn_target_sport AS
SELECT 
    t.id AS tournament_id,
    CASE
        -- DOTA 2 (34)
        WHEN t.canonical_name ILIKE '%dota%' 
          OR t.canonical_name ILIKE '%dreamleague%' 
          OR t.canonical_name ILIKE '%the international%' 
          OR t.canonical_name ILIKE '%wallachia%' 
          OR t.canonical_name ILIKE '%blast slam%' 
          OR t.canonical_name ILIKE '%streamers battle%' 
          OR t.canonical_name ILIKE '%betboom storm%' 
          OR t.canonical_name ILIKE '%betboom dacha dota%'
          OR t.canonical_name ILIKE '%fissure%' 
          OR t.canonical_name ILIKE '%epl world series%' 
          OR t.canonical_name ILIKE '%cpa%'
          OR t.slug ILIKE '%dota%' 
          THEN 34

        -- CS2 (33)
        WHEN t.canonical_name ILIKE '%cs 2%' 
          OR t.canonical_name ILIKE '%cs2%' 
          OR t.canonical_name ILIKE '%cs:go%' 
          OR t.canonical_name ILIKE '%csgo%' 
          OR t.canonical_name ILIKE '%counter-strike%' 
          OR t.canonical_name ILIKE '%counter strike%' 
          OR t.canonical_name ILIKE '%cct%' 
          OR t.canonical_name ILIKE '%esea%' 
          OR t.canonical_name ILIKE '%esl challenger%' 
          OR t.canonical_name ILIKE '%esl pro league%' 
          OR t.canonical_name ILIKE '%blast premier%' 
          OR t.canonical_name ILIKE '%iem%' 
          OR t.canonical_name ILIKE '%intel extreme masters%' 
          OR t.canonical_name ILIKE '%thunderpick%' 
          OR t.canonical_name ILIKE '%european pro league%' 
          OR t.canonical_name ILIKE '%res regional%' 
          OR t.canonical_name ILIKE '%united21%' 
          OR t.canonical_name ILIKE '%nodwin%' 
          OR t.canonical_name ILIKE '%dust2%' 
          OR t.canonical_name ILIKE '%gamers club%' 
          OR t.canonical_name ILIKE '%rog journey%' 
          OR t.canonical_name ILIKE '%epl masters%' 
          OR t.canonical_name ILIKE '%draculan%' 
          OR t.canonical_name ILIKE '%cis lan%' 
          OR t.canonical_name ILIKE '%frag%' 
          OR t.canonical_name ILIKE '%tipsport%' 
          OR t.canonical_name ILIKE '%urban riga%' 
          OR t.canonical_name ILIKE '%svenska cs%' 
          OR t.canonical_name ILIKE '%epic.lan%' 
          OR t.canonical_name ILIKE '%dach cs%' 
          OR t.canonical_name ILIKE '%firesports%' 
          OR t.canonical_name ILIKE '%starladder%' 
          OR t.canonical_name ILIKE '%1win%' 
          OR t.canonical_name ILIKE '%bc game%' 
          OR t.canonical_name ILIKE '%stake pulse%'
          OR t.canonical_name ILIKE '%stake ranked%'
          OR t.canonical_name ILIKE '%esports world cup%'
          OR t.canonical_name ILIKE '%pgl%'
          OR t.canonical_name ILIKE '%world star challengers%'
          OR t.canonical_name ILIKE '%exort fiesta%'
          OR t.canonical_name ILIKE '%fox legacy cup%'
          OR t.canonical_name ILIKE '%gluck yungstar%'
          OR t.canonical_name ILIKE '%elisa%'
          OR t.canonical_name ILIKE '%cs asia%'
          OR t.canonical_name ILIKE '%winline star series%'
          OR t.canonical_name ILIKE '%pari universe%'
          OR t.canonical_name ILIKE '%nexus league%'
          OR t.canonical_name ILIKE '%leon.bet masters%'
          OR t.canonical_name ILIKE '%parken challenger%'
          OR t.canonical_name ILIKE '%cyberclash%'
          OR t.canonical_name ILIKE '%exort series%'
          OR t.canonical_name ILIKE '%cpt%'
          OR t.canonical_name ILIKE '%betboom dacha cs%'
          OR t.canonical_name ILIKE '%h2h liga 2x2%'
          OR t.canonical_name ILIKE '%pr universe%'
          OR t.canonical_name ILIKE '%mad dogs league%'
          OR t.canonical_name ILIKE '%logitech g pro%'
          OR t.slug ILIKE '%cs2%' 
          OR t.slug ILIKE '%csgo%' 
          OR t.slug ILIKE '%counter-strike%' 
          THEN 33

        -- CYBER HOCKEY (42)
        WHEN t.canonical_name ILIKE '%nhl%' 
          OR t.canonical_name ILIKE '%киберхоккей%' 
          OR t.canonical_name ILIKE '%cyber hockey%' 
          OR t.canonical_name ILIKE '%cyber_hockey%' 
          OR t.canonical_name ILIKE '%esportsbattle hockey%' 
          OR t.canonical_name ILIKE '%h2h liga (3x4%' 
          OR t.canonical_name ILIKE '%h2h liga. stream%3x4%'
          OR t.slug ILIKE '%nhl%' 
          THEN 42

        -- CYBER BASKETBALL (41)
        WHEN t.canonical_name ILIKE '%nba%' 
          OR t.canonical_name ILIKE '%кибербаскетбол%' 
          OR t.canonical_name ILIKE '%cyber basketball%' 
          OR t.canonical_name ILIKE '%cyber_basketball%' 
          OR t.canonical_name ILIKE '%esportsbattle basketball%' 
          OR t.canonical_name ILIKE '%esports battle (4x5%' 
          OR t.canonical_name ILIKE '%esports battle. european conference (4x5%' 
          OR t.slug ILIKE '%nba%' 
          THEN 41

        -- VALORANT (36)
        WHEN t.canonical_name ILIKE '%valorant%' 
          OR t.canonical_name ILIKE '%vct%' 
          OR t.canonical_name ILIKE '%game changers%' 
          OR t.slug ILIKE '%valorant%' 
          THEN 36

        -- LEAGUE OF LEGENDS (35)
        WHEN t.canonical_name ILIKE '%league of legends%' 
          OR t.canonical_name ILIKE '% lck%' 
          OR t.canonical_name ILIKE '% lpl%' 
          OR t.canonical_name ILIKE 'lpl%'
          OR t.canonical_name ILIKE '% lec%' 
          OR t.canonical_name ILIKE 'lec%'
          OR t.canonical_name ILIKE '% lcs%' 
          OR t.canonical_name ILIKE 'lcs%'
          OR t.canonical_name ILIKE '%lol%' 
          OR t.canonical_name ILIKE '%emea masters%' 
          OR t.canonical_name ILIKE '%lfl%' 
          OR t.canonical_name ILIKE '%circuito desafiante%' 
          OR t.canonical_name ILIKE '%prime league%'
          OR t.canonical_name ILIKE '%road of legends%'
          OR t.canonical_name ILIKE '%les%'
          OR t.canonical_name ILIKE '%arabian league%'
          OR t.canonical_name ILIKE '%demacia cup%'
          OR t.canonical_name ILIKE '%nlc%'
          OR t.canonical_name ILIKE '%vcs%'
          OR t.canonical_name ILIKE '%ebl%'
          OR t.slug ILIKE '%league-of-legends%' 
          OR t.slug ILIKE '%lol%' 
          THEN 35

        -- MOBILE LEGENDS (40)
        WHEN t.canonical_name ILIKE '%mobile legends%' 
          OR t.canonical_name ILIKE '%mlbb%' 
          OR t.slug ILIKE '%mobile-legends%' 
          THEN 40

        -- RAINBOW SIX (38)
        WHEN t.canonical_name ILIKE '%rainbow six%' 
          OR t.canonical_name ILIKE '%rainbow_six%' 
          OR t.canonical_name ILIKE '% r6%' 
          OR t.canonical_name ILIKE '%r6s%' 
          THEN 38

        -- STARCRAFT (39)
        WHEN t.canonical_name ILIKE '%starcraft%' 
          OR t.canonical_name ILIKE '%sc2%' 
          THEN 39

        -- CYBER FOOTBALL (37)
        WHEN t.canonical_name ILIKE '%fc 2%' 
          OR t.canonical_name ILIKE '%fc 1%' 
          OR t.canonical_name ILIKE '%fc2%' 
          OR t.canonical_name ILIKE '%fifa%' 
          OR t.canonical_name ILIKE '%киберфутбол%' 
          OR t.canonical_name ILIKE '%ea sports fc%' 
          OR t.canonical_name ILIKE '%gt sports league%' 
          OR t.canonical_name ILIKE 'fc.%' 
          OR t.canonical_name ILIKE 'fc %' 
          OR t.canonical_name ILIKE '%eadriatic%' 
          OR t.canonical_name ILIKE '%esports battle football%' 
          OR t.canonical_name ILIKE '%esports battle (2x4%' 
          OR t.canonical_name ILIKE '%esports battle (2x5%' 
          OR t.canonical_name ILIKE '%esports battle (2x6%' 
          OR t.canonical_name ILIKE '%adriatic league%' 
          OR t.canonical_name ILIKE '%h2h лига%' 
          OR t.canonical_name ILIKE '%esportsbattle. world cup%' 
          OR t.canonical_name ILIKE '%esportsbattle. champions league%' 
          OR t.canonical_name ILIKE '%esportsbattle. serie a%' 
          OR t.canonical_name ILIKE '%esportsbattle. premier league%' 
          OR t.canonical_name ILIKE '%esportsbattle. laliga%' 
          OR t.canonical_name ILIKE '%esportsbattle. bundesliga%' 
          OR t.canonical_name ILIKE '%valhalla cup%' 
          OR t.canonical_name ILIKE '%cyber live arena%' 
          OR t.canonical_name ILIKE '%симуляция%' 
          OR t.slug ILIKE '%fifa%' 
          OR t.slug ILIKE '%ea-sports-fc%' 
          OR t.slug ILIKE '%fc-2%' 
          OR t.slug ILIKE '%fc2%' 
          THEN 37

        ELSE 6
    END AS target_sport_id
FROM mdm_tournament t
WHERE t.sport_id = 6;

-- 3. Deduplicate tournaments where counterpart already exists in target_sport_id
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT DISTINCT m.tournament_id AS old_id, t_target.id AS new_id
        FROM tmp_tourn_target_sport m
        JOIN mdm_tournament t_old ON t_old.id = m.tournament_id
        JOIN mdm_tournament t_target ON t_target.sport_id = m.target_sport_id
          AND (
              t_target.slug = t_old.slug 
              OR (lower(t_target.canonical_name) = lower(t_old.canonical_name) AND t_target.country_id IS NOT DISTINCT FROM t_old.country_id)
          )
        WHERE m.target_sport_id != 6
    ) LOOP
        UPDATE mdm_match SET tournament_id = r.new_id, stage_id = NULL WHERE tournament_id = r.old_id;
        DELETE FROM mdm_tournament_team WHERE tournament_id = r.old_id AND EXISTS (
            SELECT 1 FROM mdm_tournament_team ex WHERE ex.tournament_id = r.new_id AND ex.team_id = mdm_tournament_team.team_id
        );
        UPDATE mdm_tournament_team SET tournament_id = r.new_id WHERE tournament_id = r.old_id;
        UPDATE mdm_tournament_ingestion_map SET canonical_tournament_id = r.new_id WHERE canonical_tournament_id = r.old_id;
        DELETE FROM mdm_tournament_translation WHERE tournament_id = r.old_id AND EXISTS (
            SELECT 1 FROM mdm_tournament_translation ex WHERE ex.tournament_id = r.new_id AND ex.lang = mdm_tournament_translation.lang
        );
        UPDATE mdm_tournament_translation SET tournament_id = r.new_id WHERE tournament_id = r.old_id;
        DELETE FROM mdm_season_stage WHERE season_id IN (SELECT id FROM mdm_season WHERE tournament_id = r.old_id);
        DELETE FROM mdm_season WHERE tournament_id = r.old_id;
        DELETE FROM mdm_tournament WHERE id = r.old_id;
    END LOOP;
END $$;

-- 4. Update remaining tournaments sport_id
UPDATE mdm_tournament t
SET sport_id = m.target_sport_id
FROM tmp_tourn_target_sport m
WHERE t.id = m.tournament_id
  AND m.target_sport_id != 6;

-- 5. Update matches sport_id from tournaments
UPDATE mdm_match mat
SET sport_id = t.sport_id
FROM mdm_tournament t
WHERE mat.tournament_id = t.id
  AND mat.sport_id != t.sport_id;

-- 6. Update tournament_team sport_id from tournaments
UPDATE mdm_tournament_team tt
SET sport_id = t.sport_id
FROM mdm_tournament t
WHERE tt.tournament_id = t.id
  AND tt.sport_id != t.sport_id;

-- 7. Ensure teams exist in target sport for all matches and tournament memberships
-- 7a. Insert counterpart teams in target sports if they do not exist
INSERT INTO mdm_team (
    sport_id, category_id, slug, canonical_name, normalized_signature,
    country_id, country_code, country_name, is_national, is_individual,
    gender, age_category, team_format, popularity_rank, source_origin, created_at, updated_at
)
SELECT DISTINCT 
    needed.target_sport_id,
    old_team.category_id,
    old_team.slug,
    old_team.canonical_name,
    old_team.normalized_signature,
    old_team.country_id,
    old_team.country_code,
    old_team.country_name,
    old_team.is_national,
    old_team.is_individual,
    old_team.gender,
    old_team.age_category,
    old_team.team_format,
    old_team.popularity_rank,
    'NATIVE_LOCAL',
    NOW(),
    NOW()
FROM (
    SELECT mat.home_team_id AS team_id, mat.sport_id AS target_sport_id FROM mdm_match mat
    UNION
    SELECT mat.away_team_id AS team_id, mat.sport_id AS target_sport_id FROM mdm_match mat
    UNION
    SELECT tt.team_id, tt.sport_id AS target_sport_id FROM mdm_tournament_team tt
) needed
JOIN mdm_team old_team ON needed.team_id = old_team.id
WHERE needed.target_sport_id != old_team.sport_id
  AND NOT EXISTS (
      SELECT 1 FROM mdm_team existing 
      WHERE existing.sport_id = needed.target_sport_id 
        AND (existing.slug = old_team.slug OR (lower(existing.canonical_name) = lower(old_team.canonical_name) AND existing.country_id IS NOT DISTINCT FROM old_team.country_id))
  )
ON CONFLICT DO NOTHING;

-- 7b. Update match home_team_id and away_team_id to counterpart in mat.sport_id
UPDATE mdm_match mat
SET home_team_id = counterpart.id
FROM mdm_team old_team, mdm_team counterpart
WHERE mat.home_team_id = old_team.id
  AND mat.sport_id != old_team.sport_id
  AND counterpart.sport_id = mat.sport_id
  AND (counterpart.slug = old_team.slug OR (lower(counterpart.canonical_name) = lower(old_team.canonical_name) AND counterpart.country_id IS NOT DISTINCT FROM old_team.country_id));

UPDATE mdm_match mat
SET away_team_id = counterpart.id
FROM mdm_team old_team, mdm_team counterpart
WHERE mat.away_team_id = old_team.id
  AND mat.sport_id != old_team.sport_id
  AND counterpart.sport_id = mat.sport_id
  AND (counterpart.slug = old_team.slug OR (lower(counterpart.canonical_name) = lower(old_team.canonical_name) AND counterpart.country_id IS NOT DISTINCT FROM old_team.country_id));

-- 7c. Ensure tournament_team memberships point to counterpart in tt.sport_id
DELETE FROM mdm_tournament_team tt
WHERE EXISTS (
    SELECT 1 FROM mdm_tournament_team ex
    JOIN mdm_team counterpart ON counterpart.sport_id = tt.sport_id
    JOIN mdm_team old_team ON tt.team_id = old_team.id
      AND (counterpart.slug = old_team.slug OR (lower(counterpart.canonical_name) = lower(old_team.canonical_name) AND counterpart.country_id IS NOT DISTINCT FROM old_team.country_id))
    WHERE ex.tournament_id = tt.tournament_id
      AND ex.team_id = counterpart.id
      AND tt.sport_id != old_team.sport_id
);

UPDATE mdm_tournament_team tt
SET team_id = counterpart.id
FROM mdm_team old_team, mdm_team counterpart
WHERE tt.team_id = old_team.id
  AND tt.sport_id != old_team.sport_id
  AND counterpart.sport_id = tt.sport_id
  AND (counterpart.slug = old_team.slug OR (lower(counterpart.canonical_name) = lower(old_team.canonical_name) AND counterpart.country_id IS NOT DISTINCT FROM old_team.country_id));

-- Remove any dangling tournament memberships where team does not exist in target sport
DELETE FROM mdm_tournament_team tt
WHERE NOT EXISTS (
    SELECT 1 FROM mdm_team t WHERE t.id = tt.team_id AND t.sport_id = tt.sport_id
);

-- 7d. Update sport_id of remaining teams that are now isolated to target sports
UPDATE mdm_team t
SET sport_id = sub.sport_id
FROM (
    SELECT DISTINCT mat.home_team_id AS team_id, mat.sport_id
    FROM mdm_match mat
    JOIN mdm_team t ON mat.home_team_id = t.id
    WHERE mat.sport_id != t.sport_id
) sub
WHERE t.id = sub.team_id
  AND NOT EXISTS (
      SELECT 1 FROM mdm_team existing 
      WHERE existing.sport_id = sub.sport_id 
        AND (existing.slug = t.slug OR (lower(existing.canonical_name) = lower(t.canonical_name) AND existing.country_id IS NOT DISTINCT FROM t.country_id))
  );

UPDATE mdm_team t
SET sport_id = sub.sport_id
FROM (
    SELECT DISTINCT mat.away_team_id AS team_id, mat.sport_id
    FROM mdm_match mat
    JOIN mdm_team t ON mat.away_team_id = t.id
    WHERE mat.sport_id != t.sport_id
) sub
WHERE t.id = sub.team_id
  AND NOT EXISTS (
      SELECT 1 FROM mdm_team existing 
      WHERE existing.sport_id = sub.sport_id 
        AND (existing.slug = t.slug OR (lower(existing.canonical_name) = lower(t.canonical_name) AND existing.country_id IS NOT DISTINCT FROM t.country_id))
  );

-- 8. Update hierarchy (mdm_season & mdm_season_stage sport_id)
UPDATE mdm_season s
SET sport_id = t.sport_id
FROM mdm_tournament t
WHERE s.tournament_id = t.id
  AND s.sport_id != t.sport_id;

UPDATE mdm_season_stage st
SET sport_id = s.sport_id
FROM mdm_season s
WHERE st.season_id = s.id
  AND st.sport_id != s.sport_id;

-- 9. Populate seasons and stages for any tournaments without them
INSERT INTO mdm_season (tournament_id, sport_id, slug, name, year, status)
SELECT t.id, t.sport_id, 'default', 'Regular Season', EXTRACT(YEAR FROM NOW())::int, 'ACTIVE'
FROM mdm_tournament t
ON CONFLICT (tournament_id, slug) DO NOTHING;

INSERT INTO mdm_season_stage (season_id, sport_id, slug, name, stage_type, default_best_of, stage_order)
SELECT s.id, s.sport_id, 'main-event', 'Main Event', 'SINGLE_ELIMINATION', 3, 1
FROM mdm_season s
ON CONFLICT (season_id, slug) DO NOTHING;

-- 10. Link matches to their tournament's default stage if stage_id is null or invalid
UPDATE mdm_match m
SET stage_id = st.id
FROM mdm_season s
JOIN mdm_season_stage st ON st.season_id = s.id AND st.slug = 'main-event'
WHERE m.tournament_id = s.tournament_id
  AND m.sport_id = s.sport_id
  AND (m.stage_id IS NULL OR NOT EXISTS (
      SELECT 1 FROM mdm_season_stage valid_st 
      WHERE valid_st.id = m.stage_id AND valid_st.sport_id = m.sport_id
  ));

-- Clear any remaining orphaned stage_id to NULL
UPDATE mdm_match m
SET stage_id = NULL
WHERE stage_id IS NOT NULL 
  AND NOT EXISTS (
      SELECT 1 FROM mdm_season_stage valid_st 
      WHERE valid_st.id = m.stage_id AND valid_st.sport_id = m.sport_id
  );

-- 11. Re-add composite foreign keys with validation
ALTER TABLE mdm_match
    ADD CONSTRAINT fk_mdm_match_tournament FOREIGN KEY (tournament_id, sport_id)
        REFERENCES mdm_tournament(id, sport_id) ON DELETE RESTRICT;

ALTER TABLE mdm_match
    ADD CONSTRAINT fk_mdm_match_home_team FOREIGN KEY (home_team_id, sport_id)
        REFERENCES mdm_team(id, sport_id) ON DELETE RESTRICT;

ALTER TABLE mdm_match
    ADD CONSTRAINT fk_mdm_match_away_team FOREIGN KEY (away_team_id, sport_id)
        REFERENCES mdm_team(id, sport_id) ON DELETE RESTRICT;

ALTER TABLE mdm_match
    ADD CONSTRAINT fk_mdm_match_stage FOREIGN KEY (stage_id, sport_id)
        REFERENCES mdm_season_stage(id, sport_id) ON DELETE SET NULL;

ALTER TABLE mdm_tournament_team
    ADD CONSTRAINT fk_mdm_tourn_team_tournament FOREIGN KEY (tournament_id, sport_id)
        REFERENCES mdm_tournament(id, sport_id) ON DELETE CASCADE;

ALTER TABLE mdm_tournament_team
    ADD CONSTRAINT fk_mdm_tourn_team_team FOREIGN KEY (team_id, sport_id)
        REFERENCES mdm_team(id, sport_id) ON DELETE CASCADE;

ALTER TABLE mdm_season
    ADD CONSTRAINT fk_mdm_season_tournament FOREIGN KEY (tournament_id, sport_id)
        REFERENCES mdm_tournament(id, sport_id) ON DELETE CASCADE;

ALTER TABLE mdm_season_stage
    ADD CONSTRAINT fk_mdm_stage_season FOREIGN KEY (season_id, sport_id)
        REFERENCES mdm_season(id, sport_id) ON DELETE CASCADE;

-- 12. Format validation (best_of) for esports disciplines
UPDATE mdm_match
SET best_of = 1
WHERE sport_id IN (33, 34, 35, 36, 38, 39, 40)
  AND (stage ILIKE '%bo1%' OR round ILIKE '%bo1%' OR stage ILIKE '%best of 1%' OR round ILIKE '%best of 1%');

UPDATE mdm_match
SET best_of = 5
WHERE sport_id IN (33, 34, 35, 36, 38, 39, 40)
  AND (stage ILIKE '%bo5%' OR round ILIKE '%bo5%' OR stage ILIKE '%best of 5%' OR round ILIKE '%best of 5%');

UPDATE mdm_match
SET best_of = 7
WHERE sport_id IN (33, 34, 35, 36, 38, 39, 40)
  AND (stage ILIKE '%bo7%' OR round ILIKE '%bo7%' OR stage ILIKE '%best of 7%' OR round ILIKE '%best of 7%');

DROP TABLE IF EXISTS tmp_tourn_target_sport;

COMMIT;
