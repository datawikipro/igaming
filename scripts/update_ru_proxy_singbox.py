import json
import subprocess
import shutil

CONFIG_PATH = '/etc/sing-box/config.json'
BACKUP_PATH = '/etc/sing-box/config.json.bak_altenar'

shutil.copyfile(CONFIG_PATH, BACKUP_PATH)
print("Backup created at", BACKUP_PATH)

with open(CONFIG_PATH, 'r') as f:
    config = json.load(f)

new_keywords = ["altenar", "sbtech", "10bet", "betway", "apuestatotal"]
new_suffixes = [
    "altenar.com", ".altenar.com",
    "sb-feed.altenar.com", ".sb-feed.altenar.com",
    "sbtech.com", ".sbtech.com",
    "10bet.com", ".10bet.com",
    "betway.com", ".betway.com",
    "apuestatotal.com", ".apuestatotal.com"
]

# 1. Update dns rules
for rule in config.get('dns', {}).get('rules', []):
    if rule.get('server') == 'dns-fi':
        keywords = rule.setdefault('domain_keyword', [])
        for k in new_keywords:
            if k not in keywords:
                keywords.append(k)
        suffixes = rule.setdefault('domain_suffix', [])
        for s in new_suffixes:
            if s not in suffixes:
                suffixes.append(s)

# 2. Update route rules
for rule in config.get('route', {}).get('rules', []):
    if rule.get('outbound') == 'outline-fi':
        keywords = rule.setdefault('domain_keyword', [])
        for k in new_keywords:
            if k not in keywords:
                keywords.append(k)
        suffixes = rule.setdefault('domain_suffix', [])
        for s in new_suffixes:
            if s not in suffixes:
                suffixes.append(s)

with open(CONFIG_PATH, 'w') as f:
    json.dump(config, f, indent=2)

print("Updated config written successfully.")
