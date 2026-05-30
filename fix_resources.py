import os
import re

def fix_resources_in_file(file_path):
    print(f"Processing resources in: {file_path}")
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    new_content = content

    # 1. Crawler resources replacement
    # We want to replace the resources block for crawler
    # Search for crawler container, find its resources: block and replace it
    crawler_res_pattern = r"(- name: igaming-source-[\w-]+-crawler.*?resources:)\s+requests:\s+cpu:\s+\S+\s+memory:\s+\S+\s+limits:\s+cpu:\s+\S+\s+memory:\s+\S+"
    # Wait, some might have requests/limits in different order. Let's make it simpler:
    # Just match `resources:` and any lines following it that start with spaces until `ports:` or another section, or we can use a more flexible regex.
    # A robust way is to replace the resources: block directly:
    # Let's match from `resources:` to the next non-indented or lesser-indented line or livenessProbe/ports.
    # In these files:
    #         resources:
    #           requests:
    #             cpu: ...
    #             memory: ...
    #           limits:
    #             cpu: ...
    #             memory: ...
    
    # Let's write regex to match resources block for crawler
    crawler_res = """        resources:
          requests:
            cpu: 50m
            memory: 256Mi
          limits:
            cpu: 500m
            memory: 1.5Gi"""
            
    loader_res = """        resources:
          requests:
            cpu: 30m
            memory: 192Mi
          limits:
            cpu: 300m
            memory: 512Mi"""
            
    redis_res = """        resources:
          requests:
            cpu: 5m
            memory: 32Mi
          limits:
            cpu: 50m
            memory: 64Mi"""

    # We can match specifically:
    # - name: igaming-source-xxxx-crawler
    # ...
    #         resources:
    #           requests:
    #             cpu: ...
    #             memory: ...
    #           limits:
    #             cpu: ...
    #             memory: ...
    # Look at the crawler block structure. It has `resources:` block.
    # Let's replace the resources block of crawler:
    # We find the block starting with `- name: igaming-source-xxxx-crawler` and ending with the next container `- name: redis`
    # and replace its `resources:` block.
    
    # Helper to replace resources inside a specific container block
    def replace_container_resources(block, res_content):
        # Find `resources:` and replace everything until the end of the block or next field
        # The resources block has lines indented by 8 or 10 or 12 spaces.
        # We can find `resources:` and replace all lines after it that have more than 8 spaces indent.
        block = re.sub(
            r"(\s+)resources:\s*\n(\s+\w+:\s*\n?)+", # matches resources and subkeys
            res_content + "\n",
            block
        )
        # Actually, let's use a very robust regex that matches the exact structure:
        block = re.sub(
            r"\s+resources:\s*\n\s+requests:\s*\n\s+cpu:\s*\S+\s*\n\s+memory:\s*\S+\s*\n\s+limits:\s*\n\s+cpu:\s*\S+\s*\n\s+memory:\s*\S+",
            "\n" + res_content,
            block
        )
        # If it matches requests/limits in different order or format:
        block = re.sub(
            r"\s+resources:\s*\n\s+limits:\s*\n\s+cpu:\s*\S+\s*\n\s+memory:\s*\S+\s*\n\s+requests:\s*\n\s+cpu:\s*\S+\s*\n\s+memory:\s*\S+",
            "\n" + res_content,
            block
        )
        return block

    # Let's do a simple split and replace using regex on the whole content for the specific container names.
    # To be extremely precise, let's find:
    # `- name: igaming-source-XXX-crawler` ... `ports:`
    # Since ports: always follows resources: or vice versa, let's match the entire container block:
    # We can find `- name: igaming-source-XXX-crawler` up to `- name: redis`
    # Let's use a regex with a custom replace function:
    
    def crawler_match(m):
        block = m.group(0)
        return replace_container_resources(block, crawler_res)
        
    def loader_match(m):
        block = m.group(0)
        return replace_container_resources(block, loader_res)
        
    def redis_match(m):
        block = m.group(0)
        return replace_container_resources(block, redis_res)

    # Replace crawler
    new_content = re.sub(
        r"- name: igaming-source-[\w-]+-crawler.*?(?=- name: redis)",
        crawler_match,
        new_content,
        flags=re.DOTALL
    )
    
    # Replace loader
    # It starts with `- name: igaming-source-XXX-loader` and ends with `- name: redis`
    new_content = re.sub(
        r"- name: igaming-source-[\w-]+-loader.*?(?=- name: redis)",
        loader_match,
        new_content,
        flags=re.DOTALL
    )
    
    # Replace redis
    # It starts with `- name: redis` and ends with `---` or end of string
    new_content = re.sub(
        r"- name: redis.*?(?=(---|apiVersion:|$))",
        redis_match,
        new_content,
        flags=re.DOTALL
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
                    if fix_resources_in_file(full_path):
                        updated_count += 1
                        
                        # Apply the changes to the cluster
                        print(f"  Applying {full_path} to K8s...")
                        os.system(f'set KUBECONFIG=C:\\Users\\chernousov_a\\.kube\\igaming-cluster.yaml && kubectl apply -f "{full_path}"')
    
    print(f"\nFinished! Total updated files: {updated_count}")

if __name__ == "__main__":
    main()
