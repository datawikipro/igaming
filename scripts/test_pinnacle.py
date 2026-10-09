import subprocess

cmd = 'kubectl exec -n proxy deployment/purevpn-nl-direct -c curl-tester -- curl -sI -o /dev/null -w "%{http_code}" --proxy http://127.0.0.1:3128 --max-time 10 https://guest.api.arcadia.pinnacle.com/0.1/sports/19/matchups'
res = subprocess.run(['ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f"ssh root@172.16.0.29 '{cmd}'"], capture_output=True, text=True)
print("Pinnacle HTTP code:", res.stdout)
print("STDERR:", res.stderr)

