import subprocess

cmd = [
    "ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101",
    """kubectl exec -n proxy proxy-postgres-0 -- psql -U postgres -d service_proxy -c "SELECT id, name, openvpn_config_id FROM proxy_node WHERE name LIKE 'purevpn-uk%' OR name LIKE 'purevpn-sg%';" """
]
res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
