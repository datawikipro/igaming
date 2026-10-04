#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Unit tests for Reddit Karma Warmup & Content Cycle Manager
plane-1d0662f9: Прогрев кармы, сабреддиты r/sportsbook и r/matchedbetting

Tests cover:
- RedditAccountPool: registration, karma gate, cooldown, shadowban
- KarmaWarmupEngine: warmup cycle, karma gate enforcement
- SubredditContentCycleManager: rate limits, karma gate, dedup, UTM
- Integration: RedditCrowdAgent with karma pool and content cycle
"""

import json
import os
import sys
import time
import unittest
from unittest.mock import MagicMock, patch

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from reddit_agent import MockRedis, RedditConfig, RedditCrowdAgent
from reddit_karma_warmup import (
    KarmaWarmupEngine,
    MIN_KARMA_FOR_CROWD,
    RedditAccountPool,
    RedditAccountProfile,
    ShadowbanDetector,
    WARMUP_SUBREDDITS,
    build_default_account_pool,
)
from reddit_content_cycle import (
    SUBREDDIT_STRATEGIES,
    SubredditContentCycleManager,
    SubredditCycleState,
    SubredditStrategy,
)


# ==============================================================================
# RedditAccountPool Tests
# ==============================================================================

class TestRedditAccountPool(unittest.TestCase):
    """Tests account registration, karma gate, cooldown and shadowban handling."""

    def _make_pool(self, karma: int = 0, cooldown: int = 0) -> RedditAccountPool:
        mock_redis = MockRedis()
        acc = RedditAccountProfile(
            account_id="test_agent_01",
            username="test_user_01",
            comment_karma=karma,
            cooldown_seconds=cooldown,
        )
        pool = RedditAccountPool(redis_client=mock_redis, accounts=[acc])
        return pool

    def test_account_registration(self):
        pool = self._make_pool(karma=0)
        self.assertIn("test_agent_01", pool._accounts)
        acc = pool._accounts["test_agent_01"]
        self.assertEqual(acc.username, "test_user_01")

    def test_karma_gate_below_threshold(self):
        pool = self._make_pool(karma=MIN_KARMA_FOR_CROWD - 1)
        eligible = pool.get_eligible_for_crowd()
        self.assertEqual(len(eligible), 0, "Should not be eligible below threshold")

    def test_karma_gate_at_threshold(self):
        pool = self._make_pool(karma=MIN_KARMA_FOR_CROWD, cooldown=0)
        eligible = pool.get_eligible_for_crowd()
        self.assertEqual(len(eligible), 1, "Should be eligible at threshold")

    def test_karma_update_persists_to_redis(self):
        mock_redis = MockRedis()
        acc = RedditAccountProfile(
            account_id="agent_karma_test",
            username="karma_user",
            comment_karma=10,
        )
        pool = RedditAccountPool(redis_client=mock_redis, accounts=[acc])
        pool.update_karma("agent_karma_test", comment_karma=60, post_karma=5)
        saved = mock_redis.get(acc.redis_key)
        self.assertIsNotNone(saved)
        data = json.loads(saved)
        self.assertEqual(data["comment_karma"], 60)
        self.assertEqual(data["post_karma"], 5)

    def test_cooldown_blocks_eligible(self):
        pool = self._make_pool(karma=MIN_KARMA_FOR_CROWD, cooldown=300)
        # Simulate recent use
        pool._accounts["test_agent_01"].last_used_ts = time.time()
        eligible = pool.get_eligible_for_crowd()
        self.assertEqual(len(eligible), 0, "Cooldown should block eligibility")

    def test_shadowban_flag_excludes_account(self):
        pool = self._make_pool(karma=MIN_KARMA_FOR_CROWD + 100, cooldown=0)
        pool.flag_shadowban("test_agent_01")
        eligible = pool.get_eligible_for_crowd()
        self.assertEqual(len(eligible), 0, "Shadowbanned account must be excluded")

    def test_warmup_queue_returns_below_threshold(self):
        pool = self._make_pool(karma=5, cooldown=0)
        warming = pool.get_for_warmup()
        self.assertEqual(len(warming), 1)

    def test_mark_warmup_increments_counter(self):
        pool = self._make_pool(karma=10, cooldown=0)
        pool.mark_warmup("test_agent_01", delta_karma=2)
        acc = pool._accounts["test_agent_01"]
        self.assertEqual(acc.warmup_comments_posted, 1)
        self.assertEqual(acc.comment_karma, 12)  # 10 + delta 2

    def test_pool_status_summary(self):
        pool = self._make_pool(karma=MIN_KARMA_FOR_CROWD, cooldown=0)
        status = pool.status()
        self.assertEqual(status["total_accounts"], 1)
        self.assertIn("accounts", status)

    def test_build_default_pool(self):
        mock_redis = MockRedis()
        pool = build_default_account_pool(mock_redis)
        self.assertIsNotNone(pool)
        self.assertGreater(len(pool._accounts), 0)


# ==============================================================================
# KarmaWarmupEngine Tests
# ==============================================================================

class TestKarmaWarmupEngine(unittest.TestCase):
    """Tests warmup cycle execution and karma gate enforcement."""

    def _make_engine(self, karma: int = 0) -> tuple:
        mock_redis = MockRedis()
        acc = RedditAccountProfile(
            account_id="warmup_test_acc",
            username="warmup_test_user",
            comment_karma=karma,
            cooldown_seconds=0,
        )
        pool = RedditAccountPool(redis_client=mock_redis, accounts=[acc])
        engine = KarmaWarmupEngine(pool=pool, dry_run=True)
        return pool, engine, acc

    def test_warmup_cycle_runs_for_below_threshold(self):
        pool, engine, acc = self._make_engine(karma=5)
        results = engine.run_warmup_cycle(max_comments=2)
        self.assertGreater(len(results), 0)
        self.assertTrue(all(r.comment_posted for r in results))

    def test_warmup_skips_eligible_accounts(self):
        """Accounts already above threshold are not included in warmup."""
        pool, engine, acc = self._make_engine(karma=MIN_KARMA_FOR_CROWD + 10)
        results = engine.run_warmup_cycle()
        self.assertEqual(len(results), 0, "No warmup needed for eligible accounts")

    def test_warmup_increments_comment_counter(self):
        pool, engine, acc = self._make_engine(karma=1)
        initial_comments = acc.warmup_comments_posted
        engine.run_warmup_cycle(max_comments=1)
        self.assertEqual(acc.warmup_comments_posted, initial_comments + 1)

    def test_karma_gate_blocked(self):
        pool, engine, acc = self._make_engine(karma=10)
        blocked = engine.check_karma_gate(acc)
        self.assertFalse(blocked, "Account below threshold should be blocked")

    def test_karma_gate_passed(self):
        pool, engine, acc = self._make_engine(karma=MIN_KARMA_FOR_CROWD)
        passed = engine.check_karma_gate(acc)
        self.assertTrue(passed, "Account at threshold should pass karma gate")

    def test_warmup_comment_uses_safe_subreddits(self):
        """Warmup comments must not target r/sportsbook or r/matchedbetting."""
        pool, engine, acc = self._make_engine(karma=0)
        results = engine.run_warmup_cycle(max_comments=5)
        for r in results:
            self.assertNotIn(r.subreddit, ["sportsbook", "matchedbetting"],
                             "Warmup must use only safe subreddits")
            self.assertIn(r.subreddit, WARMUP_SUBREDDITS)


# ==============================================================================
# ShadowbanDetector Tests
# ==============================================================================

class TestShadowbanDetector(unittest.TestCase):
    """Tests shadowban detection with mocked HTTP responses."""

    def test_shadowban_detected_on_404(self):
        mock_requests = MagicMock()
        mock_response = MagicMock()
        mock_response.status_code = 404
        mock_requests.get.return_value = mock_response

        detector = ShadowbanDetector(requests_lib=mock_requests)
        result = detector.check_account("banned_user")
        self.assertTrue(result, "404 response should flag shadowban")

    def test_no_shadowban_on_200(self):
        mock_requests = MagicMock()
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"data": {"is_suspended": False}}
        mock_requests.get.return_value = mock_response

        detector = ShadowbanDetector(requests_lib=mock_requests)
        result = detector.check_account("active_user")
        self.assertFalse(result, "200 non-suspended response should not flag shadowban")

    def test_suspended_account_flagged(self):
        mock_requests = MagicMock()
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"data": {"is_suspended": True}}
        mock_requests.get.return_value = mock_response

        detector = ShadowbanDetector(requests_lib=mock_requests)
        result = detector.check_account("suspended_user")
        self.assertTrue(result, "Suspended account should be flagged")

    def test_no_requests_lib(self):
        detector = ShadowbanDetector(requests_lib=None)
        result = detector.check_account("any_user")
        self.assertFalse(result, "Without requests lib, should return False (safe default)")


# ==============================================================================
# SubredditContentCycleManager Tests
# ==============================================================================

class TestSubredditContentCycleManager(unittest.TestCase):
    """Tests rate limiting, karma gate, dedup, and UTM generation."""

    def _make_manager(self, dry_run: bool = True) -> SubredditContentCycleManager:
        import copy
        mock_redis = MockRedis()
        # Use a deep copy of strategies to prevent test-order pollution of global objects
        strategies = {k: copy.copy(v) for k, v in SUBREDDIT_STRATEGIES.items()}
        return SubredditContentCycleManager(redis_client=mock_redis, strategies=strategies, dry_run=dry_run)

    def test_default_strategies_exist(self):
        manager = self._make_manager()
        self.assertIn("sportsbook", manager.strategies)
        self.assertIn("matchedbetting", manager.strategies)
        self.assertIn("Arbing", manager.strategies)
        self.assertIn("sportsbetting", manager.strategies)

    def test_sportsbook_strategy_focus(self):
        strategy = SUBREDDIT_STRATEGIES["sportsbook"]
        self.assertEqual(strategy.content_focus, "arbitrage")

    def test_matchedbetting_strategy_focus(self):
        strategy = SUBREDDIT_STRATEGIES["matchedbetting"]
        self.assertEqual(strategy.content_focus, "freebet_snr")

    def test_karma_gate_blocks_low_karma(self):
        manager = self._make_manager()
        strategy = manager.strategies["sportsbook"]
        # r/sportsbook requires 100 karma; force active hours = True to isolate karma gate
        original = strategy.active_hours_utc_start, strategy.active_hours_utc_end
        strategy.active_hours_utc_start = 0
        strategy.active_hours_utc_end = 24
        try:
            can_post = manager.can_post_in_subreddit("sportsbook", account_karma=10)
        finally:
            strategy.active_hours_utc_start, strategy.active_hours_utc_end = original
        self.assertFalse(can_post, "Low karma should be blocked for r/sportsbook")

    def test_daily_limit_blocks_after_max(self):
        manager = self._make_manager()
        state = manager._get_state("matchedbetting")
        strategy = manager.strategies["matchedbetting"]
        # Exhaust daily limit
        state.replies_today = strategy.max_replies_per_day
        # Ensure we're in active hours by patching
        with patch.object(strategy, "is_active_hours", return_value=True):
            can_post = manager.can_post_in_subreddit("matchedbetting", account_karma=999)
        self.assertFalse(can_post, "Daily limit should block posting")

    def test_deduplication_prevents_double_post(self):
        manager = self._make_manager()
        manager.mark_post_replied("post_xyz_123")
        self.assertTrue(manager.is_post_replied("post_xyz_123"))
        self.assertFalse(manager.is_post_replied("post_xyz_456"))

    def test_record_reply_increments_counter(self):
        manager = self._make_manager()
        state = manager._get_state("matchedbetting")
        initial = state.replies_today
        manager.record_reply("matchedbetting", "post_111")
        self.assertEqual(state.replies_today, initial + 1)
        self.assertTrue(manager.is_post_replied("post_111"))

    def test_utm_params_per_subreddit(self):
        manager = self._make_manager()
        params = manager.get_utm_params("matchedbetting", "post_aaa")
        self.assertEqual(params["utm_source"], "reddit")
        self.assertEqual(params["utm_medium"], "crowd")
        self.assertEqual(params["utm_campaign"], "r_matchedbetting_snr")
        self.assertEqual(params["utm_content"], "post_aaa")

    def test_content_focus_selection_freebet(self):
        manager = self._make_manager()
        focus = manager.select_content_focus("matchedbetting")
        self.assertEqual(focus, "freebet_snr")

    def test_content_focus_selection_arbitrage(self):
        manager = self._make_manager()
        focus = manager.select_content_focus("sportsbook")
        self.assertEqual(focus, "arbitrage")

    def test_cycle_summary_structure(self):
        manager = self._make_manager()
        summary = manager.cycle_summary()
        for sub in ["sportsbook", "matchedbetting", "Arbing", "sportsbetting"]:
            self.assertIn(sub, summary)
            self.assertIn("replies_today", summary[sub])
            self.assertIn("content_focus", summary[sub])

    def test_get_next_subreddit_returns_with_high_karma(self):
        """With very high karma and unconstrained hours, should return a subreddit."""
        manager = self._make_manager()
        # Patch all strategies to report active hours
        for strategy in manager.strategies.values():
            strategy.active_hours_utc_start = 0
            strategy.active_hours_utc_end = 24
            strategy.min_account_karma = 1
        result = manager.get_next_subreddit(account_karma=9999)
        self.assertIsNotNone(result)
        self.assertIn(result, manager.strategies)


# ==============================================================================
# Integration: RedditCrowdAgent with Karma + Content Cycle
# ==============================================================================

class TestRedditCrowdAgentIntegration(unittest.TestCase):
    """Integration tests ensuring karma_pool and content_cycle are wired into the agent."""

    def setUp(self):
        self.cfg = RedditConfig(cooldown_seconds=0, dry_run=True, max_replies_per_run=20)
        self.agent = RedditCrowdAgent(self.cfg)
        # Replace Redis clients with MockRedis for isolation
        mock_redis = MockRedis()
        self.agent.session_mgr.client = mock_redis
        if self.agent.karma_pool:
            self.agent.karma_pool.redis = mock_redis
        if self.agent.content_cycle:
            self.agent.content_cycle.redis = mock_redis

    def test_karma_warmup_phase_runs(self):
        result = self.agent.run_karma_warmup_phase()
        self.assertIn("status", result)

    def test_crowd_cycle_returns_success_or_warmup_only(self):
        result = self.agent.run_crowd_cycle()
        self.assertIn(result["status"], ["SUCCESS", "WARMUP_ONLY"])

    def test_warmup_comments_tracked_in_metrics(self):
        # Set account karma high enough to skip warmup
        if self.agent.karma_pool:
            for acc in self.agent.karma_pool._accounts.values():
                acc.comment_karma = MIN_KARMA_FOR_CROWD + 100
        # Patch content cycle to allow posting
        if self.agent.content_cycle:
            for strategy in self.agent.content_cycle.strategies.values():
                strategy.active_hours_utc_start = 0
                strategy.active_hours_utc_end = 24
                strategy.min_account_karma = 1
        self.agent.run_crowd_cycle()
        # Metrics should be accessible
        self.assertIsNotNone(self.agent.warmup_comments_posted)

    def test_healthcheck_includes_karma_pool(self):
        """Healthcheck should include karma_pool status when available."""
        from reddit_agent import start_healthcheck_server
        import http.client

        self.cfg.healthcheck_port = 19091
        agent = RedditCrowdAgent(self.cfg)
        server = start_healthcheck_server(agent, 19091)
        try:
            conn = http.client.HTTPConnection("127.0.0.1", 19091, timeout=2)
            conn.request("GET", "/healthz")
            resp = conn.getresponse()
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertEqual(data["status"], "UP")
            self.assertIn("warmup_comments_posted", data)
            conn.close()
        finally:
            server.shutdown()


if __name__ == "__main__":
    unittest.main()
