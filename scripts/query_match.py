import sys
import subprocess

if len(sys.argv) < 2:
    print("Usage: query_match.py <match_id>")
    sys.exit(1)

mid = sys.argv[1]

sql = f"""
SELECT m.id, t1.default_display_name as home_team, t2.default_display_name as away_team, m.sport_name, m.start_time, m.is_live
FROM match_record m 
LEFT JOIN team t1 ON m.team1_id = t1.id
LEFT JOIN team t2 ON m.team2_id = t2.id
WHERE m.id = {mid};

SELECT oa.bookmaker, ot.code, oa.param, oa.value, oa.updated_at
FROM odds_actual oa
JOIN odds_type ot ON oa.odds_type_id = ot.id
WHERE oa.match_id = {mid}
ORDER BY ot.code, oa.param, oa.bookmaker;
"""

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-P", "pager=off", "-c", sql
]

res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
