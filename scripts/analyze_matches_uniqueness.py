import subprocess
from collections import defaultdict
from datetime import datetime

cmd = [
    'kubectl', 'exec', '-i', '-n', 'igaming-master', 'igaming-aggregator-db-0', '--',
    'env', 'PGCLIENTENCODING=UTF8', 'psql', '-U', 'postgres', '-d', 'igaming_aggregator', '-t', '-A', '-F', '|||',
    '-c', '''
    SELECT 
        m.id, 
        m.sport_name, 
        m.team1_id, 
        m.team2_id, 
        COALESCE(m.start_time, 0), 
        COALESCE(m.created_by_source_id, ''),
        COALESCE(m.source_count, 1),
        COALESCE(m.is_live, false),
        t1.default_display_name,
        t2.default_display_name
    FROM match_record m
    LEFT JOIN team t1 ON m.team1_id = t1.id
    LEFT JOIN team t2 ON m.team2_id = t2.id;
    '''
]

res = subprocess.run(cmd, capture_output=True)
stdout = res.stdout.decode('utf-8', errors='replace')

matches = []
for line in stdout.strip().split('\n'):
    if not line.strip():
        continue
    p = line.split('|||')
    if len(p) >= 8:
        matches.append({
            'id': int(p[0]),
            'sport': p[1],
            't1_id': int(p[2]) if p[2] else 0,
            't2_id': int(p[3]) if p[3] else 0,
            'start_time': int(p[4]),
            'source': p[5],
            'source_count': int(p[6]),
            'is_live': p[7].lower() in ('t', 'true', '1'),
            't1_name': p[8] if len(p) > 8 else '',
            't2_name': p[9] if len(p) > 9 else '',
        })

total_matches = len(matches)
print(f'Total match records in database: {total_matches}')

# Group matches by (sport, min_team, max_team, time_bucket_3h)
def get_time_bucket(st, window_ms=3 * 3600 * 1000):
    if st == 0:
        return 0
    return round(st / window_ms)

clusters = defaultdict(list)
for m in matches:
    t_min = min(m['t1_id'], m['t2_id'])
    t_max = max(m['t1_id'], m['t2_id'])
    tb = get_time_bucket(m['start_time'])
    cluster_key = (m['sport'].upper(), t_min, t_max, tb)
    clusters[cluster_key].append(m)

unique_real_events = len(clusters)
duplicate_clusters = {k: v for k, v in clusters.items() if len(v) > 1}
total_duplicate_match_rows = sum(len(v) - 1 for v in duplicate_clusters.values())

sport_breakdown = defaultdict(lambda: {'total_records': 0, 'unique_events': 0, 'sources': set()})
for k, v in clusters.items():
    sport = k[0]
    sport_breakdown[sport]['unique_events'] += 1
    sport_breakdown[sport]['total_records'] += len(v)
    for m in v:
        if m['source']:
            sport_breakdown[sport]['sources'].add(m['source'])

print('\n================ MATCH UNIQUENESS ANALYSIS ================')
print(f'1. Total match rows in DB: {total_matches}')
print(f'2. Truly unique real-world match events: {unique_real_events}')
print(f'3. Redundant duplicate match rows (same teams within +/- 3h): {total_duplicate_match_rows}')
print(f'4. Event duplication rate: {total_duplicate_match_rows / total_matches * 100:.1f}%')
print(f'5. Multi-source merged matches: {len([m for m in matches if m["source_count"] > 1])}')

print('\n================ SPORT DISTRIBUTION ================')
for sport, data in sorted(sport_breakdown.items(), key=lambda x: -x[1]['unique_events']):
    sources_str = ', '.join(sorted(data['sources'])) if data['sources'] else 'N/A'
    print(f'- {sport:20s}: {data["unique_events"]:5d} unique matches (from {data["total_records"]} rows, sources: [{sources_str}])')

print('\n================ TOP DUPLICATED MATCH EXAMPLES ================')
for i, (k, v) in enumerate(list(sorted(duplicate_clusters.items(), key=lambda x: -len(x[1])))[:5]):
    m0 = v[0]
    dt_str = datetime.utcfromtimestamp(m0['start_time']/1000).strftime('%Y-%m-%d %H:%M') if m0['start_time'] else 'N/A'
    print(f'Cluster #{i+1} [{k[0]}] {dt_str} UTC: "{m0["t1_name"]}" vs "{m0["t2_name"]}"')
    for m in v:
        print(f'   -> Match ID: {m["id"]} | Source: {m["source"]} | start_time: {m["start_time"]} | source_count: {m["source_count"]}')
