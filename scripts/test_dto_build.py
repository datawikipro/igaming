import subprocess

def run():
    cmd = "/usr/local/bin/mvn -f igaming-dto/pom.xml clean install -DskipTests -B"
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True, timeout=120)
    print("STDOUT:\n", res.stdout[-1500:] if len(res.stdout) > 1500 else res.stdout)
    print("STDERR:\n", res.stderr[-1000:] if len(res.stderr) > 1000 else res.stderr)
    print("RC:", res.returncode)

if __name__ == '__main__':
    run()
