SELECT o.id, o.param, o.value, ot.code
FROM odds_actual o
JOIN odds_type ot ON o.odds_type_id = ot.id
WHERE o.match_id = (SELECT match_id FROM surebet_alert WHERE id = 38901)
  AND o.bookmaker = '1xbet'
ORDER BY ot.code, o.param;
