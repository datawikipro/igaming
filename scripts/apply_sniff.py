import json
import subprocess
import shutil
import sys

CONFIG_PATH = '/etc/sing-box/config.json'
BACKUP_PATH = '/etc/sing-box/config.json.bak_sniff2'

shutil.copyfile(CONFIG_PATH, BACKUP_PATH)

with open(CONFIG_PATH, 'r', encoding='utf-8') as f:
    config = json.load(f)

# In sing-box 1.13+, sniff is a rule action in route.rules:
# { "action": "sniff" }
rules = config.setdefault('route', {}).setdefault('rules', [])

# Check if sniff rule is already at the beginning
has_sniff = any(r.get('action') == 'sniff' for r in rules)
if not has_sniff:
    rules.insert(0, {"action": "sniff"})
    print("Added {'action': 'sniff'} at the beginning of route.rules")

with open(CONFIG_PATH, 'w', encoding='utf-8') as f:
    json.dump(config, f, indent=2, ensure_ascii=False)

res = subprocess.run(['sing-box', 'check', '-c', CONFIG_PATH], capture_output=True, text=True)
if res.returncode != 0:
    print("ERROR sing-box check:")
    print(res.stderr)
    shutil.copyfile(BACKUP_PATH, CONFIG_PATH)
    sys.exit(1)

print("sing-box check PASSED! Restarting service...")
subprocess.run(['sudo', 'systemctl', 'restart', 'sing-box'], check=True)
print("sing-box successfully restarted with sniff action in route.rules!")
