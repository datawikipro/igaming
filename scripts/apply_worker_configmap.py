import subprocess

# Read updated daemon code
with open("scripts/updated_daemon.py", "r", encoding="utf-8") as f:
    daemon_code = f.read()

# Copy to remote server /tmp/plane_worker_daemon.py
scp_cmd = ["scp", "-i", "C:\\Users\\chernousov_a\\.ssh\\id_ed25519", "-o", "BatchMode=yes", "scripts/updated_daemon.py", "root@100.78.183.101:/tmp/plane_worker_daemon.py"]
res_scp = subprocess.run(scp_cmd, capture_output=True, text=True)
if res_scp.returncode != 0:
    print("SCP failed:", res_scp.stderr)
    exit(1)

print("Copied updated_daemon.py to remote /tmp/plane_worker_daemon.py")

# Create or update ConfigMap plane-ai-worker-daemon in namespace plane
remote_apply = """
kubectl -n plane create configmap plane-ai-worker-daemon \
  --from-file=plane_worker_daemon.py=/tmp/plane_worker_daemon.py \
  --dry-run=client -o yaml | kubectl apply -f -
"""

ssh_cmd = ["ssh", "-i", "C:\\Users\\chernousov_a\\.ssh\\id_ed25519", "-o", "BatchMode=yes", "root@100.78.183.101", remote_apply]
res_ssh = subprocess.run(ssh_cmd, capture_output=True, text=True)
print("ConfigMap Apply stdout:", res_ssh.stdout)
print("ConfigMap Apply stderr:", res_ssh.stderr)
if res_ssh.returncode == 0:
    print("ConfigMap plane-ai-worker-daemon successfully updated!")
else:
    print("Failed to update ConfigMap!")
