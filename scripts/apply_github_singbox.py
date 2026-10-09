import subprocess
import json

update_script = """
import json, shutil, subprocess

config_path = "/etc/sing-box/config.json"
backup_path = "/etc/sing-box/config.json.bak_before_github_fix"

shutil.copyfile(config_path, backup_path)
print("Backup created at", backup_path)

with open(config_path, "r", encoding="utf-8") as f:
    cfg = json.load(f)

# 1. In route.rules, find the rule with "outbound": "direct" that has "github.com" and remove github domains from it
rules = cfg.get("route", {}).get("rules", [])
github_domains = ["github.com", ".github.com", "githubusercontent.com", ".githubusercontent.com", "ghcr.io"]

for r in rules:
    if r.get("outbound") == "direct" and "domain_suffix" in r:
        orig = r["domain_suffix"]
        r["domain_suffix"] = [d for d in orig if d not in github_domains]
        if len(orig) != len(r["domain_suffix"]):
            print(f"Removed {len(orig) - len(r['domain_suffix'])} github domains from direct rule")

# 2. Add an explicit rule for github to route via outline-us before the general rules
github_rule = {
    "domain_suffix": github_domains,
    "outbound": "outline-us"
}

# Insert before direct rules
rules.insert(5, github_rule)
print("Inserted github route rule via outline-us")

with open(config_path, "w", encoding="utf-8") as f:
    json.dump(cfg, f, indent=2, ensure_ascii=False)

# Validate config
chk = subprocess.run(["sing-box", "check", "-C", "/etc/sing-box"], capture_output=True, text=True)
if chk.returncode != 0:
    print("CHECK FAILED:", chk.stderr)
    shutil.copyfile(backup_path, config_path)
    print("Reverted to backup!")
    exit(1)

print("Config syntax is VALID! Restarting sing-box...")
restart_res = subprocess.run(["systemctl", "restart", "sing-box"], capture_output=True, text=True)
if restart_res.returncode != 0:
    print("RESTART FAILED:", restart_res.stderr)
    shutil.copyfile(backup_path, config_path)
    subprocess.run(["systemctl", "restart", "sing-box"])
    exit(1)

print("sing-box restarted successfully with github routing via outline-us!")
"""

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f'sudo python3 -c {subprocess.list2cmdline([update_script])}']
res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
print(res.stderr)
