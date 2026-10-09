#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

remote_sh = """
for i in 0 1 2 3 8 9 10 11 12 13; do
    echo "=== worker-$i ==="
    kubectl exec -n plane plane-ai-worker-$i -- sh -c 'tail -n 12 /root/.gemini/antigravity-cli/cli.log 2>/dev/null || echo no-cli-log'
done
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    remote_sh
]

res = subprocess.run(cmd, capture_output=True, text=True, timeout=45, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
