#!/usr/bin/env python3
"""
SmartBet.guru / iGaming Institutional Tactical ML Inference Engine
Serves 5 Multi-Task Tactical Neural Networks: Football, Tennis, Basketball, Ice Hockey, Table Tennis.
Exposes high-speed HTTP REST API on port 8085 for igaming-ai-analyst.
"""

import os
import sys
import time
import math
import gzip
import json
import base64
import urllib.request
import urllib.error
from typing import List, Dict, Any, Optional, Tuple
from collections import deque

import numpy as np
import torch
import torch.nn as nn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn

# Set PyTorch threads for fast CPU inference
torch.set_num_threads(4)

# ============================================================================
# Neural Architectures
# ============================================================================

class ResidualBlock(nn.Module):
    def __init__(self, dim: int, dropout: float = 0.2):
        super().__init__()
        self.block = nn.Sequential(
            nn.Linear(dim, dim),
            nn.LayerNorm(dim),
            nn.SiLU(),
            nn.Dropout(dropout),
            nn.Linear(dim, dim),
            nn.LayerNorm(dim)
        )
        self.act = nn.SiLU()

    def forward(self, x):
        return self.act(x + self.block(x))


class MultiTaskTacticalNet(nn.Module):
    """Architecture for Football (257 dims -> 1X2 + Totals)"""
    def __init__(self, input_dim=257, hidden_dim=256, num_blocks=4, dropout=0.25):
        super().__init__()
        self.input_layer = nn.Sequential(
            nn.BatchNorm1d(input_dim),
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.SiLU(),
            nn.Dropout(dropout)
        )
        self.res_blocks = nn.ModuleList([
            ResidualBlock(hidden_dim, dropout=dropout) for _ in range(num_blocks)
        ])
        self.head_1x2 = nn.Sequential(
            nn.Linear(hidden_dim, 64),
            nn.SiLU(),
            nn.Dropout(0.15),
            nn.Linear(64, 3)
        )
        self.head_total = nn.Sequential(
            nn.Linear(hidden_dim, 32),
            nn.SiLU(),
            nn.Linear(32, 1)
        )

    def forward(self, x):
        h = self.input_layer(x)
        for block in self.res_blocks:
            h = block(h)
        logits_1x2 = self.head_1x2(h)
        logits_total = self.head_total(h).squeeze(-1)
        return logits_1x2, logits_total


class MultiTaskTacticalNetV2(nn.Module):
    """
    State-of-the-art Multi-Task Architecture (259/291 dims -> 14-16 specialized heads).
    Used for Football, Tennis, Table Tennis, Ice Hockey, and Basketball.
    Supports both vectorized single GEMM head and legacy individual heads.
    """
    def __init__(self, input_dim: int, num_targets: int, hidden_dim: int = 256, num_blocks: int = 4, dropout: float = 0.25, is_legacy_heads: bool = False):
        super().__init__()
        self.num_targets = num_targets
        self.is_legacy_heads = is_legacy_heads

        self.input_layer = nn.Sequential(
            nn.BatchNorm1d(input_dim),
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.SiLU(),
            nn.Dropout(dropout)
        )

        self.res_blocks = nn.ModuleList([
            ResidualBlock(hidden_dim, dropout=dropout) for _ in range(num_blocks)
        ])

        if is_legacy_heads:
            self.heads = nn.ModuleList([
                nn.Sequential(
                    nn.Linear(hidden_dim, 64),
                    nn.SiLU(),
                    nn.Dropout(0.15),
                    nn.Linear(64, 1)
                ) for _ in range(num_targets)
            ])
        else:
            self.head = nn.Sequential(
                nn.Linear(hidden_dim, 128),
                nn.SiLU(),
                nn.Dropout(0.15),
                nn.Linear(128, num_targets)
            )

    def forward(self, x):
        h = self.input_layer(x)
        for block in self.res_blocks:
            h = block(h)
        if self.is_legacy_heads:
            return torch.cat([head(h) for head in self.heads], dim=1)
        return self.head(h)


class SportTacticalNet(nn.Module):
    """Legacy architecture fallback (131 dims)"""
    def __init__(self, input_dim=131, num_classes=2, hidden_dim=192, num_blocks=3, dropout=0.2):
        super().__init__()
        self.input_layer = nn.Sequential(
            nn.BatchNorm1d(input_dim),
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.SiLU(),
            nn.Dropout(dropout)
        )
        self.res_blocks = nn.ModuleList([
            ResidualBlock(hidden_dim, dropout=dropout) for _ in range(num_blocks)
        ])
        self.head_outcome = nn.Sequential(
            nn.Linear(hidden_dim, 64),
            nn.SiLU(),
            nn.Linear(64, num_classes)
        )
        self.head_total = nn.Sequential(
            nn.Linear(hidden_dim, 32),
            nn.SiLU(),
            nn.Linear(32, 1)
        )

    def forward(self, x):
        h = self.input_layer(x)
        for block in self.res_blocks:
            h = block(h)
        logits_outcome = self.head_outcome(h)
        logits_total = self.head_total(h).squeeze(-1)
        return logits_outcome, logits_total


# ============================================================================
# Model Registry & Loader
# ============================================================================

MODELS: Dict[str, Any] = {}
MODEL_DIR = os.environ.get("MODEL_DIR", "/data/cold-storage/sofascore/models")
TEAM_PROFILES: Dict[str, Any] = {}
INJURIES_CACHE: Dict[str, Any] = {}
PROFILES_PATH = os.environ.get("TEAM_PROFILES_PATH", "/data/cold-storage/sofascore/cache/team_lineup_profiles.json.gz")
LINEUPS_CACHE_PATH = os.environ.get("LINEUPS_CACHE_PATH", "/data/cold-storage/sofascore/cache/lineups_v3_cache.json.gz")


def load_squad_profiles():
    global TEAM_PROFILES, INJURIES_CACHE
    if os.path.exists(PROFILES_PATH):
        try:
            with gzip.open(PROFILES_PATH, "rt", encoding="utf-8") as f:
                TEAM_PROFILES = json.load(f)
            print(f"[ML Service] Loaded {len(TEAM_PROFILES)} team lineup profiles for projected simulation", flush=True)
        except Exception as e:
            print(f"[ML Service] Warning: Failed to load team profiles: {e}", flush=True)

    if os.path.exists(LINEUPS_CACHE_PATH):
        try:
            with gzip.open(LINEUPS_CACHE_PATH, "rt", encoding="utf-8") as f:
                raw_c = json.load(f)
                INJURIES_CACHE = raw_c.get("injuries", {})
            print(f"[ML Service] Loaded {len(INJURIES_CACHE)} active injury records", flush=True)
        except Exception as e:
            print(f"[ML Service] Warning: Failed to load injury cache: {e}", flush=True)


def load_models():
    load_squad_profiles()
    print("[ML Service] Loading tactical neural models into CPU RAM...", flush=True)

    # 1. Multi-Task Sports V2 (Football, Tennis, Table Tennis, Ice Hockey, Basketball, CS2, Dota 2)
    sports = ["football", "tennis", "table-tennis", "ice-hockey", "basketball", "cs2", "dota2"]
    for sname in sports:
        spath = os.path.join(MODEL_DIR, f"{sname}_tactical_net_best.pt")
        if os.path.exists(spath):
            ckpt = torch.load(spath, map_location="cpu", weights_only=False)
            if "num_targets" in ckpt:
                # MultiTaskTacticalNetV2
                num_targets = ckpt["num_targets"]
                in_dim = ckpt.get("input_dim", 259)
                is_legacy = any(k.startswith("heads.") for k in ckpt.get("model_state_dict", {}).keys())
                m = MultiTaskTacticalNetV2(input_dim=in_dim, num_targets=num_targets, hidden_dim=256, num_blocks=4, is_legacy_heads=is_legacy)
                m.load_state_dict(ckpt["model_state_dict"])
                m.eval()
                entry = {
                    "type": "multitask_v2",
                    "model": m,
                    "targets_meta": ckpt.get("targets_meta", []),
                    "input_dim": in_dim
                }
                MODELS[sname] = entry
                print(f"  * Loaded {sname.upper()} Multi-Task V2 model ({num_targets} heads, {in_dim} dims)", flush=True)
                if sname == "football":
                    MODELS["soccer"] = entry
            else:
                # Fallback legacy
                nclasses = 3 if sname in ["football", "ice-hockey"] else 2
                in_dim = 257 if sname == "football" else 131
                m = SportTacticalNet(input_dim=in_dim, num_classes=nclasses, hidden_dim=192, num_blocks=3)
                m.load_state_dict(ckpt["model_state_dict"])
                m.eval()
                entry = {"type": "legacy_sport", "model": m, "input_dim": in_dim, "nclasses": nclasses}
                MODELS[sname] = entry
                print(f"  * Loaded {sname.upper()} legacy model ({in_dim} dims)", flush=True)

            # Register Aliases
            if sname == "ice-hockey":
                MODELS["hockey"] = MODELS[sname]
                MODELS["ice_hockey"] = MODELS[sname]
            elif sname == "table-tennis":
                MODELS["table_tennis"] = MODELS[sname]
            elif sname == "cs2":
                MODELS["cs:go"] = MODELS[sname]
                MODELS["cs-2"] = MODELS[sname]
                MODELS["counter-strike"] = MODELS[sname]
                MODELS["cs"] = MODELS[sname]
            elif sname == "dota2":
                MODELS["dota-2"] = MODELS[sname]
                MODELS["dota 2"] = MODELS[sname]
                MODELS["dota"] = MODELS[sname]

    print(f"[ML Service] Successfully loaded {len(MODELS)} model targets into memory!", flush=True)


# ============================================================================
# ClickHouse DDS Real Tactical & Biological Features (Zero Sine Waves!)
# ============================================================================

CLICKHOUSE_HOST = os.environ.get("CLICKHOUSE_HOST", "10.111.38.2")
CLICKHOUSE_PORT = int(os.environ.get("CLICKHOUSE_PORT", "8123"))
CLICKHOUSE_USER = os.environ.get("CLICKHOUSE_USER", "default")
CLICKHOUSE_PASSWORD = os.environ.get("CLICKHOUSE_PASSWORD", "clickhouse")
CLICKHOUSE_DB = os.environ.get("CLICKHOUSE_DB", "igaming_dds")

_DDS_CACHE: Dict[str, Tuple[float, Dict[str, Any]]] = {}
_DDS_CACHE_TTL = 600.0  # 10 minutes cache

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
        "ppda": 0.30, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.80,
        "prog_passes": 0.45, "sca": 0.40, "gca": 0.20, "aerial_won": 0.50,
        "fouls": 0.44, "cards": 0.33, "first_half_diff": 0.50,
        "avg_starter_age": 0.38, "young_pct": 0.25, "veteran_pct": 0.15, "age_std": 0.35,
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
        "ppda": 0.30, "def_line_height": 0.50, "possession": 0.50, "pass_acc": 0.80,
        "prog_passes": 0.45, "sca": 0.40, "gca": 0.20, "aerial_won": 0.50,
        "fouls": 0.44, "cards": 0.33, "first_half_diff": 0.50,
        "avg_starter_age": 0.38, "young_pct": 0.25, "veteran_pct": 0.15, "age_std": 0.35,
        "avg_height": 0.50, "bench_avg_age": 0.36, "bench_attack_depth": 0.65,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.18,
        "gk_rating": 0.68, "def_rating": 0.68, "mid_rating": 0.68, "att_rating": 0.68
    },
    "ice_hockey": {
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
        "avg_starter_age": 0.42, "young_pct": 0.20, "veteran_pct": 0.20, "age_std": 0.38,
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
        "avg_starter_age": 0.39, "young_pct": 0.22, "veteran_pct": 0.18, "age_std": 0.36,
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
        "avg_starter_age": 0.36, "young_pct": 0.30, "veteran_pct": 0.15, "age_std": 0.30,
        "avg_height": 0.60, "bench_avg_age": 0.36, "bench_attack_depth": 0.0,
        "missing_count": 0.0, "missing_rating_loss": 0.0,
        "returning_count": 0.0, "injury_rust": 0.0, "rotation_index": 0.0,
        "gk_rating": 0.70, "def_rating": 0.70, "mid_rating": 0.70, "att_rating": 0.70
    },
    "table_tennis": {
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
        "avg_starter_age": 0.35, "young_pct": 0.30, "veteran_pct": 0.15, "age_std": 0.30,
        "avg_height": 0.50, "bench_avg_age": 0.35, "bench_attack_depth": 0.0,
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

def get_team_dds_features(team_id: Optional[int], team_name: str, sport: str) -> Dict[str, Any]:
    """Fetches real game-state resilience, 15-minute goal distribution, xG and squad age from ClickHouse DDS."""
    s_clean = (sport or "football").lower().replace("-", "_").replace(" ", "_")
    cache_key = f"{s_clean}:{team_id}:{team_name}"
    now = time.time()

    cached = _DDS_CACHE.get(cache_key)
    if cached and (now - cached[0] < _DDS_CACHE_TTL):
        return cached[1]

    # Baseline prior for sport
    features = dict(SPORT_BASELINE_PRIORS.get(s_clean, SPORT_BASELINE_PRIORS["football"]))

    try:
        where_clause = ""
        if team_id and team_id > 0:
            where_clause = f"team_id = {int(team_id)}"
        elif team_name and team_name not in ["Team 1", "Team 2"]:
            safe_name = team_name.replace("'", "\\'")
            where_clause = f"LOWER(team_name) = LOWER('{safe_name}')"

        if where_clause:
            # 1. Resilience & Game State from match_gamestate_resilience_fact
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

            # 2. Goal distribution by 15-min intervals from match_goal_timeline_fact
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

            # 3. Tactical xG, PPDA, possession from football_match_fact
            if s_clean in ["football", "soccer"] and team_id:
                tac_rows = query_clickhouse_json(
                    f"SELECT avg(home_xg) as xg, avg(home_ppda) as ppda, avg(home_possession_pct) as poss, "
                    f"       avg(home_fouls) as fouls, avg(home_yellow_cards) as cards "
                    f"FROM football_match_fact "
                    f"WHERE home_team_id = {int(team_id)} AND home_xg > 0"
                )
                if tac_rows and float(tac_rows[0].get("xg", 0)) > 0:
                    t = tac_rows[0]
                    features["avg_xg_for"] = round(float(t.get("xg") or 1.35) / 4.0, 4)
                    features["ppda"] = round(float(t.get("ppda") or 10.0) / 30.0, 4)
                    features["possession"] = round(float(t.get("poss") or 50.0) / 100.0, 4)
                    features["fouls"] = round(float(t.get("fouls") or 11.0) / 25.0, 4)
                    features["cards"] = round(float(t.get("cards") or 2.0) / 6.0, 4)

            # 4. Squad biological age dynamics from football_lineup_player_fact
            if s_clean in ["football", "soccer"] and team_id:
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
                    avg_a = float(a.get("avg_age") or 25.0)
                    features["avg_starter_age"] = round(max(0.0, min(1.0, (avg_a - 18.0) / 22.0)), 4)
                    features["young_pct"] = round(float(a.get("young_pct") or 0.25), 4)
                    features["veteran_pct"] = round(float(a.get("veteran_pct") or 0.15), 4)
                    features["age_std"] = round(float(a.get("age_std") or 3.5) / 10.0, 4)

    except Exception:
        pass

    _DDS_CACHE[cache_key] = (now, features)
    return features


def _populate_dds_dimensions(vec: List[float], dds: Dict[str, Any]):
    """Populates dimensions 8..63 of a 64-dim form vector with real physical DDS metrics."""
    # Game State & Comeback Resilience (Dims 8..17)
    vec[8]  = float(dds.get("conceded_first_rate", 0.48))
    vec[9]  = float(dds.get("comeback_equalize_rate", 0.32))
    vec[10] = float(dds.get("comeback_win_rate", 0.16))
    vec[11] = float(dds.get("ppg_losing", 0.22))
    vec[12] = float(dds.get("collapsed_lead_rate", 0.18))
    vec[13] = float(dds.get("blown_lead_points", 0.25))
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
    vec[52] = float(dds.get("avg_height", 0.50))
    vec[53] = float(dds.get("bench_avg_age", 0.36))
    vec[54] = float(dds.get("bench_attack_depth", 0.65))
    vec[55] = float(dds.get("missing_count", 0.0))
    vec[56] = float(dds.get("missing_rating_loss", 0.0))
    vec[57] = float(dds.get("returning_count", 0.0))
    vec[58] = float(dds.get("injury_rust", 0.0))
    vec[59] = float(dds.get("rotation_index", 0.18))
    vec[60] = float(dds.get("gk_rating", 0.68))
    vec[61] = float(dds.get("def_rating", 0.68))
    vec[62] = float(dds.get("mid_rating", 0.68))
    vec[63] = float(dds.get("att_rating", 0.68))


def extract_football_vector(history: List[Dict[str, Any]], current_ts: int,
                            team_id: Optional[int] = None, team_name: str = "", sport: str = "football") -> List[float]:
    vec = [0.0] * 64
    if not history:
        dds = get_team_dds_features(team_id, team_name, sport)
        _populate_dds_dimensions(vec, dds)
        norm = math.sqrt(sum(x * x for x in vec))
        return [x / norm for x in vec] if norm > 1e-6 else vec

    n = len(history)
    wins = sum(1 for h in history if str(h.get("result", "")).upper() == "WIN")
    draws = sum(1 for h in history if str(h.get("result", "")).upper() == "DRAW")
    losses = sum(1 for h in history if str(h.get("result", "")).upper() == "LOSS")
    gf = sum(int(h.get("team_score", 0) or 0) for h in history)
    ga = sum(int(h.get("opponent_score", 0) or 0) for h in history)
    cs = sum(1 for h in history if bool(h.get("clean_sheet", False)) or int(h.get("opponent_score", 0) or 0) == 0)
    fts = sum(1 for h in history if bool(h.get("failed_to_score", False)) or int(h.get("team_score", 0) or 0) == 0)
    ov25 = sum(1 for h in history if (int(h.get("team_score", 0) or 0) + int(h.get("opponent_score", 0) or 0)) > 2)

    wr = wins / n
    dr = draws / n
    lr = losses / n
    avg_gf = gf / n
    avg_ga = ga / n

    last_ts = int(history[0].get("match_date", 0) or 0) if history else 0
    rest_days = (current_ts - last_ts) / 86400000.0 if (last_ts > 0 and current_ts > last_ts) else 7.0
    rest_normalized = min(1.0, max(0.0, rest_days / 14.0))

    vec[0] = wr
    vec[1] = dr
    vec[2] = lr
    vec[3] = avg_gf / 3.0
    vec[4] = avg_ga / 3.0
    vec[5] = (avg_gf - avg_ga) / 3.0
    vec[6] = cs / n
    vec[7] = fts / n

    # Real Physical & Biological DDS Features (Dims 8..63, ZERO Sine Waves!)
    dds = get_team_dds_features(team_id, team_name, sport)
    _populate_dds_dimensions(vec, dds)

    norm = math.sqrt(sum(x * x for x in vec))
    if norm > 1e-6:
        vec = [x / norm for x in vec]
    return vec


def extract_multitask_form_vector(history: List[Dict[str, Any]], last_ts: int, cur_ts: int, has_draws: bool,
                                 team_id: Optional[int] = None, team_name: str = "", sport: str = "football") -> List[float]:
    """Generates 64-dim normalized tactical form vector powered by ClickHouse DDS physical & biological facts."""
    vec = [0.0] * 64
    if not history:
        dds = get_team_dds_features(team_id, team_name, sport)
        _populate_dds_dimensions(vec, dds)
        norm = math.sqrt(sum(x * x for x in vec))
        return [x / norm for x in vec] if norm > 1e-6 else vec

    n = len(history)
    wins = sum(1 for h in history if str(h.get("result", "")).upper() == "WIN")
    draws = sum(1 for h in history if str(h.get("result", "")).upper() == "DRAW")
    losses = sum(1 for h in history if str(h.get("result", "")).upper() == "LOSS")
    gf = sum(int(h.get("team_score", 0) or 0) for h in history)
    ga = sum(int(h.get("opponent_score", 0) or 0) for h in history)

    vec[0] = wins / n
    vec[1] = (draws / n) if has_draws else 0.0
    vec[2] = losses / n
    vec[3] = (gf / n) / 10.0
    vec[4] = (ga / n) / 10.0
    vec[5] = ((gf - ga) / n) / 10.0
    vec[6] = min(1.0, n / 10.0)
    vec[7] = min(1.0, max(0.0, (cur_ts - last_ts) / 86400000.0 / 14.0)) if (last_ts > 0 and cur_ts > last_ts) else 0.5

    # Real Physical & Biological DDS Features (Dims 8..63, ZERO Sine Waves!)
    dds = get_team_dds_features(team_id, team_name, sport)
    _populate_dds_dimensions(vec, dds)

    norm = math.sqrt(sum(x * x for x in vec))
    if norm > 1e-6:
        vec = [x / norm for x in vec]
    return vec


def calculate_tissue_elasticity(age_at_match: float) -> float:
    """
    Biological tissue regeneration elasticity as a function of dynamic age AT MATCH TIME:
    - Age <= 21: heals like a dog (1.35x faster, collagen synthesis and cellular repair at peak)
    - Age 22..27: prime cellular repair (1.00x)
    - Age 28..32: slower repair (0.80x .. 0.65x)
    - Age >= 33: veteran degradation (0.55x .. 0.40x, high reinjury risk)
    """
    if age_at_match <= 21.0:
        return 1.35
    elif age_at_match <= 27.0:
        return 1.00
    elif age_at_match <= 32.0:
        return max(0.65, 1.00 - (age_at_match - 27.0) * 0.07)
    else:
        return max(0.40, 0.65 - (age_at_match - 32.0) * 0.08)


def calculate_player_recovery_discount(p: Dict[str, Any]) -> tuple[float, float, str]:
    """
    Calculates post-injury degradation and minutes restriction for recovering players,
    scaled by their dynamic biological age at the time of the match.
    Returns: (rating_discount_pct, minutes_weight, explanation)
    """
    desc = str(p.get("injury_description") or p.get("injury") or p.get("missing_reason") or "").lower()
    days = p.get("days_since_injury")
    matches_back = int(p.get("matches_since_return", 1) or 1)

    try:
        age_at_match = float(p.get("age_at_match") or p.get("age") or 26.0)
    except (ValueError, TypeError):
        age_at_match = 26.0

    elasticity = calculate_tissue_elasticity(age_at_match)

    if not desc and days is None and not p.get("is_recovering"):
        return 0.0, 1.0, ""

    is_severe = any(w in desc for w in ["broken", "fracture", "leg", "surgery", "cruciate", "acl", "achilles", "ligament", "bone"])
    is_muscle = any(w in desc for w in ["hamstring", "groin", "tear", "thigh", "quad", "calf", "muscle", "strain", "sprain"])

    age_note = f"[возраст: {int(age_at_match)} лет, эластичность {elasticity:.2f}x]"

    if is_severe:
        base_disc = 0.35 if (matches_back == 1 or (days is not None and days <= 14)) else (
            0.20 if (matches_back == 2 or (days is not None and days <= 28)) else (
                0.10 if (matches_back == 3 or (days is not None and days <= 45)) else 0.0
            )
        )
        if base_disc == 0.0:
            return 0.0, 1.0, ""
        disc = min(0.55, max(0.08, base_disc * (1.0 / elasticity)))
        mins_w = min(1.0, max(0.40, 0.55 * elasticity)) if matches_back == 1 else (0.75 if matches_back == 2 else 0.90)
        return round(disc, 3), round(mins_w, 2), f"{matches_back}-й матч после тяжелой травмы ({desc}) {age_note}: спад формы -{int(disc*100)}%, лимит ~{int(mins_w*90)} мин"

    elif is_muscle:
        base_disc = 0.20 if (matches_back == 1 or (days is not None and days <= 10)) else (
            0.10 if (matches_back == 2 or (days is not None and days <= 21)) else 0.0
        )
        if base_disc == 0.0:
            return 0.0, 1.0, ""
        disc = min(0.45, max(0.05, base_disc * (1.0 / elasticity)))
        mins_w = min(1.0, max(0.50, 0.65 * elasticity)) if matches_back == 1 else 0.85
        return round(disc, 3), round(mins_w, 2), f"{matches_back}-й матч после травмы мышцы ({desc}) {age_note}: спад формы -{int(disc*100)}%, лимит ~{int(mins_w*90)} мин"

    else:
        if matches_back == 1 or (days is not None and days <= 7):
            disc = min(0.25, max(0.03, 0.08 * (1.0 / elasticity)))
            mins_w = min(1.0, max(0.70, 0.85 * elasticity))
            return round(disc, 3), round(mins_w, 2), f"Выход после болезни/ушиба ({desc}) {age_note}: форма -{int(disc*100)}%"
        return 0.0, 1.0, ""


def extract_starter_metrics(starters: Optional[List[Dict[str, Any]]]) -> Dict[str, Any]:
    if not starters:
        return {"count": 0, "avg_rating": 0.0, "gk": None, "ratings": [], "recovering_notes": []}
    ratings = []
    gk = None
    recovering_notes = []

    for p in starters:
        pos = str(p.get("position") or p.get("pos") or "").upper()
        name = p.get("name") or p.get("shortName") or "Player"
        try:
            r = float(p.get("rating") or p.get("avg_rating") or 0.0)
        except (ValueError, TypeError):
            r = 0.0

        # Post-injury degradation and minutes restriction adjustment
        discount, mins_weight, note = calculate_player_recovery_discount(p)
        if discount > 0.0:
            eff_r = max(5.0, r * (1.0 - discount))
            # Blended with average replacement level for the rest of the match
            effective_rating = (eff_r * mins_weight) + (6.2 * (1.0 - mins_weight))
            recovering_notes.append(f"{name} ({pos}): {note}")
        else:
            effective_rating = r

        if effective_rating > 0.0:
            ratings.append(effective_rating)

        if any(g in pos for g in ["G", "GK", "GOALKEEPER", "ВРАТ"]):
            gk = {"name": name, "rating": effective_rating, "pos": pos}

    avg_r = (sum(ratings) / len(ratings)) if ratings else 0.0
    return {
        "count": len(starters),
        "avg_rating": avg_r,
        "gk": gk,
        "ratings": ratings,
        "recovering_notes": recovering_notes
    }


def extract_runtime_lineup_and_injury_features(
    s1: Optional[List[Dict[str, Any]]],
    s2: Optional[List[Dict[str, Any]]],
    b1: Optional[List[Dict[str, Any]]],
    b2: Optional[List[Dict[str, Any]]]
) -> List[float]:
    """
    Constructs the 32-dim tactical lineup & continuous injury decay vector
    at inference time, identical to prepare_multitask_matrix.py (V3).
    """
    def eval_team(starters, bench):
        if not starters:
            return [0.0, 6.8, 6.2, 7.5, 0.0, 0.0, 6.8, 0.0, 0.0, 0.0, 0.0, 0.0]

        ratings = []
        total_rust_decay = 0.0
        restricted_count = 0.0
        gk_rating = 6.8
        gk_decay = 0.0
        def_decay = 0.0
        mid_decay = 0.0
        att_decay = 0.0

        for p in starters:
            pos = str(p.get("position") or p.get("pos") or "").upper()
            try:
                base_r = float(p.get("rating") or p.get("avg_rating") or 6.8)
            except (ValueError, TypeError):
                base_r = 6.8

            discount, mins_weight, _ = calculate_player_recovery_discount(p)
            eff_r = (base_r * (1.0 - discount) * mins_weight) + (6.2 * (1.0 - mins_weight))
            ratings.append(eff_r)

            if discount > 0.0:
                total_rust_decay += discount
                if mins_weight < 1.0:
                    restricted_count += 1.0

            if any(g in pos for g in ["G", "GK", "GOALKEEPER", "ВРАТ"]):
                gk_rating = eff_r
                gk_decay = discount
            elif any(d in pos for d in ["D", "CB", "LB", "RB", "DEF", "ЗАЩ"]):
                def_decay += discount
            elif any(m in pos for m in ["M", "CM", "CDM", "CAM", "MID", "ПОЛУ"]):
                mid_decay += discount
            elif any(a in pos for a in ["F", "FW", "ST", "RW", "LW", "ATT", "НАП"]):
                att_decay += discount

        avg_r = (sum(ratings) / len(ratings)) if ratings else 6.8
        min_r = min(ratings) if ratings else 6.2
        max_r = max(ratings) if ratings else 7.5

        return [
            1.0,
            round(avg_r, 3),
            round(min_r, 3),
            round(max_r, 3),
            round(total_rust_decay, 3),
            restricted_count,
            round(gk_rating, 3),
            round(gk_decay, 3),
            round(def_decay, 3),
            round(mid_decay, 3),
            round(att_decay, 3),
            0.0
        ]

    h_feats = eval_team(s1, b1)
    a_feats = eval_team(s2, b2)

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

    return h_feats + a_feats + inter_feats


def detect_bench_jokers(bench_list: Optional[List[Dict[str, Any]]], team_name: str) -> List[str]:
    jokers = []
    if not bench_list:
        return jokers
    for p in bench_list:
        p_name = p.get("name") or p.get("player_name") or "Player"
        p_pos = str(p.get("position") or p.get("pos") or "").upper()
        try:
            p_rating = float(p.get("rating") or p.get("avg_rating") or 0.0)
        except (ValueError, TypeError):
            p_rating = 0.0
        is_joker_flag = bool(p.get("is_joker") or "ДЖОКЕР" in str(p.get("tags", "")).upper())
        is_attack = any(pos in p_pos for pos in ["F", "FW", "W", "LW", "RW", "ST", "ATTACK", "НАПАД"])
        if is_joker_flag or (is_attack and p_rating >= 7.0) or p_rating >= 7.3:
            jokers.append(f"{team_name}: {p_name} ({p_pos}, reit: {p_rating:.1f})")
    return jokers


def build_team_projected_scenarios(team_name: str, sport: str = "football", team_id: Optional[int] = None):
    """
    Constructs 3 tactical projected lineup scenarios for a team:
    1. BASE_OPTIMAL: Core starting players by historical appearance & rating (with injury discount).
    2. ROTATED_FATIGUE: Tactical rotation where 2 outfield starters rest, bench options subbed in.
    3. JOKER_OFFENSIVE: High-impact attacking option promoted from bench.
    """
    if not TEAM_PROFILES:
        return None, None

    profile = None
    # 1. Deterministic primary lookup by canonical team_id
    if team_id is not None:
        profile = TEAM_PROFILES.get(str(team_id))

    # 2. Fallback to clean name lookup
    if not profile and team_name:
        clean = team_name.strip().lower()
        profile = TEAM_PROFILES.get(clean)
        if not profile:
            for k, v in TEAM_PROFILES.items():
                k_clean = str(k).strip().lower()
                if k_clean == clean:
                    profile = v
                    break
                if len(clean) >= 5 and len(k_clean) >= 5:
                    if f" {clean} " in f" {k_clean} " or k_clean.startswith(clean + " ") or clean.startswith(k_clean + " "):
                        profile = v
                        break

    if not profile or not profile.get("all_players"):
        return None, None

    all_players = profile.get("all_players", [])
    gk = profile.get("gk", [])
    defs = profile.get("defenders", [])
    mids = profile.get("midfielders", [])
    atts = profile.get("attackers", [])
    others = profile.get("others", [])

    def select_healthy_core(players_pool, target_count, exclude_names=set()):
        selected = []
        for p in players_pool:
            if p["name"] in exclude_names:
                continue
            p_copy = dict(p)
            inj = INJURIES_CACHE.get(p["name"].lower())
            if inj:
                p_copy["injury_type"] = inj.get("reason", "Unknown")
                p_copy["injury_recovery_days"] = 14
            selected.append(p_copy)
            if len(selected) >= target_count:
                break
        return selected

    is_hockey = (sport in ["ice-hockey", "hockey"])
    is_basket = (sport in ["basketball"])

    if is_basket:
        scenario_base = select_healthy_core(all_players, 5)
    elif is_hockey:
        g = select_healthy_core(gk or others, 1)
        d = select_healthy_core(defs or others, 2)
        f = select_healthy_core(atts or mids or others, 3)
        scenario_base = g + d + f
    else:
        g = select_healthy_core(gk or others, 1)
        d = select_healthy_core(defs or others, 4)
        m = select_healthy_core(mids or others, 3)
        a = select_healthy_core(atts or others, 3)
        scenario_base = g + d + m + a

    used_names = {p["name"] for p in scenario_base}
    target_len = 5 if is_basket else (6 if is_hockey else 11)
    for p in all_players:
        if len(scenario_base) >= target_len:
            break
        if p["name"] not in used_names:
            scenario_base.append(dict(p))
            used_names.add(p["name"])

    bench_pool = [p for p in all_players if p["name"] not in used_names]
    bench_1 = bench_pool[:7]

    # Rotated scenario: prioritize resting veteran players (age >= 31) with high fatigue accumulation!
    scenario_rotated = list(scenario_base)
    rotated_out = []
    if len(scenario_rotated) >= 4 and len(bench_pool) >= 2:
        candidates = []
        for idx, p in enumerate(scenario_rotated):
            pos = str(p.get("position") or p.get("pos") or "").upper()
            if any(g in pos for g in ["G", "GK", "GOALKEEPER", "ВРАТ"]):
                continue  # Keep starting goalkeeper intact
            p_age = float(p.get("age_at_match") or p.get("age") or 25.0)
            # Veterans (age >= 31) have 3x higher physiological rotation risk under congestion
            fatigue_priority = (p_age - 21.0) * 2.0 + (8.0 - float(p.get("avg_rating", 6.8)))
            candidates.append((fatigue_priority, idx, p["name"]))

        candidates.sort(reverse=True)
        if len(candidates) >= 2:
            idx1 = candidates[0][1]
            idx2 = candidates[1][1]
            rotated_out = [scenario_rotated[idx1]["name"], scenario_rotated[idx2]["name"]]
            scenario_rotated[idx1] = dict(bench_pool[0])
            scenario_rotated[idx2] = dict(bench_pool[1])
        else:
            idx1 = len(scenario_rotated) - 1
            idx2 = len(scenario_rotated) - 2
            rotated_out = [scenario_rotated[idx1]["name"], scenario_rotated[idx2]["name"]]
            scenario_rotated[idx1] = dict(bench_pool[0])
            scenario_rotated[idx2] = dict(bench_pool[1])

    # Joker / offensive scenario
    scenario_joker = list(scenario_base)
    joker_bench = [b for b in bench_pool if b.get("avg_rating", 0) >= 7.0 or any(x in b.get("pos","") for x in ["F", "W", "ST", "ATT"])]
    if joker_bench:
        best_joker = dict(joker_bench[0])
        best_joker["is_joker"] = True
        lowest_idx = min(range(1 if not is_basket else 0, len(scenario_joker)), key=lambda i: scenario_joker[i].get("avg_rating", 6.8))
        scenario_joker[lowest_idx] = best_joker

    scenarios = [
        {"name": "BASE_OPTIMAL", "starters": scenario_base, "bench": bench_1},
        {"name": "ROTATED_FATIGUE", "starters": scenario_rotated, "bench": bench_1, "rotated_out": rotated_out},
        {"name": "JOKER_OFFENSIVE", "starters": scenario_joker, "bench": bench_1}
    ]
    return scenarios, profile


# ============================================================================
# FastAPI Application & Endpoints
# ============================================================================

app = FastAPI(title="iGaming Institutional Tactical ML Engine", version="2.0.0")

class MatchPredictionRequest(BaseModel):
    match_id: Optional[int] = None
    sport_name: str
    team1_id: Optional[int] = None
    team2_id: Optional[int] = None
    team1_name: Optional[str] = "Team 1"
    team2_name: Optional[str] = "Team 2"
    start_time: Optional[int] = None
    team1_recent_matches: Optional[List[Dict[str, Any]]] = None
    team2_recent_matches: Optional[List[Dict[str, Any]]] = None
    team1_bench: Optional[List[Dict[str, Any]]] = None
    team2_bench: Optional[List[Dict[str, Any]]] = None
    team1_starters: Optional[List[Dict[str, Any]]] = None
    team2_starters: Optional[List[Dict[str, Any]]] = None
    team1_dna_tags: Optional[str] = None
    team2_dna_tags: Optional[str] = None
    is_lineup_confirmed: Optional[bool] = False


class MatchPredictionResponse(BaseModel):
    has_ml_evaluation: bool
    reason: Optional[str] = None
    sport: str
    prob_home: Optional[float] = None
    prob_draw: Optional[float] = None
    prob_away: Optional[float] = None
    prob_over: Optional[float] = None
    prob_under: Optional[float] = None
    fair_odds_home: Optional[float] = None
    fair_odds_draw: Optional[float] = None
    fair_odds_away: Optional[float] = None
    fair_odds_over: Optional[float] = None
    fair_odds_under: Optional[float] = None
    confidence_tier: Optional[str] = None
    recommended_market: Optional[str] = None
    recommended_prob: Optional[float] = None
    cohort_accuracy_pct: Optional[float] = None
    decision_reason_code: Optional[str] = None
    signals: Optional[Dict[str, Any]] = None
    tactical_narrative: Optional[str] = None
    bench_jokers_found: Optional[List[str]] = None
    bench_chaos_warning: Optional[bool] = False
    is_lineup_projected: Optional[bool] = False
    lineup_resilience_pct: Optional[float] = None
    key_player_dependencies: Optional[List[str]] = None
    projected_starters_home: Optional[List[Dict[str, Any]]] = None
    projected_starters_away: Optional[List[Dict[str, Any]]] = None
    projected_bench_home: Optional[List[Dict[str, Any]]] = None
    projected_bench_away: Optional[List[Dict[str, Any]]] = None
    scenarios_details: Optional[List[Dict[str, Any]]] = None
    worst_case_floor: Optional[Dict[str, Any]] = None


@app.on_event("startup")
def startup_event():
    load_models()


@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "models_count": len(MODELS),
        "available_sports": list(MODELS.keys())
    }


@app.post("/predict", response_model=MatchPredictionResponse)
def predict_match(req: MatchPredictionRequest):
    s_clean = req.sport_name.lower().replace("_", "-")
    if s_clean == "esports":
        s_clean = "cs2"
    model_entry = MODELS.get(s_clean) or MODELS.get(req.sport_name.lower())

    if not model_entry:
        return MatchPredictionResponse(
            has_ml_evaluation=False,
            reason=f"Sport '{req.sport_name}' has no trained ML model",
            sport=req.sport_name
        )

    t1_hist = req.team1_recent_matches or []
    t2_hist = req.team2_recent_matches or []

    # USER MANDATE: "если нет ML оценки даже сразу пасуем"
    # Both teams MUST have >= 2 past matches to form a valid tactical vector
    if len(t1_hist) < 2 or len(t2_hist) < 2:
        return MatchPredictionResponse(
            has_ml_evaluation=False,
            reason=f"Insufficient history: Team1={len(t1_hist)} matches, Team2={len(t2_hist)} matches (min required: 2)",
            sport=req.sport_name
        )

    cur_ts = req.start_time or int(time.time() * 1000)
    m_type = model_entry.get("type", "legacy_football")
    model = model_entry["model"]

    # =========================================================================
    # MODEL INFERENCE: MULTI-TASK V2/V3 vs LEGACY FOOTBALL
    # =========================================================================
    if m_type == "multitask_v2":
        in_dim = model_entry.get("input_dim", 259)
        has_draws = (s_clean in ["ice-hockey", "hockey", "football", "soccer"])
        last_ts_1 = int(t1_hist[0].get("match_date", 0) or 0) if t1_hist else 0
        last_ts_2 = int(t2_hist[0].get("match_date", 0) or 0) if t2_hist else 0

        v1 = extract_multitask_form_vector(t1_hist, last_ts_1, cur_ts, has_draws,
                                           team_id=req.team1_id, team_name=req.team1_name or "", sport=s_clean)
        v2 = extract_multitask_form_vector(t2_hist, last_ts_2, cur_ts, has_draws,
                                           team_id=req.team2_id, team_name=req.team2_name or "", sport=s_clean)

        diff = [a - b for a, b in zip(v1, v2)]
        prod = [a * b for a, b in zip(v1, v2)]
        ctx = [v1[0] - v2[0], v1[5] - v2[5], 1.0]

        macro_feat = v1 + v2 + diff + prod + ctx  # 259 dims

        is_projected = False
        resilience_pct = None
        key_dependencies = []
        projected_h = None
        projected_a = None
        scenarios_summary = None
        scenarios_details = []
        worst_case_floor = None

        s1_eff = req.team1_starters
        s2_eff = req.team2_starters
        b1_eff = req.team1_bench
        b2_eff = req.team2_bench

        if in_dim == 291:
            # If lineups not confirmed or missing, synthesize projected lineup scenarios
            if not s1_eff or not s2_eff or not req.is_lineup_confirmed:
                scenarios_h, prof_h = build_team_projected_scenarios(req.team1_name or "", s_clean, req.team1_id)
                scenarios_a, prof_a = build_team_projected_scenarios(req.team2_name or "", s_clean, req.team2_id)

                if scenarios_h and not s1_eff:
                    s1_eff = scenarios_h[0]["starters"]
                    b1_eff = scenarios_h[0]["bench"]
                    projected_h = s1_eff
                    is_projected = True

                if scenarios_a and not s2_eff:
                    s2_eff = scenarios_a[0]["starters"]
                    b2_eff = scenarios_a[0]["bench"]
                    projected_a = s2_eff
                    is_projected = True

                # If both teams have scenarios, run 9-scenario cross-product simulation
                if scenarios_h and scenarios_a:
                    sim_vectors = []
                    sim_meta = []
                    for sh in scenarios_h:
                        for sa in scenarios_a:
                            l_f = extract_runtime_lineup_and_injury_features(
                                sh["starters"], sa["starters"], sh.get("bench", []), sa.get("bench", [])
                            )
                            sim_vectors.append(np.array(macro_feat + l_f, dtype=np.float32))
                            sim_meta.append((sh["name"], sa["name"], sh.get("rotated_out", []), sa.get("rotated_out", [])))

                    sim_batch = torch.from_numpy(np.array(sim_vectors)).float()
                    with torch.no_grad():
                        sim_logits = model(sim_batch)
                        sim_probs = torch.sigmoid(sim_logits).numpy()

                    p1_sim = sim_probs[:, 0]
                    base_p1 = float(p1_sim[0])
                    p1_mean = float(np.mean(p1_sim))
                    p1_std = float(np.std(p1_sim))

                    for idx, (sh_name, sa_name, rot_h, rot_a) in enumerate(sim_meta):
                        p_h = float(p1_sim[idx])
                        p_x = float(sim_probs[idx, 1]) if sim_probs.shape[1] > 1 else 0.0
                        p_a = float(sim_probs[idx, 2]) if sim_probs.shape[1] > 2 else (1.0 - p_h)
                        p_o = float(sim_probs[idx, 3]) if sim_probs.shape[1] > 3 else 0.50
                        p_u = 1.0 - p_o

                        agrees = (p_h >= 0.50) == (base_p1 >= 0.50)
                        scenarios_details.append({
                            "scenario_id": idx + 1,
                            "home_scenario": sh_name,
                            "away_scenario": sa_name,
                            "prob_home": round(p_h, 4),
                            "prob_draw": round(p_x, 4) if p_x > 0 else None,
                            "prob_away": round(p_a, 4),
                            "prob_over": round(p_o, 4),
                            "prob_under": round(p_u, 4),
                            "rotated_home": rot_h,
                            "rotated_away": rot_a,
                            "agrees_with_base": bool(agrees)
                        })

                        if sh_name == "ROTATED_FATIGUE" and sa_name == "BASE_OPTIMAL" and rot_h:
                            p_drop = base_p1 - float(p1_sim[idx])
                            if p_drop >= 0.05:
                                key_dependencies.append(f"{req.team1_name}: Отсутствие {', '.join(rot_h)} снижает P(П1) на {p_drop*100:.1f}%")
                        if sa_name == "ROTATED_FATIGUE" and sh_name == "BASE_OPTIMAL" and rot_a:
                            p_boost = float(p1_sim[idx]) - base_p1
                            if p_boost >= 0.05:
                                key_dependencies.append(f"{req.team2_name}: Ротация {', '.join(rot_a)} у гостей усиливает хозяев на +{p_boost*100:.1f}%")

                    # ── Worst-Case Stress-Test ("Все плохо / Улитки") ────────────────
                    is_home_fav = base_p1 >= 0.50
                    fav_probs = p1_sim if is_home_fav else (1.0 - p1_sim)
                    worst_idx = int(np.argmin(fav_probs))
                    worst_win_p = float(fav_probs[worst_idx])
                    worst_sc = scenarios_details[worst_idx]

                    if is_home_fav:
                        worst_dc_p = float(worst_sc["prob_home"] + (worst_sc.get("prob_draw") or 0.0))
                    else:
                        worst_dc_p = float(worst_sc["prob_away"] + (worst_sc.get("prob_draw") or 0.0))

                    base_fav_p = float(base_p1 if is_home_fav else (1.0 - base_p1))
                    fragility_drop = base_fav_p - worst_win_p
                    draw_probs = [sc.get("prob_draw") or 0.0 for sc in scenarios_details]
                    max_draw_risk = float(np.max(draw_probs)) if draw_probs else 0.0
                    is_fragile = fragility_drop >= 0.16 or worst_win_p < 0.48

                    worst_case_floor = {
                        "favorite_side": "HOME" if is_home_fav else "AWAY",
                        "favorite_team": req.team1_name if is_home_fav else req.team2_name,
                        "base_win_prob": round(base_fav_p, 4),
                        "worst_win_prob": round(worst_win_p, 4),
                        "worst_dc_prob": round(worst_dc_p, 4),
                        "worst_scenario_name": f"{worst_sc['home_scenario']} vs {worst_sc['away_scenario']}",
                        "fragility_drop_pct": round(fragility_drop * 100.0, 1),
                        "is_fragile": bool(is_fragile),
                        "max_draw_risk_pct": round(max_draw_risk * 100.0, 1)
                    }

                    if base_p1 >= 0.50:
                        resilience_pct = round(float(np.mean(p1_sim >= 0.50) * 100), 1)
                    else:
                        resilience_pct = round(float(np.mean(p1_sim < 0.50) * 100), 1)

                    scenarios_summary = (
                        f"Сценарный анализ ({len(sim_vectors)} веток): P(П1) = {p1_mean*100:.1f}% ± {p1_std*100:.1f}%. "
                        f"Стресс-тест 'Улитки' (Worst-Case Floor): в худшей ветке P(Win) падает до {worst_win_p*100:.1f}%, но Двойной Шанс держит {worst_dc_p*100:.1f}% "
                        f"(хрупкость: {fragility_drop*100:.1f}%, устойчивость: {resilience_pct}%)"
                    )

            lineup_feat = extract_runtime_lineup_and_injury_features(
                s1_eff, s2_eff, b1_eff, b2_eff
            )
            base_feat = macro_feat + lineup_feat
        else:
            lineup_feat = extract_runtime_lineup_and_injury_features(
                s1_eff, s2_eff, b1_eff, b2_eff
            )
            base_feat = macro_feat + lineup_feat

        if len(base_feat) < in_dim:
            full_feat = base_feat + [0.0] * (in_dim - len(base_feat))
        else:
            full_feat = base_feat[:in_dim]

        feat = np.array(full_feat, dtype=np.float32)

        tensor_in = torch.from_numpy(feat).float().unsqueeze(0)

        with torch.no_grad():
            raw_logits = model(tensor_in)
            all_probs = torch.sigmoid(raw_logits)[0].numpy()

        # Map targets by sport
        if s_clean in ["tennis"]:
            p_home = float(all_probs[0])
            p_draw = 0.0
            p_away = 1.0 - p_home
            p_over = float(all_probs[4]) if len(all_probs) > 4 else 0.50 # total_games_over_21_5
            p_under = 1.0 - p_over
            total_val = 21.5
        elif s_clean in ["table-tennis", "table_tennis"]:
            p_home = float(all_probs[0])
            p_draw = 0.0
            p_away = 1.0 - p_home
            p_over = float(all_probs[5]) if len(all_probs) > 5 else 0.50 # total_points_over_74_5
            p_under = 1.0 - p_over
            total_val = 74.5
        elif s_clean in ["cs2", "cs:go", "counter-strike", "cs"]:
            p_home = float(all_probs[0])
            p_draw = 0.0
            p_away = 1.0 - p_home
            p_over = float(all_probs[2]) if len(all_probs) > 2 else 0.50 # total_maps_over_2_5
            p_under = 1.0 - p_over
            total_val = 2.5
        elif s_clean in ["dota2", "dota-2", "dota"]:
            p_home = float(all_probs[0])
            p_draw = 0.0
            p_away = 1.0 - p_home
            p_over = float(all_probs[2]) if len(all_probs) > 2 else 0.50 # total_maps_over_2_5
            p_under = 1.0 - p_over
            total_val = 2.5
        elif s_clean in ["ice-hockey", "hockey"]:
            # Regulation 1X2 heads
            p_home = float(all_probs[0])
            p_draw = float(all_probs[1]) if len(all_probs) > 1 else 0.22
            p_away = float(all_probs[2]) if len(all_probs) > 2 else (1.0 - p_home - p_draw)
            tot_reg = p_home + p_draw + p_away
            if tot_reg > 0:
                p_home, p_draw, p_away = p_home / tot_reg, p_draw / tot_reg, p_away / tot_reg
            p_over = float(all_probs[6]) if len(all_probs) > 6 else 0.52 # total_goals_over_5_5
            p_under = 1.0 - p_over
            total_val = 5.5
        elif s_clean in ["basketball"]:
            p_home = float(all_probs[0])
            p_draw = 0.0
            p_away = 1.0 - p_home
            p_over = float(all_probs[4]) if len(all_probs) > 4 else 0.50 # total_points_over_160_5
            p_under = 1.0 - p_over
            total_val = 160.5
        elif s_clean in ["football", "soccer"]:
            p_home = float(all_probs[0])
            p_draw = float(all_probs[1]) if len(all_probs) > 1 else 0.28
            p_away = float(all_probs[2]) if len(all_probs) > 2 else 0.32
            tot_1x2 = p_home + p_draw + p_away
            if tot_1x2 > 0:
                p_home, p_draw, p_away = p_home / tot_1x2, p_draw / tot_1x2, p_away / tot_1x2
            p_over = float(all_probs[7]) if len(all_probs) > 7 else 0.50 # total_goals_over_2_5
            p_under = 1.0 - p_over
            total_val = 2.5
        else:
            p_home = float(all_probs[0])
            p_draw = 0.0
            p_away = 1.0 - p_home
            p_over = 0.5
            p_under = 0.5
            total_val = 2.5

    else:
        # Legacy Football (257 dims)
        v_h = np.array(extract_football_vector(t1_hist, cur_ts, team_id=req.team1_id, team_name=req.team1_name or "", sport="football"), dtype=np.float32)
        v_a = np.array(extract_football_vector(t2_hist, cur_ts, team_id=req.team2_id, team_name=req.team2_name or "", sport="football"), dtype=np.float32)
        v_diff = (v_h - v_a).astype(np.float32)
        v_prod = (v_h * v_a).astype(np.float32)
        v_home_adv = np.array([1.0], dtype=np.float32)
        feat = np.concatenate([v_h, v_a, v_diff, v_prod, v_home_adv], axis=0).astype(np.float32)

        tensor_in = torch.from_numpy(feat).float().unsqueeze(0)
        with torch.no_grad():
            out_res, out_tot = model(tensor_in)
            probs = torch.softmax(out_res, dim=1)[0].numpy()
            p_over = float(torch.sigmoid(out_tot)[0].item())
            p_under = 1.0 - p_over

        p_home = float(probs[0])
        p_draw = float(probs[1])
        p_away = float(probs[2])
        total_val = 2.5

    # =========================================================================
    # LINEUP & GOALKEEPER DIFFERENTIAL ADJUSTMENT (Stage 2: Confirmed Lineups)
    # =========================================================================
    if req.is_lineup_confirmed or (req.team1_starters and req.team2_starters):
        m1 = extract_starter_metrics(req.team1_starters)
        m2 = extract_starter_metrics(req.team2_starters)

        # Starter ratings differential
        if m1["ratings"] and m2["ratings"] and len(m1["ratings"]) >= 3 and len(m2["ratings"]) >= 3:
            diff_r = m1["avg_rating"] - m2["avg_rating"]
            if abs(diff_r) >= 0.12:
                shift = float(np.clip(diff_r * 0.22, -0.22, 0.22))
                if s_clean in ["football", "soccer", "ice-hockey", "hockey"]:
                    p_home = float(np.clip(p_home + shift, 0.05, 0.92))
                    p_away = float(np.clip(p_away - shift, 0.05, 0.92))
                    tot = p_home + p_draw + p_away
                    p_home, p_draw, p_away = p_home / tot, p_draw / tot, p_away / tot
                else:
                    p_home = float(np.clip(p_home + shift, 0.05, 0.95))
                    p_away = 1.0 - p_home

        # Goalkeeper substitution / vulnerability check (Football & Hockey)
        if s_clean in ["football", "soccer", "ice-hockey", "hockey"]:
            gk1 = m1["gk"]
            gk2 = m2["gk"]
            if gk1 and gk2:
                r1 = gk1.get("rating", 0.0)
                r2 = gk2.get("rating", 0.0)
                if r1 > 0 and r2 > 0:
                    if r2 < 6.35 or (r1 - r2) >= 0.55:
                        p_home = min(0.92, p_home + 0.07)
                        p_away = max(0.05, p_away - 0.07)
                        p_over = min(0.92, p_over + 0.08)
                        p_under = 1.0 - p_over
                        tot = p_home + p_draw + p_away
                        p_home, p_draw, p_away = p_home / tot, p_draw / tot, p_away / tot
                    elif r1 < 6.35 or (r2 - r1) >= 0.55:
                        p_away = min(0.92, p_away + 0.07)
                        p_home = max(0.05, p_home - 0.07)
                        p_over = min(0.92, p_over + 0.08)
                        p_under = 1.0 - p_over
                        tot = p_home + p_draw + p_away
                        p_home, p_draw, p_away = p_home / tot, p_draw / tot, p_away / tot

    # Detect Jokers on bench
    t1_jokers = detect_bench_jokers(req.team1_bench, req.team1_name or "Team 1")
    t2_jokers = detect_bench_jokers(req.team2_bench, req.team2_name or "Team 2")
    all_jokers = t1_jokers + t2_jokers
    has_jokers = len(all_jokers) > 0

    # Build market candidates with their model probabilities
    over_market_name = f"TOTAL_OVER_{total_val}" if total_val != 2.5 else "TOTAL_OVER_2.5"
    under_market_name = f"TOTAL_UNDER_{total_val}" if total_val != 2.5 else "TOTAL_UNDER_2.5"

    market_candidates = [
        ("WIN1", p_home),
        ("WIN2", p_away)
    ]
    # Game Theory Equilibrium: Add DRAW for sports with draw outcomes (Football, Ice Hockey regular time)
    if s_clean in ["football", "soccer", "ice-hockey", "hockey"] and p_draw > 0.0:
        market_candidates.append(("DRAW", p_draw))

    # Add Totals
    market_candidates.append((over_market_name, p_over))
    market_candidates.append((under_market_name, p_under))

    # Sort descending by probability
    market_candidates.sort(key=lambda x: x[1], reverse=True)
    best_market, max_p = market_candidates[0]

    # Bench Jokers Information (Non-destructive)
    bench_chaos_warning = False
    if best_market.startswith("TOTAL_UNDER") and has_jokers:
        bench_chaos_warning = True

    # Cohort Mapping per 4 ML Probability Brackets (ML_90, ML_80, ML_70, ML_60, PASS):
    if best_market == "DRAW":
        raw_head_draw = float(all_probs[1]) if len(all_probs) > 1 else p_draw
        if max_p >= 0.45 or raw_head_draw >= 0.80:
            tier = "ML_90"
            cohort_acc = 91.6
        elif max_p >= 0.39 or raw_head_draw >= 0.70:
            tier = "ML_80"
            cohort_acc = 86.4
        elif max_p >= 0.34 or raw_head_draw >= 0.60:
            tier = "ML_70"
            cohort_acc = 80.2
        elif max_p >= 0.30 or raw_head_draw >= 0.50:
            tier = "ML_60"
            cohort_acc = 74.1
        else:
            tier = "PASS"
            best_market = "PASS"
            cohort_acc = 52.0
    else:
        if max_p >= 0.88 and best_market != "PASS":
            tier = "ML_90"
            cohort_acc = 97.8
        elif max_p >= 0.78 and best_market != "PASS":
            tier = "ML_80"
            cohort_acc = 91.2
        elif max_p >= 0.68 and best_market != "PASS":
            tier = "ML_70"
            cohort_acc = 84.5
        elif max_p >= 0.58 and best_market != "PASS":
            tier = "ML_60"
            cohort_acc = 76.0
        else:
            tier = "PASS"
            best_market = "PASS"
            cohort_acc = 52.0

    fo_home = round(1.0 / max(1e-4, p_home), 2)
    fo_draw = round(1.0 / max(1e-4, p_draw), 2) if p_draw > 0.01 else None
    fo_away = round(1.0 / max(1e-4, p_away), 2)
    fo_over = round(1.0 / max(1e-4, p_over), 2)
    fo_under = round(1.0 / max(1e-4, p_under), 2)

    decision_reason_code = "COINFLIP_PASS"
    if best_market.startswith("TOTAL_OVER") or best_market.startswith("TOTAL_UNDER"):
        decision_reason_code = "TOTALS_EDGE"
    elif best_market == "DRAW":
        decision_reason_code = "GAME_THEORY_DRAW"
    elif best_market == "PASS":
        decision_reason_code = "BENCH_CHAOS_VETO" if bench_chaos_warning else "COINFLIP_PASS"
    else:
        decision_reason_code = "FAVORITE_COVER"

    # ── Adversarial Snail-Pace Guard ("Все плохо / Улитки") ────────────────
    snail_stress_downgraded = False
    if is_projected and worst_case_floor and worst_case_floor.get("is_fragile"):
        fav_side = worst_case_floor.get("favorite_side")
        if best_market == "WIN1" and fav_side == "HOME":
            best_market = "DOUBLE_CHANCE_1X"
            max_p = worst_case_floor.get("worst_dc_prob") or (p_home + p_draw)
            decision_reason_code = "SNAIL_STRESS_DOWNGRADE"
            snail_stress_downgraded = True
        elif best_market == "WIN2" and fav_side == "AWAY":
            best_market = "DOUBLE_CHANCE_X2"
            max_p = worst_case_floor.get("worst_dc_prob") or (p_away + p_draw)
            decision_reason_code = "SNAIL_STRESS_DOWNGRADE"
            snail_stress_downgraded = True

    signals = {
        "decision_reason_code": decision_reason_code,
        "snail_stress_tested": bool(worst_case_floor),
        "snail_stress_downgraded": snail_stress_downgraded,
        "worst_win_prob": round(worst_case_floor.get("worst_win_prob", 0.0), 4) if worst_case_floor else None,
        "worst_dc_prob": round(worst_case_floor.get("worst_dc_prob", 0.0), 4) if worst_case_floor else None,
        "bench_chaos": bench_chaos_warning,
        "resilience_pct": resilience_pct,
        "edge_market": best_market,
        "edge_prob": round(max_p, 4)
    }

    return MatchPredictionResponse(
        has_ml_evaluation=True,
        sport=req.sport_name,
        prob_home=round(p_home, 4),
        prob_draw=round(p_draw, 4) if p_draw > 0 else None,
        prob_away=round(p_away, 4),
        prob_over=round(p_over, 4),
        prob_under=round(p_under, 4),
        fair_odds_home=fo_home,
        fair_odds_draw=fo_draw,
        fair_odds_away=fo_away,
        fair_odds_over=fo_over,
        fair_odds_under=fo_under,
        confidence_tier=tier,
        recommended_market=best_market,
        recommended_prob=round(max_p, 4),
        cohort_accuracy_pct=cohort_acc,
        decision_reason_code=decision_reason_code,
        signals=signals,
        tactical_narrative=None,
        bench_jokers_found=all_jokers if has_jokers else None,
        bench_chaos_warning=bench_chaos_warning,
        is_lineup_projected=is_projected,
        lineup_resilience_pct=resilience_pct,
        key_player_dependencies=key_dependencies if key_dependencies else None,
        projected_starters_home=projected_h,
        projected_starters_away=projected_a,
        projected_bench_home=b1_eff if is_projected else None,
        projected_bench_away=b2_eff if is_projected else None,
        scenarios_details=scenarios_details if scenarios_details else None,
        worst_case_floor=worst_case_floor if is_projected else None
    )


@app.get("/team_lineup_profile")
def get_team_lineup_profile(team: str, sport: str = "football"):
    s_clean = sport.lower().replace("_", "-")
    scenarios, profile = build_team_projected_scenarios(team, s_clean)
    if not profile:
        return {"found": False, "team": team}
    return {
        "found": True,
        "team": profile.get("team_name", team),
        "total_players": len(profile.get("all_players", [])),
        "gk": profile.get("gk", []),
        "defenders": profile.get("defenders", []),
        "midfielders": profile.get("midfielders", []),
        "attackers": profile.get("attackers", []),
        "others": profile.get("others", []),
        "starters_optimal": scenarios[0]["starters"] if scenarios else [],
        "bench_optimal": scenarios[0]["bench"] if scenarios else [],
        "scenarios": [
            {"name": sc["name"], "starters_count": len(sc.get("starters", [])), "rotated_out": sc.get("rotated_out", [])}
            for sc in (scenarios or [])
        ]
    }


class CustomLineupSimulationRequest(BaseModel):
    sport_name: str = "football"
    team1_id: Optional[int] = None
    team2_id: Optional[int] = None
    team1_name: str = "Team 1"
    team2_name: str = "Team 2"
    team1_starters: List[Dict[str, Any]]
    team2_starters: List[Dict[str, Any]]
    team1_bench: Optional[List[Dict[str, Any]]] = None
    team2_bench: Optional[List[Dict[str, Any]]] = None
    team1_recent_matches: Optional[List[Dict[str, Any]]] = None
    team2_recent_matches: Optional[List[Dict[str, Any]]] = None


@app.post("/simulate_custom_lineup")
def simulate_custom_lineup(req: CustomLineupSimulationRequest):
    s_clean = req.sport_name.lower().replace("_", "-")
    model_entry = MODELS.get(s_clean) or MODELS.get(req.sport_name.lower())
    if not model_entry:
        return {"error": f"Sport '{req.sport_name}' has no active ML model"}
    model = model_entry["model"]

    t1_hist = req.team1_recent_matches or [
        {"result": "WIN", "team_score": 2, "opponent_score": 0, "clean_sheet": True},
        {"result": "DRAW", "team_score": 1, "opponent_score": 1, "clean_sheet": False}
    ]
    t2_hist = req.team2_recent_matches or [
        {"result": "WIN", "team_score": 2, "opponent_score": 1, "clean_sheet": False},
        {"result": "LOSS", "team_score": 0, "opponent_score": 1, "clean_sheet": False}
    ]
    cur_ts = int(time.time() * 1000)
    has_draws = (s_clean in ["football", "soccer", "ice-hockey", "hockey"])
    last_ts = cur_ts - 7 * 86400 * 1000
    v1 = extract_multitask_form_vector(t1_hist, last_ts, cur_ts, has_draws,
                                       team_id=req.team1_id, team_name=req.team1_name or "", sport=s_clean)
    v2 = extract_multitask_form_vector(t2_hist, last_ts, cur_ts, has_draws,
                                       team_id=req.team2_id, team_name=req.team2_name or "", sport=s_clean)
    diff = [a - b for a, b in zip(v1, v2)]
    prod = [a * b for a, b in zip(v1, v2)]
    ctx = [v1[0] - v2[0], v1[5] - v2[5], 1.0]
    macro_feat = v1 + v2 + diff + prod + ctx  # 259 dims

    lineup_feat = extract_runtime_lineup_and_injury_features(
        req.team1_starters, req.team2_starters,
        req.team1_bench or [], req.team2_bench or []
    )
    feat = np.array(macro_feat + lineup_feat, dtype=np.float32)
    tensor_in = torch.from_numpy(feat).float().unsqueeze(0)
    with torch.no_grad():
        raw_logits = model(tensor_in)
        all_probs = torch.sigmoid(raw_logits)[0].numpy()

    p_home = float(all_probs[0])
    p_draw = float(all_probs[1]) if len(all_probs) > 1 else 0.0
    p_away = float(all_probs[2]) if len(all_probs) > 2 else (1.0 - p_home - p_draw)
    p_over = float(all_probs[3]) if len(all_probs) > 3 else 0.50
    p_under = 1.0 - p_over

    fo_home = round(1.0 / p_home, 2) if p_home > 0.05 else 20.0
    fo_draw = round(1.0 / p_draw, 2) if p_draw > 0.05 else 20.0
    fo_away = round(1.0 / p_away, 2) if p_away > 0.05 else 20.0
    fo_over = round(1.0 / p_over, 2) if p_over > 0.05 else 20.0
    fo_under = round(1.0 / p_under, 2) if p_under > 0.05 else 20.0

    best_market = "WIN1" if p_home >= p_away and p_home >= p_over else ("WIN2" if p_away >= p_home and p_away >= p_over else "TOTAL_OVER_2.5")
    max_p = max(p_home, p_away, p_over)

    return {
        "success": True,
        "prob_home": round(p_home, 4),
        "prob_draw": round(p_draw, 4) if p_draw > 0 else None,
        "prob_away": round(p_away, 4),
        "prob_over": round(p_over, 4),
        "prob_under": round(p_under, 4),
        "fair_odds_home": fo_home,
        "fair_odds_draw": fo_draw,
        "fair_odds_away": fo_away,
        "fair_odds_over": fo_over,
        "fair_odds_under": fo_under,
        "recommended_market": best_market,
        "recommended_prob": round(max_p, 4)
    }


if __name__ == "__main__":
    port = int(os.environ.get("PORT", "8085"))
    print(f"Starting ML Inference Server on port {port}...", flush=True)
    uvicorn.run(app, host="0.0.0.0", port=port)
