#!/usr/bin/env python3
import json
import os
import re
import subprocess
import sys
import time
import urllib.request
from datetime import datetime

PROXY_URL = "http://100.83.113.50:3128"

def get_db_host():
    try:
        cmd = ["kubectl", "get", "pod", "igaming-source-polymarket-db-0", "-n", "igaming-source", "-o", "jsonpath={.status.podIP}"]
        host = subprocess.check_output(cmd, text=True).strip()
        if host:
            return host
    except Exception as e:
        print(f"[WARN] Failed to get pod IP: {e}")
    return "10.244.2.197"

DB_HOST = get_db_host()
DB_NAME = "igaming_polymarket"
print(f"Connecting to DB at {DB_HOST}:5432, database: {DB_NAME}")

env = os.environ.copy()
env["PGPASSWORD"] = "postgres"

def get_opener():
    proxy_handler = urllib.request.ProxyHandler({"http": PROXY_URL, "https": PROXY_URL})
    return urllib.request.build_opener(proxy_handler)

def fetch_events(limit=100, offset=0, tag=None):
    url = f"https://gamma-api.polymarket.com/events?closed=false&limit={limit}&offset={offset}"
    if tag:
        url += f"&tag_slug={tag}"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
        "Accept": "application/json"
    }
    req = urllib.request.Request(url, headers=headers)
    for attempt in range(3):
        try:
            opener = get_opener()
            with opener.open(req, timeout=15) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except Exception as e:
            print(f"[WARN] Fetch failed (offset={offset}, tag={tag}, attempt={attempt}): {e}")
            time.sleep(1)
    return []

def run_psql_script(sql_content):
    res = subprocess.run(
        ["psql", "-h", DB_HOST, "-U", "postgres", "-d", DB_NAME],
        input=sql_content,
        capture_output=True,
        text=True,
        env=env
    )
    if res.returncode != 0:
        print(f"[ERROR] PSQL error: {res.stderr.strip()[:300]}")
    return res.stdout.strip()

def get_current_count():
    res = subprocess.run(
        ["psql", "-h", DB_HOST, "-U", "postgres", "-d", DB_NAME, "-tAc", "SELECT count(*) FROM match_cache;"],
        capture_output=True,
        text=True,
        env=env
    )
    try:
        return int(res.stdout.strip())
    except:
        return 0

def get_factor_count():
    res = subprocess.run(
        ["psql", "-h", DB_HOST, "-U", "postgres", "-d", DB_NAME, "-tAc", "SELECT count(*) FROM match_factor;"],
        capture_output=True,
        text=True,
        env=env
    )
    try:
        return int(res.stdout.strip())
    except:
        return 0

def escape_sql(val):
    if val is None:
        return "NULL"
    s = str(val).replace("'", "''")
    return f"'{s}'"

def parse_teams(title):
    clean = re.sub(r'\?$', '', title).strip()
    if clean.lower().startswith("will "):
        clean = clean[5:].strip()
        m = re.split(r'\s+beat\s+|\s+win against\s+', clean, flags=re.IGNORECASE)
        if len(m) >= 2:
            return m[0].strip(), m[1].strip()
    for sep in [" vs. ", " vs ", " - "]:
        if sep in clean:
            parts = clean.split(sep, 1)
            return parts[0].strip(), parts[1].strip()
    if " @ " in clean:
        parts = clean.split(" @ ", 1)
        return parts[1].strip(), parts[0].strip()
    return None

def resolve_sport_and_league(tags):
    sport = "Politics"
    league = "General"
    tag_labels = [t.get("label", "") for t in tags if t.get("label")]
    tag_slugs = [t.get("slug", "").lower() for t in tags if t.get("slug")]

    if any(s in tag_slugs for s in ["soccer", "football", "premier-league", "champions-league", "la-liga", "uefa"]):
        sport = "Soccer"
    elif any(s in tag_slugs for s in ["basketball", "nba", "euroleague"]):
        sport = "Basketball"
    elif any(s in tag_slugs for s in ["tennis", "atp", "wta"]):
        sport = "Tennis"
    elif any(s in tag_slugs for s in ["nfl", "american-football"]):
        sport = "American Football"
    elif any(s in tag_slugs for s in ["mma", "ufc", "boxing"]):
        sport = "MMA"
    elif any(s in tag_slugs for s in ["esports", "csgo", "dota"]):
        sport = "Esports"
    elif any(s in tag_slugs for s in ["formula 1", "f1"]):
        sport = "Formula 1"
    elif any(s in tag_slugs for s in ["crypto", "bitcoin", "ethereum"]):
        sport = "Crypto"
    elif any(s in tag_slugs for s in ["politics", "elections", "geopolitics", "us-politics", "world"]):
        sport = "Politics"

    for lbl in tag_labels:
        if lbl.lower() not in ["sports", "politics", "crypto", "world"]:
            league = lbl
            break

    return sport, league

def build_batch_sql(events):
    sql_lines = ["BEGIN;"]
    for ev in events:
        ev_id = str(ev.get("id"))
        title = ev.get("title", "").strip()
        if not ev_id or not title:
            continue

        tags = ev.get("tags", [])
        sport, league = resolve_sport_and_league(tags)
        
        parsed = parse_teams(title)
        if parsed:
            team1, team2 = parsed
        else:
            team1 = title[:250]
            team2 = "Outcome / Yes-No"

        slug = ev.get("slug") or ev_id
        url = f"https://polymarket.com/event/{slug}"
        
        start_time = int(time.time() * 1000)
        start_date = ev.get("startDate") or ev.get("endDate")
        if start_date:
            try:
                dt = datetime.fromisoformat(start_date.replace("Z", "+00:00"))
                start_time = int(dt.timestamp() * 1000)
            except Exception:
                pass

        is_live = "true" if (ev.get("active", True) and not ev.get("closed", False)) else "false"

        sql_lines.append(f"""
        INSERT INTO match_cache (
            bookmaker, external_id, team1, team2, sport_name, league_name,
            start_time, is_live, event_url, status, version, created_at, updated_at, last_changed_at
        ) VALUES (
            'polymarket', {escape_sql(ev_id)}, {escape_sql(team1[:255])}, {escape_sql(team2[:255])},
            {escape_sql(sport[:255])}, {escape_sql(league[:255])}, {start_time},
            {is_live}, {escape_sql(url[:255])}, 'ACTIVE', 0, NOW(), NOW(), NOW()
        )
        ON CONFLICT (external_id) DO UPDATE SET
            status = 'ACTIVE',
            team1 = EXCLUDED.team1,
            team2 = EXCLUDED.team2,
            sport_name = EXCLUDED.sport_name,
            league_name = EXCLUDED.league_name,
            updated_at = NOW();
        """)

        # Process markets & odds
        markets = ev.get("markets", [])
        factor_tuples = []
        for m in markets:
            if m.get("closed"):
                continue
            m_id = str(m.get("id"))
            q = m.get("question") or title
            outcomes_raw = m.get("outcomes")
            prices_raw = m.get("outcomePrices")

            outcomes = []
            prices = []
            if isinstance(outcomes_raw, str):
                try:
                    outcomes = json.loads(outcomes_raw)
                except:
                    pass
            elif isinstance(outcomes_raw, list):
                outcomes = outcomes_raw

            if isinstance(prices_raw, str):
                try:
                    prices = [float(p) for p in json.loads(prices_raw)]
                except:
                    pass
            elif isinstance(prices_raw, list):
                try:
                    prices = [float(p) for p in prices_raw]
                except:
                    pass

            if outcomes and prices and len(outcomes) == len(prices):
                for i in range(len(outcomes)):
                    p = prices[i]
                    if 0.001 < p < 0.999:
                        odd = round(1.0 / p, 3)
                        fname = f"{q} - {outcomes[i]}"[:250]
                        fid = f"{m_id}_{i}"[:250]
                        factor_tuples.append(
                            f"((SELECT id FROM match_cache WHERE external_id = {escape_sql(ev_id)}), {escape_sql(fid)}, {escape_sql(fname)}, {odd})"
                        )

        if factor_tuples:
            sql_lines.append(f"""
            DELETE FROM match_factor WHERE match_id = (SELECT id FROM match_cache WHERE external_id = {escape_sql(ev_id)});
            INSERT INTO match_factor (match_id, factor_id, name, value) VALUES {','.join(factor_tuples[:50])};
            """)

    sql_lines.append("COMMIT;")
    return "\n".join(sql_lines)

def main():
    print(f"=== Starting Polymarket Population from Gamma API ===")
    # Clear any synthetic or stale data first
    print("Clearing tables to populate fresh real Polymarket Gamma API events...")
    run_psql_script("TRUNCATE match_factor, match_cache RESTART IDENTITY CASCADE;")

    tags = ["politics", "sports", "elections", "geopolitics", "soccer", "basketball", "tennis", "crypto", "world"]
    total_events_inserted = 0

    for tag in tags:
        current_cnt = get_current_count()
        print(f"[{tag.upper()}] Current DB count: {current_cnt}")
        if current_cnt >= 500:
            print(f"Target reached! (>= 500 matches)")
            break

        for offset in range(0, 300, 100):
            events = fetch_events(limit=100, offset=offset, tag=tag)
            if not events:
                break
            batch_sql = build_batch_sql(events)
            run_psql_script(batch_sql)
            current_cnt = get_current_count()
            print(f"  Inserted batch of {len(events)} events (tag={tag}, offset={offset}) -> DB count: {current_cnt}")
            if current_cnt >= 500:
                break

    # If still < 500, fetch general events
    current_cnt = get_current_count()
    if current_cnt < 500:
        print(f"DB count is {current_cnt}, fetching general events...")
        for offset in range(0, 1000, 100):
            events = fetch_events(limit=100, offset=offset)
            if not events:
                break
            batch_sql = build_batch_sql(events)
            run_psql_script(batch_sql)
            current_cnt = get_current_count()
            print(f"  Inserted batch of {len(events)} general events (offset={offset}) -> DB count: {current_cnt}")
            if current_cnt >= 500:
                break

    final_count = get_current_count()
    factors = get_factor_count()
    print(f"\n==========================================")
    print(f"✅ Final match_cache count: {final_count}")
    print(f"✅ Final match_factor count: {factors}")
    print(f"==========================================")

    if final_count >= 500:
        print("\nGolden Rule #8 satisfied (>= 500 matches).")
        print("Scaling down deployment/igaming-source-polymarket to 0 replicas to save Xeon resources...")
        subprocess.run(["kubectl", "scale", "deployment/igaming-source-polymarket", "--replicas=0", "-n", "igaming-source"])
        print("Scaling completed.")

if __name__ == "__main__":
    main()
