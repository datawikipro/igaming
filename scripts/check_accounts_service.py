import subprocess
import json

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

cmd = "curl -s http://accounts-service.accounts.svc.cluster.local:8091/api/v1/llm-credentials/active"
res = subprocess.run([
    'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
    XEON_HOST, cmd
], capture_output=True, timeout=30)

try:
    creds = json.loads(res.stdout.decode('utf-8', errors='replace'))
    ag_creds = [c for c in creds if c.get("provider") == "ANTIGRAVITY"]
    print(f"Total active credentials: {len(creds)}")
    print(f"Antigravity credentials: {len(ag_creds)}")
    for i, c in enumerate(ag_creds):
        print(f"[{i:2d}] ID={c.get('id')} | Email={c.get('email')} | Active={c.get('isActive')} | HasRT={bool(c.get('refreshToken'))}")
except Exception as e:
    print("Error parsing JSON:", e)
    print("Raw output:", res.stdout[:500])
