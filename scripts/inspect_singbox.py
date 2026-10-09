import subprocess
import json

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', 'sudo cat /etc/sing-box/config.json']
res = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
if res.returncode == 0:
    try:
        data = json.loads(res.stdout)
        print("Successfully loaded config.json")
        print("\n--- INBOUNDS ---")
        for ib in data.get("inbounds", []):
            print(f"Tag: {ib.get('tag')}, Type: {ib.get('type')}, Listen: {ib.get('listen')}:{ib.get('listen_port')}")
        print("\n--- OUTBOUNDS ---")
        for ob in data.get("outbounds", []):
            print(f"Tag: {ob.get('tag')}, Type: {ob.get('type')}, Server: {ob.get('server')}:{ob.get('server_port')}")
        with open("scripts/sing_box_config.json", "w", encoding="utf-8") as f:
            f.write(res.stdout)
    except Exception as e:
        print("JSON parse error:", e)
else:
    print("Error:", res.stderr)
