import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

def check():
    cmd = "kubectl get pods -n igaming-source | grep -E 'pinnacle|betb2b|1xbet|melbet|888starz|megapari|fansport'"
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    print("=== PODS ===")
    print(res.stdout)

    cmd_matches = """
    python3 -c "
import psycopg2
conn = psycopg2.connect('postgresql://postgres:postgres@igaming-aggregator-db.igaming-dev.svc.cluster.local:5432/igaming_aggregator')
cur = conn.cursor()
cur.execute('''
    SELECT bookmaker, count(*) 
    FROM bookmaker_event 
    WHERE bookmaker IN ('pinnacle', '1xbet', 'betb2b', 'melbet', '888starz', 'fansport', 'megapari')
    GROUP BY bookmaker;
''')
print('Matches in aggregator for Pinnacle and 1x-clones:')
for r in cur.fetchall():
    print(f'  {r[0]:15s}: {r[1]} matches')
"
    """
    res_m = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd_matches
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    print(res_m.stdout)

if __name__ == '__main__':
    check()
