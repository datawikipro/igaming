#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import subprocess
import os
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

with open("scratch/all_plane_issues.json", encoding="utf-8") as f:
    data = json.load(f)

# Fetch latest origin
env = dict(os.environ, GIT_TERMINAL_PROMPT="0")
try:
    subprocess.run(["git", "fetch", "origin"], capture_output=True, text=True, timeout=30, env=env)
except Exception as e:
    print(f"git fetch err: {e}")

res = subprocess.run(["git", "log", "origin/master", "-n", "100", "--oneline"], capture_output=True, text=True, encoding="utf-8", errors="replace")
git_log = res.stdout

import re
ai_dev = [i for i in data["issues"] if i["state_name"] == "AI разработка"]
print(f"Checking {len(ai_dev)} tasks in AI разработка against git log:")
for it in ai_dev:
    seq = it["sequence_id"]
    sid = it["id"][:8]
    found = False
    for line in git_log.splitlines():
        # Match exact short uuid: #<sid> or plane-<sid> or (plane-<seq>) or (#<seq> followed by non-hex/space)
        if f"#{sid}" in line or f"plane-{sid}" in line:
            print(f"  FOUND in master by UUID: #{seq} ({sid}) -> {line}")
            found = True
            break
        m = re.search(r'#(\d+)\b', line)
        if m and int(m.group(1)) == seq:
            print(f"  FOUND in master by SeqID: #{seq} ({sid}) -> {line}")
            found = True
            break
    if not found:
        print(f"  Still running/pending: #{seq} ({sid}) - {it['name'][:50]}")

