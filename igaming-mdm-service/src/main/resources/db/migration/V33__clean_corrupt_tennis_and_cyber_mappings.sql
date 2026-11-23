-- V33: Clean corrupt tennis team and cyber tournament mappings
-- 1. Disentangle female tennis player Luisa Hrda from male player Herman Hoeyeraal (267532)
DELETE FROM mdm_team_ingestion_map
WHERE raw_name IN ('Хрда Луиза', 'хрда луиза')
  AND canonical_team_id = 267532;

-- 2. Disentangle male tennis players (Brandon Perez, Roberto Perez Socas, Giulio Perego) from female player Perez B. (626697)
DELETE FROM mdm_team_ingestion_map
WHERE raw_name IN ('Брандон Перес', 'Перез Брандон', 'Роберто Перес Сокас', 'Перего Джулио')
  AND canonical_team_id = 626697;

-- 3. Cancel corrupt mutant match created with mismapped male player
UPDATE mdm_match
SET status = 'CANCELLED'
WHERE id = 2240405;

-- 4. Remove corrupt cyber tournament mapping for real football league 'Liga Pro' (Betrivers)
DELETE FROM mdm_tournament_ingestion_map
WHERE external_system_id = 36
  AND raw_key = 'Liga Pro'
  AND canonical_tournament_id = 719549;
