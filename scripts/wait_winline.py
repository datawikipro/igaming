import subprocess
import time

def wait_and_check():
    print("Waiting 20 seconds...")
    time.sleep(20)
    cmd = """
    kubectl get pods -n igaming-source -l app=igaming-source-winline-loader
    """
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True)
    print(res.stdout)

if __name__ == '__main__':
    wait_and_check()
