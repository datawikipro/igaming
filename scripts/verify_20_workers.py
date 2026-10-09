import subprocess
import json

runner = """
import urllib.request, json

print(f"{'Pod Index':<10} | {'Pod Name':<20} | {'Cred ID':<8} | {'Email':<35} | {'Status':<8} | {'5h Quota':<10} | {'Weekly Quota':<12}")
print("-" * 115)

for i in range(20):
    url = f"http://plane-ai-worker-{i}.plane-ai-worker.plane.svc.cluster.local:8000/status"
    try:
        req = urllib.request.Request(url, headers={"Accept": "application/json"})
        with urllib.request.urlopen(req, timeout=3) as resp:
            data = json.loads(resp.read().decode())
            p_idx = data.get("podIndex", i)
            p_name = data.get("podName", f"worker-{i}")
            c_id = str(data.get("credentialId", "None"))
            email = data.get("email", "unknown")
            st = data.get("status", "UNKNOWN")
            g5h = data.get("gemini5hQuota")
            gw = data.get("geminiWeeklyQuota")
            s5h = f"{g5h*100:.0f}%" if g5h is not None else "N/A"
            sw = f"{gw*100:.0f}%" if gw is not None else "N/A"
            print(f"{p_idx:<10} | {p_name:<20} | {c_id:<8} | {email:<35} | {st:<8} | {s5h:<10} | {sw:<12}")
    except Exception as e:
        print(f"{i:<10} | {f'plane-ai-worker-{i}':<20} | {'ERR':<8} | {str(e)[:35]:<35} | {'ERROR':<8} | {'N/A':<10} | {'N/A':<12}")
"""

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'root@100.78.183.101', f'kubectl -n plane exec plane-ai-worker-0 -- python3 -c {subprocess.list2cmdline([runner])}']
res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
print(res.stderr)
