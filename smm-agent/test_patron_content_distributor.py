#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Unit and integration tests for SmartBet.guru Patron Content Distributor
Task [patron-content] (Plane ID #487c4156) & Rules 6, 9, 10 (AGENTS.md)
"""

import json
import os
import sys
import unittest
from unittest.mock import MagicMock, patch

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from patron_content_distributor import (
    EN_DISCLAIMER,
    RU_DISCLAIMER,
    ContentTemplateEngine,
    PatronContentDistributor,
    calculate_freebet_cash,
)


class TestFreebetCalculations(unittest.TestCase):
    """Verifies Matched Betting Rule 10: 80% Guaranteed Cash Conversion."""

    def test_rule_10_mathematical_conversion(self):
        # Rule 10: eta = ((K1 - 1) * (K2 - 1)) / K2
        # K1 = 5.0, K2 = 1.25 -> eta = (4 * 0.25) / 1.25 = 1 / 1.25 = 0.80 (80%)
        nominal = 3000.0
        k1 = 5.0
        k2 = 1.25
        result = calculate_freebet_cash(nominal=nominal, k1=k1, k2=k2)

        self.assertEqual(result["nominal"], 3000.0)
        self.assertEqual(result["conversion_ratio"], 0.8)
        self.assertEqual(result["guaranteed_cash"], 2400.0)
        self.assertEqual(result["hedge_stake"], 9600.0)

        # Net profit verification if Leg 1 (Underdog) wins:
        # Profit = (K1 - 1) * nominal - hedge_stake = 4 * 3000 - 9600 = 2400
        leg1_profit = (k1 - 1.0) * nominal - result["hedge_stake"]
        self.assertAlmostEqual(leg1_profit, 2400.0, places=2)

        # Net profit verification if Leg 2 (Favorite) wins:
        # Profit = hedge_stake * (K2 - 1) = 9600 * 0.25 = 2400
        leg2_profit = result["hedge_stake"] * (k2 - 1.0)
        self.assertAlmostEqual(leg2_profit, 2400.0, places=2)

    def test_custom_nominal_and_odds(self):
        # 100 USD freebet at K1 = 6.0, K2 = 1.20
        # eta = (5 * 0.20) / 1.20 = 1 / 1.20 = 0.8333
        result = calculate_freebet_cash(nominal=100.0, k1=6.0, k2=1.20)
        self.assertEqual(result["nominal"], 100.0)
        self.assertGreaterEqual(result["conversion_ratio"], 0.80)
        self.assertGreaterEqual(result["guaranteed_cash"], 80.0)


class TestContentTemplates(unittest.TestCase):
    """Verifies content rendering, required elements, and mandatory disclaimers."""

    def test_surebet_corridor_template_ru(self):
        ctx = {
            "match": "Реал Мадрид — Барселона",
            "sport": "Баскетбол (Евролига)",
            "profit_pct": 21.4,
            "bk1": "Винлайн",
            "bk2": "Pinnacle",
            "corridor_window": "4-7 очков",
        }
        res = ContentTemplateEngine.render_surebet_corridor(ctx, lang="ru")
        self.assertIn("Реал Мадрид — Барселона", res["title"])
        self.assertIn("21.4%", res["title"])
        self.assertIn("Винлайн", res["content"])
        self.assertIn("Pinnacle", res["content"])
        self.assertIn("4-7 очков", res["content"])
        self.assertIn("лимитам", res["content"])
        self.assertIn("Ставки на спорт сопряжены с финансовыми рисками", res["content"])
        self.assertTrue(bool(res["teaser"]))

    def test_surebet_corridor_template_en(self):
        ctx = {
            "match": "Boston Celtics vs LA Lakers",
            "sport": "Basketball (NBA)",
            "profit_pct": 19.2,
            "bk1": "DraftKings",
            "bk2": "Pinnacle",
            "corridor_window": "5-8 points",
        }
        res = ContentTemplateEngine.render_surebet_corridor(ctx, lang="en")
        self.assertIn("Boston Celtics", res["title"])
        self.assertIn("19.2%", res["title"])
        self.assertIn("DraftKings", res["content"])
        self.assertIn("Sports betting involves financial risks", res["content"])

    def test_freebet_80_cash_template_ru(self):
        ctx = {"nominal": 3000.0, "bookmaker": "Винлайн"}
        res = ContentTemplateEngine.render_freebet_80_cash(ctx, lang="ru")
        self.assertIn("80%", res["title"])
        self.assertIn("2,400 ₽", res["title"])
        self.assertIn("2,400 ₽ чистыми деньгами на счёт при любом исходе", res["content"])
        self.assertIn("SNR Matched Betting", res["content"])
        self.assertIn("Ставки на спорт сопряжены с финансовыми рисками", res["content"])
        self.assertIn("2,400", res["teaser"])

    def test_freebet_80_cash_template_en(self):
        ctx = {"nominal": 100.0, "bookmaker": "FanDuel"}
        res = ContentTemplateEngine.render_freebet_80_cash(ctx, lang="en")
        self.assertIn("Convert $100 Freebet to $80 Guaranteed Cash", res["title"])
        self.assertIn("SNR 80% GUARANTEED CASH RULE", res["content"])
        self.assertIn("Sports betting involves financial risks", res["content"])

    def test_antifraud_guide_template_ru_and_en(self):
        ru_res = ContentTemplateEngine.render_antifraud_guide(lang="ru")
        self.assertIn("Camoufox", ru_res["content"])
        self.assertIn("инкогнито", ru_res["content"])
        self.assertIn("Cache Warmup", ru_res["content"])
        self.assertIn("Ставки на спорт сопряжены с финансовыми рисками", ru_res["content"])

        en_res = ContentTemplateEngine.render_antifraud_guide(lang="en")
        self.assertIn("Gecko / Camoufox", en_res["content"])
        self.assertIn("Cache Warmup", en_res["content"])
        self.assertIn("Sports betting involves financial risks", en_res["content"])


class TestPatronContentDistributor(unittest.TestCase):
    """Verifies distribution logic, dry-run simulation, and platform protections."""

    def setUp(self):
        self.distributor = PatronContentDistributor(
            vip_url="http://mock-vip:8080/api/v1/vip/publish-post",
            donut_url="http://mock-donut:8080/admin/donut/posts",
            boosty_url="http://mock-boosty:8080/admin/boosty/posts",
            patreon_url="http://mock-patreon:8080/api/v1/patreon/posts",
        )

    def test_telegram_vip_dry_run_protection_flag(self):
        res = self.distributor.publish_to_telegram_vip(
            title="VIP Signal", content="Signal content", protect_content=True, dry_run=True
        )
        self.assertEqual(res["platform"], "telegram_vip")
        self.assertEqual(res["status"], "simulated")
        self.assertTrue(res["payload"]["protect_content"], "Telegram VIP posts must enforce protect_content=True")

    def test_vk_donut_dry_run_duration_flag(self):
        res = self.distributor.publish_to_vk_donut(
            title="Donut Post", content="Exclusive wall content", donut_paid_duration=-1, dry_run=True
        )
        self.assertEqual(res["platform"], "vk_donut")
        self.assertEqual(res["status"], "simulated")
        self.assertEqual(res["payload"]["donut_paid_duration"], -1, "VK Donut posts must enforce donut_paid_duration=-1")

    def test_boosty_dry_run_min_tier(self):
        res = self.distributor.publish_to_boosty(
            title="Boosty Post", content="Exclusive content", teaser="Teaser", min_tier_rub=2500, dry_run=True
        )
        self.assertEqual(res["platform"], "boosty")
        self.assertEqual(res["status"], "simulated")
        self.assertEqual(res["payload"]["min_tier_rub"], 2500)

    def test_patreon_dry_run_usd_tier(self):
        res = self.distributor.publish_to_patreon(
            title="Patreon Post", content="USD VIP content", min_tier_cents=2500, dry_run=True
        )
        self.assertEqual(res["platform"], "patreon")
        self.assertEqual(res["status"], "simulated")
        self.assertEqual(res["payload"]["min_tier_cents"], 2500)

    def test_distribute_all_platforms_dry_run(self):
        report = self.distributor.distribute(
            template_type="freebet",
            platforms=["telegram_vip", "vk_donut", "boosty", "patreon"],
            dry_run=True,
            context={"nominal": 3000.0},
        )
        self.assertEqual(report["template"], "freebet")
        self.assertTrue(report["dry_run"])
        results = report["results"]
        self.assertIn("telegram_vip", results)
        self.assertIn("vk_donut", results)
        self.assertIn("boosty", results)
        self.assertIn("patreon", results)

        self.assertTrue(results["telegram_vip"]["payload"]["protect_content"])
        self.assertEqual(results["vk_donut"]["payload"]["donut_paid_duration"], -1)
        self.assertEqual(results["boosty"]["payload"]["min_tier_rub"], 2500)
        self.assertEqual(results["patreon"]["payload"]["min_tier_cents"], 2500)

    def test_distribute_live_http_mocked(self):
        with patch.object(self.distributor, "_http_post") as mock_http:
            mock_http.side_effect = [
                {"success": True, "message_length": 150, "protect_content": True},
                {"status": "success", "donut_paid_duration": -1, "result": {"post_id": 999}},
                {"status": "success", "result": {"id": "boosty-123", "status": "published"}},
                {"status": "success", "post_id": "patreon-post-456", "mode": "live"},
            ]

            report = self.distributor.distribute(
                template_type="surebet",
                platforms=["telegram_vip", "vk_donut", "boosty", "patreon"],
                dry_run=False,
            )

            self.assertEqual(mock_http.call_count, 4)
            results = report["results"]
            self.assertEqual(results["telegram_vip"]["status"], "success")
            self.assertEqual(results["vk_donut"]["status"], "success")
            self.assertEqual(results["boosty"]["status"], "success")
            self.assertEqual(results["patreon"]["status"], "success")

    def test_distribute_error_handling(self):
        with patch.object(self.distributor, "_http_post") as mock_http:
            mock_http.side_effect = Exception("Connection refused by igaming-vip-bot")

            report = self.distributor.distribute(
                template_type="antifraud",
                platforms=["telegram_vip"],
                dry_run=False,
            )

            res = report["results"]["telegram_vip"]
            self.assertEqual(res["status"], "error")
            self.assertIn("Connection refused", res["error"])


if __name__ == "__main__":
    unittest.main()
