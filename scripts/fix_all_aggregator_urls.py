import subprocess
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(cmd, timeout=60):
    res = subprocess.run([
        'ssh', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=timeout)
    if res.returncode != 0:
        print(f"FAILED: {cmd}\nSTDERR: {res.stderr}")
    return res

res = run_ssh("kubectl get deployments -n igaming-source -o json")
data = json.loads(res.stdout)

CORRECT_URL = "http://igaming-aggregator.igaming-dev.svc.cluster.local:8080"

wrong_deps = set()
for item in data.get("items", []):
    name = item["metadata"]["name"]
    for c in item["spec"]["template"]["spec"]["containers"]:
        for e in c.get("env", []):
            if e.get("name") == "APP_AGGREGATOR_URL":
                val = e.get("value", "")
                if val in ["http://igaming-aggregator", "http://igaming-aggregator:8080", "http://igaming-aggregator.igaming-dev.svc.cluster.local", "http://igaming-aggregator.igaming-dev.svc.cluster.local:3034"]:
                    wrong_deps.add(name)

print(f"Found {len(wrong_deps)} deployments with wrong APP_AGGREGATOR_URL:")
for d in sorted(wrong_deps):
    print(f" - {d}")

for d in sorted(wrong_deps):
    print(f"Updating {d}...")
    run_ssh(f"kubectl set env deployment/{d} -n igaming-source APP_AGGREGATOR_URL={CORRECT_URL}")

print("Done updating all APP_AGGREGATOR_URL env vars!")
