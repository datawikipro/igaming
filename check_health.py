import json
import subprocess
import re
import datetime
import sys

ALL_BOOKMAKERS = [
    # Working
    "baltbet", "betboom", "betcity", "betm", "bettery", "fon-bet-kz", "fon-bet-ru",
    "leon", "marathonbet", "marathonbet-by", "marathonbet-com", "olimpbet", "pari", "zenit",
    # Group 1 (BetB2B Clones)
    "1xbet", "888starz", "betandyou", "fansport", "linebet", "megapari",
    "22bet", "betwinner", "melbet-com", "1xbit",
    # Kambi / Betsson / Entain Clones
    "888sport", "leovegas", "mrgreen", "betplay",
    "betsson", "betsafe", "nordicbet",
    # Group 2 (Mirrors & Regional)
    "melbet", "betcity-com", "fon-bet-by", "olimpbet-kz", "sportbet",
    # Group 3 (Standalone)
    "winline", "tennisi", "ligastavok", "pinnacle", "sbobet"
]

KUBECTL_PATH = r"C:\Program Files\Lens\resources\x64\kubectl.exe"
KUBECONFIG_PATH = r"C:\Users\chernousov_a\.kube\config"

def get_pods_data():
    """Runs kubectl get pods -o json and parses the output."""
    try:
        cmd = [KUBECTL_PATH, "--kubeconfig", KUBECONFIG_PATH, "get", "pods", "-n", "igaming-dev", "-o", "json"]
        result = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, encoding='utf-8', errors='ignore')
        if result.returncode != 0:
            print(f"Error running kubectl: {result.stderr}", file=sys.stderr)
            return []
        
        data = json.loads(result.stdout)
        return data.get("items", [])
    except Exception as e:
        print(f"Failed to fetch pods data: {e}", file=sys.stderr)
        return []

def get_bookmaker_and_type(pod_name):
    """Extracts bookmaker name and component type from igaming-source-xxxx name."""
    # Pod pattern: igaming-source-{bm}-{crawler|loader|db}-{deployment-hash}-{pod-hash}
    # Example: igaming-source-marathonbet-by-crawler-5c8f6b7ff-fzfwb
    # Example: igaming-source-betcity-com-loader-5565d767fb-gwtvl
    # Example: igaming-source-marathonbet-db-0
    match = re.match(r"^igaming-source-([\w-]+)-(crawler|loader|db)(?:-[a-z0-9]+-[a-z0-9]+|-\d+)?$", pod_name)
    if match:
        bm = match.group(1)
        comp_type = match.group(2)
        # Verify it's one of our known bookmakers, or return it anyway
        return bm, comp_type
    return None, None

def parse_html_error(log_content):
    """Summarizes HTML responses in logs to avoid dumping giant HTML blocks."""
    if "<!doctype html" in log_content.lower() or "<html" in log_content.lower():
        title_match = re.search(r"<title>(.*?)</title>", log_content, re.IGNORECASE)
        title = title_match.group(1).strip() if title_match else "HTML Page"
        h1_match = re.search(r"<h1>(.*?)</h1>", log_content, re.IGNORECASE)
        h1 = h1_match.group(1).strip() if h1_match else ""
        summary = f"HTML Error Response (Access Denied/Forbidden): Title='{title}'"
        if h1:
            summary += f", H1='{h1}'"
        for code in ["403", "404", "500", "502", "503", "504"]:
            if code in log_content:
                summary += f" (Contains HTTP status {code})"
                break
        return summary
    return None

def analyze_logs(pod_name, container_name, check_previous=False):
    """Fetches logs and checks for errors."""
    try:
        cmd = [KUBECTL_PATH, "--kubeconfig", KUBECONFIG_PATH, "logs", pod_name, "-c", container_name, "--tail=150", "-n", "igaming-dev"]
        if check_previous:
            cmd.append("--previous")
            
        result = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, encoding='utf-8', errors='ignore')
        if result.returncode != 0:
            return f"Failed to fetch logs: {result.stderr.strip()}"
        
        logs = result.stdout
        if not logs.strip():
            return "Logs are empty."
        
        lines = logs.splitlines()
        error_lines = []
        
        # Scan for errors
        for line in lines:
            lower_line = line.lower()
            is_err = False
            for term in ["error", "exception", "failed to", "timeout", "forbidden", "socket is closed", "connection refused", "403 forbidden", "error fetching"]:
                if term in lower_line:
                    is_err = True
                    break
            
            if is_err:
                # Filter out benign performance metrics report or non-error status logs
                is_benign = False
                for benign in ["known errors: {}", "errors: {}", "errors: 0", "performance report", "0 errors", "errors: null", "has errors = false", "errors=0"]:
                    if benign in lower_line:
                        is_benign = True
                        break
                if not is_benign:
                    error_lines.append(line)
        
        if not error_lines:
            return ""
        
        # Clean HTML from errors
        combined_errors = "\n".join(error_lines)
        html_summary = parse_html_error(combined_errors)
        if html_summary:
            return f"{html_summary}\nSample log line: " + error_lines[0][:150]
        
        # Deduplicate and return unique error messages
        unique_errors = []
        seen = set()
        for el in error_lines:
            # Normalize to deduplicate similar errors with different timestamps
            norm_el = re.sub(r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+Z", "", el)
            norm_el = re.sub(r"\[.*?\]", "", norm_el)
            norm_el = norm_el.strip()
            if len(norm_el) > 200:
                norm_el = norm_el[:200] + "..."
            
            if norm_el not in seen:
                seen.add(norm_el)
                unique_errors.append(el)
        
        return "\n".join(unique_errors[:5])
    except Exception as e:
        return f"Error analyzing logs: {e}"

def check_pods_health():
    pods = get_pods_data()
    
    # Structure to hold results
    results = {bm: {"crawler": [], "loader": [], "db": []} for bm in ALL_BOOKMAKERS}
    
    # Process each pod
    for pod in pods:
        name = pod["metadata"]["name"]
        bm, comp_type = get_bookmaker_and_type(name)
        if not bm:
            continue
            
        # Add bookmaker to results dynamically if not in static list
        if bm not in results:
            results[bm] = {"crawler": [], "loader": [], "db": []}
            
        # Extract general status info
        phase = pod["status"].get("phase", "Unknown")
        creation_time = pod["metadata"].get("creationTimestamp", "")
        
        # Calculate age
        age = "Unknown"
        if creation_time:
            try:
                created = datetime.datetime.fromisoformat(creation_time.replace("Z", "+00:00"))
                now = datetime.datetime.now(datetime.timezone.utc)
                diff = now - created
                if diff.days > 0:
                    age = f"{diff.days}d"
                elif diff.seconds >= 3600:
                    age = f"{diff.seconds // 3600}h"
                else:
                    age = f"{diff.seconds // 60}m"
            except Exception:
                pass
                
        # Container statuses
        container_statuses = pod["status"].get("containerStatuses", [])
        restarts = 0
        ready_count = 0
        total_containers = len(container_statuses)
        ready_str = f"{ready_count}/{total_containers}"
        
        main_container = None
        waiting_reason = ""
        waiting_message = ""
        
        for cs in container_statuses:
            c_name = cs["name"]
            if cs.get("ready", False):
                ready_count += 1
            restarts += cs.get("restartCount", 0)
            
            # Find the main container (not redis)
            if c_name != "redis":
                main_container = c_name
                state = cs.get("state", {})
                if "waiting" in state:
                    waiting_reason = state["waiting"].get("reason", "Waiting")
                    waiting_message = state["waiting"].get("message", "")
                elif "terminated" in state:
                    waiting_reason = "Terminated"
                    waiting_message = state["terminated"].get("message", f"Exit Code: {state['terminated'].get('exitCode')}")
                    
        ready_str = f"{ready_count}/{total_containers}"
        
        pod_info = {
            "name": name,
            "phase": phase,
            "ready": ready_str,
            "restarts": restarts,
            "age": age,
            "main_container": main_container,
            "waiting_reason": waiting_reason,
            "waiting_message": waiting_message,
            "errors": "",
            "prev_errors": ""
        }
        
        # Fetch logs if pod is running/completed and has a main container
        if main_container and phase in ["Running", "Succeeded"]:
            # Check current logs
            errors = analyze_logs(name, main_container)
            pod_info["errors"] = errors
            
            # If restarts occurred, check previous logs too
            if restarts > 0:
                prev_errors = analyze_logs(name, main_container, check_previous=True)
                pod_info["prev_errors"] = prev_errors
        elif waiting_reason:
            pod_info["errors"] = f"Container state is {waiting_reason}: {waiting_message}"
            
        results[bm][comp_type].append(pod_info)
        
    return results

def generate_report(results, output_path):
    print("Generating report...")
    
    # Calculate some summary stats
    total_bms = len(results)
    working_bms = []
    failing_bms = []
    not_deployed_bms = []
    
    for bm, comps in results.items():
        crawler_list = comps["crawler"]
        loader_list = comps["loader"]
        
        has_crawler = len(crawler_list) > 0
        has_loader = len(loader_list) > 0
        
        if not has_crawler and not has_loader:
            not_deployed_bms.append(bm)
            continue
            
        # Check if any crawler or loader has errors
        has_errors = False
        
        # Check crawler
        for c in crawler_list:
            if c["phase"] != "Running" or "0/" in c["ready"] or c["restarts"] > 3 or (c["errors"] and "No errors" not in c["errors"]):
                has_errors = True
                break
                
        # Check loader
        if not has_errors:
            for l in loader_list:
                if l["phase"] != "Running" or "0/" in l["ready"] or l["restarts"] > 3 or (l["errors"] and "No errors" not in l["errors"]):
                    has_errors = True
                    break
                    
        if has_errors:
            failing_bms.append(bm)
        else:
            working_bms.append(bm)
            
    with open(output_path, "w", encoding="utf-8") as f:
        f.write("# 📋 Отчет о состоянии краулеров и лоадеров букмекеров\n\n")
        f.write(f"**Дата проверки:** {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        
        # Summary section
        f.write("## 📊 Сводная статистика\n\n")
        f.write(f"* **Всего букмекеров в отчете:** {total_bms}\n")
        f.write(f"* **🟢 Полностью исправные БК (без ошибок):** {len(working_bms)}\n")
        f.write(f"* **🔴 БК с ошибками/сбоями:** {len(failing_bms)}\n")
        f.write(f"* **⚪ Не развернутые БК (нет подов):** {len(not_deployed_bms)}\n\n")
        
        # Table of status overview
        f.write("### 🔍 Обзор по всем букмекерам\n\n")
        f.write("| Букмекер | Статус | Краулер Pod (Готовность / Рестарты) | Лоадер Pod (Готовность / Рестарты) |\n")
        f.write("| :--- | :--- | :--- | :--- |\n")
        
        for bm in sorted(results.keys()):
            comps = results[bm]
            crawler = comps["crawler"][0] if comps["crawler"] else None
            loader = comps["loader"][0] if comps["loader"] else None
            
            status_emoji = "⚪"
            if bm in working_bms:
                status_emoji = "🟢"
            elif bm in failing_bms:
                status_emoji = "🔴"
                
            crawler_str = "-"
            if crawler:
                crawler_str = f"`{crawler['ready']}` (рестартов: {crawler['restarts']})"
                if crawler["phase"] != "Running":
                    crawler_str += f" **{crawler['phase']}**"
                    
            loader_str = "-"
            if loader:
                loader_str = f"`{loader['ready']}` (рестартов: {loader['restarts']})"
                if loader["phase"] != "Running":
                    loader_str += f" **{loader['phase']}**"
                    
            f.write(f"| `{bm}` | {status_emoji} | {crawler_str} | {loader_str} |\n")
            
        f.write("\n---\n\n")
        
        # Details of failing/error bookmakers
        f.write("## 🔴 Детализация ошибок и проблемных БК\n\n")
        if not failing_bms:
            f.write("Ошибок не обнаружено! Все запущенные БК работают исправно.\n")
        else:
            for bm in sorted(failing_bms):
                f.write(f"### 🗂️ Букмекер: `{bm}`\n\n")
                
                # Check Crawler
                c_list = results[bm]["crawler"]
                if c_list:
                    f.write("#### 🕷️ Краулер (Crawler):\n")
                    for c in c_list:
                        status_str = "Исправен"
                        is_issue = False
                        if c["phase"] != "Running" or "0/" in c["ready"] or c["restarts"] > 3 or c["errors"]:
                            status_str = "Проблемы"
                            is_issue = True
                            
                        f.write(f"* **Pod:** `{c['name']}` ({c['ready']}, {c['phase']}, рестартов: {c['restarts']}, возраст: {c['age']})\n")
                        if is_issue:
                            if c["errors"]:
                                f.write("  * **Ошибки в текущих логах:**\n")
                                f.write("    ```text\n")
                                f.write(c["errors"] + "\n")
                                f.write("    ```\n")
                            if c["prev_errors"]:
                                f.write("  * **Ошибки до последнего перезапуска:**\n")
                                f.write("    ```text\n")
                                f.write(c["prev_errors"] + "\n")
                                f.write("    ```\n")
                else:
                    f.write("#### 🕷️ Краулер (Crawler): *Не запущен*\n")
                    
                # Check Loader
                l_list = results[bm]["loader"]
                if l_list:
                    f.write("#### 📥 Лоадер (Loader):\n")
                    for l in l_list:
                        status_str = "Исправен"
                        is_issue = False
                        if l["phase"] != "Running" or "0/" in l["ready"] or l["restarts"] > 3 or l["errors"]:
                            status_str = "Проблемы"
                            is_issue = True
                            
                        f.write(f"* **Pod:** `{l['name']}` ({l['ready']}, {l['phase']}, рестартов: {l['restarts']}, возраст: {l['age']})\n")
                        if is_issue:
                            if l["errors"]:
                                f.write("  * **Ошибки в текущих логах:**\n")
                                f.write("    ```text\n")
                                f.write(l["errors"] + "\n")
                                f.write("    ```\n")
                            if l["prev_errors"]:
                                f.write("  * **Ошибки до последнего перезапуска:**\n")
                                f.write("    ```text\n")
                                f.write(l["prev_errors"] + "\n")
                                f.write("    ```\n")
                else:
                    f.write("#### 📥 Лоадер (Loader): *Не запущен*\n")
                    
                f.write("\n---\n\n")
                
        # Details of working bookmakers
        f.write("## 🟢 Исправные БК (Без ошибок)\n\n")
        if not working_bms:
            f.write("Нет полностью исправных БК.\n")
        else:
            for bm in sorted(working_bms):
                f.write(f"* `{bm}`:\n")
                for c in results[bm]["crawler"]:
                    f.write(f"  * Crawler pod `{c['name']}` ({c['ready']}, {c['phase']}, restarts: {c['restarts']}, age: {c['age']})\n")
                for l in results[bm]["loader"]:
                    f.write(f"  * Loader pod `{l['name']}` ({l['ready']}, {l['phase']}, restarts: {l['restarts']}, age: {l['age']})\n")
            f.write("\n---\n\n")
            
        # Details of not deployed bookmakers
        f.write("## ⚪ Не развернутые букмекеры (Нет подов)\n\n")
        if not not_deployed_bms:
            f.write("Все букмекеры развернуты в системе.\n")
        else:
            for bm in sorted(not_deployed_bms):
                f.write(f"* `{bm}`\n")

    print(f"Report successfully saved to {output_path}")

if __name__ == "__main__":
    output_file = "crawlers_and_loaders_health_report.md"
    results = check_pods_health()
    generate_report(results, output_file)
