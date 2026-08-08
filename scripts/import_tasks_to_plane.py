import json
import sys
from pathlib import Path
import django

# Setup Django ORM inside plane-api pod
import os
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "plane.settings.production")
django.setup()

from plane.db.models import (
    User, Workspace, Project, State, Module, ModuleIssue, Issue, IssueSequence
)

TASKS_JSON_PATH = Path("/tmp/plane_tasks_import.json")

def import_tasks():
    if not TASKS_JSON_PATH.exists():
        print(f"❌ Tasks JSON not found at {TASKS_JSON_PATH}")
        return

    with open(TASKS_JSON_PATH, "r", encoding="utf-8") as f:
        data = json.load(f)

    # 1. Get User & Workspace
    user = User.objects.filter(email="aleksei.a.chernousov@gmail.com").first()
    if not user:
        user = User.objects.filter(is_superuser=True).first()
    
    workspace = Workspace.objects.filter(slug="smartbet_guru").first()
    if not workspace:
        workspace = Workspace.objects.first()

    print(f"👤 User: {user.email} ({user.id})")
    print(f"🏢 Workspace: {workspace.name} ({workspace.slug})")

    # 2. Get or Create Project IGAMING
    project = Project.objects.filter(workspace=workspace, identifier="IGAMING").first()
    if not project:
        project = Project.objects.create(
            workspace=workspace,
            name="iGaming Platform (Fail-Fast)",
            identifier="IGAMING",
            description="iGaming platform monorepo health standardization, fail-fast implementation & K8s probes",
            created_by=user,
            updated_by=user,
            network=2 # public / workspace members
        )
        print(f"✨ Created Project: {project.name} (IGAMING)")
    else:
        print(f"📁 Found existing Project: {project.name} (IGAMING)")

    # 3. Ensure Default States exist
    state_backlog = State.objects.filter(project=project, group="backlog").first()
    if not state_backlog:
        state_backlog = State.objects.create(
            project=project, workspace=workspace, name="Backlog", group="backlog",
            color="#a3a3a3", sequence=10000, created_by=user, updated_by=user
        )
    
    state_in_progress = State.objects.filter(project=project, group="started").first()
    if not state_in_progress:
        state_in_progress = State.objects.create(
            project=project, workspace=workspace, name="In Progress", group="started",
            color="#f59e0b", sequence=20000, created_by=user, updated_by=user
        )

    # 4. Get or Create Modules for Epics
    epic_modules = {}
    for epic in data.get("epics", []):
        mod = Module.objects.filter(project=project, name=epic["name"]).first()
        if not mod:
            mod = Module.objects.create(
                project=project,
                workspace=workspace,
                name=epic["name"],
                description=f"Epic module for {epic['name']}",
                created_by=user,
                updated_by=user
            )
            print(f"📦 Created Module: {mod.name}")
        epic_modules[epic["id"]] = mod

    # 5. Create Issues (Tasks)
    created_count = 0
    updated_count = 0

    for task in data.get("tasks", []):
        key = task["key"]
        title = f"[{key}] {task['title']}"
        desc = task["description"]
        module_name = task["module"]
        priority_val = task["priority"].lower() # urgent, high, medium, low
        status_val = task.get("status", "Backlog")

        target_state = state_in_progress if status_val == "In Progress" else state_backlog

        html_desc = f"""
        <h3>📌 Task: {key}</h3>
        <p><b>Module:</b> <code>{module_name}</code></p>
        <p><b>Priority:</b> {task['priority']}</p>
        <p><b>Epic:</b> {task['epic']}</p>
        <hr/>
        <h4>🎯 Description & Robot Protocol</h4>
        <p>{desc}</p>
        <ul>
          <li><b>Step 1:</b> Verify <code>spring-boot-starter-actuator</code> in pom.xml.</li>
          <li><b>Step 2:</b> Audit exception handlers and scheduler loops.</li>
          <li><b>Step 3:</b> Bind <code>EmergencyIncidentTracker</code> for liveness degradation.</li>
          <li><b>Step 4:</b> Add <code>livenessProbe</code> & <code>readinessProbe</code> in K8s manifest.</li>
          <li><b>Step 5:</b> Verify compilation via <code>mvn -pl {module_name} test-compile</code>.</li>
        </ul>
        """

        issue = Issue.objects.filter(project=project, name__startswith=f"[{key}]").first()
        if not issue:
            issue = Issue.objects.create(
                project=project,
                workspace=workspace,
                name=title,
                description_html=html_desc,
                description_stripped=desc,
                priority=priority_val,
                state=target_state,
                created_by=user,
                updated_by=user
            )
            created_count += 1
            print(f"  ➕ Created Issue #{issue.sequence_id}: {title}")
        else:
            issue.name = title
            issue.description_html = html_desc
            issue.priority = priority_val
            issue.state = target_state
            issue.save()
            updated_count += 1
            print(f"  ✏️ Updated Issue #{issue.sequence_id}: {title}")

        # Link to Module
        epic_id = task.get("epic")
        if epic_id in epic_modules:
            mod = epic_modules[epic_id]
            if not ModuleIssue.objects.filter(module=mod, issue=issue).exists():
                ModuleIssue.objects.create(
                    module=mod,
                    issue=issue,
                    project=project,
                    workspace=workspace,
                    created_by=user,
                    updated_by=user
                )

    print("\n" + "=" * 60)
    print(f"🎉 Plane Tasks Import Complete!")
    print(f"   Project: {project.name} (IGAMING)")
    print(f"   Created: {created_count} new tasks")
    print(f"   Updated: {updated_count} existing tasks")
    print(f"   URL:     https://plane.smartbet.guru/{workspace.slug}/projects/{project.id}/issues/")
    print("=" * 60)

if __name__ == "__main__":
    import_tasks()
