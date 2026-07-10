#!/usr/bin/env python3
"""
Scales all igaming-source-* deployments in igaming-dev namespace to 1 replica.
This restores the crawlers and loaders to active status now that the cluster is healthy.
"""
import subprocess
import json

NAMESPACE = "igaming-dev"

def run(args):
    result = subprocess.run(args, capture_output=True, text=True)
    return result.stdout.strip(), result.stderr.strip(), result.returncode

# Get all deployments
out, err, rc = run(["kubectl", "get", "deployments", "-n", NAMESPACE, "-o", "json"])
if rc != 0:
    print(f"Failed to get deployments: {err}")
    exit(1)

deployments = json.loads(out)
scaled = 0

for dep in deployments["items"]:
    name = dep["metadata"]["name"]
    if "igaming-source" in name and ("crawler" in name or "loader" in name):
        print(f"Scaling up: {name}")
        _, _, rc2 = run(["kubectl", "scale", "deployment", name, "--replicas=1", "-n", NAMESPACE])
        if rc2 == 0:
            scaled += 1

print(f"\nScaled up {scaled} deployments to 1 replica.")
