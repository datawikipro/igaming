#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Twitter/X Automation Agent (@smartbetguru)
Task: [smm-twitter] Автоматизация Twitter/X (@smartbetguru): авторизация, постинг вилок и мониторинг комментариев
Plane Task ID: 0297c2c5-db68-4b97-a7fc-16a5d6b5cd80

Golden Rules enforced:
- Rule 6 (Network Proxy Routing): Twitter/X routes through `outline-us` (US West Coast, LA, CA)
  via cluster proxy http://100.83.113.50:3128, using dedicated static `purevpn-us-ca.proxy:3128`.
- Rule 9 (Firefox / Camoufox, No Incognito, Cache Warmup):
  Uses launch_persistent_context(). Profile persisted in Redis `smm:profile:twitter:<account_id>`.
  Mandatory 2–3 min neutral-site warmup before any Twitter interactions.
- Rule 10 (80% Freebet Cash & Affiliate):
  All surebet/freebet posts include the 80% guaranteed cash formula and UTM affiliate links.
- Rule 12 (Dedicated Static Proxy 1:1 for Twitter):
  @smartbetguru maps 1:1 to `purevpn-us-ca` (LA, CA) — never rotated.

Responsibilities:
1. Session-based Twitter/X authentication via persistent Firefox profile (Playwright + Camoufox).
   Credentials loaded from env/Redis; cookies.sqlite preserved across restarts.
2. Periodic posting of surebet/arbitrage alerts consumed from Redis queue `smm:queue:twitter`.
   Posts formatted with bookmaker names, stake ratios, odds, and mandatory disclaimer.
   Freebet posts include 80% guaranteed cash calculation and UTM affiliate tracking links.
3. Comment monitoring on own timeline and @mentions via polling loop.
   Patron comments (known TG VIP / Boosty IDs) escalated to `feedback:queue:twitter`.
   Anti-spam cooldown and deduplication via Redis sets.
4. Human-kinematics: cubic Bezier mouse curves, jitter, randomized delays.
5. HTTP Healthcheck server: /healthz, /actuator/health, /actuator/health/readiness,
   /actuator/health/liveness, /api/v1/twitter/status on port 8080.
"""

import argparse
import asyncio
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
import hashlib
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
import logging
import math
import os
import random
import sys
import threading
import time
from typing import Any, Dict, List, Optional, Tuple

try:
    import redis
except ImportError:
    redis = None

try:
    import requests
except ImportError:
    requests = None

# SMM shared modules
try:
    from affiliate_manager import AFFILIATE_CATALOG, BookmakerAffiliateInfo, generate_tracking_link
except ImportError:
    AFFILIATE_CATALOG = {}

    def generate_tracking_link(
        bookmaker_id: str,
        utm_source: str = "twitter",
        utm_medium: str = "surebet",
        **kwargs,
    ) -> str:
        return (
            f"https://smartbet.guru/go/{bookmaker_id}"
            f"?utm_source={utm_source}&utm_medium={utm_medium}"
        )


try:
    from cache_warmup import CacheWarmupManager, DEFAULT_WARMUP_SITES
except ImportError:
    CacheWarmupManager = None  # type: ignore[assignment,misc]
    DEFAULT_WARMUP_SITES = [
        "https://www.bbc.com/sport",
        "https://www.espn.com",
        "https://www.flashscore.com",
    ]

try:
    from browser_manager import BrowserConfig, StealthBrowserSession
except ImportError:
    BrowserConfig = None  # type: ignore[assignment,misc]
    StealthBrowserSession = None  # type: ignore[assignment]

logger = logging.getLogger("TwitterAgent")
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [smm-twitter] %(message)s",
)

# ==============================================================================
# Constants
# ==============================================================================

MANDATORY_DISCLAIMER = (
    "Ставки на спорт сопряжены с финансовыми рисками. "
    "Мы против лудомании и необдуманного беттинга. Играйте ответственно."
)

TWITTER_BASE_URL = "https://x.com"
TWITTER_LOGIN_URL = "https://x.com/i/flow/login"
TWITTER_HOME_URL = "https://x.com/home"
TWITTER_NOTIFICATIONS_URL = "https://x.com/notifications/mentions"

# Cooldowns & rate-limit constants
MIN_COMMENT_REPLY_COOLDOWN_SEC = 120  # 2 min between replies to same thread
MAX_POSTS_PER_HOUR = 6
MIN_POST_INTERVAL_SEC = 600  # 10 min between posts

# Twitter character limit
TWITTER_CHAR_LIMIT = 280


# ==============================================================================
# 1. Configuration
# ==============================================================================


@dataclass
class TwitterAgentConfig:
    """Configuration for the Twitter/X Automation Agent."""

    account_id: str = "smartbetguru_twitter"
    twitter_handle: str = "@smartbetguru"

    # Redis
    redis_url: str = os.getenv("REDIS_URL", "redis://igaming-redis:6379/0")
    session_key: str = os.getenv("REDIS_SESSION_KEY", "smm:session:twitter")
    profile_key_prefix: str = os.getenv("REDIS_PROFILE_KEY", "smm:profile:twitter")
    queue_key: str = os.getenv("TWITTER_QUEUE_KEY", "smm:queue:twitter")
    feedback_queue_key: str = "feedback:queue:twitter"
    seen_comments_key: str = "smm:seen:twitter:comments"

    # Rule 6 + Rule 12: Dedicated static proxy for Twitter (US West Coast, LA)
    # `purevpn-us-ca` K8s DNS Service → HTTP proxy on port 3128
    proxy_server: str = os.getenv(
        "TWITTER_PROXY",
        os.getenv("US_PROXY", "http://purevpn-us-ca.proxy:3128"),
    )

    # Portal API (surebet feed)
    portal_api_url: str = os.getenv("PORTAL_API_URL", "http://igaming-portal:80")

    # Twitter credentials (loaded from env or K8s Secret)
    twitter_username: str = os.getenv("TWITTER_USERNAME", "smartbetguru")
    twitter_password: str = os.getenv("TWITTER_PASSWORD", "")
    twitter_email: str = os.getenv("TWITTER_EMAIL", "")

    # Operational settings
    healthcheck_port: int = int(os.getenv("PORT", os.getenv("HEALTHCHECK_PORT", "8080")))
    post_interval_sec: int = int(os.getenv("POST_INTERVAL_SEC", str(MIN_POST_INTERVAL_SEC)))
    monitor_interval_sec: int = int(os.getenv("MONITOR_INTERVAL_SEC", "180"))
    warmup_duration_sec: int = int(os.getenv("WARMUP_DURATION", "150"))
    headless: bool = os.getenv("HEADLESS", "false").lower() == "true"
    dry_run: bool = os.getenv("DRY_RUN", "false").lower() == "true"

    def get_profile_key(self) -> str:
        return f"{self.profile_key_prefix}:{self.account_id}"


# ==============================================================================
# 2. Freebet Math Helper (Rule 10)
# ==============================================================================


class FreebetMathHelper:
    """
    Rule 10: Implements the 80% guaranteed cash conversion formula for freebet SNR.

    Formula: eta = ((K1 - 1) * (K2 - 1)) / K2 ≈ 0.80
    Recommended range: K1 ∈ [4.5, 6.0], K2 ∈ [1.20, 1.28]
    """

    @staticmethod
    def calculate_freebet_conversion(
        k1: float,
        k2: float,
        freebet_amount: float = 1000.0,
    ) -> Dict[str, float]:
        """
        Calculate guaranteed cash from a SNR freebet.

        Formula: eta = ((K1 - 1) * (K2 - 1)) / K2 ≈ 0.80 (Rule 10: 80% guaranteed cash)
        Recommended range: K1 ∈ [4.5, 6.0], K2 ∈ [1.20, 1.28]

        Args:
            k1: Back odds at target bookmaker (surebet leg).
            k2: Lay odds at exchange / opposing bookmaker.
            freebet_amount: Nominal freebet value in RUB.

        Returns:
            Dict with conversion_rate, conversion_percentage, guaranteed_cash, hedge_stake.

        Raises:
            ValueError: If odds are invalid (k1 <= 1.0 or k2 <= 1.0).
        """
        if k1 <= 1.0:
            raise ValueError(f"k1 must be > 1.0, got {k1}")
        if k2 <= 1.0:
            raise ValueError(f"k2 must be > 1.0, got {k2}")

        eta = ((k1 - 1.0) * (k2 - 1.0)) / k2
        guaranteed_cash = freebet_amount * eta
        # Hedge stake formula for SNR freebet: lay_stake = freebet * (k1 - 1) / k2
        hedge_stake = freebet_amount * (k1 - 1.0) / k2

        return {
            "conversion_rate": round(eta, 4),
            "conversion_percentage": round(eta * 100.0, 2),
            "guaranteed_cash": round(guaranteed_cash, 2),
            "hedge_stake": round(hedge_stake, 2),
            "k1": k1,
            "k2": k2,
            "freebet_amount": freebet_amount,
        }

    @staticmethod
    def generate_surebet_tweet(
        bookmaker1: str,
        bookmaker2: str,
        event: str,
        odds1: float,
        odds2: float,
        profit_pct: float,
        sport: str = "Футбол",
        affiliate_bm1: Optional[str] = None,
        affiliate_bm2: Optional[str] = None,
    ) -> str:
        """
        Generate a compliant surebet tweet (≤280 chars) with disclaimer.
        Includes affiliate UTM links if provided.
        """
        link1 = (
            generate_tracking_link(affiliate_bm1, utm_source="twitter", utm_medium="surebet")
            if affiliate_bm1
            else f"https://smartbet.guru/go/{bookmaker1.lower()}?utm_source=twitter&utm_medium=surebet"
        )
        link2 = (
            generate_tracking_link(affiliate_bm2, utm_source="twitter", utm_medium="surebet")
            if affiliate_bm2
            else f"https://smartbet.guru/go/{bookmaker2.lower()}?utm_source=twitter&utm_medium=surebet"
        )

        body = (
            f"⚡ Вилка +{profit_pct:.1f}% | {sport}\n"
            f"📌 {event}\n"
            f"🔹 {bookmaker1}: К={odds1:.2f} → {link1}\n"
            f"🔸 {bookmaker2}: К={odds2:.2f} → {link2}\n"
            f"📊 smartbet.guru | #арбитраж #вилки #беттинг"
        )

        # Twitter limit: append disclaimer if it fits, else truncate body
        disclaimer_short = "⚠️ Играйте ответственно."
        full_tweet = f"{body}\n{disclaimer_short}"
        if len(full_tweet) <= TWITTER_CHAR_LIMIT:
            return full_tweet
        # Trim body to fit
        available = TWITTER_CHAR_LIMIT - len(disclaimer_short) - 2
        return body[:available] + f"\n{disclaimer_short}"

    @staticmethod
    def generate_freebet_tweet(
        bookmaker: str,
        freebet_amount: float,
        k1: float = 5.0,
        k2: float = 1.25,
        affiliate_id: Optional[str] = None,
    ) -> str:
        """
        Generate a compliant freebet marketing tweet with 80% cash calculation.
        Rule 10: Must state guaranteed_cash_80 and include affiliate tracking URL.
        """
        calc = FreebetMathHelper.calculate_freebet_conversion(k1=k1, k2=k2, freebet_amount=freebet_amount)
        guaranteed = calc["guaranteed_cash"]

        tracking_url = (
            generate_tracking_link(affiliate_id, utm_source="twitter", utm_medium="freebet")
            if affiliate_id
            else f"https://smartbet.guru/tools/freebet-calculator?utm_source=twitter&utm_medium=freebet"
        )

        def _fmt_rub(amount: float) -> str:
            """Format RUB amount with Russian thousand separator (space)."""
            return f"{int(amount):,}".replace(",", "\u00a0")  # non-breaking space

        body = (
            f"🎁 Фрибет {_fmt_rub(freebet_amount)} ₽ в {bookmaker} → {_fmt_rub(guaranteed)} ₽ чистыми!\n"
            f"✅ Гарантия при любом исходе через вилку ({calc['conversion_percentage']:.0f}% конвертация)\n"
            f"🔗 Калькулятор: {tracking_url}\n"
            f"⚠️ Играйте ответственно. #фрибет #беттинг"
        )

        if len(body) <= TWITTER_CHAR_LIMIT:
            return body
        # Truncate keeping disclaimer
        disclaimer = "⚠️ Играйте ответственно. #фрибет"
        available = TWITTER_CHAR_LIMIT - len(disclaimer) - 2
        return body[:available] + f"\n{disclaimer}"


# ==============================================================================
# 3. Mock Redis Session (fallback when Redis is unavailable)
# ==============================================================================


class MockRedisSession:
    """In-memory fallback Redis session for local testing."""

    def __init__(self) -> None:
        self._store: Dict[str, Any] = {}

    def get(self, key: str) -> Optional[bytes]:
        val = self._store.get(key)
        return val.encode() if isinstance(val, str) else val

    def set(self, key: str, value: Any, ex: Optional[int] = None) -> bool:
        self._store[key] = value
        return True

    def setex(self, key: str, seconds: int, value: Any) -> bool:
        self._store[key] = value
        return True

    def hset(self, name: str, mapping: Optional[Dict] = None, **kwargs: Any) -> int:
        if name not in self._store:
            self._store[name] = {}
        if mapping:
            self._store[name].update(mapping)
        self._store[name].update(kwargs)
        return 1

    def hgetall(self, name: str) -> Dict[bytes, bytes]:
        data = self._store.get(name, {})
        return {k.encode(): str(v).encode() for k, v in data.items()}

    def lpop(self, key: str) -> Optional[bytes]:
        lst = self._store.get(key, [])
        if lst:
            val = lst.pop(0)
            self._store[key] = lst
            return val.encode() if isinstance(val, str) else val
        return None

    def rpush(self, key: str, *values: Any) -> int:
        lst = self._store.setdefault(key, [])
        lst.extend(values)
        return len(lst)

    def sismember(self, name: str, value: Any) -> bool:
        return value in self._store.get(name, set())

    def sadd(self, name: str, *values: Any) -> int:
        s = self._store.setdefault(name, set())
        added = len(set(values) - s)
        s.update(values)
        return added

    def incr(self, key: str) -> int:
        self._store[key] = int(self._store.get(key, 0)) + 1
        return self._store[key]

    def expire(self, key: str, seconds: int) -> bool:
        return True

    def ping(self) -> bool:
        return True


# ==============================================================================
# 4. Session Manager (Redis profile persistence)
# ==============================================================================


class TwitterSessionManager:
    """
    Manages Twitter agent session state in Redis.
    Keys:
    - smm:session:twitter → current agent status (JSON)
    - smm:profile:twitter:<account_id> → serialized browser profile metadata
    - smm:seen:twitter:comments → set of processed comment IDs (deduplication)
    """

    SESSION_TTL_SECONDS = 86400 * 7  # 7 days

    def __init__(self, config: TwitterAgentConfig) -> None:
        self.config = config
        self._redis: Optional[Any] = None
        self.metrics: Dict[str, int] = {
            "posts_published": 0,
            "comments_replied": 0,
            "mentions_monitored": 0,
            "surebets_posted": 0,
            "freebets_posted": 0,
            "warmup_cycles": 0,
            "login_attempts": 0,
            "login_success": 0,
        }
        self.started_at = datetime.now(timezone.utc).isoformat()

    def connect(self) -> None:
        if redis is None:
            logger.warning("Redis library not available, using MockRedisSession")
            self._redis = MockRedisSession()
            return
        try:
            client = redis.Redis.from_url(self.config.redis_url, decode_responses=False)
            client.ping()
            self._redis = client
            logger.info(f"Redis connected: {self.config.redis_url}")
        except Exception as exc:
            logger.warning(f"Redis connection failed ({exc}), using MockRedisSession")
            self._redis = MockRedisSession()

    @property
    def r(self) -> Any:
        if self._redis is None:
            self.connect()
        return self._redis

    def save_status(self, status: str = "running") -> None:
        payload = {
            "account_id": self.config.account_id,
            "handle": self.config.twitter_handle,
            "status": status,
            "started_at": self.started_at,
            "updated_at": datetime.now(timezone.utc).isoformat(),
            **self.metrics,
        }
        try:
            self.r.setex(
                self.config.session_key,
                self.SESSION_TTL_SECONDS,
                json.dumps(payload),
            )
        except Exception as exc:
            logger.warning(f"Failed to persist session: {exc}")

    def load_status(self) -> Dict[str, Any]:
        try:
            raw = self.r.get(self.config.session_key)
            if raw:
                return json.loads(raw)
        except Exception:
            pass
        return {}

    def is_comment_seen(self, comment_id: str) -> bool:
        try:
            return bool(self.r.sismember(self.config.seen_comments_key, comment_id))
        except Exception:
            return False

    def mark_comment_seen(self, comment_id: str) -> None:
        try:
            self.r.sadd(self.config.seen_comments_key, comment_id)
            self.r.expire(self.config.seen_comments_key, 86400 * 30)
        except Exception as exc:
            logger.warning(f"Could not mark comment seen: {exc}")

    def pop_tweet_job(self) -> Optional[Dict[str, Any]]:
        """Dequeue next tweet job from `smm:queue:twitter`."""
        try:
            raw = self.r.lpop(self.config.queue_key)
            if raw:
                return json.loads(raw)
        except Exception as exc:
            logger.warning(f"Queue pop failed: {exc}")
        return None

    def push_feedback(self, comment_data: Dict[str, Any]) -> None:
        """Enqueue escalated comment to feedback queue for Patron CRM."""
        try:
            self.r.rpush(self.config.feedback_queue_key, json.dumps(comment_data))
        except Exception as exc:
            logger.warning(f"Feedback push failed: {exc}")

    def increment(self, metric: str, amount: int = 1) -> None:
        self.metrics[metric] = self.metrics.get(metric, 0) + amount


# ==============================================================================
# 5. Twitter/X Browser Automation Core
# ==============================================================================


class TwitterBrowserAgent:
    """
    Core Twitter/X browser automation agent.

    Uses Playwright Firefox via StealthBrowserSession (persistent context).
    Rule 9: NO new_context(), always launch_persistent_context().
    Rule 6 + 12: Outbound via `purevpn-us-ca.proxy:3128` (1:1 static, US-West Coast).
    """

    def __init__(
        self,
        config: TwitterAgentConfig,
        session_mgr: TwitterSessionManager,
    ) -> None:
        self.config = config
        self.session_mgr = session_mgr
        self._page: Optional[Any] = None
        self._browser_session: Optional[Any] = None
        self._authenticated: bool = False
        self._last_post_time: float = 0.0
        self._reply_cooldowns: Dict[str, float] = {}  # thread_id → last_reply_ts

    # ------------------------------------------------------------------
    # Human Kinematics (Rule 9: Bezier curves, jitter, delays)
    # ------------------------------------------------------------------

    @staticmethod
    def _cubic_bezier(
        p0: Tuple[float, float],
        p3: Tuple[float, float],
        steps: int = 20,
    ) -> List[Tuple[float, float]]:
        """Generate a cubic Bezier curve between two points with random control points."""
        dx = p3[0] - p0[0]
        dy = p3[1] - p0[1]
        dist = math.hypot(dx, dy)
        deviation = dist * random.uniform(0.15, 0.30)

        p1 = (
            p0[0] + dx * 0.3 + random.uniform(-deviation, deviation),
            p0[1] + dy * 0.3 + random.uniform(-deviation, deviation),
        )
        p2 = (
            p0[0] + dx * 0.7 + random.uniform(-deviation, deviation),
            p0[1] + dy * 0.7 + random.uniform(-deviation, deviation),
        )

        points = []
        for i in range(steps + 1):
            t = i / steps
            # Smoothstep easing
            t_ease = t * t * (3 - 2 * t)
            bx = (
                (1 - t_ease) ** 3 * p0[0]
                + 3 * (1 - t_ease) ** 2 * t_ease * p1[0]
                + 3 * (1 - t_ease) * t_ease ** 2 * p2[0]
                + t_ease ** 3 * p3[0]
            )
            by = (
                (1 - t_ease) ** 3 * p0[1]
                + 3 * (1 - t_ease) ** 2 * t_ease * p1[1]
                + 3 * (1 - t_ease) * t_ease ** 2 * p2[1]
                + t_ease ** 3 * p3[1]
            )
            # Micro-jitter
            jitter_x = random.gauss(0, 0.5)
            jitter_y = random.gauss(0, 0.5)
            points.append((bx + jitter_x, by + jitter_y))
        return points

    async def _human_move_and_click(self, x: float, y: float) -> None:
        """Move mouse along Bezier curve to (x, y) and click."""
        if self._page is None:
            return
        try:
            cur = await self._page.evaluate("() => ({x: window.mouseX || 640, y: window.mouseY || 400})")
            p0 = (cur.get("x", 640), cur.get("y", 400))
        except Exception:
            p0 = (random.uniform(400, 800), random.uniform(300, 500))

        trajectory = self._cubic_bezier(p0, (x, y))
        for px, py in trajectory:
            await self._page.mouse.move(px, py)
            await asyncio.sleep(random.uniform(0.005, 0.020))
        await asyncio.sleep(random.uniform(0.05, 0.15))
        await self._page.mouse.click(x, y)

    async def _human_type(self, text: str, delay_ms_min: int = 40, delay_ms_max: int = 120) -> None:
        """Type text character-by-character with random delays."""
        if self._page is None:
            return
        for char in text:
            await self._page.keyboard.type(char)
            await asyncio.sleep(random.uniform(delay_ms_min / 1000, delay_ms_max / 1000))
            # Occasional hesitation
            if random.random() < 0.05:
                await asyncio.sleep(random.uniform(0.3, 0.8))

    async def _random_scroll(self, ticks: int = 3) -> None:
        """Natural intermittent scroll."""
        if self._page is None:
            return
        for _ in range(ticks):
            delta = random.randint(200, 450)
            await self._page.mouse.wheel(0, delta)
            await asyncio.sleep(random.uniform(1.0, 2.5))
        if random.random() < 0.35:
            await self._page.mouse.wheel(0, -random.randint(80, 200))

    # ------------------------------------------------------------------
    # 5.1 Cache Warmup (Rule 9)
    # ------------------------------------------------------------------

    async def perform_cache_warmup(self) -> None:
        """
        Rule 9: Mandatory 2–3 minute neutral-site warmup before Twitter interactions.
        Builds organic profile: sports news, flashscore, BBC Sport.
        """
        if self._page is None:
            return

        logger.info("=== Cache Warmup: Starting neutral browsing phase (Rule 9) ===")
        warmup_sites = list(DEFAULT_WARMUP_SITES) + [
            "https://www.goal.com",
            "https://www.sport-express.ru",
        ]
        random.shuffle(warmup_sites)

        target_duration = self.config.warmup_duration_sec
        start_ts = time.monotonic()

        for site_url in warmup_sites:
            if time.monotonic() - start_ts >= target_duration:
                break
            try:
                logger.info(f"Warmup: visiting {site_url}")
                await self._page.goto(site_url, timeout=25000, wait_until="domcontentloaded")
                await asyncio.sleep(random.uniform(2.0, 5.0))
                await self._random_scroll(ticks=random.randint(2, 5))
                await asyncio.sleep(random.uniform(3.0, 8.0))
            except Exception as exc:
                logger.warning(f"Warmup site {site_url} failed: {exc}")

        elapsed = time.monotonic() - start_ts
        logger.info(f"=== Cache Warmup complete: {elapsed:.1f}s ===")
        self.session_mgr.increment("warmup_cycles")

    # ------------------------------------------------------------------
    # 5.2 Authentication
    # ------------------------------------------------------------------

    async def login(self) -> bool:
        """
        Authenticate to Twitter/X using persistent Firefox profile.
        Rule 9: Uses launch_persistent_context — cookies survive restarts.
        Checks if already logged in by detecting the home timeline.
        Returns True if authenticated successfully.
        """
        if self._page is None:
            logger.error("Page not initialised — cannot log in")
            return False

        self.session_mgr.increment("login_attempts")
        logger.info(f"Twitter login: navigating to {TWITTER_HOME_URL}")

        try:
            await self._page.goto(TWITTER_HOME_URL, timeout=30000, wait_until="domcontentloaded")
            await asyncio.sleep(random.uniform(2.0, 4.0))

            # Check if already authenticated (persistent cookies)
            current_url = self._page.url
            if "home" in current_url and "login" not in current_url:
                logger.info("Twitter: already authenticated via persistent profile cookies")
                self._authenticated = True
                self.session_mgr.increment("login_success")
                return True

            # Need to log in
            logger.info("Twitter: session expired, initiating login flow")
            await self._page.goto(TWITTER_LOGIN_URL, timeout=30000, wait_until="domcontentloaded")
            await asyncio.sleep(random.uniform(2.5, 4.0))

            # Step 1: username / email
            username_input = await self._page.wait_for_selector(
                'input[autocomplete="username"], input[name="text"]',
                timeout=15000,
            )
            if username_input is None:
                logger.error("Twitter login: username field not found")
                return False

            bbox = await username_input.bounding_box()
            if bbox:
                await self._human_move_and_click(
                    bbox["x"] + bbox["width"] / 2,
                    bbox["y"] + bbox["height"] / 2,
                )
            await asyncio.sleep(random.uniform(0.3, 0.8))
            await self._human_type(self.config.twitter_username)
            await asyncio.sleep(random.uniform(0.5, 1.2))

            # Click "Next"
            next_btn = await self._page.wait_for_selector(
                '[data-testid="LoginForm_Forward_Button"], button:has-text("Next"), button:has-text("Далее")',
                timeout=10000,
            )
            if next_btn:
                btn_box = await next_btn.bounding_box()
                if btn_box:
                    await self._human_move_and_click(
                        btn_box["x"] + btn_box["width"] / 2,
                        btn_box["y"] + btn_box["height"] / 2,
                    )
            await asyncio.sleep(random.uniform(1.5, 3.0))

            # Step 2: handle unusual_activity check (phone / email verification prompt)
            try:
                verification_input = await self._page.wait_for_selector(
                    'input[data-testid="ocfEnterTextTextInput"]',
                    timeout=5000,
                )
                if verification_input and self.config.twitter_email:
                    logger.info("Twitter: additional verification step (email/phone)")
                    v_bbox = await verification_input.bounding_box()
                    if v_bbox:
                        await self._human_move_and_click(
                            v_bbox["x"] + v_bbox["width"] / 2,
                            v_bbox["y"] + v_bbox["height"] / 2,
                        )
                    await self._human_type(self.config.twitter_email)
                    await asyncio.sleep(random.uniform(0.5, 1.0))
                    next_btn2 = await self._page.query_selector(
                        '[data-testid="ocfEnterTextNextButton"]'
                    )
                    if next_btn2:
                        await next_btn2.click()
                    await asyncio.sleep(random.uniform(1.5, 3.0))
            except Exception:
                pass  # No verification step needed

            # Step 3: password
            pwd_input = await self._page.wait_for_selector(
                'input[name="password"], input[type="password"]',
                timeout=12000,
            )
            if pwd_input is None:
                logger.error("Twitter login: password field not found")
                return False

            pwd_box = await pwd_input.bounding_box()
            if pwd_box:
                await self._human_move_and_click(
                    pwd_box["x"] + pwd_box["width"] / 2,
                    pwd_box["y"] + pwd_box["height"] / 2,
                )
            await asyncio.sleep(random.uniform(0.4, 0.9))
            await self._human_type(self.config.twitter_password)
            await asyncio.sleep(random.uniform(0.6, 1.4))

            # Click "Log in"
            login_btn = await self._page.wait_for_selector(
                '[data-testid="LoginForm_Login_Button"], button:has-text("Log in"), button:has-text("Войти")',
                timeout=10000,
            )
            if login_btn:
                lb_box = await login_btn.bounding_box()
                if lb_box:
                    await self._human_move_and_click(
                        lb_box["x"] + lb_box["width"] / 2,
                        lb_box["y"] + lb_box["height"] / 2,
                    )
            await asyncio.sleep(random.uniform(3.0, 6.0))

            # Verify successful login
            final_url = self._page.url
            if "home" in final_url or "status" in final_url:
                logger.info(f"Twitter login: SUCCESS (url={final_url})")
                self._authenticated = True
                self.session_mgr.increment("login_success")
                return True

            logger.warning(f"Twitter login: unexpected URL after login attempt: {final_url}")
            return False

        except Exception as exc:
            logger.error(f"Twitter login failed: {exc}", exc_info=True)
            return False

    # ------------------------------------------------------------------
    # 5.3 Tweet Posting
    # ------------------------------------------------------------------

    async def post_tweet(self, text: str) -> bool:
        """
        Compose and publish a tweet.
        Enforces MAX_POSTS_PER_HOUR and MIN_POST_INTERVAL_SEC rate limits.
        Returns True on success.
        """
        if not self._authenticated:
            logger.warning("Cannot post: not authenticated")
            return False

        if self._page is None:
            return False

        # Rate limiting
        now = time.monotonic()
        elapsed = now - self._last_post_time
        if elapsed < self.config.post_interval_sec:
            wait_sec = self.config.post_interval_sec - elapsed
            logger.info(f"Post rate-limit: waiting {wait_sec:.0f}s before next tweet")
            await asyncio.sleep(wait_sec)

        # Truncate if needed
        if len(text) > TWITTER_CHAR_LIMIT:
            text = text[: TWITTER_CHAR_LIMIT - 1] + "…"

        if self.config.dry_run:
            logger.info(f"[DRY RUN] Would tweet ({len(text)} chars):\n{text}")
            self._last_post_time = time.monotonic()
            self.session_mgr.increment("posts_published")
            return True

        try:
            # Navigate to home if not there
            if "home" not in self._page.url:
                await self._page.goto(TWITTER_HOME_URL, timeout=20000, wait_until="domcontentloaded")
                await asyncio.sleep(random.uniform(1.5, 3.0))

            # Click tweet compose box
            compose_box = await self._page.wait_for_selector(
                '[data-testid="tweetTextarea_0"], [aria-label="Tweet text"], div[role="textbox"]',
                timeout=12000,
            )
            if compose_box is None:
                logger.error("Tweet compose box not found")
                return False

            cb_box = await compose_box.bounding_box()
            if cb_box:
                await self._human_move_and_click(
                    cb_box["x"] + cb_box["width"] / 2,
                    cb_box["y"] + cb_box["height"] / 2,
                )
            await asyncio.sleep(random.uniform(0.5, 1.0))
            await self._human_type(text)
            await asyncio.sleep(random.uniform(1.0, 2.5))

            # Click "Tweet" / "Post" button
            post_btn = await self._page.wait_for_selector(
                '[data-testid="tweetButtonInline"], button:has-text("Tweet"), button:has-text("Post"), button:has-text("Опубликовать")',
                timeout=10000,
            )
            if post_btn is None:
                logger.error("Tweet post button not found")
                return False

            pb_box = await post_btn.bounding_box()
            if pb_box:
                await self._human_move_and_click(
                    pb_box["x"] + pb_box["width"] / 2,
                    pb_box["y"] + pb_box["height"] / 2,
                )
            await asyncio.sleep(random.uniform(2.5, 5.0))

            self._last_post_time = time.monotonic()
            self.session_mgr.increment("posts_published")
            logger.info(f"Tweet posted successfully ({len(text)} chars)")
            return True

        except Exception as exc:
            logger.error(f"Failed to post tweet: {exc}", exc_info=True)
            return False

    # ------------------------------------------------------------------
    # 5.4 Comment Monitoring
    # ------------------------------------------------------------------

    async def monitor_mentions(self) -> List[Dict[str, Any]]:
        """
        Poll @mentions tab and extract new comments for processing.
        Returns list of new comment dicts with id, author, text, url, is_patron flag.
        """
        if not self._authenticated or self._page is None:
            return []

        new_comments: List[Dict[str, Any]] = []

        try:
            await self._page.goto(TWITTER_NOTIFICATIONS_URL, timeout=20000, wait_until="domcontentloaded")
            await asyncio.sleep(random.uniform(2.0, 4.0))
            await self._random_scroll(ticks=2)

            # Extract tweet/mention items from the notifications timeline
            items = await self._page.query_selector_all('[data-testid="tweet"]')
            for item in items[:20]:  # Process top 20 notifications
                try:
                    # Extract tweet ID from embedded link
                    link_el = await item.query_selector('a[href*="/status/"]')
                    tweet_url = ""
                    tweet_id = ""
                    if link_el:
                        href = await link_el.get_attribute("href")
                        if href:
                            tweet_url = f"https://x.com{href}" if href.startswith("/") else href
                            # Extract ID from URL: /status/1234567890
                            parts = href.rstrip("/").split("/")
                            if "status" in parts:
                                idx = parts.index("status")
                                if idx + 1 < len(parts):
                                    tweet_id = parts[idx + 1]

                    if not tweet_id:
                        tweet_id = hashlib.md5(tweet_url.encode()).hexdigest()[:16]

                    if self.session_mgr.is_comment_seen(tweet_id):
                        continue

                    # Extract author
                    author_el = await item.query_selector('[data-testid="User-Name"]')
                    author = ""
                    if author_el:
                        author = (await author_el.inner_text()).strip().split("\n")[0]

                    # Extract text content
                    text_el = await item.query_selector('[data-testid="tweetText"]')
                    comment_text = ""
                    if text_el:
                        comment_text = (await text_el.inner_text()).strip()

                    comment = {
                        "id": tweet_id,
                        "author": author,
                        "text": comment_text,
                        "url": tweet_url,
                        "platform": "twitter",
                        "captured_at": datetime.now(timezone.utc).isoformat(),
                        "is_patron": False,  # Patron detection enriched downstream
                    }
                    new_comments.append(comment)
                    self.session_mgr.mark_comment_seen(tweet_id)

                except Exception as item_exc:
                    logger.debug(f"Error extracting mention item: {item_exc}")

            self.session_mgr.increment("mentions_monitored", len(new_comments))
            logger.info(f"Mentions monitor: {len(new_comments)} new mentions captured")

        except Exception as exc:
            logger.error(f"Mentions monitoring failed: {exc}", exc_info=True)

        return new_comments

    async def reply_to_comment(self, tweet_url: str, reply_text: str) -> bool:
        """
        Reply to a specific tweet.
        Enforces per-thread cooldown (MIN_COMMENT_REPLY_COOLDOWN_SEC).
        Returns True on success.
        """
        if not self._authenticated or self._page is None:
            return False

        # Per-thread cooldown
        thread_id = tweet_url.rstrip("/").split("/")[-1]
        last_reply = self._reply_cooldowns.get(thread_id, 0.0)
        if time.monotonic() - last_reply < MIN_COMMENT_REPLY_COOLDOWN_SEC:
            logger.info(f"Reply cooldown active for thread {thread_id}, skipping")
            return False

        if len(reply_text) > TWITTER_CHAR_LIMIT:
            reply_text = reply_text[: TWITTER_CHAR_LIMIT - 1] + "…"

        if self.config.dry_run:
            logger.info(f"[DRY RUN] Would reply to {tweet_url}:\n{reply_text}")
            self._reply_cooldowns[thread_id] = time.monotonic()
            self.session_mgr.increment("comments_replied")
            return True

        try:
            await self._page.goto(tweet_url, timeout=20000, wait_until="domcontentloaded")
            await asyncio.sleep(random.uniform(2.0, 4.0))
            await self._random_scroll(ticks=1)

            # Click reply button on the original tweet
            reply_btn = await self._page.wait_for_selector(
                '[data-testid="reply"], [aria-label*="Reply"], [aria-label*="Ответить"]',
                timeout=10000,
            )
            if reply_btn:
                rb_box = await reply_btn.bounding_box()
                if rb_box:
                    await self._human_move_and_click(
                        rb_box["x"] + rb_box["width"] / 2,
                        rb_box["y"] + rb_box["height"] / 2,
                    )
            await asyncio.sleep(random.uniform(1.0, 2.0))

            # Type reply in compose box
            compose = await self._page.wait_for_selector(
                '[data-testid="tweetTextarea_0"], div[role="textbox"][aria-label*="reply"]',
                timeout=10000,
            )
            if compose:
                c_box = await compose.bounding_box()
                if c_box:
                    await self._human_move_and_click(
                        c_box["x"] + c_box["width"] / 2,
                        c_box["y"] + c_box["height"] / 2,
                    )
                await asyncio.sleep(random.uniform(0.4, 0.8))
                await self._human_type(reply_text)
                await asyncio.sleep(random.uniform(0.8, 1.8))

            # Submit reply
            post_btn = await self._page.wait_for_selector(
                '[data-testid="tweetButton"], button:has-text("Reply"), button:has-text("Ответить")',
                timeout=8000,
            )
            if post_btn:
                pb_box = await post_btn.bounding_box()
                if pb_box:
                    await self._human_move_and_click(
                        pb_box["x"] + pb_box["width"] / 2,
                        pb_box["y"] + pb_box["height"] / 2,
                    )
            await asyncio.sleep(random.uniform(2.0, 4.0))

            self._reply_cooldowns[thread_id] = time.monotonic()
            self.session_mgr.increment("comments_replied")
            logger.info(f"Replied to {tweet_url} ({len(reply_text)} chars)")
            return True

        except Exception as exc:
            logger.error(f"Failed to reply to {tweet_url}: {exc}", exc_info=True)
            return False


# ==============================================================================
# 6. HTTP Healthcheck Server
# ==============================================================================


class TwitterHealthHandler(BaseHTTPRequestHandler):
    """HTTP health endpoints for Kubernetes liveness/readiness probes."""

    agent_ref: Optional["TwitterActivityAgent"] = None

    def log_message(self, fmt: str, *args: Any) -> None:  # type: ignore[override]
        pass  # Suppress default access log

    def _send_json(self, status_code: int, body: Dict[str, Any]) -> None:
        encoded = json.dumps(body).encode()
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)

    def do_GET(self) -> None:  # noqa: N802
        path = self.path.split("?")[0]
        agent = self.agent_ref

        if path in ("/healthz", "/actuator/health/liveness"):
            self._send_json(200, {"status": "UP"})

        elif path == "/actuator/health/readiness":
            ready = agent is not None and agent.is_ready()
            self._send_json(
                200 if ready else 503,
                {"status": "UP" if ready else "DOWN"},
            )

        elif path == "/actuator/health":
            info = agent.get_health_info() if agent else {"status": "UNKNOWN"}
            code = 200 if info.get("status") == "UP" else 503
            self._send_json(code, info)

        elif path == "/api/v1/twitter/status":
            status = agent.session_mgr.load_status() if agent else {}
            self._send_json(200, status)

        else:
            self._send_json(404, {"error": "not found"})


def start_health_server(agent: "TwitterActivityAgent", port: int = 8080) -> threading.Thread:
    TwitterHealthHandler.agent_ref = agent
    server = ThreadingHTTPServer(("0.0.0.0", port), TwitterHealthHandler)

    def _run() -> None:
        logger.info(f"Health server listening on :{port}")
        server.serve_forever()

    t = threading.Thread(target=_run, daemon=True)
    t.start()
    return t


# ==============================================================================
# 7. Main Activity Agent Orchestrator
# ==============================================================================


class TwitterActivityAgent:
    """
    Orchestrates the full Twitter/X automation lifecycle:
    1. Healthcheck server startup
    2. Redis session connect
    3. Browser launch (persistent Firefox, Rule 9)
    4. Cache warmup (Rule 9)
    5. Twitter login (persistent cookies, Rule 9)
    6. Main loop:
       a. Dequeue and post tweet jobs from smm:queue:twitter
       b. Monitor @mentions and escalate patron comments to feedback queue
    """

    def __init__(self, config: TwitterAgentConfig) -> None:
        self.config = config
        self.session_mgr = TwitterSessionManager(config)
        self._browser_agent: Optional[TwitterBrowserAgent] = None
        self._running = False

    def is_ready(self) -> bool:
        return (
            self._browser_agent is not None
            and self._browser_agent._authenticated
        )

    def get_health_info(self) -> Dict[str, Any]:
        status = "UP" if self.is_ready() else "STARTING"
        info: Dict[str, Any] = {
            "status": status,
            "account": self.config.twitter_handle,
            "authenticated": self._browser_agent._authenticated if self._browser_agent else False,
            "metrics": self.session_mgr.metrics,
        }
        return info

    async def _process_tweet_queue(self) -> None:
        """Dequeue and publish pending tweet jobs from Redis queue."""
        processed = 0
        while True:
            job = self.session_mgr.pop_tweet_job()
            if job is None:
                break
            job_type = job.get("type", "raw")

            if job_type == "surebet":
                text = FreebetMathHelper.generate_surebet_tweet(
                    bookmaker1=job.get("bookmaker1", "БК1"),
                    bookmaker2=job.get("bookmaker2", "БК2"),
                    event=job.get("event", "Матч"),
                    odds1=float(job.get("odds1", 2.0)),
                    odds2=float(job.get("odds2", 2.2)),
                    profit_pct=float(job.get("profit_pct", 3.5)),
                    sport=job.get("sport", "Футбол"),
                    affiliate_bm1=job.get("affiliate_bm1"),
                    affiliate_bm2=job.get("affiliate_bm2"),
                )
                success = await self._browser_agent.post_tweet(text)  # type: ignore[union-attr]
                if success:
                    self.session_mgr.increment("surebets_posted")

            elif job_type == "freebet":
                text = FreebetMathHelper.generate_freebet_tweet(
                    bookmaker=job.get("bookmaker", "БК"),
                    freebet_amount=float(job.get("freebet_amount", 1000.0)),
                    k1=float(job.get("k1", 5.0)),
                    k2=float(job.get("k2", 1.25)),
                    affiliate_id=job.get("affiliate_id"),
                )
                success = await self._browser_agent.post_tweet(text)  # type: ignore[union-attr]
                if success:
                    self.session_mgr.increment("freebets_posted")

            elif job_type == "raw":
                raw_text = job.get("text", "")
                if raw_text:
                    await self._browser_agent.post_tweet(raw_text)  # type: ignore[union-attr]

            processed += 1
            if processed >= MAX_POSTS_PER_HOUR:
                logger.info(f"Hourly post cap reached ({MAX_POSTS_PER_HOUR}), deferring remaining jobs")
                break

    async def _process_mentions(self) -> None:
        """Monitor @mentions and route patron comments to feedback queue."""
        mentions = await self._browser_agent.monitor_mentions()  # type: ignore[union-attr]
        for mention in mentions:
            # Enqueue all mentions to feedback queue for Patron CRM processing
            self.session_mgr.push_feedback(mention)
            logger.debug(f"Mention routed to feedback queue: {mention['id']} by {mention['author']}")

    async def run(self) -> None:
        """Main agent lifecycle."""
        self._running = True
        self.session_mgr.connect()
        self.session_mgr.save_status("starting")

        # Start healthcheck server
        start_health_server(self, port=self.config.healthcheck_port)

        if StealthBrowserSession is None or BrowserConfig is None:
            logger.warning(
                "StealthBrowserSession / BrowserConfig not available. "
                "Running in dry-run / mock mode only."
            )
            # Headless dry-run loop
            while self._running:
                self.session_mgr.save_status("running_mock")
                await asyncio.sleep(30)
            return

        # Build browser config (Rule 9: persistent context, Rule 6+12: US proxy)
        browser_cfg = BrowserConfig(
            account_id=self.config.account_id,
            proxy_server=self.config.proxy_server,
            headless=self.config.headless,
            locale="en-US",
            timezone_id="America/Los_Angeles",
            redis_url=self.config.redis_url,
        )

        async with StealthBrowserSession(browser_cfg) as session:
            self._browser_agent = TwitterBrowserAgent(self.config, self.session_mgr)
            self._browser_agent._page = session.page

            # Rule 9: Cache warmup BEFORE any Twitter interaction
            await self._browser_agent.perform_cache_warmup()

            # Authenticate
            authenticated = await self._browser_agent.login()
            if not authenticated:
                logger.error("Twitter login failed — agent entering retry loop")
                retry_count = 0
                while not authenticated and retry_count < 3 and self._running:
                    await asyncio.sleep(60)
                    authenticated = await self._browser_agent.login()
                    retry_count += 1
                if not authenticated:
                    self.session_mgr.save_status("auth_failed")
                    logger.error("Twitter: all login attempts exhausted, shutting down")
                    return

            self.session_mgr.save_status("running")
            logger.info(f"Twitter agent RUNNING as {self.config.twitter_handle}")

            last_monitor_ts = 0.0

            while self._running:
                try:
                    # Process pending tweet jobs from Redis queue
                    await self._process_tweet_queue()

                    # Periodic mention monitoring
                    now = time.monotonic()
                    if now - last_monitor_ts >= self.config.monitor_interval_sec:
                        await self._process_mentions()
                        last_monitor_ts = now

                    self.session_mgr.save_status("running")
                    await asyncio.sleep(random.uniform(25, 45))

                except Exception as loop_exc:
                    logger.error(f"Main loop error: {loop_exc}", exc_info=True)
                    self.session_mgr.save_status("error")
                    await asyncio.sleep(60)

        logger.info("Twitter agent stopped")
        self.session_mgr.save_status("stopped")


# ==============================================================================
# 8. CLI Entry Point
# ==============================================================================


def main() -> None:
    parser = argparse.ArgumentParser(
        description="SmartBet.guru Twitter/X Automation Agent (@smartbetguru)"
    )
    parser.add_argument(
        "--account-id",
        default=os.getenv("ACCOUNT_ID", "smartbetguru_twitter"),
        help="Account identifier (used for Redis profile key)",
    )
    parser.add_argument(
        "--headless",
        action="store_true",
        default=os.getenv("HEADLESS", "false").lower() == "true",
        help="Run browser in headless mode",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        default=os.getenv("DRY_RUN", "false").lower() == "true",
        help="Simulate all actions without real posting",
    )
    parser.add_argument(
        "--proxy",
        default=os.getenv("TWITTER_PROXY", os.getenv("US_PROXY", "http://purevpn-us-ca.proxy:3128")),
        help="HTTP proxy for Twitter (Rule 6 + 12: dedicated static US-CA proxy)",
    )
    parser.add_argument(
        "--port",
        type=int,
        default=int(os.getenv("PORT", "8080")),
        help="Healthcheck HTTP port",
    )
    args = parser.parse_args()

    config = TwitterAgentConfig(
        account_id=args.account_id,
        headless=args.headless,
        dry_run=args.dry_run,
        proxy_server=args.proxy,
        healthcheck_port=args.port,
    )

    agent = TwitterActivityAgent(config)
    asyncio.run(agent.run())


if __name__ == "__main__":
    main()
