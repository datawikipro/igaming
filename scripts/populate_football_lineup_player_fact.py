#!/usr/bin/env python3
"""
SmartBet.guru — 3NF Normalization of Football Lineup Players
Populates `igaming_dds.football_lineup_player_fact` (1 row = 1 player).
Upserts players into PostgreSQL `igaming_mdm.mdm_player` to guarantee FK integrity.
"""

import psycopg2
import requests
import json
import datetime
import urllib.parse
import time
import sys

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

def calc_age(birth_date, match_dt):
    if not birth_date or not match_dt:
        return 0
    try:
        m_date = match_dt.date() if isinstance(match_dt, (datetime.datetime, datetime.date)) else match_dt
        days = (m_date - birth_date).days
        age = days // 365
        return age if 12 <= age <= 65 else 0
    except Exception:
        return 0

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
    print(f"[{datetime.datetime.now()}] Connecting to PostgreSQL...", flush=True)
    conn_sofa = psycopg2.connect(
        host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_sofascore"
    )
    conn_mdm = psycopg2.connect(
        host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_mdm"
    )
    conn_mdm.autocommit = False

    mdm_cur = conn_mdm.cursor()
    print(f"[{datetime.datetime.now()}] Pre-loading existing sofascore_id -> mdm_player_id cache...", flush=True)
    mdm_cur.execute("SELECT sofascore_id, id FROM mdm_player WHERE sofascore_id IS NOT NULL;")
    sofa_to_mdm = {r[0]: r[1] for r in mdm_cur.fetchall()}
    print(f"[{datetime.datetime.now()}] Loaded {len(sofa_to_mdm)} existing players in cache.", flush=True)

    upsert_sql = """
        INSERT INTO mdm_player (sport_id, slug, sofascore_id, common_name, primary_position, birth_date, gender, created_at, updated_at)
        VALUES (1, %s, %s, %s, %s, %s, 'MALE', now(), now())
        ON CONFLICT (sofascore_id) DO UPDATE SET 
            common_name = COALESCE(NULLIF(EXCLUDED.common_name, ''), mdm_player.common_name),
            primary_position = COALESCE(EXCLUDED.primary_position, mdm_player.primary_position),
            updated_at = now()
        RETURNING id;
    """

    def resolve_or_upsert_player(player_dict):
        sofa_id = player_dict.get("id")
        if not sofa_id:
            return None
        sofa_id = int(sofa_id)
        if sofa_id in sofa_to_mdm:
            return sofa_to_mdm[sofa_id]

        name = player_dict.get("name") or player_dict.get("shortName") or f"Player {sofa_id}"
        pos = player_dict.get("position") or ""
        birth_ts = player_dict.get("dateOfBirthTimestamp")
        birth_date = datetime.datetime.fromtimestamp(birth_ts, tz=datetime.timezone.utc).date() if birth_ts else None
        slug = f"p-{sofa_id}"

        try:
            mdm_cur.execute(upsert_sql, (slug, sofa_id, str(name)[:128], str(pos)[:32], birth_date))
            pid = mdm_cur.fetchone()[0]
            sofa_to_mdm[sofa_id] = pid
            return pid
        except Exception as e:
            conn_mdm.rollback()
            # If slug conflict, fallback with unique suffix
            try:
                slug_fallback = f"p-{sofa_id}-{int(time.time())}"
                mdm_cur.execute(upsert_sql, (slug_fallback, sofa_id, str(name)[:128], str(pos)[:32], birth_date))
                pid = mdm_cur.fetchone()[0]
                sofa_to_mdm[sofa_id] = pid
                return pid
            except Exception:
                conn_mdm.rollback()
                return None

    sofa_cur = conn_sofa.cursor(name="sofa_stream_cursor")
    sofa_cur.itersize = 500
    query = """
        SELECT event_id, mdm_match_id, mdm_home_team_id, mdm_away_team_id, match_date, lineups_json, captured_at
        FROM sofascore_raw_events
        WHERE sport = 'football' AND lineups_json IS NOT NULL AND lineups_json LIKE '%players%'
        ORDER BY event_id;
    """
    sofa_cur.execute(query)

    print(f"[{datetime.datetime.now()}] Streaming matches and populating football_lineup_player_fact...", flush=True)

    clickhouse_buffer = []
    total_matches = 0
    total_players_ingested = 0
    new_players_upserted_count = 0
    now_str = datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d %H:%M:%S")

    for row in sofa_cur:
        total_matches += 1
        event_id, mdm_match_id, mdm_home_team_id, mdm_away_team_id, match_date_ts, lineups_raw, captured_at = row
        match_id = safe_int(mdm_match_id, event_id)
        if match_id == 0:
            match_id = event_id

        # Match date
        match_dt = None
        if match_date_ts:
            try:
                match_dt = datetime.datetime.fromtimestamp(match_date_ts, tz=datetime.timezone.utc)
            except Exception:
                match_dt = captured_at
        else:
            match_dt = captured_at

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
                team_id = safe_int(first_pl_team, 0)
            if not team_id:
                team_id = event_id

            for p in players:
                if not isinstance(p, dict):
                    continue
                pl_dict = p.get("player") or {}
                sofa_pl_id = safe_int(pl_dict.get("id"))
                if not sofa_pl_id:
                    continue

                prev_cache_len = len(sofa_to_mdm)
                mdm_pl_id = resolve_or_upsert_player(pl_dict)
                if len(sofa_to_mdm) > prev_cache_len:
                    new_players_upserted_count += 1

                if not mdm_pl_id:
                    continue

                # Rating
                stats = p.get("statistics") or {}
                rating = stats.get("rating")
                if rating is None and isinstance(stats.get("ratingVersions"), dict):
                    rating = stats["ratingVersions"].get("original")
                frating = safe_float(rating, 0.0)
                if not (3.0 <= frating <= 10.0):
                    frating = 0.0

                # Height & Age
                height_cm = safe_float(pl_dict.get("height"), 0.0)
                birth_ts = pl_dict.get("dateOfBirthTimestamp")
                birth_date = datetime.datetime.fromtimestamp(birth_ts, tz=datetime.timezone.utc).date() if birth_ts else None
                age = calc_age(birth_date, match_dt)

                # Status
                is_sub = bool(p.get("substitute"))
                is_starter = 0 if is_sub else 1
                is_bench = 1 if is_sub else 0

                position = p.get("position") or pl_dict.get("position") or ""
                shirt_number = safe_int(p.get("jerseyNumber") or p.get("shirtNumber"), 0)
                if shirt_number > 255:
                    shirt_number = 0

                clickhouse_buffer.append({
                    "mdm_match_id": match_id,
                    "mdm_team_id": team_id,
                    "mdm_player_id": mdm_pl_id,
                    "player_sofascore_id": sofa_pl_id,
                    "position": str(position)[:16],
                    "shirt_number": shirt_number,
                    "match_rating": round(frating, 2),
                    "height_cm": round(height_cm, 1),
                    "age_at_match": age,
                    "is_starter": is_starter,
                    "is_bench": is_bench,
                    "is_missing": 0,
                    "missing_reason": "",
                    "is_home": is_home,
                    "updated_at": now_str
                })

        # Process missing players
        all_missing = lineup_data.get("missingPlayers") or []
        for mp in all_missing:
            if not isinstance(mp, dict):
                continue
            pl_dict = mp.get("player") or {}
            sofa_pl_id = safe_int(pl_dict.get("id"))
            if not sofa_pl_id:
                continue

            mdm_pl_id = resolve_or_upsert_player(pl_dict)
            if not mdm_pl_id:
                continue

            # Determine team
            mp_team_id = safe_int(mp.get("teamId") or (mp.get("team") or {}).get("id"))
            is_home_mp = 1
            if mp_team_id:
                if mp_team_id == safe_int(mdm_away_team_id):
                    is_home_mp = 0
                team_id_mp = mp_team_id
            else:
                team_id_mp = safe_int(mdm_home_team_id, event_id)

            reason_obj = mp.get("reason") or {}
            reason_str = reason_obj.get("short") or reason_obj.get("longName") or mp.get("type") or ""

            height_cm = safe_float(pl_dict.get("height"), 0.0)
            birth_ts = pl_dict.get("dateOfBirthTimestamp")
            birth_date = datetime.datetime.fromtimestamp(birth_ts, tz=datetime.timezone.utc).date() if birth_ts else None
            age = calc_age(birth_date, match_dt)
            position = pl_dict.get("position") or ""

            clickhouse_buffer.append({
                "mdm_match_id": match_id,
                "mdm_team_id": team_id_mp,
                "mdm_player_id": mdm_pl_id,
                "player_sofascore_id": sofa_pl_id,
                "position": str(position)[:16],
                "shirt_number": 0,
                "match_rating": 0.0,
                "height_cm": round(height_cm, 1),
                "age_at_match": age,
                "is_starter": 0,
                "is_bench": 0,
                "is_missing": 1,
                "missing_reason": str(reason_str)[:32],
                "is_home": is_home_mp,
                "updated_at": now_str
            })

        # Flush buffer
        if len(clickhouse_buffer) >= 5000:
            insert_clickhouse_batch(clickhouse_buffer)
            total_players_ingested += len(clickhouse_buffer)
            clickhouse_buffer = []
            conn_mdm.commit()

        if total_matches % 1000 == 0:
            print(f"[{datetime.datetime.now()}] Processed {total_matches} matches | Ingested {total_players_ingested} player rows | New players upserted: {new_players_upserted_count}...", flush=True)

    if clickhouse_buffer:
        insert_clickhouse_batch(clickhouse_buffer)
        total_players_ingested += len(clickhouse_buffer)
        conn_mdm.commit()

    sofa_cur.close()
    conn_sofa.close()
    mdm_cur.close()
    conn_mdm.close()

    print(f"[{datetime.datetime.now()}] COMPLETE! Processed matches: {total_matches}, Total player rows: {total_players_ingested}, New MDM players: {new_players_upserted_count}", flush=True)

    # ClickHouse verification query
    url_cnt = f"{CLICKHOUSE_URL}&query=SELECT+count(),countIf(is_starter=1),countIf(is_bench=1),countIf(is_missing=1),countIf(match_rating>0),count(DISTINCT+mdm_match_id),count(DISTINCT+mdm_player_id)+FROM+football_lineup_player_fact+FORMAT+TabSeparatedWithNames"
    with urllib.request.urlopen(url_cnt, timeout=10) as resp:
        print("ClickHouse Verification:\n", resp.read().decode("utf-8").strip(), flush=True)

if __name__ == "__main__":
    main()
