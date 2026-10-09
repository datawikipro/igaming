import subprocess
import os
import re

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

# Comprehensive mapping: canonical_en, canonical_ru, aliases_en, aliases_ru
COUNTRIES = [
    ("USA", "США", ["United States", "U.S.A.", "United States of America"], ["Соединенные Штаты", "Соединенные Штаты Америки"]),
    ("Russia", "Россия", ["Russian Federation"], ["Российская Федерация", "РФ"]),
    ("Germany", "Германия", ["Deutschland"], ["ФРГ"]),
    ("France", "Франция", [], []),
    ("Spain", "Испания", ["Espana"], []),
    ("Italy", "Италия", ["Italia"], []),
    ("England", "Англия", ["Great Britain", "United Kingdom", "UK"], ["Великобритания"]),
    ("Brazil", "Бразилия", ["Brasil"], []),
    ("Argentina", "Аргентина", [], []),
    ("Netherlands", "Нидерланды", ["Holland", "The Netherlands"], ["Голландия"]),
    ("Portugal", "Португалия", [], []),
    ("Belgium", "Бельгия", [], []),
    ("Croatia", "Хорватия", ["Hrvatska"], []),
    ("Poland", "Польша", ["Polska"], []),
    ("Czech Republic", "Чехия", ["Czechia"], ["Чешская Республика"]),
    ("Sweden", "Швеция", ["Sverige"], []),
    ("Norway", "Норвегия", ["Norge"], []),
    ("Denmark", "Дания", ["Danmark"], []),
    ("Finland", "Финляндия", ["Suomi"], []),
    ("Switzerland", "Швейцария", ["Swiss"], []),
    ("Austria", "Австрия", ["Osterreich"], []),
    ("Turkey", "Турция", ["Turkiye"], []),
    ("Greece", "Греция", ["Hellas"], []),
    ("Serbia", "Сербия", ["Srbija"], []),
    ("Ukraine", "Украина", [], []),
    ("Japan", "Япония", ["Nippon"], []),
    ("China", "Китай", ["PRC"], ["КНР"]),
    ("South Korea", "Южная Корея", ["Korea Republic", "Korea"], ["Корея"]),
    ("Australia", "Австралия", [], []),
    ("Canada", "Канада", [], []),
    ("Mexico", "Мексика", [], []),
    ("Chile", "Чили", [], []),
    ("Peru", "Перу", [], []),
    ("Colombia", "Колумбия", [], []),
    ("Uruguay", "Уругвай", [], []),
    ("Ecuador", "Эквадор", [], []),
    ("Venezuela", "Венесуэла", [], []),
    ("Paraguay", "Парагвай", [], []),
    ("Bolivia", "Боливия", [], []),
    ("Saudi Arabia", "Саудовская Аравия", [], []),
    ("Iran", "Иран", [], []),
    ("Morocco", "Марокко", [], []),
    ("Egypt", "Египет", [], []),
    ("Senegal", "Сенегал", [], []),
    ("Nigeria", "Нигерия", [], []),
    ("Ghana", "Гана", [], []),
    ("Cameroon", "Камерун", [], []),
    ("Algeria", "Алжир", [], []),
    ("Tunisia", "Тунис", [], []),
    ("South Africa", "ЮАР", ["RSA"], ["Южно-Африканская Республика"]),
    ("Ivory Coast", "Кот-д'Ивуар", ["Cote d'Ivoire"], ["Берег Слоновой Кости"]),
    ("Iceland", "Исландия", [], []),
    ("Wales", "Уэльс", [], []),
    ("Scotland", "Шотландия", [], []),
    ("Northern Ireland", "Северная Ирландия", [], []),
    ("Ireland", "Ирландия", ["Republic of Ireland"], []),
    ("Hungary", "Венгрия", ["Magyarorszag"], []),
    ("Romania", "Румыния", [], []),
    ("Bulgaria", "Болгария", [], []),
    ("Slovakia", "Словакия", [], []),
    ("Slovenia", "Словения", [], []),
    ("Bosnia and Herzegovina", "Босния и Герцеговина", ["Bosnia"], ["Босния"]),
    ("Montenegro", "Черногория", ["Crna Gora"], []),
    ("North Macedonia", "Северная Македония", ["Macedonia"], ["Македония"]),
    ("Albania", "Албания", [], []),
    ("Israel", "Израиль", [], []),
    ("Kazakhstan", "Казахстан", [], []),
    ("Uzbekistan", "Узбекистан", [], []),
    ("Georgia", "Грузия", [], []),
    ("Armenia", "Армения", [], []),
    ("Azerbaijan", "Азербайджан", [], []),
    ("Belarus", "Беларусь", [], ["Белоруссия"]),
    ("Lithuania", "Литва", ["Lietuva"], []),
    ("Latvia", "Латвия", ["Latvija"], []),
    ("Estonia", "Эстония", ["Eesti"], []),
    ("Moldova", "Молдова", [], ["Молдавия"]),
    ("Cyprus", "Кипр", [], []),
    ("Malta", "Мальта", [], []),
    ("Luxembourg", "Люксембург", [], []),
    ("Faroe Islands", "Фарерские острова", [], ["Фареры"]),
    ("Liechtenstein", "Лихтенштейн", [], []),
    ("Gibraltar", "Гибралтар", [], []),
    ("San Marino", "Сан-Марино", [], []),
    ("Andorra", "Андорра", [], []),
    ("Costa Rica", "Коста-Рика", [], []),
    ("Panama", "Панама", [], []),
    ("Honduras", "Гондурас", [], []),
    ("Jamaica", "Ямайка", [], []),
    ("Trinidad and Tobago", "Тринидад и Тобаго", [], []),
    ("El Salvador", "Сальвадор", [], []),
    ("Guatemala", "Гватемала", [], []),
    ("Cuba", "Куба", [], []),
    ("Dominican Republic", "Доминиканская Республика", ["Dominicana"], ["Доминикана"]),
    ("India", "Индия", [], []),
    ("Pakistan", "Пакистан", [], []),
    ("Bangladesh", "Бангладеш", [], []),
    ("New Zealand", "Новая Зеландия", [], []),
    ("Thailand", "Таиланд", [], ["Тайланд"]),
    ("Vietnam", "Вьетнам", [], []),
    ("Malaysia", "Малайзия", [], []),
    ("Indonesia", "Индонезия", [], []),
    ("Philippines", "Филиппины", [], []),
    ("Singapore", "Сингапур", [], []),
    ("Qatar", "Катар", [], []),
    ("UAE", "ОАЭ", ["United Arab Emirates"], ["Объединенные Арабские Эмираты"]),
    ("Kuwait", "Кувейт", [], []),
    ("Bahrain", "Бахрейн", [], []),
    ("Oman", "Оман", [], []),
    ("Jordan", "Иордания", [], []),
    ("Iraq", "Ирак", [], []),
    ("Lebanon", "Ливан", [], []),
    ("Syria", "Сирия", [], [])
]

def escape_sql(val):
    if val is None or val == '':
        return 'NULL'
    return "'" + str(val).replace("'", "''") + "'"

def main():
    print("Fetching teams from database...")
    query = "SELECT t.id, t.sport_id, t.default_display_name, COALESCE(t.name_english, ''), COALESCE(t.name_local, '') FROM team t;"
    stdout = run_psql(query)
    
    teams_by_sport = {}
    for line in stdout.strip().split('\n'):
        line = line.strip()
        if not line:
            continue
        p = line.split('|||')
        if len(p) >= 3:
            t_id = int(p[0])
            sport = p[1].upper()
            name = p[2]
            name_en = p[3] if len(p) > 3 else ''
            name_ru = p[4] if len(p) > 4 else ''
            
            if sport not in teams_by_sport:
                teams_by_sport[sport] = []
            teams_by_sport[sport].append({
                'id': t_id,
                'name': name,
                'name_en': name_en,
                'name_ru': name_ru
            })
            
    print(f"Loaded {sum(len(v) for v in teams_by_sport.values())} teams across {len(teams_by_sport)} sports.")
    
    sql_lines = ['BEGIN;']
    merged_teams_count = 0
    
    # Suffix pattern: e.g. " (haiko)", " (tbl)", " (жен)", " (w)", " U21", " U19"
    TAG_PATTERN = re.compile(r'(?ui)\s*(\([^)]*\)|\bU-?\d{2}\b)$')
    
    for sport, team_list in teams_by_sport.items():
        # Map: normalized tag -> list of matching teams
        for canon_en, canon_ru, alts_en, alts_ru in COUNTRIES:
            all_en_names = {canon_en.lower()} | {a.lower() for a in alts_en}
            all_ru_names = {canon_ru.lower()} | {a.lower() for a in alts_ru}
            
            # Find all teams matching EN or RU country with their tags
            en_teams_by_tag = {}
            ru_teams_by_tag = {}
            
            for t in team_list:
                name = t['name'].strip()
                tag_match = TAG_PATTERN.search(name)
                tag = tag_match.group(0).strip().lower() if tag_match else ""
                base_name = TAG_PATTERN.sub('', name).strip().lower()
                
                if base_name in all_en_names:
                    en_teams_by_tag.setdefault(tag, []).append(t)
                elif base_name in all_ru_names:
                    ru_teams_by_tag.setdefault(tag, []).append(t)
            
            # Check for tags that exist in both EN and RU
            all_tags = set(en_teams_by_tag.keys()) | set(ru_teams_by_tag.keys())
            for tag in all_tags:
                en_matches = en_teams_by_tag.get(tag, [])
                ru_matches = ru_teams_by_tag.get(tag, [])
                
                if en_matches and ru_matches:
                    # We have a duplicate EN vs RU team in the same sport with the same tag!
                    # Pick primary (prefer the Russian name for default_display_name on Russian sports/bookies, or lowest ID)
                    primary = ru_matches[0]
                    p_id = primary['id']
                    
                    raw_tag = TAG_PATTERN.search(primary['name'])
                    tag_str = raw_tag.group(0) if raw_tag else ""
                    
                    full_en = f"{canon_en}{tag_str}".strip()
                    full_ru = f"{canon_ru}{tag_str}".strip()
                    
                    sql_lines.append(
                        f"UPDATE team SET default_display_name = {escape_sql(full_ru)}, "
                        f"name_english = {escape_sql(full_en)}, "
                        f"name_local = {escape_sql(full_ru)}, "
                        f"is_temporary = false WHERE id = {p_id};"
                    )
                    
                    # Global aliases for both
                    sql_lines.append(f"INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES ({p_id}, {escape_sql(full_ru)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;")
                    sql_lines.append(f"INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES ({p_id}, {escape_sql(full_en)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;")
                    
                    # Merge all other matches into primary
                    other_teams = (ru_matches[1:] if len(ru_matches) > 1 else []) + en_matches
                    for dup in other_teams:
                        d_id = dup['id']
                        if d_id == p_id:
                            continue
                        sql_lines.append(f"UPDATE team_alias SET team_id = {p_id} WHERE team_id = {d_id};")
                        sql_lines.append(f"INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES ({p_id}, {escape_sql(dup['name'])}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;")
                        sql_lines.append(f"UPDATE match_record SET team1_id = {p_id} WHERE team1_id = {d_id};")
                        sql_lines.append(f"UPDATE match_record SET team2_id = {p_id} WHERE team2_id = {d_id};")
                        sql_lines.append(f"DELETE FROM team WHERE id = {d_id};")
                        merged_teams_count += 1
                elif en_matches and not ru_matches:
                    # Only EN team exists: add canonical RU name and alias
                    for t in en_matches:
                        p_id = t['id']
                        raw_tag = TAG_PATTERN.search(t['name'])
                        tag_str = raw_tag.group(0) if raw_tag else ""
                        full_en = f"{canon_en}{tag_str}".strip()
                        full_ru = f"{canon_ru}{tag_str}".strip()
                        
                        sql_lines.append(
                            f"UPDATE team SET name_english = {escape_sql(full_en)}, "
                            f"name_local = {escape_sql(full_ru)}, "
                            f"is_temporary = false WHERE id = {p_id};"
                        )
                        sql_lines.append(f"INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES ({p_id}, {escape_sql(full_ru)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;")
                        sql_lines.append(f"INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES ({p_id}, {escape_sql(full_en)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;")
                elif ru_matches and not en_matches:
                    # Only RU team exists: add canonical EN name and alias
                    for t in ru_matches:
                        p_id = t['id']
                        raw_tag = TAG_PATTERN.search(t['name'])
                        tag_str = raw_tag.group(0) if raw_tag else ""
                        full_en = f"{canon_en}{tag_str}".strip()
                        full_ru = f"{canon_ru}{tag_str}".strip()
                        
                        sql_lines.append(
                            f"UPDATE team SET name_english = {escape_sql(full_en)}, "
                            f"name_local = {escape_sql(full_ru)}, "
                            f"is_temporary = false WHERE id = {p_id};"
                        )
                        sql_lines.append(f"INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES ({p_id}, {escape_sql(full_ru)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;")
                        sql_lines.append(f"INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES ({p_id}, {escape_sql(full_en)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;")

    sql_lines.append("COMMIT;")
    
    out_file = 'scripts/merge_countries.sql'
    with open(out_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_lines))
        
    print(f"Generated {out_file}: scheduled {merged_teams_count} country/national team merges.")
    if merged_teams_count > 0 or len(sql_lines) > 2:
        execute_sql_file(out_file)

if __name__ == '__main__':
    main()
