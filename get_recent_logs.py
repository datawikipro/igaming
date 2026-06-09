import json
import subprocess
import re
import sys
import concurrent.futures
from datetime import datetime

KUBECTL_PATH = r"C:\Program Files\Lens\resources\x64\kubectl.exe"
KUBECONFIG_PATH = r"C:\Users\chernousov_a\.kube\config"
NAMESPACE = "igaming-dev"

def get_pods():
    print("Fetching pods...")
    cmd = [KUBECTL_PATH, "--kubeconfig", KUBECONFIG_PATH, "get", "pods", "-n", NAMESPACE, "-o", "json"]
    try:
        res = subprocess.run(cmd, capture_output=True, text=True, timeout=30, encoding='utf-8', errors='ignore')
        if res.returncode != 0:
            print(f"Error getting pods: {res.stderr}")
            return []
        data = json.loads(res.stdout)
        return data.get("items", [])
    except Exception as e:
        print(f"Failed to get pods: {e}")
        return []

def get_pod_logs(pod_name, container_name):
    cmd = [
        KUBECTL_PATH, "--kubeconfig", KUBECONFIG_PATH,
        "logs", pod_name, "-c", container_name,
        "-n", NAMESPACE, "--tail=100"
    ]
    try:
        res = subprocess.run(cmd, capture_output=True, text=True, timeout=15, encoding='utf-8', errors='ignore')
        if res.returncode != 0:
            return f"ERROR: {res.stderr.strip()}"
        return res.stdout
    except subprocess.TimeoutExpired:
        return "TIMEOUT: Command timed out after 15 seconds"
    except Exception as e:
        return f"ERROR: Exception: {e}"

def analyze_logs(logs):
    if not logs or "ERROR" in logs or "TIMEOUT" in logs:
        return logs
    
    # Check for exceptions, errors, etc.
    lines = logs.splitlines()
    error_lines = []
    for line in lines:
        lower = line.lower()
        if any(term in lower for term in ["error", "exception", "failed to", "timeout", "forbidden", "connection refused", "403 forbidden", "unreachable"]):
            # ignore benign stuff
            if not any(b in lower for b in ["known errors: {}", "errors: {}", "errors: 0", "0 errors", "errors: null", "errors=0"]):
                error_lines.append(line)
                
    if not error_lines:
        return "OK (No errors found in last 100 lines)"
    
    # Return last 5 errors
    return "\n".join(error_lines[-5:])

def process_pod(pod):
    pod_name = pod["metadata"]["name"]
    # Check if this is an igaming-source pod
    if not pod_name.startswith("igaming-source-"):
        return None
        
    status = pod["status"].get("phase", "Unknown")
    container_statuses = pod["status"].get("containerStatuses", [])
    
    main_container = None
    for cs in container_statuses:
        if cs["name"] != "redis":
            main_container = cs["name"]
            break
            
    if not main_container:
        return None
        
    # Extract bookmaker and role
    parts = pod_name.split("-")
    # pattern: igaming-source-{bm}-{role}-...
    # let's find the role index which is 'crawler' or 'loader' or 'db'
    role = "unknown"
    bm = "unknown"
    for i, part in enumerate(parts):
        if part in ["crawler", "loader", "db"]:
            role = part
            bm = "-".join(parts[2:i])
            break
            
    if role == "db":
        return None # skip database pods
        
    logs = get_pod_logs(pod_name, main_container)
    summary = analyze_logs(logs)
    
    # Get last 3 lines of actual logs for context
    last_lines = []
    if logs and "ERROR" not in logs and "TIMEOUT" not in logs:
        last_lines = [l.strip() for l in logs.splitlines() if l.strip()][-3:]
    
    return {
        "pod_name": pod_name,
        "bookmaker": bm,
        "role": role,
        "status": status,
        "summary": summary,
        "last_lines": last_lines
    }

def main():
    pods = get_pods()
    if not pods:
        print("No pods found.")
        return
        
    print(f"Processing logs for {len(pods)} pods in parallel...")
    results = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=15) as executor:
        futures = {executor.submit(process_pod, pod): pod for pod in pods}
        for i, future in enumerate(concurrent.futures.as_completed(futures)):
            res = future.result()
            if res:
                results.append(res)
                print(f"[{i+1}/{len(pods)}] Finished {res['pod_name']}")
                
    # Group results by bookmaker
    grouped = {}
    for r in results:
        bm = r["bookmaker"]
        if bm not in grouped:
            grouped[bm] = {"crawler": None, "loader": None}
        grouped[bm][r["role"]] = r
        
    # Generate report file
    report_path = "crawler_logs_analysis.md"
    with open(report_path, "w", encoding="utf-8") as f:
        f.write("# 🕵️ Анализ логов кроулеров и лоадеров\n\n")
        f.write(f"**Время анализа:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        
        f.write("## 📌 Сводная таблица по всем БК\n\n")
        f.write("| Букмекер | Краулер Статус | Лоадер Статус | Анализ ошибок краулера | Анализ ошибок лоадера |\n")
        f.write("| :--- | :--- | :--- | :--- | :--- |\n")
        
        for bm in sorted(grouped.keys()):
            c = grouped[bm]["crawler"]
            l = grouped[bm]["loader"]
            
            c_status = c["status"] if c else "N/A"
            l_status = l["status"] if l else "N/A"
            
            c_err = c["summary"].replace("\n", "<br>") if c else "-"
            l_err = l["summary"].replace("\n", "<br>") if l else "-"
            
            # Truncate long error messages for the table
            if len(c_err) > 150: c_err = c_err[:150] + "..."
            if len(l_err) > 150: l_err = l_err[:150] + "..."
            
            f.write(f"| `{bm}` | {c_status} | {l_status} | {c_err} | {l_err} |\n")
            
        f.write("\n\n## 📝 Детальные логи и контекст по проблемным БК\n\n")
        
        for bm in sorted(grouped.keys()):
            c = grouped[bm]["crawler"]
            l = grouped[bm]["loader"]
            
            # Check if there is an issue (either status is not Running, or there are errors)
            has_issue = False
            if c and (c["status"] != "Running" or "OK" not in c["summary"]): has_issue = True
            if l and (l["status"] != "Running" or "OK" not in l["summary"]): has_issue = True
            
            if not has_issue:
                continue
                
            f.write(f"### 🗂️ Букмекер: `{bm}`\n\n")
            
            if c:
                f.write(f"#### 🕷️ Crawler (`{c['pod_name']}` - {c['status']})\n")
                f.write(f"**Анализ ошибок:**\n```text\n{c['summary']}\n```\n")
                if c["last_lines"]:
                    f.write("**Последние строки логов:**\n```text\n" + "\n".join(c["last_lines"]) + "\n```\n")
                    
            if l:
                f.write(f"#### 📥 Loader (`{l['pod_name']}` - {l['status']})\n")
                f.write(f"**Анализ ошибок:**\n```text\n{l['summary']}\n```\n")
                if l["last_lines"]:
                    f.write("**Последние строки логов:**\n```text\n" + "\n".join(l["last_lines"]) + "\n```\n")
            f.write("\n---\n\n")
            
    print(f"Report written to {report_path}")

if __name__ == "__main__":
    main()
