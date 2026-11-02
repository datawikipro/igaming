import psycopg2
import requests
import json
import time

print("Step 1: Truncating ai_strategy_vote and deduplicating recommendations to v0.3.3.1.1...")
conn = psycopg2.connect('postgresql://postgres:postgres@10.108.82.251:5432/igaming_analytics')
conn.autocommit = True
cur = conn.cursor()

cur.execute("TRUNCATE TABLE ai_strategy_vote")
print("Truncated ai_strategy_vote.")

cur.execute("""
    DELETE FROM match_ai_recommendation 
    WHERE id NOT IN (
        SELECT DISTINCT ON (match_id) id
        FROM match_ai_recommendation
        ORDER BY match_id, created_at DESC
    )
""")
print(f"Purged {cur.rowcount} duplicate historical recommendations.")

cur.execute("UPDATE match_ai_recommendation SET model_version = 'v0.3.3.1.1'")
print(f"Updated {cur.rowcount} recommendations to v0.3.3.1.1.")

print("\nStep 2: Triggering complete recalculation of all strategy votes via Java API...")
t0 = time.time()
resp = requests.post("http://100.78.183.101:30895/api/v1/ai-analyst/strategies/recalculate?forceAll=false", timeout=180)
print(f"Recalculate API response ({time.time() - t0:.1f}s): {resp.status_code}")
try:
    print("Response JSON:", resp.json())
except Exception as e:
    print("Response text:", resp.text[:200])

print("\nStep 3: Fetching new strategy performance across all 7 strategies...")
resp_perf = requests.get("http://100.78.183.101:30895/api/v1/ai-analyst/strategies/performance")
data = resp_perf.json()

print("\n" + "=" * 90)
print(f"{'Strategy Name':<20} | {'Winrate':<8} | {'Won/Lost':<10} | {'Turnover':<12} | {'PnL (RUB)':<12} | {'ROI %':<8} | Status")
print("=" * 90)

total_pnl = 0.0
for s in data:
    name = s.get("strategyName", s.get("strategySlug"))
    winrate = f"{s.get('winratePct', 0):.1f}%"
    won_lost = f"{s.get('wonCount', 0)}/{s.get('lostCount', 0)}"
    turnover = f"{s.get('turnoverRub', 0):.0f} ₽"
    pnl = s.get("totalPnlRub", 0)
    total_pnl += pnl
    pnl_str = f"{pnl:+.2f} ₽"
    roi = f"{s.get('roiPct', 0):+.1f}%"
    status = "🟢 PROFIT" if pnl > 0 else ("🔴 LOSS" if pnl < 0 else "⚪ EVEN")
    print(f"{name:<20} | {winrate:<8} | {won_lost:<10} | {turnover:<12} | {pnl_str:<12} | {roi:<8} | {status}")

print("=" * 90)
print(f"TOTAL ECOSYSTEM PnL: {total_pnl:+.2f} RUB")
