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
states = [{
    'id': str(s.id),
    'name': s.name,
    'group': s.group,
    'description': s.description,
    'color': s.color
} for s in m.State.objects.filter(project=p)]
print('STATES_JSON:' + json.dumps(states, ensure_ascii=False))

modules = [{
    'id': str(mod.id),
    'name': mod.name,
    'status': mod.status
} for mod in m.Module.objects.filter(project=p)]
print('MODULES_JSON:' + json.dumps(modules, ensure_ascii=False))

# Check IssueRelation relation_type choices
try:
    print('RELATION_TYPE_CHOICES:', getattr(m.IssueRelation, 'RELATION_CHOICES', None) or [c for c in m.IssueRelation._meta.get_field('relation_type').choices])
except Exception as e:
    print('RELATION_TYPE_ERR:', str(e))
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
if res.stderr:
    print("STDERR:\n", res.stderr)
