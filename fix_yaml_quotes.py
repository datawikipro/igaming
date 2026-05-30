import os

def fix_yaml_quotes(file_path):
    print(f"Checking: {file_path}")
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    new_content = content
    # Replace value: http://service-proxy-backend.service-proxy.svc.cluster.local
    # with value: 'http://service-proxy-backend.service-proxy.svc.cluster.local'
    bad_string = "value: http://service-proxy-backend.service-proxy.svc.cluster.local"
    good_string = "value: 'http://service-proxy-backend.service-proxy.svc.cluster.local'"
    
    new_content = new_content.replace(bad_string, good_string)

    if new_content != content:
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"  [FIXED] {file_path}")
        return True
    else:
        print("  [NO CHANGE]")
        return False

def main():
    fixed_count = 0
    for root, dirs, files in os.walk("."):
        if "k8s" in root and "dev" in root:
            for file in files:
                if file.startswith("igaming-source-") and file.endswith(".yaml"):
                    full_path = os.path.join(root, file)
                    if fix_yaml_quotes(full_path):
                        fixed_count += 1
    
    print(f"\nDone! Fixed {fixed_count} files.")

if __name__ == "__main__":
    main()
