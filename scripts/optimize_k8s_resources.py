#!/usr/bin/env python3
import os, re, sys
from pathlib import Path

K8S_DIR = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\igaming-k8s")

def optimize_manifests():
    yaml_files = sorted(list(K8S_DIR.glob("*.yaml")))
    print(f"Analyzing and optimizing {len(yaml_files)} manifests in {K8S_DIR}...\n")
    
    updated_count = 0
    for file_path in yaml_files:
        if file_path.name in ["antigravity-overlay.yaml", "kafka-externalname-shared.yaml"]:
            continue
            
        content = file_path.read_text(encoding="utf-8")
        original = content
        
        docs = content.split("\n---")
        new_docs = []
        
        for doc in docs:
            # 1. Optimize Crawler Deployment
            if "kind: Deployment" in doc and "-crawler" in doc:
                crawler_res_pattern = r'(name:\s*igaming-source-[a-zA-Z0-9\.\_-]+-crawler[\s\S]*?resources:\s*\n\s*requests:\s*\n\s*cpu:\s*)[^\n]+(\s*\n\s*memory:\s*)[^\n]+(\s*\n\s*limits:\s*\n\s*cpu:\s*)[^\n]+(\s*\n\s*memory:\s*)[^\n]+'
                doc = re.sub(
                    crawler_res_pattern,
                    r'\1 200m\2 384Mi\3 1200m\4 1280Mi',
                    doc
                )
                
            # 2. Optimize Loader Deployment
            elif "kind: Deployment" in doc and "-loader" in doc:
                loader_res_pattern = r'(name:\s*igaming-source-[a-zA-Z0-9\.\_-]+-loader[\s\S]*?resources:\s*\n\s*requests:\s*\n\s*cpu:\s*)[^\n]+(\s*\n\s*memory:\s*)[^\n]+(\s*\n\s*limits:\s*\n\s*cpu:\s*)[^\n]+(\s*\n\s*memory:\s*)[^\n]+'
                doc = re.sub(
                    loader_res_pattern,
                    r'\1 150m\2 200Mi\3 800m\4 512Mi',
                    doc
                )
                
            # 3. Optimize Database StatefulSet
            elif "kind: StatefulSet" in doc and "-db" in doc:
                if "resources:" not in doc:
                    if "volumeMounts:" in doc:
                        doc = doc.replace(
                            "        volumeMounts:",
                            "        resources:\n          requests:\n            cpu: 10m\n            memory: 32Mi\n          limits:\n            cpu: 100m\n            memory: 96Mi\n        volumeMounts:"
                        )
                    elif "ports:" in doc:
                        doc = doc.replace(
                            "        ports:",
                            "        resources:\n          requests:\n            cpu: 10m\n            memory: 32Mi\n          limits:\n            cpu: 100m\n            memory: 96Mi\n        ports:"
                        )
                else:
                    db_res_pattern = r'(name:\s*postgres[\s\S]*?resources:\s*\n\s*requests:\s*\n\s*cpu:\s*)[^\n]+(\s*\n\s*memory:\s*)[^\n]+(\s*\n\s*limits:\s*\n\s*cpu:\s*)[^\n]+(\s*\n\s*memory:\s*)[^\n]+'
                    doc = re.sub(
                        db_res_pattern,
                        r'\1 10m\2 32Mi\3 100m\4 96Mi',
                        doc
                    )
            
            new_docs.append(doc)
            
        new_content = "\n---".join(new_docs)
        if new_content != original:
            file_path.write_text(new_content, encoding="utf-8")
            updated_count += 1
            print(f"  [OK] Optimized {file_path.name}")
            
    print(f"\nTotal manifests updated: {updated_count} / {len(yaml_files)}")

if __name__ == "__main__":
    optimize_manifests()