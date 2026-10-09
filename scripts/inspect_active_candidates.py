import subprocess

dbs_1_1 = [
    'betano', 'ligastavok', 'leon', 'sbobet', 'pinnacle', 'bet365', 'draftkings',
    '1xbit', 'melbet-com', 'marathonbet-com', 'marathonbet-by', 'fon-bet-by',
    'fon-bet-kz', 'olimpbet-kz', 'betwinner', 'bwin', 'mostbet', 'betway',
    'parimatch', 'stake', 'vbet', 'caliente', 'bet7k', 'betesporte', 'betnacional'
]

print(f"{'BOOKMAKER':<16} | {'CACHE':<7} | {'POD STATUS':<12} | {'LAST ERROR / WARNING'}")
print("-" * 90)

for bk in dbs_1_1:
    dbname = f"igaming_{bk.replace('-', '_')}"
    cmd_mc = ['kubectl', 'exec', '-n', 'igaming-source', f'igaming-source-{bk}-db-0', '--', 'psql', '-U', 'postgres', '-d', dbname, '-t', '-c', 'SELECT count(*) FROM match_cache;']
    res_mc = subprocess.run(cmd_mc, capture_output=True, text=True, timeout=8)
    if res_mc.returncode == 0:
        mc = res_mc.stdout.strip()
    else:
        mc = "ERR_DB"
        
    res_p = subprocess.run(['kubectl', 'get', 'pods', '-n', 'igaming-source', '--no-headers'], capture_output=True, text=True)
    pstat = "NO_POD"
    pname = ""
    for line in res_p.stdout.splitlines():
        parts = line.split()
        if len(parts) >= 3:
            name = parts[0]
            stat = parts[2]
            if f"-{bk}-crawler" in name:
                pname = name
                pstat = stat
                break
            elif f"-{bk}-" in name and "-db" not in name and "-loader" not in name:
                pname = name
                pstat = stat
                
    err = ""
    if pname and "Running" in pstat:
        res_l = subprocess.run(['kubectl', 'logs', '-n', 'igaming-source', pname, '--tail=30'], capture_output=True, text=True)
        lines = (res_l.stdout + "\n" + res_l.stderr).splitlines()
        for l in reversed(lines):
            low = l.lower()
            if any(k in low for k in ['error', 'warn', '403', '401', '404', 'qrator', 'cloudflare', 'timeout', 'exception', 'blocked']):
                err = l.strip()[:80]
                break
                
    print(f"{bk:<16} | {mc:<7} | {pstat:<12} | {err}")
