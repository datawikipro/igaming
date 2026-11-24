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

try:
    from prepare_multitask_matrix import extract_form_vector as extract_multitask_form_vector
except Exception:
    def extract_multitask_form_vector(*args, **kwargs):
        return [0.0] * 64

from sport_vector_extractors import get_sport_extractor

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
LEADERBOARDS: Dict[str, Dict[str, Any]] = {}
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

    # 1. Multi-Task Sports V2 (Football, Tennis, Table Tennis, Ice Hockey, Basketball, Basketball-48m, CS2, Dota 2, Volleyball, Handball, Futsal, Baseball, American Football)
    sports = [
        "football", "tennis", "table-tennis", "ice-hockey", "basketball", "basketball-48m", "cs2", "dota2",
        "volleyball", "handball", "futsal", "baseball", "american-football"
    ]
    for sname in sports:
        spath = os.path.join(MODEL_DIR, f"{sname}_tactical_net_best.pt")
        if not os.path.exists(spath) and sname == "basketball-48m":
            spath = os.path.join(MODEL_DIR, "basketball_tactical_net_best.pt")
        if os.path.exists(spath):
            ckpt = torch.load(spath, map_location="cpu", weights_only=False)
            if "num_targets" in ckpt:
                # MultiTaskTacticalNetV2
                num_targets = ckpt["num_targets"]
                in_dim = ckpt.get("input_dim", 259)
                temp = float(ckpt.get("temperature", 1.0))
                if temp <= 0.0:
                    temp = 1.0
                is_legacy = any(k.startswith("heads.") for k in ckpt.get("model_state_dict", {}).keys())
                m = MultiTaskTacticalNetV2(input_dim=in_dim, num_targets=num_targets, hidden_dim=256, num_blocks=4, is_legacy_heads=is_legacy)
                m.load_state_dict(ckpt["model_state_dict"])
                m.eval()
                entry = {
                    "type": "multitask_v2",
                    "model": m,
                    "targets_meta": ckpt.get("targets_meta", []),
                    "input_dim": in_dim,
                    "temperature": temp
                }
                MODELS[sname] = entry
                print(f"  * Loaded {sname.upper()} Multi-Task V2 model ({num_targets} heads, {in_dim} dims, temp={temp})", flush=True)
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
            if sname == "basketball":
                MODELS["classic-basketball"] = MODELS[sname]
                if "basketball-48m" not in MODELS:
                    MODELS["basketball-48m"] = MODELS[sname]
            elif sname == "basketball-48m":
                MODELS["basketball_48m"] = MODELS[sname]
            elif sname == "ice-hockey":
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
            elif sname == "american-football":
                MODELS["american_football"] = MODELS[sname]
                MODELS["nfl"] = MODELS[sname]
            elif sname == "table-tennis":
                MODELS["table_tennis"] = MODELS[sname]
                MODELS["ping-pong"] = MODELS[sname]

    # Universal Polymorphic Sub-Sport Family Aliases
    sub_sport_family_map = {
        "basketball-women": "basketball",
        "basketball_women": "basketball",
        "basketball-48m": "basketball",
        "basketball_48m": "basketball",
        "tennis-women": "tennis",
        "tennis_women": "tennis",
        "tennis-doubles": "tennis",
        "tennis_doubles": "tennis",
        "tennis-doubles-women": "tennis",
        "tennis_doubles_women": "tennis",
        "table-tennis-women": "table-tennis",
        "table_tennis_women": "table-tennis",
        "football-women": "football",
        "football_women": "football",
        "soccer-women": "football",
        "soccer_women": "football",
        "ice-hockey-women": "ice-hockey",
        "ice_hockey_women": "ice-hockey",
        "volleyball-women": "volleyball",
        "volleyball_women": "volleyball",
        "handball-women": "handball",
        "handball_women": "handball",
        "futsal-women": "futsal",
        "futsal_women": "futsal"
    }
    for sub, base in sub_sport_family_map.items():
        if sub not in MODELS and base in MODELS:
            MODELS[sub] = MODELS[base]
            if base in LEADERBOARDS and sub not in LEADERBOARDS:
                LEADERBOARDS[sub] = LEADERBOARDS[base]
            print(f"  * Aliased sub-sport '{sub}' -> base model '{base}'", flush=True)

            # Load empirical predictability leaderboard if available
            lb_path = os.path.join(MODEL_DIR, f"{sname}_predictability_leaderboard.json")
            if os.path.exists(lb_path):
                try:
                    with open(lb_path, "r", encoding="utf-8") as lbf:
                        LEADERBOARDS[sname] = json.load(lbf)
                        print(f"  * Loaded {sname.upper()} empirical predictability leaderboard", flush=True)
                except Exception as lbe:
                    print(f"  ! Warning: Failed reading leaderboard for {sname}: {lbe}", flush=True)

    print(f"[ML Service] Successfully loaded {len(MODELS)} model targets into memory!", flush=True)


# ClickHouse DDS feature extraction moved 100% to Java (TacticalTensorBuilder / Rule 1.1)


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
    is_basket = (sport in ["basketball", "basketball-48m", "classic-basketball"])

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
# Continuous Bivariate Probability Matrix & Multi-Scenario Generator
# ============================================================================

def compute_bivariate_probability_matrix(lambda_h: float, lambda_a: float, rho: float = -0.05, max_goals: int = 7) -> Dict[str, Any]:
    """
    Computes exact Dixon-Coles bivariate Poisson probability matrix (up to max_goals x max_goals)
    and projects complete continuous totals (0.5 to 7.5) and Asian handicaps (-3.5 to +3.5).
    """
    lh = max(0.05, min(6.0, float(lambda_h)))
    la = max(0.05, min(6.0, float(lambda_a)))
    
    # 1. Poisson marginals
    p_h = [math.exp(-lh) * (lh ** i) / math.factorial(i) for i in range(max_goals + 1)]
    p_a = [math.exp(-la) * (la ** j) / math.factorial(j) for j in range(max_goals + 1)]
    
    # 2. Joint matrix with Dixon-Coles tau adjustment for low scores
    matrix = np.zeros((max_goals + 1, max_goals + 1), dtype=np.float64)
    for i in range(max_goals + 1):
        for j in range(max_goals + 1):
            tau = 1.0
            if i == 0 and j == 0:
                tau = 1.0 - lh * la * rho
            elif i == 1 and j == 0:
                tau = 1.0 + la * rho
            elif i == 0 and j == 1:
                tau = 1.0 + lh * rho
            elif i == 1 and j == 1:
                tau = 1.0 - rho
            matrix[i, j] = max(0.0, p_h[i] * p_a[j] * tau)
            
    tot_sum = np.sum(matrix)
    if tot_sum > 0:
        matrix = matrix / tot_sum
        
    # 3. Project continuous totals (0.5 to 7.5)
    total_over = {}
    total_under = {}
    total_lines = [0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.5]
    for line in total_lines:
        over_prob = float(np.sum([matrix[i, j] for i in range(max_goals + 1) for j in range(max_goals + 1) if (i + j) > line]))
        total_over[str(line)] = round(over_prob, 4)
        total_under[str(line)] = round(1.0 - over_prob, 4)
        
    # 4. Project continuous Asian handicaps (-3.5 to +3.5)
    hc_home = {}
    hc_away = {}
    hc_lines = [-3.5, -2.5, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.5, 3.5]
    for line in hc_lines:
        home_cov = float(np.sum([matrix[i, j] for i in range(max_goals + 1) for j in range(max_goals + 1) if (i + line) > j]))
        away_cov = float(np.sum([matrix[i, j] for i in range(max_goals + 1) for j in range(max_goals + 1) if (j - line) > i]))
        hc_home[str(line)] = round(home_cov, 4)
        hc_away[str(-line)] = round(away_cov, 4)
        
    # 5. BTTS
    btts_yes = float(np.sum(matrix[1:, 1:]))
    btts_no = float(1.0 - btts_yes)
    
    return {
        "lambda_home": round(lh, 3),
        "lambda_away": round(la, 3),
        "correlation_rho": round(rho, 3),
        "score_matrix": [[round(float(matrix[i, j]), 4) for j in range(max_goals + 1)] for i in range(max_goals + 1)],
        "total_over_probabilities": total_over,
        "total_under_probabilities": total_under,
        "handicap_home_probabilities": hc_home,
        "handicap_away_probabilities": hc_away,
        "btts_yes_prob": round(btts_yes, 4),
        "btts_no_prob": round(btts_no, 4)
    }


def generate_match_scenarios(
    sport: str,
    lambda_h: float,
    lambda_a: float,
    p_h: float,
    p_x: float,
    p_a: float,
    team1_name: str,
    team2_name: str,
    is_derby: bool = False,
    bench_jokers: Optional[List[str]] = None,
    matrix_dict: Optional[Dict[str, Any]] = None
) -> Dict[str, Any]:
    """
    Generates 1 Base Scenario + 2-3 Contingency Branches (Game Tree).
    """
    # 1. Base Scenario: Mode of score matrix
    sm = matrix_dict.get("score_matrix") if matrix_dict else None
    best_h, best_a = 1, 0
    if sm:
        max_p = -1.0
        for i in range(len(sm)):
            for j in range(len(sm[i])):
                if sm[i][j] > max_p:
                    max_p = sm[i][j]
                    best_h, best_a = i, j
    proj_score = f"{best_h}:{best_a}"
    
    fav_name = team1_name if p_h >= p_a else team2_name
    dog_name = team2_name if p_h >= p_a else team1_name
    
    base_prob = round(max(0.48, min(0.72, max(p_h, p_a) + p_x * 0.35)) * 100.0, 1)
    
    base_branch = {
        "branch_id": "BASE_TACTICAL_FLOW",
        "title": f"Базовый сценарий: Контроль темпа ({fav_name})",
        "probability_pct": base_prob,
        "trigger_condition": "Стандартное развитие игры при равных составах",
        "projected_final_score": proj_score,
        "tactical_narrative": f"Ожидается инициатива {fav_name}. При сохранении позиционного рисунка ключевое преимущество реализуется во 2-м тайме.",
        "recommended_market": "DOUBLE_CHANCE_1X" if p_h >= p_a else "DOUBLE_CHANCE_X2",
        "recommended_odd": None,
        "expected_value_pct": None
    }
    
    contingencies = []
    
    # Branch A: Early Underdog Goal
    contingencies.append({
        "branch_id": "UNDERDOG_EARLY_GOAL",
        "title": f"Шок-сценарий: Быстрый гол {dog_name} (до 25')",
        "probability_pct": 22.5,
        "trigger_condition": f"Быстрый гол {dog_name} со стандарта или контратаки",
        "projected_final_score": f"{best_h + 1}:{best_a + 1}",
        "tactical_narrative": f"{fav_name} вынужден поднимать линию прессинга. Игра раскрывается на встречных курсах, резко возрастает вероятность ТБ 2.5 и карточек.",
        "recommended_market": "TOTAL_OVER_2.5",
        "recommended_odd": None,
        "expected_value_pct": None
    })
    
    # Branch B: Disciplinary / Friction Shift
    if is_derby:
        contingencies.append({
            "branch_id": "DERBY_DISCIPLINARY_WAR",
            "title": "Сценарий жесткой борьбы и судейского слома",
            "probability_pct": 28.0,
            "trigger_condition": "3+ желтые карточки в первом тайме или удаление",
            "projected_final_score": "1:1 или 1:0",
            "tactical_narrative": "Повышенная конфликтность сбивает темп позиционных атак. Обилие остановок переводит игру в режим борьбы за стандарты.",
            "recommended_market": "CARDS_OVER_4.5",
            "recommended_odd": None,
            "expected_value_pct": None
        })
    else:
        contingencies.append({
            "branch_id": "LOW_BLOCK_STALEMATE",
            "title": "Сценарий вязкой обороны (Низкий блок)",
            "probability_pct": 24.0,
            "trigger_condition": "0:0 к 60-й минуте матча",
            "projected_final_score": "0:0 или 1:0",
            "tactical_narrative": f"Оборонительный блок {dog_name} успешно нейтрализует передачи в штрафную. Игра переходит в дефицит свободного пространства.",
            "recommended_market": "TOTAL_UNDER_2.5",
            "recommended_odd": None,
            "expected_value_pct": None
        })
        
    # Branch C: Bench Super-Sub Impact
    if bench_jokers and len(bench_jokers) > 0:
        joker_names = ", ".join(bench_jokers[:2])
        contingencies.append({
            "branch_id": "BENCH_JOKER_BURST",
            "title": f"Сценарий взрыва со скамейки ({joker_names})",
            "probability_pct": 31.5,
            "trigger_condition": "Замены на 60-70 минутах против уставшей обороны",
            "projected_final_score": f"{best_h + 1}:{best_a}",
            "tactical_narrative": f"Выход свежих атакующих опций ({joker_names}) взламывает фланг соперника на фоне физического закисления защитников.",
            "recommended_market": "LATE_GOAL_75_90",
            "recommended_odd": None,
            "expected_value_pct": None
        })
        
    return {
        "base_scenario": base_branch,
        "contingency_scenarios": contingencies
    }


# ============================================================================
# FastAPI Application & Endpoints
# ============================================================================

app = FastAPI(title="iGaming Institutional Tactical ML Engine", version="2.0.0")

class MatchPredictionRequest(BaseModel):
    match_id: Optional[int] = None
    sport_name: str
    league_name: Optional[str] = None
    tournament_name: Optional[str] = None
    features: Optional[List[float]] = None  # Pre-computed 291-dim Java polymorphic vector
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
    probability_matrix: Optional[Dict[str, Any]] = None
    scenario_tree: Optional[Dict[str, Any]] = None


class LivePredictionRequest(BaseModel):
    match_id: Optional[int] = None
    sport_name: str = "football"
    minute: int = 0
    score_home: int = 0
    score_away: int = 0
    red_cards_home: Optional[int] = 0
    red_cards_away: Optional[int] = 0
    live_xg_home: Optional[float] = 0.0
    live_xg_away: Optional[float] = 0.0
    shots_on_target_home: Optional[int] = 0
    shots_on_target_away: Optional[int] = 0
    team1_name: Optional[str] = "Хозяева"
    team2_name: Optional[str] = "Гости"
    pre_match_lambda_home: Optional[float] = 1.55
    pre_match_lambda_away: Optional[float] = 1.10
    features: Optional[List[float]] = None


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


@app.post("/reload")
def reload_models_endpoint():
    load_models()
    return {
        "status": "RELOADED",
        "models_count": len(MODELS),
        "available_sports": list(MODELS.keys())
    }


@app.post("/predict", response_model=MatchPredictionResponse)
def predict_match(req: MatchPredictionRequest):
    s_clean = req.sport_name.lower().replace("_", "-")
    if s_clean == "esports":
        s_clean = "cs2"
    model_entry = MODELS.get(s_clean) or MODELS.get(req.sport_name.lower()) or MODELS.get(sub_sport_family_map.get(s_clean)) or MODELS.get(sub_sport_family_map.get(req.sport_name.lower()))

    if not model_entry:
        return MatchPredictionResponse(
            has_ml_evaluation=False,
            reason=f"Sport '{req.sport_name}' has no trained ML model",
            sport=req.sport_name
        )

    t1_hist = req.team1_recent_matches or []
    t2_hist = req.team2_recent_matches or []

    cur_ts = req.start_time or int(time.time() * 1000)
    m_type = model_entry.get("type", "multitask_v2")
    model = model_entry["model"]

    # =========================================================================
    # MODEL INFERENCE: MULTI-TASK V2/V3
    # =========================================================================
    if m_type == "multitask_v2":
        in_dim = model_entry.get("input_dim", 259)
        model_temp = float(model_entry.get("temperature", 1.0))
        if model_temp <= 0.0:
            model_temp = 1.0

        # Assert Pre-Computed Java Feature Tensor Contract (Rule 1.1 / Rule 1.4)
        if req.features is None or len(req.features) < in_dim:
            return MatchPredictionResponse(
                has_ml_evaluation=False,
                reason=f"Pre-computed feature tensor missing or insufficient length (expected >= {in_dim}, received {len(req.features) if req.features else 0}). Java TacticalTensorBuilder contract required.",
                decision_reason_code="MISSING_FEATURE_TENSOR",
                sport=req.sport_name
            )

        extractor = get_sport_extractor(s_clean)
        macro_dim = extractor.macro_dimension
        macro_feat = req.features[:min(macro_dim, len(req.features))]
        base_feat = req.features[:in_dim]

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

        if in_dim == 64:
            base_feat = req.features[:in_dim] if req.features is not None else [0.0] * in_dim
        elif in_dim == 291:
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
                        if model_temp != 1.0:
                            sim_logits = sim_logits / model_temp
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

        if req.features is not None and len(req.features) >= in_dim:
            # 100% Java Polymorphic Vector Engine overrides raw feature concatenation
            base_feat = req.features

        if len(base_feat) < in_dim:
            full_feat = base_feat + [0.0] * (in_dim - len(base_feat))
        else:
            full_feat = base_feat[:in_dim]

        feat = np.array(full_feat, dtype=np.float32)

        tensor_in = torch.from_numpy(feat).float().unsqueeze(0)

        with torch.no_grad():
            raw_logits = model(tensor_in)
            if model_temp != 1.0:
                raw_logits = raw_logits / model_temp
            all_probs = torch.sigmoid(raw_logits)[0].numpy()

        # Map targets by sport
        if s_clean in ["tennis", "tennis-women", "tennis-doubles", "tennis-doubles-women"]:
            p_home = float(all_probs[0])
            p_draw = 0.0
            p_away = 1.0 - p_home
            p_over = float(all_probs[4]) if len(all_probs) > 4 else 0.50 # total_games_over_21_5
            p_under = 1.0 - p_over
            total_val = 20.5 if "women" in s_clean else 21.5
        elif s_clean in ["table-tennis", "table_tennis", "table-tennis-women"]:
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
        elif s_clean in ["basketball-48m"]:
            p_home = float(all_probs[0])
            p_draw = 0.0
            p_away = 1.0 - p_home
            p_over = float(all_probs[4]) if len(all_probs) > 4 else 0.50 # total_points_over_215_5
            p_under = 1.0 - p_over
            total_val = 215.5
        elif s_clean in ["basketball", "classic-basketball"]:
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

    # Empirical Cohort Accuracy from Leaderboard
    lb_data = LEADERBOARDS.get(s_clean, {})
    lb_list = lb_data.get("leaderboard", [])
    head_meta = None
    for entry in lb_list:
        ename = entry.get("name", "").lower()
        if ename == best_market.lower() or (best_market == "WIN1" and "p1" in ename) or (best_market == "DRAW" and "px" in ename):
            head_meta = entry
            break

    # Determine Conviction Tier strictly from probabilities
    if best_market == "DRAW":
        raw_head_draw = float(all_probs[1]) if ('all_probs' in locals() and len(all_probs) > 1) else p_draw
        if max_p >= 0.45 or raw_head_draw >= 0.80:
            tier = "ML_90"
        elif max_p >= 0.39 or raw_head_draw >= 0.70:
            tier = "ML_80"
        elif max_p >= 0.34 or raw_head_draw >= 0.60:
            tier = "ML_70"
        elif max_p >= 0.30 or raw_head_draw >= 0.50:
            tier = "ML_60"
        else:
            tier = "PASS"
            best_market = "PASS"
    else:
        if max_p >= 0.88 and best_market != "PASS":
            tier = "ML_90"
        elif max_p >= 0.78 and best_market != "PASS":
            tier = "ML_80"
        elif max_p >= 0.68 and best_market != "PASS":
            tier = "ML_70"
        elif max_p >= 0.58 and best_market != "PASS":
            tier = "ML_60"
        else:
            tier = "PASS"
            best_market = "PASS"

    if head_meta:
        cohort_acc = float(head_meta.get("acc_high") if max_p >= 0.75 else head_meta.get("acc_mid", head_meta.get("acc_overall", round(max_p * 100, 1))))
    else:
        cohort_acc = round(max_p * 100.0, 1) if best_market != "PASS" else 50.0

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

    # Procedural mutation of best_market removed (Constitution Rule 1.1)
    signals = {
        "decision_reason_code": decision_reason_code,
        "snail_stress_tested": bool(worst_case_floor),
        "snail_stress_downgraded": False,
        "worst_win_prob": round(worst_case_floor.get("worst_win_prob", 0.0), 4) if worst_case_floor else None,
        "worst_dc_prob": round(worst_case_floor.get("worst_dc_prob", 0.0), 4) if worst_case_floor else None,
        "bench_chaos": bench_chaos_warning,
        "resilience_pct": resilience_pct,
        "edge_market": best_market,
        "edge_prob": round(max_p, 4)
    }

    if 'all_probs' in locals() and all_probs is not None and len(all_probs) > 0:
        signals["multi_task_heads"] = [round(float(p), 4) for p in all_probs]
        if s_clean in ["tennis"]:
            signals["prob_set_handicap_minus_1_5"] = round(float(all_probs[1]), 4)
            signals["prob_set_handicap_plus_1_5"] = round(float(all_probs[2]), 4)
            signals["prob_clean_sweep_2_0"] = round(float(all_probs[11]), 4)
            signals["prob_total_sets_over_2_5"] = round(float(all_probs[3]), 4)
        elif s_clean in ["basketball-48m"]:
            signals["prob_spread_minus_7_5"] = round(float(all_probs[1]), 4)
            signals["prob_spread_plus_7_5"] = round(float(all_probs[2]), 4)
            signals["prob_spread_minus_12_5"] = round(float(all_probs[3]), 4)
            signals["prob_clutch"] = round(float(all_probs[11]), 4)
            signals["prob_blowout"] = round(float(all_probs[12]), 4)
            signals["prob_overtime"] = round(float(all_probs[13]), 4)
        elif s_clean in ["basketball", "classic-basketball"]:
            signals["prob_spread_minus_5_5"] = round(float(all_probs[1]), 4)
            signals["prob_spread_plus_5_5"] = round(float(all_probs[2]), 4)
            signals["prob_spread_minus_9_5"] = round(float(all_probs[3]), 4)
            signals["prob_clutch"] = round(float(all_probs[11]), 4)
            signals["prob_blowout"] = round(float(all_probs[12]), 4)
            signals["prob_overtime"] = round(float(all_probs[13]), 4)
        elif s_clean in ["football", "soccer"] and len(all_probs) >= 11:
            signals["prob_double_chance_1x"] = round(float(all_probs[3]), 4)
            signals["prob_double_chance_x2"] = round(float(all_probs[4]), 4)
            signals["prob_handicap_minus_1"] = round(float(all_probs[5]), 4)
            signals["prob_handicap_plus_1"] = round(float(all_probs[6]), 4)
            signals["prob_total_goals_over_2_5"] = round(float(all_probs[7]), 4)
            signals["prob_total_goals_over_1_5"] = round(float(all_probs[8]), 4)
            signals["prob_total_goals_under_3_5"] = round(float(all_probs[9]), 4)
            signals["prob_btts"] = round(float(all_probs[10]), 4)
        elif s_clean in ["ice-hockey", "hockey"] and len(all_probs) >= 9:
            signals["prob_puckline_minus_1_5"] = round(float(all_probs[4]), 4)
            signals["prob_puckline_plus_1_5"] = round(float(all_probs[5]), 4)
            signals["prob_total_goals_over_5_5"] = round(float(all_probs[6]), 4)
            signals["prob_total_goals_over_4_5"] = round(float(all_probs[7]), 4)
            signals["prob_total_goals_under_6_5"] = round(float(all_probs[8]), 4)

    # -------------------------------------------------------------------------
    # Continuous Probability Surface & Multi-Scenario Game Tree
    # -------------------------------------------------------------------------
    if s_clean in ["football", "soccer"] and 'all_probs' in locals() and len(all_probs) >= 11:
        p_to_2_5 = float(all_probs[7])
        p_to_1_5 = float(all_probs[8])
        p_tu_3_5 = float(all_probs[9])
        p_btts = float(all_probs[10])
        p_h_m1 = float(all_probs[5])
        p_a_p1 = float(all_probs[6])
        lam_h = 1.4
        lam_a = 1.1
        prob_matrix = {
            "handicap_home_probabilities": {
                -1.0: round(p_h_m1, 4),
                1.0: round(1.0 - p_a_p1, 4)
            },
            "handicap_away_probabilities": {
                1.0: round(p_a_p1, 4),
                -1.0: round(1.0 - p_h_m1, 4)
            },
            "total_over_probabilities": {
                1.5: round(p_to_1_5, 4),
                2.5: round(p_to_2_5, 4),
                3.5: round(1.0 - p_tu_3_5, 4)
            },
            "total_under_probabilities": {
                1.5: round(1.0 - p_to_1_5, 4),
                2.5: round(1.0 - p_to_2_5, 4),
                3.5: round(p_tu_3_5, 4)
            },
            "btts_yes_prob": round(p_btts, 4),
            "btts_no_prob": round(1.0 - p_btts, 4)
        }
    elif s_clean in ["ice-hockey", "hockey"] and 'all_probs' in locals() and len(all_probs) >= 9:
        p_to_5_5 = float(all_probs[6])
        p_to_4_5 = float(all_probs[7])
        p_tu_6_5 = float(all_probs[8])
        p_h_m1_5 = float(all_probs[4])
        p_a_p1_5 = float(all_probs[5])
        lam_h = 2.8
        lam_a = 2.4
        prob_matrix = {
            "handicap_home_probabilities": {
                -1.5: round(p_h_m1_5, 4),
                1.5: round(1.0 - p_a_p1_5, 4)
            },
            "handicap_away_probabilities": {
                1.5: round(p_a_p1_5, 4),
                -1.5: round(1.0 - p_h_m1_5, 4)
            },
            "total_over_probabilities": {
                4.5: round(p_to_4_5, 4),
                5.5: round(p_to_5_5, 4),
                6.5: round(1.0 - p_tu_6_5, 4)
            },
            "total_under_probabilities": {
                4.5: round(1.0 - p_to_4_5, 4),
                5.5: round(1.0 - p_to_5_5, 4),
                6.5: round(p_tu_6_5, 4)
            }
        }
    elif s_clean in ["table-tennis", "table_tennis"] and 'all_probs' in locals() and len(all_probs) >= 8:
        p_h_set_m1_5 = float(all_probs[1])
        p_a_set_p1_5 = float(all_probs[2])
        p_to_74_5 = float(all_probs[5])
        p_to_68_5 = float(all_probs[6])
        p_tu_80_5 = float(all_probs[7])
        lam_h = 1.5
        lam_a = 1.0
        prob_matrix = {
            "handicap_home_probabilities": {
                -1.5: round(p_h_set_m1_5, 4),
                1.5: round(1.0 - p_a_set_p1_5, 4)
            },
            "handicap_away_probabilities": {
                1.5: round(p_a_set_p1_5, 4),
                -1.5: round(1.0 - p_h_set_m1_5, 4)
            },
            "total_over_probabilities": {
                68.5: round(p_to_68_5, 4),
                74.5: round(p_to_74_5, 4),
                80.5: round(1.0 - p_tu_80_5, 4)
            },
            "total_under_probabilities": {
                68.5: round(1.0 - p_to_68_5, 4),
                74.5: round(1.0 - p_to_74_5, 4),
                80.5: round(p_tu_80_5, 4)
            }
        }
    elif s_clean in ["tennis"] and 'all_probs' in locals() and len(all_probs) >= 7:
        lam_h = 1.5
        lam_a = 1.0
        p_h_clean = float(all_probs[1])
        p_clean_sweep = float(all_probs[11]) if len(all_probs) > 11 else (p_h_clean + 0.1)
        p_a_clean = max(0.001, p_clean_sweep - p_h_clean)
        p_a_plus_1_5 = float(all_probs[2])
        p_h_plus_1_5 = max(0.001, min(0.999, 1.0 - p_a_clean))

        prob_matrix = {
            "handicap_home_probabilities": {
                -1.5: round(p_h_clean, 4),
                1.5: round(p_h_plus_1_5, 4)
            },
            "handicap_away_probabilities": {
                1.5: round(p_a_plus_1_5, 4),
                -1.5: round(p_a_clean, 4)
            },
            "total_over_probabilities": {
                2.5: round(float(all_probs[3]), 4),
                21.5: round(float(all_probs[4]), 4),
                19.5: round(float(all_probs[5]), 4)
            },
            "total_under_probabilities": {
                2.5: round(1.0 - float(all_probs[3]), 4),
                21.5: round(1.0 - float(all_probs[4]), 4),
                23.5: round(float(all_probs[6]), 4)
            }
        }
    elif s_clean in ["basketball-48m"] and 'all_probs' in locals() and len(all_probs) >= 7:
        lam_h = 1.5
        lam_a = 1.0
        prob_matrix = {
            "handicap_home_probabilities": {
                -7.5: round(float(all_probs[1]), 4),
                7.5: round(float(all_probs[2]), 4),
                -12.5: round(float(all_probs[3]), 4)
            },
            "handicap_away_probabilities": {
                7.5: round(1.0 - float(all_probs[1]), 4),
                -7.5: round(1.0 - float(all_probs[2]), 4),
                12.5: round(1.0 - float(all_probs[3]), 4)
            },
            "total_over_probabilities": {
                215.5: round(float(all_probs[4]), 4),
                205.5: round(float(all_probs[5]), 4)
            },
            "total_under_probabilities": {
                215.5: round(1.0 - float(all_probs[4]), 4),
                225.5: round(float(all_probs[6]), 4)
            }
        }
    elif s_clean in ["basketball", "classic-basketball"] and 'all_probs' in locals() and len(all_probs) >= 7:
        lam_h = 1.5
        lam_a = 1.0
        prob_matrix = {
            "handicap_home_probabilities": {
                -5.5: round(float(all_probs[1]), 4),
                5.5: round(float(all_probs[2]), 4),
                -9.5: round(float(all_probs[3]), 4)
            },
            "handicap_away_probabilities": {
                5.5: round(1.0 - float(all_probs[1]), 4),
                -5.5: round(1.0 - float(all_probs[2]), 4),
                9.5: round(1.0 - float(all_probs[3]), 4)
            },
            "total_over_probabilities": {
                160.5: round(float(all_probs[4]), 4),
                150.5: round(float(all_probs[5]), 4)
            },
            "total_under_probabilities": {
                160.5: round(1.0 - float(all_probs[4]), 4),
                170.5: round(float(all_probs[6]), 4)
            }
        }
    elif s_clean in ["cs2", "cs:go", "counter-strike", "cs", "dota2", "dota-2", "dota"] and 'all_probs' in locals() and len(all_probs) >= 4:
        lam_h = 1.5
        lam_a = 1.0
        prob_matrix = {
            "handicap_home_probabilities": {
                -1.5: round(float(all_probs[1]), 4),
                1.5: round(float(all_probs[2]), 4)
            },
            "handicap_away_probabilities": {
                1.5: round(float(all_probs[2]), 4),
                -1.5: round(float(all_probs[1]), 4)
            },
            "total_over_probabilities": {
                2.5: round(float(all_probs[3]), 4)
            },
            "total_under_probabilities": {
                2.5: round(1.0 - float(all_probs[3]), 4)
            }
        }
    else:
        lam_h = 1.5
        lam_a = 1.0
        prob_matrix = {
            "total_over_probabilities": {2.5: round(p_over, 4)},
            "total_under_probabilities": {2.5: round(p_under, 4)}
        }

    scenarios = generate_match_scenarios(
        s_clean, lam_h, lam_a, p_home, p_draw or 0.0, p_away,
        req.team1_name or "Хозяева", req.team2_name or "Гости",
        is_derby=signals.get("is_derby", False),
        bench_jokers=all_jokers if has_jokers else None,
        matrix_dict=prob_matrix
    )

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
        worst_case_floor=worst_case_floor if is_projected else None,
        probability_matrix=prob_matrix,
        scenario_tree=scenarios
    )


# ============================================================================
# Live In-Play Point-in-Time Prediction Endpoint
# ============================================================================

@app.post("/live-predict")
def live_predict_match(req: LivePredictionRequest):
    s_clean = req.sport_name.lower().replace("_", "-")
    minute = max(0, min(120, req.minute))
    rem_time = max(0.02, (90.0 - minute) / 90.0)

    # Fatigue & late-game stretch factor: goals per minute increase late in games
    fatigue_factor = 1.0 + 0.25 * (minute / 90.0)

    lh = req.pre_match_lambda_home or 1.55
    la = req.pre_match_lambda_away or 1.10

    score_diff = req.score_home - req.score_away

    # State modifiers: leading team defends / trailing attacks
    if score_diff > 0:
        gamma_h = max(0.55, 0.90 - 0.12 * score_diff)
        gamma_a = min(1.65, 1.00 + 0.20 * score_diff)
    elif score_diff < 0:
        gamma_h = min(1.65, 1.00 + 0.20 * abs(score_diff))
        gamma_a = max(0.55, 0.90 - 0.12 * abs(score_diff))
    else:
        gamma_h = 1.0
        gamma_a = 1.0

    # Red card modifiers
    if req.red_cards_home > 0:
        gamma_h *= 0.60
        gamma_a *= 1.40
    if req.red_cards_away > 0:
        gamma_a *= 0.60
        gamma_h *= 1.40

    # Live xG friction: if trailing team generated lots of xG, boost comeback
    if req.live_xg_home > 0 and minute >= 20:
        xg_perf_h = req.live_xg_home / max(0.1, req.score_home + 0.2)
        gamma_h *= max(0.85, min(1.25, math.sqrt(xg_perf_h)))
    if req.live_xg_away > 0 and minute >= 20:
        xg_perf_a = req.live_xg_away / max(0.1, req.score_away + 0.2)
        gamma_a *= max(0.85, min(1.25, math.sqrt(xg_perf_a)))

    lam_rem_h = max(0.01, lh * rem_time * fatigue_factor * gamma_h)
    lam_rem_a = max(0.01, la * rem_time * fatigue_factor * gamma_a)

    rem_matrix = compute_bivariate_probability_matrix(lam_rem_h, lam_rem_a, rho=-0.02, max_goals=5)

    # Next goal probabilities
    tot_lam = lam_rem_h + lam_rem_a
    p_no_more = math.exp(-tot_lam)
    p_next_h = (1.0 - p_no_more) * (lam_rem_h / max(0.001, tot_lam))
    p_next_a = (1.0 - p_no_more) * (lam_rem_a / max(0.001, tot_lam))

    next_fav = "NONE" if p_no_more >= max(p_next_h, p_next_a) else ("HOME" if p_next_h >= p_next_a else "AWAY")

    # Find most probable remaining score
    sm = rem_matrix["score_matrix"]
    max_rem_prob = -1.0
    best_rh, best_ra = 0, 0
    for i in range(len(sm)):
        for j in range(len(sm[i])):
            if sm[i][j] > max_rem_prob:
                max_rem_prob = sm[i][j]
                best_rh, best_ra = i, j

    proj_final_h = req.score_home + best_rh
    proj_final_a = req.score_away + best_ra
    proj_score_str = f"{proj_final_h}:{proj_final_a}"

    # Fast Live Scenarios
    live_tree = {
        "base_scenario": {
            "branch_id": "BASE_LIVE_FLOW",
            "title": f"Базовый прогноз остатка ({minute}'): финальный счет {proj_score_str}",
            "probability_pct": round(max(0.40, 1.0 - p_no_more * 0.5) * 100.0, 1),
            "trigger_condition": f"Текущий счет {req.score_home}:{req.score_away} на {minute}-й минуте",
            "projected_final_score": proj_score_str,
            "tactical_narrative": f"Остаток матча ({max(0, 90 - minute)} мин): ожидается {round(lam_rem_h, 2)} голов хозяев и {round(lam_rem_a, 2)} гостей.",
            "recommended_market": "TOTAL_OVER_" + str(req.score_home + req.score_away + 0.5),
            "recommended_odd": None,
            "expected_value_pct": None
        },
        "contingency_scenarios": [
            {
                "branch_id": "LATE_EQUALIZER_PUSH",
                "title": "Сценарий штурма в концовке (75-90+')",
                "probability_pct": round(p_next_a * 100.0 if score_diff > 0 else p_next_h * 100.0, 1),
                "trigger_condition": "Открытие зон проигрывающей командой после 70-й минуты",
                "projected_final_score": f"{req.score_home + (0 if score_diff > 0 else 1)}:{req.score_away + (1 if score_diff > 0 else 0)}",
                "tactical_narrative": "Проигрывающая команда перейдет на максимальный риск и навал.",
                "recommended_market": "BTTS_YES" if (req.score_home == 0 or req.score_away == 0) else "TOTAL_OVER_" + str(req.score_home + req.score_away + 1.5),
                "recommended_odd": None,
                "expected_value_pct": None
            }
        ]
    }

    snapshot = {
        "match_id": req.match_id,
        "sofa_event_id": None,
        "minute": minute,
        "score_home": req.score_home,
        "score_away": req.score_away,
        "red_cards_home": req.red_cards_home,
        "red_cards_away": req.red_cards_away,
        "live_xg_home": req.live_xg_home,
        "live_xg_away": req.live_xg_away,
        "shots_on_target_home": req.shots_on_target_home,
        "shots_on_target_away": req.shots_on_target_away,
        "match_period": "2ND_HALF" if minute > 45 else "1ST_HALF",
        "is_live": True
    }

    return {
        "snapshot": snapshot,
        "remaining_matrix": rem_matrix,
        "projected_final_score": proj_score_str,
        "next_goal_favorite": next_fav,
        "next_goal_prob_home": round(p_next_h, 4),
        "next_goal_prob_away": round(p_next_a, 4),
        "next_goal_prob_none": round(p_no_more, 4),
        "live_scenario_tree": live_tree,
        "top_live_edges": [],
        "executive_summary": f"Матч {minute}' ({req.score_home}:{req.score_away}). Прогноз до конца матча: итоговый счет {proj_score_str}. Вероятность следующего гола: {req.team1_name} {round(p_next_h * 100)}%, {req.team2_name} {round(p_next_a * 100)}%, Без голов {round(p_no_more * 100)}%."
    }


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
    model_entry = MODELS.get(s_clean) or MODELS.get(req.sport_name.lower()) or MODELS.get(sub_sport_family_map.get(s_clean)) or MODELS.get(sub_sport_family_map.get(req.sport_name.lower()))
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
    extractor = get_sport_extractor(s_clean)
    cur_ts = int(time.time() * 1000)
    has_draws = extractor.has_draws
    last_ts = cur_ts - 7 * 86400 * 1000
    v1 = extract_multitask_form_vector(t1_hist, last_ts, cur_ts, has_draws,
                                       team_id=req.team1_id, team_name=req.team1_name or "", sport=s_clean)
    v2 = extract_multitask_form_vector(t2_hist, last_ts, cur_ts, has_draws,
                                       team_id=req.team2_id, team_name=req.team2_name or "", sport=s_clean)
    if extractor.expected_model_dimension == 64:
        feat = np.array(extractor.extract_pure_physics_vector(v1, v2), dtype=np.float32)
    else:
        diff = [a - b for a, b in zip(v1, v2)]
        prod = [a * b for a, b in zip(v1, v2)]
        ctx = extractor.extract_macro_context(v1, v2)
        macro_feat = v1 + v2 + diff + prod + ctx

        lineup_feat = extract_runtime_lineup_and_injury_features(
            req.team1_starters, req.team2_starters,
            req.team1_bench or [], req.team2_bench or []
        )
        feat = np.array(macro_feat + lineup_feat, dtype=np.float32)
    tensor_in = torch.from_numpy(feat).float().unsqueeze(0)
    model_temp = float(model_entry.get("temperature", 1.0))
    if model_temp <= 0.0:
        model_temp = 1.0
    with torch.no_grad():
        raw_logits = model(tensor_in)
        if model_temp != 1.0:
            raw_logits = raw_logits / model_temp
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
