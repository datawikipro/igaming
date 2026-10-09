import sys
import subprocess

if len(sys.argv) < 2:
    print("Usage: sql.py '<SQL_QUERY>'")
    sys.exit(1)

query = " ".join(sys.argv[1:])

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-P", "pager=off", "-c", query
]

res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
