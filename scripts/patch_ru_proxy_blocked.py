import subprocess
import sys

SSH_HOST = "prodius@100.83.113.50"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

remote_code = """
import json, subprocess

path = "/etc/sing-box/rules/blocked_general.json"
with open(path, "r", encoding="utf-8") as f:
    data = json.load(f)

current_domains = set(data["rules"][0]["domain_suffix"])

new_domains = [
    "twimg.com", ".twimg.com",
    "reddit.com", ".reddit.com",
    "redd.it", ".redd.it",
    "redditmedia.com", ".redditmedia.com",
    "redditstatic.com", ".redditstatic.com",
    "threads.net", ".threads.net",
    "meta.com", ".meta.com",
    "patreon.com", ".patreon.com",
    "patreonusercontent.com", ".patreonusercontent.com",
    "tiktok.com", ".tiktok.com",
    "tiktokcdn.com", ".tiktokcdn.com",
    "byteoversea.com", ".byteoversea.com",
    "ibytedtos.com", ".ibytedtos.com",
    "5sim.net", ".5sim.net"
]

for d in new_domains:
    current_domains.add(d)

data["rules"][0]["domain_suffix"] = sorted(list(current_domains))

with open(path, "w", encoding="utf-8") as f:
    json.dump(data, f, indent=2, ensure_ascii=False)

print("Updated blocked_general.json with total domains:", len(data["rules"][0]["domain_suffix"]))

# Check sing-box config
res = subprocess.run(["sing-box", "check", "-c", "/etc/sing-box/config.json"], capture_output=True, text=True)
print("sing-box check:", res.returncode, res.stdout, res.stderr)
if res.returncode == 0:
    subprocess.run(["systemctl", "restart", "sing-box"], check=True)
    print("sing-box restarted successfully!")
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "sudo python3 -"
]

res = subprocess.run(cmd, input=remote_code, capture_output=True, text=True, timeout=30, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
