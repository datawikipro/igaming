-- V12: Expand age category constraints to allow U20, U18, YOUTH, U15, U14, U13
ALTER TABLE mdm_team DROP CONSTRAINT IF EXISTS ck_mdm_team_age;
ALTER TABLE mdm_team ADD CONSTRAINT ck_mdm_team_age
    CHECK (age_category IN ('SENIOR', 'U23', 'U21', 'U20', 'U19', 'U18', 'U17', 'U16', 'U15', 'U14', 'U13', 'RESERVE', 'YOUTH'));

ALTER TABLE mdm_tournament DROP CONSTRAINT IF EXISTS ck_mdm_tournament_age;
ALTER TABLE mdm_tournament ADD CONSTRAINT ck_mdm_tournament_age
    CHECK (age_category IN ('SENIOR', 'U23', 'U21', 'U20', 'U19', 'U18', 'U17', 'U16', 'U15', 'U14', 'U13', 'RESERVE', 'YOUTH'));
