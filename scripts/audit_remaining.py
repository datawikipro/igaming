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
    print("=== 1. Normalization requests by Type and Status ===")
    print(query_pg("SELECT type, status, count(*) FROM normalization_request GROUP BY type, status ORDER BY count(*) DESC;"))

    print("\n=== 2. Top Bookmakers with Uncompleted ODDS_TYPE ===")
    print(query_pg("SELECT source_id, count(*) as cnt FROM normalization_request WHERE type = 'ODDS_TYPE' AND status != 'COMPLETED' GROUP BY source_id ORDER BY cnt DESC LIMIT 15;"))

    print("\n=== 3. Top Unmapped Odds Types (raw_name, sport, bookmaker) ===")
    print(query_pg("SELECT raw_name, sport_id, source_id, count(*) as cnt FROM normalization_request WHERE type = 'ODDS_TYPE' AND status != 'COMPLETED' GROUP BY raw_name, sport_id, source_id ORDER BY cnt DESC LIMIT 25;"))

if __name__ == '__main__':
    main()
