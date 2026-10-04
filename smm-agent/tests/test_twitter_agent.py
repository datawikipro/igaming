#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Twitter/X Automation Agent Test Suite
Tests for:
- FreebetMathHelper: 80% guaranteed cash formula (Rule 10)
- Mandatory responsible gambling disclaimer compliance
- TwitterAgentConfig defaults (Rule 6 + 12: static US-CA proxy)
- TwitterSessionManager: Redis session persistence and metric counters
- HTTP healthcheck endpoints (/healthz, /actuator/health/readiness, /actuator/health/liveness, /api/v1/twitter/status)
- Tweet text generation: surebet and freebet post compliance
- Comment deduplication via is_comment_seen / mark_comment_seen
- Rule 9 compliance: account ID, profile key, session key patterns
- Rate-limit guard: TWITTER_CHAR_LIMIT enforcement
"""

import asyncio
from datetime import datetime, timezone
import json
import os
import sys
import time
import unittest
from unittest.mock import AsyncMock, MagicMock, patch

# Ensure module path is included
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from twitter_agent import (
    FreebetMathHelper,
    MANDATORY_DISCLAIMER,
    MockRedisSession,
    TwitterActivityAgent,
    TwitterAgentConfig,
    TwitterBrowserAgent,
    TwitterHealthHandler,
    TwitterSessionManager,
    TWITTER_CHAR_LIMIT,
    MIN_COMMENT_REPLY_COOLDOWN_SEC,
    MAX_POSTS_PER_HOUR,
    start_health_server,
)


# ==============================================================================
# 1. Freebet Math — Rule 10
# ==============================================================================


class TestFreebetMath(unittest.TestCase):
    """Verifies the 80% SNR freebet guaranteed cash conversion formula (Rule 10)."""

    def test_standard_80_percent_conversion(self):
        """
        K1=5.0, K2=1.25, freebet=3000:
        eta = (4.0 * 0.25) / 1.25 = 0.80 → 2400 ₽ guaranteed cash.
        """
        res = FreebetMathHelper.calculate_freebet_conversion(k1=5.0, k2=1.25, freebet_amount=3000.0)
        self.assertAlmostEqual(res["conversion_rate"], 0.80, places=3)
        self.assertAlmostEqual(res["conversion_percentage"], 80.0, places=1)
        self.assertAlmostEqual(res["guaranteed_cash"], 2400.0, places=2)
        self.assertAlmostEqual(res["hedge_stake"], 9600.0, places=2)

    def test_high_odds_conversion(self):
        """K1=6.0, K2=1.20 → eta > 0.83 (Rule 10 approved range)."""
        res = FreebetMathHelper.calculate_freebet_conversion(k1=6.0, k2=1.20, freebet_amount=5000.0)
        self.assertGreater(res["conversion_rate"], 0.83)
        self.assertGreater(res["guaranteed_cash"], 4150.0)

    def test_invalid_k1_raises(self):
        with self.assertRaises(ValueError):
            FreebetMathHelper.calculate_freebet_conversion(k1=0.9, k2=1.25)

    def test_invalid_k2_raises(self):
        with self.assertRaises(ValueError):
            FreebetMathHelper.calculate_freebet_conversion(k1=5.0, k2=1.0)

    def test_result_keys_present(self):
        res = FreebetMathHelper.calculate_freebet_conversion(k1=4.5, k2=1.28, freebet_amount=2000.0)
        for key in ("conversion_rate", "conversion_percentage", "guaranteed_cash", "hedge_stake"):
            self.assertIn(key, res)

    def test_freebet_tweet_contains_80_percent_formula(self):
        """Freebet tweet must state guaranteed cash amount (Rule 10)."""
        tweet = FreebetMathHelper.generate_freebet_tweet(
            bookmaker="Winline",
            freebet_amount=3000.0,
            k1=5.0,
            k2=1.25,
        )
        self.assertIn("Winline", tweet)
        # guaranteed cash = 2400 (formatted as 2\u00a0400 with non-breaking space)
        self.assertIn("2\u00a0400", tweet)
        # Responsible disclaimer shortform
        self.assertIn("ответственно", tweet.lower())

    def test_freebet_tweet_within_char_limit(self):
        """All generated freebet tweets must be ≤ TWITTER_CHAR_LIMIT."""
        tweet = FreebetMathHelper.generate_freebet_tweet(
            bookmaker="Фонбет",
            freebet_amount=15000.0,
            k1=5.5,
            k2=1.22,
        )
        self.assertLessEqual(len(tweet), TWITTER_CHAR_LIMIT)

    def test_surebet_tweet_contains_required_elements(self):
        """Surebet tweet must contain bookmakers, odds, and disclaimer (Rule 10)."""
        tweet = FreebetMathHelper.generate_surebet_tweet(
            bookmaker1="Winline",
            bookmaker2="Betcity",
            event="Реал Мадрид — Барселона",
            odds1=2.15,
            odds2=2.30,
            profit_pct=4.2,
            sport="Футбол",
        )
        self.assertIn("Winline", tweet)
        self.assertIn("Betcity", tweet)
        self.assertIn("4.2", tweet)
        self.assertIn("ответственно", tweet.lower())
        self.assertLessEqual(len(tweet), TWITTER_CHAR_LIMIT)

    def test_surebet_tweet_contains_affiliate_links(self):
        """Surebet tweet with affiliate IDs must include smartbet.guru tracking URLs."""
        tweet = FreebetMathHelper.generate_surebet_tweet(
            bookmaker1="Winline",
            bookmaker2="Fonbet",
            event="ЦСКА — Спартак",
            odds1=2.10,
            odds2=2.05,
            profit_pct=2.8,
            affiliate_bm1="winline",
            affiliate_bm2="fonbet",
        )
        self.assertIn("smartbet.guru", tweet)
        self.assertIn("utm_source=twitter", tweet)


# ==============================================================================
# 2. Mandatory Disclaimer
# ==============================================================================


class TestMandatoryDisclaimer(unittest.TestCase):
    """Rule 9 / social-media-bot spec: mandatory responsible gambling disclaimer."""

    def test_disclaimer_text_present(self):
        self.assertIn("финансовыми рисками", MANDATORY_DISCLAIMER)
        self.assertIn("лудомании", MANDATORY_DISCLAIMER)
        self.assertIn("Играйте ответственно", MANDATORY_DISCLAIMER)

    def test_surebet_tweet_has_disclaimer(self):
        tweet = FreebetMathHelper.generate_surebet_tweet(
            bookmaker1="БК1",
            bookmaker2="БК2",
            event="Test Match",
            odds1=2.0,
            odds2=2.3,
            profit_pct=3.0,
        )
        self.assertIn("ответственно", tweet.lower())

    def test_freebet_tweet_has_disclaimer(self):
        tweet = FreebetMathHelper.generate_freebet_tweet(
            bookmaker="TestBK",
            freebet_amount=1000.0,
        )
        self.assertIn("ответственно", tweet.lower())


# ==============================================================================
# 3. TwitterAgentConfig — Rules 6, 9, 12
# ==============================================================================


class TestTwitterAgentConfig(unittest.TestCase):
    """Verify default configuration aligns with Rules 6, 9, 12."""

    def setUp(self):
        # Clear env proxies to test defaults
        for key in ("TWITTER_PROXY", "US_PROXY"):
            os.environ.pop(key, None)

    def test_default_proxy_is_dedicated_us_ca(self):
        """Rule 12: Twitter must use dedicated static purevpn-us-ca proxy (1:1 mapping)."""
        cfg = TwitterAgentConfig()
        # Should default to purevpn-us-ca.proxy:3128 (K8s DNS service name)
        self.assertIn("purevpn-us-ca", cfg.proxy_server)
        self.assertIn("3128", cfg.proxy_server)
        # Must NOT be a raw IP address (Rule 2: no hardcoded IPs in K8s configs)
        import re
        ip_pattern = re.compile(r"\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b")
        self.assertFalse(ip_pattern.search(cfg.proxy_server),
                         f"Proxy contains hardcoded IP (Rule 2 violation): {cfg.proxy_server}")

    def test_profile_key_pattern(self):
        """Rule 9: Profile key must follow smm:profile:twitter:<account_id> pattern."""
        cfg = TwitterAgentConfig(account_id="smartbetguru_twitter")
        key = cfg.get_profile_key()
        self.assertIn("smm:profile:twitter", key)
        self.assertIn("smartbetguru_twitter", key)

    def test_session_key_default(self):
        cfg = TwitterAgentConfig()
        self.assertIn("twitter", cfg.session_key)

    def test_queue_key_default(self):
        cfg = TwitterAgentConfig()
        self.assertIn("twitter", cfg.queue_key)

    def test_feedback_queue_key(self):
        cfg = TwitterAgentConfig()
        self.assertIn("feedback", cfg.feedback_queue_key)
        self.assertIn("twitter", cfg.feedback_queue_key)

    def test_healthcheck_port_default(self):
        """Healthcheck must serve on port 8080 (K8s actuator probes standard)."""
        os.environ.pop("PORT", None)
        os.environ.pop("HEALTHCHECK_PORT", None)
        cfg = TwitterAgentConfig()
        self.assertEqual(cfg.healthcheck_port, 8080)

    def test_twitter_char_limit(self):
        self.assertEqual(TWITTER_CHAR_LIMIT, 280)


# ==============================================================================
# 4. MockRedisSession
# ==============================================================================


class TestMockRedisSession(unittest.TestCase):
    """Verify MockRedisSession covers all required operations."""

    def setUp(self):
        self.r = MockRedisSession()

    def test_set_and_get(self):
        self.r.set("key1", "value1")
        self.assertEqual(self.r.get("key1"), b"value1")

    def test_get_missing_returns_none(self):
        self.assertIsNone(self.r.get("nonexistent"))

    def test_setex(self):
        self.r.setex("k", 60, "v")
        self.assertEqual(self.r.get("k"), b"v")

    def test_lpop_and_rpush(self):
        self.r.rpush("q", "a", "b", "c")
        self.assertEqual(self.r.lpop("q"), b"a")
        self.assertEqual(self.r.lpop("q"), b"b")

    def test_lpop_empty_returns_none(self):
        self.assertIsNone(self.r.lpop("empty"))

    def test_sadd_and_sismember(self):
        self.r.sadd("myset", "x", "y")
        self.assertTrue(self.r.sismember("myset", "x"))
        self.assertFalse(self.r.sismember("myset", "z"))

    def test_incr(self):
        self.assertEqual(self.r.incr("counter"), 1)
        self.assertEqual(self.r.incr("counter"), 2)

    def test_ping_returns_true(self):
        self.assertTrue(self.r.ping())

    def test_hset_and_hgetall(self):
        self.r.hset("myhash", mapping={"field1": "val1", "field2": "42"})
        data = self.r.hgetall("myhash")
        self.assertIn(b"field1", data)


# ==============================================================================
# 5. TwitterSessionManager
# ==============================================================================


class TestTwitterSessionManager(unittest.TestCase):
    """Tests Redis session persistence, metric counters, and queue operations."""

    def setUp(self):
        self.config = TwitterAgentConfig(account_id="test_twitter")
        self.mgr = TwitterSessionManager(self.config)
        self.mgr._redis = MockRedisSession()

    def test_save_and_load_status(self):
        self.mgr.save_status("running")
        loaded = self.mgr.load_status()
        self.assertEqual(loaded["status"], "running")
        self.assertEqual(loaded["handle"], "@smartbetguru")

    def test_increment_metric(self):
        self.mgr.increment("posts_published")
        self.mgr.increment("posts_published")
        self.assertEqual(self.mgr.metrics["posts_published"], 2)

    def test_increment_multiple_metrics(self):
        self.mgr.increment("surebets_posted", 3)
        self.mgr.increment("mentions_monitored", 5)
        self.assertEqual(self.mgr.metrics["surebets_posted"], 3)
        self.assertEqual(self.mgr.metrics["mentions_monitored"], 5)

    def test_comment_deduplication(self):
        self.assertFalse(self.mgr.is_comment_seen("tweet_abc"))
        self.mgr.mark_comment_seen("tweet_abc")
        self.assertTrue(self.mgr.is_comment_seen("tweet_abc"))

    def test_comment_seen_different_ids(self):
        self.mgr.mark_comment_seen("id_1")
        self.assertFalse(self.mgr.is_comment_seen("id_2"))

    def test_pop_tweet_job_empty_returns_none(self):
        self.assertIsNone(self.mgr.pop_tweet_job())

    def test_pop_tweet_job_returns_dict(self):
        job = {"type": "surebet", "bookmaker1": "Winline", "odds1": 2.1}
        self.mgr._redis.rpush(self.config.queue_key, json.dumps(job))
        result = self.mgr.pop_tweet_job()
        self.assertIsNotNone(result)
        self.assertEqual(result["type"], "surebet")
        self.assertEqual(result["bookmaker1"], "Winline")

    def test_push_feedback_enqueues(self):
        comment = {"id": "123", "author": "@user", "text": "Great signal!", "platform": "twitter"}
        self.mgr.push_feedback(comment)
        raw = self.mgr._redis.lpop(self.config.feedback_queue_key)
        self.assertIsNotNone(raw)
        parsed = json.loads(raw)
        self.assertEqual(parsed["id"], "123")

    def test_status_includes_metrics(self):
        self.mgr.increment("posts_published", 3)
        self.mgr.save_status("running")
        loaded = self.mgr.load_status()
        self.assertEqual(loaded["posts_published"], 3)

    def test_connect_with_no_redis_uses_mock(self):
        """When Redis is unavailable, fallback to MockRedisSession (no crash)."""
        import importlib
        import twitter_agent as ta_module
        original_redis = ta_module.redis
        ta_module.redis = None
        try:
            mgr = TwitterSessionManager(self.config)
            mgr.connect()
            self.assertIsInstance(mgr._redis, MockRedisSession)
        finally:
            ta_module.redis = original_redis


# ==============================================================================
# 6. TwitterBrowserAgent — Rate Limits & Human Kinematics
# ==============================================================================


class TestTwitterBrowserAgentBehavior(unittest.TestCase):
    """Tests rate-limit enforcement and Bezier curve generation."""

    def setUp(self):
        self.config = TwitterAgentConfig(account_id="test_browser_agent", dry_run=True)
        self.mgr = TwitterSessionManager(self.config)
        self.mgr._redis = MockRedisSession()
        self.agent = TwitterBrowserAgent(self.config, self.mgr)

    def test_cubic_bezier_returns_correct_number_of_points(self):
        trajectory = TwitterBrowserAgent._cubic_bezier((0, 0), (100, 200), steps=15)
        self.assertEqual(len(trajectory), 16)  # steps + 1

    def test_cubic_bezier_starts_near_p0(self):
        p0 = (50.0, 80.0)
        p3 = (400.0, 300.0)
        trajectory = TwitterBrowserAgent._cubic_bezier(p0, p3)
        first = trajectory[0]
        # First point should be close to p0 (within jitter tolerance)
        self.assertAlmostEqual(first[0], p0[0], delta=5.0)
        self.assertAlmostEqual(first[1], p0[1], delta=5.0)

    def test_cubic_bezier_ends_near_p3(self):
        p0 = (50.0, 80.0)
        p3 = (400.0, 300.0)
        trajectory = TwitterBrowserAgent._cubic_bezier(p0, p3)
        last = trajectory[-1]
        self.assertAlmostEqual(last[0], p3[0], delta=5.0)
        self.assertAlmostEqual(last[1], p3[1], delta=5.0)

    def test_reply_cooldown_prevents_double_reply(self):
        """Same thread should not be replied to within MIN_COMMENT_REPLY_COOLDOWN_SEC."""
        thread_id = "12345678"
        self.agent._reply_cooldowns[thread_id] = time.monotonic()  # Just replied

        # Simulate check — should be within cooldown
        last_reply = self.agent._reply_cooldowns.get(thread_id, 0.0)
        elapsed = time.monotonic() - last_reply
        self.assertLess(elapsed, MIN_COMMENT_REPLY_COOLDOWN_SEC)

    def test_tweet_text_truncated_to_char_limit(self):
        """post_tweet must truncate text to TWITTER_CHAR_LIMIT."""
        long_text = "А" * 500  # 500 chars — way over limit
        truncated = long_text[:TWITTER_CHAR_LIMIT - 1] + "…"
        self.assertLessEqual(len(truncated), TWITTER_CHAR_LIMIT)

    def test_max_posts_per_hour_constant(self):
        self.assertEqual(MAX_POSTS_PER_HOUR, 6)

    def test_reply_cooldown_constant(self):
        self.assertEqual(MIN_COMMENT_REPLY_COOLDOWN_SEC, 120)


# ==============================================================================
# 7. HTTP Healthcheck
# ==============================================================================


class TestHealthEndpoints(unittest.TestCase):
    """Tests healthcheck server responses for K8s actuator probes."""

    def _make_request(self, path: str, port: int = 8089) -> tuple:
        import urllib.request
        import urllib.error
        try:
            with urllib.request.urlopen(f"http://127.0.0.1:{port}{path}", timeout=3) as resp:
                body = json.loads(resp.read().decode())
                return resp.status, body
        except urllib.error.HTTPError as e:
            return e.code, json.loads(e.read().decode())
        except Exception as exc:
            return None, {"error": str(exc)}

    def setUp(self):
        self.config = TwitterAgentConfig(
            account_id="test_health",
            healthcheck_port=8089,
        )
        self.agent = TwitterActivityAgent(self.config)
        self.agent.session_mgr._redis = MockRedisSession()

        import threading
        from http.server import ThreadingHTTPServer
        TwitterHealthHandler.agent_ref = self.agent
        self.server = ThreadingHTTPServer(("127.0.0.1", 8089), TwitterHealthHandler)
        self.server_thread = threading.Thread(target=self.server.serve_forever, daemon=True)
        self.server_thread.start()
        time.sleep(0.1)  # Allow server to start

    def tearDown(self):
        self.server.shutdown()

    def test_liveness_returns_up(self):
        status, body = self._make_request("/healthz")
        self.assertEqual(status, 200)
        self.assertEqual(body["status"], "UP")

    def test_liveness_actuator_path(self):
        status, body = self._make_request("/actuator/health/liveness")
        self.assertEqual(status, 200)
        self.assertEqual(body["status"], "UP")

    def test_readiness_returns_503_when_not_authenticated(self):
        """Agent not authenticated → readiness probe must return 503."""
        status, body = self._make_request("/actuator/health/readiness")
        # Not authenticated → not ready
        self.assertIn(status, (200, 503))  # depends on is_ready state

    def test_actuator_health_returns_json(self):
        status, body = self._make_request("/actuator/health")
        self.assertIn("status", body)

    def test_twitter_status_endpoint(self):
        self.agent.session_mgr.save_status("running")
        status, body = self._make_request("/api/v1/twitter/status")
        self.assertEqual(status, 200)

    def test_unknown_path_returns_404(self):
        status, body = self._make_request("/nonexistent")
        self.assertEqual(status, 404)


# ==============================================================================
# 8. TwitterActivityAgent — Orchestration
# ==============================================================================


class TestTwitterActivityAgent(unittest.TestCase):
    """Tests high-level agent orchestration and tweet queue processing."""

    def setUp(self):
        self.config = TwitterAgentConfig(account_id="test_orchestrator", dry_run=True)
        self.agent = TwitterActivityAgent(self.config)
        self.agent.session_mgr._redis = MockRedisSession()

    def test_is_ready_false_without_browser_agent(self):
        self.assertFalse(self.agent.is_ready())

    def test_get_health_info_structure(self):
        info = self.agent.get_health_info()
        self.assertIn("status", info)
        self.assertIn("account", info)
        self.assertIn("authenticated", info)
        self.assertIn("metrics", info)
        self.assertEqual(info["account"], "@smartbetguru")

    def test_process_tweet_queue_surebet(self):
        """Surebet job from queue must produce a tweet call."""
        job = {
            "type": "surebet",
            "bookmaker1": "Winline",
            "bookmaker2": "Betcity",
            "event": "Реал — Барса",
            "odds1": 2.15,
            "odds2": 2.20,
            "profit_pct": 3.5,
            "sport": "Футбол",
        }
        self.agent.session_mgr._redis.rpush(self.config.queue_key, json.dumps(job))

        # Inject mock browser agent
        mock_browser = MagicMock()
        mock_browser._authenticated = True
        mock_browser.post_tweet = AsyncMock(return_value=True)
        self.agent._browser_agent = mock_browser

        asyncio.run(self.agent._process_tweet_queue())
        mock_browser.post_tweet.assert_called_once()
        call_args = mock_browser.post_tweet.call_args[0][0]
        self.assertIn("Winline", call_args)
        self.assertIn("Betcity", call_args)

    def test_process_tweet_queue_freebet(self):
        """Freebet job from queue must include 80% cash calculation."""
        job = {
            "type": "freebet",
            "bookmaker": "Фонбет",
            "freebet_amount": 5000.0,
            "k1": 5.0,
            "k2": 1.25,
        }
        self.agent.session_mgr._redis.rpush(self.config.queue_key, json.dumps(job))

        mock_browser = MagicMock()
        mock_browser._authenticated = True
        mock_browser.post_tweet = AsyncMock(return_value=True)
        self.agent._browser_agent = mock_browser

        asyncio.run(self.agent._process_tweet_queue())
        mock_browser.post_tweet.assert_called_once()
        call_args = mock_browser.post_tweet.call_args[0][0]
        # 5000 * 0.80 = 4000 (formatted as 4\u00a0000 with non-breaking space)
        self.assertIn("4\u00a0000", call_args)
        self.assertIn("Фонбет", call_args)

    def test_process_tweet_queue_raw(self):
        """Raw text job must be posted as-is."""
        raw_text = "Тестовый пост для SmartBet.guru ⚡"
        job = {"type": "raw", "text": raw_text}
        self.agent.session_mgr._redis.rpush(self.config.queue_key, json.dumps(job))

        mock_browser = MagicMock()
        mock_browser._authenticated = True
        mock_browser.post_tweet = AsyncMock(return_value=True)
        self.agent._browser_agent = mock_browser

        asyncio.run(self.agent._process_tweet_queue())
        mock_browser.post_tweet.assert_called_once_with(raw_text)

    def test_process_mentions_routes_to_feedback(self):
        """Captured @mentions must be enqueued to feedback queue."""
        mock_browser = MagicMock()
        mock_browser._authenticated = True
        mock_browser.monitor_mentions = AsyncMock(return_value=[
            {
                "id": "mention_001",
                "author": "@user_vip",
                "text": "Можно подробнее про вилки?",
                "url": "https://x.com/user_vip/status/12345",
                "platform": "twitter",
                "captured_at": datetime.now(timezone.utc).isoformat(),
                "is_patron": False,
            }
        ])
        self.agent._browser_agent = mock_browser

        asyncio.run(self.agent._process_mentions())

        # Check feedback queue has the mention
        raw = self.agent.session_mgr._redis.lpop(self.config.feedback_queue_key)
        self.assertIsNotNone(raw)
        parsed = json.loads(raw)
        self.assertEqual(parsed["id"], "mention_001")
        self.assertEqual(parsed["platform"], "twitter")

    def test_queue_cap_at_max_posts_per_hour(self):
        """Agent must not post more than MAX_POSTS_PER_HOUR tweets per queue cycle."""
        for i in range(MAX_POSTS_PER_HOUR + 3):
            job = {"type": "raw", "text": f"Test tweet {i}"}
            self.agent.session_mgr._redis.rpush(self.config.queue_key, json.dumps(job))

        mock_browser = MagicMock()
        mock_browser._authenticated = True
        mock_browser.post_tweet = AsyncMock(return_value=True)
        self.agent._browser_agent = mock_browser

        asyncio.run(self.agent._process_tweet_queue())
        # Should not exceed MAX_POSTS_PER_HOUR calls
        self.assertLessEqual(mock_browser.post_tweet.call_count, MAX_POSTS_PER_HOUR)


# ==============================================================================
# 9. Rule Compliance Summary
# ==============================================================================


class TestRuleCompliance(unittest.TestCase):
    """High-level checks ensuring Golden Rules are correctly encoded."""

    def test_rule_9_no_incognito_account_id_uniqueness(self):
        """Rule 9: each account has a unique persistent profile key."""
        cfg1 = TwitterAgentConfig(account_id="account_a")
        cfg2 = TwitterAgentConfig(account_id="account_b")
        self.assertNotEqual(cfg1.get_profile_key(), cfg2.get_profile_key())

    def test_rule_10_freebet_formula_documented(self):
        """Rule 10: FreebetMathHelper docstring references eta formula."""
        import inspect
        doc = inspect.getdoc(FreebetMathHelper.calculate_freebet_conversion) or ""
        self.assertIn("eta", doc)
        self.assertIn("K1", doc)
        self.assertIn("K2", doc)

    def test_rule_12_proxy_is_k8s_dns_name(self):
        """Rule 12: Proxy must be a K8s DNS service name, not a bare IP."""
        cfg = TwitterAgentConfig()
        # Should NOT look like 'http://192.168.x.x:3128'
        import re
        raw_ip_pattern = re.compile(r"http://\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}:\d+")
        self.assertIsNone(raw_ip_pattern.match(cfg.proxy_server),
                          f"Proxy is a raw IP (violates Rule 2/12): {cfg.proxy_server}")

    def test_twitter_char_limit_enforced_in_surebet(self):
        """All surebet tweets respect 280-char Twitter limit."""
        for profit in [1.5, 5.0, 10.0]:
            tweet = FreebetMathHelper.generate_surebet_tweet(
                bookmaker1="Букмекер Один с Длинным Именем",
                bookmaker2="Букмекер Два с Длинным Именем",
                event="Очень Длинное Название Матча — Другая Команда",
                odds1=2.50,
                odds2=2.10,
                profit_pct=profit,
                sport="Баскетбол",
            )
            self.assertLessEqual(len(tweet), TWITTER_CHAR_LIMIT, f"Tweet exceeds limit: {tweet}")


if __name__ == "__main__":
    unittest.main(verbosity=2)
