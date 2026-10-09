import subprocess

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c",
    "SELECT id, code FROM odds_type WHERE id IN (314, 376, 527);"
]
r = subprocess.run(cmd, capture_output=True, text=True)
print("STDOUT:", r.stdout)
print("STDERR:", r.stderr)
