import subprocess
import json
import re
import sys
from collections import defaultdict
from datetime import datetime

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

CYRILLIC_TO_LATIN = {
    'а': 'a', 'б': 'b', 'в': 'v', 'г': 'g', 'д': 'd', 'е': 'e', 'ё': 'e',
    'ж': 'zh', 'з': 'z', 'и': 'i', 'й': 'i', 'к': 'k', 'л': 'l', 'м': 'm',
    'н': 'n', 'о': 'o', 'п': 'p', 'р': 'r', 'с': 's', 'т': 't', 'у': 'u',
    'ф': 'f', 'х': 'h', 'ц': 'ts', 'ч': 'ch', 'ш': 'sh', 'щ': 'sh',
    'ъ': '', 'ы': 'y', 'ь': '', 'э': 'e', 'ю': 'yu', 'я': 'ya'
}

def transliterate(text):
    if not text:
        return ""
    text = text.lower()
    res = []
    for ch in text:
        if ch in CYRILLIC_TO_LATIN:
            res.append(CYRILLIC_TO_LATIN[ch])
        else:
            res.append(ch)
    return "".join(res)

def normalize_stem(name):
    if not name:
        return ""
    t = transliterate(name)
    t = re.sub(r'\(.*?\)', '', t)
    # remove noise words
    t = re.sub(r'\b(fc|fk|cf|sc|cd|sad|bc|sk|u\d+|games|reserve|women|damas|frauen|de|la|el|the)\b', '', t, flags=re.I)
    # phonetics
    t = t.replace('c', 'k').replace('y', 'i').replace('s', 'z').replace('ph', 'f').replace('w', 'v').replace('j', 'i')
    # keep alphanumeric only
    t = re.sub(r'[^a-z0-9]', '', t)
    return t[:8] if len(t) >= 8 else t

def main():
    print("⏳ [1/3] Fetching active matches and merge map from MDM...", flush=True)
    psql_cmd = [
        "kubectl", "exec", "-n", "igaming-enrichment", "igaming-aggregator-db-0", "--",
        "psql", "-U", "postgres", "-d", "igaming_mdm", "-t", "-A", "-F", "|||", "-c",
        """
        SELECT 
            m.id,
            m.sport_id,
            s.slug,
            m.tournament_id,
            tourn.canonical_name as tourn_name,
            m.home_team_id,
            t1.canonical_name as home_name,
            m.away_team_id,
            t2.canonical_name as away_name,
            EXTRACT(EPOCH FROM m.start_time)::bigint as start_epoch,
            m.status,
            m.source_origin,
            m.created_by_source,
            COALESCE(m.sofascore_id, 0) as sofa_id,
            COALESCE(mm1.sofa_id, m.home_team_id) as norm_home_id,
            COALESCE(mm2.sofa_id, m.away_team_id) as norm_away_id
        FROM mdm_match m
        JOIN mdm_sport s ON m.sport_id = s.id
        JOIN mdm_tournament tourn ON m.tournament_id = tourn.id
        JOIN mdm_team t1 ON m.home_team_id = t1.id
        JOIN mdm_team t2 ON m.away_team_id = t2.id
        LEFT JOIN mdm_team_merge_map mm1 ON m.home_team_id = mm1.local_id
        LEFT JOIN mdm_team_merge_map mm2 ON m.away_team_id = mm2.local_id
        WHERE m.status IN ('SCHEDULED', 'LIVE')
          AND m.start_time >= NOW() - INTERVAL '12 hours' AND m.start_time <= NOW() + INTERVAL '7 days';
        """
    ]
    p_proc = subprocess.run(psql_cmd, capture_output=True, text=True, encoding='utf-8')
    if p_proc.returncode != 0:
        print("Error fetching matches:", p_proc.stderr, flush=True)
        return

    matches = []
    for line in p_proc.stdout.strip().split("\n"):
        if not line.strip():
            continue
        parts = line.split("|||")
        if len(parts) >= 16:
            matches.append({
                "id": int(parts[0]),
                "sport_id": int(parts[1]),
                "sport": parts[2],
                "tourn_id": int(parts[3]),
                "tourn_name": parts[4],
                "home_id": int(parts[5]),
                "home_name": parts[6],
                "away_id": int(parts[7]),
                "away_name": parts[8],
                "start_epoch": int(parts[9]),
                "status": parts[10],
                "origin": parts[11],
                "created_by": parts[12],
                "sofa_id": int(parts[13]),
                "norm_home_id": int(parts[14]),
                "norm_away_id": int(parts[15]),
                "stem_home": normalize_stem(parts[6]),
                "stem_away": normalize_stem(parts[8])
            })
    print(f"✅ Loaded {len(matches)} active matches from MDM.", flush=True)

    print("⏳ [2/3] Fetching bookmaker allocations from Redis...", flush=True)
    redis_cmd = [
        "kubectl", "exec", "-n", "igaming-enrichment", "igaming-redis-594d8d879-7qrrr", "--",
        "redis-cli", "EVAL",
        "local keys = redis.call('keys', 'match:bms:*') local res = {} for i=1,#keys do local bms = redis.call('smembers', keys[i]) table.insert(res, keys[i] .. '=' .. table.concat(bms, ',')) end return res",
        "0"
    ]
    r_proc = subprocess.run(redis_cmd, capture_output=True, text=True, encoding='utf-8')
    match_bms = defaultdict(set)
    if r_proc.returncode == 0:
        for line in r_proc.stdout.strip().split("\n"):
            line = line.strip()
            if not line or "=" not in line:
                continue
            k, v = line.split("=", 1)
            mid_str = k.replace("match:bms:", "").strip()
            if mid_str.isdigit():
                mid = int(mid_str)
                bms = [b.strip() for b in v.split(",") if b.strip()]
                match_bms[mid] = set(bms)
    print(f"✅ Loaded bookmakers for {len(match_bms)} matches from Redis.", flush=True)

    for m in matches:
        bms = set(match_bms.get(m["id"], []))
        if m["created_by"] and m["created_by"] not in ("sofascore", "system"):
            bms.add(m["created_by"])
        m["bms"] = bms
        m["bm_count"] = len(bms)

    print("⏳ [3/3] Clustering duplicate matches...", flush=True)

    # Disjoint Set Union (DSU) to group matches
    parent = {m["id"]: m["id"] for m in matches}
    def find(i):
        if parent[i] == i:
            return i
        parent[i] = find(parent[i])
        return parent[i]
    def union(i, j):
        root_i = find(i)
        root_j = find(j)
        if root_i != root_j:
            parent[root_i] = root_j

    # 1. Cluster by exact or merge_map IDs
    team_id_map = defaultdict(list)
    for m in matches:
        # Group by 12-hour window
        tb = round(m["start_epoch"] / (12 * 3600))
        t_pair = tuple(sorted([m["norm_home_id"], m["norm_away_id"]]))
        team_id_map[(m["sport_id"], t_pair, tb)].append(m)

    for key, group in team_id_map.items():
        if len(group) > 1:
            for k in range(1, len(group)):
                union(group[0]["id"], group[k]["id"])

    # 2. Cluster by normalized stem names (Cyrillic + Phonetic transliteration)
    stem_map = defaultdict(list)
    for m in matches:
        if not m["stem_home"] or not m["stem_away"]:
            continue
        # 6-hour window
        tb = round(m["start_epoch"] / (6 * 3600))
        s_pair = tuple(sorted([m["stem_home"], m["stem_away"]]))
        stem_map[(m["sport_id"], s_pair, tb)].append(m)

    for key, group in stem_map.items():
        if len(group) > 1:
            for k in range(1, len(group)):
                union(group[0]["id"], group[k]["id"])

    # Group into final clusters
    match_by_id = {m["id"]: m for m in matches}
    cluster_groups = defaultdict(list)
    for m in matches:
        root = find(m["id"])
        cluster_groups[root].append(m)

    duplicate_clusters = [grp for grp in cluster_groups.values() if len(grp) > 1]

    print("\n" + "="*80, flush=True)
    print("📊 РЕЗУЛЬТАТЫ СВЕРКИ: МАСШТАБ ДУБЛИКАТОВ И РАЗРЫВА БУКМЕКЕРСКОЙ ЛИНИИ", flush=True)
    print("="*80, flush=True)

    total_active = len(matches)
    total_dups = len(duplicate_clusters)
    total_dup_records = sum(len(c) for c in duplicate_clusters)
    excess_rows = sum(len(c) - 1 for c in duplicate_clusters)

    print(f"Всего активных матчей в линии (SCHEDULED/LIVE): {total_active}", flush=True)
    print(f"Групп идентичных матчей (реальных спортивных событий): {total_dups}", flush=True)
    print(f"Всего затронутых записей в mdm_match: {total_dup_records}", flush=True)
    print(f"Лишних записей матчей-клонов (excess duplicates): {excess_rows}", flush=True)

    sport_dups = defaultdict(lambda: {"clusters": 0, "excess": 0, "fragmented": 0})
    for c in duplicate_clusters:
        sp = c[0]["sport"]
        sport_dups[sp]["clusters"] += 1
        sport_dups[sp]["excess"] += (len(c) - 1)
        all_bms = set()
        max_bms = 0
        for m in c:
            all_bms.update(m["bms"])
            if len(m["bms"]) > max_bms:
                max_bms = len(m["bms"])
        if len(all_bms) > max_bms:
            sport_dups[sp]["fragmented"] += 1

    print("\n📌 Распределение дубликатов по видам спорта:", flush=True)
    print(f"{'Вид спорта':<20} | {'Групп дублей':<15} | {'Лишних записей':<15} | {'С разрывом букмекеров':<20}", flush=True)
    print("-" * 75, flush=True)
    for sp, data in sorted(sport_dups.items(), key=lambda x: -x[1]["clusters"]):
        print(f"{sp:<20} | {data['clusters']:<15} | {data['excess']:<15} | {data['fragmented']:<20}", flush=True)

    cluster_stats = []
    for c in duplicate_clusters:
        all_bms = set()
        for m in c:
            all_bms.update(m["bms"])
        max_bms = max(len(m["bms"]) for m in c)
        is_frag = len(all_bms) > max_bms
        cluster_stats.append({
            "cluster": c,
            "total_bms": len(all_bms),
            "all_bms": all_bms,
            "max_single": max_bms,
            "is_frag": is_frag,
            "sport": c[0]["sport"]
        })

    frag_list = [cs for cs in cluster_stats if cs["is_frag"]]
    print(f"\n⚡ ВНИМАНИЕ: Из {total_dups} групп в {len(frag_list)} группах букмекеры РАЗОРВАНЫ между клонами!", flush=True)
    print("   (То есть пользователи на портале видят 2 или 3 клона одного матча, где в каждом сидит часть БК!)", flush=True)

    print("\n" + "="*80, flush=True)
    print("🏆 ТОП-15 КРУПНЕЙШИХ МАТЧЕЙ С РАЗОРВАННЫМИ БУКМЕКЕРАМИ ПРЯМО СЕЙЧАС:", flush=True)
    print("="*80, flush=True)

    sorted_top = sorted(frag_list, key=lambda x: -x["total_bms"])[:15]
    for idx, item in enumerate(sorted_top, 1):
        c = item["cluster"]
        m0 = c[0]
        dt_str = datetime.utcfromtimestamp(m0["start_epoch"]).strftime('%Y-%m-%d %H:%M')
        print(f"\n#{idx} [{m0['sport'].upper()}] {m0['tourn_name']}", flush=True)
        print(f"   Матч: '{m0['home_name']}' vs '{m0['away_name']}' ({dt_str} UTC)", flush=True)
        print(f"   🔥 ВСЕГО БУКМЕЙКЕРОВ ПРИ ОБЪЕДИНЕНИИ: {item['total_bms']} БК (сейчас разбросано по {len(c)} клонам):", flush=True)
        for m in c:
            m_dt = datetime.utcfromtimestamp(m["start_epoch"]).strftime('%H:%M')
            bms_list = ", ".join(sorted(m["bms"])) if m["bms"] else "нет котировок"
            print(f"   👉 Match #{m['id']} [{m['origin']}/{m['created_by']}] ({m_dt} UTC) [tourn: #{m['tourn_id']} {m['tourn_name']}]:", flush=True)
            print(f"      {len(m['bms'])} БК -> [{bms_list}]", flush=True)
            print(f"      Команды: '{m['home_name']}' vs '{m['away_name']}'", flush=True)

if __name__ == '__main__':
    main()
