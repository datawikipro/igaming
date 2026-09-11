import subprocess

# Get all deployments in igaming-source
output = subprocess.check_output(["kubectl", "get", "deployments", "-n", "igaming-source", "-o", "name"]).decode("utf-8")
deployments = [line.strip() for line in output.splitlines() if line.strip()]

print(f"Found {len(deployments)} deployments in igaming-source.")
print("Triggering rollout restart for all deployments so they adopt the updated initContainer manifests...")

# Restart in batches of 10
batch_size = 10
for i in range(0, len(deployments), batch_size):
    batch = deployments[i:i+batch_size]
    print(f"Restarting batch {i//batch_size + 1}: {', '.join([b.replace('deployment.apps/', '') for b in batch])}")
    subprocess.run(["kubectl", "rollout", "restart", "-n", "igaming-source"] + batch)

print("Rollout restart trigger complete.")
