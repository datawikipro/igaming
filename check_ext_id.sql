SELECT o.id, o.bookmaker, ot.code as bet_type, o.value, o.external_event_id, o.updated_at
FROM odds_actual o
JOIN odds_type ot ON o.odds_type_id = ot.id
WHERE o.match_id = (SELECT match_id FROM surebet_alert WHERE id = 38885)
  AND o.bookmaker = 'olimpbet-kz';
