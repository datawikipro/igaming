import subprocess

sql = "SELECT id, ovpn_content FROM openvpn_config WHERE id = 109;"
cmd = ["ssh", "-i", r"C:\Users\chernousov_a\.ssh\id_ed25519", "-o", "BatchMode=yes", "prodius@100.83.113.50", "ssh root@172.16.0.29 'kubectl exec -i -n proxy proxy-postgres-0 -- psql -U postgres -d service_proxy'"]
res = subprocess.run(cmd, input=sql, capture_output=True, text=True)
for line in res.stdout.splitlines()[:15]:
    print(line)



