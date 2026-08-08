import os, sys, django
from pathlib import Path

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "plane.settings.production")
sys.path.insert(0, "/code")
django.setup()

from plane.db.models import Project, State, Issue

p = Project.objects.filter(identifier="IGAMING").first()
if not p:
    p = Project.objects.filter(identifier="SMART").first()

ai_state = State.objects.filter(project=p, name="AI разработка").first()
if not ai_state:
    ai_state = State.objects.create(
        project=p,
        workspace=p.workspace,
        name="AI разработка",
        group="started",
        color="#8b5cf6",
        sequence=15000,
        created_by=p.created_by,
        updated_by=p.updated_by
    )
    print("✨ Created State: AI разработка")
else:
    print("📁 Found State: AI разработка")

issue = Issue.objects.filter(project=p, name__contains="IGAMING-CORE-001").first()
if not issue:
    issue = Issue.objects.filter(project=p).first()

if issue:
    issue.state = ai_state
    issue.save()
    print(f"✅ Issue #{issue.sequence_id} '{issue.name}' is now in state '{ai_state.name}'!")
else:
    print("❌ Issue not found")
