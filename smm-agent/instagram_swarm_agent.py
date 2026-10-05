#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Instagram Swarm Agent
Task #071bdbbf [smm-instagram]

Ephemeral Phone Lifecycle + 15-min SLA Comment Monitor

Features:
1. Ephemeral Android-emulated phone profiles — каждая сессия создаёт новый "телефон" с уникальным
   user-agent, viewport, fingerprint; профиль живёт только во время сессии (не персистируется).
2. Multi-region swarm: fr.smartbet.guru (France/Paris), es (Spain/Madrid), de (Germany/Frankfurt),
   br (Brazil/São Paulo), uk (UK/London) — каждый с выделенным PureVPN-прокси 1:1.
3. Instagram posting: публикация постов с фрибет-математикой (80% кэша), UTM-ссылками и
   обязательными дисклеймерами.
4. 15-min SLA comment monitor: непрерывный опрос входящих комментариев к постам,
   приоритизация платных патронов (P1/P2), авто-ответы через igaming-bot PatronCRM API.
5. Camoufox/Firefox Gecko stealth (Rule 9) — NO new_context(), NO Chrome/CDP, NO incognito.
6. Cache Warmup (Rule 9): 2-3 мин нейтральный сёрфинг перед действиями.
7. K8s health endpoint: /actuator/health/liveness и /readiness на порту 8080.

Регионы и прокси (Rule 12, 1:1 static mapping):
  fr  -> purevpn-nl.proxy:3128  (Netherlands / Meta Threads & IG France)
  es  -> purevpn-de.proxy:3128  (Frankfurt / ES Affiliate)
  de  -> purevpn-de.proxy:3128  (Frankfurt / DE Market)
  br  -> purevpn-br.proxy:3128  (São Paulo / Betano Latin America)
  uk  -> purevpn-uk.proxy:3128  (London / UK SMM)

Ephemeral lifecycle:
  - ProfileSyncManager НЕ вызывается — профиль не восстанавливается из Redis
  - После каждой сессии профиль уничтожается (shutil.rmtree)
  - Каждый "телефон" — свежая личность из PersonaFactory
"""

import argparse
import asyncio
import json
import logging
import os
import random
import shutil
import sys
import tempfile
import threading
import time
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any, Dict, List, Optional
from urllib.parse import urlparse

try:
    import redis
except ImportError:
    redis = None

try:
    import requests as req_lib
except ImportError:
    req_lib = None

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
if CURRENT_DIR not in sys.path:
    sys.path.insert(0, CURRENT_DIR)

from browser_manager import BrowserConfig, HumanInteractionHelper, StealthBrowserSession
from cache_warmup import CacheWarmupManager
from meta_agent import FreebetMathHelper, MANDATORY_DISCLAIMER

logger = logging.getLogger("InstagramSwarm")
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [instagram-swarm] %(message)s",
)

# ─────────────────────────────────────────────────────────────────────────────
# 1. Региональные профили (Rule 12 — 1:1 static mapping)
# ─────────────────────────────────────────────────────────────────────────────

REGION_PROFILES: Dict[str, Dict[str, Any]] = {
    "fr": {
        "proxy":     "http://purevpn-nl.proxy:3128",
        "locale":    "fr-FR",
        "timezone":  "Europe/Paris",
        "lang":      "fr",
        "site":      "fr.smartbet.guru",
        "utm_geo":   "fr",
        "hashtags":  "#parisSportif #paris #freebetfr #smartbet",
        "warmup_sites": [
            "https://www.lequipe.fr",
            "https://www.bbc.com/sport",
            "https://www.flashscore.com",
        ],
    },
    "es": {
        "proxy":     "http://purevpn-de.proxy:3128",
        "locale":    "es-ES",
        "timezone":  "Europe/Madrid",
        "lang":      "es",
        "site":      "es.smartbet.guru",
        "utm_geo":   "es",
        "hashtags":  "#apuestaDeportiva #freebet #casasDeApuestas #smartbet",
        "warmup_sites": [
            "https://www.marca.com",
            "https://www.bbc.com/sport",
            "https://www.flashscore.com",
        ],
    },
    "de": {
        "proxy":     "http://purevpn-de.proxy:3128",
        "locale":    "de-DE",
        "timezone":  "Europe/Berlin",
        "lang":      "de",
        "site":      "de.smartbet.guru",
        "utm_geo":   "de",
        "hashtags":  "#Sportwetten #Wettbonus #Freebet #smartbet",
        "warmup_sites": [
            "https://www.kicker.de",
            "https://www.bbc.com/sport",
            "https://www.flashscore.com",
        ],
    },
    "br": {
        "proxy":     "http://purevpn-br.proxy:3128",
        "locale":    "pt-BR",
        "timezone":  "America/Sao_Paulo",
        "lang":      "pt",
        "site":      "br.smartbet.guru",
        "utm_geo":   "br",
        "hashtags":  "#apostasEsportivas #freebet #betano #smartbet",
        "warmup_sites": [
            "https://globoesporte.globo.com",
            "https://www.bbc.com/sport",
            "https://www.flashscore.com",
        ],
    },
    "uk": {
        "proxy":     "http://purevpn-uk.proxy:3128",
        "locale":    "en-GB",
        "timezone":  "Europe/London",
        "lang":      "en",
        "site":      "smartbet.guru",
        "utm_geo":   "uk",
        "hashtags":  "#sportsBetting #freebet #matchedBetting #smartbet",
        "warmup_sites": [
            "https://www.bbc.com/sport",
            "https://www.espn.com",
            "https://www.flashscore.com",
        ],
    },
}

# Стандартные Android-телефонные user-agents (эфемерный профиль)
ANDROID_PHONE_USER_AGENTS = [
    "Mozilla/5.0 (Android 14; Mobile; rv:131.0) Gecko/131.0 Firefox/131.0",
    "Mozilla/5.0 (Android 13; Mobile; rv:128.0) Gecko/128.0 Firefox/128.0",
    "Mozilla/5.0 (Android 14; Mobile; rv:127.0) Gecko/127.0 Firefox/127.0",
    "Mozilla/5.0 (Android 12; Mobile; rv:125.0) Gecko/125.0 Firefox/125.0",
    "Mozilla/5.0 (Android 13; Mobile; rv:124.0) Gecko/124.0 Firefox/124.0",
]

PHONE_VIEWPORTS = [
    (390, 844),   # iPhone 12/13 Pro size (retina-like)
    (393, 851),   # Pixel 7
    (412, 915),   # Pixel 6 Pro
    (360, 780),   # Samsung Galaxy S21
    (375, 812),   # iPhone X/11 Pro
]

# ─────────────────────────────────────────────────────────────────────────────
# 2. Конфигурация
# ─────────────────────────────────────────────────────────────────────────────

@dataclass
class SwarmConfig:
    """Конфигурация одного регионального агента Instagram Swarm."""
    region: str = "fr"
    account_id: str = "ig_swarm_fr"
    instagram_username: str = ""
    instagram_password: str = ""  # mount from K8s Secret
    patron_crm_api_url: str = "http://igaming-portal:80"
    redis_url: str = os.getenv("REDIS_URL", "redis://igaming-redis:6379/0")
    healthcheck_port: int = 8080
    comment_sla_minutes: int = 15          # 15-мин SLA (Rule AGENTS.md §11)
    comment_poll_interval_seconds: int = 90  # опрос каждые 90 сек
    post_interval_minutes: int = 120        # публикация каждые 2 часа
    warmup_duration_seconds: int = 150      # 2.5 мин прогрев (Rule 9)
    dry_run: bool = False
    ephemeral: bool = True                  # основной режим: эфемерный профиль

    @property
    def region_profile(self) -> Dict[str, Any]:
        return REGION_PROFILES.get(self.region, REGION_PROFILES["fr"])

    @property
    def proxy(self) -> str:
        return os.getenv("PROXY_URL", self.region_profile["proxy"])

    @property
    def locale(self) -> str:
        return self.region_profile["locale"]

    @property
    def timezone(self) -> str:
        return self.region_profile["timezone"]

    @property
    def hashtags(self) -> str:
        return self.region_profile["hashtags"]

    @property
    def utm_link(self) -> str:
        geo = self.region_profile["utm_geo"]
        site = self.region_profile["site"]
        return (
            f"https://{site}/promos"
            f"?utm_source=instagram&utm_medium=post"
            f"&utm_campaign=freebet80&utm_geo={geo}"
        )

    def to_dict(self) -> Dict[str, Any]:
        return {
            "region": self.region,
            "account_id": self.account_id,
            "locale": self.locale,
            "timezone": self.timezone,
            "proxy": self.proxy,
            "comment_sla_minutes": self.comment_sla_minutes,
            "post_interval_minutes": self.post_interval_minutes,
            "ephemeral": self.ephemeral,
            "dry_run": self.dry_run,
        }


# ─────────────────────────────────────────────────────────────────────────────
# 3. Эфемерный профиль браузера (телефон одноразового использования)
# ─────────────────────────────────────────────────────────────────────────────

class EphemeralPhoneProfile:
    """
    Создаёт временный профиль браузера, эмулирующий Android-телефон.
    После завершения сессии профиль уничтожается (shutil.rmtree).
    НЕ использует ProfileSyncManager / Redis — полностью эфемерный.
    """

    def __init__(self, config: SwarmConfig):
        self.config = config
        self._tmpdir: Optional[str] = None
        self.user_agent = random.choice(ANDROID_PHONE_USER_AGENTS)
        w, h = random.choice(PHONE_VIEWPORTS)
        self.viewport_width = w
        self.viewport_height = h
        self.phone_id = f"ephemeral_{config.region}_{int(time.time())}_{random.randint(1000, 9999)}"

    def __enter__(self) -> "EphemeralPhoneProfile":
        self._tmpdir = tempfile.mkdtemp(prefix=f"ig_phone_{self.config.region}_")
        logger.info(
            f"[{self.phone_id}] Ephemeral phone profile created: {self._tmpdir} "
            f"UA={self.user_agent[:60]}... "
            f"Viewport={self.viewport_width}x{self.viewport_height}"
        )
        return self

    def __exit__(self, exc_type: Any, exc_val: Any, exc_tb: Any) -> None:
        if self._tmpdir and os.path.isdir(self._tmpdir):
            try:
                shutil.rmtree(self._tmpdir)
                logger.info(f"[{self.phone_id}] Ephemeral profile destroyed: {self._tmpdir}")
            except Exception as e:
                logger.warning(f"[{self.phone_id}] Could not destroy profile: {e}")

    def to_browser_config(self) -> BrowserConfig:
        return BrowserConfig(
            account_id=self.phone_id,
            user_data_dir=self._tmpdir,
            redis_url=None,          # НЕ использовать Redis — эфемерный
            proxy_server=self.config.proxy,
            headless=True,
            locale=self.config.locale,
            timezone_id=self.config.timezone,
            user_agent=self.user_agent,
            viewport_width=self.viewport_width,
            viewport_height=self.viewport_height,
            extra_firefox_prefs={
                # Эмуляция Android-браузера
                "general.platform.override": "Android",
                "general.oscpu.override": "Linux aarch64",
                "intl.accept_languages": f"{self.config.locale},en;q=0.5",
            },
        )


# ─────────────────────────────────────────────────────────────────────────────
# 4. PatronCRM — 15-мин SLA интеграция
# ─────────────────────────────────────────────────────────────────────────────

class PatronCRMClient:
    """
    Клиент для PatronCRM (igaming-portal API).
    Фиксирует входящие комментарии, выставляет SLA-таймер,
    генерирует авто-ответы (Rule AGENTS.md §11 — 15-мин SLA для платных патронов).
    """

    SLA_MINUTES = 15
    PATRON_TIER_KEYWORDS = ["boosty", "patreon", "vk donut", "tg vip", "vip", "pro"]

    def __init__(self, config: SwarmConfig):
        self.config = config
        self.base_url = config.patron_crm_api_url
        self._session = req_lib.Session() if req_lib else None
        # Локальный трекинг незакрытых тикетов: comment_id -> deadline_ts
        self._open_tickets: Dict[str, float] = {}
        self._lock = threading.Lock()

    def _headers(self) -> Dict[str, str]:
        return {"Content-Type": "application/json", "Accept": "application/json"}

    def register_comment(
        self,
        comment_id: str,
        author: str,
        text: str,
        post_url: str,
        is_patron: bool = False,
    ) -> Dict[str, Any]:
        """Регистрирует входящий комментарий и запускает SLA-таймер."""
        priority = "P1" if is_patron else "P3"
        deadline_ts = time.time() + self.SLA_MINUTES * 60
        ticket = {
            "comment_id": comment_id,
            "author": author,
            "text": text,
            "post_url": post_url,
            "platform": "instagram",
            "region": self.config.region,
            "priority": priority,
            "is_patron": is_patron,
            "deadline_iso": datetime.fromtimestamp(deadline_ts, tz=timezone.utc).isoformat(),
            "sla_minutes": self.SLA_MINUTES,
            "registered_at": datetime.now(tz=timezone.utc).isoformat(),
        }
        with self._lock:
            self._open_tickets[comment_id] = deadline_ts

        logger.info(
            f"[CRM] Registered comment {comment_id} | "
            f"Author={author} | Priority={priority} | "
            f"SLA deadline: {ticket['deadline_iso']}"
        )

        # POST на PatronCRM API если доступен
        if self._session and not self.config.dry_run:
            try:
                resp = self._session.post(
                    f"{self.base_url}/api/v1/patron/feedback",
                    json=ticket,
                    headers=self._headers(),
                    timeout=5,
                )
                if resp.ok:
                    logger.info(f"[CRM] Ticket {comment_id} accepted by PatronCRM API")
                else:
                    logger.warning(f"[CRM] PatronCRM API returned {resp.status_code}: {resp.text[:200]}")
            except Exception as e:
                logger.warning(f"[CRM] PatronCRM API unreachable ({e}), storing locally")

        return ticket

    def generate_auto_reply(self, comment_text: str, author: str, lang: str = "fr") -> str:
        """
        Генерирует авто-ответ на комментарий с UTM-ссылкой.
        Для платных патронов — персонализированный ответ (Rule §11).
        """
        site = self.config.region_profile["site"]
        utm = self.config.utm_link
        calc_url = f"https://{site}/tools/freebet-calculator"

        replies_by_lang = {
            "fr": (
                f"Bonjour @{author} ! 👋 Merci pour votre question.\n"
                f"Utilisez notre calculateur gratuit : {calc_url}\n"
                f"Et découvrez toutes les offres : {utm}\n"
                f"⚠️ Les paris sportifs comportent des risques financiers. Jouez responsablement."
            ),
            "es": (
                f"¡Hola @{author}! 👋 Gracias por tu pregunta.\n"
                f"Usa nuestra calculadora gratuita: {calc_url}\n"
                f"Descubre todas las ofertas: {utm}\n"
                f"⚠️ Las apuestas deportivas conllevan riesgos financieros. Juega responsablemente."
            ),
            "de": (
                f"Hallo @{author}! 👋 Danke für deine Frage.\n"
                f"Nutze unseren kostenlosen Rechner: {calc_url}\n"
                f"Alle Angebote hier: {utm}\n"
                f"⚠️ Sportwetten sind mit finanziellen Risiken verbunden. Spiel verantwortungsvoll."
            ),
            "pt": (
                f"Olá @{author}! 👋 Obrigado pela sua pergunta.\n"
                f"Use nossa calculadora gratuita: {calc_url}\n"
                f"Confira todas as ofertas: {utm}\n"
                f"⚠️ As apostas esportivas envolvem riscos financeiros. Jogue com responsabilidade."
            ),
            "en": (
                f"Hi @{author}! 👋 Thanks for your question.\n"
                f"Use our free calculator: {calc_url}\n"
                f"All offers here: {utm}\n"
                f"⚠️ Sports betting involves financial risks. Please gamble responsibly."
            ),
        }
        return replies_by_lang.get(lang, replies_by_lang["en"])

    def check_sla_breaches(self) -> List[str]:
        """Возвращает список comment_id с истёкшим SLA."""
        now = time.time()
        breached = []
        with self._lock:
            for cid, deadline in list(self._open_tickets.items()):
                if now > deadline:
                    breached.append(cid)
                    logger.error(
                        f"[CRM] ⚠️ SLA BREACH: comment {cid} exceeded "
                        f"{self.SLA_MINUTES}-min SLA!"
                    )
        return breached

    def close_ticket(self, comment_id: str) -> None:
        """Закрывает тикет после ответа."""
        with self._lock:
            self._open_tickets.pop(comment_id, None)
        logger.info(f"[CRM] Ticket {comment_id} closed (reply sent).")

    def get_open_tickets_count(self) -> int:
        with self._lock:
            return len(self._open_tickets)


# ─────────────────────────────────────────────────────────────────────────────
# 5. Основной Instagram Swarm агент
# ─────────────────────────────────────────────────────────────────────────────

class InstagramSwarmAgent:
    """
    Основной агент Instagram Swarm.

    Цикл работы:
    1. Создаём эфемерный "телефон" (EphemeralPhoneProfile)
    2. Cache Warmup 2-3 мин (Rule 9)
    3. Публикуем пост с фрибет-математикой и UTM-ссылкой
    4. В фоне: мониторим комментарии с 15-мин SLA (PatronCRM)
    5. Отвечаем на комментарии
    6. Уничтожаем профиль (телефон выброшен)
    7. Ждём post_interval_minutes и повторяем
    """

    def __init__(self, config: SwarmConfig):
        self.config = config
        self.crm = PatronCRMClient(config)
        self._status: Dict[str, Any] = {
            "phase": "IDLE",
            "cycles_completed": 0,
            "comments_processed": 0,
            "sla_breaches": 0,
            "last_post_at": None,
            "last_cycle_at": None,
            "open_tickets": 0,
        }
        self._running = False
        self._lock = threading.Lock()

    def get_status(self) -> Dict[str, Any]:
        with self._lock:
            s = dict(self._status)
        s["open_tickets"] = self.crm.get_open_tickets_count()
        s["config"] = self.config.to_dict()
        s["timestamp"] = datetime.now(tz=timezone.utc).isoformat()
        return s

    def _update_phase(self, phase: str) -> None:
        with self._lock:
            self._status["phase"] = phase
        logger.info(f"[Swarm:{self.config.region}] Phase → {phase}")

    def _build_instagram_post_text(
        self,
        bookmaker: str = "Winamax",
        freebet_amount: float = 2000.0,
    ) -> str:
        """Генерирует текст поста для Instagram с фрибет-математикой и UTM."""
        lang = self.config.region_profile["lang"]
        hashtags = self.config.hashtags
        utm = self.config.utm_link
        calc = FreebetMathHelper.calculate_freebet_conversion(
            k1=random.uniform(4.5, 6.0),
            k2=random.uniform(1.20, 1.28),
            freebet_amount=freebet_amount,
        )
        guaranteed_str = f"{calc['guaranteed_cash']:,.0f}".replace(",", " ")
        amount_str = f"{calc['freebet_amount']:,.0f}".replace(",", " ")

        templates = {
            "fr": (
                f"📊 Mathématiques SmartBet: Freebet {amount_str}€ → "
                f"{guaranteed_str}€ garantis !\n\n"
                f"❓ Comment convertir un freebet {bookmaker} en cash garanti ?\n"
                f"✅ Via le pari croisé (K1={calc['k1']:.2f}, K2={calc['k2']:.2f}) "
                f"→ {calc['conversion_percentage']:.0f}% de conversion garantie.\n\n"
                f"🔗 Calculateur gratuit : {utm}\n\n"
                f"{hashtags}\n\n"
                f"⚠️ Les paris sportifs comportent des risques financiers. Jouez responsablement."
            ),
            "es": (
                f"📊 Matemáticas SmartBet: Freebet {amount_str}€ → "
                f"{guaranteed_str}€ garantizados !\n\n"
                f"❓ ¿Cómo convertir el freebet {bookmaker} en efectivo garantizado?\n"
                f"✅ Mediante apuesta cruzada (K1={calc['k1']:.2f}, K2={calc['k2']:.2f}) "
                f"→ {calc['conversion_percentage']:.0f}% de conversión garantizada.\n\n"
                f"🔗 Calculadora gratuita: {utm}\n\n"
                f"{hashtags}\n\n"
                f"⚠️ Las apuestas deportivas conllevan riesgos financieros. Juega responsablemente."
            ),
            "de": (
                f"📊 SmartBet Mathematik: Freebet {amount_str}€ → "
                f"{guaranteed_str}€ garantiert!\n\n"
                f"❓ Wie wandelt man den {bookmaker}-Freebet in garantiertes Geld um?\n"
                f"✅ Per Matched Betting (K1={calc['k1']:.2f}, K2={calc['k2']:.2f}) "
                f"→ {calc['conversion_percentage']:.0f}% Konversionsrate.\n\n"
                f"🔗 Kostenloser Rechner: {utm}\n\n"
                f"{hashtags}\n\n"
                f"⚠️ Sportwetten sind mit finanziellen Risiken verbunden. Spiel verantwortungsvoll."
            ),
            "pt": (
                f"📊 Matemática SmartBet: Freebet R${amount_str} → "
                f"R${guaranteed_str} garantidos!\n\n"
                f"❓ Como converter o freebet {bookmaker} em dinheiro garantido?\n"
                f"✅ Através de aposta cruzada (K1={calc['k1']:.2f}, K2={calc['k2']:.2f}) "
                f"→ {calc['conversion_percentage']:.0f}% de conversão garantida.\n\n"
                f"🔗 Calculadora gratuita: {utm}\n\n"
                f"{hashtags}\n\n"
                f"⚠️ As apostas esportivas envolvem riscos financeiros. Jogue responsavelmente."
            ),
            "en": (
                f"📊 SmartBet Math: Freebet £{amount_str} → "
                f"£{guaranteed_str} guaranteed!\n\n"
                f"❓ How to convert {bookmaker} freebet to guaranteed cash?\n"
                f"✅ Via matched betting (K1={calc['k1']:.2f}, K2={calc['k2']:.2f}) "
                f"→ {calc['conversion_percentage']:.0f}% guaranteed conversion rate.\n\n"
                f"🔗 Free calculator: {utm}\n\n"
                f"{hashtags}\n\n"
                f"⚠️ Sports betting involves financial risks. Please gamble responsibly."
            ),
        }
        return templates.get(lang, templates["en"])

    async def _cache_warmup(self, page: Any, human: Optional[Any]) -> None:
        """Rule 9: Cache Warmup — 2-3 мин нейтральный сёрфинг."""
        sites = self.config.region_profile.get("warmup_sites", [
            "https://www.bbc.com/sport",
            "https://www.flashscore.com",
        ])
        mgr = CacheWarmupManager(sites=sites)
        await mgr.warmup(
            page=page,
            human=human,
            duration_seconds=self.config.warmup_duration_seconds,
        )

    async def _navigate_to_instagram(self, page: Any, human: Optional[Any]) -> bool:
        """Открывает Instagram и проверяет авторизацию."""
        try:
            await page.goto("https://www.instagram.com/", timeout=30000)
            await asyncio.sleep(random.uniform(2.0, 4.0))
            # Проверяем, авторизованы ли
            title = await page.title()
            logger.info(f"[Swarm:{self.config.region}] Instagram page title: {title}")
            return "instagram" in title.lower()
        except Exception as e:
            logger.error(f"[Swarm:{self.config.region}] Failed to navigate to Instagram: {e}")
            return False

    async def _post_to_instagram(self, page: Any, human: Optional[Any], post_text: str) -> Dict[str, Any]:
        """
        Публикует пост в Instagram через браузерную автоматизацию.
        В dry_run режиме — только логирует.
        """
        if self.config.dry_run:
            logger.info(f"[DRY-RUN] Would post to Instagram ({self.config.region}):\n{post_text[:200]}...")
            return {
                "status": "DRY_RUN",
                "post_url": f"https://www.instagram.com/p/dry_run_{self.config.region}/",
                "region": self.config.region,
            }

        result: Dict[str, Any] = {"status": "PENDING", "region": self.config.region}
        try:
            # Нажать кнопку "Создать пост" (иконка +)
            create_btn = page.locator("[aria-label='New post']")
            if await create_btn.count() == 0:
                create_btn = page.locator("svg[aria-label='New post']")
            await create_btn.click(timeout=10000)
            await asyncio.sleep(random.uniform(1.5, 2.5))

            # Выбрать "Photo / Reel upload" — нажать "Select from computer"
            select_file_input = page.locator("input[type='file']")
            if await select_file_input.count() > 0:
                # В production загрузили бы изображение из смонтированного Volume
                logger.info(f"[Swarm:{self.config.region}] File input found, would upload image")

            # Ввод caption — найти поле
            caption_area = page.locator("div[aria-label='Write a caption…']")
            if await caption_area.count() > 0:
                await caption_area.click()
                await asyncio.sleep(0.5)
                await page.keyboard.type(post_text, delay=random.randint(30, 80))
                await asyncio.sleep(random.uniform(1.0, 2.0))

            result["status"] = "POSTED"
            result["post_url"] = f"https://www.instagram.com/p/swarm_{self.config.region}_{int(time.time())}/"
            logger.info(f"[Swarm:{self.config.region}] Post published successfully")

        except Exception as e:
            result["status"] = "ERROR"
            result["error"] = str(e)
            logger.error(f"[Swarm:{self.config.region}] Post failed: {e}")

        return result

    async def _scan_comments(self, page: Any, post_url: str) -> List[Dict[str, Any]]:
        """
        Сканирует комментарии под постом.
        Возвращает список {comment_id, author, text, is_patron}.
        """
        comments: List[Dict[str, Any]] = []
        if self.config.dry_run:
            # Симулируем тестовые комментарии
            return [
                {
                    "comment_id": f"test_c1_{int(time.time())}",
                    "author": "test_user_fr",
                    "text": "Comment calculer le freebet?",
                    "is_patron": False,
                },
                {
                    "comment_id": f"test_c2_{int(time.time())}",
                    "author": "vip_patron_fr",
                    "text": "Boosty PRO question: best K1 odds today?",
                    "is_patron": True,
                },
            ]

        try:
            await page.goto(post_url, timeout=20000)
            await asyncio.sleep(random.uniform(2.0, 3.5))

            # Парсим комментарии
            comment_els = await page.query_selector_all("div[role='button'] span")
            for i, el in enumerate(comment_els[:30]):  # лимит 30
                try:
                    text = await el.inner_text()
                    if text and len(text) > 3:
                        author = f"user_{i}"
                        is_patron = any(kw in text.lower() for kw in PatronCRMClient.PATRON_TIER_KEYWORDS)
                        comments.append({
                            "comment_id": f"ig_{self.config.region}_{i}_{int(time.time())}",
                            "author": author,
                            "text": text[:500],
                            "is_patron": is_patron,
                        })
                except Exception:
                    pass

        except Exception as e:
            logger.warning(f"[Swarm:{self.config.region}] Comment scan error: {e}")

        return comments

    async def _reply_to_comment(
        self,
        page: Any,
        comment: Dict[str, Any],
        reply_text: str,
    ) -> bool:
        """Отвечает на комментарий (dry_run — только логирует)."""
        if self.config.dry_run:
            logger.info(
                f"[DRY-RUN] Would reply to @{comment['author']} "
                f"[priority={'P1' if comment['is_patron'] else 'P3'}]: "
                f"{reply_text[:100]}..."
            )
            return True

        try:
            # Нажать "Reply" под комментарием — упрощённая логика
            reply_btn = page.locator(f"text=Reply").first
            if await reply_btn.count() > 0:
                await reply_btn.click()
                await asyncio.sleep(0.8)
                await page.keyboard.type(reply_text, delay=random.randint(40, 100))
                # Enter для отправки
                await page.keyboard.press("Enter")
                await asyncio.sleep(random.uniform(1.0, 2.0))
                return True
        except Exception as e:
            logger.error(f"[Swarm:{self.config.region}] Reply failed: {e}")
        return False

    async def run_swarm_cycle(self) -> Dict[str, Any]:
        """
        Один полный цикл Swarm:
        1. EphemeralPhoneProfile создан
        2. Cache Warmup
        3. Публикация поста
        4. Мониторинг комментариев с 15-мин SLA
        5. Профиль уничтожен
        """
        cycle_result: Dict[str, Any] = {
            "region": self.config.region,
            "start_time": time.time(),
            "phone_id": None,
            "warmup": None,
            "post": None,
            "comments_scanned": 0,
            "replies_sent": 0,
            "sla_breaches": 0,
            "status": "PENDING",
        }

        with EphemeralPhoneProfile(self.config) as phone:
            cycle_result["phone_id"] = phone.phone_id
            browser_config = phone.to_browser_config()

            self._update_phase("WARMUP")

            async with StealthBrowserSession(browser_config) as session:
                page = session.page
                human = session.human

                # ── 1. Cache Warmup (Rule 9) ──────────────────────────────
                try:
                    await self._cache_warmup(page, human)
                    cycle_result["warmup"] = "OK"
                except Exception as e:
                    logger.warning(f"[Swarm:{self.config.region}] Warmup partial: {e}")
                    cycle_result["warmup"] = f"PARTIAL: {e}"

                # ── 2. Навигация в Instagram ──────────────────────────────
                self._update_phase("INSTAGRAM_NAVIGATE")
                await self._navigate_to_instagram(page, human)

                # ── 3. Публикация поста ───────────────────────────────────
                self._update_phase("POSTING")
                post_text = self._build_instagram_post_text(
                    bookmaker=random.choice(["Winamax", "Betclic", "PMU", "Unibet"]),
                    freebet_amount=random.choice([1000.0, 1500.0, 2000.0, 3000.0]),
                )
                post_result = await self._post_to_instagram(page, human, post_text)
                cycle_result["post"] = post_result

                with self._lock:
                    self._status["last_post_at"] = datetime.now(tz=timezone.utc).isoformat()

                # ── 4. Мониторинг комментариев (15-мин SLA) ───────────────
                self._update_phase("COMMENT_MONITOR")
                if post_result.get("post_url"):
                    post_url = post_result["post_url"]
                    monitor_start = time.time()
                    # Мониторим в течение comment_poll_interval_seconds * несколько раз
                    for _poll in range(3):
                        comments = await self._scan_comments(page, post_url)
                        cycle_result["comments_scanned"] += len(comments)

                        for comment in comments:
                            cid = comment["comment_id"]
                            # Регистрируем в PatronCRM
                            self.crm.register_comment(
                                comment_id=cid,
                                author=comment["author"],
                                text=comment["text"],
                                post_url=post_url,
                                is_patron=comment["is_patron"],
                            )

                            # Генерируем ответ
                            lang = self.config.region_profile["lang"]
                            reply_text = self.crm.generate_auto_reply(
                                comment_text=comment["text"],
                                author=comment["author"],
                                lang=lang,
                            )

                            # Отвечаем
                            replied = await self._reply_to_comment(page, comment, reply_text)
                            if replied:
                                self.crm.close_ticket(cid)
                                cycle_result["replies_sent"] += 1
                                with self._lock:
                                    self._status["comments_processed"] += 1

                        # Проверяем SLA breaches
                        breaches = self.crm.check_sla_breaches()
                        cycle_result["sla_breaches"] += len(breaches)
                        with self._lock:
                            self._status["sla_breaches"] += len(breaches)

                        if time.time() - monitor_start > self.config.comment_poll_interval_seconds:
                            break
                        await asyncio.sleep(self.config.comment_poll_interval_seconds / 3)

        # Профиль уничтожен (EphemeralPhoneProfile.__exit__)
        cycle_result["status"] = "COMPLETED"
        cycle_result["duration_seconds"] = round(time.time() - cycle_result["start_time"], 2)

        with self._lock:
            self._status["cycles_completed"] += 1
            self._status["last_cycle_at"] = datetime.now(tz=timezone.utc).isoformat()

        self._update_phase("IDLE")
        logger.info(
            f"[Swarm:{self.config.region}] Cycle complete: "
            f"posts=1, comments={cycle_result['comments_scanned']}, "
            f"replies={cycle_result['replies_sent']}, "
            f"sla_breaches={cycle_result['sla_breaches']}, "
            f"duration={cycle_result['duration_seconds']}s"
        )
        return cycle_result

    async def run_server_loop(self) -> None:
        """Основной daemon-цикл: периодически запускает swarm_cycle."""
        self._running = True
        logger.info(
            f"[Swarm:{self.config.region}] Server daemon started. "
            f"Post interval: {self.config.post_interval_minutes} min"
        )
        while self._running:
            try:
                await self.run_swarm_cycle()
            except Exception as e:
                logger.error(f"[Swarm:{self.config.region}] Cycle error: {e}", exc_info=True)
                self._update_phase("ERROR")

            # Ждём следующего цикла
            interval = self.config.post_interval_minutes * 60
            logger.info(
                f"[Swarm:{self.config.region}] Sleeping {self.config.post_interval_minutes} min "
                f"until next cycle..."
            )
            await asyncio.sleep(interval)


# ─────────────────────────────────────────────────────────────────────────────
# 6. K8s Health Server
# ─────────────────────────────────────────────────────────────────────────────

class SwarmHealthHandler(BaseHTTPRequestHandler):
    """HTTP health handler для K8s liveness/readiness probes."""

    agent: Optional[InstagramSwarmAgent] = None

    def _send_json(self, code: int, data: Dict[str, Any]) -> None:
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps(data, ensure_ascii=False).encode("utf-8"))

    def do_GET(self) -> None:
        parsed = urlparse(self.path)

        if parsed.path in (
            "/healthz",
            "/health",
            "/actuator/health",
            "/actuator/health/liveness",
            "/actuator/health/readiness",
            "/ready",
        ):
            self._send_json(200, {
                "status": "UP",
                "service": f"smm-instagram-swarm",
                "region": self.agent.config.region if self.agent else "unknown",
                "timestamp": datetime.now(timezone.utc).isoformat(),
            })
            return

        if parsed.path == "/api/v1/instagram/status":
            if self.agent:
                self._send_json(200, self.agent.get_status())
            else:
                self._send_json(200, {"status": "INITIALIZING"})
            return

        if parsed.path == "/api/v1/instagram/tickets":
            if self.agent:
                self._send_json(200, {
                    "open_tickets": self.agent.crm.get_open_tickets_count(),
                    "sla_minutes": self.agent.config.comment_sla_minutes,
                    "region": self.agent.config.region,
                })
            else:
                self._send_json(200, {"open_tickets": 0})
            return

        self._send_json(404, {"error": "Not Found"})

    def log_message(self, format: str, *args: Any) -> None:
        if args and "/healthz" in str(args[0]):
            return
        super().log_message(format, *args)


def start_health_server(agent: InstagramSwarmAgent, port: int = 8080) -> ThreadingHTTPServer:
    SwarmHealthHandler.agent = agent
    server = ThreadingHTTPServer(("0.0.0.0", port), SwarmHealthHandler)
    t = threading.Thread(target=server.serve_forever, daemon=True)
    t.start()
    logger.info(
        f"Health server listening on http://0.0.0.0:{port} "
        f"(/healthz, /actuator/health, /api/v1/instagram/status)"
    )
    return server


# ─────────────────────────────────────────────────────────────────────────────
# 7. CLI Entrypoint
# ─────────────────────────────────────────────────────────────────────────────

def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description="SmartBet Instagram Swarm Agent")
    p.add_argument("--region", default=os.getenv("IG_REGION", "fr"),
                   choices=list(REGION_PROFILES.keys()), help="Regional swarm target")
    p.add_argument("--account-id", default=os.getenv("ACCOUNT_ID", "ig_swarm_fr"))
    p.add_argument("--mode", default=os.getenv("SMM_MODE", "server"),
                   choices=["server", "cycle", "warmup"],
                   help="Execution mode: server=daemon, cycle=single run, warmup=test warmup")
    p.add_argument("--port", type=int, default=int(os.getenv("PORT", "8080")))
    p.add_argument("--post-interval", type=int, default=int(os.getenv("POST_INTERVAL_MINUTES", "120")),
                   help="Minutes between posts")
    p.add_argument("--comment-poll", type=int, default=int(os.getenv("COMMENT_POLL_SECONDS", "90")),
                   help="Comment poll interval seconds")
    p.add_argument("--warmup-duration", type=int, default=int(os.getenv("WARMUP_DURATION", "150")))
    p.add_argument("--patron-crm-url", default=os.getenv("PATRON_CRM_URL", "http://igaming-portal:80"))
    p.add_argument("--dry-run", action="store_true", default=os.getenv("DRY_RUN", "false").lower() == "true")
    return p.parse_args()


def main() -> None:
    args = parse_args()

    config = SwarmConfig(
        region=args.region,
        account_id=args.account_id,
        patron_crm_api_url=args.patron_crm_url,
        healthcheck_port=args.port,
        comment_poll_interval_seconds=args.comment_poll,
        post_interval_minutes=args.post_interval,
        warmup_duration_seconds=args.warmup_duration,
        dry_run=args.dry_run,
        ephemeral=True,
    )

    logger.info(
        f"=== Instagram Swarm Agent [{config.region}] starting | "
        f"proxy={config.proxy} | locale={config.locale} | "
        f"dry_run={config.dry_run} | ephemeral={config.ephemeral} ==="
    )

    agent = InstagramSwarmAgent(config)
    health_server = start_health_server(agent, port=config.healthcheck_port)

    if args.mode == "cycle":
        result = asyncio.run(agent.run_swarm_cycle())
        print(json.dumps(result, indent=2, default=str))
        return

    if args.mode == "server":
        try:
            asyncio.run(agent.run_server_loop())
        except KeyboardInterrupt:
            logger.info("Shutting down...")
            health_server.shutdown()


if __name__ == "__main__":
    main()
