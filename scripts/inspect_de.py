import subprocess

cmd = 'kubectl get configmap vpn-pool-openvpn-scripts -n proxy -o yaml'
res = subprocess.run(['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f'ssh root@172.16.0.29 "{cmd}"'], capture_output=True, text=True)
with open(r'C:\Users\chernousov_a\IdeaProjects\igaming\scripts\scripts_cm.yaml', 'w', encoding='utf-8') as f:
    f.write(res.stdout)
print("Saved scripts_cm.yaml")
exit(0)


for line in lines:
    if line.startswith('  client.ovpn: |'):
        in_ovpn = True
        continue
    elif line.startswith('  ') and not line.startswith('    ') and in_ovpn:
        break
    elif line.startswith('kind:') or line.startswith('metadata:'):
        if in_ovpn:
            break
    if in_ovpn:
        ovpn_lines.append(line[4:] if line.startswith('    ') else line)

with open(r'C:\Users\chernousov_a\IdeaProjects\igaming\scripts\de_client.ovpn', 'w') as f:
    f.write('\n'.join(ovpn_lines))

print(f"Extracted DE ovpn lines: {len(ovpn_lines)}")

