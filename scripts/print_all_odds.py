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
        fId = odd.get("factorId")
        val = odd.get("value")
        p = odd.get("param")
        name = odd.get("factorName")
        grp = odd.get("groupName")
        print(f"fId={fId}\tval={val}\tp={p}\tname={name}\tgrp={grp}")
