#!/usr/bin/env python3
"""
MDM Blueprint Seeding for FastSports & FlashScore (Table Tennis & Tennis).
Populates mdm_tournament_ingestion_map and mdm_team_ingestion_map
for external systems:
  170: 'flashscore'
  171: 'ligapro'
  172: 'setkacup'
  173: 'ttcup'
using phonetic, signature, and name-permutation normalization.
"""

import os
import re
import sys
import psycopg2
from psycopg2.extras import execute_values

DB_URL = os.environ.get("MDM_DB_URL", "postgresql://postgres:postgres@10.108.82.251:5432/igaming_mdm")

# Cyrillic to Latin phonetic transliteration table
CYR_TO_LAT = {
    'а': 'a', 'б': 'b', 'в': 'v', 'г': 'g', 'д': 'd', 'е': 'e', 'ё': 'e', 'ж': 'zh',
    'з': 'z', 'и': 'i', 'й': 'y', 'к': 'k', 'л': 'l', 'м': 'm', 'н': 'n', 'о': 'o',
    'п': 'p', 'р': 'r', 'с': 's', 'т': 't', 'у': 'u', 'ф': 'f', 'х': 'kh', 'ц': 'ts',
    'ч': 'ch', 'ш': 'sh', 'щ': 'shch', 'ъ': '', 'ы': 'y', 'ь': '', 'э': 'e', 'ю': 'yu',
    'я': 'ya'
}

LAT_TO_CYR = {
    'shch': 'щ', 'zh': 'ж', 'ch': 'ч', 'sh': 'ш', 'ts': 'ц', 'yu': 'ю', 'ya': 'я',
    'kh': 'х', 'ph': 'ф',
    'a': 'а', 'b': 'б', 'c': 'к', 'd': 'д', 'e': 'е', 'f': 'ф', 'g': 'г', 'h': 'х',
    'i': 'и', 'j': 'й', 'k': 'к', 'l': 'л', 'm': 'м', 'n': 'н', 'o': 'о', 'p': 'п',
    'q': 'к', 'r': 'р', 's': 'с', 't': 'т', 'u': 'у', 'v': 'в', 'w': 'в', 'x': 'кс',
    'y': 'и', 'z': 'з'
}

def transliterate_cyr_to_lat(text: str) -> str:
    res = []
    for ch in text.lower():
        res.append(CYR_TO_LAT.get(ch, ch))
    return ''.join(res)

def clean_noise(text: str) -> str:
    if not text:
        return ""
    # Remove parenthetical tags like (Рос), (Cze), (W), etc.
    s = re.sub(r'\s*\([^\)]*\)', '', text)
    s = re.sub(r'[\(\)\[\]"\'`]', '', s)
    return s.strip()

def generate_player_keys(name: str, signature: str) -> set:
    """Generates phonetic and signature variations for player mapping."""
    keys = set()
    cleaned = clean_noise(name)
    if not cleaned:
        return keys

    # Add raw cleaned lower
    keys.add(cleaned.lower())
    if signature:
        keys.add(signature.lower().strip())
        keys.add(signature.lower().replace('-', ' ').strip())

    parts = cleaned.split()
    if len(parts) >= 2:
        p1, p2 = parts[0], parts[1]
        # Variation: First Last vs Last First
        keys.add(f"{p1} {p2}".lower())
        keys.add(f"{p2} {p1}".lower())

        # Variation: Surname + Initial (Flashscore style: "Smith J." or "Обухов В.")
        keys.add(f"{p1} {p2[0]}.".lower())
        keys.add(f"{p1} {p2[0]}".lower())
        keys.add(f"{p2} {p1[0]}.".lower())
        keys.add(f"{p2} {p1[0]}".lower())

        # Transliteration variations
        lat_p1 = transliterate_cyr_to_lat(p1)
        lat_p2 = transliterate_cyr_to_lat(p2)
        keys.add(f"{lat_p1} {lat_p2}".lower())
        keys.add(f"{lat_p2} {lat_p1}".lower())
        keys.add(f"{lat_p1} {lat_p2[0]}.".lower())
        keys.add(f"{lat_p1} {lat_p2[0]}".lower())
        keys.add(f"{lat_p2} {lat_p1[0]}.".lower())
        keys.add(f"{lat_p2} {lat_p1[0]}".lower())

        # Normalized slug style
        keys.add(f"{lat_p1}-{lat_p2}".lower())
        keys.add(f"{lat_p2}-{lat_p1}".lower())
    elif len(parts) == 1:
        keys.add(parts[0].lower())
        keys.add(transliterate_cyr_to_lat(parts[0]).lower())

    return {k for k in keys if len(k) >= 2 and len(k) <= 255}

def seed_tournaments(cur):
    print("\n[MDM Seeding] Seeding Tournaments for 170, 171, 172, 173...", flush=True)

    # 1. Table Tennis Tournaments
    cur.execute("SELECT id, canonical_name, slug FROM mdm_tournament WHERE sport_id = 5")
    tt_tourneys = cur.fetchall()

    mappings = []
    # 171: ligapro
    # 172: setkacup
    # 173: ttcup
    # 170: flashscore

    for tid, name, slug in tt_tourneys:
        n_low = name.lower()
        s_low = slug.lower()

        # Liga Pro tournaments
        if "liga pro" in n_low or "лига про" in n_low:
            for sys_id in [171, 170]:
                mappings.append((sys_id, name.lower(), tid, name))
                mappings.append((sys_id, s_low, tid, name))
                mappings.append((sys_id, s_low.replace('-', ' '), tid, name))
                if sys_id == 170:
                    mappings.append((sys_id, f"table tennis: {name.lower()}", tid, name))
                    mappings.append((sys_id, f"table tennis: liga pro", tid, name))

        # Setka Cup tournaments
        if "setka" in n_low or "сетка" in n_low:
            for sys_id in [172, 170]:
                mappings.append((sys_id, name.lower(), tid, name))
                mappings.append((sys_id, s_low, tid, name))
                mappings.append((sys_id, s_low.replace('-', ' '), tid, name))
                if sys_id == 170:
                    mappings.append((sys_id, f"table tennis: {name.lower()}", tid, name))
                    mappings.append((sys_id, f"table tennis: setka cup", tid, name))

        # TT Cup & TT Elite Series
        if "tt cup" in n_low or "tt elite" in n_low or "тт кап" in n_low:
            for sys_id in [173, 170]:
                mappings.append((sys_id, name.lower(), tid, name))
                mappings.append((sys_id, s_low, tid, name))
                mappings.append((sys_id, s_low.replace('-', ' '), tid, name))
                if sys_id == 170:
                    mappings.append((sys_id, f"table tennis: {name.lower()}", tid, name))
                    mappings.append((sys_id, f"table tennis: tt cup", tid, name))

    # 2. Tennis Tournaments for Flashscore (170)
    cur.execute("SELECT id, canonical_name, slug FROM mdm_tournament WHERE sport_id = 4")
    tennis_tourneys = cur.fetchall()
    print(f"  Found {len(tennis_tourneys)} tennis tournaments in MDM.", flush=True)

    for tid, name, slug in tennis_tourneys:
        n_low = name.lower()
        s_low = slug.lower()
        # Direct name and slug
        mappings.append((170, n_low, tid, name))
        mappings.append((170, s_low, tid, name))
        mappings.append((170, s_low.replace('-', ' '), tid, name))

        # Flashscore pattern: "ATP - SINGLES: Name", "WTA - SINGLES: Name", etc.
        mappings.append((170, f"atp: {n_low}", tid, name))
        mappings.append((170, f"wta: {n_low}", tid, name))
        mappings.append((170, f"itf men - singles: {n_low}", tid, name))
        mappings.append((170, f"itf women - singles: {n_low}", tid, name))
        mappings.append((170, f"challenger men - singles: {n_low}", tid, name))
        mappings.append((170, f"tennis: {n_low}", tid, name))

    # Deduplicate by (external_system_id, raw_key)
    unique_map = {}
    for ext_id, rkey, c_id, rname in mappings:
        k = (ext_id, rkey[:255].strip())
        if k[1] and k not in unique_map:
            unique_map[k] = (ext_id, k[1], c_id, rname[:255])

    rows = list(unique_map.values())
    print(f"  Upserting {len(rows)} tournament mappings...", flush=True)
    query = """
        INSERT INTO mdm_tournament_ingestion_map (external_system_id, raw_key, canonical_tournament_id, raw_name)
        VALUES %s
        ON CONFLICT (external_system_id, raw_key) DO UPDATE
        SET canonical_tournament_id = EXCLUDED.canonical_tournament_id,
            raw_name = EXCLUDED.raw_name
    """
    execute_values(cur, query, rows, page_size=2000)
    print("  -> Tournament seeding complete!", flush=True)

def seed_teams(cur):
    print("\n[MDM Seeding] Seeding Teams/Participants for 170, 171, 172, 173...", flush=True)

    # 1. Table Tennis Participants (Sport 5)
    cur.execute("SELECT id, canonical_name, normalized_signature FROM mdm_team WHERE sport_id = 5")
    tt_teams = cur.fetchall()
    print(f"  Found {len(tt_teams)} table tennis participants in MDM.", flush=True)

    tt_mappings = []
    for tid, name, sig in tt_teams:
        keys = generate_player_keys(name, sig)
        # Add provider fixtures: 171 (ligapro), 172 (setkacup), 173 (ttcup), 170 (flashscore)
        for k in keys:
            for sys_id in [171, 172, 173, 170]:
                tt_mappings.append((sys_id, k, tid, name))

    # 2. Tennis Participants (Sport 4)
    cur.execute("SELECT id, canonical_name, normalized_signature FROM mdm_team WHERE sport_id = 4")
    tennis_teams = cur.fetchall()
    print(f"  Found {len(tennis_teams)} tennis participants in MDM.", flush=True)

    tennis_mappings = []
    for tid, name, sig in tennis_teams:
        keys = generate_player_keys(name, sig)
        for k in keys:
            tennis_mappings.append((170, k, tid, name))

    all_team_mappings = tt_mappings + tennis_mappings

    # Deduplicate
    unique_teams = {}
    for ext_id, rkey, c_id, rname in all_team_mappings:
        k = (ext_id, rkey[:255].strip())
        if k[1] and k not in unique_teams:
            unique_teams[k] = (ext_id, k[1], c_id, rname[:255], True)

    team_rows = list(unique_teams.values())
    print(f"  Upserting {len(team_rows)} team mappings in batches...", flush=True)
    query = """
        INSERT INTO mdm_team_ingestion_map (external_system_id, raw_key, canonical_team_id, raw_name, is_verified)
        VALUES %s
        ON CONFLICT (external_system_id, raw_key) DO UPDATE
        SET canonical_team_id = EXCLUDED.canonical_team_id,
            raw_name = EXCLUDED.raw_name,
            is_verified = true
    """
    batch_size = 5000
    for i in range(0, len(team_rows), batch_size):
        batch = team_rows[i:i + batch_size]
        execute_values(cur, query, batch, page_size=2000)
        if (i // batch_size + 1) % 10 == 0 or (i + batch_size >= len(team_rows)):
            print(f"    -> Seeded {min(i + batch_size, len(team_rows))}/{len(team_rows)} team mappings", flush=True)

    print("  -> Team seeding complete!", flush=True)

def main():
    print("==================================================", flush=True)
    print("      MDM FASTSPORTS & FLASHSCORE BLUEPRINT SEEDER", flush=True)
    print("==================================================", flush=True)
    conn = psycopg2.connect(DB_URL)
    conn.autocommit = False
    try:
        with conn.cursor() as cur:
            seed_tournaments(cur)
            seed_teams(cur)
            conn.commit()
            print("\n[MDM Seeding] Transaction committed successfully!", flush=True)
    except Exception as e:
        conn.rollback()
        print(f"\n[Error] Seeding failed, rolled back: {e}", flush=True)
        sys.exit(1)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
