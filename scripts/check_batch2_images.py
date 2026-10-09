import subprocess
import sys

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

keywords = [
    'igaming-source-10bet-json',
    'igaming-source-apuestatotal-json',
    'igaming-source-bet7k-json',
    'igaming-source-betesporte-json',
    'igaming-source-betnacional-json',
    'igaming-source-betrivers',
    'igaming-source-betway-json',
    'igaming-source-esportesdasorte-json',
    'igaming-source-paf',
    'igaming-source-unibet',
    'igaming-source-vaidebet-json'
]

for d in keywords:
    cmd = f"kubectl get deployment {d} -n igaming-source -o jsonpath='{{.spec.template.spec.containers[*].image}}'"
    res = subprocess.run([
        'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ], capture_output=True, text=True)
    img = res.stdout.strip()
    print(f"{d:36} -> {img}")
