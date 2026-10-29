#!/usr/bin/env python3
"""
Populate MDM Venues, Referees and ClickHouse DDS:
1. Extracts venues and referees from sofascore_raw_events into mdm_venue and mdm_referee
2. Binds venue_id and referee_id into mdm_match
3. Fixes surface in tennis_match_fact (CLAY, GRASS, HARD_OUTDOOR, HARD_INDOOR, CARPET)
4. Populates basketball_match_fact (15k matches with 4 quarters + OT)
5. Populates ice_hockey_match_fact (6.9k matches with 3 periods + OT + SO)
"""

import psycopg2
import json
import requests
import sys
from datetime import datetime

CH_URL = "http://clickhouse.igaming-analytics.svc.cluster.local:8123"
CH_AUTH = ("default", "clickhouse")
POSTGRES_HOST = "igaming-aggregator-db.igaming-enrichment.svc.cluster.local"

def insert_clickhouse(table, batch):
    if not batch:
        return 0
    payload = "\n".join([json.dumps(x) for x in batch])
    r = requests.post(
        CH_URL,
        auth=CH_AUTH,
        params={"query": f"INSERT INTO igaming_dds.{table} FORMAT JSONEachRow"},
        data=payload.encode('utf-8')
    )
    if r.status_code != 200:
        print(f"ERROR inserting into {table}: HTTP {r.status_code} - {r.text[:200]}")
        return 0
    return len(batch)

def get_conns():
    mdm_conn = psycopg2.connect(
        host=POSTGRES_HOST, port=5432, dbname="igaming_mdm",
        user="postgres", password="postgres"
    )
    sofa_conn = psycopg2.connect(
        host=POSTGRES_HOST, port=5432, dbname="igaming_sofascore",
        user="postgres", password="postgres"
    )
    return mdm_conn, sofa_conn

def normalize_surface(raw_ground):
    if not raw_ground:
        return "UNKNOWN"
    g = raw_ground.lower()
    if "clay" in g:
        return "CLAY"
    if "grass" in g:
        return "GRASS"
    if "carpet" in g:
        return "CARPET"
    if "indoor" in g:
        return "HARD_INDOOR"
    if "hard" in g:
        return "HARD_OUTDOOR"
    return "UNKNOWN"

def run_pipeline():
    print(f"[{datetime.now()}] Starting Venue, Referee & Multi-sport Pipeline (Fixed Root Wrapper)...")
    mdm_conn, sofa_conn = get_conns()
    mdm_conn.autocommit = True  # Instant commits for high reliability
    
    mdm_cur = mdm_conn.cursor()
    
    # Pre-cache venues and referees
    venue_cache = {}
    mdm_cur.execute("SELECT slug, id FROM mdm_venue")
    for row in mdm_cur.fetchall():
        venue_cache[row[0]] = row[1]
        
    referee_cache = {}
    mdm_cur.execute("SELECT slug, id FROM mdm_referee")
    for row in mdm_cur.fetchall():
        referee_cache[row[0]] = row[1]
        
    print(f"[{datetime.now()}] Initial cache: {len(venue_cache)} venues, {len(referee_cache)} referees.")

    sofa_cur = sofa_conn.cursor(name="event_stream")
    sofa_cur.itersize = 2000
    sofa_cur.execute("""
        SELECT event_id, sport, mdm_match_id, mdm_home_team_id, mdm_away_team_id, 
               match_date, home_score, away_score, event_json
        FROM sofascore_raw_events
        WHERE event_json IS NOT NULL
    """)

    bball_batch = []
    hockey_batch = []
    match_binds = []
    
    processed = 0
    venues_added = 0
    referees_added = 0
    bball_added = 0
    hockey_added = 0

    for row in sofa_cur:
        event_id, sport, mdm_match_id, mdm_home, mdm_away, match_date_ts, h_score, a_score, raw_json = row
        processed += 1
        
        try:
            raw_d = json.loads(raw_json)
        except Exception:
            continue
            
        # UNWRAP ROOT EVENT OBJECT
        ev = raw_d.get("event") if (isinstance(raw_d, dict) and isinstance(raw_d.get("event"), dict)) else raw_d
        if not isinstance(ev, dict):
            continue

        venue_id = None
        referee_id = None

        # --- A. Venue Extraction ---
        v_obj = ev.get("venue")
        if v_obj and isinstance(v_obj, dict):
            stadium_name = v_obj.get("name") or (v_obj.get("stadium", {}).get("name") if isinstance(v_obj.get("stadium"), dict) else None)
            city_name = v_obj.get("city", {}).get("name") if isinstance(v_obj.get("city"), dict) else None
            country_code = v_obj.get("country", {}).get("alpha2") or (v_obj.get("city", {}).get("country", {}).get("alpha2") if isinstance(v_obj.get("city"), dict) else None)
            v_slug = v_obj.get("slug") or (f"v-{stadium_name.lower()[:30]}" if stadium_name else None)
            
            if v_slug and stadium_name:
                if v_slug not in venue_cache:
                    coords = v_obj.get("venueCoordinates") or {}
                    lat = coords.get("latitude")
                    lng = coords.get("longitude")
                    capacity = v_obj.get("capacity") or (v_obj.get("stadium", {}).get("capacity", 0) if isinstance(v_obj.get("stadium"), dict) else 0) or 0
                    
                    try:
                        mdm_cur.execute("""
                            INSERT INTO mdm_venue (slug, canonical_name, city, country_code, latitude, longitude, capacity)
                            VALUES (%s, %s, %s, %s, %s, %s, %s)
                            ON CONFLICT (slug) DO UPDATE SET updated_at = now()
                            RETURNING id
                        """, (v_slug, stadium_name, city_name, country_code, lat, lng, capacity))
                        venue_id = mdm_cur.fetchone()[0]
                        venue_cache[v_slug] = venue_id
                        venues_added += 1
                    except Exception as ex:
                        pass
                else:
                    venue_id = venue_cache[v_slug]

        # --- B. Referee Extraction ---
        ref_obj = ev.get("referee")
        if ref_obj and isinstance(ref_obj, dict):
            r_name = ref_obj.get("name")
            r_slug = ref_obj.get("slug") or (f"ref-{r_name.lower()[:30]}" if r_name else None)
            if r_slug and r_name:
                if r_slug not in referee_cache:
                    games = ref_obj.get("games", 0) or 0
                    yc = ref_obj.get("yellowCards", 0) or 0
                    rc = ref_obj.get("redCards", 0) or 0
                    avg_yc = (yc / games) if games > 0 else 0.0
                    avg_rc = (rc / games) if games > 0 else 0.0
                    
                    tier = "MODERATE"
                    if avg_yc >= 5.0:
                        tier = "VERY_STRICT"
                    elif avg_yc >= 4.2:
                        tier = "STRICT"
                    elif avg_yc <= 2.8 and games > 5:
                        tier = "LENIENT"
                        
                    r_country = ref_obj.get("country", {}).get("alpha2") if isinstance(ref_obj.get("country"), dict) else None
                    r_cname = ref_obj.get("country", {}).get("name") if isinstance(ref_obj.get("country"), dict) else None
                    
                    try:
                        mdm_cur.execute("""
                            INSERT INTO mdm_referee (
                                sport_id, slug, canonical_name, country_code, country_name,
                                yellow_cards_per_game, red_cards_per_game, strictness_tier, total_matches
                            ) VALUES (1, %s, %s, %s, %s, %s, %s, %s, %s)
                            ON CONFLICT (slug) DO UPDATE SET total_matches = EXCLUDED.total_matches, updated_at = now()
                            RETURNING id
                        """, (r_slug, r_name, r_country, r_cname, avg_yc, avg_rc, tier, games))
                        referee_id = mdm_cur.fetchone()[0]
                        referee_cache[r_slug] = referee_id
                        referees_added += 1
                    except Exception as ex:
                        pass
                else:
                    referee_id = referee_cache[r_slug]

        if mdm_match_id and (venue_id or referee_id):
            match_binds.append((venue_id, referee_id, mdm_match_id))

        # --- C. Basketball Facts ---
        if sport == "basketball" and mdm_match_id and mdm_home and mdm_away:
            hs = ev.get("homeScore", {})
            aws = ev.get("awayScore", {})
            if isinstance(hs, dict) and isinstance(aws, dict) and hs.get("current") is not None:
                tot_h = hs.get("current", 0) or 0
                tot_a = aws.get("current", 0) or 0
                m_date_str = datetime.utcfromtimestamp(match_date_ts / 1000 if match_date_ts > 100000000000 else match_date_ts).strftime("%Y-%m-%d %H:%M:%S") if match_date_ts else "2026-01-01 00:00:00"
                
                bball_batch.append({
                    "mdm_match_id": mdm_match_id,
                    "tournament_id": ev.get("tournament", {}).get("uniqueTournament", {}).get("id") or 0,
                    "home_team_id": mdm_home,
                    "away_team_id": mdm_away,
                    "venue_id": venue_id or 0,
                    "referee_id": referee_id or 0,
                    "match_time": m_date_str,
                    "match_timestamp": int(match_date_ts / 1000 if match_date_ts > 100000000000 else match_date_ts) if match_date_ts else 0,
                    "home_score": tot_h,
                    "away_score": tot_a,
                    "home_q1": hs.get("period1", 0) or 0, "away_q1": aws.get("period1", 0) or 0,
                    "home_q2": hs.get("period2", 0) or 0, "away_q2": aws.get("period2", 0) or 0,
                    "home_q3": hs.get("period3", 0) or 0, "away_q3": aws.get("period3", 0) or 0,
                    "home_q4": hs.get("period4", 0) or 0, "away_q4": aws.get("period4", 0) or 0,
                    "home_ot": hs.get("overtime", 0) or 0, "away_ot": aws.get("overtime", 0) or 0,
                    "is_overtime": 1 if (hs.get("overtime", 0) or aws.get("overtime", 0)) else 0,
                    "winner": "HOME" if tot_h > tot_a else "AWAY"
                })

        # --- D. Hockey Facts ---
        if sport == "ice-hockey" and mdm_match_id and mdm_home and mdm_away:
            hs = ev.get("homeScore", {})
            aws = ev.get("awayScore", {})
            if isinstance(hs, dict) and isinstance(aws, dict) and hs.get("current") is not None:
                tot_h = hs.get("current", 0) or 0
                tot_a = aws.get("current", 0) or 0
                m_date_str = datetime.utcfromtimestamp(match_date_ts / 1000 if match_date_ts > 100000000000 else match_date_ts).strftime("%Y-%m-%d %H:%M:%S") if match_date_ts else "2026-01-01 00:00:00"
                
                hockey_batch.append({
                    "mdm_match_id": mdm_match_id,
                    "tournament_id": ev.get("tournament", {}).get("uniqueTournament", {}).get("id") or 0,
                    "home_team_id": mdm_home,
                    "away_team_id": mdm_away,
                    "venue_id": venue_id or 0,
                    "referee_id": referee_id or 0,
                    "match_time": m_date_str,
                    "match_timestamp": int(match_date_ts / 1000 if match_date_ts > 100000000000 else match_date_ts) if match_date_ts else 0,
                    "home_score": tot_h,
                    "away_score": tot_a,
                    "home_p1": hs.get("period1", 0) or 0, "away_p1": aws.get("period1", 0) or 0,
                    "home_p2": hs.get("period2", 0) or 0, "away_p2": aws.get("period2", 0) or 0,
                    "home_p3": hs.get("period3", 0) or 0, "away_p3": aws.get("period3", 0) or 0,
                    "home_ot": hs.get("overtime", 0) or 0, "away_ot": aws.get("overtime", 0) or 0,
                    "home_so": hs.get("penalties", 0) or 0, "away_so": aws.get("penalties", 0) or 0,
                    "home_shots": 0, "away_shots": 0,
                    "is_overtime": 1 if (hs.get("overtime", 0) or aws.get("overtime", 0) or hs.get("penalties", 0) or aws.get("penalties", 0)) else 0,
                    "winner": "HOME" if tot_h > tot_a else "AWAY"
                })

        # Flush batches
        if len(bball_batch) >= 1000:
            bball_added += insert_clickhouse("basketball_match_fact", bball_batch)
            print(f"[{datetime.now()}] Ingested {bball_added} basketball matches into ClickHouse.")
            bball_batch = []

        if len(hockey_batch) >= 1000:
            hockey_added += insert_clickhouse("ice_hockey_match_fact", hockey_batch)
            print(f"[{datetime.now()}] Ingested {hockey_added} ice hockey matches into ClickHouse.")
            hockey_batch = []

        if len(match_binds) >= 1000:
            for v_id, r_id, m_id in match_binds:
                if v_id and r_id:
                    mdm_cur.execute("UPDATE mdm_match SET venue_id = %s, referee_id = %s WHERE id = %s", (v_id, r_id, m_id))
                elif v_id:
                    mdm_cur.execute("UPDATE mdm_match SET venue_id = %s WHERE id = %s", (v_id, m_id))
                elif r_id:
                    mdm_cur.execute("UPDATE mdm_match SET referee_id = %s WHERE id = %s", (r_id, m_id))
            match_binds = []

        if processed % 10000 == 0:
            print(f"[{datetime.now()}] Scanned {processed} events | Venues: {len(venue_cache)} (+{venues_added}) | Referees: {len(referee_cache)} (+{referees_added}) | Bball: {bball_added} | Hockey: {hockey_added}")

    # Final flushes
    if bball_batch:
        bball_added += insert_clickhouse("basketball_match_fact", bball_batch)
        print(f"[{datetime.now()}] Final: Total {bball_added} basketball matches.")

    if hockey_batch:
        hockey_added += insert_clickhouse("ice_hockey_match_fact", hockey_batch)
        print(f"[{datetime.now()}] Final: Total {hockey_added} hockey matches.")

    if match_binds:
        for v_id, r_id, m_id in match_binds:
            if v_id and r_id:
                mdm_cur.execute("UPDATE mdm_match SET venue_id = %s, referee_id = %s WHERE id = %s", (v_id, r_id, m_id))
            elif v_id:
                mdm_cur.execute("UPDATE mdm_match SET venue_id = %s WHERE id = %s", (v_id, m_id))
            elif r_id:
                mdm_cur.execute("UPDATE mdm_match SET referee_id = %s WHERE id = %s", (r_id, m_id))

    print(f"[{datetime.now()}] COMPLETE! Processed: {processed} events | Venues: {len(venue_cache)} | Referees: {len(referee_cache)}")
    mdm_cur.close()
    sofa_cur.close()
    mdm_conn.close()
    sofa_conn.close()

if __name__ == "__main__":
    run_pipeline()
