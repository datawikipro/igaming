SELECT o.id, o.bookmaker, o.external_event_id, ot.code as bet_type, o.value, o.updated_at
FROM odds_actual o
JOIN odds_type ot ON o.odds_type_id = ot.id
WHERE o.bookmaker = 'olimpbet' AND o.match_id = (SELECT match_id FROM surebet_alert WHERE id = 38885);
