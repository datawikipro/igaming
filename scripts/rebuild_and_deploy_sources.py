import subprocess
import sys

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
    print("STDOUT:", res.stdout[-2500:] if len(res.stdout) > 2500 else res.stdout)
    if res.stderr:
        print("STDERR:", res.stderr[-1000:] if len(res.stderr) > 1000 else res.stderr)
    if res.returncode != 0:
        print(f"FAILED with exit code {res.returncode}")
        sys.exit(res.returncode)
    return res

if __name__ == '__main__':
    print("Uploading igaming-source-leon/pom.xml...")
    subprocess.run([
        'scp', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'igaming-source-leon/pom.xml', f"{XEON_HOST}:/data/igaming/igaming-source-leon/pom.xml"
    ], check=True)

    jib_cmd = (
        "/usr/local/bin/mvn -f igaming-source-leon/pom.xml compile "
        "com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
        "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
        "-Djib.to.image=100.78.183.101:30500/igaming-source-leon:latest "
        "-Djib.allowInsecureRegistries=true -DskipTests -B"
    )
    run_ssh(jib_cmd, timeout=300)
    run_ssh("kubectl rollout restart deployment/igaming-source-leon-loader -n igaming-source", timeout=60)
    print("\n=== LEON BUILT AND ROLLED OUT SUCCESSFULLY ===")
