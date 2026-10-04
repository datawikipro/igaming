#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Threads Surebet Agent Test Suite
Task: [smm-threads] Meta Threads: браузерный агент Camoufox (Gecko), прогрев кэша и публикация вилочных тредов

Tests for:
- Surebet fetcher (mock data & portal API fallback)
- Threads post formatter (compliance with Rule 10 disclaimer, UTM links, char limits)
- ThreadsAgentConfig and session manager
- HTTP healthcheck endpoints (/healthz, /actuator/health, /api/v1/threads/status)
- Full dry-run surebet cycle
- Cache warmup integration (Rule 9)
- Publisher dry-run path
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

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from threads_agent import (
    MANDATORY_DISCLAIMER,
    MockRedis,
    SurebetFetcher,
    ThreadsAgentConfig,
    ThreadsBrowserPublisher,
    ThreadsHealthHandler,
    ThreadsPostFormatter,
    ThreadsSessionManager,
    ThreadsSurebetAgent,
    start_health_server,
)


# ==============================================================================
# 1. SurebetFetcher
# ==============================================================================

class TestSurebetFetcher(unittest.TestCase):
    """Tests surebet fetcher mock fallback and filtering."""

    def setUp(self):
        self.fetcher = SurebetFetcher(
            portal_api_url="http://igaming-portal:80",
            min_profit_percent=1.5,
        )

    def test_mock_data_returned_on_requests_unavailable(self):
        """When requests is not importable, mock data must be returned."""
        with patch.object(SurebetFetcher, "_filter_mock", wraps=self.fetcher._filter_mock) as m:
            with patch("threads_agent.requests", None):
                surebets = self.fetcher.fetch(limit=3)
        self.assertIsInstance(surebets, list)
        self.assertGreater(len(surebets), 0)

    def test_mock_data_filtered_by_profit(self):
        """Only surebets above min_profit_percent threshold are returned."""
        high_fetcher = SurebetFetcher(
            portal_api_url="http://igaming-portal:80",
            min_profit_percent=99.0,
        )
        result = high_fetcher._filter_mock(10)
        self.assertEqual(len(result), 0)

    def test_mock_data_respects_limit(self):
        result = self.fetcher._filter_mock(1)
        self.assertLessEqual(len(result), 1)

    def test_mock_data_structure(self):
        surebets = self.fetcher._filter_mock(5)
        for sb in surebets:
            self.assertIn("event", sb)
            self.assertIn("profit_percent", sb)
            self.assertIn("bookmaker_1", sb)
            self.assertIn("bookmaker_2", sb)
            self.assertIn("odds_1", sb)
            self.assertIn("odds_2", sb)

    def test_portal_fallback_on_connection_error(self):
        """Connection errors to portal must fall back to mock data gracefully."""
        with patch("threads_agent.requests") as mock_req:
            mock_req.get.side_effect = ConnectionError("refused")
            surebets = self.fetcher.fetch(limit=2)
        self.assertIsInstance(surebets, list)
        self.assertGreater(len(surebets), 0)


# ==============================================================================
# 2. ThreadsPostFormatter
# ==============================================================================

class TestThreadsPostFormatter(unittest.TestCase):
    """Tests post formatting, compliance validation, and character limits."""

    SAMPLE_SUREBET = {
        "id": "sb-test-001",
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
    }

    def test_post_contains_mandatory_disclaimer(self):
        """Rule 10: every post MUST contain the responsible gambling disclaimer."""
        post = ThreadsPostFormatter.format_surebet_post(self.SAMPLE_SUREBET)
        self.assertIn(MANDATORY_DISCLAIMER, post)

    def test_compliance_validation_passes(self):
        post = ThreadsPostFormatter.format_surebet_post(self.SAMPLE_SUREBET)
        self.assertTrue(ThreadsPostFormatter.validate_compliance(post))

    def test_compliance_validation_fails_without_disclaimer(self):
        self.assertFalse(ThreadsPostFormatter.validate_compliance("Some post without disclaimer"))

    def test_post_contains_bookmaker_names(self):
        post = ThreadsPostFormatter.format_surebet_post(self.SAMPLE_SUREBET)
        self.assertIn("Winline", post)
        self.assertIn("Betcity", post)

    def test_post_contains_utm_affiliate_link(self):
        post = ThreadsPostFormatter.format_surebet_post(self.SAMPLE_SUREBET)
        self.assertIn("utm_source=meta", post)
        self.assertIn("utm_medium=threads", post)
        self.assertIn("smartbet.guru", post)

    def test_post_contains_profit_percent(self):
        post = ThreadsPostFormatter.format_surebet_post(self.SAMPLE_SUREBET)
        self.assertIn("+2.4%", post)

    def test_post_contains_sport_emoji(self):
        post = ThreadsPostFormatter.format_surebet_post(self.SAMPLE_SUREBET)
        self.assertIn("⚽", post)  # Футбол → ⚽

    def test_post_char_limit(self):
        """Post must not exceed THREADS_MAX_CHARS (500) characters."""
        from threads_agent import THREADS_MAX_CHARS
        post = ThreadsPostFormatter.format_surebet_post(self.SAMPLE_SUREBET)
        self.assertLessEqual(len(post), THREADS_MAX_CHARS)

    def test_post_with_missing_time(self):
        """Posts with missing starts_at must still be formatted cleanly."""
        sb = dict(self.SAMPLE_SUREBET)
        sb["starts_at"] = ""
        post = ThreadsPostFormatter.format_surebet_post(sb)
        self.assertIn(MANDATORY_DISCLAIMER, post)

    def test_post_with_unknown_sport(self):
        """Unknown sports must fall back to default emoji."""
        sb = dict(self.SAMPLE_SUREBET)
        sb["sport"] = "Бадминтон"
        post = ThreadsPostFormatter.format_surebet_post(sb)
        self.assertIn("🎯", post)  # default emoji

    def test_all_mock_surebets_pass_compliance(self):
        """All built-in mock surebets must produce compliant posts."""
        for sb in SurebetFetcher.MOCK_SUREBETS:
            post = ThreadsPostFormatter.format_surebet_post(sb)
            self.assertTrue(
                ThreadsPostFormatter.validate_compliance(post),
                msg=f"Compliance failed for surebet: {sb.get('id')}",
            )


# ==============================================================================
# 3. ThreadsAgentConfig & SessionManager
# ==============================================================================

class TestThreadsAgentConfig(unittest.TestCase):
    """Tests configuration defaults and to_dict serialization."""

    def test_default_proxy_rule6(self):
        """Rule 6: default US proxy must route through cluster proxy."""
        cfg = ThreadsAgentConfig()
        self.assertEqual(cfg.proxy_server, "http://100.83.113.50:3128")

    def test_default_healthcheck_port(self):
        cfg = ThreadsAgentConfig()
        self.assertEqual(cfg.healthcheck_port, 8081)

    def test_to_dict_serialization(self):
        cfg = ThreadsAgentConfig(account_id="test_bot", dry_run=True)
        d = cfg.to_dict()
        self.assertEqual(d["account_id"], "test_bot")
        self.assertTrue(d["dry_run"])
        self.assertIn("surebets_per_cycle", d)
        self.assertIn("min_profit_percent", d)

    def test_browser_config_returns_none_when_unavailable(self):
        """When BrowserConfig is not installed, to_browser_config returns None gracefully."""
        with patch("threads_agent.BrowserConfig", None):
            cfg = ThreadsAgentConfig()
            result = cfg.to_browser_config()
        self.assertIsNone(result)


class TestThreadsSessionManager(unittest.TestCase):
    """Tests session CRUD operations against mock Redis."""

    def setUp(self):
        self.config = ThreadsAgentConfig(
            account_id="test_threads_bot",
            session_key="smm:test:threads",
        )
        self.mock_redis = MockRedis()
        self.mgr = ThreadsSessionManager(self.config, redis_client=self.mock_redis)

    def test_load_initial_session(self):
        session = self.mgr.load_session()
        self.assertEqual(session["account_id"], "test_threads_bot")
        self.assertEqual(session["status"], "INITIALIZED")
        self.assertEqual(session["total_posts_published"], 0)
        self.assertEqual(session["total_surebets_shared"], 0)

    def test_save_and_reload(self):
        data = self.mgr.load_session()
        data["total_posts_published"] = 5
        data["status"] = "HEALTHY"
        self.assertTrue(self.mgr.save_session(data))

        reloaded = self.mgr.load_session()
        self.assertEqual(reloaded["total_posts_published"], 5)
        self.assertEqual(reloaded["status"], "HEALTHY")

    def test_save_updates_timestamp(self):
        data = self.mgr.load_session()
        before = datetime.now(timezone.utc).isoformat()
        self.mgr.save_session(data)
        reloaded = self.mgr.load_session()
        self.assertGreaterEqual(reloaded["updated_at"], before)


# ==============================================================================
# 4. ThreadsBrowserPublisher — dry-run
# ==============================================================================

class TestThreadsBrowserPublisher(unittest.IsolatedAsyncioTestCase):
    """Tests browser publisher dry-run path (no real browser required)."""

    async def test_dry_run_returns_correct_status(self):
        pub = ThreadsBrowserPublisher()
        result = await pub.publish_post(
            page=None,
            human=None,
            post_text="Test post content. " + MANDATORY_DISCLAIMER,
            dry_run=True,
        )
        self.assertEqual(result["status"], "DRY_RUN")
        self.assertIn("post_text", result)
        self.assertIn("published_at", result)

    async def test_dry_run_logs_post_text(self):
        pub = ThreadsBrowserPublisher()
        post_text = "Surebet: Winline vs Betcity. " + MANDATORY_DISCLAIMER
        result = await pub.publish_post(page=None, human=None, post_text=post_text, dry_run=True)
        self.assertEqual(result["post_text"], post_text)


# ==============================================================================
# 5. ThreadsSurebetAgent — dry-run cycle
# ==============================================================================

class TestThreadsSurebetAgentDryRun(unittest.IsolatedAsyncioTestCase):
    """Tests full dry-run cycle without real browser or portal."""

    async def asyncSetUp(self):
        self.config = ThreadsAgentConfig(
            account_id="dry_run_agent",
            dry_run=True,
            surebets_per_cycle=2,
            min_profit_percent=1.0,
        )
        self.agent = ThreadsSurebetAgent(self.config, redis_client=MockRedis())

    async def test_dry_run_cycle_completes(self):
        result = await self.agent.run_surebet_cycle()
        self.assertEqual(result["status"], "COMPLETED")
        self.assertEqual(result["warmup"]["status"], "SKIPPED_DRY_RUN")
        self.assertIsInstance(result["posts"], list)

    async def test_dry_run_cycle_publishes_posts(self):
        result = await self.agent.run_surebet_cycle()
        self.assertGreater(len(result["posts"]), 0)
        for post_res in result["posts"]:
            self.assertEqual(post_res["status"], "DRY_RUN")

    async def test_dry_run_all_posts_compliant(self):
        result = await self.agent.run_surebet_cycle()
        for post_res in result["posts"]:
            self.assertIn(MANDATORY_DISCLAIMER, post_res["post_text"])

    async def test_dry_run_updates_session_counters(self):
        await self.agent.run_surebet_cycle()
        session = self.agent.session_mgr.load_session()
        self.assertGreater(session["total_posts_published"], 0)
        self.assertGreater(session["total_surebets_shared"], 0)
        self.assertIsNotNone(session["last_post_timestamp"])

    async def test_dry_run_warmup_skipped(self):
        result = await self.agent.run_surebet_cycle()
        self.assertEqual(result["warmup"]["status"], "SKIPPED_DRY_RUN")


# ==============================================================================
# 6. HTTP Healthcheck Server
# ==============================================================================

class TestThreadsHealthcheckServer(unittest.TestCase):
    """Tests embedded healthcheck HTTP endpoints for Kubernetes probes."""

    @classmethod
    def setUpClass(cls):
        cls.port = 18091  # Unique port to avoid conflict with meta_agent tests (18088)
        cls.config = ThreadsAgentConfig(
            account_id="hc_test_bot", healthcheck_port=cls.port
        )
        cls.agent = ThreadsSurebetAgent(cls.config, redis_client=MockRedis())
        cls.server = start_health_server(cls.agent, port=cls.port)
        time.sleep(0.1)

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()

    def _get(self, path: str) -> dict:
        url = f"http://127.0.0.1:{self.port}{path}"
        with urllib.request.urlopen(url) as resp:
            self.assertEqual(resp.status, 200)
            return json.loads(resp.read().decode("utf-8"))

    def test_healthz(self):
        data = self._get("/healthz")
        self.assertEqual(data["status"], "UP")
        self.assertEqual(data["service"], "smm-threads-agent")

    def test_actuator_health(self):
        data = self._get("/actuator/health")
        self.assertEqual(data["status"], "UP")

    def test_actuator_health_liveness(self):
        data = self._get("/actuator/health/liveness")
        self.assertEqual(data["status"], "UP")

    def test_actuator_health_readiness(self):
        data = self._get("/actuator/health/readiness")
        self.assertEqual(data["status"], "UP")

    def test_threads_status_endpoint(self):
        data = self._get("/api/v1/threads/status")
        self.assertEqual(data["status"], "UP")
        self.assertIn("config", data)
        self.assertIn("session", data)

    def test_threads_status_contains_config(self):
        data = self._get("/api/v1/threads/status")
        self.assertEqual(data["config"]["account_id"], "hc_test_bot")

    def test_preview_post_endpoint(self):
        url = f"http://127.0.0.1:{self.port}/api/v1/threads/preview-post"
        payload = json.dumps({
            "surebet": SurebetFetcher.MOCK_SUREBETS[0]
        }).encode("utf-8")
        req = urllib.request.Request(
            url, data=payload, headers={"Content-Type": "application/json"}
        )
        with urllib.request.urlopen(req) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
        self.assertIn("post_text", data)
        self.assertTrue(data["compliance_ok"])
        self.assertLessEqual(data["char_count"], 500)

    def test_not_found_returns_404(self):
        url = f"http://127.0.0.1:{self.port}/nonexistent"
        try:
            urllib.request.urlopen(url)
            self.fail("Expected HTTPError 404")
        except urllib.error.HTTPError as exc:
            self.assertEqual(exc.code, 404)


# ==============================================================================
# 7. Cache Warmup Integration
# ==============================================================================

class TestCacheWarmupIntegration(unittest.IsolatedAsyncioTestCase):
    """Tests that cache warmup is properly invoked during agent cycle."""

    async def asyncSetUp(self):
        self.config = ThreadsAgentConfig(
            account_id="warmup_test_agent",
            dry_run=False,  # warmup only — no real browser needed via mock
            warmup_duration_seconds=5,
        )
        self.agent = ThreadsSurebetAgent(self.config, redis_client=MockRedis())

    async def test_warmup_updates_session_timestamp(self):
        """After warmup, last_warmup_timestamp must be set in session."""
        mock_page = AsyncMock()
        mock_human = AsyncMock()

        with patch.object(
            self.agent.warmup_mgr, "warmup", new_callable=AsyncMock
        ) as mock_warmup:
            mock_warmup.return_value = {
                "status": "success",
                "duration_seconds": 5.0,
                "sites_visited": ["https://www.bbc.com/sport"],
            }
            result = await self.agent.execute_cache_warmup(mock_page, mock_human)

        self.assertEqual(result["status"], "success")
        session = self.agent.session_mgr.load_session()
        self.assertIsNotNone(session["last_warmup_timestamp"])

    async def test_warmup_called_with_correct_duration(self):
        """Warmup must use configured duration_seconds."""
        mock_page = AsyncMock()
        mock_human = AsyncMock()

        with patch.object(
            self.agent.warmup_mgr, "warmup", new_callable=AsyncMock
        ) as mock_warmup:
            mock_warmup.return_value = {"status": "success", "sites_visited": []}
            await self.agent.execute_cache_warmup(mock_page, mock_human)
            mock_warmup.assert_called_once()
            call_kwargs = mock_warmup.call_args[1]
            self.assertEqual(
                call_kwargs.get("duration_seconds"),
                self.config.warmup_duration_seconds,
            )

    async def test_warmup_graceful_when_manager_unavailable(self):
        """If CacheWarmupManager is None, warmup must return SKIPPED status gracefully."""
        self.agent.warmup_mgr = None
        mock_page = AsyncMock()
        result = await self.agent.execute_cache_warmup(mock_page, None)
        self.assertEqual(result["status"], "SKIPPED_NO_MANAGER")


if __name__ == "__main__":
    unittest.main()
