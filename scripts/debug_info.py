import subprocess

cmd = ['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50',
       'ss -tn | grep 158.255.213.224']

res = subprocess.run(cmd, capture_output=True, text=True, timeout=10)
print('Connections to outline-us (158.255.213.224):\n', res.stdout.strip())
