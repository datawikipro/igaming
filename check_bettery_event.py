import subprocess
import json

cmd = [
    "kubectl", "exec", "-n", "igaming-source", "igaming-source-fon-bet-ru-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_fonbet", "-t", "-A", "-c",
    "SELECT json_payload FROM match_cache WHERE external_id = '68174938';"
]

res = subprocess.run(cmd, capture_output=True, text=True)
if res.returncode == 0 and res.stdout.strip():
    data = json.loads(res.stdout.strip())
    print("Match:", data.get("team1"), "vs", data.get("team2"))
    odds = data.get("odds", [])
    for o in odds:
        p = o.get("param")
        fid = o.get("factorId")
        if p in [250, 300, 350, 400] or fid in [930, 931, 1791, 1793, 1794, 1796, 1797]:
            print(f"factorId={fid}, param={p}, value={o.get('value')}, factorName='{o.get('factorName')}', groupName='{o.get('groupName')}'")
else:
    print("Not found in igaming_fonbet, stdout:", res.stdout, "stderr:", res.stderr)
