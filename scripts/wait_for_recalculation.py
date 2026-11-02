import time
import requests
import psycopg2

def main():
    print("Connecting to DB to monitor strategy votes recalculation...")
    conn = psycopg2.connect('postgresql://postgres:postgres@10.108.82.251:5432/igaming_analytics')
    conn.autocommit = True
    cur = conn.cursor()

    last_count = -1
    stable_cycles = 0

    while True:
        cur.execute("SELECT count(*) FROM ai_strategy_vote")
        cnt = cur.fetchone()[0]
        cur.execute("SELECT count(*) FROM match_ai_recommendation")
        recs_cnt = cur.fetchone()[0]

        print(f"Current votes count: {cnt} (Expected ~ {recs_cnt * 7})")

        if cnt > 0 and cnt == last_count:
            stable_cycles += 1
            if stable_cycles >= 3:
                print("Recalculation votes count stabilized!")
                break
        else:
            stable_cycles = 0

        last_count = cnt
        time.sleep(15)

    print("\nFetching final strategy performance...")
    resp = requests.get("http://100.78.183.101:30895/api/v1/ai-analyst/strategies/performance", timeout=30)
    data = resp.json()

    print("\n" + "=" * 95)
    print(f"{'Strategy Name':<20} | {'Winrate':<8} | {'Won/Lost':<10} | {'Turnover':<14} | {'PnL (RUB)':<14} | {'ROI %':<8} | Status")
    print("=" * 95)

    total_pnl = 0.0
    total_turnover = 0.0
    for s in data:
        name = s.get("strategyName", s.get("strategySlug"))
        winrate = f"{s.get('winratePct', 0):.1f}%"
        won_lost = f"{s.get('wonCount', 0)}/{s.get('lostCount', 0)}"
        turnover = s.get('turnoverRub', 0)
        total_turnover += turnover
        turnover_str = f"{turnover:,.0f} ₽"
        pnl = s.get("totalPnlRub", 0)
        total_pnl += pnl
        pnl_str = f"{pnl:+,.2f} ₽"
        roi = f"{s.get('roiPct', 0):+.1f}%"
        status = "🟢 PROFIT" if pnl > 0 else ("🔴 LOSS" if pnl < 0 else "⚪ EVEN")
        print(f"{name:<20} | {winrate:<8} | {won_lost:<10} | {turnover_str:<14} | {pnl_str:<14} | {roi:<8} | {status}")

    print("=" * 95)
    print(f"TOTAL TURNOVER: {total_turnover:,.2f} RUB")
    print(f"TOTAL ECOSYSTEM PnL: {total_pnl:+,.2f} RUB (Status: {'🟢 PROFIT' if total_pnl > 0 else '🔴 LOSS'})")

if __name__ == "__main__":
    main()
