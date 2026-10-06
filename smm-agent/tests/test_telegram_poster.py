#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Telegram Multilingual Poster & AI-Prompter Test Suite
Tests for:
- Freebet SNR conversion formula (80% guaranteed cash per Rule 10)
- Multilingual content formatting (RU, EN, FR, ES) and market terminology
- Responsible gambling disclaimers in all supported languages
- Inline keyboard URLs with UTM tracking tags and freebet calculator
- AI Prompter comment ingestion and intent classification
- Fallback & resilience on Telegram Bot API errors
"""

import json
import os
import sys
import unittest
from unittest.mock import MagicMock, patch

# Ensure module path is included
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from channel_poster_scheduler import (
    AICommentPrompter,
    ChannelPosterScheduler,
    LocalizedCardFormatter,
    SurebetSignal,
    calculate_freebet_cash,
    normalize_bk_slug,
    translate_bookmaker,
    translate_market,
    translate_sport,
    translate_tournament,
)
from telegram_web_manager import REGIONAL_CHANNELS, TelegramWebManager


class TestFreebetMathAndFormatting(unittest.TestCase):
    """Tests Rule 10 Freebet Matched Betting calculations and content verification."""

    def test_freebet_conversion_standard_80_percent(self):
        """
        Verify formula: eta = ((K1 - 1) * (K2 - 1)) / K2
        When K1 = 5.0, K2 = 1.25, Freebet = 3000:
        eta = (4.0 * 0.25) / 1.25 = 1.0 / 1.25 = 0.80 (80%)
        Guaranteed Cash = 3000 * 0.80 = 2400
        Hedge Stake = (3000 * 4.0) / 1.25 = 9600
        """
        res = calculate_freebet_cash(nominal=3000.0, k1=5.0, k2=1.25)

        self.assertAlmostEqual(res["conversion_ratio"], 0.80, places=3)
        self.assertAlmostEqual(res["guaranteed_cash_percent"], 80.0, places=1)
        self.assertAlmostEqual(res["guaranteed_cash"], 2400.0, places=2)
        self.assertAlmostEqual(res["hedge_stake"], 9600.0, places=2)

    def test_freebet_conversion_different_odds(self):
        """Test with K1 = 5.20, K2 = 1.26, Freebet = 50 EUR."""
        res = calculate_freebet_cash(nominal=50.0, k1=5.20, k2=1.26)
        expected_eta = (4.20 * 0.26) / 1.26
        self.assertAlmostEqual(res["conversion_ratio"], round(expected_eta, 4), places=3)
        self.assertAlmostEqual(res["guaranteed_cash"], round(50.0 * expected_eta, 2), places=1)

    def test_multilingual_formatting_ru(self):
        """Verify Russian post structure, disclaimer, and 80% freebet mention."""
        signal = SurebetSignal(
            sport="football",
            tournament="UEFA Champions League",
            event_name="Arsenal vs Real Madrid",
            bk1="Winline",
            market1="Победа 1 (П1)",
            odds1=5.20,
            bk2="Pinnacle",
            market2="X2",
            odds2=1.26,
            profit_percent=4.15,
            freebet_nominal=3000.0,
            currency="RUB",
        )
        post_text, reply_markup = LocalizedCardFormatter.format_card(signal, lang="ru")

        self.assertIn("80% ГАРАНТИРОВАННЫЙ КЭШ", post_text)
        self.assertIn("Футбол", post_text)
        self.assertIn("Лига чемпионов УЕФА", post_text)
        self.assertIn("чистыми деньгами на баланс при любом исходе через вилку", post_text)
        self.assertIn("Ставки на спорт сопряжены с финансовыми рисками", post_text)
        self.assertIn("inline_keyboard", reply_markup)

    def test_multilingual_formatting_en(self):
        """Verify English post structure, disclaimer, and 80% freebet mention."""
        signal = SurebetSignal(
            sport="football",
            tournament="UEFA Champions League",
            event_name="Arsenal vs Real Madrid",
            bk1="Bet365",
            market1="1",
            odds1=5.20,
            bk2="Pinnacle",
            market2="X2",
            odds2=1.26,
            profit_percent=4.15,
            freebet_nominal=50.0,
            currency="EUR",
        )
        post_text, reply_markup = LocalizedCardFormatter.format_card(signal, lang="en")

        self.assertIn("IDEAL FOR FREEBET (80% GUARANTEED CASH)", post_text)
        self.assertIn("Football", post_text)
        self.assertIn("Sports betting involves financial risks", post_text)
        self.assertIn("inline_keyboard", reply_markup)

    def test_multilingual_formatting_fr(self):
        """Verify French post structure, disclaimer, and 80% freebet mention."""
        signal = SurebetSignal(
            sport="football",
            tournament="UEFA Champions League",
            event_name="PSG vs Marseille",
            bk1="Betclic",
            market1="1",
            odds1=5.00,
            bk2="Pinnacle",
            market2="X2",
            odds2=1.25,
            profit_percent=5.00,
            freebet_nominal=50.0,
            currency="EUR",
        )
        post_text, reply_markup = LocalizedCardFormatter.format_card(signal, lang="fr")

        self.assertIn("IDÉAL POUR FREEBET (80% DE CASH GARANTI)", post_text)
        self.assertIn("Les paris sportifs comportent des risques financiers", post_text)

    def test_multilingual_formatting_es(self):
        """Verify Spanish post structure, disclaimer, and 80% freebet mention."""
        signal = SurebetSignal(
            sport="football",
            tournament="UEFA Champions League",
            event_name="Real Madrid vs Barcelona",
            bk1="Codere",
            market1="1",
            odds1=5.00,
            bk2="Pinnacle",
            market2="X2",
            odds2=1.25,
            profit_percent=5.00,
            freebet_nominal=50.0,
            currency="EUR",
        )
        post_text, reply_markup = LocalizedCardFormatter.format_card(signal, lang="es")

        self.assertIn("IDEAL PARA FREEBET (80% DE DINERO GARANTIZADO)", post_text)
        self.assertIn("Las apuestas deportivas conllevan riesgos financieros", post_text)


class TestAIPrompterAndIngestion(unittest.TestCase):
    """Tests discussion group comment processing and AI prompter categorization."""

    def setUp(self):
        self.prompter = AICommentPrompter(bot_poster=None, redis_client=None)

    def test_freebet_calculation_inquiry(self):
        update = {
            "update_id": 1001,
            "message": {
                "message_id": 100,
                "chat": {"id": -1003960368887, "type": "supergroup"},
                "from": {"id": 999, "username": "bettor123"},
                "text": "Как рассчитывается 80% кэша с фрибета?",
                "reply_to_message": {
                    "message_id": 90,
                    "text": "ИДЕАЛЬНО ПОД ФРИБЕТ (80% ГАРАНТИРОВАННЫХ ДЕНЕГ)",
                },
            },
        }
        res = self.prompter.process_incoming_comment(update)
        self.assertIsNotNone(res)
        self.assertEqual(res["category"], "FREEBET_CALC")
        self.assertIn("80%", res["ai_suggested_reply"])

    def test_vip_inquiry(self):
        update = {
            "update_id": 1002,
            "message": {
                "message_id": 101,
                "chat": {"id": -1003960368887, "type": "supergroup"},
                "from": {"id": 888, "username": "patron_user"},
                "text": "Хочу доступ в закрытый VIP клуб и сигналы без задержек",
            },
        }
        res = self.prompter.process_incoming_comment(update)
        self.assertIsNotNone(res)
        self.assertEqual(res["category"], "VIP_PATRON")


class TestTelegramSchedulerDispatcher(unittest.TestCase):
    """Tests ChannelPosterScheduler posting and fallback."""

    def test_dry_run_dispatch(self):
        scheduler = ChannelPosterScheduler(bot_token="test_token", dry_run=True)
        res = scheduler.post_signal_to_channel("ru")
        self.assertEqual(res["channel_code"], "ru")
        self.assertTrue(res["result"]["ok"])

    def test_broadcast_all_channels(self):
        scheduler = ChannelPosterScheduler(bot_token="test_token", dry_run=True)
        summary = scheduler.broadcast_cycle()
        self.assertEqual(summary["status"], "success")
        self.assertEqual(summary["channels_posted"], 4)
        self.assertIn("ru", summary["results"])
        self.assertIn("en", summary["results"])
        self.assertIn("fr", summary["results"])
        self.assertIn("es", summary["results"])


if __name__ == "__main__":
    unittest.main()
