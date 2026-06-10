import json
import subprocess
import time

def run_cmd(cmd):
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error running {' '.join(cmd)}: {result.stderr}")
    return result

def main():
    print("Getting all deployments in igaming-dev namespace...")
    cmd = ["kubectl", "get", "deployments", "-n", "igaming-dev", "-o", "json", "--request-timeout=15s"]
    result = run_cmd(cmd)
    if result.returncode != 0:
        return
        
    deploy_data = json.loads(result.stdout)
    deployments = []
    
    for item in deploy_data.get("items", []):
        name = item.get("metadata", {}).get("name", "")
        if name.startswith("igaming-source-") and (name.endswith("-crawler") or name.endswith("-loader")):
            deployments.append(name)
            
    print(f"Found {len(deployments)} crawler/loader deployments.")
    
    # Step 1: Scale down all to 0
    print("Scaling down all crawler/loader deployments to 0...")
    for deploy in deployments:
        print(f"Scaling down {deploy}...")
        scale_cmd = ["kubectl", "scale", f"deployment/{deploy}", "--replicas=0", "-n", "igaming-dev", "--request-timeout=15s"]
        run_cmd(scale_cmd)
        
    # Step 2: Wait for pods to terminate
    print("Waiting 15 seconds for pods to terminate...")
    time.sleep(15)
    
    # Step 3: Scale back to 1
    print("Scaling all crawler/loader deployments back to 1...")
    for deploy in deployments:
        print(f"Scaling up {deploy}...")
        scale_cmd = ["kubectl", "scale", f"deployment/{deploy}", "--replicas=1", "-n", "igaming-dev", "--request-timeout=15s"]
        run_cmd(scale_cmd)
        
    print("Scale restart sequence completed successfully!")

if __name__ == "__main__":
    main()
