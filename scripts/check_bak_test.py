import subprocess
import json

script = """
import json
d = json.load(open("/etc/sing-box/config.json.bak_test"))
for o in d.get("outbounds", []):
    if o.get("tag") in ["outline-us", "outline-nl"]:
        print(json.dumps(o, indent=2))
"""

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f'sudo python3 -c {subprocess.list2cmdline([script])}']
res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
print(res.stderr)
