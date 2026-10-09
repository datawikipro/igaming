import subprocess

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'root@100.78.183.101', 'kubectl -n plane get cm plane-ai-worker-daemon -o jsonpath="{.data.plane_worker_daemon\\.py}"']
res = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
if res.returncode == 0:
    with open('scripts/current_daemon.py', 'w', encoding='utf-8') as f:
        f.write(res.stdout)
    print("Successfully saved", len(res.stdout), "characters to scripts/current_daemon.py")
else:
    print("Error:", res.stderr)
