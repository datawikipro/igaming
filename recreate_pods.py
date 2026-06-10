import json
import subprocess

def main():
    print("Getting all pods in igaming-dev namespace...")
    cmd = ["kubectl", "get", "pods", "-n", "igaming-dev", "-o", "json", "--request-timeout=15s"]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print("Error getting pods:", result.stderr)
        return
        
    pods_data = json.loads(result.stdout)
    pods = pods_data.get("items", [])
    
    # We want to identify pods that are running (or not pending) and belong to crawler/loader deployments,
    # where there is also a pending pod for the same app.
    # App label identifies the deployment, e.g., app=igaming-source-winline-crawler
    
    pending_apps = set()
    running_pods = []
    
    for pod in pods:
        app = pod.get("metadata", {}).get("labels", {}).get("app", "")
        if not app.startswith("igaming-source-"):
            continue
        if "-crawler" not in app and "-loader" not in app:
            continue
            
        status = pod.get("status", {}).get("phase", "")
        pod_name = pod.get("metadata", {}).get("name", "")
        
        if status == "Pending":
            pending_apps.add(app)
        elif status == "Running":
            running_pods.append((pod_name, app))
            
    print(f"Found {len(pending_apps)} apps with pending pods.")
    print(f"Found {len(running_pods)} running pods for crawlers/loaders.")
    
    deleted_count = 0
    for pod_name, app in running_pods:
        if app in pending_apps:
            print(f"Deleting running pod {pod_name} because there is a pending pod for {app}...")
            del_cmd = ["kubectl", "delete", "pod", pod_name, "-n", "igaming-dev", "--grace-period=0", "--force", "--request-timeout=15s"]
            subprocess.run(del_cmd)
            deleted_count += 1
            
    print(f"Successfully deleted {deleted_count} running pods to break the deadlock.")

if __name__ == "__main__":
    main()
