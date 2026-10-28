import os
import glob
import json
import pyarrow as pa
import pyarrow.parquet as pq
from collections import defaultdict

BASE_DIR = os.environ.get("PARQUET_BASE", "/data/cold-storage/sofascore/parquet")
SRC_DIR = os.path.join(BASE_DIR, "sport=esports")

def resolve_esports_discipline(row_dict):
    cat = (row_dict.get("category_name") or row_dict.get("category_slug") or "").lower()
    if "counter" in cat or "cs:go" in cat or "csgo" in cat or "cs2" in cat:
        return "cs2"
    if "dota" in cat:
        return "dota2"
    if "league of legends" in cat or "lol" in cat:
        return "league-of-legends"
    if "valorant" in cat:
        return "valorant"
    if "rainbow" in cat or "r6" in cat:
        return "rainbow-six"
    if "starcraft" in cat or "sc2" in cat:
        return "starcraft"

    raw_str = row_dict.get("raw_event_json") or ""
    if raw_str and raw_str.startswith("{"):
        try:
            raw = json.loads(raw_str)
            ev = raw.get("event", raw)
            c = ev.get("tournament", {}).get("category", {})
            slug = (c.get("slug") or "").lower()
            name = (c.get("name") or "").lower()
            if slug in ("csgo", "cs2") or "counter" in name or "cs" in slug:
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

    tourn = (row_dict.get("tournament_name") or row_dict.get("tournament_slug") or "").lower()
    if any(k in tourn for k in ("cs:go", "csgo", "cs2", "cct", "iem ", "blast ", "esl pro league", "pgl ")):
        return "cs2"
    if any(k in tourn for k in ("dota", "international", "dreamleague")):
        return "dota2"
    if any(k in tourn for k in ("lck", "lpl", "lec", "lcs", "cblol", "league of legends", "worlds")):
        return "league-of-legends"
    if any(k in tourn for k in ("vct", "valorant")):
        return "valorant"

    return "esports"

def main():
    print("[Splitter] Scanning esports parquet files...", flush=True)
    all_files = sorted(glob.glob(os.path.join(SRC_DIR, "**/*.parquet"), recursive=True))
    print(f"[Splitter] Found {len(all_files)} parquet files to process.", flush=True)

    stats = defaultdict(int)
    file_count = 0

    for file_path in all_files:
        rel_path = os.path.relpath(file_path, SRC_DIR)
        parts = rel_path.split(os.sep)
        if len(parts) < 3:
            continue
        year_part = parts[0]
        month_part = parts[1]
        file_name = parts[2]

        try:
            table = pq.read_table(file_path)
            schema = table.schema
            pylist = table.to_pylist()

            buckets = defaultdict(list)
            for row in pylist:
                target_sport = resolve_esports_discipline(row)
                if "sport" in row:
                    row["sport"] = target_sport
                buckets[target_sport].append(row)
                stats[target_sport] += 1

            for target_sport, rows in buckets.items():
                if target_sport == "esports":
                    out_dir = os.path.join(BASE_DIR, "sport=esports_filtered", year_part, month_part)
                else:
                    out_dir = os.path.join(BASE_DIR, f"sport={target_sport}", year_part, month_part)

                os.makedirs(out_dir, exist_ok=True)
                out_file = os.path.join(out_dir, file_name)

                sub_table = pa.Table.from_pylist(rows, schema=schema)
                pq.write_table(sub_table, out_file, compression="zstd", compression_level=9)

            file_count += 1
            if file_count % 50 == 0 or file_count == len(all_files):
                print(f"  [Progress] Processed {file_count}/{len(all_files)} files...", flush=True)

        except Exception as e:
            print(f"Error processing {file_path}: {e}", flush=True)

    print("\n[Splitter] Slicing finished! Resulting counts:")
    for sp, cnt in sorted(stats.items(), key=lambda x: -x[1]):
        print(f"  sport={sp}: {cnt:,} matches")

if __name__ == "__main__":
    main()
