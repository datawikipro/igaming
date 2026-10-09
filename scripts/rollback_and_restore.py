import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(cmd):
    print(f"\n>>> SSH: {cmd}")
    res = subprocess.run([
        'ssh', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    print("STDOUT:", res.stdout)
    if res.stderr:
        print("STDERR:", res.stderr)
    return res

deployments = [
    "igaming-source-1xbet-crawler",
    "igaming-source-1xbet-loader",
    "igaming-source-888starz-crawler",
    "igaming-source-888starz-loader",
    "igaming-source-fansport-crawler",
    "igaming-source-fansport-loader",
    "igaming-source-megapari-crawler",
    "igaming-source-megapari-loader",
    "igaming-source-melbet-com-crawler",
    "igaming-source-melbet-com-loader",
    "igaming-source-melbet-crawler",
    "igaming-source-melbet-loader",
    "igaming-source-pinnacle-crawler",
    "igaming-source-sbobet-crawler",
    "igaming-source-sbobet-loader"
]

for d in deployments:
    print(f"Rolling back {d}...")
    run_ssh(f"kubectl rollout undo deployment/{d} -n igaming-source")

print("\n--- Current pods in igaming-source ---")
run_ssh("kubectl get pods -n igaming-source | grep -E 'pinnacle|1xbet|melbet|888starz|fansport|megapari|sbobet'")
