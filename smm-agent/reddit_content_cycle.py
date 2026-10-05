#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Reddit Subreddit Content Cycle Manager
Task [smm-reddit] plane-1d0662f9: Content-cycle таргетинг r/sportsbook и r/matchedbetting

Golden Rules: Rule 9 (Anti-spam, Warmup), Rule 10 (80% Freebet, Affiliate), Rule 12 (Static Proxy)

Responsibilities:
1. Scheduled content cycle with per-subreddit posting cadence and daily limits.
2. r/sportsbook focus: arbitrage math, sharp money, line movement tips.
3. r/matchedbetting focus: SNR freebet 80% cash conversion formula and calculator links.
4. Karma gate integration: only post when account karma >= MIN_KARMA_FOR_CROWD.
5. Rate-limit enforcement: per-subreddit daily reply budget and inter-post cooldown.
6. Deduplication: Redis set tracks replied post IDs to prevent double-posting.
7. Affiliate UTM rotation: per-subreddit campaign tags for analytics tracking.
"""

import json
import logging
import os
import random
import time
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
from typing import Any, Dict, List, Optional

logger = logging.getLogger("SubredditContentCycle")

# ---------------------------------------------------------------------------
# Per-subreddit content strategy configuration
# ---------------------------------------------------------------------------

@dataclass
class SubredditStrategy:
    """
    Defines the content strategy and rate limits for a specific subreddit.
    """
    name: str                        # Subreddit name without r/
    display_name: str                # Human-readable label

    # Content focus determines which reply template category to use
    content_focus: str               # "freebet_snr" | "arbitrage" | "tools_calculator" | "mixed"

    # Anti-spam limits
    max_replies_per_day: int = 3     # Maximum comments per day in this subreddit
    min_interval_hours: float = 2.0  # Minimum hours between consecutive replies in same subreddit
    min_account_karma: int = 50      # Minimum karma required before posting here

    # UTM campaign tag for this subreddit (Rule 10 affiliate tracking)
    utm_campaign_tag: str = ""

    # Posting hours (UTC): only post between these hours to match subreddit active times
    active_hours_utc_start: int = 12  # Noon UTC = 8am EST
    active_hours_utc_end: int = 22    # 10pm UTC = 6pm EST

    def is_active_hours(self) -> bool:
        """Check if current UTC time falls within this subreddit's active posting window."""
        now_hour = datetime.now(timezone.utc).hour
        return self.active_hours_utc_start <= now_hour < self.active_hours_utc_end

    def __post_init__(self):
        if not self.utm_campaign_tag:
            self.utm_campaign_tag = f"r_{self.name.lower()}_crowd"


# Default strategies for target subreddits
SUBREDDIT_STRATEGIES: Dict[str, SubredditStrategy] = {
    "sportsbook": SubredditStrategy(
        name="sportsbook",
        display_name="r/sportsbook",
        content_focus="arbitrage",          # Focus on arbitrage & sharp money topics
        max_replies_per_day=4,
        min_interval_hours=3.0,
        min_account_karma=100,              # Higher bar — active moderation
        utm_campaign_tag="r_sportsbook_arb",
        active_hours_utc_start=13,          # 1pm UTC = 9am EST
        active_hours_utc_end=23,
    ),
    "matchedbetting": SubredditStrategy(
        name="matchedbetting",
        display_name="r/matchedbetting",
        content_focus="freebet_snr",        # Focus on 80% freebet SNR conversion
        max_replies_per_day=5,
        min_interval_hours=2.0,
        min_account_karma=50,
        utm_campaign_tag="r_matchedbetting_snr",
        active_hours_utc_start=12,
        active_hours_utc_end=22,
    ),
    "Arbing": SubredditStrategy(
        name="Arbing",
        display_name="r/Arbing",
        content_focus="arbitrage",
        max_replies_per_day=3,
        min_interval_hours=4.0,
        min_account_karma=75,
        utm_campaign_tag="r_arbing_crowd",
        active_hours_utc_start=10,
        active_hours_utc_end=22,
    ),
    "sportsbetting": SubredditStrategy(
        name="sportsbetting",
        display_name="r/sportsbetting",
        content_focus="mixed",
        max_replies_per_day=3,
        min_interval_hours=3.0,
        min_account_karma=60,
        utm_campaign_tag="r_sportsbetting_crowd",
        active_hours_utc_start=12,
        active_hours_utc_end=23,
    ),
}


# ---------------------------------------------------------------------------
# Cycle state tracking per subreddit
# ---------------------------------------------------------------------------

@dataclass
class SubredditCycleState:
    """Tracks posting state for one subreddit within the current rolling day."""
    subreddit: str
    replies_today: int = 0
    last_reply_ts: float = 0.0
    day_start_ts: float = field(default_factory=time.time)

    def reset_if_new_day(self) -> None:
        """Reset daily counter if 24 hours have passed."""
        if time.time() - self.day_start_ts >= 86400:
            self.replies_today = 0
            self.day_start_ts = time.time()
            logger.debug(f"Daily counter reset for r/{self.subreddit}.")

    def can_post(self, strategy: SubredditStrategy) -> bool:
        """Returns True if posting is allowed given rate limits and active hours."""
        self.reset_if_new_day()
        if not strategy.is_active_hours():
            logger.debug(f"r/{self.subreddit}: outside active posting hours.")
            return False
        if self.replies_today >= strategy.max_replies_per_day:
            logger.debug(
                f"r/{self.subreddit}: daily limit reached ({self.replies_today}/{strategy.max_replies_per_day})."
            )
            return False
        elapsed_hours = (time.time() - self.last_reply_ts) / 3600.0
        if elapsed_hours < strategy.min_interval_hours and self.last_reply_ts > 0:
            logger.debug(
                f"r/{self.subreddit}: inter-post interval not reached "
                f"({elapsed_hours:.1f}h < {strategy.min_interval_hours}h)."
            )
            return False
        return True

    def record_post(self) -> None:
        """Record a successful post to update rate-limit counters."""
        self.replies_today += 1
        self.last_reply_ts = time.time()


# ---------------------------------------------------------------------------
# Content Cycle Manager
# ---------------------------------------------------------------------------

class SubredditContentCycleManager:
    """
    Manages the full content posting lifecycle across target subreddits.

    Workflow:
    1. Iterate through subreddits with configured strategies.
    2. Check karma gate and rate limits before each post.
    3. Select appropriate content template based on subreddit focus.
    4. Post reply with UTM affiliate tracking link.
    5. Update Redis deduplication set and cycle state.
    6. Persist cycle state to Redis for survivability across restarts.
    """

    REDIS_CYCLE_STATE_KEY = "smm:reddit:cycle_state"
    REDIS_PROCESSED_KEY = "smm:reddit:processed_posts"

    def __init__(
        self,
        redis_client: Any,
        strategies: Optional[Dict[str, SubredditStrategy]] = None,
        dry_run: bool = True,
    ):
        import copy as _copy
        self.redis = redis_client
        # Deep copy to ensure mutations (e.g. in tests) do not affect global SUBREDDIT_STRATEGIES
        self.strategies = {k: _copy.copy(v) for k, v in (strategies or SUBREDDIT_STRATEGIES).items()}
        self.dry_run = dry_run
        self._states: Dict[str, SubredditCycleState] = {}
        self._load_state()

    # ------------------------------------------------------------------ state

    def _load_state(self) -> None:
        """Load persisted cycle state from Redis."""
        try:
            raw = self.redis.get(self.REDIS_CYCLE_STATE_KEY)
            if raw:
                data = json.loads(raw)
                for sub, s in data.items():
                    self._states[sub] = SubredditCycleState(
                        subreddit=sub,
                        replies_today=s.get("replies_today", 0),
                        last_reply_ts=s.get("last_reply_ts", 0.0),
                        day_start_ts=s.get("day_start_ts", time.time()),
                    )
                logger.info(f"Loaded cycle state for {len(self._states)} subreddits from Redis.")
        except Exception as e:
            logger.warning(f"Could not load cycle state from Redis: {e}")

    def _save_state(self) -> None:
        """Persist cycle state to Redis (TTL 48h to survive restarts)."""
        try:
            data = {
                sub: {
                    "replies_today": state.replies_today,
                    "last_reply_ts": state.last_reply_ts,
                    "day_start_ts": state.day_start_ts,
                }
                for sub, state in self._states.items()
            }
            self.redis.set(self.REDIS_CYCLE_STATE_KEY, json.dumps(data), ex=48 * 3600)
        except Exception as e:
            logger.warning(f"Could not save cycle state to Redis: {e}")

    def _get_state(self, subreddit: str) -> SubredditCycleState:
        """Get or create cycle state for a subreddit."""
        if subreddit not in self._states:
            self._states[subreddit] = SubredditCycleState(subreddit=subreddit)
        return self._states[subreddit]

    # ------------------------------------------------------- deduplication

    def is_post_replied(self, post_id: str) -> bool:
        """Check if we already replied to this post."""
        try:
            return bool(self.redis.sismember(self.REDIS_PROCESSED_KEY, post_id))
        except Exception:
            return False

    def mark_post_replied(self, post_id: str) -> None:
        """Mark post as replied in Redis deduplication set."""
        try:
            self.redis.sadd(self.REDIS_PROCESSED_KEY, post_id)
        except Exception as e:
            logger.warning(f"Could not mark post {post_id} as replied: {e}")

    # --------------------------------------------------- posting decision

    def can_post_in_subreddit(
        self,
        subreddit: str,
        account_karma: int,
    ) -> bool:
        """
        Master check: karma gate + rate limits + active hours.
        """
        strategy = self.strategies.get(subreddit)
        if not strategy:
            logger.debug(f"No strategy configured for r/{subreddit}.")
            return False

        # Karma gate (Rule 9: account must be warmed up)
        if account_karma < strategy.min_account_karma:
            logger.debug(
                f"r/{subreddit}: karma gate — account karma {account_karma} "
                f"< required {strategy.min_account_karma}."
            )
            return False

        state = self._get_state(subreddit)
        return state.can_post(strategy)

    def record_reply(self, subreddit: str, post_id: str) -> None:
        """Record a successful reply — updates rate-limit state and deduplication."""
        state = self._get_state(subreddit)
        state.record_post()
        self.mark_post_replied(post_id)
        self._save_state()

    # ------------------------------------------- content template selection

    def select_content_focus(self, subreddit: str) -> str:
        """
        Determine the content template category for this subreddit.
        For 'mixed' strategies, alternate between freebet_snr and arbitrage
        based on the current hour to create natural variation.
        """
        strategy = self.strategies.get(subreddit)
        if not strategy:
            return "arbitrage"

        if strategy.content_focus == "mixed":
            # Alternate focus every hour so consecutive posts differ
            hour = datetime.now(timezone.utc).hour
            return "freebet_snr" if hour % 2 == 0 else "arbitrage"

        return strategy.content_focus

    def get_utm_params(self, subreddit: str, post_id: str) -> Dict[str, str]:
        """Build UTM tracking parameters for this subreddit's affiliate links."""
        strategy = self.strategies.get(subreddit)
        campaign = strategy.utm_campaign_tag if strategy else f"r_{subreddit.lower()}_crowd"
        return {
            "utm_source": "reddit",
            "utm_medium": "crowd",
            "utm_campaign": campaign,
            "utm_content": post_id,
        }

    # ------------------------------------------------- cycle execution

    def cycle_summary(self) -> Dict[str, Any]:
        """
        Return current posting status across all subreddits.
        Useful for health endpoint and operator dashboard.
        """
        summary = {}
        for sub, strategy in self.strategies.items():
            state = self._get_state(sub)
            state.reset_if_new_day()
            summary[sub] = {
                "display_name": strategy.display_name,
                "content_focus": strategy.content_focus,
                "replies_today": state.replies_today,
                "max_replies_per_day": strategy.max_replies_per_day,
                "last_reply_ts": state.last_reply_ts,
                "can_post": state.can_post(strategy),
                "is_active_hours": strategy.is_active_hours(),
            }
        return summary

    def get_next_subreddit(self, account_karma: int) -> Optional[str]:
        """
        Select the next subreddit to post in, prioritizing:
        1. Within active hours
        2. Under daily limit
        3. Past inter-post interval
        4. Above karma threshold
        5. Ordered by replies_today ascending (balance load across subs)
        """
        candidates = []
        for sub, strategy in self.strategies.items():
            if self.can_post_in_subreddit(sub, account_karma):
                state = self._get_state(sub)
                candidates.append((sub, state.replies_today))

        if not candidates:
            return None

        # Select subreddit with fewest posts today for natural distribution
        candidates.sort(key=lambda x: x[1])
        return candidates[0][0]
