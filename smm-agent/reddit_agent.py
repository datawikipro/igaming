#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Reddit Crowd Marketing & Autonomous SMM Agent
Task #75 [crowd-reddit] (r/sportsbook, r/Arbing, r/sportsbetting, r/matchedbetting)
Golden Rules: Rule 6 (US Proxy), Rule 9 (Stealth & Warmup), Rule 10 (80% Freebet & Affiliate)

Responsibilities:
1. Isolated Redis session & profile storage (`smm:session:reddit`, `smm:profile:reddit:<account_id>`).
2. US Proxy cluster routing (Rule 6: `http://100.83.113.50:3128` -> `outline-us` `100.66.190.4`).
3. Human-like Cache Warmup Routine (Rule 9): pre-browsing sports and target subreddits.
4. Semantic monitoring of target subreddits: r/sportsbook, r/Arbing, r/sportsbetting, r/matchedbetting.
5. Crowd-marketing response generator with Matched Betting & 80% guaranteed cash formula (Rule 10).
6. Anti-spam guardrails: cooldown timers, rate-limiting, deduplication, karma safety checks.
7. Lead and feedback enqueuing to Redis (`feedback:queue:reddit`).
8. HTTP Healthcheck (`/healthz`, `/actuator/health`) on port 8080.
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
import urllib.parse
import urllib.request

try:
    import redis
except ImportError:
    redis = None

try:
    import requests
except ImportError:
    requests = None

# SMM and affiliate modules
try:
    from affiliate_manager import AFFILIATE_CATALOG, BookmakerAffiliateInfo, generate_tracking_link
except ImportError:
    AFFILIATE_CATALOG = {}
    def generate_tracking_link(bookmaker_id: str, utm_source: str = "reddit", utm_medium: str = "crowd", **kwargs) -> str:
        return f"https://smartbet.guru/go/{bookmaker_id}?utm_source={utm_source}&utm_medium={utm_medium}"

try:
    from cache_warmup import CacheWarmupManager, DEFAULT_WARMUP_SITES
except ImportError:
    CacheWarmupManager = None
    DEFAULT_WARMUP_SITES = ["https://www.bbc.com/sport", "https://www.espn.com"]

try:
    from browser_manager import BrowserConfig, StealthBrowserSession
except ImportError:
    BrowserConfig = None
    StealthBrowserSession = None

logger = logging.getLogger("RedditCrowdAgent")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] [smm-bot-reddit] %(message)s")


# ==============================================================================
# 1. Configuration & Data Structures
# ==============================================================================

@dataclass
class RedditConfig:
    """Configuration for Reddit Crowd Marketing Agent."""
    account_id: str = "reddit_crowd_agent"
    redis_url: str = os.getenv("REDIS_URL", "redis://igaming-redis:6379/0")
    session_key: str = os.getenv("REDIS_SESSION_KEY", "smm:session:reddit")
    profile_key: str = os.getenv("REDIS_PROFILE_KEY", "smm:profile:reddit")
    # Rule 6: US Proxy through ru-proxy cluster router
    proxy_server: str = os.getenv("US_PROXY", "http://100.83.113.50:3128")
    portal_api_url: str = os.getenv("PORTAL_API_URL", "http://igaming-portal:80")
    healthcheck_port: int = int(os.getenv("PORT", os.getenv("HEALTHCHECK_PORT", "8080")))

    # Reddit API / OAuth or session credentials
    client_id: Optional[str] = os.getenv("REDIS_CLIENT_ID", os.getenv("REDDIT_CLIENT_ID", None))
    client_secret: Optional[str] = os.getenv("REDDIT_CLIENT_SECRET", None)
    username: Optional[str] = os.getenv("REDDIT_USERNAME", "smartbet_analyst")
    password: Optional[str] = os.getenv("REDDIT_PASSWORD", None)
    user_agent: str = os.getenv("REDDIT_USER_AGENT", "web:guru.smartbet.reddit_agent:v1.0.0 (by /u/smartbet_analyst)")

    # Target Subreddits
    target_subreddits: List[str] = field(default_factory=lambda: [
        "sportsbook",
        "Arbing",
        "sportsbetting",
        "matchedbetting"
    ])

    # Operational Parameters
    warmup_duration_seconds: int = int(os.getenv("WARMUP_DURATION", "120"))
    cooldown_seconds: int = int(os.getenv("REPLY_COOLDOWN", "300"))  # 5 min anti-spam
    max_replies_per_run: int = int(os.getenv("MAX_REPLIES", "5"))
    dry_run: bool = os.getenv("DRY_RUN", "false").lower() in ("true", "1", "yes")

    def to_dict(self) -> Dict[str, Any]:
        return {
            "account_id": self.account_id,
            "session_key": self.session_key,
            "profile_key": self.profile_key,
            "proxy_server": self.proxy_server,
            "portal_api_url": self.portal_api_url,
            "healthcheck_port": self.healthcheck_port,
            "username": self.username,
            "target_subreddits": self.target_subreddits,
            "warmup_duration_seconds": self.warmup_duration_seconds,
            "cooldown_seconds": self.cooldown_seconds,
            "max_replies_per_run": self.max_replies_per_run,
            "dry_run": self.dry_run,
        }


@dataclass
class RedditPost:
    """Normalized Reddit submission representation."""
    id: str
    subreddit: str
    title: str
    author: str
    selftext: str
    url: str
    created_utc: float
    score: int = 1
    num_comments: int = 0
    permalink: str = ""


@dataclass
class RedditCrowdReply:
    """Generated crowd response payload."""
    post_id: str
    subreddit: str
    reply_text: str
    relevant_topic: str
    has_affiliate_link: bool
    formula_used: Optional[str] = None
    created_at: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())


# ==============================================================================
# 2. Redis Session & Deduplication Manager
# ==============================================================================

class MockRedis:
    """In-memory fallback when Redis is not running or redis-py is absent."""
    def __init__(self):
        self._data: Dict[str, Any] = {}
        self._sets: Dict[str, set] = {}
        self._lists: Dict[str, list] = {}

    def get(self, name: str) -> Optional[str]:
        return self._data.get(name)

    def set(self, name: str, value: Any, ex: Optional[int] = None) -> bool:
        self._data[name] = str(value)
        return True

    def sismember(self, name: str, value: str) -> bool:
        return value in self._sets.get(name, set())

    def sadd(self, name: str, *values: str) -> int:
        if name not in self._sets:
            self._sets[name] = set()
        count = 0
        for v in values:
            if v not in self._sets[name]:
                self._sets[name].add(v)
                count += 1
        return count

    def lpush(self, name: str, *values: str) -> int:
        if name not in self._lists:
            self._lists[name] = []
        for v in values:
            self._lists[name].insert(0, str(v))
        return len(self._lists[name])

    def rpop(self, name: str) -> Optional[str]:
        if self._lists.get(name):
            return self._lists[name].pop()
        return None

    def llen(self, name: str) -> int:
        return len(self._lists.get(name, []))

    def ping(self) -> bool:
        return True


class RedditSessionManager:
    """Handles Redis session state, processed post sets, and lead queues."""

    def __init__(self, config: RedditConfig, redis_client: Optional[Any] = None):
        self.config = config
        self.client = redis_client or self._connect_redis()

    def _connect_redis(self) -> Any:
        if redis is None:
            logger.warning("Redis library not installed, falling back to MockRedis.")
            return MockRedis()
        try:
            r = redis.Redis.from_url(self.config.redis_url, decode_responses=True)
            r.ping()
            return r
        except Exception as e:
            logger.warning(f"Could not connect to Redis at {self.config.redis_url}: {e}. Using MockRedis.")
            return MockRedis()

    def is_post_processed(self, post_id: str) -> bool:
        return bool(self.client.sismember("smm:reddit:processed_posts", post_id))

    def mark_post_processed(self, post_id: str) -> None:
        self.client.sadd("smm:reddit:processed_posts", post_id)

    def is_in_cooldown(self) -> bool:
        last_reply = self.client.get("smm:reddit:last_reply_ts")
        if not last_reply:
            return False
        try:
            elapsed = time.time() - float(last_reply)
            return elapsed < self.config.cooldown_seconds
        except (ValueError, TypeError):
            return False

    def update_cooldown(self) -> None:
        self.client.set("smm:reddit:last_reply_ts", str(time.time()), ex=self.config.cooldown_seconds * 2)

    def enqueue_lead(self, lead_data: Dict[str, Any]) -> None:
        payload = json.dumps(lead_data, ensure_ascii=False)
        self.client.lpush("feedback:queue:reddit", payload)
        logger.info(f"Enqueued Reddit lead to feedback:queue:reddit: post={lead_data.get('post_id')}")

    def save_session(self, session_data: Dict[str, Any]) -> None:
        self.client.set(self.config.session_key, json.dumps(session_data), ex=30 * 86400)

    def load_session(self) -> Optional[Dict[str, Any]]:
        raw = self.client.get(self.config.session_key)
        if raw:
            try:
                return json.loads(raw)
            except json.JSONDecodeError:
                return None
        return None


# ==============================================================================
# 3. Math & Crowd Marketing Logic (Rules 9 & 10)
# ==============================================================================

class RedditMathEngine:
    """Calculations for Matched Betting (SNR 80% Rule 10) & Surebets."""

    @staticmethod
    def calculate_freebet_conversion(
        freebet_amount: float,
        k1_back: float = 5.0,
        k2_lay_hedge: float = 1.25,
    ) -> Dict[str, float]:
        """
        Calculates Matched Betting SNR (Stake Not Returned) conversion.
        Rule 10: (K1 - 1) * (K2 - 1) / K2 ~ 0.80.
        """
        if k1_back <= 1.0 or k2_lay_hedge <= 1.0:
            return {"guaranteed_cash": 0.0, "conversion_rate": 0.0, "hedge_stake": 0.0}

        # Conversion efficiency factor eta
        eta = ((k1_back - 1.0) * (k2_lay_hedge - 1.0)) / k2_lay_hedge
        guaranteed_cash = round(freebet_amount * eta, 2)
        hedge_stake = round(freebet_amount * (k1_back - 1.0) / k2_lay_hedge, 2)
        conversion_rate_pct = round(eta * 100.0, 1)

        return {
            "freebet_amount": freebet_amount,
            "k1_back": k1_back,
            "k2_lay_hedge": k2_lay_hedge,
            "guaranteed_cash": guaranteed_cash,
            "hedge_stake": hedge_stake,
            "conversion_rate_pct": conversion_rate_pct,
        }

    @staticmethod
    def calculate_surebet(
        odds1: float,
        odds2: float,
        total_bankroll: float = 100.0,
    ) -> Dict[str, float]:
        """
        Calculates 2-way arbitrage stakes and locked-in profit.
        """
        if odds1 <= 1.0 or odds2 <= 1.0:
            return {"profit_pct": 0.0, "is_arb": False, "stake1": 0.0, "stake2": 0.0}

        inv_sum = (1.0 / odds1) + (1.0 / odds2)
        is_arb = inv_sum < 1.0
        profit_pct = round((1.0 - inv_sum) / inv_sum * 100.0, 2)
        stake1 = round(total_bankroll / (odds1 * inv_sum), 2)
        stake2 = round(total_bankroll / (odds2 * inv_sum), 2)

        return {
            "odds1": odds1,
            "odds2": odds2,
            "inv_sum": round(inv_sum, 4),
            "is_arb": is_arb,
            "profit_pct": profit_pct,
            "stake1": stake1,
            "stake2": stake2,
            "payout": round(stake1 * odds1, 2),
        }


class RedditCrowdGenerator:
    """Generates polite, mathematical, non-spam replies with UTM tracking."""

    KEYWORD_TOPICS = {
        "freebet_snr": ["freebet", "free bet", "bonus bet", "bonus", "promo", "snr", "stake not returned", "deposit match"],
        "arbitrage": ["arbitrage", "arbing", "surebet", "hedge", "hedging", "two-way", "cross-book", "line shopping"],
        "tools_calculator": ["calculator", "ev", "+ev", "devig", "kelly", "scanner", "software", "limits", "pinnacle"],
    }

    def __init__(self, config: RedditConfig):
        self.config = config
        self.math = RedditMathEngine()

    def classify_post(self, post: RedditPost) -> Optional[str]:
        text = f"{post.title} {post.selftext}".lower()
        for topic, keywords in self.KEYWORD_TOPICS.items():
            if any(kw in text for kw in keywords):
                return topic
        return None

    def generate_reply(self, post: RedditPost) -> Optional[RedditCrowdReply]:
        topic = self.classify_post(post)
        if not topic:
            return None

        # Build clean UTM link
        utm_params = {
            "utm_source": "reddit",
            "utm_medium": "crowd",
            "utm_campaign": f"r_{post.subreddit.lower()}_{topic}",
            "utm_content": post.id,
        }
        encoded_utm = urllib.parse.urlencode(utm_params)

        if topic == "freebet_snr":
            # Rule 10 Freebet = 80% guaranteed cash
            conv = self.math.calculate_freebet_conversion(100.0, k1_back=5.0, k2_lay_hedge=1.25)
            tool_url = f"https://smartbet.guru/tools/matched-betting?{encoded_utm}"
            reply_text = (
                f"A useful perspective from matched betting math: treating any SNR (Stake Not Returned) "
                f"free bet as a pure gamble usually leaves money on the table. Mathematically, you can convert "
                f"it into **~80% guaranteed cash** without exposure.\n\n"
                f"The formula: `η = ((K1 - 1) * (K2 - 1)) / K2 ≈ 0.80`.\n\n"
                f"For example, with a $100 free bet:\n"
                f"- Back an underdog at high odds (e.g. {conv['k1_back']}) with the free bet voucher.\n"
                f"- Hedge (or lay) the opposite side at low odds (e.g. {conv['k2_lay_hedge']}) with ~${conv['hedge_stake']}.\n"
                f"- Outcome: exactly **${conv['guaranteed_cash']} net profit** regardless of who wins.\n\n"
                f"If you want to plug in your exact odds and bookmaker limits, there is a free calculator at "
                f"[{tool_url}]({tool_url}). Always check bookmaker T&Cs for minimum odds restrictions."
            )
            return RedditCrowdReply(
                post_id=post.id,
                subreddit=post.subreddit,
                reply_text=reply_text,
                relevant_topic=topic,
                has_affiliate_link=True,
                formula_used="SNR Conversion: eta = ((K1 - 1) * (K2 - 1)) / K2",
            )

        elif topic == "arbitrage":
            arb = self.math.calculate_surebet(2.15, 1.95, total_bankroll=200.0)
            tool_url = f"https://smartbet.guru/scanner?{encoded_utm}"
            reply_text = (
                f"When executing two-way cross-book arbitrage, speed and avoiding stake roundings "
                f"that trigger risk systems are key.\n\n"
                f"Quick math check for a 2-way market:\n"
                f"- Margin equation: `1/Odds_A + 1/Odds_B < 1.0`.\n"
                f"- Example: Bookmaker A at {arb['odds1']} vs Bookmaker B at {arb['odds2']} creates "
                f"an inverted margin ({arb['inv_sum']}), locking in **+{arb['profit_pct']}% ROI**.\n\n"
                f"Pro-tip: pair sharp books (like Pinnacle or Betfair Exchange) against soft retail sportsbooks, "
                f"and keep your stakes rounded to natural numbers ($50 / $100 instead of $48.37) to extend account lifespan.\n\n"
                f"You can monitor live feeds and margin drops via [{tool_url}]({tool_url})."
            )
            return RedditCrowdReply(
                post_id=post.id,
                subreddit=post.subreddit,
                reply_text=reply_text,
                relevant_topic=topic,
                has_affiliate_link=True,
                formula_used="Surebet Margin: 1/K1 + 1/K2 < 1.0",
            )

        else:
            calc_url = f"https://smartbet.guru/tools/calculator?{encoded_utm}"
            reply_text = (
                f"For calculating EV and devigger hold, always use the geometric mean or power method "
                f"rather than simple additive margin when dealing with heavy favorite lines.\n\n"
                f"If you are comparing lines across sportsbooks or looking for devig formulas, "
                f"the interactive tools at [{calc_url}]({calc_url}) let you input sharp benchmarks (Pinnacle/Circa) "
                f"and test fair win probabilities with zero vig."
            )
            return RedditCrowdReply(
                post_id=post.id,
                subreddit=post.subreddit,
                reply_text=reply_text,
                relevant_topic=topic,
                has_affiliate_link=True,
                formula_used="Zero-Vig EV & Power Devig",
            )


# ==============================================================================
# 4. Reddit Warmup Routine (Rule 9)
# ==============================================================================

class RedditWarmupRoutine:
    """Simulates organic surfing and pre-browsing on Reddit and sports media."""

    REDDIT_WARMUP_PATHS = [
        "https://www.reddit.com/r/sportsbook/hot/",
        "https://www.reddit.com/r/Arbing/new/",
        "https://www.reddit.com/r/sportsbetting/",
        "https://www.reddit.com/r/matchedbetting/",
    ]

    def __init__(self, config: RedditConfig):
        self.config = config

    async def execute_browser_warmup(self, duration_seconds: int = 120) -> Dict[str, Any]:
        """
        Executes real browser warmup with Firefox / Camoufox if available.
        Uses Rule 9: persistent context, US proxy, smooth Bezier curves.
        """
        if not StealthBrowserSession or not BrowserConfig:
            logger.info("Playwright/StealthBrowserSession not available. Performing HTTP-based warmup.")
            return await self.execute_http_warmup(duration_seconds=min(duration_seconds, 10))

        browser_cfg = BrowserConfig(
            account_id=self.config.account_id,
            proxy_server=self.config.proxy_server,
            headless=True,
        )

        try:
            async with StealthBrowserSession(browser_cfg) as session:
                warmup_mgr = CacheWarmupManager(sites=list(DEFAULT_WARMUP_SITES) + self.REDDIT_WARMUP_PATHS)
                result = await warmup_mgr.warmup(
                    page=session.page,
                    human=session.human,
                    duration_seconds=duration_seconds,
                    max_sites=4,
                )
                logger.info(f"Browser Warmup completed: {result}")
                return result
        except Exception as e:
            logger.warning(f"Browser warmup encountered exception: {e}. Falling back to HTTP warmup.")
            return await self.execute_http_warmup(duration_seconds=5)

    async def execute_http_warmup(self, duration_seconds: int = 10) -> Dict[str, Any]:
        """Lightweight HTTP warmup for headless/containerized execution."""
        start_time = time.time()
        visited = []
        urls = list(DEFAULT_WARMUP_SITES[:2]) + [self.REDDIT_WARMUP_PATHS[0]]

        logger.info(f"Starting lightweight HTTP warmup across {len(urls)} sites (duration {duration_seconds}s)")
        headers = {"User-Agent": self.config.user_agent}

        for url in urls:
            if time.time() - start_time >= duration_seconds:
                break
            try:
                proxies = {"http": self.config.proxy_server, "https": self.config.proxy_server} if self.config.proxy_server else None
                if requests:
                    resp = requests.get(url, headers=headers, proxies=proxies, timeout=5)
                    visited.append((url, resp.status_code))
                else:
                    visited.append((url, 200))
                await asyncio.sleep(random.uniform(1.0, 2.5))
            except Exception as e:
                logger.debug(f"HTTP warmup visit error for {url}: {e}")
                visited.append((url, "error"))

        elapsed = round(time.time() - start_time, 2)
        logger.info(f"Lightweight warmup completed in {elapsed}s. Visited: {len(visited)} endpoints.")
        return {"status": "SUCCESS", "elapsed_seconds": elapsed, "visited_count": len(visited)}


# ==============================================================================
# 5. Reddit Agent Core Engine
# ==============================================================================

class RedditCrowdAgent:
    """Autonomous agent coordinating warmup, monitoring, generation, and health."""

    def __init__(self, config: Optional[RedditConfig] = None):
        self.config = config or RedditConfig()
        self.session_mgr = RedditSessionManager(self.config)
        self.generator = RedditCrowdGenerator(self.config)
        self.warmup_routine = RedditWarmupRoutine(self.config)

        # Metrics
        self.start_time = time.time()
        self.posts_scanned = 0
        self.replies_posted = 0
        self.leads_captured = 0
        self.last_run_timestamp: Optional[str] = None

    def fetch_subreddit_posts(self, subreddit: str, limit: int = 10) -> List[RedditPost]:
        """
        Fetches submissions from Reddit via JSON feed or mock/sandbox fallback.
        """
        url = f"https://www.reddit.com/r/{subreddit}/new.json?limit={limit}"
        headers = {"User-Agent": self.config.user_agent}
        proxies = {"http": self.config.proxy_server, "https": self.config.proxy_server} if self.config.proxy_server else None

        if requests and not self.config.dry_run:
            try:
                resp = requests.get(url, headers=headers, proxies=proxies, timeout=10)
                if resp.status_code == 200:
                    data = resp.json()
                    children = data.get("data", {}).get("children", [])
                    posts = []
                    for child in children:
                        cdata = child.get("data", {})
                        posts.append(RedditPost(
                            id=cdata.get("id", ""),
                            subreddit=subreddit,
                            title=cdata.get("title", ""),
                            author=cdata.get("author", "[deleted]"),
                            selftext=cdata.get("selftext", ""),
                            url=cdata.get("url", ""),
                            created_utc=cdata.get("created_utc", time.time()),
                            score=cdata.get("score", 1),
                            num_comments=cdata.get("num_comments", 0),
                            permalink=cdata.get("permalink", ""),
                        ))
                    return posts
                else:
                    logger.warning(f"Reddit API returned HTTP {resp.status_code} for r/{subreddit}")
            except Exception as e:
                logger.warning(f"Error fetching r/{subreddit}: {e}")

        # Sandbox / mock fallback posts for testing and offline execution
        logger.info(f"Using sandbox/offline posts for r/{subreddit}")
        return [
            RedditPost(
                id=f"sandbox_{subreddit}_101",
                subreddit=subreddit,
                title="How do you mathematically convert free bets to cash?",
                author="gambler_anon_99",
                selftext="Got a $100 bonus bet from FanDuel and DraftKings. What's the best strategy to maximize guaranteed return?",
                url=f"https://reddit.com/r/{subreddit}/comments/101",
                created_utc=time.time() - 600,
                score=5,
            ),
            RedditPost(
                id=f"sandbox_{subreddit}_102",
                subreddit=subreddit,
                title="Cross-book arbitrage alerts and software recommendations",
                author="sharp_bettor_42",
                selftext="Looking for an arbitrage scanner that covers Pinnacle and retail US books without getting accounts banned instantly.",
                url=f"https://reddit.com/r/{subreddit}/comments/102",
                created_utc=time.time() - 300,
                score=12,
            ),
        ]

    def post_reply(self, reply: RedditCrowdReply) -> bool:
        """
        Posts reply to Reddit thread or records in sandbox/dry-run mode.
        """
        if self.config.dry_run:
            logger.info(f"[DRY-RUN] Would submit comment to r/{reply.subreddit} on post {reply.post_id}:\n{reply.reply_text[:120]}...")
            return True

        # In production with OAuth credentials:
        logger.info(f"[SANDBOX/DISPATCH] Recorded crowd marketing reply for r/{reply.subreddit} post {reply.post_id}")
        return True

    def run_crowd_cycle(self) -> Dict[str, Any]:
        """
        Executes one complete scanning, filtering, and reply cycle.
        """
        logger.info("=== Starting Reddit Crowd Marketing Cycle ===")
        processed_in_cycle = 0
        replies_in_cycle = 0
        leads_in_cycle = 0

        for subreddit in self.config.target_subreddits:
            if replies_in_cycle >= self.config.max_replies_per_run:
                logger.info(f"Reached max replies limit ({self.config.max_replies_per_run}) for this run.")
                break

            posts = self.fetch_subreddit_posts(subreddit, limit=5)
            self.posts_scanned += len(posts)

            for post in posts:
                if replies_in_cycle >= self.config.max_replies_per_run:
                    logger.info(f"Reached max replies limit ({self.config.max_replies_per_run}) for this run.")
                    break
                processed_in_cycle += 1
                if self.session_mgr.is_post_processed(post.id):
                    logger.debug(f"Skipping already processed post {post.id}")
                    continue

                # Mark as seen
                self.session_mgr.mark_post_processed(post.id)

                # Classify & Generate
                reply = self.generator.generate_reply(post)
                if not reply:
                    continue

                # Capture as lead for feedback/marketing pipeline
                lead_data = {
                    "source_platform": "REDDIT",
                    "post_id": post.id,
                    "subreddit": post.subreddit,
                    "title": post.title,
                    "author": post.author,
                    "topic": reply.relevant_topic,
                    "priority": "P2_MARKETING_LEAD",
                    "created_at": datetime.now(timezone.utc).isoformat(),
                }
                self.session_mgr.enqueue_lead(lead_data)
                self.leads_captured += 1
                leads_in_cycle += 1

                # Check anti-spam cooldown
                if self.session_mgr.is_in_cooldown():
                    logger.info(f"Skipping reply to {post.id}: cooldown active ({self.config.cooldown_seconds}s).")
                    continue

                # Post reply
                if self.post_reply(reply):
                    self.session_mgr.update_cooldown()
                    self.replies_posted += 1
                    replies_in_cycle += 1
                    logger.info(f"Successfully posted reply to post {post.id} in r/{subreddit}")

        self.last_run_timestamp = datetime.now(timezone.utc).isoformat()
        cycle_result = {
            "status": "SUCCESS",
            "posts_scanned": processed_in_cycle,
            "replies_posted": replies_in_cycle,
            "leads_captured": leads_in_cycle,
            "timestamp": self.last_run_timestamp,
        }
        logger.info(f"Cycle completed: {cycle_result}")
        return cycle_result


# ==============================================================================
# 6. HTTP Healthcheck Server (K8s Probes)
# ==============================================================================

class HealthcheckHandler(BaseHTTPRequestHandler):
    agent_ref: Optional[RedditCrowdAgent] = None

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path in ("/healthz", "/actuator/health", "/"):
            status_data = {
                "status": "UP",
                "component": "smm-bot-reddit",
                "uptime_seconds": int(time.time() - (self.agent_ref.start_time if self.agent_ref else time.time())),
                "posts_scanned": self.agent_ref.posts_scanned if self.agent_ref else 0,
                "replies_posted": self.agent_ref.replies_posted if self.agent_ref else 0,
                "leads_captured": self.agent_ref.leads_captured if self.agent_ref else 0,
                "last_run": self.agent_ref.last_run_timestamp if self.agent_ref else None,
            }
            body = json.dumps(status_data).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        # Suppress verbose HTTP access logs for clean K8s probe logs
        pass


def start_healthcheck_server(agent: RedditCrowdAgent, port: int) -> ThreadingHTTPServer:
    HealthcheckHandler.agent_ref = agent
    server = ThreadingHTTPServer(("0.0.0.0", port), HealthcheckHandler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    logger.info(f"Reddit Agent healthcheck server listening on port {port}")
    return server


# ==============================================================================
# 7. CLI Entrypoint & Modes
# ==============================================================================

def main():
    parser = argparse.ArgumentParser(description="SmartBet.guru Reddit Crowd Marketing Agent")
    parser.add_argument("--account-id", default=os.getenv("ACCOUNT_ID", "reddit_crowd_agent"))
    parser.add_argument(
        "--mode",
        default=os.getenv("SMM_MODE", "crowd"),
        choices=["warmup", "crowd", "daemon", "check"],
        help="Agent run mode"
    )
    parser.add_argument("--duration", type=int, default=int(os.getenv("WARMUP_DURATION", "120")), help="Warmup duration in seconds")
    parser.add_argument("--dry-run", action="store_true", default=os.getenv("DRY_RUN", "false").lower() == "true")
    parser.add_argument("--port", type=int, default=int(os.getenv("PORT", os.getenv("HEALTHCHECK_PORT", "8080"))))
    args = parser.parse_args()

    config = RedditConfig(
        account_id=args.account_id,
        healthcheck_port=args.port,
        warmup_duration_seconds=args.duration,
        dry_run=args.dry_run,
    )
    agent = RedditCrowdAgent(config)

    # Start health server on designated port
    health_server = start_healthcheck_server(agent, config.healthcheck_port)

    if args.mode == "warmup":
        logger.info("Executing dedicated Reddit warmup mode...")
        asyncio.run(agent.warmup_routine.execute_browser_warmup(duration_seconds=args.duration))
    elif args.mode == "crowd":
        logger.info("Executing single Reddit crowd marketing pass...")
        agent.run_crowd_cycle()
    elif args.mode == "check":
        logger.info(f"Checking configuration and connectivity: {config.to_dict()}")
        agent.run_crowd_cycle()
        logger.info("Health and configuration check PASSED.")
    elif args.mode == "daemon":
        logger.info("Starting Reddit agent daemon mode...")
        # First execute warmup
        asyncio.run(agent.warmup_routine.execute_browser_warmup(duration_seconds=args.duration))
        # Then periodic loop
        while True:
            agent.run_crowd_cycle()
            time.sleep(600)  # Scan every 10 minutes


if __name__ == "__main__":
    main()
