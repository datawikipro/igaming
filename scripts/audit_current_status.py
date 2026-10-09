#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Audits current status of:
1. Plane tasks by state (overall and specifically the bookmaker tasks)
2. Kubernetes pods in igaming-source and igaming-dev
3. Aggregator odds_actual counts per bookmaker
"""
import subprocess
import json
import sys

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"
PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
DB_USER = "plane"
DB_NAME = "plane"
DB_PASSWORD = "plane_db_secure_password_2026"

def run_ssh(cmd_str, timeout=25):
    cmd = [
        "ssh", "-i", SSH_KEY, "-o", "BatchMode=yes", "-o", "ConnectTimeout=10",
        "-o", "StrictHostKeyChecking=no", SSH_HOST, cmd_str
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout, encoding="utf-8")
    return res

def query_plane_sql(sql):
    cmd = [
        "ssh", "-i", SSH_KEY, "-o", "BatchMode=yes", "-o", "ConnectTimeout=10",
        "-o", "StrictHostKeyChecking=no", SSH_HOST,
        f"PGPASSWORD={DB_PASSWORD} kubectl exec -i -n plane deployment/plane-db-primary -- psql -U {DB_USER} -d {DB_NAME} -t -A -F '|||'"
    ]
    res = subprocess.run(cmd, input=sql, capture_output=True, text=True, timeout=25, encoding="utf-8")
    if res.returncode != 0:
        print("SQL ERROR:", res.stderr)
        return []
    lines = [l.strip() for l in res.stdout.splitlines() if l.strip()]
    return [l.split("|||") for l in lines]

def main():
    print("=== 1. PLANE TASK STATUS ===")
    sql_states = f"""
    SELECT s.name, s.group, count(i.id)
    FROM issues i
    JOIN states s ON i.state_id = s.id
    WHERE i.project_id = '{PROJECT_ID}'
    GROUP BY s.name, s.group, s.sequence
    ORDER BY s.sequence;
    """
    rows = query_plane_sql(sql_states)
    for r in rows:
        print(f"  State: {r[0]:<20} (group: {r[1]:<12}) -> {r[2]} tasks")

    # Check tasks #483..#546 (the 64 tasks we created)
    sql_new_tasks = f"""
    SELECT s.name, count(i.id)
    FROM issues i
    JOIN states s ON i.state_id = s.id
    WHERE i.project_id = '{PROJECT_ID}' AND i.sequence_id >= 483 AND i.sequence_id <= 546
    GROUP BY s.name;
    """
    new_rows = query_plane_sql(sql_new_tasks)
    print("\n  Status of newly created bookmaker tasks (#483-#546):")
    for r in new_rows:
        print(f"    {r[0]}: {r[1]}")

    # Check any active tasks in progress or AI dev
    sql_active = f"""
    SELECT i.sequence_id, i.name, s.name
    FROM issues i
    JOIN states s ON i.state_id = s.id
    WHERE i.project_id = '{PROJECT_ID}' AND s.group IN ('started', 'unstarted') AND s.name != 'Backlog'
    ORDER BY i.sequence_id
    LIMIT 20;
    """
    act_rows = query_plane_sql(sql_active)
    print(f"\n  Active / Todo tasks ({len(act_rows)} shown):")
    for r in act_rows:
        print(f"    #{r[0]}: [{r[2]}] {r[1]}")

    print("\n=== 2. KUBERNETES PODS IN IGAMING-SOURCE ===")
    r_pods = run_ssh("kubectl get pods -n igaming-source --no-headers | awk '{print $1, $2, $3, $4, $5}'")
    if r_pods.returncode == 0:
        lines = r_pods.stdout.strip().splitlines()
        running = [l for l in lines if "Running" in l]
        crashes = [l for l in lines if "CrashLoop" in l or "Error" in l or "ImagePull" in l]
        print(f"  Total pods in igaming-source: {len(lines)}")
        print(f"  Running: {len(running)}, Issues/Crashes: {len(crashes)}")
        if crashes:
            print("  Crash pods:")
            for c in crashes[:10]:
                print(f"    {c}")
        print("  Sample running pods:")
        for rp in running[:10]:
            print(f"    {rp}")
    else:
        print("  Error fetching pods:", r_pods.stderr)

    print("\n=== 3. AGGREGATOR ODDS COUNTS BY BOOKMAKER ===")
    sql_agg = "SELECT bookmaker, count(*) FROM odds_actual GROUP BY bookmaker ORDER BY count(*) DESC;"
    cmd_agg = [
        "ssh", "-i", SSH_KEY, "-o", "BatchMode=yes", "-o", "ConnectTimeout=10",
        "-o", "StrictHostKeyChecking=no", SSH_HOST,
        "kubectl exec -i -n igaming-dev igaming-aggregator-db-0 -- psql -U postgres -d igaming_aggregator -t -A -F '|||'"
    ]
    res_agg = subprocess.run(cmd_agg, input=sql_agg, capture_output=True, text=True, timeout=20, encoding="utf-8")
    if res_agg.returncode == 0:
        agg_lines = [l.strip().split("|||") for l in res_agg.stdout.strip().splitlines() if l.strip()]
        total_odds = sum(int(r[1]) for r in agg_lines if len(r) == 2 and r[1].isdigit())
        print(f"  Total active odds in aggregator: {total_odds} across {len(agg_lines)} bookmakers")
        for r in agg_lines:
            if len(r) == 2:
                print(f"    {r[0]:<20}: {r[1]} odds")
    else:
        print("  Error fetching aggregator odds:", res_agg.stderr)

    print("\n=== 4. PLANE AI WORKERS STATUS ===")
    r_workers = run_ssh("kubectl get pods -n plane -l app=plane-ai-worker --no-headers | awk '{print $1, $2, $3, $4, $5}'")
    if r_workers.returncode == 0:
        w_lines = r_workers.stdout.strip().splitlines()
        w_running = [l for l in w_lines if "Running" in l]
        print(f"  Plane AI Workers: {len(w_running)}/{len(w_lines)} Running")

if __name__ == "__main__":
    main()
