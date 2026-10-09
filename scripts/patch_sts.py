import subprocess

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'root@100.78.183.101', 'kubectl -n plane patch sts plane-ai-worker --type=merge -p \'{"spec":{"updateStrategy":{"type":"RollingUpdate"}}}\'']
res = subprocess.run(cmd, capture_output=True, text=True)
print("STDOUT:", res.stdout)
print("STDERR:", res.stderr)
