#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Persona Factory Unit Tests
Task #55 [auto-reg]
"""

import datetime
import os
import sys
import unittest
from pathlib import Path

# Add smm-agent to python path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from persona_factory import (
    AffiliatePersona,
    BasePersona,
    BettorPersona,
    BrowserFingerprint,
    PersonaFactory,
    SocialPersona
)


class TestPersonaFactory(unittest.TestCase):

    def test_browser_fingerprint_generation(self):
        """Verify realistic Firefox browser fingerprints for US, RU, and EU locales."""
        for loc in ["us", "ru", "eu"]:
            fp = PersonaFactory.create_browser_fingerprint(locale=loc)
            self.assertIn("Firefox", fp.user_agent)
            self.assertIn("Gecko", fp.user_agent)
            self.assertIn(fp.viewport_width, [1920, 1920, 2560, 1440, 1536])
            self.assertTrue(len(fp.webgl_vendor) > 0)
            self.assertTrue(len(fp.webgl_renderer) > 0)
            self.assertIn(fp.hardware_concurrency, [8, 12, 16])
            self.assertIn(fp.device_memory_gb, [8, 16, 32])
            prefs = fp.to_firefox_prefs()
            self.assertEqual(prefs["general.useragent.override"], fp.user_agent)
            self.assertEqual(prefs["dom.maxHardwareConcurrency"], fp.hardware_concurrency)

    def test_bettor_persona_generation_us(self):
        """Verify Bettor persona for US jurisdiction with valid age (21+) and sports preferences."""
        persona = PersonaFactory.create_bettor_persona(locale="us", preferred_currency="USD")
        self.assertIsInstance(persona, BettorPersona)
        self.assertTrue(persona.persona_id.startswith("bet_"))
        self.assertEqual(persona.country_code, "US")
        self.assertEqual(persona.preferred_currency, "USD")
        self.assertTrue(len(persona.first_name) > 0)
        self.assertTrue(len(persona.last_name) > 0)
        self.assertTrue(persona.email.endswith("@gmail.com"))
        self.assertTrue(persona.phone.startswith("+1"))
        self.assertEqual(len(persona.ssn_last4), 4)
        self.assertTrue(persona.ssn_last4.isdigit())
        self.assertEqual(persona.promo_code, "SMARTBET")
        self.assertTrue(persona.bankroll_usd >= 500.0)

        # Verify age >= 21
        dob = datetime.datetime.strptime(persona.date_of_birth, "%Y-%m-%d").date()
        today = datetime.date.today()
        age = today.year - dob.year - ((today.month, today.day) < (dob.month, dob.day))
        self.assertGreaterEqual(age, 21)

    def test_bettor_persona_generation_ru(self):
        """Verify Bettor persona for Russian sportsbooks (Winline, Fonbet, Pari)."""
        persona = PersonaFactory.create_bettor_persona(locale="ru", preferred_currency="RUB")
        self.assertEqual(persona.country_code, "RU")
        self.assertTrue(persona.phone.startswith("+7"))
        self.assertTrue(persona.email.endswith("@yandex.ru"))

    def test_affiliate_persona_generation(self):
        """Verify Affiliate webmaster persona for partner network applications."""
        persona = PersonaFactory.create_affiliate_persona(locale="ru", domain="smartbet.guru")
        self.assertIsInstance(persona, AffiliatePersona)
        self.assertTrue(persona.persona_id.startswith("aff_"))
        self.assertEqual(persona.website_url, "https://smartbet.guru")
        self.assertEqual(persona.payment_method, "USDT TRC20")
        self.assertTrue(persona.wallet_address.startswith("T"))
        self.assertEqual(len(persona.wallet_address), 34)
        self.assertTrue(len(persona.traffic_sources) >= 3)
        self.assertIn("150,000+", persona.monthly_traffic_estimate)
        self.assertTrue(persona.telegram.startswith("@smartbet_partner_"))

    def test_social_persona_generation(self):
        """Verify Social / Reddit persona generation."""
        persona = PersonaFactory.create_social_persona(locale="us")
        self.assertIsInstance(persona, SocialPersona)
        self.assertTrue(persona.persona_id.startswith("soc_"))
        self.assertTrue(persona.reddit_username.startswith("u/"))
        self.assertTrue(len(persona.bio) > 0)
        self.assertIn("r/Sportsbook", persona.target_subreddits)

    def test_password_entropy(self):
        """Verify generated passwords contain upper, lower, digits and special characters."""
        for _ in range(20):
            pwd = PersonaFactory.generate_password(length=14)
            self.assertEqual(len(pwd), 14)
            self.assertTrue(any(c.isupper() for c in pwd))
            self.assertTrue(any(c.islower() for c in pwd))
            self.assertTrue(any(c.isdigit() for c in pwd))
            self.assertTrue(any(c in "!@#$%^&*" for c in pwd))


if __name__ == "__main__":
    unittest.main()
