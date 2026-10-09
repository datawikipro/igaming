#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Patch sing-box on ru-proxy:
1. Update google_ai_us.json with all google and twitter domains.
2. Route blocked_general and telegram through outline-us instead of dead outline-fi.
3. Update dns-fi detour to outline-us.
4. Verify config with sing-box check and restart service.
"""
import subprocess
import json
import sys

SSH_HOST = "prodius@100.83.113.50"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

remote_patch = """
import json
import shutil
import subprocess

# 1. Patch /etc/sing-box/rules/google_ai_us.json
rule_path = '/etc/sing-box/rules/google_ai_us.json'
shutil.copyfile(rule_path, rule_path + '.bak')
with open(rule_path, 'r') as f:
    rule_data = json.load(f)

current_suffixes = rule_data['rules'][0].get('domain_suffix', [])
new_domains = [
    "google.com", ".google.com",
    "googleapis.com", ".googleapis.com",
    "gstatic.com", ".gstatic.com",
    "googleusercontent.com", ".googleusercontent.com",
    "googletagmanager.com", ".googletagmanager.com",
    "google-analytics.com", ".google-analytics.com",
    "x.com", ".x.com",
    "twitter.com", ".twitter.com",
    "t.co", ".t.co",
    "twimg.com", ".twimg.com",
    "reddit.com", ".reddit.com",
    "redd.it", ".redd.it",
    "threads.net", ".threads.net",
    "instagram.com", ".instagram.com",
    "facebook.com", ".facebook.com"
]

for d in new_domains:
    if d not in current_suffixes:
        current_suffixes.append(d)

rule_data['rules'][0]['domain_suffix'] = current_suffixes
with open(rule_path, 'w') as f:
    json.dump(rule_data, f, indent=2)
print("Updated google_ai_us.json successfully.")

# 2. Patch /etc/sing-box/config.json
cfg_path = '/etc/sing-box/config.json'
shutil.copyfile(cfg_path, cfg_path + '.bak_ai')
with open(cfg_path, 'r') as f:
    cfg = json.load(f)

# Update DNS detours
for d in cfg.get('dns', {}).get('servers', []):
    if d.get('tag') == 'dns-fi':
        d['detour'] = 'outline-us'
        print("Updated dns-fi detour to outline-us")

# Update DNS rules
for r in cfg.get('dns', {}).get('rules', []):
    if r.get('rule_set') in ('blocked_general', 'telegram'):
        r['server'] = 'dns-us'
        print(f"Updated DNS rule for {r.get('rule_set')} to dns-us")

# Update Route rules
for r in cfg.get('route', {}).get('rules', []):
    if r.get('rule_set') in ('blocked_general', 'telegram'):
        r['outbound'] = 'outline-us'
        print(f"Updated Route rule for {r.get('rule_set')} to outline-us")

with open(cfg_path, 'w') as f:
    json.dump(cfg, f, indent=2)
print("Updated config.json successfully.")

# 3. Check config
res = subprocess.run(['sing-box', 'check', '-c', cfg_path], capture_output=True, text=True)
print("sing-box check:", res.stdout, res.stderr)
if res.returncode == 0:
    restart = subprocess.run(['systemctl', 'restart', 'sing-box'], capture_output=True, text=True)
    print("sing-box restart:", restart.returncode)
else:
    print("CHECK FAILED! Restoring backup...")
    shutil.copyfile(cfg_path + '.bak_ai', cfg_path)
    shutil.copyfile(rule_path + '.bak', rule_path)
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "sudo python3 -"
]

res = subprocess.run(cmd, input=remote_patch, capture_output=True, text=True, timeout=30)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
