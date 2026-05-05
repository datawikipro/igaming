-- Clean up duplicate team aliases
DELETE FROM team_alias a USING team_alias b
WHERE a.id > b.id
AND a.raw_name = b.raw_name
AND a.source_id = b.source_id
AND (a.sport_id = b.sport_id OR (a.sport_id IS NULL AND b.sport_id IS NULL));

-- Clean up duplicate league aliases
DELETE FROM league_alias a USING league_alias b
WHERE a.id > b.id
AND a.raw_name = b.raw_name
AND a.source_id = b.source_id
AND (a.sport_id = b.sport_id OR (a.sport_id IS NULL AND b.sport_id IS NULL));
