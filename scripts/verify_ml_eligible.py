import subprocess
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

def main():
    cmd = [
        'kubectl', 'exec', '-n', 'igaming-analytics',
        'deployment/igaming-ai-analyst', '--',
        'curl', '-s', 'http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095/api/v1/mdm/matches/active?since_hours=1&until_hours=24'
    ]
    res = subprocess.run(cmd, capture_output=True, encoding='utf-8', errors='replace')
    if res.returncode != 0:
        print("Error:", res.stderr)
        return

    data = json.loads(res.stdout)
    print(f"Total active matches in MDM window: {len(data)}")
    tier1 = [m for m in data if m.get('ml_eligible')]
    tier0 = [m for m in data if not m.get('ml_eligible')]
    print(f"  TIER 1 (ml_eligible=True):  {len(tier1)}")
    print(f"  TIER 0/2 (ml_eligible=False): {len(tier0)}")

    print("\n--- Sample TIER 1 (ML Eligible) ---")
    for m in tier1[:5]:
        print(f"  Match #{m['match_id']}: [{m.get('coverage_tier')}] {m.get('tournament_name')} | {m.get('home_team_name')} vs {m.get('away_team_name')}")

    print("\n--- Sample TIER 0 / SCRATCH (Filtered Out from ML) ---")
    for m in tier0[:5]:
        print(f"  Match #{m['match_id']}: [{m.get('coverage_tier')}] {m.get('tournament_name')} | {m.get('home_team_name')} vs {m.get('away_team_name')}")

if __name__ == '__main__':
    main()
