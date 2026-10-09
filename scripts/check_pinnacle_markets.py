import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

def run_ssh(cmd):
    return subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')

print("=== Pinnacle DB Query ===")
r = run_ssh("kubectl exec -n igaming-source igaming-source-pinnacle-db-0 -- psql -U postgres -d igaming_pinnacle -c 'SELECT count(*) FROM match_cache; SELECT count(*) FROM match_factor; SELECT factor_id, name, count(*) FROM match_factor GROUP BY factor_id, name ORDER BY count(*) DESC LIMIT 30;'")
print(r.stdout)
if r.stderr:
    print("Stderr:", r.stderr)

print("=== Pinnacle Loader Logs (last 30 lines) ===")
r2 = run_ssh("kubectl logs --tail=30 -n igaming-source -l app=pinnacle-loader")
print(r2.stdout)
if not r2.stdout.strip():
    # Try finding exact pod name
    r3 = run_ssh("kubectl logs --tail=30 -n igaming-source igaming-source-pinnacle-loader-58c85fc7bd-rnhxq")
    print(r3.stdout)
