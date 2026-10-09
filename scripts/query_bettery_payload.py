import subprocess

def run_sql(db_pod, sql):
    cmd = [
        "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-source", db_pod,
        "--", "psql", "-U", "postgres", "-d", "postgres", "-c", sql
    ]
    r = subprocess.run(cmd, capture_output=True, text=True)
    return r.stdout

print(run_sql("igaming-source-bettery-db-0", "SELECT count(*) FROM match_cache;"))
print(run_sql("igaming-source-bettery-db-0", "SELECT id, external_id, name, length(payload) FROM match_cache WHERE external_id = '68412232';"))
