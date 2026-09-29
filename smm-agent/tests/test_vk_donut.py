#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Unit and Integration Tests for VK Donut Agent & Callback API
Task #74 [pod-vk-donut]
"""

import json
import sys
import threading
import time
import unittest
import urllib.error
import urllib.request
from http.server import ThreadingHTTPServer
from pathlib import Path

# Add smm-agent to path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from vk_donut_agent import (
    MockRedisSession,
    VkApiClient,
    VkDonutAgentService,
    VkDonutHttpHandler,
    VkDonutSessionManager,
    VkDonutSubscriber,
    agent_service
)


class TestVkDonutAgent(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        # Start background test HTTP server on ephemeral port
        cls.server = ThreadingHTTPServer(("127.0.0.1", 0), VkDonutHttpHandler)
        cls.port = cls.server.server_address[1]
        cls.server_thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.server_thread.start()
        time.sleep(0.1)

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()

    def setUp(self):
        self.mock_redis = MockRedisSession()
        self.session_mgr = VkDonutSessionManager()
        self.session_mgr._redis = self.mock_redis  # Inject mock redis

        self.vk_client = VkApiClient(access_token="dummy_token")
        self.agent = VkDonutAgentService(
            group_id=229123456,
            confirmation_code="test_confirm_code_123",
            secret_key="test_secret_xyz",
            session_mgr=self.session_mgr,
            vk_client=self.vk_client
        )
        # Update global service instance used by HTTP handler
        agent_service.group_id = 229123456
        agent_service.confirmation_code = "test_confirm_code_123"
        agent_service.secret_key = "test_secret_xyz"
        agent_service.session_mgr = self.session_mgr
        agent_service.vk_client = self.vk_client

    def test_confirmation_handshake(self):
        """Verify Callback API confirmation handshake returns configured code."""
        payload = {
            "type": "confirmation",
            "group_id": 229123456
        }
        res = self.agent.process_callback_event(payload)
        self.assertEqual(res, "test_confirm_code_123")

    def test_secret_key_validation(self):
        """Verify rejecting callback requests with invalid secret key."""
        payload = {
            "type": "donut_subscription_create",
            "group_id": 229123456,
            "secret": "invalid_secret",
            "object": {"user_id": 101, "amount": 500}
        }
        with self.assertRaises(PermissionError):
            self.agent.process_callback_event(payload)

    def test_donut_subscription_create(self):
        """Verify handling new donut subscriber, LTV calculation and community identity sync."""
        payload = {
            "type": "donut_subscription_create",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_sub_create_1",
            "object": {
                "user_id": 1001,
                "amount": 1000.0,
                "amount_without_fee": 950.0
            }
        }
        res = self.agent.process_callback_event(payload)
        self.assertEqual(res, "ok")

        sub = self.session_mgr.get_subscriber(1001)
        self.assertIsNotNone(sub)
        self.assertEqual(sub.user_id, 1001)
        self.assertEqual(sub.status, "ACTIVE")
        self.assertEqual(sub.amount, 1000.0)
        self.assertEqual(sub.total_donated, 1000.0)
        self.assertTrue(sub.is_paid)

        # Check cross-system community identity in Redis
        identity_raw = self.mock_redis.get("community_identity:vk:1001")
        self.assertIsNotNone(identity_raw)
        identity = json.loads(identity_raw)
        self.assertEqual(identity["user_id"], 1001)
        self.assertEqual(identity["vip_tier"], "VK_DONUT")
        self.assertEqual(identity["total_donated"], 1000.0)
        self.assertTrue(identity["is_paid"])

    def test_donut_subscription_prolonged(self):
        """Verify subscription prolongation increments LTV."""
        # Initial subscription
        self.agent.process_callback_event({
            "type": "donut_subscription_create",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_create_1",
            "object": {"user_id": 2002, "amount": 500.0}
        })

        # Prolongation after 1 month
        res = self.agent.process_callback_event({
            "type": "donut_subscription_prolonged",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_prolong_1",
            "object": {"user_id": 2002, "amount": 500.0}
        })
        self.assertEqual(res, "ok")

        sub = self.session_mgr.get_subscriber(2002)
        self.assertEqual(sub.total_donated, 1000.0)
        self.assertEqual(sub.status, "ACTIVE")

    def test_donut_subscription_price_changed(self):
        """Verify tier price update."""
        self.agent.process_callback_event({
            "type": "donut_subscription_create",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_create_2",
            "object": {"user_id": 3003, "amount": 500.0}
        })

        res = self.agent.process_callback_event({
            "type": "donut_subscription_price_changed",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_price_change_1",
            "object": {"user_id": 3003, "amount_old": 500.0, "amount_new": 1500.0}
        })
        self.assertEqual(res, "ok")

        sub = self.session_mgr.get_subscriber(3003)
        self.assertEqual(sub.amount, 1500.0)

    def test_donut_subscription_cancelled_and_expired(self):
        """Verify status update on cancellation/expiration."""
        self.agent.process_callback_event({
            "type": "donut_subscription_create",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_create_3",
            "object": {"user_id": 4004, "amount": 500.0}
        })

        self.agent.process_callback_event({
            "type": "donut_subscription_cancelled",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_cancel_1",
            "object": {"user_id": 4004}
        })

        sub = self.session_mgr.get_subscriber(4004)
        self.assertEqual(sub.status, "CANCELLED")
        self.assertFalse(sub.is_paid)

    def test_wall_reply_donut_detection(self):
        """Verify that wall comments from active dons generate P1_URGENT_PATRON tickets."""
        # Non-don comment
        self.agent.process_callback_event({
            "type": "wall_reply_new",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_reply_free",
            "object": {
                "id": 11,
                "post_id": 99,
                "from_id": 5005,
                "text": "Обычный комментарий от бесплатного пользователя",
                "donut": {"is_don": False}
            }
        })
        queue = self.mock_redis.lrange("feedback:queue:patrons", 0, -1)
        self.assertEqual(len(queue), 0)

        # Don comment
        self.agent.process_callback_event({
            "type": "wall_reply_new",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_reply_don",
            "object": {
                "id": 12,
                "post_id": 99,
                "from_id": 6006,
                "text": "Добавьте пожалуйста букмекера Dafabet в VIP сканер",
                "donut": {"is_don": True}
            }
        })
        queue = self.mock_redis.lrange("feedback:queue:patrons", 0, -1)
        self.assertEqual(len(queue), 1)

        ticket = json.loads(queue[0])
        self.assertEqual(ticket["user_id"], 6006)
        self.assertTrue(ticket["is_paid"])
        self.assertEqual(ticket["priority"], "P1_URGENT_PATRON")
        self.assertEqual(ticket["category"], "FEATURE_REQUEST")
        self.assertIn("Dafabet", ticket["content"])

    def test_event_deduplication(self):
        """Verify that identical event_id is skipped on second arrival."""
        payload = {
            "type": "donut_subscription_create",
            "group_id": 229123456,
            "secret": "test_secret_xyz",
            "event_id": "evt_dup_check",
            "object": {"user_id": 7007, "amount": 300.0}
        }
        res1 = self.agent.process_callback_event(payload)
        res2 = self.agent.process_callback_event(payload)
        self.assertEqual(res1, "ok")
        self.assertEqual(res2, "ok")

        # LTV should only have been counted once
        self.assertEqual(self.session_mgr.get_ltv(7007), 300.0)

    def test_http_healthcheck_endpoint(self):
        """Verify /healthz endpoint returns HTTP 200 with status UP."""
        url = f"http://127.0.0.1:{self.port}/healthz"
        with urllib.request.urlopen(url, timeout=5) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
            self.assertEqual(data["status"], "UP")
            self.assertEqual(data["service"], "smm-bot-vk")
            self.assertEqual(data["routing"], "ru-proxy:direct")
            self.assertIn("active_dons_count", data)

    def test_http_callback_endpoint(self):
        """Verify POST /api/v1/vk/callback over real HTTP socket."""
        url = f"http://127.0.0.1:{self.port}/api/v1/vk/callback"
        payload = {
            "type": "confirmation",
            "group_id": 229123456
        }
        req = urllib.request.Request(
            url,
            data=json.dumps(payload).encode("utf-8"),
            headers={"Content-Type": "application/json"}
        )
        with urllib.request.urlopen(req, timeout=5) as resp:
            self.assertEqual(resp.status, 200)
            body = resp.read().decode("utf-8")
            self.assertEqual(body, "test_confirm_code_123")

    def test_post_exclusive_donut_wall(self):
        """Verify exclusive wall post call parameters."""
        res = self.vk_client.post_exclusive_donut_wall(
            group_id=229123456,
            message="⚡ Эксклюзивный коридор: прибыль 14.2%",
            donut_paid_duration=-1
        )
        self.assertEqual(res, {"response": 1})


if __name__ == "__main__":
    unittest.main()
