import subprocess
import json
import sys
import time

def migrate_source(name):
    old_dep_name = f"igaming-source-{name}"
    new_dep_name = f"igaming-source-{name}-json"
    
    print(f"\n--- Migrating {old_dep_name} -> {new_dep_name} ---")
    
    # 1. Get existing deployment
    try:
        raw = subprocess.check_output(["kubectl", "get", "deployment", old_dep_name, "-n", "igaming-source", "-o", "json"])
        dep = json.loads(raw)
    except subprocess.CalledProcessError:
        print(f"Deployment {old_dep_name} not found, skipping.")
        return False
        
    # 2. Clean metadata
    dep["metadata"]["name"] = new_dep_name
    dep["metadata"]["labels"]["app"] = new_dep_name
    for field in ["resourceVersion", "uid", "creationTimestamp", "generation", "annotations", "status"]:
        dep.pop(field, None)
    dep["metadata"].pop("annotations", None)
    
    # 3. Update spec selector and template labels
    dep["spec"]["selector"]["matchLabels"]["app"] = new_dep_name
    dep["spec"]["template"]["metadata"]["labels"]["app"] = new_dep_name
    dep["spec"]["template"]["metadata"].pop("annotations", None)
    
    # 4. Update container name & env vars
    container = dep["spec"]["template"]["spec"]["containers"][0]
    container["name"] = new_dep_name
    
    env = container.setdefault("env", [])
    # Add or update APP_CRAWLER_DISCOVERY_INTERVAL_MS = 10000 (10s)
    has_interval = False
    for e in env:
        if e.get("name") in ["APP_CRAWLER_DISCOVERY_INTERVAL_MS", "app.crawler.discovery.interval-ms"]:
            e["value"] = "10000"
            has_interval = True
    if not has_interval:
        env.append({"name": "APP_CRAWLER_DISCOVERY_INTERVAL_MS", "value": "10000"})
        
    # 5. Apply new deployment
    manifest_path = f"/tmp/{new_dep_name}.json"
    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump(dep, f, indent=2)
        
    print(f"Applying new deployment {new_dep_name}...")
    subprocess.check_call(["kubectl", "apply", "-f", manifest_path])
    
    # 6. Wait for rollout to complete
    print(f"Waiting for rollout of {new_dep_name}...")
    rollout_ok = False
    try:
        subprocess.check_call(["kubectl", "rollout", "status", f"deployment/{new_dep_name}", "-n", "igaming-source", "--timeout=180s"])
        rollout_ok = True
    except subprocess.CalledProcessError as e:
        print(f"❌ Rollout of {new_dep_name} timed out or failed: {e}. Preserving {old_dep_name}!")
        return False

    # 7. Update service if exists
    try:
        svc_raw = subprocess.check_output(["kubectl", "get", "service", old_dep_name, "-n", "igaming-source", "-o", "json"])
        svc = json.loads(svc_raw)
        if svc.get("spec", {}).get("selector", {}).get("app") == old_dep_name:
            svc["spec"]["selector"]["app"] = new_dep_name
            svc_path = f"/tmp/{old_dep_name}_svc.json"
            with open(svc_path, "w", encoding="utf-8") as f:
                json.dump(svc, f, indent=2)
            subprocess.check_call(["kubectl", "apply", "-f", svc_path])
            print(f"Updated service {old_dep_name} selector to {new_dep_name}!")
    except Exception as e:
        print(f"Service update note: {e}")

    # 8. Delete old deployment
    print(f"New deployment {new_dep_name} is READY! Deleting old deployment {old_dep_name}...")
    subprocess.check_call(["kubectl", "delete", "deployment", old_dep_name, "-n", "igaming-source"])
    print(f"✅ Successfully migrated {name} to {new_dep_name}!")
    time.sleep(5)
    return True

if __name__ == "__main__":
    targets = sys.argv[1:] if len(sys.argv) > 1 else ["bovada"]
    for t in targets:
        migrate_source(t)
