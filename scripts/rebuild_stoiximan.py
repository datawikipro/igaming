import subprocess
import sys
import tarfile
import os
import time

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(cmd, timeout=300, ignore_errors=False):
    print(f"\n>>> SSH: {cmd}", flush=True)
    full_cmd = f"cd /data/igaming && {cmd}" if not cmd.startswith("kubectl") and not cmd.startswith("crictl") and not cmd.startswith("ctr") and not cmd.startswith("cd ") and not cmd.startswith("podman") else cmd
    res = subprocess.run([
        'ssh', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, full_cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=timeout)
    print("STDOUT:", res.stdout[-2000:] if len(res.stdout) > 2000 else res.stdout, flush=True)
    if res.stderr:
        print("STDERR:", res.stderr[-1000:] if len(res.stderr) > 1000 else res.stderr, flush=True)
    if res.returncode != 0 and not ignore_errors:
        print(f"FAILED with exit code {res.returncode}", flush=True)
        sys.exit(res.returncode)
    return res

archive_name = "stoiximan_rebuild.tar.gz"
print("Packaging stoiximan...", flush=True)
with tarfile.open(archive_name, "w:gz") as tar:
    for root, dirs, files in os.walk('igaming-source-stoiximan'):
        if 'target' in dirs:
            dirs.remove('target')
        for file in files:
            full_p = os.path.join(root, file)
            tar.add(full_p)
    if os.path.exists('igaming-k8s/stoiximan.yaml'):
        tar.add('igaming-k8s/stoiximan.yaml')

print("Uploading to Xeon...", flush=True)
subprocess.run(['scp', '-i', SSH_KEY, '-o', 'BatchMode=yes', archive_name, f"{XEON_HOST}:/data/igaming/{archive_name}"], check=True)
run_ssh(f"tar -xzf {archive_name} && rm -f {archive_name}")
if os.path.exists(archive_name):
    os.remove(archive_name)

print("\n--- 1. Build & Push igaming-source-stoiximan with Jib via Podman ---", flush=True)
run_ssh(
    "podman run --rm --net=host -v /root/.m2:/root/.m2 -v /data/igaming:/data/igaming -w /data/igaming/igaming-source-stoiximan "
    "docker.io/library/maven:3.9-eclipse-temurin-21 "
    "mvn compile jib:build -DskipTests "
    "-Djib.from.image=127.0.0.1:30500/eclipse-temurin:21-jre "
    "-Djib.to.image=127.0.0.1:30500/igaming-source-stoiximan:latest "
    "-Djib.allowInsecureRegistries=true -B",
    timeout=300
)

print("\n--- 2. Pull updated image into containerd ---", flush=True)
run_ssh("ctr -n k8s.io images pull 127.0.0.1:30500/igaming-source-stoiximan:latest --plain-http", ignore_errors=True)

print("\n--- 3. Delete legacy resources ---", flush=True)
run_ssh("kubectl delete deployment igaming-source-stoiximan -n igaming-source --ignore-not-found=true", ignore_errors=True)
run_ssh("kubectl delete service igaming-source-stoiximan -n igaming-source --ignore-not-found=true", ignore_errors=True)
run_ssh("kubectl delete statefulset igaming-source-stoiximan-db -n igaming-source --ignore-not-found=true", ignore_errors=True)
run_ssh("kubectl delete service igaming-source-stoiximan-db -n igaming-source --ignore-not-found=true", ignore_errors=True)
run_ssh("kubectl delete pvc data-igaming-source-stoiximan-db-0 -n igaming-source --ignore-not-found=true", ignore_errors=True)

print("\n--- 4. Apply updated stoiximan.yaml manifest ---", flush=True)
run_ssh("kubectl apply -f igaming-k8s/stoiximan.yaml")

print("\n--- 5. Restart rollout to pick up new image ---", flush=True)
run_ssh("kubectl rollout restart deployment/igaming-source-stoiximan-json -n igaming-source")

print("\n--- 6. Wait for rollout of igaming-source-stoiximan-json ---", flush=True)
run_ssh("kubectl rollout status deployment/igaming-source-stoiximan-json -n igaming-source --timeout=120s")

print("\n--- 7. Check logs ---", flush=True)
time.sleep(10)
run_ssh("kubectl logs -n igaming-source deployment/igaming-source-stoiximan-json --tail=50")

print("\n>>> Stoiximan rebuild & rollout complete!", flush=True)
