#!/usr/bin/env python3
"""
SmartBet.guru — Ingestion of Football Shot Facts into ClickHouse DDS `igaming_dds.football_shot_fact`
Crawls Understat shot maps for top leagues (EPL, La Liga, Serie A, Bundesliga, Ligue 1, RFPL).
Computes pitch Euclidean distance and subtended goal angles.
"""

import requests
import psycopg2
import json
import math
import datetime
import urllib.request
import urllib.parse
import concurrent.futures
import time
import sys

CLICKHOUSE_URL = "http://clickhouse.igaming-analytics.svc.cluster.local:8123/?database=igaming_dds&user=default&password=clickhouse"
PG_HOST = "10.108.82.251"
PG_PORT = 5432
PG_USER = "postgres"
PG_PASS = "postgres"

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "X-Requested-With": "XMLHttpRequest"
}

LEAGUES = ["EPL", "La_liga", "Bundesliga", "Serie_A", "Ligue_1", "RFPL"]
SEASONS = ["2024", "2023"]

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

def compute_shot_geometry(x, y):
    """
    x, y in [0, 1] pitch coordinates.
    Goal center is at (1.0, 0.5). Pitch is 105m x 68m.
    Goal width is 7.32m.
    """
    dx = (1.0 - x) * 105.0
    dy = (y - 0.5) * 68.0
    distance = math.sqrt(dx * dx + dy * dy)

    # Subtended angle by goal line posts (y = -3.66 and y = +3.66 at x=0 relative to goal)
    # Vector from ball to post 1: (-dx, -3.66 - dy)
    # Vector from ball to post 2: (-dx, 3.66 - dy)
    v1_x, v1_y = -dx, -3.66 - dy
    v2_x, v2_y = -dx, 3.66 - dy
    len1 = math.sqrt(v1_x * v1_x + v1_y * v1_y)
    len2 = math.sqrt(v2_x * v2_x + v2_y * v2_y)

    if len1 > 0.001 and len2 > 0.001:
        cos_theta = (v1_x * v2_x + v1_y * v2_y) / (len1 * len2)
        cos_theta = max(-1.0, min(1.0, cos_theta))
        angle = math.acos(cos_theta)
    else:
        angle = math.pi

    return round(distance, 2), round(angle, 3)

def load_mdm_team_map():
    print(f"[{datetime.datetime.now()}] Loading MDM team map for Understat (source 166)...", flush=True)
    conn = psycopg2.connect(
        host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_mdm"
    )
    cur = conn.cursor()
    cur.execute("SELECT lower(raw_name), canonical_team_id FROM mdm_team_ingestion_map WHERE external_system_id = 166;")
    team_map = {}
    for r in cur.fetchall():
        if r[0] and r[1]:
            team_map[r[0].strip()] = r[1]
    cur.close()
    conn.close()
    print(f"[{datetime.datetime.now()}] Loaded {len(team_map)} team mappings.", flush=True)
    return team_map

def insert_clickhouse_shots(rows):
    if not rows:
        return
    q = "INSERT INTO football_shot_fact FORMAT JSONEachRow"
    url = f"{CLICKHOUSE_URL}&query={urllib.parse.quote(q)}"
    data = ("\n".join(json.dumps(r, ensure_ascii=False) for r in rows) + "\n").encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=60) as resp:
        if resp.status != 200:
            raise RuntimeError(f"ClickHouse insert failed: HTTP {resp.status} {resp.read().decode('utf-8')}")

def fetch_match_shots(match_info):
    mid = match_info["id"]
    url = f"https://understat.com/getMatchData/{mid}"
    try:
        r = requests.get(url, headers=HEADERS, timeout=12)
        if r.status_code == 200:
            d = r.json()
            shots_data = d.get("shots", {})
            home_shots = shots_data.get("h", [])
            away_shots = shots_data.get("a", [])
            return match_info, home_shots + away_shots
    except Exception as e:
        # Retry once
        try:
            time.sleep(0.5)
            r = requests.get(url, headers=HEADERS, timeout=12)
            if r.status_code == 200:
                d = r.json()
                shots_data = d.get("shots", {})
                return match_info, shots_data.get("h", []) + shots_data.get("a", [])
        except Exception:
            pass
    return match_info, []

def main():
    team_map = load_mdm_team_map()
    total_shots_inserted = 0
    total_matches_processed = 0

    shot_batch = []

    for league in LEAGUES:
        for season in SEASONS:
            print(f"\n[{datetime.datetime.now()}] Crawling Understat league '{league}' season {season}...", flush=True)
            url_league = f"https://understat.com/getLeagueData/{league}/{season}"
            try:
                r = requests.get(url_league, headers=HEADERS, timeout=15)
                if r.status_code != 200:
                    print(f"  Warning: failed to fetch {url_league}: {r.status_code}")
                    continue
                league_data = r.json()
                dates = league_data.get("dates", [])
                completed = [m for m in dates if m.get("isResult")]
                print(f"  Found {len(completed)} completed matches in {league} {season}.", flush=True)
            except Exception as e:
                print(f"  Error fetching league {league} {season}: {e}")
                continue

            # Process matches with thread pool
            with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
                for match_info, raw_shots in executor.map(fetch_match_shots, completed):
                    total_matches_processed += 1
                    for s in raw_shots:
                        shot_id = safe_int(s.get("id"))
                        if shot_id == 0:
                            continue

                        h_a = s.get("h_a")
                        h_team = s.get("h_team") or (match_info.get("h") or {}).get("title") or "Home"
                        a_team = s.get("a_team") or (match_info.get("a") or {}).get("title") or "Away"
                        team_name = h_team if h_a == "h" else a_team

                        team_id = team_map.get(team_name.lower())
                        if not team_id:
                            team_id = hash(team_name) & 0x7FFFFFFF

                        match_id = safe_int(s.get("match_id"), safe_int(match_info.get("id")))
                        player_id_val = safe_int(s.get("player_id"))
                        player_id = player_id_val if player_id_val > 0 else None
                        player_name = s.get("player") or "Unknown"
                        minute = safe_int(s.get("minute"), 0)
                        coord_x = safe_float(s.get("X"), 0.0)
                        coord_y = safe_float(s.get("Y"), 0.0)

                        dist, angle = compute_shot_geometry(coord_x, coord_y)
                        xg = safe_float(s.get("xG"), 0.0)
                        result = s.get("result") or "MissedShots"
                        situation = s.get("situation") or "OpenPlay"
                        shot_type = s.get("shotType") or "RightFoot"

                        created_at = s.get("date") or match_info.get("datetime")
                        if not created_at:
                            created_at = datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d %H:%M:%S")

                        shot_row = {
                            "id": shot_id,
                            "mdm_match_id": match_id,
                            "team_id": team_id,
                            "player_id": player_id,
                            "player_name": str(player_name)[:128],
                            "minute": minute,
                            "coord_x": coord_x,
                            "coord_y": coord_y,
                            "distance_to_goal": dist,
                            "shot_angle": angle,
                            "xg": xg,
                            "result": str(result)[:32],
                            "situation": str(situation)[:64],
                            "shot_type": str(shot_type)[:64],
                            "created_at": str(created_at)[:19]
                        }
                        shot_batch.append(shot_row)

                        if len(shot_batch) >= 2000:
                            insert_clickhouse_shots(shot_batch)
                            total_shots_inserted += len(shot_batch)
                            print(f"[{datetime.datetime.now()}] Ingested {total_shots_inserted} shots into ClickHouse...", flush=True)
                            shot_batch = []

            # Flush after each league
            if shot_batch:
                insert_clickhouse_shots(shot_batch)
                total_shots_inserted += len(shot_batch)
                print(f"[{datetime.datetime.now()}] Ingested {total_shots_inserted} shots after {league} {season}...", flush=True)
                shot_batch = []

    if shot_batch:
        insert_clickhouse_shots(shot_batch)
        total_shots_inserted += len(shot_batch)

    print(f"\n[{datetime.datetime.now()}] All done! Total matches: {total_matches_processed}, Total shots ingested: {total_shots_inserted}", flush=True)

    # Verify ClickHouse count
    url_cnt = f"{CLICKHOUSE_URL}&query=SELECT+count()+FROM+football_shot_fact"
    with urllib.request.urlopen(url_cnt, timeout=10) as resp:
        print("Final ClickHouse football_shot_fact count:", resp.read().decode("utf-8").strip(), flush=True)

if __name__ == "__main__":
    main()
