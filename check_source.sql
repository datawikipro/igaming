SELECT o.id, o.bookmaker, o.bet_source_id, ot.code, o.value, o.updated_at
FROM odds_actual o
JOIN odds_type ot ON o.odds_type_id = ot.id
WHERE o.external_event_id = '8918131';
