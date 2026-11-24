#!/usr/bin/env python3
"""
High-Performance Universal Multi-Sport MMR Backfill Engine
Replays historical matches chronologically through pure OOP MultiTierGlickoEngine
with symmetrical mass conservation, dynamic MOV weighting, and tournament tier anchors.
Persists ratings to ClickHouse DDS and PostgreSQL sport_mmr_rating.

Supports:
- volleyball
- handball
- baseball
- ice-hockey
- basketball
- cs2
- dota2
- tennis-doubles
- table-tennis
"""

import os
import sys
import time
import math
import argparse
from datetime import datetime
from typing import Dict, Tuple, Optional, List, Any

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8', errors='replace')

import psycopg2
from psycopg2.extras import execute_batch
import requests

# Import OOP Glicko Engines
sys.path.insert(0, os.path.dirname(__file__))
from sport_mmr_engine import GlickoPlayer, MultiTierGlickoEngine, TableTennisGlickoEngine

CLICKHOUSE_URL = os.environ.get("CLICKHOUSE_URL", "http://127.0.0.1:8123")
CLICKHOUSE_USER = os.environ.get("CLICKHOUSE_USER", "default")
CLICKHOUSE_PASSWORD = os.environ.get("CLICKHOUSE_PASSWORD", "clickhouse")

PG_HOST = os.environ.get("PG_HOST", "127.0.0.1")
PG_PORT = int(os.environ.get("PG_PORT", "5432"))
PG_USER = os.environ.get("PG_USER", "postgres")
PG_PASS = os.environ.get("PG_PASS", "postgres")
PG_ANALYTICS_DB = os.environ.get("PG_ANALYTICS_DB", "igaming_analytics")
PG_MDM_DB = os.environ.get("PG_MDM_DB", "igaming_mdm")

# Universal Tier Configurations (Tier -> Level, Anchor, Soft Cap)
UNIVERSAL_TIERS = {
    1: {"name": "World Elite / Premier S-Tier", "anchor": 2400, "soft_cap": 3200},
    2: {"name": "Top National / Major Continental", "anchor": 2000, "soft_cap": 2700},
    3: {"name": "Secondary National / Regional", "anchor": 1650, "soft_cap": 2300},
    4: {"name": "Development / Minor Leagues", "anchor": 1350, "soft_cap": 1850},
    5: {"name": "Amateur / Youth / Semi-Pro", "anchor": 900,  "soft_cap": 1350},
}

# Sport-specific keyword patterns to upgrade tournaments
SPORT_TIER_KEYWORDS = {
    "baseball": {
        1: ["mlb", "world series", "major league", "npb", "kbo", "world baseball classic"],
        2: ["triple-a", "aaa", "cpbl", "lmb", "mexican league", "caribbean series"],
        3: ["double-a", "aa", "high-a", "single-a", "fall league"]
    },
    "basketball": {
        1: ["nba", "euroleague", "olympic", "world cup", "fiba world cup"],
        2: ["eurocup", "acb", "liga endesa", "bsl", "vtb", "bbl", "serie a", "nbl", "cba", "lnb"],
        3: ["g league", "pro a", "pro b", "lebed", "college", "ncaa"]
    },
    "ice-hockey": {
        1: ["nhl", "stanley cup", "olympic", "world championship", "khl"],
        2: ["shl", "liiga", "ahl", "national league", "del", "extraliga", "champions hockey league"],
        3: ["vhl", "echl", "hockeyallsvenskan", "mestis", "alpshl"]
    },
    "volleyball": {
        1: ["olympic", "world championship", "nations league", "vnl", "cev champions league", "superlega", "plusliga"],
        2: ["superliga", "cev cup", "challenge cup", "serie a1", "ligue a", "super league", "ekstraklasa", "v-league"],
        3: ["serie a2", "serie b", "1. division", "2. division"]
    },
    "handball": {
        1: ["olympic", "world championship", "ehf champions league", "bundesliga", "starligue", "liga asobal"],
        2: ["ehf european league", "ehf european cup", "handboldligaen", "handbollsligan", "superliga", "pgnig superliga"],
        3: ["2. bundesliga", "proligue", "división de honor", "1. division"]
    },
    "cs2": {
        1: ["major", "pgl major", "blast premier", "iem", "intel extreme masters", "esl pro league", "world final"],
        2: ["esl challenger", "cct", "fireleague", "elisa", "betboom", "roobet", "thunderpick", "champion of champions"],
        3: ["esea", "european pro league", "res", "united21", "y-games", "mistgames"]
    },
    "dota2": {
        1: ["the international", "riyadh masters", "dreamleague", "esl one", "pgl wallachia", "betboom dacha"],
        2: ["elite league", "fissure", "games of the future", "1win series"],
        3: ["european pro league", "epl", "res", "destiny league", "cct", "dota 2 champions league"]
    },
    "tennis-doubles": {
        1: ["slam", "wimbledon", "us open", "australian open", "roland garros", "masters 1000", "atp finals", "wta finals"],
        2: ["atp 500", "wta 500", "atp 250", "wta 250", "atp tour", "wta tour"],
        3: ["challenger", "wta 125"],
        4: ["itf m25", "itf w35", "itf w50", "m25", "w35"],
        5: ["itf m15", "itf w15", "m15", "w15"]
    }
}


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


def load_tournament_metadata() -> Tuple[Dict[int, str], Dict[int, int]]:
    """
    Loads tournament canonical name and verified tier from PostgreSQL mdm_tournament
    and ClickHouse dict_tournament.
    Returns (tourn_names, tourn_tiers).
    """
    tourn_names = {}
    tourn_tiers = {}

    print("  [Meta 1/2] Loading tournament metadata from PostgreSQL mdm_tournament...")
    try:
        conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname=PG_MDM_DB)
        cur = conn.cursor()
        cur.execute("SELECT id, canonical_name, coalesce(tier, 0), sofascore_id FROM mdm_tournament")
        for row in cur.fetchall():
            tid = int(row[0])
            name = str(row[1]) if row[1] else ""
            tier = int(row[2]) if row[2] else 0
            sofa_id = int(row[3]) if row[3] else None

            tourn_names[tid] = name
            if tier > 0:
                tourn_tiers[tid] = tier

            if sofa_id:
                tourn_names[sofa_id] = name
                if tier > 0:
                    tourn_tiers[sofa_id] = tier
        cur.close()
        conn.close()
        print(f"  Loaded {len(tourn_names):,} tournament mappings from PostgreSQL mdm_tournament.")
    except Exception as e:
        print(f"  Notice fetching tournaments from PG: {e}")

    # Fallback/enrich from ClickHouse dict_tournament
    print("  [Meta 1/2] Enriching tournament metadata from ClickHouse dict_tournament...")
    try:
        sql = "SELECT id, canonical_name, tier FROM igaming_dds.dict_tournament WHERE id > 0"
        for parts in query_clickhouse_tsv(sql):
            if len(parts) >= 3:
                tid = int(parts[0])
                name = parts[1]
                tier = int(parts[2]) if parts[2].isdigit() else 0
                if tid not in tourn_names and name:
                    tourn_names[tid] = name
                if tid not in tourn_tiers and tier > 0:
                    tourn_tiers[tid] = tier
        print(f"  Total enriched tournament mappings: {len(tourn_names):,}")
    except Exception as e:
        print(f"  Notice fetching tournaments from ClickHouse: {e}")

    return tourn_names, tourn_tiers


SPORT_MDM_IDS = {
    "volleyball": [7, 61],
    "handball": [8, 62],
    "baseball": [9],
    "ice-hockey": [2, 64],
    "basketball": [3, 57, 58],
    "cs2": [33, 6],
    "dota2": [34, 6],
    "tennis-doubles": [45, 65],
    "table-tennis": [5, 63]
}


def load_team_metadata_for_sports(sport_slugs: List[str]) -> Dict[int, Tuple[str, str]]:
    """
    Loads team/player canonical name and country from PostgreSQL mdm_team filtered by sport_id.
    """
    team_meta = {}
    target_sport_ids = set()
    for s in sport_slugs:
        target_sport_ids.update(SPORT_MDM_IDS.get(s, []))

    if not target_sport_ids:
        target_sport_ids = {2, 3, 5, 7, 8, 9, 33, 34, 45, 57, 58, 61, 62, 63, 64, 65}

    print(f"  [Meta 2/2] Loading team/player metadata for sports {list(target_sport_ids)} from PostgreSQL mdm_team...")
    t0 = time.time()
    try:
        conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname=PG_MDM_DB)
        cur = conn.cursor()
        cur.execute(
            "SELECT id, canonical_name, coalesce(country_name, ''), sofascore_id "
            "FROM mdm_team "
            "WHERE sport_id = ANY(%s) AND canonical_name IS NOT NULL",
            (list(target_sport_ids),)
        )
        for row in cur.fetchall():
            tid = int(row[0])
            name = str(row[1])
            country = str(row[2]) if row[2] else ""
            sofa_id = int(row[3]) if row[3] else None
            team_meta[tid] = (name, country)
            if sofa_id:
                team_meta[sofa_id] = (name, country)
        cur.close()
        conn.close()
        print(f"  Loaded {len(team_meta):,} team/player names in {time.time() - t0:.2f}s.")
    except Exception as e:
        print(f"  Notice fetching teams from PG: {e}")

    return team_meta


def classify_tier(sport_slug: str, tourn_id: int, tourn_names: Dict[int, str], tourn_tiers: Dict[int, int]) -> Tuple[int, int]:
    """
    Determines tournament (tier_level, soft_cap_mmr) for any sport.
    """
    # 1. Check verified tier from DB
    raw_tier = tourn_tiers.get(tourn_id, 0)
    t_name = tourn_names.get(tourn_id, "").lower()

    # 2. Check keyword patterns for sport
    kw_map = SPORT_TIER_KEYWORDS.get(sport_slug, {})
    for tier_lvl in (1, 2, 3, 4, 5):
        if tier_lvl in kw_map:
            for kw in kw_map[tier_lvl]:
                if kw in t_name:
                    tier_cfg = UNIVERSAL_TIERS.get(tier_lvl, UNIVERSAL_TIERS[3])
                    return tier_lvl, tier_cfg["soft_cap"]

    # 3. If raw tier is 1 or 2, use it
    if raw_tier in (1, 2):
        tier_cfg = UNIVERSAL_TIERS.get(raw_tier, UNIVERSAL_TIERS[2])
        return raw_tier, tier_cfg["soft_cap"]

    # 4. Check if raw tier is 3
    if raw_tier == 3:
        return 3, UNIVERSAL_TIERS[3]["soft_cap"]

    # 5. Sensible defaults by sport
    if sport_slug in ("baseball", "basketball", "ice-hockey", "volleyball", "handball"):
        # Most matches in DB are professional leagues
        return 2, UNIVERSAL_TIERS[2]["soft_cap"]
    elif sport_slug in ("cs2", "dota2"):
        return 3, UNIVERSAL_TIERS[3]["soft_cap"]
    elif sport_slug == "tennis-doubles":
        return 4, UNIVERSAL_TIERS[4]["soft_cap"]

    return 3, UNIVERSAL_TIERS[3]["soft_cap"]


# Sport Specification Registry
SPORT_CONFIGS: Dict[str, Dict[str, Any]] = {
    "volleyball": {
        "table": "volleyball_match_fact",
        "sql": (
            "SELECT home_team_id, away_team_id, match_timestamp, home_score, away_score, coalesce(tournament_id, 0) "
            "FROM igaming_dds.volleyball_match_fact "
            "WHERE (home_score > 0 OR away_score > 0) AND home_team_id > 0 AND away_team_id > 0 "
            "ORDER BY match_timestamp ASC"
        ),
        "mov_fn": lambda s1, s2: 1.0 + (abs(s1 - s2) - 1) * 0.10,
        "engine": MultiTierGlickoEngine()
    },
    "handball": {
        "table": "handball_match_fact",
        "sql": (
            "SELECT home_team_id, away_team_id, match_timestamp, home_score, away_score, coalesce(tournament_id, 0) "
            "FROM igaming_dds.handball_match_fact "
            "WHERE (home_score > 0 OR away_score > 0) AND home_team_id > 0 AND away_team_id > 0 "
            "ORDER BY match_timestamp ASC"
        ),
        "mov_fn": lambda s1, s2: 1.0 + min(0.30, abs(s1 - s2) * 0.02),
        "engine": MultiTierGlickoEngine()
    },
    "baseball": {
        "table": "baseball_match_fact",
        "sql": (
            "SELECT home_team_id, away_team_id, match_timestamp, home_score, away_score, coalesce(tournament_id, 0) "
            "FROM igaming_dds.baseball_match_fact "
            "WHERE (home_score > 0 OR away_score > 0) AND home_team_id > 0 AND away_team_id > 0 "
            "ORDER BY match_timestamp ASC"
        ),
        "mov_fn": lambda s1, s2: 1.0 + min(0.30, max(0, abs(s1 - s2) - 1) * 0.03),
        "engine": MultiTierGlickoEngine()
    },
    "ice-hockey": {
        "table": "ice_hockey_match_fact",
        "sql": (
            "SELECT home_team_id, away_team_id, match_timestamp, home_score, away_score, coalesce(tournament_id, 0) "
            "FROM igaming_dds.ice_hockey_match_fact "
            "WHERE (home_score > 0 OR away_score > 0) AND home_team_id > 0 AND away_team_id > 0 "
            "ORDER BY match_timestamp ASC"
        ),
        "mov_fn": lambda s1, s2: 1.0 + min(0.30, max(0, abs(s1 - s2) - 1) * 0.04),
        "engine": MultiTierGlickoEngine()
    },
    "basketball": {
        "table": "basketball_match_fact",
        "sql": (
            "SELECT home_team_id, away_team_id, match_timestamp, home_score, away_score, coalesce(tournament_id, 0) "
            "FROM igaming_dds.basketball_match_fact "
            "WHERE (home_score > 0 OR away_score > 0) AND home_team_id > 0 AND away_team_id > 0 "
            "ORDER BY match_timestamp ASC"
        ),
        "mov_fn": lambda s1, s2: 1.0 + min(0.30, abs(s1 - s2) * 0.01),
        "engine": MultiTierGlickoEngine()
    },
    "cs2": {
        "table": "cs2_match_fact",
        "sql": (
            "SELECT team1_id, team2_id, match_timestamp, team1_score, team2_score, coalesce(tournament_id, 0) "
            "FROM igaming_dds.cs2_match_fact "
            "WHERE (team1_score > 0 OR team2_score > 0) AND team1_id > 0 AND team2_id > 0 "
            "ORDER BY match_timestamp ASC"
        ),
        "mov_fn": lambda s1, s2: 1.0 + min(0.20, max(0, abs(s1 - s2) - 1) * 0.10),
        "engine": MultiTierGlickoEngine()
    },
    "dota2": {
        "table": "dota2_match_fact",
        "sql": (
            "SELECT radiant_team_id, dire_team_id, match_timestamp, "
            "if(radiant_score=0 AND dire_score=0 AND radiant_win=1, 1, radiant_score), "
            "if(radiant_score=0 AND dire_score=0 AND radiant_win=0, 1, dire_score), "
            "coalesce(tournament_id, 0) "
            "FROM igaming_dds.dota2_match_fact "
            "WHERE radiant_team_id > 0 AND dire_team_id > 0 AND (radiant_score > 0 OR dire_score > 0 OR radiant_win > 0) "
            "ORDER BY match_timestamp ASC"
        ),
        "mov_fn": lambda s1, s2: 1.0 + min(0.20, max(0, abs(s1 - s2) - 1) * 0.10),
        "engine": MultiTierGlickoEngine()
    },
    "tennis-doubles": {
        "table": "tennis_doubles_match_fact",
        "sql": (
            "SELECT tandem1_id, tandem2_id, match_timestamp, score1, score2, coalesce(tournament_id, 0), "
            "tournament_name, t1_p1_name, t1_p2_name, t2_p1_name, t2_p2_name "
            "FROM igaming_dds.tennis_doubles_match_fact "
            "WHERE (score1 > 0 OR score2 > 0) AND tandem1_id > 0 AND tandem2_id > 0 "
            "ORDER BY match_timestamp ASC"
        ),
        "mov_fn": lambda s1, s2: 1.0 + (abs(s1 - s2) - 1) * 0.10,
        "engine": MultiTierGlickoEngine(),
        "is_doubles": True
    }
}


def run_sport_backfill(
    sport_slug: str,
    tourn_names: Dict[int, str],
    tourn_tiers: Dict[int, int],
    team_meta: Dict[int, Tuple[str, str]]
):
    cfg = SPORT_CONFIGS.get(sport_slug)
    if not cfg:
        print(f"Unknown sport slug: {sport_slug}")
        return

    print("=" * 80)
    print(f"🏆 STARTING DYNAMIC MMR BACKFILL FOR: {sport_slug.upper()} ({cfg['table']})")
    print("=" * 80)

    engine: MultiTierGlickoEngine = cfg["engine"]
    mov_fn = cfg["mov_fn"]
    is_doubles = cfg.get("is_doubles", False)

    ratings: Dict[int, GlickoPlayer] = {}
    tandem_names: Dict[int, str] = {}
    recent_h2h: Dict[Tuple[int, int], List[int]] = {}

    def get_player(pid: int) -> GlickoPlayer:
        if pid not in ratings:
            p = GlickoPlayer(pid, initial_mmr=1500.0, initial_rd=350.0, initial_vol=0.06)
            p.last_match_time = 0
            ratings[pid] = p
        return ratings[pid]

    print(f"  Streaming matches from {cfg['table']}...")
    t_start = time.time()
    count = 0

    for m in query_clickhouse_tsv(cfg["sql"]):
        p1_id = int(m[0])
        p2_id = int(m[1])
        ts = int(m[2])
        s1 = int(m[3])
        s2 = int(m[4])
        tourn_id = int(m[5]) if len(m) > 5 and m[5] and m[5] != '\\N' else 0

        # Tennis doubles inline tournament name & tandem player names
        if is_doubles and len(m) >= 11:
            t_name = m[6] if m[6] and m[6] != '\\N' else ""
            if tourn_id > 0 and tourn_id not in tourn_names and t_name:
                tourn_names[tourn_id] = t_name
            t1_name = f"{m[7]} / {m[8]}" if m[7] and m[8] else ""
            t2_name = f"{m[9]} / {m[10]}" if m[9] and m[10] else ""
            if t1_name and p1_id not in tandem_names:
                tandem_names[p1_id] = t1_name
            if t2_name and p2_id not in tandem_names:
                tandem_names[p2_id] = t2_name

        tier, soft_cap = classify_tier(sport_slug, tourn_id, tourn_names, tourn_tiers)
        mov = mov_fn(s1, s2)

        p1 = get_player(p1_id)
        p2 = get_player(p2_id)
        p1.last_match_time = ts
        p2.last_match_time = ts

        # Track 7-day rolling H2H count for diminishing returns on rematches
        h2h_key = (min(p1_id, p2_id), max(p1_id, p2_id))
        seven_days_ago = ts - (7 * 86400)
        h2h_list = recent_h2h.get(h2h_key, [])
        h2h_list = [t for t in h2h_list if t >= seven_days_ago]
        h2h_count = len(h2h_list) + 1
        h2h_list.append(ts)
        recent_h2h[h2h_key] = h2h_list

        engine.apply_match_update(p1, p2, s1, s2, tier, soft_cap, mov_multiplier=mov, h2h_count=h2h_count)

        count += 1
        if count % 20000 == 0:
            print(f"    Replayed {count:,} matches... (Active entities: {len(ratings):,})")

    duration = time.time() - t_start
    throughput = count / duration if duration > 0 else 0
    print(f"\n  ✅ Replay Complete! Processed {count:,} matches in {duration:.2f}s ({throughput:.0f} matches/sec)!")
    print(f"  Total unique rated entities: {len(ratings):,}")

    if not ratings:
        print(f"  No rated entities found for {sport_slug}, skipping persistence.")
        return

    # Statistical Evaluation (3-Level Drill-Down)
    min_mmr = min(p.mmr for p in ratings.values())
    avg_mmr = sum(p.mmr for p in ratings.values()) / len(ratings)
    max_mmr = max(p.mmr for p in ratings.values())

    print(f"\n  📈 Rating Summary for {sport_slug}:")
    print(f"     Min MMR: {min_mmr:.1f} | Avg MMR: {avg_mmr:.1f} | Max MMR: {max_mmr:.1f}")

    # Top 15 Entities
    active_players = [p for p in ratings.values() if p.matches_played >= 3]
    if not active_players:
        active_players = list(ratings.values())
    active_players.sort(key=lambda x: x.mmr, reverse=True)

    print(f"\n  🏆 TOP 15 ENTITIES BY MMR ({sport_slug.upper()}):")
    print(f"  {'Rank':<5} | {'ID':<10} | {'Name':<32} | {'MMR':<7} | {'RD':<5} | {'Matches':<7} | {'Win %':<6} | {'Peak':<7}")
    print("  " + "-" * 90)
    for idx, p in enumerate(active_players[:15], 1):
        if is_doubles and p.player_id in tandem_names:
            pname = tandem_names[p.player_id][:30]
        else:
            name_info = team_meta.get(p.player_id, (f"Entity #{p.player_id}", ""))
            pname = name_info[0][:30]
        wr = (p.wins / p.matches_played * 100) if p.matches_played > 0 else 0
        print(f"  #{idx:<4} | {p.player_id:<10} | {pname:<32} | {p.mmr:<7.1f} | {p.rd:<5.1f} | {p.matches_played:<7} | {wr:<5.1f}% | {p.peak_mmr:<7.1f}")

    # Tier Breakdown
    bins = [
        (2200, 3200, "World Elite / Premier S-Tier"),
        (1900, 2200, "Top National / Major Continental"),
        (1650, 1900, "Secondary National / Regional"),
        (1400, 1650, "Core Division"),
        (1150, 1400, "Lower Division"),
        (500,  1150, "Development / Amateur")
    ]
    print(f"\n  📊 MMR DISTRIBUTION BREAKDOWN ({sport_slug}):")
    for low, high, label in bins:
        matching = [p for p in active_players if low <= p.mmr < high]
        pct = (len(matching) / len(active_players) * 100) if active_players else 0
        print(f"     {label:<34} ({low:>4} - {high:>4} MMR): {len(matching):>6} ({pct:>5.1f}%)")

    # Export to ClickHouse igaming_dds.sport_mmr_rating via TabSeparated streaming
    print(f"\n  [ClickHouse] Exporting {len(ratings):,} rating rows to igaming_dds.sport_mmr_rating...")
    tsv_lines = []
    now_str = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    for pid, p in ratings.items():
        if is_doubles and pid in tandem_names:
            pname = tandem_names[pid].replace('\t', ' ').replace('\n', ' ')
            pcountry = ""
        else:
            name_info = team_meta.get(pid, ("", ""))
            pname = name_info[0].replace('\t', ' ').replace('\n', ' ')
            pcountry = name_info[1].replace('\t', ' ').replace('\n', ' ')
        last_dt = datetime.fromtimestamp(p.last_match_time).strftime('%Y-%m-%d %H:%M:%S') if getattr(p, 'last_match_time', 0) > 0 else '2024-01-01 00:00:00'
        tsv_lines.append(f"{pid}\t{sport_slug}\tALL\t{p.mmr:.2f}\t{p.rd:.2f}\t{p.vol:.4f}\t{p.matches_played}\t{p.wins}\t{p.losses}\t{p.streak}\t{p.peak_mmr:.2f}\t{pname}\t{pcountry}\t{last_dt}\t{now_str}\n")

    batch_size = 10000
    t_ch = time.time()
    for i in range(0, len(tsv_lines), batch_size):
        chunk = "".join(tsv_lines[i:i + batch_size])
        resp = requests.post(
            f"{CLICKHOUSE_URL}/",
            params={"query": "INSERT INTO igaming_dds.sport_mmr_rating (player_id, sport_slug, surface, mmr, rating_deviation, volatility, matches_played, wins_count, losses_count, current_streak, peak_mmr, player_name, country, last_match_at, updated_at) FORMAT TabSeparated"},
            data=chunk.encode("utf-8"),
            auth=(CLICKHOUSE_USER, CLICKHOUSE_PASSWORD),
            timeout=60
        )
        if resp.status_code != 200:
            print(f"ClickHouse insert error: {resp.text}")
        resp.raise_for_status()

    print(f"  [ClickHouse] Successfully inserted {len(tsv_lines):,} rows in {time.time() - t_ch:.2f}s!")

    # Persist to PostgreSQL igaming_analytics.sport_mmr_rating
    print(f"  [PostgreSQL] Upserting active ratings to igaming_analytics.sport_mmr_rating...")
    try:
        conn_pg = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASS, dbname=PG_ANALYTICS_DB)
        cur = conn_pg.cursor()
        pg_tuples = []
        for pid, p in ratings.items():
            if is_doubles and pid in tandem_names:
                pname = tandem_names[pid]
                pcountry = ""
            else:
                name_info = team_meta.get(pid, ("", ""))
                pname = name_info[0]
                pcountry = name_info[1]
            last_dt = datetime.fromtimestamp(p.last_match_time) if getattr(p, 'last_match_time', 0) > 0 else datetime(2024, 1, 1)
            pg_tuples.append((
                pid, sport_slug, 'ALL',
                float(round(p.mmr, 2)), float(round(p.rd, 2)), float(round(p.vol, 4)),
                int(p.matches_played), int(p.wins), int(p.losses), int(p.streak),
                float(round(p.peak_mmr, 2)), pname, pcountry, last_dt
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
        print(f"  [PostgreSQL] Successfully upserted {len(pg_tuples):,} rows!")
    except Exception as e:
        print(f"  Notice: PostgreSQL persistence failed: {e}")

    print(f"🎉 Complete for {sport_slug}!\n")


def main():
    parser = argparse.ArgumentParser(description="Universal Multi-Sport MMR Backfill Engine")
    parser.add_argument("--sport", type=str, default="", help="Specific sport slug to backfill (e.g. volleyball, cs2)")
    parser.add_argument("--all", action="store_true", help="Run backfill for all supported sports")
    args = parser.parse_args()

    sports_to_run = []
    if args.sport:
        sports_to_run = [args.sport.strip().lower().replace('_', '-')]
    elif args.all:
        sports_to_run = list(SPORT_CONFIGS.keys())
    else:
        print("Please specify --sport <slug> or --all. Supported sports:")
        for s in SPORT_CONFIGS.keys():
            print(f"  - {s}")
        sys.exit(1)

    print("=" * 80)
    print("🚀 INITIALIZING UNIVERSAL MULTI-SPORT MMR BACKFILL ENGINE")
    print(f"Target Sports: {', '.join(sports_to_run)}")
    print("=" * 80)

    # 1. Load Global Metadata
    t_meta = time.time()
    tourn_names, tourn_tiers = load_tournament_metadata()
    team_meta = load_team_metadata_for_sports(sports_to_run)
    print(f"Metadata initialized in {time.time() - t_meta:.2f}s.\n")

    # 2. Replay & Backfill for each target sport
    for sport in sports_to_run:
        if sport not in SPORT_CONFIGS:
            print(f"Warning: Sport '{sport}' not registered in SPORT_CONFIGS. Skipping.")
            continue
        run_sport_backfill(sport, tourn_names, tourn_tiers, team_meta)

    # 3. Optimize ClickHouse Table
    print("=" * 80)
    print("🔧 Running OPTIMIZE TABLE igaming_dds.sport_mmr_rating FINAL...")
    try:
        resp = requests.post(
            f"{CLICKHOUSE_URL}/",
            params={"query": "OPTIMIZE TABLE igaming_dds.sport_mmr_rating FINAL"},
            auth=(CLICKHOUSE_USER, CLICKHOUSE_PASSWORD),
            timeout=120
        )
        print("✅ OPTIMIZE TABLE completed successfully!")
    except Exception as e:
        print(f"Notice during OPTIMIZE TABLE: {e}")

    print("=" * 80)
    print("🎉 UNIVERSAL MULTI-SPORT MMR BACKFILL COMPLETE!")
    print("=" * 80)


if __name__ == "__main__":
    main()
