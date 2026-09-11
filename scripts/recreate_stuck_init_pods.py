import subprocess

# Get pods in igaming-source
output = subprocess.check_output(["kubectl", "get", "pods", "-n", "igaming-source", "-o", "wide"]).decode("utf-8")
stuck_pods = []

for line in output.splitlines():
    parts = line.split()
    if len(parts) >= 3:
        pod_name = parts[0]
        status = parts[2]
        # Check if pod is stuck in Init:0/2 or Init:0/1
        if "Init:0/" in status or status == "Error":
            stuck_pods.append(pod_name)

print(f"Found {len(stuck_pods)} stuck pods in Init phase.")
if stuck_pods:
    print("Deleting stuck pods to force fresh recreation...")
    subprocess.run(["kubectl", "delete", "pod", "-n", "igaming-source"] + stuck_pods)
