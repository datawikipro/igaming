import subprocess

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

cmd = "kubectl get pods -n igaming-source --no-headers"
res = subprocess.run(['ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', XEON_HOST, cmd], capture_output=True, text=True)
keywords = ['apuestatotal', 'bet7k', 'betesporte', 'betnacional', 'betway', 'esportesdasorte', 'vaidebet', 'betrivers', 'paf', '10bet', 'unibet']
for line in res.stdout.strip().splitlines():
    parts = line.split()
    if not parts:
        continue
    pod_name = parts[0]
    if any(k in pod_name for k in keywords):
        status = parts[2]
        if 'ImagePull' in status or 'Err' in status or 'Terminating' in status:
            print(f"Deleting stuck pod {pod_name} (status {status})...")
            subprocess.run(['ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', XEON_HOST, f"kubectl delete pod {pod_name} -n igaming-source --grace-period=0 --force"], capture_output=True)

print("Done resetting stuck pods.")
