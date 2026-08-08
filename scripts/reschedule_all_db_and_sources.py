import subprocess

# Delete all DB pods so StatefulSets recreate them on node-type=master nodes
output_db = subprocess.check_output(["kubectl", "get", "pods", "-n", "igaming-source", "-l", "app", "-o", "name"]).decode("utf-8")

db_pods = []
for line in output_db.splitlines():
    if "-db-" in line:
        db_pods.append(line.replace("pod/", ""))

print(f"Found {len(db_pods)} DB pods to reschedule onto master nodes.")

# Delete DB pods in batches of 10
batch_size = 10
for i in range(0, len(db_pods), batch_size):
    batch = db_pods[i:i+batch_size]
    print(f"Rescheduling DB batch {i//batch_size + 1}...")
    subprocess.run(["kubectl", "delete", "pod", "-n", "igaming-source"] + batch)

print("DB pod rescheduling initiated. Triggering rollout restart for all crawler/loader deployments...")

# Get all deployments
output_dep = subprocess.check_output(["kubectl", "get", "deployments", "-n", "igaming-source", "-o", "name"]).decode("utf-8")
deployments = [line.strip() for line in output_dep.splitlines() if line.strip()]

for i in range(0, len(deployments), batch_size):
    batch = deployments[i:i+batch_size]
    subprocess.run(["kubectl", "rollout", "restart", "-n", "igaming-source"] + batch)

print("All DB pods rescheduled onto master nodes and all deployments restarted.")
