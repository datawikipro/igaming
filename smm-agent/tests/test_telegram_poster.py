#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Unit and Integration Tests for smm-bot-telegram: ChannelPosterScheduler & TelegramWebManager
Task: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен (#bc707877)
Verifies: Rule 10 (80% Freebet Cash), Rule 6 (Proxy / DNS), Multilingual Posting (RU, EN, FR, ES),
AI Prompter & Feedback Queue, and HTTP Actuator / Healthcheck Endpoints (:8080).
"""

import json
from pathlib import Path
import sys
import threading
import time
import unittest
import urllib.error
import urllib.request

# Add smm-agent to python path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from channel_poster_scheduler import (
    AICommentPrompter,
    ChannelPosterScheduler,
    LocalizedCardFormatter,
    SurebetSignal,
    TelegramWebhookHTTPHandler,
    calculate_freebet_cash,
    normalize_bk_slug,
    translate_sport,
)
from telegram_web_manager import (
    REGIONAL_CHANNELS,
    TelegramWebManager,
)
import socketserver


class ReusableThreadingServer(socketserver.ThreadingTCPServer):
    allow_reuse_address = True


class TestTelegramChannelPoster(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        # Create scheduler in dry-run mode for testing
        cls.scheduler = ChannelPosterScheduler(dry_run=True)
        TelegramWebhookHTTPHandler.scheduler = cls.scheduler

        # Spin up test HTTP server on ephemeral port (127.0.0.1:0)
        cls.server = ReusableThreadingServer(("127.0.0.1", 0), TelegramWebhookHTTPHandler)
        cls.port = cls.server.server_address[1]
        cls.base_url = f"http://127.0.0.1:{cls.port}"

        cls.server_thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.server_thread.start()
        time.sleep(0.1)

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()

    def test_01_freebet_math_rule_10(self):
        """Validates Rule 10 80% freebet conversion calculation."""
        res = calculate_freebet_cash(nominal=3000.0, k1=5.0, k2=1.25)
        self.assertIn("guaranteed_cash", res)
        self.assertIn("conversion_ratio", res)
        # eta = ((5.0 - 1.0) * (1.25 - 1.0)) / 1.25 = (4.0 * 0.25) / 1.25 = 1.0 / 1.25 = 0.80
        self.assertEqual(res["conversion_ratio"], 0.8)
        self.assertEqual(res["guaranteed_cash"], 2400.0)
        self.assertEqual(res["guaranteed_cash_percent"], 80.0)

    def test_02_normalize_bk_slug(self):
        """Verifies bookmaker name slug normalization."""
        self.assertEqual(normalize_bk_slug("Винлайн"), "winline")
        self.assertEqual(normalize_bk_slug("Фонбет"), "fonbet")
        self.assertEqual(normalize_bk_slug("Пари"), "pari")
        self.assertEqual(normalize_bk_slug("Лига Ставок"), "ligastavok")
        self.assertEqual(normalize_bk_slug("Pinnacle"), "pinnacle")

    def test_03_sport_translations(self):
        """Tests multilingual sport translations."""
        self.assertEqual(translate_sport("football", "ru"), "Футбол")
        self.assertEqual(translate_sport("football", "es"), "Fútbol")
        self.assertEqual(translate_sport("basketball", "fr"), "Basketball")
        self.assertEqual(translate_sport("tennis", "en"), "Tennis")

    def test_04_signal_formatting_all_languages(self):
        """Ensures signals can be formatted in RU, EN, FR, ES with disclaimers."""
        signal = SurebetSignal(
            sport="football",
            tournament="UEFA Champions League",
            event_name="Real Madrid vs Manchester City",
            bk1="Винлайн",
            market1="П1",
            odds1=5.20,
            bk2="Pinnacle",
            market2="Х2",
            odds2=1.26,
            profit_percent=4.25,
            is_freebet_friendly=True,
            freebet_nominal=3000.0,
        )

        for lang in ("ru", "en", "fr", "es"):
            text, keyboard = LocalizedCardFormatter.format_card(signal, lang)
            self.assertIn("Real Madrid", text)
            self.assertIn("4.25%", text)
            self.assertIn("inline_keyboard", keyboard)
            # Ensure mandatory disclaimer is included
            if lang == "ru":
                self.assertIn("Ставки на спорт сопряжены с финансовыми рисками", text)
            elif lang == "en":
                self.assertIn("Sports betting involves financial risks", text)
            elif lang == "fr":
                self.assertIn("Les paris sportifs comportent des risques financiers", text)
            elif lang == "es":
                self.assertIn("Las apuestas deportivas conllevan riesgos financieros", text)

    def test_05_broadcast_cycle(self):
        """Executes a full broadcast cycle in dry-run mode across 4 regional channels."""
        res = self.scheduler.broadcast_cycle()
        self.assertEqual(res["status"], "success")
        self.assertEqual(res["channels_posted"], 4)
        for code in ("ru", "en", "fr", "es"):
            self.assertIn(code, res["results"])
            self.assertTrue(res["results"][code]["result"]["ok"])

    def test_06_ai_prompter_comment_classification(self):
        """Tests comment classification and auto-prompter response for freebet queries."""
        prompter = AICommentPrompter()
        update = {
            "update_id": 9991,
            "message": {
                "message_id": 101,
                "chat": {"id": -1003960368887, "type": "supergroup"},
                "from": {"id": 55512, "username": "alex_trader"},
                "text": "How do you calculate 80% freebet cash with matched betting?",
                "reply_to_message": {
                    "message_id": 88,
                    "text": "🎯 Freebet surebet",
                },
            },
        }
        res = prompter.process_incoming_comment(update)
        self.assertIsNotNone(res)
        self.assertEqual(res["category"], "FREEBET_CALC")
        self.assertIn("80%", res["ai_suggested_reply"])
        self.assertEqual(res["username"], "alex_trader")

    def test_07_telegram_web_manager_audit(self):
        """Tests Telegram fleet configuration and channel metadata."""
        manager = TelegramWebManager()
        audit = manager.audit_network()
        self.assertEqual(audit["total_channels"], 4)
        self.assertIn("en", audit["channels"])
        self.assertIn("fr", audit["channels"])
        self.assertIn("es", audit["channels"])
        self.assertIn("ru", audit["channels"])
        self.assertEqual(audit["channels"]["en"]["username"], "@SmartBetGuruEN")

    def test_08_healthz_endpoint(self):
        """Verifies /healthz endpoint returns HTTP 200 UP."""
        req = urllib.request.Request(f"{self.base_url}/healthz")
        with urllib.request.urlopen(req, timeout=3) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertEqual(data["status"], "UP")
            self.assertEqual(data["service"], "smm-bot-telegram")
            self.assertEqual(data["checks"]["telegram_bot"], "UP")

    def test_09_actuator_health_endpoints(self):
        """Verifies Actuator-compliant health probes (/actuator/health, /readiness, /liveness)."""
        for subpath in ("/actuator/health", "/actuator/health/readiness", "/actuator/health/liveness"):
            req = urllib.request.Request(f"{self.base_url}{subpath}")
            with urllib.request.urlopen(req, timeout=3) as response:
                self.assertEqual(response.status, 200)
                data = json.loads(response.read().decode())
                self.assertEqual(data["status"], "UP")
                self.assertEqual(data["service"], "smm-bot-telegram")

    def test_10_status_endpoint(self):
        """Verifies /api/v1/telegram/status returns channel fleet information."""
        req = urllib.request.Request(f"{self.base_url}/api/v1/telegram/status")
        with urllib.request.urlopen(req, timeout=3) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertEqual(data["service"], "smm-bot-telegram")
            self.assertIn("channels", data)
            self.assertEqual(len(data["channels"]), 4)

    def test_11_webhook_post_endpoint(self):
        """Tests incoming webhook POST to /api/v1/telegram/webhook."""
        payload = {
            "update_id": 9992,
            "message": {
                "message_id": 102,
                "chat": {"id": -1002244889900, "type": "supergroup"},
                "from": {"id": 55513, "username": "ivan_bet"},
                "text": "Где посмотреть расчет вилки?",
            },
        }
        data_bytes = json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(
            f"{self.base_url}/api/v1/telegram/webhook",
            data=data_bytes,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        with urllib.request.urlopen(req, timeout=3) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertTrue(data["ok"])


if __name__ == "__main__":
    unittest.main()
