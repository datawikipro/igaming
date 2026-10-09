#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import json
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

check_cmd = """
for i in $(seq 0 17); do
    pod="plane-ai-worker-$i"
    stat=$(kubectl exec -n plane $pod -- curl -s --max-time 3 http://127.0.0.1:8000/status 2>/dev/null || echo '{"status":"UNREACHABLE"}')
    echo "$pod|||$stat"
done
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    check_cmd
]

res = subprocess.run(cmd, capture_output=True, text=True, timeout=60, encoding="utf-8")
for line in res.stdout.strip().splitlines():
    if "|||" in line:
        pod, raw = line.split("|||", 1)
        try:
            info = json.loads(raw)
            st = info.get("status", "UNKNOWN")
            task = info.get("currentTask", {})
            task_title = task.get("title", "") if isinstance(task, dict) else str(task)
            email = info.get("accountEmail", "")
            print(f"{pod:20s} | {st:15s} | {email:25s} | {task_title[:50]}")
        except Exception:
            print(f"{pod:20s} | RAW: {raw[:60]}")
