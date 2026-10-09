import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

def query_pg(sql):
    cmd = f'kubectl exec -i -n igaming-dev igaming-aggregator-db-0 -- psql -U postgres -d igaming_aggregator -c "{sql}"'
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    return res.stdout.strip()

def main():
    print("=== 1. Total Matches in match_record ===")
    print(query_pg("SELECT count(*) as total_matches FROM match_record;"))

    print("\n=== 2. Total Alerts in surebet_alert ===")
    print(query_pg("SELECT count(*) as total_surebets FROM surebet_alert;"))

    print("\n=== 3. Surebet Alert Statuses ===")
    print(query_pg("SELECT status, count(*) FROM surebet_alert GROUP BY status;"))

    print("\n=== 4. Total Odds in odds_actual ===")
    print(query_pg("SELECT count(*) as total_odds FROM odds_actual;"))

    print("\n=== 5. Valuebets & Middles ===")
    print(query_pg("SELECT count(*) as total_valuebets FROM valuebet_alert;"))
    print(query_pg("SELECT count(*) as total_middles FROM middle_alert;"))

if __name__ == '__main__':
    main()
