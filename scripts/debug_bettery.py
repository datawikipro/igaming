import subprocess

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-source", "igaming-source-bettery-db-0",
    "--", "psql", "-U", "postgres", "-d", "postgres", "-c", "SELECT datname FROM pg_database;"
]
r = subprocess.run(cmd, capture_output=True, text=True)
print("STDOUT:", r.stdout)
print("STDERR:", r.stderr)
