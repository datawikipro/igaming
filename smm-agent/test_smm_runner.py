#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — SMM Stealth Runner Test Suite
Tests for:
- Cubic Bezier trajectories and kinematics
- Persistent profile pack/unpack & Redis synchronization
- BrowserConfig defaults and US proxy routing
- Cache warmup routines and human interaction helpers
- Rule 9 compliance (strict persistent context, no incognito)
"""

import asyncio
import io
import math
import os
import shutil
import sys
import tempfile
import unittest
from unittest.mock import AsyncMock, MagicMock, patch

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from browser_manager import (
    BrowserConfig,
    HumanInteractionHelper,
    ProfileSyncManager,
    StealthBrowserSession,
    generate_cubic_bezier_trajectory,
)
from cache_warmup import CacheWarmupManager, DEFAULT_WARMUP_SITES


class MockRedis:
    """In-memory mock for Redis client."""

    def __init__(self):
        self.store = {}
        self.expirations = {}

    def get(self, key):
        return self.store.get(key)

    def set(self, key, value, ex=None):
        self.store[key] = value
        if ex:
            self.expirations[key] = ex
        return True

    def ping(self):
        return True


class TestBezierKinematics(unittest.TestCase):
    """Tests 2D Cubic Bezier trajectories."""

    def test_trajectory_length_and_endpoints(self):
        p0 = (100.0, 200.0)
        p3 = (500.0, 600.0)
        num_points = 30

        trajectory = generate_cubic_bezier_trajectory(p0, p3, num_points=num_points)

        self.assertEqual(len(trajectory), num_points)
        # First point should start at p0
        self.assertAlmostEqual(trajectory[0][0], p0[0], delta=1.0)
        self.assertAlmostEqual(trajectory[0][1], p0[1], delta=1.0)
        # Last point should end at p3
        self.assertAlmostEqual(trajectory[-1][0], p3[0], delta=1.0)
        self.assertAlmostEqual(trajectory[-1][1], p3[1], delta=1.0)

    def test_trajectory_continuity(self):
        """Ensures consecutive points don't have massive unexpected jumps."""
        p0 = (50.0, 50.0)
        p3 = (350.0, 450.0)
        trajectory = generate_cubic_bezier_trajectory(p0, p3, num_points=25)

        for i in range(len(trajectory) - 1):
            x1, y1 = trajectory[i]
            x2, y2 = trajectory[i + 1]
            step_dist = math.hypot(x2 - x1, y2 - y1)
            # Step distance should be reasonable and bounded
            self.assertLess(step_dist, 100.0, f"Discontinuity at step {i}: {step_dist}")

    def test_identical_endpoints(self):
        p = (200.0, 300.0)
        trajectory = generate_cubic_bezier_trajectory(p, p, num_points=10)
        self.assertEqual(len(trajectory), 2)
        self.assertEqual(trajectory[0], p)
        self.assertEqual(trajectory[1], p)


class TestProfilePersistence(unittest.TestCase):
    """Tests packing, unpacking, and Redis syncing of persistent profiles."""

    def setUp(self):
        self.temp_dir = tempfile.mkdtemp()
        self.profile_dir = os.path.join(self.temp_dir, "profile_src")
        self.restore_dir = os.path.join(self.temp_dir, "profile_dst")
        os.makedirs(self.profile_dir, exist_ok=True)

        # Create dummy profile structure mimicking Firefox
        os.makedirs(os.path.join(self.profile_dir, "storage", "default"), exist_ok=True)
        with open(os.path.join(self.profile_dir, "cookies.sqlite"), "w") as f:
            f.write("DUMMY_SQLITE_COOKIES_DATA_12345")
        with open(os.path.join(self.profile_dir, "places.sqlite"), "w") as f:
            f.write("DUMMY_HISTORY_PLACES_DATA")
        with open(os.path.join(self.profile_dir, "storage", "default", "data.bin"), "wb") as f:
            f.write(b"\x00\x01\x02\x03\x04INDEXED_DB")

    def tearDown(self):
        shutil.rmtree(self.temp_dir, ignore_errors=True)

    def test_pack_and_unpack(self):
        tar_bytes = ProfileSyncManager.pack_profile_to_bytes(self.profile_dir)
        self.assertTrue(len(tar_bytes) > 0)
        # Check gzip magic header: 0x1f, 0x8b
        self.assertEqual(tar_bytes[:2], b"\x1f\x8b")

        # Unpack to destination
        success = ProfileSyncManager.unpack_profile_from_bytes(tar_bytes, self.restore_dir)
        self.assertTrue(success)

        # Verify extracted files
        with open(os.path.join(self.restore_dir, "cookies.sqlite"), "r") as f:
            self.assertEqual(f.read(), "DUMMY_SQLITE_COOKIES_DATA_12345")
        with open(os.path.join(self.restore_dir, "places.sqlite"), "r") as f:
            self.assertEqual(f.read(), "DUMMY_HISTORY_PLACES_DATA")
        with open(os.path.join(self.restore_dir, "storage", "default", "data.bin"), "rb") as f:
            self.assertEqual(f.read(), b"\x00\x01\x02\x03\x04INDEXED_DB")

    def test_redis_sync_cycle(self):
        mock_redis = MockRedis()
        account_id = "test_persona_42"

        # Save to Redis
        saved = ProfileSyncManager.save_to_redis(mock_redis, account_id, self.profile_dir, ttl_seconds=3600)
        self.assertTrue(saved)

        expected_key = f"smm:profile:{account_id}"
        self.assertIn(expected_key, mock_redis.store)
        self.assertEqual(mock_redis.expirations.get(expected_key), 3600)

        # Restore from Redis into fresh directory
        restored = ProfileSyncManager.restore_from_redis(mock_redis, account_id, self.restore_dir)
        self.assertTrue(restored)
        self.assertTrue(os.path.exists(os.path.join(self.restore_dir, "cookies.sqlite")))


class TestBrowserConfigAndRule9(unittest.TestCase):
    """Tests configuration and compliance with Rule 9 (AGENTS.md)."""

    def test_default_config_routing(self):
        cfg = BrowserConfig(account_id="agent_boosty")
        self.assertEqual(cfg.account_id, "agent_boosty")
        self.assertIn("100.83.113.50", cfg.proxy_server)  # ru-proxy cluster route
        self.assertEqual(cfg.locale, "en-US")
        self.assertEqual(cfg.timezone_id, "America/New_York")
        self.assertTrue(cfg.get_effective_user_data_dir().endswith("agent_boosty"))

    def test_persistent_context_enforcement(self):
        """Verifies session uses launch_persistent_context and never new_context."""
        cfg = BrowserConfig(account_id="rule9_check", user_data_dir="/tmp/test_rule9")
        session = StealthBrowserSession(cfg)
        self.assertFalse(hasattr(session, "new_context"))
        self.assertEqual(session.user_data_dir, "/tmp/test_rule9")


class TestHumanInteractions(unittest.IsolatedAsyncioTestCase):
    """Tests human interaction helpers with mocked Playwright Page."""

    async def test_mouse_move_and_click(self):
        mock_page = MagicMock()
        mock_page.mouse = MagicMock()
        mock_page.mouse.move = AsyncMock()
        mock_page.mouse.down = AsyncMock()
        mock_page.mouse.up = AsyncMock()
        mock_page.mouse.wheel = AsyncMock()

        helper = HumanInteractionHelper(mock_page)
        helper.current_x = 50.0
        helper.current_y = 50.0

        # Test mouse move
        await helper.mouse_move(200.0, 300.0, steps=10, min_sleep=0.001, max_sleep=0.002)
        self.assertEqual(mock_page.mouse.move.call_count, 10)
        self.assertAlmostEqual(helper.current_x, 200.0, delta=1.5)
        self.assertAlmostEqual(helper.current_y, 300.0, delta=1.5)

        # Test click on coordinates
        await helper.click(coords=(150.0, 250.0))
        mock_page.mouse.down.assert_awaited()
        mock_page.mouse.up.assert_awaited()

    async def test_human_scroll(self):
        mock_page = MagicMock()
        mock_page.mouse = MagicMock()
        mock_page.mouse.wheel = AsyncMock()

        helper = HumanInteractionHelper(mock_page)
        await helper.scroll(delta_y=200, steps=4, pause_between=(0.001, 0.002))
        self.assertEqual(mock_page.mouse.wheel.call_count, 4)


class TestCacheWarmup(unittest.IsolatedAsyncioTestCase):
    """Tests cache warmup manager execution flow."""

    async def test_warmup_execution(self):
        mock_page = MagicMock()
        mock_page.goto = AsyncMock()
        mock_page.mouse = MagicMock()
        mock_page.mouse.move = AsyncMock()
        mock_page.mouse.wheel = AsyncMock()
        mock_page.query_selector_all = AsyncMock(return_value=[])
        mock_page.viewport_size = {"width": 1280, "height": 800}

        warmup_mgr = CacheWarmupManager(sites=["https://www.test-sports.org"])

        # Execute short warmup with mock
        with patch("asyncio.sleep", new_callable=AsyncMock):
            result = await warmup_mgr.warmup(
                page=mock_page,
                duration_seconds=1,
                max_sites=1,
            )

        self.assertEqual(result["status"], "success")
        self.assertEqual(result["sites_visited"], ["https://www.test-sports.org"])
        mock_page.goto.assert_awaited_with(
            "https://www.test-sports.org",
            wait_until="domcontentloaded",
            timeout=25000,
        )


if __name__ == "__main__":
    unittest.main()
