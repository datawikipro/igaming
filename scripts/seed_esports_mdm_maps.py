#!/usr/bin/env python3
"""
Seed MDM Ingestion Maps for Esports External Systems:
- 167: HLTV CS2 ('hltv')
- 168: OpenDota ('opendota')
- 169: VLR.gg Valorant ('vlr')
"""

import sys
import subprocess

SEED_DATA = {
    # 167: HLTV CS2
    167: [
        {"name": "Team Spirit", "id": 104484, "src": "7020", "keys": ["team spirit", "spirit", "7020", "team-spirit"]},
        {"name": "Natus Vincere", "id": 104211, "src": "4608", "keys": ["natus vincere", "navi", "4608", "natus-vincere"]},
        {"name": "FaZe Clan", "id": 142210, "src": "6667", "keys": ["faze clan", "faze", "6667", "faze-clan"]},
        {"name": "Team Vitality", "id": 104490, "src": "9565", "keys": ["team vitality", "vitality", "9565", "team-vitality"]},
        {"name": "G2 Esports", "id": 129301, "src": "5995", "keys": ["g2 esports", "g2", "5995", "g2-esports"]},
        {"name": "Astralis", "id": 129309, "src": "6665", "keys": ["astralis", "6665", "astralis-364378"]},
        {"name": "Team Liquid", "id": 104470, "src": "5973", "keys": ["team liquid", "liquid", "5973", "team-liquid"]},
        {"name": "MOUZ", "id": 104203, "src": "4494", "keys": ["mouz", "mousesports", "4494", "mouz-508238"]},
        {"name": "Virtus.pro", "id": 129299, "src": "5378", "keys": ["virtus.pro", "virtus pro", "vp", "5378", "virtuspro"]},
        {"name": "HEROIC", "id": 129312, "src": "7175", "keys": ["heroic", "7175", "heroic-364395"]},
        {"name": "BetBoom Team", "id": 131910, "src": "12394", "keys": ["betboom team", "betboom", "12394", "betboom-team"]},
        {"name": "Team Falcons", "id": 130384, "src": "11283", "keys": ["team falcons", "falcons", "11283", "team-falcons"]},
        {"name": "Eternal Fire", "id": 129897, "src": "11251", "keys": ["eternal fire", "ef", "11251", "eternal-fire"]},
        {"name": "BIG", "id": 103714, "src": "7532", "keys": ["big", "berlin international gaming", "7532"]},
        {"name": "FlyQuest", "id": 103925, "src": "12774", "keys": ["flyquest", "12774", "flyquest-512453"]},
        {"name": "FURIA Esports", "id": 129281, "src": "8297", "keys": ["furia", "furia esports", "8297", "furia-esports"]},
        {"name": "Monte", "id": 104198, "src": "11811", "keys": ["monte", "11811"]},
        {"name": "Ninjas in Pyjamas", "id": 104228, "src": "4411", "keys": ["ninjas in pyjamas", "nip", "4411", "ninjas-in-pyjamas"]},
        {"name": "fnatic", "id": 129316, "src": "4991", "keys": ["fnatic", "4991", "fnatic-364400"]},
        {"name": "Cloud9", "id": 129334, "src": "5752", "keys": ["cloud9", "c9", "5752", "cloud9-365222"]},
        {"name": "3DMAX", "id": 103612, "src": "4914", "keys": ["3dmax", "4914"]},
        {"name": "SAW", "id": 129290, "src": "10567", "keys": ["saw", "10567"]},
        {"name": "paiN Gaming", "id": 104278, "src": "4773", "keys": ["pain", "pain gaming", "4773", "pain-gaming"]},
        {"name": "MIBR", "id": 104183, "src": "9215", "keys": ["mibr", "made in brazil", "9215"]},
        {"name": "Imperial", "id": 129342, "src": "9455", "keys": ["imperial", "imperial esports", "9455"]},
        {"name": "The MongolZ", "id": 129349, "src": "6248", "keys": ["the mongolz", "mongolz", "themongolz", "6248"]},
        {"name": "PARIVISION", "id": 104283, "src": "12467", "keys": ["parivision", "12467", "parivision-933876"]},
        {"name": "Aurora", "id": 132212, "src": "11861", "keys": ["aurora", "aurora gaming", "11861", "aurora-498782"]},
        {"name": "Nemiga Gaming", "id": 129324, "src": "7969", "keys": ["nemiga", "nemiga gaming", "7969"]},
        {"name": "GamerLegion", "id": 134215, "src": "9928", "keys": ["gamerlegion", "9928"]},
        {"name": "Sangal", "id": 129326, "src": "10333", "keys": ["sangal", "sangal esports", "10333"]},
        {"name": "B8", "id": 129874, "src": "11241", "keys": ["b8", "b8 esports", "11241"]},
        {"name": "ENCE", "id": 129337, "src": "4869", "keys": ["ence", "4869"]},
        {"name": "1win", "id": 103608, "src": "12592", "keys": ["1win", "1win team", "12592"]},
    ],

    # 168: OpenDota Dota 2
    168: [
        {"name": "Team Spirit", "id": 104484, "src": "7119388", "keys": ["team spirit", "spirit", "7119388", "team-spirit"]},
        {"name": "Team Liquid", "id": 104470, "src": "2163", "keys": ["team liquid", "liquid", "2163", "team-liquid"]},
        {"name": "Xtreme Gaming", "id": 129500, "src": "8599101", "keys": ["xtreme gaming", "xtreme", "8599101", "xtreme-gaming"]},
        {"name": "BetBoom Team", "id": 131910, "src": "8894818", "keys": ["betboom team", "betboom", "8894818", "betboom-team"]},
        {"name": "Team Falcons", "id": 130384, "src": "9247354", "keys": ["team falcons", "falcons", "9247354", "team-falcons"]},
        {"name": "Tundra Esports", "id": 104539, "src": "8255756", "keys": ["tundra esports", "tundra", "8255756", "tundra-esports"]},
        {"name": "OG", "id": 104253, "src": "2586976", "keys": ["og", "2586976"]},
        {"name": "HEROIC", "id": 129312, "src": "9303492", "keys": ["heroic", "9303492"]},
        {"name": "MOUZ", "id": 104203, "src": "9363650", "keys": ["mouz", "mousesports", "9363650"]},
        {"name": "Virtus.pro", "id": 129299, "src": "1883502", "keys": ["virtus.pro", "virtus pro", "vp", "1883502", "virtuspro"]},
        {"name": "Natus Vincere", "id": 104211, "src": "36", "keys": ["natus vincere", "navi", "36", "natus-vincere"]},
        {"name": "G2 Esports", "id": 129301, "src": "5", "keys": ["g2 esports", "g2.ig", "5", "g2"]},
        {"name": "Cloud9", "id": 129334, "src": "1333179", "keys": ["cloud9", "c9", "1333179"]},
        {"name": "Aurora", "id": 132212, "src": "9238381", "keys": ["aurora", "aurora gaming", "9238381"]},
        {"name": "1win", "id": 103608, "src": "9351935", "keys": ["1win", "9351935"]},
        {"name": "Nigma Galaxy", "id": 104227, "src": "7554697", "keys": ["nigma galaxy", "nigma", "7554697"]},
        {"name": "Shopify Rebellion", "id": 132258, "src": "8948110", "keys": ["shopify rebellion", "shopify", "8948110"]},
        {"name": "PARIVISION", "id": 104283, "src": "9504500", "keys": ["parivision", "9504500"]},
    ],

    # 169: VLR.gg Valorant
    169: [
        {"name": "Sentinels", "id": 104396, "src": "2", "keys": ["sentinels", "sen", "2"]},
        {"name": "fnatic", "id": 129316, "src": "2593", "keys": ["fnatic", "fnc", "2593"]},
        {"name": "EDward Gaming", "id": 103851, "src": "1120", "keys": ["edward gaming", "edg", "1120"]},
        {"name": "Gen.G Esports", "id": 145476, "src": "17", "keys": ["gen.g", "gen.g esports", "geng", "17"]},
        {"name": "LOUD", "id": 129498, "src": "6961", "keys": ["loud", "6961"]},
        {"name": "Karmine Corp", "id": 132277, "src": "8877", "keys": ["karmine corp", "kc", "8877"]},
        {"name": "Team Vitality", "id": 104490, "src": "2059", "keys": ["team vitality", "vitality", "vit", "2059"]},
        {"name": "Team Liquid", "id": 104470, "src": "474", "keys": ["team liquid", "liquid", "tl", "474"]},
        {"name": "FUT Esports", "id": 133547, "src": "1184", "keys": ["fut esports", "fut", "1184"]},
        {"name": "Bilibili Gaming", "id": 129341, "src": "12010", "keys": ["bilibili gaming", "blg", "12010"]},
        {"name": "Leviatan", "id": 104125, "src": "2359", "keys": ["leviatan", "leviatan esports", "lev", "2359"]},
        {"name": "G2 Esports", "id": 129301, "src": "11058", "keys": ["g2 esports", "g2", "11058"]},
        {"name": "Cloud9", "id": 129334, "src": "188", "keys": ["cloud9", "c9", "188"]},
        {"name": "NRG", "id": 129350, "src": "1034", "keys": ["nrg", "1034"]},
        {"name": "100 Thieves", "id": 129303, "src": "84", "keys": ["100 thieves", "100t", "84"]},
        {"name": "Natus Vincere", "id": 104211, "src": "4915", "keys": ["natus vincere", "navi", "4915"]},
        {"name": "Team Heretics", "id": 104464, "src": "1001", "keys": ["team heretics", "heretics", "th", "1001"]},
    ]
}

def generate_sql() -> str:
    lines = ["BEGIN;"]
    total = 0
    for ext_sys, teams in SEED_DATA.items():
        for t in teams:
            team_id = t["id"]
            team_name = t["name"].replace("'", "''")
            src_id = t["src"]
            for raw_k in t["keys"]:
                raw_k_esc = raw_k.lower().strip().replace("'", "''")
                sql = (
                    f"INSERT INTO mdm_team_ingestion_map "
                    f"(external_system_id, raw_key, canonical_team_id, raw_name, source_id, is_verified, created_at) "
                    f"VALUES ({ext_sys}, '{raw_k_esc}', {team_id}, '{team_name}', '{src_id}', true, NOW()) "
                    f"ON CONFLICT (external_system_id, raw_key) DO UPDATE "
                    f"SET canonical_team_id = EXCLUDED.canonical_team_id, "
                    f"    raw_name = EXCLUDED.raw_name, "
                    f"    source_id = EXCLUDED.source_id, "
                    f"    is_verified = true;"
                )
                lines.append(sql)
                total += 1
    lines.append("COMMIT;")
    print(f"-- Generated {total} mapping statements across external systems 167, 168, 169.")
    return "\n".join(lines)

def main():
    sql = generate_sql()
    out_file = "/tmp/seed_esports_mdm.sql"
    with open(out_file, "w", encoding="utf-8") as f:
        f.write(sql)
    print(f"Written SQL to {out_file}")

if __name__ == "__main__":
    main()
