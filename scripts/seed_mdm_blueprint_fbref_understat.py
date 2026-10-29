#!/usr/bin/env python3
"""
seed_mdm_blueprint_fbref_understat.py

Populates mdm_team_ingestion_map for external systems 165 ('fbref') and 166 ('understat')
from canonical football clubs in mdm_team.
Includes:
- canonical_name
- canonical_name.lower()
- normalized_signature
- Java normalizeSignature (stripping fc, cf, sc, united, city, etc.)
- Top club aliases used by FBref (StatsBomb) and Understat

Invalidates MDM Blueprint caches after seeding:
- /api/v1/mdm/blueprint/fbref/invalidate
- /api/v1/mdm/blueprint/understat/invalidate
"""

import os
import sys
import re
import time
import psycopg2
from psycopg2.extras import execute_values
import httpx

DB_HOST = os.environ.get("DB_HOST", "10.108.82.251")
DB_PORT = int(os.environ.get("DB_PORT", "5432"))
DB_NAME = os.environ.get("DB_NAME", "igaming_mdm")
DB_USER = os.environ.get("DB_USER", "postgres")
DB_PASS = os.environ.get("DB_PASS", "postgres")
MDM_URL = os.environ.get("MDM_SERVICE_URL", "http://10.109.207.77:8095")

EXTERNAL_SYSTEMS = [
    (165, "fbref"),
    (166, "understat")
]

# Exact Java normalizeSignature implementation from FbrefIngestionMatchingService.java:
# name.toLowerCase().replaceAll("(?i)\\b(fc|cf|sc|afc|fk|sv|united|city|rovers|wanderers)\\b", "").replaceAll("[^a-z0-9]", "").trim()
def java_normalize_signature(name: str) -> str:
    if not name:
        return ""
    s = name.lower()
    s = re.sub(r"(?i)\b(fc|cf|sc|afc|fk|sv|united|city|rovers|wanderers)\b", "", s)
    s = re.sub(r"[^a-z0-9]", "", s)
    return s.strip()

# Hand-curated aliases for top European and world clubs to guarantee 100% O(1) hit
TOP_CLUB_ALIASES = {
    # Premier League
    "Arsenal": ["Arsenal", "Arsenal FC", "arsenal", "The Gunners"],
    "Manchester City": ["Manchester City", "Man City", "Man. City", "Manchester C", "manchester city", "mcfc"],
    "Manchester United": ["Manchester United", "Man United", "Man Utd", "Man. United", "Manchester U", "manchester united", "mufc"],
    "Liverpool": ["Liverpool", "Liverpool FC", "liverpool", "lfc"],
    "Liverpool FC": ["Liverpool", "Liverpool FC", "liverpool", "lfc"],
    "Chelsea": ["Chelsea", "Chelsea FC", "chelsea", "cfc"],
    "Tottenham Hotspur": ["Tottenham Hotspur", "Tottenham", "Spurs", "tottenham", "tottenham hotspur", "thfc"],
    "Newcastle United": ["Newcastle United", "Newcastle", "Newcastle Utd", "newcastle united", "nufc"],
    "Aston Villa": ["Aston Villa", "Villa", "aston villa", "avfc"],
    "Brighton & Hove Albion": ["Brighton & Hove Albion", "Brighton", "Brighton and Hove Albion", "brighton"],
    "West Ham United": ["West Ham United", "West Ham", "West Ham Utd", "west ham united", "whufc"],
    "Wolverhampton Wanderers": ["Wolverhampton Wanderers", "Wolves", "Wolverhampton", "wolves"],
    "Everton": ["Everton", "Everton FC", "everton"],
    "Crystal Palace": ["Crystal Palace", "crystal palace", "cpfc"],
    "Fulham": ["Fulham", "Fulham FC", "fulham"],
    "Bournemouth": ["Bournemouth", "AFC Bournemouth", "bournemouth"],
    "Brentford": ["Brentford", "Brentford FC", "brentford"],
    "Nottingham Forest": ["Nottingham Forest", "Nott'm Forest", "Nottingham", "nottingham forest"],
    "Leicester City": ["Leicester City", "Leicester", "leicester city"],
    "Southampton": ["Southampton", "Southampton FC", "southampton"],
    "Ipswich Town": ["Ipswich Town", "Ipswich", "ipswich town"],
    
    # La Liga
    "Real Madrid": ["Real Madrid", "Real Madrid CF", "real madrid", "rmcf"],
    "FC Barcelona": ["FC Barcelona", "Barcelona", "Barca", "Barça", "fc barcelona", "barcelona"],
    "Barcelona": ["FC Barcelona", "Barcelona", "Barca", "Barça", "fc barcelona", "barcelona"],
    "Atlético Madrid": ["Atlético Madrid", "Atletico Madrid", "Atleti", "Atletico", "atlético madrid", "atletico madrid"],
    "Athletic Club": ["Athletic Club", "Athletic Bilbao", "Athletic", "athletic club", "athletic bilbao"],
    "Real Sociedad": ["Real Sociedad", "La Real", "real sociedad"],
    "Real Betis": ["Real Betis", "Betis", "real betis"],
    "Villarreal": ["Villarreal", "Villarreal CF", "villarreal"],
    "Sevilla": ["Sevilla", "Sevilla FC", "sevilla"],
    "Valencia": ["Valencia", "Valencia CF", "valencia"],
    "Girona": ["Girona", "Girona FC", "girona"],
    "Celta Vigo": ["Celta Vigo", "Celta de Vigo", "RC Celta", "celta vigo"],
    "Osasuna": ["Osasuna", "CA Osasuna", "osasuna"],
    "Getafe": ["Getafe", "Getafe CF", "getafe"],
    "Mallorca": ["Mallorca", "RCD Mallorca", "mallorca"],
    "Espanyol": ["Espanyol", "RCD Espanyol", "espanyol"],
    "Rayo Vallecano": ["Rayo Vallecano", "Rayo", "rayo vallecano"],
    "Deportivo Alavés": ["Deportivo Alavés", "Alaves", "Alavés", "deportivo alaves"],
    "Las Palmas": ["Las Palmas", "UD Las Palmas", "las palmas"],
    "Leganés": ["Leganés", "Leganes", "CD Leganés", "leganes"],
    "Real Valladolid": ["Real Valladolid", "Valladolid", "real valladolid"],

    # Bundesliga
    "FC Bayern München": ["FC Bayern München", "Bayern Munich", "Bayern München", "Bayern", "bayern munich", "bayern munchen"],
    "Borussia Dortmund": ["Borussia Dortmund", "Dortmund", "BVB", "borussia dortmund", "bvb"],
    "Bayer 04 Leverkusen": ["Bayer 04 Leverkusen", "Bayer Leverkusen", "Leverkusen", "bayer leverkusen", "leverkusen"],
    "RB Leipzig": ["RB Leipzig", "Leipzig", "rb leipzig", "rbl"],
    "Eintracht Frankfurt": ["Eintracht Frankfurt", "Frankfurt", "eintracht frankfurt"],
    "VfB Stuttgart": ["VfB Stuttgart", "Stuttgart", "vfb stuttgart"],
    "Borussia Mönchengladbach": ["Borussia Mönchengladbach", "Gladbach", "Mönchengladbach", "borussia mönchengladbach", "gladbach"],
    "VfL Wolfsburg": ["VfL Wolfsburg", "Wolfsburg", "wolfsburg"],
    "SC Freiburg": ["SC Freiburg", "Freiburg", "sc freiburg"],
    "1. FSV Mainz 05": ["1. FSV Mainz 05", "Mainz 05", "Mainz", "mainz 05", "mainz"],
    "1. FC Union Berlin": ["1. FC Union Berlin", "Union Berlin", "union berlin"],
    "FC Augsburg": ["FC Augsburg", "Augsburg", "augsburg"],
    "SV Werder Bremen": ["SV Werder Bremen", "Werder Bremen", "Bremen", "werder bremen"],
    "TSG Hoffenheim": ["TSG Hoffenheim", "Hoffenheim", "1899 Hoffenheim", "hoffenheim"],
    "1. FC Heidenheim": ["1. FC Heidenheim", "Heidenheim", "heidenheim"],
    "FC St. Pauli": ["FC St. Pauli", "St. Pauli", "st pauli"],
    "Holstein Kiel": ["Holstein Kiel", "Kiel", "holstein kiel"],
    "VfL Bochum": ["VfL Bochum", "Bochum", "vfl bochum"],

    # Serie A
    "Inter": ["Inter", "Inter Milan", "Internazionale", "FC Internazionale Milano", "inter milan", "inter"],
    "AC Milan": ["AC Milan", "Milan", "ac milan", "milan"],
    "Juventus": ["Juventus", "Juve", "juventus"],
    "Napoli": ["Napoli", "SSC Napoli", "napoli"],
    "Atalanta": ["Atalanta", "Atalanta BC", "atalanta"],
    "Roma": ["Roma", "AS Roma", "roma", "as roma"],
    "Lazio": ["Lazio", "SS Lazio", "lazio"],
    "Fiorentina": ["Fiorentina", "ACF Fiorentina", "fiorentina"],
    "Bologna": ["Bologna", "Bologna FC", "bologna"],
    "Torino": ["Torino", "Torino FC", "torino"],
    "Monza": ["Monza", "AC Monza", "monza"],
    "Genoa": ["Genoa", "Genoa CFC", "genoa"],
    "Udinese": ["Udinese", "Udinese Calcio", "udinese"],
    "Cagliari": ["Cagliari", "Cagliari Calcio", "cagliari"],
    "Empoli": ["Empoli", "Empoli FC", "empoli"],
    "Hellas Verona": ["Hellas Verona", "Verona", "hellas verona"],
    "Parma": ["Parma", "Parma Calcio", "parma"],
    "Como": ["Como", "Como 1907", "como"],
    "Venezia": ["Venezia", "Venezia FC", "venezia"],
    "Lecce": ["Lecce", "US Lecce", "lecce"],

    # Ligue 1
    "Paris Saint-Germain": ["Paris Saint-Germain", "Paris S-G", "PSG", "paris saint germain", "paris sg", "psg"],
    "Olympique de Marseille": ["Olympique de Marseille", "Marseille", "OM", "olympique marseille", "marseille"],
    "AS Monaco": ["AS Monaco", "Monaco", "monaco", "as monaco"],
    "Olympique Lyonnais": ["Olympique Lyonnais", "Lyon", "OL", "lyon", "olympique lyonnais"],
    "Lille": ["Lille", "LOSC Lille", "LOSC", "lille", "lille osc"],
    "RC Lens": ["RC Lens", "Lens", "lens", "rc lens"],
    "Stade Rennais": ["Stade Rennais", "Rennes", "rennes"],
    "OGC Nice": ["OGC Nice", "Nice", "nice"],
    "Stade Brestois 29": ["Stade Brestois 29", "Brest", "Stade Brestois", "brest"],
    "RC Strasbourg": ["RC Strasbourg", "Strasbourg", "strasbourg"],
    "Toulouse": ["Toulouse", "Toulouse FC", "toulouse"],
    "Stade de Reims": ["Stade de Reims", "Reims", "reims"],
    "AJ Auxerre": ["AJ Auxerre", "Auxerre", "auxerre"],
    "FC Nantes": ["FC Nantes", "Nantes", "nantes"],
    "Angers SCO": ["Angers SCO", "Angers", "angers"],
    "AS Saint-Étienne": ["AS Saint-Étienne", "Saint-Étienne", "St Etienne", "saint-etienne", "st etienne"],
    "Le Havre": ["Le Havre", "Le Havre AC", "le havre"],
    "Montpellier": ["Montpellier", "Montpellier HSC", "montpellier"],

    # Other Top European & World Clubs
    "Sporting CP": ["Sporting CP", "Sporting Lisbon", "Sporting", "sporting cp", "sporting lisbon"],
    "SL Benfica": ["SL Benfica", "Benfica", "benfica"],
    "FC Porto": ["FC Porto", "Porto", "porto"],
    "SC Braga": ["SC Braga", "Braga", "braga"],
    "Ajax": ["Ajax", "AFC Ajax", "ajax"],
    "PSV Eindhoven": ["PSV Eindhoven", "PSV", "psv"],
    "Feyenoord": ["Feyenoord", "Feyenoord Rotterdam", "feyenoord"],
    "Celtic": ["Celtic", "Celtic FC", "celtic"],
    "Rangers": ["Rangers", "Rangers FC", "rangers"],
    "Galatasaray": ["Galatasaray", "Galatasaray SK", "galatasaray"],
    "Fenerbahçe": ["Fenerbahçe", "Fenerbahce", "fenerbahce"],
    "Beşiktaş": ["Beşiktaş", "Besiktas", "besiktas"],
    "FC Zenit St. Petersburg": ["Zenit", "Zenit St. Petersburg", "FC Zenit", "zenit"],
    "FC Krasnodar": ["Krasnodar", "FC Krasnodar", "krasnodar"],
    "FC Dynamo Moscow": ["Dynamo Moscow", "Dinamo Moscow", "dynamo moscow"],
    "FC Spartak Moscow": ["Spartak Moscow", "spartak moscow"],
    "FC Lokomotiv Moscow": ["Lokomotiv Moscow", "lokomotiv moscow"],
    "PFC CSKA Moscow": ["CSKA Moscow", "cska moscow"],
    "Shakhtar Donetsk": ["Shakhtar Donetsk", "Shakhtar", "shakhtar donetsk"],
    "Dynamo Kyiv": ["Dynamo Kyiv", "Dynamo Kiev", "dynamo kyiv"],
    "CR Flamengo": ["Flamengo", "CR Flamengo", "flamengo"],
    "SE Palmeiras": ["Palmeiras", "palmeiras"],
    "São Paulo": ["São Paulo", "Sao Paulo", "sao paulo"],
    "Santos": ["Santos", "Santos FC", "santos"],
    "SC Corinthians": ["Corinthians", "corinthians"],
    "Grêmio": ["Grêmio", "Gremio", "gremio"],
    "Internacional": ["Internacional", "Sport Club Internacional", "internacional"],
    "Fluminense": ["Fluminense", "fluminense"],
    "Cruzeiro": ["Cruzeiro", "cruzeiro"],
    "Atlético Mineiro": ["Atlético Mineiro", "Atletico Mineiro", "atletico mineiro"],
    "Botafogo": ["Botafogo", "botafogo"],
    "Boca Juniors": ["Boca Juniors", "boca juniors"],
    "River Plate": ["River Plate", "river plate"],
    "Al-Hilal": ["Al-Hilal", "Al Hilal", "al hilal"],
    "Al-Nassr": ["Al-Nassr", "Al Nassr", "al nassr"],
    "Al-Ittihad": ["Al-Ittihad", "Al Ittihad", "al ittihad"],
    "Al-Ahli": ["Al-Ahli", "Al Ahli", "al ahli"],
    "Inter Miami CF": ["Inter Miami CF", "Inter Miami", "inter miami"],
    "Los Angeles FC": ["Los Angeles FC", "LAFC", "lafc"],
    "LA Galaxy": ["LA Galaxy", "Los Angeles Galaxy", "la galaxy"]
}

def main():
    print("=" * 80)
    print(" 🏛️ MDM BLUEPRINT SEEDING: FBREF (165) & UNDERSTAT (166)")
    print("=" * 80, flush=True)

    t0 = time.time()
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASS
    )
    cur = conn.cursor()

    # 1. Fetch clean senior football clubs with sofascore_id from mdm_team
    # Prioritize: sofascore_id IS NOT NULL, gender = 'MALE', age_category = 'SENIOR'
    print(" Querying canonical football clubs from mdm_team...", flush=True)
    sql = """
        SELECT id, canonical_name, normalized_signature, slug, sofascore_id, country_name
        FROM mdm_team
        WHERE sport_id = 1
          AND sofascore_id IS NOT NULL
          AND canonical_name NOT ILIKE '%(%)%'
          AND canonical_name NOT ILIKE '%u1%'
          AND canonical_name NOT ILIKE '%u2%'
          AND canonical_name NOT ILIKE '%women%'
          AND canonical_name NOT ILIKE '%(w)%'
        ORDER BY popularity_rank ASC, id ASC;
    """
    cur.execute(sql)
    teams = cur.fetchall()
    print(f" Loaded {len(teams):,} canonical senior football clubs.", flush=True)

    # Build mapping dictionary: raw_key -> (canonical_team_id, raw_name, source_id)
    # We will insert for both 165 (fbref) and 166 (understat)
    mappings_by_key = {}

    for tid, c_name, norm_sig, slug, sofa_id, country in teams:
        source_id = str(sofa_id) if sofa_id else (slug or str(tid))

        keys_to_add = set()
        if c_name:
            keys_to_add.add(c_name.strip())
            keys_to_add.add(c_name.strip().lower())
            j_norm = java_normalize_signature(c_name)
            if j_norm:
                keys_to_add.add(j_norm)
        if norm_sig:
            keys_to_add.add(norm_sig.strip())
            keys_to_add.add(norm_sig.strip().lower())
        if slug:
            keys_to_add.add(slug.strip().lower())

        for k in keys_to_add:
            k_clean = k.strip()
            if len(k_clean) >= 2 and k_clean not in mappings_by_key:
                mappings_by_key[k_clean] = (tid, c_name, source_id)

    # Add hand-curated aliases for top clubs
    print(f" Applying curated top club aliases for FBref & Understat...", flush=True)
    alias_hits = 0
    for canonical_target, aliases in TOP_CLUB_ALIASES.items():
        # Find the canonical team id
        cur.execute("""
            SELECT id, canonical_name, sofascore_id, slug
            FROM mdm_team
            WHERE sport_id = 1 AND canonical_name = %s
            ORDER BY popularity_rank ASC, id ASC
            LIMIT 1;
        """, (canonical_target,))
        row = cur.fetchone()
        if not row:
            # Try ILIKE
            cur.execute("""
                SELECT id, canonical_name, sofascore_id, slug
                FROM mdm_team
                WHERE sport_id = 1 AND canonical_name ILIKE %s AND sofascore_id IS NOT NULL
                ORDER BY popularity_rank ASC, id ASC
                LIMIT 1;
            """, (canonical_target,))
            row = cur.fetchone()

        if row:
            tid, c_name, sofa_id, slug = row
            source_id = str(sofa_id) if sofa_id else (slug or str(tid))
            for alias in aliases:
                a_clean = alias.strip()
                mappings_by_key[a_clean] = (tid, c_name, source_id)
                mappings_by_key[a_clean.lower()] = (tid, c_name, source_id)
                j_norm = java_normalize_signature(a_clean)
                if j_norm:
                    mappings_by_key[j_norm] = (tid, c_name, source_id)
                alias_hits += 1

    print(f" Total distinct keys compiled across all clubs and aliases: {len(mappings_by_key):,}")

    # Now insert into mdm_team_ingestion_map for both external systems
    for sys_id, sys_code in EXTERNAL_SYSTEMS:
        print(f"\n Seeding mdm_team_ingestion_map for external system {sys_id} ('{sys_code}')...", flush=True)
        rows_to_insert = []
        for raw_key, (tid, raw_name, source_id) in mappings_by_key.items():
            rows_to_insert.append((
                sys_id,
                raw_key,
                tid,
                raw_name,
                True, # is_verified
                source_id
            ))

        insert_sql = """
            INSERT INTO mdm_team_ingestion_map (
                external_system_id, raw_key, canonical_team_id, raw_name, is_verified, source_id, created_at
            ) VALUES %s
            ON CONFLICT (external_system_id, raw_key) DO UPDATE
            SET canonical_team_id = EXCLUDED.canonical_team_id,
                raw_name = EXCLUDED.raw_name,
                source_id = EXCLUDED.source_id,
                is_verified = TRUE;
        """

        # Batch insert with execute_values (chunks of 10,000)
        chunk_size = 10000
        total_inserted = 0
        t_sys0 = time.time()

        for i in range(0, len(rows_to_insert), chunk_size):
            chunk = rows_to_insert[i:i + chunk_size]
            template = "(%s, %s, %s, %s, %s, %s, NOW())"
            execute_values(cur, insert_sql, chunk, template=template, page_size=2000)
            conn.commit()
            total_inserted += len(chunk)
            print(f"  Inserted {total_inserted:,} / {len(rows_to_insert):,} rows for {sys_code}...", flush=True)

        elapsed_sys = time.time() - t_sys0
        print(f" Completed {sys_code} in {elapsed_sys:.1f}s.")

    # Invalidate MDM Blueprint Cache via HTTP
    print("\n Invalidating MDM Blueprint Cache via MDM Service...", flush=True)
    with httpx.Client(timeout=10.0) as client:
        for sys_id, sys_code in EXTERNAL_SYSTEMS:
            try:
                url = f"{MDM_URL}/api/v1/mdm/blueprint/{sys_code}/invalidate"
                resp = client.post(url)
                print(f"  Invalidate {sys_code}: HTTP {resp.status_code}")
            except Exception as e:
                print(f"  Warning: could not reach {MDM_URL} for {sys_code}: {e}")

    # Verify resolution of key clubs
    print("\n Testing O(1) Blueprint Resolution Simulation:")
    test_queries = [
        "Arsenal", "arsenal", "The Gunners",
        "Manchester City", "Man City", "mcfc",
        "Manchester United", "Man Utd", "Man United",
        "Liverpool", "Liverpool FC",
        "Chelsea", "Chelsea FC",
        "Real Madrid", "real madrid",
        "FC Barcelona", "Barcelona", "Barca",
        "Bayern Munich", "FC Bayern München", "Bayern",
        "Paris Saint-Germain", "Paris S-G", "PSG",
        "Inter", "Inter Milan", "AC Milan", "Juventus",
        "Bayer Leverkusen", "Leverkusen", "Borussia Dortmund", "Dortmund",
        "Sporting CP", "Benfica", "Porto", "Ajax"
    ]

    for q in test_queries:
        cur.execute("""
            SELECT tm.raw_key, tm.canonical_team_id, t.canonical_name, t.sofascore_id
            FROM mdm_team_ingestion_map tm
            JOIN mdm_team t ON tm.canonical_team_id = t.id
            WHERE tm.external_system_id = 165
              AND (tm.raw_key = %s OR lower(tm.raw_key) = lower(%s))
            LIMIT 1;
        """, (q, q))
        res = cur.fetchone()
        if res:
            print(f"  ✅ '{q:22s}' -> ID {res[1]} ('{res[2]}', sofa_id={res[3]}) [raw_key: '{res[0]}']")
        else:
            print(f"  ❌ '{q:22s}' -> UNMAPPED!")

    cur.close()
    conn.close()

    elapsed = time.time() - t0
    print(f"\n Blueprint seeding fully finished in {elapsed:.1f}s.")
    print("=" * 80, flush=True)

if __name__ == "__main__":
    main()
