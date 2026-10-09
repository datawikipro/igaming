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
for i in 8 9 10 12 13 14 15 16 17; do
    echo -n "worker-$i: "
    kubectl exec -n plane plane-ai-worker-$i -- timeout 20s agy -p "Reply with one word: OK" --model gemini-3.8-flash-high --mode accept-edits --dangerously-skip-permissions 2>&1 | tr '\\n' ' ' | cut -c1-140
    echo ""
done
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    remote_sh
]

res = subprocess.run(cmd, capture_output=True, text=True, timeout=180, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
