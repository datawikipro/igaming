import json
import math
import psycopg2
import urllib.request
from collections import defaultdict
from datetime import datetime

def main():
    print("Connecting to PostgreSQL igaming_sofascore...")
    pg_conn = psycopg2.connect("postgresql://postgres:postgres@10.108.82.251:5432/igaming_sofascore")
    cur = pg_conn.cursor()

    print("Querying tennis raw events...")
    cur.execute("""
        SELECT event_id, event_json, match_date 
        FROM sofascore_raw_events 
        WHERE sport = 'tennis' 
          AND event_json IS NOT NULL 
          AND (event_json LIKE '%"ranking"%' OR event_json LIKE '%"currentRanking"%')
        ORDER BY match_date ASC
    """)

    players = {}  # pid -> {'name': str, 'ranks': list of (date, rank)}

    row_count = 0
    for event_id, event_json_str, match_date in cur:
        row_count += 1
        if not event_json_str:
            continue
        try:
            root = json.loads(event_json_str)
            ev = root.get('event', root)
            if not isinstance(ev, dict):
                continue
            
            for side in ['homeTeam', 'awayTeam']:
                team = ev.get(side)
                if not isinstance(team, dict):
                    continue
                pid = team.get('id')
                if not pid or not isinstance(pid, int):
                    continue
                name = team.get('name') or team.get('fullName') or ''
                
                # Check ranking
                rank = team.get('ranking')
                if not rank and isinstance(team.get('playerTeamInfo'), dict):
                    rank = team.get('playerTeamInfo', {}).get('currentRanking')
                
                if rank:
                    try:
                        rank_int = int(rank)
                        if 1 <= rank_int <= 3000:
                            if pid not in players:
                                players[pid] = {'name': name, 'ranks': []}
                            elif name and not players[pid]['name']:
                                players[pid]['name'] = name
                            players[pid]['ranks'].append((match_date or 0, rank_int))
                    except (ValueError, TypeError):
                        pass
        except Exception as e:
            continue
        
        if row_count % 10000 == 0:
            print(f"Processed {row_count} events, found {len(players)} ranked players so far...")

    print(f"Total events processed: {row_count}. Total distinct ranked players found: {len(players)}")

    # Compute peak_rank, current_rank, quartic_stature
    rows_to_insert = []
    for pid, pdata in players.items():
        ranks_sorted = sorted(pdata['ranks'], key=lambda x: x[0])
        current_rank = ranks_sorted[-1][1]
        peak_rank = min(r[1] for r in ranks_sorted)
        
        # Quartic stature formula: 1.0 / (peak_rank ** 0.25)
        quartic_stature = round(1.0 / (peak_rank ** 0.25), 4)
        name_clean = pdata['name'].replace("'", "\\'").replace('"', '\\"')
        
        rows_to_insert.append((pid, name_clean, current_rank, peak_rank, quartic_stature))

    print(f"Sample of extracted players:")
    for sample in sorted(rows_to_insert, key=lambda x: x[3])[:10]:
        print(f"  PID: {sample[0]}, Name: {sample[1]}, Current: {sample[2]}, Peak: {sample[3]}, Stature: {sample[4]}")

    # Insert into ClickHouse
    print("Writing TSV for ClickHouse insertion...")
    tsv_path = "/tmp/tennis_rankings.tsv"
    now_str = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")
    with open(tsv_path, "w", encoding="utf-8") as f:
        for pid, name, cur_r, peak_r, stat in rows_to_insert:
            f.write(f"{pid}\t{name}\t{cur_r}\t{peak_r}\t{stat}\t{now_str}\n")
    
    print(f"Wrote {len(rows_to_insert)} lines to {tsv_path}. Inserting via clickhouse-client...")
    import subprocess
    cmd = [
        "clickhouse-client",
        "--host", "10.111.38.2",
        "--port", "9000",
        "--user", "default",
        "--password", "clickhouse",
        "--database", "igaming_dds",
        "--query", "INSERT INTO igaming_dds.tennis_player_rankings (player_id, player_name, current_rank, peak_rank, quartic_stature, updated_at) FORMAT TabSeparated"
    ]
    with open(tsv_path, "r", encoding="utf-8") as f:
        res = subprocess.run(cmd, stdin=f, capture_output=True, text=True)
        if res.returncode != 0:
            print("clickhouse-client error:", res.stderr)
            # Fallback to local clickhouse container or http
            import base64
            auth = base64.b64encode(b"default:clickhouse").decode("ascii")
            req = urllib.request.Request(
                "http://10.111.38.2:8123/?query=" + urllib.parse.quote("INSERT INTO igaming_dds.tennis_player_rankings FORMAT TabSeparated"),
                data=open(tsv_path, "rb").read(),
                headers={"Authorization": f"Basic {auth}", "Content-Type": "text/tab-separated-values"}
            )
            with urllib.request.urlopen(req, timeout=30) as resp:
                print("ClickHouse HTTP response:", resp.read().decode())
        else:
            print("Successfully inserted into ClickHouse via clickhouse-client!")

    # Also insert into PostgreSQL table
    print("Inserting into PostgreSQL tennis_player_rankings...")
    from psycopg2.extras import execute_values
    pg_rows = [(pid, name, cur_r, peak_r, len(players[pid]['ranks'])) for pid, name, cur_r, peak_r, stat in rows_to_insert]
    execute_values(cur, """
        INSERT INTO tennis_player_rankings (player_id, player_name, current_rank, peak_rank, matches_count)
        VALUES %s
        ON CONFLICT (player_id) DO UPDATE 
        SET current_rank = EXCLUDED.current_rank, 
            peak_rank = LEAST(tennis_player_rankings.peak_rank, EXCLUDED.peak_rank),
            matches_count = EXCLUDED.matches_count,
            updated_at = now()
    """, pg_rows)
    pg_conn.commit()
    print("PostgreSQL tennis_player_rankings updated!")

    print("Success! Tennis player rankings populated in ClickHouse DDS.")

if __name__ == "__main__":
    main()
