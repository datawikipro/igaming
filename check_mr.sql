SELECT mr.id, mr.start_time, t1.default_display_name as t1, t2.default_display_name as t2
FROM match_record mr
LEFT JOIN team t1 ON mr.team1_id = t1.id
LEFT JOIN team t2 ON mr.team2_id = t2.id
WHERE mr.id = (SELECT match_id FROM surebet_alert WHERE id = 38885);
