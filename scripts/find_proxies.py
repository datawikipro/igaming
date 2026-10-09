import subprocess
import json

cmd = "kubectl get deployments -n igaming-source -o json"
res = subprocess.run(['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f"ssh root@172.16.0.29 '{cmd}'"], capture_output=True, text=True)
data = json.loads(res.stdout)

print(f"Total deployments in igaming-source: {len(data['items'])}")
for item in data['items']:
    name = item['metadata']['name']
    text = json.dumps(item)
    if '3128' in text or 'proxy' in text.lower():
        # find proxy env vars
        proxies = []
        for c in item['spec']['template']['spec']['containers']:
            for env in c.get('env', []):
                if 'proxy' in env.get('name', '').lower() or '3128' in env.get('value', ''):
                    proxies.append(f"{env.get('name')}={env.get('value')}")
        if proxies:
            print(f"{name}: {proxies}")
