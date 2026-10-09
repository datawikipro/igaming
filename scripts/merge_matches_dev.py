import subprocess
from collections import defaultdict
import os

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

def merge_matches():
    print("--- STEP 2: Merging duplicate matches ---")
    query = '''
    SELECT 
        m.id, 
        m.sport_name, 
        m.team1_id, 
        m.team2_id, 
        COALESCE(m.start_time, 0), 
        COALESCE(m.created_by_source_id, ''),
        COALESCE(m.source_count, 1)
    FROM match_record m;
    '''
    stdout = run_psql(query)

    matches = []
    for line in stdout.strip().split('\n'):
        if not line.strip():
            continue
        p = line.split('|||')
        if len(p) >= 7:
            matches.append({
                'id': int(p[0]),
                'sport': p[1],
                't1_id': int(p[2]) if p[2] else 0,
                't2_id': int(p[3]) if p[3] else 0,
                'start_time': int(p[4]),
                'source': p[5],
                'source_count': int(p[6]),
            })

    print(f'Total matches loaded: {len(matches)}')

    def get_time_bucket(st, sport, window_ms=3 * 3600 * 1000):
        if st == 0:
            return 0
        if 'TENNIS' in sport.upper():
            window_ms = 12 * 3600 * 1000
        return round(st / window_ms)

    clusters = defaultdict(list)
    for m in matches:
        t_min = min(m['t1_id'], m['t2_id'])
        t_max = max(m['t1_id'], m['t2_id'])
        tb = get_time_bucket(m['start_time'], m['sport'])
        cluster_key = (m['sport'].upper(), t_min, t_max, tb)
        clusters[cluster_key].append(m)

    duplicate_clusters = {k: v for k, v in clusters.items() if len(v) > 1}
    total_duplicates = sum(len(v) - 1 for v in duplicate_clusters.values())
    print(f'Found {len(duplicate_clusters)} duplicate clusters to merge ({total_duplicates} duplicate matches).')

    if not duplicate_clusters:
        print("No duplicate matches to merge.")
        return

    sql = [
        'BEGIN;',
        '-- Temporary table for conflict-free odds merging'
    ]

    merged_matches_count = 0
    for k, match_list in duplicate_clusters.items():
        # Pick primary match: highest source_count, or oldest id
        primary = max(match_list, key=lambda x: (x['source_count'], -x['id']))
        p_id = primary['id']
        
        for dup in match_list:
            if dup['id'] == p_id:
                continue
            d_id = dup['id']
            merged_matches_count += 1
            
            # 1. Delete conflicting duplicate odds_actual
            sql.append(f'''
            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = {d_id} 
              AND p.match_id = {p_id}
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            ''')
            
            # 2. Transfer remaining odds_actual
            sql.append(f'UPDATE odds_actual SET match_id = {p_id} WHERE match_id = {d_id};')
            
            # 3. Transfer snapshots and alerts
            sql.append(f'UPDATE odds_snapshot SET match_id = {p_id} WHERE match_id = {d_id};')
            sql.append(f'UPDATE surebet_alert SET match_id = {p_id} WHERE match_id = {d_id};')
            sql.append(f'UPDATE middle_alert SET match_id = {p_id} WHERE match_id = {d_id};')
            sql.append(f'UPDATE valuebet_alert SET match_id = {p_id} WHERE match_id = {d_id};')
            
            # 4. Delete duplicate match
            sql.append(f'DELETE FROM match_record WHERE id = {d_id};')
        
        # Update source_count on primary match
        sql.append(f'''
        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = {p_id})),
            updated_at = NOW()
        WHERE id = {p_id};
        ''')

    sql.append('COMMIT;')

    out_file = 'scripts/merge_matches_dev.sql'
    with open(out_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql))

    print(f'Generated {out_file} with {len(sql)} statements.')
    print(f'Duplicate matches scheduled for merge: {merged_matches_count}')
    execute_sql_file(out_file)

if __name__ == '__main__':
    merge_matches()
