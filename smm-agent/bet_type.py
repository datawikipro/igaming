#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Canonical BetType Domain & Multilingual Renderer
Directly mirrors Java canonical classes:
- pro.datawiki.igaming.dto.BetType
- pro.datawiki.igaming.dto.BetTypeRegistry
- pro.datawiki.igaming.dto.SportType
- pro.datawiki.igaming.dto.market.MatchResultBet
- pro.datawiki.igaming.dto.market.TotalBet
- pro.datawiki.igaming.dto.market.HandicapBet
- pro.datawiki.igaming.dto.market.BinaryMarketBet
- pro.datawiki.igaming.dto.market.CorrectScoreBet
- pro.datawiki.igaming.dto.market.BetScope
- pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper
"""

from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import Enum
import re
from typing import Any, Dict, Optional, Tuple, Union


# ==============================================================================
# Sport Types (mirrors pro.datawiki.igaming.dto.SportType)
# ==============================================================================

class SportType(str, Enum):
    FOOTBALL = "FOOTBALL"
    BASKETBALL = "BASKETBALL"
    HOCKEY = "HOCKEY"
    TENNIS = "TENNIS"
    VOLLEYBALL = "VOLLEYBALL"
    TABLE_TENNIS = "TABLE_TENNIS"
    BASEBALL = "BASEBALL"
    HANDBALL = "HANDBALL"
    ESPORTS = "ESPORTS"
    MMA = "MMA"
    BOXING = "BOXING"
    AMERICAN_FOOTBALL = "AMERICAN_FOOTBALL"
    RUGBY_UNION = "RUGBY_UNION"
    WATER_POLO = "WATER_POLO"
    CRICKET = "CRICKET"
    DARTS = "DARTS"
    SNOOKER = "SNOOKER"
    FUTSAL = "FUTSAL"
    BADMINTON = "BADMINTON"
    UNKNOWN = "UNKNOWN"

    @classmethod
    def resolve(cls, value: Union[str, "SportType"]) -> "SportType":
        if isinstance(value, SportType):
            return value
        if not value:
            return cls.UNKNOWN
        clean = value.strip().upper().replace(" ", "_")
        for member in cls:
            if member.value == clean or member.name == clean:
                return member
        # Russian / French / Spanish aliases
        aliases = {
            "ФУТБОЛ": cls.FOOTBALL, "SOCCER": cls.FOOTBALL, "FÚTBOL": cls.FOOTBALL,
            "БАСКЕТБОЛ": cls.BASKETBALL, "BALONCESTO": cls.BASKETBALL,
            "ХОККЕЙ": cls.HOCKEY, "ICE_HOCKEY": cls.HOCKEY, "ХОККЕЙ_С_ШАЙБОЙ": cls.HOCKEY, "HOCKEY_SUR_GLACE": cls.HOCKEY,
            "ТЕННИС": cls.TENNIS, "TENIS": cls.TENNIS,
            "ВОЛЕЙБОЛ": cls.VOLLEYBALL, "VOLEIBOL": cls.VOLLEYBALL,
            "НАСТОЛЬНЫЙ_ТЕННИС": cls.TABLE_TENNIS, "TENNIS_DE_TABLE": cls.TABLE_TENNIS, "TENIS_DE_MESA": cls.TABLE_TENNIS,
            "БЕЙСБОЛ": cls.BASEBALL, "BÉISBOL": cls.BASEBALL,
            "ГАНДБОЛ": cls.HANDBALL, "BALONMANO": cls.HANDBALL,
            "КИБЕРСПОРТ": cls.ESPORTS, "CYBERSPORT": cls.ESPORTS,
            "ММА": cls.MMA, "БОКС": cls.BOXING, "BOXE": cls.BOXING, "BOXEO": cls.BOXING,
        }
        return aliases.get(clean, cls.UNKNOWN)

    def to_localized(self, lang: str = "ru") -> str:
        lang = lang.lower()
        names = {
            SportType.FOOTBALL: {"ru": "Футбол", "en": "Football", "fr": "Football", "es": "Fútbol"},
            SportType.BASKETBALL: {"ru": "Баскетбол", "en": "Basketball", "fr": "Basketball", "es": "Baloncesto"},
            SportType.HOCKEY: {"ru": "Хоккей", "en": "Ice Hockey", "fr": "Hockey sur glace", "es": "Hockey sobre hielo"},
            SportType.TENNIS: {"ru": "Теннис", "en": "Tennis", "fr": "Tennis", "es": "Tenis"},
            SportType.VOLLEYBALL: {"ru": "Волейбол", "en": "Volleyball", "fr": "Volleyball", "es": "Voleibol"},
            SportType.TABLE_TENNIS: {"ru": "Настольный теннис", "en": "Table Tennis", "fr": "Tennis de table", "es": "Tenis de mesa"},
            SportType.BASEBALL: {"ru": "Бейсбол", "en": "Baseball", "fr": "Baseball", "es": "Béisbol"},
            SportType.HANDBALL: {"ru": "Гандбол", "en": "Handball", "fr": "Handball", "es": "Balonmano"},
            SportType.ESPORTS: {"ru": "Киберспорт", "en": "Esports", "fr": "Esports", "es": "Esports"},
            SportType.MMA: {"ru": "ММА", "en": "MMA", "fr": "MMA", "es": "MMA"},
            SportType.BOXING: {"ru": "Бокс", "en": "Boxing", "fr": "Boxe", "es": "Boxeo"},
            SportType.AMERICAN_FOOTBALL: {"ru": "Американский футбол", "en": "American Football", "fr": "Football américain", "es": "Fútbol americano"},
        }
        return names.get(self, {}).get(lang, self.name.replace("_", " ").title())


# ==============================================================================
# Bet Scope (mirrors pro.datawiki.igaming.dto.market.BetScope)
# ==============================================================================

class BetScope(str, Enum):
    FULL_MATCH = ""
    FULL_MATCH_INCLUDING_OT = "OT_"
    FIRST_HALF = "HALFTIME_"
    SECOND_HALF = "SECOND_HALF_"
    PERIOD_1 = "PERIOD_1_"
    PERIOD_2 = "PERIOD_2_"
    PERIOD_3 = "PERIOD_3_"
    SET_1 = "SET_1_"
    SET_2 = "SET_2_"
    SET_3 = "SET_3_"

    @property
    def prefix(self) -> str:
        return self.value


# ==============================================================================
# Canonical BetType Base and Implementations (mirrors BetType records)
# ==============================================================================

class BetType(ABC):
    """Abstract base matching pro.datawiki.igaming.dto.BetType."""

    @abstractmethod
    def code(self) -> str:
        """Canonical unique code for JSON & storage (e.g., WIN1, TOTAL_OVER_2.5)."""
        pass

    @abstractmethod
    def market_code(self) -> str:
        """Canonical market grouping code (e.g., MATCH_RESULT, TOTAL)."""
        pass

    @abstractmethod
    def description(self) -> str:
        pass

    def base_code(self) -> str:
        return self.code()

    def get_param(self) -> Optional[float]:
        return None


# 1. MatchResultBet (mirrors pro.datawiki.igaming.dto.market.MatchResultBet)
class MatchResultOutcome(str, Enum):
    WIN1 = "WIN1"
    DRAW = "DRAW"
    WIN2 = "WIN2"
    WIN1_2WAY = "WIN1_2WAY"
    WIN2_2WAY = "WIN2_2WAY"
    DC_1X = "DC_1X"
    DC_12 = "DC_12"
    DC_X2 = "DC_X2"


@dataclass(frozen=True)
class MatchResultBet(BetType):
    outcome: MatchResultOutcome
    scope: BetScope = BetScope.FULL_MATCH

    def code(self) -> str:
        return f"{self.scope.prefix}{self.outcome.value}"

    def market_code(self) -> str:
        suffix = "MATCH_RESULT_2WAY" if "2WAY" in self.outcome.value else "MATCH_RESULT"
        return f"{self.scope.prefix}{suffix}"

    def description(self) -> str:
        return f"Result {self.outcome.value}"


# 2. TotalBet (mirrors pro.datawiki.igaming.dto.market.TotalBet)
class TotalDirection(str, Enum):
    OVER = "OVER"
    UNDER = "UNDER"
    EXACT = "EXACT"


@dataclass(frozen=True)
class TotalBet(BetType):
    direction: TotalDirection
    param: float
    is_asian: bool = False
    scope: BetScope = BetScope.FULL_MATCH

    def code(self) -> str:
        prefix = f"{self.scope.prefix}{'ASIAN_' if self.is_asian else ''}TOTAL_{self.direction.value}"
        p_str = f"{self.param:g}"
        return f"{prefix}_{p_str}"

    def base_code(self) -> str:
        return f"{self.scope.prefix}{'ASIAN_' if self.is_asian else ''}TOTAL_{self.direction.value}"

    def market_code(self) -> str:
        return f"{self.scope.prefix}{'ASIAN_' if self.is_asian else ''}TOTAL"

    def description(self) -> str:
        return f"Total {'Asian ' if self.is_asian else ''}{self.direction.value} {self.param:g}"

    def get_param(self) -> Optional[float]:
        return self.param


# 3. HandicapBet (mirrors pro.datawiki.igaming.dto.market.HandicapBet)
class HandicapTeam(str, Enum):
    TEAM1 = "1"
    TEAM2 = "2"
    DRAW = "DRAW"


@dataclass(frozen=True)
class HandicapBet(BetType):
    team: HandicapTeam
    param: float
    is_asian: bool = False
    scope: BetScope = BetScope.FULL_MATCH

    def code(self) -> str:
        prefix = f"{self.scope.prefix}{'ASIAN_' if self.is_asian else ''}HANDICAP_{self.team.value}"
        p_str = f"{self.param:g}"
        return f"{prefix}_{p_str}"

    def base_code(self) -> str:
        return f"{self.scope.prefix}{'ASIAN_' if self.is_asian else ''}HANDICAP_{self.team.value}"

    def market_code(self) -> str:
        return f"{self.scope.prefix}{'ASIAN_' if self.is_asian else ''}HANDICAP"

    def description(self) -> str:
        return f"Handicap {self.team.value} ({self.param:g})"

    def get_param(self) -> Optional[float]:
        return self.param


# 4. BinaryMarketBet (mirrors pro.datawiki.igaming.dto.market.BinaryMarketBet)
class BinaryMarketType(str, Enum):
    BTTS = "BTTS"
    BOTH_HALVES_BTTS = "BOTH_HALVES_BTTS"
    TO_SCORE_IN_BOTH_HALVES = "TO_SCORE_IN_BOTH_HALVES"
    ODD_EVEN = "ODD_EVEN"
    RED_CARD = "RED_CARD"
    CLEAN_SHEET = "CLEAN_SHEET"
    QUALIFY = "QUALIFY"
    OVERTIME = "OVERTIME"


class BinaryOutcome(str, Enum):
    YES = "YES"
    NO = "NO"
    ODD = "ODD"
    EVEN = "EVEN"


@dataclass(frozen=True)
class BinaryMarketBet(BetType):
    market_type: BinaryMarketType
    outcome: BinaryOutcome
    scope: BetScope = BetScope.FULL_MATCH

    def code(self) -> str:
        return f"{self.scope.prefix}{self.market_type.value}_{self.outcome.value}"

    def market_code(self) -> str:
        return f"{self.scope.prefix}{self.market_type.value}"

    def description(self) -> str:
        return f"{self.market_type.value} {self.outcome.value}"


# 5. UnknownBet (fallback for unmapped custom markets)
@dataclass(frozen=True)
class UnknownBet(BetType):
    raw_code: str

    def code(self) -> str:
        return self.raw_code

    def market_code(self) -> str:
        return "UNKNOWN"

    def description(self) -> str:
        return self.raw_code


# ==============================================================================
# Canonical BetTypeRegistry & Resolver (mirrors BetTypeRegistry.java)
# ==============================================================================

class BetTypeRegistry:
    """Registry and parser matching pro.datawiki.igaming.dto.BetTypeRegistry."""

    @staticmethod
    def from_code(code: str) -> BetType:
        if not code:
            return UnknownBet("")
        norm = code.strip().upper()

        # MatchResult
        for outcome in MatchResultOutcome:
            if norm == outcome.value or norm == f"FULL_MATCH_{outcome.value}":
                return MatchResultBet(outcome)

        # Totals (e.g. TOTAL_OVER (+4.5), TOTAL_OVER_4.5, TOTAL_OVER (161.5), OVER (+2.5), etc.)
        m_tot = re.match(r"^(?:FULL_MATCH_)?(?:(ASIAN)_)?(?:TOTAL_)?(OVER|UNDER|EXACT)[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?$", norm)
        if m_tot:
            is_asian = bool(m_tot.group(1))
            dir_str = m_tot.group(2)
            param = float(m_tot.group(3))
            return TotalBet(direction=TotalDirection(dir_str), param=param, is_asian=is_asian)

        # Team Totals (e.g. TEAM1_TOTAL_OVER (+1.5))
        m_ttot = re.match(r"^(?:FULL_MATCH_)?(?:(ASIAN)_)?TEAM[_\s]*(1|2)[_\s]*TOTAL_(OVER|UNDER|EXACT)[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?$", norm)
        if m_ttot:
            is_asian = bool(m_ttot.group(1))
            dir_str = m_ttot.group(3)
            param = float(m_ttot.group(4))
            return TotalBet(direction=TotalDirection(dir_str), param=param, is_asian=is_asian)

        # Handicaps (e.g. HANDICAP_1 (-1.5), HANDICAP_2 (+1.5), HANDICAP_1_-1.5, HANDICAP_1 -1.5, etc.)
        m_hnd = re.match(r"^(?:FULL_MATCH_)?(?:(ASIAN)_)?HANDICAP[_\s]*(1|2|TEAM1|TEAM2)[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?$", norm)
        if m_hnd:
            is_asian = bool(m_hnd.group(1))
            team_str = "1" if m_hnd.group(2) in ("1", "TEAM1") else "2"
            param = float(m_hnd.group(3))
            return HandicapBet(team=HandicapTeam(team_str), param=param, is_asian=is_asian)

        # Handicaps without parameter
        m_hnd_noparam = re.match(r"^(?:FULL_MATCH_)?(?:(ASIAN)_)?HANDICAP[_\s]*(1|2|TEAM1|TEAM2)$", norm)
        if m_hnd_noparam:
            is_asian = bool(m_hnd_noparam.group(1))
            team_str = "1" if m_hnd_noparam.group(2) in ("1", "TEAM1") else "2"
            return HandicapBet(team=HandicapTeam(team_str), param=0.0, is_asian=is_asian)

        # Binary markets
        for m_type in BinaryMarketType:
            for b_out in BinaryOutcome:
                expected = f"{m_type.value}_{b_out.value}"
                if norm == expected or norm == f"FULL_MATCH_{expected}":
                    return BinaryMarketBet(market_type=m_type, outcome=b_out)

        # If not standard canonical code, try legacy resolver
        resolved = BetTypeRegistry.resolve(code)
        if resolved:
            return resolved

        return UnknownBet(code)

    @staticmethod
    def resolve(raw_name: str) -> Optional[BetType]:
        """
        Attempts to resolve arbitrary string representations (Russian, English, etc.)
        into canonical BetType implementations. Matches BetTypeRegistry.resolve()
        and AbstractBetTypeMapper in igaming-source-core.
        """
        if not raw_name:
            return None
        clean = raw_name.strip().upper()
        clean_compact = re.sub(r"[\s()\-]+", "", clean).replace(",", ".")

        # 1. 1X2 and Double Chance (map1X2DCRecord in AbstractBetTypeMapper)
        # Check Win 1
        if clean in ("1", "HOME", "П1", "P1", "W1", "ПОБЕДА 1", "ПОБЕДА1") or clean_compact in ("П1", "W1", "1", "ПОБЕДА1", "ПОБЕДА1П1", "П1ПОБЕДА1"):
            return MatchResultBet(MatchResultOutcome.WIN1)
        if "ПОБЕДА 1" in clean or "ПОБЕДА1" in clean_compact or clean.startswith("П1 ") or clean.endswith(" П1") or clean == "П1":
            if not ("Х" in clean or "X" in clean or "2" in clean):
                return MatchResultBet(MatchResultOutcome.WIN1)

        # Check Win 2
        if clean in ("2", "AWAY", "П2", "P2", "W2", "ПОБЕДА 2", "ПОБЕДА2") or clean_compact in ("П2", "W2", "2", "ПОБЕДА2", "ПОБЕДА2П2", "П2ПОБЕДА2"):
            return MatchResultBet(MatchResultOutcome.WIN2)
        if "ПОБЕДА 2" in clean or "ПОБЕДА2" in clean_compact or clean.startswith("П2 ") or clean.endswith(" П2") or clean == "П2":
            if not ("Х" in clean or "X" in clean or "1" in clean):
                return MatchResultBet(MatchResultOutcome.WIN2)

        # Check Draw
        if clean in ("X", "DRAW", "НИЧЬЯ", "Х", "ПХ") or clean_compact in ("X", "Х", "НИЧЬЯ", "DRAW"):
            return MatchResultBet(MatchResultOutcome.DRAW)

        # Double Chance: 1X
        if clean_compact in ("1X", "1Х", "HD", "П1Х", "1-X", "ПОБЕДА1ИЛИНИЧЬЯ", "1ИЛИХ", "1ИЛИX", "1XПОБЕДА1ИЛИНИЧЬЯ") or "1X" in clean_compact or "1Х" in clean_compact or "1ИЛИХ" in clean_compact or "1ИЛИX" in clean_compact:
            if not ("2" in clean_compact):
                return MatchResultBet(MatchResultOutcome.DC_1X)

        # Double Chance: X2
        if clean_compact in ("X2", "Х2", "AD", "ПХ2", "X-2", "Х-2", "НИЧЬЯИЛИПОБЕДА2", "ХИЛИ2", "XИЛИ2", "Х2НИЧЬЯИЛИП2", "X2НИЧЬЯИЛИП2") or "X2" in clean_compact or "Х2" in clean_compact or "ХИЛИ2" in clean_compact or "XИЛИ2" in clean_compact:
            if not ("1" in clean_compact):
                return MatchResultBet(MatchResultOutcome.DC_X2)

        # Double Chance: 12
        if clean_compact in ("12", "HA", "П1П2", "1-2", "ПОБЕДА1ИЛИПОБЕДА2", "1ИЛИ2") or "12" in clean_compact or "1ИЛИ2" in clean_compact:
            return MatchResultBet(MatchResultOutcome.DC_12)

        # 2. Totals (mapTotalRecord in AbstractBetTypeMapper)
        m_tot_over = re.search(r"(?:ТОТАЛ\s*БОЛЬШЕ|ТБ|TOTAL[_\s]*OVER|OVER|TOTБ|БОЛЬШЕ)[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?", clean)
        if m_tot_over:
            return TotalBet(direction=TotalDirection.OVER, param=float(m_tot_over.group(1)))

        m_tot_under = re.search(r"(?:ТОТАЛ\s*МЕНЬШЕ|ТМ|TOTAL[_\s]*UNDER|UNDER|TOTМ|МЕНЬШЕ)[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?", clean)
        if m_tot_under:
            return TotalBet(direction=TotalDirection.UNDER, param=float(m_tot_under.group(1)))

        # 3. Handicaps (mapHandicapRecord in AbstractBetTypeMapper)
        m_h1 = re.search(r"(?:ФОРА\s*1|Ф1|F1|HANDICAP[_\s]*(?:1|TEAM1))[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?", clean)
        if m_h1:
            return HandicapBet(team=HandicapTeam.TEAM1, param=float(m_h1.group(1)))

        m_h2 = re.search(r"(?:ФОРА\s*2|Ф2|F2|HANDICAP[_\s]*(?:2|TEAM2))[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?", clean)
        if m_h2:
            return HandicapBet(team=HandicapTeam.TEAM2, param=float(m_h2.group(1)))

        # 4. BTTS
        if "ОБЕ ЗАБЬЮТ" in clean or "BTTS" in clean:
            is_yes = any(y in clean for y in ["ДА", "YES", "OUI", "SÍ", "SI"])
            return BinaryMarketBet(market_type=BinaryMarketType.BTTS, outcome=BinaryOutcome.YES if is_yes else BinaryOutcome.NO)

        return None


# ==============================================================================
# Multilingual BetType Renderer
# ==============================================================================

def parse_match_teams(event_name: str) -> Tuple[str, str]:
    """Splits match title into team1 and team2 after stripping any sport tag."""
    if not event_name:
        return "", ""
    clean_event = re.sub(r"\s*\([A-Za-z0-9_ -]+\)\s*$", "", event_name).strip()
    delims = [" vs ", " VS ", " — ", " - ", " – ", " v ", " V "]
    for d in delims:
        if d in clean_event:
            p = clean_event.split(d, 1)
            return p[0].strip(), p[1].strip()
    return "", ""


class BetTypeRenderer:
    """
    Renders canonical BetType instances into target channel languages:
    - ru (Russian)
    - en (English)
    - fr (French)
    - es (Spanish)
    """

    @classmethod
    def render(
        cls,
        bet_type_input: Union[BetType, str],
        lang: str = "ru",
        event_name: str = "",
        team1: str = "",
        team2: str = "",
        sport: str = ""
    ) -> str:
        lang = (lang or "ru").lower()
        if not team1 or not team2:
            t1, t2 = parse_match_teams(event_name)
            team1 = team1 or t1
            team2 = team2 or t2

        # 1. Resolve to BetType instance
        if isinstance(bet_type_input, BetType):
            bet = bet_type_input
        else:
            bet = BetTypeRegistry.from_code(bet_type_input)

        # 2. Render MatchResultBet
        if isinstance(bet, MatchResultBet):
            return cls._render_match_result(bet.outcome, lang, team1, team2)

        # 3. Render TotalBet
        if isinstance(bet, TotalBet):
            sport_obj = SportType.resolve(sport) if sport else SportType.UNKNOWN
            return cls._render_total(bet.direction, bet.param, bet.is_asian, lang, sport_obj)

        # 4. Render HandicapBet
        if isinstance(bet, HandicapBet):
            return cls._render_handicap(bet.team, bet.param, bet.is_asian, lang, team1, team2)

        # 5. Render BinaryMarketBet
        if isinstance(bet, BinaryMarketBet):
            return cls._render_binary(bet.market_type, bet.outcome, lang)

        # 6. Fallback for UnknownBet
        raw = bet.code()
        # Fallback humanizer to prevent technical constants leaking
        norm_raw = raw.strip()
        m_h1 = re.search(r"HANDICAP[_\s]*1[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?", norm_raw, re.I)
        if m_h1:
            return cls._render_handicap(HandicapTeam.TEAM1, float(m_h1.group(1)), False, lang, team1, team2)
        m_h2 = re.search(r"HANDICAP[_\s]*2[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?", norm_raw, re.I)
        if m_h2:
            return cls._render_handicap(HandicapTeam.TEAM2, float(m_h2.group(1)), False, lang, team1, team2)
        m_to = re.search(r"TOTAL[_\s]*OVER[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?", norm_raw, re.I)
        if m_to:
            sport_obj = SportType.resolve(sport) if sport else SportType.UNKNOWN
            return cls._render_total(TotalDirection.OVER, float(m_to.group(1)), False, lang, sport_obj)
        m_tu = re.search(r"TOTAL[_\s]*UNDER[_\s]*(?:\(\s*)?([+-]?[0-9]+(?:\.[0-9]+)?)(?:\s*\))?", norm_raw, re.I)
        if m_tu:
            sport_obj = SportType.resolve(sport) if sport else SportType.UNKNOWN
            return cls._render_total(TotalDirection.UNDER, float(m_tu.group(1)), False, lang, sport_obj)
        if lang != "ru":
            # Sanitize Cyrillic 'Х' to Latin 'X'
            return raw.replace("Х", "X")
        return raw

    @staticmethod
    def _render_match_result(outcome: MatchResultOutcome, lang: str, team1: str, team2: str) -> str:
        if outcome in (MatchResultOutcome.WIN1, MatchResultOutcome.WIN1_2WAY):
            t_label = team1 if team1 else "1"
            if lang == "ru":
                return f"Победа 1 (П1)" if not team1 else f"Победа {team1} (П1)"
            elif lang == "fr":
                return f"Victoire {t_label} (1)"
            elif lang == "es":
                return f"Gana {t_label} (1)"
            else:
                return f"{t_label} to Win (1)" if team1 else "Home Win (1)"

        if outcome == MatchResultOutcome.DRAW:
            if lang == "ru":
                return "Ничья (X)"
            elif lang == "fr":
                return "Match Nul (N)"
            elif lang == "es":
                return "Empate (X)"
            else:
                return "Draw (X)"

        if outcome in (MatchResultOutcome.WIN2, MatchResultOutcome.WIN2_2WAY):
            t_label = team2 if team2 else "2"
            if lang == "ru":
                return f"Победа 2 (П2)" if not team2 else f"Победа {team2} (П2)"
            elif lang == "fr":
                return f"Victoire {t_label} (2)"
            elif lang == "es":
                return f"Gana {t_label} (2)"
            else:
                return f"{t_label} to Win (2)" if team2 else "Away Win (2)"

        if outcome == MatchResultOutcome.DC_1X:
            t_label = team1 if team1 else "1"
            if lang == "ru":
                return f"1X ({team1} или ничья)" if team1 else "1X (П1 или ничья)"
            elif lang == "fr":
                return f"{t_label} ou Nul (1N)"
            elif lang == "es":
                return f"{t_label} o Empate (1X)"
            else:
                return f"{t_label} or Draw (1X)" if team1 else "Home or Draw (1X)"

        if outcome == MatchResultOutcome.DC_X2:
            t_label = team2 if team2 else "2"
            if lang == "ru":
                # Russian uses Cyrillic Х in standard sports press
                return f"Х2 (Ничья или {team2})" if team2 else "Х2 (Ничья или П2)"
            elif lang == "fr":
                return f"Nul ou {t_label} (N2)"
            elif lang == "es":
                return f"Empate o {t_label} (X2)"
            else:
                return f"Draw or {t_label} (X2)" if team2 else "Draw or Away (X2)"

        if outcome == MatchResultOutcome.DC_12:
            t1_lbl = team1 if team1 else "1"
            t2_lbl = team2 if team2 else "2"
            if lang == "ru":
                return f"12 ({team1} или {team2})" if (team1 and team2) else "12 (П1 или П2)"
            elif lang == "fr":
                return f"{t1_lbl} ou {t2_lbl} (12)"
            elif lang == "es":
                return f"{t1_lbl} o {t2_lbl} (12)"
            else:
                return f"{t1_lbl} or {t2_lbl} (12)" if (team1 and team2) else "Home or Away (12)"

        return outcome.value

    @staticmethod
    def _render_total(direction: TotalDirection, param: float, is_asian: bool, lang: str, sport: SportType = SportType.UNKNOWN) -> str:
        param_str = f"{param:g}"
        if sport in (SportType.BASKETBALL, SportType.VOLLEYBALL, SportType.TABLE_TENNIS, SportType.TENNIS):
            unit_en = "Points"
            unit_fr = "points"
            unit_es = "puntos"
            unit_ru = "очков"
        else:
            unit_en = "Goals"
            unit_fr = "buts"
            unit_es = "goles"
            unit_ru = ""

        prefix_asian = "Азиатский " if (is_asian and lang == "ru") else ("Asian " if is_asian else "")
        if direction == TotalDirection.OVER:
            if lang == "ru":
                return f"{prefix_asian}Тотал больше {param_str}" + (f" {unit_ru}" if unit_ru else "")
            elif lang == "fr":
                return f"{prefix_asian}Plus de {param_str} {unit_fr}"
            elif lang == "es":
                return f"{prefix_asian}Más de {param_str} {unit_es}"
            else:
                return f"{prefix_asian}Over {param_str} {unit_en}"
        elif direction == TotalDirection.UNDER:
            if lang == "ru":
                return f"{prefix_asian}Тотал меньше {param_str}" + (f" {unit_ru}" if unit_ru else "")
            elif lang == "fr":
                return f"{prefix_asian}Moins de {param_str} {unit_fr}"
            elif lang == "es":
                return f"{prefix_asian}Menos de {param_str} {unit_es}"
            else:
                return f"{prefix_asian}Under {param_str} {unit_en}"
        else:
            if lang == "ru":
                return f"Ровно {param_str}" + (f" {unit_ru}" if unit_ru else "")
            else:
                return f"Exactly {param_str} {unit_en}"

    @staticmethod
    def _render_handicap(team: HandicapTeam, param: float, is_asian: bool, lang: str, team1: str, team2: str) -> str:
        sign = f"{param:+g}"
        asian_tag = " (Азиатская)" if (is_asian and lang == "ru") else (" (Asian)" if is_asian else "")
        team_name = team1 if team == HandicapTeam.TEAM1 else team2
        team_clean = team_name.strip() if (team_name and len(team_name.strip()) <= 30) else ""

        if team == HandicapTeam.TEAM1:
            if lang == "ru":
                lbl = f"Фора 1 ({team_clean})" if team_clean else "Фора 1"
                return f"{lbl} ({sign}){asian_tag}"
            elif lang == "es":
                lbl = f"Hándicap 1 ({team_clean})" if team_clean else "Hándicap 1"
                return f"{lbl} ({sign}){asian_tag}"
            elif lang == "fr":
                lbl = f"Handicap 1 ({team_clean})" if team_clean else "Handicap 1"
                return f"{lbl} ({sign}){asian_tag}"
            else:
                lbl = f"Handicap 1 ({team_clean})" if team_clean else "Handicap 1"
                return f"{lbl} ({sign}){asian_tag}"
        else:
            if lang == "ru":
                lbl = f"Фора 2 ({team_clean})" if team_clean else "Фора 2"
                return f"{lbl} ({sign}){asian_tag}"
            elif lang == "es":
                lbl = f"Hándicap 2 ({team_clean})" if team_clean else "Hándicap 2"
                return f"{lbl} ({sign}){asian_tag}"
            elif lang == "fr":
                lbl = f"Handicap 2 ({team_clean})" if team_clean else "Handicap 2"
                return f"{lbl} ({sign}){asian_tag}"
            else:
                lbl = f"Handicap 2 ({team_clean})" if team_clean else "Handicap 2"
                return f"{lbl} ({sign}){asian_tag}"

    @staticmethod
    def _render_binary(m_type: BinaryMarketType, outcome: BinaryOutcome, lang: str) -> str:
        if m_type == BinaryMarketType.BTTS:
            is_yes = (outcome == BinaryOutcome.YES)
            if lang == "ru":
                return f"Обе забьют: {'Да' if is_yes else 'Нет'}"
            elif lang == "fr":
                return f"Les deux équipes marquent : {'Oui' if is_yes else 'Non'}"
            elif lang == "es":
                return f"Ambos equipos marcan: {'Sí' if is_yes else 'No'}"
            else:
                return f"Both Teams To Score: {'Yes' if is_yes else 'No'}"

        if m_type == BinaryMarketType.ODD_EVEN:
            is_odd = (outcome == BinaryOutcome.ODD)
            if lang == "ru":
                return "Нечетный тотал" if is_odd else "Четный тотал"
            elif lang == "fr":
                return "Total impair" if is_odd else "Total pair"
            elif lang == "es":
                return "Total impar" if is_odd else "Total par"
            else:
                return "Odd Total" if is_odd else "Even Total"

        return f"{m_type.value} {outcome.value}"
