-- V20: Add tournament economic prize tier and per-match win bounty to mdm_tournament

ALTER TABLE mdm_tournament
    ADD COLUMN IF NOT EXISTS win_bounty_tier VARCHAR(32) DEFAULT 'BINARY_QUALIFY_ONLY',
    ADD COLUMN IF NOT EXISTS per_match_win_bonus_eur BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS qualification_only_format BOOLEAN DEFAULT true;

-- Update major European and international competitions with exact economic bounties
UPDATE mdm_tournament 
SET win_bounty_tier = 'HIGH_FINANCIAL_BOUNTY',
    per_match_win_bonus_eur = 2100000,
    qualification_only_format = false
WHERE canonical_name ILIKE '%Champions League%' OR canonical_name ILIKE '%Лига Чемпионов%';

UPDATE mdm_tournament 
SET win_bounty_tier = 'MODERATE_FINANCIAL_BOUNTY',
    per_match_win_bonus_eur = 450000,
    qualification_only_format = false
WHERE canonical_name ILIKE '%Europa League%' OR canonical_name ILIKE '%Лига Европы%';

UPDATE mdm_tournament 
SET win_bounty_tier = 'MODERATE_FINANCIAL_BOUNTY',
    per_match_win_bonus_eur = 166000,
    qualification_only_format = false
WHERE canonical_name ILIKE '%Conference League%' OR canonical_name ILIKE '%Лига Конференций%';

UPDATE mdm_tournament 
SET win_bounty_tier = 'BINARY_QUALIFY_ONLY',
    per_match_win_bonus_eur = 0,
    qualification_only_format = true
WHERE canonical_name ILIKE '%World Cup%' OR canonical_name ILIKE '%Чемпионат Мира%'
   OR canonical_name ILIKE '%Euro%' OR canonical_name ILIKE '%Чемпионат Европы%'
   OR canonical_name ILIKE '%Copa America%' OR canonical_name ILIKE '%Кубок Америки%';

UPDATE mdm_tournament 
SET win_bounty_tier = 'HIGH_FINANCIAL_BOUNTY',
    per_match_win_bonus_eur = 390000,
    qualification_only_format = false
WHERE sport_id = 4 AND (canonical_name ILIKE '%ATP Finals%' OR canonical_name ILIKE '%WTA Finals%');

CREATE INDEX IF NOT EXISTS idx_mdm_tournament_win_bounty ON mdm_tournament(win_bounty_tier);
