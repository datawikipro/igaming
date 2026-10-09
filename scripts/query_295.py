import subprocess

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c",
    "SELECT * FROM odds_actual WHERE match_id = 95517 AND value = 2.95;"
]
r = subprocess.run(cmd, capture_output=True, text=True)
print(r.stdout)
