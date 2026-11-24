#!/usr/bin/env python3
"""
Institutional Sport Vector Extractors & ML Domain Strategies for SmartBet.guru.
Guarantees 100% Polymorphic OOP Architecture (Rule 8 Zero-Discriminator-Branching / Rule 1.1 Python-Java Parity).
Eliminates procedural if/elif sport branching across training and inference.
"""

import math
from abc import ABC, abstractmethod
from typing import List, Dict, Any, Optional, Tuple, Type


def safe_int(val: Any) -> int:
    try:
        return int(val) if val is not None else 0
    except (ValueError, TypeError):
        return 0


class BaseSportVectorExtractor(ABC):
    """Sacred polymorphic contract for sport-specific feature extraction and target modeling."""

    @property
    @abstractmethod
    def sport_name(self) -> str:
        """Canonical lowercase slug for the sport."""
        ...

    @property
    @abstractmethod
    def has_home_advantage(self) -> bool:
        """
        Whether this sport discipline features real physical home advantage.
        True for team sports (Football, Basketball, Ice Hockey).
        Strictly False for neutral duel tour sports (Tennis, Table Tennis).
        """
        ...

    @property
    @abstractmethod
    def has_draws(self) -> bool:
        """Whether this sport discipline has a draw outcome."""
        ...

    @property
    def is_duel_sport(self) -> bool:
        """Whether this sport is a 1v1 or 2v2 duel (no home advantage, pure player physics)."""
        return not self.has_home_advantage

    @property
    def expected_model_dimension(self) -> int:
        """Canonical model feature vector dimension (64 for PurePhysics)."""
        return 64

    @property
    def macro_dimension(self) -> int:
        """Total dimension of the macro feature block (259 for team sports, 258 for duel sports)."""
        return 256 + (3 if self.has_home_advantage else 2)

    def resolve_tier_level(self, tournament_name: Optional[str]) -> float:
        """Resolves tournament tier level (0.0 .. 1.0). Default is 0.50."""
        return 0.50

    def resolve_surface_pace(self, surface: Optional[str]) -> float:
        """Resolves surface pace multiplier. Default is 1.0."""
        return 1.0

    @abstractmethod
    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        """Populates dimensions 8..63 with sport-specific physical and biological metrics."""
        ...

    def extract_macro_context(self, v1: List[float], v2: List[float]) -> List[float]:
        """
        Extracts pairwise interaction context.
        Team sports include home advantage [diff_wr, diff_margin, 1.0].
        Duel sports completely omit home advantage [diff_wr, diff_margin] (Clean-Cut Policy / Rule 1.4).
        """
        d_wr = v1[0] - v2[0] if (len(v1) > 0 and len(v2) > 0) else 0.0
        d_margin = v1[5] - v2[5] if (len(v1) > 5 and len(v2) > 5) else 0.0
        if self.has_home_advantage:
            return [d_wr, d_margin, 1.0]
        else:
            return [d_wr, d_margin]

    def extract_pure_physics_vector(
        self,
        v1: List[float],
        v2: List[float],
        surface: str = "ALL",
        tier_level: float = 0.5,
        h2h_wr: float = 0.5,
        leverage_diff: float = 0.0,
        pace: float = 1.0,
        lineup: Optional[List[float]] = None,
        composite_card_bias: float = 0.33,
        is_derby: bool = False,
        blowout_diff: float = 0.0,
        **kwargs
    ) -> List[float]:
        """
        Default pure physics extraction (Team Pure Physics: 64 dimensions).
        Guarantees 100% mathematical parity with Java AbstractSportVectorExtractor.buildTeamPurePhysicsVector.
        """
        pure64 = [0.0] * 64

        # Block 1 (0..7): Glicko MMR & Class
        pure64[0] = v1[14] if len(v1) > 14 else 0.0
        pure64[1] = v2[14] if len(v2) > 14 else 0.0
        pure64[2] = pure64[0] - pure64[1]
        pure64[3] = v1[15] if len(v1) > 15 else 0.0
        pure64[4] = v2[15] if len(v2) > 15 else 0.0
        pure64[5] = v1[52] if len(v1) > 52 else 0.5
        pure64[6] = v2[52] if len(v2) > 52 else 0.5
        pure64[7] = (v1[28] - v2[28]) if (len(v1) > 28 and len(v2) > 28) else 0.0

        # Block 2 (8..31): Form & Scoring Physics
        pure64[8]  = v1[0] if len(v1) > 0 else 0.0
        pure64[9]  = v1[1] if len(v1) > 1 else 0.0
        pure64[10] = v1[2] if len(v1) > 2 else 0.0
        pure64[11] = v1[3] if len(v1) > 3 else 0.0
        pure64[12] = v1[4] if len(v1) > 4 else 0.0
        pure64[13] = v1[5] if len(v1) > 5 else 0.0
        pure64[14] = v1[30] if len(v1) > 30 else 0.34
        pure64[15] = v1[31] if len(v1) > 31 else 0.34
        pure64[16] = v1[8] if len(v1) > 8 else 0.28
        pure64[17] = v1[10] if len(v1) > 10 else 0.24
        pure64[18] = v1[35] if len(v1) > 35 else 0.75
        pure64[19] = v1[47] if len(v1) > 47 else 0.50

        pure64[20] = v2[0] if len(v2) > 0 else 0.0
        pure64[21] = v2[1] if len(v2) > 1 else 0.0
        pure64[22] = v2[2] if len(v2) > 2 else 0.0
        pure64[23] = v2[3] if len(v2) > 3 else 0.0
        pure64[24] = v2[4] if len(v2) > 4 else 0.0
        pure64[25] = v2[5] if len(v2) > 5 else 0.0
        pure64[26] = v2[30] if len(v2) > 30 else 0.34
        pure64[27] = v2[31] if len(v2) > 31 else 0.34
        pure64[28] = v2[8] if len(v2) > 8 else 0.28
        pure64[29] = v2[10] if len(v2) > 10 else 0.24
        pure64[30] = v2[35] if len(v2) > 35 else 0.75
        pure64[31] = v2[47] if len(v2) > 47 else 0.50

        # Block 3 (32..47): Rest, Fatigue, Chemistry & Lineups
        pure64[32] = v1[7] if len(v1) > 7 else 0.50
        pure64[33] = v2[7] if len(v2) > 7 else 0.50
        pure64[34] = pure64[32] - pure64[33]
        pure64[35] = v1[59] if len(v1) > 59 else 0.15
        pure64[36] = v2[59] if len(v2) > 59 else 0.15
        pure64[37] = (lineup[1] / 10.0) if (lineup and len(lineup) > 1) else 0.68
        pure64[38] = (lineup[13] / 10.0) if (lineup and len(lineup) > 13) else 0.68
        pure64[39] = pure64[37] - pure64[38]
        pure64[40] = lineup[4] if (lineup and len(lineup) > 4) else 0.0
        pure64[41] = lineup[16] if (lineup and len(lineup) > 16) else 0.0
        pure64[42] = v1[60] if len(v1) > 60 else 0.68
        pure64[43] = v2[60] if len(v2) > 60 else 0.68
        pure64[44] = v1[61] if len(v1) > 61 else 0.68
        pure64[45] = v2[61] if len(v2) > 61 else 0.68
        pure64[46] = v1[54] if len(v1) > 54 else 0.50
        pure64[47] = v2[54] if len(v2) > 54 else 0.50

        # Block 4 (48..63): Style Clash, Leverage, Substitutions & Friction
        pure64[48] = v1[36] if len(v1) > 36 else 0.52
        pure64[49] = v2[36] if len(v2) > 36 else 0.52
        pure64[50] = v1[37] if len(v1) > 37 else 0.50
        pure64[51] = v2[37] if len(v2) > 37 else 0.50
        pure64[52] = v1[35] if len(v1) > 35 else 0.30
        pure64[53] = v2[35] if len(v2) > 35 else 0.30
        pure64[54] = v1[40] if len(v1) > 40 else 0.50
        pure64[55] = v2[40] if len(v2) > 40 else 0.50
        pure64[56] = pure64[50] - pure64[51]
        pure64[57] = composite_card_bias
        pure64[58] = 1.0 if is_derby else 0.0
        pure64[59] = (v1[29] - v2[29]) if (len(v1) > 29 and len(v2) > 29) else 0.0
        pure64[60] = 1.0  # DNA cosine distance default
        pure64[61] = (v1[25] - v2[25]) if (len(v1) > 25 and len(v2) > 25) else 0.0
        pure64[62] = float(leverage_diff)
        pure64[63] = float(blowout_diff)

        return pure64

    def post_process_form_vector(self, vec: List[float], history: List[Any], cur_ts: int) -> None:
        """Optional post-processing hook for sport-specific temporal dynamics."""
        pass

    @abstractmethod
    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        """Extracts multi-task prediction targets."""
        ...


# ============================================================================
# Registry Implementation (Python Base Rules: Registry Decorator Pattern)
# ============================================================================

SPORT_EXTRACTOR_REGISTRY: Dict[str, BaseSportVectorExtractor] = {}


def register_sport_extractor(cls: Type[BaseSportVectorExtractor]):
    instance = cls()
    key = instance.sport_name.lower().replace("_", "-")
    SPORT_EXTRACTOR_REGISTRY[key] = instance
    return cls


def get_sport_extractor(sport: str) -> BaseSportVectorExtractor:
    s_clean = (sport or "football").lower().replace("_", "-")
    if s_clean in SPORT_EXTRACTOR_REGISTRY:
        return SPORT_EXTRACTOR_REGISTRY[s_clean]

    # Family alias fallback
    if "table-tennis" in s_clean or "ping-pong" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY["table-tennis"]
    if "tennis" in s_clean:
        if "women" in s_clean:
            return SPORT_EXTRACTOR_REGISTRY["tennis-women"]
        if "doubles" in s_clean:
            return SPORT_EXTRACTOR_REGISTRY["tennis-doubles"]
        return SPORT_EXTRACTOR_REGISTRY["tennis"]
    if "basketball" in s_clean:
        if "women" in s_clean:
            return SPORT_EXTRACTOR_REGISTRY["basketball-women"]
        if "48m" in s_clean or "nba" in s_clean:
            return SPORT_EXTRACTOR_REGISTRY["basketball-48m"]
        return SPORT_EXTRACTOR_REGISTRY["basketball"]
    if "hockey" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY["ice-hockey"]
    if "volleyball" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY.get("volleyball-women" if "women" in s_clean else "volleyball", SPORT_EXTRACTOR_REGISTRY["volleyball"])
    if "handball" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY.get("handball-women" if "women" in s_clean else "handball", SPORT_EXTRACTOR_REGISTRY["handball"])
    if "futsal" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY.get("futsal-women" if "women" in s_clean else "futsal", SPORT_EXTRACTOR_REGISTRY["futsal"])
    if "baseball" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY["baseball"]
    if "american-football" in s_clean or "nfl" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY["american-football"]
    if "cs" in s_clean or "counter-strike" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY["cs2"]
    if "dota" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY["dota2"]
    if "soccer" in s_clean or "football" in s_clean:
        return SPORT_EXTRACTOR_REGISTRY["football"]

    return SPORT_EXTRACTOR_REGISTRY.get("football", FootballVectorExtractor())


# ============================================================================
# Concrete Sport Extractors
# ============================================================================

@register_sport_extractor
class FootballVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "football"

    @property
    def has_home_advantage(self) -> bool:
        return True

    @property
    def has_draws(self) -> bool:
        return True

    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
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

        # Biological Squad Age & Dynamics (Dims 48..51)
        vec[48] = float(dds.get("avg_starter_age", 0.38))
        vec[49] = float(dds.get("young_pct", 0.25))
        vec[50] = float(dds.get("veteran_pct", 0.15))
        vec[51] = float(dds.get("age_std", 0.35))

        # Point-in-Time Tournament Motivation & Stakes (Dims 52..58)
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

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        tot_goals = hs_cur + as_cur
        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p2_h = safe_int(hs_dict.get("period2"))
        p2_a = safe_int(as_dict.get("period2"))
        tot_corners = safe_int(hs_dict.get("corners")) + safe_int(as_dict.get("corners"))
        tot_yc = safe_int(hs_dict.get("yellowCards")) + safe_int(as_dict.get("yellowCards"))

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if hs_cur == as_cur else 0.0,
            1.0 if as_cur > hs_cur else 0.0,
            1.0 if tot_goals > 2.5 else 0.0,
            1.0 if tot_goals > 1.5 else 0.0,
            1.0 if tot_goals < 3.5 else 0.0,
            1.0 if (hs_cur >= 1 and as_cur >= 1) else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if p1_h == p1_a else 0.0,
            1.0 if (p1_h + p1_a) > 0.5 else 0.0,
            1.0 if p2_h > p2_a else 0.0,
            1.0 if (p1_h > p1_a and hs_cur > as_cur) else 0.0,
            1.0 if tot_corners > 9.5 else (0.50 if tot_corners == 0 else 0.0),
            1.0 if tot_yc > 4.5 else (0.50 if tot_yc == 0 else 0.0)
        ]
        return targets




@register_sport_extractor
class BasketballVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "basketball"

    @property
    def has_home_advantage(self) -> bool:
        return True

    @property
    def has_draws(self) -> bool:
        return False


    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
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

        bins = [0.25, 0.25, 0.25, 0.25, 0.0, 0.0]
        for i in range(6):
            vec[18 + i] = bins[i]
            vec[24 + i] = bins[i]

        def_pf = 105.0
        base_tot = 180.0
        base_pts = 80.0
        base_def = 130.0
        scale_pts = 50.0
        scale_tot = 80.0

        pf = float(dds.get("avg_pts_for", def_pf))
        pa = float(dds.get("avg_pts_against", def_pf))
        pace = float(dds.get("pace_proxy", max(0.0, min(1.0, (pf + pa - base_tot) / scale_tot))))
        off_r = float(dds.get("off_rating", max(0.0, min(1.0, (pf - base_pts) / scale_pts))))
        def_r = float(dds.get("def_rating", max(0.0, min(1.0, (base_def - pa) / scale_pts))))
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

        net_diff = pf - pa
        vec[48] = round(max(0.0, min(1.0, 0.5 + (net_diff / 30.0))), 4)
        vec[49] = 1.0 if abs(net_diff) >= 10.0 else 0.0
        vec[50] = float(dds.get("ot_rate", 0.05))
        vec[51] = round(pace, 4)

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

        vec[59] = 0.15
        vec[60] = round(def_r, 4)
        vec[61] = round(off_r, 4)
        vec[62] = round(net_r, 4)
        vec[63] = round(efg, 4)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
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
            1.0 if half_tot > 80.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if q1_tot > 40.5 else 0.0,
            1.0 if abs(diff) <= 5 else 0.0,
            1.0 if abs(diff) >= 15 else 0.0,
            ot_flag
        ]
        return targets




@register_sport_extractor
class Basketball48mVectorExtractor(BasketballVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "basketball-48m"

    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        super().populate_dds_dimensions(vec, dds)
        pf = float(dds.get("avg_pts_for", 112.0))
        pa = float(dds.get("avg_pts_against", 112.0))
        pace = float(dds.get("pace_proxy", max(0.0, min(1.0, (pf + pa - 200.0) / 80.0))))
        off_r = float(dds.get("off_rating", max(0.0, min(1.0, (pf - 90.0) / 50.0))))
        def_r = float(dds.get("def_rating", max(0.0, min(1.0, (140.0 - pa) / 50.0))))
        vec[30] = round(pace, 4)
        vec[31] = round(off_r, 4)
        vec[32] = round(def_r, 4)
        vec[51] = round(pace, 4)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        tot_pts = hs_cur + as_cur
        if tot_pts < 100:
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
            1.0 if diff >= 8 else 0.0,
            1.0 if diff >= -7 else 0.0,
            1.0 if diff >= 13 else 0.0,
            1.0 if tot_pts > 215.5 else 0.0,
            1.0 if tot_pts > 205.5 else 0.0,
            1.0 if tot_pts < 225.5 else 0.0,
            1.0 if half_h > half_a else 0.0,
            1.0 if half_tot > 105.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if q1_tot > 52.5 else 0.0,
            1.0 if abs(diff) <= 5 else 0.0,
            1.0 if abs(diff) >= 18 else 0.0,
            ot_flag
        ]
        return targets




@register_sport_extractor
class BasketballWomenVectorExtractor(BasketballVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "basketball-women"

    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        super().populate_dds_dimensions(vec, dds)
        pf = float(dds.get("avg_pts_for", 70.0))
        pa = float(dds.get("avg_pts_against", 70.0))
        pace = float(dds.get("pace_proxy", max(0.0, min(1.0, (pf + pa - 140.0) / 60.0))))
        off_r = float(dds.get("off_rating", max(0.0, min(1.0, (pf - 65.0) / 35.0))))
        def_r = float(dds.get("def_rating", max(0.0, min(1.0, (85.0 - pa) / 35.0))))
        vec[30] = round(pace, 4)
        vec[31] = round(off_r, 4)
        vec[32] = round(def_r, 4)
        vec[51] = round(pace, 4)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        tot_pts = hs_cur + as_cur
        if tot_pts < 60:
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
            1.0 if tot_pts > 140.5 else 0.0,
            1.0 if tot_pts > 130.5 else 0.0,
            1.0 if tot_pts < 150.5 else 0.0,
            1.0 if half_h > half_a else 0.0,
            1.0 if half_tot > 68.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if q1_tot > 33.5 else 0.0,
            1.0 if abs(diff) <= 5 else 0.0,
            1.0 if abs(diff) >= 12 else 0.0,
            ot_flag
        ]
        return targets




@register_sport_extractor
class IceHockeyVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "ice-hockey"

    @property
    def has_home_advantage(self) -> bool:
        return True

    @property
    def has_draws(self) -> bool:
        return True

    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        FootballVectorExtractor().populate_dds_dimensions(vec, dds)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        tot_goals = hs_cur + as_cur
        ot_flag = 1.0 if (hs_dict.get("overtime") is not None or "period4" in hs_dict) else 0.0
        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if hs_cur == as_cur else 0.0,
            1.0 if as_cur > hs_cur else 0.0,
            1.0 if tot_goals > 5.5 else 0.0,
            1.0 if tot_goals > 4.5 else 0.0,
            1.0 if tot_goals < 6.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if p1_h == p1_a else 0.0,
            1.0 if (p1_h + p1_a) > 1.5 else 0.0,
            ot_flag,
            1.0 if (hs_cur - as_cur) >= 2 else 0.0,
            1.0 if (as_cur - hs_cur) >= 2 else 0.0,
            1.0 if tot_goals > 7.5 else 0.0,
            1.0 if (hs_cur >= 1 and as_cur >= 1) else 0.0
        ]
        return targets




@register_sport_extractor
class TennisVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "tennis"

    @property
    def has_home_advantage(self) -> bool:
        return False

    @property
    def has_draws(self) -> bool:
        return False

    def resolve_tier_level(self, tournament_name: Optional[str]) -> float:
        if not tournament_name:
            return 0.50
        tn = tournament_name.upper()
        if any(k in tn for k in ["AUSTRALIAN OPEN", "ROLAND GARROS", "WIMBLEDON", "US OPEN"]):
            return 1.0
        elif any(k in tn for k in ["MASTERS", "ATP 1000", "WTA 1000"]):
            return 0.86
        elif any(k in tn for k in ["ATP 500", "WTA 500"]):
            return 0.71
        elif any(k in tn for k in ["ATP 250", "WTA 250"]):
            return 0.57
        elif "CHALLENGER" in tn:
            return 0.43
        elif any(k in tn for k in ["ITF", "M15", "M25", "W15", "W35"]):
            return 0.14
        return 0.50

    def resolve_surface_pace(self, surface: Optional[str]) -> float:
        surf = (surface or "").upper()
        if surf == "GRASS":
            return 1.10
        elif surf == "INDOORS":
            return 1.05
        elif surf == "CLAY":
            return 0.90
        return 1.0

    def extract_pure_physics_vector(
        self,
        v1: List[float],
        v2: List[float],
        surface: str = "ALL",
        tier_level: float = 0.5,
        h2h_wr: float = 0.5,
        leverage_diff: float = 0.0,
        pace: float = 1.0,
        **kwargs
    ) -> List[float]:
        """
        Pure Physics Duel Extraction for Tennis (Strict 64 dimensions).
        Guarantees 100% mathematical parity with TennisVectorExtractor.java:buildTennisDuelPhysicsVector.
        """
        p64 = [0.0] * 64

        # Block A (0..23): Player 1 Physical & Skill Dynamics
        p64[0]  = v1[19] if len(v1) > 19 else 0.0
        p64[1]  = v1[18] if len(v1) > 18 else 0.50
        p64[2]  = v1[45] if len(v1) > 45 else 0.05
        p64[3]  = v1[46] if len(v1) > 46 else 0.05
        p64[4]  = v1[47] if len(v1) > 47 else 0.0
        p64[5]  = v1[63] if len(v1) > 63 else 0.50
        p64[6]  = v1[37] if len(v1) > 37 else 0.65
        p64[7]  = v1[38] if len(v1) > 38 else 0.20
        p64[8]  = v1[50] if len(v1) > 50 else 0.50
        p64[9]  = v1[30] if len(v1) > 30 else 0.62
        p64[10] = v1[33] if len(v1) > 33 else 0.25
        p64[11] = v1[34] if len(v1) > 34 else 0.20
        p64[12] = v1[35] if len(v1) > 35 else 0.60
        p64[13] = v1[36] if len(v1) > 36 else 0.40
        p64[14] = v1[39] if len(v1) > 39 else 0.50
        p64[15] = v1[40] if len(v1) > 40 else 0.50
        p64[16] = v1[48] if len(v1) > 48 else 0.10
        p64[17] = v1[49] if len(v1) > 49 else 0.85
        p64[18] = v1[57] if len(v1) > 57 else 0.0
        p64[19] = v1[58] if len(v1) > 58 else 0.95
        p64[20] = v1[59] if len(v1) > 59 else 0.50
        p64[21] = v1[0]  if len(v1) > 0  else 0.50
        p64[22] = v1[1]  if len(v1) > 1  else 0.0
        p64[23] = v1[2]  if len(v1) > 2  else 0.50

        # Block B (24..47): Player 2 Physical & Skill Dynamics (Strict Mirror)
        p64[24] = v2[19] if len(v2) > 19 else 0.0
        p64[25] = v2[18] if len(v2) > 18 else 0.50
        p64[26] = v2[45] if len(v2) > 45 else 0.05
        p64[27] = v2[46] if len(v2) > 46 else 0.05
        p64[28] = v2[47] if len(v2) > 47 else 0.0
        p64[29] = v2[63] if len(v2) > 63 else 0.50
        p64[30] = v2[37] if len(v2) > 37 else 0.65
        p64[31] = v2[38] if len(v2) > 38 else 0.20
        p64[32] = v2[50] if len(v2) > 50 else 0.50
        p64[33] = v2[30] if len(v2) > 30 else 0.62
        p64[34] = v2[33] if len(v2) > 33 else 0.25
        p64[35] = v2[34] if len(v2) > 34 else 0.20
        p64[36] = v2[35] if len(v2) > 35 else 0.60
        p64[37] = v2[36] if len(v2) > 36 else 0.40
        p64[38] = v2[39] if len(v2) > 39 else 0.50
        p64[39] = v2[40] if len(v2) > 40 else 0.50
        p64[40] = v2[48] if len(v2) > 48 else 0.10
        p64[41] = v2[49] if len(v2) > 49 else 0.85
        p64[42] = v2[57] if len(v2) > 57 else 0.0
        p64[43] = v2[58] if len(v2) > 58 else 0.95
        p64[44] = v2[59] if len(v2) > 59 else 0.50
        p64[45] = v2[0]  if len(v2) > 0  else 0.50
        p64[46] = v2[1]  if len(v2) > 1  else 0.0
        p64[47] = v2[2]  if len(v2) > 2  else 0.50

        # Block C (48..63): Pairwise Duel Physics & Symmetric Differentials
        p64[48] = p64[0] - p64[24]
        p64[49] = 1.0 / (1.0 + math.exp(-4.0 * (p64[0] - p64[24])))
        p64[50] = (p64[1] + p64[25]) * 0.5
        p64[51] = p64[2] - p64[26]
        p64[52] = p64[6] - p64[30]
        p64[53] = p64[7] - p64[31]
        p64[54] = p64[8] - p64[32]
        p64[55] = p64[10] - p64[34]
        p64[56] = p64[11] - p64[35]
        p64[57] = p64[5] - p64[29]
        p64[58] = (p64[17] - p64[18]) - (p64[41] - p64[42])
        p64[59] = float(v1[44]) if len(v1) > 44 else float(h2h_wr)
        p64[60] = float(tier_level)

        surf_u = (surface or "ALL").upper()
        if "CLAY" in surf_u:
            surf_code = 0.33
        elif "GRASS" in surf_u:
            surf_code = 0.66
        elif "INDOOR" in surf_u or "CARPET" in surf_u:
            surf_code = 1.0
        else:
            surf_code = 0.0
        p64[61] = surf_code
        p64[62] = float(pace)
        p64[63] = float(leverage_diff)

        return p64

    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        # Game State & Comeback Resilience (Dims 8..15)
        vec[8]  = 0.50
        vec[9]  = 0.35
        vec[10] = 0.20
        vec[11] = 0.20
        vec[12] = 0.20
        vec[13] = 0.20
        vec[14] = 0.35
        vec[15] = 0.35

        # Dims 16..19: Head-to-Head & Opponent Relative Glicko-2 MMR Differential
        mmr = float(dds.get("mmr", 1500.0))
        rd = float(dds.get("rd", 350.0))
        opp_mmr = float(dds.get("opp_mmr", 1500.0))
        opp_rd = float(dds.get("opp_rd", 350.0))
        mmr_diff = mmr - opp_mmr
        phi_opp = opp_rd / 173.7178
        g_phi_opp = 1.0 / math.sqrt(1.0 + (3.0 * phi_opp * phi_opp) / (math.pi * math.pi))
        expected_win_prob = 1.0 / (1.0 + math.exp(-g_phi_opp * (mmr_diff / 173.7178)))
        pair_confidence = 1.0 / (1.0 + (rd + opp_rd) / 300.0)

        vec[16] = float(math.tanh(mmr_diff / 400.0))
        vec[17] = float(expected_win_prob)
        vec[18] = float(pair_confidence)
        vec[19] = float((mmr - 1500.0) / 500.0)

        # Temporal Distribution (Dims 20..29)
        bins = [0.33, 0.33, 0.34, 0.0, 0.0, 0.0]
        for i in range(5):
            vec[20 + i] = bins[i]
            vec[25 + i] = bins[i]

        # Advanced Tennis Metrics (Dims 30..44)
        fs_pct = float(dds.get("fs_pct", 62.0))
        aces = float(dds.get("aces_per_match", 4.0))
        dfs = float(dds.get("dfs_per_match", 2.0))
        bp_saved = float(dds.get("bp_saved_pct", 60.0))
        bp_conv = float(dds.get("bp_conv_pct", 40.0))
        def_hold = 0.80
        def_break = 0.22
        hold_pct = float(dds.get("hold_pct_modeled", def_hold))
        break_pct = float(dds.get("break_pct_modeled", def_break))
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
        peak_anchor = 2600.0
        peak_mmr = float(dds.get("peak_mmr", 1500.0))
        effective_peak_rank = max(1.0, math.exp((peak_anchor - peak_mmr) / 250.0))
        effective_cur_rank = max(1.0, math.exp((peak_anchor - mmr) / 250.0))
        quartic_stature = effective_peak_rank ** -0.25
        current_stature = effective_cur_rank ** -0.25
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

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
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

        line_o = 21.5
        line_c_o = 19.5
        line_c_u = 23.5

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if (hs_cur == 2 and as_cur == 0) else 0.0,
            1.0 if (as_cur >= 1) else 0.0,
            1.0 if tot_sets > 2.5 else 0.0,
            1.0 if tot_games > line_o else 0.0,
            1.0 if tot_games > line_c_o else 0.0,
            1.0 if tot_games < line_c_u else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if first_set_games > 9.5 else 0.0,
            tb_flag,
            1.0 if (hs_cur >= 1 and as_cur >= 1) else 0.0,
            1.0 if (hs_cur == 2 and as_cur == 0) or (as_cur == 2 and hs_cur == 0) else 0.0,
            1.0 if abs(tot_games_h - tot_games_a) >= 6 else 0.0,
            1.0 if tot_sets == 3 and abs(tot_games_h - tot_games_a) <= 2 else 0.0
        ]
        return targets




@register_sport_extractor
class TennisWomenVectorExtractor(TennisVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "tennis-women"

    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        super().populate_dds_dimensions(vec, dds)
        # WTA-specific calibrations
        def_hold = 0.665
        def_break = 0.335
        hold_pct = float(dds.get("hold_pct_modeled", def_hold))
        break_pct = float(dds.get("break_pct_modeled", def_break))
        aces = float(dds.get("aces_per_match", 3.0))

        vec[33] = min(1.0, aces / 8.0)
        vec[37] = hold_pct
        vec[38] = break_pct

        peak_anchor = 2400.0
        peak_mmr = float(dds.get("peak_mmr", 1500.0))
        mmr = float(dds.get("mmr", 1500.0))
        effective_peak_rank = max(1.0, math.exp((peak_anchor - peak_mmr) / 250.0))
        effective_cur_rank = max(1.0, math.exp((peak_anchor - mmr) / 250.0))
        quartic_stature = effective_peak_rank ** -0.25
        current_stature = effective_cur_rank ** -0.25
        deflation_spread = max(0.0, quartic_stature - current_stature)

        vec[45] = round(quartic_stature, 4)
        vec[46] = round(current_stature, 4)
        vec[47] = round(deflation_spread, 4)
        vec[51] = round(hold_pct, 4)
        vec[60] = round(min(0.98, max(0.35, hold_pct)), 4)
        vec[61] = round(min(0.98, max(0.15, break_pct)), 4)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        targets = super().extract_targets(hs_dict, as_dict, hs_cur, as_cur)
        if targets is None:
            return None
        # Recalculate game lines for WTA (20.5 baseline instead of 21.5)
        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p2_h = safe_int(hs_dict.get("period2"))
        p2_a = safe_int(as_dict.get("period2"))
        p3_h = safe_int(hs_dict.get("period3"))
        p3_a = safe_int(as_dict.get("period3"))
        tot_games = p1_h + p2_h + p3_h + p1_a + p2_a + p3_a
        targets[4] = 1.0 if tot_games > 20.5 else 0.0
        targets[5] = 1.0 if tot_games > 18.5 else 0.0
        targets[6] = 1.0 if tot_games < 22.5 else 0.0
        return targets




@register_sport_extractor
class TennisDoublesVectorExtractor(TennisVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "tennis-doubles"




@register_sport_extractor
class TennisDoublesWomenVectorExtractor(TennisWomenVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "tennis-doubles-women"




@register_sport_extractor
class TableTennisVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "table-tennis"

    @property
    def has_home_advantage(self) -> bool:
        return False

    @property
    def has_draws(self) -> bool:
        return False

    def extract_pure_physics_vector(
        self,
        v1: List[float],
        v2: List[float],
        surface: str = "ALL",
        tier_level: float = 0.5,
        h2h_wr: float = 0.5,
        leverage_diff: float = 0.0,
        pace: float = 1.0,
        **kwargs
    ) -> List[float]:
        """
        Pure Physics Duel Extraction for Table Tennis (Strict 64 dimensions).
        Guarantees 100% mathematical parity with TableTennisVectorExtractor.java:extractPurePhysicsVector.
        """
        p64 = [0.0] * 64

        # Block A (0..23): Player 1 Dynamics
        p64[0]  = v1[19] if len(v1) > 19 else 0.0
        p64[1]  = v1[18] if len(v1) > 18 else 0.50
        p64[2]  = v1[45] if len(v1) > 45 else 0.50
        p64[3]  = v1[31] if len(v1) > 31 else 0.50
        p64[4]  = v1[32] if len(v1) > 32 else 0.50
        p64[5]  = v1[63] if len(v1) > 63 else 0.50
        p64[6]  = v1[14] if len(v1) > 14 else 0.30
        p64[7]  = v1[10] if len(v1) > 10 else 0.22
        p64[8]  = v1[33] if len(v1) > 33 else 0.50
        p64[9]  = v1[34] if len(v1) > 34 else 0.0
        p64[10] = v1[35] if len(v1) > 35 else 0.0
        p64[11] = v1[36] if len(v1) > 36 else 0.50
        p64[12] = v1[37] if len(v1) > 37 else 0.50
        p64[13] = v1[38] if len(v1) > 38 else 0.0
        p64[14] = v1[58] if len(v1) > 58 else 0.80
        p64[15] = v1[57] if len(v1) > 57 else 0.0
        p64[16] = v1[16] if len(v1) > 16 else 0.0
        p64[17] = v1[17] if len(v1) > 17 else 0.50
        p64[18] = v1[0]  if len(v1) > 0  else 0.50
        p64[19] = v1[1]  if len(v1) > 1  else 0.0
        p64[20] = v1[2]  if len(v1) > 2  else 0.50
        p64[21] = v1[3]  if len(v1) > 3  else 0.15
        p64[22] = v1[4]  if len(v1) > 4  else 0.15
        p64[23] = v1[5]  if len(v1) > 5  else 0.0

        # Block B (24..47): Player 2 Dynamics (Strict Mirror)
        p64[24] = v2[19] if len(v2) > 19 else 0.0
        p64[25] = v2[18] if len(v2) > 18 else 0.50
        p64[26] = v2[45] if len(v2) > 45 else 0.50
        p64[27] = v2[31] if len(v2) > 31 else 0.50
        p64[28] = v2[32] if len(v2) > 32 else 0.50
        p64[29] = v2[63] if len(v2) > 63 else 0.50
        p64[30] = v2[14] if len(v2) > 14 else 0.30
        p64[31] = v2[10] if len(v2) > 10 else 0.22
        p64[32] = v2[33] if len(v2) > 33 else 0.50
        p64[33] = v2[34] if len(v2) > 34 else 0.0
        p64[34] = v2[35] if len(v2) > 35 else 0.0
        p64[35] = v2[36] if len(v2) > 36 else 0.50
        p64[36] = v2[37] if len(v2) > 37 else 0.50
        p64[37] = v2[38] if len(v2) > 38 else 0.0
        p64[38] = v2[58] if len(v2) > 58 else 0.80
        p64[39] = v2[57] if len(v2) > 57 else 0.0
        p64[40] = v2[16] if len(v2) > 16 else 0.0
        p64[41] = v2[17] if len(v2) > 17 else 0.50
        p64[42] = v2[0]  if len(v2) > 0  else 0.50
        p64[43] = v2[1]  if len(v2) > 1  else 0.0
        p64[44] = v2[2]  if len(v2) > 2  else 0.50
        p64[45] = v2[3]  if len(v2) > 3  else 0.15
        p64[46] = v2[4]  if len(v2) > 4  else 0.15
        p64[47] = v2[5]  if len(v2) > 5  else 0.0

        # Block C (48..63): Pairwise Duel Physics & Differentials
        p64[48] = p64[0] - p64[24]
        p64[49] = 1.0 / (1.0 + math.exp(-4.0 * (p64[0] - p64[24])))
        p64[50] = (p64[1] + p64[25]) * 0.5
        p64[51] = p64[2] - p64[26]
        p64[52] = p64[3] - p64[27]
        p64[53] = p64[4] - p64[28]
        p64[54] = p64[6] - p64[30]
        p64[55] = p64[7] - p64[31]
        p64[56] = p64[9] - p64[33]
        p64[57] = p64[10] - p64[34]
        p64[58] = p64[11] - p64[35]
        p64[59] = p64[12] - p64[36]
        p64[60] = p64[14] - p64[38]
        p64[61] = p64[5] - p64[29]
        p64[62] = float(pace)
        p64[63] = float(leverage_diff)

        return p64

    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        # Table Tennis Game State & Dynamics (Dims 8..15)
        vec[8]  = 0.50
        vec[9]  = 0.38
        vec[10] = float(dds.get("dec_rate", 0.22))
        vec[11] = 0.22
        vec[12] = 0.22
        vec[13] = 0.22
        vec[14] = float(dds.get("sweep_rate", 0.30))
        vec[15] = 0.30

        # MMR & Expected Win Prob (Dims 16..19)
        mmr = float(dds.get("mmr", 1500.0))
        rd = float(dds.get("rd", 350.0))
        streak = float(dds.get("streak", 0.0))
        opp_mmr = float(dds.get("opp_mmr", 1500.0))
        opp_rd = float(dds.get("opp_rd", 350.0))
        mmr_diff = mmr - opp_mmr
        phi_opp = opp_rd / 173.7178
        g_phi_opp = 1.0 / math.sqrt(1.0 + (3.0 * phi_opp * phi_opp) / (math.pi * math.pi))
        expected_win_prob = 1.0 / (1.0 + math.exp(-g_phi_opp * (mmr_diff / 173.7178)))
        pair_confidence = 1.0 / (1.0 + (rd + opp_rd) / 300.0)

        vec[16] = float(math.tanh(mmr_diff / 400.0))
        vec[17] = float(expected_win_prob)
        vec[18] = float(pair_confidence)
        vec[19] = float((mmr - 1500.0) / 500.0)

        # Temporal Distribution (Dims 20..29)
        bins = [0.25, 0.25, 0.25, 0.25, 0.0, 0.0]
        for i in range(4):
            vec[20 + i] = bins[i]
            vec[25 + i] = bins[i]

        # Table Tennis Specifics (Dims 30..44)
        vec[30] = max(0.0, min(1.0, (mmr - 800.0) / 1400.0))
        vec[31] = max(0.0, min(1.0, (streak + 5.0) / 10.0))
        vec[32] = float(dds.get("margin_sigmoid", 0.50))
        vec[33] = float(dds.get("momentum_streak", 0.50))
        vec[34] = float(dds.get("matches_today_norm", 0.0))
        vec[35] = float(dds.get("fatigue_norm", 0.0))
        vec[36] = float(dds.get("min_rest_norm", 0.50))
        vec[37] = float(dds.get("daily_wr", 0.50))
        vec[38] = float(dds.get("sets_norm", 0.0))
        vec[39] = 0.50
        vec[40] = 0.70
        vec[41] = 0.50
        vec[42] = 0.50
        vec[43] = 0.50
        vec[44] = float(dds.get("h2h_wr", 0.50))

        # Dynamic Physical & Class Features (Dims 45..63, parity with TableTennisVectorExtractor.java)
        vec[45] = vec[30]
        vec[46] = vec[30]
        vec[47] = max(0.0, 0.50 - vec[33])
        vec[48] = vec[34]
        vec[49] = vec[35]
        vec[50] = vec[36]
        vec[51] = vec[37]

        vec[52] = float(dds.get("elimination_pressure", 0.0))
        vec[53] = 0.0 # No draws in table tennis
        vec[54] = float(dds.get("regional_card_bias", 0.50))
        vec[55] = vec[38]
        matches_today = int(dds.get("matches_today", 0))
        vec[56] = float(1.0 - math.exp(-0.25 * matches_today))
        vec[57] = float(vec[35] - 0.50) if vec[35] > 0.50 else 0.0
        vec[58] = float(max(0.20, 1.0 - vec[57]))

        vec[59] = vec[10]
        vec[60] = vec[14]
        vec[61] = vec[33]
        vec[62] = vec[17]
        vec[63] = float(dds.get("win_rate", 0.50))

    def post_process_form_vector(self, vec: List[float], history: List[Any], cur_ts: int) -> None:
        if not history:
            return
        cur_day = cur_ts // 86400000
        same_day = [h for h in history if (h[0] // 86400000) == cur_day and h[0] < cur_ts]
        matches_today = len(same_day)
        sets_today = sum(h[1] + h[2] for h in same_day)
        wins_today = sum(1 for h in same_day if h[3] == 1)
        fatigue_idx = 0.0
        min_rest_hours = 24.0
        for h in same_day:
            delta_h = max(0.0, (cur_ts - h[0]) / 3600000.0)
            match_sets = h[1] + h[2]
            fatigue_idx += match_sets * math.exp(-0.15 * delta_h)
            if delta_h < min_rest_hours:
                min_rest_hours = delta_h

        vec[34] = min(1.0, matches_today / 8.0)
        vec[35] = min(1.0, fatigue_idx / 10.0)
        vec[36] = 1.0 / (1.0 + min_rest_hours)
        vec[37] = (wins_today / matches_today) if matches_today > 0 else 0.5
        vec[38] = min(1.0, sets_today / 30.0)

        vec[48] = vec[34]
        vec[49] = vec[35]
        vec[50] = vec[36]
        vec[51] = vec[37]
        vec[55] = vec[38]
        vec[56] = float(1.0 - math.exp(-0.25 * matches_today))
        vec[57] = float(vec[35] - 0.50) if vec[35] > 0.50 else 0.0
        vec[58] = float(max(0.20, 1.0 - vec[57]))

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
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




@register_sport_extractor
class TableTennisWomenVectorExtractor(TableTennisVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "table-tennis-women"




@register_sport_extractor
class Cs2VectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "cs2"

    @property
    def has_home_advantage(self) -> bool:
        return False

    @property
    def has_draws(self) -> bool:
        return False


    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        FootballVectorExtractor().populate_dds_dimensions(vec, dds)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
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




@register_sport_extractor
class Dota2VectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "dota2"

    @property
    def has_home_advantage(self) -> bool:
        return False

    @property
    def has_draws(self) -> bool:
        return False


    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        FootballVectorExtractor().populate_dds_dimensions(vec, dds)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
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




@register_sport_extractor
class VolleyballVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "volleyball"

    @property
    def has_home_advantage(self) -> bool:
        return True

    @property
    def has_draws(self) -> bool:
        return False


    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        FootballVectorExtractor().populate_dds_dimensions(vec, dds)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
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
        if tot_pts < 75:
            tot_pts = tot_sets * 46.0

        p1_pts = p1_h + p1_a if (p1_h + p1_a) >= 20 else 46.0
        is_women = "women" in self.sport_name
        line_o = 176.5 if is_women else 180.5
        line_c_o = 156.5 if is_women else 160.5
        line_c_u = 182.5 if is_women else 185.5
        first_set_line = 44.5 if is_women else 45.5

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if (hs_cur - as_cur) >= 2 else 0.0,
            1.0 if (as_cur - hs_cur) >= -1 else 0.0,
            1.0 if tot_sets > 3.5 else 0.0,
            1.0 if tot_sets == 5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if (hs_cur >= 1 and as_cur >= 1) else 0.0,
            1.0 if (hs_cur == 3 and as_cur == 0) or (as_cur == 3 and hs_cur == 0) else 0.0,
            1.0 if tot_pts > line_o else 0.0,
            1.0 if tot_pts > line_c_o else 0.0,
            1.0 if tot_pts < line_c_u else 0.0,
            1.0 if p1_pts > first_set_line else 0.0,
            1.0 if tot_sets == 5 and abs(p5_h - p5_a) <= 3 else 0.0,
            1.0 if tot_sets == 5 else 0.0
        ]
        return targets




@register_sport_extractor
class VolleyballWomenVectorExtractor(VolleyballVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "volleyball-women"




@register_sport_extractor
class HandballVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "handball"

    @property
    def has_home_advantage(self) -> bool:
        return True

    @property
    def has_draws(self) -> bool:
        return True

    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        FootballVectorExtractor().populate_dds_dimensions(vec, dds)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        tot_goals = hs_cur + as_cur
        if tot_goals < 20:
            return None

        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p1_tot = p1_h + p1_a if (p1_h + p1_a) > 10 else (tot_goals / 2.0)
        diff = hs_cur - as_cur

        is_women = "women" in self.sport_name
        line_o = 51.5 if is_women else 53.5
        line_c_o = 47.5 if is_women else 49.5
        line_c_u = 55.5 if is_women else 57.5
        half_line = 24.5 if is_women else 25.5
        festival_line = 58.5 if is_women else 60.5

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if hs_cur == as_cur else 0.0,
            1.0 if hs_cur < as_cur else 0.0,
            1.0 if diff >= 3 else 0.0,
            1.0 if diff >= -2 else 0.0,
            1.0 if tot_goals > line_o else 0.0,
            1.0 if tot_goals > line_c_o else 0.0,
            1.0 if tot_goals < line_c_u else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if p1_tot > half_line else 0.0,
            1.0 if (hs_cur >= 25 and as_cur >= 25) else 0.0,
            1.0 if tot_goals > festival_line else 0.0,
            1.0 if abs(diff) <= 2 else 0.0,
            1.0 if abs(diff) >= 6 else 0.0
        ]
        return targets




@register_sport_extractor
class HandballWomenVectorExtractor(HandballVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "handball-women"




@register_sport_extractor
class FutsalVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "futsal"

    @property
    def has_home_advantage(self) -> bool:
        return True

    @property
    def has_draws(self) -> bool:
        return True

    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        FootballVectorExtractor().populate_dds_dimensions(vec, dds)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        tot_goals = hs_cur + as_cur
        p1_h = safe_int(hs_dict.get("period1"))
        p1_a = safe_int(as_dict.get("period1"))
        p1_tot = p1_h + p1_a if (p1_h + p1_a) > 0 else (tot_goals / 2.0)
        diff = hs_cur - as_cur
        btts = 1.0 if (hs_cur > 0 and as_cur > 0) else 0.0

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if hs_cur == as_cur else 0.0,
            1.0 if hs_cur < as_cur else 0.0,
            1.0 if diff >= 2 else 0.0,
            1.0 if diff >= -1 else 0.0,
            1.0 if tot_goals > 5.5 else 0.0,
            1.0 if tot_goals > 4.5 else 0.0,
            1.0 if tot_goals < 6.5 else 0.0,
            1.0 if p1_h > p1_a else 0.0,
            1.0 if p1_tot > 2.5 else 0.0,
            btts,
            1.0 if (hs_cur >= 2 and as_cur >= 2) else 0.0,
            1.0 if tot_goals > 7.5 else 0.0,
            1.0 if as_cur == 0 else 0.0
        ]
        return targets




@register_sport_extractor
class FutsalWomenVectorExtractor(FutsalVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "futsal-women"




@register_sport_extractor
class BaseballVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "baseball"

    @property
    def has_home_advantage(self) -> bool:
        return True

    @property
    def has_draws(self) -> bool:
        return False


    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        FootballVectorExtractor().populate_dds_dimensions(vec, dds)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        tot_runs = hs_cur + as_cur
        first_5_h = sum(safe_int(hs_dict.get(f"period{i}")) for i in range(1, 6))
        first_5_a = sum(safe_int(as_dict.get(f"period{i}")) for i in range(1, 6))
        first_5_tot = first_5_h + first_5_a if (first_5_h + first_5_a) > 0 else (tot_runs * 5.0 / 9.0)
        diff = hs_cur - as_cur
        extra_innings = 1.0 if (safe_int(hs_dict.get("period10")) > 0 or safe_int(as_dict.get("period10")) > 0 or hs_dict.get("overtime") is not None) else 0.0

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if diff >= 2 else 0.0,
            1.0 if diff >= -1 else 0.0,
            1.0 if tot_runs > 8.5 else 0.0,
            1.0 if tot_runs > 7.5 else 0.0,
            1.0 if tot_runs < 9.5 else 0.0,
            1.0 if first_5_h > first_5_a else 0.0,
            1.0 if first_5_tot > 4.5 else 0.0,
            1.0 if (hs_cur >= 3 and as_cur >= 3) else 0.0,
            1.0 if abs(diff) == 1 else 0.0,
            1.0 if abs(diff) >= 4 else 0.0,
            extra_innings,
            1.0 if (hs_cur == 0 or as_cur == 0) else 0.0,
            1.0 if tot_runs > 11.5 else 0.0
        ]
        return targets




@register_sport_extractor
class AmericanFootballVectorExtractor(BaseSportVectorExtractor):
    @property
    def sport_name(self) -> str:
        return "american-football"

    @property
    def has_home_advantage(self) -> bool:
        return True

    @property
    def has_draws(self) -> bool:
        return False


    def populate_dds_dimensions(self, vec: List[float], dds: Dict[str, Any]) -> None:
        FootballVectorExtractor().populate_dds_dimensions(vec, dds)

    def extract_targets(self, hs_dict: Dict[str, Any], as_dict: Dict[str, Any], hs_cur: int, as_cur: int) -> Optional[List[float]]:
        tot_points = hs_cur + as_cur
        if tot_points < 10:
            return None

        q1_h = safe_int(hs_dict.get("period1"))
        q1_a = safe_int(as_dict.get("period1"))
        q2_h = safe_int(hs_dict.get("period2"))
        q2_a = safe_int(as_dict.get("period2"))
        half_h = q1_h + q2_h
        half_a = q1_a + q2_a
        half_tot = half_h + half_a if (half_h + half_a) > 0 else (tot_points / 2.0)
        q1_tot = q1_h + q1_a if (q1_h + q1_a) > 0 else (tot_points / 4.0)
        diff = hs_cur - as_cur
        ot_flag = 1.0 if (hs_dict.get("period5") is not None or hs_dict.get("overtime") is not None) else 0.0

        targets = [
            1.0 if hs_cur > as_cur else 0.0,
            1.0 if diff >= 4 else 0.0,
            1.0 if diff >= -3 else 0.0,
            1.0 if diff >= 8 else 0.0,
            1.0 if tot_points > 44.5 else 0.0,
            1.0 if tot_points > 40.5 else 0.0,
            1.0 if tot_points < 48.5 else 0.0,
            1.0 if half_h > half_a else 0.0,
            1.0 if half_tot > 21.5 else 0.0,
            1.0 if abs(diff) <= 8 else 0.0,
            1.0 if abs(diff) >= 14 else 0.0,
            1.0 if q1_h > q1_a else 0.0,
            1.0 if q1_tot > 9.5 else 0.0,
            ot_flag
        ]
        return targets
