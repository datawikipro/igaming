SELECT h.id, h.odds_actual_id, h.old_value, h.new_value, h.changed_at
FROM odds_history h
WHERE h.odds_actual_id = 34701571
ORDER BY h.changed_at DESC
LIMIT 10;
