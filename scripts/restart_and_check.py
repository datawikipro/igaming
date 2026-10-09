import subprocess
import time

def restart_and_check():
    print("=== Restarting winline loader ===")
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', 'kubectl rollout restart deployment/igaming-source-winline-loader -n igaming-source'
    ], capture_output=True, text=True)
    print(res.stdout.strip())
    
    print("\nWaiting 25 seconds for container startup & readiness...")
    time.sleep(25)

    print("\n=== All Target Pods Status ===")
    cmd_pods = """
    echo "--- igaming-dev ---"
    kubectl get pods -n igaming-dev -l app=igaming-aggregator-surebet -o wide
    echo "--- igaming-source ---"
    kubectl get pods -n igaming-source | grep -E 'winline-loader|baltbet-loader|betcity-loader|marathonbet-loader|olimpbet-loader|fon-bet-ru-loader'
    """
    res_pods = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd_pods
    ], capture_output=True, text=True)
    print(res_pods.stdout)

if __name__ == '__main__':
    restart_and_check()
