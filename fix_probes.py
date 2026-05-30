import os

def replace_probes(file_path):
    print(f"Processing: {file_path}")
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    new_content = content
    # Replace /actuator/health/liveness -> /api/v1/monitoring/info
    new_content = new_content.replace("path: /actuator/health/liveness", "path: /api/v1/monitoring/info")
    # Replace /actuator/health/readiness -> /api/v1/monitoring/info
    new_content = new_content.replace("path: /actuator/health/readiness", "path: /api/v1/monitoring/info")
    # Replace /actuator/health -> /api/v1/monitoring/info (for any other variants)
    new_content = new_content.replace("path: /actuator/health", "path: /api/v1/monitoring/info")

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
                    if replace_probes(full_path):
                        updated_count += 1
    
    print(f"\nFinished! Total updated files: {updated_count}")

if __name__ == "__main__":
    main()
