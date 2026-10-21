import subprocess
import json

pods_res = subprocess.run(["kubectl", "get", "pods", "-n", "igaming-source", "-o", "json"], capture_output=True, text=True)
data = json.loads(pods_res.stdout)

running_crawlers = {}
running_dbs = set()

for item in data.get("items", []):
    name = item["metadata"]["name"]
    phase = item["status"]["phase"]
    if phase != "Running":
        continue
    if "-db-" in name:
        base = name.split("-db-")[0].replace("igaming-source-", "")
        running_dbs.add(base)
    else:
        base = name.replace("igaming-source-", "")
        # Remove pod hash suffixes
        # e.g. 10bet-json-694f7fc468-qg7b9 -> 10bet
        # 1xbet-crawler-7d7f8d5c-v5wkw -> 1xbet
        # adjarabet-6c95cb8fcb-t5jf7 -> adjarabet
        parts = base.split("-")
        bookmaker = parts[0]
        running_crawlers[bookmaker] = name

print(f"Unique running crawler apps: {len(running_crawlers)}")
print(f"Unique running DB pods: {len(running_dbs)}")

candidates_zero = []
active = []
errors = []

for c in sorted(running_crawlers.keys()):
    if c in running_dbs:
        dbname = "igaming_" + c.replace("-", "_")
        dbpod = f"igaming-source-{c}-db-0"
        try:
            r = subprocess.run(
                ["kubectl", "exec", "-n", "igaming-source", dbpod, "-c", "postgres", "--", "psql", "-U", "postgres", "-d", dbname, "-t", "-A", "-c", "SELECT count(*) FROM match_cache;"],
                capture_output=True, text=True, timeout=3
            )
            out = r.stdout.strip()
            if r.returncode == 0 and out.isdigit():
                cnt = int(out)
                if cnt == 0:
                    candidates_zero.append((c, running_crawlers[c]))
                else:
                    active.append((c, cnt))
            else:
                errors.append((c, f"exit={r.returncode}, out={out}"))
        except subprocess.TimeoutExpired:
            errors.append((c, "timeout"))

print(f"\n=== ACTIVE CRAWLERS ({len(active)}) ===")
for c, cnt in sorted(active, key=lambda x: x[1], reverse=True):
    print(f"  {c:20}: {cnt}")

print(f"\n=== CANDIDATES TO CONFIGURE: RUNNING POD BUT 0 MATCHES ({len(candidates_zero)}) ===")
for c, pod in candidates_zero:
    print(f"  {c:20} (pod: {pod})")

print(f"\n=== DB ERRORS ({len(errors)}) ===")
for c, err in errors:
    print(f"  {c:20}: {err}")
