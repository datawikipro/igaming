import subprocess
import sys

SSH_CMD = ['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', 'prodius@100.83.113.50']

remote_script = """
import json, subprocess, shutil

shutil.copyfile('/etc/sing-box/config.json', '/etc/sing-box/config.json.bak_k8s_purevpn_fix')

with open('/etc/sing-box/config.json', 'r') as f:
    conf = json.load(f)

changed = 0
for r in conf.get('route', {}).get('rules', []):
    if r.get('outbound') == 'k8s-purevpn-de':
        r['outbound'] = 'outline-us'
        changed += 1

with open('/etc/sing-box/config.json', 'w') as f:
    json.dump(conf, f, indent=2)

print(f"Updated {changed} rules to outline-us")

res = subprocess.run(['sing-box', 'check', '-c', '/etc/sing-box/config.json'], capture_output=True, text=True)
print("sing-box check code:", res.returncode)
if res.stderr:
    print("sing-box check stderr:", res.stderr)

if res.returncode == 0:
    subprocess.run(['systemctl', 'restart', 'sing-box'])
    print("sing-box service restarted successfully!")
else:
    print("Check failed! Restoring backup...")
    shutil.copyfile('/etc/sing-box/config.json.bak_k8s_purevpn_fix', '/etc/sing-box/config.json')
"""

cmd = SSH_CMD + ['sudo python3 -']
p = subprocess.run(cmd, input=remote_script, capture_output=True, text=True)
print("STDOUT:", p.stdout)
print("STDERR:", p.stderr)
print("EXIT:", p.returncode)
