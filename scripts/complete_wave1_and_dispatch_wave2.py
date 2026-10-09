#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

sql_commands = """
-- Mark #50, #51, #52, #53 as Завершено
UPDATE issues SET state_id='e5f607b9-2f87-46c3-b4d0-243b3ca4a8c3', updated_at=NOW()
WHERE project_id='2df124d7-25b0-4145-a63c-aafcb0fe0041' AND sequence_id IN (50, 51, 52, 53);

-- Move unblocked #54 and #57 to AI разработка
UPDATE issues SET state_id='1d4476f7-1d60-4b35-8211-439d318bf5d7', updated_at=NOW()
WHERE project_id='2df124d7-25b0-4145-a63c-aafcb0fe0041' AND sequence_id IN (54, 57);

SELECT sequence_id, state_id, name FROM issues WHERE project_id='2df124d7-25b0-4145-a63c-aafcb0fe0041' AND sequence_id >= 50 ORDER BY sequence_id;
"""

cmd_sql = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "kubectl exec -i -n plane deployment/plane-db-primary -- psql -U plane -d plane"
]

res = subprocess.run(cmd_sql, input=sql_commands, capture_output=True, text=True, timeout=30, encoding="utf-8")
print("SQL Execution:\n", res.stdout)
if res.stderr:
    print("SQL Stderr:", res.stderr)

dispatch_py = """
import json, urllib.request, time

PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
WORKSPACE_SLUG = "dataplatform"

issues = [
    (54, "a36e7819-1749-4359-8289-c80a0220264c"),
    (57, "61c5500d-110e-4f3b-ab4e-34ea8b2c6d9a")
]

for seq_id, iss_id in issues:
    payload = {
        "event": "issue_activity",
        "action": "updated",
        "data": {
            "field": "state",
            "new_value": "AI разработка",
            "issue": iss_id,
            "project": PROJECT_ID,
            "workspace": WORKSPACE_SLUG
        }
    }
    req = urllib.request.Request(
        "http://127.0.0.1:30885/webhook/plane",
        data=json.dumps(payload).encode('utf-8'),
        headers={'Content-Type': 'application/json'},
        method='POST'
    )
    with urllib.request.urlopen(req, timeout=15) as r:
        print(f"Dispatched #{seq_id} ({iss_id}): HTTP {r.status} -> {r.read().decode('utf-8')}")
    time.sleep(1)
"""

cmd_disp = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "python3 -"
]
res_disp = subprocess.run(cmd_disp, input=dispatch_py, capture_output=True, text=True, timeout=30, encoding="utf-8")
print("Dispatch result:\n", res_disp.stdout)
if res_disp.stderr:
    print("Dispatch Stderr:", res_disp.stderr)
