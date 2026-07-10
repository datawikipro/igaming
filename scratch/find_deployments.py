import os
import yaml

for root, dirs, files in os.walk('c:/Users/chernousov_a/IdeaProjects/igaming'):
    for f in files:
        if f.endswith('.yaml'):
            path = os.path.join(root, f)
            # Skip bookmakers to avoid long output
            if "igaming-k8s" in path or "igaming-source-" in path:
                continue
            try:
                with open(path, 'r', encoding='utf-8', errors='ignore') as file:
                    docs = yaml.safe_load_all(file)
                    for doc in docs:
                        if isinstance(doc, dict) and doc.get('kind') == 'Deployment':
                            print(f"{path} -> {doc.get('metadata', {}).get('name')}")
            except Exception as e:
                pass
