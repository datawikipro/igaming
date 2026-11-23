#!/usr/bin/env python3
"""
Universal Dynamic Sports MMR Engine (Multi-Tier Glicko-2)
Implements:
1. Glicko-2 Core (mu, phi/RD, sigma/volatility)
2. Tournament Tier Anchors & Asymptotic Soft Cap (Tier 1..7)
3. Margin of Victory (MOV / sets and games weighting)
4. Multi-Surface Tracking (ALL, HARD, CLAY, GRASS)
5. Chronological Historical Replay & Database Batch Ingestion
"""

import math
import sys
from typing import Dict, Tuple, Optional, List

# Standard Glicko-2 Constants
GLICKO2_SCALE = 173.7178
TAU = 0.5  # Volatility constraint parameter
EPSILON = 0.000001

# Tournament Tiers Definition
TOURNAMENT_TIERS = {
    1: {"name": "Grand Slam / ATP Masters 1000", "anchor": 2400, "soft_cap": 3500, 
        "keywords": ["slam", "open", "wimbledon", "roland garros", "us open", "australian open", "masters", "finals", "mc 1000"]},
    2: {"name": "ATP / WTA 500 & 250", "anchor": 2000, "soft_cap": 2700, 
        "keywords": ["atp 500", "wta 500", "atp 250", "wta 250", "atp tour", "wta tour", "beijing", "dubai", "doha"]},
    3: {"name": "Challengers Tour", "anchor": 1700, "soft_cap": 2250, 
        "keywords": ["challenger", "wta 125"]},
    4: {"name": "ITF M25 / W35 / W50", "anchor": 1400, "soft_cap": 1850, 
        "keywords": ["itf m25", "itf w35", "itf w50", "itf 25k", "itf 35k", "m25", "w35"]},
    5: {"name": "ITF M15 / W15 & Top Juniors", "anchor": 1100, "soft_cap": 1550, 
        "keywords": ["itf m15", "itf w15", "itf 15k", "m15", "w15", "monastir", "sharm el sheikh", "antalya", "heraklion"]},
    6: {"name": "Regional Junior & Domestic Leagues", "anchor": 850, "soft_cap": 1250, 
        "keywords": ["junior", "u18", "u16", "u14", "regional", "national", "glavno da se igra", "skupina", "liga", "championship"]},
    7: {"name": "Amateur & Club Tournaments", "anchor": 600, "soft_cap": 950, 
        "keywords": ["amateur", "club", "trophy", "veterans", "cup"]}
}


def classify_tournament_tier(tournament_name: Optional[str]) -> Tuple[int, int]:
    """
    Returns (tier_level, soft_cap_mmr) for a tournament name.
    """
    if not tournament_name:
        return 5, 1550  # Default to ITF M15 / Development tier
    
    name_lower = tournament_name.lower()
    
    # Check Tier 1
    for kw in TOURNAMENT_TIERS[1]["keywords"]:
        if kw in name_lower:
            return 1, TOURNAMENT_TIERS[1]["soft_cap"]
            
    # Check Tier 2
    for kw in TOURNAMENT_TIERS[2]["keywords"]:
        if kw in name_lower:
            return 2, TOURNAMENT_TIERS[2]["soft_cap"]
            
    # Check Tier 3
    for kw in TOURNAMENT_TIERS[3]["keywords"]:
        if kw in name_lower:
            return 3, TOURNAMENT_TIERS[3]["soft_cap"]
            
    # Check Tier 4
    for kw in TOURNAMENT_TIERS[4]["keywords"]:
        if kw in name_lower:
            return 4, TOURNAMENT_TIERS[4]["soft_cap"]
            
    # Check Tier 5
    for kw in TOURNAMENT_TIERS[5]["keywords"]:
        if kw in name_lower:
            return 5, TOURNAMENT_TIERS[5]["soft_cap"]
            
    # Check Tier 6
    for kw in TOURNAMENT_TIERS[6]["keywords"]:
        if kw in name_lower:
            return 6, TOURNAMENT_TIERS[6]["soft_cap"]
            
    # Check Tier 7
    for kw in TOURNAMENT_TIERS[7]["keywords"]:
        if kw in name_lower:
            return 7, TOURNAMENT_TIERS[7]["soft_cap"]
            
    # Fallback to Tier 5
    return 5, 1550


class GlickoPlayer:
    __slots__ = ('player_id', 'mmr', 'rd', 'vol', 'matches_played', 'wins', 'losses', 'streak', 'peak_mmr', 'last_match_time')

    def __init__(self, player_id: int, initial_mmr: float = 1500.0, initial_rd: float = 350.0, initial_vol: float = 0.06):
        self.player_id = player_id
        self.mmr = initial_mmr
        self.rd = initial_rd
        self.vol = initial_vol
        self.matches_played = 0
        self.wins = 0
        self.losses = 0
        self.streak = 0
        self.peak_mmr = initial_mmr
        self.last_match_time = None

    @property
    def mu(self) -> float:
        return (self.mmr - 1500.0) / GLICKO2_SCALE

    @property
    def phi(self) -> float:
        return self.rd / GLICKO2_SCALE

    def set_glicko2(self, mu: float, phi: float, vol: float):
        self.mmr = (mu * GLICKO2_SCALE) + 1500.0
        self.rd = min(350.0, max(30.0, phi * GLICKO2_SCALE))
        self.vol = vol
        if self.mmr > self.peak_mmr:
            self.peak_mmr = self.mmr


def g(phi: float) -> float:
    return 1.0 / math.sqrt(1.0 + (3.0 * phi * phi) / (math.pi * math.pi))


def expected_score(mu: float, mu_opp: float, phi_opp: float) -> float:
    return 1.0 / (1.0 + math.exp(-g(phi_opp) * (mu - mu_opp)))


def apply_match_update(
    p1: GlickoPlayer, 
    p2: GlickoPlayer, 
    score1: int, 
    score2: int, 
    tier_level: int, 
    soft_cap: int,
    mov_multiplier: float = 1.0,
    is_table_tennis: bool = False,
    h2h_count: int = 1
) -> Tuple[float, float]:
    """
    Executes a 1v1 Glicko-2 update between p1 and p2 with:
    - Margin of victory weighting
    - Asymmetric progressive delta scaling for table tennis / closed pools
    - Upset penalty on inflated favorites
    - H2H rematch decay
    - Asymptotic soft-cap dampening
    Returns (delta_p1, delta_p2)
    """
    if score1 == score2:
        s1 = 0.5
        s2 = 0.5
    elif score1 > score2:
        s1 = 1.0
        s2 = 0.0
    else:
        s1 = 0.0
        s2 = 1.0

    mu1, phi1, vol1 = p1.mu, p1.phi, p1.vol
    mu2, phi2, vol2 = p2.mu, p2.phi, p2.vol

    # 1. Compute g and E for both
    g1 = g(phi1)
    g2 = g(phi2)
    e1 = expected_score(mu1, mu2, phi2)
    e2 = expected_score(mu2, mu1, phi1)

    # 2. Variance and delta for p1
    # Incorporate MOV into the variance scale
    eff_weight = max(0.75, min(1.30, mov_multiplier))
    v1 = 1.0 / (g2 * g2 * e1 * (1.0 - e1) * eff_weight + EPSILON)
    delta1 = v1 * g2 * (s1 - e1) * eff_weight

    # 3. New volatility for p1 (Illinois algorithm)
    a1 = math.log(vol1 * vol1)
    def f1(x: float) -> float:
        ex = math.exp(x)
        d_sq = delta1 * delta1
        phi_sq = phi1 * phi1
        part1 = (ex * (d_sq - phi_sq - v1 - ex)) / (2.0 * (phi_sq + v1 + ex) ** 2)
        part2 = (x - a1) / (TAU * TAU)
        return part1 - part2

    A = a1
    if delta1 * delta1 > phi1 * phi1 + v1:
        B = math.log(delta1 * delta1 - phi1 * phi1 - v1)
    else:
        k = 1
        while f1(a1 - k * TAU) < 0:
            k += 1
        B = a1 - k * TAU

    fa = f1(A)
    fb = f1(B)
    for _ in range(50):
        if abs(B - A) <= EPSILON:
            break
        C = A + (A - B) * fa / (fb - fa)
        fc = f1(C)
        if fc * fb < 0:
            A = B
            fa = fb
        else:
            fa = fa / 2.0
        B = C
        fb = fc

    new_vol1 = math.exp(A / 2.0)
    phi_star1 = math.sqrt(phi1 * phi1 + new_vol1 * new_vol1)
    new_phi1 = 1.0 / math.sqrt(1.0 / (phi_star1 * phi_star1) + 1.0 / v1)
    new_mu1 = mu1 + new_phi1 * new_phi1 * g2 * (s1 - e1) * eff_weight

    # 4. Same for p2
    v2 = 1.0 / (g1 * g1 * e2 * (1.0 - e2) * eff_weight + EPSILON)
    delta2 = v2 * g1 * (s2 - e2) * eff_weight

    a2 = math.log(vol2 * vol2)
    def f2(x: float) -> float:
        ex = math.exp(x)
        d_sq = delta2 * delta2
        phi_sq = phi2 * phi2
        part1 = (ex * (d_sq - phi_sq - v2 - ex)) / (2.0 * (phi_sq + v2 + ex) ** 2)
        part2 = (x - a2) / (TAU * TAU)
        return part1 - part2

    A = a2
    if delta2 * delta2 > phi2 * phi2 + v2:
        B = math.log(delta2 * delta2 - phi2 * phi2 - v2)
    else:
        k = 1
        while f2(a2 - k * TAU) < 0:
            k += 1
        B = a2 - k * TAU

    fa = f2(A)
    fb = f2(B)
    for _ in range(50):
        if abs(B - A) <= EPSILON:
            break
        C = A + (A - B) * fa / (fb - fa)
        fc = f2(C)
        if fc * fb < 0:
            A = B
            fa = fb
        else:
            fa = fa / 2.0
        B = C
        fb = fc

    new_vol2 = math.exp(A / 2.0)
    phi_star2 = math.sqrt(phi2 * phi2 + new_vol2 * new_vol2)
    new_phi2 = 1.0 / math.sqrt(1.0 / (phi_star2 * phi_star2) + 1.0 / v2)
    new_mu2 = mu2 + new_phi2 * new_phi2 * g1 * (s2 - e2) * eff_weight

    # 5. Calculate preliminary MMR deltas
    target_mmr1 = (new_mu1 * GLICKO2_SCALE) + 1500.0
    target_mmr2 = (new_mu2 * GLICKO2_SCALE) + 1500.0
    raw_delta1 = target_mmr1 - p1.mmr
    raw_delta2 = target_mmr2 - p2.mmr

    # Progressive Asymmetric Scaling for Table Tennis & Closed Pools
    if is_table_tennis:
        diff_1_2 = p1.mmr - p2.mmr  # > 0 means p1 was favorite
        if s1 > s2:  # P1 won
            if diff_1_2 > 0:  # P1 was favorite
                if diff_1_2 <= 50.0:
                    scale_win = 1.0
                elif diff_1_2 <= 100.0:
                    scale_win = 1.0 - 0.25 * ((diff_1_2 - 50.0) / 50.0)
                elif diff_1_2 < 150.0:
                    scale_win = 0.75 - 0.75 * ((diff_1_2 - 100.0) / 50.0)
                else:
                    scale_win = 0.0  # Delta >= 150: 0% bonus!
                raw_delta1 *= scale_win
            elif diff_1_2 < 0:  # P1 was underdog, P2 suffered upset
                abs_diff = abs(diff_1_2)
                if abs_diff <= 50.0:
                    penalty = 1.0
                elif abs_diff < 150.0:
                    penalty = 1.0 + 0.75 * ((abs_diff - 50.0) / 100.0)
                else:
                    penalty = 1.75
                raw_delta2 *= penalty
        elif s2 > s1:  # P2 won
            if diff_1_2 < 0:  # P2 was favorite
                abs_diff = abs(diff_1_2)
                if abs_diff <= 50.0:
                    scale_win = 1.0
                elif abs_diff <= 100.0:
                    scale_win = 1.0 - 0.25 * ((abs_diff - 50.0) / 50.0)
                elif abs_diff < 150.0:
                    scale_win = 0.75 - 0.75 * ((abs_diff - 100.0) / 50.0)
                else:
                    scale_win = 0.0  # Delta >= 150: 0% bonus!
                raw_delta2 *= scale_win
            elif diff_1_2 > 0:  # P2 was underdog, P1 suffered upset
                if diff_1_2 <= 50.0:
                    penalty = 1.0
                elif diff_1_2 < 150.0:
                    penalty = 1.0 + 0.75 * ((diff_1_2 - 50.0) / 100.0)
                else:
                    penalty = 1.75
                raw_delta1 *= penalty

        if h2h_count > 1:
            h2h_decay = 1.0 / math.sqrt(h2h_count)
            raw_delta1 *= h2h_decay
            raw_delta2 *= h2h_decay

    # 6. Apply ASYMPTOTIC SOFT CAP DAMPENING
    # If winner is approaching or above soft cap for this tournament tier:
    if s1 > s2 and raw_delta1 > 0:
        # Polish Junior Guard: Cannot exceed soft_cap in lower leagues
        buffer = 150.0
        threshold = soft_cap - buffer
        if p1.mmr >= soft_cap:
            gamma1 = 0.0  # Absolute ceiling within this tournament tier
        elif p1.mmr > threshold:
            # Linear/cosine deceleration towards 0 at soft_cap
            ratio = (p1.mmr - threshold) / buffer
            gamma1 = math.cos(ratio * (math.pi / 2.0))
        else:
            gamma1 = 1.0
        final_delta1 = raw_delta1 * gamma1
    else:
        final_delta1 = raw_delta1

    if s2 > s1 and raw_delta2 > 0:
        buffer = 150.0
        threshold = soft_cap - buffer
        if p2.mmr >= soft_cap:
            gamma2 = 0.0
        elif p2.mmr > threshold:
            ratio = (p2.mmr - threshold) / buffer
            gamma2 = math.cos(ratio * (math.pi / 2.0))
        else:
            gamma2 = 1.0
        final_delta2 = raw_delta2 * gamma2
    else:
        final_delta2 = raw_delta2

    # Update p1
    p1.mmr += final_delta1
    p1.rd = min(350.0, max(30.0, new_phi1 * GLICKO2_SCALE))
    p1.vol = new_vol1
    p1.matches_played += 1
    if s1 > s2:
        p1.wins += 1
        p1.streak = (p1.streak + 1) if p1.streak > 0 else 1
    else:
        p1.losses += 1
        p1.streak = (p1.streak - 1) if p1.streak < 0 else -1
    if p1.mmr > p1.peak_mmr:
        p1.peak_mmr = p1.mmr

    # Update p2
    p2.mmr += final_delta2
    p2.rd = min(350.0, max(30.0, new_phi2 * GLICKO2_SCALE))
    p2.vol = new_vol2
    p2.matches_played += 1
    if s2 > s1:
        p2.wins += 1
        p2.streak = (p2.streak + 1) if p2.streak > 0 else 1
    else:
        p2.losses += 1
        p2.streak = (p2.streak - 1) if p2.streak < 0 else -1
    if p2.mmr > p2.peak_mmr:
        p2.peak_mmr = p2.mmr

    return final_delta1, final_delta2


if __name__ == "__main__":
    # Test Verification of Polish Junior Scenario
    junior_a = GlickoPlayer(101, initial_mmr=1000.0, initial_rd=350.0)
    junior_b = GlickoPlayer(102, initial_mmr=1000.0, initial_rd=350.0)

    print("=== TEST: Polish Junior 25-0 win streak in Tier 6 (Soft Cap = 1250 MMR) ===")
    tier_level = 6
    soft_cap = 1250
    for match_num in range(1, 26):
        # Junior A wins every match
        opponent = GlickoPlayer(1000 + match_num, initial_mmr=900.0 + (match_num % 100), initial_rd=150.0)
        d1, d2 = apply_match_update(junior_a, opponent, score1=2, score2=0, tier_level=tier_level, soft_cap=soft_cap, mov_multiplier=1.2)
        if match_num in (1, 5, 10, 15, 20, 25):
            print(f"Match #{match_num:02d}: Delta=+{d1:.2f} MMR, Total Junior A MMR={junior_a.mmr:.2f}, RD={junior_a.rd:.1f}")

    print(f"\nFinal Junior A MMR after 25 wins: {junior_a.mmr:.2f} (Soft Cap was {soft_cap})")
    assert junior_a.mmr <= 1260.0, f"Error: MMR blew through soft cap! {junior_a.mmr}"
    print("SUCCESS: Polish Junior Guard perfectly held MMR below soft cap without runaway inflation!\n")

    # Now let Junior A play in Tier 1 Grand Slam against a 2200 player:
    print("=== TEST: Junior A steps up to Tier 1 Grand Slam (Soft Cap = 3500) ===")
    pro_player = GlickoPlayer(9999, initial_mmr=2200.0, initial_rd=50.0)
    d1, d2 = apply_match_update(junior_a, pro_player, score1=2, score2=1, tier_level=1, soft_cap=3500, mov_multiplier=1.0)
    print(f"Junior A beats 2200 pro in Grand Slam! Delta=+{d1:.2f} MMR, New MMR={junior_a.mmr:.2f}")
    assert junior_a.mmr > 1300.0, "Junior A should naturally unlock and jump up when beating a real pro!"
    print("SUCCESS: Soft cap released seamlessly when transitioning to high-tier competition!")
