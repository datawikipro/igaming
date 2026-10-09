import subprocess

def check():
    for ns in ['igaming-dev', 'igaming-source']:
        print(f"=== PODS IN {ns} ===")
        cmd = f"kubectl get pods -n {ns} -o wide | grep -E 'surebet|winline-loader|fon-bet-ru-loader|betcity-loader|marathonbet-loader|baltbet-loader|olimpbet-loader'"
        res = subprocess.run([
            'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
            '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
            'root@100.78.183.101', cmd
        ], capture_output=True, text=True)
        print(res.stdout)

if __name__ == '__main__':
    check()
