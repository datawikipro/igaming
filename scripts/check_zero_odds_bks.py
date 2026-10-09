import subprocess
import json

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

cmd = "curl -s http://10.108.32.50:3042/api/v1/workload/bookmakers/telemetry"
res = subprocess.run([
    'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
    XEON_HOST, cmd
], capture_output=True, timeout=30)

stdout_str = res.stdout.decode('utf-8', errors='replace')

try:
    data = json.loads(stdout_str)
    bks = data.get('bookmakers', [])
    print(f"Total bookmakers in telemetry: {len(bks)}")
    print(f"Total online: {data.get('online_bookmakers')}")
    print(f"Total offline: {data.get('offline_bookmakers')}")
    print("\n--- ONLINE BOOKMAKERS WITH 0 ODDS ---")
    zero_odds_online = []
    has_odds_online = []
    for b in bks:
        is_online = b.get('is_online', False)
        odds = b.get('active_odds_count', 0) or 0
        matches = b.get('active_matches_count', 0) or 0
        if is_online:
            if odds == 0:
                zero_odds_online.append(b)
                print(f"- ID: {b.get('id', 'N/A'):<22} | Name: {b.get('name', 'N/A'):<20} | Matches: {matches:<5} | Odds: {odds:<5} | Delay: {b.get('delay_minutes')}m | Status: {b.get('health_status')}")
            else:
                has_odds_online.append(b)

    print(f"\nSummary:")
    print(f"  Online with ACTIVE ODDS: {len(has_odds_online)}")
    print(f"  Online with 0 ODDS:      {len(zero_odds_online)}")
except Exception as e:
    print("Error parsing JSON:", e)
    print("Raw stdout sample:", stdout_str[:500])
