#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

with open("scratch/all_plane_issues.json", encoding="utf-8") as f:
    data = json.load(f)

by_state = {}
for iss in data["issues"]:
    st = iss["state_name"]
    by_state.setdefault(st, []).append(iss)

done = [i for i in data["issues"] if i["state_name"] in ("Done", "Завершено")]
print(f"=== Done / Завершено ({len(done)}) ===")
for it in done:
    seq = it["sequence_id"]
    iid = it["id"][:8]
    name = it["name"]
    print(f"  #{seq:2d} ({iid}) {name}")

for st, items in by_state.items():
    if st in ("Done", "Завершено"):
        continue
    print(f"\n=== {st} ({len(items)}) ===")
    for it in items:
        seq = it["sequence_id"]
        iid = it["id"][:8]
        name = it["name"]
        print(f"  #{seq:2d} ({iid}) {name}")

