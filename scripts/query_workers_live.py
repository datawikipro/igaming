#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys
import json

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

remote_code = '''import json
import subprocess
import urllib.request

res = subprocess.run(["kubectl", "--kubeconfig", "/root/.kube/config", "get", "pods", "-n", "plane", "-l", "app=plane-ai-worker", "-o", "json"], capture_output=True, text=True)
items = json.loads(res.stdout).get("items", [])
for it in sorted(items, key=lambda x: int(x["metadata"]["name"].split("-")[-1])):
    name = it["metadata"]["name"]
    ip = it.get("status", {}).get("podIP")
    if not ip:
        print(f"{name:18s} | NO IP")
        continue
    try:
        req = urllib.request.Request(f"http://{ip}:8000/status", headers={"User-Agent": "Probe"})
        with urllib.request.urlopen(req, timeout=2) as r:
            d = json.loads(r.read().decode())
            st = d.get("status", "UNKNOWN")
            task = d.get("currentTask")
            acc = d.get("email", "none")
            t_title = task.get("title") if task else "None"
            t_start = task.get("startedAt", "") if task else ""
            print(f"{name:18s} | status={st:6s} | account={acc:30s} | task={t_title} ({t_start})")
    except Exception as e:
        print(f"{name:18s} | ERROR: {e}")
'''

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "python3 -c" + repr(remote_code)
]

# Pass code via stdin to avoid quote escaping issues
ssh_cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "python3 -"
]

res = subprocess.run(ssh_cmd, input=remote_code.encode("utf-8"), capture_output=True, timeout=30)
print(res.stdout.decode("utf-8", errors="replace"))
if res.stderr:
    print("STDERR:", res.stderr.decode("utf-8", errors="replace"))
