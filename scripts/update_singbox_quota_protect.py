import json
import shutil
import subprocess
import sys

CONFIG_PATH = '/etc/sing-box/config.json'
BACKUP_PATH = '/etc/sing-box/config.json.bak_quota_20261005'

shutil.copyfile(CONFIG_PATH, BACKUP_PATH)
print(f"Created backup at {BACKUP_PATH}")

with open(CONFIG_PATH, 'r', encoding='utf-8') as f:
    config = json.load(f)

# 1. Update general-outbound
for ob in config.get('outbounds', []):
    if ob.get('tag') == 'general-outbound':
        ob['outbounds'] = ['outline-nl', 'outline-us']
        print("Updated general-outbound outbounds to ['outline-nl', 'outline-us']")

# 2. Update route rules
for rule in config.get('route', {}).get('rules', []):
    rule_set = rule.get('rule_set')
    if rule_set == 'betting_eu_fi':
        old_outbound = rule.get('outbound')
        rule['outbound'] = 'k8s-purevpn-de'
        print(f"Updated rule_set betting_eu_fi outbound from {old_outbound} to k8s-purevpn-de")
    elif rule_set == 'blocked_general':
        old_outbound = rule.get('outbound')
        rule['outbound'] = 'general-outbound'
        print(f"Updated rule_set blocked_general outbound from {old_outbound} to general-outbound")
    
    suffixes = rule.get('domain_suffix', []) or []
    if any('pinnacle.com' in s for s in suffixes):
        old_outbound = rule.get('outbound')
        rule['outbound'] = 'k8s-purevpn-de'
        print(f"Updated pinnacle domain_suffix rule outbound from {old_outbound} to k8s-purevpn-de")

# Write to file
with open(CONFIG_PATH, 'w', encoding='utf-8') as f:
    json.dump(config, f, indent=2, ensure_ascii=False)

print("Saved new config. Testing with sing-box check...")
res = subprocess.run(['sing-box', 'check', '-c', CONFIG_PATH], capture_output=True, text=True)
if res.returncode != 0:
    print("ERROR checking sing-box config:")
    print(res.stderr)
    shutil.copyfile(BACKUP_PATH, CONFIG_PATH)
    print("Reverted to backup.")
    sys.exit(1)

print("sing-box check passed successfully! Restarting service...")
res_restart = subprocess.run(['sudo', 'systemctl', 'restart', 'sing-box'], capture_output=True, text=True)
if res_restart.returncode != 0:
    print("ERROR restarting sing-box:")
    print(res_restart.stderr)
    sys.exit(1)

print("sing-box restarted successfully!")
