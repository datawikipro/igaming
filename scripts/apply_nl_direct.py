import subprocess

with open(r'scripts/purevpn-nl-direct.yaml', 'r') as f:
    yaml_content = f.read()

cmd = ['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50',
       'ssh root@172.16.0.29 "cat > /tmp/purevpn-nl-direct.yaml && kubectl apply -f /tmp/purevpn-nl-direct.yaml"']

p = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
stdout, stderr = p.communicate(input=yaml_content)
print('STDOUT:', stdout)
print('STDERR:', stderr)
