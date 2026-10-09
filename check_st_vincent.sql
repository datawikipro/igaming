SELECT o.id, o.bookmaker, ot.code as bet_type, o.value, o.param, o.updated_at
FROM odds_actual o
JOIN odds_type ot ON o.odds_type_id = ot.id
WHERE o.match_id = (SELECT match_id FROM surebet_alert WHERE id = 38885)
  AND ot.code IN ('WIN1', 'DRAW', 'WIN2', 'DC_1X', 'DC_12', 'DC_X2')
ORDER BY ot.code, o.bookmaker;
