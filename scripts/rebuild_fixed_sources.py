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

archive_name = "fixed_sources_rebuild.tar.gz"
targets = [
    'igaming-dto',
    'igaming-source-core',
    'igaming-source-winline',
    'igaming-source-baltbet',
    'igaming-source-marathonbet'
]

def tar_filter(tarinfo):
    name = tarinfo.name.replace('\\', '/')
    for skip in ['/target/', '/.git/', '/.idea/', '/node_modules/']:
        if skip in name or name.endswith('/target') or name.endswith('/.git'):
            return None
    return tarinfo

print("Packaging targets (excluding target/ and build artifacts)...", flush=True)
with tarfile.open(archive_name, "w:gz") as tar:
    for m in targets:
        if os.path.exists(m):
            tar.add(m, filter=tar_filter)

print("Uploading to Xeon...", flush=True)
subprocess.run(['scp', '-i', SSH_KEY, '-o', 'BatchMode=yes', archive_name, f"{XEON_HOST}:/data/igaming/{archive_name}"], check=True)
run_ssh(f"tar -xzf {archive_name} && rm -f {archive_name}")
if os.path.exists(archive_name):
    os.remove(archive_name)

print("\n--- 1. Rebuild and install igaming-dto and igaming-source-core ---", flush=True)
run_ssh("/usr/local/bin/mvn -f igaming-dto/pom.xml install -DskipTests -B", timeout=120)
run_ssh("/usr/local/bin/mvn -f igaming-source-core/pom.xml install -DskipTests -B", timeout=120)

build_targets = [
    ('igaming-source-winline', 'igaming-source-winline', '100.78.183.101:30500/playwright-java:21', ['igaming-source-winline-crawler', 'igaming-source-winline-loader']),
    ('igaming-source-baltbet', 'igaming-source-baltbet', '100.78.183.101:30500/eclipse-temurin:21-jre', ['igaming-source-baltbet-crawler', 'igaming-source-baltbet-loader']),
    ('igaming-source-marathonbet', 'igaming-source-marathonbet', '100.78.183.101:30500/eclipse-temurin:21-jre', ['igaming-source-marathonbet-crawler', 'igaming-source-marathonbet-loader'])
]

for module, img_name, base_img, dep_names in build_targets:
    print(f"\n==========================================")
    print(f"Building {module} with base {base_img}")
    print(f"==========================================")
    run_ssh(
        f"/usr/local/bin/mvn -f {module}/pom.xml compile "
        f"com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
        f"-Djib.from.image={base_img} "
        f"-Djib.to.image=100.78.183.101:30500/{img_name}:latest "
        f"-Djib.allowInsecureRegistries=true -DskipTests -B",
        timeout=300
    )
    
    # Remove cached containerd image to force fresh pull
    try:
        run_ssh(f"crictl rmi 100.78.183.101:30500/{img_name}:latest")
    except Exception as e:
        print(f"Warning removing crictl image: {e}")

    for dep in dep_names:
        print(f"Restarting deployment {dep} in igaming-source...")
        run_ssh(f"kubectl rollout restart deployment/{dep} -n igaming-source", timeout=60)

print("\n=== ALL FIXED SOURCES BUILT AND ROLLED OUT SUCCESSFULLY ===")
