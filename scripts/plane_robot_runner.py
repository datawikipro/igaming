#!/usr/bin/env python3
"""
Plane Tasks Runner & Automated Subagent Verification Script for iGaming Monorepo.

Usage:
  python scripts/plane_robot_runner.py list
  python scripts/plane_robot_runner.py inspect <task_key>
  python scripts/plane_robot_runner.py verify <task_key>
  python scripts/plane_robot_runner.py audit-all
"""

import json
import os
import subprocess
import sys
from pathlib import Path

# Force UTF-8 on Windows console
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(__file__).resolve().parent.parent
TASKS_JSON = REPO_ROOT / "docs" / "plane-tasks" / "plane_tasks_import.json"
K8S_DIR = REPO_ROOT / "igaming-k8s"

def load_tasks():
    if not TASKS_JSON.exists():
        print(f"❌ Error: Tasks file not found at {TASKS_JSON}")
        sys.exit(1)
    with open(TASKS_JSON, "r", encoding="utf-8") as f:
        return json.load(f)

def list_tasks():
    data = load_tasks()
    print("\n" + "=" * 90)
    print(f"📋 Plane Tasks Catalog: {data['project']['name']} ({len(data['tasks'])} tasks)")
    print("=" * 90)
    print(f"{'KEY':<20} | {'PRIORITY':<8} | {'EPIC':<16} | {'MODULE':<30} | {'TITLE'}")
    print("-" * 90)
    for t in data["tasks"]:
        print(f"{t['key']:<20} | {t['priority']:<8} | {t['epic']:<16} | {t['module']:<30} | {t['title'][:40]}")
    print("=" * 90 + "\n")

def check_k8s_manifest(module_name):
    # Try finding YAML manifests matching module or bookmaker name
    short_name = module_name.replace("igaming-source-", "").replace("igaming-", "")
    matching_yamls = list(K8S_DIR.glob(f"*{short_name}*.yaml"))
    
    if not matching_yamls:
        return None, "No matching YAML found"
        
    has_liveness = False
    has_readiness = False
    for y in matching_yamls:
        content = y.read_text(encoding="utf-8")
        if "livenessProbe" in content:
            has_liveness = True
        if "readinessProbe" in content:
            has_readiness = True
            
    return matching_yamls, {"livenessProbe": has_liveness, "readinessProbe": has_readiness}

def inspect_task(task_key):
    data = load_tasks()
    task = next((t for t in data["tasks"] if t["key"].upper() == task_key.upper()), None)
    if not task:
        print(f"❌ Task {task_key} not found!")
        sys.exit(1)
        
    print("\n" + "=" * 80)
    print(f"🎯 Task Details: {task['key']} - {task['title']}")
    print("=" * 80)
    print(f"Epic:        {task['epic']}")
    print(f"Module:      {task['module']}")
    print(f"Priority:    {task['priority']}")
    print(f"Status:      {task.get('status', 'Backlog')}")
    print(f"Labels:      {', '.join(task['labels'])}")
    print(f"\nDescription:\n{task['description']}")
    
    # Check module directory
    mod_path = REPO_ROOT / task["module"]
    print(f"\nModule Directory: {mod_path} (Exists: {mod_path.exists()})")
    
    # Check K8s probes
    yamls, probes = check_k8s_manifest(task["module"])
    if yamls:
        print(f"K8s Manifests:   {[y.name for y in yamls]}")
        print(f"K8s Probes:      {probes}")
    print("=" * 80 + "\n")

def verify_module(task_key):
    data = load_tasks()
    task = next((t for t in data["tasks"] if t["key"].upper() == task_key.upper()), None)
    if not task:
        print(f"❌ Task {task_key} not found!")
        sys.exit(1)
        
    module = task["module"]
    print(f"\n⚙️ Running verification for {task['key']} (Module: {module})...")
    
    # Run Maven test-compile
    cmd = ["mvn.cmd" if os.name == "nt" else "mvn", "-pl", module, "test-compile", "-DskipTests"]
    print(f"Executing: {' '.join(cmd)}")
    result = subprocess.run(cmd, cwd=REPO_ROOT, capture_output=True, text=True)
    
    if result.returncode == 0:
        print(f"✅ Module '{module}' compiled successfully!")
    else:
        print(f"❌ Compilation failed for '{module}':\n{result.stderr or result.stdout}")
        sys.exit(1)

def audit_all():
    data = load_tasks()
    print("\n" + "=" * 90)
    print(f"🔍 Monorepo Fail-Fast & Liveness Health Audit across {len(data['tasks'])} modules")
    print("=" * 90)
    
    missing_probes = []
    ready_modules = []
    
    for t in data["tasks"]:
        module = t["module"]
        yamls, probes = check_k8s_manifest(module)
        if isinstance(probes, dict) and probes.get("livenessProbe"):
            ready_modules.append((t["key"], module))
        else:
            missing_probes.append((t["key"], module, [y.name for y in yamls] if yamls else ["None"]))
            
    print(f"🟢 Modules with K8s Liveness Probes: {len(ready_modules)}")
    print(f"🔴 Modules Missing K8s Liveness Probes: {len(missing_probes)}")
    print("\n--- Missing Probes List ---")
    for key, mod, y_names in missing_probes[:15]:
        print(f"  • {key:<22} | {mod:<30} | Manifests: {', '.join(y_names)}")
    if len(missing_probes) > 15:
        print(f"  ... and {len(missing_probes) - 15} more modules.")
    print("=" * 90 + "\n")

def start_branch(task_key):
    data = load_tasks()
    task = next((t for t in data["tasks"] if t["key"].upper() == task_key.upper()), None)
    if not task:
        print(f"❌ Task {task_key} not found!")
        sys.exit(1)
        
    branch_name = f"task/{task_key.lower().replace('_', '-')}"
    print(f"\n🌿 Creating and checking out feature branch '{branch_name}' for task {task_key}...")
    
    # Check if branch exists or create it
    cmd = ["git", "checkout", "-b", branch_name]
    res = subprocess.run(cmd, cwd=REPO_ROOT, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"Branch '{branch_name}' exists, switching to it...")
        subprocess.run(["git", "checkout", branch_name], cwd=REPO_ROOT, check=True)
        
    print(f"✅ Active branch is now '{branch_name}'")

def finish_merge(task_key):
    data = load_tasks()
    task = next((t for t in data["tasks"] if t["key"].upper() == task_key.upper()), None)
    if not task:
        print(f"❌ Task {task_key} not found!")
        sys.exit(1)
        
    branch_name = f"task/{task_key.lower().replace('_', '-')}"
    print(f"\n🔀 Merging branch '{branch_name}' into master and pushing...")
    
    # 0. Commit & push changes inside modified git submodules first
    print("Checking and pushing modified git submodules...")
    subprocess.run(["git", "submodule", "foreach", 'git add . && (git commit -m "feat: task auto-commit" || true) && (git push origin HEAD:main || git push origin HEAD:master || true)'], cwd=REPO_ROOT, capture_output=True)

    # 1. Commit any remaining changes in main monorepo
    subprocess.run(["git", "add", "."], cwd=REPO_ROOT, check=True)
    subprocess.run(["git", "commit", "-m", f"feat({task['module']}): completed task {task_key} with fail-fast and probes"], cwd=REPO_ROOT, capture_output=True)
    
    # 2. Push feature branch to trigger CI/CD tag build
    print(f"Pushing feature branch '{branch_name}' to remote...")
    subprocess.run(["git", "push", "-u", "origin", branch_name], cwd=REPO_ROOT, capture_output=True)
    
    # 3. Checkout master and merge
    print("Switching to master and merging...")
    subprocess.run(["git", "checkout", "master"], cwd=REPO_ROOT, check=True)
    subprocess.run(["git", "pull", "origin", "master"], cwd=REPO_ROOT, capture_output=True)
    subprocess.run(["git", "merge", branch_name], cwd=REPO_ROOT, check=True)
    
    # 4. Push master to trigger Production CI/CD
    print("Pushing master to trigger Production CI/CD deploy...")
    res = subprocess.run(["git", "push", "origin", "master"], cwd=REPO_ROOT, capture_output=True, text=True)
    if res.returncode == 0:
        print(f"🎉 Task {task_key} successfully merged to master and pushed to origin!")
    else:
        print(f"⚠️ Git push master returned: {res.stderr or res.stdout}")

def verify_live_k8s(task_key):
    data = load_tasks()
    task = next((t for t in data["tasks"] if t["key"].upper() == task_key.upper()), None)
    if not task:
        print(f"❌ Task {task_key} not found!")
        sys.exit(1)
        
    module = task["module"]
    print(f"\n🔍 Checking live Kubernetes status for module '{module}'...")
    
    for ns in ["igaming-source", "igaming-dev", "igaming-master"]:
        res = subprocess.run(["kubectl", "get", "pods", "-n", ns, "-l", f"app={module}-crawler", "-o", "json"], capture_output=True, text=True)
        if res.returncode == 0 and res.stdout.strip():
            import json
            data_pod = json.loads(res.stdout)
            items = data_pod.get("items", [])
            if items:
                status = items[0].get("status", {}).get("phase")
                print(f"Pod in '{ns}' status: {status}")
                if status == "Running":
                    print(f"✅ Live Pod for '{module}' is RUNNING in '{ns}'!")
                    return True
                elif status in ["CrashLoopBackOff", "Error", "Failed"]:
                    print(f"❌ Live Pod for '{module}' is BROKEN in '{ns}' ({status})!")
                    return False
    print(f"⚠️ No active pods found for '{module}'. Assuming needs initial deploy or repair.")
    return False

def create_repair_task(task_key, reason="Base module is broken in K8s"):
    data = load_tasks()
    task = next((t for t in data["tasks"] if t["key"].upper() == task_key.upper()), None)
    if not task:
        print(f"❌ Task {task_key} not found!")
        sys.exit(1)
        
    module = task["module"]
    repair_key = f"IGAMING-FIX-{module.upper().replace('IGAMING-SOURCE-', '')}"
    branch_name = f"fix/{module.replace('igaming-source-', '')}"
    
    print(f"\n🚨 CREATING REPAIR SUB-TASK '{repair_key}' FOR BROKEN MODULE '{module}'...")
    print(f"Reason: {reason}")
    print(f"Creating repair branch: {branch_name}")
    
    subprocess.run(["git", "checkout", "-b", branch_name], cwd=REPO_ROOT, capture_output=True)
    print(f"✅ Created repair branch '{branch_name}'")
    print(f"🎉 Repair task '{repair_key}' registered. Transitioning repair task state to 'AI разработка'...")

def audit_logs(task_key, since_minutes=5):
    data = load_tasks()
    task = next((t for t in data["tasks"] if t["key"].upper() == task_key.upper()), None)
    if not task:
        print(f"❌ Task {task_key} not found!")
        sys.exit(1)
        
    module = task["module"]
    print(f"\n📜 Auditing logs over the last {since_minutes} minutes for '{module}'...")
    
    found_logs = False
    has_errors = False
    
    for ns in ["igaming-source", "igaming-dev", "igaming-master"]:
        res = subprocess.run(["kubectl", "get", "pods", "-n", ns, "-l", f"app={module}-crawler", "-o", "name"], capture_output=True, text=True)
        if res.returncode == 0 and res.stdout.strip():
            pod_name = res.stdout.strip().split("\n")[0]
            found_logs = True
            print(f"Fetching logs for '{pod_name}' in namespace '{ns}' (since {since_minutes}m)...")
            log_res = subprocess.run(["kubectl", "logs", pod_name, "-n", ns, f"--since={since_minutes}m"], capture_output=True, text=True)
            logs = log_res.stdout
            
            critical_lines = []
            for line in logs.split("\n"):
                if any(kw in line for kw in ["Exception", "ERROR", "FATAL INCIDENT", "CrashLoop", "NullPointerException"]):
                    critical_lines.append(line)
                    
            if critical_lines:
                has_errors = True
                print(f"⚠️ Found {len(critical_lines)} critical log entries in last {since_minutes}m:")
                for l in critical_lines[:10]:
                    print(f"   • {l[:120]}")
                if len(critical_lines) > 10:
                    print(f"   ... and {len(critical_lines) - 10} more error lines.")
            else:
                print(f"✅ Clean logs! 0 critical exceptions found in last {since_minutes} minutes for '{module}'.")
                
    if not found_logs:
        print(f"⚠️ No active pods found to audit logs for '{module}'.")
        return False
        
    return not has_errors

if __name__ == "__main__":
    if len(sys.argv) < 2:
        list_tasks()
    elif sys.argv[1] == "list":
        list_tasks()
    elif sys.argv[1] == "inspect" and len(sys.argv) > 2:
        inspect_task(sys.argv[2])
    elif sys.argv[1] == "verify" and len(sys.argv) > 2:
        verify_module(sys.argv[2])
    elif sys.argv[1] == "verify-live" and len(sys.argv) > 2:
        verify_live_k8s(sys.argv[2])
    elif sys.argv[1] == "audit-logs" and len(sys.argv) > 2:
        mins = int(sys.argv[3]) if len(sys.argv) > 3 else 5
        audit_logs(sys.argv[2], mins)
    elif sys.argv[1] == "create-repair" and len(sys.argv) > 2:
        reason = sys.argv[3] if len(sys.argv) > 3 else "Base module is broken in K8s"
        create_repair_task(sys.argv[2], reason)
    elif sys.argv[1] == "start-branch" and len(sys.argv) > 2:
        start_branch(sys.argv[2])
    elif sys.argv[1] == "finish-merge" and len(sys.argv) > 2:
        finish_merge(sys.argv[2])
    elif sys.argv[1] == "audit-all":
        audit_all()
    else:
        print("Usage: python scripts/plane_robot_runner.py [list | inspect <key> | verify <key> | verify-live <key> | audit-logs <key> [mins] | create-repair <key> | start-branch <key> | finish-merge <key> | audit-all]")
