import subprocess

sql = """
SELECT a.id, ROUND(a.profit_percent::numeric, 2) AS profit, a.match_description, a.match_id,
       string_agg(l.bookmaker || ': ' || l.bet_type || ' (' || l.odds_value_at_detection || ')', ' vs ') AS legs
FROM surebet_alert a
JOIN surebet_outcome_legs l ON a.id = l.alert_id
WHERE a.status = 'ACTIVE' AND a.profit_percent >= 10.0
GROUP BY a.id, a.profit_percent, a.match_description, a.match_id
ORDER BY a.profit_percent DESC;
"""

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c", sql
]

res = subprocess.run(cmd, capture_output=True, text=True)
print("=== ALERTS ===")
print(res.stdout)

# For each alert, print the legs from surebet_outcome_legs
cmd_legs = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c",
    """
    SELECT l.alert_id, l.bookmaker, l.bet_type, l.odds_value_at_detection,
           oa.value as current_odds_value, oa.param, oa.updated_at, oa.external_event_id
    FROM surebet_outcome_legs l
    JOIN surebet_alert a ON l.alert_id = a.id
    LEFT JOIN match m ON a.match_id = m.id
    LEFT JOIN odds_actual oa ON oa.match_id = a.match_id 
                            AND oa.bookmaker = l.bookmaker 
                            AND oa.odds_type_id = (SELECT id FROM odds_type WHERE code = l.bet_type)
    WHERE a.status = 'ACTIVE' AND a.profit_percent >= 10.0
    ORDER BY l.alert_id, l.bookmaker;
    """
]
res_legs = subprocess.run(cmd_legs, capture_output=True, text=True)
print("=== LEGS & CURRENT ODDS ===")
print(res_legs.stdout)
