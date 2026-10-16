#!/usr/bin/env python3
import json
import time
import subprocess
import requests

SOFA_ON_DEMAND_URL = "http://10.100.90.245:3041"
ENRICHMENT_URL = "http://10.106.83.152:8085"

print("=" * 60)
print("1. Testing SofaScore On-Demand: GET /queue-stats")
print("=" * 60)
res = requests.get(f"{SOFA_ON_DEMAND_URL}/api/v1/sofascore/on-demand/queue-stats", timeout=10)
print(f"Status: {res.status_code}")
print(f"Queue Stats: {res.json()}")
assert res.status_code == 200, "Queue stats endpoint failed"

print("\n" + "=" * 60)
print("2. Fetching Dynamic Active Match from DB")
print("=" * 60)
psql_cmd = [
    "kubectl", "exec", "-n", "igaming-dev", "igaming-aggregator-db-0", "--",
    "psql", "-U", "postgres", "-d", "igaming_aggregator", "-t", "-A", "-c",
    "SELECT m.id, m.sport_name, t1.default_display_name, t2.default_display_name FROM match_record m JOIN team t1 ON m.team1_id = t1.id JOIN team t2 ON m.team2_id = t2.id WHERE m.team1_id IS NOT NULL AND m.team2_id IS NOT NULL ORDER BY m.id DESC LIMIT 1;"
]
db_out = subprocess.check_output(psql_cmd, text=True).strip()
# Remove any defaulted container warning
lines = [l for l in db_out.splitlines() if "|" in l]
if not lines:
    raise RuntimeError(f"Could not find match in DB output: {db_out}")

parts = lines[0].split("|")
match_id = int(parts[0])
sport_name = parts[1]
team1 = parts[2]
team2 = parts[3]
print(f"Selected Match: ID={match_id}, Sport={sport_name}, Team1='{team1}', Team2='{team2}'")

print("\n" + "=" * 60)
print("3. Testing SofaScore On-Demand: POST /refresh (Async Mode)")
print("=" * 60)
async_req = {
    "match_id": match_id,
    "sport": sport_name.lower(),
    "home_team": team1,
    "away_team": team2,
    "async": True,
    "priority": 0
}
res = requests.post(f"{SOFA_ON_DEMAND_URL}/api/v1/sofascore/on-demand/refresh", json=async_req, timeout=10)
print(f"Status: {res.status_code}")
async_body = res.json()
print(f"Response: {async_body}")
assert res.status_code == 202, "Async on-demand submission failed"
task_id = async_body.get("task_id")
print(f"Polling Sofa task status for task_id={task_id}...")
for _ in range(5):
    time.sleep(1)
    status_res = requests.get(f"{SOFA_ON_DEMAND_URL}/api/v1/sofascore/on-demand/status/{task_id}", timeout=5)
    print(f"Poll status: {status_res.status_code} -> {status_res.json().get('status')}")

print("\n" + "=" * 60)
print("4. Testing SofaScore On-Demand: POST /refresh (Sync Mode)")
print("=" * 60)
sync_req = {
    "match_id": match_id,
    "sport": sport_name.lower(),
    "home_team": team1,
    "away_team": team2,
    "async": False,
    "priority": 0
}
t0 = time.time()
res = requests.post(f"{SOFA_ON_DEMAND_URL}/api/v1/sofascore/on-demand/refresh", json=sync_req, timeout=45)
elapsed = int((time.time() - t0) * 1000)
print(f"Status: {res.status_code} (took {elapsed}ms)")
sync_body = res.json()
print(f"Sync response: status={sync_body.get('status')}, message={sync_body.get('message')}")
assert res.status_code == 200, "Sync on-demand failed"

print("\n" + "=" * 60)
print("5. Testing Prediction API: POST /prepare (Sync Mode)")
print("=" * 60)
pred_url = f"{ENRICHMENT_URL}/api/matches/{match_id}/prediction/prepare?triggerLlm=false&async=false&lang=ru"
t0 = time.time()
res = requests.post(pred_url, timeout=45)
elapsed = int((time.time() - t0) * 1000)
print(f"Status: {res.status_code} (took {elapsed}ms)")
if res.status_code == 200:
    pred_data = res.json()
    print(f"Prediction Context Generated successfully:")
    print(f" - match_id: {pred_data.get('match_id')}")
    print(f" - sport: {pred_data.get('sport')}")
    print(f" - teams: {pred_data.get('team1')} vs {pred_data.get('team2')}")
    print(f" - current_score: {pred_data.get('current_score')}")
    print(f" - lineups_confirmed: {pred_data.get('lineups_confirmed')}")
    print(f" - team1_dna: {pred_data.get('team1_dna')}")
    print(f" - team2_dna: {pred_data.get('team2_dna')}")
    print(f" - context_tags: {pred_data.get('context_tags')}")
    print(f" - narrative present: {pred_data.get('narrative') is not None}")
    print(f" - prompt_context snippet: {pred_data.get('prompt_context', '')[:200]}...")
else:
    print(f"Error: {res.text}")
assert res.status_code == 200, f"Sync prediction preparation failed: {res.status_code}"

print("\n" + "=" * 60)
print("6. Testing Prediction API: POST /prepare (Async Mode)")
print("=" * 60)
async_pred_url = f"{ENRICHMENT_URL}/api/matches/{match_id}/prediction/prepare?triggerLlm=false&async=true&lang=ru"
res = requests.post(async_pred_url, timeout=10)
print(f"Status: {res.status_code}")
async_pred_body = res.json()
print(f"Response: {async_pred_body}")
assert res.status_code == 202, "Async prediction failed"
pred_task_id = async_pred_body.get("task_id")

print(f"Polling prediction task status for task_id={pred_task_id}...")
for attempt in range(15):
    time.sleep(1)
    status_res = requests.get(f"{ENRICHMENT_URL}/api/matches/{match_id}/prediction/status/{pred_task_id}", timeout=5)
    body = status_res.json()
    st = body.get("status")
    print(f"[{attempt+1}s] Poll status: {st or 'COMPLETED (Result DTO)'}")
    if st == "COMPLETED" or "match_id" in body:
        print("Async prediction context completed successfully!")
        result = body
        print(f" - match_id: {result.get('match_id')}")
        print(f" - current_score: {result.get('current_score')}")
        break
    elif st == "FAILED":
        raise RuntimeError("Async prediction task failed!")

print("\n" + "=" * 60)
print("7. Testing Quick Refresh-Data Endpoint: POST /refresh-data")
print("=" * 60)
ref_url = f"{ENRICHMENT_URL}/api/matches/{match_id}/prediction/refresh-data?lang=ru"
res = requests.post(ref_url, timeout=30)
print(f"Status: {res.status_code}")
assert res.status_code == 200, f"Refresh-data failed: {res.status_code}"
ref_data = res.json()
print(f"Refresh-data succeeded for match {match_id}. Score: {ref_data.get('current_score')}")

print("\n" + "=" * 60)
print("ALL E2E VERIFICATION CHECKS PASSED WITH FLYING COLORS!")
print("=" * 60)
