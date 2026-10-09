import subprocess

sql = "SELECT sequence_id, id, name FROM issues WHERE sequence_id >= 72 ORDER BY sequence_id;\n\\q\n"
cmd = [
    "ssh", "-i", r"C:\Users\chernousov_a\.ssh\id_ed25519",
    "-o", "BatchMode=yes",
    "root@100.78.183.101",
    "kubectl exec -i -n plane deployment/plane-db-primary -- psql -U plane -d plane -t -A -F ','"
]
res = subprocess.run(cmd, input=sql, capture_output=True, text=True, timeout=45, encoding="utf-8")
for line in res.stdout.strip().splitlines():
    parts = line.strip().split(",")
    if len(parts) >= 3:
        print(f'    {parts[0]}: ("{parts[1]}", "{parts[2]}"),')
