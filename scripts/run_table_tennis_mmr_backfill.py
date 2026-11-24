#!/usr/bin/env python3
"""
High-Performance Universal Table Tennis MMR Backfill Engine
Reads 824k+ finished table tennis matches from ClickHouse table_tennis_match_fact,
replays chronologically through pure OOP TableTennisGlickoEngine with symmetrical mass conservation,
applies Polish Junior Guard soft cap, table tennis tier anchors,
and writes clean ratings to PostgreSQL and ClickHouse sport_mmr_rating.
"""

import os
import sys
import time
import math
import json
from datetime import datetime

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8', errors='replace')

import psycopg2
from psycopg2.extras import execute_batch
import requests

from sport_mmr_engine import GlickoPlayer, TableTennisGlickoEngine

CLICKHOUSE_URL = os.environ.get("CLICKHOUSE_URL", "http://127.0.0.1:8123")
CLICKHOUSE_USER = os.environ.get("CLICKHOUSE_USER", "default")
CLICKHOUSE_PASSWORD = os.environ.get("CLICKHOUSE_PASSWORD", "clickhouse")

PG_HOST = os.environ.get("PG_HOST", "127.0.0.1")
PG_PORT = int(os.environ.get("PG_PORT", "5432"))
PG_USER = os.environ.get("PG_USER", "postgres")
PG_PASS = os.environ.get("PG_PASS", "postgres")
PG_DB = os.environ.get("PG_DB", "igaming_analytics")

# Table Tennis Tournament Tier Definitions
TT_TOURNAMENT_TIERS = {
    1: {"name": "World Championship / Olympic Games / WTT Grand Smash", "anchor": 2400, "soft_cap": 3200,
        "keywords": ["grand smash", "olympic", "world championships", "wtt finals", "world cup", "china smash", "europe smash", "world tour finals", "world team championships"]},
    2: {"name": "WTT Champions / WTT Star Contender", "anchor": 2100, "soft_cap": 2800,
        "keywords": ["wtt champions", "star contender", "europe top 16", "asian championships", "asian games", "european championships", "champions league"]},
    3: {"name": "WTT Contender / WTT Feeder / Europe Top 16", "anchor": 1800, "soft_cap": 2400,
        "keywords": ["wtt contender", "feeder", "national championships", "pan american", "ettu", "europe cup", "europe trophy"]},
    4: {"name": "Czech Liga Pro / Russian Liga Pro / TT Elite Series", "anchor": 1450, "soft_cap": 2000,
        "keywords": ["czech liga pro", "liga pro", "tt elite", "elite series", "poland tt", "liga pro ("]},
    5: {"name": "Setka Cup / TT Cup / Win Cup / National League", "anchor": 1200, "soft_cap": 1650,
        "keywords": ["setka cup", "tt cup", "win cup", "challenger series", "pro series", "setka"]},
    6: {"name": "Youth & Regional Table Tennis Leagues", "anchor": 900, "soft_cap": 1300,
        "keywords": ["youth", "junior", "u19", "u17", "u15", "regional"]},
    7: {"name": "Amateur / Club Table Tennis", "anchor": 650, "soft_cap": 950,
        "keywords": ["amateur", "club", "open cup"]}
}


def classify_tt_tier(tournament_name: str) -> tuple:
    if not tournament_name:
        return 4, TT_TOURNAMENT_TIERS[4]["soft_cap"]
    name_lower = tournament_name.lower()
    for tier in [1, 2, 3, 4, 5, 6, 7]:
        for kw in TT_TOURNAMENT_TIERS[tier]["keywords"]:
            if kw in name_lower:
                return tier, TT_TOURNAMENT_TIERS[tier]["soft_cap"]
    return 4, TT_TOURNAMENT_TIERS[4]["soft_cap"]


def query_clickhouse_tsv(sql: str):
    url = f"{CLICKHOUSE_URL}/"
    resp = requests.post(
        url,
        params={"query": f"{sql} FORMAT TabSeparated"},
        auth=(CLICKHOUSE_USER, CLICKHOUSE_PASSWORD),
        stream=True,
        timeout=300
    )
    resp.raise_for_status()
    for line in resp.iter_lines(decode_unicode=True):
        if line:
            yield line.split('\t')


def fetch_tt_tournament_names():
    print("[1/5] Fetching table tennis tournament metadata...")
    tourn_map = {}

    # 1. Load from pre-extracted Sofascore JSON if available
    json_path = os.path.join(os.path.dirname(__file__), "tt_tournaments_sofascore.json")
    if os.path.exists(json_path):
        try:
            with open(json_path, "r", encoding="utf-8") as f:
                data = json.load(f)
                for k, v in data.items():
                    tourn_map[int(k)] = str(v)
            print(f"Loaded {len(tourn_map)} tournament mappings from {json_path}")
        except Exception as e:
            print(f"Notice loading {json_path}: {e}")

    # 2. Add canonical mdm tournaments
    try:
        conn_mdm = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_mdm")
        cur = conn_mdm.cursor()
        cur.execute("SELECT id, canonical_name FROM mdm_tournament WHERE sport_id = 5")
        for r in cur.fetchall():
            tourn_map[int(r[0])] = str(r[1])
        cur.execute("SELECT sofascore_id, canonical_name FROM mdm_tournament WHERE sport_id = 5 AND sofascore_id IS NOT NULL")
        for r in cur.fetchall():
            tourn_map[int(r[0])] = str(r[1])
        cur.close()
        conn_mdm.close()
    except Exception as e:
        print(f"Warning fetching tournaments from mdm: {e}")

    # 3. Explicit known top volume table tennis tournament IDs fallback
    known_top = {
        99281: "TT Elite Series",
        78888: "TT Cup",
        99075: "Czech Liga Pro",
        78914: "Setka Cup",
        78895: "Liga Pro",
        171028: "Challenger Series, Group stage",
        43640: "Challenger Series, Group stage",
        111477: "Czech Liga Pro",
    }
    for tid, name in known_top.items():
        if tid not in tourn_map:
            tourn_map[tid] = name

    print(f"Loaded total {len(tourn_map):,} table tennis tournament name mappings.")
    return tourn_map


def fetch_player_names_from_mdm():
    print("[2/5] Loading table tennis player canonical names from PostgreSQL...")
    player_names = {}
    try:
        conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname="igaming_mdm")
        cur = conn.cursor()
        cur.execute("SELECT sofascore_id, canonical_name, coalesce(country_name, '') FROM mdm_team WHERE sport_id = 5 AND sofascore_id IS NOT NULL")
        for row in cur.fetchall():
            player_names[int(row[0])] = (str(row[1]), str(row[2]))
        cur.close()
        conn.close()
        print(f"Loaded {len(player_names):,} player names from PostgreSQL mdm_team.")
    except Exception as e:
        print(f"Warning fetching player names from PG: {e}")
    return player_names


def run_backfill():
    start_time = time.time()
    print("=" * 70)
    print("🏓 STARTING UNIVERSAL TABLE TENNIS MMR HISTORICAL BACKFILL (Clean Symmetrical Physics)")
    print("=" * 70)

    tourn_meta = fetch_tt_tournament_names()
    player_meta = fetch_player_names_from_mdm()

    print("[3/5] Streaming 824k+ finished table tennis matches from ClickHouse...")
    sql = (
        "SELECT "
        "player1_id, player2_id, match_timestamp, score1, score2, points_margin, "
        "coalesce(tournament_id, 0) as tourn_id, mdm_match_id "
        "FROM igaming_dds.table_tennis_match_fact "
        "WHERE (score1 > 0 OR score2 > 0) AND player1_id > 0 AND player2_id > 0 "
        "ORDER BY match_timestamp ASC"
    )

    # In-memory ratings: player_id -> GlickoPlayer
    ratings = {}

    def get_player(pid: int) -> GlickoPlayer:
        if pid not in ratings:
            p = GlickoPlayer(pid, initial_mmr=1500.0, initial_rd=350.0, initial_vol=0.06)
            p.last_match_time = 0
            ratings[pid] = p
        return ratings[pid]

    print("[4/5] Replaying matches through Symmetrical TableTennisGlickoEngine...")
    t_replay = time.time()
    count = 0
    engine = TableTennisGlickoEngine()
    recent_h2h = {}

    for m in query_clickhouse_tsv(sql):
        p1_id = int(m[0])
        p2_id = int(m[1])
        ts = int(m[2])
        s1 = int(m[3])
        s2 = int(m[4])
        pt_margin = int(m[5]) if len(m) > 5 and m[5] and m[5] != '\\N' else 0
        tourn_id = int(m[6]) if len(m) > 6 and m[6] and m[6] != '\\N' else 0

        tourn_name = tourn_meta.get(tourn_id, "")
        tier, soft_cap = classify_tt_tier(tourn_name)

        # Margin of victory multiplier based on set and point differential
        set_diff = abs(s1 - s2)
        mov = 1.0 + (set_diff - 1) * 0.08 + max(-0.15, min(0.20, pt_margin * 0.01))

        p1 = get_player(p1_id)
        p2 = get_player(p2_id)
        p1.last_match_time = ts
        p2.last_match_time = ts

        # Track 7-day rolling H2H count for diminishing returns on repeated matches (ts in seconds)
        h2h_key = (min(p1_id, p2_id), max(p1_id, p2_id))
        seven_days_ago = ts - (7 * 86400)
        h2h_list = recent_h2h.get(h2h_key, [])
        h2h_list = [t for t in h2h_list if t >= seven_days_ago]
        h2h_count = len(h2h_list) + 1
        h2h_list.append(ts)
        recent_h2h[h2h_key] = h2h_list

        engine.apply_match_update(p1, p2, s1, s2, tier, soft_cap, mov_multiplier=mov, h2h_count=h2h_count)

        count += 1
        if count % 100000 == 0:
            print(f"  Processed {count:,} matches... (Active unique players: {len(ratings):,})")

    replay_duration = time.time() - t_replay
    print(f"\n✅ Replay Complete! Processed {count:,} matches in {replay_duration:.2f}s ({count / replay_duration:.0f} matches/sec)!")
    print(f"Total player rating states computed: {len(ratings):,}")

    # Top 25 Table Tennis Players
    print("\n" + "=" * 70)
    print("🏆 TOP 25 PLAYERS BY COMPUTED DYNAMIC MMR (Table Tennis)")
    print("=" * 70)
    all_players = [p for p in ratings.values() if p.matches_played >= 15]
    all_players.sort(key=lambda x: x.mmr, reverse=True)

    print(f"{'Rank':<5} | {'Player ID':<10} | {'Name':<28} | {'MMR':<7} | {'RD':<5} | {'Matches':<7} | {'Win %':<6} | {'Peak':<7}")
    print("-" * 85)
    for idx, p in enumerate(all_players[:25], 1):
        name_info = player_meta.get(p.player_id, (f"Player #{p.player_id}", ""))
        pname = name_info[0][:26]
        wr = (p.wins / p.matches_played * 100) if p.matches_played > 0 else 0
        print(f"#{idx:<4} | {p.player_id:<10} | {pname:<28} | {p.mmr:<7.1f} | {p.rd:<5.1f} | {p.matches_played:<7} | {wr:<5.1f}% | {p.peak_mmr:<7.1f}")

    # MMR Distribution across tiers
    print("\n📊 MMR DISTRIBUTION ACROSS ALL ACTIVE PLAYERS:")
    bins = [
        (2200, 3200, "World Elite / WTT Grand Smash"),
        (1900, 2200, "WTT Champions / Star Contender"),
        (1650, 1900, "WTT Contender / Top Liga Pro"),
        (1400, 1650, "Liga Pro / Setka Cup Core"),
        (1150, 1400, "Setka Cup / TT Cup Lower"),
        (900,  1150, "Regional & Youth Leagues"),
        (500,  900,  "Amateur & Beginners")
    ]
    for low, high, label in bins:
        matching = [p for p in all_players if low <= p.mmr < high]
        pct = (len(matching) / len(all_players) * 100) if all_players else 0
        print(f"  {label:<32} ({low:>4} - {high:>4} MMR): {len(matching):>6} players ({pct:>5.1f}%)")

    # Min, Avg, Max verification
    min_mmr = min(p.mmr for p in ratings.values())
    avg_mmr = sum(p.mmr for p in ratings.values()) / len(ratings)
    max_mmr = max(p.mmr for p in ratings.values())
    print(f"\n📈 Rating Sanity Check: Min MMR={min_mmr:.2f}, Avg MMR={avg_mmr:.2f}, Max MMR={max_mmr:.2f}")
    assert min_mmr >= 500.0, f"Critical Bug: Min MMR dropped below 500.0! {min_mmr}"

    # Export to ClickHouse igaming_dds.sport_mmr_rating
    print("\n[5/5] Exporting computed ratings to ClickHouse igaming_dds.sport_mmr_rating...")
    ch_rows = []
    for pid, p in ratings.items():
        name_info = player_meta.get(pid, ("", ""))
        pname = name_info[0].replace("'", " ").replace("\\", " ")
        pcountry = name_info[1]
        last_dt = datetime.fromtimestamp(p.last_match_time).strftime('%Y-%m-%d %H:%M:%S') if getattr(p, 'last_match_time', 0) > 0 else '2024-01-01 00:00:00'
        ch_rows.append(f"({pid}, 'table-tennis', 'ALL', {p.mmr:.2f}, {p.rd:.2f}, {p.vol:.4f}, {p.matches_played}, {p.wins}, {p.losses}, {p.streak}, {p.peak_mmr:.2f}, '{pname}', '{pcountry}', '{last_dt}', now())")

    # Batch insert into ClickHouse
    batch_size = 5000
    total_inserted = 0
    t_ch = time.time()
    for i in range(0, len(ch_rows), batch_size):
        chunk = ch_rows[i:i + batch_size]
        vals = ", ".join(chunk)
        insert_sql = (
            "INSERT INTO igaming_dds.sport_mmr_rating "
            "(player_id, sport_slug, surface, mmr, rating_deviation, volatility, matches_played, wins_count, losses_count, current_streak, peak_mmr, player_name, country, last_match_at, updated_at) "
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

    print(f"Successfully inserted {total_inserted:,} table tennis rating rows into ClickHouse in {time.time() - t_ch:.2f}s!")

    # Optimize table to force ReplacingMergeTree deduplication
    try:
        requests.post(
            f"{CLICKHOUSE_URL}/",
            params={"query": "OPTIMIZE TABLE igaming_dds.sport_mmr_rating FINAL"},
            auth=(CLICKHOUSE_USER, CLICKHOUSE_PASSWORD),
            timeout=60
        )
        print("OPTIMIZE TABLE igaming_dds.sport_mmr_rating FINAL completed.")
    except Exception as e:
        print(f"Notice during optimize: {e}")

    # Persist to PostgreSQL igaming_analytics.sport_mmr_rating
    print("\nPersisting active ratings to PostgreSQL igaming_analytics.sport_mmr_rating...")
    try:
        conn_pg = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname=PG_DB)
        cur = conn_pg.cursor()
        pg_tuples = []
        for pid, p in ratings.items():
            name_info = player_meta.get(pid, ("", ""))
            last_dt = datetime.fromtimestamp(p.last_match_time) if getattr(p, 'last_match_time', 0) > 0 else datetime(2024, 1, 1)
            pg_tuples.append((
                pid, 'table-tennis', 'ALL',
                float(round(p.mmr, 2)), float(round(p.rd, 2)), float(round(p.vol, 4)),
                int(p.matches_played), int(p.wins), int(p.losses), int(p.streak),
                float(round(p.peak_mmr, 2)), name_info[0], name_info[1], last_dt
            ))

        upsert_sql = """
            INSERT INTO public.sport_mmr_rating 
            (player_id, sport_slug, surface, mmr, rating_deviation, volatility, matches_played, wins_count, losses_count, current_streak, peak_mmr, player_name, country, last_match_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
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
                last_match_at = EXCLUDED.last_match_at,
                updated_at = NOW();
        """
        execute_batch(cur, upsert_sql, pg_tuples, page_size=2000)
        conn_pg.commit()
        cur.close()
        conn_pg.close()
        print(f"Successfully upserted {len(pg_tuples):,} rows into PostgreSQL!")
    except Exception as e:
        print(f"Notice: PostgreSQL persistence: {e}")

    total_time = time.time() - start_time
    print(f"\n🎉 ALL COMPLETE! Universal Table Tennis MMR Backfill took {total_time:.2f}s total.")


if __name__ == "__main__":
    run_backfill()
