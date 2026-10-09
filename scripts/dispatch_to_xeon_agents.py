#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

ISSUES = [
    (50, "f6a7bb98-1621-4410-bd33-2a060f2392d8"),
    (51, "9d1de8b8-a51d-4fb0-8ee1-008057422d47"),
    (52, "dc0603d1-8adb-4d1f-a263-f5d4d899dd77"),
    (53, "e2bb0c5b-e316-42a6-bfd2-336575dd8816")
]

PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
WORKSPACE_SLUG = "dataplatform"

remote_python = f"""
import json, urllib.request

issues = {ISSUES}
for seq_id, iss_id in issues:
    payload = {{
        "event": "issue_activity",
        "action": "updated",
        "data": {{
            "field": "state",
            "new_value": "AI разработка",
            "issue": iss_id,
            "project": "{PROJECT_ID}",
            "workspace": "{WORKSPACE_SLUG}"
        }}
    }}
    req = urllib.request.Request(
        "http://127.0.0.1:30885/webhook/plane",
        data=json.dumps(payload).encode('utf-8'),
        headers={{'Content-Type': 'application/json'}},
        method='POST'
    )
    with urllib.request.urlopen(req, timeout=15) as r:
        print(f"Dispatched #{{seq_id}} ({{iss_id}}): HTTP {{r.status}} -> {{r.read().decode('utf-8')}}")
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "python3 -"
]

res = subprocess.run(cmd, input=remote_python, capture_output=True, text=True, timeout=60, encoding="utf-8")
print("STDOUT:\n", res.stdout)
if res.stderr:
    print("STDERR:\n", res.stderr)
