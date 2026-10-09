import subprocess
import json

sql = """
SELECT a.id, ROUND(a.profit_percent::numeric, 2) AS profit, a.status, a.match_id, a.match_description, a.detected_at,
       json_agg(json_build_object(
           'bookmaker', l.bookmaker,
           'bet_type', l.bet_type,
           'param', l.odds_param_at_detection,
           'value', l.odds_value_at_detection
       )) as legs
FROM surebet_alert a
JOIN surebet_outcome_legs l ON a.id = l.alert_id
WHERE a.status = 'ACTIVE'
GROUP BY a.id, a.profit_percent, a.status, a.match_id, a.match_description, a.detected_at
ORDER BY a.profit_percent DESC;
"""

cmd = [
    "kubectl", "exec", "-c", "postgres", "-i", "-n", "igaming-dev", "igaming-aggregator-db-0",
    "--", "psql", "-U", "postgres", "-d", "igaming_aggregator", "-t", "-A", "-c", sql
]

res = subprocess.run(cmd, capture_output=True, text=True)
if res.stderr:
    print("STDERR:", res.stderr)

lines = [l for l in res.stdout.strip().split("\n") if l.strip()]
print(f"Total ACTIVE alerts found: {len(lines)}")

alerts = []
for line in lines:
    parts = line.split("|")
    if len(parts) >= 7:
        aid = parts[0]
        profit = float(parts[1])
        status = parts[2]
        mid = parts[3]
        mdesc = parts[4]
        detected = parts[5]
        legs = json.loads(parts[6])
        alerts.append({
            "id": aid,
            "profit": profit,
            "status": status,
            "match_id": mid,
            "match_desc": mdesc,
            "detected_at": detected,
            "legs": legs
        })

print("\n=== ACTIVE SUREBETS AUDIT ===")
for a in alerts:
    print(f"\nAlert #{a['id']} | Profit: {a['profit']}% | Match: {a['match_desc']} (ID: {a['match_id']})")
    print(f"Detected: {a['detected_at']}")
    legs_str = []
    inv_prob_sum = 0.0
    for leg in a['legs']:
        val = float(leg['value'])
        inv_prob_sum += 1.0 / val if val > 0 else 999.0
        legs_str.append(f"  - {leg['bookmaker']}: {leg['bet_type']} (param={leg['param']}, odds={val})")
    print("\n".join(legs_str))
    
    # Validation checks
    issues = []
    if len(a['legs']) == 2:
        val1 = float(a['legs'][0]['value'])
        val2 = float(a['legs'][1]['value'])
        bt1 = a['legs'][0]['bet_type']
        bt2 = a['legs'][1]['bet_type']
        p1 = a['legs'][0]['param']
        p2 = a['legs'][1]['param']
        
        if val1 > 2.20 and val2 > 2.20:
            issues.append(f"IMPOSSIBLE ODDS: Both legs > 2.20 ({val1}, {val2}) in 2-way market! Indicates INVERSE FACTOR MAPPING or SAME-SIDE SELECTION!")
        
        # Check totals parameter matching
        if "TOTAL" in bt1 and "TOTAL" in bt2:
            if p1 != p2:
                issues.append(f"TOTAL PARAM MISMATCH: {p1} vs {p2}")
            if bt1 == bt2:
                issues.append(f"IDENTICAL TOTAL OUTCOMES: {bt1} vs {bt2}")
                
        # Check handicap parameter matching (should be sum = 0 or complementary)
        if "HANDICAP" in bt1 and "HANDICAP" in bt2:
            if p1 is not None and p2 is not None:
                if abs(float(p1) + float(p2)) > 0.001:
                    issues.append(f"HANDICAP PARAM NOT OPPOSITE: {p1} + {p2} != 0")
            if bt1 == bt2:
                issues.append(f"IDENTICAL HANDICAP OUTCOMES: {bt1} vs {bt2}")
                
        # Check syndicates/clones
        bm1 = a['legs'][0]['bookmaker'].lower()
        bm2 = a['legs'][1]['bookmaker'].lower()
        clones = [{'1xbet', 'melbet', 'betandyou', 'megapari', 'linebet', 'paripesa', 'spinbetter'}]
        for cg in clones:
            if any(c in bm1 for c in cg) and any(c in bm2 for c in cg):
                issues.append(f"SYNDICATE CLONE ARB: {bm1} vs {bm2}")
                
    if issues:
        print("  >>> ISSUES DETECTED:")
        for iss in issues:
            print(f"      [!] {iss}")
    else:
        print("  >>> VALID MATHEMATICAL SUREBET (Clean)")
