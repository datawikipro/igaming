-- V29: Case-insensitive unique constraint for mdm_sport_ingestion_map and cleanup of misclassified tournaments

-- 1. Correct canonical_sport_id for specialized sports across all raw_key variations
UPDATE mdm_sport_ingestion_map
SET canonical_sport_id = 29
WHERE lower(trim(raw_key)) IN (
    '3x3 basketball', '3x3_basketball', '3x3-basketball',
    'basketball 3x3', 'basketball_3x3', 'basketball-3x3',
    'баскетбол 3x3', 'баскетбол 3х3', 'баскетбол_3x3', 'баскетбол-3x3',
    'fiba 3x3', 'fiba_3x3', '3x3', '3х3', 'стритбол', 'streetball'
) AND canonical_sport_id != 29;

UPDATE mdm_sport_ingestion_map
SET canonical_sport_id = 37
WHERE lower(trim(raw_key)) IN (
    'cyber football', 'cyber_football', 'cyber-football', 'cyberfootball',
    'киберфутбол', 'кибер-футбол', 'кибер футбол',
    'fifa', 'fc 24', 'fc 25', 'fc24', 'fc25', 'e-football', 'efootball'
) AND canonical_sport_id != 37;

UPDATE mdm_sport_ingestion_map
SET canonical_sport_id = 42
WHERE lower(trim(raw_key)) IN (
    'cyber hockey', 'cyber_hockey', 'cyber-hockey', 'cyberhockey',
    'киберхоккей', 'кибер-хоккей', 'кибер хоккей',
    'e-hockey', 'ehockey'
) AND canonical_sport_id != 42;

UPDATE mdm_sport_ingestion_map
SET canonical_sport_id = 41
WHERE lower(trim(raw_key)) IN (
    'cyber basketball', 'cyber_basketball', 'cyber-basketball', 'cyberbasketball',
    'кибербаскетбол', 'кибер-баскетбол', 'кибер баскетбол',
    'e-basketball', 'ebasketball', 'nba 2k'
) AND canonical_sport_id != 41;

UPDATE mdm_sport_ingestion_map
SET canonical_sport_id = 50
WHERE lower(trim(raw_key)) IN ('boxing', 'бокс') AND canonical_sport_id != 50;

UPDATE mdm_sport_ingestion_map
SET canonical_sport_id = 55
WHERE lower(trim(raw_key)) IN ('billiards', 'бильярд') AND canonical_sport_id != 55;

UPDATE mdm_sport_ingestion_map
SET canonical_sport_id = 30
WHERE lower(trim(raw_key)) IN (
    'хоккей 3x3', 'хоккей 3х3', 'hockey 3x3', 'short hockey', 'шорт хоккей', 'шорт-хоккей', 'шортхоккей'
) AND canonical_sport_id != 30;

UPDATE mdm_sport_ingestion_map
SET canonical_sport_id = 28
WHERE lower(trim(raw_key)) IN (
    'футбол 3x3', 'футбол 3х3', 'short football 3x3', 'шорт футбол', 'шорт-футбол', 'шортфутбол'
) AND canonical_sport_id != 28;

-- 2. Deduplicate mdm_sport_ingestion_map on (external_system_id, lower(trim(raw_key)))
-- Specialized child sports (parent_id IS NOT NULL) and higher IDs win
WITH ranked AS (
    SELECT sim.id,
           ROW_NUMBER() OVER (
               PARTITION BY sim.external_system_id, lower(trim(sim.raw_key))
               ORDER BY (s.parent_id IS NOT NULL) DESC, sim.canonical_sport_id DESC, sim.id DESC
           ) as rn
    FROM mdm_sport_ingestion_map sim
    JOIN mdm_sport s ON sim.canonical_sport_id = s.id
)
DELETE FROM mdm_sport_ingestion_map
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

-- 3. Normalize all remaining raw_key values to lowercase trimmed strings
UPDATE mdm_sport_ingestion_map
SET raw_key = lower(trim(raw_key))
WHERE raw_key != lower(trim(raw_key));

-- 4. Replace case-sensitive constraint with case-insensitive unique index & constraint
ALTER TABLE mdm_sport_ingestion_map DROP CONSTRAINT IF EXISTS uk_mdm_sport_map;
DROP INDEX IF EXISTS uk_mdm_sport_map;
DROP INDEX IF EXISTS uk_mdm_sport_map_ci;
DROP INDEX IF EXISTS uk_mdm_sport_map_lower;

CREATE UNIQUE INDEX IF NOT EXISTS uk_mdm_sport_map_ci ON mdm_sport_ingestion_map (external_system_id, raw_key);
ALTER TABLE mdm_sport_ingestion_map ADD CONSTRAINT uk_mdm_sport_map UNIQUE USING INDEX uk_mdm_sport_map_ci;
CREATE UNIQUE INDEX IF NOT EXISTS uk_mdm_sport_map_lower ON mdm_sport_ingestion_map (external_system_id, lower(trim(raw_key)));

-- 5. Delete cyber football mappings pointing to sport 1
DELETE FROM mdm_tournament_ingestion_map 
WHERE id IN (
    SELECT m.id 
    FROM mdm_tournament_ingestion_map m 
    JOIN mdm_tournament t ON m.canonical_tournament_id = t.id 
    WHERE (t.canonical_name ~* '(fifa|киберфутбол|fc 24|fc 25|volta|esportsbattle.*(2x|мин)|gt league|h2h liga)' OR t.canonical_name ILIKE '%esoccer%') 
      AND t.sport_id = 1
);

-- 6. Delete cyber hockey mappings pointing to sport 2
DELETE FROM mdm_tournament_ingestion_map 
WHERE id IN (
    SELECT m.id 
    FROM mdm_tournament_ingestion_map m 
    JOIN mdm_tournament t ON m.canonical_tournament_id = t.id 
    WHERE (t.canonical_name ~* '(киберхоккей|esportsbattle.*(3x4|4 мин)|cyber.*nhl)' OR t.canonical_name ~* '(\behockey\b|eicehockey)') 
      AND t.sport_id = 2
);

-- 7. Delete cyber basketball mappings pointing to sport 3
DELETE FROM mdm_tournament_ingestion_map 
WHERE id IN (
    SELECT m.id 
    FROM mdm_tournament_ingestion_map m 
    JOIN mdm_tournament t ON m.canonical_tournament_id = t.id 
    WHERE (t.canonical_name ~* '(кибербаскетбол|\b2k\b|esportsbattle.*(4x5|5x5))') 
      AND t.sport_id = 3
);

-- 8. Delete bogus non-stats raw keys that were erroneously mapped to stats tournaments
DELETE FROM mdm_tournament_ingestion_map 
WHERE id IN (
    SELECT m.id 
    FROM mdm_tournament_ingestion_map m 
    JOIN mdm_tournament t ON m.canonical_tournament_id = t.id 
    WHERE t.canonical_name ~* 'статистика' 
      AND m.raw_key !~* '(статистик|stats|\(жк\)|\(фолы\)|\(угловые\)|\(ауты\)|\(удары\)|\.ауты|\.жк|\.фолы|\.угловые)'
);

-- 9. Delete throw-ins stats mapped to sport 1
DELETE FROM mdm_tournament_ingestion_map 
WHERE id IN (
    SELECT m.id 
    FROM mdm_tournament_ingestion_map m 
    JOIN mdm_tournament t ON m.canonical_tournament_id = t.id 
    WHERE t.canonical_name ~* '(\.\s*ауты|статистика\.\s*ауты)' 
      AND t.sport_id = 1
);

-- 10. Delete ghost cyber football tournaments in sport 1 (they have 0 matches)
DELETE FROM mdm_tournament
WHERE (canonical_name ~* '(fifa|киберфутбол|fc 24|fc 25|volta|esportsbattle.*(2x|мин)|gt league|h2h liga)' OR canonical_name ILIKE '%esoccer%') 
  AND sport_id = 1
  AND NOT EXISTS (SELECT 1 FROM mdm_match WHERE tournament_id = mdm_tournament.id);
