#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

remote_code = """
import urllib.request
for i in [3, 8, 9, 10, 12, 13]:
    try:
        req = urllib.request.Request(f"http://plane-ai-worker-{i}.plane-ai-worker.plane.svc.cluster.local:8000/cancel", data=b"{}", method="POST")
        print(f"Cancelled worker-{i}:", urllib.request.urlopen(req, timeout=5).read().decode())
    except Exception as e:
        print(f"worker-{i} cancel err:", e)
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "kubectl exec -i -n plane plane-ai-worker-0 -- python3 -"
]
res = subprocess.run(cmd, input=remote_code, capture_output=True, text=True, timeout=30, encoding="utf-8")
print(res.stdout)

subprocess.run(["python", "scripts/check_xeon_workers.py"])

logs_cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "for p in 14 15 16 17; do echo === WORKER-$p ===; kubectl logs -n plane plane-ai-worker-$p --tail=20; done"
]
res_l = subprocess.run(logs_cmd, capture_output=True, text=True, timeout=30, encoding="utf-8")
print(res_l.stdout)
