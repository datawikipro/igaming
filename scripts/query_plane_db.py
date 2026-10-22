#!/usr/bin/env python3
import subprocess
import json
import sys

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

def run_sql(sql):
    cmd = [
        "ssh", "-i", SSH_KEY, "-o", "BatchMode=yes", "-o", "ConnectTimeout=10",
        SSH_HOST,
        "kubectl exec -i -n plane deployment/plane-db-primary -- psql -U plane -d plane -t -A -F '\t'"
    ]
    res = subprocess.run(cmd, input=sql, capture_output=True, text=True, timeout=30, encoding="utf-8")
    if res.returncode != 0:
        print("ERR:", res.stderr)
        return []
    lines = [l.strip() for l in res.stdout.splitlines() if l.strip()]
    return [l.split('\t') for l in lines]

if __name__ == "__main__":
    query = sys.argv[1] if len(sys.argv) > 1 else "SELECT id, name, identifier FROM projects;"
    rows = run_sql(query)
    for r in rows:
        print(r)
