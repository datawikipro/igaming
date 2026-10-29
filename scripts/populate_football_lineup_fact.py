#!/usr/bin/env python3
"""
Populate ClickHouse DDS `igaming_dds.football_lineup_fact`
Ingests detailed match lineups, formations, ratings, squad market values,
and bench attacking depth from PostgreSQL `sofascore_raw_events`.
"""

import psycopg2
import json
import datetime
import urllib.request
import urllib.parse
import sys
import time

CLICKHOUSE_URL = "http://clickhouse.igaming-analytics.svc.cluster.local:8123/?database=igaming_dds&user=default&password=clickhouse"
PG_HOST = "10.108.82.251"
PG_PORT = 5432
PG_USER = "postgres"
PG_PASS = "postgres"

def safe_float(v, default=0.0):
    if v is None:
        return default
    try:
        return float(v)
    except (ValueError, TypeError):
        return default

def safe_int(v, default=0):
    if v is None:
        return default
    try:
        return int(v)
    except (ValueError, TypeError):
        return default

def insert_clickhouse_batch(rows):
    if not rows:
        return
    q = "INSERT INTO football_lineup_fact FORMAT JSONEachRow"
    url = f"{CLICKHOUSE_URL}&query={urllib.parse.quote(q)}"
    data = ("\n".join(json.dumps(r, ensure_ascii=False) for r in rows) + "\n").encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=60) as resp:
        if resp.status != 200:
            raise RuntimeError(f"ClickHouse insert failed: HTTP {resp.status} {resp.read().decode('utf-8')}")

def main():
    print(f"[{datetime.datetime.now()}] Connecting to PostgreSQL {PG_HOST}...", flush=True)
    conn = psycopg2.connect(
        host=PG_HOST,
        port=PG_PORT,
        user=PG_USER,
        password=PG_PASS,
        dbname="igaming_sofascore"
    )
    cur = conn.cursor(name="lineup_cursor")
    query = """
        SELECT event_id, mdm_match_id, mdm_home_team_id, mdm_away_team_id, 
               home_team, away_team, lineups_json, captured_at
        FROM sofascore_raw_events
        WHERE sport = 'football' AND lineups_json LIKE '%players%'
        ORDER BY event_id;
    """
    cur.itersize = 1000
    cur.execute(query)

    print(f"[{datetime.datetime.now()}] Streaming football lineups into ClickHouse...", flush=True)
    batch = []
    total_matches = 0
    total_lineups_inserted = 0
    now_str = datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d %H:%M:%S")

    for row in cur:
        total_matches += 1
        event_id, mdm_match_id, mdm_home_team_id, mdm_away_team_id, home_team, away_team, lineups_raw, captured_at = row
        
        match_id = safe_int(mdm_match_id, event_id)
        if match_id == 0:
            match_id = event_id

        try:
            lineup_data = json.loads(lineups_raw)
        except Exception:
            continue

        if not isinstance(lineup_data, dict):
            continue

        is_confirmed = 1 if lineup_data.get("confirmed") else 0
        all_missing = lineup_data.get("missingPlayers") or []

        # Process home (1) and away (0)
        sides = [
            ("home", 1, mdm_home_team_id, home_team),
            ("away", 0, mdm_away_team_id, away_team)
        ]

        for side_name, is_home, team_id_raw, team_name in sides:
            side_data = lineup_data.get(side_name)
            if not isinstance(side_data, dict):
                continue

            formation = side_data.get("formation") or "Unknown"
            players = side_data.get("players") or []
            if not players:
                continue

            # Resolve team_id
            team_id = safe_int(team_id_raw)
            if team_id == 0:
                first_player_team_id = side_data.get("players", [{}])[0].get("teamId")
                team_id = safe_int(first_player_team_id, 0)
            if team_id == 0:
                team_id = hash(team_name or side_name) & 0x7FFFFFFF

            starters = []
            bench = []
            for p in players:
                if not isinstance(p, dict):
                    continue
                if p.get("substitute"):
                    bench.append(p)
                else:
                    starters.append(p)

            # 1. avg_starter_rating
            starter_ratings = []
            for p in starters:
                stats = p.get("statistics") or {}
                r = stats.get("rating")
                if r is None and isinstance(stats.get("ratingVersions"), dict):
                    r = stats["ratingVersions"].get("original")
                if r is not None:
                    fr = safe_float(r)
                    if 3.0 <= fr <= 10.0:
                        starter_ratings.append(fr)
            avg_starter_rating = round(sum(starter_ratings) / len(starter_ratings), 2) if starter_ratings else 0.0

            # 2. avg_starter_height_cm
            starter_heights = []
            for p in starters:
                pl = p.get("player") or {}
                h = pl.get("height")
                if h is not None:
                    fh = safe_float(h)
                    if 150 <= fh <= 220:
                        starter_heights.append(fh)
            avg_starter_height_cm = round(sum(starter_heights) / len(starter_heights), 1) if starter_heights else 0.0

            # 3. squad_market_value_eur
            total_mv = 0
            for p in players:
                pl = p.get("player") or {}
                mv = pl.get("proposedMarketValue") or pl.get("marketValue")
                if mv is not None:
                    total_mv += safe_int(mv, 0)
            squad_market_value_eur = total_mv

            # 4. bench_attacking_depth_rating
            bench_attack_ratings = []
            for p in bench:
                pos = p.get("position")
                if pos in ("F", "M"):
                    stats = p.get("statistics") or {}
                    r = stats.get("rating")
                    if r is not None:
                        fr = safe_float(r)
                        if 3.0 <= fr <= 10.0:
                            bench_attack_ratings.append(fr)
            if bench_attack_ratings:
                bench_attacking_depth_rating = round(sum(bench_attack_ratings) / len(bench_attack_ratings), 2)
            else:
                bench_attacking_depth_rating = avg_starter_rating if avg_starter_rating > 0.0 else 0.0

            # 5. starters_json & bench_json
            def format_players(pl_list):
                res = []
                for p in pl_list:
                    pl = p.get("player") or {}
                    stats = p.get("statistics") or {}
                    r = stats.get("rating")
                    res.append({
                        "id": pl.get("id"),
                        "name": pl.get("name") or pl.get("shortName"),
                        "pos": p.get("position"),
                        "shirt": p.get("shirtNumber") or p.get("jerseyNumber"),
                        "rating": safe_float(r, 0.0),
                        "height": safe_int(pl.get("height"), 0)
                    })
                return json.dumps(res, ensure_ascii=False)

            starters_json = format_players(starters)
            bench_json = format_players(bench)

            # 6. missing_players_json
            team_missing = []
            for mp in all_missing:
                if not isinstance(mp, dict):
                    continue
                # Missing player team matching
                mp_team_id = mp.get("teamId") or (mp.get("team") or {}).get("id")
                if mp_team_id and team_id and safe_int(mp_team_id) == team_id:
                    team_missing.append(mp)
                elif not mp_team_id:
                    team_missing.append(mp)
            missing_players_json = json.dumps(team_missing, ensure_ascii=False)

            row_record = {
                "mdm_match_id": match_id,
                "team_id": team_id,
                "is_home": is_home,
                "formation": str(formation)[:32],
                "is_confirmed": is_confirmed,
                "avg_starter_rating": avg_starter_rating,
                "avg_starter_height_cm": avg_starter_height_cm,
                "squad_market_value_eur": squad_market_value_eur,
                "bench_attacking_depth_rating": bench_attacking_depth_rating,
                "starters_json": starters_json,
                "bench_json": bench_json,
                "missing_players_json": missing_players_json,
                "updated_at": now_str
            }
            batch.append(row_record)

            if len(batch) >= 2000:
                insert_clickhouse_batch(batch)
                total_lineups_inserted += len(batch)
                print(f"[{datetime.datetime.now()}] Ingested {total_lineups_inserted} lineup rows (from {total_matches} matches)...", flush=True)
                batch = []

    if batch:
        insert_clickhouse_batch(batch)
        total_lineups_inserted += len(batch)

    cur.close()
    conn.close()

    print(f"[{datetime.datetime.now()}] Done! Total matches processed: {total_matches}, Total lineups ingested: {total_lineups_inserted}", flush=True)

    # Verify ClickHouse count
    url_cnt = f"{CLICKHOUSE_URL}&query=SELECT+count()+FROM+football_lineup_fact"
    with urllib.request.urlopen(url_cnt, timeout=10) as resp:
        print("Final ClickHouse football_lineup_fact count:", resp.read().decode("utf-8").strip(), flush=True)

if __name__ == "__main__":
    main()
