SELECT param, count(*)
FROM odds_actual
WHERE bookmaker = '1xbet' AND odds_type_id IN (SELECT id FROM odds_type WHERE code LIKE 'HANDICAP%')
GROUP BY param
ORDER BY count(*) DESC
LIMIT 20;
