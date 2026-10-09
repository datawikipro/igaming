import subprocess
import json

patch_script = """
import json, subprocess

res = subprocess.run(["kubectl", "-n", "plane", "get", "sts", "plane-ai-worker", "-o", "json"], capture_output=True, text=True)
sts = json.loads(res.stdout)

c = sts["spec"]["template"]["spec"]["containers"][0]
for e in c.get("env", []):
    if e.get("name") == "NO_PROXY":
        val = e.get("value", "")
        # Remove github domains
        val = val.replace(",github.com,.github.com,githubusercontent.com,.githubusercontent.com,ghcr.io", "")
        e["value"] = val
        print("Updated NO_PROXY to:", val[:100] + "...")
    elif e.get("name") in ["PROXY_HTTP_URL", "HTTP_PROXY", "HTTPS_PROXY"]:
        e["value"] = "http://100.83.113.50:3128"

with open("/tmp/sts_patch.json", "w") as f:
    json.dump(sts, f)

subprocess.run(["kubectl", "apply", "-f", "/tmp/sts_patch.json"], check=True)
print("StatefulSet plane-ai-worker successfully patched and applied!")
"""

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'root@100.78.183.101', f'python3 -c {subprocess.list2cmdline([patch_script])}']
res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
print(res.stderr)
