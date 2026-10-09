import subprocess

def run_sql(db_pod, sql):
    cmd = [
        "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-source", db_pod,
        "--", "psql", "-U", "postgres", "-d", "postgres", "-c", sql
    ]
    r = subprocess.run(cmd, capture_output=True, text=True)
    return r.stdout

print(run_sql("igaming-source-bettery-db-0", """
    SELECT mf.factor_id, mf.name, mf.value 
    FROM match_factor mf
    JOIN match_cache mc ON mf.match_id = mc.id
    WHERE mc.external_id = '68412232' AND mf.value IN (2.95, 1.45, 2.5)
    ORDER BY mf.factor_id;
"""))
