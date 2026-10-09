import subprocess

def dump_ovpn(config_id, filename):
    script = f"""
    kubectl exec -n proxy proxy-postgres-0 -- psql -U postgres -d service_proxy -t -A -c "SELECT ovpn_content FROM openvpn_config WHERE id = {config_id};"
    """
    res = subprocess.run(["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", script],
                         capture_output=True, text=True)
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(res.stdout)
    print(f"Dumped config {config_id} to {filename} ({len(res.stdout)} bytes)")

dump_ovpn(141, 'scripts/purevpn_uk_141.ovpn')
dump_ovpn(133, 'scripts/purevpn_sg_133.ovpn')
