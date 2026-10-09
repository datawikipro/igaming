import subprocess
import sys

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

sql = """
SELECT id, name, "group", sequence FROM states WHERE project_id='2df124d7-25b0-4145-a63c-aafcb0fe0041' ORDER BY sequence;

SELECT i.sequence_id, i.id, s.name as state, i.name 
FROM issues i 
JOIN states s ON i.state_id = s.id 
WHERE i.sequence_id >= 87 AND i.sequence_id <= 96
ORDER BY i.sequence_id;
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "kubectl exec -i -n plane deployment/plane-db-primary -- psql -U plane -d plane"
]

res = subprocess.run(cmd, input=sql, capture_output=True, text=True, timeout=30, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
