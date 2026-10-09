#!/usr/bin/env python3
import subprocess
import re

out = subprocess.check_output(['kubectl', 'get', 'statefulset', '-n', 'igaming-source', '-o', 'custom-columns=NAME:.metadata.name'], text=True)
sts_list = [line.strip() for line in out.strip().split('\n')[1:] if line.strip()]

print(f'Total StatefulSets found: {len(sts_list)}')

under_500 = []
zeros = []
errors = []

for sts in sts_list:
    m = re.match(r'igaming-source-(.+)-db', sts)
    if not m:
        continue
    name = m.group(1)
    
    clean_name = name.replace('-', '_')
    db_candidates = [f'igaming_{clean_name}', f'igaming_{name}', clean_name, name, 'postgres']
    found = False
    for db in db_candidates:
        cmd = ['kubectl', 'exec', '-n', 'igaming-source', f'{sts}-0', '--', 'psql', '-U', 'postgres', '-d', db, '-tAc', 'SELECT count(*) FROM match_cache;']
        res = subprocess.run(cmd, capture_output=True, text=True)
        if res.returncode == 0:
            val_str = res.stdout.strip()
            if val_str.isdigit():
                cnt = int(val_str)
                found = True
                if cnt == 0:
                    zeros.append((name, sts, db, 0))
                elif cnt < 500:
                    under_500.append((name, sts, db, cnt))
                break
    if not found:
        errors.append((name, sts))

print('\n=== ZERO MATCHES (0) ===')
for name, sts, db, cnt in zeros:
    print(f'  {name} ({sts}, db={db}): {cnt} matches')

print('\n=== UNDER 500 MATCHES (1-499) ===')
for name, sts, db, cnt in under_500:
    print(f'  {name} ({sts}, db={db}): {cnt} matches')

print('\n=== ERRORS / NO MATCH_CACHE ===')
for name, sts in errors:
    print(f'  {name} ({sts})')
