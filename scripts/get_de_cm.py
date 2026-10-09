import subprocess

cmd = ['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50',
       'ssh root@172.16.0.29 "kubectl get configmap purevpn-de-config -n proxy -o jsonpath=\'{.data.client\\\\.ovpn}\'"']

res = subprocess.run(cmd, capture_output=True, text=True)
with open(r'scripts/purevpn-de-client.ovpn', 'w') as f:
    f.write(res.stdout)
print('Length:', len(res.stdout))
print('First 30 lines:')
print('\n'.join(res.stdout.splitlines()[:30]))
