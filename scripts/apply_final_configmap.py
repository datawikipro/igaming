import subprocess

# Copy to remote server
scp_cmd = ["scp", "-i", "C:\\Users\\chernousov_a\\.ssh\\id_ed25519", "-o", "BatchMode=yes", "scripts/final_daemon.py", "root@100.78.183.101:/tmp/plane_worker_daemon.py"]
res_scp = subprocess.run(scp_cmd, capture_output=True, text=True)
if res_scp.returncode != 0:
    print("SCP failed:", res_scp.stderr)
    exit(1)

# Apply configmap
remote_apply = """
kubectl -n plane create configmap plane-ai-worker-daemon \
  --from-file=plane_worker_daemon.py=/tmp/plane_worker_daemon.py \
  --dry-run=client -o yaml | kubectl apply -f -
"""

ssh_cmd = ["ssh", "-i", "C:\\Users\\chernousov_a\\.ssh\\id_ed25519", "-o", "BatchMode=yes", "root@100.78.183.101", remote_apply]
res_ssh = subprocess.run(ssh_cmd, capture_output=True, text=True)
print("ConfigMap Apply stdout:", res_ssh.stdout)
print("ConfigMap Apply stderr:", res_ssh.stderr)
