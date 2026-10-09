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
print('PROJECT:', p.name, p.identifier, p.id)

states = [(str(s.id), s.name, s.group) for s in m.State.objects.filter(project=p)]
print('STATES:', json.dumps(states, ensure_ascii=False))

modules = [(str(mod.id), mod.name) for mod in m.Module.objects.filter(project=p)]
print('MODULES:', json.dumps(modules, ensure_ascii=False))

relation_models = [name for name in dir(m) if any(k in name.lower() for k in ['relation', 'link', 'block', 'issue', 'dependency'])]
print('RELEVANT_MODELS:', json.dumps(relation_models, ensure_ascii=False))

# Check fields of IssueLink or IssueBlocker if exist
for model_name in ['IssueLink', 'IssueBlocker', 'IssueRelation', 'BlockedIssue']:
    if hasattr(m, model_name):
        model_cls = getattr(m, model_name)
        fields = [f.name for f in model_cls._meta.get_fields()]
        print(f'MODEL_{model_name}_FIELDS:', json.dumps(fields))
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "kubectl exec -i -n plane deploy/plane-api -- python3 -"
]

res = subprocess.run(cmd, input=django_code, capture_output=True, text=True, timeout=30, encoding="utf-8")
print("STDOUT:\n", res.stdout)
if res.stderr:
    print("STDERR:\n", res.stderr)
