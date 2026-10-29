#!/usr/bin/env python3
"""
Populate Remaining Sports DDS in ClickHouse:
1. Volleyball (sets s1-s5, total points)
2. Handball (halves h1-h2, total goals)
3. Baseball (runs, hits, errors)
4. Combat / MMA (round, method)
Streams directly from sofascore_raw_events into ClickHouse DDS.
"""

import psycopg2
import json
import requests
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

def run_sports_ingest():
    print(f"[{datetime.now()}] Starting Multi-Sport Ingestion (Volleyball, Handball, Baseball, Combat)...")
    sofa_conn = psycopg2.connect(
        host=POSTGRES_HOST, port=5432, dbname="igaming_sofascore",
        user="postgres", password="postgres"
    )
    sofa_cur = sofa_conn.cursor(name="multi_sport_cursor")
    sofa_cur.itersize = 2000
    
    sofa_cur.execute("""
        SELECT event_id, sport, mdm_match_id, mdm_home_team_id, mdm_away_team_id,
               match_date, home_score, away_score, event_json
        FROM sofascore_raw_events
        WHERE sport IN ('volleyball', 'handball', 'baseball', 'mma')
          AND event_json IS NOT NULL
          AND mdm_match_id IS NOT NULL
    """)

    volleyball_batch = []
    handball_batch = []
    baseball_batch = []
    combat_batch = []
    
    counts = {"volleyball": 0, "handball": 0, "baseball": 0, "combat": 0}
    scanned = 0

    for row in sofa_cur:
        event_id, sport, mdm_match_id, mdm_home, mdm_away, match_date_ts, h_score, a_score, raw_json = row
        scanned += 1
        
        try:
            raw_d = json.loads(raw_json)
        except Exception:
            continue
            
        ev = raw_d.get("event") if (isinstance(raw_d, dict) and isinstance(raw_d.get("event"), dict)) else raw_d
        if not isinstance(ev, dict):
            continue

        hs = ev.get("homeScore", {})
        aws = ev.get("awayScore", {})
        if not isinstance(hs, dict) or not isinstance(aws, dict):
            continue

        tot_h = hs.get("current", 0) or 0
        tot_a = aws.get("current", 0) or 0
        m_date_str = datetime.utcfromtimestamp(match_date_ts / 1000 if match_date_ts and match_date_ts > 100000000000 else (match_date_ts or 0)).strftime("%Y-%m-%d %H:%M:%S") if match_date_ts else "2026-01-01 00:00:00"
        ts_sec = int(match_date_ts / 1000 if match_date_ts and match_date_ts > 100000000000 else (match_date_ts or 0))
        tourn_id = ev.get("tournament", {}).get("uniqueTournament", {}).get("id") or 0

        # 1. Volleyball
        if sport == "volleyball" and mdm_home and mdm_away:
            s1_h, s1_a = hs.get("period1", 0) or 0, aws.get("period1", 0) or 0
            s2_h, s2_a = hs.get("period2", 0) or 0, aws.get("period2", 0) or 0
            s3_h, s3_a = hs.get("period3", 0) or 0, aws.get("period3", 0) or 0
            s4_h, s4_a = hs.get("period4", 0) or 0, aws.get("period4", 0) or 0
            s5_h, s5_a = hs.get("period5", 0) or 0, aws.get("period5", 0) or 0
            tot_pts = (s1_h + s1_a + s2_h + s2_a + s3_h + s3_a + s4_h + s4_a + s5_h + s5_a)
            
            volleyball_batch.append({
                "mdm_match_id": mdm_match_id, "tournament_id": tourn_id,
                "home_team_id": mdm_home, "away_team_id": mdm_away,
                "venue_id": 0, "referee_id": 0, "match_time": m_date_str, "match_timestamp": ts_sec,
                "home_score": tot_h, "away_score": tot_a,
                "home_s1": s1_h, "away_s1": s1_a, "home_s2": s2_h, "away_s2": s2_a,
                "home_s3": s3_h, "away_s3": s3_a, "home_s4": s4_h, "away_s4": s4_a,
                "home_s5": s5_h, "away_s5": s5_a, "total_points": tot_pts,
                "winner": "HOME" if tot_h > tot_a else "AWAY"
            })

        # 2. Handball
        elif sport == "handball" and mdm_home and mdm_away:
            h1_h, h1_a = hs.get("period1", 0) or 0, aws.get("period1", 0) or 0
            h2_h, h2_a = hs.get("period2", 0) or 0, aws.get("period2", 0) or 0
            handball_batch.append({
                "mdm_match_id": mdm_match_id, "tournament_id": tourn_id,
                "home_team_id": mdm_home, "away_team_id": mdm_away,
                "venue_id": 0, "referee_id": 0, "match_time": m_date_str, "match_timestamp": ts_sec,
                "home_score": tot_h, "away_score": tot_a,
                "home_h1": h1_h, "away_h1": h1_a, "home_h2": h2_h, "away_h2": h2_a,
                "total_goals": tot_h + tot_a,
                "winner": "HOME" if tot_h > tot_a else ("AWAY" if tot_a > tot_h else "DRAW")
            })

        # 3. Baseball
        elif sport == "baseball" and mdm_home and mdm_away:
            baseball_batch.append({
                "mdm_match_id": mdm_match_id, "tournament_id": tourn_id,
                "home_team_id": mdm_home, "away_team_id": mdm_away,
                "venue_id": 0, "referee_id": 0, "match_time": m_date_str, "match_timestamp": ts_sec,
                "home_score": tot_h, "away_score": tot_a,
                "home_hits": hs.get("hits", 0) or 0, "away_hits": aws.get("hits", 0) or 0,
                "home_errors": hs.get("errors", 0) or 0, "away_errors": aws.get("errors", 0) or 0,
                "extra_innings": 1 if hs.get("period10") is not None else 0,
                "winner": "HOME" if tot_h > tot_a else "AWAY"
            })

        # 4. MMA / Combat
        elif sport == "mma" and mdm_home and mdm_away:
            win_code = ev.get("status", {}).get("description", "DECISION")
            method = "DECISION_UNANIMOUS"
            if "KO" in win_code.upper() or "TKO" in win_code.upper():
                method = "KO_TKO"
            elif "SUBMISSION" in win_code.upper():
                method = "SUBMISSION"
            elif "SPLIT" in win_code.upper():
                method = "DECISION_SPLIT"

            winner_id = mdm_home if tot_h > tot_a else (mdm_away if tot_a > tot_h else 0)
            combat_batch.append({
                "mdm_match_id": mdm_match_id, "tournament_id": tourn_id,
                "fighter1_id": mdm_home, "fighter2_id": mdm_away,
                "venue_id": 0, "referee_id": 0, "match_time": m_date_str, "match_timestamp": ts_sec,
                "end_round": hs.get("round", 3) or 3,
                "win_method": method,
                "winner_fighter_id": winner_id,
                "is_title_fight": 1 if "title" in str(ev.get("tournament", {})).lower() else 0
            })

        # Batch flushes
        if len(volleyball_batch) >= 1000:
            counts["volleyball"] += insert_clickhouse("volleyball_match_fact", volleyball_batch)
            volleyball_batch = []

        if len(handball_batch) >= 1000:
            counts["handball"] += insert_clickhouse("handball_match_fact", handball_batch)
            handball_batch = []

        if len(baseball_batch) >= 1000:
            counts["baseball"] += insert_clickhouse("baseball_match_fact", baseball_batch)
            baseball_batch = []

    # Final flushes
    if volleyball_batch:
        counts["volleyball"] += insert_clickhouse("volleyball_match_fact", volleyball_batch)

    if handball_batch:
        counts["handball"] += insert_clickhouse("handball_match_fact", handball_batch)

    if baseball_batch:
        counts["baseball"] += insert_clickhouse("baseball_match_fact", baseball_batch)

    if combat_batch:
        counts["combat"] += insert_clickhouse("combat_match_fact", combat_batch)

    print(f"[{datetime.now()}] Multi-Sport Ingestion COMPLETE: {counts}")
    sofa_cur.close()
    sofa_conn.close()

if __name__ == "__main__":
    run_sports_ingest()
