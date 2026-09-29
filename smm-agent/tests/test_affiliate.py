#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Unit and Integration Tests for SMM Affiliate Hub
Task #61 [affiliate-hub]
"""

import sys
import unittest
from pathlib import Path

# Add smm-agent to path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from affiliate_manager import (
    AFFILIATE_CATALOG,
    format_affiliate_link,
    calculate_freebet_guaranteed_cash,
    generate_surebet_affiliate_buttons,
    format_freebet_promo_post,
    generate_social_bio_links
)
from affiliate_auto_reg import PersonaFactory, AuthInboxGatewayClient


class TestAffiliateHub(unittest.TestCase):

    def test_catalog_coverage(self):
        """Verify presence of key bookmakers across RU and international markets."""
        expected_bks = ["winline", "fonbet", "pari", "betcity", "pinnacle", "1xbet", "stake", "draftkings"]
        for bk in expected_bks:
            self.assertIn(bk, AFFILIATE_CATALOG, f"Missing bookmaker in catalog: {bk}")
            info = AFFILIATE_CATALOG[bk]
            self.assertTrue(len(info.promo_code) > 0)
            self.assertTrue(len(info.network_name) > 0)

    def test_tracking_link_generation(self):
        """Verify cloaked 302 redirect URL formatting with UTM parameters."""
        url = format_affiliate_link(
            bookmaker="winline",
            utm_source="telegram",
            utm_campaign="arb_999",
            utm_medium="channel",
            utm_content="leg1_winline",
            subid="click_abc123"
        )
        self.assertTrue(url.startswith("https://smartbet.guru/go/winline?"))
        self.assertIn("utm_source=telegram", url)
        self.assertIn("utm_campaign=arb_999", url)
        self.assertIn("utm_content=leg1_winline", url)
        self.assertIn("subid=click_abc123", url)

    def test_freebet_80_percent_cash_rule(self):
        """
        Verify Golden Rule #10:
        Freebet 3000 RUB -> 2400 RUB guaranteed cash (80%).
        """
        res = calculate_freebet_guaranteed_cash(3000.0, k1=5.0, k2=1.25)
        self.assertEqual(res["freebet_amount"], 3000.0)
        self.assertEqual(res["guaranteed_cash"], 2400.0)
        self.assertEqual(res["cash_percent"], 80.0)

    def test_surebet_affiliate_buttons(self):
        """Verify generation of Telegram inline buttons for both arb legs and calculator."""
        buttons = generate_surebet_affiliate_buttons(
            bookmaker1="fonbet",
            bookmaker2="pinnacle",
            arb_id="arb_555",
            profit_percent=6.5,
            k1=2.15,
            k2=2.08
        )
        self.assertEqual(len(buttons), 3)
        self.assertIn("Фонбет", buttons[0]["text"])
        self.assertIn("/go/fonbet", buttons[0]["url"])
        self.assertIn("Pinnacle", buttons[1]["text"])
        self.assertIn("/go/pinnacle", buttons[1]["url"])
        self.assertIn("калькулятор", buttons[2]["text"].lower())
        self.assertIn("arb_id=arb_555", buttons[2]["url"])

    def test_format_freebet_promo_post(self):
        """Verify promo radar post formatting with 80% cash and disclaimer."""
        post = format_freebet_promo_post(
            bookmaker="winline",
            freebet_amount=3000.0
        )
        text = post["text"]
        self.assertIn("3 000 ₽", text)
        self.assertIn("2 400 ₽", text)
        self.assertIn("80%", text)
        self.assertIn("Промокод", text)
        self.assertIn("SMARTBET", text)
        self.assertIn("18+", text)

        buttons = post["buttons"]
        self.assertTrue(len(buttons) >= 2)
        self.assertIn("/go/winline", buttons[0]["url"])

    def test_digital_persona_generation(self):
        """Verify digital persona generation for automated network signups."""
        persona = PersonaFactory.create_persona("smartbet.guru")
        self.assertTrue(len(persona.first_name) > 0)
        self.assertTrue(len(persona.last_name) > 0)
        self.assertIn("@smartbet.guru", persona.email)
        self.assertTrue(persona.wallet_address.startswith("T"))
        self.assertTrue(len(persona.traffic_sources) >= 3)

    def test_totp_generation(self):
        """Verify TOTP token generator for 2FA gateway."""
        client = AuthInboxGatewayClient()
        secret = "JBSWY3DPEHPK3PXP"  # standard test base32 secret
        code = client.generate_totp_code(secret)
        self.assertEqual(len(code), 6)
        self.assertTrue(code.isdigit())


if __name__ == "__main__":
    unittest.main()
