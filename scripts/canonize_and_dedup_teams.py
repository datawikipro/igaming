import re
import subprocess
import os
import sys

NAMESPACE = 'igaming-dev'
POD = 'igaming-aggregator-db-0'

def run_psql(sql_command):
    cmd = [
        'kubectl', 'exec', '-i', '-n', NAMESPACE, POD, '--',
        'env', 'PGCLIENTENCODING=UTF8', 'psql', '-U', 'postgres', '-d', 'igaming_aggregator',
        '-t', '-A', '-F', '|||', '-c', sql_command
    ]
    res = subprocess.run(cmd, capture_output=True)
    if res.returncode != 0:
        print(f"Error running PSQL: {res.stderr.decode('utf-8', errors='replace')}")
    return res.stdout.decode('utf-8', errors='replace')

def execute_sql_file(file_path):
    print(f"Executing {file_path} in {POD}...")
    cmd = [
        'kubectl', 'exec', '-i', '-n', NAMESPACE, POD, '--',
        'env', 'PGCLIENTENCODING=UTF8', 'psql', '-U', 'postgres', '-d', 'igaming_aggregator'
    ]
    with open(file_path, 'rb') as f:
        res = subprocess.run(cmd, stdin=f, capture_output=True)
    if res.returncode != 0:
        print(f"Execution failed: {res.stderr.decode('utf-8', errors='replace')}")
        return False
    print(f"Execution finished successfully: {res.stdout.decode('utf-8', errors='replace')[:200]}")
    return True

LA_PATTERN = re.compile(r'(?ui)^Л-А\s+')
NY_PATTERN = re.compile(r'(?ui)^Н-Й\s+')
NO_PATTERN = re.compile(r'(?ui)^Н-О\s+')
SP_PATTERN = re.compile(r'(?ui)^С-П\s+')
AGE_WINLINE = re.compile(r'(?ui)\s*\(до\s*(\d{2})\)')
AGE_BETCITY = re.compile(r'(?ui)\s*\((\d{2})\)$')
PREFIX_NOISE = re.compile(r'(?ui)^(?:ФК|FC|БК|BC|ХК|HC|ПФК|PFC|МФК|MFC|ЖФК|WFC|СК|SC|КСМ|CSM|СВ|SV|ТСВ|TSV|БВБ|BVB|КФ|CF|ИРП|IRP|КД|CD|УД|UD|СД|SD|ВФБ|VFB|ФСВ|FSV|РБ|RB|Клуб|Club)\s+')
SUFFIX_NOISE = re.compile(r'(?ui)\s+(?:ФК|FC|БК|BC|ХК|HC|ПФК|PFC|МФК|MFC|ЖФК|WFC|СК|SC)$')
STAT_PREFIX = re.compile(r'(?ui)^(?:УГЛ|ЖК|ФОЛЫ|УДАРЫ|ОФСАЙДЫ|АУТЫ)\s+')

def canonicalize_team_name(name, sport):
    if not name:
        return ''
    s = name.strip()
    s = LA_PATTERN.sub('Лос-Анджелес ', s)
    s = NY_PATTERN.sub('Нью-Йорк ', s)
    s = NO_PATTERN.sub('Нью-Орлеан ', s)
    s = SP_PATTERN.sub('Санкт-Петербург ', s)
    s = STAT_PREFIX.sub('', s)
    s = AGE_WINLINE.sub(r' U\1', s)
    s = AGE_BETCITY.sub(r' U\1', s)
    s = re.sub(r'(?ui)\s+II$', ' 2', s)
    s = re.sub(r'(?ui)\s+III$', ' 3', s)
    s = re.sub(r'(?ui)\s+B$', ' 2', s)
    s = re.sub(r'(?ui)\s+C$', ' 3', s)
    s = PREFIX_NOISE.sub('', s)
    s = SUFFIX_NOISE.sub('', s)
    if 'TENNIS' in sport.upper():
        if '/' in s:
            players = [p.strip() for p in s.split('/') if p.strip()]
            s = ' / '.join(players)
    return re.sub(r'\s+', ' ', s).strip()

def escape_sql(val):
    if val is None:
        return 'NULL'
    return "'" + str(val).replace("'", "''") + "'"

def step1_canonize_teams():
    print("--- STEP 1: Canonizing and deduplicating teams ---")
    query = "SELECT t.id, COALESCE(s.id, 'FOOTBALL'), t.default_display_name, t.is_temporary, COALESCE(t.name_english, ''), COALESCE(t.name_local, '') FROM team t LEFT JOIN sport s ON t.sport_id = s.id;"
    stdout = run_psql(query)
    
    teams = []
    for line in stdout.strip().split('\n'):
        line = line.strip()
        if not line:
            continue
        p = line.split('|||')
        if len(p) >= 4:
            teams.append({
                'id': int(p[0]),
                'sport': p[1],
                'name': p[2],
                'is_temp': p[3].lower() in ('t', 'true', '1'),
                'name_en': p[4] if len(p) > 4 else '',
                'name_local': p[5] if len(p) > 5 else ''
            })
    print(f"Loaded {len(teams)} teams.")
    
    groups = {}
    for t in teams:
        canon = canonicalize_team_name(t['name'], t['sport'])
        key = (t['sport'].upper(), canon.lower())
        if key not in groups:
            groups[key] = {'canonical': canon, 'sport': t['sport'], 'teams': []}
        groups[key]['teams'].append(t)
    
    sql_lines = ['BEGIN;']
    renamed = 0
    merged = 0
    
    for key, g in groups.items():
        team_list = g['teams']
        canonical_name = g['canonical']
        sport_id = g['sport']
        
        # Primary: non-temporary first, then lowest id
        primary = min(team_list, key=lambda x: (x['is_temp'], x['id']))
        p_id = primary['id']
        
        # Set local name if cyrillic, english if latin
        has_cyrillic = bool(re.search(r'[а-яА-ЯёЁ]', canonical_name))
        name_local_sql = escape_sql(canonical_name) if has_cyrillic else "NULL"
        name_en_sql = escape_sql(canonical_name) if not has_cyrillic else "NULL"
        
        sql_lines.append(
            f"UPDATE team SET default_display_name = {escape_sql(canonical_name)}, "
            f"name_english = COALESCE(name_english, {name_en_sql}), "
            f"name_local = COALESCE(name_local, {name_local_sql}), "
            f"is_temporary = false WHERE id = {p_id};"
        )
        renamed += 1
        
        for dup in team_list:
            if dup['id'] == p_id:
                continue
            d_id = dup['id']
            # Remap aliases
            sql_lines.append(f"UPDATE team_alias SET team_id = {p_id} WHERE team_id = {d_id};")
            if dup['name']:
                sql_lines.append(
                    f"INSERT INTO team_alias (team_id, raw_name, sport_id) "
                    f"VALUES ({p_id}, {escape_sql(dup['name'])}, {escape_sql(sport_id)}) "
                    f"ON CONFLICT DO NOTHING;"
                )
            sql_lines.append(f"UPDATE match_record SET team1_id = {p_id} WHERE team1_id = {d_id};")
            sql_lines.append(f"UPDATE match_record SET team2_id = {p_id} WHERE team2_id = {d_id};")
            sql_lines.append(f"DELETE FROM team WHERE id = {d_id};")
            merged += 1
            
    sql_lines.append("COMMIT;")
    out_file = 'scripts/apply_team_backfill_dev.sql'
    with open(out_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_lines))
    print(f"Generated {out_file}: {renamed} canonized, {merged} duplicate teams to merge.")
    execute_sql_file(out_file)

if __name__ == '__main__':
    step1_canonize_teams()
