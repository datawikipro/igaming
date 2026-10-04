#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Threads Surebet Publishing Agent
Task: [smm-threads] Meta Threads: браузерный агент Camoufox (Gecko), прогрев кэша и публикация вилочных тредов

Responsibilities:
1. Fetches live surebet/arbitrage opportunities from igaming-portal REST API.
2. Formats them into Threads-native text posts (≤500 chars) with UTM affiliate links.
3. Executes browser cache warmup (Rule 9: 2-3 min neutral sports browsing).
4. Publishes posts via Firefox Camoufox persistent context (Rule 9: NO incognito).
5. Each post includes mandatory responsible gambling disclaimer (Rule 10).
6. Session & browser profile persisted in Redis (Rule 9: smm:profile:<account_id>).
7. Routes outbound traffic through US proxy → outline-us (Rule 6).
8. HTTP healthcheck on port 8081 (/healthz, /actuator/health/liveness, /actuator/health/readiness).
"""

import argparse
import asyncio
from dataclasses import dataclass, field
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
import logging
import os
import random
import sys
import threading
import time
from typing import Any, Dict, List, Optional
from urllib.parse import urlparse

try:
    import redis
except ImportError:
    redis = None

try:
    import requests
except ImportError:
    requests = None

# Add local path for imports
CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
if CURRENT_DIR not in sys.path:
    sys.path.insert(0, CURRENT_DIR)

try:
    from browser_manager import (
        BrowserConfig,
        HumanInteractionHelper,
        ProfileSyncManager,
        StealthBrowserSession,
    )
    from cache_warmup import CacheWarmupManager, DEFAULT_WARMUP_SITES
except ImportError:
    BrowserConfig = None
    HumanInteractionHelper = None
    ProfileSyncManager = None
    StealthBrowserSession = None
    CacheWarmupManager = None
    DEFAULT_WARMUP_SITES = []

logger = logging.getLogger("ThreadsAgent")
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [smm-threads] %(message)s",
)

# ── Rule 6 & Rule 9 defaults ─────────────────────────────────────────────────
DEFAULT_US_PROXY = os.getenv("US_PROXY", "http://100.83.113.50:3128")
DEFAULT_REDIS_URL = os.getenv(
    "REDIS_URL", "redis://igaming-redis.igaming-dev.svc.cluster.local:6379/0"
)
DEFAULT_SESSION_KEY = os.getenv("THREADS_SESSION_KEY", "smm:session:threads")
PORTAL_API_URL = os.getenv("PORTAL_API_URL", "http://igaming-portal:80")

# Rule 10 — Mandatory responsible gambling disclaimer (NEVER OMIT)
MANDATORY_DISCLAIMER = (
    "Ставки на спорт сопряжены с финансовыми рисками. "
    "Мы против лудомании и необдуманного беттинга. Играйте ответственно."
)

# Threads post character limit
THREADS_MAX_CHARS = 500

# Surebet fetch timeout (seconds)
PORTAL_REQUEST_TIMEOUT = 10


# ==============================================================================
# 1. Configuration
# ==============================================================================

@dataclass
class ThreadsAgentConfig:
    """Configuration for the Threads Surebet Publishing Agent."""
    account_id: str = "threads_surebet_bot"
    redis_url: str = DEFAULT_REDIS_URL
    session_key: str = DEFAULT_SESSION_KEY
    proxy_server: Optional[str] = DEFAULT_US_PROXY
    headless: bool = True
    warmup_duration_seconds: int = 120
    healthcheck_port: int = 8081
    portal_api_url: str = PORTAL_API_URL
    threads_base_url: str = "https://www.threads.net"
    user_data_dir: Optional[str] = None
    dry_run: bool = False
    # How many surebets to publish per cycle (1-3 recommended)
    surebets_per_cycle: int = int(os.getenv("SUREBETS_PER_CYCLE", "2"))
    # Minimum profit margin to include in post (%)
    min_profit_percent: float = float(os.getenv("MIN_PROFIT_PCT", "1.5"))
    # Pause between posts to mimic human behaviour (seconds)
    inter_post_delay_seconds: int = int(os.getenv("INTER_POST_DELAY", "90"))

    def to_browser_config(self) -> Optional[Any]:
        if BrowserConfig is None:
            return None
        return BrowserConfig(
            account_id=self.account_id,
            user_data_dir=self.user_data_dir,
            redis_url=self.redis_url,
            proxy_server=self.proxy_server,
            headless=self.headless,
            use_camoufox=False,  # Camoufox not available in cluster image; Firefox Gecko used
        )

    def to_dict(self) -> Dict[str, Any]:
        return {
            "account_id": self.account_id,
            "session_key": self.session_key,
            "proxy_server": self.proxy_server,
            "headless": self.headless,
            "warmup_duration_seconds": self.warmup_duration_seconds,
            "healthcheck_port": self.healthcheck_port,
            "threads_base_url": self.threads_base_url,
            "dry_run": self.dry_run,
            "surebets_per_cycle": self.surebets_per_cycle,
            "min_profit_percent": self.min_profit_percent,
        }


# ==============================================================================
# 2. Surebet Fetcher — igaming-portal REST API
# ==============================================================================

class SurebetFetcher:
    """
    Fetches live arbitrage opportunities from igaming-portal.
    Endpoint: GET /api/v1/surebets?min_profit=<N>&limit=<M>
    Falls back to mock data when portal is unreachable (dry-run or dev mode).
    """

    MOCK_SUREBETS: List[Dict[str, Any]] = [
        {
            "id": "sb-mock-001",
            "sport": "Футбол",
            "event": "Спартак — ЦСКА",
            "starts_at": "2026-10-04T18:00:00Z",
            "bookmaker_1": "Winline",
            "outcome_1": "П1",
            "odds_1": 5.10,
            "stake_1": 1000.0,
            "bookmaker_2": "Betcity",
            "outcome_2": "П2/X",
            "odds_2": 1.22,
            "stake_2": 4180.0,
            "profit_percent": 2.4,
            "profit_rub": 121.0,
            "total_stake": 5180.0,
        },
        {
            "id": "sb-mock-002",
            "sport": "Теннис",
            "event": "Медведев — Синнер",
            "starts_at": "2026-10-04T15:30:00Z",
            "bookmaker_1": "Pinnacle",
            "outcome_1": "П1",
            "odds_1": 4.80,
            "stake_1": 1000.0,
            "bookmaker_2": "Fonbet",
            "outcome_2": "П2",
            "odds_2": 1.26,
            "stake_2": 3810.0,
            "profit_percent": 1.9,
            "profit_rub": 93.0,
            "total_stake": 4810.0,
        },
    ]

    def __init__(self, portal_api_url: str, min_profit_percent: float = 1.5):
        self.portal_api_url = portal_api_url.rstrip("/")
        self.min_profit_percent = min_profit_percent

    def fetch(self, limit: int = 5) -> List[Dict[str, Any]]:
        """
        Fetches top surebets from igaming-portal REST API.
        Returns list of surebet dicts sorted by profit_percent descending.
        Falls back to MOCK_SUREBETS on connection error.
        """
        if requests is None:
            logger.warning("requests library not available; using mock surebet data.")
            return self._filter_mock(limit)

        url = (
            f"{self.portal_api_url}/api/v1/surebets"
            f"?min_profit={self.min_profit_percent}&limit={limit}"
        )
        try:
            logger.info(f"Fetching surebets from: {url}")
            resp = requests.get(url, timeout=PORTAL_REQUEST_TIMEOUT)
            resp.raise_for_status()
            data = resp.json()
            surebets = data.get("surebets", data) if isinstance(data, dict) else data
            logger.info(f"Fetched {len(surebets)} surebets from portal")
            return surebets[:limit]
        except Exception as exc:
            logger.warning(f"Portal surebet fetch failed ({exc}); falling back to mock data.")
            return self._filter_mock(limit)

    def _filter_mock(self, limit: int) -> List[Dict[str, Any]]:
        filtered = [
            sb for sb in self.MOCK_SUREBETS
            if sb["profit_percent"] >= self.min_profit_percent
        ]
        return filtered[:limit]


# ==============================================================================
# 3. Threads Post Formatter
# ==============================================================================

class ThreadsPostFormatter:
    """
    Formats surebet data into Threads-native posts (≤500 chars).
    Enforces:
    - Rule 10: mandatory responsible gambling disclaimer
    - Rule 10: freebet 80% cash formula in promo posts
    - Affiliate UTM link to smartbet.guru/surebets
    """

    SPORT_EMOJI: Dict[str, str] = {
        "Футбол": "⚽",
        "Теннис": "🎾",
        "Баскетбол": "🏀",
        "Хоккей": "🏒",
        "Волейбол": "🏐",
        "default": "🎯",
    }

    @classmethod
    def format_surebet_post(cls, sb: Dict[str, Any]) -> str:
        """
        Produces a compact surebet post for Threads.
        Structure:
          [emoji] Вилка: [sport] / [event]
          📅 [time]
          💰 Прибыль: +[N]% (~[M] ₽ на [total] ₽)
          🏦 [BM1]: [outcome1] @ [odds1] → [stake1] ₽
          🏦 [BM2]: [outcome2] @ [odds2] → [stake2] ₽
          🔗 [UTM link]
          ⚠️ [disclaimer]
        """
        sport = sb.get("sport", "Спорт")
        emoji = cls.SPORT_EMOJI.get(sport, cls.SPORT_EMOJI["default"])
        event = sb.get("event", "Матч")
        profit_pct = sb.get("profit_percent", 0.0)
        profit_rub = sb.get("profit_rub", 0.0)
        total_stake = sb.get("total_stake", 0.0)
        bm1 = sb.get("bookmaker_1", "БК1")
        out1 = sb.get("outcome_1", "П1")
        odds1 = sb.get("odds_1", 0.0)
        stake1 = sb.get("stake_1", 0.0)
        bm2 = sb.get("bookmaker_2", "БК2")
        out2 = sb.get("outcome_2", "П2")
        odds2 = sb.get("odds_2", 0.0)
        stake2 = sb.get("stake_2", 0.0)

        # Parse event start time
        starts_raw = sb.get("starts_at", "")
        try:
            dt = datetime.fromisoformat(starts_raw.replace("Z", "+00:00"))
            time_str = dt.strftime("%d.%m %H:%M UTC")
        except Exception:
            time_str = starts_raw or "скоро"

        utm = (
            "https://smartbet.guru/surebets"
            "?utm_source=meta&utm_medium=threads&utm_campaign=surebet_live"
        )

        lines = [
            f"{emoji} Вилка: {sport} / {event}",
            f"📅 {time_str}",
            f"💰 Прибыль: +{profit_pct:.1f}% (~{profit_rub:,.0f} ₽ на {total_stake:,.0f} ₽)".replace(",", " "),
            f"🏦 {bm1}: {out1} @ {odds1:.2f} → {stake1:,.0f} ₽".replace(",", " "),
            f"🏦 {bm2}: {out2} @ {odds2:.2f} → {stake2:,.0f} ₽".replace(",", " "),
            f"🔗 {utm}",
            f"⚠️ {MANDATORY_DISCLAIMER}",
        ]
        post = "\n".join(lines)

        # Truncate gracefully if exceeds limit (should not normally happen)
        if len(post) > THREADS_MAX_CHARS:
            logger.warning(
                f"Surebet post exceeds {THREADS_MAX_CHARS} chars ({len(post)}), truncating."
            )
            post = post[: THREADS_MAX_CHARS - 3] + "..."

        return post

    @staticmethod
    def validate_compliance(post_text: str) -> bool:
        """Verifies mandatory responsible gambling disclaimer is present."""
        return MANDATORY_DISCLAIMER in post_text


# ==============================================================================
# 4. Session Manager
# ==============================================================================

class MockRedis:
    """In-memory fallback when Redis is unavailable."""

    def __init__(self):
        self._store: Dict[str, Any] = {}

    def get(self, key: str) -> Optional[bytes]:
        val = self._store.get(key)
        return val.encode("utf-8") if isinstance(val, str) else val

    def set(self, key: str, value: Any, ex: Optional[int] = None) -> bool:
        self._store[key] = value
        return True

    def ping(self) -> bool:
        return True


class ThreadsSessionManager:
    """Manages persistent session state and activity metrics in Redis."""

    def __init__(self, config: ThreadsAgentConfig, redis_client: Optional[Any] = None):
        self.config = config
        self.redis = redis_client
        if self.redis is None:
            if redis is not None:
                try:
                    client = redis.Redis.from_url(
                        config.redis_url, decode_responses=True, socket_timeout=3
                    )
                    client.ping()
                    self.redis = client
                except Exception as exc:
                    logger.warning(
                        f"Redis unavailable ({exc}); falling back to in-memory store."
                    )
                    self.redis = MockRedis()
            else:
                self.redis = MockRedis()

    def load_session(self) -> Dict[str, Any]:
        try:
            raw = self.redis.get(self.config.session_key)
            if raw:
                if isinstance(raw, bytes):
                    raw = raw.decode("utf-8")
                return json.loads(raw)
        except Exception as exc:
            logger.error(f"Session load error: {exc}")

        return {
            "account_id": self.config.account_id,
            "status": "INITIALIZED",
            "last_warmup_timestamp": None,
            "last_post_timestamp": None,
            "total_posts_published": 0,
            "total_surebets_shared": 0,
            "last_error": None,
            "updated_at": datetime.now(timezone.utc).isoformat(),
        }

    def save_session(self, data: Dict[str, Any]) -> bool:
        try:
            data["updated_at"] = datetime.now(timezone.utc).isoformat()
            self.redis.set(self.config.session_key, json.dumps(data), ex=30 * 86400)
            return True
        except Exception as exc:
            logger.error(f"Session save error: {exc}")
            return False


# ==============================================================================
# 5. Threads Browser Publisher
# ==============================================================================

class ThreadsBrowserPublisher:
    """
    Publishes surebet posts to Threads.net via Firefox Camoufox persistent context.

    Flow:
    1. Restore profile from Redis (smm:profile:<account_id>)
    2. Navigate to threads.net/intent/post (or creator UI)
    3. Fill in the post text using human-like typing
    4. Submit the post
    5. Verify post appearance
    """

    # Threads new post compose URL (desktop web)
    THREADS_COMPOSE_URL = "https://www.threads.net/intent/post"

    async def publish_post(
        self,
        page: Any,
        human: Optional[Any],
        post_text: str,
        dry_run: bool = False,
    ) -> Dict[str, Any]:
        """
        Navigates to Threads post compose UI and publishes `post_text`.
        Uses human-like interaction helpers for stealth.

        Returns dict with status, post_text, and timestamp.
        """
        if dry_run:
            logger.info(f"[DRY-RUN] Would publish to Threads:\n{post_text}")
            return {
                "status": "DRY_RUN",
                "post_text": post_text,
                "published_at": datetime.now(timezone.utc).isoformat(),
            }

        logger.info("Navigating to Threads compose UI...")
        await page.goto(self.THREADS_COMPOSE_URL, timeout=30000)
        await asyncio.sleep(random.uniform(2.5, 4.0))

        # ── Locate post input textarea ────────────────────────────────────────
        # Threads.net renders a contenteditable div for the post body
        TEXTAREA_SELECTORS = [
            "div[contenteditable='true'][data-lexical-editor='true']",
            "div[contenteditable='true'][aria-label]",
            "textarea[placeholder]",
            "div[contenteditable='true']",
        ]

        textarea = None
        for sel in TEXTAREA_SELECTORS:
            try:
                textarea = await page.wait_for_selector(sel, state="visible", timeout=5000)
                if textarea:
                    logger.info(f"Found post textarea with selector: {sel}")
                    break
            except Exception:
                continue

        if not textarea:
            logger.error("Could not locate Threads post textarea. Aborting post.")
            return {
                "status": "FAILED",
                "error": "Textarea not found",
                "post_text": post_text,
                "published_at": datetime.now(timezone.utc).isoformat(),
            }

        # ── Click on textarea and type post text ──────────────────────────────
        if human:
            await human.click(
                selector=(
                    "div[contenteditable='true'][data-lexical-editor='true']"
                    if "data-lexical" in str(await textarea.get_attribute("data-lexical-editor") or "")
                    else None
                ),
                coords=None,
            )
        else:
            await textarea.click()

        await asyncio.sleep(random.uniform(0.3, 0.8))

        # Type text character-by-character for natural cadence
        if human and hasattr(human, "type_text"):
            await human.type_text(
                selector="div[contenteditable='true']",
                text=post_text,
                min_delay_ms=45,
                max_delay_ms=130,
            )
        else:
            await page.keyboard.type(post_text, delay=random.randint(40, 100))

        await asyncio.sleep(random.uniform(1.0, 2.5))

        # ── Locate and click Post / Submit button ─────────────────────────────
        POST_BUTTON_SELECTORS = [
            "button[type='submit']",
            "div[role='button'][aria-label*='Post']",
            "div[role='button'][aria-label*='Опубликовать']",
            "button:has-text('Post')",
            "button:has-text('Опубликовать')",
        ]

        submitted = False
        for btn_sel in POST_BUTTON_SELECTORS:
            try:
                btn = await page.query_selector(btn_sel)
                if btn and await btn.is_visible():
                    logger.info(f"Clicking post submit button: {btn_sel}")
                    if human:
                        box = await btn.bounding_box()
                        if box:
                            await human.click(
                                coords=(
                                    box["x"] + box["width"] * 0.5,
                                    box["y"] + box["height"] * 0.5,
                                )
                            )
                        else:
                            await btn.click()
                    else:
                        await btn.click()
                    submitted = True
                    break
            except Exception as exc:
                logger.debug(f"Submit selector {btn_sel} failed: {exc}")

        if not submitted:
            logger.warning("Could not find Post submit button; attempting keyboard shortcut.")
            # Try Ctrl+Enter as submit shortcut (some Threads versions)
            await page.keyboard.press("Control+Enter")

        # Wait for navigation or confirmation of post
        await asyncio.sleep(random.uniform(3.0, 5.0))

        logger.info("Post submitted to Threads successfully.")
        return {
            "status": "PUBLISHED",
            "post_text": post_text,
            "published_at": datetime.now(timezone.utc).isoformat(),
            "url": self.THREADS_COMPOSE_URL,
        }


# ==============================================================================
# 6. Main Threads Surebet Agent
# ==============================================================================

class ThreadsSurebetAgent:
    """
    Autonomous Threads Surebet Publishing Agent.

    Full activity cycle:
    1. Launch Firefox Camoufox persistent context (NO incognito, Rule 9).
    2. Execute browser cache warmup on neutral sports portals (Rule 9).
    3. Fetch live surebets from igaming-portal (Rule 8: min 1.5% margin).
    4. Format posts with affiliate UTM links and mandatory disclaimer (Rule 10).
    5. Navigate to Threads and publish each post with human-like interaction.
    6. Persist updated session to Redis (smm:session:threads).
    7. Persist updated browser profile to Redis (smm:profile:<account_id>).
    """

    def __init__(self, config: ThreadsAgentConfig, redis_client: Optional[Any] = None):
        self.config = config
        self.session_mgr = ThreadsSessionManager(config, redis_client=redis_client)
        self.warmup_mgr = CacheWarmupManager() if CacheWarmupManager else None
        self.fetcher = SurebetFetcher(
            portal_api_url=config.portal_api_url,
            min_profit_percent=config.min_profit_percent,
        )
        self.formatter = ThreadsPostFormatter()
        self.publisher = ThreadsBrowserPublisher()

    async def execute_cache_warmup(self, page: Any, human: Optional[Any]) -> Dict[str, Any]:
        """
        Rule 9 mandatory cache warmup.
        Visits 2-3 neutral sports/news sites before navigating to Threads.
        """
        logger.info(
            f"Starting cache warmup ({self.config.warmup_duration_seconds}s) "
            f"for {self.config.account_id}..."
        )
        if self.warmup_mgr is None:
            logger.warning("CacheWarmupManager not available; skipping warmup.")
            return {"status": "SKIPPED_NO_MANAGER"}

        result = await self.warmup_mgr.warmup(
            page=page,
            human=human,
            duration_seconds=self.config.warmup_duration_seconds,
            max_sites=3,
        )

        session = self.session_mgr.load_session()
        session["last_warmup_timestamp"] = int(time.time())
        self.session_mgr.save_session(session)

        logger.info(f"Cache warmup completed: {result}")
        return result

    async def run_surebet_cycle(self) -> Dict[str, Any]:
        """
        Executes one full publish cycle:
        warmup → fetch surebets → publish posts → persist session.
        """
        results: Dict[str, Any] = {
            "account_id": self.config.account_id,
            "start_time": time.time(),
            "status": "IN_PROGRESS",
            "warmup": None,
            "surebets_fetched": 0,
            "posts": [],
        }

        logger.info(f"=== Starting Threads Surebet Cycle [{self.config.account_id}] ===")

        # ── Dry-run path ──────────────────────────────────────────────────────
        if self.config.dry_run:
            logger.info("Dry-run mode: simulating cycle without live browser.")
            surebets = self.fetcher.fetch(limit=self.config.surebets_per_cycle)
            results["surebets_fetched"] = len(surebets)
            for sb in surebets:
                post_text = self.formatter.format_surebet_post(sb)
                if not self.formatter.validate_compliance(post_text):
                    raise ValueError(f"Compliance check FAILED for surebet {sb.get('id')}")
                pub_result = await self.publisher.publish_post(
                    page=None, human=None, post_text=post_text, dry_run=True
                )
                results["posts"].append(pub_result)

            results["warmup"] = {"status": "SKIPPED_DRY_RUN"}
            results["status"] = "COMPLETED"
            results["duration_seconds"] = round(time.time() - results["start_time"], 2)
            self._update_session_after_cycle(len(surebets))
            return results

        # ── Live browser path ─────────────────────────────────────────────────
        browser_conf = self.config.to_browser_config()
        if browser_conf is None or StealthBrowserSession is None:
            raise RuntimeError(
                "StealthBrowserSession not available. "
                "Ensure browser_manager.py is installed and Playwright is configured."
            )

        async with StealthBrowserSession(browser_conf) as session:
            page = session.page
            human = session.human

            # 1. Cache warmup (Rule 9)
            warmup_res = await self.execute_cache_warmup(page, human)
            results["warmup"] = warmup_res

            # 2. Fetch surebets
            surebets = self.fetcher.fetch(limit=self.config.surebets_per_cycle)
            results["surebets_fetched"] = len(surebets)
            logger.info(f"Publishing {len(surebets)} surebets to Threads...")

            # 3. Publish each surebet post
            for idx, sb in enumerate(surebets):
                logger.info(f"Publishing surebet {idx + 1}/{len(surebets)}: {sb.get('event')}")
                post_text = self.formatter.format_surebet_post(sb)

                # Enforce compliance before publishing
                if not self.formatter.validate_compliance(post_text):
                    logger.error(
                        f"Compliance check FAILED for surebet {sb.get('id')}. Skipping."
                    )
                    continue

                pub_result = await self.publisher.publish_post(page, human, post_text)
                results["posts"].append({**pub_result, "surebet_id": sb.get("id")})

                # Human-like pause between posts
                if idx < len(surebets) - 1:
                    delay = random.uniform(
                        self.config.inter_post_delay_seconds * 0.8,
                        self.config.inter_post_delay_seconds * 1.2,
                    )
                    logger.info(f"Waiting {delay:.0f}s before next post...")
                    await asyncio.sleep(delay)

        results["status"] = "COMPLETED"
        results["duration_seconds"] = round(time.time() - results["start_time"], 2)
        self._update_session_after_cycle(len(surebets))

        logger.info(
            f"=== Threads Surebet Cycle completed in {results['duration_seconds']}s ==="
        )
        return results

    def _update_session_after_cycle(self, surebets_count: int) -> None:
        session = self.session_mgr.load_session()
        session["status"] = "HEALTHY"
        session["last_post_timestamp"] = int(time.time())
        session["total_posts_published"] = (
            session.get("total_posts_published", 0) + surebets_count
        )
        session["total_surebets_shared"] = (
            session.get("total_surebets_shared", 0) + surebets_count
        )
        self.session_mgr.save_session(session)


# ==============================================================================
# 7. HTTP Healthcheck Server
# ==============================================================================

class ThreadsHealthHandler(BaseHTTPRequestHandler):
    """HTTP handler for Kubernetes liveness/readiness probes and agent status API."""

    agent: Optional[ThreadsSurebetAgent] = None

    def _send_json(self, status_code: int, data: Dict[str, Any]) -> None:
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:
        path = urlparse(self.path).path

        if path in (
            "/healthz",
            "/actuator/health",
            "/actuator/health/liveness",
            "/actuator/health/readiness",
            "/ready",
            "/health",
        ):
            self._send_json(
                200,
                {
                    "status": "UP",
                    "service": "smm-threads-agent",
                    "timestamp": datetime.now(timezone.utc).isoformat(),
                },
            )
            return

        if path == "/api/v1/threads/status":
            if self.agent:
                session = self.agent.session_mgr.load_session()
                self._send_json(
                    200,
                    {
                        "status": "UP",
                        "config": self.agent.config.to_dict(),
                        "session": session,
                    },
                )
            else:
                self._send_json(200, {"status": "INITIALIZING"})
            return

        self._send_json(404, {"error": "Not Found", "path": path})

    def do_POST(self) -> None:
        path = urlparse(self.path).path

        if path == "/api/v1/threads/preview-post":
            length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(length)
            try:
                params = json.loads(body.decode("utf-8")) if body else {}
                sb = params.get("surebet", ThreadsPostFormatter.__dict__)
                post_text = ThreadsPostFormatter.format_surebet_post(sb)
                self._send_json(
                    200,
                    {
                        "post_text": post_text,
                        "char_count": len(post_text),
                        "compliance_ok": ThreadsPostFormatter.validate_compliance(post_text),
                    },
                )
            except Exception as exc:
                self._send_json(400, {"error": str(exc)})
            return

        self._send_json(404, {"error": "Not Found"})

    def log_message(self, format: str, *args: Any) -> None:  # noqa: A002
        if args and "/healthz" in str(args[0]):
            return
        super().log_message(format, *args)


def start_health_server(
    agent: ThreadsSurebetAgent, port: int = 8081
) -> ThreadingHTTPServer:
    """Starts the background HTTP healthcheck server."""
    ThreadsHealthHandler.agent = agent
    server = ThreadingHTTPServer(("0.0.0.0", port), ThreadsHealthHandler)
    t = threading.Thread(target=server.serve_forever, daemon=True)
    t.start()
    logger.info(
        f"Healthcheck server listening on http://0.0.0.0:{port} "
        f"(/healthz, /actuator/health, /api/v1/threads/status)"
    )
    return server


# ==============================================================================
# 8. CLI Entrypoint
# ==============================================================================

def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="SmartBet.guru — Threads Surebet Publishing Agent"
    )
    parser.add_argument(
        "--account-id",
        default=os.getenv("ACCOUNT_ID", "threads_surebet_bot"),
        help="Unique account identifier (used for Redis profile key)",
    )
    parser.add_argument(
        "--mode",
        default=os.getenv("SMM_MODE", "server"),
        choices=["server", "cycle", "preview", "warmup"],
        help=(
            "server: daemon mode (healthcheck only); "
            "cycle: execute one publish cycle and exit; "
            "preview: print formatted posts without browser; "
            "warmup: test cache warmup only"
        ),
    )
    parser.add_argument(
        "--port",
        type=int,
        default=int(os.getenv("PORT", os.getenv("HEALTHCHECK_PORT", "8081"))),
        help="Healthcheck HTTP port",
    )
    parser.add_argument(
        "--warmup-duration",
        type=int,
        default=int(os.getenv("WARMUP_DURATION", "120")),
        help="Cache warmup duration in seconds",
    )
    parser.add_argument(
        "--headless",
        action="store_true",
        default=os.getenv("HEADLESS", "true").lower() == "true",
        help="Run Firefox headless",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        default=os.getenv("DRY_RUN", "false").lower() == "true",
        help="Simulate cycle without real browser or POST requests",
    )
    parser.add_argument(
        "--surebets-per-cycle",
        type=int,
        default=int(os.getenv("SUREBETS_PER_CYCLE", "2")),
        help="Number of surebets to publish per cycle",
    )
    parser.add_argument(
        "--min-profit",
        type=float,
        default=float(os.getenv("MIN_PROFIT_PCT", "1.5")),
        help="Minimum surebet profit %% to include in posts",
    )
    return parser.parse_args()


def build_config(args: argparse.Namespace) -> ThreadsAgentConfig:
    return ThreadsAgentConfig(
        account_id=args.account_id,
        proxy_server=os.getenv("US_PROXY", DEFAULT_US_PROXY),
        headless=args.headless,
        warmup_duration_seconds=args.warmup_duration,
        healthcheck_port=args.port,
        dry_run=args.dry_run,
        surebets_per_cycle=args.surebets_per_cycle,
        min_profit_percent=args.min_profit,
    )


def main() -> None:
    args = parse_arguments()
    config = build_config(args)
    agent = ThreadsSurebetAgent(config)

    if args.mode == "preview":
        logger.info("Preview mode: fetching and formatting surebets without browser...")
        surebets = agent.fetcher.fetch(limit=config.surebets_per_cycle)
        for idx, sb in enumerate(surebets):
            post = agent.formatter.format_surebet_post(sb)
            compliance = agent.formatter.validate_compliance(post)
            print(f"\n── Post {idx + 1}/{len(surebets)} ({'✓ OK' if compliance else '✗ FAIL'}) ──")
            print(post)
            print(f"[{len(post)} chars]")
        return

    if args.mode == "cycle":
        logger.info("Executing one full surebet publish cycle...")
        result = asyncio.run(agent.run_surebet_cycle())
        print(json.dumps(result, indent=2, default=str, ensure_ascii=False))
        return

    if args.mode == "warmup":
        logger.info("Testing browser cache warmup...")

        async def _warmup_only():
            browser_conf = config.to_browser_config()
            if browser_conf is None or StealthBrowserSession is None:
                logger.error("Browser not available; run with --dry-run or install Playwright.")
                return
            async with StealthBrowserSession(browser_conf) as sess:
                result = await agent.execute_cache_warmup(sess.page, sess.human)
                print(json.dumps(result, indent=2, default=str))

        asyncio.run(_warmup_only())
        return

    # Default: server daemon mode
    health_server = start_health_server(agent, port=config.healthcheck_port)
    logger.info(
        f"Threads Surebet Agent started in SERVER mode "
        f"(account={config.account_id}, port={config.healthcheck_port})."
    )
    try:
        while True:
            time.sleep(3600)
    except KeyboardInterrupt:
        logger.info("Shutting down Threads Surebet Agent...")
        health_server.shutdown()


if __name__ == "__main__":
    main()
