import subprocess
import json
import time

raw = subprocess.check_output(["kubectl", "get", "deployments", "-n", "igaming-source", "-o", "json"])
deps = json.loads(raw)["items"]

json_targets = [d["metadata"]["name"] for d in deps if d["metadata"]["name"].endswith("-json")]
print(f"Scaling {len(json_targets)} -json deployments to replicas=1...")

for name in sorted(json_targets):
    print(f"Scaling {name} -> 1")
    subprocess.call(["kubectl", "scale", f"deployment/{name}", "--replicas=1", "-n", "igaming-source"])
    time.sleep(1)

print("✅ All -json deployments scaled to 1!")
