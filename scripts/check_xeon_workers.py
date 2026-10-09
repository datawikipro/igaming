#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

remote_code = """
import urllib.request, json

print("=== ACCOUNTS SERVICE SNAPSHOTS ===")
creds = json.loads(urllib.request.urlopen('http://accounts-service.accounts.svc.cluster.local:8091/api/v1/llm-credentials/active', timeout=5).read().decode())
for idx, c in enumerate(creds):
    cid = c['id']
    snaps = json.loads(urllib.request.urlopen(f'http://accounts-service.accounts.svc.cluster.local:8091/api/v1/llm-credentials/{cid}/snapshots', timeout=5).read().decode())
    s = snaps[0] if snaps else {}
    print(f"Idx {idx:2d} | ID {cid:2d} | {c.get('email'):34s} | Gem5h:{str(s.get('gemini5hPct')):6s} GemWk:{str(s.get('geminiWeeklyPct')):6s} | Cld5h:{str(s.get('claude5hPct')):6s} CldWk:{str(s.get('claudeWeeklyPct')):6s}")

print("\\n=== WORKER PODS STATUS ===")
for i in range(18):
    try:
        url = f"http://plane-ai-worker-{i}.plane-ai-worker.plane.svc.cluster.local:8000/status"
        st = json.loads(urllib.request.urlopen(url, timeout=3).read().decode())
        t = st.get("currentTask")
        t_str = f"{t.get('issueId', '')[:8]} ({t.get('title', '')[:45]})" if t else "None"
        print(f"worker-{i:2d}: {st.get('status'):12s} | {st.get('email'):34s} | GemQ:{st.get('geminiQuota')} CldQ:{st.get('claudeQuota')} | Task: {t_str}")
    except Exception as e:
        print(f"worker-{i:2d}: ERR {e}")
"""

cmd = [
    "kubectl", "exec", "-i", "-n", "plane", "plane-ai-worker-0", "--", "python3", "-"
]

res = subprocess.run(cmd, input=remote_code, capture_output=True, text=True, timeout=35, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
