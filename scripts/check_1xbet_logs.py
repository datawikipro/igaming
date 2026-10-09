import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

def check():
    print("=== 1XBET CRAWLER LOGS ===")
    res_c = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', 'kubectl logs deployment/igaming-source-1xbet-crawler -c igaming-source-1xbet-crawler -n igaming-source --tail=30'
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    print(res_c.stdout)

    print("\n=== 1XBET LOADER LOGS ===")
    res_l = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', 'kubectl logs deployment/igaming-source-1xbet-loader -c igaming-source-1xbet-loader -n igaming-source --tail=30'
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    print(res_l.stdout)

if __name__ == '__main__':
    check()
