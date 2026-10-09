#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Unit & Integration Tests for Mobile Device Ecosystem
Verifies:
1. Strict Anti-Fraud Isolation (Rule 13: SMM vs Capture Pools).
2. Bookmaker APK Catalog & Auto-grant permissions (-g).
3. Matched Betting Freebet 80% Math (Rule 10).
4. Surebet Arbitrage Calculations.
5. Social Card Generation.
"""

import os
from pathlib import Path
import sys
import unittest

# Add smm-agent to sys.path
CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
PARENT_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))
if PARENT_DIR not in sys.path:
    sys.path.insert(0, PARENT_DIR)

from apk_manager import BOOKMAKER_CATALOG, BookmakerAPKManager
from mobile_coupon_capture import CouponLeg, MobileCouponCaptureEngine, SocialCardPayload
from mobile_device_pool import (
    AndroidDevice,
    DeviceStatus,
    IsolationViolationError,
    MobileDevicePoolManager,
    PoolType,
)


class TestMobileDeviceEcosystem(unittest.TestCase):

    def setUp(self):
        self.pool_manager = MobileDevicePoolManager(adb_binary_path="mock_adb")
        self.temp_storage = os.path.join(PARENT_DIR, "scratch", "test_mobile_storage")
        os.makedirs(self.temp_storage, exist_ok=True)
        self.apk_manager = BookmakerAPKManager(storage_dir=self.temp_storage)
        self.capture_engine = MobileCouponCaptureEngine(output_dir=self.temp_storage)

    def test_pool_inventory_initialization(self):
        """Verify pre-configured pools exist and have correct geo routing."""
        smm_devices = self.pool_manager.get_devices_by_pool(PoolType.SMM_POSTER)
        ru_devices = self.pool_manager.get_devices_by_pool(PoolType.CAPTURE_RU)
        global_devices = self.pool_manager.get_devices_by_pool(PoolType.CAPTURE_GLOBAL)

        self.assertGreaterEqual(len(smm_devices), 1)
        self.assertGreaterEqual(len(ru_devices), 1)
        self.assertGreaterEqual(len(global_devices), 1)

        # Check that SMM phones are on foreign proxies
        for dev in smm_devices:
            self.assertIn("purevpn", dev.assigned_proxy.lower())

        # Check that RU capture phones are on direct SPb IP
        for dev in ru_devices:
            self.assertEqual(dev.assigned_proxy, "direct-spb")

    def test_strict_isolation_guard_smm_protection(self):
        """
        RULE 13: Attempting to install or run a betting app on an SMM_POSTER device
        MUST raise an IsolationViolationError.
        """
        smm_device = self.pool_manager.get_devices_by_pool(PoolType.SMM_POSTER)[0]

        with self.assertRaises(IsolationViolationError) as ctx:
            self.apk_manager.install_on_device(
                device_id=smm_device.device_id,
                bookmaker_key="winline",
                pool_manager=self.pool_manager
            )
        self.assertIn("SECURITY VIOLATION", str(ctx.exception))

    def test_strict_routing_guard_ru_on_foreign_ip(self):
        """
        RULE 6 & 13: Russian bookmakers cannot be installed on Global devices (foreign IP).
        """
        global_device = self.pool_manager.get_devices_by_pool(PoolType.CAPTURE_GLOBAL)[0]

        with self.assertRaises(IsolationViolationError) as ctx:
            self.apk_manager.install_on_device(
                device_id=global_device.device_id,
                bookmaker_key="fonbet",
                pool_manager=self.pool_manager
            )
        self.assertIn("ROUTING VIOLATION", str(ctx.exception))

    def test_catalog_completeness(self):
        """Verify bookmaker catalog has required metadata and launcher activities."""
        expected_keys = ["winline", "fonbet", "pari", "betcity", "pinnacle", "1xbet", "betano", "bet365"]
        for key in expected_keys:
            self.assertIn(key, BOOKMAKER_CATALOG)
            meta = BOOKMAKER_CATALOG[key]
            self.assertTrue(meta.package_name)
            self.assertTrue(meta.download_url)
            self.assertTrue(meta.theme_color.startswith("#"))

    def test_freebet_80_math_accuracy(self):
        """
        RULE 10: Freebet conversion formula eta = ((K1 - 1) * (K2 - 1)) / K2.
        For K1=5.20 and K2=1.24 with Freebet=3000 RUB, guaranteed cash must be ~80% (approx 2438 RUB).
        """
        res = self.capture_engine.calculate_freebet_80_cash(freebet_amount=3000.0, k1=5.20, k2=1.24)

        self.assertAlmostEqual(res["conversion_percentage"], 81.3, delta=0.5)
        self.assertAlmostEqual(res["guaranteed_cash"], 2438.71, delta=1.0)
        self.assertAlmostEqual(res["hedge_stake"], 10161.29, delta=1.0)

    def test_surebet_arbitrage_math(self):
        """Verify 2-way surebet arbitrage math."""
        # K1=2.15, K2=2.10 -> Inv sum = 1/2.15 + 1/2.10 = 0.4651 + 0.4762 = 0.9413 < 1.0 (Profitable!)
        res = self.capture_engine.calculate_surebet(k1=2.15, k2=2.10, total_bankroll=10000.0)

        self.assertGreater(res["profit_percentage"], 0.0)
        self.assertGreater(res["guaranteed_profit"], 0.0)
        self.assertAlmostEqual(res["stake_1"] + res["stake_2"], 10000.0, delta=1.0)

    def test_social_card_rendering_pipeline(self):
        """Renders a social card and verifies artifact generation."""
        payload = SocialCardPayload(
            title="Отыгрыш фрибета Winline 3000 руб",
            card_type="FREEBET_CONVERSION",
            leg_1=CouponLeg(
                bookmaker_key="winline",
                match_title="Манчестер Сити vs Арсенал",
                tournament="Английская Премьер-Лига",
                market_name="П1 (Победа Манчестер Сити)",
                odds=5.20,
                stake_amount=3000.0,
                is_freebet=True
            ),
            leg_2=CouponLeg(
                bookmaker_key="fonbet",
                match_title="Манчестер Сити vs Арсенал",
                tournament="Английская Премьер-Лига",
                market_name="Х2 (Ничья или Арсенал)",
                odds=1.24,
                stake_amount=10161.0,
                is_freebet=False
            ),
            guaranteed_profit=2439.0,
            conversion_ratio=0.813,
            affiliate_url="https://smartbet.guru/go/winline?utm_source=mobile_app_card"
        )

        card_path = self.capture_engine.render_social_card(payload, aspect_ratio="4:5")
        self.assertTrue(card_path.exists())
        self.assertGreater(card_path.stat().st_size, 0)


if __name__ == "__main__":
    unittest.main()
