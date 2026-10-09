#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

with open("scratch/all_plane_issues.json", encoding="utf-8") as f:
    data = json.load(f)

issues = sorted(data["issues"], key=lambda x: x["sequence_id"])

print(f"Total issues: {len(issues)}")
for iss in issues[:40]:
    seq = iss["sequence_id"]
    iid = iss["id"][:8]
    st = iss["state_name"]
    name = iss["name"]
    print(f"#{seq:2d} | {iid} | {st:14s} | {name}")

