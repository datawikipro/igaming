import json
import shutil
import subprocess
import sys

CONFIG_PATH = '/etc/sing-box/config.json'
BACKUP_PATH = '/etc/sing-box/config.json.bak_reorder_20261005'

shutil.copyfile(CONFIG_PATH, BACKUP_PATH)
print("Backup created at", BACKUP_PATH)

with open(CONFIG_PATH, 'r', encoding='utf-8') as f:
    config = json.load(f)

rules = config.get('route', {}).get('rules', [])

# 1. Ensure sniff action is at index 0
has_sniff = any(r.get('action') == 'sniff' for r in rules)
if not has_sniff:
    rules.insert(0, {"action": "sniff"})

# 2. Extract betting rules so we can promote them before generic IP rules
betting_rules = []
other_rules = []
sniff_rule = None

for r in rules:
    if r.get('action') == 'sniff':
        sniff_rule = r
    elif r.get('rule_set') in ['betting_ru', 'betting_us', 'betting_eu_fi']:
        # Ensure outbound is correct
        if r.get('rule_set') == 'betting_ru':
            r['outbound'] = 'direct'
        else:
            r['outbound'] = 'k8s-purevpn-de'
        betting_rules.append(r)
    else:
        # Check domain-specific overrides
        suffixes = r.get('domain_suffix') or []
        if any('pinnacle.com' in s for s in suffixes):
            r['outbound'] = 'k8s-purevpn-de'
            betting_rules.append(r)
            continue
        
        # General blocked sites and telegram -> use outline-nl (GCP Netherlands) to save paid US quota
        if r.get('rule_set') == 'blocked_general':
            r['outbound'] = 'outline-nl'
        elif r.get('rule_set') == 'telegram':
            r['outbound'] = 'outline-nl'
        
        # Cloudflare IP rule -> route to outline-nl instead of general-outbound
        ip_cidrs = r.get('ip_cidr') or []
        if '172.64.0.0/13' in ip_cidrs or '198.41.128.0/17' in ip_cidrs:
            r['outbound'] = 'outline-nl'

        other_rules.append(r)

# Reassemble rules:
# 1) Sniff action
# 2) Specific betting domain/rule_set rules (high priority, unmetered k8s-purevpn-de)
# 3) Other rules (AI -> outline-us, Cloudflare / blocked -> outline-nl, direct -> direct)
new_rules = []
if sniff_rule:
    new_rules.append(sniff_rule)
new_rules.extend(betting_rules)
new_rules.extend(other_rules)

config['route']['rules'] = new_rules

# Write back
with open(CONFIG_PATH, 'w', encoding='utf-8') as f:
    json.dump(config, f, indent=2, ensure_ascii=False)

print("Saved updated configuration. Testing with sing-box check...")
res = subprocess.run(['sing-box', 'check', '-c', CONFIG_PATH], capture_output=True, text=True)
if res.returncode != 0:
    print("ERROR sing-box check failed:")
    print(res.stderr)
    shutil.copyfile(BACKUP_PATH, CONFIG_PATH)
    sys.exit(1)

print("sing-box check PASSED! Restarting sing-box service...")
subprocess.run(['sudo', 'systemctl', 'restart', 'sing-box'], check=True)
print("sing-box restarted successfully with optimized betting priority and quota protection!")
