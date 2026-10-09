import subprocess
import base64

remote_script = """
import os, sys, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
print("STATES:")
for s in State.objects.filter(project=p):
    print(f'  {s.name} ({s.group})')

print("\nISSUES:")
for issue in Issue.objects.filter(project=p).order_by('-updated_at'):
    print(f'  #{issue.sequence_id} [{issue.state.name}] {issue.name}')
"""

b64 = base64.b64encode(remote_script.strip().encode('utf-8')).decode('ascii')
cmd = f'echo {b64} | base64 -d > /root/list_plane.py && kubectl cp /root/list_plane.py plane/$(kubectl get pods -n plane -l app=plane-api -o jsonpath="{{.items[0].metadata.name}}"):/tmp/list_plane.py && kubectl exec -n plane deploy/plane-api -- python3 /tmp/list_plane.py'

ssh_cmd = ["ssh", "-n", "-o", "StrictHostKeyChecking=no", "root@100.78.183.101", cmd]
p = subprocess.run(ssh_cmd, capture_output=True, text=True)
print("STDOUT:\n", p.stdout)
