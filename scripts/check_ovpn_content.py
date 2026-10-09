import subprocess

script = """
kubectl exec -n proxy proxy-postgres-0 -- psql -U postgres -d service_proxy -t -A -c "SELECT id, username, length(ovpn_content), substring(ovpn_content from 1 for 100) FROM openvpn_config WHERE id IN (133, 141);"
"""
res = subprocess.run(["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", script],
                     capture_output=True, text=True)
print(res.stdout)
