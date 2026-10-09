import subprocess

def inspect():
    print("=== WINLINE LOADER LOGS ===")
    cmd_winline = "kubectl logs deployment/igaming-source-winline-loader -c igaming-source-winline-loader -n igaming-source --tail=60"
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd_winline
    ], capture_output=True, text=True)
    print(res.stdout)
    if res.stderr:
        print("STDERR:", res.stderr)

    print("=== POD STATUS NOW ===")
    cmd_pods = "kubectl get pods -n igaming-source | grep -E 'winline-loader|baltbet-loader|betcity-loader|marathonbet-loader|olimpbet-loader|fon-bet-ru-loader'"
    res_pods = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd_pods
    ], capture_output=True, text=True)
    print(res_pods.stdout)

if __name__ == '__main__':
    inspect()
