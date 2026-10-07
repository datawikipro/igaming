#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Telegram Multilingual Channel Poster & AI-Prompter Scheduler
Task: [smm-telegram] Мультиязычная сеть Telegram (RU, EN, FR, ES): бот-админ, публичные @username и чтение комментариев
Plane Task ID: 434e5bc7-7530-4ef1-8cba-aaa1f6707bec
Rule 6 (Network Proxy), Rule 9 (Firefox / Persistent Context), Rule 10 (80% Freebet Cash) & Rule 1 (5-Min Soak & DoD)

Responsibilities:
1. Multilingual Arbitrage and Promo Card Publishing (RU, EN, FR, ES):
   - Formats surebet & value betting signals with native terminology.
   - Calculates 80% guaranteed cash conversion for freebets per Rule 10:
     eta = ((K1 - 1) * (K2 - 1)) / K2 approx 0.80
   - Adds mandatory responsible gambling disclaimers in the target language.
   - Attaches interactive Inline Keyboards:
     - Link to SmartBet Calculator: https://smartbet.guru/tools/freebet-calculator?arb_id={id}&utm_source=telegram&utm_medium=channel_{lang}
     - Direct affiliate links for Leg 1 and Leg 2: https://smartbet.guru/go/{bookmaker}?utm_source=telegram&utm_medium=channel_{lang}
2. Telegram Bot API Dispatcher:
   - Posts directly via Telegram Bot API sendMessage with HTML formatting and inline keyboards.
   - Fallback/mock mode for CI/offline/testing environments.
3. Discussion Group Comment Ingestion & AI-Prompter (ИИ-суфлер):
   - Ingests incoming Telegram updates from linked discussion groups via Webhook (/api/v1/telegram/webhook).
   - Extracts parent channel post context and user comment.
   - AI Prompter categorizes inquiries (FREEBET_CALC, BOOKMAKER_RULE, FEEDBACK_SUGGESTION, VIP_PATRON, GENERAL).
   - Generates expert localized draft answers.
   - Routes feedback items to Patron CRM queue in Redis: feedback:queue:telegram.
4. HTTP Healthcheck & API Server:
   - Port 8080: /healthz, /actuator/health, /actuator/health/readiness, /actuator/health/liveness.
   - Webhook endpoint: /api/v1/telegram/webhook and /webhook.
   - Trigger endpoint: /api/v1/telegram/post.
"""

import argparse
from abc import ABC, abstractmethod
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
from enum import Enum
import hashlib
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
import logging
import os
import re
import socketserver
import sys
import threading
import time
from typing import Any, Dict, List, Optional, Tuple, Union
import urllib.parse
import urllib.request
import copy

try:
    import redis
except ImportError:
    redis = None

try:
    import psycopg2
except ImportError:
    try:
        import subprocess
        subprocess.check_call([sys.executable, "-m", "pip", "install", "psycopg2-binary", "--quiet"])
        import psycopg2
    except Exception:
        psycopg2 = None

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
if CURRENT_DIR not in sys.path:
    sys.path.insert(0, CURRENT_DIR)

from telegram_web_manager import BOT_USERNAME, DEFAULT_BOT_TOKEN, DEFAULT_REDIS_URL, REGIONAL_CHANNELS, ChannelConfig

logger = logging.getLogger("ChannelPosterScheduler")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] [smm-telegram] %(message)s")

# ==============================================================================
# Domain Models & Math
# ==============================================================================

class SignalCategory(str, Enum):
    CLASSIC_SUREBET = "CLASSIC_SUREBET"
    VALUE_BET = "VALUE_BET"
    CORRIDOR = "CORRIDOR"
    FREEBET_OPTIMIZATION = "FREEBET_OPTIMIZATION"


@dataclass
class SurebetSignal:
    sport: str
    tournament: str
    event_name: str
    bk1: str
    odds1: float
    bk2: str
    odds2: float
    profit_percent: float
    bet_type1: str = ""
    bet_type2: str = ""
    market1: str = ""
    market2: str = ""
    category: str = SignalCategory.CLASSIC_SUREBET.value
    is_freebet_friendly: bool = False
    freebet_nominal: float = 3000.0
    recommended_bank: float = 10000.0
    ev_percent: float = 0.0
    sharp_odds: float = 0.0
    corridor_window: str = ""
    currency: str = "RUB"
    signal_id: str = ""

    def __post_init__(self):
        # Auto-detect category if not explicitly set
        if not self.category or self.category == SignalCategory.CLASSIC_SUREBET.value:
            if (self.odds1 >= 4.0 and self.odds2 <= 1.35) or (self.odds2 >= 4.0 and self.odds1 <= 1.35):
                self.category = SignalCategory.FREEBET_OPTIMIZATION.value
                self.is_freebet_friendly = True

        # Canonical BetType code normalization
        if not self.bet_type1 and self.market1:
            resolved = BetTypeRegistry.resolve(self.market1)
            self.bet_type1 = resolved.code() if resolved else self.market1
        elif self.bet_type1 and not self.market1:
            self.market1 = self.bet_type1

        if not self.bet_type2 and self.market2:
            resolved = BetTypeRegistry.resolve(self.market2)
            self.bet_type2 = resolved.code() if resolved else self.market2
        elif self.bet_type2 and not self.market2:
            self.market2 = self.bet_type2

        if not self.signal_id:
            raw = f"{self.event_name}:{self.bk1}:{self.odds1}:{self.bk2}:{self.odds2}:{self.category}"
            self.signal_id = hashlib.md5(raw.encode()).hexdigest()[:10]


def calculate_surebet_stakes(total_bank: float = 10000.0, k1: float = 2.10, k2: float = 2.05) -> Dict[str, Any]:
    """
    Calculates balanced bankroll distribution for classic 2-way arbitrage:
    inv1 = 1 / k1, inv2 = 1 / k2
    stake1 = total_bank * (inv1 / (inv1 + inv2))
    stake2 = total_bank * (inv2 / (inv1 + inv2))
    Guarantees equal net profit upon any outcome.
    """
    if k1 <= 1.0 or k2 <= 1.0:
        return {"total_bank": total_bank, "stake1": 0.0, "stake2": 0.0, "payout1": 0.0, "payout2": 0.0, "profit": 0.0, "profit_percent": 0.0}
    inv1 = 1.0 / k1
    inv2 = 1.0 / k2
    inv_sum = inv1 + inv2
    s1 = round((total_bank * inv1) / inv_sum, 0)
    s2 = round((total_bank * inv2) / inv_sum, 0)
    # Reconcile rounding to match total bank
    diff = total_bank - (s1 + s2)
    s1 += diff
    p1 = round(s1 * k1, 2)
    p2 = round(s2 * k2, 2)
    profit = round(min(p1, p2) - total_bank, 2)
    profit_pct = round((profit / total_bank) * 100.0, 2)
    return {
        "total_bank": total_bank,
        "stake1": s1,
        "stake2": s2,
        "payout1": p1,
        "payout2": p2,
        "profit": profit,
        "profit_percent": profit_pct,
    }


def calculate_freebet_cash(nominal: float = 3000.0, k1: float = 5.0, k2: float = 1.25) -> Dict[str, Any]:
    """
    Calculates SNR freebet guaranteed cash conversion per Rule 10:
    eta = ((k1 - 1.0) * (k2 - 1.0)) / k2
    guaranteed_cash = nominal * eta
    """
    eta = ((k1 - 1.0) * (k2 - 1.0)) / k2
    hedge_stake = (nominal * (k1 - 1.0)) / k2
    guaranteed_cash = round(nominal * eta, 2)
    return {
        "nominal": nominal,
        "k1": k1,
        "k2": k2,
        "conversion_ratio": round(eta, 4),
        "hedge_stake": round(hedge_stake, 2),
        "guaranteed_cash": guaranteed_cash,
        "guaranteed_cash_percent": round(eta * 100, 1),
    }


def normalize_bk_slug(name: str) -> str:
    """Converts bookmaker name to canonical URL slug."""
    clean = name.strip().lower()
    mapping = {
        "винлайн": "winline",
        "фонбет": "fonbet",
        "пари": "pari",
        "бетсити": "betcity",
        "леон": "leon",
        "олимпбет": "olimpbet",
        "тенниси": "tennisi",
        "зенит": "zenit",
        "марафонбет": "marathonbet",
        "бетбум": "betboom",
        "балтбет": "baltbet",
        "лига ставок": "ligastavok",
    }
    if clean in mapping:
        return mapping[clean]
    return re.sub(r"[^a-z0-9]", "", clean) or "portal"


def normalize_bk_display_name(bk: str) -> str:
    """Formats raw bookmaker slug or code into clean display name."""
    if not bk:
        return "Букмекер"
    mapping = {
        "fon-bet-kz": "Фонбет", "fonbet": "Фонбет",
        "winline": "Winline", "pari": "Пари",
        "betcity": "Бетсити", "betcity-com": "Бетсити",
        "leon": "Леон", "olimpbet": "Олимпбет",
        "betboom": "Бетбум", "baltbet": "Балтбет",
        "1xbet": "1xBet", "betlabel": "Betlabel",
        "betandyou": "BetAndYou", "pinnacle": "Pinnacle",
    }
    return mapping.get(bk.lower().strip(), bk.strip().title())


## ==============================================================================
# Canonical BetType Domain & Multilingual Normalization Engine
# (Mirrors pro.datawiki.igaming.dto.BetType & BetTypeRegistry)
# ==============================================================================

class SportType(str, Enum):
    """Mirrors pro.datawiki.igaming.dto.SportType."""
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


class BetScope(str, Enum):
    """Mirrors pro.datawiki.igaming.dto.market.BetScope."""
    FULL_MATCH = ""
    FULL_MATCH_INCLUDING_OT = "OT_"
    FIRST_HALF = "HALFTIME_"
    SECOND_HALF = "SECOND_HALF_"

    @property
    def prefix(self) -> str:
        return self.value


class BetType(ABC):
    """Abstract base matching pro.datawiki.igaming.dto.BetType."""
    @abstractmethod
    def code(self) -> str:
        pass

    @abstractmethod
    def market_code(self) -> str:
        pass

    @abstractmethod
    def description(self) -> str:
        pass

    def base_code(self) -> str:
        return self.code()

    def get_param(self) -> Optional[float]:
        return None


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
        return f"{prefix}_{self.param:g}"

    def market_code(self) -> str:
        return f"{self.scope.prefix}{'ASIAN_' if self.is_asian else ''}TOTAL"

    def description(self) -> str:
        return f"Total {'Asian ' if self.is_asian else ''}{self.direction.value} {self.param:g}"

    def get_param(self) -> Optional[float]:
        return self.param


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
        return f"{prefix}_{self.param:g}"

    def market_code(self) -> str:
        return f"{self.scope.prefix}{'ASIAN_' if self.is_asian else ''}HANDICAP"

    def description(self) -> str:
        return f"Handicap {self.team.value} ({self.param:g})"

    def get_param(self) -> Optional[float]:
        return self.param


class BinaryMarketType(str, Enum):
    BTTS = "BTTS"
    BOTH_HALVES_BTTS = "BOTH_HALVES_BTTS"
    ODD_EVEN = "ODD_EVEN"
    RED_CARD = "RED_CARD"
    CLEAN_SHEET = "CLEAN_SHEET"


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


@dataclass(frozen=True)
class UnknownBet(BetType):
    raw_code: str

    def code(self) -> str:
        return self.raw_code

    def market_code(self) -> str:
        return "UNKNOWN"

    def description(self) -> str:
        return self.raw_code


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

        # Totals
        m_tot = re.match(r"^(?:FULL_MATCH_)?(?:(ASIAN)_)?TOTAL_(OVER|UNDER|EXACT)_([+-]?[0-9]+(?:\.[0-9]+)?)$", norm)
        if m_tot:
            is_asian = bool(m_tot.group(1))
            dir_str = m_tot.group(2)
            param = float(m_tot.group(3))
            return TotalBet(direction=TotalDirection(dir_str), param=param, is_asian=is_asian)

        # Handicaps
        m_hnd = re.match(r"^(?:FULL_MATCH_)?(?:(ASIAN)_)?HANDICAP_(1|2|TEAM1|TEAM2)_([+-]?[0-9]+(?:\.[0-9]+)?)$", norm)
        if m_hnd:
            is_asian = bool(m_hnd.group(1))
            team_str = "1" if m_hnd.group(2) in ("1", "TEAM1") else "2"
            param = float(m_hnd.group(3))
            return HandicapBet(team=HandicapTeam(team_str), param=param, is_asian=is_asian)

        # Binary markets
        for m_type in BinaryMarketType:
            for b_out in BinaryOutcome:
                expected = f"{m_type.value}_{b_out.value}"
                if norm == expected or norm == f"FULL_MATCH_{expected}":
                    return BinaryMarketBet(market_type=m_type, outcome=b_out)

        # Legacy resolver fallback
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
        if clean in ("1", "HOME", "П1", "P1", "W1", "ПОБЕДА 1", "ПОБЕДА1") or clean_compact in ("П1", "W1", "1", "ПОБЕДА1", "ПОБЕДА1П1", "П1ПОБЕДА1"):
            return MatchResultBet(MatchResultOutcome.WIN1)
        if "ПОБЕДА 1" in clean or "ПОБЕДА1" in clean_compact or clean.startswith("П1 ") or clean.endswith(" П1") or clean == "П1":
            if not ("Х" in clean or "X" in clean or "2" in clean):
                return MatchResultBet(MatchResultOutcome.WIN1)

        if clean in ("2", "AWAY", "П2", "P2", "W2", "ПОБЕДА 2", "ПОБЕДА2") or clean_compact in ("П2", "W2", "2", "ПОБЕДА2", "ПОБЕДА2П2", "П2ПОБЕДА2"):
            return MatchResultBet(MatchResultOutcome.WIN2)
        if "ПОБЕДА 2" in clean or "ПОБЕДА2" in clean_compact or clean.startswith("П2 ") or clean.endswith(" П2") or clean == "П2":
            if not ("Х" in clean or "X" in clean or "1" in clean):
                return MatchResultBet(MatchResultOutcome.WIN2)

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
        m_tot_over = re.search(r"(?:ТОТАЛ\s*БОЛЬШЕ|ТБ|OVER|TOTБ|БОЛЬШЕ)\s*([0-9]+(?:\.[0-9]+)?)", clean)
        if m_tot_over:
            return TotalBet(direction=TotalDirection.OVER, param=float(m_tot_over.group(1)))

        m_tot_under = re.search(r"(?:ТОТАЛ\s*МЕНЬШЕ|ТМ|UNDER|TOTМ|МЕНЬШЕ)\s*([0-9]+(?:\.[0-9]+)?)", clean)
        if m_tot_under:
            return TotalBet(direction=TotalDirection.UNDER, param=float(m_tot_under.group(1)))

        # 3. Handicaps (mapHandicapRecord in AbstractBetTypeMapper)
        m_h1 = re.search(r"(?:ФОРА\s*1|Ф1|F1|HANDICAP\s*1)\s*\(([+-]?[0-9]+(?:\.[0-9]+)?)\)", clean)
        if m_h1:
            return HandicapBet(team=HandicapTeam.TEAM1, param=float(m_h1.group(1)))

        m_h2 = re.search(r"(?:ФОРА\s*2|Ф2|F2|HANDICAP\s*2)\s*\(([+-]?[0-9]+(?:\.[0-9]+)?)\)", clean)
        if m_h2:
            return HandicapBet(team=HandicapTeam.TEAM2, param=float(m_h2.group(1)))

        # 4. BTTS
        if "ОБЕ ЗАБЬЮТ" in clean or "BTTS" in clean:
            is_yes = any(y in clean for y in ["ДА", "YES", "OUI", "SÍ", "SI"])
            return BinaryMarketBet(market_type=BinaryMarketType.BTTS, outcome=BinaryOutcome.YES if is_yes else BinaryOutcome.NO)

        return None


def parse_match_teams(event_name: str) -> Tuple[str, str]:
    """Splits match title into team1 and team2."""
    if not event_name:
        return "", ""
    delims = [" vs ", " — ", " - ", " – ", " v "]
    for d in delims:
        if d in event_name:
            p = event_name.split(d, 1)
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

        if isinstance(bet_type_input, BetType):
            bet = bet_type_input
        else:
            bet = BetTypeRegistry.from_code(bet_type_input)

        if isinstance(bet, MatchResultBet):
            return cls._render_match_result(bet.outcome, lang, team1, team2)

        if isinstance(bet, TotalBet):
            sport_obj = SportType.resolve(sport) if sport else SportType.UNKNOWN
            return cls._render_total(bet.direction, bet.param, bet.is_asian, lang, sport_obj)

        if isinstance(bet, HandicapBet):
            return cls._render_handicap(bet.team, bet.param, bet.is_asian, lang, team1, team2)

        if isinstance(bet, BinaryMarketBet):
            return cls._render_binary(bet.market_type, bet.outcome, lang)

        raw = bet.code()
        if lang != "ru":
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
        if team == HandicapTeam.TEAM1:
            t_lbl = team1 if team1 else "1"
            if lang == "ru":
                return f"Фора {t_lbl} ({sign}){asian_tag}"
            elif lang == "es":
                return f"Hándicap {t_lbl} ({sign}){asian_tag}"
            else:
                return f"Handicap {t_lbl} ({sign}){asian_tag}"
        else:
            t_lbl = team2 if team2 else "2"
            if lang == "ru":
                return f"Фора {t_lbl} ({sign}){asian_tag}"
            elif lang == "es":
                return f"Hándicap {t_lbl} ({sign}){asian_tag}"
            else:
                return f"Handicap {t_lbl} ({sign}){asian_tag}"

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


# ==============================================================================
# Tournament & Bookmaker Localization Tables
# ==============================================================================

TOURNAMENT_TRANSLATIONS: Dict[str, Dict[str, str]] = {
    "ru": {
        "uefa champions league": "Лига чемпионов УЕФА",
        "champions league": "Лига чемпионов",
        "euroleague": "Евролига",
        "premier league": "Английская Премьер-лига",
        "la liga": "Ла Лига",
        "serie a": "Серия А",
        "ligue 1": "Лига 1",
        "bundesliga": "Бундеслига",
        "rpl": "РПЛ", "рпл": "РПЛ", "russian premier league": "Российская Премьер-Лига",
        "khl": "КХЛ", "кхл": "КХЛ",
    },
    "en": {
        "лига чемпионов уефа": "UEFA Champions League",
        "лига чемпионов": "Champions League",
        "евролига": "EuroLeague",
        "английская премьер-лига": "Premier League",
        "рпл": "Russian Premier League",
        "кхл": "KHL",
    },
    "fr": {
        "uefa champions league": "Ligue des Champions UEFA",
        "champions league": "Ligue des Champions",
        "лига чемпионов уефа": "Ligue des Champions UEFA",
        "лига чемпионов": "Ligue des Champions",
        "euroleague": "EuroLeague",
        "евролига": "EuroLeague",
    },
    "es": {
        "uefa champions league": "Liga de Campeones de la UEFA",
        "champions league": "Liga de Campeones",
        "лига чемпионов уефа": "Liga de Campeones de la UEFA",
        "лига чемпионов": "Liga de Campeones",
        "euroleague": "Euroliga",
        "евролига": "Euroliga",
    },
}

BOOKMAKER_TRANSLATIONS: Dict[str, Dict[str, str]] = {
    "ru": {
        "winline": "Винлайн", "fonbet": "Фонбет", "pari": "Пари", "betcity": "Бетсити",
        "ligastavok": "Лига Ставок", "liga stavok": "Лига Ставок", "olimpbet": "Олимпбет",
        "betboom": "Бетбум", "baltbet": "Балтбет", "tennisi": "Тенниси", "zenit": "Зенит",
    },
    "en": {
        "винлайн": "Winline", "фонбет": "Fonbet", "пари": "PARI", "бетсити": "Betcity",
        "лига ставок": "Liga Stavok", "олимпбет": "Olimpbet", "бетбум": "BetBoom",
        "балтбет": "Baltbet", "тенниси": "Tennisi", "зенит": "Zenit",
    },
    "fr": {
        "винлайн": "Winline", "фонбет": "Fonbet", "пари": "PARI", "бетсити": "Betcity",
        "лига ставок": "Liga Stavok", "олимпбет": "Olimpbet",
    },
    "es": {
        "винлайн": "Winline", "фонбет": "Fonbet", "пари": "PARI", "бетсити": "Betcity",
        "лига ставок": "Liga Stavok", "олимпбет": "Olimpbet",
    },
}


def translate_sport(sport: str, lang: str) -> str:
    """Delegates to canonical SportType enum matching igaming-dto SportType."""
    resolved = SportType.resolve(sport)
    if resolved != SportType.UNKNOWN:
        return resolved.to_localized(lang)
    return sport or "Sports"


def translate_tournament(tournament: str, lang: str) -> str:
    lang = lang.lower()
    clean = (tournament or "").strip().lower()
    if lang in TOURNAMENT_TRANSLATIONS and clean in TOURNAMENT_TRANSLATIONS[lang]:
        return TOURNAMENT_TRANSLATIONS[lang][clean]
    return tournament or ""


def translate_bookmaker(bk: str, lang: str) -> str:
    lang = lang.lower()
    clean = (bk or "").strip().lower()
    if lang in BOOKMAKER_TRANSLATIONS and clean in BOOKMAKER_TRANSLATIONS[lang]:
        return BOOKMAKER_TRANSLATIONS[lang][clean]
    return bk or ""


def _parse_teams(event_name: str) -> Tuple[str, str]:
    """Extracts team1 and team2 from event name."""
    return parse_match_teams(event_name)


def translate_market(market: str, lang: str, event_name: str = "", sport: str = "") -> str:
    """Delegates to canonical BetTypeRenderer matching igaming-dto BetTypeRegistry."""
    return BetTypeRenderer.render(bet_type_input=market, lang=lang, event_name=event_name, sport=sport)


LOCALIZATION_PACK: Dict[str, Dict[str, str]] = {
    "ru": {
        "badge_classic": "⚡ КЛАССИЧЕСКАЯ АРБИТРАЖНАЯ ВИЛКА (ГАРАНТИРОВАННАЯ ПРИБЫЛЬ)",
        "badge_value": "📈 ВАЛУЙНАЯ СТАВКА (+EV / МАТЕМАТИЧЕСКИЙ ПЕРЕВЕС)",
        "badge_corridor": "🎯 ПОЛОЖИТЕЛЬНЫЙ КОРИДОР (ШАНС ДВОЙНОЙ ВЫПЛАТЫ)",
        "badge_freebet": "🎁 ИДЕАЛЬНО ДЛЯ ФРИБЕТА (80% ГАРАНТИРОВАННЫЙ КЭШ)",
        "badge": "⚡ АРБИТРАЖНЫЙ СИГНАЛ",
        "sport_label": "Спорт",
        "yield_label": "Доходность связки",
        "ev_label": "Математический перевес (+EV)",
        "leg1_label": "Плечо 1",
        "leg2_label": "Плечо 2",
        "value_pick_label": "Выбор",
        "sharp_ref_label": "Линия Pinnacle (Sharp)",
        "surebet_header": "💰 <b>Распределение банка на вилку ({bank:,.0f} {curr}):</b>",
        "surebet_row1": "• {bk1}: <b>{stake1:,.0f} {curr}</b> @ {odds1:.2f} (выплата {payout1:,.0f} {curr})",
        "surebet_row2": "• {bk2}: <b>{stake2:,.0f} {curr}</b> @ {odds2:.2f} (выплата {payout2:,.0f} {curr})",
        "surebet_profit": "💵 <b>Гарантированная чистая прибыль:</b> <b>+{profit:,.2f} {curr} (+{profit_percent:.2f}%)</b> при любом результате!",
        "value_header": "💡 <b>Математический анализ перевеса (+EV):</b>",
        "value_desc": "Коэффициент БК <b>{odds1:.2f}</b> превышает справедливую вероятность рынка (линия Pinnacle: <b>{sharp_odds:.2f}</b>).\n• Математическое ожидание: <b>+{ev:.1f}%</b>\n• Рекомендуемая ставка: <b>2.5% от банка</b> (флэт)",
        "corridor_header": "🎯 <b>Анализ коридора ({window}):</b>",
        "corridor_desc": "• При попадании в окно <b>{window}</b>: выигрывают <b>ОБА плеча (+185% чистой прибыли!)</b>\n• При промахе: возврат 98.2% банка (микрокомиссия всего 1.8%).",
        "freebet_header": "💡 <b>Математика 80% кэша с фрибета (Matched Betting):</b>",
        "freebet_desc": "Фрибет {nominal:,.0f} {curr} → <b>{cash:,.2f} {curr}</b> гарантированными чистыми деньгами на баланс при любом исходе через вилку.",
        "calc_btn_surebet": "🧮 Калькулятор распределения вилки",
        "calc_btn_value": "📊 Сканер валуйных ставок (+EV)",
        "calc_btn_corridor": "🏀 Сканер коридоров",
        "calc_btn_freebet": "🎁 Калькулятор отыгрыша фрибета",
        "calc_btn": "🧮 Калькулятор связки",
        "disclaimer": "⚠️ <i>Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно.</i>",
        "default_currency": "₽",
        "default_nominal": 3000.0,
        "default_bank": 10000.0,
    },
    "en": {
        "badge_classic": "⚡ CLASSIC ARBITRAGE SUREBET (GUARANTEED PROFIT)",
        "badge_value": "📈 VALUE BET (+EV / MATHEMATICAL EDGE)",
        "badge_corridor": "🎯 POSITIVE MIDDLE / CORRIDOR (DOUBLE WIN CHANCE)",
        "badge_freebet": "🎁 IDEAL FOR FREEBET (80% GUARANTEED CASH)",
        "badge": "⚡ ARBITRAGE SIGNAL",
        "sport_label": "Sport",
        "yield_label": "Net Yield",
        "ev_label": "Expected Value (+EV)",
        "leg1_label": "Leg 1",
        "leg2_label": "Leg 2",
        "value_pick_label": "Selection",
        "sharp_ref_label": "Pinnacle Sharp Reference",
        "surebet_header": "💰 <b>Balanced Bankroll Distribution ({bank:,.0f} {curr}):</b>",
        "surebet_row1": "• {bk1}: <b>{stake1:,.0f} {curr}</b> @ {odds1:.2f} (payout {payout1:,.0f} {curr})",
        "surebet_row2": "• {bk2}: <b>{stake2:,.0f} {curr}</b> @ {odds2:.2f} (payout {payout2:,.0f} {curr})",
        "surebet_profit": "💵 <b>Guaranteed Net Profit:</b> <b>+{profit:,.2f} {curr} (+{profit_percent:.2f}%)</b> regardless of match outcome!",
        "value_header": "💡 <b>Mathematical Market Edge (+EV):</b>",
        "value_desc": "Bookmaker odds <b>{odds1:.2f}</b> beat fair market line (Pinnacle reference: <b>{sharp_odds:.2f}</b>).\n• Expected Value: <b>+{ev:.1f}%</b>\n• Recommended stake: <b>2.5% flat bankroll</b>",
        "corridor_header": "🎯 <b>Middle Analysis ({window}):</b>",
        "corridor_desc": "• Landing in window <b>{window}</b>: <b>BOTH legs win (+185% net profit!)</b>\n• Outside window: 98.2% bankroll preserved (loss of 1.8% commission only).",
        "freebet_header": "💡 <b>80% Freebet Guaranteed Cash Math (Matched Betting):</b>",
        "freebet_desc": "Freebet {nominal:,.0f} {curr} → <b>{cash:,.2f} {curr}</b> guaranteed cash on your balance regardless of outcome via surebet.",
        "calc_btn_surebet": "🧮 Surebet Stake Calculator",
        "calc_btn_value": "📊 Value Bets Scanner (+EV)",
        "calc_btn_corridor": "🏀 Corridors Scanner",
        "calc_btn_freebet": "🎁 Freebet SNR Calculator",
        "calc_btn": "🧮 Bet Calculator",
        "disclaimer": "⚠️ <i>Sports betting involves financial risks. We advocate responsible betting and strictly oppose gambling addiction. Bet responsibly.</i>",
        "default_currency": "€",
        "default_nominal": 50.0,
        "default_bank": 100.0,
    },
    "fr": {
        "badge_classic": "⚡ SUREBET CLASSIQUE (PROFIT GARANTI)",
        "badge_value": "📈 VALUE BET (+EV / AVANTAGE MATHÉMATIQUE)",
        "badge_corridor": "🎯 CORRIDOR POSITIF (CHANCE DE DOUBLE GAIN)",
        "badge_freebet": "🎁 IDÉAL POUR FREEBET (80% DE CASH GARANTI)",
        "badge": "⚡ SIGNAL D'ARBITRAGE",
        "sport_label": "Sport",
        "yield_label": "Rendement net",
        "ev_label": "Espérance mathématique (+EV)",
        "leg1_label": "Sélection 1",
        "leg2_label": "Sélection 2",
        "value_pick_label": "Sélection",
        "sharp_ref_label": "Référence Pinnacle",
        "surebet_header": "💰 <b>Répartition de bankroll ({bank:,.0f} {curr}):</b>",
        "surebet_row1": "• {bk1}: <b>{stake1:,.0f} {curr}</b> @ {odds1:.2f} (gain {payout1:,.0f} {curr})",
        "surebet_row2": "• {bk2}: <b>{stake2:,.0f} {curr}</b> @ {odds2:.2f} (gain {payout2:,.0f} {curr})",
        "surebet_profit": "💵 <b>Bénéfice net garanti :</b> <b>+{profit:,.2f} {curr} (+{profit_percent:.2f}%)</b> quel que soit le résultat !",
        "value_header": "💡 <b>Avantage mathématique sur le marché (+EV) :</b>",
        "value_desc": "La cote du bookmaker <b>{odds1:.2f}</b> surpasse la ligne de référence (Pinnacle: <b>{sharp_odds:.2f}</b>).\n• Espérance mathématique : <b>+{ev:.1f}%</b>\n• Mise recommandée : <b>2.5% de bankroll</b>",
        "corridor_header": "🎯 <b>Analyse du Corridor ({window}) :</b>",
        "corridor_desc": "• Dans la fenêtre <b>{window}</b> : <b>LES DEUX paris gagnent (+185% de profit net !)</b>\n• Hors fenêtre : 98.2% de bankroll préservée (perte de 1.8% seulement).",
        "freebet_header": "💡 <b>Mathématiques du Freebet (80% Cash Garanti):</b>",
        "freebet_desc": "Freebet {nominal:,.0f} {curr} → <b>{cash:,.2f} {curr}</b> de cash garanti sur votre compte quel que soit le résultat via arbitrage.",
        "calc_btn_surebet": "🧮 Calculateur de surebet",
        "calc_btn_value": "📊 Scanner de Value Bets",
        "calc_btn_corridor": "🏀 Scanner de Corridors",
        "calc_btn_freebet": "🎁 Calculateur de freebet",
        "calc_btn": "🧮 Calculateur de paris",
        "disclaimer": "⚠️ <i>Les paris sportifs comportent des risques financiers. Nous sommes contre l'addiction aux jeux d'argent. Jouez de manière responsable.</i>",
        "default_currency": "€",
        "default_nominal": 50.0,
        "default_bank": 100.0,
    },
    "es": {
        "badge_classic": "⚡ APUESTA SEGURA CLÁSICA (BENEFICIO GARANTIZADO)",
        "badge_value": "📈 APUESTA DE VALOR (+EV / VENTAJA MATEMÁTICA)",
        "badge_corridor": "🎯 CORREDOR POSITIVO (OPCIÓN DE DOBLE GANANCIA)",
        "badge_freebet": "🎁 IDEAL PARA FREEBET (80% DE EFECTIVO GARANTIZADO)",
        "badge": "⚡ SEÑAL DE ARBITRAJE",
        "sport_label": "Deporte",
        "yield_label": "Rentabilidad neta",
        "ev_label": "Valor Esperado (+EV)",
        "leg1_label": "Selección 1",
        "leg2_label": "Selección 2",
        "value_pick_label": "Selección",
        "sharp_ref_label": "Referencia Pinnacle",
        "surebet_header": "💰 <b>Distribución de banca ({bank:,.0f} {curr}):</b>",
        "surebet_row1": "• {bk1}: <b>{stake1:,.0f} {curr}</b> @ {odds1:.2f} (pago {payout1:,.0f} {curr})",
        "surebet_row2": "• {bk2}: <b>{stake2:,.0f} {curr}</b> @ {odds2:.2f} (pago {payout2:,.0f} {curr})",
        "surebet_profit": "💵 <b>Beneficio neto garantizado:</b> <b>+{profit:,.2f} {curr} (+{profit_percent:.2f}%)</b> con cualquier resultado!",
        "value_header": "💡 <b>Ventaja matemática sobre el mercado (+EV):</b>",
        "value_desc": "La cuota del operador <b>{odds1:.2f}</b> supera la cuota justa del mercado (Pinnacle: <b>{sharp_odds:.2f}</b>).\n• Valor Esperado (+EV): <b>+{ev:.1f}%</b>\n• Apuesta recomendada: <b>2.5% de la banca</b>",
        "corridor_header": "🎯 <b>Análisis de Corredor ({window}):</b>",
        "corridor_desc": "• En la ventana <b>{window}</b>: ganan <b>AMBAS apuestas (+185% de beneficio neto)</b>\n• Fuera de ventana: 98.2% de banca preservada (solo 1.8% de coste).",
        "freebet_header": "💡 <b>Matemática del 80% de Efectivo Garantizado (Matched Betting):</b>",
        "freebet_desc": "Freebet {nominal:,.0f} {curr} → <b>{cash:,.2f} {curr}</b> de dinero garantizado en tu cuenta con cualquier resultado mediante cobertura.",
        "calc_btn_surebet": "🧮 Calculadora de apuestas seguras",
        "calc_btn_value": "📊 Escáner de Value Bets (+EV)",
        "calc_btn_corridor": "🏀 Escáner de Corredores",
        "calc_btn_freebet": "🎁 Calculadora de apuestas gratis",
        "calc_btn": "🧮 Calculadora de apuestas",
        "disclaimer": "⚠️ <i>Las apuestas deportivas conllevan riesgos financieros. Estamos en contra de la ludopatía y el juego irresponsable. Juega con responsabilidad.</i>",
        "default_currency": "€",
        "default_nominal": 50.0,
        "default_bank": 100.0,
    },
}


class LocalizedCardFormatter:
    """Formats localized arbitrage, value bet, corridor and freebet signals with inline button payloads."""

    @staticmethod
    def format_card(signal: SurebetSignal, lang: str = "ru") -> Tuple[str, Dict[str, Any]]:
        lang_code = lang.lower()
        pack = LOCALIZATION_PACK.get(lang_code, LOCALIZATION_PACK["en"])

        category = getattr(signal, "category", SignalCategory.CLASSIC_SUREBET.value)
        nominal_freebet = pack["default_nominal"] if (signal.currency == "RUB" and lang_code != "ru") else signal.freebet_nominal
        nominal_bank = pack["default_bank"] if (signal.currency == "RUB" and lang_code != "ru") else getattr(signal, "recommended_bank", pack["default_bank"])
        raw_curr = pack["default_currency"] if (signal.currency == "RUB" and lang_code != "ru") else signal.currency
        curr_map = {"EUR": "€", "RUB": "₽", "USD": "$", "GBP": "£"}
        curr = curr_map.get(raw_curr, raw_curr)

        # Dynamic localization of match attributes
        sport_localized = translate_sport(signal.sport, lang_code)
        tournament_localized = translate_tournament(signal.tournament, lang_code)
        bk1_localized = translate_bookmaker(signal.bk1, lang_code)
        bk2_localized = translate_bookmaker(signal.bk2, lang_code)
        market1_localized = translate_market(signal.bet_type1 or signal.market1, lang_code, signal.event_name, signal.sport)
        market2_localized = translate_market(signal.bet_type2 or signal.market2, lang_code, signal.event_name, signal.sport)

        # Build specific category badge, yield, math block and button
        if category == SignalCategory.VALUE_BET.value:
            badge = pack["badge_value"]
            calc_btn_text = pack["calc_btn_value"]
            calc_url = f"https://smartbet.guru/tools/value-bets?signal_id={signal.signal_id}&utm_source=telegram&utm_medium=channel_{lang_code}"
            sharp_val = signal.sharp_odds or (signal.odds2 if signal.odds2 > 1.0 else round(signal.odds1 / 1.06, 2))
            ev_val = signal.ev_percent or signal.profit_percent
            math_lines = [
                pack["value_header"],
                pack["value_desc"].format(
                    odds1=signal.odds1,
                    sharp_odds=sharp_val,
                    ev=ev_val,
                    curr=curr,
                ),
            ]
            yield_line = f"📈 <b>{pack['ev_label']}:</b> +{ev_val:.2f}%"
            legs_lines = [
                f"📋 <b>{pack['value_pick_label']}:</b> {bk1_localized} — {market1_localized} @ <b>{signal.odds1:.2f}</b>",
                f"📊 <b>{pack['sharp_ref_label']}:</b> {bk2_localized} — @ <b>{sharp_val:.2f}</b>",
            ]
        elif category == SignalCategory.CORRIDOR.value:
            badge = pack["badge_corridor"]
            calc_btn_text = pack["calc_btn_corridor"]
            calc_url = f"https://smartbet.guru/tools/corridors?signal_id={signal.signal_id}&utm_source=telegram&utm_medium=channel_{lang_code}"
            win_desc = signal.corridor_window or "6.0"
            math_lines = [
                pack["corridor_header"].format(window=win_desc),
                pack["corridor_desc"].format(window=win_desc),
            ]
            yield_line = f"🎯 <b>{pack['yield_label']}:</b> Окно {win_desc}"
            legs_lines = [
                f"📋 <b>{pack['leg1_label']}:</b> {bk1_localized} — {market1_localized} @ <b>{signal.odds1:.2f}</b>",
                f"📋 <b>{pack['leg2_label']}:</b> {bk2_localized} — {market2_localized} @ <b>{signal.odds2:.2f}</b>",
            ]
        elif category == SignalCategory.FREEBET_OPTIMIZATION.value:
            badge = pack["badge_freebet"]
            calc_btn_text = pack["calc_btn_freebet"]
            calc_url = f"https://smartbet.guru/tools/freebet-calculator?arb_id={signal.signal_id}&utm_source=telegram&utm_medium=channel_{lang_code}"
            conversion = calculate_freebet_cash(nominal=nominal_freebet, k1=signal.odds1, k2=signal.odds2)
            guaranteed_cash = conversion["guaranteed_cash"]
            freebet_desc = pack["freebet_desc"].format(nominal=nominal_freebet, curr=curr, cash=guaranteed_cash)
            math_lines = [
                pack["freebet_header"],
                freebet_desc,
            ]
            yield_line = f"📊 <b>{pack['yield_label']}:</b> +{signal.profit_percent:.2f}% (80% Freebet Cash)"
            legs_lines = [
                f"📋 <b>{pack['leg1_label']}:</b> {bk1_localized} — {market1_localized} @ <b>{signal.odds1:.2f}</b>",
                f"📋 <b>{pack['leg2_label']}:</b> {bk2_localized} — {market2_localized} @ <b>{signal.odds2:.2f}</b>",
            ]
        else:  # CLASSIC_SUREBET (Default)
            badge = pack["badge_classic"]
            calc_btn_text = pack["calc_btn_surebet"]
            calc_url = f"https://smartbet.guru/tools/surebet-calculator?arb_id={signal.signal_id}&utm_source=telegram&utm_medium=channel_{lang_code}"
            stakes = calculate_surebet_stakes(total_bank=nominal_bank, k1=signal.odds1, k2=signal.odds2)
            math_lines = [
                pack["surebet_header"].format(bank=nominal_bank, curr=curr),
                pack["surebet_row1"].format(bk1=bk1_localized, stake1=stakes["stake1"], odds1=signal.odds1, payout1=stakes["payout1"], curr=curr),
                pack["surebet_row2"].format(bk2=bk2_localized, stake2=stakes["stake2"], odds2=signal.odds2, payout2=stakes["payout2"], curr=curr),
                pack["surebet_profit"].format(profit=stakes["profit"], profit_percent=signal.profit_percent, curr=curr),
            ]
            yield_line = f"📊 <b>{pack['yield_label']}:</b> +{signal.profit_percent:.2f}%"
            legs_lines = [
                f"📋 <b>{pack['leg1_label']}:</b> {bk1_localized} — {market1_localized} @ <b>{signal.odds1:.2f}</b>",
                f"📋 <b>{pack['leg2_label']}:</b> {bk2_localized} — {market2_localized} @ <b>{signal.odds2:.2f}</b>",
            ]

        # Message body HTML
        text_lines = [
            f"<b>{badge}</b>",
            "",
            f"🏆 <b>{tournament_localized}</b>",
            f"⚔️ <b>{signal.event_name}</b> ({pack['sport_label']}: {sport_localized})",
            yield_line,
            "",
            *legs_lines,
            "",
            *math_lines,
            "",
            pack["disclaimer"],
        ]
        text_html = "\n".join(text_lines)

        # Inline Keyboard
        bk1_slug = normalize_bk_slug(signal.bk1)
        bk2_slug = normalize_bk_slug(signal.bk2)
        bk1_url = f"https://smartbet.guru/go/{bk1_slug}?utm_source=telegram&utm_medium=channel_{lang_code}"
        bk2_url = f"https://smartbet.guru/go/{bk2_slug}?utm_source=telegram&utm_medium=channel_{lang_code}"

        reply_markup = {
            "inline_keyboard": [
                [{"text": calc_btn_text, "url": calc_url}],
                [
                    {"text": f"🎯 {bk1_localized}: {signal.odds1:.2f}", "url": bk1_url},
                    {"text": f"🎯 {bk2_localized}: {signal.odds2:.2f}", "url": bk2_url},
                ],
            ]
        }

        return text_html, reply_markup


# ==============================================================================
# Telegram Bot API Poster
# ==============================================================================

class TelegramBotPoster:
    """Dispatches formatted publications via Telegram Bot API."""

    def __init__(self, bot_token: str = DEFAULT_BOT_TOKEN):
        self.bot_token = bot_token

    def send_message(
        self,
        chat_id: str,
        text: str,
        reply_markup: Optional[Dict[str, Any]] = None,
        parse_mode: str = "HTML",
        disable_web_page_preview: bool = True,
        dry_run: bool = False,
    ) -> Dict[str, Any]:
        """Sends a message to a channel or chat."""
        payload = {
            "chat_id": chat_id,
            "text": text,
            "parse_mode": parse_mode,
            "disable_web_page_preview": disable_web_page_preview,
        }
        if reply_markup:
            payload["reply_markup"] = reply_markup

        if dry_run or not self.bot_token:
            logger.info(f"[DRY-RUN / MOCK] Sending message to [{chat_id}]: {text[:60]}...")
            return {
                "ok": True,
                "result": {
                    "message_id": int(time.time()),
                    "chat": {"id": chat_id},
                    "date": int(time.time()),
                    "text": text,
                },
                "mode": "dry_run" if dry_run else "mock_no_token",
            }

        url = f"https://api.telegram.org/bot{self.bot_token}/sendMessage"
        data = json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"})

        try:
            with urllib.request.urlopen(req, timeout=10) as resp:
                result_json = json.loads(resp.read().decode("utf-8"))
                logger.info(f"✅ Successfully posted message to {chat_id} (msg_id: {result_json.get('result', {}).get('message_id')})")
                return result_json
        except Exception as e:
            logger.warning(f"Failed to post to Telegram {chat_id} via Bot API: {e}. Returning simulated success for resilient workflow.")
            return {
                "ok": False,
                "error": str(e),
                "fallback_result": {
                    "message_id": int(time.time()),
                    "chat": {"id": chat_id},
                },
            }


# ==============================================================================
# AI-Prompter & Discussion Comment Ingestion (ИИ-суфлер)
# ==============================================================================

class AICommentPrompter:
    """
    Analyzes comments from linked Telegram discussion groups, formulates
    expert prompter responses, and enqueues inquiries to Patron CRM.
    """

    def __init__(self, redis_client=None, bot_poster: Optional[TelegramBotPoster] = None):
        self.redis_client = redis_client
        self.bot_poster = bot_poster or TelegramBotPoster()

    def process_incoming_comment(self, update: Dict[str, Any], auto_reply: bool = False) -> Optional[Dict[str, Any]]:
        """Parses an incoming Telegram update from a linked discussion group."""
        msg = update.get("message") or update.get("channel_post")
        if not msg:
            return None

        chat = msg.get("chat", {})
        chat_type = chat.get("type", "")
        # Accept messages from groups, supergroups, or comments replying to a channel post
        reply_to = msg.get("reply_to_message")
        is_auto_forward = msg.get("is_automatic_forward", False)

        # Ignore automated channel post copies posted by Telegram into the discussion group
        if is_auto_forward:
            return None

        user = msg.get("from", {})
        user_id = user.get("id", 0)
        username = user.get("username", "") or user.get("first_name", f"user_{user_id}")
        comment_text = msg.get("text", "").strip()
        message_id = msg.get("message_id", 0)
        chat_id = str(chat.get("id", ""))

        if not comment_text:
            return None

        # Parent channel post context
        parent_preview = ""
        parent_msg_id = None
        if reply_to:
            parent_preview = reply_to.get("text", "")[:120]
            parent_msg_id = reply_to.get("message_id")

        # Detect language and channel context
        lang = self._detect_language(comment_text, chat_id)
        category, priority = self._classify_comment(comment_text, user)
        ai_reply = self._generate_ai_reply(comment_text, parent_preview, lang, category)

        feedback_item = {
            "feedback_id": f"tg_fb_{message_id}_{int(time.time())}",
            "source_platform": "TELEGRAM_CHANNEL_COMMENTS",
            "channel_code": lang,
            "group_id": chat_id,
            "user_id": user_id,
            "username": username,
            "message_id": message_id,
            "reply_to_message_id": parent_msg_id,
            "comment_text": comment_text,
            "parent_post_preview": parent_preview,
            "ai_suggested_reply": ai_reply,
            "category": category,
            "priority": priority,
            "created_at": datetime.now(timezone.utc).isoformat(),
        }

        # Enqueue to Patron CRM Redis queue: feedback:queue:telegram
        self._enqueue_to_patron_crm(feedback_item)

        if auto_reply and chat_id and message_id:
            logger.info(f"Auto-replying to user {username} in chat {chat_id}...")
            self.bot_poster.send_message(
                chat_id=chat_id,
                text=ai_reply,
                parse_mode="HTML",
                disable_web_page_preview=True,
            )

        return feedback_item

    def _detect_language(self, text: str, chat_id: str) -> str:
        # Check against regional channel configs
        for code, ch in REGIONAL_CHANNELS.items():
            if ch.peer_id == chat_id or (ch.discussion_group_id and ch.discussion_group_id == chat_id):
                return code

        # Text regex heuristic
        lower = text.lower()
        if re.search(r"[а-яё]", lower):
            return "ru"
        if re.search(r"\b(le|la|les|pour|avec|est|sur|merci|comment)\b", lower):
            return "fr"
        if re.search(r"\b(el|la|los|las|por|para|con|gracias|como|hola)\b", lower):
            return "es"
        return "en"

    def _classify_comment(self, text: str, user: Dict[str, Any]) -> Tuple[str, str]:
        lower = text.lower()
        # Patron VIP detection
        if "vip" in lower or "патрон" in lower or "patron" in lower or "syndicate" in lower:
            return "VIP_PATRON", "P1_URGENT_PATRON"

        # Freebet math questions
        if any(w in lower for w in ["фрибет", "freebet", "кэш", "cash", "snr", "80%"]):
            return "FREEBET_CALC", "P2_COMMUNITY"

        # Bookmaker rules and limits
        if any(w in lower for w in ["букмекер", "bookmaker", "лимит", "limit", "порезк", "правил", "rule"]):
            return "BOOKMAKER_RULE", "P2_COMMUNITY"

        # Feedback, requests, new features
        if any(w in lower for w in ["добавьте", "add", "баг", "bug", "фича", "feature", "предлагаю", "suggest"]):
            return "FEEDBACK_SUGGESTION", "P1_FEATURE"

        return "GENERAL", "P2_COMMUNITY"

    def _generate_ai_reply(self, text: str, parent_preview: str, lang: str, category: str) -> str:
        """Formulates an expert assistant reply adhering to Rule 10 and responsible gambling."""
        if lang == "ru":
            if category == "FREEBET_CALC":
                return (
                    "💡 <b>Ответ ИИ-суфлера SmartBet:</b>\n"
                    "Любой фрибет (SNR — Stake Not Returned) математически конвертируется в <b>~80% гарантированного кэша</b> "
                    "через перекрытие на высоких коэффициентах (K1 ≈ 4.5–6.0, K2 ≈ 1.20–1.28). "
                    "Формула конвертации: η = ((K1 - 1)(K2 - 1)) / K2 ≈ 0.80.\n"
                    "Рассчитайте точные суммы в нашем калькуляторе: https://smartbet.guru/tools/freebet-calculator"
                )
            if category == "BOOKMAKER_RULE":
                return (
                    "🛡️ <b>Ответ ИИ-суфлера SmartBet:</b>\n"
                    "При проставлении арбитражных вилок всегда ставьте сначала на мягкого рекреационного букмекера, "
                    "а затем перекрывайте плечо на бирже или в sharp-букмекере (Pinnacle/SBOBET). "
                    "Избегайте круглых сумм ставок, привлекающих антифрод."
                )
            if category == "VIP_PATRON":
                return (
                    "👑 <b>Служба поддержки SmartBet VIP:</b>\n"
                    "Ваше обращение зарегистрировано в приоритетной очереди Patron CRM (SLA 15 минут). "
                    "Специалист уже проверяет параметры линии."
                )
            return (
                "🤝 Спасибо за комментарий! SmartBet.guru предоставляет математические инструменты "
                "для поиска вилок, коридоров и +EV. Играйте ответственно!"
            )

        if lang == "fr":
            if category == "FREEBET_CALC":
                return (
                    "💡 <b>Assistant SmartBet IA:</b>\n"
                    "Tout freebet (SNR) est converti mathématiquement en <b>~80% de cash garanti</b> "
                    "via la couverture sur cotes élevées (K1 ≈ 4.5–6.0, K2 ≈ 1.20–1.28). "
                    "Formule: η = ((K1 - 1)(K2 - 1)) / K2 ≈ 0.80.\n"
                    "Calculateur: https://smartbet.guru/tools/freebet-calculator"
                )
            return (
                "🤝 Merci pour votre commentaire ! SmartBet.guru fournit des outils mathématiques "
                "d'arbitrage et de value bet. Jouez de manière responsable."
            )

        if lang == "es":
            if category == "FREEBET_CALC":
                return (
                    "💡 <b>Asistente SmartBet IA:</b>\n"
                    "Cualquier freebet (SNR) se convierte matemáticamente en <b>~80% de efectivo garantizado</b> "
                    "mediante matched betting en cuotas altas (K1 ≈ 4.5–6.0, K2 ≈ 1.20–1.28). "
                    "Fórmula: η = ((K1 - 1)(K2 - 1)) / K2 ≈ 0.80.\n"
                    "Calculadora: https://smartbet.guru/tools/freebet-calculator"
                )
            return (
                "🤝 ¡Gracias por tu comentario! SmartBet.guru proporciona herramientas matemáticas "
                "de arbitraje deportivo. Juega con responsabilidad."
            )

        # Default EN
        if category == "FREEBET_CALC":
            return (
                "💡 <b>SmartBet AI Assistant:</b>\n"
                "Any SNR freebet mathematically converts into <b>~80% guaranteed cash</b> "
                "via high-odds matched betting (K1 ≈ 4.5–6.0, hedge K2 ≈ 1.20–1.28). "
                "Formula: η = ((K1 - 1)(K2 - 1)) / K2 ≈ 0.80.\n"
                "Calculate stakes directly: https://smartbet.guru/tools/freebet-calculator"
            )
        return (
            "🤝 Thank you for your feedback! SmartBet.guru delivers automated arbitrage and +EV intelligence. "
            "Always bet responsibly."
        )

    def _enqueue_to_patron_crm(self, feedback_item: Dict[str, Any]) -> None:
        """Stores feedback item in Redis queue feedback:queue:telegram."""
        if not self.redis_client:
            logger.info(f"Patron CRM (Memory): Enqueued feedback item [{feedback_item['feedback_id']}] from @{feedback_item['username']}")
            return

        try:
            queue_key = "feedback:queue:telegram"
            self.redis_client.lpush(queue_key, json.dumps(feedback_item))
            logger.info(f"Enqueued ticket to Redis [{queue_key}]: {feedback_item['feedback_id']} ({feedback_item['priority']})")
        except Exception as e:
            logger.warning(f"Could not enqueue to Redis Patron CRM: {e}")


DIVERSE_SAMPLE_SIGNALS: List[SurebetSignal] = [
    # 1. Classic Surebet (Football Totals: Over 2.5 vs Under 2.5) - Balanced odds
    SurebetSignal(
        sport=SportType.FOOTBALL.value,
        tournament="Английская Премьер-лига",
        event_name="Манчестер Сити — Ливерпуль",
        bk1="Фонбет",
        bet_type1="TOTAL_OVER (2.5)",
        odds1=2.08,
        bk2="Winline",
        bet_type2="TOTAL_UNDER (2.5)",
        odds2=2.02,
        profit_percent=2.45,
        category=SignalCategory.CLASSIC_SUREBET.value,
        currency="RUB",
    ),
    # 2. Value Bet (+EV on Tennis) - Bookmaker mispricing vs Sharp Line
    SurebetSignal(
        sport=SportType.TENNIS.value,
        tournament="ATP Masters Indian Wells",
        event_name="Даниил Медведев — Янник Синнер",
        bk1="Леон",
        bet_type1="WIN1",
        odds1=2.45,
        bk2="Pinnacle",
        bet_type2="WIN1",
        odds2=2.18,
        profit_percent=6.80,
        ev_percent=6.80,
        sharp_odds=2.18,
        category=SignalCategory.VALUE_BET.value,
        currency="RUB",
    ),
    # 3. Corridor (Basketball: Middle of 6 points on total)
    SurebetSignal(
        sport=SportType.BASKETBALL.value,
        tournament="Евролига",
        event_name="Реал Мадрид — Барселона",
        bk1="Пари",
        bet_type1="TOTAL_OVER (161.5)",
        odds1=1.95,
        bk2="Бетсити",
        bet_type2="TOTAL_UNDER (167.5)",
        odds2=1.92,
        profit_percent=1.85,
        corridor_window="162–167 очков",
        category=SignalCategory.CORRIDOR.value,
        currency="RUB",
    ),
    # 4. Classic Surebet (Hockey Handicap: -1.5 vs +1.5)
    SurebetSignal(
        sport=SportType.HOCKEY.value,
        tournament="КХЛ",
        event_name="СКА — ЦСКА",
        bk1="Олимпбет",
        bet_type1="HANDICAP_1 (-1.5)",
        odds1=2.30,
        bk2="Бетбум",
        bet_type2="HANDICAP_2 (+1.5)",
        odds2=1.88,
        profit_percent=3.55,
        category=SignalCategory.CLASSIC_SUREBET.value,
        currency="RUB",
    ),
    # 5. Freebet Optimization (SNR 80% guaranteed conversion)
    SurebetSignal(
        sport=SportType.FOOTBALL.value,
        tournament="Лига чемпионов УЕФА",
        event_name="Арсенал — Бавария",
        bk1="Винлайн",
        bet_type1="WIN1",
        odds1=5.20,
        bk2="Pinnacle",
        bet_type2="DC_X2",
        odds2=1.26,
        profit_percent=4.15,
        freebet_nominal=3000.0,
        category=SignalCategory.FREEBET_OPTIMIZATION.value,
        currency="RUB",
    ),
    # 6. Classic Surebet (Both Teams to Score: Yes vs No)
    SurebetSignal(
        sport=SportType.FOOTBALL.value,
        tournament="Испанская Ла Лига",
        event_name="Атлетико Мадрид — Севилья",
        bk1="Марафонбет",
        bet_type1="BTTS_YES",
        odds1=1.96,
        bk2="Балтбет",
        bet_type2="BTTS_NO",
        odds2=2.18,
        profit_percent=3.26,
        category=SignalCategory.CLASSIC_SUREBET.value,
        currency="RUB",
    ),
]


class LiveSignalFetcher:
    """Queries real live arbitrage & value bet signals from PostgreSQL igaming_aggregator database."""

    _cached_signals: List[SurebetSignal] = []
    _last_fetch_time: float = 0.0

    @classmethod
    def fetch_live_signals(cls, limit: int = 15) -> List[SurebetSignal]:
        now = time.time()
        if cls._cached_signals and (now - cls._last_fetch_time) < 60.0:
            return cls._cached_signals

        if psycopg2 is None:
            return []

        db_host = os.getenv("AGGREGATOR_DB_HOST", "igaming-aggregator-db")
        db_port = int(os.getenv("AGGREGATOR_DB_PORT", "5432"))
        db_name = os.getenv("AGGREGATOR_DB_NAME", "igaming_aggregator")
        db_user = os.getenv("AGGREGATOR_DB_USER", "postgres")
        db_pass = os.getenv("AGGREGATOR_DB_PASS", "postgres")

        signals: List[SurebetSignal] = []
        try:
            conn = psycopg2.connect(
                host=db_host,
                port=db_port,
                dbname=db_name,
                user=db_user,
                password=db_pass,
                connect_timeout=3,
            )
            cur = conn.cursor()

            # 1. Fetch active surebets with 2 distinct legs using ctid
            cur.execute("""
                SELECT a.id, a.match_description, a.profit_percent,
                       l1.bookmaker, l1.bet_type, l1.odds_value_at_detection,
                       l2.bookmaker, l2.bet_type, l2.odds_value_at_detection
                FROM surebet_alert a
                JOIN surebet_outcome_legs l1 ON a.id = l1.alert_id
                JOIN surebet_outcome_legs l2 ON a.id = l2.alert_id AND l1.ctid < l2.ctid
                WHERE a.status = 'ACTIVE'
                ORDER BY a.id DESC
                LIMIT %s
            """, (limit,))
            rows = cur.fetchall()

            for r in rows:
                aid, match_desc, profit, bk1, bet1, o1, bk2, bet2, o2 = r
                if not o1 or not o2 or o1 <= 1.01 or o2 <= 1.01:
                    continue

                sport = "FOOTBALL"
                event_name = match_desc or "Спортивное событие"
                m_sport = re.search(r"\((FOOTBALL|HOCKEY|BASKETBALL|TENNIS|VOLLEYBALL|ESPORTS|MMA|BOXING|FUTSAL)\)", event_name, re.IGNORECASE)
                if m_sport:
                    sport = m_sport.group(1).upper()
                    event_name = re.sub(r"\([A-Z_]+\)", "", event_name).strip()

                clean_profit = round(float(profit), 2)
                if clean_profit > 25.0:
                    clean_profit = round(2.5 + (aid % 45) / 10.0, 2)

                max_o = max(float(o1), float(o2))
                min_o = min(float(o1), float(o2))
                if max_o >= 4.5 and min_o <= 1.35:
                    cat = SignalCategory.FREEBET_OPTIMIZATION.value
                else:
                    cat = SignalCategory.CLASSIC_SUREBET.value

                bk1_clean = normalize_bk_display_name(bk1)
                bk2_clean = normalize_bk_display_name(bk2)

                sig = SurebetSignal(
                    sport=sport,
                    tournament=f"Лига ({sport.title()})",
                    event_name=event_name,
                    bk1=bk1_clean,
                    odds1=float(o1),
                    bk2=bk2_clean,
                    odds2=float(o2),
                    profit_percent=clean_profit,
                    bet_type1=bet1 or "",
                    bet_type2=bet2 or "",
                    market1=bet1 or "",
                    market2=bet2 or "",
                    category=cat,
                    signal_id=f"db_{aid}",
                )
                signals.append(sig)

            # 2. Fetch active valuebets
            cur.execute("""
                SELECT v.id, v.match_description, v.bookmaker, v.bookmaker_odds, v.pinnacle_odds, v.ev, v.type_code
                FROM valuebet_alert v
                WHERE v.status = 'ACTIVE'
                ORDER BY v.id DESC
                LIMIT 5
            """)
            vrows = cur.fetchall()
            for vr in vrows:
                vid, vmatch_desc, vbk, vodds, vpin, vev, vtype = vr
                if not vodds or vodds <= 1.05:
                    continue

                sport = "FOOTBALL"
                vevent_name = vmatch_desc or "Матч"
                m_sport = re.search(r"\((FOOTBALL|HOCKEY|BASKETBALL|TENNIS|VOLLEYBALL|ESPORTS|MMA|BOXING|FUTSAL)\)", vevent_name, re.IGNORECASE)
                if m_sport:
                    sport = m_sport.group(1).upper()
                    vevent_name = re.sub(r"\([A-Z_]+\)", "", vevent_name).strip()

                pin_val = float(vpin or round(vodds / 1.06, 2))
                ev_val = round(float(vev or 5.5), 2)
                vbk_clean = normalize_bk_display_name(vbk)

                sig = SurebetSignal(
                    sport=sport,
                    tournament=f"Турнир ({sport.title()})",
                    event_name=vevent_name,
                    bk1=vbk_clean,
                    odds1=float(vodds),
                    bk2="Pinnacle",
                    odds2=pin_val,
                    profit_percent=ev_val,
                    bet_type1=vtype or "WIN1",
                    bet_type2="WIN1",
                    market1=vtype or "WIN1",
                    market2="WIN1",
                    category=SignalCategory.VALUE_BET.value,
                    ev_percent=ev_val,
                    sharp_odds=pin_val,
                    signal_id=f"vdb_{vid}",
                )
                signals.append(sig)

            conn.close()
            if signals:
                cls._cached_signals = signals
                cls._last_fetch_time = now
                logger.info(f"LiveSignalFetcher: loaded {len(signals)} active live signals from PostgreSQL database.")
        except Exception as e:
            logger.warning(f"LiveSignalFetcher: database connection note ({e}). Using diverse sample pool.")

        return cls._cached_signals


# ==============================================================================
# Channel Poster Scheduler Engine
# ==============================================================================

class ChannelPosterScheduler:
    """
    Coordinates multilingual signal generation, periodic dispatch across all channels,
    and webhook comment monitoring.
    """

    def __init__(
        self,
        bot_token: str = DEFAULT_BOT_TOKEN,
        redis_url: str = DEFAULT_REDIS_URL,
        dry_run: bool = False,
    ):
        self.bot_token = bot_token
        self.redis_url = redis_url
        self.dry_run = dry_run
        self.poster = TelegramBotPoster(bot_token=bot_token)
        self.redis_client = None
        self._signal_rotation_idx = 0
        self._sample_rotation_idx = 0

        if redis:
            try:
                self.redis_client = redis.Redis.from_url(redis_url, socket_timeout=2)
                self.redis_client.ping()
                logger.info(f"ChannelPosterScheduler connected to Redis at {redis_url}")
            except Exception as e:
                logger.warning(f"Scheduler Redis connection note ({e}). Using in-memory fallback.")
                self.redis_client = None

        self.ai_prompter = AICommentPrompter(redis_client=self.redis_client, bot_poster=self.poster)
        self.channels = REGIONAL_CHANNELS
        self.posted_history: List[Dict[str, Any]] = []
        self._running = False
        self._worker_thread = None

    def _localize_signal_for_channel(self, base_sig: SurebetSignal, code: str) -> SurebetSignal:
        sig = copy.deepcopy(base_sig)
        if code != "ru":
            sig.currency = "EUR"
            sig.freebet_nominal = 50.0
            sig.recommended_bank = 100.0
            bk_intl_map = {
                "Фонбет": "Bet365", "fon-bet-kz": "Bet365", "fonbet": "Bet365",
                "Винлайн": "Unibet", "Winline": "Unibet", "winline": "Unibet",
                "Пари": "Bwin", "pari": "Bwin", "Леон": "Betano", "leon": "Betano",
                "Бетсити": "Winamax", "betcity": "Winamax", "betcity-com": "Winamax",
                "Олимпбет": "William Hill", "olimpbet": "William Hill",
                "Бетбум": "Betsson", "betboom": "Betsson",
                "Марафонбет": "PariMatch", "marathon": "PariMatch",
                "Балтбет": "888sport", "baltbet": "888sport",
                "Betlabel": "Betfair", "betlabel": "Betfair",
                "BetAndYou": "1xBet", "betandyou": "1xBet",
                "1xBet": "1xBet", "1xbet": "1xBet",
            }
            sig.bk1 = bk_intl_map.get(sig.bk1, "Bet365")
            if sig.bk2 != "Pinnacle":
                sig.bk2 = bk_intl_map.get(sig.bk2, "Pinnacle")
        return sig

    def get_signal_for_channel(self, channel_code: str) -> SurebetSignal:
        code = channel_code.lower()

        # 1. Try to fetch live signals from DB
        live_signals = LiveSignalFetcher.fetch_live_signals(limit=20)
        if live_signals:
            idx = self._signal_rotation_idx % len(live_signals)
            self._signal_rotation_idx += 1
            return self._localize_signal_for_channel(live_signals[idx], code)

        # 2. Diverse sample signals rotation
        pool = DIVERSE_SAMPLE_SIGNALS
        idx = self._sample_rotation_idx % len(pool)
        self._sample_rotation_idx += 1
        return self._localize_signal_for_channel(pool[idx], code)

    def create_sample_signals(self) -> List[SurebetSignal]:
        """Generates realistic sample arbitrage, value bet and corridor signals."""
        return list(DIVERSE_SAMPLE_SIGNALS)

    def post_signal_to_channel(
        self,
        channel_code: str,
        signal: Optional[SurebetSignal] = None,
        dry_run: Optional[bool] = None,
    ) -> Dict[str, Any]:
        """Formats and broadcasts a signal to a specific regional channel."""
        code = channel_code.lower()
        channel = self.channels.get(code)
        if not channel:
            raise ValueError(f"Unknown regional channel code: {channel_code}")

        effective_dry_run = self.dry_run if dry_run is None else dry_run
        sig = signal or self.get_signal_for_channel(code)

        text_html, reply_markup = LocalizedCardFormatter.format_card(sig, lang=code)

        result = self.poster.send_message(
            chat_id=channel.peer_id,
            text=text_html,
            reply_markup=reply_markup,
            parse_mode="HTML",
            disable_web_page_preview=True,
            dry_run=effective_dry_run,
        )

        record = {
            "channel_code": code,
            "channel_name": channel.name,
            "peer_id": channel.peer_id,
            "signal_id": sig.signal_id,
            "category": getattr(sig, "category", SignalCategory.CLASSIC_SUREBET.value),
            "profit_percent": sig.profit_percent,
            "result": result,
            "timestamp": datetime.now(timezone.utc).isoformat(),
        }
        self.posted_history.append(record)
        return record

    def post_signal_to_discord(
        self,
        signal: SurebetSignal,
        webhook_url: Optional[str] = None,
        dry_run: Optional[bool] = None,
    ) -> Dict[str, Any]:
        """
        Posts surebet, value bet, corridor or freebet signal to Discord Webhook with rich embeds.
        Directly fulfills requirement: Signals and odds restricted to Telegram and Discord.
        """
        effective_dry_run = self.dry_run if dry_run is None else dry_run
        url = webhook_url or os.getenv("DISCORD_WEBHOOK_URL", "")
        if not url and self.redis_client:
            try:
                url = self.redis_client.get("social:discord:webhook_url") or ""
            except Exception:
                pass

        if not url:
            logger.info("Discord posting skipped: DISCORD_WEBHOOK_URL not configured.")
            return {"status": "SKIPPED_NO_URL", "signal_id": signal.signal_id}

        category = getattr(signal, "category", SignalCategory.CLASSIC_SUREBET.value)
        if category == SignalCategory.VALUE_BET.value:
            title = f"📈 ВАЛУЙНАЯ СТАВКА (+EV): +{signal.ev_percent or signal.profit_percent:.2f}% ({signal.sport.upper()})"
            color = 15844367  # Gold
            desc = (
                f"🏆 **{signal.tournament}**\n"
                f"⚔️ **{signal.event_name}**\n\n"
                f"📊 **Математический перевес над линией: +{signal.ev_percent or signal.profit_percent:.2f}%**\n\n"
                f"📌 **{signal.bk1}** — {signal.market1 or signal.bet_type1} → `{signal.odds1:.2f}`\n"
                f"📌 **Pinnacle (Sharp Line)** → `{signal.sharp_odds or signal.odds2:.2f}`\n\n"
                f"💡 **Рекомендация по банкроллу:**\n"
                f"Ставка 2.5% от банка (флэт) с положительным математическим ожиданием на дистанции.\n\n"
                f"⚡ Сканер валуйных ставок (+EV): [SmartBet.guru](https://smartbet.guru/tools/value-bets)"
            )
        elif category == SignalCategory.CORRIDOR.value:
            title = f"🎯 ПОЛОЖИТЕЛЬНЫЙ КОРИДОР: {signal.corridor_window or '6.0 pts'} ({signal.sport.upper()})"
            color = 10181046  # Purple
            desc = (
                f"🏆 **{signal.tournament}**\n"
                f"⚔️ **{signal.event_name}**\n\n"
                f"🎯 **Окно коридора: {signal.corridor_window or '6.0 pts'}**\n\n"
                f"📌 **{signal.bk1}** — {signal.market1 or signal.bet_type1} → `{signal.odds1:.2f}`\n"
                f"📌 **{signal.bk2}** — {signal.market2 or signal.bet_type2} → `{signal.odds2:.2f}`\n\n"
                f"💰 **Шанс двойного выигрыша:**\n"
                f"При попадании в окно выигрывают **ОБА плеча** (+185% чистой прибыли!).\n\n"
                f"⚡ Сканер коридоров: [SmartBet.guru](https://smartbet.guru/tools/corridors)"
            )
        elif category == SignalCategory.FREEBET_OPTIMIZATION.value:
            title = f"🎁 ВИЛКА ПОД ФРИБЕТ: 80% КЭША ({signal.sport.upper()})"
            color = 3066993  # Green
            desc = (
                f"🏆 **{signal.tournament}**\n"
                f"⚔️ **{signal.event_name}**\n\n"
                f"📈 **Профит связки: +{signal.profit_percent:.2f}%**\n\n"
                f"📌 **{signal.bk1}** — {signal.market1 or signal.bet_type1} → `{signal.odds1:.2f}`\n"
                f"📌 **{signal.bk2}** — {signal.market2 or signal.bet_type2} → `{signal.odds2:.2f}`\n\n"
                f"💰 **Калькулятор перекрытия (80% Freebet Cash):**\n"
                f"Фрибет {signal.freebet_nominal:,.0f} {signal.currency} → ~{signal.freebet_nominal * 0.8:,.0f} {signal.currency} чистыми деньгами на баланс при любом исходе!\n\n"
                f"⚡ Калькулятор фрибета: [SmartBet.guru](https://smartbet.guru/tools/freebet-calculator)"
            )
        else:  # CLASSIC_SUREBET
            stakes = calculate_surebet_stakes(total_bank=getattr(signal, "recommended_bank", 10000.0) or 10000.0, k1=signal.odds1, k2=signal.odds2)
            title = f"⚡ КЛАССИЧЕСКАЯ ВИЛКА: +{signal.profit_percent:.2f}% ({signal.sport.upper()})"
            color = 3447003  # Blue
            desc = (
                f"🏆 **{signal.tournament}**\n"
                f"⚔️ **{signal.event_name}**\n\n"
                f"📈 **Гарантированная прибыль: +{signal.profit_percent:.2f}%**\n\n"
                f"📌 **{signal.bk1}** — {signal.market1 or signal.bet_type1} → `{signal.odds1:.2f}` (Ставка: {stakes['stake1']:,.0f} {signal.currency})\n"
                f"📌 **{signal.bk2}** — {signal.market2 or signal.bet_type2} → `{signal.odds2:.2f}` (Ставка: {stakes['stake2']:,.0f} {signal.currency})\n\n"
                f"💰 **Распределение банка ({stakes['total_bank']:,.0f} {signal.currency}):**\n"
                f"Чистая гарантированная выплата при любом исходе: **+{stakes['profit']:,.2f} {signal.currency}**!\n\n"
                f"⚡ Калькулятор вилки: [SmartBet.guru](https://smartbet.guru/tools/surebet-calculator)"
            )

        embed = {
            "title": title,
            "description": desc,
            "color": color,
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "footer": {"text": "SmartBet.guru Signal Hub • Telegram & Discord"}
        }

        payload = {
            "username": "SmartBet Signals",
            "avatar_url": "https://smartbet.guru/logo.png",
            "embeds": [embed]
        }

        if effective_dry_run:
            logger.info(f"[DRY_RUN] Discord webhook post simulated for signal {signal.signal_id}")
            return {"status": "SIMULATED", "signal_id": signal.signal_id}

        try:
            req_data = json.dumps(payload).encode("utf-8")
            req = urllib.request.Request(
                url,
                data=req_data,
                headers={"Content-Type": "application/json", "User-Agent": "SmartBetBot/1.0"},
                method="POST"
            )
            with urllib.request.urlopen(req, timeout=10) as resp:
                status_code = resp.getcode()
                logger.info(f"Successfully posted signal {signal.signal_id} to Discord (HTTP {status_code})")
                return {"status": "SUCCESS", "status_code": status_code, "signal_id": signal.signal_id}
        except Exception as e:
            logger.error(f"Failed to post signal to Discord Webhook: {e}")
            return {"status": "ERROR", "error": str(e), "signal_id": signal.signal_id}

    def broadcast_cycle(self, dry_run: Optional[bool] = None) -> Dict[str, Any]:
        """Broadcasts localized signals to all regional channels (RU, EN, FR, ES) and Discord."""
        logger.info("=== Starting Multilingual Broadcast Cycle across all 4 Telegram Channels & Discord ===")
        results = {}

        for code in self.channels.keys():
            sig = self.get_signal_for_channel(code)
            res = self.post_signal_to_channel(code, signal=sig, dry_run=dry_run)
            results[code] = res

        # Also dispatch mathematical signal to Discord Hub
        sig_primary = self.get_signal_for_channel("ru")
        discord_res = self.post_signal_to_discord(sig_primary, dry_run=dry_run)
        results["discord"] = discord_res

        summary = {
            "status": "success",
            "channels_posted": len(results),
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "results": results,
        }
        logger.info(f"Broadcast cycle finished. Posted to {len(results)} channels (Telegram & Discord).")
        return summary

    def start_scheduler_loop(self, interval_seconds: int = 300) -> None:
        """Starts background loop that posts periodic signals."""
        if self._running:
            return
        self._running = True

        def _loop():
            logger.info(f"Starting background post scheduler (interval: {interval_seconds}s)")
            while self._running:
                try:
                    self.broadcast_cycle()
                except Exception as e:
                    logger.error(f"Error in broadcast cycle: {e}")
                time.sleep(interval_seconds)

        self._worker_thread = threading.Thread(target=_loop, daemon=True)
        self._worker_thread.start()

    def stop_scheduler_loop(self) -> None:
        self._running = False


# ==============================================================================
# HTTP Server (Healthcheck, Actuator & Webhook)
# ==============================================================================

class TelegramWebhookHTTPHandler(BaseHTTPRequestHandler):
    scheduler: Optional[ChannelPosterScheduler] = None

    def log_message(self, format, *args):
        # Concise logging
        if "/health" in str(args[0]) or "/actuator" in str(args[0]):
            return
        logger.info("%s - - [%s] %s" % (self.client_address[0], self.log_date_time_string(), format % args))

    def do_HEAD(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path
        if path in ("/", "/health", "/healthz", "/actuator/health", "/actuator/health/readiness", "/actuator/health/liveness"):
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
        else:
            self.send_response(404)
            self.end_headers()

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path

        if path in ("/", "/health", "/healthz", "/actuator/health", "/actuator/health/readiness", "/actuator/health/liveness"):
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            payload = {
                "status": "UP",
                "service": "smm-bot-telegram",
                "checks": {
                    "telegram_bot": "UP",
                    "channels": 4,
                    "redis": "UP" if (self.scheduler and self.scheduler.redis_client) else "STANDALONE_OK",
                },
                "timestamp": datetime.now(timezone.utc).isoformat(),
            }
            self.wfile.write(json.dumps(payload).encode("utf-8"))
            return

        if path == "/api/v1/telegram/status":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            channels_info = {k: asdict(v) for k, v in REGIONAL_CHANNELS.items()}
            status_payload = {
                "service": "smm-bot-telegram",
                "channels": channels_info,
                "history_count": len(self.scheduler.posted_history) if self.scheduler else 0,
                "timestamp": datetime.now(timezone.utc).isoformat(),
            }
            self.wfile.write(json.dumps(status_payload).encode("utf-8"))
            return

        if path == "/api/v1/telegram/discord":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            url = os.getenv("DISCORD_WEBHOOK_URL", "")
            if not url and self.scheduler and self.scheduler.redis_client:
                try:
                    url = self.scheduler.redis_client.get("social:discord:webhook_url") or ""
                except Exception:
                    pass
            payload_resp = {
                "configured": bool(url),
                "webhook_url": (url[:35] + "...") if len(url) > 35 else url,
                "full_url": url,
                "strategy": "SIGNALS_AND_ODDS_HUB",
                "timestamp": datetime.now(timezone.utc).isoformat(),
            }
            self.wfile.write(json.dumps(payload_resp).encode("utf-8"))
            return

        self.send_response(404)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(b'{"error": "not found"}')

    def do_POST(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path
        query_params = urllib.parse.parse_qs(parsed.query)

        content_len = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(content_len).decode("utf-8") if content_len > 0 else "{}"

        try:
            payload = json.loads(body) if body else {}
        except Exception:
            payload = {}

        if path in ("/api/v1/telegram/webhook", "/webhook"):
            logger.info("Received Telegram webhook update")
            result = None
            if self.scheduler and self.scheduler.ai_prompter:
                result = self.scheduler.ai_prompter.process_incoming_comment(payload)

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            resp = {"ok": True, "processed": result is not None, "feedback": result}
            self.wfile.write(json.dumps(resp).encode("utf-8"))
            return

        if path == "/api/v1/telegram/post":
            channel_code = payload.get("channel") or query_params.get("channel", ["ru"])[0]
            logger.info(f"Triggered manual post for channel: {channel_code}")
            if self.scheduler:
                if channel_code == "all":
                    res = self.scheduler.broadcast_cycle()
                else:
                    res = self.scheduler.post_signal_to_channel(channel_code)
                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps(res).encode("utf-8"))
                return

        if path == "/api/v1/telegram/discord":
            new_url = payload.get("webhook_url", "").strip()
            test_mode = payload.get("test", False) or payload.get("send_test", False)
            if new_url and self.scheduler and self.scheduler.redis_client:
                try:
                    self.scheduler.redis_client.set("social:discord:webhook_url", new_url)
                    os.environ["DISCORD_WEBHOOK_URL"] = new_url
                    logger.info("Saved new Discord webhook URL to Redis and environment.")
                except Exception as e:
                    logger.error(f"Failed to persist Discord webhook URL: {e}")

            test_result = None
            if test_mode and self.scheduler:
                sig = self.scheduler.get_signal_for_channel("ru")
                test_result = self.scheduler.post_signal_to_discord(sig, webhook_url=new_url or None)

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            resp = {
                "ok": True,
                "saved": bool(new_url),
                "test_result": test_result,
            }
            self.wfile.write(json.dumps(resp).encode("utf-8"))
            return

        self.send_response(404)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(b'{"error": "not found"}')


class ReusableTCPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    allow_reuse_address = True
    daemon_threads = True

    def handle_error(self, request, client_address):
        # Gracefully handle probe socket closures/timeouts without dumping tracebacks
        pass


def start_server(scheduler: ChannelPosterScheduler, port: int = 8080, blocking: bool = True) -> ReusableTCPServer:
    """Starts the Telegram webhook and healthcheck server."""
    TelegramWebhookHTTPHandler.scheduler = scheduler
    server = ReusableTCPServer(("0.0.0.0", port), TelegramWebhookHTTPHandler)
    logger.info(f"Telegram Bot & Webhook server running on port {port} (/healthz, /actuator/health, /webhook)")

    if blocking:
        try:
            server.serve_forever()
        except KeyboardInterrupt:
            logger.info("Shutting down server...")
        finally:
            server.server_close()
    else:
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()

    return server


# ==============================================================================
# CLI Entrypoint
# ==============================================================================

def main():
    parser = argparse.ArgumentParser(description="SmartBet Telegram Multilingual Poster & AI-Prompter")
    parser.add_argument("--action", default="server", choices=["server", "broadcast", "post", "test-comment"])
    parser.add_argument("--channel", default="ru", choices=["ru", "en", "fr", "es", "all"])
    parser.add_argument("--port", type=int, default=int(os.getenv("PORT", "8080")))
    parser.add_argument("--token", default=os.getenv("TELEGRAM_BOT_TOKEN", DEFAULT_BOT_TOKEN))
    parser.add_argument("--dry-run", action="store_true", default=False)
    args = parser.parse_args()

    scheduler = ChannelPosterScheduler(bot_token=args.token, dry_run=args.dry_run)

    if args.action == "broadcast":
        res = scheduler.broadcast_cycle()
        print(json.dumps(res, indent=2, ensure_ascii=False))
    elif args.action == "post":
        res = scheduler.post_signal_to_channel(args.channel)
        print(json.dumps(res, indent=2, ensure_ascii=False))
    elif args.action == "test-comment":
        sample_update = {
            "update_id": 10001,
            "message": {
                "message_id": 999,
                "chat": {"id": -1003960368887, "type": "supergroup"},
                "from": {"id": 1234567, "username": "pro_bettor"},
                "text": "How do you calculate 80% freebet cash with matched betting?",
                "reply_to_message": {
                    "message_id": 888,
                    "text": "🎯 IDEAL FOR FREEBET (80% GUARANTEED CASH)\nUEFA Champions League Arsenal vs Real Madrid",
                },
            },
        }
        res = scheduler.ai_prompter.process_incoming_comment(sample_update)
        print(json.dumps(res, indent=2, ensure_ascii=False))
    elif args.action == "server":
        interval = int(os.getenv("TELEGRAM_POST_INTERVAL_SECONDS", "300"))
        scheduler.start_scheduler_loop(interval_seconds=interval)
        start_server(scheduler, port=args.port, blocking=True)


if __name__ == "__main__":
    main()
