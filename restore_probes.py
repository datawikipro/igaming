import os
import re

def restore_probes(file_path):
    print(f"Processing: {file_path}")
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    new_content = content
    
    # Use regex to replace path under livenessProbe
    new_content = re.sub(
        r"(livenessProbe:\s+httpGet:\s+path:)\s*/actuator/health/liveness",
        r"\1 /api/v1/monitoring/info",
        new_content
    )
    
    # Use regex to replace path under readinessProbe
    new_content = re.sub(
        r"(readinessProbe:\s+httpGet:\s+path:)\s*/actuator/health/readiness",
        r"\1 /api/v1/monitoring/info",
        new_content
    )

    if new_content != content:
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"  [UPDATED] {file_path}")
        return True
    else:
        print("  [NO CHANGE]")
        return False

def main():
    updated_count = 0
    # Walk through the directory and look for igaming-source-*.yaml in k8s/dev directories
    for root, dirs, files in os.walk("."):
        if "k8s" in root and "dev" in root:
            for file in files:
                if file.startswith("igaming-source-") and file.endswith(".yaml"):
                    full_path = os.path.join(root, file)
                    if restore_probes(full_path):
                        updated_count += 1
                        
                        # Apply the changes to the cluster
                        print(f"  Applying {full_path} to K8s...")
                        os.system(f'set KUBECONFIG=C:\\Users\\chernousov_a\\.kube\\igaming-cluster.yaml && kubectl apply -f "{full_path}"')
    
    print(f"\nFinished! Total updated files: {updated_count}")

if __name__ == "__main__":
    main()
