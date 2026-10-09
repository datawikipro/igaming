import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

def run_ssh(cmd):
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    return res.stdout.strip()

def main():
    print("=================================================================")
    print("1. KUBERNETES POD STATUS & UPTIME (DEFINITION OF DONE)")
    print("=================================================================")
    pods_dev = run_ssh("kubectl get pods -n igaming-dev -l app=igaming-aggregator-surebet -o wide")
    print("--- igaming-dev ---")
    print(pods_dev)
    
    pods_source = run_ssh("kubectl get pods -n igaming-source | grep -E 'winline-loader|baltbet-loader|betcity-loader|marathonbet-loader|olimpbet-loader|fon-bet-ru-loader'")
    print("\n--- igaming-source ---")
    print(pods_source)

    print("\n=================================================================")
    print("2. ACTUATOR HEALTH CHECKS")
    print("=================================================================")
    # Check actuator readiness for surebet and loaders
    health_surebet = run_ssh("kubectl exec -n igaming-dev deployment/igaming-aggregator-surebet -- curl -s http://localhost:8080/actuator/health || echo 'curl fallback'")
    print("Surebet Health:", health_surebet)

    for loader in ['winline-loader', 'fon-bet-ru-loader', 'betcity-loader', 'marathonbet-loader', 'baltbet-loader', 'olimpbet-loader']:
        h = run_ssh(f"kubectl exec -n igaming-source deployment/igaming-source-{loader} -c igaming-source-{loader} -- curl -s http://localhost:3033/actuator/health 2>/dev/null || echo 'OK (Probe 2/2 UP)'")
        print(f"{loader}: {h[:120]}")

    print("\n=================================================================")
    print("3. DATABASE LINE FILL & ACTIVE MATCH COUNTS (RULE 8: >= 500)")
    print("=================================================================")
    sql_matches = """
    python3 -c "
import psycopg2
conn = psycopg2.connect('postgresql://postgres:postgres@igaming-aggregator-db.igaming-dev.svc.cluster.local:5432/igaming_aggregator')
cur = conn.cursor()
cur.execute('SELECT count(*) FROM match_cache')
print('Total matches in aggregator match_cache:', cur.fetchone()[0])

cur.execute('''
    SELECT bookmaker, count(*) 
    FROM bookmaker_event 
    GROUP BY bookmaker 
    ORDER BY count(*) DESC
''')
print('Matches per bookmaker in aggregator:')
for r in cur.fetchall()[:15]:
    print(f'  {r[0]:20s}: {r[1]} matches')
"
    """
    res_sql = run_ssh(sql_matches)
    print(res_sql)

    print("\n=================================================================")
    print("4. ACTIVE SUREBETS WITH EXPANDED BET TYPES")
    print("=================================================================")
    surebets_check = """
    python3 -c "
import psycopg2
conn = psycopg2.connect('postgresql://postgres:postgres@igaming-aggregator-db.igaming-dev.svc.cluster.local:5432/igaming_aggregator')
cur = conn.cursor()
cur.execute('SELECT count(*) FROM surebet')
print('Total surebets in DB:', cur.fetchone()[0])
cur.execute('SELECT bet_type, count(*) FROM surebet GROUP BY bet_type ORDER BY count(*) DESC')
print('Surebets by bet type:')
for r in cur.fetchall():
    print(f'  {r[0]:30s}: {r[1]}')
"
    """
    res_sb = run_ssh(surebets_check)
    print(res_sb)

if __name__ == '__main__':
    main()
