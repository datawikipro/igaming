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
from plane.db.models import Issue
items = [{'seq': i.sequence_id, 'id': str(i.id), 'name': i.name} for i in Issue.objects.filter(sequence_id__gte=66).order_by('sequence_id')]
print('__ITEMS__:' + json.dumps(items))
"""

cmd = ["ssh", "-i", SSH_KEY, "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", SSH_HOST, "kubectl exec -i -n plane deploy/plane-api -- python3 -"]
res = subprocess.run(cmd, input=code, capture_output=True, text=True, timeout=30, encoding="utf-8")
for line in res.stdout.splitlines():
    if line.startswith("__ITEMS__:"):
        items = json.loads(line.replace("__ITEMS__:", ""))
        for it in items:
            print(f"{it['seq']}: (\"{it['id']}\", \"{it['name']}\"),")
