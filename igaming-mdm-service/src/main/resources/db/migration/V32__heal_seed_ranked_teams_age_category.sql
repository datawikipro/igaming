-- V32: Heal tournament seed-ranked teams age category
-- US collegiate/tournament seeds in team names (e.g., '(17) Wake Forest', '(6) Indiana', '#1 Alabama')
-- were erroneously parsed as youth age brackets (U17, YOUTH, etc.), causing mutant match blocks in NCAA leagues.
-- This migration updates all such seed-prefixed teams to SENIOR.

UPDATE mdm_team
SET age_category = 'SENIOR'
WHERE (canonical_name ~ '^(#|seed\s*|\[)[0-9]+' OR canonical_name ~ '^\([0-9]+\)')
  AND age_category IN ('U6','U7','U8','U9','U10','U11','U12','U13','U14','U15','U16','U17','U18','U19','U20','U21','U22','U23','YOUTH');
