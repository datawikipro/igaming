import subprocess
import json
import re
import sys
import time
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
        res.append(CYRILLIC_TO_LATIN.get(ch, ch))
    return "".join(res)

def normalize_stem(name):
    if not name:
        return ""
    t = transliterate(name)
    t = re.sub(r'\(.*?\)', '', t)
    # remove noise words and stat markers
    t = re.sub(r'\b(fc|fk|cf|sc|cd|sad|bc|sk|u\d+|games|reserve|women|damas|frauen|de|la|el|the|жк|угл|голы|фолы|офсайды)\b', '', t, flags=re.I)
    # phonetics
    t = t.replace('c', 'k').replace('y', 'i').replace('s', 'z').replace('ph', 'f').replace('w', 'v').replace('j', 'i')
    t = re.sub(r'[^a-z0-9]', '', t)
    return t[:8] if len(t) >= 8 else t

def clean_tokens(name):
    if not name: return set()
    t = transliterate(name)
    t = re.sub(r'\(.*?\)', '', t)
    t = re.sub(r'\b(fc|fk|cf|sc|cd|sad|bc|sk|u\d+|games|reserve|women|damas|frauen|de|la|el|the|жк|угл|голы|фолы|офсайды)\b', '', t, flags=re.I)
    t = t.replace('c', 'k').replace('y', 'i').replace('s', 'z').replace('ph', 'f').replace('w', 'v').replace('j', 'i')
    words = re.findall(r'[a-z0-9]{3,}', t)
    return set(words)

GENERIC_WORDS = {
    'united', 'city', 'real', 'sporting', 'atletico', 'dinamo', 'dynamo', 'spartak',
    'lokomotiv', 'torpedo', 'cska', 'state', 'college', 'university', 'club', 'team',
    'inter', 'racing', 'saint', 'san', 'santa', 'academica', 'deportivo', 'olimpik'
}

def opps_match(name1, name2, stem1, stem2):
    if not name1 or not name2: return False
    # 1. Direct stem match
    if stem1 and stem2 and stem1 == stem2:
        return True
    
    # Clean alphanumeric representations
    clean1 = re.sub(r'[^a-zA-Z0-9]', '', transliterate(name1)).lower()
    clean2 = re.sub(r'[^a-zA-Z0-9]', '', transliterate(name2)).lower()
    
    # 2. Substring inclusion (e.g. 'Dynamo Moscow' in 'Dynamo Moscow U20')
    if len(clean1) >= 6 and len(clean2) >= 6:
        if clean1 in clean2 or clean2 in clean1:
            return True
            
    # 3. Token Jaccard >= 0.5 (excluding generic words)
    t1 = clean_tokens(name1) - GENERIC_WORDS
    t2 = clean_tokens(name2) - GENERIC_WORDS
    if t1 and t2:
        intersection = t1 & t2
        union = t1 | t2
        jaccard = len(intersection) / len(union)
        if jaccard >= 0.5 or (len(intersection) >= 2):
            return True
            
    return False

def is_stat_market(name):
    if not name:
        return False
    lower = name.lower()
    stat_words = [
        '(bookings)', '(corners)', '(games)', 'желтые карточки', 'жк ', 'угловые',
        '(голы)', 'броски', '(углы)', 'углы', '(жк)', '(фолы)', '(офсайды)',
        '(kamito)', '(crinner)', '(loka)', 'cyber', 'battle', 'short'
    ]
    return any(w in lower for w in stat_words)

def run_psql(db, sql):
    cmd = [
        "kubectl", "exec", "-i", "-n", "igaming-enrichment", "igaming-aggregator-db-0", "--",
        "psql", "-U", "postgres", "-d", db, "-t", "-A", "-F", "|||"
    ]
    proc = subprocess.run(cmd, input=sql, capture_output=True, text=True, encoding='utf-8')
    if proc.returncode != 0:
        raise RuntimeError(f"PSQL Error ({db}): {proc.stderr}")
    return proc.stdout

def run_redis_eval(lua_script, *args):
    cmd = [
        "kubectl", "exec", "-n", "igaming-enrichment", "igaming-redis-594d8d879-7qrrr", "--",
        "redis-cli", "EVAL", lua_script, "0"
    ] + list(args)
    proc = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
    if proc.returncode != 0:
        raise RuntimeError(f"Redis EVAL Error: {proc.stderr}")
    return proc.stdout.strip()

def main():
    print("=" * 80, flush=True)
    print("🚀 НАЧАЛО СКВОЗНОЙ СКЛЕЙКИ ДУБЛИКАТОВ МАТЧЕЙ И КОНСОЛИДАЦИИ ЛИНИИ", flush=True)
    print("=" * 80, flush=True)

    # 1. Fetch active matches from MDM
    print("⏳ [1/5] Загрузка активных матчей из MDM...", flush=True)
    matches_raw = run_psql("igaming_mdm", """
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
        WHERE (m.status IN ('SCHEDULED', 'LIVE') OR (m.status = 'CANCELLED' AND m.updated_at >= NOW() - INTERVAL '2 hours'))
          AND m.start_time >= NOW() - INTERVAL '12 hours' AND m.start_time <= NOW() + INTERVAL '7 days';
    """)

    matches = []
    for line in matches_raw.strip().split("\n"):
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
                "stem_away": normalize_stem(parts[8]),
                "is_stat": is_stat_market(parts[6]) or is_stat_market(parts[8]) or is_stat_market(parts[4])
            })
    print(f"✅ Загружено {len(matches)} активных матчей.", flush=True)

    # 2. Fetch bookmakers from Redis
    print("⏳ [2/5] Загрузка списка БК для каждого матча из Redis...", flush=True)
    redis_bms_raw = run_redis_eval("""
        local keys = redis.call('keys', 'match:bms:*')
        local res = {}
        for i = 1, #keys do
            local bms = redis.call('smembers', keys[i])
            table.insert(res, keys[i] .. '=' .. table.concat(bms, ','))
        end
        return res
    """)
    match_bms = defaultdict(set)
    for line in redis_bms_raw.split("\n"):
        line = line.strip()
        if not line or "=" not in line:
            continue
        k, v = line.split("=", 1)
        mid_str = k.replace("match:bms:", "").strip()
        if mid_str.isdigit():
            mid = int(mid_str)
            bms = [b.strip() for b in v.split(",") if b.strip()]
            match_bms[mid] = set(bms)

    for m in matches:
        bms = set(match_bms.get(m["id"], []))
        if m["created_by"] and m["created_by"] not in ("sofascore", "system"):
            bms.add(m["created_by"])
        m["bms"] = bms
        m["bm_count"] = len(bms)

    print(f"✅ Привязаны котировки БК к {len(matches)} матчам.", flush=True)

    # 3. DSU Cluster Clustering
    print("⏳ [3/5] Кластеризация и определение канонических матчей...", flush=True)
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

    # A) Exact or merge_map team IDs (12h window)
    team_id_map = defaultdict(list)
    for m in matches:
        if m["is_stat"]:
            continue
        tb = round(m["start_epoch"] / (12 * 3600))
        t_pair = tuple(sorted([m["norm_home_id"], m["norm_away_id"]]))
        team_id_map[(m["sport_id"], t_pair, tb)].append(m)

    for key, group in team_id_map.items():
        if len(group) > 1:
            for k in range(1, len(group)):
                union(group[0]["id"], group[k]["id"])

    # B) Normalized stem names (Cyrillic + Phonetic, 6h window)
    stem_map = defaultdict(list)
    for m in matches:
        if m["is_stat"] or not m["stem_home"] or not m["stem_away"]:
            continue
        tb = round(m["start_epoch"] / (6 * 3600))
        s_pair = tuple(sorted([m["stem_home"], m["stem_away"]]))
        stem_map[(m["sport_id"], s_pair, tb)].append(m)

    for key, group in stem_map.items():
        if len(group) > 1:
            for k in range(1, len(group)):
                union(group[0]["id"], group[k]["id"])

    # C) Home Anchor Match: Home team matches by stem, away team matches by token overlap / substring (3h window)
    home_index = defaultdict(list)
    for m in matches:
        if m["is_stat"] or not m["stem_home"]:
            continue
        tb = round(m["start_epoch"] / (3 * 3600))
        home_index[(m["sport_id"], m["stem_home"], tb)].append(m)

    for key, grp in home_index.items():
        if len(grp) < 2:
            continue
        for i in range(len(grp)):
            for j in range(i + 1, len(grp)):
                m1, m2 = grp[i], grp[j]
                if abs(m1["start_epoch"] - m2["start_epoch"]) <= 7200:
                    if opps_match(m1["away_name"], m2["away_name"], m1["stem_away"], m2["stem_away"]):
                        union(m1["id"], m2["id"])

    # D) Away Anchor Match: Away team matches by stem, home team matches by token overlap / substring (3h window)
    away_index = defaultdict(list)
    for m in matches:
        if m["is_stat"] or not m["stem_away"]:
            continue
        tb = round(m["start_epoch"] / (3 * 3600))
        away_index[(m["sport_id"], m["stem_away"], tb)].append(m)

    for key, grp in away_index.items():
        if len(grp) < 2:
            continue
        for i in range(len(grp)):
            for j in range(i + 1, len(grp)):
                m1, m2 = grp[i], grp[j]
                if abs(m1["start_epoch"] - m2["start_epoch"]) <= 7200:
                    if opps_match(m1["home_name"], m2["home_name"], m1["stem_home"], m2["stem_home"]):
                        union(m1["id"], m2["id"])

    # Group into clusters
    cluster_groups = defaultdict(list)
    for m in matches:
        root = find(m["id"])
        cluster_groups[root].append(m)

    duplicate_clusters = [grp for grp in cluster_groups.values() if len(grp) > 1]
    print(f"🎯 Найдено {len(duplicate_clusters)} групп дубликатов для объединения.", flush=True)

    # Prepare merge instructions
    dup_to_canon = {}      # dup_match_id -> canon_match_id
    team_merge_records = []# (local_id, sofa_id, local_name, sofa_name, sport_id)
    canon_sofa_promotions = [] # (canon_id, sofa_id)

    top_examples_summary = []

    for c in duplicate_clusters:
        # Score candidates to select the absolute best canonical match:
        # 1. Has sofascore_id: +1000
        # 2. Origin SOFASCORE: +500
        # 3. Number of bookmakers in Redis: +bm_count * 10
        # 4. Tie-break: lowest ID (earliest created)
        def score_match(m):
            s = 0
            if m["status"] in ("SCHEDULED", "LIVE"):
                s += 2000
            if m["sofa_id"] > 0:
                s += 1000
            if m["origin"] == "SOFASCORE":
                s += 500
            s += m["bm_count"] * 10
            s -= (m["id"] / 10000000.0)
            return s

        sorted_cluster = sorted(c, key=score_match, reverse=True)
        canon = sorted_cluster[0]
        dups = sorted_cluster[1:]

        # Check if any dup has a sofascore_id that canon lacked
        if canon["sofa_id"] == 0:
            for d in dups:
                if d["sofa_id"] > 0:
                    canon_sofa_promotions.append((canon["id"], d["sofa_id"]))
                    canon["sofa_id"] = d["sofa_id"]
                    break

        all_cluster_bms = set(canon["bms"])
        for d in dups:
            dup_to_canon[d["id"]] = canon["id"]
            all_cluster_bms.update(d["bms"])

            # If team IDs differ, record team merge mapping
            if d["home_id"] != canon["home_id"]:
                team_merge_records.append((d["home_id"], canon["home_id"], d["home_name"], canon["home_name"], canon["sport_id"]))
            if d["away_id"] != canon["away_id"]:
                team_merge_records.append((d["away_id"], canon["away_id"], d["away_name"], canon["away_name"], canon["sport_id"]))

        if len(dups) >= 1 and len(all_cluster_bms) >= 5:
            top_examples_summary.append({
                "sport": canon["sport"],
                "canon_id": canon["id"],
                "canon_teams": f"{canon['home_name']} vs {canon['away_name']}",
                "tourn": canon["tourn_name"],
                "canon_bms": len(canon["bms"]),
                "dups_count": len(dups),
                "total_bms": len(all_cluster_bms)
            })

    total_dups_to_merge = len(dup_to_canon)
    print(f"📊 Всего матчей-клонов к ликвидации и слиянию: {total_dups_to_merge}", flush=True)

    if total_dups_to_merge == 0:
        print("🎉 Дубликатов не обнаружено. Все чисто!", flush=True)
        return

    # 4. Apply Postgres Updates
    print("⏳ [4/5] Применение слияния в PostgreSQL (MDM и Aggregator)...", flush=True)

    # 4a. Update team merge map in chunks via stdin
    if team_merge_records:
        chunk_size = 200
        for i in range(0, len(team_merge_records), chunk_size):
            chunk = team_merge_records[i:i + chunk_size]
            values_sql = ", ".join(
                f"({loc_id}, {sofa_id}, '{loc_n.replace(chr(39), chr(39)+chr(39))}', '{sofa_n.replace(chr(39), chr(39)+chr(39))}', {sp_id})"
                for loc_id, sofa_id, loc_n, sofa_n, sp_id in chunk
            )
            sql_team_merge = f"""
                INSERT INTO mdm_team_merge_map (local_id, sofa_id, local_name, sofa_name, sport_id)
                VALUES {values_sql}
                ON CONFLICT (local_id) DO UPDATE 
                SET sofa_id = EXCLUDED.sofa_id, sofa_name = EXCLUDED.sofa_name;
            """
            run_psql("igaming_mdm", sql_team_merge)
        print(f"   ✅ Записано/обновлено {len(team_merge_records)} связок команд в mdm_team_merge_map.", flush=True)

    # 4b. Promote sofascore_ids if any
    for cid, sid in canon_sofa_promotions:
        run_psql("igaming_mdm", f"UPDATE mdm_match SET sofascore_id = {sid}, source_origin = 'SOFASCORE' WHERE id = {cid};")

    # 4c. Cancel duplicate matches in MDM and Aggregator
    dup_ids_list = list(dup_to_canon.keys())
    chunk_size = 500
    for i in range(0, len(dup_ids_list), chunk_size):
        chunk = dup_ids_list[i:i + chunk_size]
        ids_str = ",".join(str(x) for x in chunk)
        run_psql("igaming_mdm", f"UPDATE mdm_match SET status = 'CANCELLED', updated_at = NOW() WHERE id IN ({ids_str});")
        run_psql("igaming_aggregator", f"UPDATE match_enrichment SET status = 'CANCELLED', is_live = false, updated_at = NOW() WHERE match_id IN ({ids_str});")

    print(f"   ✅ Переведены в статус CANCELLED {len(dup_ids_list)} записей матчей-клонов в MDM и Aggregator.", flush=True)

    # 4d. Re-point or cancel AI Recommendations for duplicate matches
    print("⏳ [4d] Перенос AI-рекомендаций с дубликатов на канонические матчи...", flush=True)
    if dup_to_canon:
        try:
            pairs_sql = ", ".join(f"({d}, {c})" for d, c in dup_to_canon.items())
            batch_rec_sql = f"""
                WITH pairs(d_id, c_id) AS (
                    VALUES {pairs_sql}
                ),
                canon_status AS (
                    SELECT p.d_id, p.c_id,
                           EXISTS (SELECT 1 FROM match_ai_recommendation r WHERE r.match_id = p.c_id) AS has_canon
                    FROM pairs p
                ),
                repointed AS (
                    UPDATE match_ai_recommendation r
                    SET match_id = cs.c_id, updated_at = NOW()
                    FROM canon_status cs
                    WHERE r.match_id = cs.d_id AND NOT cs.has_canon
                    RETURNING r.id
                )
                UPDATE match_ai_recommendation r
                SET result_status = 'CANCELLED', updated_at = NOW()
                FROM canon_status cs
                WHERE r.match_id = cs.d_id AND cs.has_canon AND r.result_status = 'PENDING';
            """
            run_psql("igaming_analytics", batch_rec_sql)
            print(f"   ✅ AI-рекомендации пакетно синхронизированы для {len(dup_to_canon)} пар дубликатов.", flush=True)
        except Exception as e:
            print(f"   ⚠️ Ошибка синхронизации AI-рекомендаций: {e}", flush=True)

    # 5. Apply Redis Migrations (Quotes, BMS, unify:match repointing)
    print("⏳ [5/5] Применение слияния в Redis (перенос котировок, БК и редирект unify:match)...", flush=True)

    redis_merge_lua = """
        local dup_to_canon = {}
        for i = 1, #ARGV, 2 do
            dup_to_canon[ARGV[i]] = ARGV[i+1]
        end

        local repointed = 0
        local keys = redis.call('keys', 'unify:match:*')
        for _, k in ipairs(keys) do
            local v = redis.call('get', k)
            if v and dup_to_canon[v] then
                redis.call('set', k, dup_to_canon[v])
                repointed = repointed + 1
            end
        end

        local merged_bms = 0
        for dup_id, canon_id in pairs(dup_to_canon) do
            -- Merge bms
            local bms = redis.call('smembers', 'match:bms:' .. dup_id)
            if #bms > 0 then
                for _, bm in ipairs(bms) do
                    redis.call('sadd', 'match:bms:' .. canon_id, bm)
                    merged_bms = merged_bms + 1
                end
            end
            redis.call('del', 'match:bms:' .. dup_id)

            -- Merge bm_updated
            local bm_ups = redis.call('hgetall', 'match:bm_updated:' .. dup_id)
            if #bm_ups > 0 then
                for j = 1, #bm_ups, 2 do
                    redis.call('hset', 'match:bm_updated:' .. canon_id, bm_ups[j], bm_ups[j+1])
                end
            end
            redis.call('del', 'match:bm_updated:' .. dup_id)

            -- Merge odds
            local odds = redis.call('hgetall', 'odds:match:' .. dup_id)
            if #odds > 0 then
                for j = 1, #odds, 2 do
                    local field = odds[j]
                    local val = odds[j+1]
                    if redis.call('hexists', 'odds:match:' .. canon_id, field) == 0 then
                        redis.call('hset', 'odds:match:' .. canon_id, field, val)
                    end
                end
            end
            redis.call('del', 'odds:match:' .. dup_id)

            -- Remove dup from active
            redis.call('srem', 'matches:active', dup_id)
            redis.call('sadd', 'matches:active', canon_id)
        end

        return {repointed, merged_bms}
    """

    # Run Redis merge in chunks of 50 pairs to keep ARGV small and safe
    total_repointed = 0
    total_bms_merged = 0
    items = list(dup_to_canon.items())
    chunk_size = 50

    for i in range(0, len(items), chunk_size):
        chunk = items[i:i + chunk_size]
        args = []
        for d_id, c_id in chunk:
            args.append(str(d_id))
            args.append(str(c_id))
        res_str = run_redis_eval(redis_merge_lua, *args)
        lines = [x.strip() for x in res_str.split("\n") if x.strip()]
        if len(lines) >= 2:
            try:
                total_repointed += int(lines[0])
                total_bms_merged += int(lines[1])
            except Exception:
                pass
        if (i // chunk_size) % 5 == 0 or (i + chunk_size) >= len(items):
            print(f"   ... обработано {min(i + chunk_size, len(items))}/{len(items)} пар дубликатов", flush=True)

    print(f"✅ В Redis перенаправлено {total_repointed} ключей unify:match, перенесено {total_bms_merged} котировок БК.", flush=True)

    print("\n" + "=" * 80, flush=True)
    print("🏆 ПРИМЕРЫ УСПЕШНО СКЛЕЕННЫХ ТОПОВЫХ МАТЧЕЙ:", flush=True)
    print("=" * 80, flush=True)
    top_examples_summary.sort(key=lambda x: -x["total_bms"])
    for idx, ex in enumerate(top_examples_summary[:15], 1):
        print(f"#{idx} [{ex['sport'].upper()}] {ex['tourn']}")
        print(f"   Матч: {ex['canon_teams']} (Канонический ID: #{ex['canon_id']})")
        print(f"   🔥 Было у канона: {ex['canon_bms']} БК -> СТАЛО ПОСЛЕ СКЛЕЙКИ: {ex['total_bms']} БК! (поглощено {ex['dups_count']} клонов)", flush=True)

    print("\n🎉 ВСЕ ДУБЛИКАТЫ УСПЕШНО ОБЪЕДИНЕНЫ! ЛИКВИДНОСТЬ ВОССТАНОВЛЕНА В РЕАЛЬНОМ ВРЕМЕНИ.", flush=True)

if __name__ == '__main__':
    main()
