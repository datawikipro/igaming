import subprocess
import sys
import tarfile
import os

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

MODULES = [
    'igaming-source-core',
    'igaming-source-unibet',
    'igaming-source-10bet',
    'igaming-source-betway',
    'igaming-source-apuestatotal',
    'igaming-source-bet7k',
    'igaming-source-betesporte',
    'igaming-source-betnacional',
    'igaming-source-esportesdasorte',
    'igaming-source-vaidebet',
    'igaming-source-paf',
    'igaming-source-betrivers'
]

# Mapping module to (deployment_name, container_name)
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

def run_ssh(cmd, timeout=300):
    print(f"\n>>> SSH: {cmd}", flush=True)
    full_cmd = f"cd /data/igaming && {cmd}" if not cmd.startswith("kubectl") and not cmd.startswith("cd ") else cmd
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

def create_archive():
    archive_name = "batch2_oop.tar.gz"
    print(f"Creating {archive_name}...", flush=True)
    with tarfile.open(archive_name, "w:gz") as tar:
        for m in MODULES:
            for item in ['src', 'pom.xml']:
                p = os.path.join(m, item)
                if os.path.exists(p):
                    print(f"  Adding {p}", flush=True)
                    tar.add(p)
    print("Archive created successfully.", flush=True)
    return archive_name

def upload_and_extract(archive_name):
    print("Uploading archive to Xeon...", flush=True)
    res = subprocess.run([
        'scp', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        archive_name, f"{XEON_HOST}:/data/igaming/{archive_name}"
    ], capture_output=True, text=True)
    if res.returncode != 0:
        print("SCP failed:", res.stderr, flush=True)
        sys.exit(res.returncode)
    print("Extracting on Xeon...", flush=True)
    run_ssh(f"cd /data/igaming && tar -xzf {archive_name} && rm -f {archive_name}")

def build_all():
    print("\n--- 1. Build and install igaming-source-core ---", flush=True)
    run_ssh("/usr/local/bin/mvn -f igaming-source-core/pom.xml install -DskipTests -B", timeout=180)

    for m in MODULES:
        if m == 'igaming-source-core':
            continue
        print(f"\n--- Building & Pushing {m} via Jib ---", flush=True)
        run_ssh(
            f"/usr/local/bin/mvn -f {m}/pom.xml compile "
            f"com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
            f"-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
            f"-Djib.to.image=100.78.183.101:30500/{m}:latest "
            f"-Djib.allowInsecureRegistries=true -DskipTests -B",
            timeout=300
        )

def update_and_rollout():
    print("\n--- Updating K8s Deployments and Rolling Out ---", flush=True)
    for m, (dep, container) in DEPLOYMENTS.items():
        image = f"100.78.183.101:30500/{m}:latest"
        print(f"Updating deployment {dep}: container {container} -> {image}", flush=True)
        run_ssh(f"kubectl set image deployment/{dep} {container}={image} -n igaming-source")
        run_ssh(f"kubectl rollout restart deployment/{dep} -n igaming-source")

    print("\n--- Checking Pods Status ---", flush=True)
    run_ssh("kubectl get pods -n igaming-source -o wide | grep -E '(unibet|10bet|betway|apuestatotal|bet7k|betesporte|betnacional|esportesdasorte|vaidebet|paf|betrivers)'")

if __name__ == '__main__':
    archive = create_archive()
    upload_and_extract(archive)
    build_all()
    update_and_rollout()
    print("\n=== BATCH 2 SUCCESSFULLY BUILT, DEPLOYED, AND ROLLED OUT! ===")
