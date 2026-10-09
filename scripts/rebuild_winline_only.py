import subprocess
import sys
import tarfile
import os

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

archive_name = "winline_rebuild.tar.gz"
targets = ['igaming-source-winline']

def tar_filter(tarinfo):
    name = tarinfo.name.replace('\\', '/')
    for skip in ['/target/', '/.git/', '/.idea/', '/node_modules/']:
        if skip in name or name.endswith('/target') or name.endswith('/.git'):
            return None
    return tarinfo

print("Packaging winline...", flush=True)
with tarfile.open(archive_name, "w:gz") as tar:
    for m in targets:
        if os.path.exists(m):
            tar.add(m, filter=tar_filter)

print("Uploading to Xeon...", flush=True)
subprocess.run(['scp', '-i', SSH_KEY, '-o', 'BatchMode=yes', archive_name, f"{XEON_HOST}:/data/igaming/{archive_name}"], check=True)
run_ssh(f"tar -xzf {archive_name} && rm -f {archive_name}")
if os.path.exists(archive_name):
    os.remove(archive_name)

print("\nBuilding and pushing winline image...", flush=True)
run_ssh(
    f"/usr/local/bin/mvn -f igaming-source-winline/pom.xml compile "
    f"com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
    f"-Djib.from.image=100.78.183.101:30500/playwright-java:21 "
    f"-Djib.to.image=100.78.183.101:30500/igaming-source-winline:latest "
    f"-Djib.allowInsecureRegistries=true -DskipTests -B",
    timeout=300
)

try:
    run_ssh("crictl rmi 100.78.183.101:30500/igaming-source-winline:latest")
except Exception as e:
    print(f"Warning: {e}")

run_ssh("kubectl rollout restart deployment/igaming-source-winline-crawler deployment/igaming-source-winline-loader -n igaming-source", timeout=60)
print("\n=== WINLINE REBUILT AND RESTARTED SUCCESSFULLY ===")
