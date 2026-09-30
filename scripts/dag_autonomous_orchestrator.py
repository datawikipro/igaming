#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Autonomous DAG Orchestrator for Plane Tasks (SmartBet.guru / iGaming)
Continuously monitors dependencies, resolves completion via Git & DB,
and dispatches ready tasks to available AI workers until 100% project completion.
"""

import os
import sys
import json
import time
import re
import argparse
import subprocess
import urllib.request
import urllib.parse
from datetime import datetime

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

# Default Environment & URLs
PROJECT_ID = os.environ.get("PROJECT_ID", "2df124d7-25b0-4145-a63c-aafcb0fe0041")
WORKSPACE_SLUG = os.environ.get("WORKSPACE_SLUG", "dataplatform")

# Plane DB Settings
DB_HOST = os.environ.get("PLANE_DB_HOST", "10.105.178.50")
DB_PORT = os.environ.get("PLANE_DB_PORT", "5432")
DB_USER = os.environ.get("PLANE_DB_USER", "plane")
DB_PASSWORD = os.environ.get("PLANE_DB_PASSWORD", "plane_db_secure_password_2026")
DB_NAME = os.environ.get("PLANE_DB_NAME", "plane")

# State UUIDs
STATE_BACKLOG = "ed9f5e45-27fa-4e42-bcb0-5866b2e06a02"
STATE_TODO = "ab00339e-5465-4112-9282-1a458e08b568"
STATE_IN_PROGRESS = "fd3f75bd-2613-4916-a030-3d6c7de6ebb0"
STATE_AI_DEV = "1d4476f7-1d60-4b35-8211-439d318bf5d7"     # AI разработка
STATE_REVIEW = "2e948935-ab9a-4559-8406-bb082d405bec"
STATE_DONE = "b1239dd6-2d97-432a-bf6f-8f5fa90d75ae"
STATE_COMPLETED = "e5f607b9-2f87-46c3-b4d0-243b3ca4a8c3"  # Завершено

# Executor Webhook URL
EXECUTOR_URL = os.environ.get("PLANE_EXECUTOR_URL", "http://127.0.0.1:30885/webhook/plane")

# Worker settings
TOTAL_WORKERS = int(os.environ.get("TOTAL_WORKERS", "17"))  # 0..16
WORKER_HOST_TEMPLATE = os.environ.get("WORKER_HOST_TEMPLATE", "plane-ai-worker-{i}.plane-ai-worker.plane.svc.cluster.local:8000")

# SSH Configuration for remote execution if running from developer machine
SSH_HOST = os.environ.get("SSH_HOST", "root@100.78.183.101")
SSH_KEY = os.environ.get("SSH_KEY", r"C:\Users\chernousov_a\.ssh\id_ed25519")
IS_LOCAL_K8S = os.path.exists("/var/run/secrets/kubernetes.io/serviceaccount") or os.environ.get("KUBERNETES_SERVICE_HOST") is not None


def log(msg):
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{ts}] {msg}", flush=True)


def parse_yaml_simple(path):
    """
    Lightweight YAML parser for dependency_graph.yaml without external dependencies.
    """
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    tasks = []
    current_task = None
    for line in content.splitlines():
        line_strip = line.strip()
        if not line_strip or line_strip.startswith("#"):
            continue

        if line_strip.startswith("- sequence_id:"):
            if current_task:
                tasks.append(current_task)
            seq = int(line_strip.split(":", 1)[1].strip())
            current_task = {"sequence_id": seq, "depends_on": []}
        elif current_task:
            if line_strip.startswith("id:"):
                current_task["id"] = line_strip.split(":", 1)[1].strip().strip("'\"")
            elif line_strip.startswith("module:"):
                current_task["module"] = line_strip.split(":", 1)[1].strip().strip("'\"")
            elif line_strip.startswith("title:"):
                current_task["title"] = line_strip.split(":", 1)[1].strip().strip("'\"")
            elif line_strip.startswith("depends_on:"):
                raw_deps = line_strip.split(":", 1)[1].strip().strip("[]")
                if raw_deps:
                    current_task["depends_on"] = [int(x.strip()) for x in raw_deps.split(",") if x.strip()]
                else:
                    current_task["depends_on"] = []

    if current_task:
        tasks.append(current_task)

    return tasks


try:
    import psycopg2
except ImportError:
    psycopg2 = None


def run_sql(query):
    """
    Executes SQL against plane db via psycopg2 (if available) or psql/SSH.
    """
    if psycopg2 and (IS_LOCAL_K8S or os.environ.get("DIRECT_PG_ACCESS") == "1"):
        try:
            conn = psycopg2.connect(
                host=DB_HOST,
                port=int(DB_PORT),
                user=DB_USER,
                password=DB_PASSWORD,
                dbname=DB_NAME,
                connect_timeout=10
            )
            conn.autocommit = True
            with conn.cursor() as cur:
                cur.execute(query)
                if cur.description:
                    rows = cur.fetchall()
                    return ["|||".join(str(v) if v is not None else "" for v in row) for row in rows]
                return []
        except Exception as pg_err:
            log(f"psycopg2 direct query error ({pg_err}), falling back...")

    if IS_LOCAL_K8S or os.environ.get("DIRECT_PG_ACCESS") == "1":
        # Direct psql in container or local host
        env = dict(os.environ, PGPASSWORD=DB_PASSWORD)
        cmd = [
            "psql", "-h", DB_HOST, "-p", DB_PORT, "-U", DB_USER, "-d", DB_NAME,
            "-t", "-A", "-F", "|||", "-c", query
        ]
        res = subprocess.run(cmd, capture_output=True, text=True, env=env, timeout=20)
        if res.returncode != 0:
            raise RuntimeError(f"Direct psql error: {res.stderr}")
        return res.stdout.strip().splitlines()
    else:
        # Run via SSH to xeon-srv
        cmd = [
            "ssh", "-i", SSH_KEY,
            "-o", "BatchMode=yes",
            "-o", "ConnectTimeout=10",
            SSH_HOST,
            f"kubectl exec -i -n plane deployment/plane-db-primary -- psql -U {DB_USER} -d {DB_NAME} -t -A -F '|||' -c \"{query.replace('\"', '\\\"')}\""
        ]
        res = subprocess.run(cmd, capture_output=True, text=True, timeout=30, encoding="utf-8")
        if res.returncode != 0:
            raise RuntimeError(f"SSH psql error: {res.stderr}")
        return res.stdout.strip().splitlines()



def get_plane_issues():
    """
    Fetches sequence_id, id, name, state_id, state_name, state_group from Plane DB.
    """
    sql = f"""
    SELECT i.sequence_id, i.id, i.name, i.state_id, s.name, s.group
    FROM issues i
    JOIN states s ON i.state_id = s.id
    WHERE i.project_id='{PROJECT_ID}'
    ORDER BY i.sequence_id;
    """
    lines = run_sql(sql)
    issues_map = {}
    for line in lines:
        if not line.strip():
            continue
        parts = line.split("|||")
        if len(parts) >= 6:
            issues_map[int(parts[0])] = {
                "sequence_id": int(parts[0]),
                "id": parts[1],
                "name": parts[2],
                "state_id": parts[3],
                "state_name": parts[4],
                "state_group": parts[5]
            }
    return issues_map


def update_plane_issue_state(issue_id, new_state_id):
    """
    Updates the state_id of an issue in Plane PostgreSQL.
    """
    sql = f"UPDATE issues SET state_id='{new_state_id}', updated_at=NOW() WHERE id='{issue_id}';"
    run_sql(sql)


def check_git_master_completions(issues_map):
    """
    Checks origin/master git commits and marks issues as completed if verified commits exist.
    Supports local git log with fallback to GitHub REST API.
    """
    git_log_lines = []

    # Try local git first
    env = dict(os.environ, GIT_TERMINAL_PROMPT="0")
    try:
        subprocess.run(["git", "fetch", "origin"], capture_output=True, text=True, timeout=20, env=env)
        res = subprocess.run(["git", "log", "origin/master", "-n", "80", "--oneline"], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=10)
        if res.returncode == 0 and res.stdout:
            git_log_lines = res.stdout.splitlines()
    except Exception as e:
        log(f"Local git check notice ({e}), falling back to GitHub REST API...")

    # If local git returned empty or failed, query GitHub REST API
    if not git_log_lines:
        github_token = os.environ.get("GITHUB_TOKEN", "")
        api_url = "https://api.github.com/repos/datawikipro/igaming/commits?sha=master&per_page=60"
        try:
            headers = {"User-Agent": "DAGOrchestrator"}
            if github_token:
                headers["Authorization"] = f"token {github_token}"
            req = urllib.request.Request(api_url, headers=headers)
            with urllib.request.urlopen(req, timeout=10) as r:

                commits = json.loads(r.read().decode())
                for c in commits:
                    msg = c.get("commit", {}).get("message", "").split("\n")[0]
                    git_log_lines.append(f"{c.get('sha', '')[:7]} {msg}")
            log(f"Fetched {len(git_log_lines)} latest commits from GitHub REST API.")
        except Exception as api_err:
            log(f"Warning: GitHub API commit check failed: {api_err}")
            return

    updated_count = 0
    for seq, iss in issues_map.items():
        if iss["state_group"] == "completed":
            continue

        short_id = iss["id"][:8]
        found = False
        for line in git_log_lines:
            if f"#{short_id}" in line or f"plane-{short_id}" in line:
                found = True
                break
            # Match exact sequence id: e.g., #49, plane-49
            m = re.search(r'#(\d+)\b', line)
            if m and int(m.group(1)) == seq:
                found = True
                break

        if found:
            log(f"Git Master Match: Task #{seq} ({short_id}) '{iss['name'][:40]}' verified in master! Marking completed.")
            update_plane_issue_state(iss["id"], STATE_COMPLETED)
            iss["state_id"] = STATE_COMPLETED
            iss["state_name"] = "Завершено"
            iss["state_group"] = "completed"
            updated_count += 1

    if updated_count > 0:
        log(f"Synchronized {updated_count} completed tasks from git master into Plane DB.")



def get_workers_status():
    """
    Inspects all plane-ai-workers (0..TOTAL_WORKERS-1).
    Returns list of dicts: [{'pod': 'plane-ai-worker-0', 'status': 'IDLE'/'BUSY'/..., 'task': ...}]
    """
    workers = []
    if IS_LOCAL_K8S:
        for i in range(TOTAL_WORKERS):
            pod_name = f"plane-ai-worker-{i}"
            url = f"http://{pod_name}.plane-ai-worker.plane.svc.cluster.local:8000/status"
            try:
                req = urllib.request.Request(url, headers={"User-Agent": "DAGOrchestrator"})
                with urllib.request.urlopen(req, timeout=3) as resp:
                    data = json.loads(resp.read().decode())
                    workers.append({
                        "pod": pod_name,
                        "status": data.get("status", "UNKNOWN"),
                        "task": data.get("currentTask"),
                        "account": data.get("accountEmail")
                    })
            except Exception:
                workers.append({"pod": pod_name, "status": "UNREACHABLE", "task": None, "account": None})
    else:
        # Check via SSH and kubectl curl
        check_cmd = """
for i in $(seq 0 %d); do
    pod="plane-ai-worker-$i"
    stat=$(kubectl exec -n plane $pod -- curl -s --max-time 3 http://127.0.0.1:8000/status 2>/dev/null || echo '{"status":"UNREACHABLE"}')
    echo "$pod|||$stat"
done
""" % (TOTAL_WORKERS - 1)
        cmd = [
            "ssh", "-i", SSH_KEY,
            "-o", "BatchMode=yes",
            "-o", "ConnectTimeout=10",
            SSH_HOST,
            check_cmd
        ]
        res = subprocess.run(cmd, capture_output=True, text=True, timeout=40, encoding="utf-8")
        for line in res.stdout.strip().splitlines():
            if "|||" in line:
                pod, raw = line.split("|||", 1)
                try:
                    data = json.loads(raw)
                    workers.append({
                        "pod": pod,
                        "status": data.get("status", "UNKNOWN"),
                        "task": data.get("currentTask"),
                        "account": data.get("accountEmail")
                    })
                except Exception:
                    workers.append({"pod": pod, "status": "UNREACHABLE", "task": None, "account": None})

    return workers


def trigger_executor_webhook(task_id, seq_id, title):
    """
    Sends webhook to plane-ai-executor to dispatch task to an available worker.
    """
    payload = {
        "event": "issue_activity",
        "action": "updated",
        "data": {
            "field": "state",
            "new_value": "AI разработка",
            "issue": task_id,
            "project": PROJECT_ID,
            "workspace": WORKSPACE_SLUG
        }
    }
    data_bytes = json.dumps(payload).encode("utf-8")

    if IS_LOCAL_K8S:
        url = EXECUTOR_URL
        req = urllib.request.Request(url, data=data_bytes, headers={"Content-Type": "application/json"}, method="POST")
        with urllib.request.urlopen(req, timeout=10) as resp:
            log(f"Triggered webhook for #{seq_id} ({task_id[:8]}) via K8s: HTTP {resp.status}")
    else:
        # Run via SSH
        py_cmd = f"""
import json, urllib.request
payload = {json.dumps(payload)}
req = urllib.request.Request(
    'http://127.0.0.1:30885/webhook/plane',
    data=json.dumps(payload).encode('utf-8'),
    headers={{'Content-Type': 'application/json'}},
    method='POST'
)
try:
    with urllib.request.urlopen(req, timeout=10) as r:
        print(f"Triggered #{seq_id}: {{r.status}}")
except Exception as e:
    print(f"Trigger error: {{e}}")
"""
        cmd = [
            "ssh", "-i", SSH_KEY,
            "-o", "BatchMode=yes",
            "-o", "ConnectTimeout=10",
            SSH_HOST,
            f"python3 -c \"{py_cmd.replace('\"', '\\\"')}\""
        ]
        res = subprocess.run(cmd, capture_output=True, text=True, timeout=20, encoding="utf-8")
        log(f"Webhook dispatch #{seq_id}: {res.stdout.strip()}")


def run_orchestration_cycle(dag_tasks, dry_run=False):
    """
    Executes one reconciliation and dispatch cycle across the DAG.
    """
    log("=" * 70)
    log("Autonomous DAG Orchestrator: Starting Reconciliation Cycle")
    log("=" * 70)

    # 1. Fetch current issues from Plane DB
    plane_issues = get_plane_issues()
    log(f"Loaded {len(plane_issues)} issues from Plane DB.")

    # 2. Check completions from git origin/master
    if not dry_run:
        check_git_master_completions(plane_issues)

    # 3. Classify DAG nodes
    resolved_tasks = set()
    running_tasks = set()
    ready_tasks = []
    blocked_tasks = []

    # Map sequence_id -> task metadata
    dag_map = {t["sequence_id"]: t for t in dag_tasks}

    # Pass 1: find resolved & running
    for seq, t in dag_map.items():
        p_iss = plane_issues.get(seq)
        if not p_iss:
            continue
        group = p_iss.get("state_group", "")
        sname = p_iss.get("state_name", "")

        if group == "completed" or sname in ("Done", "Завершено"):
            resolved_tasks.add(seq)
        elif group == "started" or sname in ("AI разработка", "In Progress", "Review", "Уточнение требований"):
            running_tasks.add(seq)

    # Pass 2: check dependencies for unstarted tasks
    for seq, t in dag_map.items():
        if seq in resolved_tasks or seq in running_tasks:
            continue

        p_iss = plane_issues.get(seq, {})
        parents = t.get("depends_on", [])
        unresolved_parents = [p for p in parents if p not in resolved_tasks]

        if not unresolved_parents:
            ready_tasks.append((seq, t))
        else:
            blocked_tasks.append((seq, t, unresolved_parents))

    # Priority sorting for ready tasks:
    # Priority 1: Foundation Infra & WAF (#49, #47, #48)
    # Priority 2: Data Models & Core (#77, #78, #63, #41)
    # Priority 3: Other ready tasks
    def get_task_priority(seq):
        if seq in (49, 47, 48):
            return 100
        if seq in (77, 78, 63, 41):
            return 80
        if seq in (42, 44, 79):
            return 60
        return 10

    ready_tasks.sort(key=lambda item: get_task_priority(item[0]), reverse=True)

    # 4. Check worker availability
    workers = get_workers_status()
    idle_workers = [w for w in workers if w["status"] == "IDLE"]
    busy_workers = [w for w in workers if w["status"] == "BUSY"]
    waiting_quota = [w for w in workers if w["status"] == "WAITING_QUOTA_RESET"]
    unreachable = [w for w in workers if w["status"] in ("UNREACHABLE", "UNKNOWN")]

    log(f"Workers Pool: Total={len(workers)} | IDLE={len(idle_workers)} | BUSY={len(busy_workers)} | WAITING_QUOTA={len(waiting_quota)} | OFFLINE={len(unreachable)}")
    log(f"DAG Status: Total={len(dag_tasks)} | RESOLVED={len(resolved_tasks)} | RUNNING={len(running_tasks)} | READY_TO_RUN={len(ready_tasks)} | BLOCKED={len(blocked_tasks)}")

    # 5. Check currently executing tasks on workers
    active_worker_task_ids = set()
    for w in workers:
        ct = w.get("task")
        if isinstance(ct, dict):
            tid = ct.get("issueId") or ct.get("id")
            if tid:
                active_worker_task_ids.add(tid)

    # Detect orphaned running tasks: in Plane they are 'AI разработка', but no worker is running them!
    orphaned_running_tasks = []
    for seq in sorted(list(running_tasks)):
        iss = plane_issues.get(seq)
        if not iss:
            continue
        task_id = iss["id"]
        if task_id not in active_worker_task_ids:
            orphaned_running_tasks.append((seq, iss))

    log(f"Active tasks executing on workers: {len(active_worker_task_ids)} | Orphaned tasks in AI разработка: {len(orphaned_running_tasks)}")

    # 6. Dispatch ready tasks and recover orphaned tasks if idle workers exist
    dispatched_count = 0
    slots_available = len(idle_workers)

    # First, recover orphaned tasks if we have idle workers
    if orphaned_running_tasks and slots_available > 0:
        log(f"Recovering orphaned tasks in AI разработка ({len(orphaned_running_tasks)} pending execution):")
        for seq, iss in orphaned_running_tasks:
            if slots_available <= 0:
                break
            task_id = iss["id"]
            log(f"  -> Re-triggering orphaned task #{seq} ({task_id[:8]}) '{iss['name'][:40]}' on available worker...")
            if not dry_run:
                trigger_executor_webhook(task_id, seq, iss["name"])
            dispatched_count += 1
            slots_available -= 1

    # Second, dispatch newly unblocked ready tasks
    if ready_tasks and slots_available > 0:
        log(f"Ready to run newly unblocked tasks ({len(ready_tasks)}, available slots: {slots_available}):")
        for seq, t in ready_tasks:
            log(f"  -> #{seq:2d} [{t['module']}] {t['title'][:60]}")

        for seq, t in ready_tasks:
            if slots_available <= 0:
                break

            task_id = t["id"]
            p_iss = plane_issues.get(seq)
            if not p_iss:
                continue

            log(f"Dispatching Ready Task #{seq} ({task_id[:8]}) '{t['title'][:40]}' to worker pool...")
            if not dry_run:
                # Move to AI разработка in DB
                update_plane_issue_state(task_id, STATE_AI_DEV)
                # Trigger executor webhook
                trigger_executor_webhook(task_id, seq, t["title"])
                # Mark as running in our local tracking
                running_tasks.add(seq)

            dispatched_count += 1
            slots_available -= 1
    elif ready_tasks and slots_available <= 0:
        log(f"{len(ready_tasks)} tasks READY_TO_RUN, but all worker slots are currently full. Waiting for workers to finish.")
    else:
        if len(resolved_tasks) == len(dag_tasks):
            log("🎉🎉🎉 ALL 81 TASKS FULLY RESOLVED AND VERIFIED! PROJECT 100% COMPLETE! 🎉🎉🎉")
        elif running_tasks and not orphaned_running_tasks:
            log(f"All active tasks are currently RUNNING ({len(running_tasks)} active on workers). Monitoring progress...")
        elif not running_tasks and not ready_tasks:
            log("No tasks ready to run and no tasks currently running. Check blocked tasks dependencies.")

    # 7. Save current state snapshot

    status_summary = {
        "timestamp": datetime.now().isoformat(),
        "total_tasks": len(dag_tasks),
        "resolved_count": len(resolved_tasks),
        "running_count": len(running_tasks),
        "ready_count": len(ready_tasks),
        "blocked_count": len(blocked_tasks),
        "idle_workers_count": len(idle_workers),
        "busy_workers_count": len(busy_workers),
        "resolved_sequence_ids": sorted(list(resolved_tasks)),
        "running_sequence_ids": sorted(list(running_tasks)),
        "ready_sequence_ids": [s for s, _ in ready_tasks],
        "blocked_details": [
            {"sequence_id": s, "module": t["module"], "title": t["title"], "waiting_for": unres}
            for s, t, unres in blocked_tasks
        ]
    }

    status_file = "docs/plane-tasks/dag_status.json"
    try:
        os.makedirs(os.path.dirname(status_file), exist_ok=True)
        with open(status_file, "w", encoding="utf-8") as f:
            json.dump(status_summary, f, ensure_ascii=False, indent=2)
    except Exception as e:
        log(f"Warning: failed to write {status_file}: {e}")

    log(f"Cycle completed. Dispatched: {dispatched_count}. Summary saved to {status_file}.\n")
    return status_summary


def main():
    parser = argparse.ArgumentParser(description="Autonomous DAG Orchestrator for Plane Tasks")
    parser.add_argument("--config", default="docs/plane-tasks/dependency_graph.yaml", help="Path to dependency_graph.yaml")
    parser.add_argument("--once", action="store_true", help="Run a single reconciliation cycle and exit")
    parser.add_argument("--dry-run", action="store_true", help="Calculate ready tasks without modifying DB or triggering webhooks")
    parser.add_argument("--interval", type=int, default=30, help="Reconciliation interval in seconds (default: 30)")
    args = parser.parse_args()

    if not os.path.exists(args.config):
        log(f"Error: DAG config file not found at {args.config}")
        sys.exit(1)

    log(f"Loading DAG from {args.config}...")
    dag_tasks = parse_yaml_simple(args.config)
    log(f"Parsed {len(dag_tasks)} tasks from DAG manifest.")

    if args.once or args.dry_run:
        run_orchestration_cycle(dag_tasks, dry_run=args.dry_run)
    else:
        log(f"Starting Daemon mode with {args.interval}s interval. Press Ctrl+C to terminate.")
        while True:
            try:
                summary = run_orchestration_cycle(dag_tasks, dry_run=False)
                if summary["resolved_count"] == summary["total_tasks"]:
                    log("Project completed 100%! Orchestrator enters standby mode (checking every 300s).")
                    time.sleep(300)
                else:
                    time.sleep(args.interval)
            except KeyboardInterrupt:
                log("Daemon stopped by user.")
                break
            except Exception as e:
                log(f"Unexpected error in reconciliation cycle: {e}")
                time.sleep(args.interval)


if __name__ == "__main__":
    main()
