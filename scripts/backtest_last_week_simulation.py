#!/usr/bin/env python3
"""
Backtest & Simulation of Last Week Matches (October 20-27, 2026):
Detailed Probabilistic Evaluation:
1. Brier Score (Mean Squared Probability Error - Lower is Better)
2. Draw Calibration & Value Capture
3. Fatigue Delta Impact on 2nd Half / Goal Concession
"""

import math
import json
import requests
from datetime import datetime

CH_URL = "http://clickhouse.igaming-analytics.svc.cluster.local:8123"
CH_AUTH = ("default", "clickhouse")

def ch_query(query):
    r = requests.post(
        CH_URL,
        auth=CH_AUTH,
        params={"query": query + " FORMAT JSON"},
        timeout=30
    )
    if r.status_code != 200:
        raise Exception(f"CH Error HTTP {r.status_code}: {r.text[:300]}")
    return r.json()["data"]

def run_simulation():
    print(f"[{datetime.now()}] Fetching finished matches from last week (2026-10-20 to 2026-10-27)...")
    
    matches_sql = """
    WITH ordered_matches AS (
        SELECT 
            mdm_match_id,
            home_team_id,
            away_team_id,
            tournament_id,
            match_time,
            home_score,
            away_score,
            winner,
            home_xg,
            away_xg,
            lagInFrame(match_time) OVER (PARTITION BY home_team_id ORDER BY match_time) AS prev_home_time,
            lagInFrame(match_time) OVER (PARTITION BY away_team_id ORDER BY match_time) AS prev_away_time
        FROM igaming_dds.football_match_fact
        WHERE match_time >= '2026-09-01'
    )
    SELECT 
        mdm_match_id,
        home_team_id,
        away_team_id,
        tournament_id,
        match_time,
        home_score,
        away_score,
        winner,
        home_xg,
        away_xg,
        round(dateDiff('second', prev_home_time, match_time) / 86400.0, 1) AS home_rest_days,
        round(dateDiff('second', prev_away_time, match_time) / 86400.0, 1) AS away_rest_days
    FROM ordered_matches
    WHERE match_time >= '2026-10-20 00:00:00' 
      AND match_time <= '2026-10-27 23:59:59'
      AND (home_score > 0 OR away_score > 0 OR winner = 'DRAW')
    ORDER BY match_time ASC
    LIMIT 300
    """
    
    rows = ch_query(matches_sql)
    print(f"[{datetime.now()}] Loaded {len(rows)} matches with verified ground truth.")

    base_brier_sum = 0.0
    enh_brier_sum = 0.0
    
    total_draws = 0
    draw_bets_placed = 0
    draw_bets_won = 0
    draw_units_staked = 0.0
    draw_units_returned = 0.0
    
    fatigue_matches = 0
    fatigue_favored_won = 0

    total_evaluated = 0

    for m in rows:
        h_score = m["home_score"]
        a_score = m["away_score"]
        actual_winner = m["winner"]
        h_xg = float(m["home_xg"] or 1.2)
        a_xg = float(m["away_xg"] or 1.0)
        
        h_rest = float(m["home_rest_days"] or 7.0)
        a_rest = float(m["away_rest_days"] or 7.0)
        h_rest = max(2.0, min(14.0, h_rest)) if h_rest > 0 else 7.0
        a_rest = max(2.0, min(14.0, a_rest)) if a_rest > 0 else 7.0
        rest_delta = round(h_rest - a_rest, 1)

        diff_xg = h_xg - a_xg
        
        # Ground truth vector [is_home, is_draw, is_away]
        y_h = 1.0 if actual_winner == "HOME" else 0.0
        y_d = 1.0 if actual_winner == "DRAW" else 0.0
        y_a = 1.0 if actual_winner == "AWAY" else 0.0
        if actual_winner == "DRAW":
            total_draws += 1

        # 1. BASELINE MODEL PROBABILITIES
        p_home_base = 1.0 / (1.0 + math.exp(-1.1 * diff_xg - 0.2))
        p_away_base = 1.0 / (1.0 + math.exp(1.1 * diff_xg - 0.2))
        p_draw_base = 0.25 # standard market prior for draw
        tot_b = p_home_base + p_draw_base + p_away_base
        p_h_b, p_d_b, p_a_b = p_home_base/tot_b, p_draw_base/tot_b, p_away_base/tot_b

        # Baseline Brier Score: (p_h - y_h)^2 + (p_d - y_d)^2 + (p_a - y_a)^2
        brier_base = (p_h_b - y_h)**2 + (p_d_b - y_d)**2 + (p_a_b - y_a)**2
        base_brier_sum += brier_base

        # 2. ENHANCED MODEL PROBABILITIES
        # A. Rest Delta Adjustment
        fatigue_factor = 0.0
        if rest_delta >= 3.0:
            fatigue_factor = 0.38 * (rest_delta / 5.0)
            fatigue_matches += 1
            if actual_winner == "HOME": fatigue_favored_won += 1
        elif rest_delta <= -3.0:
            fatigue_factor = -0.38 * (abs(rest_delta) / 5.0)
            fatigue_matches += 1
            if actual_winner == "AWAY": fatigue_favored_won += 1

        # B. Draw Collusion & Balance Incentive
        draw_boost = 0.0
        if abs(diff_xg) <= 0.30:
            draw_boost = 0.12 # teams equally matched in tactical rigidity
        if abs(diff_xg) <= 0.10:
            draw_boost = 0.20 # tight stalemate incentive

        # C. Quality / Plan B Reversal
        comeback_boost = 0.0
        if diff_xg > 0.7:
            comeback_boost = 0.12

        adj_diff = diff_xg + fatigue_factor + comeback_boost
        p_h_e_raw = 1.0 / (1.0 + math.exp(-1.1 * adj_diff - 0.2))
        p_a_e_raw = 1.0 / (1.0 + math.exp(1.1 * adj_diff - 0.2))
        p_d_e_raw = 0.25 + draw_boost
        tot_e = p_h_e_raw + p_d_e_raw + p_a_e_raw
        p_h_e, p_d_e, p_a_e = p_h_e_raw/tot_e, p_d_e_raw/tot_e, p_away_e/tot_e if 'away_e' in locals() else (p_a_e_raw/tot_e)

        # Enhanced Brier Score
        brier_enh = (p_h_e - y_h)**2 + (p_d_e - y_d)**2 + (p_a_e - y_a)**2
        enh_brier_sum += brier_enh

        # 3. Value Bet Simulation on Draw (Odds implied = 1 / p_d_b)
        market_odds_draw = 1.0 / max(0.15, p_d_b)
        if p_d_e > 0.28: # Model flags elevated draw probability / value
            draw_bets_placed += 1
            draw_units_staked += 1.0
            if actual_winner == "DRAW":
                draw_bets_won += 1
                draw_units_returned += market_odds_draw

        total_evaluated += 1

    brier_base_avg = base_brier_sum / total_evaluated
    brier_enh_avg = enh_brier_sum / total_evaluated
    draw_roi = ((draw_units_returned - draw_units_staked) / draw_units_staked * 100.0) if draw_units_staked > 0 else 0.0

    print(f"\n=======================================================")
    print(f"📊 PROBABILISTIC BACKTEST & SIMULATION (LAST WEEK)")
    print(f"=======================================================")
    print(f"Total Matches Analyzed: {total_evaluated}")
    print(f"Total Actual Draws: {total_draws} ({total_draws/total_evaluated*100:.1f}%)")
    print(f"Baseline Brier Score: {brier_base_avg:.4f}")
    print(f"Enhanced Brier Score: {brier_enh_avg:.4f} (Calibrated Error Reduction: {(brier_base_avg - brier_enh_avg)/brier_base_avg*100:+.2f}%)")
    print(f"\n--- FATIGUE / REST DELTA IMPACT (Δ >= 3 days) ---")
    print(f"Matches with high fatigue asymmetry: {fatigue_matches}")
    print(f"Win rate of more rested team: {fatigue_favored_won}/{fatigue_matches} ({fatigue_favored_won/fatigue_matches*100:.1f}%)" if fatigue_matches > 0 else "N/A")
    print(f"\n--- VALUE BETTING ON DETECTED DRAWS (P_draw > 0.28) ---")
    print(f"Bets Placed on Draw: {draw_bets_placed}")
    win_rate = (draw_bets_won / draw_bets_placed * 100.0) if draw_bets_placed > 0 else 0.0
    print(f"Bets Won: {draw_bets_won} (Win Rate: {win_rate:.1f}%)")
    print(f"Units Staked: {draw_units_staked:.1f}u | Returned: {draw_units_returned:.1f}u")
    print(f"Calculated ROI on Draws: {draw_roi:+.1f}%")

if __name__ == "__main__":
    run_simulation()
