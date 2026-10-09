import subprocess

def run_sql(sql):
    cmd = [
        "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
        "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c", sql
    ]
    r = subprocess.run(cmd, capture_output=True, text=True)
    return r.stdout

print(run_sql("""
    SELECT oa.bookmaker, ot.code, oa.value, oa.param, oa.updated_at, oa.external_event_id
    FROM odds_actual oa
    JOIN odds_type ot ON oa.odds_type_id = ot.id
    WHERE oa.match_id = 95517 AND ot.code LIKE '%TOTAL%'
    ORDER BY oa.param, oa.bookmaker, ot.code;
"""))
