import subprocess
import json

update_script = """
import json, shutil, subprocess

config_path = "/etc/sing-box/config.json"
backup_path = "/etc/sing-box/config.json.bak_before_fix_ts_us"

shutil.copyfile(config_path, backup_path)
print("Backup created at", backup_path)

with open(config_path, "r", encoding="utf-8") as f:
    cfg = json.load(f)

# Update outline-us outbound
updated = False
for o in cfg.get("outbounds", []):
    if o.get("tag") == "outline-us":
        o["type"] = "shadowsocks"
        o["server"] = "100.110.168.10"
        o["server_port"] = 443
        o["method"] = "chacha20-ietf-poly1305"
        o["password"] = "qZORoKkMxgUdJu7dZrGVY7"
        o["bind_interface"] = "tailscale0"
        # remove vless specific fields
        o.pop("uuid", None)
        o.pop("flow", None)
        o.pop("packet_encoding", None)
        o.pop("tls", None)
        updated = True
        print("Updated outline-us outbound to 100.110.168.10:443")
        break

if not updated:
    raise RuntimeError("outline-us not found in outbounds")

with open(config_path, "w", encoding="utf-8") as f:
    json.dump(cfg, f, indent=2, ensure_ascii=False)

print("Saved config.json. Testing config with sing-box check...")
chk = subprocess.run(["sing-box", "check", "-C", "/etc/sing-box"], capture_output=True, text=True)
if chk.returncode != 0:
    print("CHECK FAILED:", chk.stderr)
    shutil.copyfile(backup_path, config_path)
    print("Reverted to backup!")
    exit(1)

print("Config syntax is VALID! Restarting sing-box service...")
restart_res = subprocess.run(["systemctl", "restart", "sing-box"], capture_output=True, text=True)
if restart_res.returncode != 0:
    print("RESTART FAILED:", restart_res.stderr)
    shutil.copyfile(backup_path, config_path)
    subprocess.run(["systemctl", "restart", "sing-box"])
    exit(1)

print("sing-box restarted successfully!")
"""

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f'sudo python3 -c {subprocess.list2cmdline([update_script])}']
res = subprocess.run(cmd, capture_output=True, text=True)
print("OUTPUT:")
print(res.stdout)
print(res.stderr)
