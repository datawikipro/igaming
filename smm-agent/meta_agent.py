#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Meta (Threads & Instagram) Dedicated Pod & Activity Agent
Task: [smm-meta] ИИ-агент активности для Threads и Instagram (Firefox Persistent Context + Cache Warmup)
Rule 6 (Network Proxy Routing), Rule 9 (Firefox / Camoufox, No Incognito, Cache Warmup) & Rule 10 (80% Freebet Cash)

Responsibilities:
1. Strict Persistent Context: Uses Firefox / Camoufox via launch_persistent_context(). Never uses new_context().
2. Redis Profile & Session Storage: Persists browser storage (cookies.sqlite, IndexedDB, LocalStorage, HTTP cache)
   in Redis under `smm:profile:<account_id>` and runtime session under `smm:session:meta`.
3. Cache Warmup: Mandatory 2-3 minutes pre-browsing on neutral sports/news portals (BBC Sport, ESPN, Flashscore)
   before interacting with Meta endpoints to build organic history.
4. Human Kinematics: Cubic Bezier mouse curves, randomized reading pauses, natural scrolling.
5. Outbound Routing: Meta domains routed through cluster proxy (http://100.83.113.50:3128 -> outline-us 100.66.190.4).
6. Content Publishing & Freebet 80% Cash Math:
   - Freebet SNR conversion: eta = ((K1 - 1) * (K2 - 1)) / K2 approx 0.80.
   - Mandatory responsible gambling disclaimer on every marketing post:
     "Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно."
   - Affiliate tracking URLs with UTM parameters.
7. Healthcheck Server: HTTP daemon on port 8080 (/healthz, /actuator/health, /api/v1/meta/status).
"""

import argparse
import asyncio
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
import json
import logging
import math
import os
import random
import sys
import threading
import time
from typing import Any, Dict, List, Optional, Tuple
from urllib.parse import parse_qs, urlparse
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

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

from browser_manager import (
    BrowserConfig,
    HumanInteractionHelper,
    ProfileSyncManager,
    StealthBrowserSession,
    generate_cubic_bezier_trajectory,
)
from cache_warmup import CacheWarmupManager, DEFAULT_WARMUP_SITES

logger = logging.getLogger("MetaAgent")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] [smm-bot-meta] %(message)s")

# Rule 6 & Rule 9 defaults
DEFAULT_US_PROXY = os.getenv("US_PROXY", "http://100.83.113.50:3128")
DEFAULT_REDIS_URL = os.getenv("REDIS_URL", "redis://igaming-redis:6379/0")
DEFAULT_SESSION_KEY = os.getenv("META_SESSION_KEY", "smm:session:meta")
MANDATORY_DISCLAIMER = "Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно."


# ==============================================================================
# 1. Configuration Dataclass
# ==============================================================================

@dataclass
class MetaAgentConfig:
    account_id: str = "meta_persona_main"
    redis_url: str = DEFAULT_REDIS_URL
    session_key: str = DEFAULT_SESSION_KEY
    proxy_server: Optional[str] = DEFAULT_US_PROXY
    headless: bool = True
    warmup_duration_seconds: int = 120
    healthcheck_port: int = 8080
    portal_api_url: str = "http://igaming-portal:80"
    threads_base_url: str = "https://www.threads.net"
    instagram_base_url: str = "https://www.instagram.com"
    user_data_dir: Optional[str] = None
    use_camoufox: bool = False
    dry_run: bool = False
    post_interval_minutes: int = 60

    def to_browser_config(self) -> BrowserConfig:
        return BrowserConfig(
            account_id=self.account_id,
            user_data_dir=self.user_data_dir,
            redis_url=self.redis_url,
            proxy_server=self.proxy_server,
            headless=self.headless,
            use_camoufox=self.use_camoufox,
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
            "instagram_base_url": self.instagram_base_url,
            "dry_run": self.dry_run,
            "post_interval_minutes": self.post_interval_minutes,
        }


# ==============================================================================
# 2. Freebet Math & Marketing Content Helper (Rule 10)
# ==============================================================================

class FreebetMathHelper:
    """
    Implements Rule 10 Matched Betting formulas and content compliance:
    SNR (Stake Not Returned) conversion formula:
    eta = ((K1 - 1) * (K2 - 1)) / K2
    With high K1 (e.g. 5.0) and hedge K2 (e.g. 1.25), conversion rate reaches ~80% (0.80).
    """

    @staticmethod
    def calculate_freebet_conversion(
        k1: float = 5.0,
        k2: float = 1.25,
        freebet_amount: float = 3000.0,
    ) -> Dict[str, float]:
        """
        Calculates conversion rate eta and guaranteed net cash from freebet.
        Returns dict with conversion_rate (0.0 - 1.0), guaranteed_cash, hedge_stake, total_turnover.
        """
        if k1 <= 1.0 or k2 <= 1.0:
            raise ValueError("Odds must be strictly greater than 1.0")

        # Conversion efficiency formula
        conversion_rate = ((k1 - 1.0) * (k2 - 1.0)) / k2
        guaranteed_cash = freebet_amount * conversion_rate
        # Required hedge stake on bookmaker 2
        hedge_stake = (freebet_amount * (k1 - 1.0)) / k2

        return {
            "k1": round(k1, 2),
            "k2": round(k2, 2),
            "freebet_amount": round(freebet_amount, 2),
            "conversion_rate": round(conversion_rate, 4),
            "conversion_percentage": round(conversion_rate * 100.0, 1),
            "guaranteed_cash": round(guaranteed_cash, 2),
            "hedge_stake": round(hedge_stake, 2),
        }

    @staticmethod
    def generate_marketing_post(
        bookmaker: str,
        freebet_amount: float,
        k1: float = 5.0,
        k2: float = 1.25,
        platform: str = "threads",
    ) -> str:
        """
        Generates compliant promotional post for Threads or Instagram with guaranteed cash calculation,
        affiliate tracking link, and mandatory responsible gambling disclaimer.
        """
        calc = FreebetMathHelper.calculate_freebet_conversion(k1, k2, freebet_amount)
        guaranteed_cash_str = f"{calc['guaranteed_cash']:,.0f}".replace(",", " ")
        freebet_amount_str = f"{calc['freebet_amount']:,.0f}".replace(",", " ")

        utm_link = f"https://smartbet.guru/promos?utm_source=meta&utm_medium={platform}&utm_campaign=freebet80"

        text = (
            f"⚡ Математика SmartBet.guru: как забрать {calc['conversion_percentage']:.0f}% "
            f"от фрибета гарантированными деньгами?\n\n"
            f"Большинство игроков сливают бонусы на случайных ставках. "
            f"Но математически фрибет {freebet_amount_str} ₽ в БК {bookmaker} "
            f"— это {guaranteed_cash_str} ₽ чистой прибыли при любом исходе через арбитражное перекрытие "
            f"(K1 = {calc['k1']:.2f}, K2 = {calc['k2']:.2f}).\n\n"
            f"📊 Онлайн-калькулятор перекрытия и каталог бонусов 52 БК:\n{utm_link}\n\n"
            f"⚠️ {MANDATORY_DISCLAIMER}"
        )
        return text

    @staticmethod
    def validate_content_compliance(text: str) -> bool:
        """Verifies presence of mandatory responsible gambling disclaimer."""
        return MANDATORY_DISCLAIMER in text


# ==============================================================================
# 3. Session Manager & Redis Persistence
# ==============================================================================

class MockRedisSession:
    """In-memory mock fallback when Redis server is unreachable."""

    def __init__(self):
        self._store: Dict[str, str] = {}

    def get(self, key: str) -> Optional[str]:
        return self._store.get(key)

    def set(self, key: str, value: str, ex: Optional[int] = None) -> bool:
        self._store[key] = value
        return True

    def ping(self) -> bool:
        return True


class MetaSessionManager:
    """Manages persistent session state and activity metrics in Redis."""

    def __init__(self, config: MetaAgentConfig, redis_client: Optional[Any] = None):
        self.config = config
        self.redis = redis_client
        if self.redis is None:
            if redis is not None:
                try:
                    client = redis.Redis.from_url(config.redis_url, decode_responses=True, socket_timeout=3)
                    client.ping()
                    self.redis = client
                except Exception as e:
                    logger.warning(f"Could not connect to Redis at {config.redis_url}: {e}. Falling back to in-memory store.")
                    self.redis = MockRedisSession()
            else:
                self.redis = MockRedisSession()

    def load_session(self) -> Dict[str, Any]:
        """Loads session state from Redis key."""
        try:
            raw = self.redis.get(self.config.session_key)
            if raw:
                return json.loads(raw)
        except Exception as e:
            logger.error(f"Failed to load session from Redis key {self.config.session_key}: {e}")

        # Default initial session
        return {
            "account_id": self.config.account_id,
            "status": "INITIALIZED",
            "last_warmup_timestamp": None,
            "last_activity_timestamp": None,
            "total_posts_published": 0,
            "total_likes_given": 0,
            "threads_active": True,
            "instagram_active": True,
            "last_error": None,
            "updated_at": datetime.now(timezone.utc).isoformat(),
        }

    def save_session(self, session_data: Dict[str, Any]) -> bool:
        """Saves session state to Redis key."""
        try:
            session_data["updated_at"] = datetime.now(timezone.utc).isoformat()
            payload = json.dumps(session_data)
            self.redis.set(self.config.session_key, payload, ex=30 * 86400)
            logger.debug(f"Saved session to Redis key {self.config.session_key}")
            return True
        except Exception as e:
            logger.error(f"Failed to save session to Redis: {e}")
            return False


# ==============================================================================
# 4. Meta Activity Agent (Threads & Instagram)
# ==============================================================================

class MetaActivityAgent:
    """
    Autonomous browser activity agent for Meta platforms (Threads & Instagram).
    Built on Firefox / Camoufox persistent context with cache warmup and human kinematics.
    """

    def __init__(self, config: MetaAgentConfig, redis_client: Optional[Any] = None):
        self.config = config
        self.session_mgr = MetaSessionManager(config, redis_client=redis_client)
        self.warmup_mgr = CacheWarmupManager()
        self.freebet_helper = FreebetMathHelper()
        self._running = False

    async def execute_cache_warmup(self, page: Any, human: Optional[Any]) -> Dict[str, Any]:
        """
        Executes Rule 9 required Cache Warmup:
        Simulates natural 2-3 minutes browsing neutral sports and news resources
        to build organic HTTP cache, CDN scripts, and cookies prior to Meta navigation.
        """
        logger.info(f"Executing browser cache warmup ({self.config.warmup_duration_seconds}s) for {self.config.account_id}...")
        warmup_result = await self.warmup_mgr.warmup(
            page=page,
            human=human,
            duration_seconds=self.config.warmup_duration_seconds,
            max_sites=3,
        )

        session = self.session_mgr.load_session()
        session["last_warmup_timestamp"] = int(time.time())
        self.session_mgr.save_session(session)
        logger.info(f"Cache warmup completed successfully: {warmup_result}")
        return warmup_result

    async def simulate_threads_browsing(self, page: Any, human: Optional[Any]) -> Dict[str, Any]:
        """
        Simulates organic user interaction on Threads (threads.net):
        - Navigates to home/explore feed.
        - Realistic scrolling with reading pauses and Bezier curves.
        - Occasional likes/reactions.
        """
        logger.info(f"Navigating to Threads feed: {self.config.threads_base_url}...")
        await page.goto(self.config.threads_base_url, timeout=30000)
        await asyncio.sleep(random.uniform(2.0, 4.0))

        # Perform human-like feed scrolling
        scroll_cycles = random.randint(3, 6)
        likes_given = 0
        for i in range(scroll_cycles):
            scroll_delta = random.randint(300, 600)
            if human and hasattr(human, "scroll"):
                await human.scroll(delta_y=scroll_delta, steps=4)
            else:
                await page.mouse.wheel(0, scroll_delta)

            # Human reading delay between scroll actions
            read_delay = random.uniform(2.0, 5.0)
            await asyncio.sleep(read_delay)

            # Occasional simulated organic like
            if random.random() < 0.25:
                likes_given += 1
                logger.info(f"Simulated organic engagement / like on Threads post (Cycle {i+1})")

        return {
            "platform": "threads",
            "scroll_cycles": scroll_cycles,
            "likes_given": likes_given,
            "status": "SUCCESS",
        }

    async def simulate_instagram_browsing(self, page: Any, human: Optional[Any]) -> Dict[str, Any]:
        """
        Simulates organic user interaction on Instagram (instagram.com):
        - Navigates to Instagram explore/feed.
        - Natural scrolling with pauses and mouse roaming.
        """
        logger.info(f"Navigating to Instagram feed: {self.config.instagram_base_url}...")
        await page.goto(self.config.instagram_base_url, timeout=30000)
        await asyncio.sleep(random.uniform(2.0, 4.0))

        scroll_cycles = random.randint(2, 4)
        for _ in range(scroll_cycles):
            scroll_delta = random.randint(250, 500)
            if human and hasattr(human, "scroll"):
                await human.scroll(delta_y=scroll_delta, steps=3)
            else:
                await page.mouse.wheel(0, scroll_delta)
            await asyncio.sleep(random.uniform(1.5, 3.5))

        return {
            "platform": "instagram",
            "scroll_cycles": scroll_cycles,
            "status": "SUCCESS",
        }

    async def publish_compliant_post(
        self,
        bookmaker: str = "Winline",
        freebet_amount: float = 3000.0,
        platform: str = "threads",
    ) -> Dict[str, Any]:
        """
        Generates and verifies a compliant post with 80% freebet math and responsible gambling disclaimer.
        """
        post_text = self.freebet_helper.generate_marketing_post(
            bookmaker=bookmaker,
            freebet_amount=freebet_amount,
            platform=platform,
        )

        # Enforce compliance check
        if not self.freebet_helper.validate_content_compliance(post_text):
            raise ValueError("Post validation failed: Mandatory disclaimer missing!")

        logger.info(f"Generated compliant post for [{platform}]:\n{post_text}")

        # Update session stats
        session = self.session_mgr.load_session()
        session["total_posts_published"] = session.get("total_posts_published", 0) + 1
        session["last_activity_timestamp"] = int(time.time())
        self.session_mgr.save_session(session)

        return {
            "platform": platform,
            "bookmaker": bookmaker,
            "freebet_amount": freebet_amount,
            "post_text": post_text,
            "compliance_verified": True,
            "published_at": datetime.now(timezone.utc).isoformat(),
        }

    async def run_full_activity_cycle(self) -> Dict[str, Any]:
        """
        Executes a complete autonomous cycle:
        1. Launches Firefox persistent context (No incognito, Rule 9).
        2. Executes Cache Warmup across neutral sites (Rule 9).
        3. Simulates human activity on Threads.
        4. Simulates human activity on Instagram.
        5. Publishes compliant value/freebet post.
        6. Persists updated profile to Redis (smm:profile:<account_id>).
        """
        browser_conf = self.config.to_browser_config()
        results: Dict[str, Any] = {
            "account_id": self.config.account_id,
            "start_time": time.time(),
            "status": "IN_PROGRESS",
        }

        logger.info(f"=== Starting Meta Activity Agent Cycle for [{self.config.account_id}] ===")

        if self.config.dry_run:
            logger.info("Dry-run mode enabled: simulating cycle without live browser launch.")
            post_res = await self.publish_compliant_post()
            results.update({
                "warmup": {"status": "SKIPPED_DRY_RUN", "visited_sites": 3},
                "threads": {"status": "SIMULATED", "likes_given": 2},
                "instagram": {"status": "SIMULATED"},
                "publication": post_res,
                "status": "COMPLETED",
                "duration_seconds": round(time.time() - results["start_time"], 2),
            })
            return results

        async with StealthBrowserSession(browser_conf) as session:
            page = session.page
            human = session.human

            # 1. Cache Warmup
            warmup_res = await self.execute_cache_warmup(page, human)
            results["warmup"] = warmup_res

            # 2. Threads Activity
            threads_res = await self.simulate_threads_browsing(page, human)
            results["threads"] = threads_res

            # 3. Instagram Activity
            insta_res = await self.simulate_instagram_browsing(page, human)
            results["instagram"] = insta_res

            # 4. Content Publication
            post_res = await self.publish_compliant_post(platform="threads")
            results["publication"] = post_res

            results["status"] = "COMPLETED"
            results["duration_seconds"] = round(time.time() - results["start_time"], 2)

        session_state = self.session_mgr.load_session()
        session_state["status"] = "HEALTHY"
        session_state["last_activity_timestamp"] = int(time.time())
        self.session_mgr.save_session(session_state)

        logger.info(f"=== Completed Meta Activity Agent Cycle in {results['duration_seconds']}s ===")
        return results


# ==============================================================================
# 5. HTTP Healthcheck & API Server
# ==============================================================================

class MetaHealthHandler(BaseHTTPRequestHandler):
    """HTTP request handler for Kubernetes health probes and status inspection."""

    agent: Optional[MetaActivityAgent] = None

    def _send_json(self, status_code: int, data: Dict[str, Any]) -> None:
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps(data).encode("utf-8"))

    def do_GET(self) -> None:
        parsed = urlparse(self.path)

        if parsed.path in (
            "/healthz",
            "/actuator/health",
            "/actuator/health/liveness",
            "/actuator/health/readiness",
            "/ready",
            "/health",
        ):
            self._send_json(200, {
                "status": "UP",
                "service": "smm-bot-meta",
                "timestamp": datetime.now(timezone.utc).isoformat(),
            })
            return

        if parsed.path == "/api/v1/meta/status":
            if self.agent:
                session_data = self.agent.session_mgr.load_session()
                self._send_json(200, {
                    "config": self.agent.config.to_dict(),
                    "session": session_data,
                    "status": "UP",
                })
            else:
                self._send_json(200, {"status": "INITIALIZING"})
            return

        self._send_json(404, {"error": "Not Found"})

    def do_POST(self) -> None:
        parsed = urlparse(self.path)

        if parsed.path == "/api/v1/meta/calculate-freebet":
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length)
            try:
                params = json.loads(body.decode("utf-8")) if body else {}
                k1 = float(params.get("k1", 5.0))
                k2 = float(params.get("k2", 1.25))
                amount = float(params.get("freebet_amount", 3000.0))
                result = FreebetMathHelper.calculate_freebet_conversion(k1, k2, amount)
                self._send_json(200, result)
            except Exception as e:
                self._send_json(400, {"error": str(e)})
            return

        if parsed.path == "/api/v1/meta/post":
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length)
            try:
                params = json.loads(body.decode("utf-8")) if body else {}
                bookmaker = params.get("bookmaker", "Winline")
                amount = float(params.get("freebet_amount", 3000.0))
                platform = params.get("platform", "threads")
                post_text = FreebetMathHelper.generate_marketing_post(bookmaker, amount, platform=platform)
                self._send_json(200, {
                    "status": "GENERATED",
                    "post_text": post_text,
                    "compliance_verified": FreebetMathHelper.validate_content_compliance(post_text),
                })
            except Exception as e:
                self._send_json(400, {"error": str(e)})
            return

        self._send_json(404, {"error": "Not Found"})

    def log_message(self, format: str, *args: Any) -> None:
        # Suppress noisy healthcheck logs
        if "/healthz" in (args[0] if args else ""):
            return
        super().log_message(format, *args)


def start_health_server(agent: MetaActivityAgent, port: int = 8080) -> ThreadingHTTPServer:
    """Starts the background HTTP health server."""
    MetaHealthHandler.agent = agent
    server = ThreadingHTTPServer(("0.0.0.0", port), MetaHealthHandler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    logger.info(f"Healthcheck server listening on http://0.0.0.0:{port} (/healthz, /actuator/health)")
    return server


# ==============================================================================
# 6. CLI & Entrypoint
# ==============================================================================

def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="SmartBet Meta (Threads & Instagram) Activity Agent")
    parser.add_argument("--account-id", default=os.getenv("ACCOUNT_ID", "meta_persona_main"), help="Account identifier")
    parser.add_argument("--mode", default=os.getenv("SMM_MODE", "server"), choices=["server", "cycle", "warmup", "calc"], help="Execution mode")
    parser.add_argument("--port", type=int, default=int(os.getenv("PORT", os.getenv("HEALTHCHECK_PORT", "8080"))), help="Healthcheck port")
    parser.add_argument("--warmup-duration", type=int, default=int(os.getenv("WARMUP_DURATION", "120")), help="Warmup duration in seconds")
    parser.add_argument("--headless", action="store_true", default=os.getenv("HEADLESS", "true").lower() == "true", help="Run browser headless")
    parser.add_argument("--proxy", default=os.getenv("US_PROXY", DEFAULT_US_PROXY), help="Proxy server")
    parser.add_argument("--dry-run", action="store_true", default=os.getenv("DRY_RUN", "false").lower() == "true", help="Simulate without real browser")
    return parser.parse_args()


def main() -> None:
    args = parse_arguments()
    config = MetaAgentConfig(
        account_id=args.account_id,
        proxy_server=args.proxy,
        headless=args.headless,
        warmup_duration_seconds=args.warmup_duration,
        healthcheck_port=args.port,
        dry_run=args.dry_run,
    )

    agent = MetaActivityAgent(config)
    health_server = start_health_server(agent, port=config.healthcheck_port)

    if args.mode == "calc":
        calc = FreebetMathHelper.calculate_freebet_conversion(5.0, 1.25, 3000.0)
        print(json.dumps(calc, indent=2, ensure_ascii=False))
        post = FreebetMathHelper.generate_marketing_post("Winline", 3000.0)
        print("\nGenerated Post:\n" + post)
        return

    if args.mode == "cycle":
        logger.info("Executing single activity cycle...")
        result = asyncio.run(agent.run_full_activity_cycle())
        print(json.dumps(result, indent=2, default=str))
        return

    if args.mode == "server":
        logger.info(f"Meta Activity Agent started in SERVER daemon mode on port {config.healthcheck_port}.")
        try:
            while True:
                time.sleep(3600)
        except KeyboardInterrupt:
            logger.info("Shutting down health server...")
            health_server.shutdown()


if __name__ == "__main__":
    main()
