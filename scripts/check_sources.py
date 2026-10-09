import subprocess
import json

sources = [
    "1xbit", "melbet-com", "marathonbet-com", "ligastavok", "pinnacle",
    "sbobet", "leon", "fon-bet-by", "fon-bet-kz", "olimpbet-kz",
    "betwinner", "bwin", "bet365", "draftkings", "mostbet", "10bet",
    "betano", "betway", "betboom", "zenit", "unibet"
]

print(f"{'SOURCE':<18} | {'DB POD':<10} | {'MATCH_CACHE':<12} | {'CRAWLER STATUS':<15} | {'LOADER STATUS':<15}")
print("-" * 80)

for s in sources:
    db_pod = f"igaming-source-{s}-db-0"
    dbname = f"igaming_{s.replace('-', '_')}"
    
    # Check match_cache
    cmd = ["kubectl", "exec", "-n", "igaming-source", db_pod, "--", "psql", "-U", "postgres", "-d", dbname, "-t", "-c", "SELECT count(*) FROM match_cache;"]
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode == 0:
        cnt = res.stdout.strip()
    else:
        # Try finding DB name dynamically
        cmd_db = ["kubectl", "exec", "-n", "igaming-source", db_pod, "--", "psql", "-U", "postgres", "-t", "-c", "SELECT datname FROM pg_database WHERE datname NOT IN ('postgres', 'template0', 'template1');"]
        res_db = subprocess.run(cmd_db, capture_output=True, text=True)
        if res_db.returncode == 0 and res_db.stdout.strip():
            actual_dbname = res_db.stdout.strip().split()[0]
            cmd = ["kubectl", "exec", "-n", "igaming-source", db_pod, "--", "psql", "-U", "postgres", "-d", actual_dbname, "-t", "-c", "SELECT count(*) FROM match_cache;"]
            res2 = subprocess.run(cmd, capture_output=True, text=True)
            if res2.returncode == 0:
                cnt = f"{res2.stdout.strip()} ({actual_dbname})"
            else:
                cnt = f"ERR: {res2.stderr.strip()[:30]}"
        else:
            cnt = "NO DB"
        
    # Check pod status
    cmd_pods = ["kubectl", "get", "pods", "-n", "igaming-source", "-l", f"app={s}", "-o", "json"]
    # If app label doesn't match, let's search by name
    res_pods = subprocess.run(["kubectl", "get", "pods", "-n", "igaming-source", "--no-headers"], capture_output=True, text=True)
    crawler_status = "N/A"
    loader_status = "N/A"
    for line in res_pods.stdout.splitlines():
        parts = line.split()
        if len(parts) >= 3:
            pname = parts[0]
            pstat = parts[2]
            if f"-{s}-crawler" in pname:
                crawler_status = pstat
            elif f"-{s}-loader" in pname:
                loader_status = pstat
            elif f"-{s}-" in pname and "db" not in pname:
                crawler_status = pstat
                
    print(f"{s:<18} | {'OK' if 'DB ERR' not in cnt else 'FAIL':<10} | {cnt:<12} | {crawler_status:<15} | {loader_status:<15}")
