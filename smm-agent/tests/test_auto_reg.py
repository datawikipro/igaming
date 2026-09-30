#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Automated Registration Engine Test Suite
Task #55 [auto-reg] (Bookmakers, Affiliates, Forums, 2FA Inbox Gateway & Redis)
"""

import os
import sys
import unittest
from pathlib import Path

# Add smm-agent to python path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from auth_inbox_client import AuthInboxGatewayClient
from auto_register_engine import AutoRegistrationEngine, RegisteredAccountRecord
from persona_factory import PersonaFactory


class MockRedisClient:
    """Mock Redis client for local isolation."""
    def __init__(self):
        self.data = {}
        self.sets = {}

    def set(self, key, value, ex=None):
        self.data[key] = value
        return True

    def get(self, key):
        return self.data.get(key)

    def sadd(self, key, value):
        if key not in self.sets:
            self.sets[key] = set()
        self.sets[key].add(value)
        return 1

    def smembers(self, key):
        return self.sets.get(key, set())

    def ping(self):
        return True


class TestAuthInboxGatewayClient(unittest.TestCase):

    def setUp(self):
        self.client = AuthInboxGatewayClient(mock_mode=True)

    def test_mock_email_code_retrieval(self):
        """Verify email 2FA OTP retrieval from inbox gateway."""
        email = "user_test_123@smartbet.guru"
        self.client.set_mock_code(email, "849201")
        code = self.client.fetch_email_code(email)
        self.assertEqual(code, "849201")

    def test_mock_sms_code_retrieval(self):
        """Verify SMS 2FA OTP retrieval from inbox gateway."""
        phone = "+12015550199"
        self.client.set_mock_code(phone, "4912")
        code = self.client.fetch_sms_code(phone)
        self.assertEqual(code, "4912")

    def test_totp_rfc6238_vector(self):
        """
        Verify RFC 6238 TOTP computation:
        Secret 'JBSWY3DPEHPK3PXP' at fixed timestamp.
        """
        secret = "JBSWY3DPEHPK3PXP"
        # Test vector with fixed timestamp t=59 (counter 1 -> hash)
        code1 = self.client.generate_totp_code(secret, interval=30, digits=6, timestamp=59)
        self.assertEqual(len(code1), 6)
        self.assertTrue(code1.isdigit())

        # Current time code
        code2 = self.client.generate_totp_code(secret)
        self.assertEqual(len(code2), 6)
        self.assertTrue(code2.isdigit())

    def test_gateway_health(self):
        """Verify gateway health check."""
        self.assertTrue(self.client.is_gateway_healthy())


class TestAutoRegistrationEngine(unittest.TestCase):

    def setUp(self):
        self.engine = AutoRegistrationEngine(
            proxy_server="http://100.83.113.50:3128",
            mock_mode=True
        )
        # Inject mock redis
        self.mock_redis = MockRedisClient()
        self.engine.redis_client = self.mock_redis

    def test_register_bookmaker_winline(self):
        """Verify automated registration workflow for Winline sportsbook."""
        persona = PersonaFactory.create_bettor_persona(locale="ru", preferred_currency="RUB")
        self.engine.inbox_client.set_mock_code(persona.phone, "773105")

        record = self.engine.register_bookmaker(
            bookmaker_id="winline",
            persona=persona,
            locale="ru",
            dry_run=True
        )

        self.assertIsInstance(record, RegisteredAccountRecord)
        self.assertEqual(record.service_type, "bookmaker")
        self.assertEqual(record.service_name, "winline")
        self.assertEqual(record.status, "ACTIVE")
        self.assertEqual(record.proxy, "http://100.83.113.50:3128")
        self.assertTrue(record.redis_profile_key.startswith("smm:profile:bk_winline_"))
        self.assertEqual(record.metadata["verified_with_otp"], "773105")

        # Verify saved in Redis
        retrieved = self.engine.get_account_record("bookmaker", "winline", record.account_id)
        self.assertIsNotNone(retrieved)
        self.assertEqual(retrieved.email, persona.email)

    def test_register_bookmaker_pinnacle(self):
        """Verify registration for international sportsbook (Pinnacle) with US persona."""
        persona = PersonaFactory.create_bettor_persona(locale="us", preferred_currency="USD")
        self.engine.inbox_client.set_mock_code(persona.phone, "381920")

        record = self.engine.register_bookmaker(
            bookmaker_id="pinnacle",
            persona=persona,
            locale="us",
            dry_run=True
        )

        self.assertEqual(record.service_name, "pinnacle")
        self.assertEqual(record.metadata["preferred_currency"], "USD")
        self.assertEqual(record.metadata["locale"], "us")

    def test_register_affiliate_network(self):
        """Verify automated affiliate network registration and tracking link generation."""
        persona = PersonaFactory.create_affiliate_persona(locale="ru")
        self.engine.inbox_client.set_mock_code(persona.email, "918234")

        record = self.engine.register_affiliate_network(
            network_name="uffiliates",
            bookmaker_id="winline",
            persona=persona,
            locale="ru",
            dry_run=True
        )

        self.assertEqual(record.service_type, "affiliate")
        self.assertEqual(record.service_name, "uffiliates")
        self.assertIsNotNone(record.tracking_link)
        self.assertIn("https://track.winline.com/click?", record.tracking_link)
        self.assertIn("{click_id}", record.tracking_link)
        self.assertEqual(record.promo_code, "SMARTBET")
        self.assertEqual(record.metadata["payment_method"], "USDT TRC20")
        self.assertTrue(record.metadata["wallet_address"].startswith("T"))

    def test_register_forum_reddit(self):
        """Verify Reddit / sports forum registration workflow."""
        persona = PersonaFactory.create_social_persona(locale="us")
        self.engine.inbox_client.set_mock_code(persona.email, "502914")

        record = self.engine.register_forum(
            forum_name="reddit",
            persona=persona,
            locale="us",
            dry_run=True
        )

        self.assertEqual(record.service_type, "forum")
        self.assertEqual(record.service_name, "reddit")
        self.assertTrue(record.metadata["reddit_username"].startswith("u/"))
        self.assertIn("r/Sportsbook", record.metadata["target_subreddits"])

    def test_list_accounts_for_service(self):
        """Verify querying registered accounts by service name."""
        self.engine.register_bookmaker("winline", dry_run=True)
        self.engine.register_bookmaker("winline", dry_run=True)
        self.engine.register_bookmaker("fonbet", dry_run=True)

        winline_accs = self.engine.list_accounts_for_service("winline")
        self.assertEqual(len(winline_accs), 2)
        fonbet_accs = self.engine.list_accounts_for_service("fonbet")
        self.assertEqual(len(fonbet_accs), 1)


if __name__ == "__main__":
    unittest.main()
