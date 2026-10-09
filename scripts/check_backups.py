import subprocess

script = """
import json, glob

for f in sorted(glob.glob("/etc/sing-box/config.json*")):
    try:
        data = json.load(open(f))
        obs = data.get("outbounds", [])
        for o in obs:
            if o.get("tag") in ["outline-us", "outline-nl", "outline-fi"]:
                print(f"{f}: {o.get('tag')} -> {o.get('type')} {o.get('server')}:{o.get('server_port')}")
    except Exception:
        pass
"""

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f'sudo python3 -c {subprocess.list2cmdline([script])}']
res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
print(res.stderr)
