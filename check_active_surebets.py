import subprocess

sql = """
SELECT a.id, a.match_id, ROUND(a.profit_percent::numeric, 2) AS profit, a.detected_at, a.match_description,
       string_agg(l.bookmaker || ': ' || l.bet_type || ' (' || l.odds_value_at_detection || ')', ' vs ') AS legs
FROM surebet_alert a
JOIN surebet_outcome_legs l ON a.id = l.alert_id
WHERE a.status = 'ACTIVE' AND a.profit_percent >= 10.0
GROUP BY a.id, a.match_id, a.profit_percent, a.detected_at, a.match_description
ORDER BY a.profit_percent DESC;
"""

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c", sql
]

res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
