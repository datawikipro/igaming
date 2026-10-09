SELECT id, team1, team2, json_payload
FROM match_cache
WHERE id IN (19494, 19492, 26296)
LIMIT 3;
