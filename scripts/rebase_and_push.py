import subprocess

repos = [
    'igaming-source-winline',
    'igaming-source-betcity',
    'igaming-source-marathonbet',
    'igaming-source-baltbet',
    'igaming-source-olimpbet'
]

for repo in repos:
    print(f"=== Pulling and pushing {repo} ===")
    p_pull = subprocess.run(['git', '-C', repo, 'pull', '--rebase', 'origin', 'main'], capture_output=True, text=True, timeout=60)
    print("PULL:", p_pull.stdout.strip())
    if p_pull.stderr:
        print("PULL ERR:", p_pull.stderr.strip()[:200])
    p_push = subprocess.run(['git', '-C', repo, 'push', 'origin', 'HEAD:main'], capture_output=True, text=True, timeout=60)
    print("PUSH:", p_push.stdout.strip())
    if p_push.stderr:
        print("PUSH ERR:", p_push.stderr.strip()[:200])

print("=== REBASE AND PUSH DONE ===")
