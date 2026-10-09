import subprocess
import json

cmd = ["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", 
       "kubectl get pods -n igaming-source -o json"]
out = subprocess.check_output(cmd)
data = json.loads(out)

pods = data.get("items", [])
db_pods = [p["metadata"]["name"] for p in pods if "-db-" in p["metadata"]["name"]]

print(f"Found {len(db_pods)} DB pods in igaming-source.")

# Check a selection of offshore and key bookmakers
check_script = """
for pod in $(kubectl get pods -n igaming-source -o jsonpath='{.items[*].metadata.name}' | tr ' ' '\\n' | grep -- '-db-'); do
    bk=$(echo "$pod" | sed -e 's/igaming-source-//' -e 's/-db-0//' -e 's/-db-[0-9]*//')
    db="igaming_${bk}"
    cnt=$(kubectl exec -n igaming-source "$pod" -- psql -U postgres -d "$db" -t -c "SELECT count(*) FROM match_cache;" 2>/dev/null | tr -d ' ')
    if [ -n "$cnt" ]; then
        echo "$bk: $cnt"
    fi
done
"""

res = subprocess.run(["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", check_script],
                     capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("ERR:", res.stderr[:500])
