INSERT INTO ai_model_version (
    version_code, 
    display_name, 
    is_active, 
    release_date, 
    description,
    changelog
) VALUES (
    'v0.5.2.1.2', 
    'Football WIN1 & Handicap 0 (Draw-No-Bet) Edition', 
    true, 
    NOW(), 
    'Football market reform: replaced Double Chance 1X/X2 with WIN1 and Asian Handicap 0 (Draw-No-Bet). Isolated pure DRAW outcomes strictly to high-risk speculative strategies (jackpot_outlier, high_odds). Added synthetic DNB consensus derivation.', 
    '1. Replaced DOUBLE_CHANCE_1X and X2 in FootballSport ladder with AH_HOME_0 and AH_AWAY_0 (DNB push on draw). 2. Removed procedural Poisson/ML Frankenstein from FootballSport total goals ladder. 3. Allowed DRAW market strictly for high-risk strategies (sort_order >= 6), while keeping conservative strategies (sort_order <= 5) protected. 4. Implemented synthetic Draw-No-Bet consensus derivation in InstitutionalConsensusOddsResolver. 5. Included AH(0) and DRAW in ContinuousMatrixEdgeScanner.'
)
ON CONFLICT (version_code) DO UPDATE 
SET is_active = true, release_date = NOW(), description = EXCLUDED.description;

-- Deactivate previous versions
UPDATE ai_model_version 
SET is_active = false 
WHERE version_code != 'v0.5.2.1.2';
