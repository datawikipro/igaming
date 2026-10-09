import subprocess
import json

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-source", "igaming-source-bettery-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_bettery", "-c",
    "SELECT json_payload FROM match_cache WHERE external_id = '68412232';"
]
r = subprocess.run(cmd, capture_output=True, text=True)

start = r.stdout.find("{")
end = r.stdout.rfind("}")
if start != -1 and end != -1:
    raw_json = r.stdout[start:end+1]
    data = json.loads(raw_json)
    for odd in data.get("odds", []):
        val = odd.get("value")
        if val is not None and 2.85 <= val <= 3.15:
            print(f"factorId={odd.get('factorId')}, factorName='{odd.get('factorName')}', groupName='{odd.get('groupName')}', param={odd.get('param')}, value={val}")
