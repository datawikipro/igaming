import json
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

def escape_sql(val):
    if val is None or val == '':
        return 'NULL'
    return "'" + str(val).replace("'", "''") + "'"

def main():
    print("Loading canonical teams into database...")
    
    # 1. Load wiki pairs
    wiki_pairs = []
    if os.path.exists('data/wiki_pairs.json'):
        with open('data/wiki_pairs.json', 'r', encoding='utf-8') as f:
            wiki_pairs = json.load(f)
            
    # 2. Load wikidata teams
    wikidata_teams = []
    if os.path.exists('data/wikidata_canonical_teams.json'):
        with open('data/wikidata_canonical_teams.json', 'r', encoding='utf-8') as f:
            wikidata_teams = json.load(f)
            
    print(f"Loaded {len(wiki_pairs)} wiki pairs and {len(wikidata_teams)} wikidata teams.")
    
    sql_lines = ['BEGIN;']
    
    # Process wiki pairs
    for p in wiki_pairs:
        sport = p['sport']
        ru = p['ru'].strip()
        en = p['en'].strip()
        if not ru or not en:
            continue
        
        # Upsert or link team
        # If team exists with ru or en name, update both fields and set is_temporary = false
        sql_lines.append(f"""
        DO $$
        DECLARE
            t_id BIGINT;
        BEGIN
            SELECT id INTO t_id FROM team WHERE sport_id = {escape_sql(sport)} AND (default_display_name ILIKE {escape_sql(ru)} OR default_display_name ILIKE {escape_sql(en)}) LIMIT 1;
            IF t_id IS NOT NULL THEN
                UPDATE team SET name_english = {escape_sql(en)}, name_local = {escape_sql(ru)}, is_temporary = false WHERE id = t_id;
                INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES (t_id, {escape_sql(ru)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;
                INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES (t_id, {escape_sql(en)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;
            ELSE
                INSERT INTO team (sport_id, default_display_name, name_english, name_local, name_bookmaker, is_temporary, source_count)
                VALUES ({escape_sql(sport)}, {escape_sql(ru)}, {escape_sql(en)}, {escape_sql(ru)}, {escape_sql(en)}, false, 1)
                RETURNING id INTO t_id;
                INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES (t_id, {escape_sql(ru)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;
                INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES (t_id, {escape_sql(en)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;
            END IF;
        END $$;
        """)

    # Process wikidata teams (especially Tennis and National teams)
    for wt in wikidata_teams:
        sport = wt.get('sport', 'FOOTBALL')
        en = wt.get('name_en', '').strip()
        ru = wt.get('name_ru', '').strip()
        alts_en = wt.get('aliases_en', [])
        alts_ru = wt.get('aliases_ru', [])
        
        if not en and not ru:
            continue
            
        display = ru if ru else en
        
        # Build block
        sql_lines.append(f"""
        DO $$
        DECLARE
            t_id BIGINT;
        BEGIN
            SELECT id INTO t_id FROM team WHERE sport_id = {escape_sql(sport)} AND (
                (name_english IS NOT NULL AND name_english ILIKE {escape_sql(en)}) OR
                (name_local IS NOT NULL AND name_local ILIKE {escape_sql(ru)}) OR
                default_display_name ILIKE {escape_sql(display)}
            ) LIMIT 1;
            
            IF t_id IS NOT NULL THEN
                UPDATE team SET 
                    name_english = COALESCE(name_english, {escape_sql(en)}),
                    name_local = COALESCE(name_local, {escape_sql(ru)}),
                    is_temporary = false 
                WHERE id = t_id;
            ELSE
                INSERT INTO team (sport_id, default_display_name, name_english, name_local, name_bookmaker, is_temporary, source_count)
                VALUES ({escape_sql(sport)}, {escape_sql(display)}, {escape_sql(en)}, {escape_sql(ru)}, {escape_sql(display)}, false, 1)
                RETURNING id INTO t_id;
            END IF;
            
            IF {escape_sql(en)} IS NOT NULL THEN
                INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES (t_id, {escape_sql(en)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;
            END IF;
            IF {escape_sql(ru)} IS NOT NULL THEN
                INSERT INTO team_alias (team_id, raw_name, sport_id) VALUES (t_id, {escape_sql(ru)}, {escape_sql(sport)}) ON CONFLICT DO NOTHING;
            END IF;
        END $$;
        """)

    sql_lines.append("COMMIT;")
    
    out_file = 'scripts/apply_canonical_teams_dev.sql'
    with open(out_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_lines))
        
    print(f"Generated {out_file}. Executing in {POD}...")
    execute_sql_file(out_file)

if __name__ == '__main__':
    main()
