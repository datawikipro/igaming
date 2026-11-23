-- V35: Fix Sochaux country from Tunisia to France
UPDATE mdm_team SET country_id = 427 WHERE id = 121105 AND country_id = 865;
