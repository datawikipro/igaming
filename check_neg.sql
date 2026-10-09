SELECT count(*) as negative_count
FROM odds_actual
WHERE bookmaker = '1xbet' AND param < 0;

SELECT count(*) as positive_count
FROM odds_actual
WHERE bookmaker = '1xbet' AND param > 0;
