import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

# Get all pods in igaming-source
output = subprocess.check_output(["kubectl", "get", "pods", "-n", "igaming-source", "-o", "wide"]).decode("utf-8")

pods_with_errors = []

for line in output.splitlines()[1:]:
    parts = line.split()
    if len(parts) >= 3:
        pod_name = parts[0]
        status = parts[2]
        restarts = parts[3]
        
        # Check logs if pod is Running or Error/CrashLoop
        if status in ["Running", "Error", "CrashLoopBackOff"] or int(restarts if restarts.isdigit() else 0) > 0:
            try:
                logs = subprocess.check_output(
                    ["kubectl", "logs", "-n", "igaming-source", f"pod/{pod_name}", "--all-containers", "--tail=40"],
                    stderr=subprocess.STDOUT
                ).decode("utf-8", errors="ignore")
                
                # Check for explicit errors/exceptions
                error_lines = [l for l in logs.splitlines() if "ERROR" in l or "Exception" in l or "Fatal" in l or "ERR_" in l]
                if error_lines:
                    pods_with_errors.append((pod_name, status, restarts, error_lines[:3]))
            except Exception as e:
                pass

print(f"--- POD LOG AUDIT REPORT ({len(pods_with_errors)} pods with log warnings/errors) ---")
for name, st, r, errs in pods_with_errors:
    print(f"\n[POD]: {name} | Status: {st} | Restarts: {r}")
    for err in errs:
        print(f"   [ERR]: {err}")
