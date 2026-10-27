#!/usr/bin/env python3
"""
SmartBet.guru / iGaming Institutional Tactical Neural Network
Multi-Task Feature & Target Extraction Matrix (5 Sports) - Generation V3
Extracts 14-16 tactical parameters per sport and constructs unified 291-dimensional
feature matrices incorporating:
  - Macro Team Dynamics (259 dims: multi-frequency Fourier/cosine form, momentum, rest)
  - Micro Tactical Lineups & Continuous Player Injury Decay (32 dims: 11 starters evaluation,
    recovering rust penalties, goalkeeper weakness, minutes restrictions, positional breakdown)
"""

import os
import sys
import glob
import json
import gzip
import time
import math
from collections import defaultdict, deque
from typing import List, Dict, Any, Tuple, Optional
import numpy as np
import pyarrow.parquet as pq

CACHE_DIR = os.environ.get("CACHE_DIR", "/data/cold-storage/sofascore/cache")
PARQUET_BASE = os.environ.get("PARQUET_BASE", "/data/cold-storage/sofascore/parquet")
LINEUPS_CACHE_FILE = os.environ.get("LINEUPS_CACHE_FILE", "/data/cold-storage/sofascore/cache/lineups_v3_cache.json.gz")
if not os.path.exists(LINEUPS_CACHE_FILE) and os.path.exists(os.path.join(CACHE_DIR, "lineups_v3_cache.json.gz")):
    LINEUPS_CACHE_FILE = os.path.join(CACHE_DIR, "lineups_v3_cache.json.gz")

# ============================================================================
# Target Definitions by Sport
# ============================================================================

SPORT_TARGETS_META = {
    "table-tennis": [
        {"id": 0, "name": "winner_p1", "type": "binary", "desc": "Match Winner Home (P1)"},
        {"id": 1, "name": "set_handicap_minus_1_5", "type": "binary", "desc": "Home -1.5 Sets (3:0 or 3:1)"},
        {"id": 2, "name": "set_handicap_plus_1_5", "type": "binary", "desc": "Away +1.5 Sets (Away wins or 2+ sets)"},
        {"id": 3, "name": "total_sets_over_3_5", "type": "binary", "desc": "Total Sets Over 3.5 (4 or 5 sets)"},
        {"id": 4, "name": "total_sets_over_4_5", "type": "binary", "desc": "Deciding 5th Set (3:2 match)"},
        {"id": 5, "name": "total_points_over_74_5", "type": "binary", "desc": "Total Match Points Over 74.5"},
        {"id": 6, "name": "total_points_over_68_5", "type": "binary", "desc": "Cushion Total Points Over 68.5"},
        {"id": 7, "name": "total_points_under_80_5", "type": "binary", "desc": "Cushion Total Points Under 80.5"},
        {"id": 8, "name": "first_set_winner_p1", "type": "binary", "desc": "1st Set Winner Home (P1)"},
        {"id": 9, "name": "first_set_total_over_18_5", "type": "binary", "desc": "1st Set Points Over 18.5 (11:8+ or deuce)"},
        {"id": 10, "name": "both_win_set", "type": "binary", "desc": "Both Players Win >= 1 Set (No 3:0)"},
        {"id": 11, "name": "clean_sweep_3_0", "type": "binary", "desc": "Clean Sweep 3:0 (Home or Away)"},
        {"id": 12, "name": "points_margin_over_10", "type": "binary", "desc": "Total Points Margin >= 10 (Blowout)"},
        {"id": 13, "name": "high_entropy_clutch", "type": "binary", "desc": "Clutch Game (Points diff <= 4)"}
    ],
    "tennis": [
        {"id": 0, "name": "winner_p1", "type": "binary", "desc": "Match Winner Home (P1)"},
        {"id": 1, "name": "set_handicap_minus_1_5", "type": "binary", "desc": "Home 2:0 in Sets (Clean Win)"},
        {"id": 2, "name": "set_handicap_plus_1_5", "type": "binary", "desc": "Away Wins >= 1 Set (+1.5)"},
        {"id": 3, "name": "total_sets_over_2_5", "type": "binary", "desc": "Total Sets Over 2.5 (3-Set Match)"},
        {"id": 4, "name": "total_games_over_21_5", "type": "binary", "desc": "Total Games Over 21.5"},
        {"id": 5, "name": "total_games_over_19_5", "type": "binary", "desc": "Cushion Games Over 19.5"},
        {"id": 6, "name": "total_games_under_23_5", "type": "binary", "desc": "Cushion Games Under 23.5"},
        {"id": 7, "name": "first_set_winner_p1", "type": "binary", "desc": "1st Set Winner Home"},
        {"id": 8, "name": "first_set_over_9_5", "type": "binary", "desc": "1st Set Games Over 9.5 (6:4, 7:5, 7:6)"},
        {"id": 9, "name": "first_set_tiebreak", "type": "binary", "desc": "1st Set Tiebreak (7:6)"},
        {"id": 10, "name": "both_win_set", "type": "binary", "desc": "Both Win >= 1 Set"},
        {"id": 11, "name": "clean_sweep_2_0", "type": "binary", "desc": "Clean Sweep 2:0"},
        {"id": 12, "name": "games_margin_over_5_5", "type": "binary", "desc": "Game Spread >= 6 Games (Blowout)"},
        {"id": 13, "name": "deciding_set_tiebreak", "type": "binary", "desc": "High Volatility Clutch Match"}
    ],
    "basketball": [
        {"id": 0, "name": "winner_p1", "type": "binary", "desc": "Moneyline Winner Home (with OT)"},
        {"id": 1, "name": "spread_minus_5_5", "type": "binary", "desc": "Home Cover -5.5 Points"},
        {"id": 2, "name": "spread_plus_5_5", "type": "binary", "desc": "Away Cover +5.5 Points"},
        {"id": 3, "name": "spread_minus_9_5", "type": "binary", "desc": "Home Cover -9.5 Points (Strong Favorite)"},
        {"id": 4, "name": "total_points_over_160_5", "type": "binary", "desc": "Total Match Points Over 160.5"},
        {"id": 5, "name": "total_points_over_150_5", "type": "binary", "desc": "Cushion Points Over 150.5"},
        {"id": 6, "name": "total_points_under_170_5", "type": "binary", "desc": "Cushion Points Under 170.5"},
        {"id": 7, "name": "first_half_winner_p1", "type": "binary", "desc": "1st Half Winner Home"},
        {"id": 8, "name": "first_half_over_78_5", "type": "binary", "desc": "1st Half Total Points Over 78.5"},
        {"id": 9, "name": "first_quarter_winner_p1", "type": "binary", "desc": "1st Quarter Winner Home"},
        {"id": 10, "name": "first_quarter_over_38_5", "type": "binary", "desc": "1st Quarter Points Over 38.5"},
        {"id": 11, "name": "clutch_margin_under_6", "type": "binary", "desc": "Clutch Game (Final Margin <= 5)"},
        {"id": 12, "name": "blowout_margin_over_15", "type": "binary", "desc": "Blowout Game (Final Margin >= 15)"},
        {"id": 13, "name": "overtime_flag", "type": "binary", "desc": "Game Went to Overtime (Draw in 4Q)"}
    ],
    "ice-hockey": [
        {"id": 0, "name": "winner_reg_p1", "type": "binary", "desc": "Regulation Time Win Home (P1)"},
        {"id": 1, "name": "winner_reg_px", "type": "binary", "desc": "Regulation Draw (PX -> Overtime)"},
        {"id": 2, "name": "winner_reg_p2", "type": "binary", "desc": "Regulation Time Win Away (P2)"},
        {"id": 3, "name": "moneyline_ot_p1", "type": "binary", "desc": "Moneyline Winner Home (incl. OT/SO)"},
        {"id": 4, "name": "puckline_minus_1_5", "type": "binary", "desc": "Puckline Home -1.5 (2+ Goal Win)"},
        {"id": 5, "name": "puckline_plus_1_5", "type": "binary", "desc": "Puckline Away +1.5"},
        {"id": 6, "name": "total_goals_over_5_5", "type": "binary", "desc": "Total Goals Over 5.5"},
        {"id": 7, "name": "total_goals_over_4_5", "type": "binary", "desc": "Cushion Goals Over 4.5"},
        {"id": 8, "name": "total_goals_under_6_5", "type": "binary", "desc": "Cushion Goals Under 6.5"},
        {"id": 9, "name": "first_period_p1", "type": "binary", "desc": "1st Period Winner Home"},
        {"id": 10, "name": "first_period_over_1_5", "type": "binary", "desc": "1st Period Goals Over 1.5"},
        {"id": 11, "name": "both_teams_score_2plus", "type": "binary", "desc": "Both Teams Score >= 2 Goals"},
        {"id": 12, "name": "shutout_flag", "type": "binary", "desc": "Shutout (Zero goals by either team)"},
        {"id": 13, "name": "high_scoring_over_7_5", "type": "binary", "desc": "High Scoring Festival Over 7.5"}
    ],
    "football": [
        {"id": 0, "name": "winner_1x2_p1", "type": "binary", "desc": "1X2 Win Home (P1)"},
        {"id": 1, "name": "winner_1x2_px", "type": "binary", "desc": "1X2 Draw (PX)"},
        {"id": 2, "name": "winner_1x2_p2", "type": "binary", "desc": "1X2 Win Away (P2)"},
        {"id": 3, "name": "double_chance_1x", "type": "binary", "desc": "Double Chance 1X"},
        {"id": 4, "name": "double_chance_x2", "type": "binary", "desc": "Double Chance X2"},
        {"id": 5, "name": "handicap_minus_1", "type": "binary", "desc": "Handicap Home -1.0 (2+ Goals)"},
        {"id": 6, "name": "handicap_plus_1", "type": "binary", "desc": "Handicap Away +1.0"},
        {"id": 7, "name": "total_goals_over_2_5", "type": "binary", "desc": "Total Goals Over 2.5"},
        {"id": 8, "name": "total_goals_over_1_5", "type": "binary", "desc": "Cushion Goals Over 1.5"},
        {"id": 9, "name": "total_goals_under_3_5", "type": "binary", "desc": "Cushion Goals Under 3.5"},
        {"id": 10, "name": "btts", "type": "binary", "desc": "Both Teams to Score (BTTS Yes)"},
        {"id": 11, "name": "btts_and_over_2_5", "type": "binary", "desc": "Combo: BTTS Yes + Over 2.5"},
        {"id": 12, "name": "first_half_p1", "type": "binary", "desc": "1st Half Winner Home"},
        {"id": 13, "name": "first_half_over_0_5", "type": "binary", "desc": "1st Half Goals Over 0.5"},
        {"id": 14, "name": "first_half_over_1_5", "type": "binary", "desc": "1st Half Goals Over 1.5"},
        {"id": 15, "name": "clean_sheet_home", "type": "binary", "desc": "Home Clean Sheet (0 goals conceded)"}
    ]
}

# ============================================================================
# Post-Injury Recovery & Minutes Restriction Calculator
# ============================================================================

def calculate_player_recovery_discount(desc: str, days: Optional[float], matches_back: int) -> Tuple[float, float]:
    """
    Calculates post-injury degradation and minutes restriction for recovering players.
    Returns: (discount_factor, minutes_weight)
    """
    if not desc and days is None:
        return 0.0, 1.0

    desc_lower = str(desc).lower()
    is_severe = any(w in desc_lower for w in ["broken", "fracture", "leg", "surgery", "cruciate", "acl", "achilles", "ligament", "bone"])
    is_muscle = any(w in desc_lower for w in ["hamstring", "groin", "tear", "thigh", "quad", "calf", "muscle"])

    if is_severe:
        if matches_back == 1 or (days is not None and days <= 14):
            return 0.35, 0.55
        elif matches_back == 2 or (days is not None and days <= 28):
            return 0.20, 0.75
        elif matches_back == 3 or (days is not None and days <= 45):
            return 0.10, 0.90
        return 0.0, 1.0
    elif is_muscle:
        if matches_back == 1 or (days is not None and days <= 10):
            return 0.20, 0.65
        elif matches_back == 2 or (days is not None and days <= 21):
            return 0.10, 0.85
        return 0.0, 1.0
    else:
        if matches_back == 1 or (days is not None and days <= 7):
            return 0.08, 0.85
        return 0.0, 1.0

# ============================================================================
# Target Extraction Engine
# ============================================================================

def safe_int(v, default=0):
    try:
        return int(v) if v is not None else default
    except (ValueError, TypeError):
        return default

def extract_targets(sport: str, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
    if hs_cur is None or as_cur is None or hs_cur < 0 or as_cur < 0:
        return None

    if sport == "table-tennis":
        tot_sets = hs_cur + as_cur
        if tot_sets < 3:
            return None

        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p2_h = safe_int(hs_dict.get("period2"))
        p2_a = safe_int(as_dict.get("period2"))
        p3_h = safe_int(hs_dict.get("period3"))
        p3_a = safe_int(as_dict.get("period3"))
        p4_h = safe_int(hs_dict.get("period4"))
        p4_a = safe_int(as_dict.get("period4"))
        p5_h = safe_int(hs_dict.get("period5"))
        p5_a = safe_int(as_dict.get("period5"))

        tot_pts_h = p1_h + p2_h + p3_h + p4_h + p5_h
        tot_pts_a = p1_a + p2_a + p3_a + p4_a + p5_a
        tot_pts = tot_pts_h + tot_pts_a

        if tot_pts < 30:
            tot_pts = tot_sets * 18.5

        first_set_pts = p1_h + p1_a if (p1_h + p1_a) > 10 else 18.0
        first_set_win = 1.0 if p1_h > p1_a else 0.0

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if (hs_cur - as_cur) >= 2 else 0.0,
            1.0 if (as_cur - hs_cur) >= -1 else 0.0,
            1.0 if tot_sets > 3.5 else 0.0,
            1.0 if tot_sets == 5 else 0.0,
            1.0 if tot_pts > 74.5 else 0.0,
            1.0 if tot_pts > 68.5 else 0.0,
            1.0 if tot_pts < 80.5 else 0.0,
            first_set_win,
            1.0 if first_set_pts > 18.5 else 0.0,
            1.0 if (hs_cur >= 1 and as_cur >= 1) else 0.0,
            1.0 if (hs_cur == 3 and as_cur == 0) or (as_cur == 3 and hs_cur == 0) else 0.0,
            1.0 if abs(tot_pts_h - tot_pts_a) >= 10 else 0.0,
            1.0 if abs(tot_pts_h - tot_pts_a) <= 4 else 0.0
        ]
        return targets

    elif sport == "tennis":
        tot_sets = hs_cur + as_cur
        if tot_sets < 2:
            return None

        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p2_h = safe_int(hs_dict.get("period2"))
        p2_a = safe_int(as_dict.get("period2"))
        p3_h = safe_int(hs_dict.get("period3"))
        p3_a = safe_int(as_dict.get("period3"))

        tot_games_h = p1_h + p2_h + p3_h
        tot_games_a = p1_a + p2_a + p3_a
        tot_games = tot_games_h + tot_games_a
        if tot_games < 12:
            tot_games = tot_sets * 9.5

        first_set_games = p1_h + p1_a if (p1_h + p1_a) >= 6 else 9.0
        tb_flag = 1.0 if (p1_h == 7 and p1_a == 6) or (p1_a == 7 and p1_h == 6) or ("period1TieBreak" in hs_dict) else 0.0

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if (hs_cur == 2 and as_cur == 0) else 0.0,
            1.0 if (as_cur >= 1) else 0.0,
            1.0 if tot_sets > 2.5 else 0.0,
            1.0 if tot_games > 21.5 else 0.0,
            1.0 if tot_games > 19.5 else 0.0,
            1.0 if tot_games < 23.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if first_set_games > 9.5 else 0.0,
            tb_flag,
            1.0 if (hs_cur >= 1 and as_cur >= 1) else 0.0,
            1.0 if (hs_cur == 2 and as_cur == 0) or (as_cur == 2 and hs_cur == 0) else 0.0,
            1.0 if abs(tot_games_h - tot_games_a) >= 6 else 0.0,
            1.0 if tot_sets == 3 and abs(tot_games_h - tot_games_a) <= 2 else 0.0
        ]
        return targets

    elif sport == "basketball":
        tot_pts = hs_cur + as_cur
        if tot_pts < 80:
            return None

        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p2_h = safe_int(hs_dict.get("period2"))
        p2_a = safe_int(as_dict.get("period2"))
        half_h = p1_h + p2_h
        half_a = p1_a + p2_a
        half_tot = half_h + half_a if (half_h + half_a) > 40 else (tot_pts / 2.0)
        q1_tot = p1_h + p1_a if (p1_h + p1_a) > 20 else (tot_pts / 4.0)

        diff = hs_cur - as_cur
        ot_flag = 1.0 if (hs_dict.get("period5") is not None or hs_dict.get("overtime") is not None) else 0.0

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if diff >= 6 else 0.0,
            1.0 if diff >= -5 else 0.0,
            1.0 if diff >= 10 else 0.0,
            1.0 if tot_pts > 160.5 else 0.0,
            1.0 if tot_pts > 150.5 else 0.0,
            1.0 if tot_pts < 170.5 else 0.0,
            1.0 if half_h > half_a else 0.0,
            1.0 if half_tot > 78.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if q1_tot > 38.5 else 0.0,
            1.0 if abs(diff) <= 5 else 0.0,
            1.0 if abs(diff) >= 15 else 0.0,
            ot_flag
        ]
        return targets

    elif sport == "ice-hockey":
        tot_goals = hs_cur + as_cur
        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p1_tot = p1_h + p1_a
        is_draw = 1.0 if hs_cur == as_cur else 0.0
        winner_code = safe_int(hs_dict.get("normaltime"), hs_cur) - safe_int(as_dict.get("normaltime"), as_cur)

        targets = [
            1.0 if winner_code > 0 else 0.0,
            is_draw,
            1.0 if winner_code < 0 else 0.0,
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if (hs_cur - as_cur) >= 2 else 0.0,
            1.0 if (as_cur - hs_cur) >= -1 else 0.0,
            1.0 if tot_goals > 5.5 else 0.0,
            1.0 if tot_goals > 4.5 else 0.0,
            1.0 if tot_goals < 6.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if p1_tot > 1.5 else 0.0,
            1.0 if (hs_cur >= 2 and as_cur >= 2) else 0.0,
            1.0 if (hs_cur == 0 or as_cur == 0) else 0.0,
            1.0 if tot_goals > 7.5 else 0.0
        ]
        return targets

    elif sport == "football":
        tot_goals = hs_cur + as_cur
        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p1_tot = p1_h + p1_a
        btts = 1.0 if (hs_cur > 0 and as_cur > 0) else 0.0

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if hs_cur == as_cur else 0.0,
            1.0 if hs_cur < as_cur else 0.0,
            1.0 if hs_cur >= as_cur else 0.0,
            1.0 if as_cur >= hs_cur else 0.0,
            1.0 if (hs_cur - as_cur) >= 2 else 0.0,
            1.0 if (as_cur - hs_cur) >= 0 else 0.0,
            1.0 if tot_goals > 2.5 else 0.0,
            1.0 if tot_goals > 1.5 else 0.0,
            1.0 if tot_goals < 3.5 else 0.0,
            btts,
            1.0 if (btts > 0 and tot_goals > 2.5) else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if p1_tot > 0.5 else 0.0,
            1.0 if p1_tot > 1.5 else 0.0,
            1.0 if as_cur == 0 else 0.0
        ]
        return targets

    return None

# ============================================================================
# Macro Dynamics Vector Extraction Engine (64 dims)
# ============================================================================

SINE_FREQS = [(d - 7) * 0.3 for d in range(8, 64)]

def extract_form_vector(history: List[Tuple[int, int, int, int]], last_ts: int, cur_ts: int, has_draws: bool) -> List[float]:
    vec = [0.0] * 64
    if not history:
        return vec
    n = len(history)
    wins = sum(1 for h in history if h[3] == 1)
    draws = sum(1 for h in history if h[3] == 0)
    losses = sum(1 for h in history if h[3] == -1)
    gf = sum(h[1] for h in history)
    ga = sum(h[2] for h in history)

    vec[0] = wins / n
    vec[1] = draws / n if has_draws else 0.0
    vec[2] = losses / n
    vec[3] = (gf / n) / 10.0
    vec[4] = (ga / n) / 10.0
    vec[5] = ((gf - ga) / n) / 10.0
    vec[6] = min(1.0, n / 10.0)
    vec[7] = min(1.0, max(0.0, (cur_ts - last_ts) / 86400000.0 / 14.0)) if (last_ts > 0 and cur_ts > last_ts) else 0.5

    ratio = gf / max(1, ga)
    win_term = (wins / n) * 3.1415
    for idx, freq in enumerate(SINE_FREQS):
        vec[8 + idx] = math.sin(ratio * freq + win_term) * 0.5 + 0.5

    norm = math.sqrt(sum(x * x for x in vec))
    if norm > 1e-6:
        vec = [x / norm for x in vec]
    return vec

def extract_score_subdicts(raw_j: Optional[str]) -> Tuple[Dict[str, Any], Dict[str, Any]]:
    if not raw_j:
        return {}, {}
    hs_dict = {}
    as_dict = {}
    try:
        h_pos = raw_j.find('"homeScore"')
        if h_pos != -1:
            h_open = raw_j.find('{', h_pos)
            h_close = raw_j.find('}', h_open)
            if h_open != -1 and h_close != -1:
                hs_dict = json.loads(raw_j[h_open:h_close + 1])
        a_pos = raw_j.find('"awayScore"')
        if a_pos != -1:
            a_open = raw_j.find('{', a_pos)
            a_close = raw_j.find('}', a_open)
            if a_open != -1 and a_close != -1:
                as_dict = json.loads(raw_j[a_open:a_close + 1])
    except Exception:
        pass
    return hs_dict, as_dict

# ============================================================================
# Micro Tactical Lineup & Injury Decay Feature Extractor (32 dims)
# ============================================================================

DEFAULT_LINEUP_FEATS = [
    0.0, 6.8, 6.2, 7.5, 0.0, 0.0, 6.8, 0.0, 0.0, 0.0, 0.0, 0.0,
    0.0, 6.8, 6.2, 7.5, 0.0, 0.0, 6.8, 0.0, 0.0, 0.0, 0.0, 0.0,
    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
]

def extract_lineup_and_injury_features(
    eid: int,
    ts: int,
    home: str,
    away: str,
    lineups_cache: Dict[int, Any],
    team_player_medical: Dict[str, Dict[str, Any]]
) -> List[float]:
    """
    Constructs 32-dim tactical vector capturing starter effective ratings,
    post-injury decay penalties, minutes restrictions, goalkeeper status,
    and missing player impacts.
    """
    match_data = lineups_cache.get(eid)
    if not match_data:
        return DEFAULT_LINEUP_FEATS

    def process_team_lineup(team_name: str, starters_data: Optional[List[Dict[str, Any]]], missing_data: Optional[List[Dict[str, Any]]]) -> Tuple[List[float], int]:
        if not starters_data:
            # Imputed neutral defaults for matches without verified lineups
            return [
                0.0,   # has_lineup
                6.8,   # avg_effective_rating
                6.2,   # min_rating
                7.5,   # max_rating
                0.0,   # total_injury_rust_decay
                0.0,   # restricted_minutes_count
                6.8,   # gk_effective_rating
                0.0,   # gk_injury_decay
                0.0,   # def_injury_decay
                0.0,   # mid_injury_decay
                0.0,   # att_injury_decay
                0.0    # missing_players_count
            ], 0

        # Register any missing/injured players into rolling medical registry
        med_registry = team_player_medical[team_name]
        for m in (missing_data or []):
            pname = m.get("name") or m.get("slug")
            if pname:
                med_registry[pname] = {
                    "injury": m.get("reason") or "Injury",
                    "ts": ts,
                    "status": "injured",
                    "matches_back": 0
                }

        effective_ratings = []
        total_rust_decay = 0.0
        restricted_count = 0.0
        gk_rating = 6.8
        gk_decay = 0.0
        def_decay = 0.0
        mid_decay = 0.0
        att_decay = 0.0

        for p in starters_data:
            pname = p.get("name") or p.get("slug") or "Player"
            pos = str(p.get("pos") or "").upper()
            try:
                base_r = float(p.get("rating") or 6.8)
            except (ValueError, TypeError):
                base_r = 6.8

            discount = 0.0
            mins_weight = 1.0

            if pname in med_registry and med_registry[pname]["status"] == "injured":
                med = med_registry[pname]
                days = (ts - med["ts"]) / 86400000.0 if ts > med["ts"] else 0.0
                matches_back = med["matches_back"] + 1
                med["matches_back"] = matches_back
                discount, mins_weight = calculate_player_recovery_discount(med["injury"], days, matches_back)

                if matches_back >= 3 and days > 45.0:
                    med["status"] = "recovered"

            eff_r = (base_r * (1.0 - discount) * mins_weight) + (6.2 * (1.0 - mins_weight))
            effective_ratings.append(eff_r)

            if discount > 0.0:
                total_rust_decay += discount
                if mins_weight < 1.0:
                    restricted_count += 1.0

            # Positional categorization
            if any(g in pos for g in ["G", "GK", "GOALKEEPER"]):
                gk_rating = eff_r
                gk_decay = discount
            elif any(d in pos for d in ["D", "CB", "LB", "RB", "DEF"]):
                def_decay += discount
            elif any(m in pos for m in ["M", "CM", "CDM", "CAM", "MID"]):
                mid_decay += discount
            elif any(a in pos for a in ["F", "FW", "ST", "RW", "LW", "ATT"]):
                att_decay += discount

        avg_r = (sum(effective_ratings) / len(effective_ratings)) if effective_ratings else 6.8
        min_r = min(effective_ratings) if effective_ratings else 6.2
        max_r = max(effective_ratings) if effective_ratings else 7.5
        missing_count = float(len(missing_data or []))

        features = [
            1.0,                    # has_lineup
            round(avg_r, 3),        # avg_effective_rating
            round(min_r, 3),        # min_rating
            round(max_r, 3),        # max_rating
            round(total_rust_decay, 3),  # total_injury_rust_decay
            restricted_count,       # restricted_minutes_count
            round(gk_rating, 3),    # gk_effective_rating
            round(gk_decay, 3),     # gk_injury_decay
            round(def_decay, 3),    # def_injury_decay
            round(mid_decay, 3),    # mid_injury_decay
            round(att_decay, 3),    # att_injury_decay
            missing_count           # missing_players_count
        ]
        return features, len(starters_data)

    if match_data:
        h_feats, _ = process_team_lineup(home, match_data.get("home_starters"), match_data.get("home_missing"))
        a_feats, _ = process_team_lineup(away, match_data.get("away_starters"), match_data.get("away_missing"))
    else:
        h_feats, _ = process_team_lineup(home, None, None)
        a_feats, _ = process_team_lineup(away, None, None)

    # Tactical Interactions & Differential Block (8 dims)
    diff_avg_r = h_feats[1] - a_feats[1]
    diff_rust = h_feats[4] - a_feats[4]
    diff_gk = h_feats[6] - a_feats[6]
    diff_def_mid = (h_feats[8] + h_feats[9]) - (a_feats[8] + a_feats[9])
    diff_att = h_feats[10] - a_feats[10]
    diff_missing = h_feats[11] - a_feats[11]
    tactical_asymmetry = (h_feats[10] - a_feats[8]) - (a_feats[10] - h_feats[8])
    both_confirmed = h_feats[0] * a_feats[0]

    inter_feats = [
        round(diff_avg_r, 3),
        round(diff_rust, 3),
        round(diff_gk, 3),
        round(diff_def_mid, 3),
        round(diff_att, 3),
        round(diff_missing, 3),
        round(tactical_asymmetry, 3),
        both_confirmed
    ]

    # Total = 12 (home) + 12 (away) + 8 (interaction) = 32 dimensions
    return h_feats + a_feats + inter_feats

# ============================================================================
# Main Processing Pipeline
# ============================================================================

def process_sport_multitask(sport: str, max_files: Optional[int] = None):
    print("\n" + "=" * 80)
    print(f" 🚀 EXTRACTING MULTI-TASK MATRIX V3 (291 DIMS): {sport.upper()}")
    print("=" * 80, flush=True)

    # Load Lineups Cache if available
    lineups_cache = {}
    if os.path.exists(LINEUPS_CACHE_FILE):
        print(f" Ingesting verified lineups cache: {LINEUPS_CACHE_FILE}...", flush=True)
        t_cache0 = time.time()
        try:
            with gzip.open(LINEUPS_CACHE_FILE, "rt", encoding="utf-8") as f:
                raw_c = json.load(f)
                lineups_cache = {int(k): v for k, v in raw_c.items()}
            print(f" Loaded {len(lineups_cache):,} match protocols into memory in {time.time() - t_cache0:.2f}s.", flush=True)
        except Exception as e:
            print(f" [Warning] Could not load lineups cache: {e}. Running with imputed defaults.", flush=True)
    else:
        print(" [Notice] lineups_v3_cache.json.gz not found. Lineup coordinates will use imputed defaults.", flush=True)

    pattern = os.path.join(PARQUET_BASE, f"sport={sport}/**/*.parquet")
    files = sorted(glob.glob(pattern, recursive=True))
    if not files:
        print(f" [Warning] No parquet files found for {sport} in {pattern}")
        return

    if max_files:
        files = files[-max_files:]

    print(f" Ingesting {len(files):,} parquet partitions...", flush=True)
    t0 = time.time()

    raw_events = []
    for f in files:
        try:
            t = pq.read_table(f, columns=[
                "event_id", "start_timestamp", "home_team_name", "away_team_name",
                "home_score_current", "away_score_current", "raw_event_json"
            ])
            c_eid = t.column("event_id").to_pylist()
            c_ts = t.column("start_timestamp").to_pylist()
            c_ht = t.column("home_team_name").to_pylist()
            c_at = t.column("away_team_name").to_pylist()
            c_hs = t.column("home_score_current").to_pylist()
            c_as = t.column("away_score_current").to_pylist()
            c_raw = t.column("raw_event_json").to_pylist()

            for eid, ts, ht, at, hs, ascore, raw_j in zip(c_eid, c_ts, c_ht, c_at, c_hs, c_as, c_raw):
                if not ht or not at or ts is None or ts <= 0 or hs is None or ascore is None:
                    continue
                raw_events.append((safe_int(eid), int(ts), str(ht), str(at), int(hs), int(ascore), raw_j))
        except Exception:
            continue

    print(f" Loaded {len(raw_events):,} events in {time.time() - t0:.1f}s. Chronological sorting...", flush=True)
    raw_events.sort(key=lambda x: x[1])

    team_history = defaultdict(lambda: deque(maxlen=10))
    team_last_ts = defaultdict(int)
    team_player_medical = defaultdict(dict)
    has_draws = (sport in ["football", "ice-hockey"])

    X_list = []
    Y_list = []
    lineups_matched = 0
    dropped = 0

    for eid, ts, home, away, hs, ascore, raw_j in raw_events:
        h_hist = team_history[home]
        a_hist = team_history[away]

        if len(h_hist) >= 2 and len(a_hist) >= 2:
            hs_dict, as_dict = extract_score_subdicts(raw_j)
            target_vec = extract_targets(sport, hs_dict, as_dict, hs, ascore)
            if target_vec is not None:
                # 1. Macro Dynamics Form Vector (259 dims)
                v_h = extract_form_vector(h_hist, team_last_ts[home], ts, has_draws)
                v_a = extract_form_vector(a_hist, team_last_ts[away], ts, has_draws)
                diff = [h - a for h, a in zip(v_h, v_a)]
                prod = [h * a for h, a in zip(v_h, v_a)]
                ctx = [v_h[0] - v_a[0], v_h[5] - v_a[5], 1.0]
                macro_feat = v_h + v_a + diff + prod + ctx  # 259 dims

                # 2. Micro Tactical Lineup & Continuous Injury Decay Block (32 dims)
                lineup_feat = extract_lineup_and_injury_features(eid, ts, home, away, lineups_cache, team_player_medical)
                if lineup_feat[0] > 0.0 or lineup_feat[12] > 0.0:
                    lineups_matched += 1

                # Combined V3 Feature Vector = 259 + 32 = 291 dimensions
                feat = macro_feat + lineup_feat
                X_list.append(feat)
                Y_list.append(target_vec)
            else:
                dropped += 1

        # Update rolling state
        h_res = 1 if hs > ascore else (-1 if hs < ascore else 0)
        a_res = 1 if ascore > hs else (-1 if ascore < hs else 0)
        team_history[home].append((ts, hs, ascore, h_res))
        team_history[away].append((ts, ascore, hs, a_res))
        team_last_ts[home] = ts
        team_last_ts[away] = ts

    X_arr = np.array(X_list, dtype=np.float32)
    Y_arr = np.array(Y_list, dtype=np.float32)

    print(f" Extracted Multi-Task Matrix V3 for {sport.upper()}:")
    print(f"   * Matches with full vectors & targets: {len(X_arr):,}")
    print(f"   * Verified Lineups Matched: {lineups_matched:,}")
    print(f"   * Input Features Shape: {X_arr.shape} (291 dimensions: 259 macro + 32 lineup/injury decay)")
    print(f"   * Target Matrix Shape:  {Y_arr.shape} ({Y_arr.shape[1]} tactical targets)")

    os.makedirs(CACHE_DIR, exist_ok=True)
    out_x = os.path.join(CACHE_DIR, f"{sport}_multitask_features_X.npy")
    out_y = os.path.join(CACHE_DIR, f"{sport}_multitask_targets_Y.npy")
    out_meta = os.path.join(CACHE_DIR, f"{sport}_targets_meta.json")

    np.save(out_x, X_arr)
    np.save(out_y, Y_arr)
    with open(out_meta, "w", encoding="utf-8") as f:
        json.dump(SPORT_TARGETS_META.get(sport, []), f, indent=2, ensure_ascii=False)

    print(f" [OK] Successfully saved V3 matrices to {CACHE_DIR}!\n", flush=True)

def main():
    import argparse
    parser = argparse.ArgumentParser(description="Multi-Task Matrix V3 Extractor")
    parser.add_argument("--sport", type=str, default=os.environ.get("SPORT", "all"),
                        help="Specific sport to process or 'all'")
    parser.add_argument("--max-files", type=int, default=None,
                        help="Limit number of recent parquet partitions (optional)")
    args = parser.parse_args()

    all_sports = ["table-tennis", "tennis", "basketball", "ice-hockey", "football"]
    target_sports = all_sports if args.sport == "all" else [args.sport]

    for s in target_sports:
        process_sport_multitask(s, max_files=args.max_files)
    print(f"\n🏁 MULTI-TASK MATRICES (291 DIMS) FOR [{','.join(target_sports)}] EXTRACTED SUCCESSFULLY!")

if __name__ == "__main__":
    main()
