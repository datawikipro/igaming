import subprocess
import base64

remote_script = """
import os, sys, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue, User

p = Project.objects.filter(identifier='IGAMING').first()
if not p:
    p = Project.objects.first()

ai_state = State.objects.filter(project=p, name='AI разработка').first()
if not ai_state:
    ai_state = State.objects.filter(project=p, name='In Progress').first()

user = User.objects.first()

title = '[estrelabet] Ingestion and match line population (target >= 500 matches)'
description = 'Automated bookmaker ingestion for EstrelaBet (Altenar platform). Golden Rule #8: populate match_cache with >= 500 matches and scale crawler deployment to 0.'

issue = Issue.objects.filter(project=p, name__icontains='estrelabet').first()
if not issue:
    issue = Issue.objects.create(
        project=p,
        workspace=p.workspace,
        name=title,
        description_html=f'<p>{description}</p>',
        state=ai_state,
        created_by=user,
        updated_by=user,
        priority='urgent'
    )
    print(f'✨ Created Issue #{issue.sequence_id}: "{issue.name}" in state "{ai_state.name}"')
else:
    issue.state = ai_state
    issue.save()
    print(f'🔄 Updated Issue #{issue.sequence_id}: "{issue.name}" to state "{ai_state.name}"')
"""

b64 = base64.b64encode(remote_script.strip().encode('utf-8')).decode('ascii')

cmd = f'echo {b64} | base64 -d > /root/mark_estrelabet_plane.py && kubectl cp /root/mark_estrelabet_plane.py plane/$(kubectl get pods -n plane -l app=plane-api -o jsonpath="{{.items[0].metadata.name}}"):/tmp/mark_estrelabet_plane.py && kubectl exec -n plane deploy/plane-api -- python3 /tmp/mark_estrelabet_plane.py'

ssh_cmd = ["ssh", "-n", "-o", "StrictHostKeyChecking=no", "root@100.78.183.101", cmd]
p = subprocess.run(ssh_cmd, capture_output=True, text=True)
print("STDOUT:", p.stdout)
print("STDERR:", p.stderr)
