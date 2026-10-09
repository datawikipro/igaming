import subprocess
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

print("Starting sanitary scaling of inactive deployments...", flush=True)

# 1. Get all statefulsets in igaming-source
res_sts = subprocess.run(['kubectl', 'get', 'statefulsets', '-n', 'igaming-source', '-o', 'json'], capture_output=True, text=True)
sts_data = json.loads(res_sts.stdout).get('items', [])

dbs_at_0 = set()
dbs_at_1 = set()
for s in sts_data:
    name = s['metadata']['name']
    reps = s['spec'].get('replicas', 0)
    if reps == 0:
        dbs_at_0.add(name)
    else:
        dbs_at_1.add(name)

print(f"Total StatefulSets: {len(sts_data)} (DB=0: {len(dbs_at_0)}, DB=1: {len(dbs_at_1)})", flush=True)

# 2. Get all deployments in igaming-source
res_deps = subprocess.run(['kubectl', 'get', 'deployments', '-n', 'igaming-source', '-o', 'json'], capture_output=True, text=True)
deps_data = json.loads(res_deps.stdout).get('items', [])

# Map deployment to its expected DB
def find_expected_db(dep_name):
    raw = dep_name
    if raw.startswith('igaming-source-'):
        raw = raw[len('igaming-source-'):]
    if raw.endswith('-crawler'):
        raw = raw[:-len('-crawler')]
    elif raw.endswith('-loader'):
        raw = raw[:-len('-loader')]
    elif raw.endswith('-json'):
        raw = raw[:-len('-json')]
    elif raw.endswith('-refresher'):
        raw = raw[:-len('-refresher')]
        
    # Check exact match
    exact = f"igaming-source-{raw}-db"
    return exact

to_scale_0 = []
kept_active = []

for d in deps_data:
    dname = d['metadata']['name']
    reps = d['spec'].get('replicas', 0)
    if reps == 0:
        continue
        
    exp_db = find_expected_db(dname)
    if exp_db in dbs_at_0:
        to_scale_0.append((dname, exp_db))
    elif exp_db in dbs_at_1:
        kept_active.append((dname, exp_db))
    else:
        # DB doesn't exist or special deployment
        if "refresher" in dname:
            kept_active.append((dname, "SPECIAL_REFRESHER"))
        else:
            to_scale_0.append((dname, f"MISSING_{exp_db}"))

print(f"\nActive Deployments KEPT RUNNING: {len(kept_active)}")
print(f"Inactive Deployments to scale to 0: {len(to_scale_0)}")

# Save list to scratch for safety/rollback
with open("scratch/scaled_down_deployments.json", "w", encoding="utf-8") as f:
    json.dump([item[0] for item in to_scale_0], f, indent=2)

print("\nExecuting scale to 0 for inactive deployments...", flush=True)
scaled_count = 0
for dname, reason in to_scale_0:
    res = subprocess.run(['kubectl', 'scale', 'deployment', dname, '-n', 'igaming-source', '--replicas=0'], capture_output=True, text=True)
    if res.returncode == 0:
        scaled_count += 1
    else:
        print(f"Error scaling {dname}: {res.stderr.strip()}", flush=True)

print(f"Successfully scaled {scaled_count} deployments to 0.", flush=True)

# 3. Check new pod status in igaming-source
print("\nWaiting 5 seconds for pod termination...", flush=True)
import time
time.sleep(5)

res_pods = subprocess.run(['kubectl', 'get', 'pods', '-n', 'igaming-source', '--no-headers'], capture_output=True, text=True)
statuses = {}
for line in res_pods.stdout.splitlines():
    parts = line.split()
    if len(parts) >= 3:
        st = parts[2]
        statuses[st] = statuses.get(st, 0) + 1

print("\n=== NEW POD STATUS DISTRIBUTION IN igaming-source ===")
for st, cnt in sorted(statuses.items(), key=lambda x: -x[1]):
    print(f"  {st}: {cnt}")
