import subprocess
import json

raw = subprocess.check_output(["kubectl", "get", "deployments", "-n", "igaming-source", "-o", "json"])
deps = json.loads(raw)["items"]

json_deps = []
heavy_deps = []
refresher_deps = []
other_active = []
zero_replicas = []

for d in deps:
    name = d["metadata"]["name"]
    replicas = d.get("spec", {}).get("replicas", 0)
    ready = d.get("status", {}).get("readyReplicas", 0)
    
    if replicas == 0:
        zero_replicas.append(name)
        continue
        
    if name.endswith("-json"):
        json_deps.append((name, ready))
    elif name.endswith("-crawler") or name.endswith("-loader"):
        heavy_deps.append((name, ready))
    elif name.endswith("-refresher"):
        refresher_deps.append((name, ready))
    else:
        other_active.append((name, ready))

print(f"=== AUDIT REPORT: igaming-source ===")
print(f"1. REST/JSON Deployments (-json): {len(json_deps)}")
for n, r in sorted(json_deps):
    print(f"   ✓ {n} (ready: {r})")

print(f"\n2. Heavy Protected Deployments (-crawler / -loader): {len(heavy_deps)}")
for n, r in sorted(heavy_deps):
    print(f"   ✓ {n} (ready: {r})")

print(f"\n3. Reactive Sentry Refreshers (-refresher): {len(refresher_deps)}")
for n, r in sorted(refresher_deps):
    print(f"   ⚡ {n} (ready: {r})")

print(f"\n4. Non-standard Active Deployments: {len(other_active)}")
for n, r in sorted(other_active):
    print(f"   ⚠️ {n} (ready: {r})")

print(f"\n5. Scaled-to-zero Deployments: {len(zero_replicas)}")
