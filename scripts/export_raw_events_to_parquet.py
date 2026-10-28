import os
import json
import psycopg2
import pyarrow as pa
import pyarrow.parquet as pq
import datetime

DB_URL = os.environ.get("DB_URL", "postgresql://postgres:postgres@10.108.82.251:5432/igaming_aggregator")
PARQUET_BASE = os.environ.get("PARQUET_BASE", "/data/cold-storage/sofascore/parquet")

EVENT_SCHEMA = pa.schema([
    ("event_id", pa.int64()),
    ("mdm_match_id", pa.int64()),
    ("mdm_home_team_id", pa.int64()),
    ("mdm_away_team_id", pa.int64()),
    ("mdm_status", pa.string()),
    ("sport", pa.string()),
    ("match_date", pa.string()),
    ("start_timestamp", pa.int64()),
    ("tournament_name", pa.string()),
    ("season_name", pa.string()),
    ("status", pa.string()),
    ("home_team_name", pa.string()),
    ("away_team_name", pa.string()),
    ("home_score", pa.int32()),
    ("away_score", pa.int32()),
    ("raw_event_json", pa.string()),
    ("lineups_json", pa.string()),
    ("h2h_json", pa.string()),
    ("streaks_json", pa.string())
])

def resolve_canonical_sport(sport, tourn, ev_json):
    sp = (sport or "unknown").lower()
    if sp != "esports":
        return sp
    if ev_json and ev_json.startswith("{"):
        try:
            raw = json.loads(ev_json)
            ev = raw.get("event", raw)
            c = ev.get("tournament", {}).get("category", {})
            slug = (c.get("slug") or "").lower()
            name = (c.get("name") or "").lower()
            if slug in ("csgo", "cs2") or "counter" in name:
                return "cs2"
            if slug in ("dota2", "dota-2") or "dota" in name:
                return "dota2"
            if slug in ("lol", "league-of-legends") or "league of legends" in name:
                return "league-of-legends"
            if slug == "valorant" or "valorant" in name:
                return "valorant"
            if "rainbow" in slug or "rainbow" in name:
                return "rainbow-six"
            if "starcraft" in slug or "starcraft" in name:
                return "starcraft"
        except Exception:
            pass
    t = (tourn or "").lower()
    if any(k in t for k in ("cs:go", "csgo", "cs2", "cct", "iem ", "blast ", "esl pro league", "pgl ")):
        return "cs2"
    if any(k in t for k in ("dota", "international", "dreamleague")):
        return "dota2"
    if any(k in t for k in ("lck", "lpl", "lec", "lcs", "league of legends", "worlds")):
        return "league-of-legends"
    if any(k in t for k in ("vct", "valorant")):
        return "valorant"
    return "esports"

def main():
    print("[Exporter] Connecting to PostgreSQL...", flush=True)
    conn = psycopg2.connect(DB_URL)
    cur = conn.cursor(name="raw_events_cursor")
    cur.itersize = 10000

    cur.execute("""
        SELECT event_id, mdm_match_id, mdm_home_team_id, mdm_away_team_id, mdm_status,
               sport, match_date, home_team, away_team,
               home_score, away_score, status, tournament, season,
               event_json, lineups_json, h2h_json, streaks_json
        FROM sofascore_raw_events
        ORDER BY sport, match_date
    """)

    current_key = None
    batch = []
    total_exported = 0
    total_mapped = 0

    def flush_batch(key, rows):
        nonlocal total_exported, total_mapped
        if not rows:
            return
        sport, year, month = key
        target_dir = os.path.join(PARQUET_BASE, f"sport={sport}", f"year={year}", f"month={month:02d}")
        os.makedirs(target_dir, exist_ok=True)
        file_path = os.path.join(target_dir, "events.parquet")

        table = pa.Table.from_pylist(rows, schema=EVENT_SCHEMA)
        pq.write_table(table, file_path, compression="zstd", compression_level=9)
        total_exported += len(rows)
        print(f"  -> Saved {len(rows)} events to {file_path}", flush=True)

    print("[Exporter] Streaming rows from database with raw_event_json and mdm_id...", flush=True)
    while True:
        records = cur.fetchmany(10000)
        if not records:
            break
        for r in records:
            eid, mdm_mid, mdm_hid, mdm_aid, mdm_st, sport, mdate, home, away, hs, ascore, st, tourn, ssn, ev_json, lineups, h2h, streaks = r
            sp = resolve_canonical_sport(sport, tourn, ev_json)

            if mdate and mdate > 0:
                try:
                    dt = datetime.datetime.fromtimestamp(mdate, tz=datetime.timezone.utc).date()
                except Exception:
                    dt = datetime.date(2026, 10, 1)
            else:
                dt = datetime.date(2026, 10, 1)

            key = (sp, dt.year, dt.month)
            if current_key is None:
                current_key = key
            elif key != current_key:
                flush_batch(current_key, batch)
                batch = []
                current_key = key

            if mdm_mid:
                total_mapped += 1

            batch.append({
                "event_id": int(eid),
                "mdm_match_id": int(mdm_mid) if mdm_mid is not None else None,
                "mdm_home_team_id": int(mdm_hid) if mdm_hid is not None else None,
                "mdm_away_team_id": int(mdm_aid) if mdm_aid is not None else None,
                "mdm_status": str(mdm_st or "UNMAPPED"),
                "sport": str(sp),
                "match_date": dt.isoformat(),
                "start_timestamp": int(mdate or 0),
                "tournament_name": str(tourn or ""),
                "season_name": str(ssn or ""),
                "status": str(st or ""),
                "home_team_name": str(home or ""),
                "away_team_name": str(away or ""),
                "home_score": int(hs if hs is not None else -1),
                "away_score": int(ascore if ascore is not None else -1),
                "raw_event_json": ev_json or "",
                "lineups_json": lineups or "",
                "h2h_json": h2h or "",
                "streaks_json": streaks or ""
            })

    if batch and current_key:
        flush_batch(current_key, batch)

    cur.close()
    conn.close()
    print(f"\n[Exporter] Finished! Total events exported: {total_exported} (with mdm_match_id: {total_mapped})", flush=True)

if __name__ == "__main__":
    main()
