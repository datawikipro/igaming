-- V30: Heal individual female athletes gender in mdm_team based on tournament participation
-- In individual sports (tennis=4, table-tennis=5, mma=10, badminton=13, snooker=14, darts=15, boxing=25, biathlon=27, beach-volleyball=32, tennis-doubles=45)
-- athletes participating in FEMALE tournaments are women, but historically defaulted to MALE.

UPDATE mdm_team t
SET gender = 'FEMALE'
FROM mdm_tournament_team tt
JOIN mdm_tournament tr ON tt.tournament_id = tr.id
WHERE tt.team_id = t.id
  AND t.sport_id IN (4, 5, 10, 13, 14, 15, 25, 27, 32, 45)
  AND t.gender = 'MALE'
  AND tr.gender = 'FEMALE';
