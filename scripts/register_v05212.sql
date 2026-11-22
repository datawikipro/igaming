INSERT INTO ai_model_version (
    version_code, 
    display_name, 
    is_active, 
    release_date, 
    description,
    changelog
) VALUES (
    'v0.5.2.1.2', 
    'Football WIN1/DNB & Canonical Multi-Sport Disinfection Edition', 
    true, 
    NOW(), 
    'Football market reform (WIN1 & AH 0 Draw-No-Bet) + Strict multi-sport resolution: eliminated UTR substring false-positives for NBA In-Season Tournament, quarantined cyber sports (FC 24/25/26, eAdriatic, NBA 2K), fixed MDM sport ID mappings.', 
    '1. Replaced DOUBLE_CHANCE_1X and X2 in FootballSport ladder with AH_HOME_0 and AH_AWAY_0 (DNB push on draw). 2. Removed procedural Poisson/ML Frankenstein from FootballSport total goals ladder. 3. Disinfected Sports.resolveSport: word-boundary regex for tennis tournament tokens prevents Vnutrisezonnyy Kubok from becoming TENNIS. 4. Canonical sport priority: explicit raw sports (basketball, hockey, football) are trusted over tournament heuristics. 5. FastTrackDictionary and Sports cyber quarantine: FC 24/25/26, eAdriatic, Valhalla Cup, NBA 2K, NHL 24/25 deterministically mapped to CYBER_FOOTBALL/BASKETBALL/HOCKEY and filtered from physical betting. 6. Corrected MDM sport IDs 37 (CYBER_FOOTBALL), 41 (CYBER_BASKETBALL), 42 (CYBER_HOCKEY).'
)
ON CONFLICT (version_code) DO UPDATE 
SET is_active = true, display_name = EXCLUDED.display_name, release_date = NOW(), description = EXCLUDED.description, changelog = EXCLUDED.changelog;

-- Deactivate previous versions
UPDATE ai_model_version 
SET is_active = false 
WHERE version_code != 'v0.5.2.1.2';
