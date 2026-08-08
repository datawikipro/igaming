#!/usr/bin/env python3
"""
Fix namespaces and Hikari initialization timeouts across all K8s manifests in igaming-k8s/
"""

import re
import sys
from pathlib import Path

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

K8S_DIR = Path(__file__).resolve().parent.parent / "igaming-k8s"

def fix_manifests():
    updated_files = 0
    yaml_files = list(K8S_DIR.glob("*.yaml"))
    
    print(f"🔍 Inspecting {len(yaml_files)} K8s manifests in {K8S_DIR}...")
    
    for y in yaml_files:
        # Skip manifests that belong to igaming-master or igaming-dev explicitly (like portal/bot/grafana)
        name = y.name.lower()
        if any(skip in name for skip in ["aggregator", "portal", "bot", "grafana", "prometheus"]):
            continue
            
        content = y.read_text(encoding="utf-8")
        original = content
        
        # 1. Update namespace from igaming-dev to igaming-source for bookmaker sources
        content = content.replace("namespace: igaming-dev", "namespace: igaming-source")
        
        # 2. Update HIKARI INITIALIZATION_FAIL_TIMEOUT from "-1" to "0"
        content = re.sub(
            r'(- name:\s*SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT\s*\n\s*value:\s*)"-1"',
            r'\1"0"',
            content
        )
        content = re.sub(
            r'\n\s*- name:\s*SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT\s*\n\s*value:\s*"3000"',
            r'',
            content
        )
        
        # 3. Use FQDN for database URLs to bypass ndots:5 search domain lookups and prevent UnknownHostException
        content = re.sub(
            r'jdbc:postgresql://(igaming-source-[a-zA-Z0-9\.\_-]+-db):5432',
            r'jdbc:postgresql://\1.igaming-source.svc.cluster.local:5432',
            content
        )
        
        if content != original:
            y.write_text(content, encoding="utf-8")
            updated_files += 1
            print(f"  • Updated {y.name}")
            
    print(f"\n🎉 Successfully updated {updated_files} K8s manifests!")

if __name__ == "__main__":
    fix_manifests()
