import subprocess

sql = """
SELECT a.id, a.bookmaker, a.odds_type_id, ot.code as type_code, a.param, a.value, a.updated_at
FROM odds_actual a
LEFT JOIN odds_type ot ON a.odds_type_id::int = ot.id
WHERE a.match_id = 33260 AND a.bookmaker IN ('bettery', 'olimpbet')
ORDER BY a.param, a.odds_type_id, a.bookmaker;
"""

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c", sql
]

res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
