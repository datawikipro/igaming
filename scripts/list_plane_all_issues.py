#!/usr/bin/env python3
import subprocess
import json

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

code = """
import os, sys, django, json
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue

for p in Project.objects.all():
    print(f"PROJECT: {p.name} | id={p.id} | slug={p.identifier}")
    states = {s.id: s.name for s in State.objects.filter(project=p)}
    items = []
    for i in Issue.objects.filter(project=p).order_by('sequence_id'):
        items.append({
            'seq': i.sequence_id,
            'id': str(i.id),
            'name': i.name,
            'state': states.get(i.state_id, 'Unknown')
        })
    print(f"COUNT: {len(items)}")
    print('__ITEMS__:' + json.dumps(items))
"""

cmd = ["ssh", "-i", SSH_KEY, "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", SSH_HOST, "kubectl exec -i -n plane deploy/plane-api -- python3 -"]
res = subprocess.run(cmd, input=code, capture_output=True, text=True, timeout=30, encoding="utf-8")
for line in res.stdout.splitlines():
    if line.startswith("PROJECT:") or line.startswith("COUNT:"):
        print(line)
    elif line.startswith("__ITEMS__:"):
        items = json.loads(line.replace("__ITEMS__:", ""))
        with open("scratch/plane_existing_issues.json", "w", encoding="utf-8") as f:
            json.dump(items, f, indent=2, ensure_ascii=False)
        print("Saved to scratch/plane_existing_issues.json")
if res.stderr:
    print("STDERR:", res.stderr[:500])
