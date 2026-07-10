#!/usr/bin/env python3
"""
Force deletes stuck pods (Terminating, ContainerStatusUnknown, Error) across all namespaces.
This frees up node resources and keeps the cluster clean.
"""
import subprocess
import json

def run(args):
    result = subprocess.run(args, capture_output=True, text=True)
    return result.stdout.strip(), result.stderr.strip(), result.returncode

# Get all pods
out, err, rc = run(["kubectl", "get", "pods", "-A", "-o", "json"])
if rc != 0:
    print(f"Failed to get pods: {err}")
    exit(1)

pods = json.loads(out)
deleted = 0

for pod in pods["items"]:
    ns = pod["metadata"]["namespace"]
    name = pod["metadata"]["name"]
    
    # Check deletionTimestamp (means Terminating)
    is_terminating = "deletionTimestamp" in pod["metadata"]
    
    # Check status phase and container states
    status = pod.get("status", {})
    phase = status.get("phase", "")
    reason = status.get("reason", "")
    
    should_delete = False
    
    if is_terminating:
        should_delete = True
        print(f"Pod {ns}/{name} is Terminating.")
    elif phase == "Failed" or reason in ("Error", "ContainerStatusUnknown"):
        should_delete = True
        print(f"Pod {ns}/{name} is in status {phase}/{reason}.")
    elif "containerStatuses" in status:
        for c in status["containerStatuses"]:
            state = c.get("state", {})
            if "waiting" in state and state["waiting"].get("reason") in ("ContainerStatusUnknown", "StartError", "ImagePullBackOff", "ErrImagePull"):
                should_delete = True
                print(f"Pod {ns}/{name} has container {c['name']} with status {state['waiting']['reason']}.")
                break

    if should_delete:
        print(f"Force deleting pod {ns}/{name}...")
        _, _, rc2 = run(["kubectl", "delete", "pod", name, "-n", ns, "--force", "--grace-period=0"])
        if rc2 == 0:
            deleted += 1

print(f"\nForce deleted {deleted} pods.")
