#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Unit Tests for Multilingual Telegram Fleet & AI-Prompter
Task: [smm-telegram] Мультиязычная сеть Telegram (RU, EN, FR, ES): бот-админ, публичные @username и чтение комментариев
Plane Task ID: 434e5bc7-7530-4ef1-8cba-aaa1f6707bec
Rules 1 (5-Min Soak & DoD), 6 (Network Proxy), 9 (Firefox / Persistent Context), 10 (80% Freebet Cash)
"""

import http.client
import json
import os
import sys
import unittest
from unittest.mock import MagicMock, patch

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
PARENT_DIR = os.path.dirname(CURRENT_DIR)
if PARENT_DIR not in sys.path:
    sys.path.insert(0, PARENT_DIR)

from telegram_web_manager import (
    BOT_USERNAME,
    OWNER_PHONE,
    REGIONAL_CHANNELS,
    TelegramWebManager,
)
from channel_poster_scheduler import (
    AICommentPrompter,
    ChannelPosterScheduler,
    LocalizedCardFormatter,
    SurebetSignal,
    TelegramBotPoster,
    calculate_freebet_cash,
    normalize_bk_slug,
    start_server,
)


class TestTelegramWebManager(unittest.TestCase):
    """Tests configuration and automation of the regional Telegram channel fleet."""

    def setUp(self):
        self.manager = TelegramWebManager(
            owner_phone="+79137671550",
            bot_username="@smartbet_guru_bot",
            bot_token="",
            redis_url="redis://localhost:6379/0",
        )

    def test_regional_channel_identifiers(self):
        """Verify peer IDs, public usernames and discussion group titles across EN, FR, ES, RU."""
        en = self.manager.get_channel("en")
        self.assertIsNotNone(en)
        self.assertEqual(en.raw_id, "-3960368887")
        self.assertEqual(en.peer_id, "-1003960368887")
        self.assertEqual(en.username, "@SmartBetGuruEN")
        self.assertEqual(en.discussion_group_title, "SmartBet Community | English Discussion")

        fr = self.manager.get_channel("fr")
        self.assertIsNotNone(fr)
        self.assertEqual(fr.raw_id, "-4371643544")
        self.assertEqual(fr.peer_id, "-1004371643544")
        self.assertEqual(fr.username, "@SmartBetGuruFR")
        self.assertEqual(fr.discussion_group_title, "SmartBet Communauté | Discussion France")

        es = self.manager.get_channel("es")
        self.assertIsNotNone(es)
        self.assertEqual(es.raw_id, "-4346736376")
        self.assertEqual(es.peer_id, "-1004346736376")
        self.assertEqual(es.username, "@SmartBetGuruES")
        self.assertEqual(es.discussion_group_title, "SmartBet Comunidad | Discusión España")

        ru = self.manager.get_channel("ru")
        self.assertIsNotNone(ru)
        self.assertEqual(ru.peer_id, "-1002244889900")
        self.assertEqual(ru.username, "@SmartBetGuru")

    def test_promote_bot_admin(self):
        """Verify promoting bot to admin with full posting and chat management permissions."""
        en = self.manager.get_channel("en")
        res = self.manager.promote_bot_admin(en, dry_run=True)
        self.assertTrue(res["promoted"])
        self.assertEqual(res["bot_username"], "@smartbet_guru_bot")
        self.assertTrue(res["rights"]["can_post_messages"])
        self.assertTrue(res["rights"]["can_edit_messages"])
        self.assertTrue(res["rights"]["can_delete_messages"])
        self.assertTrue(res["rights"]["can_invite_users"])
        self.assertTrue(res["rights"]["can_manage_chat"])
        self.assertTrue(en.admin_promoted)

    def test_set_public_username(self):
        """Verify setting and validation of public Telegram handles."""
        fr = self.manager.get_channel("fr")
        res = self.manager.set_public_username(fr, username="@SmartBetGuruFR", dry_run=True)
        self.assertEqual(res["username"], "@SmartBetGuruFR")
        self.assertEqual(res["public_url"], "https://t.me/SmartBetGuruFR")
        self.assertTrue(fr.username_set)

        # Invalid username should raise ValueError
        with self.assertRaises(ValueError):
            self.manager.set_public_username(fr, username="bad")

    def test_link_discussion_group(self):
        """Verify linking discussion group to enable comments."""
        es = self.manager.get_channel("es")
        res = self.manager.link_discussion_group(es, dry_run=True)
        self.assertTrue(res["comments_enabled"])
        self.assertEqual(res["discussion_group_title"], "SmartBet Comunidad | Discusión España")
        self.assertTrue(es.discussion_linked)

    def test_run_full_setup_and_audit(self):
        """Verify full setup pipeline across all 4 channels and audit output."""
        setup_res = self.manager.run_full_setup(dry_run=True)
        self.assertEqual(setup_res["status"], "success")
        self.assertEqual(setup_res["channels_configured"], 4)

        audit = self.manager.audit_network()
        self.assertEqual(audit["total_channels"], 4)
        for code in ["ru", "en", "fr", "es"]:
            self.assertIn(code, audit["channels"])
            self.assertTrue(audit["channels"][code]["bot_promoted"])
            self.assertTrue(audit["channels"][code]["username_set"])
            self.assertTrue(audit["channels"][code]["discussion_linked"])


class TestLocalizedCardFormatting(unittest.TestCase):
    """Tests localized signal card formatting and Rule 10 80% freebet math."""

    def test_freebet_cash_math(self):
        """Rule 10: eta = ((K1 - 1) * (K2 - 1)) / K2 approx 0.80."""
        calc = calculate_freebet_cash(nominal=3000.0, k1=5.0, k2=1.25)
        self.assertEqual(calc["nominal"], 3000.0)
        self.assertEqual(calc["conversion_ratio"], 0.80)
        self.assertEqual(calc["guaranteed_cash"], 2400.0)
        self.assertEqual(calc["guaranteed_cash_percent"], 80.0)

    def test_formatting_russian_card(self):
        signal = SurebetSignal(
            sport="Футбол",
            tournament="РПЛ",
            event_name="Спартак — Зенит",
            bk1="Винлайн",
            market1="П1",
            odds1=5.0,
            bk2="Pinnacle",
            market2="Х2",
            odds2=1.25,
            profit_percent=5.25,
            freebet_nominal=3000.0,
            currency="RUB",
        )
        text, reply_markup = LocalizedCardFormatter.format_card(signal, lang="ru")
        self.assertIn("🎯 ИДЕАЛЬНО ДЛЯ ФРИБЕТА (80% ГАРАНТИРОВАННОГО КЭША)", text)
        self.assertIn("Спартак — Зенит", text)
        self.assertIn("Фрибет 3,000 ₽ → <b>2,400.00 ₽</b>", text)
        self.assertIn("Мы против лудомании", text)

        # Inline Keyboard
        buttons = reply_markup["inline_keyboard"]
        self.assertEqual(len(buttons), 2)
        self.assertIn("Калькулятор вилки & фрибета", buttons[0][0]["text"])
        self.assertIn("utm_medium=channel_ru", buttons[0][0]["url"])
        self.assertIn("Винлайн", buttons[1][0]["text"])
        self.assertIn("Pinnacle", buttons[1][1]["text"])

    def test_formatting_english_card(self):
        signal = SurebetSignal(
            sport="Football",
            tournament="Premier League",
            event_name="Arsenal vs Chelsea",
            bk1="Bet365",
            market1="Home Win",
            odds1=5.0,
            bk2="Pinnacle",
            market2="Away or Draw",
            odds2=1.25,
            profit_percent=4.80,
            freebet_nominal=50.0,
            currency="EUR",
        )
        text, reply_markup = LocalizedCardFormatter.format_card(signal, lang="en")
        self.assertIn("🎯 IDEAL FOR FREEBET (80% GUARANTEED CASH)", text)
        self.assertIn("Arsenal vs Chelsea", text)
        self.assertIn("Freebet 50 € → <b>40.00 €</b>", text)
        self.assertIn("We advocate responsible betting", text)
        self.assertIn("utm_medium=channel_en", reply_markup["inline_keyboard"][0][0]["url"])

    def test_formatting_french_and_spanish_cards(self):
        signal = SurebetSignal(
            sport="Tennis",
            tournament="Roland Garros",
            event_name="Alcaraz vs Djokovic",
            bk1="Unibet",
            market1="Vainqueur 1",
            odds1=4.80,
            bk2="Pinnacle",
            market2="Vainqueur 2",
            odds2=1.27,
            profit_percent=4.10,
            freebet_nominal=50.0,
            currency="EUR",
        )
        text_fr, markup_fr = LocalizedCardFormatter.format_card(signal, lang="fr")
        self.assertIn("🎯 IDÉAL POUR FREEBET (80% DE CASH GARANTI)", text_fr)
        self.assertIn("Jouez de manière responsable", text_fr)
        self.assertIn("utm_medium=channel_fr", markup_fr["inline_keyboard"][0][0]["url"])

        text_es, markup_es = LocalizedCardFormatter.format_card(signal, lang="es")
        self.assertIn("🎯 IDEAL PARA FREEBET (80% DE DINERO GARANTIZADO)", text_es)
        self.assertIn("Juega con responsabilidad", text_es)
        self.assertIn("utm_medium=channel_es", markup_es["inline_keyboard"][0][0]["url"])


class TestAICommentPrompter(unittest.TestCase):
    """Tests comment reading, intent classification, and AI draft responses."""

    def setUp(self):
        self.prompter = AICommentPrompter(redis_client=None)

    def test_ignore_automatic_forward_copies(self):
        update = {
            "message": {
                "message_id": 101,
                "is_automatic_forward": True,
                "text": "🎯 IDEAL FOR FREEBET\nChampions League",
                "chat": {"id": -1003960368887, "type": "supergroup"},
            }
        }
        res = self.prompter.process_incoming_comment(update)
        self.assertIsNone(res)

    def test_process_freebet_question_english(self):
        update = {
            "message": {
                "message_id": 202,
                "chat": {"id": -1003960368887, "type": "supergroup"},
                "from": {"id": 999111, "username": "london_bettor"},
                "text": "How do you achieve 80% freebet cash guaranteed?",
                "reply_to_message": {
                    "message_id": 200,
                    "text": "Premier League Arsenal vs Chelsea",
                },
            }
        }
        res = self.prompter.process_incoming_comment(update)
        self.assertIsNotNone(res)
        self.assertEqual(res["channel_code"], "en")
        self.assertEqual(res["category"], "FREEBET_CALC")
        self.assertEqual(res["priority"], "P2_COMMUNITY")
        self.assertIn("~80% guaranteed cash", res["ai_suggested_reply"])
        self.assertIn("https://smartbet.guru/tools/freebet-calculator", res["ai_suggested_reply"])

    def test_process_patron_vip_inquiry_russian(self):
        update = {
            "message": {
                "message_id": 303,
                "chat": {"id": -1002244889900, "type": "supergroup"},
                "from": {"id": 888222, "username": "vip_investor"},
                "text": "Здравствуйте, я VIP патрон. Подскажите по лимитам на этот коридор.",
                "reply_to_message": {
                    "message_id": 300,
                    "text": "Единая лига ВТБ",
                },
            }
        }
        res = self.prompter.process_incoming_comment(update)
        self.assertIsNotNone(res)
        self.assertEqual(res["channel_code"], "ru")
        self.assertEqual(res["category"], "VIP_PATRON")
        self.assertEqual(res["priority"], "P1_URGENT_PATRON")
        self.assertIn("SmartBet VIP", res["ai_suggested_reply"])
        self.assertIn("SLA 15 минут", res["ai_suggested_reply"])


class TestSchedulerAndServer(unittest.TestCase):
    """Tests scheduler broadcasting and HTTP server endpoints (/healthz, /actuator, /webhook)."""

    def setUp(self):
        self.scheduler = ChannelPosterScheduler(bot_token="", dry_run=True)
        # Choose port 18099
        self.port = 18099
        self.server = start_server(self.scheduler, port=self.port, blocking=False)

    def tearDown(self):
        if self.server:
            self.server.shutdown()
            self.server.server_close()

    def test_healthz_and_actuator_probes(self):
        conn = http.client.HTTPConnection("127.0.0.1", self.port, timeout=3)
        try:
            # /healthz
            conn.request("GET", "/healthz")
            resp = conn.getresponse()
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertEqual(data["status"], "UP")
            self.assertEqual(data["service"], "smm-bot-telegram")

            # /actuator/health
            conn.request("GET", "/actuator/health")
            resp_act = conn.getresponse()
            self.assertEqual(resp_act.status, 200)
            data_act = json.loads(resp_act.read().decode())
            self.assertEqual(data_act["status"], "UP")

            # /actuator/health/readiness
            conn.request("GET", "/actuator/health/readiness")
            resp_read = conn.getresponse()
            self.assertEqual(resp_read.status, 200)

            # /actuator/health/liveness
            conn.request("GET", "/actuator/health/liveness")
            resp_live = conn.getresponse()
            self.assertEqual(resp_live.status, 200)
        finally:
            conn.close()

    def test_webhook_post_endpoint(self):
        conn = http.client.HTTPConnection("127.0.0.1", self.port, timeout=3)
        try:
            payload = {
                "update_id": 99901,
                "message": {
                    "message_id": 555,
                    "chat": {"id": -1004371643544, "type": "supergroup"},
                    "from": {"id": 777333, "username": "paris_user"},
                    "text": "Comment fonctionne le cash garanti du freebet à 80% ?",
                },
            }
            body = json.dumps(payload)
            headers = {"Content-Type": "application/json", "Content-Length": str(len(body))}
            conn.request("POST", "/api/v1/telegram/webhook", body, headers)
            resp = conn.getresponse()
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertTrue(data["ok"])
            self.assertTrue(data["processed"])
            self.assertEqual(data["feedback"]["channel_code"], "fr")
            self.assertEqual(data["feedback"]["category"], "FREEBET_CALC")
        finally:
            conn.close()

    def test_multilingual_broadcast_cycle(self):
        res = self.scheduler.broadcast_cycle()
        self.assertEqual(res["status"], "success")
        self.assertEqual(res["channels_posted"], 4)
        for code in ["ru", "en", "fr", "es"]:
            self.assertIn(code, res["results"])
            self.assertTrue(res["results"][code]["result"]["ok"])


if __name__ == "__main__":
    unittest.main()
