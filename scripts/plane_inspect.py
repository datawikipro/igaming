import subprocess
import base64

remote_script = """
import os, sys, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue, User

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
print("PROJECT:", p.name, p.identifier)
print("STATES:")
for s in State.objects.filter(project=p):
    print("  ", s.name, "group=", s.group, "id=", s.id)

print("RECENT ISSUES:")
for i in Issue.objects.filter(project=p).order_by('-created_at')[:10]:
    print(f"  #{i.sequence_id} [{i.state.name}] {i.name}")
"""

b64 = base64.b64encode(remote_script.strip().encode('utf-8')).decode('ascii')
cmd = f"echo {b64} | base64 -d > /tmp/inspect_plane.py && kubectl cp /tmp/inspect_plane.py plane/$(kubectl get pods -n plane -l app=plane-api -o jsonpath='{{.items[0].metadata.name}}'):/tmp/inspect_plane.py && kubectl exec -n plane deploy/plane-api -- python3 /tmp/inspect_plane.py"

ssh = ["ssh", "-i", r"C:\Users\chernousov_a\.ssh\id_ed25519", "-o", "BatchMode=yes", "root@100.78.183.101", cmd]
res = subprocess.run(ssh, capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
