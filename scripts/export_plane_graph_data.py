#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import json
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"
PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"

def run_psql(query):
    cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes",
        "-o", "ConnectTimeout=10",
        SSH_HOST,
        "kubectl exec -i -n plane deployment/plane-db-primary -- psql -U plane -d plane -t -A -F '|||'"
    ]
    res = subprocess.run(cmd, input=query, capture_output=True, text=True, timeout=30, encoding="utf-8")
    if res.returncode != 0:
        raise RuntimeError(f"psql error: {res.stderr}")
    return res.stdout.strip().splitlines()

def main():
    states_query = f"SELECT id, name, \"group\" FROM states WHERE project_id='{PROJECT_ID}' ORDER BY sequence;"
    states_lines = run_psql(states_query)
    states = {}
    for line in states_lines:
        if not line.strip():
            continue
        parts = line.split("|||")
        if len(parts) == 3:
            states[parts[0]] = {"name": parts[1], "group": parts[2]}
    
    print(f"Loaded {len(states)} states:")
    for sid, sinfo in states.items():
        print(f"  {sid}: {sinfo['name']} (group={sinfo['group']})")

    issues_query = f"""
    SELECT i.sequence_id, i.id, i.name, i.state_id, s.name, s.group
    FROM issues i
    JOIN states s ON i.state_id = s.id
    WHERE i.project_id='{PROJECT_ID}'
    ORDER BY i.sequence_id;
    """
    issues_lines = run_psql(issues_query)
    issues = []
    for line in issues_lines:
        if not line.strip():
            continue
        parts = line.split("|||")
        if len(parts) >= 6:
            issues.append({
                "sequence_id": int(parts[0]),
                "id": parts[1],
                "name": parts[2],
                "state_id": parts[3],
                "state_name": parts[4],
                "state_group": parts[5]
            })

    print(f"\nLoaded {len(issues)} issues.")
    with open("scratch/all_plane_issues.json", "w", encoding="utf-8") as f:
        json.dump({"states": states, "issues": issues}, f, ensure_ascii=False, indent=2)
    print("Saved to scratch/all_plane_issues.json")

if __name__ == "__main__":
    main()
