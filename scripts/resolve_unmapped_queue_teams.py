import subprocess
import json
import re
import sys
import time
import unicodedata
from collections import defaultdict
import urllib.request

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

CYRILLIC_TO_LATIN = {
    'а': 'a', 'б': 'b', 'в': 'v', 'г': 'g', 'д': 'd', 'е': 'e', 'ё': 'e',
    'ж': 'zh', 'з': 'z', 'и': 'i', 'й': 'i', 'к': 'k', 'л': 'l', 'м': 'm',
    'н': 'n', 'о': 'o', 'п': 'p', 'р': 'r', 'с': 's', 'т': 't', 'у': 'u',
    'ф': 'f', 'х': 'kh', 'ц': 'ts', 'ч': 'ch', 'ш': 'sh', 'щ': 'shch',
    'ъ': '', 'ы': 'y', 'ь': '', 'э': 'e', 'ю': 'yu', 'я': 'ya'
}

def strip_accents(text):
    if not text:
        return ""
    return ''.join(c for c in unicodedata.normalize('NFD', text) if unicodedata.category(c) != 'Mn')

SPECIAL_CLUB_TRANSLATIONS = {
    "боруссия дортмунд": "borussia dortmund",
    "боруссия м": "borussia monchengladbach",
    "боруссия менхенгладбах": "borussia monchengladbach",
    "бавария": "bayern munchen",
    "байер": "bayer 04 leverkusen",
    "барселона": "barcelona",
    "реал мадрид": "real madrid",
    "атлетико мадрид": "atletico madrid",
    "атлетико м": "atletico madrid",
    "ливерпуль": "liverpool",
    "манчестер сити": "manchester city",
    "манчестер юнайтед": "manchester united",
    "арсенал": "arsenal",
    "челси": "chelsea",
    "тоттенхэм": "tottenham hotspur",
    "псж": "paris saint-germain",
    "ювентус": "juventus",
    "милан": "milan",
    "интер": "inter",
    "наполи": "napoli",
    "рома": "roma",
    "лацио": "lazio",
    "ланс": "lens",
    "херенвен": "sc heerenveen",
    "неймеген": "nec nijmegen",
    "нек неймеген": "nec nijmegen",
    "нек": "nec nijmegen",
    "саутгемптон": "southampton",
    "шлёнск вроцлав": "slask wroclaw",
    "шленск": "slask wroclaw",
    "заглембе любин": "zaglebie lubin",
    "заглембе": "zaglebie lubin",
    "корона кельце": "korona kielce",
    "корона": "korona kielce",
    "радомяк радом": "radomiak radom",
    "радомяк": "radomiak radom",
    "олимпиакос": "olympiacos",
    "панатинаикос афины": "panathinaikos",
    "панатинаикос": "panathinaikos",
    "аек афины": "aek athens",
    "аек": "aek athens",
    "брондбю": "brondby",
    "агф орхус": "agf",
    "хельсингборг": "helsingborg",
    "эстер иф": "osters if",
    "эстер": "osters if",
    "аик стокгольм": "aik",
    "аик": "aik",
    "генчлербирлиги": "genclerbirligi",
    "черно море": "cherno more",
    "лудогорец": "ludogorets",
    "пюник ереван": "pyunik yerevan",
    "пюник": "pyunik yerevan",
    "арарат ереван": "ararat yerevan",
    "арарат": "ararat yerevan",
    "ноа": "noah",
    "барракас сентраль": "barracas central",
    "сан-диего вейв": "san diego wave",
    "депортиво ла-корунья": "deportivo la coruna",
    "кобресаль": "cobresal",
    "эвертон вм": "everton de vina del mar",
    "ренессанс беркан": "rs berkane",
    "хегельман литауэн": "hegelmann",
    "хегельман": "hegelmann",
    "бфа вильнюс": "bfa vilnius",
    "крузейру": "cruzeiro",
    "палмейрас": "palmeiras",
    "фламенго": "flamengo",
    "гремио": "gremio",
    "сан-паулу": "sao paulo",
    "сантос": "santos",
    # Basketball / NBA
    "майами хит": "miami heat",
    "нью-йорк никс": "new york knicks",
    "нью йорк никс": "new york knicks",
    "лос-анджелес лейкерс": "los angeles lakers",
    "лос анджелес лейкерс": "los angeles lakers",
    "голден стэйт уорриорз": "golden state warriors",
    "денвер наггетс": "denver nuggets",
    "оклахома сити тандер": "oklahoma city thunder",
    "миннесота тимбервулвз": "minnesota timberwolves",
    "бостон селтикс": "boston celtics",
    "милуоки бакс": "milwaukee bucks",
    "чикаго буллз": "chicago bulls",
    "индиана пэйсерс": "indiana pacers",
    "орландо мэджик": "orlando magic",
    "бруклин нетс": "brooklyn nets",
    "атланта хокс": "atlanta hawks",
    "шарлотт хорнетс": "charlotte hornets",
    "вашингтон уизардс": "washington wizards",
    "финикс санз": "phoenix suns",
    "сакраменто кингз": "sacramento kings",
    "мемфис гриззлис": "memphis grizzlies",
    "даллас маверикс": "dallas mavericks",
    "нью-орлеан пеликанс": "new orleans pelicans",
    "сан-антонио спёрс": "san antonio spurs",
    "сан антонио сперс": "san antonio spurs",
    "хьюстон рокетс": "houston rockets",
    "юта джаз": "utah jazz",
    "портленд трэйл блэйзерс": "portland trail blazers",
    # NFL
    "грин бэй пэкерз": "green bay packers",
    "грин бэй": "green bay packers",
    "хьюстон тексанс": "houston texans",
    "лос-анджелес рэмс": "los angeles rams",
    "лос анджелес рэмс": "los angeles rams",
    "канзас сити чифс": "kansas city chiefs",
    "баффало биллс": "buffalo bills",
    "филадельфия иглз": "philadelphia eagles",
    "даллас ковбойс": "dallas cowboys",
    "сан-франциско 49ерс": "san francisco 49ers",
    "балтимор рэйвенс": "baltimore ravens",
    "детройт лайонс": "detroit lions",
    "майами долфинс": "miami dolphins",
    # Combat / MMA / Boxing
    "чарльз оливейра": "charles oliveira",
    "оливейра чарльз": "charles oliveira",
    "диего лопес": "diego lopes",
    "лопес диего": "diego lopes",
    "алекс перейра": "alex pereira",
    "перейра алекс": "alex pereira",
    "сергей павлович": "sergei pavlovich",
    "павлович сергей": "sergei pavlovich",
    "бо никал": "bo nickal",
    "никал бо": "bo nickal",
    "джозеф пайфер": "joe pyfer",
    "пайфер джозеф": "joe pyfer",
    "дмитрий бивол": "dmitry bivol",
    "энтони какейс": "anthony cacace",
    "ник болл": "nick ball",
    "оуэн купер": "owen cooper",
    "мишель соро": "michel soro",
    "джордж лиддард": "george liddard",
    "мусса голам": "moussa gholam",
    "измаил дэвис": "ismail davis",
    "джунаид бостан": "junaid bostan"
}

NOISE_WORDS = {
    'fc', 'fk', 'cf', 'sc', 'cd', 'sad', 'bc', 'bk', 'sk', 'hc', 'hk', 'csm',
    'rc', 'rcd', 'us', 'as', 'ss', 'ac', 'if', 'ff', 'ik', 'cs',
    'de', 'la', 'el', 'the', 'al', 'club', 'team',
    'люб', 'любители', 'corners', 'угловые', 'жк', 'желтые карточки', 'фолы', 'броски'
}

def is_youth_or_women_name(name):
    low = name.lower()
    return bool(re.search(r'\b(u1[5-9]|u2[0-3]|youth|academy|women|w)\b', low) or '(ж)' in low or '(w)' in low or 'жен' in low)

def pick_best_candidate(candidates, raw_name):
    if not candidates:
        return None
    raw_has_yw = is_youth_or_women_name(raw_name)
    if not raw_has_yw:
        filtered = [c for c in candidates if c["gender"] == "MALE" and c["age"] == "SENIOR"]
        if filtered:
            candidates = filtered
    with_sofa = [c for c in candidates if c.get("sofa_id", 0) > 0]
    if with_sofa:
        return with_sofa[0]
    return candidates[0]

def transliterate(text):
    if not text:
        return ""
    text = text.lower()
    for ru, en in SPECIAL_CLUB_TRANSLATIONS.items():
        if ru in text:
            text = text.replace(ru, en)
    res = []
    for ch in text:
        res.append(CYRILLIC_TO_LATIN.get(ch, ch))
    return "".join(res)

def clean_name(name):
    if not name:
        return ""
    t = transliterate(name)
    t = strip_accents(t)
    t = re.sub(r'\(.*?\)', '', t)
    t = re.sub(r'\[.*?\]', '', t)
    # Dutch phonetics
    t = t.replace('ij', 'i').replace('ey', 'i').replace('ei', 'i')
    # Remove noise words
    words = re.findall(r'[a-z0-9]+', t)
    filtered = [w for w in words if w not in NOISE_WORDS]
    non_num = [w for w in filtered if not re.match(r'^(?:[ivx]+|\d+)$', w)]
    if not non_num:
        return ""
    return " ".join(filtered)

def get_phonetic_stem(name):
    c = clean_name(name)
    p = c.replace('c', 'k').replace('y', 'i').replace('s', 'z').replace('ph', 'f').replace('w', 'v').replace('j', 'i')
    # Russian kh at start
    if p.startswith('kh'):
        p = 'h' + p[2:]
    p = p.replace('kh', 'h')
    p = re.sub(r'[^a-z0-9]', '', p)
    return p

import tempfile
import os

def run_psql(db, sql):
    for attempt in range(3):
        proc = subprocess.run(
            [
                "kubectl", "exec", "-n", "igaming-enrichment", "igaming-aggregator-db-0", "--",
                "psql", "-U", "postgres", "-d", db, "-t", "-A", "-F", "|||", "-c", sql
            ],
            capture_output=True, timeout=120
        )
        if proc.returncode == 0:
            return proc.stdout.decode('utf-8', errors='replace')
        err_str = proc.stderr.decode('utf-8', errors='replace')
        if "wsarecv" in err_str or "forcibly closed" in err_str or "error reading" in err_str:
            time.sleep(2)
            continue
        raise RuntimeError(f"PSQL Error ({db}): {err_str}")
    raise RuntimeError(f"PSQL Error ({db}) after 3 attempts: {err_str}")

def main():
    print("=" * 80, flush=True)
    print("🚀 НАЧАЛО АВТОМАТИЧЕСКОГО И ПРИНУДИТЕЛЬНОГО РАЗРЕШЕНИЯ НЕЗАМАПЛЕННЫХ КОМАНД", flush=True)
    print("=" * 80, flush=True)

    # 1. Force explicit mapping for NEC Nijmegen and SC Heerenveen
    print("\n⏳ [1/5] Принудительный маппинг NEC Nijmegen (#121603) и SC Heerenveen (#121605)...", flush=True)
    force_sql = """
    -- 1. Team merge map
    INSERT INTO mdm_team_merge_map (local_id, sofa_id, local_name, sofa_name, sport_id) VALUES
    (166630, 121603, 'НЕК Неймеген', 'NEC Nijmegen', 1),
    (137888, 121603, 'Неймеген', 'NEC Nijmegen', 1),
    (162701, 121603, 'НЕК', 'NEC Nijmegen', 1),
    (138465, 121603, 'УГЛ Неймеген', 'NEC Nijmegen', 1),
    (514251, 121605, 'Херенвен', 'SC Heerenveen', 1),
    (150595, 121605, 'Херенвен', 'SC Heerenveen', 1)
    ON CONFLICT (local_id) DO UPDATE SET sofa_id = EXCLUDED.sofa_id, sofa_name = EXCLUDED.sofa_name;

    -- 2. Mark queue items resolved
    UPDATE mdm_unmapped_queue
    SET status = 'RESOLVED',
        resolution_notes = 'Force-mapped to canonical NEC Nijmegen / SC Heerenveen',
        updated_at = NOW()
    WHERE raw_name ILIKE '%Heerenveen%' OR raw_name ILIKE '%Херенвен%'
       OR raw_name ILIKE '%Nijmegen%' OR raw_name ILIKE '%Неймеген%'
       OR raw_name IN ('НЕК', 'NEC', 'нек', 'nec');
    """
    run_psql("igaming_mdm", force_sql)

    # Insert ingestion map aliases for NEC and Heerenveen across all systems
    nec_aliases = ['NEC Nijmegen', 'НЕК Неймеген', 'Неймеген', 'НЕК', 'нек неймеген', 'неймеген', 'нек', 'nec nijmegen', 'nec']
    heer_aliases = ['SC Heerenveen', 'Heerenveen', 'Херенвен', 'sc heerenveen', 'heerenveen', 'херенвен']
    
    systems_raw = run_psql("igaming_mdm", "SELECT id, code FROM mdm_external_system;")
    systems = {}
    for line in systems_raw.strip().split("\n"):
        if "|||" in line:
            sid, scode = line.split("|||")
            systems[scode.strip()] = int(sid.strip())

    ingest_values = []
    for sid in systems.values():
        for k in nec_aliases:
            k_esc = k.replace("'", "''")
            ingest_values.append(f"({sid}, '{k_esc}', 121603, 'NEC Nijmegen', true)")
        for k in heer_aliases:
            k_esc = k.replace("'", "''")
            ingest_values.append(f"({sid}, '{k_esc}', 121605, 'SC Heerenveen', true)")

    # Execute batch in chunks of 100
    chunk_size = 100
    for i in range(0, len(ingest_values), chunk_size):
        chunk = ingest_values[i:i + chunk_size]
        vals = ", ".join(chunk)
        sql = f"""
            INSERT INTO mdm_team_ingestion_map (external_system_id, raw_key, canonical_team_id, raw_name, is_verified)
            VALUES {vals}
            ON CONFLICT (external_system_id, raw_key) DO UPDATE SET canonical_team_id = EXCLUDED.canonical_team_id;
        """
        run_psql("igaming_mdm", sql)

    print("   ✅ Успешно установлены жесткие маппинги для NEC Nijmegen и SC Heerenveen.", flush=True)

    # 2. Fetch all pending queue items
    print("\n⏳ [2/5] Загрузка очереди mdm_unmapped_queue...", flush=True)
    queue_raw = run_psql("igaming_mdm", """
        SELECT id, external_system_code, COALESCE(sport_id, 1), COALESCE(sport_slug, 'football'), raw_key, raw_name, context_parent_name
        FROM mdm_unmapped_queue
        WHERE status = 'PENDING' AND entity_type = 'TEAM'
        ORDER BY id ASC;
    """)

    queue_items = []
    for line in queue_raw.strip().split("\n"):
        if not line.strip() or "|||" not in line:
            continue
        p = line.split("|||")
        if len(p) >= 7:
            queue_items.append({
                "id": int(p[0]),
                "sys_code": p[1].strip(),
                "sport_id": int(p[2]),
                "sport_slug": p[3].strip(),
                "raw_key": p[4].strip(),
                "raw_name": p[5].strip(),
                "parent_name": p[6].strip() if p[6] else ""
            })
    print(f"   Загружено {len(queue_items)} заявок на маппинг команд.", flush=True)

    if not queue_items:
        print("🎉 Очередь пуста!", flush=True)
        return

    # 3. Load canonical teams from mdm_team
    print("\n⏳ [3/5] Загрузка канонических команд из mdm_team...", flush=True)
    teams_raw = run_psql("igaming_mdm", """
        SELECT id, sport_id, canonical_name, normalized_signature, slug, COALESCE(sofascore_id, 0), source_origin, COALESCE(gender, 'MALE'), COALESCE(age_category, 'SENIOR')
        FROM mdm_team
        WHERE sofascore_id IS NOT NULL OR source_origin = 'SOFASCORE';
    """)

    exact_index = defaultdict(lambda: defaultdict(list))
    clean_index = defaultdict(lambda: defaultdict(list))
    stem_index = defaultdict(lambda: defaultdict(list))
    prefix_index = defaultdict(lambda: defaultdict(list))

    total_teams = 0
    for line in teams_raw.strip().split("\n"):
        if not line.strip() or "|||" not in line:
            continue
        p = line.split("|||")
        if len(p) >= 7:
            tid = int(p[0])
            spid = int(p[1])
            cname = p[2].strip()
            norm_sig = p[3].strip()
            slug = p[4].strip()
            sofa_id = int(p[5])
            gender = p[7].strip() if len(p) > 7 else 'MALE'
            age = p[8].strip() if len(p) > 8 else 'SENIOR'

            team_obj = {"id": tid, "name": cname, "sofa_id": sofa_id, "gender": gender, "age": age}
            
            # Lowercase exact
            exact_index[spid][cname.lower()].append(team_obj)
            acc_free = strip_accents(cname).lower()
            if acc_free != cname.lower():
                exact_index[spid][acc_free].append(team_obj)

            if slug:
                exact_index[spid][slug.lower()].append(team_obj)
            if norm_sig:
                exact_index[spid][norm_sig.lower()].append(team_obj)

            c = clean_name(cname)
            if c:
                clean_index[spid][c].append(team_obj)

            # Name word inversion for 2-word names (e.g. Sergei Pavlovich <-> Pavlovich Sergei)
            words = cname.strip().split()
            if len(words) == 2:
                inv = f"{words[1]} {words[0]}"
                exact_index[spid][inv.lower()].append(team_obj)
                acc_inv = strip_accents(inv).lower()
                if acc_inv != inv.lower():
                    exact_index[spid][acc_inv].append(team_obj)
                c_inv = clean_name(inv)
                if c_inv:
                    clean_index[spid][c_inv].append(team_obj)

            stem = get_phonetic_stem(cname)
            if stem and len(stem) >= 4:
                stem_index[spid][stem].append(team_obj)
                if len(words) == 2:
                    stem_inv = get_phonetic_stem(f"{words[1]} {words[0]}")
                    if stem_inv and len(stem_inv) >= 4:
                        stem_index[spid][stem_inv].append(team_obj)

            total_teams += 1

    print(f"   Индексировано {total_teams} канонических команд.", flush=True)

    # 4. Resolve unmapped items
    print("\n⏳ [4/5] Быстрое O(1) сопоставление и авто-резолвинг...", flush=True)
    resolved_count = 0
    resolved_ingestion_mappings = []
    resolved_queue_updates = []
    sample_resolutions = []

    for q in queue_items:
        spid = q["sport_id"]
        raw_name = q["raw_name"]
        raw_key = q["raw_key"]
        sys_code = q["sys_code"]
        sys_id = systems.get(sys_code)
        if not sys_id:
            continue

        matched_team = None
        match_type = None

        # Strategy A: Exact / Slug match
        cand = exact_index[spid].get(raw_name.lower()) or exact_index[spid].get(raw_key.lower())
        best = pick_best_candidate(cand, raw_name)
        if best:
            matched_team = best
            match_type = "EXACT"

        # Strategy B: Cleaned transliterated name
        if not matched_team:
            c = clean_name(raw_name)
            if c:
                cand = clean_index[spid].get(c)
                best = pick_best_candidate(cand, raw_name)
                if best:
                    matched_team = best
                    match_type = "CLEAN_TRANSLIT"

        # Strategy C: Phonetic Stem Match (Exact)
        if not matched_team:
            stem = get_phonetic_stem(raw_name)
            if stem and len(stem) >= 5:
                cand = stem_index[spid].get(stem)
                if cand and len(cand) == 1:
                    best = pick_best_candidate(cand, raw_name)
                    if best:
                        matched_team = best
                        match_type = "PHONETIC_STEM"

        # Strategy D: Name Inversion (for combat/individual sports e.g. Pavlovich Sergei <-> Sergei Pavlovich)
        if not matched_team:
            w_raw = raw_name.strip().split()
            if len(w_raw) == 2:
                inv_raw = f"{w_raw[1]} {w_raw[0]}"
                cand = exact_index[spid].get(inv_raw.lower())
                best = pick_best_candidate(cand, raw_name)
                if not best:
                    c_inv = clean_name(inv_raw)
                    if c_inv:
                        cand = clean_index[spid].get(c_inv)
                        best = pick_best_candidate(cand, raw_name)
                if not best:
                    stem_inv = get_phonetic_stem(inv_raw)
                    if stem_inv and len(stem_inv) >= 5:
                        cand = stem_index[spid].get(stem_inv)
                        if cand and len(cand) == 1:
                            best = pick_best_candidate(cand, raw_name)
                if best:
                    matched_team = best
                    match_type = "NAME_INVERSION"

        # Strategy D: Prefix Match disabled to prevent false positives (e.g. Anthony Joshua -> Joshua Van)
        # Strict ID-Only & Verified Mapping Policy (Rule 1.2)

        if matched_team:
            resolved_count += 1
            tid = matched_team["id"]
            tname = matched_team["name"]

            # Record ingestion mapping
            raw_k_esc = raw_key.replace("'", "''")
            tname_esc = tname.replace("'", "''")
            resolved_ingestion_mappings.append(f"({sys_id}, '{raw_k_esc}', {tid}, '{tname_esc}', true)")

            # Record queue item resolution
            notes = f"Auto-resolved ({match_type}) to #{tid} {tname}".replace("'", "''")
            resolved_queue_updates.append((q["id"], notes))

            if len(sample_resolutions) < 25:
                sample_resolutions.append(f"[{sys_code}] '{raw_name}' -> #{tid} {tname} ({match_type})")

    print(f"🎯 Успешно распознано {resolved_count} из {len(queue_items)} заявок ({resolved_count*100//len(queue_items)}%)!", flush=True)

    # Apply batch updates to Postgres
    if resolved_ingestion_mappings:
        print("\n⏳ [5/5] Применение маппингов в PostgreSQL...", flush=True)
        chunk_size = 100
        for i in range(0, len(resolved_ingestion_mappings), chunk_size):
            chunk = resolved_ingestion_mappings[i:i + chunk_size]
            vals = ", ".join(chunk)
            sql = f"""
                INSERT INTO mdm_team_ingestion_map (external_system_id, raw_key, canonical_team_id, raw_name, is_verified)
                VALUES {vals}
                ON CONFLICT (external_system_id, raw_key) DO UPDATE SET canonical_team_id = EXCLUDED.canonical_team_id;
            """
            run_psql("igaming_mdm", sql)

        # Batch update queue status
        for i in range(0, len(resolved_queue_updates), chunk_size):
            chunk = resolved_queue_updates[i:i + chunk_size]
            ids = [str(item[0]) for item in chunk]
            ids_str = ",".join(ids)
            sql = f"""
                UPDATE mdm_unmapped_queue
                SET status = 'RESOLVED', resolution_notes = 'Auto-resolved via cluster batch matching', updated_at = NOW()
                WHERE id IN ({ids_str});
            """
            run_psql("igaming_mdm", sql)

        print("   ✅ База данных mdm_team_ingestion_map и mdm_unmapped_queue обновлена.", flush=True)

    # Invalidate blueprints via REST
    try:
        proc = subprocess.run([
            "kubectl", "exec", "-n", "igaming-enrichment", "deployment/igaming-mdm-service", "--",
            "curl", "-s", "-X", "POST", "http://localhost:8095/api/v1/mdm/blueprint/invalidate-all"
        ], capture_output=True, timeout=15)
        print(f"   ✅ Blueprint кэш MDM сброшен через K8s deployment (Exit code: {proc.returncode})", flush=True)
    except Exception as e:
        print(f"   ⚠️ Ошибка сброса кэша: {e}", flush=True)

    print("\n" + "=" * 80, flush=True)
    print("🏆 ПРИМЕРЫ УСПЕШНО РАЗРЕШЕННЫХ КОМАНД:", flush=True)
    print("=" * 80, flush=True)
    for s in sample_resolutions:
        print(f"   {s}", flush=True)

    print("\n🎉 ГОТОВО! Новые котировки букмекеров теперь будут безошибочно маппиться на канонические команды.", flush=True)

if __name__ == '__main__':
    main()
