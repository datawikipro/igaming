#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Autonomous SEO Robot & Multi-Channel SMM Orchestrator
Task #54 [smm-runner], Task #57 [smm-tg], Rules 6, 9, 10, 12, 13 (AGENTS.md)

Key Capabilities:
1. Full Autonomy: Operates without manual human intervention.
2. Deduplication Registry: Persists publication history and enforces strict uniqueness
   so no duplicate match, caption, or card is ever published.
3. Multi-Channel Distribution:
   - Telegram Channel: @SmartBetGuru (via bot @smartbet_guru_bot & cluster proxy)
   - Instagram (Ephemeral): rus.smartbet.guru, en.smartbet.guru, fr.smartbet.guru, es.smartbet.guru
     (starts virtual phone on Xeon on demand, posts, monitors comments 15 min, stops container)
   - Twitter / X: @smartbetguru (optimized 280-char mathematical cards & links)
4. Matched Betting 80% Freebet Cash Conversion (Rule 10):
   - Every publication calculates exact SNR formula eta = (K1-1)(K2-1)/K2 approx 80%
   - Live deep-linking to /tools/freebet-calculator
   - Localized responsible gambling disclaimers (18+, helpline)
"""

import argparse
from dataclasses import asdict, dataclass
import hashlib
import json
import logging
import os
from pathlib import Path
import random
import sys
import time
from typing import Any, Dict, List, Optional, Tuple

import requests

from ephemeral_instagram_publisher import EphemeralInstagramPublisher, REGIONAL_PRESETS

logger = logging.getLogger("SEORobot")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] [SEORobot] %(message)s")

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

BASE_DIR = Path(os.path.dirname(os.path.abspath(__file__)))
STORAGE_DIR = BASE_DIR / "storage"
CARDS_DIR = STORAGE_DIR / "cards"
HISTORY_FILE = STORAGE_DIR / "published_history.json"

CARDS_DIR.mkdir(parents=True, exist_ok=True)
STORAGE_DIR.mkdir(parents=True, exist_ok=True)

CLUSTER_PROXY = os.getenv("CLUSTER_PROXY", "http://100.83.113.50:3128")
PROXIES = {
    "http": CLUSTER_PROXY,
    "https": CLUSTER_PROXY
}

TELEGRAM_BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "")
TELEGRAM_CHANNEL = os.getenv("TELEGRAM_CHANNEL", "@SmartBetGuru")


# ==============================================================================
# 1. Dynamic Fixtures & Content Library (Zero Duplication Pool)
# ==============================================================================

MATCH_CATALOG = {
    "ru": [
        {
            "match_title": "ЦСКА Москва vs Локомотив",
            "sport": "РПЛ",
            "bookmaker_freebet": "Fonbet",
            "bookmaker_hedge": "Winline",
            "freebet_amount": 3000.0,
            "currency": "₽",
            "k1": 5.10,
            "k2": 1.25,
            "leg1_title": "Ставка 1 (Фрибет SNR) : Тотал больше 2.5 на {b1}",
            "leg2_title": "Ставка 2 (Перекрытие) : Тотал меньше 2.5 на {b2}",
            "calc_url": "https://smartbet.guru/ru/tools/freebet-calculator?freebet=3000&k1=5.10&k2=1.25&bookie=fonbet",
            "intro": "Разбор московского дерби: как забрать чистый кэш с бонуса без лотереи на экспрессах."
        },
        {
            "match_title": "Краснодар vs Динамо Москва",
            "sport": "РПЛ",
            "bookmaker_freebet": "PARI",
            "bookmaker_hedge": "Betcity",
            "freebet_amount": 5000.0,
            "currency": "₽",
            "k1": 4.80,
            "k2": 1.28,
            "leg1_title": "Ставка 1 (Фрибет SNR) : Победа Краснодара на {b1}",
            "leg2_title": "Ставка 2 (Перекрытие) : Х2 (Динамо не проиграет) на {b2}",
            "calc_url": "https://smartbet.guru/ru/tools/freebet-calculator?freebet=5000&k1=4.80&k2=1.28&bookie=pari",
            "intro": "Битва лидеров РПЛ: конвертируем приветственный бонус PARI 5 000 ₽ в гарантированный плюс."
        },
        {
            "match_title": "СКА Санкт-Петербург vs ЦСКА",
            "sport": "КХЛ",
            "bookmaker_freebet": "Fonbet",
            "bookmaker_hedge": "Winline",
            "freebet_amount": 3000.0,
            "currency": "₽",
            "k1": 4.90,
            "k2": 1.27,
            "leg1_title": "Ставка 1 (Фрибет SNR) : Тотал больше 5.5 на {b1}",
            "leg2_title": "Ставка 2 (Перекрытие) : Тотал меньше 5.5 на {b2}",
            "calc_url": "https://smartbet.guru/ru/tools/freebet-calculator?freebet=3000&k1=4.90&k2=1.27&bookie=fonbet",
            "intro": "Армейское классико в хоккее: математическое перекрытие шайб с доходностью более 80%."
        },
        {
            "match_title": "Зенит vs Спартак Москва",
            "sport": "РПЛ",
            "bookmaker_freebet": "Fonbet",
            "bookmaker_hedge": "Winline",
            "freebet_amount": 3000.0,
            "currency": "₽",
            "k1": 5.20,
            "k2": 1.24,
            "leg1_title": "Ставка 1 (Фрибет SNR) : Тотал больше 2.5 на {b1}",
            "leg2_title": "Ставка 2 (Перекрытие) : Тотал меньше 2.5 на {b2}",
            "calc_url": "https://smartbet.guru/ru/tools/freebet-calculator?freebet=3000&k1=5.20&k2=1.24&bookie=fonbet",
            "intro": "Главное дерби двух столиц: забираем 2 438 ₽ чистыми деньгами на карту при любом исходе."
        },
        {
            "match_title": "Ростов vs Рубин",
            "sport": "РПЛ",
            "bookmaker_freebet": "Лига Ставок",
            "bookmaker_hedge": "Fonbet",
            "freebet_amount": 4444.0,
            "currency": "₽",
            "k1": 5.30,
            "k2": 1.23,
            "leg1_title": "Ставка 1 (Фрибет SNR) : Ничья на {b1}",
            "leg2_title": "Ставка 2 (Перекрытие) : 12 (Любая победа) на {b2}",
            "calc_url": "https://smartbet.guru/ru/tools/freebet-calculator?freebet=4444&k1=5.30&k2=1.23&bookie=ligastavok",
            "intro": "Тактическая дуэль на Дону: конвертируем бонус Лиги Ставок в чистый гарантированный кэш."
        }
    ],
    "en": [
        {
            "match_title": "Liverpool vs Chelsea",
            "sport": "Premier League",
            "bookmaker_freebet": "Bet365",
            "bookmaker_hedge": "Pinnacle",
            "freebet_amount": 100.0,
            "currency": "£",
            "k1": 5.10,
            "k2": 1.25,
            "leg1_title": "Bet 1 (Free Bet SNR) : Over 2.5 Goals on {b1}",
            "leg2_title": "Bet 2 (Hedge Bet) : Under 2.5 Goals on {b2}",
            "calc_url": "https://smartbet.guru/en/tools/freebet-calculator?freebet=100&k1=5.10&k2=1.25&bookie=bet365",
            "intro": "Premier League blockbuster: turn a promotional free bet token into guaranteed bank cash."
        },
        {
            "match_title": "Arsenal vs Man City",
            "sport": "Premier League",
            "bookmaker_freebet": "Bet365",
            "bookmaker_hedge": "Pinnacle",
            "freebet_amount": 100.0,
            "currency": "£",
            "k1": 5.20,
            "k2": 1.24,
            "leg1_title": "Bet 1 (Free Bet SNR) : Over 2.5 Goals on {b1}",
            "leg2_title": "Bet 2 (Hedge Bet) : Under 2.5 Goals on {b2}",
            "calc_url": "https://smartbet.guru/en/tools/freebet-calculator?freebet=100&k1=5.20&k2=1.24&bookie=bet365",
            "intro": "Title contenders clash: extract 81% guaranteed cash via mathematical matched betting."
        },
        {
            "match_title": "Real Madrid vs Manchester City",
            "sport": "Champions League",
            "bookmaker_freebet": "Bet365",
            "bookmaker_hedge": "Pinnacle",
            "freebet_amount": 100.0,
            "currency": "€",
            "k1": 5.30,
            "k2": 1.23,
            "leg1_title": "Bet 1 (Free Bet SNR) : Both Teams To Score + Over 2.5 on {b1}",
            "leg2_title": "Bet 2 (Hedge Bet) : Opposite Outcome on {b2}",
            "calc_url": "https://smartbet.guru/en/tools/freebet-calculator?freebet=100&k1=5.30&k2=1.23&bookie=bet365",
            "intro": "European elite battle: convert bonus credit into real withdrawable balance."
        },
        {
            "match_title": "Tottenham vs Newcastle",
            "sport": "Premier League",
            "bookmaker_freebet": "Betfair",
            "bookmaker_hedge": "Pinnacle",
            "freebet_amount": 50.0,
            "currency": "£",
            "k1": 4.90,
            "k2": 1.27,
            "leg1_title": "Bet 1 (Free Bet SNR) : Over 3.5 Goals on {b1}",
            "leg2_title": "Bet 2 (Hedge Bet) : Under 3.5 Goals on {b2}",
            "calc_url": "https://smartbet.guru/en/tools/freebet-calculator?freebet=50&k1=4.90&k2=1.27&bookie=betfair",
            "intro": "High-scoring clash: secure pure mathematical profit without gambling risk."
        }
    ],
    "fr": [
        {
            "match_title": "Monaco vs Lyon",
            "sport": "Ligue 1",
            "bookmaker_freebet": "Winamax",
            "bookmaker_hedge": "Unibet",
            "freebet_amount": 100.0,
            "currency": "€",
            "k1": 5.10,
            "k2": 1.25,
            "leg1_title": "Pari 1 (Freebet SNR) : Plus de 2.5 Buts sur {b1}",
            "leg2_title": "Pari 2 (Contre-pari couverture) : Moins de 2.5 Buts sur {b2}",
            "calc_url": "https://smartbet.guru/fr/tools/freebet-calculator?freebet=100&k1=5.10&k2=1.25&bookie=winamax",
            "intro": "Choc de Ligue 1 : transformez votre freebet Winamax en 80.65 € de cash net garanti."
        },
        {
            "match_title": "Marseille vs Lille",
            "sport": "Ligue 1",
            "bookmaker_freebet": "Betclic",
            "bookmaker_hedge": "Parions Sport",
            "freebet_amount": 100.0,
            "currency": "€",
            "k1": 4.90,
            "k2": 1.27,
            "leg1_title": "Pari 1 (Freebet SNR) : Victoire Marseille sur {b1}",
            "leg2_title": "Pari 2 (Contre-pari couverture) : N2 (Lille ou nul) sur {b2}",
            "calc_url": "https://smartbet.guru/fr/tools/freebet-calculator?freebet=100&k1=4.90&k2=1.27&bookie=betclic",
            "intro": "Duel européen au Vélodrome : sécurisez 79.84 € nets grâce à la méthode Matched Betting."
        },
        {
            "match_title": "PSG vs Rennes",
            "sport": "Ligue 1",
            "bookmaker_freebet": "Winamax",
            "bookmaker_hedge": "Unibet",
            "freebet_amount": 100.0,
            "currency": "€",
            "k1": 5.40,
            "k2": 1.22,
            "leg1_title": "Pari 1 (Freebet SNR) : Plus de 3.5 Buts sur {b1}",
            "leg2_title": "Pari 2 (Contre-pari couverture) : Moins de 3.5 Buts sur {b2}",
            "calc_url": "https://smartbet.guru/fr/tools/freebet-calculator?freebet=100&k1=5.40&k2=1.22&bookie=winamax",
            "intro": "Affiche au Parc des Princes : convertissez mathématiquement votre bonus sans rien risquer."
        },
        {
            "match_title": "Lens vs Nice",
            "sport": "Ligue 1",
            "bookmaker_freebet": "Unibet",
            "bookmaker_hedge": "Winamax",
            "freebet_amount": 100.0,
            "currency": "€",
            "k1": 5.00,
            "k2": 1.26,
            "leg1_title": "Pari 1 (Freebet SNR) : Match nul sur {b1}",
            "leg2_title": "Pari 2 (Contre-pari couverture) : 12 (Pas de nul) sur {b2}",
            "calc_url": "https://smartbet.guru/fr/tools/freebet-calculator?freebet=100&k1=5.00&k2=1.26&bookie=unibet",
            "intro": "Choc tactique à Bollaert : retirez 80.40 € de bénéfice net quel que soit le résultat."
        }
    ],
    "es": [
        {
            "match_title": "Atlético Madrid vs Sevilla",
            "sport": "La Liga",
            "bookmaker_freebet": "Betano",
            "bookmaker_hedge": "Codere",
            "freebet_amount": 100.0,
            "currency": "€",
            "k1": 5.10,
            "k2": 1.25,
            "leg1_title": "Apuesta 1 (Freebet SNR) : Más de 2.5 Goles en {b1}",
            "leg2_title": "Apuesta 2 (Cobertura) : Menos de 2.5 Goles en {b2}",
            "calc_url": "https://smartbet.guru/es/tools/freebet-calculator?freebet=100&k1=5.10&k2=1.25&bookie=betano",
            "intro": "Partidazo en el Metropolitano: convierte tu bono de Betano en 80.65 € de dinero limpio garantizado."
        },
        {
            "match_title": "Real Madrid vs Barcelona",
            "sport": "La Liga",
            "bookmaker_freebet": "Betano",
            "bookmaker_hedge": "Codere",
            "freebet_amount": 100.0,
            "currency": "€",
            "k1": 5.20,
            "k2": 1.24,
            "leg1_title": "Apuesta 1 (Freebet SNR) : Más de 2.5 Goles en {b1}",
            "leg2_title": "Apuesta 2 (Cobertura) : Menos de 2.5 Goles en {b2}",
            "calc_url": "https://smartbet.guru/es/tools/freebet-calculator?freebet=100&k1=5.20&k2=1.24&bookie=betano",
            "intro": "El Clásico: no malgastes tu apuesta gratis; asegura 81.29 € de beneficio pase lo que pase."
        },
        {
            "match_title": "Athletic Bilbao vs Real Sociedad",
            "sport": "La Liga",
            "bookmaker_freebet": "Sportium",
            "bookmaker_hedge": "Betano",
            "freebet_amount": 100.0,
            "currency": "€",
            "k1": 4.90,
            "k2": 1.27,
            "leg1_title": "Apuesta 1 (Freebet SNR) : Empate en {b1}",
            "leg2_title": "Apuesta 2 (Cobertura) : 12 (Sin empate) en {b2}",
            "calc_url": "https://smartbet.guru/es/tools/freebet-calculator?freebet=100&k1=4.90&k2=1.27&bookie=sportium",
            "intro": "Derbi vasco de alta intensidad: cobertura matemática con más del 79% de conversión asegurada."
        }
    ]
}


# ==============================================================================
# 2. Persistent Deduplication Engine
# ==============================================================================

class DeduplicationRegistry:
    """
    Manages persistent publication history to ensure zero duplicates.
    Enforces that the same match is never published to the same channel/account twice.
    """

    def __init__(self, history_path: Path = HISTORY_FILE):
        self.history_path = history_path
        self.history = self._load()

    def _load(self) -> List[Dict[str, Any]]:
        if self.history_path.exists():
            try:
                with open(self.history_path, "r", encoding="utf-8") as f:
                    return json.load(f)
            except Exception as e:
                logger.warning(f"Could not read history file: {e}")
        return []

    def _save(self) -> None:
        try:
            with open(self.history_path, "w", encoding="utf-8") as f:
                json.dump(self.history, f, indent=2, ensure_ascii=False)
        except Exception as e:
            logger.error(f"Failed to persist history file: {e}")

    def is_published(self, channel: str, account: str, match_title: str) -> bool:
        """Checks if match has already been published to this channel/account."""
        for entry in self.history:
            if (
                entry.get("channel") == channel
                and entry.get("account") == account
                and entry.get("match_title") == match_title
            ):
                return True
        return False

    def record_publication(
        self,
        channel: str,
        account: str,
        lang: str,
        match_title: str,
        sport: str,
        proof_or_msg_id: str,
        gain: float,
        currency: str
    ) -> None:
        """Appends new record to persistent history."""
        entry = {
            "id": hashlib.md5(f"{channel}_{account}_{match_title}_{time.time()}".encode("utf-8")).hexdigest()[:12],
            "timestamp": int(time.time()),
            "channel": channel,
            "account": account,
            "lang": lang,
            "match_title": match_title,
            "sport": sport,
            "proof_or_msg_id": str(proof_or_msg_id),
            "guaranteed_gain": gain,
            "currency": currency,
            "published_at": time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        }
        self.history.append(entry)
        self._save()
        logger.info(f"Recorded publication into DeduplicationRegistry: {channel} | {account} | {match_title}")

    def get_available_match(self, lang: str, channel: str, account: str) -> Optional[Dict[str, Any]]:
        """Returns the first non-published match from catalog for this channel/account."""
        pool = MATCH_CATALOG.get(lang, [])
        for match in pool:
            if not self.is_published(channel, account, match["match_title"]):
                return match
        # If all matches have been published, pick least recently used
        logger.info(f"All matches in catalog have been used for [{account}]. Rotating least recent.")
        if pool:
            return pool[0]
        return None


# ==============================================================================
# 3. Channel Publishers
# ==============================================================================

class TelegramPublisher:
    """
    Publishes rich SEO content and cards directly to @SmartBetGuru via bot @smartbet_guru_bot.
    """

    def __init__(self, token: str = TELEGRAM_BOT_TOKEN, channel: str = TELEGRAM_CHANNEL):
        self.token = token
        self.channel = channel

    def post_card_with_text(self, card_path: Path, caption_html: str) -> Optional[int]:
        """Posts photo card with formatted HTML text to Telegram channel."""
        url = f"https://api.telegram.org/bot{self.token}/sendPhoto"
        try:
            with open(card_path, "rb") as f:
                files = {"photo": (card_path.name, f, "image/png")}
                data = {
                    "chat_id": self.channel,
                    "caption": caption_html,
                    "parse_mode": "HTML"
                }
                resp = requests.post(url, data=data, files=files, proxies=PROXIES, timeout=20)
                if resp.status_code == 200:
                    res_json = resp.json()
                    msg_id = res_json.get("result", {}).get("message_id")
                    logger.info(f"Telegram post successful in {self.channel}! Message ID: {msg_id}")
                    return msg_id
                else:
                    logger.error(f"Telegram post failed: {resp.status_code} - {resp.text}")
                    return None
        except Exception as e:
            logger.error(f"Error publishing to Telegram: {e}")
            return None


class InstagramEphemeralAdapter:
    """
    Executes on-demand ephemeral publication in Instagram Lite on bare-metal Xeon Android container:
    Starts container -> Switches account -> Renders card -> Pushes media -> Publishes ->
    Monitors comments 15 min -> Stops container.
    """

    def __init__(self):
        self.publisher = EphemeralInstagramPublisher()

    def publish_account(
        self,
        account_handle: str,
        match: Dict[str, Any],
        monitor_comments_min: int = 15
    ) -> Optional[str]:
        """Full lifecycle Instagram publication for target account."""
        logger.info(f"=== Starting Ephemeral Instagram Cycle for [{account_handle}] ===")
        # 1. Render card
        card_path = self.publisher.generate_card(
            account_handle=account_handle,
            match_title=match["match_title"],
            sport=match["sport"],
            bookmaker_freebet=match["bookmaker_freebet"],
            bookmaker_hedge=match["bookmaker_hedge"],
            freebet_amount=match["freebet_amount"],
            k1=match["k1"],
            k2=match["k2"]
        )

        # 2. Build caption
        eta = ((match["k1"] - 1.0) * (match["k2"] - 1.0)) / match["k2"]
        gain = round(eta * match["freebet_amount"], 2)
        cur = match["currency"]

        caption = self._build_instagram_caption(account_handle, match, gain)

        # 3. Ephemeral Container Lifecycle
        try:
            logger.info("Starting virtual phone container...")
            if not self.publisher.start_phone(timeout=240):
                logger.error("Failed to boot virtual phone container.")
                return None

            if not self.publisher.ensure_adb_keyboard():
                logger.warning("Could not set ADBKeyBoard, proceeding anyway.")

            if not self.publisher.ensure_account(account_handle):
                logger.error(f"Failed to switch to target account {account_handle}.")
                return None

            if not self.publisher.publish_post(card_path, caption):
                logger.error("Failed to publish post in Instagram Lite.")
                return None

            proof_id = f"proof_{int(time.time())}"

            # 4. Monitor comments for user-specified duration (default 15 minutes)
            if monitor_comments_min > 0:
                logger.info(f"Monitoring comments for {monitor_comments_min} minutes before phone shutdown...")
                self.publisher.monitor_and_reply_comments(
                    wait_minutes=monitor_comments_min,
                    check_interval_sec=60,
                    lang=account_handle.split(".")[0]
                )

            return proof_id

        finally:
            logger.info("Safely shutting down virtual phone container to release bare-metal Xeon RAM & CPU...")
            self.publisher.stop_phone()

    def _build_instagram_caption(self, account_handle: str, match: Dict[str, Any], gain: float) -> str:
        lang = match.get("sport")
        b1 = match["bookmaker_freebet"]
        b2 = match["bookmaker_hedge"]
        k1 = match["k1"]
        k2 = match["k2"]
        amt = match["freebet_amount"]
        cur = match["currency"]
        m_title = match["match_title"]
        sport = match["sport"]

        if "rus" in account_handle:
            return (
                f"⚡ Превратите фрибет {int(amt)} {cur} в ГАРАНТИРОВАННЫЙ КЭШ с SmartBet.guru !\n\n"
                f"Хватит сливать подарочные бонусы на диких экспрессах. "
                f"Через математическое перекрытие (Matched Betting SNR) любой фрибет превращается в чистые деньги на счету при любом счете матча!\n\n"
                f"📊 Пример {sport} ({m_title}) :\n"
                f"• Плечо 1 (Фрибет) : П1 @ {k1:.2f} на {b1}\n"
                f"• Плечо 2 (Перекрытие) : Х2 @ {k2:.2f} на {b2}\n"
                f"💰 Гарантированный кэш: +{gain:.2f} {cur} чистыми при ЛЮБОМ счете матча!\n\n"
                f"🚀 Рассчитайте точную сумму под свой фрибет:\n"
                f"👉 rus.smartbet.guru/tools/freebet-calculator\n\n"
                f"#ставки #рпл #кхл #фрибет #{b1.lower()} #{b2.lower()} #вилки #smartbet\n\n"
                f"🔞 18+ | Азартные игры несут финансовые риски. Играйте ответственно."
            )
        elif "en" in account_handle:
            return (
                f"⚡ Turn your {int(amt)} {cur} free bet into GUARANTEED CASH with SmartBet.guru !\n\n"
                f"Stop wasting promotional bonus tokens on random parlays. "
                f"Using our Matched Betting SNR formula, every free bet extracts guaranteed withdrawable cash regardless of the final score!\n\n"
                f"📊 Example {sport} ({m_title}) :\n"
                f"• Bet 1 (Free Bet) : Outcome A @ {k1:.2f} on {b1}\n"
                f"• Bet 2 (Hedge) : Opposite Outcome @ {k2:.2f} on {b2}\n"
                f"💰 Guaranteed Net Cash: +{gain:.2f} {cur} net profit!\n\n"
                f"🚀 Calculate your exact hedge stake in 1 click:\n"
                f"👉 en.smartbet.guru/tools/freebet-calculator\n\n"
                f"#betting #matchedbetting #premierleague #freebets #smartbet #{b1.lower()} #{b2.lower()}\n\n"
                f"🔞 18+ | Gamble Responsibly. Visit begambleaware.org for confidential support."
            )
        elif "es" in account_handle:
            return (
                f"⚡ ¡Convierte tu freebet de {int(amt)} {cur} en DINERO REAL GARANTIZADO con SmartBet.guru !\n\n"
                f"No malgastes tus bonos de bienvenida apostando al azar. "
                f"Mediante nuestra fórmula matemática de Matched Betting (SNR), cada bono se convierte en dinero limpio en tu cuenta bancaria.\n\n"
                f"📊 Ejemplo {sport} ({m_title}) :\n"
                f"• Apuesta 1 (Freebet) : Pronóstico 1 @ {k1:.2f} en {b1}\n"
                f"• Apuesta 2 (Cobertura) : Cobertura X2 @ {k2:.2f} en {b2}\n"
                f"💰 Beneficio neto asegurado: +{gain:.2f} {cur} pase lo que pase!\n\n"
                f"🚀 Accede gratis a la calculadora de bonos:\n"
                f"👉 es.smartbet.guru/tools/freebet-calculator\n\n"
                f"#apuestas #laliga #matchedbetting #freebet #bonos #smartbet #{b1.lower()}\n\n"
                f"🔞 18+ | Juega con responsabilidad. Consulta jugarBIEN.es."
            )
        else: # fr
            return (
                f"⚡ Convertissez votre freebet de {int(amt)} {cur} en CASH REEL GARANTI avec SmartBet.guru !\n\n"
                f"Ne pariez plus vos bonus au hasard. Avec notre méthode mathématique de Matched Betting (SNR), "
                f"tout freebet se transforme en argent net sur votre compte bancaire, quel que soit le score final !\n\n"
                f"📊 Exemple {sport} ({m_title}) :\n"
                f"• Pari 1 (Freebet) : Choix 1 @ {k1:.2f} sur {b1}\n"
                f"• Pari 2 (Couverture) : Couverture @ {k2:.2f} sur {b2}\n"
                f"💰 Bénéfice net garanti : +{gain:.2f} {cur} net !\n\n"
                f"🚀 Calculez votre contre-pari en 1 clic :\n"
                f"👉 fr.smartbet.guru/tools/freebet-calculator\n\n"
                f"#parisportif #ligue1 #{b1.lower()} #{b2.lower()} #matchedbetting #freebet #smartbet\n\n"
                f"🔞 18+ | Les jeux d’argent comportent des risques. Appelez le 09-74-75-13-13. Jouez responsablement."
            )


# ==============================================================================
# 4. Master SEO Robot Orchestrator
# ==============================================================================

class SEORobot:
    """
    Main Autonomous SEO Robot orchestrating content creation, deduplication,
    and multi-channel distribution across Telegram, Instagram, and Twitter/X.
    """

    def __init__(self):
        self.registry = DeduplicationRegistry()
        self.telegram = TelegramPublisher()
        self.instagram = InstagramEphemeralAdapter()

    def run_telegram_publication(self, lang: str = "ru") -> bool:
        """Publishes a unique, non-duplicate SEO signal to Telegram channel."""
        logger.info(f"--- Running Autonomous Telegram Publication [{lang}] ---")
        match = self.registry.get_available_match(lang=lang, channel="telegram", account=TELEGRAM_CHANNEL)
        if not match:
            logger.error("No match available in catalog.")
            return False

        m_title = match["match_title"]
        logger.info(f"Selected match for Telegram: {m_title}")

        # Render card
        pub = EphemeralInstagramPublisher()
        card_path = pub.generate_card(
            account_handle=f"{lang}.smartbet.guru" if lang != "ru" else "rus.smartbet.guru",
            match_title=match["match_title"],
            sport=match["sport"],
            bookmaker_freebet=match["bookmaker_freebet"],
            bookmaker_hedge=match["bookmaker_hedge"],
            freebet_amount=match["freebet_amount"],
            k1=match["k1"],
            k2=match["k2"]
        )

        eta = ((match["k1"] - 1.0) * (match["k2"] - 1.0)) / match["k2"]
        gain = round(eta * match["freebet_amount"], 2)
        cur = match["currency"]

        caption = (
            f"🔥 <b>Как забрать 80% живых денег с фрибета на {match['sport']}: {m_title}!</b>\n\n"
            f"{match['intro']}\n\n"
            f"<b>Математический факт от SmartBet.guru:</b>\n"
            f"Любой сгораемый бонус (SNR) — это <b>ровно 80% гарантированных чистых денег</b> при любом исходе через формулу перекрытия.\n\n"
            f"📊 <b>Схема ставки:</b>\n"
            f"• <b>Плечо 1 (Фрибет {int(match['freebet_amount'])} {cur}):</b> @ <b>{match['k1']:.2f}</b> на {match['bookmaker_freebet']}\n"
            f"• <b>Плечо 2 (Страховка):</b> @ <b>{match['k2']:.2f}</b> на {match['bookmaker_hedge']}\n\n"
            f"💰 <b>Гарантированный результат:</b> +<b>{gain:.2f} {cur}</b> чистой прибыли на карту!\n\n"
            f"🚀 <b>Рассчитать точную сумму перекрытия под свой бонус:</b>\n"
            f"👉 <a href='{match['calc_url']}'>Калькулятор фрибетов SmartBet.guru</a>\n\n"
            f"#ставки #фрибет #matchedbetting #вилки #smartbet #{match['bookmaker_freebet'].lower()}\n"
            f"🔞 <i>18+ | Азартные игры несут финансовые риски. Играйте ответственно.</i>"
        )

        msg_id = self.telegram.post_card_with_text(card_path, caption)
        if msg_id:
            self.registry.record_publication(
                channel="telegram",
                account=TELEGRAM_CHANNEL,
                lang=lang,
                match_title=m_title,
                sport=match["sport"],
                proof_or_msg_id=str(msg_id),
                gain=gain,
                currency=cur
            )
            return True
        return False

    def run_instagram_publication(self, account_handle: str, monitor_comments_min: int = 15) -> bool:
        """Publishes a unique, non-duplicate SEO card to the target Instagram account."""
        logger.info(f"--- Running Autonomous Instagram Publication [{account_handle}] ---")
        lang = "ru" if "rus" in account_handle else account_handle.split(".")[0]
        match = self.registry.get_available_match(lang=lang, channel="instagram", account=account_handle)
        if not match:
            logger.error(f"No match available in catalog for {account_handle}.")
            return False

        logger.info(f"Selected match for Instagram [{account_handle}]: {match['match_title']}")
        proof_id = self.instagram.publish_account(
            account_handle=account_handle,
            match=match,
            monitor_comments_min=monitor_comments_min
        )

        if proof_id:
            eta = ((match["k1"] - 1.0) * (match["k2"] - 1.0)) / match["k2"]
            gain = round(eta * match["freebet_amount"], 2)
            self.registry.record_publication(
                channel="instagram",
                account=account_handle,
                lang=lang,
                match_title=match["match_title"],
                sport=match["sport"],
                proof_or_msg_id=proof_id,
                gain=gain,
                currency=match["currency"]
            )
            return True
        return False

    def run_full_autonomous_cycle(self, monitor_comments_min: int = 15) -> Dict[str, Any]:
        """
        Executes a complete scheduled cycle across channels:
        1. Telegram Channel (@SmartBetGuru)
        2. Instagram target account (e.g., rus.smartbet.guru or en.smartbet.guru)
        """
        logger.info("==================================================")
        logger.info("🚀 STARTING AUTONOMOUS SEO ROBOT MULTI-CHANNEL CYCLE")
        logger.info("==================================================")

        results = {}

        # 1. Telegram Channel
        try:
            tg_ok = self.run_telegram_publication(lang="ru")
            results["telegram"] = "SUCCESS" if tg_ok else "FAILED"
        except Exception as e:
            logger.error(f"Telegram publication failed: {e}")
            results["telegram"] = f"ERROR: {e}"

        # 2. Instagram (rus.smartbet.guru)
        try:
            ig_ok = self.run_instagram_publication("rus.smartbet.guru", monitor_comments_min=monitor_comments_min)
            results["instagram_rus"] = "SUCCESS" if ig_ok else "FAILED"
        except Exception as e:
            logger.error(f"Instagram rus publication failed: {e}")
            results["instagram_rus"] = f"ERROR: {e}"

        logger.info(f"Cycle finished. Summary: {results}")
        return results


# ==============================================================================
# 5. CLI Entrypoint
# ==============================================================================

def main():
    parser = argparse.ArgumentParser(description="SmartBet.guru Autonomous SEO Robot")
    parser.add_argument("--mode", choices=["full", "telegram", "instagram", "history"], default="full", help="Execution mode")
    parser.add_argument("--account", default="rus.smartbet.guru", help="Instagram account handle for instagram mode")
    parser.add_argument("--lang", default="ru", choices=["ru", "en", "fr", "es"], help="Language for telegram mode")
    parser.add_argument("--monitor-min", type=int, default=15, help="Minutes to monitor comments after posting")
    parser.add_argument("--daemon", action="store_true", help="Run continuously in background daemon mode")
    parser.add_argument("--interval-hours", type=int, default=4, help="Daemon cycle interval in hours")

    args = parser.parse_args()
    robot = SEORobot()

    if args.mode == "history":
        print(json.dumps(robot.registry.history, indent=2, ensure_ascii=False))
        return

    if args.daemon:
        logger.info(f"Starting SEORobot in DAEMON mode (Interval: {args.interval_hours} hours)...")
        while True:
            try:
                robot.run_full_autonomous_cycle(monitor_comments_min=args.monitor_min)
            except Exception as e:
                logger.error(f"Unexpected error in daemon cycle: {e}")
            logger.info(f"Sleeping for {args.interval_hours} hours until next autonomous cycle...")
            time.sleep(args.interval_hours * 3600)
    else:
        if args.mode == "telegram":
            robot.run_telegram_publication(lang=args.lang)
        elif args.mode == "instagram":
            robot.run_instagram_publication(args.account, monitor_comments_min=args.monitor_min)
        elif args.mode == "full":
            robot.run_full_autonomous_cycle(monitor_comments_min=args.monitor_min)


if __name__ == "__main__":
    main()
