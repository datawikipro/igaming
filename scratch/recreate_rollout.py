#!/usr/bin/env python3
"""
Switches all igaming-source deployments to Recreate strategy,
then triggers rollout restart, then switches back to RollingUpdate.
This solves the CPU deadlock where new pods can't start because old ones occupy resources.
"""
import subprocess
import json
import tempfile
import os

NAMESPACE = "igaming-dev"

def run(args):
    result = subprocess.run(args, capture_output=True, text=True)
    return result.stdout.strip(), result.stderr.strip(), result.returncode

def patch_deployment(name, patch):
    with tempfile.NamedTemporaryFile(mode='w', suffix='.json', delete=False) as f:
        json.dump(patch, f)
        tmp_path = f.name
    try:
        out, err, rc = run([
            "kubectl", "patch", "deployment", name,
            "-n", NAMESPACE,
            "--type=strategic",
            f"--patch-file={tmp_path}"
        ])
        return rc == 0, err
    finally:
        os.unlink(tmp_path)

# Get all igaming-source deployments
out, _, _ = run(["kubectl", "get", "deployments", "-n", NAMESPACE, "-o", "json"])
deps = json.loads(out)
source_deps = [d["metadata"]["name"] for d in deps["items"] if "igaming-source" in d["metadata"]["name"]]

print(f"Found {len(source_deps)} igaming-source deployments")

# Step 1: Switch to Recreate strategy
print("\n[1/3] Switching to Recreate strategy...")
recreate_patch = {"spec": {"strategy": {"type": "Recreate"}}}
for name in source_deps:
    ok, err = patch_deployment(name, recreate_patch)
    if not ok:
        print(f"  [FAIL] {name}: {err}")

print("  Done.")

# Step 2: Force rollout restart (delete old pods, create new ones with 50m)
print("\n[2/3] Triggering rollout restart...")
for name in source_deps:
    run(["kubectl", "rollout", "restart", f"deployment/{name}", "-n", NAMESPACE])
print("  Done.")

# Step 3: Switch back to RollingUpdate
print("\n[3/3] Switching back to RollingUpdate strategy...")
rolling_patch = {
    "spec": {
        "strategy": {
            "type": "RollingUpdate",
            "rollingUpdate": {
                "maxSurge": 1,
                "maxUnavailable": 0
            }
        }
    }
}
for name in source_deps:
    ok, err = patch_deployment(name, rolling_patch)
    if not ok:
        print(f"  [FAIL] {name}: {err}")

print("  Done.")
print("\nAll done! Check pod status with: kubectl get pods -n igaming-dev")
