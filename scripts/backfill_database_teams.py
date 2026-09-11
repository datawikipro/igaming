import re
import subprocess
import json

def get_db_teams():
    cmd = [
        'kubectl', 'exec', '-i', '-n', 'igaming-master', 'igaming-aggregator-db-0', '--',
        'psql', '-U', 'postgres', '-d', 'igaming_aggregator', '-t', '-A', '-F', '\t',
        '-c', 'SELECT t.id, COALESCE(s.id, \'FOOTBALL\'), t.default_display_name, t.is_temporary, COALESCE(t.name_english, \'\') FROM team t LEFT JOIN sport s ON t.sport_id = s.id;'
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
    if res.returncode != 0:
        print('Error fetching teams:', res.stderr)
        return []
    
    teams = []
    for line in res.stdout.strip().split('\n'):
        if not line.strip():
            continue
        parts = line.split('\t')
        if len(parts) >= 4:
            t_id = int(parts[0])
            sport = parts[1]
            display_name = parts[2]
            is_temp = parts[3].lower() in ('t', 'true', '1')
            name_en = parts[4] if len(parts) > 4 else ''
            teams.append({
                'id': t_id,
                'sport': sport,
                'name': display_name,
                'is_temp': is_temp,
                'name_en': name_en
            })
    return teams

# Regex rules matching ClubTeamSignatureParser
LA_PATTERN = re.compile(r'(?ui)^Л-А\s+')
NY_PATTERN = re.compile(r'(?ui)^Н-Й\s+')
NO_PATTERN = re.compile(r'(?ui)^Н-О\s+')
SP_PATTERN = re.compile(r'(?ui)^С-П\s+')
AGE_WINLINE = re.compile(r'(?ui)\s*\(до\s*(\d{2})\)')
AGE_BETCITY = re.compile(r'(?ui)\s*\((\d{2})\)$')
GENERIC_AGE = re.compile(r'(?ui)\bU-?(\d{2})\b')
PREFIX_NOISE = re.compile(r'(?ui)^(?:ФК|FC|БК|BC|ХК|HC|ПФК|PFC|МФК|MFC|ЖФК|WFC|СК|SC|КСМ|CSM|СВ|SV|ТСВ|TSV|БВБ|BVB|КФ|CF|ИРП|IRP|КД|CD|УД|UD|СД|SD|ВФБ|VFB|ФСВ|FSV|РБ|RB|Клуб|Club)\s+')
SUFFIX_NOISE = re.compile(r'(?ui)\s+(?:ФК|FC|БК|BC|ХК|HC|ПФК|PFC|МФК|MFC|ЖФК|WFC|СК|SC)$')
STAT_PREFIX = re.compile(r'(?ui)^(?:УГЛ|ЖК|ФОЛЫ|УДАРЫ|ОФСАЙДЫ|АУТЫ)\s+')

def canonicalize_team_name(name, sport):
    if not name:
        return ''
    s = name.strip()
    
    # 1. Geographic abbreviations
    s = LA_PATTERN.sub('Лос-Анджелес ', s)
    s = NY_PATTERN.sub('Нью-Йорк ', s)
    s = NO_PATTERN.sub('Нью-Орлеан ', s)
    s = SP_PATTERN.sub('Санкт-Петербург ', s)
    
    # 2. Stat prefixes
    s = STAT_PREFIX.sub('', s)
    
    # 3. Ages
    s = AGE_WINLINE.sub(r' U\1', s)
    s = AGE_BETCITY.sub(r' U\1', s)
    
    # 4. Reserves & Roman numerals
    s = re.sub(r'(?ui)\s+II$', ' 2', s)
    s = re.sub(r'(?ui)\s+III$', ' 3', s)
    s = re.sub(r'(?ui)\s+B$', ' 2', s)
    s = re.sub(r'(?ui)\s+C$', ' 3', s)
    
    # 5. Club prefixes/suffixes
    s = PREFIX_NOISE.sub('', s)
    s = SUFFIX_NOISE.sub('', s)
    
    # 6. Tennis doubles normalization (e.g. "A / B" -> standardized)
    if 'TENNIS' in sport.upper():
        if '/' in s:
            players = [p.strip() for p in s.split('/') if p.strip()]
            s = ' / '.join(players)
    
    # 7. Whitespace
    s = re.sub(r'\s+', ' ', s).strip()
    return s

def analyze_backfill():
    teams = get_db_teams()
    print(f'Total teams fetched from DB: {len(teams)}')
    
    groups = {}
    changed_names = 0
    
    for t in teams:
        canonical = canonicalize_team_name(t['name'], t['sport'])
        if canonical != t['name']:
            changed_names += 1
        key = (t['sport'].upper(), canonical.lower())
        if key not in groups:
            groups[key] = {'canonical': canonical, 'teams': []}
        groups[key]['teams'].append(t)
    
    duplicate_groups = {k: v for k, v in groups.items() if len(v['teams']) > 1}
    total_duplicates = sum(len(v['teams']) - 1 for v in duplicate_groups.values())
    
    print(f'Total unique canonical teams: {len(groups)}')
    print(f'Teams with names to be cleaned/standardized: {changed_names}')
    print(f'Duplicate groups found: {len(duplicate_groups)}')
    print(f'Total redundant duplicate team rows to be merged: {total_duplicates}')
    
    print('\nSample duplicate clusters:')
    for i, (k, v) in enumerate(list(duplicate_groups.items())[:10]):
        print(f'[{k[0]}] Canonical: "{v["canonical"]}" (Count: {len(v["teams"])})')
        for t in v['teams']:
            print(f'   -> ID: {t["id"]} | Raw: "{t["name"]}" | is_temp: {t["is_temp"]}')

if __name__ == '__main__':
    analyze_backfill()
