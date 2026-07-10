#!/usr/bin/env python3
"""
Patches CPU requests for all igaming-source-* deployments in igaming-dev.
Writes patch JSON to temp file to avoid quoting issues on Windows.
"""
import subprocess
import json
import tempfile
import os

NAMESPACE = "igaming-dev"
CPU_REQUEST = "300m"
MEM_REQUEST = "256Mi"

def run(args):
    result = subprocess.run(args, capture_output=True, text=True)
    return result.stdout.strip(), result.stderr.strip(), result.returncode

# Get all deployments as JSON
out, err, rc = run(["kubectl", "get", "deployments", "-n", NAMESPACE, "-o", "json"])
if rc != 0:
    print(f"Failed to get deployments: {err}")
    exit(1)

deployments = json.loads(out)
patched = 0
skipped = 0
failed = 0

for dep in deployments["items"]:
    name = dep["metadata"]["name"]
    if "igaming-source" not in name:
        skipped += 1
        continue

    containers = dep["spec"]["template"]["spec"]["containers"]
    patch_containers = []

    for c in containers:
        cname = c["name"]
        # Skip sidecars and infra containers
        if cname in ("wait-for-sidecar", "xvfb", "redis", "wait-for-low-cpu"):
            continue

        current_req = c.get("resources", {}).get("requests", {})
        current_cpu = current_req.get("cpu", "300m")
        current_mem = current_req.get("memory", "256Mi")

        # Skip only if already at target to avoid redundant patching
        if current_cpu == CPU_REQUEST and current_mem == MEM_REQUEST:
            continue

        patch_containers.append({
            "name": cname,
            "resources": {
                "requests": {
                    "cpu": CPU_REQUEST,
                    "memory": MEM_REQUEST
                }
            }
        })

    if not patch_containers:
        continue

    patch = {
        "spec": {
            "template": {
                "spec": {
                    "containers": patch_containers
                }
            }
        }
    }

    # Write patch to temp file to avoid quoting issues
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
        if rc == 0:
            names = [c['name'] for c in patch_containers]
            print(f"[OK] {name}: patched containers {names} -> cpu={CPU_REQUEST}")
            patched += 1
        else:
            print(f"[FAIL] {name}: {err}")
            failed += 1
    finally:
        os.unlink(tmp_path)

print(f"\nDone: {patched} patched, {failed} failed, {skipped} skipped (non-source)")
print(f"\nNow run: kubectl rollout restart deployment -n {NAMESPACE} -l app in igaming-source")
