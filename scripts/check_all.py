import subprocess

def run():
    print("=== CHECKING ALL PODS ===")
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', 
        'echo "--- DEV ---" && kubectl get pods -n igaming-dev -l app=igaming-aggregator-surebet && '
        'echo "--- SOURCE ---" && kubectl get pods -n igaming-source | grep -E "winline-loader|baltbet-loader|betcity-loader|marathonbet-loader|olimpbet-loader|fon-bet-ru-loader"'
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    print(res.stdout)
    
    print("=== WINLINE LOGS ===")
    res_w = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101',
        'kubectl logs deployment/igaming-source-winline-loader -c igaming-source-winline-loader -n igaming-source --tail=15'
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    print(res_w.stdout)

if __name__ == '__main__':
    run()
