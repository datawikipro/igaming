import subprocess
import sys
import tarfile
import os
import time

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(cmd, timeout=300, ignore_error=False):
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
    if res.returncode != 0 and not ignore_error:
        print(f"FAILED with exit code {res.returncode}", flush=True)
        sys.exit(res.returncode)
    return res

archive_name = "digitain_rebuild.tar.gz"
print("Packaging core and digitain...", flush=True)
with tarfile.open(archive_name, "w:gz") as tar:
    for m in ['igaming-source-core', 'igaming-source-digitain', 'igaming-k8s']:
        if os.path.exists(m):
            tar.add(m)

print("Uploading to Xeon...", flush=True)
subprocess.run(['scp', '-i', SSH_KEY, '-o', 'BatchMode=yes', archive_name, f"{XEON_HOST}:/data/igaming/{archive_name}"], check=True)
run_ssh(f"tar -xzf {archive_name} && rm -f {archive_name}")
if os.path.exists(archive_name):
    os.remove(archive_name)

print("\n--- 1. Rebuild and install igaming-source-core ---", flush=True)
run_ssh("/usr/local/bin/mvn -f igaming-source-core/pom.xml install -DskipTests -B", timeout=120)

print("\n--- 2. Run Unit Tests in igaming-source-digitain ---", flush=True)
run_ssh("/usr/local/bin/mvn -f igaming-source-digitain/pom.xml test -B", timeout=120)

print("\n--- 3. Build & Push igaming-source-digitain with Jib ---", flush=True)
run_ssh(
    "/usr/local/bin/mvn -f igaming-source-digitain/pom.xml compile "
    "com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
    "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
    "-Djib.to.image=100.78.183.101:30500/igaming-source-digitain:latest "
    "-Djib.allowInsecureRegistries=true -DskipTests -B",
    timeout=240
)

print("\n--- 4. Remove cached image from containerd ---", flush=True)
run_ssh("crictl rmi 100.78.183.101:30500/igaming-source-digitain:latest", ignore_error=True)

print("\n--- 5. Apply Digitain manifest & rollout restart ---", flush=True)
run_ssh("kubectl apply -f igaming-k8s/digitain.yaml")
run_ssh("kubectl rollout restart deployment/igaming-source-digitain-crawler -n igaming-source", ignore_error=True)
run_ssh("kubectl rollout restart deployment/igaming-source-digitain-loader -n igaming-source", ignore_error=True)

print("\n--- 6. Wait for rollout ---", flush=True)
run_ssh("kubectl rollout status deployment/igaming-source-digitain-crawler -n igaming-source --timeout=120s", ignore_error=True)

print("\n--- 7. Check pods ---", flush=True)
time.sleep(5)
run_ssh("kubectl get pods -n igaming-source -l app=igaming-source-digitain-crawler")

print("\n>>> Digitain test, build & rollout complete!", flush=True)
