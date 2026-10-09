import subprocess
import time

def verify():
    cmd = """
    echo "=== igaming-dev pods ==="
    kubectl get pods -n igaming-dev -l app=igaming-aggregator-surebet
    echo "=== igaming-source pods ==="
    kubectl get pods -n igaming-source | grep -E 'winline-loader|baltbet-loader|betcity-loader|marathonbet-loader|olimpbet-loader|fon-bet-ru-loader'
    """
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True)
    print(res.stdout)

if __name__ == '__main__':
    verify()
