import subprocess

cmd = ['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50',
       'sudo cat /etc/sing-box/config.json']

res = subprocess.run(cmd, capture_output=True, text=True)
with open(r'scripts/sing-box-config.json', 'w', encoding='utf-8') as f:
    f.write(res.stdout)
print('Config saved, lines:', len(res.stdout.splitlines()))
