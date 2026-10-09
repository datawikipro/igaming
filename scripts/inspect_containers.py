import subprocess

def inspect_containers():
    deployments = [
        ("igaming-dev", "igaming-aggregator-surebet"),
        ("igaming-source", "igaming-source-winline-loader"),
        ("igaming-source", "igaming-source-fon-bet-ru-loader"),
        ("igaming-source", "igaming-source-betcity-loader"),
        ("igaming-source", "igaming-source-marathonbet-loader"),
        ("igaming-source", "igaming-source-baltbet-loader"),
        ("igaming-source", "igaming-source-olimpbet-loader")
    ]
    for ns, dep in deployments:
        cmd = f"kubectl get deployment {dep} -n {ns} -o jsonpath='{{.spec.template.spec.containers[*].name}}'"
        res = subprocess.run([
            'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
            '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
            'root@100.78.183.101', cmd
        ], capture_output=True, text=True)
        print(f"{ns}/{dep} containers: {res.stdout}")

if __name__ == '__main__':
    inspect_containers()
