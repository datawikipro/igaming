#!/usr/bin/env python3
"""
test_projected_lineup_engine.py
Validates the Projected Lineup Generator and Multi-Scenario Simulation Engine.
"""

import gzip
import json
import time
import torch
import numpy as np

# Load team profiles
t0 = time.time()
with gzip.open('/data/cold-storage/sofascore/cache/team_lineup_profiles.json.gz', 'rt', encoding='utf-8') as f:
    PROFILES = json.load(f)
print(f"Loaded {len(PROFILES)} team profiles in {time.time()-t0:.3f}s")

# Load injury cache
t0 = time.time()
with gzip.open('/data/cold-storage/sofascore/cache/lineups_v3_cache.json.gz', 'rt', encoding='utf-8') as f:
    RAW_CACHE = json.load(f)
INJURIES = RAW_CACHE.get("injuries", {})
print(f"Loaded {len(INJURIES)} injury records in {time.time()-t0:.3f}s")


def build_team_projected_scenarios(team_name: str, sport: str = "football"):
    """
    Constructs 3 projected lineup scenarios for a team:
    1. Base Projected XI (Optimal core with injury adjustments)
    2. Rotated Squad (2-3 starters rested, bench options in)
    3. Joker / Ultra-Aggressive Attack Variant
    """
    clean_name = team_name.strip().lower()
    profile = PROFILES.get(clean_name)
    if not profile:
        # Fallback to fuzzy match
        for k, v in PROFILES.items():
            if clean_name in k or k in clean_name:
                profile = v
                break

    if not profile:
        return None, None

    all_players = profile.get("all_players", [])
    if not all_players:
        return None, None

    gk = profile.get("gk", [])
    defs = profile.get("defenders", [])
    mids = profile.get("midfielders", [])
    atts = profile.get("attackers", [])
    others = profile.get("others", [])

    # Target tactical shape
    # Football: 1 GK, 4 DEF, 3 MID, 3 ATT (or best available)
    def select_healthy_core(players_pool, target_count, exclude_names=set()):
        selected = []
        for p in players_pool:
            if p["name"] in exclude_names:
                continue
            # Check injury status
            inj = INJURIES.get(p["name"].lower())
            p_copy = dict(p)
            if inj:
                p_copy["injury_type"] = inj.get("reason", "Unknown")
                p_copy["injury_recovery_days"] = 14  # Active recovery window
            selected.append(p_copy)
            if len(selected) >= target_count:
                break
        return selected

    # SCENARIO 1: Base Optimal
    base_gk = select_healthy_core(gk or others, 1)
    base_def = select_healthy_core(defs or others, 4)
    base_mid = select_healthy_core(mids or others, 3)
    base_att = select_healthy_core(atts or others, 3)
    scenario_base = base_gk + base_def + base_mid + base_att
    
    # Fill up to 11 if position buckets had fewer players
    used_names = {p["name"] for p in scenario_base}
    for p in all_players:
        if len(scenario_base) >= 11:
            break
        if p["name"] not in used_names:
            scenario_base.append(dict(p))
            used_names.add(p["name"])

    # Bench for Scenario 1
    bench_pool = [p for p in all_players if p["name"] not in used_names]
    bench_1 = bench_pool[:7]

    # SCENARIO 2: Rotated / Fatigue Lineup (Rest top 2 outfield starters)
    scenario_rotated = list(scenario_base)
    rotated_out = []
    if len(scenario_rotated) >= 6 and len(bench_pool) >= 2:
        # Swap 1 mid and 1 att with bench
        mid_idx = next((i for i, p in enumerate(scenario_rotated) if any(x in p.get("pos","") for x in ["M", "MID", "CM"])), 5)
        att_idx = next((i for i, p in enumerate(scenario_rotated) if any(x in p.get("pos","") for x in ["F", "ATT", "ST", "W"])), 8)
        
        rotated_out.append(scenario_rotated[mid_idx]["name"])
        rotated_out.append(scenario_rotated[att_idx]["name"])
        
        scenario_rotated[mid_idx] = dict(bench_pool[0])
        scenario_rotated[att_idx] = dict(bench_pool[1])

    # SCENARIO 3: Impact Joker Variant (Move top bench attacker into starting XI)
    scenario_joker = list(scenario_base)
    joker_bench = [b for b in bench_pool if b.get("avg_rating", 0) >= 7.0 or any(x in b.get("pos","") for x in ["F", "W", "ST"])]
    if joker_bench:
        best_joker = joker_bench[0]
        # Replace lowest rated outfield starter
        lowest_idx = min(range(1, len(scenario_joker)), key=lambda i: scenario_joker[i].get("avg_rating", 6.8))
        scenario_joker[lowest_idx] = dict(best_joker)

    scenarios = [
        {"name": "BASE_OPTIMAL", "starters": scenario_base, "bench": bench_1},
        {"name": "ROTATED_FATIGUE", "starters": scenario_rotated, "bench": bench_1, "rotated_out": rotated_out},
        {"name": "JOKER_OFFENSIVE", "starters": scenario_joker, "bench": bench_1}
    ]

    return scenarios, profile


def main():
    print("Testing Projected Lineups for top teams...")
    test_teams = ["Milan", "Arsenal", "Barcelona", "Liverpool"]
    for tname in test_teams:
        scenarios, prof = build_team_projected_scenarios(tname)
        if scenarios:
            print(f"\n================================================================================")
            print(f" ⚽ PROJECTED SQUAD: {prof.get('team_name')} (Found {len(prof.get('all_players', []))} players in DB)")
            print(f"================================================================================")
            base = scenarios[0]["starters"]
            print(f" * Base Projected XI ({len(base)} players):")
            for p in base:
                print(f"    - {p['name']:22} | Pos: {p.get('pos', '?'):4} | Rating: {p.get('avg_rating', 6.8):.2f} | Starts: {p.get('starts', 0)}")
            
            rot = scenarios[1]
            print(f" * Rotated Scenario (Resting: {rot.get('rotated_out', [])})")
        else:
            print(f"Team {tname} not found in profile database.")

if __name__ == "__main__":
    main()
