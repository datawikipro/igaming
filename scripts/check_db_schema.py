import subprocess

def run_sql(sql):
    cmd = [
        "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
        "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c", sql
    ]
    r = subprocess.run(cmd, capture_output=True, text=True)
    return r.stdout

print("=== ALERT 39326 ROW ===")
print(run_sql("SELECT * FROM surebet_alert WHERE id = 39326;"))

print("=== TABLES IN DB ===")
print(run_sql("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';"))
