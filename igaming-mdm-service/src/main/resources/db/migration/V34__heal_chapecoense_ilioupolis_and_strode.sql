-- V34: Heal corrupt team mappings and metadata
-- 1. Heal Elijah Strode (tennis player) from FEMALE to MALE
UPDATE mdm_team SET gender = 'MALE' WHERE id = 268408 AND gender = 'FEMALE';

-- 2. Heal GS Ilioupolis (Greek basketball club) country from Hong Kong to Greece
UPDATE mdm_team SET country_id = 453 WHERE id = 84132 AND country_id = 14702;

-- 3. Remap corrupt clean Chapecoense mappings from 355955 (Chapecoense (Bookings)) to 435245 (Chapecoense)
DELETE FROM mdm_team_ingestion_map m1
WHERE m1.canonical_team_id = 355955
  AND m1.raw_key IN ('chapecoense', 'Chapecoense-SC', 'Chapecoense SC')
  AND EXISTS (
      SELECT 1 FROM mdm_team_ingestion_map m2
      WHERE m2.external_system_id = m1.external_system_id
        AND m2.raw_key = m1.raw_key
        AND m2.canonical_team_id = 435245
  );

UPDATE mdm_team_ingestion_map
SET canonical_team_id = 435245
WHERE canonical_team_id = 355955
  AND raw_key IN ('chapecoense', 'Chapecoense-SC', 'Chapecoense SC');
