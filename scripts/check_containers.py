import subprocess

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

deps = [
    'igaming-source-10bet-json', 'igaming-source-apuestatotal-json',
    'igaming-source-bet7k-json', 'igaming-source-betesporte-json',
    'igaming-source-betnacional-json', 'igaming-source-betrivers',
    'igaming-source-betway-json', 'igaming-source-esportesdasorte-json',
    'igaming-source-paf', 'igaming-source-unibet', 'igaming-source-vaidebet-json'
]

for d in deps:
    res = subprocess.run([
        'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10', XEON_HOST,
        f"kubectl get deployment {d} -n igaming-source -o jsonpath='{{.spec.template.spec.containers[*].name}}'"
    ], capture_output=True, text=True)
    print(f"{d:36}: containers = {res.stdout.strip()}")
