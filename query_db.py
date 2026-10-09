import subprocess
import sys

sql = sys.argv[1] if len(sys.argv) > 1 else "SELECT count(*) FROM surebet_alert WHERE status='ACTIVE';"
cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-c", sql
]
res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
