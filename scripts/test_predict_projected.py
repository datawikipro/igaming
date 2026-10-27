import requests
import json
import time

payload = {
    'sport_name': 'football',
    'team1_name': 'Milan',
    'team2_name': 'Arsenal',
    'start_time': int(time.time() * 1000) + 12 * 3600 * 1000,
    'is_lineup_confirmed': False,
    'team1_recent_matches': [
        {'result': 'WIN', 'team_score': 2, 'opponent_score': 0, 'clean_sheet': True},
        {'result': 'DRAW', 'team_score': 1, 'opponent_score': 1, 'clean_sheet': False}
    ],
    'team2_recent_matches': [
        {'result': 'WIN', 'team_score': 3, 'opponent_score': 1, 'clean_sheet': False},
        {'result': 'LOSS', 'team_score': 0, 'opponent_score': 1, 'clean_sheet': False}
    ]
    # NOTE: team1_starters and team2_starters are intentionally OMITTED to trigger Projected Lineups!
}

t0 = time.perf_counter()
resp = requests.post('http://127.0.0.1:8085/predict', json=payload)
dt = (time.perf_counter() - t0) * 1000

print(f"Status: {resp.status_code} in {dt:.2f} ms")
data = resp.json()
print("\n--- 🧠 PREDICT RESPONSE ---")
print(f"Has ML Evaluation:      {data.get('has_ml_evaluation')}")
print(f"Is Lineup Projected:    {data.get('is_lineup_projected')}")
print(f"Lineup Resilience:      {data.get('lineup_resilience_pct')}%")
print(f"Key Dependencies:       {data.get('key_player_dependencies')}")
print(f"Recommended Market:     {data.get('recommended_market')} @ prob {data.get('recommended_prob')}")
print(f"Confidence Tier:        {data.get('confidence_tier')}")

h_starters = data.get('projected_starters_home') or []
a_starters = data.get('projected_starters_away') or []
print(f"\n⚽ Home Projected XI ({len(h_starters)} players):")
for p in h_starters[:5]:
    print(f"  * {p.get('name')} ({p.get('pos')}): rating {p.get('avg_rating', 6.8)}")
print("  ... and 6 more")

print(f"\n⚽ Away Projected XI ({len(a_starters)} players):")
for p in a_starters[:5]:
    print(f"  * {p.get('name')} ({p.get('pos')}): rating {p.get('avg_rating', 6.8)}")
print("  ... and 6 more")

print(f"\n📖 Tactical Narrative:\n{data.get('tactical_narrative')}")
