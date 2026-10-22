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
python3 -c '
import subprocess, json

for i in range(20):
    pod = "plane-ai-worker-" + str(i)
    res = subprocess.run(["kubectl", "exec", "-n", "plane", pod, "--", "curl", "-s", "http://127.0.0.1:8000/status"], capture_output=True, text=True, timeout=10)
    out = res.stdout.strip()
    try:
        data = json.loads(out)
        email = data.get("email", "?")
        cid = data.get("credentialId", "?")
        st = data.get("status", "?")
        qmsg = data.get("quotaMessage", "")
        last_out = (data.get("lastOutput") or "").strip().replace("\\n", " ")[:100]
        last_err = (data.get("lastError") or "").strip().replace("\\n", " ")[:60]
        print(f"{pod:18s} | ID:{cid:<2} | {email:28s} | {st:10s} | {qmsg:25s} | err: {last_err} | out: {last_out}")
    except Exception as e:
        print(f"{pod:18s} | RAW: {out[:60]} ({e})")
'
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    check_cmd
]

res = subprocess.run(cmd, capture_output=True, text=True, timeout=120, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
