import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

def run_ssh(cmd):
    return subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')

sql = """
SELECT external_event_id, sport_name, league_name, team1, team2, updated_at
FROM match_cache 
ORDER BY updated_at DESC LIMIT 10;
"""
r = run_ssh(f'kubectl exec -n igaming-source igaming-source-pinnacle-db-0 -- psql -U postgres -d igaming_pinnacle -c "{sql}"')
print("=== Pinnacle match_cache ===")
print(r.stdout)

# Check crawler logs
r_crawler = run_ssh("kubectl logs --tail=50 -n igaming-source -l app=pinnacle-crawler")
print("=== Pinnacle Crawler logs ===")
print(r_crawler.stdout[-1500:])
