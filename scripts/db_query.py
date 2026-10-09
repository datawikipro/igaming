import subprocess

cmd = ['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50',
       'ssh root@172.16.0.29 "kubectl exec -n proxy proxy-postgres-0 -- psql -U postgres -d service_proxy -c \\"SELECT * FROM vpn_account;\\""']

res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
print(res.stderr)
