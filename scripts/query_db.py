import subprocess

sql = "SELECT id, config_name, protocol, port, server_name FROM openvpn_config WHERE config_name LIKE \\'%nl%\\' LIMIT 10;"
cmd = f"kubectl exec -n proxy proxy-postgres-0 -- psql -U postgres -d service_proxy -c \"{sql}\""
res = subprocess.run(['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f"ssh root@172.16.0.29 '{cmd}'"], capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("ERR:", res.stderr)
