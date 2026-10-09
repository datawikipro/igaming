#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

remote_code = """
from plane.db.models import Issue, IssueComment
for seq in range(50, 56):
    iss = Issue.objects.filter(project_id="2df124d7-25b0-4145-a63c-aafcb0fe0041", sequence_id=seq).first()
    if iss:
        print(f"=== #{iss.sequence_id} [{iss.state.name}] {iss.name} ===")
        for c in IssueComment.objects.filter(issue=iss).order_by("-created_at")[:2]:
            print("  ->", c.created_at.strftime("%H:%M:%S"), c.comment_stripped[:200])
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "timeout 30s kubectl exec -i -n plane deployment/plane-api -- python manage.py shell"
]

res = subprocess.run(cmd, input=remote_code, capture_output=True, text=True, timeout=40, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
