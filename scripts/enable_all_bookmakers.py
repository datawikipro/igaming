import subprocess
import json
import time
import sys

sys.stdout.reconfigure(encoding='utf-8')

print("==================================================", flush=True)
print("ACTIVATING ALL BOOKMAKERS IN igaming-source (1/1)", flush=True)
print("==================================================", flush=True)

# 1. Scale all StatefulSets to 1
print("\nStep 1: Scaling all 172 Database StatefulSets to replicas=1...", flush=True)
res_sts = subprocess.run(['kubectl', 'get', 'statefulsets', '-n', 'igaming-source', '-o', 'json'], capture_output=True, text=True)
sts = json.loads(res_sts.stdout).get('items', [])

scaled_sts = 0
for s in sts:
    name = s['metadata']['name']
    reps = s['spec'].get('replicas', 0)
    if reps != 1:
        subprocess.run(['kubectl', 'scale', 'statefulset', name, '-n', 'igaming-source', '--replicas=1'], capture_output=True, text=True)
        scaled_sts += 1

print(f"Scaled {scaled_sts} Database StatefulSets to replicas=1.", flush=True)

# 2. Scale all Deployments to 1
print("\nStep 2: Scaling all 225 Crawler/Loader Deployments to replicas=1...", flush=True)
res_deps = subprocess.run(['kubectl', 'get', 'deployments', '-n', 'igaming-source', '-o', 'json'], capture_output=True, text=True)
deps = json.loads(res_deps.stdout).get('items', [])

scaled_deps = 0
for d in deps:
    name = d['metadata']['name']
    reps = d['spec'].get('replicas', 0)
    if reps != 1:
        subprocess.run(['kubectl', 'scale', 'deployment', name, '-n', 'igaming-source', '--replicas=1'], capture_output=True, text=True)
        scaled_deps += 1

print(f"Scaled {scaled_deps} Crawler/Loader Deployments to replicas=1.", flush=True)

print("\nStep 3: Waiting 15 seconds for initial pod scheduling...", flush=True)
time.sleep(15)

res_pods = subprocess.run(['kubectl', 'get', 'pods', '-n', 'igaming-source', '--no-headers'], capture_output=True, text=True)
statuses = {}
for line in res_pods.stdout.splitlines():
    parts = line.split()
    if len(parts) >= 3:
        st = parts[2]
        statuses[st] = statuses.get(st, 0) + 1

print("\n=== POD STATUS DISTRIBUTION IN igaming-source ===")
print(f"Total pods scheduled: {sum(statuses.values())}")
for st, cnt in sorted(statuses.items(), key=lambda x: -x[1]):
    print(f"  {st}: {cnt}")
