import subprocess
import json
import re
import sys
from concurrent.futures import ThreadPoolExecutor

sys.stdout.reconfigure(encoding='utf-8')

# 1. Fetch current odds from aggregator
print("Fetching odds_actual from aggregator...", flush=True)
cmd_odds = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-t", "-A", "-F", ",", "-c",
    "SELECT bookmaker, count(*), count(distinct match_id) FROM odds_actual GROUP BY bookmaker ORDER BY count(*) DESC;"
]
res_odds = subprocess.run(cmd_odds, capture_output=True, text=True)
active_odds = {}
if res_odds.returncode == 0:
    for line in res_odds.stdout.strip().splitlines():
        if not line or "," not in line:
            continue
        parts = line.split(",")
        if len(parts) >= 3:
            active_odds[parts[0].strip()] = {
                "odds_count": int(parts[1].strip()),
                "matches_count": int(parts[2].strip())
            }

print(f"Found {len(active_odds)} active bookmakers in odds_actual.", flush=True)

# 2. Get all pods and deployments in igaming-source
print("Fetching deployments and pods in igaming-source...", flush=True)
res_deps = subprocess.run(["kubectl", "get", "deployments", "-n", "igaming-source", "-o", "json"], capture_output=True, text=True)
deps_data = json.loads(res_deps.stdout).get("items", [])

res_pods = subprocess.run(["kubectl", "get", "pods", "-n", "igaming-source", "-o", "json"], capture_output=True, text=True)
pods_data = json.loads(res_pods.stdout).get("items", [])

# Extract unique bookmaker names from deployments
# names are typically igaming-source-<name>-crawler or igaming-source-<name>-loader or igaming-source-<name>
bk_names = set()
for d in deps_data:
    name = d["metadata"]["name"]
    if name.startswith("igaming-source-"):
        raw = name[len("igaming-source-"):]
        if raw.endswith("-crawler"):
            raw = raw[:-len("-crawler")]
        elif raw.endswith("-loader"):
            raw = raw[:-len("-loader")]
        bk_names.add(raw)

bk_list = sorted(list(bk_names))
print(f"Discovered {len(bk_list)} distinct bookmakers.", flush=True)

# Map pods by app label and name
pods_by_name = {}
for p in pods_data:
    pname = p["metadata"]["name"]
    status = p["status"].get("phase", "Unknown")
    containers = p["status"].get("containerStatuses", [])
    ready = sum(1 for c in containers if c.get("ready", False))
    total = len(containers)
    
    # check for crashloop or errors
    waiting_reasons = []
    for c in containers:
        state = c.get("state", {})
        if "waiting" in state:
            waiting_reasons.append(state["waiting"].get("reason", "Waiting"))
        elif "terminated" in state:
            waiting_reasons.append(state["terminated"].get("reason", "Terminated"))
            
    pods_by_name[pname] = {
        "status": status,
        "ready": f"{ready}/{total}",
        "reasons": waiting_reasons,
        "restarts": sum(c.get("restartCount", 0) for c in containers)
    }

def audit_single_bookmaker(bk):
    info = {
        "bookmaker": bk,
        "odds_actual": active_odds.get(bk, {"odds_count": 0, "matches_count": 0}),
        "match_cache": 0,
        "db_status": "N/A",
        "crawler_status": "N/A",
        "crawler_pod": None,
        "loader_status": "N/A",
        "loader_pod": None,
        "sample_error": None,
        "category": "UNKNOWN"
    }
    
    # Find pods
    for pname, pinfo in pods_by_name.items():
        if f"-{bk}-crawler" in pname:
            info["crawler_pod"] = pname
            info["crawler_status"] = f"{pinfo['status']} ({pinfo['ready']})"
            if pinfo["reasons"]:
                info["crawler_status"] += f" [{','.join(pinfo['reasons'])}]"
        elif f"-{bk}-loader" in pname:
            info["loader_pod"] = pname
            info["loader_status"] = f"{pinfo['status']} ({pinfo['ready']})"
            if pinfo["reasons"]:
                info["loader_status"] += f" [{','.join(pinfo['reasons'])}]"
        elif f"-{bk}-" in pname and "-db" not in pname:
            if not info["crawler_pod"]:
                info["crawler_pod"] = pname
                info["crawler_status"] = f"{pinfo['status']} ({pinfo['ready']})"
                if pinfo["reasons"]:
                    info["crawler_status"] += f" [{','.join(pinfo['reasons'])}]"
        elif f"-{bk}-db" in pname:
            info["db_status"] = f"{pinfo['status']} ({pinfo['ready']})"
            
    # Check match_cache in DB
    db_pod = f"igaming-source-{bk}-db-0"
    dbname = f"igaming_{bk.replace('-', '_')}"
    cmd_mc = ["kubectl", "exec", "-n", "igaming-source", db_pod, "--", "psql", "-U", "postgres", "-d", dbname, "-t", "-c", "SELECT count(*) FROM match_cache;"]
    res_mc = subprocess.run(cmd_mc, capture_output=True, text=True, errors='replace', timeout=10)
    if res_mc.returncode == 0:
        try:
            info["match_cache"] = int(res_mc.stdout.strip())
        except Exception:
            info["match_cache"] = 0
    else:
        # check dynamic DB
        cmd_db = ["kubectl", "exec", "-n", "igaming-source", db_pod, "--", "psql", "-U", "postgres", "-t", "-c", "SELECT datname FROM pg_database WHERE datname NOT IN ('postgres', 'template0', 'template1');"]
        res_db = subprocess.run(cmd_db, capture_output=True, text=True, errors='replace', timeout=10)
        if res_db.returncode == 0 and res_db.stdout.strip():
            dyn_db = res_db.stdout.strip().split()[0]
            cmd_mc2 = ["kubectl", "exec", "-n", "igaming-source", db_pod, "--", "psql", "-U", "postgres", "-d", dyn_db, "-t", "-c", "SELECT count(*) FROM match_cache;"]
            res_mc2 = subprocess.run(cmd_mc2, capture_output=True, text=True, errors='replace', timeout=10)
            if res_mc2.returncode == 0:
                try:
                    info["match_cache"] = int(res_mc2.stdout.strip())
                except Exception:
                    pass
                    
    # Check recent log error if not active in odds_actual
    target_pod = info["crawler_pod"] or info["loader_pod"]
    if target_pod and info["odds_actual"]["odds_count"] == 0:
        cmd_log = ["kubectl", "logs", "-n", "igaming-source", target_pod, "--tail=60"]
        res_log = subprocess.run(cmd_log, capture_output=True, text=True, errors='replace', timeout=10)
        log_text = (res_log.stdout or "") + "\n" + (res_log.stderr or "")
        for l in log_text.splitlines():
            low = l.lower()
            if any(err in low for err in ["403", "401", "404", "500", "qrator", "cloudflare", "timeout", "refused", "exception", "blocked", "error"]):
                info["sample_error"] = l.strip()[:180]
                break
                
    # Determine Category
    if info["odds_actual"]["odds_count"] > 0:
        info["category"] = "GREEN_INGESTING"
    elif info["match_cache"] >= 500:
        info["category"] = "AMBER_CACHE_FILLED_NO_AGGREGATOR"
    elif "CrashLoop" in str(info["crawler_status"]) or "Error" in str(info["crawler_status"]) or "CrashLoop" in str(info["loader_status"]):
        info["category"] = "RED_CRASHLOOP"
    elif info["sample_error"] and any(w in info["sample_error"].lower() for w in ["403", "qrator", "cloudflare", "blocked", "access denied"]):
        info["category"] = "RED_WAF_GEO_BLOCKED"
    elif info["sample_error"] and any(w in info["sample_error"].lower() for w in ["404", "invalid response code", "cannot deserialize", "not found"]):
        info["category"] = "RED_API_SCHEMA_CHANGED"
    elif info["crawler_status"] == "N/A" and info["loader_status"] == "N/A":
        info["category"] = "RED_NO_PODS"
    else:
        info["category"] = "RED_INVESTIGATION_NEEDED"
        
    return info

print("Running multithreaded bookmaker inspection (8 threads)...", flush=True)
with ThreadPoolExecutor(max_workers=8) as ex:
    results = list(ex.map(audit_single_bookmaker, bk_list))

with open("scratch/bookmakers_audit.json", "w", encoding="utf-8") as f:
    json.dump(results, f, ensure_ascii=False, indent=2)

print("\nAudit completed! Summary by category:", flush=True)
by_cat = {}
for r in results:
    cat = r["category"]
    by_cat.setdefault(cat, []).append(r)

for cat, items in sorted(by_cat.items()):
    print(f"\n=== {cat} ({len(items)}) ===")
    for it in sorted(items, key=lambda x: x["bookmaker"]):
        mc = it["match_cache"]
        odds = it["odds_actual"]["odds_count"]
        cstat = it["crawler_status"]
        lstat = it["loader_status"]
        err = f" | Err: {it['sample_error'][:60]}..." if it["sample_error"] else ""
        print(f"  {it['bookmaker']:<18} | Cache: {mc:<5} | Odds: {odds:<6} | Crawl: {cstat:<15} | Load: {lstat:<15}{err}")
