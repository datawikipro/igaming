import subprocess, json

cmd = ["kubectl", "exec", "-i", "-c", "postgres", "-n", "igaming-source", "igaming-source-pari-db-0", "--", "psql", "-U", "postgres", "-d", "igaming_pari", "-t", "-c", "SELECT json_payload FROM match_cache WHERE id = 20418;"]
res = subprocess.run(cmd, capture_output=True, text=True)
payload_str = res.stdout.strip()
if payload_str:
    data = json.loads(payload_str)
    odds = data.get("odds", [])
    print(f"Total odds in pari: {len(odds)}")
    for o in odds[:25]:
        print(o)
