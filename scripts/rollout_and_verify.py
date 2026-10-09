import subprocess
import sys

def run_cmd(cmd):
    print(f"CMD: {cmd}")
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True, timeout=120)
    print(res.stdout.strip())
    if res.stderr:
        print("STDERR:", res.stderr.strip())
    if res.returncode != 0:
        print(f"FAILED (code {res.returncode})")
        sys.exit(res.returncode)

def main():
    print("=== 1. Pointing deployments to local registry images ===")
    run_cmd("kubectl set image deployment/igaming-source-winline-loader igaming-source-winline-loader=100.78.183.101:30500/igaming-source-winline:latest -n igaming-source")
    run_cmd("kubectl set image deployment/igaming-source-betcity-loader igaming-source-betcity-loader=100.78.183.101:30500/igaming-source-betcity:latest -n igaming-source")
    run_cmd("kubectl set image deployment/igaming-source-marathonbet-loader igaming-source-marathonbet-loader=100.78.183.101:30500/igaming-source-marathonbet:latest -n igaming-source")
    run_cmd("kubectl set image deployment/igaming-source-baltbet-loader igaming-source-baltbet-loader=100.78.183.101:30500/igaming-source-baltbet:latest -n igaming-source")

    print("\n=== 2. Rolling out restart for updated services ===")
    run_cmd("kubectl rollout restart deployment/igaming-aggregator-surebet -n igaming-dev")
    run_cmd("kubectl rollout restart deployment/igaming-source-winline-loader -n igaming-source")
    run_cmd("kubectl rollout restart deployment/igaming-source-fon-bet-ru-loader -n igaming-source")
    run_cmd("kubectl rollout restart deployment/igaming-source-betcity-loader -n igaming-source")
    run_cmd("kubectl rollout restart deployment/igaming-source-marathonbet-loader -n igaming-source")
    run_cmd("kubectl rollout restart deployment/igaming-source-baltbet-loader -n igaming-source")
    run_cmd("kubectl rollout restart deployment/igaming-source-olimpbet-loader -n igaming-source")

    print("\n=== 3. Waiting for rollout completion ===")
    run_cmd("kubectl rollout status deployment/igaming-aggregator-surebet -n igaming-dev --timeout=120s")
    run_cmd("kubectl rollout status deployment/igaming-source-winline-loader -n igaming-source --timeout=120s")
    run_cmd("kubectl rollout status deployment/igaming-source-fon-bet-ru-loader -n igaming-source --timeout=120s")
    run_cmd("kubectl rollout status deployment/igaming-source-betcity-loader -n igaming-source --timeout=120s")
    run_cmd("kubectl rollout status deployment/igaming-source-marathonbet-loader -n igaming-source --timeout=120s")
    run_cmd("kubectl rollout status deployment/igaming-source-baltbet-loader -n igaming-source --timeout=120s")
    run_cmd("kubectl rollout status deployment/igaming-source-olimpbet-loader -n igaming-source --timeout=120s")

    print("\n=== ROLLOUT COMPLETED SUCCESSFULLY ===")

if __name__ == '__main__':
    main()
