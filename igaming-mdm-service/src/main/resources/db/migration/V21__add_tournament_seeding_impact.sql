-- V21: Add tournament qualification & seeding impact classification

ALTER TABLE mdm_tournament
    ADD COLUMN IF NOT EXISTS affects_qualification_seeding BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS seeding_impact_tier VARCHAR(32) DEFAULT 'MAJOR_SEEDING_IMPACT';

-- 1. ZERO SEEDING IMPACT: Friendlies, Pre-season, Purely Commercial Exhibitions
UPDATE mdm_tournament
SET affects_qualification_seeding = false,
    seeding_impact_tier = 'ZERO_SEEDING_IMPACT'
WHERE canonical_name ILIKE '%friendly%' 
   OR canonical_name ILIKE '%Club Friendly%'
   OR canonical_name ILIKE '%Preseason%'
   OR canonical_name ILIKE '%Pre-Season%'
   OR canonical_name ILIKE '%Six Kings Slam%'
   OR canonical_name ILIKE '%Laver Cup%'
   OR canonical_name ILIKE '%World Tennis League%'
   OR canonical_name ILIKE '%Exhibition%'
   OR canonical_name ILIKE '%Showmatch%';

-- 2. INDIRECT / MINOR SEEDING IMPACT: Secondary Domestic Cups & Lower Tier Knockouts
UPDATE mdm_tournament
SET affects_qualification_seeding = true,
    seeding_impact_tier = 'INDIRECT_RANKING_ONLY'
WHERE (canonical_name ILIKE '%EFL Trophy%'
   OR canonical_name ILIKE '%Carabao Cup%'
   OR canonical_name ILIKE '%League Cup%'
   OR canonical_name ILIKE '%Copa de la Liga%')
  AND seeding_impact_tier != 'ZERO_SEEDING_IMPACT';

-- 3. MAJOR SEEDING IMPACT: Tier 1 Leagues, Grand Slams, Masters 1000, UEFA, NBA, NHL, CS2 RMR
UPDATE mdm_tournament
SET affects_qualification_seeding = true,
    seeding_impact_tier = 'MAJOR_SEEDING_IMPACT'
WHERE tier <= 2
  AND seeding_impact_tier NOT IN ('ZERO_SEEDING_IMPACT', 'INDIRECT_RANKING_ONLY');

CREATE INDEX IF NOT EXISTS idx_mdm_tournament_seeding ON mdm_tournament(seeding_impact_tier);
