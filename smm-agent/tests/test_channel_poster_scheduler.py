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
    ChannelPosterScheduler,
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

        # Must contain localized sport and human-readable handicaps (with Anti-Cyrillic Latin transliteration)
        self.assertIn("Liga de Tenis de mesa", card_html)
        self.assertIn("Deporte: Tenis de mesa", card_html)
        self.assertIn("Hándicap 1 (Drozd A.) (-5.5)", card_html)
        self.assertIn("Hándicap 2 (Klimenta M.) (+5.5)", card_html)
        self.assertIn("Juega con responsabilidad.", card_html)

        # Anti-Cyrillic Guard: Spanish card MUST contain 0 Cyrillic characters
        import re
        self.assertFalse(
            bool(re.search(r"[\u0400-\u04FF]", card_html)),
            f"Spanish card contains Cyrillic characters: {card_html}"
        )

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

    def test_french_card_anti_cyrillic_and_localization(self):
        """Matches user problem: Betis vs Barcelona in French with 0 Cyrillic and consistent math."""
        import re
        sig = SurebetSignal(
            sport="FOOTBALL",
            tournament="Испания. Примера дивизион. Сезон 26/27",
            event_name="Бетис vs Барселона",
            bk1="Bet365",
            odds1=1.43,
            bk2="Winamax",
            odds2=10.00,
            profit_percent=24.41,
            bet_type1="WIN2",
            bet_type2="WIN1",
            currency="EUR",
        )
        card_html, _ = LocalizedCardFormatter.format_card(sig, lang="fr")

        # Anti-Cyrillic Guard: Must have 0 Cyrillic characters
        cyrillic_matches = re.findall(r"[\u0400-\u04FF]+", card_html)
        self.assertEqual(cyrillic_matches, [], f"Found Cyrillic in French card: {cyrillic_matches}")

        # Localized tournament & teams
        self.assertIn("Espagne. Primera División. Saison 26/27", card_html)
        self.assertIn("Real Betis vs Barcelona", card_html)
        self.assertIn("Sport: Football", card_html)
        self.assertIn("Victoire Barcelona (2)", card_html)
        self.assertIn("Victoire Real Betis (1)", card_html)

        # Consistent yield & profit
        self.assertIn("+25.10%", card_html)
        self.assertIn("+2,510.00 € (+25.10%)", card_html)

    def test_freebet_odds_orientation_and_bankroll_protection_spanish(self):
        """Verifies fix for Napoli vs AS Roma: odds1=1.20, odds2=10.75."""
        import re
        sig = SurebetSignal(
            sport="FOOTBALL",
            tournament="Италия. Серия А. Сезон 26/27",
            event_name="Наполи vs Рома",
            bk1="Bet365",
            odds1=1.20,
            bk2="William Hill",
            odds2=10.75,
            profit_percent=7.95,
            bet_type1="TOTAL_OVER (+0.5)",
            bet_type2="TOTAL_UNDER (+0.5)",
            currency="EUR",
            freebet_nominal=50.0,
            recommended_bank=100.0,
        )
        card_html, _ = LocalizedCardFormatter.format_card(sig, lang="es")

        # 1. Anti-Cyrillic Guard: Must have 0 Cyrillic characters
        cyrillic_matches = re.findall(r"[\u0400-\u04FF]+", card_html)
        self.assertEqual(cyrillic_matches, [], f"Found Cyrillic in Spanish card: {cyrillic_matches}")

        # 2. Freebet is assigned to William Hill @ 10.75, Hedge to Bet365 @ 1.20
        self.assertIn("Selección 1 (🛡️ Cobertura):</b> Bet365", card_html)
        self.assertIn("Selección 2 (🎁 Freebet):</b> William Hill", card_html)

        # 3. Bankroll protection scales nominal down to 10 € (so hedge is 81.25 € <= 100 € bank)
        self.assertIn("Freebet 10 € → <b>16.25 €</b> de dinero garantizado", card_html)
        self.assertIn("Apuesta gratis: <b>10 €</b> en William Hill @ <b>10.75</b>", card_html)
        self.assertIn("Cobertura en efectivo: <b>81.25 €</b> en Bet365 @ <b>1.20</b>", card_html)

    def test_freebet_orientation_russian(self):
        """Verifies Freebet Leg 1 (high odds) and Leg 2 (low odds) in Russian."""
        sig = SurebetSignal(
            sport="FOOTBALL",
            tournament="Россия. Премьер-лига",
            event_name="Спартак vs Зенит",
            bk1="Винлайн",
            odds1=5.00,
            bk2="Фонбет",
            odds2=1.25,
            profit_percent=0.0,
            currency="RUB",
            freebet_nominal=1000.0,
            recommended_bank=10000.0,
        )
        card_html, _ = LocalizedCardFormatter.format_card(sig, lang="ru")

        # Freebet is assigned to Winline @ 5.00, Hedge to Fonbet @ 1.25
        self.assertIn("Плечо 1 (🎁 Фрибет):</b> Винлайн", card_html)
        self.assertIn("Плечо 2 (🛡️ Перекрытие):</b> Фонбет", card_html)
        self.assertIn("Фрибет 1,000 ₽ → <b>800.00 ₽</b>", card_html)
        self.assertIn("Ставка фрибетом: <b>1,000 ₽</b> в Винлайн @ <b>5.00</b>", card_html)
        self.assertIn("Перекрытие деньгами: <b>3,200.00 ₽</b> в Фонбет @ <b>1.25</b>", card_html)

    def test_freebet_orientation_russian_favorite_first(self):
        """Verifies fix when Leg 1 is favorite (1.20) and Leg 2 is underdog (9.50) matching user scenario."""
        sig = SurebetSignal(
            sport="FOOTBALL",
            tournament="Лига Чемпионов Азии 2. Групповой этап",
            event_name="Аль-Кувейт vs Аль-Халидия",
            bk1="Fon-Bet-Ru",
            odds1=1.20,
            bk2="Олимпбет",
            odds2=9.50,
            profit_percent=6.54,
            market1="Тотал больше 0.5",
            market2="Тотал меньше 0.5",
            currency="RUB",
            freebet_nominal=1000.0,
            recommended_bank=10000.0,
        )
        card_html, _ = LocalizedCardFormatter.format_card(sig, lang="ru")

        # Leg 1 (1.20) MUST have Hedge tag, Leg 2 (9.50) MUST have Freebet tag.
        # Fon-Bet-Ru is normalized to clean Russian name "Фонбет"
        self.assertIn("Плечо 1 (🛡️ Перекрытие):</b> Фонбет", card_html)
        self.assertIn("Плечо 2 (🎁 Фрибет):</b> Олимпбет", card_html)
        self.assertIn("Ставка фрибетом: <b>1,000 ₽</b> в Олимпбет @ <b>9.50</b>", card_html)
        self.assertIn("Перекрытие деньгами: <b>7,083.33 ₽</b> в Фонбет @ <b>1.20</b>", card_html)
        self.assertIn("Фрибет 1,000 ₽ → <b>1,416.67 ₽</b>", card_html)

    def test_russian_channel_excludes_by_and_kz_bookmakers(self):
        """Verifies that signals with Belarusian (BYN) or Kazakh (KZT) bookmakers are rejected for Russian RUB channel."""
        # Signal with Fon-Bet-By (BYN) vs Olimpbet (RUB)
        sig_by = SurebetSignal(
            sport="FOOTBALL",
            tournament="Лига",
            event_name="Матч 1",
            bk1="Fon-Bet-By",
            odds1=5.20,
            bk2="Олимпбет",
            odds2=1.26,
            profit_percent=4.15,
            raw_bk1="fon-bet-by",
            raw_bk2="olimpbet",
        )
        self.assertFalse(ChannelPosterScheduler._is_signal_compatible_with_channel(sig_by, "ru"))

        # Signal with Fon-Bet-Kz (KZT) vs Winline (RUB)
        sig_kz = SurebetSignal(
            sport="FOOTBALL",
            tournament="Лига",
            event_name="Матч 2",
            bk1="Winline",
            odds1=2.10,
            bk2="Fon-Bet-Kz",
            odds2=2.05,
            profit_percent=3.50,
            raw_bk1="winline",
            raw_bk2="fon-bet-kz",
        )
        self.assertFalse(ChannelPosterScheduler._is_signal_compatible_with_channel(sig_kz, "ru"))

        # Signal with Marathonbet-By (BYN) vs Leon (RUB)
        sig_mara_by = SurebetSignal(
            sport="FOOTBALL",
            tournament="Лига",
            event_name="Матч 3",
            bk1="Леон",
            odds1=2.34,
            bk2="marathonbet-by",
            odds2=3.60,
            profit_percent=2.50,
            raw_bk1="leon",
            raw_bk2="marathonbet-by",
        )
        self.assertFalse(ChannelPosterScheduler._is_signal_compatible_with_channel(sig_mara_by, "ru"))

        # Valid RU signal: Fon-Bet-Ru vs Winline
        sig_ru = SurebetSignal(
            sport="FOOTBALL",
            tournament="Лига",
            event_name="Матч 4",
            bk1="fon-bet-ru",
            odds1=2.08,
            bk2="winline",
            odds2=2.02,
            profit_percent=2.45,
            raw_bk1="fon-bet-ru",
            raw_bk2="winline",
        )
        self.assertTrue(ChannelPosterScheduler._is_signal_compatible_with_channel(sig_ru, "ru"))

    def test_bookmaker_display_names_and_currencies(self):
        """Verifies bookmaker name normalization and currency safety."""
        from channel_poster_scheduler import normalize_bk_display_name, BOOKMAKER_METADATA

        self.assertEqual(normalize_bk_display_name("fon-bet-ru"), "Фонбет")
        self.assertEqual(normalize_bk_display_name("fon-bet-by"), "Фонбет (BY)")
        self.assertEqual(normalize_bk_display_name("fon-bet-kz"), "Фонбет (KZ)")
        self.assertEqual(normalize_bk_display_name("marathonbet-by"), "Марафон (BY)")
        self.assertEqual(normalize_bk_display_name("olimpbet-kz"), "Олимпбет (KZ)")

        self.assertEqual(BOOKMAKER_METADATA["fon-bet-by"]["currency"], "BYN")
        self.assertEqual(BOOKMAKER_METADATA["fon-bet-kz"]["currency"], "KZT")
        self.assertEqual(BOOKMAKER_METADATA["fon-bet-ru"]["currency"], "RUB")

    def test_belarusian_signal_currency_formatting(self):
        """Verifies that an explicitly Belarusian signal uses Br (BYN) currency symbol, NEVER ₽."""
        sig = SurebetSignal(
            sport="FOOTBALL",
            tournament="Беларусь. Высшая лига",
            event_name="Динамо Минск vs БАТЭ",
            bk1="fon-bet-by",
            odds1=2.10,
            bk2="marathonbet-by",
            odds2=2.05,
            profit_percent=3.60,
            currency="BYN",
            freebet_nominal=50.0,
            recommended_bank=500.0,
        )
        card_html, _ = LocalizedCardFormatter.format_card(sig, lang="ru")
        self.assertNotIn("₽", card_html)
        self.assertIn("Br", card_html)


if __name__ == "__main__":
    unittest.main()


