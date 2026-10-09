import subprocess

def check():
    cmd = "kubectl get deployments -n igaming-source -o custom-columns='NAME:.metadata.name,IMAGE:.spec.template.spec.containers[*].image'"
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True)
    for line in res.stdout.splitlines():
        if any(bk in line for bk in ['winline', 'fon-bet', 'betcity', 'marathonbet', 'baltbet', 'olimpbet']):
            print(line)

    cmd_dev = "kubectl get deployments -n igaming-dev -o custom-columns='NAME:.metadata.name,IMAGE:.spec.template.spec.containers[*].image'"
    res_dev = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd_dev
    ], capture_output=True, text=True)
    for line in res_dev.stdout.splitlines():
        if 'surebet' in line:
            print(line)

if __name__ == '__main__':
    check()
