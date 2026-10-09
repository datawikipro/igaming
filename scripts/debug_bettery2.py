import subprocess

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-source", "igaming-source-bettery-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_bettery", "-c",
    "SELECT column_name FROM information_schema.columns WHERE table_name = 'match_cache';"
]
r = subprocess.run(cmd, capture_output=True, text=True)
print(r.stdout)
