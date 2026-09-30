#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Patron Content Distributor
Task [patron-content] (Plane ID #487c4156) & Rules 6, 9, 10 (AGENTS.md)

Multi-platform content distribution pipeline for paid subscribers across:
1. Telegram VIP Channel (@SmartBetVipBot) with protect_content=True
2. VK Donut Wall Post with donut_paid_duration=-1
3. Boosty Blog Exclusive Posts with min_tier_rub access
4. Patreon API v2 Posts for USD Tiers ($25/mo, $100/mo)

Supported content templates:
- surebet_corridor: High-yield surebets and middles (10%-25%) with limits & bookmaker rules.
- freebet_80_cash: Bonus matched betting SNR conversion (80% guaranteed cash rule).
- antifraud_guide: Stealth Gecko/Camoufox persistent profiles, warmup & proxy hygiene.
"""

import argparse
import json
import logging
import os
import sys
import time
from typing import Any, Dict, List, Optional
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

try:
    import requests
except ImportError:
    requests = None

logger = logging.getLogger("PatronContentDistributor")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")

RU_DISCLAIMER = (
    "\n\n⚠️ Ставки на спорт сопряжены с финансовыми рисками. "
    "Мы против лудомании и необдуманного беттинга. Играйте ответственно."
)

EN_DISCLAIMER = (
    "\n\n⚠️ Sports betting involves financial risks. "
    "We advocate responsible betting and strictly oppose gambling addiction. Bet responsibly."
)


def calculate_freebet_cash(nominal: float = 3000.0, k1: float = 5.0, k2: float = 1.25) -> Dict[str, Any]:
    """
    Calculates SNR freebet guaranteed cash conversion per Rule 10:
    eta = ((k1 - 1) * (k2 - 1)) / k2
    guaranteed_cash = nominal * eta
    """
    eta = ((k1 - 1.0) * (k2 - 1.0)) / k2
    hedge_stake = (nominal * (k1 - 1.0)) / k2
    guaranteed_cash = round(nominal * eta, 2)
    return {
        "nominal": nominal,
        "k1": k1,
        "k2": k2,
        "conversion_ratio": round(eta, 4),
        "hedge_stake": round(hedge_stake, 2),
        "guaranteed_cash": guaranteed_cash,
    }


class ContentTemplateEngine:
    """Generates structured exclusive content for paid patrons in RU and EN."""

    @staticmethod
    def render_surebet_corridor(context: Optional[Dict[str, Any]] = None, lang: str = "ru") -> Dict[str, str]:
        ctx = context or {}
        match = ctx.get("match", "ЦСКА Москва — Зенит Санкт-Петербург")
        sport = ctx.get("sport", "Баскетбол (Единая лига ВТБ)")
        profit_pct = ctx.get("profit_pct", 18.5)
        bk1 = ctx.get("bk1", "Винлайн")
        bk2 = ctx.get("bk2", "Pinnacle")
        market1 = ctx.get("market1", "Фора 1 (-2.5)")
        odds1 = ctx.get("odds1", 2.25)
        market2 = ctx.get("market2", "Фора 2 (+5.5)")
        odds2 = ctx.get("odds2", 2.15)
        corridor_window = ctx.get("corridor_window", "3-5 очков")

        if lang == "en":
            title = f"🎯 VIP Arbitrage & Middle Corridor (+{profit_pct:.1f}%): {match}"
            content = (
                f"🎯 **EXCLUSIVE PATRON CORRIDOR SIGNAL** (+{profit_pct:.1f}% Net Yield)\n\n"
                f"🏆 **Event:** {match} ({sport})\n"
                f"📊 **Calculated Middle Yield:** +{profit_pct:.1f}%\n"
                f"🎯 **Target Corridor Window:** {corridor_window}\n\n"
                f"📋 **Legs & Allocations:**\n"
                f"  • {bk1}: {market1} @ {odds1:.2f}\n"
                f"  • {bk2}: {market2} @ {odds2:.2f}\n\n"
                f"💡 **Limits & Execution Rules:**\n"
                f"  - Recommended max stake per bookmaker: $500–$1,000.\n"
                f"  - Check line settlement rules for overtime prior to placement.\n"
                f"  - Place bet on softer recreational bookmaker ({bk1}) first before sharp ({bk2}).\n"
                f"{EN_DISCLAIMER}"
            )
            teaser = f"VIP Corridor (+{profit_pct:.1f}%): {match}. Exclusive for Pro Arbitrageurs & Syndicate."
        else:
            title = f"🎯 Жирная связка: {profit_pct:.1f}% коридор {match}"
            content = (
                f"🎯 **ЭКСКЛЮЗИВНЫЙ РАЗБОР КОРИДОРА ДЛЯ ПАТРОНОВ** (+{profit_pct:.1f}% доходность)\n\n"
                f"🏆 **Матч:** {match}\n"
                f"🏅 **Дисциплина:** {sport}\n"
                f"📈 **Расчетный коридор (middles):** {corridor_window}\n\n"
                f"📋 **Плечи связки:**\n"
                f"  • {bk1}: {market1} @ {odds1:.2f}\n"
                f"  • {bk2}: {market2} @ {odds2:.2f}\n\n"
                f"🛡️ **Инструкция по лимитам и безопасности:**\n"
                f"  1. Первым ставится плечо в рекреационной БК ({bk1}), затем перекрывается в sharp ({bk2}).\n"
                f"  2. Избегайте круглых сумм (ставить 9 840 ₽ вместо 10 000 ₽ для маскировки под рекреационного игрока).\n"
                f"  3. При попадании в окно {corridor_window} выигрывают оба плеча, принося сверхприбыль!"
                f"{RU_DISCLAIMER}"
            )
            teaser = f"Эксклюзивный коридор {profit_pct:.1f}% на матч {match}. Только для платных подписчиков."

        return {"title": title, "content": content, "teaser": teaser}

    @staticmethod
    def render_freebet_80_cash(context: Optional[Dict[str, Any]] = None, lang: str = "ru") -> Dict[str, str]:
        ctx = context or {}
        nominal = float(ctx.get("nominal", 3000.0))
        bk_name = ctx.get("bookmaker", "Винлайн")
        k1 = float(ctx.get("k1", 5.0))
        k2 = float(ctx.get("k2", 1.25))

        calc = calculate_freebet_cash(nominal, k1, k2)
        cash = calc["guaranteed_cash"]
        hedge = calc["hedge_stake"]

        if lang == "en":
            title = f"🎁 Matched Betting Blueprint: Convert ${nominal:.0f} Freebet to ${cash:.0f} Guaranteed Cash"
            content = (
                f"🎁 **FREEBET CONVERSION BLUEPRINT (SNR 80% GUARANTEED CASH RULE)**\n\n"
                f"Bookmaker: **{bk_name}** | Bonus: **${nominal:.2f} Freebet**\n"
                f"Guaranteed Payout: **${cash:.2f} Clean Cash (80.0%)**\n\n"
                f"📐 **Mathematical Model:**\n"
                f"  Formula: η = ((K1 - 1) * (K2 - 1)) / K2 ≈ 0.80\n"
                f"  Leg 1 (Bonus Stake): Freebet ${nominal:.0f} on Underdog @ {k1:.2f}\n"
                f"  Leg 2 (Hedge Stake): ${hedge:.2f} on Favorite @ {k2:.2f}\n\n"
                f"💰 **Scenario Verification:**\n"
                f"  • Underdog wins: (${nominal:.0f} * {k1-1:.2f}) - ${hedge:.2f} = **+${cash:.2f}**\n"
                f"  • Favorite wins: ${hedge:.2f} * {k2-1:.2f} = **+${cash:.2f}**\n"
                f"  Zero gambling risk — 100% mathematical certainty.\n"
                f"{EN_DISCLAIMER}"
            )
            teaser = f"Convert a ${nominal:.0f} Freebet into ${cash:.0f} Guaranteed Cash via Matched Betting."
        else:
            title = f"🎁 Дайджест фрибетов: 80% гарантированного кэша ({bk_name} {nominal:,.0f} ₽ → {cash:,.0f} ₽)"
            content = (
                f"🎁 **ДАЙДЖЕСТ ФРИБЕТОВ: 80% ГАРАНТИРОВАННОГО КЭША**\n\n"
                f"Букмекер: **{bk_name}** | Бонус: **Фрибет {nominal:,.0f} ₽**\n"
                f"Гарантированная выплата: **{cash:,.0f} ₽ чистыми деньгами на счёт при любом исходе**\n\n"
                f"📐 **Математический расчёт перекрытия (SNR Matched Betting):**\n"
                f"  Формула конвертации: η = ((K1 - 1) * (K2 - 1)) / K2 ≈ 0.80\n"
                f"  Плечо 1 (Фрибет): ставка {nominal:,.0f} ₽ фрибетом на высокий кэф K1 = {k1:.2f}\n"
                f"  Плечо 2 (Перекрытие): ставка своими средствами {hedge:,.0f} ₽ на противоположный исход K2 = {k2:.2f}\n\n"
                f"💰 **Проверка исходов:**\n"
                f"  • Исход 1 выиграл: ({k1-1:.2f} * {nominal:,.0f} ₽) - {hedge:,.0f} ₽ = **+{cash:,.0f} ₽**\n"
                f"  • Исход 2 выиграл: {hedge:,.0f} ₽ * {k2-1:.2f} = **+{cash:,.0f} ₽**\n\n"
                f"Никакого азарта — только чистая математика SmartBet.guru."
                f"{RU_DISCLAIMER}"
            )
            teaser = f"Фрибет {nominal:,.0f} ₽ → {cash:,.0f} ₽ чистыми деньгами на счёт при любом исходе через вилку."

        return {"title": title, "content": content, "teaser": teaser}

    @staticmethod
    def render_antifraud_guide(context: Optional[Dict[str, Any]] = None, lang: str = "ru") -> Dict[str, str]:
        ctx = context or {}
        account_type = ctx.get("account_type", "Camoufox / Gecko persistent profile")

        if lang == "en":
            title = "🛡️ Anti-Fraud & Stealth Guide: Persistent Browser Warmup & Proxy Routing"
            content = (
                "🛡️ **PROFESSIONAL ANTI-FRAUD & STEALTH PLAYBOOK**\n\n"
                "1. **Gecko / Camoufox Engine Mandatory:**\n"
                "   - Strict ban on clean Chromium and incognito modes.\n"
                "   - Use persistent context preserving cookies.sqlite, storage/default, and cache.\n\n"
                "2. **Browser Cache Warmup Protocol:**\n"
                "   - 2–3 minutes of organic warm-up browsing on trusted sports portals prior to targeting.\n"
                "   - Natural mouse movement trajectories with Bezier curves.\n\n"
                "3. **Network Routing Architecture:**\n"
                "   - Domestic Clean Residential IP for local sportsbooks.\n"
                "   - Low-latency dedicated proxy nodes for international and offshore platforms.\n\n"
                "4. **Drop Account Hygiene:**\n"
                "   - Device canvas fingerprint consistency and TLS Client Hello standardization.\n"
                f"{EN_DISCLAIMER}"
            )
            teaser = "Master anti-fraud defenses with Camoufox persistent profiles & proxy routing."
        else:
            title = "🛡️ Мануал по антифроду: прогрев Gecko/Camoufox и чистые профили"
            content = (
                "🛡️ **ПРОФЕССИОНАЛЬНЫЙ МАНУАЛ ПО АНТИФРОДУ И ПРОГРЕВУ ПРОФИЛЕЙ**\n\n"
                "1. **Запрет инкогнито и 'чистого' Chromium:**\n"
                "   - Букмекерские антифрод-системы детектируют CDP и отсутствие истории в первые 10 секунд.\n"
                "   - Работа ведется строго на Firefox / Camoufox с персистентным профилем (`cookies.sqlite`, `storage/default`).\n\n"
                "2. **Прогрев браузерного кэша (Cache Warmup):**\n"
                "   - Перед ставками бот совершает 2–3 минуты серфинга по спортивным медиа с естественными кривыми Безье.\n\n"
                "3. **Умная маршрутизация IP (Правило #6):**\n"
                "   - РФ-букмекеры (ЕРАИ/ЦУПИС): прямой чистый домашний IP Санкт-Петербурга.\n"
                "   - Оффшорные и зарубежные букмекеры: европейские прокси Нидерландов/США.\n\n"
                "4. **Гигиена сессий:**\n"
                "   - Профили сохраняются в персистентное хранилище Redis / Volume под уникальными persona ID."
                f"{RU_DISCLAIMER}"
            )
            teaser = "Как избегать порезки лимитов: мануал по прогреву профилей и антифроду."

        return {"title": title, "content": content, "teaser": teaser}


class PatronContentDistributor:
    """
    Coordinates and executes distribution of exclusive content to:
    - Telegram VIP (@SmartBetVipBot) -> /api/v1/vip/publish-post
    - VK Donut community wall -> /admin/donut/posts
    - Boosty blog -> /admin/boosty/posts
    - Patreon API v2 -> /api/v1/patreon/posts
    """

    def __init__(
        self,
        vip_url: Optional[str] = None,
        donut_url: Optional[str] = None,
        boosty_url: Optional[str] = None,
        patreon_url: Optional[str] = None,
        timeout: int = 10,
    ):
        self.vip_url = vip_url or os.getenv("VIP_BOT_URL", "http://igaming-vip-bot:8080/api/v1/vip/publish-post")
        self.donut_url = donut_url or os.getenv("DONUT_BOT_URL", "http://igaming-vk-donut-bot:8080/admin/donut/posts")
        self.boosty_url = boosty_url or os.getenv("BOOSTY_BOT_URL", "http://igaming-boosty-bot:8080/admin/boosty/posts")
        self.patreon_url = patreon_url or os.getenv("PATREON_BOT_URL", "http://smm-bot-patreon:8080/api/v1/patreon/posts")
        self.timeout = timeout

    def _http_post(self, url: str, payload: Dict[str, Any]) -> Dict[str, Any]:
        """Performs HTTP POST request with requests if available, or urllib.request."""
        if requests is not None:
            try:
                resp = requests.post(url, json=payload, timeout=self.timeout)
                resp.raise_for_status()
                return resp.json()
            except Exception as e:
                logger.error(f"HTTP request failed to {url} (requests): {e}")
                raise

        # Standard library fallback
        data_bytes = json.dumps(payload).encode("utf-8")
        req = Request(url, data=data_bytes, headers={"Content-Type": "application/json"})
        with urlopen(req, timeout=self.timeout) as resp:
            raw = resp.read().decode("utf-8")
            return json.loads(raw) if raw else {"status": "ok"}

    def publish_to_telegram_vip(
        self,
        title: str,
        content: str,
        protect_content: bool = True,
        dry_run: bool = False,
    ) -> Dict[str, Any]:
        """Publishes post to Telegram VIP channel with protect_content=True."""
        logger.info(f"Publishing to Telegram VIP: '{title}' (protect_content={protect_content})")
        payload = {
            "title": title,
            "content": content,
            "protect_content": protect_content,
        }
        if dry_run:
            return {"platform": "telegram_vip", "status": "simulated", "payload": payload}

        try:
            res = self._http_post(self.vip_url, payload)
            return {"platform": "telegram_vip", "status": "success", "response": res}
        except Exception as e:
            return {"platform": "telegram_vip", "status": "error", "error": str(e)}

    def publish_to_vk_donut(
        self,
        title: str,
        content: str,
        donut_paid_duration: int = -1,
        dry_run: bool = False,
    ) -> Dict[str, Any]:
        """Publishes post to VK Donut community wall with donut_paid_duration=-1."""
        logger.info(f"Publishing to VK Donut: '{title}' (donut_paid_duration={donut_paid_duration})")
        payload = {
            "title": title,
            "content": content,
            "donut_paid_duration": donut_paid_duration,
        }
        if dry_run:
            return {"platform": "vk_donut", "status": "simulated", "payload": payload}

        try:
            res = self._http_post(self.donut_url, payload)
            return {"platform": "vk_donut", "status": "success", "response": res}
        except Exception as e:
            return {"platform": "vk_donut", "status": "error", "error": str(e)}

    def publish_to_boosty(
        self,
        title: str,
        content: str,
        teaser: Optional[str] = None,
        min_tier_rub: int = 2500,
        dry_run: bool = False,
    ) -> Dict[str, Any]:
        """Publishes exclusive post to Boosty blog."""
        logger.info(f"Publishing to Boosty: '{title}' (min_tier={min_tier_rub} RUB)")
        payload = {
            "title": title,
            "content": content,
            "teaser": teaser or "Эксклюзивный контент для подписчиков Boosty.",
            "min_tier_rub": min_tier_rub,
        }
        if dry_run:
            return {"platform": "boosty", "status": "simulated", "payload": payload}

        try:
            res = self._http_post(self.boosty_url, payload)
            return {"platform": "boosty", "status": "success", "response": res}
        except Exception as e:
            return {"platform": "boosty", "status": "error", "error": str(e)}

    def publish_to_patreon(
        self,
        title: str,
        content: str,
        min_tier_cents: int = 2500,
        dry_run: bool = False,
    ) -> Dict[str, Any]:
        """Publishes exclusive post to Patreon for USD tiers."""
        logger.info(f"Publishing to Patreon: '{title}' (min_tier=${min_tier_cents/100:.2f})")
        payload = {
            "title": title,
            "content": content,
            "min_tier_cents": min_tier_cents,
        }
        if dry_run:
            return {"platform": "patreon", "status": "simulated", "payload": payload}

        try:
            res = self._http_post(self.patreon_url, payload)
            return {"platform": "patreon", "status": "success", "response": res}
        except Exception as e:
            return {"platform": "patreon", "status": "error", "error": str(e)}

    def distribute(
        self,
        template_type: str,
        platforms: Optional[List[str]] = None,
        dry_run: bool = False,
        context: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, Any]:
        """
        Orchestrates distribution across all specified platforms.
        Default platforms: ['telegram_vip', 'vk_donut', 'boosty', 'patreon'].
        """
        target_platforms = platforms or ["telegram_vip", "vk_donut", "boosty", "patreon"]
        norm_template = template_type.lower().strip()

        # Render RU content
        if "surebet" in norm_template or "corridor" in norm_template:
            ru_data = ContentTemplateEngine.render_surebet_corridor(context, lang="ru")
            en_data = ContentTemplateEngine.render_surebet_corridor(context, lang="en")
        elif "antifraud" in norm_template or "stealth" in norm_template:
            ru_data = ContentTemplateEngine.render_antifraud_guide(context, lang="ru")
            en_data = ContentTemplateEngine.render_antifraud_guide(context, lang="en")
        else:
            # Default to freebet_80_cash
            ru_data = ContentTemplateEngine.render_freebet_80_cash(context, lang="ru")
            en_data = ContentTemplateEngine.render_freebet_80_cash(context, lang="en")

        results = {}

        if "telegram_vip" in target_platforms:
            results["telegram_vip"] = self.publish_to_telegram_vip(
                title=ru_data["title"],
                content=ru_data["content"],
                protect_content=True,
                dry_run=dry_run,
            )

        if "vk_donut" in target_platforms:
            results["vk_donut"] = self.publish_to_vk_donut(
                title=ru_data["title"],
                content=ru_data["content"],
                donut_paid_duration=-1,
                dry_run=dry_run,
            )

        if "boosty" in target_platforms:
            results["boosty"] = self.publish_to_boosty(
                title=ru_data["title"],
                content=ru_data["content"],
                teaser=ru_data.get("teaser"),
                min_tier_rub=2500,
                dry_run=dry_run,
            )

        if "patreon" in target_platforms:
            results["patreon"] = self.publish_to_patreon(
                title=en_data["title"],
                content=en_data["content"],
                min_tier_cents=2500,
                dry_run=dry_run,
            )

        return {
            "template": template_type,
            "dry_run": dry_run,
            "timestamp": int(time.time()),
            "results": results,
        }


def main() -> None:
    parser = argparse.ArgumentParser(description="SmartBet Patron Exclusive Content Distributor")
    parser.add_argument(
        "--template",
        choices=["freebet", "surebet", "antifraud"],
        default="freebet",
        help="Content template to distribute",
    )
    parser.add_argument(
        "--platforms",
        default="telegram_vip,vk_donut,boosty,patreon",
        help="Comma-separated target platforms",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Simulate execution without sending actual network requests",
    )
    parser.add_argument(
        "--nominal",
        type=float,
        default=3000.0,
        help="Nominal amount for freebet template",
    )
    args = parser.parse_args()

    platforms = [p.strip() for p in args.platforms.split(",") if p.strip()]
    distributor = PatronContentDistributor()

    context = {"nominal": args.nominal}
    logger.info(f"Distributing exclusive patron content: template={args.template}, platforms={platforms}, dry_run={args.dry_run}")
    report = distributor.distribute(
        template_type=args.template,
        platforms=platforms,
        dry_run=args.dry_run,
        context=context,
    )
    print(json.dumps(report, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
