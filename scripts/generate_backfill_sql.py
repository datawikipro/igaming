import re
import subprocess
import os

# Fetch teams using PGCLIENTENCODING=UTF8
cmd = [
    'kubectl', 'exec', '-i', '-n', 'igaming-master', 'igaming-aggregator-db-0', '--',
    'env', 'PGCLIENTENCODING=UTF8', 'psql', '-U', 'postgres', '-d', 'igaming_aggregator', '-t', '-A', '-F', '|||',
    '-c', 'SELECT t.id, COALESCE(s.id, \'FOOTBALL\'), t.default_display_name, t.is_temporary, COALESCE(t.name_english, \'\') FROM team t LEFT JOIN sport s ON t.sport_id = s.id;'
]
res = subprocess.run(cmd, capture_output=True)
stdout_text = res.stdout.decode('utf-8', errors='replace')

teams = []
for line in stdout_text.strip().split('\n'):
    line = line.strip()
    if not line:
        continue
    parts = line.split('|||')
    if len(parts) >= 4:
        teams.append({
            'id': int(parts[0]),
            'sport': parts[1],
            'name': parts[2],
            'is_temp': parts[3].lower() in ('t', 'true', '1'),
            'name_en': parts[4] if len(parts) > 4 else ''
        })

print(f'Parsed {len(teams)} teams with UTF-8 encoding.')

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

groups = {}
for t in teams:
    canon = canonicalize_team_name(t['name'], t['sport'])
    key = (t['sport'].upper(), canon.lower())
    if key not in groups:
        groups[key] = {'canonical': canon, 'sport': t['sport'], 'teams': []}
    groups[key]['teams'].append(t)

# Build SQL statements
sql_lines = ['BEGIN;']

def escape_sql(val):
    if val is None:
        return 'NULL'
    return "'" + str(val).replace("'", "''") + "'"

renamed = 0
merged = 0

for key, g in groups.items():
    team_list = g['teams']
    canonical_name = g['canonical']
    sport_id = g['sport']
    
    # Pick primary
    primary = min(team_list, key=lambda x: (x['is_temp'], x['id']))
    p_id = primary['id']
    
    # Update primary name if changed or temporary
    if primary['name'] != canonical_name or primary['is_temp']:
        sql_lines.append(
            f"UPDATE team SET default_display_name = {escape_sql(canonical_name)}, "
            f"name_english = COALESCE(name_english, {escape_sql(canonical_name)}), "
            f"is_temporary = false WHERE id = {p_id};"
        )
        renamed += 1
    
    for dup in team_list:
        if dup['id'] == p_id:
            continue
        d_id = dup['id']
        
        # 1. Remap aliases
        sql_lines.append(f"UPDATE team_alias SET team_id = {p_id} WHERE team_id = {d_id};")
        
        # 2. Insert alias for old raw name
        if dup['name']:
            sql_lines.append(
                f"INSERT INTO team_alias (team_id, raw_name, sport_id) "
                f"VALUES ({p_id}, {escape_sql(dup['name'])}, {escape_sql(sport_id)}) "
                f"ON CONFLICT DO NOTHING;"
            )
        
        # 3. Remap match references
        sql_lines.append(f"UPDATE match_record SET team1_id = {p_id} WHERE team1_id = {d_id};")
        sql_lines.append(f"UPDATE match_record SET team2_id = {p_id} WHERE team2_id = {d_id};")
        
        # 4. Delete duplicate team
        sql_lines.append(f"DELETE FROM team WHERE id = {d_id};")
        merged += 1

sql_lines.append("COMMIT;")

sql_content = '\n'.join(sql_lines)
with open('scripts/apply_team_backfill.sql', 'w', encoding='utf-8') as f:
    f.write(sql_content)

print(f'Generated scripts/apply_team_backfill.sql with {len(sql_lines)} SQL statements.')
print(f'Primary teams updated/canonized: {renamed}')
print(f'Duplicate teams to merge & delete: {merged}')
