import subprocess

script = """
kubectl exec -n proxy proxy-postgres-0 -- psql -U postgres -d service_proxy -c "\d openvpn_config"
"""
res = subprocess.run(["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", script],
                     capture_output=True, text=True)
print(res.stdout)
