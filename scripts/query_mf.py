import subprocess

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-source", "igaming-source-bettery-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_bettery", "-c",
    """
    SELECT mf.factor_id, mf.name, mf.value
    FROM match_factor mf
    JOIN match_cache mc ON mf.match_id = mc.id
    WHERE mc.external_id = '68412232'
    ORDER BY mf.factor_id;
    """
]
r = subprocess.run(cmd, capture_output=True, text=True)
print("STDOUT:", r.stdout[:3000])
print("STDERR:", r.stderr)
