import subprocess

sql = """
SELECT alert_id, bookmaker, bet_type, external_event_id, odds_param_at_detection, odds_value_at_detection
FROM surebet_outcome_legs WHERE alert_id = 39085;
"""

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c", sql
]

res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
