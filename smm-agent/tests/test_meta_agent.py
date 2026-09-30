#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Meta (Threads & Instagram) Activity Agent Test Suite
Tests for:
- Freebet SNR conversion formula (80% guaranteed cash) & Responsible Gambling disclaimer
- MetaAgentConfig & BrowserConfig inheritance
- Session persistence and metric counters in Redis
- HTTP healthcheck endpoints (/healthz, /actuator/health, /api/v1/meta/status)
- Organic feed browsing simulation and compliant post generation
- Rule 6 (US proxy routing) & Rule 9 (No incognito, persistent context, cache warmup) compliance
"""

import asyncio
from datetime import datetime, timezone
import json
import os
import sys
import time
import unittest
from unittest.mock import AsyncMock, MagicMock, patch
import urllib.request
import urllib.error

# Ensure module path is included
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from meta_agent import (
    FreebetMathHelper,
    MANDATORY_DISCLAIMER,
    MetaActivityAgent,
    MetaAgentConfig,
    MetaHealthHandler,
    MetaSessionManager,
    MockRedisSession,
    start_health_server,
)


class TestFreebetMath(unittest.TestCase):
    """Tests Rule 10 Freebet Matched Betting calculations and content verification."""

    def test_freebet_conversion_standard_80_percent(self):
        """
        Verify formula: eta = ((K1 - 1) * (K2 - 1)) / K2
        When K1 = 5.0, K2 = 1.25, Freebet = 3000:
        eta = (4.0 * 0.25) / 1.25 = 1.0 / 1.25 = 0.80 (80%)
        Guaranteed Cash = 3000 * 0.80 = 2400
        Hedge Stake = (3000 * 4.0) / 1.25 = 9600
        """
        res = FreebetMathHelper.calculate_freebet_conversion(k1=5.0, k2=1.25, freebet_amount=3000.0)

        self.assertAlmostEqual(res["conversion_rate"], 0.80, places=3)
        self.assertAlmostEqual(res["conversion_percentage"], 80.0, places=1)
        self.assertAlmostEqual(res["guaranteed_cash"], 2400.0, places=2)
        self.assertAlmostEqual(res["hedge_stake"], 9600.0, places=2)

    def test_freebet_conversion_different_odds(self):
        """Test with K1 = 6.0, K2 = 1.20, Freebet = 5000: eta = (5.0 * 0.2) / 1.20 = 0.8333."""
        res = FreebetMathHelper.calculate_freebet_conversion(k1=6.0, k2=1.20, freebet_amount=5000.0)
        self.assertGreater(res["conversion_rate"], 0.83)
        self.assertGreater(res["guaranteed_cash"], 4150.0)

    def test_invalid_odds_raise_error(self):
        with self.assertRaises(ValueError):
            FreebetMathHelper.calculate_freebet_conversion(k1=0.9, k2=1.5)
        with self.assertRaises(ValueError):
            FreebetMathHelper.calculate_freebet_conversion(k1=2.0, k2=1.0)

    def test_marketing_post_generation_and_disclaimer(self):
        """Post must contain bookmaker, 80% calculation, UTM link, and mandatory disclaimer."""
        post = FreebetMathHelper.generate_marketing_post(
            bookmaker="Winline",
            freebet_amount=3000.0,
            k1=5.0,
            k2=1.25,
            platform="threads",
        )

        self.assertIn("Winline", post)
        self.assertIn("2 400 ₽", post)
        self.assertIn("3 000 ₽", post)
        self.assertIn("utm_source=meta", post)
        self.assertIn("utm_medium=threads", post)
        self.assertIn(MANDATORY_DISCLAIMER, post)
        self.assertTrue(FreebetMathHelper.validate_content_compliance(post))

    def test_disclaimer_validation_rejects_missing(self):
        clean_text = "Check out our free bet calculator at https://smartbet.guru"
        self.assertFalse(FreebetMathHelper.validate_content_compliance(clean_text))


class TestMetaSessionManager(unittest.TestCase):
    """Tests session storage in Redis and mock fallback."""

    def test_default_session_initialization(self):
        config = MetaAgentConfig(account_id="test_persona_01", session_key="smm:test:session")
        mock_redis = MockRedisSession()
        mgr = MetaSessionManager(config, redis_client=mock_redis)

        session = mgr.load_session()
        self.assertEqual(session["account_id"], "test_persona_01")
        self.assertEqual(session["status"], "INITIALIZED")
        self.assertEqual(session["total_posts_published"], 0)
        self.assertTrue(session["threads_active"])
        self.assertTrue(session["instagram_active"])

    def test_save_and_reload_session(self):
        config = MetaAgentConfig(account_id="test_persona_02", session_key="smm:test:session")
        mock_redis = MockRedisSession()
        mgr = MetaSessionManager(config, redis_client=mock_redis)

        data = mgr.load_session()
        data["total_posts_published"] = 7
        data["total_likes_given"] = 23
        data["status"] = "HEALTHY"
        self.assertTrue(mgr.save_session(data))

        reloaded = mgr.load_session()
        self.assertEqual(reloaded["total_posts_published"], 7)
        self.assertEqual(reloaded["total_likes_given"], 23)
        self.assertEqual(reloaded["status"], "HEALTHY")


class TestMetaAgentConfig(unittest.TestCase):
    """Tests configuration and browser config conversion."""

    def test_config_defaults(self):
        cfg = MetaAgentConfig()
        self.assertEqual(cfg.account_id, "meta_persona_main")
        self.assertEqual(cfg.proxy_server, "http://100.83.113.50:3128")
        self.assertEqual(cfg.healthcheck_port, 8080)
        self.assertEqual(cfg.warmup_duration_seconds, 120)

    def test_conversion_to_browser_config(self):
        cfg = MetaAgentConfig(account_id="custom_meta_bot", headless=True, use_camoufox=False)
        b_cfg = cfg.to_browser_config()
        self.assertEqual(b_cfg.account_id, "custom_meta_bot")
        self.assertTrue(b_cfg.headless)
        self.assertEqual(b_cfg.proxy_server, "http://100.83.113.50:3128")
        self.assertIn("custom_meta_bot", b_cfg.get_effective_user_data_dir())


class TestHealthcheckServer(unittest.TestCase):
    """Tests embedded HTTP server responding to health and api endpoints."""

    @classmethod
    def setUpClass(cls):
        cls.port = 18088
        cls.config = MetaAgentConfig(account_id="test_server_bot", healthcheck_port=cls.port)
        cls.agent = MetaActivityAgent(cls.config, redis_client=MockRedisSession())
        cls.server = start_health_server(cls.agent, port=cls.port)
        time.sleep(0.1)

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()

    def test_healthz_endpoint(self):
        url = f"http://127.0.0.1:{self.port}/healthz"
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
            self.assertEqual(data["status"], "UP")
            self.assertEqual(data["service"], "smm-bot-meta")

    def test_actuator_health_endpoint(self):
        url = f"http://127.0.0.1:{self.port}/actuator/health"
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
            self.assertEqual(data["status"], "UP")

    def test_meta_status_endpoint(self):
        url = f"http://127.0.0.1:{self.port}/api/v1/meta/status"
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
            self.assertEqual(data["status"], "UP")
            self.assertIn("config", data)
            self.assertIn("session", data)

    def test_calculate_freebet_post_endpoint(self):
        url = f"http://127.0.0.1:{self.port}/api/v1/meta/calculate-freebet"
        payload = json.dumps({"k1": 5.0, "k2": 1.25, "freebet_amount": 3000.0}).encode("utf-8")
        req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"})
        with urllib.request.urlopen(req) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
            self.assertEqual(data["guaranteed_cash"], 2400.0)

    def test_publish_post_endpoint(self):
        url = f"http://127.0.0.1:{self.port}/api/v1/meta/post"
        payload = json.dumps({"bookmaker": "Fonbet", "freebet_amount": 2000.0, "platform": "threads"}).encode("utf-8")
        req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"})
        with urllib.request.urlopen(req) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
            self.assertTrue(data["compliance_verified"])
            self.assertIn(MANDATORY_DISCLAIMER, data["post_text"])


class TestMetaActivityRoutines(unittest.IsolatedAsyncioTestCase):
    """Tests browsing routines, cache warmup invocation, and post publication."""

    async def asyncSetUp(self):
        self.config = MetaAgentConfig(
            account_id="test_routine_agent",
            warmup_duration_seconds=2,
            dry_run=False,
        )
        self.agent = MetaActivityAgent(self.config, redis_client=MockRedisSession())

    async def test_publish_compliant_post_updates_stats(self):
        res = await self.agent.publish_compliant_post(bookmaker="Betcity", freebet_amount=4000.0)
        self.assertTrue(res["compliance_verified"])
        self.assertEqual(res["bookmaker"], "Betcity")

        session = self.agent.session_mgr.load_session()
        self.assertEqual(session["total_posts_published"], 1)

    @patch("asyncio.sleep", new_callable=AsyncMock)
    async def test_simulate_threads_browsing(self, mock_sleep):
        mock_page = AsyncMock()
        mock_human = AsyncMock()

        res = await self.agent.simulate_threads_browsing(mock_page, mock_human)
        self.assertEqual(res["platform"], "threads")
        self.assertEqual(res["status"], "SUCCESS")
        mock_page.goto.assert_called_once_with(self.config.threads_base_url, timeout=30000)

    @patch("asyncio.sleep", new_callable=AsyncMock)
    async def test_simulate_instagram_browsing(self, mock_sleep):
        mock_page = AsyncMock()
        mock_human = AsyncMock()

        res = await self.agent.simulate_instagram_browsing(mock_page, mock_human)
        self.assertEqual(res["platform"], "instagram")
        self.assertEqual(res["status"], "SUCCESS")
        mock_page.goto.assert_called_once_with(self.config.instagram_base_url, timeout=30000)

    async def test_execute_cache_warmup(self):
        mock_page = AsyncMock()
        mock_human = AsyncMock()

        with patch.object(self.agent.warmup_mgr, "warmup", new_callable=AsyncMock) as mock_warmup:
            mock_warmup.return_value = {"visited_sites": 2, "elapsed_seconds": 2.1}
            res = await self.agent.execute_cache_warmup(mock_page, mock_human)
            self.assertEqual(res["visited_sites"], 2)
            mock_warmup.assert_called_once()

    async def test_dry_run_cycle(self):
        dry_config = MetaAgentConfig(dry_run=True)
        dry_agent = MetaActivityAgent(dry_config, redis_client=MockRedisSession())
        res = await dry_agent.run_full_activity_cycle()
        self.assertEqual(res["status"], "COMPLETED")
        self.assertEqual(res["warmup"]["status"], "SKIPPED_DRY_RUN")
        self.assertTrue(res["publication"]["compliance_verified"])


if __name__ == "__main__":
    unittest.main()
