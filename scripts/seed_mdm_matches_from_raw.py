import json
import logging
import time
from datetime import datetime, timezone
import psycopg2
from psycopg2.extras import RealDictCursor, execute_batch

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("seed-mdm-matches")

DB_HOST = "10.244.0.99"
DB_PORT = 5432
DB_USER = "postgres"
DB_PASS = "postgres"

def run_seeding():
    logger.info("Connecting to databases on %s:%s...", DB_HOST, DB_PORT)
    conn_agg = psycopg2.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS, dbname="igaming_aggregator"
    )
    conn_mdm = psycopg2.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS, dbname="igaming_mdm"
    )
    conn_mdm.autocommit = True
    cur_agg = conn_agg.cursor(cursor_factory=RealDictCursor)
    cur_mdm = conn_mdm.cursor(cursor_factory=RealDictCursor)

    # 1. Load Sports
    logger.info("Loading sports...")
    cur_mdm.execute("SELECT id, slug FROM mdm_sport")
    sports = cur_mdm.fetchall()
    sport_map = {s["slug"]: s["id"] for s in sports}
    # Common aliases
    sport_map["soccer"] = sport_map.get("football", 1)
    sport_map["hockey"] = sport_map.get("ice-hockey", 2)
    sport_map["cs2"] = sport_map.get("esports", 6)
    sport_map["dota2"] = sport_map.get("esports", 6)
    logger.info("Loaded %d sports.", len(sport_map))

    # 2. Load Teams Cache
    logger.info("Loading existing MDM teams into memory...")
    cur_mdm.execute("SELECT id, sport_id, sofascore_id, normalized_signature, lower(canonical_name) as lname, slug FROM mdm_team")
    teams = cur_mdm.fetchall()
    team_by_sofa = {} # (sport_id, sofa_id) -> id
    team_by_sig = {}  # (sport_id, sig) -> id
    team_by_name = {} # (sport_id, lname) -> id

    for t in teams:
        sp_id = t["sport_id"]
        t_id = t["id"]
        if t["sofascore_id"]:
            team_by_sofa[(sp_id, t["sofascore_id"])] = t_id
        if t["normalized_signature"]:
            team_by_sig[(sp_id, t["normalized_signature"])] = t_id
        if t["lname"]:
            team_by_name[(sp_id, t["lname"])] = t_id
    logger.info("Loaded %d teams into memory cache.", len(teams))

    # 3. Load Tournaments Cache
    logger.info("Loading existing MDM tournaments into memory...")
    cur_mdm.execute("SELECT id, sport_id, sofascore_id, lower(canonical_name) as lname, slug FROM mdm_tournament")
    tourns = cur_mdm.fetchall()
    tourn_by_sofa = {} # (sport_id, sofa_id) -> id
    tourn_by_name = {} # (sport_id, lname) -> id
    tourn_by_slug = {} # (sport_id, slug) -> id

    for tr in tourns:
        sp_id = tr["sport_id"]
        tr_id = tr["id"]
        if tr["sofascore_id"]:
            tourn_by_sofa[(sp_id, tr["sofascore_id"])] = tr_id
        if tr["lname"]:
            tourn_by_name[(sp_id, tr["lname"])] = tr_id
        if tr["slug"]:
            tourn_by_slug[(sp_id, tr["slug"])] = tr_id
    logger.info("Loaded %d tournaments into memory cache.", len(tourns))

    # Helpers to resolve or create missing entities
    def get_or_create_team(sport_id, sofa_id, name):
        if not name or not name.strip():
            return None
        clean_name = name.strip()
        sig = clean_name.upper()
        lname = clean_name.lower()

        if sofa_id and (sport_id, sofa_id) in team_by_sofa:
            return team_by_sofa[(sport_id, sofa_id)]
        if (sport_id, sig) in team_by_sig:
            return team_by_sig[(sport_id, sig)]
        if (sport_id, lname) in team_by_name:
            return team_by_name[(sport_id, lname)]

        # Auto-create team
        slug = clean_name.lower().replace(" ", "-").replace("/", "-")[:180]
        cur_mdm.execute("""
            INSERT INTO mdm_team (sport_id, slug, sofascore_id, canonical_name, normalized_signature, source_origin)
            VALUES (%s, %s, %s, %s, %s, 'SOFASCORE')
            ON CONFLICT (sport_id, slug) DO UPDATE SET canonical_name = EXCLUDED.canonical_name
            RETURNING id
        """, (sport_id, f"{slug}-{sofa_id or int(time.time()*1000)%1000000}", sofa_id, clean_name, sig))
        row = cur_mdm.fetchone()
        new_id = row["id"]
        if sofa_id:
            team_by_sofa[(sport_id, sofa_id)] = new_id
        team_by_sig[(sport_id, sig)] = new_id
        team_by_name[(sport_id, lname)] = new_id
        return new_id

    def get_or_create_tourn(sport_id, sofa_id, name):
        if not name or not name.strip():
            name = "General League"
        clean_name = name.strip()
        lname = clean_name.lower()
        slug = clean_name.lower().replace(" ", "-").replace("/", "-")[:180]

        if sofa_id and (sport_id, sofa_id) in tourn_by_sofa:
            return tourn_by_sofa[(sport_id, sofa_id)]
        if (sport_id, lname) in tourn_by_name:
            return tourn_by_name[(sport_id, lname)]
        if (sport_id, slug) in tourn_by_slug:
            return tourn_by_slug[(sport_id, slug)]

        # Auto-create tournament
        cur_mdm.execute("""
            INSERT INTO mdm_tournament (sport_id, slug, sofascore_id, canonical_name, source_origin)
            VALUES (%s, %s, %s, %s, 'SOFASCORE')
            ON CONFLICT DO NOTHING
            RETURNING id
        """, (sport_id, f"{slug}-{sofa_id or int(time.time()*1000)%1000000}", sofa_id, clean_name))
        row = cur_mdm.fetchone()
        if not row:
            cur_mdm.execute("SELECT id FROM mdm_tournament WHERE sport_id = %s AND slug = %s", (sport_id, slug))
            row = cur_mdm.fetchone()
        new_id = row["id"] if row else 1
        if sofa_id:
            tourn_by_sofa[(sport_id, sofa_id)] = new_id
        tourn_by_name[(sport_id, lname)] = new_id
        tourn_by_slug[(sport_id, slug)] = new_id
        return new_id

    # 4. Fetch raw events starting from October 14, 2026 onwards (recent & future)
    logger.info("Querying recent & future events from sofascore_raw_events...")
    cur_agg.execute("""
        SELECT event_id, sport, tournament, home_team, away_team, match_date, status, home_score, away_score, event_json
        FROM sofascore_raw_events
        WHERE match_date >= 1792000000000 AND home_team IS NOT NULL AND home_team != '' AND away_team IS NOT NULL AND away_team != ''
        ORDER BY match_date ASC
    """)

    batch_size = 1000
    total_processed = 0
    total_inserted = 0

    insert_rows = []
    logger.info("Streaming and parsing raw events...")

    while True:
        rows = cur_agg.fetchmany(batch_size)
        if not rows:
            break

        for r in rows:
            total_processed += 1
            sp_slug = r["sport"].strip().lower()
            sp_id = sport_map.get(sp_slug, 1)

            ev_id = r["event_id"]
            m_date = r["match_date"]
            if not m_date or m_date <= 0:
                continue

            start_dt = datetime.fromtimestamp(m_date / 1000.0, tz=timezone.utc)

            # Extract from JSON if present
            sofa_home_id = None
            sofa_away_id = None
            sofa_tourn_id = None
            home_name = r["home_team"]
            away_name = r["away_team"]
            tourn_name = r["tournament"]

            if r["event_json"]:
                try:
                    obj = json.loads(r["event_json"]) if isinstance(r["event_json"], str) else r["event_json"]
                    ev = obj.get("event", obj)
                    sofa_home_id = ev.get("homeTeam", {}).get("id")
                    sofa_away_id = ev.get("awayTeam", {}).get("id")
                    sofa_tourn_id = ev.get("tournament", {}).get("id")
                    home_name = ev.get("homeTeam", {}).get("name") or home_name
                    away_name = ev.get("awayTeam", {}).get("name") or away_name
                    tourn_name = ev.get("tournament", {}).get("name") or tourn_name
                except Exception:
                    pass

            home_team_id = get_or_create_team(sp_id, sofa_home_id, home_name)
            away_team_id = get_or_create_team(sp_id, sofa_away_id, away_name)
            if not home_team_id or not away_team_id or home_team_id == away_team_id:
                continue

            tourn_id = get_or_create_tourn(sp_id, sofa_tourn_id, tourn_name)
            if not tourn_id:
                continue

            st = r["status"] or "SCHEDULED"
            if st.lower() in ("notstarted", "created"):
                st = "SCHEDULED"
            elif st.lower() in ("inprogress", "firsthalf", "secondhalf"):
                st = "LIVE"
            elif st.lower() in ("finished", "ended"):
                st = "FINISHED"
            elif st.lower() in ("canceled", "cancelled"):
                st = "CANCELLED"
            elif st.lower() in ("postponed",):
                st = "POSTPONED"
            else:
                st = "SCHEDULED"

            s1 = str(r["home_score"]) if r["home_score"] is not None else None
            s2 = str(r["away_score"]) if r["away_score"] is not None else None

            insert_rows.append((
                sp_id, tourn_id, home_team_id, away_team_id,
                start_dt, ev_id, "SOFASCORE", "sofascore", st, s1, s2
            ))

        if len(insert_rows) >= 500:
            execute_batch(cur_mdm, """
                INSERT INTO mdm_match (
                    sport_id, tournament_id, home_team_id, away_team_id,
                    start_time, sofascore_id, source_origin, created_by_source, status, score1, score2
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (sofascore_id) DO UPDATE SET
                    start_time = EXCLUDED.start_time,
                    status = EXCLUDED.status,
                    score1 = EXCLUDED.score1,
                    score2 = EXCLUDED.score2,
                    updated_at = NOW()
            """, insert_rows)
            total_inserted += len(insert_rows)
            insert_rows = []
            logger.info("Progress: processed=%d, inserted=%d matches...", total_processed, total_inserted)

    if insert_rows:
        execute_batch(cur_mdm, """
            INSERT INTO mdm_match (
                sport_id, tournament_id, home_team_id, away_team_id,
                start_time, sofascore_id, source_origin, created_by_source, status, score1, score2
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (sofascore_id) DO UPDATE SET
                start_time = EXCLUDED.start_time,
                status = EXCLUDED.status,
                score1 = EXCLUDED.score1,
                score2 = EXCLUDED.score2,
                updated_at = NOW()
        """, insert_rows)
        total_inserted += len(insert_rows)

    logger.info("SUCCESS: Finished seeding MDM matches from raw events! Total inserted/updated: %d", total_inserted)

    # Check total in mdm_match
    cur_mdm.execute("SELECT count(*) as total, count(*) FILTER (WHERE status = 'SCHEDULED') as scheduled FROM mdm_match")
    stats = cur_mdm.fetchone()
    logger.info("MDM Match Table State: Total = %d, Scheduled = %d", stats["total"], stats["scheduled"])

    conn_agg.close()
    conn_mdm.close()

if __name__ == "__main__":
    run_seeding()
