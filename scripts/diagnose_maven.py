import subprocess

def run():
    cmd = "ls -ld /root/igaming /data/igaming"
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True)
    print("STDOUT:\n", res.stdout)
    print("STDERR:\n", res.stderr)

if __name__ == '__main__':
    run()
