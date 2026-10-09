#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import subprocess
import sys
import time

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"
LOCAL_DAEMON = r"c:\Users\chernousov_a\IdeaProjects\igaming\scripts\plane_worker_daemon_fixed.py"


def run_ssh(remote_cmd, input_text=None, timeout=60):
    cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes",
        "-o", "ConnectTimeout=10",
        SSH_HOST,
        remote_cmd
    ]
    return subprocess.run(cmd, input=input_text, capture_output=True, text=True, timeout=timeout, encoding="utf-8")


print("1. Uploading fixed plane_worker_daemon.py to xeon-srv:/tmp/plane_worker_daemon.py...")
with open(LOCAL_DAEMON, "r", encoding="utf-8") as f:
    daemon_code = f.read()

res = run_ssh("cat > /tmp/plane_worker_daemon.py && wc -l /tmp/plane_worker_daemon.py", input_text=daemon_code)
print("Upload result:", res.stdout.strip(), res.stderr.strip())
if res.returncode != 0:
    sys.exit(1)

print("2. Updating ConfigMap plane-ai-worker-daemon in namespace plane...")
cm_cmd = (
    "kubectl create configmap plane-ai-worker-daemon -n plane "
    "--from-file=plane_worker_daemon.py=/tmp/plane_worker_daemon.py "
    "-o yaml --dry-run=client | kubectl apply -f -"
)
res = run_ssh(cm_cmd)
print("ConfigMap apply:", res.stdout.strip(), res.stderr.strip())

print("3. Restarting idle/stuck worker pods (3, 8, 9, 10, 12, 13, 14, 15, 16, 17) to pick up new ConfigMap...")
pods_to_restart = " ".join([f"plane-ai-worker-{i}" for i in [3, 8, 9, 10, 12, 13, 14, 15, 16, 17]])
res = run_ssh(f"kubectl delete pod {pods_to_restart} -n plane --wait=false")
print("Deleted pods:", res.stdout.strip(), res.stderr.strip())

print("4. Syncing description_stripped from description_html for issues #50-#59 and setting #50-#53 to 'AI разработка'...")
django_script = """
import re
from plane.db.models import Issue, State

proj_id = '2df124d7-25b0-4145-a63c-aafcb0fe0041'
ai_state = State.objects.filter(project_id=proj_id, name='AI разработка').first()
print('AI state:', ai_state.id if ai_state else None)

for seq in range(50, 60):
    iss = Issue.objects.filter(project_id=proj_id, sequence_id=seq).first()
    if not iss:
        continue
    html = iss.description_html or ''
    stripped = re.sub(r'<[^>]+>', ' ', html)
    stripped = re.sub(r'\\s+', ' ', stripped).strip()
    iss.description_stripped = stripped
    if seq in (50, 51, 52, 53) and ai_state:
        iss.state = ai_state
    iss.save()
    print(f'Updated #{seq} ({iss.id}): state={iss.state.name}, stripped_len={len(iss.description_stripped or "")}')
"""
res = run_ssh("timeout 30s kubectl exec -i -n plane deployment/plane-api -- python manage.py shell", input_text=django_script)
print("Django sync:\n", res.stdout.strip())

print("5. Waiting 20 seconds for restarted worker pods to initialize and bind accounts...")
time.sleep(20)

check_pods_cmd = """
kubectl get pods -n plane -l app=plane-ai-worker
"""
res = run_ssh(check_pods_cmd)
print(res.stdout)

print("6. Dispatching tasks #50, #51, #52, #53 via plane-ai-executor webhook...")
dispatch_py = """
import json, urllib.request, time

issues = [
    (50, "f6a7bb98-1621-4410-bd33-2a060f2392d8"),
    (51, "9d1de8b8-a51d-4fb0-8ee1-008057422d47"),
    (52, "dc0603d1-8adb-4d1f-a263-f5d4d899dd77"),
    (53, "e2bb0c5b-e316-42a6-bfd2-336575dd8816")
]
for seq_id, iss_id in issues:
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
    with urllib.request.urlopen(req, timeout=15) as r:
        print(f"Dispatched #{seq_id} ({iss_id}): HTTP {r.status} -> {r.read().decode('utf-8')}")
    time.sleep(1)
"""
res = run_ssh("python3 -", input_text=dispatch_py)
print("Dispatch result:\n", res.stdout.strip(), res.stderr.strip())
