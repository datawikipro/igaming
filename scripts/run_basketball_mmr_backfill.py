#!/usr/bin/env python3
"""
High-Performance Universal Basketball MMR Backfill Engine
Reads finished basketball matches from PostgreSQL sofascore_raw_events and ClickHouse basketball_match_fact,
replays chronologically through Multi-Tier Glicko-2 with MOV and Granular Basketball Tiers,
and persists ratings to ClickHouse and PostgreSQL sport_mmr_rating.
"""

import os
import sys
import time
import math
import json
import requests
import psycopg2
from psycopg2.extras import execute_batch

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8', errors='replace')

sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from sport_mmr_engine import GlickoPlayer, apply_match_update

CLICKHOUSE_URL = os.environ.get("CLICKHOUSE_URL", "http://10.111.38.2:8123")
CLICKHOUSE_USER = os.environ.get("CLICKHOUSE_USER", "default")
CLICKHOUSE_PASSWORD = os.environ.get("CLICKHOUSE_PASSWORD", "clickhouse")

PG_HOST = os.environ.get("PG_HOST", "10.244.0.57")
PG_PORT = int(os.environ.get("PG_PORT", "5432"))
PG_USER = os.environ.get("PG_USER", "postgres")
PG_PASS = os.environ.get("PG_PASS", "postgres")
PG_DB = os.environ.get("PG_DB", "igaming_analytics")

# 7 Granular Tiers for Basketball
BASKETBALL_TIERS = {
    1: {"name": "NBA / EuroLeague / FIBA World Cup / Olympic Games", "anchor": 2400, "soft_cap": 3200,
        "keywords": ["nba", "euroleague", "fiba world cup", "olympic", "world cup"]},
    2: {"name": "EuroCup / Liga ACB / BSL Turkey / VTB United / NCAA Div 1", "anchor": 2000, "soft_cap": 2700,
        "keywords": ["eurocup", "liga acb", "acb", "bsl", "vtb", "ncaa", "basketball champions league"]},
    3: {"name": "BBL Germany / LNB Pro A / Lega Serie A / CBA China / NBL Australia / ABA Liga", "anchor": 1700, "soft_cap": 2250,
        "keywords": ["bbl", "pro a", "serie a", "cba", "nbl", "aba", "adriatic"]},
    4: {"name": "G League / Greek A1 / Korisliiga / LEB Oro / NBB Brazil / Liga Nacional", "anchor": 1450, "soft_cap": 1900,
        "keywords": ["g league", "a1", "korisliiga", "leb oro", "nbb", "liga nacional", "pro b"]},
    5: {"name": "Minor National Leagues (PBA, LNBP, BNXT, Superliga)", "anchor": 1200, "soft_cap": 1600,
        "keywords": ["pba", "lnbp", "bnxt", "superliga", "division 1", "liga 2", "national league"]},
    6: {"name": "Youth / Age-restricted Basketball (U21, U20, U19, U18)", "anchor": 950, "soft_cap": 1300,
        "keywords": ["u21", "u20", "u19", "u18", "u17", "u16", "junior", "youth", "молодеж"]},
    7: {"name": "Amateur / Club / Regional Basketball", "anchor": 650, "soft_cap": 950,
        "keywords": ["amateur", "club", "trophy", "cup", "regional"]}
}


def classify_basketball_tier(tourn_name: str) -> tuple:
    if not tourn_name:
        return 4, BASKETBALL_TIERS[4]["soft_cap"]
    name_lower = tourn_name.lower()
    for tier in [1, 2, 3, 4, 5, 6, 7]:
        for kw in BASKETBALL_TIERS[tier]["keywords"]:
            if kw in name_lower:
                return tier, BASKETBALL_TIERS[tier]["soft_cap"]
    return 4, BASKETBALL_TIERS[4]["soft_cap"]


def fetch_basketball_team_names():
    print("[1/4] Loading basketball team metadata from PostgreSQL...")
    team_meta = {}
    name_to_id = {}
    try:
        conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_mdm")
        cur = conn.cursor()
        cur.execute("SELECT sofascore_id, canonical_name, coalesce(country_name, '') FROM mdm_team WHERE sport_id = 3 AND sofascore_id IS NOT NULL")
        for row in cur.fetchall():
            tid = int(row[0])
            cname = str(row[1])
            country = str(row[2])
            team_meta[tid] = (cname, country)
            name_to_id[cname.lower()] = tid
        cur.close()
        conn.close()
        print(f"Loaded {len(team_meta):,} team names from PostgreSQL mdm_team.")
    except Exception as e:
        print(f"Notice fetching from mdm: {e}")

    try:
        conn_sofa = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_sofascore")
        cur_s = conn_sofa.cursor()
        cur_s.execute("""
            SELECT DISTINCT 
                coalesce((event_json::jsonb->'homeTeam'->>'id')::bigint, (event_json::jsonb->'event'->'homeTeam'->>'id')::bigint) as tid,
                coalesce(event_json::jsonb->'homeTeam'->>'name', event_json::jsonb->'event'->'homeTeam'->>'name', home_team) as tname,
                coalesce(event_json::jsonb->'homeTeam'->'country'->>'name', event_json::jsonb->'event'->'homeTeam'->'country'->>'name', '') as country
            FROM sofascore_raw_events
            WHERE sport = 'basketball' 
              AND coalesce((event_json::jsonb->'homeTeam'->>'id')::bigint, (event_json::jsonb->'event'->'homeTeam'->>'id')::bigint) IS NOT NULL
        """)
        for r in cur_s.fetchall():
            if r[0] and int(r[0]) not in team_meta and r[1]:
                tid = int(r[0])
                cname = str(r[1])
                team_meta[tid] = (cname, str(r[2]))
                name_to_id[cname.lower()] = tid
        cur_s.close()
        conn_sofa.close()
        print(f"Expanded to {len(team_meta):,} team names from sofascore_raw_events.")
    except Exception as e:
        print(f"Notice fetching from sofascore: {e}")

    return team_meta, name_to_id


def run_basketball_backfill():
    start_time = time.time()
    print("=" * 70)
    print("🏀 STARTING UNIVERSAL BASKETBALL MMR HISTORICAL BACKFILL")
    print("=" * 70)

    team_meta, name_to_id = fetch_basketball_team_names()

    print("[2/4] Fetching all finished basketball matches from sofascore_raw_events...")
    conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_sofascore")
    cur = conn.cursor()
    cur.execute("""
        SELECT 
            coalesce((event_json::jsonb->'homeTeam'->>'id')::bigint, (event_json::jsonb->'event'->'homeTeam'->>'id')::bigint, mdm_home_team_id) as home_id,
            coalesce((event_json::jsonb->'awayTeam'->>'id')::bigint, (event_json::jsonb->'event'->'awayTeam'->>'id')::bigint, mdm_away_team_id) as away_id,
            home_team,
            away_team,
            home_score,
            away_score,
            match_date,
            tournament
        FROM sofascore_raw_events
        WHERE sport = 'basketball'
          AND status = 'finished'
          AND home_score IS NOT NULL
          AND away_score IS NOT NULL
          AND match_date IS NOT NULL
        ORDER BY match_date ASC;
    """)

    matches = cur.fetchall()
    cur.close()
    conn.close()
    print(f"Loaded {len(matches):,} finished basketball matches!")

    ratings = {}

    def get_team(tid: int, name: str = "") -> GlickoPlayer:
        if tid not in ratings:
            ratings[tid] = GlickoPlayer(tid, initial_mmr=1500.0, initial_rd=350.0, initial_vol=0.06)
            if name and tid not in team_meta:
                team_meta[tid] = (name, "")
        return ratings[tid]

    print("[3/4] Replaying matches through Multi-Tier Glicko-2 Engine...")
    t_replay = time.time()
    valid_count = 0
    next_synthetic_id = 80000000

    for h_id, a_id, h_name, a_name, h_s, a_s, m_date, tourn in matches:
        eff_h_id = h_id
        if not eff_h_id and h_name:
            eff_h_id = name_to_id.get(h_name.lower())
        if not eff_h_id:
            next_synthetic_id += 1
            eff_h_id = next_synthetic_id
            if h_name:
                name_to_id[h_name.lower()] = eff_h_id

        eff_a_id = a_id
        if not eff_a_id and a_name:
            eff_a_id = name_to_id.get(a_name.lower())
        if not eff_a_id:
            next_synthetic_id += 1
            eff_a_id = next_synthetic_id
            if a_name:
                name_to_id[a_name.lower()] = eff_a_id

        p1 = get_team(int(eff_h_id), h_name or "")
        p2 = get_team(int(eff_a_id), a_name or "")

        tier, soft_cap = classify_basketball_tier(tourn)
        pt_diff = abs(h_s - a_s)
        mov = 1.0 + min(0.35, pt_diff * 0.015)

        apply_match_update(p1, p2, h_s, a_s, tier_level=tier, soft_cap=soft_cap, mov_multiplier=mov)
        valid_count += 1

    print(f"Replay complete in {time.time() - t_replay:.2f}s! Replayed {valid_count:,} matches across {len(ratings):,} distinct basketball teams.")

    top_teams = sorted(ratings.items(), key=lambda x: x[1].mmr, reverse=True)[:10]
    print("\n🏆 Top 10 Basketball Teams by Glicko-2 MMR:")
    for tid, p in top_teams:
        tname = team_meta.get(tid, (f"Team #{tid}", ""))[0]
        print(f"  {tname} (ID: {tid}): MMR={p.mmr:.1f}, RD={p.rd:.1f}, N={p.matches_played}, W/L={p.wins}/{p.losses}, Peak={p.peak_mmr:.1f}")

    print("\n[4/4] Persisting ratings to ClickHouse and PostgreSQL...")
    ch_rows = []
    for tid, p in ratings.items():
        name_info = team_meta.get(tid, ("", ""))
        pname = name_info[0].replace("'", " ").replace("\\", " ")
        pcountry = name_info[1].replace("'", " ").replace("\\", " ")
        ch_rows.append(f"({tid}, 'basketball', 'ALL', {p.mmr:.2f}, {p.rd:.2f}, {p.vol:.4f}, {p.matches_played}, {p.wins}, {p.losses}, {p.streak}, {p.peak_mmr:.2f}, '{pname}', '{pcountry}', now())")

    # Batch insert into ClickHouse
    batch_size = 5000
    total_inserted = 0
    t_ch = time.time()
    for i in range(0, len(ch_rows), batch_size):
        chunk = ch_rows[i:i + batch_size]
        vals = ", ".join(chunk)
        insert_sql = (
            "INSERT INTO igaming_dds.sport_mmr_rating "
            "(player_id, sport_slug, surface, mmr, rating_deviation, volatility, matches_played, wins_count, losses_count, current_streak, peak_mmr, player_name, country, updated_at) "
            f"VALUES {vals}"
        )
        resp = requests.post(
            f"{CLICKHOUSE_URL}/",
            data=insert_sql.encode("utf-8"),
            auth=(CLICKHOUSE_USER, CLICKHOUSE_PASSWORD),
            timeout=60
        )
        resp.raise_for_status()
        total_inserted += len(chunk)

    print(f"Successfully inserted {total_inserted:,} rows into ClickHouse igaming_dds.sport_mmr_rating in {time.time() - t_ch:.2f}s!")

    # Persist to PostgreSQL public.sport_mmr_rating
    try:
        conn_pg = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname=PG_DB)
        cur_pg = conn_pg.cursor()
        pg_tuples = []
        for tid, p in ratings.items():
            name_info = team_meta.get(tid, ("", ""))
            pg_tuples.append((
                tid, 'basketball', 'ALL',
                float(round(p.mmr, 2)), float(round(p.rd, 2)), float(round(p.vol, 4)),
                int(p.matches_played), int(p.wins), int(p.losses), int(p.streak),
                float(round(p.peak_mmr, 2)), name_info[0], name_info[1]
            ))

        upsert_sql = """
            INSERT INTO public.sport_mmr_rating 
            (player_id, sport_slug, surface, mmr, rating_deviation, volatility, matches_played, wins_count, losses_count, current_streak, peak_mmr, player_name, country, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
            ON CONFLICT (player_id, sport_slug, surface) DO UPDATE SET
                mmr = EXCLUDED.mmr,
                rating_deviation = EXCLUDED.rating_deviation,
                volatility = EXCLUDED.volatility,
                matches_played = EXCLUDED.matches_played,
                wins_count = EXCLUDED.wins_count,
                losses_count = EXCLUDED.losses_count,
                current_streak = EXCLUDED.current_streak,
                peak_mmr = EXCLUDED.peak_mmr,
                player_name = EXCLUDED.player_name,
                country = EXCLUDED.country,
                updated_at = NOW();
        """
        execute_batch(cur_pg, upsert_sql, pg_tuples, page_size=2000)
        conn_pg.commit()
        cur_pg.close()
        conn_pg.close()
        print(f"Successfully upserted {len(pg_tuples):,} rows into PostgreSQL public.sport_mmr_rating!")
    except Exception as e:
        print(f"Notice PostgreSQL persistence: {e}")

    total_time = time.time() - start_time
    print(f"\n🎉 ALL COMPLETE! Universal Basketball MMR Backfill took {total_time:.2f}s total.")


if __name__ == "__main__":
    run_basketball_backfill()
