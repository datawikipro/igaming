import os, sys, django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "plane.settings.production")
sys.path.insert(0, "/code")
django.setup()

from plane.db.models import Project, State, Issue

p = Project.objects.filter(identifier="IGAMING").first()
if not p:
    p = Project.objects.filter(identifier="SMART").first()

done_state = State.objects.filter(project=p, group="completed").first()
if not done_state:
    done_state = State.objects.filter(project=p, name="Done").first()

issue = Issue.objects.filter(project=p, name__icontains="winline").first()

if issue and done_state:
    issue.state = done_state
    issue.save()
    print(f"🎉 Issue #{issue.sequence_id} '{issue.name}' marked as DONE (state: {done_state.name})!")
else:
    print(f"❌ Issue: {issue}, Done state: {done_state}")
