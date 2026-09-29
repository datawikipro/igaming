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
    (72, "7a41d6cf-20ee-4b7f-9e15-570fb151fe85"),
    (73, "b634e5af-0435-465a-9c1c-61ad41ce0676"),
    (74, "6b39d5d3-ec3d-4d02-b88a-4b256a992a72"),
    (75, "12226549-7eed-4535-9d0d-6250cbdb2a38"),
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
res = subprocess.run(cmd, input=py_code, capture_output=True, text=True, timeout=60, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)

