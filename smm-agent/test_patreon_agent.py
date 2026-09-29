#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Patreon Agent Test Suite
Tests for:
1. PatreonConfig defaults, tiers, and US Proxy routing (Rule 6).
2. Isolated Redis session storage (smm:session:patreon).
3. Webhook HMAC-SHA256 signature verification.
4. Parsing of members:pledge:create, update, delete and posts:comments:create.
5. Feedback item creation with USD currency and P1_URGENT_PATRON priority.
6. Member Desk: exclusive surebet alert formatting with 80% freebet conversion (Rule 10).
7. HTTP endpoints (/healthz, /actuator/health, /webhooks/patreon).
"""

import hashlib
import hmac
import json
import os
import sys
import threading
import time
import unittest
from urllib.request import Request, urlopen
from urllib.error import HTTPError

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from patreon_agent import (
    PatreonConfig,
    PatreonMemberDesk,
    PatreonSessionManager,
    PatreonWebhookHandler,
    create_patreon_server,
)


class MockRedisClient:
    """In-memory Redis client for unit testing."""

    def __init__(self):
        self.store = {}
        self.lists = {}
        self.expirations = {}

    def get(self, key):
        return self.store.get(key)

    def set(self, key, value, ex=None):
        self.store[key] = value
        if ex:
            self.expirations[key] = ex
        return True

    def lpush(self, key, value):
        if key not in self.lists:
            self.lists[key] = []
        self.lists[key].insert(0, value)
        return len(self.lists[key])

    def ping(self):
        return True


class TestPatreonConfig(unittest.TestCase):
    """Tests PatreonConfig defaults and network topology."""

    def test_default_config(self):
        cfg = PatreonConfig(account_id="patreon_unit_test")
        self.assertEqual(cfg.account_id, "patreon_unit_test")
        self.assertEqual(cfg.session_key, "smm:session:patreon")
        self.assertIn("100.83.113.50", cfg.proxy_server)
        self.assertEqual(cfg.healthcheck_port, 8080)
        self.assertIn("tier_25", cfg.tiers)
        self.assertIn("tier_100", cfg.tiers)
        self.assertEqual(cfg.tiers["tier_25"]["amount_cents"], 2500)
        self.assertEqual(cfg.tiers["tier_100"]["amount_cents"], 10000)


class TestPatreonSessionManager(unittest.TestCase):
    """Tests Redis persistence under smm:session:patreon."""

    def test_session_lifecycle(self):
        mock_r = MockRedisClient()
        cfg = PatreonConfig(account_id="patreon_test", webhook_secret="secret_abc_123")
        mgr = PatreonSessionManager(cfg, redis_client=mock_r)

        session = mgr.load_session()
        self.assertEqual(session["account_id"], "patreon_test")
        self.assertEqual(session["webhook_secret"], "secret_abc_123")

        # Verify saved in Redis
        self.assertIn("smm:session:patreon", mock_r.store)

        # Mutate in Redis and reload
        session["access_token"] = "new_access_token_xyz"
        mock_r.set("smm:session:patreon", json.dumps(session))

        mgr2 = PatreonSessionManager(cfg, redis_client=mock_r)
        loaded = mgr2.load_session()
        self.assertEqual(loaded["access_token"], "new_access_token_xyz")
        self.assertEqual(cfg.access_token, "new_access_token_xyz")


class TestPatreonWebhookHandler(unittest.TestCase):
    """Tests HMAC validation and event handling."""

    def setUp(self):
        self.mock_r = MockRedisClient()
        self.secret = "test_patreon_secret_777"
        self.cfg = PatreonConfig(webhook_secret=self.secret)
        self.session_mgr = PatreonSessionManager(self.cfg, redis_client=self.mock_r)
        self.handler = PatreonWebhookHandler(self.cfg, self.session_mgr)

    def _sign_payload(self, body_bytes: bytes, secret: str = None) -> str:
        s = secret or self.secret
        return hmac.new(s.encode("utf-8"), body_bytes, hashlib.sha256).hexdigest()

    def test_hmac_verification(self):
        raw_body = b'{"data": {"type": "member"}}'
        valid_sig = self._sign_payload(raw_body)

        # Valid
        self.assertTrue(self.handler.verify_signature(raw_body, valid_sig))

        # Tampered body
        tampered_body = b'{"data": {"type": "member", "evil": true}}'
        self.assertFalse(self.handler.verify_signature(tampered_body, valid_sig))

        # Wrong signature
        self.assertFalse(self.handler.verify_signature(raw_body, "wrong_signature_12345"))

        # Missing header
        self.assertFalse(self.handler.verify_signature(raw_body, None))

    def test_pledge_create_vip_syndicate(self):
        raw_body = json.dumps({
            "data": {
                "id": "patron_1001",
                "type": "member",
                "attributes": {
                    "full_name": "Michael Corleone",
                    "email": "michael@syndicate.io",
                    "currently_entitled_amount_cents": 10000,
                    "lifetime_support_cents": 20000,
                    "patron_status": "active_patron",
                }
            }
        }).encode("utf-8")

        headers = {
            "X-Patreon-Event": "members:pledge:create",
            "X-Patreon-Signature": self._sign_payload(raw_body),
        }

        res = self.handler.process_webhook_event(raw_body, headers)
        self.assertEqual(res["status"], "success")
        self.assertEqual(res["code"], 200)

        item = res["feedback_item"]
        self.assertIsNotNone(item)
        self.assertEqual(item["source_platform"], "PATREON")
        self.assertEqual(item["currency"], "USD")
        self.assertTrue(item["is_paid"])
        self.assertEqual(item["priority"], "P1_URGENT_PATRON")
        self.assertEqual(item["patron_tier"], "VIP Syndicate ($100/mo)")
        self.assertEqual(item["pledge_amount_cents"], 10000)

        # Check pushed to Redis
        self.assertIn("feedback:queue:patron", self.mock_r.lists)
        self.assertEqual(len(self.mock_r.lists["feedback:queue:patron"]), 1)

    def test_pledge_create_pro_arbitrageur(self):
        raw_body = json.dumps({
            "data": {
                "id": "patron_1002",
                "type": "member",
                "attributes": {
                    "full_name": "Alice Sharp",
                    "email": "alice@bets.com",
                    "currently_entitled_amount_cents": 2500,
                    "patron_status": "active_patron",
                }
            }
        }).encode("utf-8")

        headers = {
            "X-Patreon-Event": "members:pledge:create",
            "X-Patreon-Signature": self._sign_payload(raw_body),
        }

        res = self.handler.process_webhook_event(raw_body, headers)
        item = res["feedback_item"]
        self.assertEqual(item["patron_tier"], "Pro Arbitrageur ($25/mo)")
        self.assertEqual(item["priority"], "P1_URGENT_PATRON")

    def test_pledge_delete_event(self):
        raw_body = json.dumps({
            "data": {
                "id": "patron_1003",
                "attributes": {
                    "full_name": "Bob Quitter",
                }
            }
        }).encode("utf-8")

        headers = {
            "X-Patreon-Event": "members:pledge:delete",
            "X-Patreon-Signature": self._sign_payload(raw_body),
        }

        res = self.handler.process_webhook_event(raw_body, headers)
        item = res["feedback_item"]
        self.assertEqual(item["status"], "CANCELLED")
        self.assertFalse(item["is_paid"])

    def test_comment_created_heuristics(self):
        # 1. Feature request comment
        body_feat = json.dumps({
            "data": {
                "id": "comm_42",
                "attributes": {
                    "body": "Could you please add live Pinnacle vs BetMGM alert filters?",
                },
                "relationships": {
                    "commenter": {"data": {"id": "user_88", "type": "user"}}
                }
            },
            "included": [
                {"type": "user", "id": "user_88", "attributes": {"full_name": "Dave Arber"}}
            ]
        }).encode("utf-8")

        headers = {
            "X-Patreon-Event": "posts:comments:create",
            "X-Patreon-Signature": self._sign_payload(body_feat),
        }

        res = self.handler.process_webhook_event(body_feat, headers)
        item = res["feedback_item"]
        self.assertEqual(item["category"], "FEATURE_REQUEST")
        self.assertEqual(item["priority"], "P1_URGENT_PATRON")
        self.assertEqual(item["patron_name"], "Dave Arber")

        # 2. Bug report comment
        body_bug = json.dumps({
            "data": {
                "id": "comm_43",
                "attributes": {
                    "body": "Calculator error: formula is broken for 3-way markets",
                }
            }
        }).encode("utf-8")

        headers["X-Patreon-Signature"] = self._sign_payload(body_bug)
        res_bug = self.handler.process_webhook_event(body_bug, headers)
        self.assertEqual(res_bug["feedback_item"]["category"], "BUG_REPORT")
        self.assertEqual(res_bug["feedback_item"]["priority"], "P1_URGENT_PATRON")


class TestPatreonMemberDesk(unittest.TestCase):
    """Tests Member Desk, post publishing and signal formatting."""

    def test_exclusive_arb_signal_formatting(self):
        cfg = PatreonConfig()
        desk = PatreonMemberDesk(cfg)

        legs = [
            {"bookmaker": "Pinnacle", "outcome": "Over 2.5", "odds": 2.12, "stake_pct": 47.9},
            {"bookmaker": "BetMGM", "outcome": "Under 2.5", "odds": 2.05, "stake_pct": 52.1},
        ]

        signal_text = desk.format_exclusive_arb_signal(
            match="Real Madrid vs Barcelona",
            sport="Football / La Liga",
            profit_pct=11.85,
            legs=legs,
        )

        self.assertIn("EXCLUSIVE PATRON SUREBET ALERT", signal_text)
        self.assertIn("+11.85% Guaranteed Profit", signal_text)
        self.assertIn("Pinnacle: Over 2.5 @ 2.12", signal_text)
        self.assertIn("BetMGM: Under 2.5 @ 2.05", signal_text)
        # Verify 80% freebet conversion math (Rule 10)
        self.assertIn("$80.00 Guaranteed Cash", signal_text)
        self.assertIn("Pro Arbitrageur ($25/mo)", signal_text)

    def test_publish_premium_post(self):
        cfg = PatreonConfig()
        desk = PatreonMemberDesk(cfg)

        res = desk.publish_premium_post(
            title="VIP Arbitrage Weekly Digest #38",
            content="Detailed breakdown of 14% soccer corridor...",
            min_tier_cents=2500,
        )
        self.assertEqual(res["status"], "success")
        self.assertTrue(res["post_id"].startswith("post_"))

    def test_reply_to_comment(self):
        cfg = PatreonConfig()
        desk = PatreonMemberDesk(cfg)
        reply = desk.reply_to_comment(
            post_id="post_99",
            comment_id="comm_123",
            reply_text="Your requested feature is now deployed in production!",
        )
        self.assertEqual(reply["status"], "success")
        self.assertEqual(reply["comment_id"], "comm_123")


class TestPatreonHttpServer(unittest.TestCase):
    """Tests live HTTP endpoints (/healthz, /actuator/health, /webhooks/patreon)."""

    @classmethod
    def setUpClass(cls):
        # Pick a free high port for unit testing
        cls.port = 18088
        cls.secret = "http_test_secret_999"
        cls.config = PatreonConfig(
            account_id="patreon_http_test",
            healthcheck_port=cls.port,
            webhook_secret=cls.secret,
        )
        cls.server = create_patreon_server(cls.config)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        time.sleep(0.3)

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()

    def test_healthz_endpoint(self):
        url = f"http://127.0.0.1:{self.port}/healthz"
        with urlopen(url, timeout=3) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
            self.assertEqual(data["status"], "UP")
            self.assertEqual(data["service"], "smm-bot-patreon")
            self.assertEqual(data["account_id"], "patreon_http_test")

    def test_actuator_health_endpoint(self):
        url = f"http://127.0.0.1:{self.port}/actuator/health"
        with urlopen(url, timeout=3) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
            self.assertEqual(data["status"], "UP")

    def test_webhook_endpoint_hmac_valid(self):
        url = f"http://127.0.0.1:{self.port}/webhooks/patreon"
        body = json.dumps({"data": {"id": "patron_http", "attributes": {"full_name": "Test User"}}}).encode("utf-8")
        sig = hmac.new(self.secret.encode("utf-8"), body, hashlib.sha256).hexdigest()

        req = Request(url, data=body, method="POST")
        req.add_header("Content-Type", "application/json")
        req.add_header("X-Patreon-Event", "members:pledge:create")
        req.add_header("X-Patreon-Signature", sig)

        with urlopen(req, timeout=3) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode("utf-8"))
            self.assertEqual(data["status"], "success")

    def test_webhook_endpoint_hmac_invalid(self):
        url = f"http://127.0.0.1:{self.port}/webhooks/patreon"
        body = b'{"data": "evil"}'

        req = Request(url, data=body, method="POST")
        req.add_header("Content-Type", "application/json")
        req.add_header("X-Patreon-Signature", "invalid_sig_hex")

        with self.assertRaises(HTTPError) as ctx:
            urlopen(req, timeout=3)
        self.assertEqual(ctx.exception.code, 403)


if __name__ == "__main__":
    unittest.main()
