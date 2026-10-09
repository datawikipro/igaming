import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

def query(pod, db, sql):
    cmd = f'kubectl exec -i -n igaming-source {pod} -- psql -U postgres -d {db} -t -c "{sql}" 2>/dev/null'
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')
    return res.stdout.strip()

def main():
    clones = [
        ("igaming-source-1xbet-db-0", "igaming_xbet"),
        ("igaming-source-melbet-db-0", "igaming_melbet"),
        ("igaming-source-888starz-db-0", "igaming_888starz"),
        ("igaming-source-fansport-db-0", "igaming_fansport"),
        ("igaming-source-megapari-db-0", "igaming_megapari")
    ]
    for pod, db in clones:
        m = query(pod, db, "SELECT count(*) FROM match_cache;")
        print(f"{pod:35s}: {m} matches")

if __name__ == '__main__':
    main()
