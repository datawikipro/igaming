import subprocess

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c",
    """
    SELECT oa.id, oa.bookmaker, ot.code, oa.value, oa.param, oa.updated_at
    FROM odds_actual oa
    JOIN odds_type ot ON oa.odds_type_id = ot.id
    WHERE oa.match_id = 95517 AND oa.param = 6.5
    ORDER BY oa.bookmaker, ot.code;
    """
]
r = subprocess.run(cmd, capture_output=True, text=True)
print("STDOUT:", r.stdout)
