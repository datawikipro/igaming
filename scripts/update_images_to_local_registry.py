import subprocess
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(cmd):
    print(f"\n>>> SSH: {cmd}")
    res = subprocess.run([
        'ssh', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    print("STDOUT:", res.stdout)
    if res.stderr:
        print("STDERR:", res.stderr)
    return res

# Get all deployments in igaming-source
res = run_ssh("kubectl get deployments -n igaming-source -o json")
deps = json.loads(res.stdout)

target_keywords = ['1xbet', 'melbet', '888starz', 'fansport', 'megapari', 'pinnacle', 'sbobet']

for item in deps.get('items', []):
    name = item['metadata']['name']
    if any(k in name for k in target_keywords):
        containers = item['spec']['template']['spec']['containers']
        for c in containers:
            c_name = c['name']
            c_img = c['image']
            print(f"Deployment: {name}, Container: {c_name}, Image: {c_img}")
            
            new_img = None
            if 'ghcr.io/datawikipro/igaming-source-betb2b' in c_img:
                new_img = "100.78.183.101:30500/igaming-source-betb2b:latest"
            elif 'ghcr.io/datawikipro/igaming-source-pinnacle' in c_img:
                new_img = "100.78.183.101:30500/igaming-source-pinnacle:latest"
            elif 'ghcr.io/datawikipro/igaming-source-sbobet' in c_img:
                new_img = "100.78.183.101:30500/igaming-source-sbobet:latest"
            
            if new_img:
                print(f"--> Updating {name} container {c_name} to {new_img}")
                run_ssh(f"kubectl set image deployment/{name} {c_name}={new_img} -n igaming-source")

print("\n--- Pod status after updates ---")
run_ssh("kubectl get pods -n igaming-source | grep -E 'pinnacle|1xbet|melbet|888starz|fansport|megapari|sbobet'")
