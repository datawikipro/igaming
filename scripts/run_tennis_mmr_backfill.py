#!/usr/bin/env python3
"""
High-Performance Universal Tennis MMR Backfill Engine
Reads 378k+ finished matches from ClickHouse, replays in chronological order,
applies Polish Junior Guard soft cap, surface breakdown, and writes to PostgreSQL and ClickHouse.
"""

import os
import sys
import time
import math
import json

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8', errors='replace')

import psycopg2
from psycopg2.extras import execute_batch
import requests

from sport_mmr_engine import GlickoPlayer, apply_match_update, classify_tournament_tier, TOURNAMENT_TIERS

CLICKHOUSE_URL = os.environ.get("CLICKHOUSE_URL", "http://clickhouse.igaming-analytics.svc.cluster.local:8123")
PG_HOST = os.environ.get("PG_HOST", "igaming-aggregator-db.igaming-enrichment.svc.cluster.local")
PG_PORT = int(os.environ.get("PG_PORT", "5432"))
PG_USER = os.environ.get("PG_USER", "postgres")
PG_PASS = os.environ.get("PG_PASS", "postgres")
PG_DB = os.environ.get("PG_DB", "igaming_analytics")


CLICKHOUSE_USER = os.environ.get("CLICKHOUSE_USER", "default")
CLICKHOUSE_PASSWORD = os.environ.get("CLICKHOUSE_PASSWORD", "clickhouse")


def query_clickhouse_stream(sql: str):
    """
    Executes a query on ClickHouse with JSONCompact format and yields rows.
    """
    url = f"{CLICKHOUSE_URL}/"
    resp = requests.post(
        url,
        params={"query": f"{sql} FORMAT JSONCompact"},
        auth=(CLICKHOUSE_USER, CLICKHOUSE_PASSWORD),
        stream=True,
        timeout=120
    )
    resp.raise_for_status()
    data = resp.json()
    return data.get("data", [])


def fetch_tournament_names():
    """
    Fetches tournament names by id from PostgreSQL igaming_mdm and igaming_sofascore.
    """
    print("[1/5] Fetching tournament metadata from PostgreSQL...")
    tourn_map = {}
    try:
        conn_mdm = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_mdm")
        cur = conn_mdm.cursor()
        cur.execute("SELECT id, canonical_name FROM mdm_tournament WHERE sport_id = 4")
        for r in cur.fetchall():
            tourn_map[int(r[0])] = str(r[1])
        cur.execute("SELECT sofascore_id, canonical_name FROM mdm_tournament WHERE sport_id = 4 AND sofascore_id IS NOT NULL")
        for r in cur.fetchall():
            tourn_map[int(r[0])] = str(r[1])
        cur.close()
        conn_mdm.close()
    except Exception as e:
        print(f"Warning fetching tournaments from mdm: {e}")

    try:
        conn_sofa = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_sofascore")
        cur2 = conn_sofa.cursor()
        cur2.execute("SELECT DISTINCT (event_json::jsonb->'tournament'->>'id')::bigint, tournament FROM sofascore_raw_events WHERE sport = 'tennis' AND event_json::jsonb->'tournament'->>'id' IS NOT NULL")
        for r in cur2.fetchall():
            if r[0] and r[1]:
                tourn_map[int(r[0])] = str(r[1])
        cur2.close()
        conn_sofa.close()
    except Exception as e:
        print(f"Warning fetching tournaments from sofascore: {e}")

    print(f"Loaded {len(tourn_map):,} tournament name mappings.")
    return tourn_map


def fetch_player_names_from_mdm():
    """
    Loads known player names from mdm_team in PostgreSQL.
    """
    print("[2/5] Loading player canonical names from PostgreSQL igaming_mdm...")
    player_names = {}
    try:
        conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_mdm")
        cur = conn.cursor()
        cur.execute("SELECT sofascore_id, canonical_name, coalesce(country_name, '') FROM mdm_team WHERE sport_id = 4 AND sofascore_id IS NOT NULL")
        for row in cur.fetchall():
            player_names[int(row[0])] = (str(row[1]), str(row[2]))
        cur.close()
        conn.close()
        print(f"Loaded {len(player_names):,} player names from PostgreSQL mdm_team.")
    except Exception as e:
        print(f"Warning fetching player names from PG: {e}")
        # Fallback to ClickHouse
        try:
            sql = "SELECT player_id, player_name, country FROM igaming_dds.tennis_player_rankings"
            rows = query_clickhouse_stream(sql)
            for r in rows:
                pid = int(r[0])
                pname = str(r[1])
                pcountry = str(r[2]) if len(r) > 2 and r[2] else ""
                player_names[pid] = (pname, pcountry)
            print(f"Loaded {len(player_names):,} fallback names from ClickHouse.")
        except Exception as e2:
            print(f"Warning fallback from CH: {e2}")
    return player_names


def run_backfill():
    start_time = time.time()
    print("=" * 70)
    print("🚀 STARTING UNIVERSAL TENNIS MMR HISTORICAL BACKFILL")
    print("=" * 70)

    tourn_meta = fetch_tournament_names()
    player_meta = fetch_player_names_from_mdm()

    # Query all valid finished matches in chronological order
    print("[3/5] Querying 380k+ finished tennis matches from ClickHouse (ordered by match_time ASC)...")
    sql = (
        "SELECT "
        "player1_id, player2_id, match_timestamp, surface, score1, score2, "
        "coalesce(tournament_id, 0) as tourn_id, mdm_match_id "
        "FROM igaming_dds.tennis_match_fact "
        "WHERE (score1 > 0 OR score2 > 0) AND player1_id > 0 AND player2_id > 0 "
        "ORDER BY match_timestamp ASC"
    )

    t0 = time.time()
    matches = query_clickhouse_stream(sql)
    print(f"Fetched {len(matches)} historical matches in {time.time() - t0:.2f}s!")

    # In-memory rating storage:
    # Key: (player_id, surface) -> GlickoPlayer
    ratings: Dict[Tuple[int, str], GlickoPlayer] = {}

    def get_player(pid: int, surface: str) -> GlickoPlayer:
        key = (pid, surface)
        if key not in ratings:
            ratings[key] = GlickoPlayer(pid, initial_mmr=1500.0, initial_rd=350.0)
        return ratings[key]

    print("[4/5] Replaying matches through Multi-Tier Glicko-2 Engine...")
    t_replay = time.time()
    count = 0

    for m in matches:
        p1_id = int(m[0])
        p2_id = int(m[1])
        ts = int(m[2])
        surface_raw = str(m[3]).upper() if m[3] else "ALL"
        score1 = int(m[4])
        score2 = int(m[5])
        tourn_id = int(m[6])

        # Normalize surface
        if "CLAY" in surface_raw:
            surf = "CLAY"
        elif "GRASS" in surface_raw:
            surf = "GRASS"
        elif "INDOOR" in surface_raw:
            surf = "INDOORS"
        elif "HARD" in surface_raw:
            surf = "HARD"
        else:
            surf = "ALL"

        # Determine Tournament Tier & Soft Cap
        tourn_name = tourn_meta.get(tourn_id, "")
        if tourn_name:
            tier_level, soft_cap = classify_tournament_tier(tourn_name)
        else:
            # If tournament is unmapped, infer from competitors' current stature
            p1_all_mmr = ratings.get((p1_id, "ALL"), None)
            p2_all_mmr = ratings.get((p2_id, "ALL"), None)
            max_cur_mmr = max(p1_all_mmr.mmr if p1_all_mmr else 1500.0, p2_all_mmr.mmr if p2_all_mmr else 1500.0)
            if max_cur_mmr >= 2100:
                tier_level, soft_cap = 1, 3500
            elif max_cur_mmr >= 1800:
                tier_level, soft_cap = 2, 2700
            elif max_cur_mmr >= 1600:
                tier_level, soft_cap = 3, 2250
            else:
                tier_level, soft_cap = 4, 1850

        # Margin of Victory (MOV) calculation
        total_sets = score1 + score2
        if total_sets > 0:
            mov = 1.0 + 0.20 * (abs(score1 - score2) / total_sets)
        else:
            mov = 1.0

        # 1. Update Surface Rating
        if surf != "ALL":
            p1_surf = get_player(p1_id, surf)
            p2_surf = get_player(p2_id, surf)
            apply_match_update(p1_surf, p2_surf, score1, score2, tier_level, soft_cap, mov)

        # 2. Update Overall (ALL) Rating
        p1_all = get_player(p1_id, "ALL")
        p2_all = get_player(p2_id, "ALL")
        apply_match_update(p1_all, p2_all, score1, score2, tier_level, soft_cap, mov)

        count += 1
        if count % 50000 == 0:
            print(f"  Processed {count:,} matches... (Active unique entities: {len(ratings):,})")

    replay_duration = time.time() - t_replay
    print(f"\n✅ Replay Complete! Processed {count:,} matches in {replay_duration:.2f}s ({count / replay_duration:.0f} matches/sec)!")
    print(f"Total player-surface rating states computed: {len(ratings):,}")

    # Inspect Top 20 Overall Players
    print("\n" + "=" * 70)
    print("🏆 TOP 25 PLAYERS BY COMPUTED DYNAMIC MMR (Overall - ALL surfaces)")
    print("=" * 70)
    all_players = [p for (pid, s), p in ratings.items() if s == "ALL" and p.matches_played >= 15]
    all_players.sort(key=lambda x: x.mmr, reverse=True)

    print(f"{'Rank':<5} | {'Player ID':<10} | {'Name':<28} | {'MMR':<7} | {'RD':<5} | {'Matches':<7} | {'Win %':<6} | {'Peak':<7}")
    print("-" * 85)
    for idx, p in enumerate(all_players[:25], 1):
        name_info = player_meta.get(p.player_id, (f"Player #{p.player_id}", ""))
        pname = name_info[0][:26]
        wr = (p.wins / p.matches_played * 100) if p.matches_played > 0 else 0
        print(f"#{idx:<4} | {p.player_id:<10} | {pname:<28} | {p.mmr:<7.1f} | {p.rd:<5.1f} | {p.matches_played:<7} | {wr:<5.1f}% | {p.peak_mmr:<7.1f}")

    # Inspect Distribution across MMR tiers
    print("\n📊 MMR DISTRIBUTION ACROSS ALL ACTIVE PLAYERS:")
    bins = [
        (2400, 3500, "Elite Grand Slam / Top 10 ATP"),
        (2000, 2400, "ATP / WTA Main Tour Pro"),
        (1700, 2000, "Challenger Level"),
        (1400, 1700, "ITF Pro / M25 / W35"),
        (1100, 1400, "ITF M15 / Development"),
        (850, 1100,  "Regional / Junior League"),
        (0, 850,     "Amateur / Beginner")
    ]
    for low, high, label in bins:
        matching = [p for p in all_players if low <= p.mmr < high]
        print(f"  {label:<32} ({low:>4} - {high:>4} MMR): {len(matching):>6} players ({len(matching)/len(all_players)*100:>5.1f}%)")

    # Clean existing tennis ratings from ClickHouse igaming_dds.sport_mmr_rating (DO NOT TRUNCATE other sports!)
    print("\n[5/5] Exporting computed ratings to ClickHouse igaming_dds.sport_mmr_rating...")
    try:
        requests.post(
            f"{CLICKHOUSE_URL}/",
            params={"query": "ALTER TABLE igaming_dds.sport_mmr_rating DELETE WHERE sport_slug = 'tennis'"},
            auth=(CLICKHOUSE_USER, CLICKHOUSE_PASSWORD),
            timeout=30
        )
        print("Cleared previous tennis ratings from ClickHouse igaming_dds.sport_mmr_rating.")
    except Exception as e:
        print(f"Delete notice: {e}")

    ch_rows = []
    for (pid, surf), p in ratings.items():
        name_info = player_meta.get(pid, ("", ""))
        pname = name_info[0].replace("'", " ").replace("\\", " ")
        pcountry = name_info[1]
        ch_rows.append(f"({pid}, 'tennis', '{surf}', {p.mmr:.2f}, {p.rd:.2f}, {p.vol:.4f}, {p.matches_played}, {p.wins}, {p.losses}, {p.streak}, {p.peak_mmr:.2f}, '{pname}', '{pcountry}', now())")

    # Batch insert into ClickHouse
    batch_size = 5000
    total_inserted = 0
    t_ch = time.time()
    for i in range(0, len(ch_rows), batch_size):
        chunk = ch_rows[i:i + batch_size]
        vals = ", ".join(chunk)
        insert_sql = f"INSERT INTO igaming_dds.sport_mmr_rating (player_id, sport_slug, surface, mmr, rating_deviation, volatility, matches_played, wins_count, losses_count, current_streak, peak_mmr, player_name, country, last_match_at) VALUES {vals}"
        resp = requests.post(
            f"{CLICKHOUSE_URL}/",
            data=insert_sql.encode("utf-8"),
            auth=(CLICKHOUSE_USER, CLICKHOUSE_PASSWORD),
            timeout=60
        )
        resp.raise_for_status()
        total_inserted += len(chunk)

    print(f"Successfully inserted {total_inserted:,} rating rows into ClickHouse in {time.time() - t_ch:.2f}s!")

    # Also persist to PostgreSQL igaming_analytics.sport_mmr_rating
    print("\nPersisting active ratings to PostgreSQL igaming_analytics.sport_mmr_rating...")
    try:
        t_pg = time.time()
        pg_conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname=PG_DB)
        pg_cur = pg_conn.cursor()
        
        pg_records = []
        for (pid, surf), p in ratings.items():
            name_info = player_meta.get(pid, (None, None))
            pname = name_info[0]
            pcountry = name_info[1]
            pg_records.append((
                pid, 'tennis', surf, p.mmr, p.rd, p.vol,
                p.matches_played, p.wins, p.losses, p.streak, p.peak_mmr,
                pname, pcountry
            ))

        upsert_sql = """
            INSERT INTO public.sport_mmr_rating (
                player_id, sport_slug, surface, mmr, rating_deviation, volatility,
                matches_played, wins_count, losses_count, current_streak, peak_mmr,
                player_name, country, updated_at
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
            ON CONFLICT (player_id, sport_slug, surface) DO UPDATE SET
                mmr = EXCLUDED.mmr,
                rating_deviation = EXCLUDED.rating_deviation,
                volatility = EXCLUDED.volatility,
                matches_played = EXCLUDED.matches_played,
                wins_count = EXCLUDED.wins_count,
                losses_count = EXCLUDED.losses_count,
                current_streak = EXCLUDED.current_streak,
                peak_mmr = EXCLUDED.peak_mmr,
                player_name = coalesce(EXCLUDED.player_name, sport_mmr_rating.player_name),
                country = coalesce(EXCLUDED.country, sport_mmr_rating.country),
                updated_at = NOW();
        """
        execute_batch(pg_cur, upsert_sql, pg_records, page_size=2000)
        pg_conn.commit()
        pg_cur.close()
        pg_conn.close()
        print(f"Successfully persisted {len(pg_records):,} rows to PostgreSQL in {time.time() - t_pg:.2f}s!")
    except Exception as e:
        print(f"Warning persisting to PostgreSQL: {e}")

    print(f"\n🎉 FULL BACKFILL COMPLETED IN {time.time() - start_time:.2f}s TOTAL!")


if __name__ == "__main__":
    run_backfill()
