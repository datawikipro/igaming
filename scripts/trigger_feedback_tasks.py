#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import subprocess
import sys

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"
PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
WORKSPACE_SLUG = "dataplatform"

TASKS = [
    (66, "8e2a38f5-5918-4121-b819-a1e1fceb685f"),
    (67, "8235af31-9617-440d-b7bc-75691c6f1ea4"),
    (68, "2885eadb-a10e-4c87-93da-d1e2ef7026e5"),
    (69, "c1088825-05dd-4d32-aff9-91dea84a507a"),
]

py_code = """
import json, urllib.request

tasks = """ + json.dumps(TASKS) + """
for seq, iss_id in tasks:
    payload = {
        "event": "issue_activity",
        "action": "updated",
        "data": {
            "field": "state",
            "new_value": "AI разработка",
            "issue": iss_id,
            "project": "2df124d7-25b0-4145-a63c-aafcb0fe0041",
            "workspace": "dataplatform"
        }
    }
    req = urllib.request.Request(
        "http://127.0.0.1:30885/webhook/plane",
        data=json.dumps(payload).encode('utf-8'),
        headers={'Content-Type': 'application/json'},
        method='POST'
    )
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            print(f"Triggered #{seq} ({iss_id}): {r.status}")
    except Exception as e:
        print(f"Error #{seq}: {e}")
"""


cmd = ["ssh", "-i", SSH_KEY, "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", SSH_HOST, "python3 -"]
res = subprocess.run(cmd, input=py_code, capture_output=True, text=True, timeout=30, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
