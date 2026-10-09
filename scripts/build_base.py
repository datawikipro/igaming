import subprocess
import sys

def run_ssh(remote_cmd, timeout=300):
    print(f"=== RUNNING ON XEON: {remote_cmd[:120]}... ===")
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', remote_cmd
    ], capture_output=True, text=True, timeout=timeout)
    print("STDOUT:\n", res.stdout[-2500:] if len(res.stdout) > 2500 else res.stdout)
    if res.stderr:
        print("STDERR:\n", res.stderr[-1500:] if len(res.stderr) > 1500 else res.stderr)
    print("EXIT CODE:", res.returncode)
    if res.returncode != 0:
        sys.exit(res.returncode)
    return res.stdout

if __name__ == "__main__":
    # 1. Build and install igaming-dto
    run_ssh("/usr/local/bin/mvn -f /data/igaming/igaming-dto/pom.xml clean install -DskipTests -B", timeout=300)
    
    # 2. Build and install igaming-source-core
    run_ssh("/usr/local/bin/mvn -f /data/igaming/igaming-source-core/pom.xml clean install -DskipTests -B", timeout=300)
    
    print("=== DTO AND SOURCE-CORE COMPILED AND INSTALLED SUCCESSFULLY ===")
