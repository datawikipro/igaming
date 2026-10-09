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
cat << 'EOF' > /tmp/inspect_workers.py
import urllib.request
import json
import subprocess

print("=== CHECKING WORKERS 0..19 ===")
for i in range(20):
    dns_url = f"http://plane-ai-worker-{i}.plane-ai-worker.plane.svc.cluster.local:8000/status"
    try:
        req = urllib.request.Request(dns_url, headers={"User-Agent": "Inspector"})
        with urllib.request.urlopen(req, timeout=1.5) as r:
            data = json.loads(r.read().decode())
            status = data.get("status")
            email = data.get("email")
            task = data.get("currentTask")
            print(f"plane-ai-worker-{i:2d}: status={status} | account={email} | task={task}")
    except Exception as e:
        print(f"plane-ai-worker-{i:2d}: UNREACHABLE ({e})")

print("")
print("=== SYSTEMD ORCHESTRATOR STATUS ===")
subprocess.run(["systemctl", "status", "plane-dag-orchestrator", "--no-pager", "-n", "10"])

print("")
print("=== PLANE-AI-EXECUTOR POD STATUS ===")
subprocess.run(["kubectl", "get", "pods", "-n", "plane", "-l", "app=plane-ai-executor", "-o", "wide"])
EOF

python3 /tmp/inspect_workers.py
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "bash -s"
]

clean_script = remote_sh.replace("\r\n", "\n").encode("utf-8")
res = subprocess.run(cmd, input=clean_script, capture_output=True, timeout=40)
print(res.stdout.decode("utf-8", errors="replace"))
if res.stderr:
    print("STDERR:", res.stderr.decode("utf-8", errors="replace"))
