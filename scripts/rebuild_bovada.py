import subprocess
import sys
import tarfile
import os
import time

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

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

archive_name = "bovada_rebuild.tar.gz"
print("Packaging core and bovada...", flush=True)
with tarfile.open(archive_name, "w:gz") as tar:
    for m in ['igaming-dto', 'igaming-source-core', 'igaming-source-bovada']:
        for item in ['src', 'pom.xml']:
            p = os.path.join(m, item)
            if os.path.exists(p):
                tar.add(p)

print("Uploading to Xeon...", flush=True)
subprocess.run(['scp', '-i', SSH_KEY, '-o', 'BatchMode=yes', archive_name, f"{XEON_HOST}:/data/igaming/{archive_name}"], check=True)
run_ssh(f"tar -xzf {archive_name} && rm -f {archive_name}")
if os.path.exists(archive_name):
    os.remove(archive_name)

print("\n--- 1. Install igaming-dto and igaming-source-core ---", flush=True)
run_ssh("/usr/local/bin/mvn -f igaming-dto/pom.xml install -DskipTests -B", timeout=120)
run_ssh("/usr/local/bin/mvn -f igaming-source-core/pom.xml install -DskipTests -B", timeout=120)

print("\n--- 2. Build & Push igaming-source-bovada with Jib ---", flush=True)
run_ssh(
    "/usr/local/bin/mvn -f igaming-source-bovada/pom.xml compile "
    "com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
    "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
    "-Djib.to.image=100.78.183.101:30500/igaming-source-bovada:latest "
    "-Djib.allowInsecureRegistries=true -DskipTests -B",
    timeout=240
)

print("\n--- 3. Remove cached image from containerd ---", flush=True)
try:
    run_ssh("crictl rmi 100.78.183.101:30500/igaming-source-bovada:latest")
except Exception as e:
    print(f"crictl rmi ignored: {e}")

print("\n--- 4. Restart bovada ---", flush=True)
run_ssh("kubectl rollout restart deployment/igaming-source-bovada -n igaming-source")

print("\n--- 5. Wait for rollout ---", flush=True)
run_ssh("kubectl rollout status deployment/igaming-source-bovada -n igaming-source --timeout=120s")

print("\n--- 6. Check logs ---", flush=True)
time.sleep(5)
run_ssh("kubectl logs -n igaming-source -l app=igaming-source-bovada --tail=40")

print("\n>>> Bovada rebuild & rollout complete!", flush=True)
