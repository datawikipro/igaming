#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Unit tests for Reddit Crowd Marketing Agent (smm-agent/reddit_agent.py)
Covers Golden Rules: Rule 6 (US Proxy), Rule 9 (Warmup & Session), Rule 10 (80% Freebet Formula)
"""

import asyncio
import http.client
import json
import os
import sys
import unittest
from unittest.mock import MagicMock, patch

# Add parent directory to sys.path so we can import modules
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from reddit_agent import (
    MockRedis,
    RedditConfig,
    RedditCrowdAgent,
    RedditCrowdGenerator,
    RedditCrowdReply,
    RedditMathEngine,
    RedditPost,
    RedditSessionManager,
    RedditWarmupRoutine,
    start_healthcheck_server,
)


class TestRedditConfig(unittest.TestCase):
    """Verifies configuration defaults and environment overrides."""

    def test_default_config(self):
        cfg = RedditConfig()
        self.assertEqual(cfg.account_id, "reddit_crowd_agent")
        self.assertEqual(cfg.session_key, "smm:session:reddit")
        self.assertIn("sportsbook", cfg.target_subreddits)
        self.assertIn("Arbing", cfg.target_subreddits)
        self.assertEqual(cfg.proxy_server, "http://100.83.113.50:3128")
        self.assertEqual(cfg.healthcheck_port, 8080)
        self.assertEqual(cfg.cooldown_seconds, 300)

    def test_to_dict(self):
        cfg = RedditConfig(account_id="custom_agent_test")
        d = cfg.to_dict()
        self.assertEqual(d["account_id"], "custom_agent_test")
        self.assertEqual(d["proxy_server"], "http://100.83.113.50:3128")
        self.assertIn("matchedbetting", d["target_subreddits"])


class TestRedditSessionManager(unittest.TestCase):
    """Tests deduplication, session persistence, and feedback enqueuing."""

    def setUp(self):
        self.config = RedditConfig(account_id="test_session")
        self.mock_redis = MockRedis()
        self.mgr = RedditSessionManager(self.config, redis_client=self.mock_redis)

    def test_post_deduplication(self):
        post_id = "post_abc_123"
        self.assertFalse(self.mgr.is_post_processed(post_id))
        self.mgr.mark_post_processed(post_id)
        self.assertTrue(self.mgr.is_post_processed(post_id))

    def test_cooldown_behavior(self):
        self.assertFalse(self.mgr.is_in_cooldown())
        self.mgr.update_cooldown()
        self.assertTrue(self.mgr.is_in_cooldown())

    def test_enqueue_lead(self):
        lead = {
            "source_platform": "REDDIT",
            "post_id": "test_lead_01",
            "title": "Need matched betting help",
            "topic": "freebet_snr",
        }
        self.mgr.enqueue_lead(lead)
        self.assertEqual(self.mock_redis.llen("feedback:queue:reddit"), 1)
        raw = self.mock_redis.rpop("feedback:queue:reddit")
        self.assertIsNotNone(raw)
        data = json.loads(raw)
        self.assertEqual(data["post_id"], "test_lead_01")

    def test_session_save_and_load(self):
        session_data = {"token": "sample_token", "expires_at": 1893456000}
        self.mgr.save_session(session_data)
        loaded = self.mgr.load_session()
        self.assertEqual(loaded, session_data)


class TestRedditMathEngine(unittest.TestCase):
    """Verifies Matched Betting SNR formula (Rule 10) and Arbitrage formulas."""

    def test_snr_conversion_formula_80_percent(self):
        """
        Rule 10: Freebet (SNR) ~ 80% guaranteed cash via formula:
        eta = ((K1 - 1) * (K2 - 1)) / K2.
        With K1=5.0 and K2=1.25:
        (5.0 - 1) * (1.25 - 1) / 1.25 = 4 * 0.25 / 1.25 = 1 / 1.25 = 0.80 (80%).
        """
        freebet = 100.0
        res = RedditMathEngine.calculate_freebet_conversion(freebet, k1_back=5.0, k2_lay_hedge=1.25)
        self.assertEqual(res["guaranteed_cash"], 80.0)
        self.assertEqual(res["conversion_rate_pct"], 80.0)
        self.assertEqual(res["hedge_stake"], 320.0)

    def test_snr_conversion_invalid_odds(self):
        res = RedditMathEngine.calculate_freebet_conversion(100.0, k1_back=0.5, k2_lay_hedge=1.0)
        self.assertEqual(res["guaranteed_cash"], 0.0)

    def test_surebet_calculation(self):
        # 2.15 vs 1.95 -> 1/2.15 + 1/1.95 = 0.4651 + 0.5128 = 0.9779 (< 1.0 => Arb!)
        res = RedditMathEngine.calculate_surebet(2.15, 1.95, total_bankroll=100.0)
        self.assertTrue(res["is_arb"])
        self.assertGreater(res["profit_pct"], 2.0)
        self.assertAlmostEqual(res["stake1"] + res["stake2"], 100.0, delta=1.0)


class TestRedditCrowdGenerator(unittest.TestCase):
    """Tests post classification and crowd reply formatting."""

    def setUp(self):
        self.cfg = RedditConfig()
        self.generator = RedditCrowdGenerator(self.cfg)

    def test_classify_freebet_topic(self):
        post = RedditPost(
            id="f1",
            subreddit="sportsbook",
            title="How to get value out of bonus bet voucher?",
            author="bettor_1",
            selftext="DraftKings gave me a $50 free bet. What do you guys do with it?",
            url="http://reddit.com/r/sportsbook/f1",
            created_utc=0,
        )
        topic = self.generator.classify_post(post)
        self.assertEqual(topic, "freebet_snr")
        reply = self.generator.generate_reply(post)
        self.assertIsNotNone(reply)
        self.assertIn("80% guaranteed cash", reply.reply_text)
        self.assertIn("utm_source=reddit", reply.reply_text)
        self.assertIn("utm_campaign=r_sportsbook_freebet_snr", reply.reply_text)

    def test_classify_arbitrage_topic(self):
        post = RedditPost(
            id="a1",
            subreddit="Arbing",
            title="Two-way arbitrage scanner and line shopping",
            author="arber_2",
            selftext="Trying to find surebet opportunities between Pinnacle and soft retail books.",
            url="http://reddit.com/r/Arbing/a1",
            created_utc=0,
        )
        topic = self.generator.classify_post(post)
        self.assertEqual(topic, "arbitrage")
        reply = self.generator.generate_reply(post)
        self.assertIsNotNone(reply)
        self.assertIn("Margin equation", reply.reply_text)
        self.assertIn("utm_source=reddit", reply.reply_text)

    def test_classify_unrelated_topic(self):
        post = RedditPost(
            id="u1",
            subreddit="sportsbook",
            title="Who do you like tonight for NBA player props?",
            author="nba_fan",
            selftext="Lakers vs Celtics game, any thoughts?",
            url="http://reddit.com/r/sportsbook/u1",
            created_utc=0,
        )
        topic = self.generator.classify_post(post)
        self.assertIsNone(topic)
        reply = self.generator.generate_reply(post)
        self.assertIsNone(reply)


class TestRedditWarmupRoutine(unittest.TestCase):
    """Tests simulated warmup routine."""

    def test_http_warmup(self):
        cfg = RedditConfig()
        warmup = RedditWarmupRoutine(cfg)
        res = asyncio.run(warmup.execute_http_warmup(duration_seconds=2))
        self.assertEqual(res["status"], "SUCCESS")
        self.assertGreaterEqual(res["visited_count"], 1)


class TestRedditCrowdAgent(unittest.TestCase):
    """Tests agent execution cycle and metrics."""

    def setUp(self):
        self.cfg = RedditConfig(cooldown_seconds=0, dry_run=True, max_replies_per_run=20)
        self.agent = RedditCrowdAgent(self.cfg)
        self.agent.session_mgr.client = MockRedis()

    def test_crowd_cycle_execution(self):
        result = self.agent.run_crowd_cycle()
        self.assertEqual(result["status"], "SUCCESS")
        self.assertGreaterEqual(result["posts_scanned"], 2)
        self.assertGreaterEqual(result["leads_captured"], 1)
        self.assertGreaterEqual(self.agent.replies_posted, 1)

    def test_deduplication_on_second_cycle(self):
        # First cycle processes sandbox posts
        res1 = self.agent.run_crowd_cycle()
        first_replies = res1["replies_posted"]
        # Second cycle should see posts already processed
        res2 = self.agent.run_crowd_cycle()
        self.assertEqual(res2["replies_posted"], 0)


class TestHealthcheckServer(unittest.TestCase):
    """Tests K8s healthcheck probe endpoint (/healthz and /actuator/health)."""

    def test_healthz_endpoint(self):
        cfg = RedditConfig(healthcheck_port=18088)
        agent = RedditCrowdAgent(cfg)
        server = start_healthcheck_server(agent, 18088)
        try:
            conn = http.client.HTTPConnection("127.0.0.1", 18088, timeout=2)
            conn.request("GET", "/healthz")
            resp = conn.getresponse()
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertEqual(data["status"], "UP")
            self.assertEqual(data["component"], "smm-bot-reddit")

            # Check actuator probe
            conn.request("GET", "/actuator/health")
            resp_actuator = conn.getresponse()
            self.assertEqual(resp_actuator.status, 200)
            conn.close()
        finally:
            server.shutdown()


if __name__ == "__main__":
    unittest.main()
