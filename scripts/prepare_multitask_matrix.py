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
import base64
import urllib.request
import pickle
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
    "cs2": [
        {"id": 0, "name": "winner_p1", "type": "binary", "desc": "Match Winner Home (P1)"},
        {"id": 1, "name": "map_handicap_minus_1_5", "type": "binary", "desc": "Home -1.5 Maps (Clean Sweep)"},
        {"id": 2, "name": "map_handicap_plus_1_5", "type": "binary", "desc": "Away Wins >= 1 Map (+1.5)"},
        {"id": 3, "name": "total_maps_over_2_5", "type": "binary", "desc": "Total Maps Over 2.5 (3-Map Decider)"},
        {"id": 4, "name": "map1_winner_p1", "type": "binary", "desc": "Map 1 Winner Home"},
        {"id": 5, "name": "map1_rounds_over_21_5", "type": "binary", "desc": "Map 1 Total Rounds Over 21.5"},
        {"id": 6, "name": "map1_rounds_over_19_5", "type": "binary", "desc": "Cushion Map 1 Rounds Over 19.5"},
        {"id": 7, "name": "map1_rounds_under_23_5", "type": "binary", "desc": "Cushion Map 1 Rounds Under 23.5"},
        {"id": 8, "name": "map1_overtime_flag", "type": "binary", "desc": "Map 1 Went to Overtime (12-12)"},
        {"id": 9, "name": "both_win_map", "type": "binary", "desc": "Both Teams Win >= 1 Map"},
        {"id": 10, "name": "map1_handicap_minus_3_5", "type": "binary", "desc": "Map 1 Rounds P1 -3.5"},
        {"id": 11, "name": "clean_sweep_2_0", "type": "binary", "desc": "Clean Sweep 2:0 (Home or Away)"},
        {"id": 12, "name": "decider_map_played", "type": "binary", "desc": "Decider Map 3 Played"},
        {"id": 13, "name": "high_entropy_clutch", "type": "binary", "desc": "Clutch Map 1 (Rounds Diff <= 3)"}
    ],
    "dota2": [
        {"id": 0, "name": "winner_p1", "type": "binary", "desc": "Match Winner Home (P1)"},
        {"id": 1, "name": "map_handicap_minus_1_5", "type": "binary", "desc": "Home -1.5 Maps (Clean Sweep)"},
        {"id": 2, "name": "map_handicap_plus_1_5", "type": "binary", "desc": "Away Wins >= 1 Map (+1.5)"},
        {"id": 3, "name": "total_maps_over_2_5", "type": "binary", "desc": "Total Maps Over 2.5 (3-Map Decider)"},
        {"id": 4, "name": "map1_winner_p1", "type": "binary", "desc": "Map 1 Winner Home"},
        {"id": 5, "name": "both_win_map", "type": "binary", "desc": "Both Teams Win >= 1 Map"},
        {"id": 6, "name": "clean_sweep_2_0", "type": "binary", "desc": "Clean Sweep 2:0"},
        {"id": 7, "name": "decider_map_played", "type": "binary", "desc": "Decider Map 3 Played"},
        {"id": 8, "name": "home_wins_at_least_1_map", "type": "binary", "desc": "Home Takes >= 1 Map"},
        {"id": 9, "name": "away_wins_at_least_1_map", "type": "binary", "desc": "Away Takes >= 1 Map"},
        {"id": 10, "name": "clutch_2_1_home_win", "type": "binary", "desc": "2:1 Series Win Home"},
        {"id": 11, "name": "clutch_1_2_away_win", "type": "binary", "desc": "1:2 Series Win Away"},
        {"id": 12, "name": "blowout_2_0_home", "type": "binary", "desc": "2:0 Blowout Home"},
        {"id": 13, "name": "blowout_0_2_away", "type": "binary", "desc": "0:2 Blowout Away"}
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
    Uses continuous exponential decay (UEFA Elite Injury Study) without artificial step discontinuities.
    Returns: (discount_factor, minutes_weight)
    """
    if not desc and days is None:
        return 0.0, 1.0

    desc_lower = str(desc).lower()
    is_severe = any(w in desc_lower for w in ["broken", "fracture", "leg", "surgery", "cruciate", "acl", "achilles", "ligament", "bone"])
    is_muscle = any(w in desc_lower for w in ["hamstring", "groin", "tear", "thigh", "quad", "calf", "muscle"])

    d = float(days) if (days is not None and days >= 0) else 14.0
    m = max(0, matches_back)

    if is_severe:
        # Severe structural injury: half-life ~ 22 days, attenuated by matches played
        decay = 0.45 * math.exp(-d / 22.0) / (1.0 + 0.65 * m)
        mins_weight = min(1.0, 0.45 + 0.55 * (1.0 - math.exp(-d / 18.0)) * (1.0 + 0.3 * min(3, m)))
        return round(max(0.0, min(0.40, decay)), 3), round(max(0.40, min(1.0, mins_weight)), 3)
    elif is_muscle:
        # Muscle strain: half-life ~ 12 days
        decay = 0.30 * math.exp(-d / 12.0) / (1.0 + 0.75 * m)
        mins_weight = min(1.0, 0.55 + 0.45 * (1.0 - math.exp(-d / 10.0)) * (1.0 + 0.4 * min(3, m)))
        return round(max(0.0, min(0.30, decay)), 3), round(max(0.50, min(1.0, mins_weight)), 3)
    else:
        # Minor knock
        decay = 0.12 * math.exp(-d / 7.0) / (1.0 + 1.0 * m)
        mins_weight = min(1.0, 0.75 + 0.25 * (1.0 - math.exp(-d / 5.0)))
        return round(max(0.0, min(0.12, decay)), 3), round(max(0.70, min(1.0, mins_weight)), 3)

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

    elif sport == "cs2":
        tot_maps = hs_cur + as_cur
        if tot_maps < 1:
            return None
        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p1_tot = p1_h + p1_a
        is_ot = 1.0 if (p1_h >= 13 and p1_a >= 12) or (p1_a >= 13 and p1_h >= 12) or p1_tot >= 25 else 0.0

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if (hs_cur - as_cur) >= 2 else 0.0,
            1.0 if (as_cur - hs_cur) >= -1 else 0.0,
            1.0 if tot_maps > 2.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if p1_tot > 21.5 else 0.0,
            1.0 if p1_tot > 19.5 else 0.0,
            1.0 if p1_tot < 23.5 else 0.0,
            is_ot,
            1.0 if (hs_cur >= 1 and as_cur >= 1) else 0.0,
            1.0 if (p1_h - p1_a) >= 4 else 0.0,
            1.0 if (hs_cur == 2 and as_cur == 0) or (as_cur == 2 and hs_cur == 0) else 0.0,
            1.0 if tot_maps == 3 else 0.0,
            1.0 if abs(p1_h - p1_a) <= 3 else 0.0
        ]
        return targets

    elif sport == "dota2":
        tot_maps = hs_cur + as_cur
        if tot_maps < 1:
            return None
        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if (hs_cur - as_cur) >= 2 else 0.0,
            1.0 if (as_cur - hs_cur) >= -1 else 0.0,
            1.0 if tot_maps > 2.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if (hs_cur >= 1 and as_cur >= 1) else 0.0,
            1.0 if (hs_cur == 2 and as_cur == 0) or (as_cur == 2 and hs_cur == 0) else 0.0,
            1.0 if tot_maps == 3 else 0.0,
            1.0 if hs_cur >= 1 else 0.0,
            1.0 if as_cur >= 1 else 0.0,
            1.0 if (hs_cur == 2 and as_cur == 1) else 0.0,
            1.0 if (as_cur == 2 and hs_cur == 1) else 0.0,
            1.0 if (hs_cur == 2 and as_cur == 0) else 0.0,
            1.0 if (as_cur == 2 and hs_cur == 0) else 0.0
        ]
        return targets

    return None

# ============================================================================
# ClickHouse DDS Real Tactical & Biological Features (Zero Sine Waves!)
# ============================================================================

CLICKHOUSE_HOST = os.environ.get("CLICKHOUSE_HOST", "10.111.38.2")
CLICKHOUSE_PORT = int(os.environ.get("CLICKHOUSE_PORT", "8123"))
CLICKHOUSE_USER = os.environ.get("CLICKHOUSE_USER", "default")
CLICKHOUSE_PASSWORD = os.environ.get("CLICKHOUSE_PASSWORD", "clickhouse")
CLICKHOUSE_DB = os.environ.get("CLICKHOUSE_DB", "igaming_dds")

_DDS_CACHE: Dict[str, Tuple[float, Dict[str, Any]]] = {}
_DDS_CACHE_TTL = 3600.0  # 1 hour cache for matrix preparation

SPORT_BASELINE_PRIORS: Dict[str, Dict[str, Any]] = {
    "football": {
        "conceded_first_rate": 0.48, "comeback_equalize_rate": 0.32, "comeback_win_rate": 0.16,
        "ppg_losing": 0.22, "collapsed_lead_rate": 0.18, "blown_lead_points": 0.25,
        "clean_sheet_rate": 0.28, "failed_to_score_rate": 0.24, "avg_max_deficit": 0.45,
        "late_goals_scored_ratio": 0.21,
        "goal_bins_for": [0.138, 0.142, 0.150, 0.188, 0.163, 0.217],
        "goal_bins_against": [0.138, 0.142, 0.150, 0.188, 0.163, 0.217],
        "avg_xg_for": 0.34, "avg_xg_against": 0.34, "xg_diff": 0.50,
        "xg_over_for": 0.0, "xg_over_against": 0.0,
        "open_play_xg_ratio": 0.75, "set_piece_xg_ratio": 0.25,
        "ppda": 0.50, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.80,
        "prog_passes": 0.45, "sca": 0.40, "gca": 0.20, "aerial_won": 0.50,
        "fouls": 0.44, "cards": 0.33, "first_half_diff": 0.50,
        "avg_starter_age": 0.95, "young_pct": 0.25, "veteran_pct": 0.15, "age_std": 0.35,
        "avg_height": 0.50, "bench_avg_age": 0.36, "bench_attack_depth": 0.65,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.18,
        "gk_rating": 0.68, "def_rating": 0.68, "mid_rating": 0.68, "att_rating": 0.68
    },
    "soccer": {
        "conceded_first_rate": 0.48, "comeback_equalize_rate": 0.32, "comeback_win_rate": 0.16,
        "ppg_losing": 0.22, "collapsed_lead_rate": 0.18, "blown_lead_points": 0.25,
        "clean_sheet_rate": 0.28, "failed_to_score_rate": 0.24, "avg_max_deficit": 0.45,
        "late_goals_scored_ratio": 0.21,
        "goal_bins_for": [0.138, 0.142, 0.150, 0.188, 0.163, 0.217],
        "goal_bins_against": [0.138, 0.142, 0.150, 0.188, 0.163, 0.217],
        "avg_xg_for": 0.34, "avg_xg_against": 0.34, "xg_diff": 0.50,
        "xg_over_for": 0.0, "xg_over_against": 0.0,
        "open_play_xg_ratio": 0.75, "set_piece_xg_ratio": 0.25,
        "ppda": 0.50, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.80,
        "prog_passes": 0.45, "sca": 0.40, "gca": 0.20, "aerial_won": 0.50,
        "fouls": 0.44, "cards": 0.33, "first_half_diff": 0.50,
        "avg_starter_age": 0.95, "young_pct": 0.25, "veteran_pct": 0.15, "age_std": 0.35,
        "avg_height": 0.50, "bench_avg_age": 0.36, "bench_attack_depth": 0.65,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.18,
        "gk_rating": 0.68, "def_rating": 0.68, "mid_rating": 0.68, "att_rating": 0.68
    },
    "ice-hockey": {
        "conceded_first_rate": 0.50, "comeback_equalize_rate": 0.40, "comeback_win_rate": 0.22,
        "ppg_losing": 0.25, "collapsed_lead_rate": 0.22, "blown_lead_points": 0.30,
        "clean_sheet_rate": 0.08, "failed_to_score_rate": 0.08, "avg_max_deficit": 0.55,
        "late_goals_scored_ratio": 0.33,
        "goal_bins_for": [0.15, 0.16, 0.17, 0.17, 0.17, 0.18],
        "goal_bins_against": [0.15, 0.16, 0.17, 0.17, 0.17, 0.18],
        "avg_xg_for": 0.50, "avg_xg_against": 0.50, "xg_diff": 0.50,
        "xg_over_for": 0.0, "xg_over_against": 0.0,
        "open_play_xg_ratio": 0.80, "set_piece_xg_ratio": 0.20,
        "ppda": 0.40, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.75,
        "prog_passes": 0.50, "sca": 0.50, "gca": 0.30, "aerial_won": 0.50,
        "fouls": 0.30, "cards": 0.20, "first_half_diff": 0.50,
        "avg_starter_age": 0.95, "young_pct": 0.20, "veteran_pct": 0.20, "age_std": 0.38,
        "avg_height": 0.60, "bench_avg_age": 0.40, "bench_attack_depth": 0.70,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.20,
        "gk_rating": 0.70, "def_rating": 0.70, "mid_rating": 0.70, "att_rating": 0.70
    },
    "basketball": {
        "conceded_first_rate": 0.50, "comeback_equalize_rate": 0.45, "comeback_win_rate": 0.28,
        "ppg_losing": 0.30, "collapsed_lead_rate": 0.25, "blown_lead_points": 0.35,
        "clean_sheet_rate": 0.0, "failed_to_score_rate": 0.0, "avg_max_deficit": 0.60,
        "late_goals_scored_ratio": 0.28,
        "goal_bins_for": [0.25, 0.25, 0.25, 0.25, 0.0, 0.0],
        "goal_bins_against": [0.25, 0.25, 0.25, 0.25, 0.0, 0.0],
        "avg_xg_for": 0.50, "avg_xg_against": 0.50, "xg_diff": 0.50,
        "xg_over_for": 0.0, "xg_over_against": 0.0,
        "open_play_xg_ratio": 0.70, "set_piece_xg_ratio": 0.30,
        "ppda": 0.50, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.85,
        "prog_passes": 0.60, "sca": 0.60, "gca": 0.40, "aerial_won": 0.50,
        "fouls": 0.40, "cards": 0.0, "first_half_diff": 0.50,
        "avg_starter_age": 0.95, "young_pct": 0.22, "veteran_pct": 0.18, "age_std": 0.36,
        "avg_height": 0.85, "bench_avg_age": 0.37, "bench_attack_depth": 0.72,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.25,
        "gk_rating": 0.70, "def_rating": 0.70, "mid_rating": 0.70, "att_rating": 0.70
    },
    "tennis": {
        "conceded_first_rate": 0.50, "comeback_equalize_rate": 0.35, "comeback_win_rate": 0.20,
        "ppg_losing": 0.20, "collapsed_lead_rate": 0.20, "blown_lead_points": 0.20,
        "clean_sheet_rate": 0.35, "failed_to_score_rate": 0.35, "avg_max_deficit": 0.40,
        "late_goals_scored_ratio": 0.30,
        "goal_bins_for": [0.33, 0.33, 0.34, 0.0, 0.0, 0.0],
        "goal_bins_against": [0.33, 0.33, 0.34, 0.0, 0.0, 0.0],
        "avg_xg_for": 0.50, "avg_xg_against": 0.50, "xg_diff": 0.50,
        "xg_over_for": 0.0, "xg_over_against": 0.0,
        "open_play_xg_ratio": 0.50, "set_piece_xg_ratio": 0.50,
        "ppda": 0.50, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.70,
        "prog_passes": 0.50, "sca": 0.50, "gca": 0.50, "aerial_won": 0.50,
        "fouls": 0.0, "cards": 0.0, "first_half_diff": 0.50,
        "avg_starter_age": 0.95, "young_pct": 0.30, "veteran_pct": 0.15, "age_std": 0.30,
        "avg_height": 0.60, "bench_avg_age": 0.36, "bench_attack_depth": 0.0,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.0,
        "gk_rating": 0.70, "def_rating": 0.70, "mid_rating": 0.70, "att_rating": 0.70
    },
    "table-tennis": {
        "conceded_first_rate": 0.50, "comeback_equalize_rate": 0.38, "comeback_win_rate": 0.22,
        "ppg_losing": 0.22, "collapsed_lead_rate": 0.22, "blown_lead_points": 0.22,
        "clean_sheet_rate": 0.30, "failed_to_score_rate": 0.30, "avg_max_deficit": 0.40,
        "late_goals_scored_ratio": 0.30,
        "goal_bins_for": [0.25, 0.25, 0.25, 0.25, 0.0, 0.0],
        "goal_bins_against": [0.25, 0.25, 0.25, 0.25, 0.0, 0.0],
        "avg_xg_for": 0.50, "avg_xg_against": 0.50, "xg_diff": 0.50,
        "xg_over_for": 0.0, "xg_over_against": 0.0,
        "open_play_xg_ratio": 0.50, "set_piece_xg_ratio": 0.50,
        "ppda": 0.50, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.70,
        "prog_passes": 0.50, "sca": 0.50, "gca": 0.50, "aerial_won": 0.50,
        "fouls": 0.0, "cards": 0.0, "first_half_diff": 0.50,
        "avg_starter_age": 0.95, "young_pct": 0.30, "veteran_pct": 0.15, "age_std": 0.30,
        "avg_height": 0.50, "bench_avg_age": 0.35, "bench_attack_depth": 0.0,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.0,
        "gk_rating": 0.70, "def_rating": 0.70, "mid_rating": 0.70, "att_rating": 0.70
    },
    "cs2": {
        "conceded_first_rate": 0.50, "comeback_equalize_rate": 0.35, "comeback_win_rate": 0.25,
        "ppg_losing": 0.25, "collapsed_lead_rate": 0.20, "blown_lead_points": 0.20,
        "clean_sheet_rate": 0.25, "failed_to_score_rate": 0.25, "avg_max_deficit": 0.40,
        "late_goals_scored_ratio": 0.30,
        "goal_bins_for": [0.50, 0.50, 0.0, 0.0, 0.0, 0.0],
        "goal_bins_against": [0.50, 0.50, 0.0, 0.0, 0.0, 0.0],
        "avg_xg_for": 0.50, "avg_xg_against": 0.50, "xg_diff": 0.50,
        "xg_over_for": 0.0, "xg_over_against": 0.0,
        "open_play_xg_ratio": 0.50, "set_piece_xg_ratio": 0.50,
        "ppda": 0.50, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.70,
        "prog_passes": 0.50, "sca": 0.50, "gca": 0.50, "aerial_won": 0.50,
        "fouls": 0.0, "cards": 0.0, "first_half_diff": 0.50,
        "avg_starter_age": 0.95, "young_pct": 0.45, "veteran_pct": 0.05, "age_std": 0.25,
        "avg_height": 0.50, "bench_avg_age": 0.32, "bench_attack_depth": 0.0,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.0,
        "gk_rating": 0.70, "def_rating": 0.70, "mid_rating": 0.70, "att_rating": 0.70
    },
    "esports": {
        "conceded_first_rate": 0.50, "comeback_equalize_rate": 0.35, "comeback_win_rate": 0.25,
        "ppg_losing": 0.25, "collapsed_lead_rate": 0.20, "blown_lead_points": 0.20,
        "clean_sheet_rate": 0.25, "failed_to_score_rate": 0.25, "avg_max_deficit": 0.40,
        "late_goals_scored_ratio": 0.30,
        "goal_bins_for": [0.50, 0.50, 0.0, 0.0, 0.0, 0.0],
        "goal_bins_against": [0.50, 0.50, 0.0, 0.0, 0.0, 0.0],
        "avg_xg_for": 0.50, "avg_xg_against": 0.50, "xg_diff": 0.50,
        "xg_over_for": 0.0, "xg_over_against": 0.0,
        "open_play_xg_ratio": 0.50, "set_piece_xg_ratio": 0.50,
        "ppda": 0.50, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.70,
        "prog_passes": 0.50, "sca": 0.50, "gca": 0.50, "aerial_won": 0.50,
        "fouls": 0.0, "cards": 0.0, "first_half_diff": 0.50,
        "avg_starter_age": 0.95, "young_pct": 0.45, "veteran_pct": 0.05, "age_std": 0.25,
        "avg_height": 0.50, "bench_avg_age": 0.32, "bench_attack_depth": 0.0,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.0,
        "gk_rating": 0.70, "def_rating": 0.70, "mid_rating": 0.70, "att_rating": 0.70
    }
}

def query_clickhouse_json(sql: str) -> Optional[List[Dict[str, Any]]]:
    """Execute raw SQL query against ClickHouse HTTP interface and return JSON rows."""
    try:
        url = f"http://{CLICKHOUSE_HOST}:{CLICKHOUSE_PORT}/?database={CLICKHOUSE_DB}"
        auth = "Basic " + base64.b64encode(f"{CLICKHOUSE_USER}:{CLICKHOUSE_PASSWORD}".encode()).decode()
        data = (sql + " FORMAT JSONEachRow").encode("utf-8")
        req = urllib.request.Request(url, data=data, headers={"Authorization": auth, "Content-Type": "text/plain"})
        with urllib.request.urlopen(req, timeout=1.5) as resp:
            lines = resp.read().decode("utf-8").strip().split("\n")
            return [json.loads(line) for line in lines if line]
    except Exception:
        return None

_TENNIS_PLAYERS_CACHE: Dict[Any, Dict[str, Any]] = {}
_BASKETBALL_TEAMS_CACHE: Dict[Any, Dict[str, Any]] = {}
_SPORT_MMR_CACHE: Dict[Tuple[str, int], Dict[str, Any]] = {}

def preload_sport_dds(sport: str):
    """Preloads ClickHouse view rows and Dynamic Glicko-2 MMR into memory to achieve sub-second vector extraction."""
    s_clean = (sport or "football").lower().replace("_", "-")
    if s_clean == "tennis" and not _TENNIS_PLAYERS_CACHE:
        t0 = time.time()
        rows = query_clickhouse_json("SELECT * FROM igaming_dds.tennis_player_advanced_v")
        if rows:
            for r in rows:
                pid = r.get("player_id")
                pname = (r.get("player_name") or "").strip().lower()
                if pid:
                    _TENNIS_PLAYERS_CACHE[int(pid)] = r
                if pname:
                    _TENNIS_PLAYERS_CACHE[pname] = r
            print(f" Preloaded {len(rows):,} tennis player profiles from ClickHouse DDS in {time.time() - t0:.2f}s", flush=True)

    elif s_clean in ["basketball", "classic-basketball"] and not _BASKETBALL_TEAMS_CACHE:
        t0 = time.time()
        rows = query_clickhouse_json("SELECT * FROM igaming_dds.basketball_team_advanced_v")
        if rows:
            for r in rows:
                tid = r.get("team_id")
                tname = (r.get("team_name") or "").strip().lower()
                if tid:
                    _BASKETBALL_TEAMS_CACHE[int(tid)] = r
                if tname:
                    _BASKETBALL_TEAMS_CACHE[tname] = r
            print(f" Preloaded {len(rows):,} basketball team profiles from ClickHouse DDS in {time.time() - t0:.2f}s", flush=True)

    # Preload Universal Dynamic MMR ratings for the sport
    sport_map = {
        "ice-hockey": ["ice-hockey", "hockey"],
        "hockey": ["ice-hockey", "hockey"],
        "basketball": ["basketball"],
        "classic-basketball": ["basketball"],
        "tennis": ["tennis"],
        "table-tennis": ["table-tennis"],
        "football": ["football"],
    }
    slugs = sport_map.get(s_clean, [s_clean])
    slug_in = ", ".join(f"'{s}'" for s in slugs)
    try:
        t0 = time.time()
        mmr_rows = query_clickhouse_json(
            f"SELECT entity_id, mmr, rd, peak_mmr, streak FROM igaming_dds.sport_mmr_rating WHERE sport_slug IN ({slug_in})"
        )
        if mmr_rows:
            for r in mmr_rows:
                eid = r.get("entity_id")
                if eid:
                    _SPORT_MMR_CACHE[(s_clean, int(eid))] = r
            print(f" Preloaded {len(mmr_rows):,} MMR ratings for {s_clean} from ClickHouse DDS in {time.time() - t0:.2f}s", flush=True)
    except Exception as e:
        print(f" [Warning] Could not preload MMR ratings for {s_clean}: {e}", flush=True)

def get_team_dds_features(team_id: Optional[int], team_name: str, sport: str) -> Dict[str, Any]:
    """Fetches real game-state resilience, 15-minute goal distribution, xG and squad age from ClickHouse DDS."""
    s_clean = (sport or "football").lower().replace("_", "-")
    cache_key = f"{s_clean}:{team_id}:{team_name}"
    now = time.time()

    cached = _DDS_CACHE.get(cache_key)
    if cached and (now - cached[0] < _DDS_CACHE_TTL):
        return cached[1]

    features = dict(SPORT_BASELINE_PRIORS.get(s_clean, SPORT_BASELINE_PRIORS.get("football", {})))

    # Dynamic Universal Sports MMR (Glicko-2) Preloaded Match
    if team_id and (s_clean, int(team_id)) in _SPORT_MMR_CACHE:
        mmr_data = _SPORT_MMR_CACHE[(s_clean, int(team_id))]
        features["mmr"] = float(mmr_data.get("mmr", 1500.0))
        features["rd"] = float(mmr_data.get("rd", 350.0))
        features["peak_mmr"] = float(mmr_data.get("peak_mmr", 1500.0))
        features["streak"] = float(mmr_data.get("streak", 0.0))

    if s_clean == "tennis":
        t = None
        if team_id and int(team_id) in _TENNIS_PLAYERS_CACHE:
            t = _TENNIS_PLAYERS_CACHE[int(team_id)]
        elif team_name and team_name.strip().lower() in _TENNIS_PLAYERS_CACHE:
            t = _TENNIS_PLAYERS_CACHE[team_name.strip().lower()]

        if t:
            features["win_rate"] = float(t.get("win_rate", 0.50))
            features["fs_pct"] = float(t.get("fs_pct", 62.0))
            features["aces_per_match"] = float(t.get("aces_per_match", 4.0))
            features["dfs_per_match"] = float(t.get("dfs_per_match", 2.0))
            features["bp_saved_pct"] = float(t.get("bp_saved_pct", 60.0))
            features["bp_conv_pct"] = float(t.get("bp_conv_pct", 40.0))
            features["hold_pct_modeled"] = float(t.get("hold_pct_modeled", 0.80))
            features["break_pct_modeled"] = float(t.get("break_pct_modeled", 0.22))
            features["dominance_ratio_sigmoid"] = float(t.get("dominance_ratio_sigmoid", 0.50))
            features["hard_wr"] = float(t.get("hard_wr", 0.50))
            features["clay_wr"] = float(t.get("clay_wr", 0.50))
            features["days_since_last_match"] = float(t.get("days_since_last_match", 7.0))
            features["comeback_matches"] = int(t.get("comeback_matches", 5))
            features["current_rank"] = int(t.get("current_rank", 1500))
            features["peak_rank"] = int(t.get("peak_rank", 1500))
            features["quartic_stature"] = float(t.get("quartic_stature", 0.05))

        _DDS_CACHE[cache_key] = (now, features)
        return features

    elif s_clean in ["basketball", "classic-basketball"]:
        b = None
        if team_id and int(team_id) in _BASKETBALL_TEAMS_CACHE:
            b = _BASKETBALL_TEAMS_CACHE[int(team_id)]
        elif team_name and team_name.strip().lower() in _BASKETBALL_TEAMS_CACHE:
            b = _BASKETBALL_TEAMS_CACHE[team_name.strip().lower()]

        if b:
            pf = float(b.get("avg_pts_for", 105.0))
            pa = float(b.get("avg_pts_against", 105.0))
            features["avg_pts_for"] = pf
            features["avg_pts_against"] = pa
            features["pace_proxy"] = max(0.0, min(1.0, (pf + pa - 180.0) / 80.0))
            features["off_rating"] = max(0.0, min(1.0, (pf - 80.0) / 50.0))
            features["def_rating"] = max(0.0, min(1.0, (130.0 - pa) / 50.0))
            features["net_rating"] = float(b.get("net_rating_proxy", 0.50))
            features["efg_pct"] = float(b.get("efg_pct_proxy", 0.52))
            features["ts_pct"] = float(b.get("true_shooting_pct_proxy", 0.56))
            features["q4_share"] = float(b.get("q4_scoring_share", 0.25))
            features["ot_rate"] = float(b.get("ot_rate", 0.05))

        _DDS_CACHE[cache_key] = (now, features)
        return features

    try:
        where_clause = ""
        if team_id and int(team_id) > 0:
            where_clause = f"team_id = {int(team_id)}"
        elif team_name and team_name not in ["Team 1", "Team 2"]:
            safe_name = team_name.replace("'", "\\'")
            where_clause = f"LOWER(team_name) = LOWER('{safe_name}')"

        if where_clause and s_clean in ["football", "soccer"]:
            # 1. Resilience & Game State
            res_rows = query_clickhouse_json(
                f"SELECT count() as matches, "
                f"       avg(conceded_first) as cf_rate, "
                f"       avg(equalized_after_trailing) as eq_rate, "
                f"       avg(collapsed_after_leading) as col_rate, "
                f"       avg(goals_scored_75_plus) as late_for, "
                f"       avg(goals_conceded_75_plus) as late_against, "
                f"       avg(first_half_score_diff) as fh_diff "
                f"FROM match_gamestate_resilience_fact "
                f"WHERE {where_clause}"
            )
            if res_rows and int(res_rows[0].get("matches", 0)) > 0:
                r = res_rows[0]
                features["conceded_first_rate"] = round(float(r.get("cf_rate") or 0.48), 4)
                features["comeback_equalize_rate"] = round(float(r.get("eq_rate") or 0.32), 4)
                features["collapsed_lead_rate"] = round(float(r.get("col_rate") or 0.18), 4)
                late_f = float(r.get("late_for") or 0.34)
                late_a = float(r.get("late_against") or 0.34)
                features["late_goals_scored_ratio"] = round(late_f / max(0.1, late_f + late_a), 4)
                features["first_half_diff"] = round(float(r.get("fh_diff") or 0.0) / 3.0 + 0.5, 4)

            # 2. Goal distribution by 15-min intervals
            gt_rows = query_clickhouse_json(
                f"SELECT countIf(minute <= 15) / max(1, count()) as b1, "
                f"       countIf(minute > 15 AND minute <= 30) / max(1, count()) as b2, "
                f"       countIf(minute > 30 AND minute <= 45) / max(1, count()) as b3, "
                f"       countIf(minute > 45 AND minute <= 60) / max(1, count()) as b4, "
                f"       countIf(minute > 60 AND minute <= 75) / max(1, count()) as b5, "
                f"       countIf(minute > 75) / max(1, count()) as b6 "
                f"FROM match_goal_timeline_fact "
                f"WHERE {where_clause} AND incident_type = 'GOAL'"
            )
            if gt_rows and sum(float(gt_rows[0].get(f"b{i}", 0)) for i in range(1, 7)) > 0.01:
                features["goal_bins_for"] = [round(float(gt_rows[0].get(f"b{i}", 0.166)), 4) for i in range(1, 7)]

            # 3. Tactical xG, PPDA, possession from football_team_advanced_v
            if team_id:
                tac_rows = query_clickhouse_json(
                    f"SELECT * FROM igaming_dds.football_team_advanced_v WHERE team_id = {int(team_id)}"
                )
                if tac_rows and float(tac_rows[0].get("total_matches", 0)) > 0:
                    t = tac_rows[0]
                    features["avg_xg_for"] = round(float(t.get("avg_xg") or 1.35) / 4.0, 4)
                    features["ppda"] = round(float(t.get("pressing_intensity_sigmoid") or 0.50), 4)
                    features["possession"] = round(float(t.get("avg_possession") or 50.0) / 100.0, 4)
                    features["fouls"] = round(float(t.get("avg_fouls") or 11.0) / 25.0, 4)
                    features["cards"] = round(float(t.get("avg_cards") or 2.0) / 6.0, 4)

            # 4. Squad biological age dynamics
            if team_id:
                age_rows = query_clickhouse_json(
                    f"SELECT avg(age_at_match) as avg_age, "
                    f"       countIf(age_at_match <= 22) / max(1, count()) as young_pct, "
                    f"       countIf(age_at_match >= 32) / max(1, count()) as veteran_pct, "
                    f"       stddevPop(age_at_match) as age_std "
                    f"FROM football_lineup_player_fact "
                    f"WHERE mdm_team_id = {int(team_id)} AND age_at_match > 0"
                )
                if age_rows and float(age_rows[0].get("avg_age", 0)) > 0:
                    a = age_rows[0]
                    avg_a = float(a.get("avg_age") or 26.5)
                    features["avg_starter_age"] = round(math.exp(-((avg_a - 26.8) ** 2) / 24.5), 4)
                    features["young_pct"] = round(float(a.get("young_pct") or 0.25), 4)
                    features["veteran_pct"] = round(float(a.get("veteran_pct") or 0.15), 4)
                    features["age_std"] = round(float(a.get("age_std") or 3.5) / 10.0, 4)

    except Exception:
        pass

    _DDS_CACHE[cache_key] = (now, features)
    return features

def _populate_dds_dimensions(vec: List[float], dds: Dict[str, Any], sport: str = "football"):
    """Populates dimensions 8..63 of a 64-dim form vector with real physical DDS metrics (ZERO Sine Waves!)."""
    s_clean = (sport or "football").lower().replace("_", "-")

    if s_clean == "tennis":
        # Game State & Comeback Resilience (Dims 8..17)
        vec[8]  = 0.50
        vec[9]  = 0.35
        vec[10] = 0.20
        vec[11] = 0.20
        vec[12] = 0.20
        vec[13] = 0.20
        vec[14] = 0.35
        vec[15] = 0.35
        vec[16] = 0.40
        vec[17] = 0.30

        # Temporal Distribution (Dims 18..29)
        bins = [0.33, 0.33, 0.34, 0.0, 0.0, 0.0]
        for i in range(6):
            vec[18 + i] = bins[i]
            vec[24 + i] = bins[i]

        # Advanced Tennis Metrics (Dims 30..44)
        fs_pct = float(dds.get("fs_pct", 62.0))
        aces = float(dds.get("aces_per_match", 4.0))
        dfs = float(dds.get("dfs_per_match", 2.0))
        bp_saved = float(dds.get("bp_saved_pct", 60.0))
        bp_conv = float(dds.get("bp_conv_pct", 40.0))
        hold_pct = float(dds.get("hold_pct_modeled", 0.80))
        break_pct = float(dds.get("break_pct_modeled", 0.22))
        dr_sig = float(dds.get("dominance_ratio_sigmoid", 0.50))
        days_off = float(dds.get("days_since_last_match", 7.0))
        comeback_m = int(dds.get("comeback_matches", 5))

        vec[30] = fs_pct / 100.0
        vec[31] = 0.50
        vec[32] = 0.50
        vec[33] = min(1.0, aces / 15.0)
        vec[34] = min(1.0, dfs / 10.0)
        vec[35] = bp_saved / 100.0
        vec[36] = bp_conv / 100.0
        vec[37] = hold_pct
        vec[38] = break_pct
        vec[39] = float(dds.get("hard_wr", 0.50))
        vec[40] = float(dds.get("clay_wr", 0.50))
        vec[41] = 0.50
        vec[42] = 0.50
        vec[43] = 0.50
        vec[44] = float(dds.get("h2h_wr", 0.50))

        # Quartic Stature & Injury Deflation (Dims 45..51)
        peak_rank = int(dds.get("peak_rank", 1500))
        cur_rank = int(dds.get("current_rank", 1500))
        quartic_stature = float(dds.get("quartic_stature", 0.05))
        if 1 <= peak_rank <= 2000:
            quartic_stature = max(quartic_stature, 1.0 / (peak_rank ** 0.25))
        current_stature = (1.0 / (cur_rank ** 0.25)) if (1 <= cur_rank <= 2000) else quartic_stature
        deflation_spread = max(0.0, quartic_stature - current_stature)

        layoff_norm = days_off / (days_off + 60.0)
        cadence = 1.0 - math.exp(-0.65 * comeback_m)
        base_rust = 0.35 * (days_off / (days_off + 45.0)) if days_off >= 45.0 else 0.0
        rust_penalty = base_rust * math.exp(-0.65 * max(0, comeback_m - 1))
        serve_integrity = 0.95

        vec[45] = round(quartic_stature, 4)
        vec[46] = round(current_stature, 4)
        vec[47] = round(deflation_spread, 4)
        vec[48] = round(layoff_norm, 4)
        vec[49] = round(cadence, 4)
        vec[50] = round(dr_sig, 4)
        vec[51] = round(hold_pct, 4)

        # Point-in-time dynamics (Dims 52..58)
        vec[52] = 0.0
        vec[53] = 0.0
        vec[54] = 0.50
        vec[55] = round(layoff_norm, 4)
        vec[56] = round(cadence, 4)
        vec[57] = round(rust_penalty, 4)
        vec[58] = serve_integrity

        # Solidity & Clutch (Dims 59..63)
        vec[59] = round(min(1.0, max(0.05, 0.15 + rust_penalty * 0.50)), 4)
        vec[60] = round(min(0.98, max(0.40, hold_pct)), 4)
        vec[61] = round(min(0.98, max(0.10, break_pct)), 4)
        vec[62] = round(dr_sig, 4)
        vec[63] = round(float(dds.get("win_rate", 0.50)), 4)
        return

    elif s_clean in ["basketball", "classic-basketball"]:
        # Game State & Comeback Resilience (Dims 8..17)
        vec[8]  = float(dds.get("conceded_first_rate", 0.50))
        vec[9]  = float(dds.get("comeback_equalize_rate", 0.45))
        vec[10] = float(dds.get("comeback_win_rate", 0.28))
        vec[11] = float(dds.get("ppg_losing", 0.30))
        vec[12] = float(dds.get("collapsed_lead_rate", 0.25))
        vec[13] = float(dds.get("blown_lead_points", 0.35))
        if "mmr" in dds:
            mmr_val = float(dds["mmr"])
            rd_val = float(dds.get("rd", 350.0))
            vec[14] = max(-1.0, min(1.0, (mmr_val - 1500.0) / 1000.0))
            vec[15] = max(0.0, min(1.0, 1.0 - (rd_val / 350.0)))
        else:
            vec[14] = 0.0
            vec[15] = 0.0
        vec[16] = float(dds.get("avg_max_deficit", 0.60))
        vec[17] = float(dds.get("q4_share", 0.25))

        # Goal Timeline / Quarters (Dims 18..29)
        bins = [0.25, 0.25, 0.25, 0.25, 0.0, 0.0]
        for i in range(6):
            vec[18 + i] = bins[i]
            vec[24 + i] = bins[i]

        # Advanced Basketball Metrics (Dims 30..47)
        pf = float(dds.get("avg_pts_for", 105.0))
        pa = float(dds.get("avg_pts_against", 105.0))
        pace = float(dds.get("pace_proxy", max(0.0, min(1.0, (pf + pa - 180.0) / 80.0))))
        off_r = float(dds.get("off_rating", max(0.0, min(1.0, (pf - 80.0) / 50.0))))
        def_r = float(dds.get("def_rating", max(0.0, min(1.0, (130.0 - pa) / 50.0))))
        net_r = float(dds.get("net_rating", 0.50))
        efg = float(dds.get("efg_pct", 0.52))
        ts = float(dds.get("ts_pct", 0.56))

        vec[30] = round(pace, 4)
        vec[31] = round(off_r, 4)
        vec[32] = round(def_r, 4)
        vec[33] = round(net_r, 4)
        vec[34] = round(efg, 4)
        vec[35] = round(ts, 4)
        vec[36] = 0.50
        vec[37] = 0.50
        vec[38] = 0.50
        vec[39] = 0.50
        vec[40] = 0.85
        vec[41] = 0.60
        vec[42] = 0.60
        vec[43] = 0.40
        vec[44] = 0.50
        vec[45] = 0.40
        vec[46] = 0.0
        vec[47] = 0.50

        # Clutch & Spread (Dims 48..51)
        net_diff = pf - pa
        vec[48] = round(max(0.0, min(1.0, 0.5 + (net_diff / 30.0))), 4)
        vec[49] = 1.0 if abs(net_diff) >= 10.0 else 0.0
        vec[50] = float(dds.get("ot_rate", 0.05))
        vec[51] = round(pace, 4)

        # Tournament Dynamics (Dims 52..58)
        if "peak_mmr" in dds:
            peak_val = float(dds.get("peak_mmr", 1500.0))
            streak_val = float(dds.get("streak", 0.0))
            vec[52] = max(0.0, min(1.5, (peak_val - 1500.0) / 1000.0))
            vec[53] = max(-1.0, min(1.0, streak_val / 10.0))
        else:
            vec[52] = 0.50
            vec[53] = 0.0
        vec[54] = 0.50
        vec[55] = 0.0
        vec[56] = 0.50
        vec[57] = 0.50
        vec[58] = 0.05

        # Rim protection & solidities (Dims 59..63)
        vec[59] = 0.15
        vec[60] = round(def_r, 4)
        vec[61] = round(off_r, 4)
        vec[62] = round(net_r, 4)
        vec[63] = round(efg, 4)
        return

    # Game State & Comeback Resilience (Dims 8..17)
    vec[8]  = float(dds.get("conceded_first_rate", 0.48))
    vec[9]  = float(dds.get("comeback_equalize_rate", 0.32))
    vec[10] = float(dds.get("comeback_win_rate", 0.16))
    vec[11] = float(dds.get("ppg_losing", 0.22))
    vec[12] = float(dds.get("collapsed_lead_rate", 0.18))
    vec[13] = float(dds.get("blown_lead_points", 0.25))
    if "mmr" in dds:
        mmr_val = float(dds["mmr"])
        rd_val = float(dds.get("rd", 350.0))
        vec[14] = max(-1.0, min(1.0, (mmr_val - 1500.0) / 1000.0))
        vec[15] = max(0.0, min(1.0, 1.0 - (rd_val / 350.0)))
    else:
        vec[14] = float(dds.get("clean_sheet_rate", 0.28))
        vec[15] = float(dds.get("failed_to_score_rate", 0.24))
    vec[16] = float(dds.get("avg_max_deficit", 0.45))
    vec[17] = float(dds.get("late_goals_scored_ratio", 0.21))

    # Goal Timeline Temporal Distribution (Dims 18..29)
    gb_for = dds.get("goal_bins_for", [0.138, 0.142, 0.150, 0.188, 0.163, 0.217])
    gb_ag  = dds.get("goal_bins_against", [0.138, 0.142, 0.150, 0.188, 0.163, 0.217])
    for i in range(6):
        vec[18 + i] = float(gb_for[i]) if i < len(gb_for) else 0.166
        vec[24 + i] = float(gb_ag[i]) if i < len(gb_ag) else 0.166

    # Advanced Tactical & Efficiency Metrics (Dims 30..47)
    vec[30] = float(dds.get("avg_xg_for", 0.34))
    vec[31] = float(dds.get("avg_xg_against", 0.34))
    vec[32] = float(dds.get("xg_diff", 0.50))
    vec[33] = float(dds.get("xg_over_for", 0.0))
    vec[34] = float(dds.get("xg_over_against", 0.0))
    vec[35] = float(dds.get("open_play_xg_ratio", 0.75))
    vec[36] = float(dds.get("set_piece_xg_ratio", 0.25))
    vec[37] = float(dds.get("ppda", 0.30))
    vec[38] = float(dds.get("def_line_height", 0.50))
    vec[39] = float(dds.get("possession", 0.50))
    vec[40] = float(dds.get("pass_acc", 0.80))
    vec[41] = float(dds.get("prog_passes", 0.45))
    vec[42] = float(dds.get("sca", 0.40))
    vec[43] = float(dds.get("gca", 0.20))
    vec[44] = float(dds.get("aerial_won", 0.50))
    vec[45] = float(dds.get("fouls", 0.44))
    vec[46] = float(dds.get("cards", 0.33))
    vec[47] = float(dds.get("first_half_diff", 0.50))

    # Biological Squad Age & Dynamics (Dims 48..63)
    vec[48] = float(dds.get("avg_starter_age", 0.38))
    vec[49] = float(dds.get("young_pct", 0.25))
    vec[50] = float(dds.get("veteran_pct", 0.15))
    vec[51] = float(dds.get("age_std", 0.35))
    # Point-in-Time Tournament Motivation & Dynamic Stakes (Dims 52..58, synchronized with Java TacticalTensorBuilder)
    s_clean = (sport or "football").lower().replace("_", "-")
    if s_clean in ["tennis"]:
        vec[52] = float(dds.get("elimination_pressure", 0.0))
        vec[53] = float(dds.get("draw_viability", 0.0))
        vec[54] = float(dds.get("regional_card_bias", 0.50))
        vec[55] = float(dds.get("injury_layoff_duration", dds.get("missing_count", 0.10)))
        vec[56] = float(dds.get("comeback_match_cadence", dds.get("missing_rating_loss", 0.85)))
        vec[57] = float(dds.get("net_injury_rust_decay", dds.get("returning_count", 0.00)))
        vec[58] = float(dds.get("serve_integrity_ratio", dds.get("injury_rust", 0.95)))
    else:
        # Team sports (Football, Basketball, Ice Hockey, Table Tennis, Esports)
        if "peak_mmr" in dds:
            peak_val = float(dds.get("peak_mmr", 1500.0))
            streak_val = float(dds.get("streak", 0.0))
            vec[52] = max(0.0, min(1.5, (peak_val - 1500.0) / 1000.0))
            vec[53] = max(-1.0, min(1.0, streak_val / 10.0))
        else:
            vec[52] = float(dds.get("elimination_pressure", dds.get("avg_height", 0.0)))
            vec[53] = float(dds.get("draw_viability", dds.get("bench_avg_age", 1.0)))
        vec[54] = float(dds.get("regional_card_bias", dds.get("bench_attack_depth", 0.50)))
        vec[55] = float(dds.get("blowout_gradient", dds.get("missing_count", 0.0)))
        vec[56] = float(dds.get("win_leverage", dds.get("missing_rating_loss", 0.50)))
        vec[57] = float(dds.get("elimination_hazard", dds.get("returning_count", 0.50)))
        vec[58] = float(dds.get("prize_wage_ratio", dds.get("injury_rust", 0.05)))
    pass_error = max(0.0, 1.0 - vec[40])
    base_blunder = float(dds.get("blunder_frequency_index", min(1.0, max(0.05, 0.15 + (vec[49] * 0.45) + (pass_error * 0.40) + (vec[49] * pass_error * 0.80)))))
    blunder_mult = float(dds.get("blunder_risk_multiplier", 1.0))
    nat_fatigued = int(dds.get("national_fatigued_count", 0))
    blunder_fatigue_penalty = 0.08 if nat_fatigued >= 2 else (0.03 if nat_fatigued == 1 else 0.0)
    vec[59] = float(min(1.0, max(0.05, (base_blunder * blunder_mult) + blunder_fatigue_penalty)))

    vec[60] = float(dds.get("gk_rating", 0.68))
    cb_tenure_solidity = float(dds.get("cb_pairing_solidity", dds.get("def_rating", 0.68)))
    vec[61] = float(min(1.0, max(0.20, (float(dds.get("def_rating", 0.68)) * 0.40) + (cb_tenure_solidity * 0.60))))

    mid_tenure_solidity = float(dds.get("midfield_screen_solidity", dds.get("mid_rating", 0.68)))
    vec[62] = float(min(1.0, max(0.20, (float(dds.get("mid_rating", 0.68)) * 0.40) + (mid_tenure_solidity * 0.60))))

    vec[63] = float(dds.get("att_rating", 0.68))

def extract_form_vector(history: List[Tuple[int, int, int, int]], last_ts: int, cur_ts: int, has_draws: bool,
                        team_id: Optional[int] = None, team_name: str = "", sport: str = "football") -> List[float]:
    vec = [0.0] * 64
    if not history:
        dds = get_team_dds_features(team_id, team_name, sport)
        _populate_dds_dimensions(vec, dds, sport=sport)
        norm = math.sqrt(sum(x * x for x in vec))
        return [x / norm for x in vec] if norm > 1e-6 else vec

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
    # Non-linear Banister Fitness-Fatigue recovery curve
    days_rest = max(0.0, (cur_ts - last_ts) / 86400000.0) if (last_ts > 0 and cur_ts > last_ts) else 5.0
    acute_recovery = 1.0 / (1.0 + math.exp(-2.2 * (days_rest - 2.8)))
    rust_decay = math.exp(-0.035 * max(0.0, days_rest - 8.0))
    vec[7] = round(max(0.05, min(1.0, acute_recovery * rust_decay)), 4)

    # Real Physical & Biological DDS Features (Dims 8..63, ZERO Sine Waves!)
    dds = get_team_dds_features(team_id, team_name, sport)
    _populate_dds_dimensions(vec, dds, sport=sport)

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
# Qdrant Tactical DNA Vector Ingestion & Cosine Style Distance
# ============================================================================

SPORT_QDRANT_COLLECTIONS = {
    "football": ("football_tactical_dna", 64),
    "tennis": ("tennis_tactical_dna", 32),
    "table-tennis": ("table_tennis_tactical_dna", 32),
    "cs2": ("esports_cs2_tactical_dna", 48),
    "dota2": ("esports_dota2_tactical_dna", 48),
    "basketball": ("basketball_tactical_dna", 48),
    "ice-hockey": ("ice_hockey_tactical_dna", 48)
}

def load_qdrant_tactical_cache(sport: str) -> Tuple[Dict[str, np.ndarray], int]:
    cfg = SPORT_QDRANT_COLLECTIONS.get(sport)
    if not cfg:
        return {}, 0
    collection, dim = cfg
    cache_path = os.path.join(CACHE_DIR, f"{collection}_vectors.pkl")
    if os.path.exists(cache_path):
        try:
            t0 = time.time()
            with open(cache_path, "rb") as f:
                data = pickle.load(f)
            print(f" Loaded {len(data):,} Qdrant tactical DNA vectors ({dim}d) from {cache_path} in {time.time()-t0:.2f}s", flush=True)
            return data, dim
        except Exception as e:
            print(f" [Warning] Could not load Qdrant cache {cache_path}: {e}")

    # Fallback to fetching live from Qdrant cluster
    qdrant_url = os.environ.get("QDRANT_URL", "http://qdrant.igaming-analytics.svc.cluster.local:6333")
    try:
        import requests
        print(f" Fetching vectors live from Qdrant: {collection} ({dim}d)...", flush=True)
        vectors = {}
        offset = None
        while True:
            body = {"limit": 10000, "with_vector": True}
            if offset is not None:
                body["offset"] = offset
            r = requests.post(f"{qdrant_url}/collections/{collection}/points/scroll", json=body, timeout=20).json()
            result = r.get("result", {})
            points = result.get("points", [])
            if not points:
                break
            for p in points:
                vec = np.array(p["vector"], dtype=np.float32)
                pt_id = p.get("id")
                if pt_id:
                    vectors[str(pt_id)] = vec
                payload = p.get("payload", {})
                t_id = payload.get("team_id")
                if t_id:
                    vectors[f"id:{t_id}"] = vec
                    vectors[str(t_id)] = vec
                name = payload.get("name") or payload.get("team_name") or payload.get("player_name")
                if name:
                    vectors[name.strip().lower()] = vec
            offset = result.get("next_page_offset")
            if offset is None:
                break

        if vectors:
            with open(cache_path, "wb") as f:
                pickle.dump(vectors, f, protocol=pickle.HIGHEST_PROTOCOL)
            print(f" Stored {len(vectors):,} key-vector mappings to {cache_path}", flush=True)
        return vectors, dim
    except Exception as e:
        print(f" [Notice] Qdrant collection {collection} not accessible ({e}). Running with imputed defaults.", flush=True)
        return {}, dim

def extract_qdrant_features(home_name: str, away_name: str, hid: Optional[int], aid: Optional[int], qdrant_cache: Dict[str, np.ndarray], dim: int) -> Tuple[List[float], List[float], List[float]]:
    if dim <= 0 or not qdrant_cache:
        zero = [0.0] * dim if dim > 0 else []
        return zero, zero, [1.0]

    v_h = None
    if hid:
        v_h = qdrant_cache.get(f"id:{hid}")
        if v_h is None:
            v_h = qdrant_cache.get(str(hid))
    if v_h is None and home_name:
        v_h = qdrant_cache.get(home_name.strip().lower())
    if v_h is None or len(v_h) != dim:
        v_h = np.zeros(dim, dtype=np.float32)

    v_a = None
    if aid:
        v_a = qdrant_cache.get(f"id:{aid}")
        if v_a is None:
            v_a = qdrant_cache.get(str(aid))
    if v_a is None and away_name:
        v_a = qdrant_cache.get(away_name.strip().lower())
    if v_a is None or len(v_a) != dim:
        v_a = np.zeros(dim, dtype=np.float32)

    norm_h = float(np.linalg.norm(v_h))
    norm_a = float(np.linalg.norm(v_a))
    if norm_h > 1e-6 and norm_a > 1e-6:
        cos_sim = float(np.dot(v_h, v_a) / (norm_h * norm_a))
        cos_dist = float(np.clip(1.0 - cos_sim, 0.0, 2.0))
    else:
        cos_dist = 1.0

    return v_h.tolist(), v_a.tolist(), [cos_dist]

# ============================================================================
# Main Processing Pipeline
# ============================================================================

def process_sport_multitask(sport: str, max_files: Optional[int] = None):
    print("\n" + "=" * 80)
    print(f" 🚀 EXTRACTING MULTI-TASK MATRIX V3: {sport.upper()}")
    print("=" * 80, flush=True)

    # Load Qdrant Tactical DNA vectors
    qdrant_cache, q_dim = load_qdrant_tactical_cache(sport)

    # Preload Sport-Specific ClickHouse DDS Profiles into memory
    preload_sport_dds(sport)

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
            avail_cols = pq.read_schema(f).names
            load_cols = ["event_id", "start_timestamp", "home_team_name", "away_team_name"]
            if "mdm_match_id" in avail_cols:
                load_cols.append("mdm_match_id")
            if "mdm_home_team_id" in avail_cols:
                load_cols.append("mdm_home_team_id")
            if "mdm_away_team_id" in avail_cols:
                load_cols.append("mdm_away_team_id")
            if "home_score" in avail_cols:
                load_cols.extend(["home_score", "away_score"])
            elif "home_score_current" in avail_cols:
                load_cols.extend(["home_score_current", "away_score_current"])
            if "raw_event_json" in avail_cols:
                load_cols.append("raw_event_json")

            t = pq.read_table(f, columns=load_cols)
            c_eid = t.column("event_id").to_pylist()
            c_ts = t.column("start_timestamp").to_pylist()
            c_ht = t.column("home_team_name").to_pylist()
            c_at = t.column("away_team_name").to_pylist()
            c_mid = t.column("mdm_match_id").to_pylist() if "mdm_match_id" in load_cols else [None] * len(c_eid)
            c_hid = t.column("mdm_home_team_id").to_pylist() if "mdm_home_team_id" in load_cols else [None] * len(c_eid)
            c_aid = t.column("mdm_away_team_id").to_pylist() if "mdm_away_team_id" in load_cols else [None] * len(c_eid)

            score_h_col = "home_score" if "home_score" in load_cols else "home_score_current"
            score_a_col = "away_score" if "away_score" in load_cols else "away_score_current"
            c_hs = t.column(score_h_col).to_pylist() if score_h_col in load_cols else [None] * len(c_eid)
            c_as = t.column(score_a_col).to_pylist() if score_a_col in load_cols else [None] * len(c_eid)
            c_raw = t.column("raw_event_json").to_pylist() if "raw_event_json" in load_cols else [""] * len(c_eid)

            for eid, mid, hid, aid, ts, ht, at, hs, ascore, raw_j in zip(c_eid, c_mid, c_hid, c_aid, c_ts, c_ht, c_at, c_hs, c_as, c_raw):
                if not ht or not at or ts is None or ts <= 0 or hs is None or ascore is None:
                    continue
                eff_match_id = int(mid) if mid is not None and mid > 0 else safe_int(eid)
                home_k = f"mdm:{int(hid)}" if hid is not None and hid > 0 else f"str:{ht.strip().lower()}"
                away_k = f"mdm:{int(aid)}" if aid is not None and aid > 0 else f"str:{at.strip().lower()}"
                home_id = int(hid) if hid is not None and hid > 0 else None
                away_id = int(aid) if aid is not None and aid > 0 else None
                raw_events.append((eff_match_id, int(ts), home_k, away_k, str(ht), str(at), int(hs), int(ascore), raw_j, home_id, away_id))
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

    for mid, ts, home_k, away_k, home_name, away_name, hs, ascore, raw_j, hid, aid in raw_events:
        h_hist = team_history[home_k]
        a_hist = team_history[away_k]

        if len(h_hist) >= 2 and len(a_hist) >= 2:
            hs_dict, as_dict = extract_score_subdicts(raw_j)
            target_vec = extract_targets(sport, hs_dict, as_dict, hs, ascore)
            if target_vec is not None:
                v_h = extract_form_vector(h_hist, team_last_ts[home_k], ts, has_draws,
                                         team_id=hid, team_name=home_name, sport=sport)
                v_a = extract_form_vector(a_hist, team_last_ts[away_k], ts, has_draws,
                                         team_id=aid, team_name=away_name, sport=sport)
                diff = [h - a for h, a in zip(v_h, v_a)]
                prod = [h * a for h, a in zip(v_h, v_a)]
                ctx = [v_h[0] - v_a[0], v_h[5] - v_a[5], 1.0]
                macro_feat = v_h + v_a + diff + prod + ctx

                lineup_feat = extract_lineup_and_injury_features(mid, ts, home_name, away_name, lineups_cache, team_player_medical)
                if lineup_feat[0] > 0.0 or lineup_feat[12] > 0.0:
                    lineups_matched += 1

                q_h, q_a, q_cos = extract_qdrant_features(home_name, away_name, hid, aid, qdrant_cache, q_dim)
                feat = macro_feat + lineup_feat + q_h + q_a + q_cos
                X_list.append(feat)
                Y_list.append(target_vec)
            else:
                dropped += 1

        h_res = 1 if hs > ascore else (-1 if hs < ascore else 0)
        a_res = 1 if ascore > hs else (-1 if ascore < hs else 0)
        team_history[home_k].append((ts, hs, ascore, h_res))
        team_history[away_k].append((ts, ascore, hs, a_res))
        team_last_ts[home_k] = ts
        team_last_ts[away_k] = ts

    X_arr = np.array(X_list, dtype=np.float32)
    Y_arr = np.array(Y_list, dtype=np.float32)

    print(f" Extracted Multi-Task Matrix V3 for {sport.upper()}:")
    print(f"   * Matches with full vectors & targets: {len(X_arr):,}")
    print(f"   * Verified Lineups Matched: {lineups_matched:,}")
    q_desc = f" + {q_dim*2 + 1} Qdrant tactical DNA" if q_dim > 0 else ""
    print(f"   * Input Features Shape: {X_arr.shape} ({X_arr.shape[1]} dimensions: 259 macro + 32 lineup/injury decay{q_desc})")
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

    all_sports = ["table-tennis", "cs2", "dota2", "tennis", "basketball", "ice-hockey", "football"]
    target_sports = all_sports if args.sport == "all" else [s.strip() for s in args.sport.split(",") if s.strip()]

    for s in target_sports:
        process_sport_multitask(s, max_files=args.max_files)
    print(f"\n🏁 MULTI-TASK MATRICES (291 DIMS) FOR [{','.join(target_sports)}] EXTRACTED SUCCESSFULLY!")

if __name__ == "__main__":
    main()
