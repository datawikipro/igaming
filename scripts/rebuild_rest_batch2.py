import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

REST_MODULES = [
    ('igaming-source-10bet', 'igaming-source-10bet-json'),
    ('igaming-source-betway', 'igaming-source-betway-json'),
    ('igaming-source-apuestatotal', 'igaming-source-apuestatotal-json'),
    ('igaming-source-bet7k', 'igaming-source-bet7k-json'),
    ('igaming-source-betesporte', 'igaming-source-betesporte-json'),
    ('igaming-source-betnacional', 'igaming-source-betnacional-json'),
    ('igaming-source-esportesdasorte', 'igaming-source-esportesdasorte-json'),
    ('igaming-source-vaidebet', 'igaming-source-vaidebet-json')
]

def run_ssh(cmd, timeout=300):
    print(f"\n>>> SSH: {cmd}", flush=True)
    full_cmd = f"cd /data/igaming && {cmd}" if not cmd.startswith("kubectl") and not cmd.startswith("crictl") and not cmd.startswith("cd ") else cmd
    res = subprocess.run([
        'ssh', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, full_cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=timeout)
    print("STDOUT:", res.stdout[-2000:] if len(res.stdout) > 2000 else res.stdout, flush=True)
    if res.stderr:
        print("STDERR:", res.stderr[-1000:] if len(res.stderr) > 1000 else res.stderr, flush=True)
    if res.returncode != 0:
        print(f"FAILED with exit code {res.returncode}", flush=True)
        sys.exit(res.returncode)
    return res

print("\n--- 1. Clean build & push all 8 Altenar & SBTech modules ---", flush=True)
for m, dep in REST_MODULES:
    print(f"\n>>> Building {m} with fresh core...", flush=True)
    run_ssh(
        f"/usr/local/bin/mvn -f {m}/pom.xml clean compile "
        f"com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
        f"-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
        f"-Djib.to.image=100.78.183.101:30500/{m}:latest "
        f"-Djib.allowInsecureRegistries=true -DskipTests -B",
        timeout=300
    )
    # Remove stale image from containerd cache and restart pod
    print(f">>> Evicting containerd cache for {m} and restarting...", flush=True)
    run_ssh(f"crictl rmi 100.78.183.101:30500/{m}:latest || true")
    run_ssh(f"kubectl delete pod -l app={dep} -n igaming-source --grace-period=0 --force || true")

print("\n=== ALL REST MODULES REBUILT AND RESTARTED WITH FRESH CORE! ===", flush=True)
