import subprocess

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-source", "igaming-source-olimpbet-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_olimpbet", "-c",
    """
    SELECT DISTINCT substring(team1 from '^[А-ЯA-Z]{2,6}\\s') as prefix, count(*)
    FROM match_cache
    GROUP BY prefix
    ORDER BY count(*) DESC
    LIMIT 20;
    """
]
r = subprocess.run(cmd, capture_output=True, text=True)
print("STDOUT:", r.stdout)
print("STDERR:", r.stderr)
