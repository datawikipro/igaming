import subprocess
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(cmd):
    res = subprocess.run([
        'ssh', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    return res

res = run_ssh("kubectl get pods -n igaming-source -o json")
data = json.loads(res.stdout)

terminating = []
for p in data.get('items', []):
    name = p['metadata']['name']
    if p['metadata'].get('deletionTimestamp'):
        terminating.append(name)

if terminating:
    print(f"Found {len(terminating)} terminating pods: {terminating}")
    for name in terminating:
        print(f"Force deleting {name}...")
        run_ssh(f"kubectl delete pod {name} -n igaming-source --force --grace-period=0")

res2 = run_ssh("kubectl get pods -n igaming-source | grep -E 'pinnacle|1xbet|melbet|888starz|fansport|megapari|sbobet'")
print("\n--- Pods in igaming-source ---")
print(res2.stdout)
