import subprocess
import sys

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

DEPLOYMENTS = {
    'igaming-source-unibet': ('igaming-source-unibet', 'igaming-source-unibet'),
    'igaming-source-10bet': ('igaming-source-10bet-json', 'igaming-source-10bet-json'),
    'igaming-source-betway': ('igaming-source-betway-json', 'igaming-source-betway-json'),
    'igaming-source-apuestatotal': ('igaming-source-apuestatotal-json', 'igaming-source-apuestatotal-json'),
    'igaming-source-bet7k': ('igaming-source-bet7k-json', 'igaming-source-bet7k-json'),
    'igaming-source-betesporte': ('igaming-source-betesporte-json', 'igaming-source-betesporte-json'),
    'igaming-source-betnacional': ('igaming-source-betnacional-json', 'igaming-source-betnacional-json'),
    'igaming-source-esportesdasorte': ('igaming-source-esportesdasorte-json', 'igaming-source-esportesdasorte-json'),
    'igaming-source-vaidebet': ('igaming-source-vaidebet-json', 'igaming-source-vaidebet-json'),
    'igaming-source-paf': ('igaming-source-paf', 'igaming-source-paf'),
    'igaming-source-betrivers': ('igaming-source-betrivers', 'igaming-source-betrivers')
}

def run_ssh(cmd, timeout=120):
    print(f"\n>>> SSH: {cmd}", flush=True)
    res = subprocess.run([
        'ssh', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=timeout)
    print("STDOUT:", res.stdout.strip(), flush=True)
    if res.stderr:
        print("STDERR:", res.stderr.strip(), flush=True)
    return res

print("=== Setting images and restarting deployments ===", flush=True)
for m, (dep, container) in DEPLOYMENTS.items():
    image = f"100.78.183.101:30500/{m}:latest"
    print(f"\n--- Updating {dep} ({container}) -> {image} ---", flush=True)
    run_ssh(f"kubectl set image deployment/{dep} {container}={image} -n igaming-source")
    run_ssh(f"kubectl rollout restart deployment/{dep} -n igaming-source")

print("\n=== Waiting for Rollout Completion ===", flush=True)
for dep, _ in DEPLOYMENTS.values():
    print(f"\nWaiting for {dep}...", flush=True)
    run_ssh(f"kubectl rollout status deployment/{dep} -n igaming-source --timeout=60s")

print("\n=== Checking Pods Status ===", flush=True)
res = run_ssh("kubectl get pods -n igaming-source -o wide | grep -E '(unibet|10bet|betway|apuestatotal|bet7k|betesporte|betnacional|esportesdasorte|vaidebet|paf|betrivers)'")
