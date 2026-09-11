import subprocess
from concurrent.futures import ThreadPoolExecutor

bks = [
    'leon', 'betcity', 'pari', 'fon-bet-ru', 'winline', 'baltbet', 'tennisi',
    'betboom', 'olimpbet', 'pinnacle', 'zenit', 'marathonbet', 'sbobet',
    '1xbet', '22bet', '888starz', 'betandyou', 'fansport', 'spinbetter', '1xbit'
]

def check(bk):
    cmd = ["kubectl", "exec", "-n", "igaming-source", f"igaming-source-{bk}-db-0", "--", "psql", "-U", "postgres", "-tAc", "SELECT count(*), count(*) FILTER (WHERE is_live=true) FROM match_cache;"]
    try:
        p = subprocess.run(cmd, capture_output=True, text=True, timeout=8)
        if p.returncode == 0:
            out = p.stdout.strip()
            total, live = out.split('|') if '|' in out else (out, '0')
            return bk, total, live, "OK"
        return bk, "0", "0", "ERR: " + p.stderr.strip()[:20]
    except Exception as e:
        return bk, "0", "0", "TIMEOUT"

print(f"{'BOOKMAKER':<16} | {'MATCH_CACHE':<14} | {'LIVE':<8} | {'STATUS'}", flush=True)
print("-" * 52, flush=True)

with ThreadPoolExecutor(max_workers=10) as executor:
    results = executor.map(check, bks)
    for bk, total, live, status in results:
        print(f"{bk:<16} | {total:<14} | {live:<8} | {status}", flush=True)
