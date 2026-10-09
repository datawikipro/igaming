import subprocess
import sys
import tarfile
import os

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(cmd, timeout=300):
    print(f"\n>>> SSH: {cmd}")
    res = subprocess.run([
        'ssh', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=timeout)
    print("STDOUT:", res.stdout[-2000:] if len(res.stdout) > 2000 else res.stdout)
    if res.stderr:
        print("STDERR:", res.stderr[-1000:] if len(res.stderr) > 1000 else res.stderr)
    if res.returncode != 0:
        print(f"FAILED with exit code {res.returncode}")
        sys.exit(res.returncode)
    return res

def create_archive():
    archive_name = "batch_global_oop.tar.gz"
    print(f"Creating {archive_name}...")
    with tarfile.open(archive_name, "w:gz") as tar:
        for path in [
            "igaming-source-core/src",
            "igaming-source-pinnacle/src",
            "igaming-source-betb2b/src",
            "igaming-source-sbobet/src"
        ]:
            if os.path.exists(path):
                print(f"  Adding {path}")
                tar.add(path)
    print("Archive created.")
    return archive_name

def upload_and_extract(archive_name):
    print("Uploading archive to Xeon...")
    res = subprocess.run([
        'scp', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        archive_name, f"{XEON_HOST}:/data/igaming/{archive_name}"
    ], capture_output=True, text=True)
    if res.returncode != 0:
        print("SCP failed:", res.stderr)
        sys.exit(res.returncode)
    print("Extracting on Xeon...")
    run_ssh(f"cd /data/igaming && tar -xzf {archive_name} && rm -f {archive_name}")

def build_modules():
    print("\n--- 1. Build and install igaming-source-core ---")
    run_ssh("/usr/local/bin/mvn -f igaming-source-core/pom.xml install -DskipTests -B", timeout=180)

    print("\n--- 2. Build & Test & Push igaming-source-pinnacle ---")
    run_ssh("/usr/local/bin/mvn -f igaming-source-pinnacle/pom.xml test "
            "com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
            "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
            "-Djib.to.image=100.78.183.101:30500/igaming-source-pinnacle:latest "
            "-Djib.allowInsecureRegistries=true -B", timeout=300)

    print("\n--- 3. Build & Test & Push igaming-source-betb2b ---")
    run_ssh("/usr/local/bin/mvn -f igaming-source-betb2b/pom.xml test "
            "com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
            "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
            "-Djib.to.image=100.78.183.101:30500/igaming-source-betb2b:latest "
            "-Djib.allowInsecureRegistries=true -B", timeout=300)

    print("\n--- 4. Build & Test & Push igaming-source-sbobet ---")
    run_ssh("/usr/local/bin/mvn -f igaming-source-sbobet/pom.xml test "
            "com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
            "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
            "-Djib.to.image=100.78.183.101:30500/igaming-source-sbobet:latest "
            "-Djib.allowInsecureRegistries=true -B", timeout=300)

def rollout_and_check():
    print("\n--- 5. Trigger Rollout Restarts ---")
    deployments = [
        "igaming-source-pinnacle-loader",
        "igaming-source-1xbet-loader",
        "igaming-source-melbet-loader",
        "igaming-source-888starz-loader",
        "igaming-source-fansport-loader",
        "igaming-source-megapari-loader",
        "igaming-source-sbobet-loader"
    ]
    for d in deployments:
        run_ssh(f"kubectl rollout restart deployment/{d} -n igaming-source")

    print("\n--- 6. Wait for Rollout Status ---")
    for d in deployments:
        run_ssh(f"kubectl rollout status deployment/{d} -n igaming-source --timeout=90s")

if __name__ == '__main__':
    archive = create_archive()
    upload_and_extract(archive)
    build_modules()
    rollout_and_check()
    print("\n=== ALL MODULES SUCCESSFULLY BUILT AND DEPLOYED! ===")
