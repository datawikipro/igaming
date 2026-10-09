import subprocess
import sys

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

cmd = "kubectl get deployments -n igaming-source -o jsonpath='{.items[*].metadata.name}'"
res = subprocess.run([
    'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
    XEON_HOST, cmd
], capture_output=True, text=True)

keywords = ['unibet', 'paf', 'betriver', '10bet', 'apuest', 'bet7k', 'betesp', 'betnac', 'esportes', 'vaidebet', 'betway', 'kambi', 'altenar', 'sbtech']
all_deployments = res.stdout.strip().replace("'", "").split()
matched = [d for d in all_deployments if any(k in d for k in keywords)]
print(f"Matched {len(matched)} deployments:")
for d in matched:
    print(" ", d)
