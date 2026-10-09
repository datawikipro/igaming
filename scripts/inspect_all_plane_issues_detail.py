#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys
from collections import Counter

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

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

issues = []
for line in stdout_text.strip().splitlines():
    if not line.strip():
        continue
    parts = line.split("|||")
    if len(parts) >= 4:
        issues.append({
            "sequence_id": int(parts[0]),
            "name": parts[1],
            "state": parts[2],
            "group": parts[3]
        })

print(f"Total issues loaded: {len(issues)}")

# Group by state
by_state = {}
for iss in issues:
    by_state.setdefault(iss["state"], []).append(iss)

for state, list_items in by_state.items():
    print(f"\n--- State: '{state}' (Count: {len(list_items)}) ---")
    print(f"Min seq: {min(i['sequence_id'] for i in list_items)}, Max seq: {max(i['sequence_id'] for i in list_items)}")
    # Sample 5
    for i in list_items[:5]:
        print(f"  #{i['sequence_id']:3d}: {i['name'][:80]}")

print("\n=== AI разработка Breakdown ===")
ai_dev = by_state.get("AI разработка", [])
# Extract tags or bookmakers [tag]
tags = []
for i in ai_dev:
    name = i["name"]
    if name.startswith("[") and "]" in name:
        tag = name[1:name.index("]")]
        tags.append(tag)
    else:
        tags.append("other")

print("Top tags in 'AI разработка':")
for tag, count in Counter(tags).most_common(20):
    print(f"  {tag:25s}: {count}")

print("\n=== All 161 tasks in 'AI разработка' list ===")
for i in ai_dev:
    print(f"#{i['sequence_id']:3d}: {i['name']}")
