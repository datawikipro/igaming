import subprocess
import sys

def run_remote_django(code: str):
    import base64
    b64 = base64.b64encode(code.strip().encode('utf-8')).decode('ascii')
    cmd = (
        f"echo {b64} | base64 -d > /tmp/plane_script.py && "
        f"POD=$(kubectl get pods -n plane -l app=plane-api -o jsonpath='{{.items[0].metadata.name}}') && "
        f"kubectl cp /tmp/plane_script.py plane/$POD:/tmp/plane_script.py && "
        f"kubectl exec -n plane $POD -- python3 /tmp/plane_script.py"
    )
    ssh = ["ssh", "-o", "StrictHostKeyChecking=no", "root@100.78.183.101", cmd]
    res = subprocess.run(ssh, capture_output=True, text=True)
    return res.stdout, res.stderr

if __name__ == '__main__':
    script = """
import os, sys, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
print("=== PLANE STATES ===")
for s in State.objects.filter(project=p):
    print(f"[{s.group}] {s.name}")

print("\n=== CURRENT ISSUES ===")
for i in Issue.objects.filter(project=p).order_by('-updated_at'):
    print(f"#{i.sequence_id} [{i.state.name}] ({i.priority}) {i.name}")
"""
    out, err = run_remote_django(script)
    print("OUTPUT:\n" + out)
    if err:
        print("ERRORS:\n" + err)
