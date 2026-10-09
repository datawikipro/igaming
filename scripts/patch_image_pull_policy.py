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

target_keywords = ['1xbet', 'melbet', '888starz', 'fansport', 'megapari', 'pinnacle', 'sbobet']

res = run_ssh("kubectl get deployments -n igaming-source -o json")
deps = json.loads(res.stdout)

for item in deps.get('items', []):
    name = item['metadata']['name']
    if any(k in name for k in target_keywords):
        containers = item['spec']['template']['spec']['containers']
        patch_containers = []
        for c in containers:
            patch_containers.append({
                "name": c['name'],
                "image": c['image'],
                "imagePullPolicy": "IfNotPresent"
            })
        patch_json = json.dumps({"spec": {"template": {"spec": {"containers": patch_containers}}}})
        print(f"Patching {name}...")
        run_ssh(f"kubectl patch deployment {name} -n igaming-source --type=merge -p '{patch_json}'")

print("\n--- Deleting any stuck pending pods to let them recreate with IfNotPresent ---")
run_ssh("kubectl delete pods -n igaming-source -l 'app in (igaming-source-pinnacle-loader, igaming-source-1xbet-loader, igaming-source-melbet-loader, igaming-source-888starz-loader, igaming-source-fansport-loader, igaming-source-megapari-loader)' --force --grace-period=0")
