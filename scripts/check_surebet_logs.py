import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

def check_surebet_logs():
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', 
        'kubectl logs deployment/igaming-aggregator-surebet -n igaming-dev --tail=60'
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    print("=== SUREBET LOGS ===")
    print(res.stdout)

if __name__ == '__main__':
    check_surebet_logs()
