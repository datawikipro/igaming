#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import unittest
from pathlib import Path

# Add smm-agent to path
BASE_DIR = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE_DIR))

from channel_poster_scheduler import (
    BetTypeRegistry,
    BetTypeRenderer,
    HandicapBet,
    TotalBet,
    SportType,
    SurebetSignal,
    LocalizedCardFormatter,
    translate_tournament,
    translate_sport,
)


class TestChannelPosterSchedulerFixes(unittest.TestCase):
    """Verifies that sports, tournaments, and bet types (HANDICAP, TOTAL) are rendered cleanly in all languages."""

    def test_handicap_parsing_and_human_readable_rendering(self):
        # 1. Parse HANDICAP_1 (-1.5)
        bet1 = BetTypeRegistry.from_code("HANDICAP_1 (-1.5)")
        self.assertIsInstance(bet1, HandicapBet)
        self.assertEqual(bet1.param, -1.5)

        # 2. Parse HANDICAP_2 (+1.5)
        bet2 = BetTypeRegistry.from_code("HANDICAP_2 (+1.5)")
        self.assertIsInstance(bet2, HandicapBet)
        self.assertEqual(bet2.param, 1.5)

        # 3. Render Russian
        event_ru = "Османиеспор vs Мерсин Идманюрду"
        leg1_ru = BetTypeRenderer.render("HANDICAP_1 (-1.5)", lang="ru", event_name=event_ru)
        leg2_ru = BetTypeRenderer.render("HANDICAP_2 (+1.5)", lang="ru", event_name=event_ru)
        self.assertEqual(leg1_ru, "Фора 1 (Османиеспор) (-1.5)")
        self.assertEqual(leg2_ru, "Фора 2 (Мерсин Идманюрду) (+1.5)")

        # 4. Render Spanish
        event_es = "Дрозд А. vs Климента М."
        leg1_es = BetTypeRenderer.render("HANDICAP_1 (-5.5)", lang="es", event_name=event_es)
        leg2_es = BetTypeRenderer.render("HANDICAP_2 (+5.5)", lang="es", event_name=event_es)
        self.assertEqual(leg1_es, "Hándicap 1 (Дрозд А.) (-5.5)")
        self.assertEqual(leg2_es, "Hándicap 2 (Климента М.) (+5.5)")

        # 5. Render English
        leg1_en = BetTypeRenderer.render("HANDICAP_1 (-1.5)", lang="en", event_name=event_ru)
        self.assertEqual(leg1_en, "Handicap 1 (Османиеспор) (-1.5)")

        # 6. Render French
        leg1_fr = BetTypeRenderer.render("HANDICAP_1 (-1.5)", lang="fr", event_name=event_ru)
        self.assertEqual(leg1_fr, "Handicap 1 (Османиеспор) (-1.5)")

    def test_totals_parsing_and_human_readable_rendering(self):
        # 1. Parse TOTAL_OVER (+4.5)
        bet_ov = BetTypeRegistry.from_code("TOTAL_OVER (+4.5)")
        self.assertIsInstance(bet_ov, TotalBet)
        self.assertEqual(bet_ov.param, 4.5)

        # 2. Render Football Totals
        rendered_ru = BetTypeRenderer.render("TOTAL_OVER (+4.5)", lang="ru", sport="FOOTBALL")
        rendered_es = BetTypeRenderer.render("TOTAL_OVER (+4.5)", lang="es", sport="FOOTBALL")
        rendered_en = BetTypeRenderer.render("TOTAL_OVER (+4.5)", lang="en", sport="FOOTBALL")
        rendered_fr = BetTypeRenderer.render("TOTAL_OVER (+4.5)", lang="fr", sport="FOOTBALL")

        self.assertEqual(rendered_ru, "Тотал больше 4.5")
        self.assertEqual(rendered_es, "Más de 4.5 goles")
        self.assertEqual(rendered_en, "Over 4.5 Goals")
        self.assertEqual(rendered_fr, "Plus de 4.5 buts")

        # 3. Render Table Tennis Totals (Points instead of Goals)
        rendered_tt_es = BetTypeRenderer.render("TOTAL_OVER (75.5)", lang="es", sport="TABLE_TENNIS")
        self.assertEqual(rendered_tt_es, "Más de 75.5 puntos")

    def test_table_tennis_card_formatting_spanish(self):
        """Matches user Scenario 1: Drozd vs Klimenta (TABLE_TENNIS) in Spanish."""
        sig = SurebetSignal(
            sport="TABLE_TENNIS",
            tournament="Лига (Football)",
            event_name="Дрозд А. vs Климента М. (TABLE_TENNIS)",
            bk1="Unibet",
            odds1=2.11,
            bk2="Pinnacle",
            odds2=2.05,
            profit_percent=3.83,
            bet_type1="HANDICAP_1 (-5.5)",
            bet_type2="HANDICAP_2 (+5.5)",
            currency="EUR",
        )
        card_html, _ = LocalizedCardFormatter.format_card(sig, lang="es")

        # Must NOT contain raw HANDICAP_1 or FOOTBALL for table tennis
        self.assertNotIn("HANDICAP_1", card_html)
        self.assertNotIn("HANDICAP_2", card_html)
        self.assertNotIn("(TABLE_TENNIS)", card_html)
        self.assertNotIn("(Football)", card_html)
        self.assertNotIn("Fútbol", card_html)

        # Must contain localized sport and human-readable handicaps
        self.assertIn("Liga de Tenis de mesa", card_html)
        self.assertIn("Deporte: Tenis de mesa", card_html)
        self.assertIn("Hándicap 1 (Дрозд А.) (-5.5)", card_html)
        self.assertIn("Hándicap 2 (Климента М.) (+5.5)", card_html)
        self.assertIn("Juega con responsabilidad.", card_html)

    def test_football_card_formatting_russian(self):
        """Matches user Scenario 2: Osmaniyespor vs Mersin Idmanyurdu in Russian."""
        sig = SurebetSignal(
            sport="FOOTBALL",
            tournament="Лига (Football)",
            event_name="Османиеспор vs Мерсин Идманюрду (FOOTBALL)",
            bk1="Пари",
            odds1=2.75,
            bk2="Linebet",
            odds2=2.00,
            profit_percent=13.64,
            bet_type1="HANDICAP_1 (-1.5)",
            bet_type2="HANDICAP_2 (+1.5)",
            currency="RUB",
        )
        card_html, _ = LocalizedCardFormatter.format_card(sig, lang="ru")

        # Must NOT contain raw HANDICAP_1 or English (Football) in Russian card
        self.assertNotIn("HANDICAP_1", card_html)
        self.assertNotIn("HANDICAP_2", card_html)
        self.assertNotIn("(FOOTBALL)", card_html)
        self.assertNotIn("(Football)", card_html)

        # Must contain human-readable Russian betting terms
        self.assertIn("Футбол — Лига", card_html)
        self.assertIn("Спорт: Футбол", card_html)
        self.assertIn("Фора 1 (Османиеспор) (-1.5)", card_html)
        self.assertIn("Фора 2 (Мерсин Идманюрду) (+1.5)", card_html)
        self.assertIn("Играйте ответственно.", card_html)


if __name__ == "__main__":
    unittest.main()
