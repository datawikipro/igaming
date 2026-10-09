import subprocess

cmd = [
    "ssh", "-i", r"C:\Users\chernousov_a\.ssh\id_ed25519",
    "-o", "BatchMode=yes",
    "root@100.78.183.101",
    "kubectl exec -i -n plane deploy/plane-db-primary -- psql -U plane -d plane"
]
sql = "UPDATE issues SET state_id='ab00339e-5465-4112-9282-1a458e08b568' WHERE sequence_id IN (60, 61); DELETE FROM issue_blockers WHERE block_id IN (SELECT id FROM issues WHERE sequence_id IN (60, 61));"
res = subprocess.run(cmd, input=sql, capture_output=True, text=True, encoding="utf-8")
print(res.stdout)
