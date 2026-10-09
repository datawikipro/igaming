import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

res_sts = subprocess.run(['kubectl', 'get', 'statefulsets', '-n', 'igaming-source', '-o', 'jsonpath={.items[*].metadata.name}'], capture_output=True, text=True)
db_names = res_sts.stdout.split()

print(f"Checking {len(db_names)} bookmaker databases for cached matches...", flush=True)
from concurrent.futures import ThreadPoolExecutor

def check_one(db_svc):
    raw = db_svc
    if raw.startswith('igaming-source-'):
        raw = raw[len('igaming-source-'):]
    if raw.endswith('-db'):
        raw = raw[:-len('-db')]
    
    dbname = f"igaming_{raw.replace('-', '_')}"
    cmd = ['kubectl', 'exec', '-n', 'igaming-source', f'{db_svc}-0', '--', 'psql', '-U', 'postgres', '-d', dbname, '-t', '-c', 'SELECT count(*) FROM match_cache;']
    try:
        res = subprocess.run(cmd, capture_output=True, text=True, timeout=3)
        if res.returncode == 0:
            val = res.stdout.strip()
            if val.isdigit():
                cnt = int(val)
                if cnt > 0:
                    return (raw, cnt)
    except Exception:
        pass
    return None

with ThreadPoolExecutor(max_workers=16) as ex:
    results = [r for r in ex.map(check_one, db_names) if r is not None]

print(f"\nBookmakers with matches in local DB cache ({len(results)}):", flush=True)
for bk, cnt in sorted(results, key=lambda x: -x[1]):
    print(f"  {bk:<20}: {cnt} matches")
