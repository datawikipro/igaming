import os, sys, subprocess, json

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

django_code = """
import os, sys, json, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db import models as m

p = m.Project.objects.filter(identifier='IGAMING').first()
issue_fields = [f.name for f in m.Issue._meta.get_fields()]
print('HAS_PARENT:', 'parent' in issue_fields)
print('BLOCKER_FIELDS:', [f.name for f in m.IssueBlocker._meta.get_fields()])
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "kubectl exec -i -n plane deploy/plane-api -- python3 -"
]

res = subprocess.run(cmd, input=django_code, capture_output=True, text=True, timeout=30, encoding="utf-8")
print(res.stdout)
