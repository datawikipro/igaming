import os

def fix_resources_in_file(file_path):
    print(f"Processing resources in: {file_path}")
    with open(file_path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    new_lines = []
    current_container = None
    skip_mode = False
    resources_indent = 0
    
    for line in lines:
        stripped = line.strip()
        
        # Reset container if we encounter a new deployment section
        if line.startswith("---") or line.startswith("apiVersion:"):
            current_container = None
            skip_mode = False
            
        # Containers in Kubernetes are always indented by exactly 6 spaces: "      - name: XXX"
        if line.startswith("      - name:"):
            if "crawler" in stripped:
                current_container = "crawler"
                skip_mode = False
            elif "loader" in stripped:
                current_container = "loader"
                skip_mode = False
            elif stripped == "- name: redis":
                current_container = "redis"
                skip_mode = False
            else:
                current_container = None
                skip_mode = False
                
        if skip_mode:
            # Check indentation of current line
            indent = len(line) - len(line.lstrip())
            # Skip lines with greater indentation than the resources line itself
            if indent > resources_indent and stripped != "":
                continue
            else:
                skip_mode = False
                
        if current_container and stripped == "resources:":
            skip_mode = True
            # Detect the exact indentation of the resources line
            resources_indent = len(line) - len(line.lstrip())
            indent_str = " " * resources_indent
            
            # Format resource blocks with the correct detected indentation
            if current_container == "crawler":
                crawler_res = [
                    f"{indent_str}resources:\n",
                    f"{indent_str}  requests:\n",
                    f"{indent_str}    cpu: 50m\n",
                    f"{indent_str}    memory: 256Mi\n",
                    f"{indent_str}  limits:\n",
                    f"{indent_str}    cpu: 500m\n",
                    f"{indent_str}    memory: 1.5Gi\n"
                ]
                new_lines.extend(crawler_res)
            elif current_container == "loader":
                loader_res = [
                    f"{indent_str}resources:\n",
                    f"{indent_str}  requests:\n",
                    f"{indent_str}    cpu: 30m\n",
                    f"{indent_str}    memory: 192Mi\n",
                    f"{indent_str}  limits:\n",
                    f"{indent_str}    cpu: 300m\n",
                    f"{indent_str}    memory: 512Mi\n"
                ]
                new_lines.extend(loader_res)
            elif current_container == "redis":
                redis_res = [
                    f"{indent_str}resources:\n",
                    f"{indent_str}  requests:\n",
                    f"{indent_str}    cpu: 5m\n",
                    f"{indent_str}    memory: 32Mi\n",
                    f"{indent_str}  limits:\n",
                    f"{indent_str}    cpu: 50m\n",
                    f"{indent_str}    memory: 64Mi\n"
                ]
                new_lines.extend(redis_res)
            continue
            
        new_lines.append(line)

    new_content = "".join(new_lines)
    with open(file_path, "r", encoding="utf-8") as f:
        old_content = f.read()
        
    if new_content != old_content:
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"  [UPDATED] {file_path}")
        return True
    else:
        print("  [NO CHANGE]")
        return False

def main():
    updated_count = 0
    # Walk through the directory and look for yaml files in igaming-k8s and k8s/dev directories
    for root, dirs, files in os.walk("."):
        is_igaming_k8s = "igaming-k8s" in root
        is_dev_k8s = "k8s" in root and "dev" in root
        
        if is_igaming_k8s or is_dev_k8s:
            for file in files:
                if file.endswith(".yaml"):
                    if is_dev_k8s and not file.startswith("igaming-source-"):
                        continue
                    # Skip config map / db general file if we only want to optimize crawlers/loaders
                    if file == "igaming-sources-db.yaml":
                        continue
                    
                    full_path = os.path.join(root, file)
                    if fix_resources_in_file(full_path):
                        updated_count += 1
                        
                        # Apply the changes to the cluster
                        print(f"  Applying {full_path} to K8s...")
                        os.system(f'set KUBECONFIG=C:\\Users\\chernousov_a\\.kube\\config && kubectl apply -f "{full_path}"')
    
    print(f"\nFinished! Total updated files: {updated_count}")

if __name__ == "__main__":
    main()
