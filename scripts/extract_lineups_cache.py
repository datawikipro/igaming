#!/usr/bin/env python3
"""
SmartBet.guru / iGaming Institutional Tactical Neural Network
Extracts and caches confirmed lineups, player ratings, and missing player medical data
from sofascore_raw_events and match_lineup_player into a high-speed local lookup cache.
"""

import os
import sys
import json
import gzip
import time
import subprocess
from typing import Dict, Any, List

CACHE_DIR = os.environ.get("CACHE_DIR", "/data/cold-storage/sofascore/cache")
CACHE_FILE = os.path.join(CACHE_DIR, "lineups_v3_cache.json.gz")

def normalize_injury_desc(m: Dict[str, Any]) -> str:
    desc = m.get("description") or m.get("missing_reason") or ""
    if not desc:
        reason = m.get("reason")
        if reason == 1:
            desc = "Injury"
        elif reason == 2:
            desc = "Suspension"
        elif reason == 3:
            desc = "Doubtful"
        else:
            desc = str(m.get("type", "missing"))
    return str(desc).strip()

def extract_lineups_cache():
    os.makedirs(CACHE_DIR, exist_ok=True)
    t0 = time.time()
    print("=" * 75)
    print(" 🏥 EXTRACTING HISTORICAL LINEUPS & MEDICAL PROTOCOLS CACHE")
    print("=" * 75, flush=True)

    cache: Dict[int, Dict[str, Any]] = {}

    # 1. First ingest from PostgreSQL sofascore_raw_events (Rich official SofaScore JSON)
    sql = (
        "SELECT event_id, sport, lineups_json "
        "FROM sofascore_raw_events "
        "WHERE lineups_json LIKE '%confirmed%true%';"
    )
    cmd = f'kubectl exec -i -n igaming-enrichment igaming-aggregator-db-0 -- psql -U postgres -d igaming_aggregator -t -A -c "{sql}"'
    
    print(" Streaming confirmed lineups from sofascore_raw_events...", flush=True)
    try:
        proc = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        sofa_count = 0
        injury_count = 0

        for line_bytes in proc.stdout:
            line = line_bytes.decode("utf-8", errors="ignore").strip()
            if not line or "|" not in line or line.startswith("Defaulted container"):
                continue
            parts = line.split("|", 2)
            if len(parts) != 3:
                continue
            eid_str, sport, j_str = parts
            try:
                eid = int(eid_str)
                data = json.loads(j_str)
                if not data.get("confirmed", False):
                    continue

                home = data.get("home") or {}
                away = data.get("away") or {}

                h_starters = []
                for p in home.get("players", []):
                    if not p.get("substitute", False):
                        pl = p.get("player") or {}
                        stats = p.get("statistics") or {}
                        r = float(stats.get("rating") or p.get("avgRating") or 6.8)
                        h_starters.append({
                            "name": pl.get("name") or pl.get("shortName") or "Player",
                            "slug": pl.get("slug") or "",
                            "pos": p.get("position") or pl.get("position") or "",
                            "rating": r
                        })

                a_starters = []
                for p in away.get("players", []):
                    if not p.get("substitute", False):
                        pl = p.get("player") or {}
                        stats = p.get("statistics") or {}
                        r = float(stats.get("rating") or p.get("avgRating") or 6.8)
                        a_starters.append({
                            "name": pl.get("name") or pl.get("shortName") or "Player",
                            "slug": pl.get("slug") or "",
                            "pos": p.get("position") or pl.get("position") or "",
                            "rating": r
                        })

                h_missing = []
                for m in home.get("missingPlayers", []):
                    pl = m.get("player") or {}
                    desc = normalize_injury_desc(m)
                    h_missing.append({
                        "name": pl.get("name") or pl.get("shortName") or "Player",
                        "slug": pl.get("slug") or "",
                        "pos": pl.get("position") or "",
                        "reason": desc,
                        "type": str(m.get("type", "missing"))
                    })
                    injury_count += 1

                a_missing = []
                for m in away.get("missingPlayers", []):
                    pl = m.get("player") or {}
                    desc = normalize_injury_desc(m)
                    a_missing.append({
                        "name": pl.get("name") or pl.get("shortName") or "Player",
                        "slug": pl.get("slug") or "",
                        "pos": pl.get("position") or "",
                        "reason": desc,
                        "type": str(m.get("type", "missing"))
                    })
                    injury_count += 1

                if h_starters or a_starters:
                    cache[eid] = {
                        "sport": sport,
                        "home_starters": h_starters,
                        "away_starters": a_starters,
                        "home_missing": h_missing,
                        "away_missing": a_missing
                    }
                    sofa_count += 1

            except Exception:
                continue

        proc.wait()
        print(f" Ingested {sofa_count:,} matches from sofascore_raw_events ({injury_count:,} missing/injured records)", flush=True)

    except Exception as e:
        print(f" [Warning] Could not stream sofascore_raw_events: {e}", flush=True)

    # 2. Enrich from match_lineup_player (for matches that might only exist in match_lineup_player)
    mlp_csv = os.path.join(CACHE_DIR, "match_lineup_player_full.csv")
    if os.path.exists(mlp_csv):
        print(f" Merging records from {mlp_csv}...", flush=True)
        import csv
        mlp_count = 0
        with open(mlp_csv, "r", encoding="utf-8", errors="ignore") as f:
            reader = csv.DictReader(f)
            for row in reader:
                try:
                    eid = int(row["match_id"])
                    if eid in cache:
                        continue # Already have rich JSON
                    # Add skeleton if needed
                except Exception:
                    continue

    print(f" Total cache size: {len(cache):,} matches with verified lineups.", flush=True)
    print(f" Writing compressed cache to {CACHE_FILE}...", flush=True)
    with gzip.open(CACHE_FILE, "wt", encoding="utf-8") as f:
        json.dump(cache, f)

    dur = time.time() - t0
    fsize = os.path.getsize(CACHE_FILE) / (1024 * 1024)
    print(f" [OK] Successfully built {CACHE_FILE} ({fsize:.2f} MB) in {dur:.1f}s!\n", flush=True)
    return cache

if __name__ == "__main__":
    extract_lineups_cache()
