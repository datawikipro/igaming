import json
import urllib.request
import subprocess

def run_ssh(cmd):
    full_cmd = ["ssh", "-i", "C:\\Users\\chernousov_a\\.ssh\\id_ed25519", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", cmd]
    res = subprocess.run(full_cmd, capture_output=True, text=True, encoding="utf-8")
    return res.stdout, res.stderr, res.returncode

def main():
    cmd = "curl -s http://10.105.12.125:8091/api/v1/llm-credentials/active"
    out, err, code = run_ssh(cmd)
    if code != 0:
        print("Error fetching credentials:", err)
        return
    
    try:
        data = json.loads(out)
    except Exception as e:
        print("JSON parse error:", e, out[:200])
        return

    ag = [c for c in data if c.get("provider") == "ANTIGRAVITY"]
    ag.sort(key=lambda x: x["id"])
    print(f"Total ANTIGRAVITY credentials: {len(ag)}")
    for i, c in enumerate(ag):
        print(f"Index {i:2d}: ID={c.get('id'):2d} | Email={c.get('email'):<35} | Active={c.get('isActive')}")

if __name__ == "__main__":
    main()
