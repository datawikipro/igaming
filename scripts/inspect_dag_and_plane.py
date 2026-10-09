#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys
import json

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

# Query plane DB directly via kubectl
query = """
SELECT i.sequence_id, i.name, s.name as state, s.group
FROM issues i
JOIN states s ON i.state_id = s.id
WHERE i.project_id = '2df124d7-25b0-4145-a63c-aafcb0fe0041'
ORDER BY i.sequence_id;
"""

cmd = [
    "kubectl", "exec", "-i", "-n", "plane", "deployment/plane-db-primary", "--",
    "psql", "-U", "plane", "-d", "plane", "-t", "-A", "-F", "|||", "-c", query
]

res = subprocess.run(cmd, capture_output=True, text=False)
stdout_text = res.stdout.decode("utf-8", errors="replace")
if res.returncode != 0:
    print("Error:", res.stderr.decode("utf-8", errors="replace"))
    sys.exit(1)

issues = {}
for line in stdout_text.strip().splitlines():
    if not line.strip():
        continue
    parts = line.split("|||")
    if len(parts) >= 4:
        seq = int(parts[0])
        issues[seq] = {
            "name": parts[1],
            "state": parts[2],
            "group": parts[3]
        }

# Parse YAML tasks
with open("docs/plane-tasks/dependency_graph.yaml", "r", encoding="utf-8") as f:
    lines = f.readlines()

dag_tasks = []
curr = None
for l in lines:
    ls = l.strip()
    if ls.startswith("- sequence_id:"):
        if curr:
            dag_tasks.append(curr)
        curr = {"sequence_id": int(ls.split(":")[1].strip()), "depends_on": []}
    elif curr:
        if ls.startswith("title:"):
            curr["title"] = ls.split(":", 1)[1].strip().strip("'\"")
        elif ls.startswith("module:"):
            curr["module"] = ls.split(":", 1)[1].strip().strip("'\"")
        elif ls.startswith("depends_on:"):
            deps = ls.split(":", 1)[1].strip().strip("[]")
            if deps:
                curr["depends_on"] = [int(x.strip()) for x in deps.split(",") if x.strip()]
if curr:
    dag_tasks.append(curr)

completed_count = 0
ai_dev_count = 0
ready_to_run = []
blocked = []

for t in dag_tasks:
    seq = t["sequence_id"]
    iss = issues.get(seq)
    st = iss["state"] if iss else "NOT FOUND"
    grp = iss["group"] if iss else "unknown"

    if grp == "completed" or st in ("Done", "Завершено"):
        completed_count += 1
    elif st == "AI разработка":
        ai_dev_count += 1
    else:
        # Check dependencies
        deps = t.get("depends_on", [])
        unresolved = [d for d in deps if issues.get(d, {}).get("group") != "completed" and issues.get(d, {}).get("state") not in ("Done", "Завершено")]
        if not unresolved:
            ready_to_run.append((seq, t, st))
        else:
            blocked.append((seq, t, st, unresolved))

print("=== DAG OVERVIEW (from dependency_graph.yaml) ===")
print(f"Total tasks in DAG: {len(dag_tasks)}")
print(f"Completed / Done: {completed_count}")
print(f"In 'AI разработка': {ai_dev_count}")
print(f"Ready to run (dependencies satisfied): {len(ready_to_run)}")
print(f"Blocked by dependencies: {len(blocked)}")

print("\n=== TOP READY TASKS ===")
for seq, t, st in ready_to_run[:10]:
    print(f"#{seq:2d} [{t.get('module')}] current_state='{st}': {t.get('title')[:60]}")

print("\n=== SAMPLE IN 'AI разработка' ===")
ai_dev_in_dag = [t for t in dag_tasks if issues.get(t['sequence_id'], {}).get('state') == 'AI разработка']
for t in ai_dev_in_dag[:10]:
    print(f"#{t['sequence_id']:2d} [{t.get('module')}]: {t.get('title')[:60]}")
