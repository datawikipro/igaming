-- V37: Support UNKNOWN gender and explicitly heal blueprint tournament and team gender metadata
-- 1. Relax CHECK constraints on mdm_tournament, mdm_team, mdm_player to allow 'UNKNOWN'
ALTER TABLE mdm_tournament DROP CONSTRAINT IF EXISTS ck_mdm_tournament_gender;
ALTER TABLE mdm_tournament ADD CONSTRAINT ck_mdm_tournament_gender CHECK (gender IN ('MALE', 'FEMALE', 'MIXED', 'UNKNOWN'));
ALTER TABLE mdm_tournament ALTER COLUMN gender SET DEFAULT 'UNKNOWN';

ALTER TABLE mdm_team DROP CONSTRAINT IF EXISTS ck_mdm_team_gender;
ALTER TABLE mdm_team ADD CONSTRAINT ck_mdm_team_gender CHECK (gender IN ('MALE', 'FEMALE', 'MIXED', 'UNKNOWN'));
ALTER TABLE mdm_team ALTER COLUMN gender SET DEFAULT 'UNKNOWN';

ALTER TABLE mdm_player DROP CONSTRAINT IF EXISTS ck_mdm_player_gender;
ALTER TABLE mdm_player ADD CONSTRAINT ck_mdm_player_gender CHECK (gender IN ('MALE', 'FEMALE', 'MIXED', 'UNKNOWN'));
ALTER TABLE mdm_player ALTER COLUMN gender SET DEFAULT 'UNKNOWN';

-- 2. Fix falsely classified female tournaments that had curly apostrophes
UPDATE mdm_tournament
SET gender = 'FEMALE', sport_id = 60
WHERE id IN (741282, 740782);

-- 3. Explicitly verify and ensure all tournaments belonging to female sports are FEMALE
UPDATE mdm_tournament t
SET gender = 'FEMALE'
FROM mdm_sport s
WHERE t.sport_id = s.id
  AND s.slug LIKE '%women%'
  AND t.gender != 'FEMALE';

-- 4. Explicitly verify and ensure all standard tournaments in inherently male circuits / standard male leagues are MALE
-- (leagues in standard disciplines without female tokens and without mixed tokens)
UPDATE mdm_tournament t
SET gender = 'MALE'
FROM mdm_sport s
WHERE t.sport_id = s.id
  AND s.slug NOT LIKE '%women%'
  AND t.gender = 'UNKNOWN'
  AND t.canonical_name !~* '(\ywomen\y|\(w\)|\(жен\)|\yженщины\y|\yfrauen\y|\ydamen\y|\yfemmes\y|\yfemenil\y|\ywta\y|\ywnba\y|\ymixed\y|\yмикст\y)';
