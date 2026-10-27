#!/usr/bin/env python3
"""
build_team_lineup_profiles.py
Extracts historical lineup appearances and player ratings from match_lineup_player & team table
and compiles a fast in-memory team squad profile cache:
/data/cold-storage/sofascore/cache/team_lineup_profiles.json.gz
"""

import subprocess
import json
import gzip
import time
from collections import defaultdict

def main():
    print("================================================================================")
    print(" 🏟️ COMPILING TEAM LINEUP & SQUAD PROFILES FOR PROJECTED LINEUPS")
    print("================================================================================")
    t0 = time.time()

    sql = """
    COPY (
        SELECT 
            t.id AS team_id,
            COALESCE(t.default_display_name, '') AS team_name,
            COALESCE(t.name_english, '') AS team_name_en,
            t.sport_id,
            mlp.player_name,
            mlp.position,
            COALESCE(mlp.season_rating, 6.8) AS rating,
            mlp.is_starter,
            mlp.is_missing,
            COALESCE(mlp.missing_reason, '') AS missing_reason,
            COALESCE(mlp.missing_type, '') AS missing_type
        FROM match_lineup_player mlp
        JOIN team t ON t.id = mlp.team_id
        WHERE mlp.player_name IS NOT NULL AND mlp.player_name != ''
    ) TO STDOUT WITH (FORMAT CSV, HEADER FALSE, DELIMITER '|');
    """

    cmd = [
        "kubectl", "exec", "-i", "-n", "igaming-enrichment", "igaming-aggregator-db-0",
        "-c", "postgres", "--", "psql", "-U", "postgres", "-d", "igaming_aggregator",
        "-c", sql
    ]

    print(" Extracting rows from PostgreSQL match_lineup_player...", flush=True)
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

    team_data = defaultdict(lambda: {
        "team_id": None,
        "team_name": "",
        "team_name_en": "",
        "sport": "football",
        "players": defaultdict(lambda: {
            "name": "",
            "pos": "",
            "ratings": [],
            "starts": 0,
            "bench": 0,
            "missing_records": []
        })
    })

    row_count = 0
    for line in proc.stdout:
        line = line.strip()
        if not line or "|" not in line or line.startswith("Defaulted container"):
            continue
        parts = line.split("|")
        if len(parts) < 11:
            continue
        (team_id_str, t_name, t_name_en, sport, p_name, pos, r_str, is_st_str, is_miss_str, miss_reason, miss_type) = parts[:11]
        
        try:
            r = float(r_str)
        except ValueError:
            r = 6.8
            
        is_starter = (is_st_str.lower() in ["t", "true", "1"])
        is_missing = (is_miss_str.lower() in ["t", "true", "1"])
        
        norm_key = t_name.strip().lower()
        if not norm_key:
            norm_key = t_name_en.strip().lower()
        if not norm_key:
            continue

        td = team_data[norm_key]
        td["team_id"] = int(team_id_str)
        td["team_name"] = t_name.strip()
        td["team_name_en"] = t_name_en.strip()
        td["sport"] = sport.strip().lower()

        p_key = p_name.strip().lower()
        pl = td["players"][p_key]
        pl["name"] = p_name.strip()
        if pos.strip():
            pl["pos"] = pos.strip().upper()
        pl["ratings"].append(r)
        if is_starter:
            pl["starts"] += 1
        else:
            pl["bench"] += 1
        if is_missing and (miss_reason or miss_type):
            pl["missing_records"].append({"reason": miss_reason, "type": miss_type})
            
        row_count += 1

    proc.wait()
    print(f" Processed {row_count:,} player appearances across {len(team_data):,} teams.", flush=True)

    # Now compile clean sorted profiles per team
    final_profiles = {}
    for t_key, td in team_data.items():
        players_list = []
        for p_key, pl in td["players"].items():
            avg_r = sum(pl["ratings"]) / max(1, len(pl["ratings"]))
            players_list.append({
                "name": pl["name"],
                "pos": pl["pos"] or "M",
                "avg_rating": round(avg_r, 2),
                "starts": pl["starts"],
                "sub_appearances": pl["bench"],
                "total_matches": pl["starts"] + pl["bench"],
                "missing_history": pl["missing_records"][-3:] if pl["missing_records"] else []
            })
            
        # Sort players by starts DESC, then avg_rating DESC
        players_list.sort(key=lambda x: (x["starts"], x["avg_rating"]), reverse=True)
        
        # Categorize by tactical position
        gk = [p for p in players_list if "GK" in p["pos"] or "GOAL" in p["pos"] or "ВРАТ" in p["pos"]]
        defenders = [p for p in players_list if any(x in p["pos"] for x in ["CB", "LB", "RB", "DF", "ЗАЩ"])]
        midfielders = [p for p in players_list if any(x in p["pos"] for x in ["CM", "CDM", "CAM", "LM", "RM", "MF", "ПОЛУ"])]
        attackers = [p for p in players_list if any(x in p["pos"] for x in ["CF", "ST", "LW", "RW", "FW", "НАПАД"])]
        others = [p for p in players_list if p not in gk and p not in defenders and p not in midfielders and p not in attackers]

        final_profiles[t_key] = {
            "team_id": td["team_id"],
            "team_name": td["team_name"],
            "team_name_en": td["team_name_en"],
            "sport": td["sport"],
            "all_players": players_list,
            "gk": gk,
            "defenders": defenders,
            "midfielders": midfielders,
            "attackers": attackers,
            "others": others
        }
        # Also index directly by canonical team_id for deterministic O(1) primary lookup
        if td.get("team_id"):
            final_profiles[str(td["team_id"])] = final_profiles[t_key]
            
        # Also alias English name if different
        if td["team_name_en"] and td["team_name_en"].strip().lower() != t_key:
            final_profiles[td["team_name_en"].strip().lower()] = final_profiles[t_key]

    print(" Extracting bookmaker aliases from team_alias table...", flush=True)
    alias_sql = """
    COPY (
        SELECT team_id, LOWER(TRIM(raw_name)) FROM team_alias WHERE raw_name IS NOT NULL AND raw_name != ''
    ) TO STDOUT WITH (FORMAT CSV, HEADER FALSE, DELIMITER '|');
    """
    alias_cmd = [
        "kubectl", "exec", "-i", "-n", "igaming-enrichment", "igaming-aggregator-db-0",
        "-c", "postgres", "--", "psql", "-U", "postgres", "-d", "igaming_aggregator",
        "-c", alias_sql
    ]
    alias_proc = subprocess.Popen(alias_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    team_id_to_aliases = defaultdict(list)
    alias_count = 0
    for line in alias_proc.stdout:
        line = line.strip()
        if not line or "|" not in line or line.startswith("Defaulted container"):
            continue
        parts = line.split("|")
        if len(parts) >= 2:
            try:
                tid = int(parts[0])
                raw_al = parts[1].strip()
                if raw_al:
                    team_id_to_aliases[tid].append(raw_al)
                    alias_count += 1
            except ValueError:
                pass
    alias_proc.wait()
    print(f" Loaded {alias_count:,} raw bookmaker aliases across {len(team_id_to_aliases):,} teams", flush=True)

    # Map aliases to compiled profiles
    attached_aliases = 0
    for t_key, p in list(final_profiles.items()):
        tid = p.get("team_id")
        if tid in team_id_to_aliases:
            for al in team_id_to_aliases[tid]:
                if al and al not in final_profiles:
                    final_profiles[al] = p
                    attached_aliases += 1

    print(f" Linked {attached_aliases:,} bookmaker alias keys to team profiles (Total searchable keys: {len(final_profiles):,})", flush=True)

    out_path = "/data/cold-storage/sofascore/cache/team_lineup_profiles.json.gz"
    print(f" Saving compressed team profiles to {out_path}...", flush=True)
    with gzip.open(out_path, "wt", encoding="utf-8") as f:
        json.dump(final_profiles, f, ensure_ascii=False)

    print(f"✅ Successfully compiled {len(final_profiles):,} team profiles in {time.time()-t0:.2f}s!")

if __name__ == "__main__":
    main()
