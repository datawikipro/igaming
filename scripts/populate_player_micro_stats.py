#!/usr/bin/env python3
"""
Populates individual player micro-stats (fouls, was_fouled, tackles, duels, minutes)
into ClickHouse `igaming_dds.football_lineup_player_fact`.
"""
import psycopg2
import json
import urllib.request
import urllib.parse
import datetime
import time

CLICKHOUSE_URL = "http://clickhouse.igaming-analytics.svc.cluster.local:8123/?database=igaming_dds&user=default&password=clickhouse"
PG_HOST = "10.108.82.251"
PG_PORT = 5432
PG_USER = "postgres"
PG_PASS = "postgres"

def safe_int(v, default=0):
    if v is None:
        return default
    try:
        return int(v)
    except (ValueError, TypeError):
        return default

def safe_float(v, default=0.0):
    if v is None:
        return default
    try:
        return float(v)
    except (ValueError, TypeError):
        return default

def insert_clickhouse_batch(rows):
    if not rows:
        return
    q = "INSERT INTO football_lineup_player_fact FORMAT JSONEachRow"
    url = f"{CLICKHOUSE_URL}&query={urllib.parse.quote(q)}"
    data = ("\n".join(json.dumps(r, ensure_ascii=False) for r in rows) + "\n").encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=60) as resp:
        if resp.status != 200:
            raise RuntimeError(f"ClickHouse insert failed: HTTP {resp.status} {resp.read().decode('utf-8')}")

def main():
    print(f"[{datetime.datetime.now()}] Connecting to Postgres and ClickHouse...", flush=True)
    conn_sofa = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_sofascore")
    conn_mdm = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_mdm")

    mdm_cur = conn_mdm.cursor()
    print("Loading sofascore_id -> mdm_player_id mapping...", flush=True)
    mdm_cur.execute("SELECT sofascore_id, id FROM mdm_player WHERE sofascore_id IS NOT NULL;")
    sofa_to_mdm = {r[0]: r[1] for r in mdm_cur.fetchall()}
    print(f"Loaded {len(sofa_to_mdm)} players in cache.", flush=True)
    mdm_cur.close()
    conn_mdm.close()

    sofa_cur = conn_sofa.cursor(name="stream_micro_stats")
    sofa_cur.itersize = 500
    query = """
        SELECT event_id, mdm_match_id, mdm_home_team_id, mdm_away_team_id, lineups_json
        FROM sofascore_raw_events
        WHERE sport = 'football' AND lineups_json LIKE '%"fouls"%'
        ORDER BY event_id;
    """
    sofa_cur.execute(query)

    buffer = []
    total_matches = 0
    total_players = 0
    now_str = datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d %H:%M:%S")

    for row in sofa_cur:
        total_matches += 1
        event_id, mdm_match_id, mdm_home_team_id, mdm_away_team_id, lineups_raw = row
        match_id = safe_int(mdm_match_id, event_id)
        if match_id == 0:
            match_id = event_id

        try:
            lineup_data = json.loads(lineups_raw)
        except Exception:
            continue

        if not isinstance(lineup_data, dict):
            continue

        sides = [
            ("home", 1, safe_int(mdm_home_team_id)),
            ("away", 0, safe_int(mdm_away_team_id))
        ]

        for side_name, is_home, team_id in sides:
            side_data = lineup_data.get(side_name) or {}
            players = side_data.get("players") or []
            if not team_id:
                first_pl_team = players[0].get("teamId") if players else None
                team_id = safe_int(first_pl_team, event_id)

            for p in players:
                if not isinstance(p, dict):
                    continue
                pl_dict = p.get("player") or {}
                sofa_pl_id = safe_int(pl_dict.get("id"))
                if not sofa_pl_id:
                    continue

                mdm_pl_id = sofa_to_mdm.get(sofa_pl_id, sofa_pl_id)

                stats = p.get("statistics") or {}
                fouls = safe_int(stats.get("fouls"), 0)
                was_fouled = safe_int(stats.get("wasFouled"), 0)
                tackles = safe_int(stats.get("totalTackle"), 0)
                duels_won = safe_int(stats.get("duelWon"), 0)
                duels_lost = safe_int(stats.get("duelLost"), 0)
                minutes_played = safe_int(stats.get("minutesPlayed"), 0)

                # Rating
                rating = stats.get("rating")
                if rating is None and isinstance(stats.get("ratingVersions"), dict):
                    rating = stats["ratingVersions"].get("original")
                frating = safe_float(rating, 0.0)

                is_sub = bool(p.get("substitute"))
                is_starter = 0 if is_sub else 1
                is_bench = 1 if is_sub else 0

                position = p.get("position") or pl_dict.get("position") or ""
                shirt_number = safe_int(p.get("jerseyNumber") or p.get("shirtNumber"), 0)
                height_cm = safe_float(pl_dict.get("height"), 0.0)

                buffer.append({
                    "mdm_match_id": match_id,
                    "mdm_team_id": team_id,
                    "mdm_player_id": mdm_pl_id,
                    "player_sofascore_id": sofa_pl_id,
                    "position": str(position)[:16],
                    "shirt_number": min(255, shirt_number),
                    "match_rating": round(frating, 2),
                    "height_cm": round(height_cm, 1),
                    "age_at_match": 0,
                    "is_starter": is_starter,
                    "is_bench": is_bench,
                    "is_missing": 0,
                    "missing_reason": "",
                    "is_home": is_home,
                    "updated_at": now_str,
                    "fouls": min(255, fouls),
                    "was_fouled": min(255, was_fouled),
                    "tackles": min(255, tackles),
                    "duels_won": min(255, duels_won),
                    "duels_lost": min(255, duels_lost),
                    "minutes_played": min(255, minutes_played)
                })

        if len(buffer) >= 5000:
            insert_clickhouse_batch(buffer)
            total_players += len(buffer)
            buffer = []
            print(f"Processed {total_matches} matches | Ingested {total_players} player micro-stat rows...", flush=True)

    if buffer:
        insert_clickhouse_batch(buffer)
        total_players += len(buffer)

    sofa_cur.close()
    conn_sofa.close()
    print(f"COMPLETE! Ingested {total_players} player micro-stat rows across {total_matches} matches.", flush=True)

if __name__ == "__main__":
    main()
