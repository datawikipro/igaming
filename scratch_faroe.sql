SELECT o.id, o.bookmaker, ot.code, o.value, o.param, o.updated_at
FROM odds_actual o
JOIN odds_type ot ON o.odds_type_id = ot.id
WHERE o.match_id = (SELECT match_id FROM surebet_alert WHERE id = 38901)
  AND o.bookmaker IN ('pari', 'fon-bet-ru', '1xbet', 'olimpbet')
  AND ot.code IN ('WIN1', 'DRAW', 'WIN2', 'HANDICAP_1', 'HANDICAP_2')
ORDER BY ot.code, o.bookmaker;
