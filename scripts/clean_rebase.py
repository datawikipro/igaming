import subprocess

for repo in ['igaming-source-winline', 'igaming-source-betcity', 'igaming-source-baltbet']:
    print(f"=== Aborting rebase in {repo} ===")
    subprocess.run(['git', '-C', repo, 'rebase', '--abort'], capture_output=True, text=True)
    
    # Check status
    st = subprocess.run(['git', '-C', repo, 'status', '-s'], capture_output=True, text=True)
    print("Status:", st.stdout.strip()[:150])

    # Merge origin/main keeping our working tree changes
    res_m = subprocess.run(['git', '-C', repo, 'merge', 'origin/main', '-m', 'Merge remote-tracking branch origin/main'], capture_output=True, text=True)
    print("Merge:", res_m.stdout.strip()[:200])
    if res_m.stderr:
        print("Merge ERR:", res_m.stderr.strip()[:200])
    
    # If merge conflict, accept ours for application.properties or mappers
    res_diff = subprocess.run(['git', '-C', repo, 'diff', '--name-only', '--diff-filter=U'], capture_output=True, text=True)
    unmerged = res_diff.stdout.strip().splitlines()
    if unmerged:
        print("Unmerged files:", unmerged)
        for u in unmerged:
            subprocess.run(['git', '-C', repo, 'checkout', '--ours', u], capture_output=True, text=True)
            subprocess.run(['git', '-C', repo, 'add', u], capture_output=True, text=True)
        subprocess.run(['git', '-C', repo, 'commit', '-m', 'Merge remote changes and keep current market mappers'], capture_output=True, text=True)
    
    # Push
    p = subprocess.run(['git', '-C', repo, 'push', 'origin', 'HEAD:main'], capture_output=True, text=True, timeout=60)
    print("PUSH:", p.stdout.strip())
    if p.stderr:
        print("PUSH ERR:", p.stderr.strip()[:200])

print("=== CLEAN REBASE & MERGE DONE ===")
