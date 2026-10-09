#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys
import base64

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

with open("docs/plane-tasks/dependency_graph.yaml", "rb") as f:
    dag_b64 = base64.b64encode(f.read()).decode("ascii")

with open("scripts/dag_autonomous_orchestrator.py", "rb") as f:
    script_b64 = base64.b64encode(f.read()).decode("ascii")

remote_sh = f"""
mkdir -p /root/plane-dag-orchestrator
echo "{dag_b64}" | base64 -d > /root/plane-dag-orchestrator/dependency_graph.yaml
echo "{script_b64}" | base64 -d > /root/plane-dag-orchestrator/dag_autonomous_orchestrator.py
chmod +x /root/plane-dag-orchestrator/dag_autonomous_orchestrator.py
echo "Synced files to /root/plane-dag-orchestrator/"
ls -la /root/plane-dag-orchestrator/
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "tr -d '\\r' | bash -s"
]


res = subprocess.run(cmd, input=remote_sh, capture_output=True, text=True, timeout=30)

print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
