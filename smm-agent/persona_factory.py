#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Persona Generator Module
Task #55 [auto-reg] (Digital Persona Generator, Multi-Jurisdiction & Browser Fingerprints)

Features:
1. Multi-jurisdiction personal profiles (US, RU, EU) for bettors, webmasters, and forum/social users.
2. Realistic Firefox browser fingerprints (User-Agent, WebGL, Screen, Audio/Canvas noise, Hardware).
3. Realistic addresses, phone numbers, postal codes, and valid date of birth (21-40 age range).
4. Secure randomized passwords, cryptowallets (USDT TRC20/ERC20), and communication handles.
5. Exportable to JSON and compatible with Playwright / Camoufox Persistent Context.
"""

from dataclasses import asdict, dataclass, field
import datetime
import hashlib
import json
import logging
import os
import random
import string
import uuid
from typing import Any, Dict, List, Optional

logger = logging.getLogger("PersonaFactory")


@dataclass
class BrowserFingerprint:
    """Realistic Firefox browser fingerprint for stealth persistent sessions."""
    user_agent: str
    viewport_width: int
    viewport_height: int
    color_depth: int
    device_scale_factor: float
    webgl_vendor: str
    webgl_renderer: str
    hardware_concurrency: int
    device_memory_gb: int
    timezone_id: str
    locale: str
    canvas_noise_seed: int
    audio_noise_seed: int

    def to_firefox_prefs(self) -> Dict[str, Any]:
        """Returns Firefox user preferences matching this fingerprint."""
        return {
            "general.useragent.override": self.user_agent,
            "intl.accept_languages": f"{self.locale},en-US;q=0.7,en;q=0.3",
            "dom.maxHardwareConcurrency": self.hardware_concurrency,
            "privacy.resistFingerprinting": False,  # Disabled to preserve realistic custom fingerprint
            "webgl.renderer-string-override": self.webgl_renderer,
            "webgl.vendor-string-override": self.webgl_vendor,
        }


@dataclass
class BasePersona:
    """Base digital persona with identification, contacts, and browser fingerprint."""
    persona_id: str
    locale: str
    first_name: str
    last_name: str
    gender: str
    date_of_birth: str  # YYYY-MM-DD
    email: str
    phone: str
    password: str
    street_address: str
    city: str
    state_or_region: str
    postal_code: str
    country_code: str
    fingerprint: BrowserFingerprint

    def to_dict(self) -> Dict[str, Any]:
        return asdict(self)

    def to_json(self) -> str:
        return json.dumps(self.to_dict(), indent=2, ensure_ascii=False)


@dataclass
class AffiliatePersona(BasePersona):
    """Digital persona specialized for affiliate partner programs and webmaster registrations."""
    website_url: str = "https://smartbet.guru"
    telegram: str = ""
    traffic_sources: List[str] = field(default_factory=list)
    monthly_traffic_estimate: str = "150,000+ unique visitors / month"
    company_name: str = "SmartBet Media Group"
    payment_method: str = "USDT TRC20"
    wallet_address: str = ""
    skype: str = ""


@dataclass
class BettorPersona(BasePersona):
    """Digital persona specialized for bookmaker registrations across 52 sportsbooks."""
    username: str = ""
    preferred_currency: str = "USD"
    ssn_last4: str = ""
    id_document_number: str = ""
    preferred_sports: List[str] = field(default_factory=list)
    preferred_leagues: List[str] = field(default_factory=list)
    favorite_teams: List[str] = field(default_factory=list)
    bankroll_usd: float = 1000.0
    promo_code: str = "SMARTBET"


@dataclass
class SocialPersona(BasePersona):
    """Digital persona specialized for Reddit, forums, and social network presence."""
    username: str = ""
    reddit_username: str = ""
    forum_handle: str = ""
    bio: str = ""
    interests: List[str] = field(default_factory=list)
    target_subreddits: List[str] = field(default_factory=list)


class PersonaFactory:
    """Factory generating realistic digital personas and browser fingerprints."""

    # Names databases
    NAMES_RU = {
        "male": {
            "first": ["Алексей", "Дмитрий", "Сергей", "Михаил", "Иван", "Артем", "Максим", "Денис", "Павел", "Кирилл", "Евгений", "Антон"],
            "last": ["Смирнов", "Иванов", "Кузнецов", "Попов", "Соколов", "Лебедев", "Козлов", "Новиков", "Морозов", "Петров", "Волков", "Соловьев"]
        },
        "female": {
            "first": ["Анна", "Елена", "Ольга", "Мария", "Екатерина", "Наталья", "Татьяна", "Юлия", "Анастасия", "Дарья"],
            "last": ["Смирнова", "Иванова", "Кузнецова", "Попова", "Соколова", "Лебедева", "Козлова", "Новикова", "Морозова", "Петрова"]
        }
    }

    NAMES_US = {
        "male": {
            "first": ["James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Thomas", "Charles", "Daniel", "Matthew"],
            "last": ["Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Wilson"]
        },
        "female": {
            "first": ["Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen", "Emily", "Ashley"],
            "last": ["Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Taylor", "Anderson"]
        }
    }

    NAMES_EU = {
        "male": {
            "first": ["Lukas", "Leon", "Maximilian", "Jan", "Lars", "Hendrik", "Sven", "Oliver", "Arthur", "Pierre", "Luca", "Marco"],
            "last": ["Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "de Jong", "Jansen", "Bakker", "Visser"]
        },
        "female": {
            "first": ["Emma", "Mia", "Sophia", "Hannah", "Anna", "Lea", "Emilia", "Marie", "Sophie", "Laura", "Chloe", "Camille"],
            "last": ["Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "de Jong", "Jansen", "Bakker", "Visser", "Dubois", "Martin"]
        }
    }

    LOCATIONS_US = [
        {"city": "New York", "state": "NY", "zip": "10001", "street": "Broadway", "tz": "America/New_York"},
        {"city": "Brooklyn", "state": "NY", "zip": "11201", "street": "Atlantic Ave", "tz": "America/New_York"},
        {"city": "Jersey City", "state": "NJ", "zip": "07302", "street": "Grove St", "tz": "America/New_York"},
        {"city": "Chicago", "state": "IL", "zip": "60601", "street": "Michigan Ave", "tz": "America/Chicago"},
        {"city": "Austin", "state": "TX", "zip": "78701", "street": "Congress Ave", "tz": "America/Chicago"},
        {"city": "Miami", "state": "FL", "zip": "33101", "street": "Biscayne Blvd", "tz": "America/New_York"},
        {"city": "Los Angeles", "state": "CA", "zip": "90012", "street": "Sunset Blvd", "tz": "America/Los_Angeles"},
        {"city": "Las Vegas", "state": "NV", "zip": "89101", "street": "Fremont St", "tz": "America/Los_Angeles"},
    ]

    LOCATIONS_RU = [
        {"city": "Москва", "state": "Московская область", "zip": "101000", "street": "ул. Тверская", "tz": "Europe/Moscow"},
        {"city": "Санкт-Петербург", "state": "Ленинградская область", "zip": "190000", "street": "Невский проспект", "tz": "Europe/Moscow"},
        {"city": "Казань", "state": "Республика Татарстан", "zip": "420000", "street": "ул. Баумана", "tz": "Europe/Moscow"},
        {"city": "Новосибирск", "state": "Новосибирская область", "zip": "630000", "street": "Красный проспект", "tz": "Asia/Novosibirsk"},
        {"city": "Екатеринбург", "state": "Свердловская область", "zip": "620000", "street": "ул. Ленина", "tz": "Asia/Yekaterinburg"},
    ]

    LOCATIONS_EU = [
        {"city": "Amsterdam", "state": "North Holland", "zip": "1012", "street": "Damrak", "country": "NL", "tz": "Europe/Amsterdam"},
        {"city": "Rotterdam", "state": "South Holland", "zip": "3011", "street": "Coolsingel", "country": "NL", "tz": "Europe/Amsterdam"},
        {"city": "Berlin", "state": "Berlin", "zip": "10115", "street": "Friedrichstraße", "country": "DE", "tz": "Europe/Berlin"},
        {"city": "Frankfurt", "state": "Hesse", "zip": "60311", "street": "Zeil", "country": "DE", "tz": "Europe/Berlin"},
        {"city": "Vienna", "state": "Vienna", "zip": "1010", "street": "Kärntner Straße", "country": "AT", "tz": "Europe/Vienna"},
    ]

    WEBGL_RENDERERS = [
        ("Google Inc. (NVIDIA)", "ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)"),
        ("Google Inc. (NVIDIA)", "ANGLE (NVIDIA, NVIDIA GeForce RTX 3070 Direct3D11 vs_5_0 ps_5_0, D3D11)"),
        ("Google Inc. (NVIDIA)", "ANGLE (NVIDIA, NVIDIA GeForce RTX 4070 Direct3D11 vs_5_0 ps_5_0, D3D11)"),
        ("Google Inc. (AMD)", "ANGLE (AMD, AMD Radeon RX 6700 XT Direct3D11 vs_5_0 ps_5_0, D3D11)"),
        ("Google Inc. (Intel)", "ANGLE (Intel, Intel(R) Iris(R) Xe Graphics Direct3D11 vs_5_0 ps_5_0, D3D11)"),
    ]

    FIREFOX_USER_AGENTS = [
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/129.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:130.0) Gecko/20100101 Firefox/130.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14.5; rv:128.0) Gecko/20100101 Firefox/128.0",
        "Mozilla/5.0 (X11; Linux x86_64; rv:128.0) Gecko/20100101 Firefox/128.0",
    ]

    SCREEN_RESOLUTIONS = [
        (1920, 1080),
        (1920, 1200),
        (2560, 1440),
        (1440, 900),
        (1536, 864),
    ]

    @classmethod
    def generate_password(cls, length: int = 14) -> str:
        """Generates a secure password compliant with strict sportsbook / partner policies."""
        upper = random.choice(string.ascii_uppercase)
        lower = random.choice(string.ascii_lowercase)
        digit = random.choice(string.digits)
        special = random.choice("!@#$%^&*")
        rest = [random.choice(string.ascii_letters + string.digits + "!@#$%^&*") for _ in range(length - 4)]
        pwd_chars = [upper, lower, digit, special] + rest
        random.shuffle(pwd_chars)
        return "".join(pwd_chars)

    @classmethod
    def generate_dob(cls, min_age: int = 21, max_age: int = 40) -> str:
        """Generates valid date of birth ensuring legal gambling / adult age."""
        today = datetime.date.today()
        years_ago = random.randint(min_age, max_age)
        days_offset = random.randint(0, 364)
        dob = today - datetime.timedelta(days=years_ago * 365 + days_offset)
        return dob.strftime("%Y-%m-%d")

    @classmethod
    def generate_trc20_wallet(cls) -> str:
        """Generates realistic USDT TRC20 wallet address."""
        chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        return "T" + "".join(random.choices(chars, k=33))

    @classmethod
    def create_browser_fingerprint(cls, locale: str = "us") -> BrowserFingerprint:
        """Generates consistent realistic Firefox browser fingerprint."""
        res_w, res_h = random.choice(cls.SCREEN_RESOLUTIONS)
        vendor, renderer = random.choice(cls.WEBGL_RENDERERS)

        if locale.lower() == "ru":
            tz = "Europe/Moscow"
            lang = "ru-RU"
        elif locale.lower() == "eu":
            tz = random.choice(["Europe/Amsterdam", "Europe/Berlin", "Europe/Vienna"])
            lang = "en-US"
        else:
            tz = random.choice(["America/New_York", "America/Chicago", "America/Los_Angeles"])
            lang = "en-US"

        seed = random.randint(100000, 999999)
        return BrowserFingerprint(
            user_agent=random.choice(cls.FIREFOX_USER_AGENTS),
            viewport_width=res_w,
            viewport_height=res_h,
            color_depth=24,
            device_scale_factor=1.0 if res_w <= 1920 else 1.25,
            webgl_vendor=vendor,
            webgl_renderer=renderer,
            hardware_concurrency=random.choice([8, 12, 16]),
            device_memory_gb=random.choice([8, 16, 32]),
            timezone_id=tz,
            locale=lang,
            canvas_noise_seed=seed,
            audio_noise_seed=seed + 42,
        )

    @classmethod
    def create_affiliate_persona(
        cls,
        locale: str = "ru",
        domain: str = "smartbet.guru"
    ) -> AffiliatePersona:
        """Generates digital persona for webmaster / affiliate program registrations."""
        p_id = f"aff_{uuid.uuid4().hex[:10]}"
        gender = random.choice(["male", "female"])
        names_db = cls.NAMES_RU if locale == "ru" else (cls.NAMES_EU if locale == "eu" else cls.NAMES_US)
        first_name = random.choice(names_db[gender]["first"])
        last_name = random.choice(names_db[gender]["last"])

        rand_num = random.randint(100, 999)
        slug = f"{first_name.lower()}_{last_name.lower()}_{rand_num}"
        # Transliterate for email if RU
        slug_clean = "".join(c for c in slug if c.isalnum() or c == "_")
        email = f"aff_{slug_clean}@{domain}"
        telegram = f"@smartbet_partner_{rand_num}"
        phone = f"+7999{random.randint(1000000, 9999999)}" if locale == "ru" else f"+1201{random.randint(1000000, 9999999)}"

        loc = random.choice(cls.LOCATIONS_RU if locale == "ru" else (cls.LOCATIONS_EU if locale == "eu" else cls.LOCATIONS_US))
        street_num = random.randint(1, 150)
        country = loc.get("country", "RU" if locale == "ru" else "US")

        fingerprint = cls.create_browser_fingerprint(locale=locale)

        return AffiliatePersona(
            persona_id=p_id,
            locale=locale,
            first_name=first_name,
            last_name=last_name,
            gender=gender,
            date_of_birth=cls.generate_dob(min_age=24, max_age=38),
            email=email,
            phone=phone,
            password=cls.generate_password(),
            street_address=f"{loc['street']}, д. {street_num}" if locale == "ru" else f"{street_num} {loc['street']}",
            city=loc["city"],
            state_or_region=loc["state"],
            postal_code=loc["zip"],
            country_code=country,
            fingerprint=fingerprint,
            website_url=f"https://{domain}",
            telegram=telegram,
            traffic_sources=[
                "Арбитражный сканер спортивных событий (Surebets / Valuebets)",
                "Telegram-каналы со спортивной аналитикой и сигналами",
                "SEO-трафик портала SmartBet.guru (обзоры БК, сравнение линий)",
                "Калькулятор фрибетов и бонусхантинг (Matched Betting 80% Guaranteed Cash)",
            ],
            monthly_traffic_estimate="150,000+ уникальных пользователей / месяц",
            company_name="SmartBet Guru Media Ltd",
            payment_method="USDT TRC20",
            wallet_address=cls.generate_trc20_wallet(),
            skype=f"live:smartbet_{slug_clean}"
        )

    @classmethod
    def create_bettor_persona(
        cls,
        locale: str = "us",
        preferred_currency: str = "USD"
    ) -> BettorPersona:
        """Generates digital persona for bookmaker user registrations across 52 sportsbooks."""
        p_id = f"bet_{uuid.uuid4().hex[:10]}"
        gender = random.choice(["male", "female"])
        names_db = cls.NAMES_US if locale == "us" else (cls.NAMES_RU if locale == "ru" else cls.NAMES_EU)
        first_name = random.choice(names_db[gender]["first"])
        last_name = random.choice(names_db[gender]["last"])

        rand_num = random.randint(100, 999)
        username = f"{first_name[:3].lower()}_{last_name.lower()}_{rand_num}"
        email = f"{username}@gmail.com" if locale != "ru" else f"{username}@yandex.ru"
        phone = f"+1{random.randint(200, 999)}{random.randint(1000000, 9999999)}" if locale == "us" else (
            f"+7999{random.randint(1000000, 9999999)}" if locale == "ru" else f"+316{random.randint(10000000, 99999999)}"
        )

        loc = random.choice(cls.LOCATIONS_US if locale == "us" else (cls.LOCATIONS_RU if locale == "ru" else cls.LOCATIONS_EU))
        street_num = random.randint(10, 850)
        country = loc.get("country", "US" if locale == "us" else ("RU" if locale == "ru" else "NL"))

        fingerprint = cls.create_browser_fingerprint(locale=locale)

        return BettorPersona(
            persona_id=p_id,
            locale=locale,
            first_name=first_name,
            last_name=last_name,
            gender=gender,
            date_of_birth=cls.generate_dob(min_age=21, max_age=36),
            email=email,
            phone=phone,
            password=cls.generate_password(),
            street_address=f"{street_num} {loc['street']}" if locale != "ru" else f"{loc['street']}, {street_num}",
            city=loc["city"],
            state_or_region=loc["state"],
            postal_code=loc["zip"],
            country_code=country,
            fingerprint=fingerprint,
            username=username,
            preferred_currency=preferred_currency,
            ssn_last4=str(random.randint(1000, 9999)),
            id_document_number=f"DOC{random.randint(10000000, 99999999)}",
            preferred_sports=["Soccer", "Basketball", "Tennis", "Ice Hockey", "Esports"],
            preferred_leagues=["Premier League", "NBA", "Champions League", "NHL", "ATP"],
            favorite_teams=["Arsenal", "Real Madrid", "Boston Celtics", "Tampa Bay Lightning"],
            bankroll_usd=round(random.uniform(500.0, 5000.0), 2),
            promo_code="SMARTBET"
        )

    @classmethod
    def create_social_persona(
        cls,
        locale: str = "us",
        interests: Optional[List[str]] = None
    ) -> SocialPersona:
        """Generates digital persona for Reddit and sports forums."""
        p_id = f"soc_{uuid.uuid4().hex[:10]}"
        gender = random.choice(["male", "female"])
        names_db = cls.NAMES_US if locale == "us" else (cls.NAMES_RU if locale == "ru" else cls.NAMES_EU)
        first_name = random.choice(names_db[gender]["first"])
        last_name = random.choice(names_db[gender]["last"])

        rand_num = random.randint(100, 999)
        username = f"smart_analyst_{rand_num}" if locale == "us" else f"bet_guru_{rand_num}"
        email = f"{username}@outlook.com"
        phone = f"+1201{random.randint(1000000, 9999999)}"

        loc = random.choice(cls.LOCATIONS_US if locale == "us" else (cls.LOCATIONS_RU if locale == "ru" else cls.LOCATIONS_EU))
        street_num = random.randint(1, 200)

        fingerprint = cls.create_browser_fingerprint(locale=locale)

        if not interests:
            interests = ["sports analytics", "value betting", "surebets", "matched betting", "football tactics"]

        return SocialPersona(
            persona_id=p_id,
            locale=locale,
            first_name=first_name,
            last_name=last_name,
            gender=gender,
            date_of_birth=cls.generate_dob(min_age=22, max_age=34),
            email=email,
            phone=phone,
            password=cls.generate_password(),
            street_address=f"{street_num} {loc['street']}",
            city=loc["city"],
            state_or_region=loc["state"],
            postal_code=loc["zip"],
            country_code=loc.get("country", "US"),
            fingerprint=fingerprint,
            username=username,
            reddit_username=f"u/{username}",
            forum_handle=username,
            bio="Sports analytics enthusiast | Arbitrage & Value Betting models | SmartBet.guru reader",
            interests=interests,
            target_subreddits=["r/Sportsbook", "r/SoccerBetting", "r/ArbitrageBetting", "r/MatchedBettingUK"]
        )
