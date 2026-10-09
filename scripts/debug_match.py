import subprocess

def run_sql(sql):
    cmd = [
        "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
        "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c", sql
    ]
    r = subprocess.run(cmd, capture_output=True, text=True)
    return r.stdout

print(run_sql("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'match_record';"))
print("=== CHECK IF MATCH 95517 EXISTS IN match_record ===")
print(run_sql("SELECT * FROM match_record WHERE id = 95517;"))
print("=== CHECK IF ODDS EXIST FOR MATCH 95517 ===")
print(run_sql("SELECT count(*) FROM odds_actual WHERE match_id = 95517;"))
