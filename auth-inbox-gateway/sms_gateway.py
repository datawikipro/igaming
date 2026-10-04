#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Multi-Provider SMS Gateway Service
plane-4a454c05 / Task 2: SmsGatewayService

Supported providers:
  - OnlineSim   (onlinesim.io)       — RU virtual numbers
  - SmsActivate (sms-activate.org)   — RU / EU / US virtual numbers
  - GrizzlySms  (grizzlysms.com)     — RU virtual numbers
  - Mock                             — Deterministic codes for CI / local testing

Provider selection priority (descending):
  1. preferred_provider (if passed to rent_number)
  2. First provider with sufficient balance
  3. Next available provider (fallback chain)

Environment variables:
  SMS_PROVIDER_ONLINESIM_KEY    — OnlineSim API key
  SMS_PROVIDER_SMS_ACTIVATE_KEY — SmsActivate API key
  SMS_PROVIDER_GRIZZLY_SMS_KEY  — GrizzlySms API key
  SMS_MOCK_MODE                 — "true" to enable deterministic mock mode
"""

import asyncio
import enum
import hashlib
import logging
import os
import re
import time
from dataclasses import dataclass, field
from datetime import datetime, timedelta, timezone
from typing import Dict, List, Optional

try:
    import aiohttp
except ImportError:
    aiohttp = None  # will raise on actual use; mock mode still works

logger = logging.getLogger("SmsGatewayService")


# ---------------------------------------------------------------------------
# Constants / config
# ---------------------------------------------------------------------------

SMS_MOCK_MODE: bool = os.getenv("SMS_MOCK_MODE", "false").lower() == "true"

ONLINESIM_KEY: Optional[str] = os.getenv("SMS_PROVIDER_ONLINESIM_KEY")
SMS_ACTIVATE_KEY: Optional[str] = os.getenv("SMS_PROVIDER_SMS_ACTIVATE_KEY")
GRIZZLY_SMS_KEY: Optional[str] = os.getenv("SMS_PROVIDER_GRIZZLY_SMS_KEY")

# Polling interval (seconds) when waiting for an SMS code from the provider
POLL_INTERVAL: float = 5.0

# Minimum balance threshold to consider a provider "usable"
MIN_BALANCE: float = 0.5


# ---------------------------------------------------------------------------
# Data models
# ---------------------------------------------------------------------------


class SmsProvider(enum.Enum):
    ONLINESIM = "onlinesim"
    SMS_ACTIVATE = "sms_activate"
    GRIZZLY_SMS = "grizzly_sms"
    MOCK = "mock"


@dataclass
class VirtualNumber:
    number: str              # E.164 format: +7XXXXXXXXXX / +1XXXXXXXXXX / +316XXXXXXXX
    provider: SmsProvider
    activation_id: str
    service: str             # "google", "reddit", "instagram", "telegram" …
    country_code: int        # 7=RU, 1=US, 31=NL
    expires_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc) + timedelta(minutes=20))


# ---------------------------------------------------------------------------
# Provider adapters
# ---------------------------------------------------------------------------


class _OnlineSimAdapter:
    """
    OnlineSim (onlinesim.io) adapter.

    Docs: https://onlinesim.io/docs/api
    """

    BASE = "https://onlinesim.io/api"

    def __init__(self, api_key: str, session: "aiohttp.ClientSession"):
        self._key = api_key
        self._s = session

    async def get_balance(self) -> float:
        url = f"{self.BASE}/getBalance.php"
        params = {"apikey": self._key}
        async with self._s.get(url, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            data = await r.json(content_type=None)
        balance = data.get("balance", 0.0)
        logger.debug("OnlineSim balance: %.2f", balance)
        return float(balance)

    async def rent_number(self, service: str, country_code: int = 7) -> VirtualNumber:
        """
        GET https://onlinesim.io/api/getNum.php?apikey=&service=&country=7
        Returns tzid (activation_id) + number.
        """
        url = f"{self.BASE}/getNum.php"
        params = {"apikey": self._key, "service": service, "country": country_code}
        async with self._s.get(url, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            data = await r.json(content_type=None)

        if data.get("response") != 1:
            raise RuntimeError(f"OnlineSim getNum error: {data}")

        tzid = str(data["tzid"])
        number = _normalize_phone(str(data.get("number", "")), country_code)
        logger.info("OnlineSim rented #%s for service=%s country=%s", tzid, service, country_code)
        return VirtualNumber(
            number=number,
            provider=SmsProvider.ONLINESIM,
            activation_id=tzid,
            service=service,
            country_code=country_code,
        )

    async def get_sms(self, activation_id: str) -> Optional[str]:
        """
        GET https://onlinesim.io/api/getState.php?apikey=&tzid=
        Returns code if received.
        """
        url = f"{self.BASE}/getState.php"
        params = {"apikey": self._key, "tzid": activation_id}
        async with self._s.get(url, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            data = await r.json(content_type=None)

        # data is a list; each element has "response" and optionally "msg"
        items = data if isinstance(data, list) else [data]
        for item in items:
            if item.get("response") == 1 and item.get("msg"):
                return str(item["msg"])
        return None

    async def release_number(self, activation_id: str) -> bool:
        url = f"{self.BASE}/setOperationRevise.php"
        params = {"apikey": self._key, "tzid": activation_id}
        async with self._s.get(url, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            data = await r.json(content_type=None)
        ok = data.get("response") == 1
        logger.info("OnlineSim release #%s -> %s", activation_id, ok)
        return ok


class _SmsActivateAdapter:
    """
    SmsActivate (sms-activate.org) adapter.

    Docs: https://sms-activate.org/en/api2
    """

    BASE = "https://api.sms-activate.org/stubs/handler_api.php"

    def __init__(self, api_key: str, session: "aiohttp.ClientSession"):
        self._key = api_key
        self._s = session

    async def get_balance(self) -> float:
        params = {"api_key": self._key, "action": "getBalance"}
        async with self._s.get(self.BASE, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            text = await r.text()
        # Response: "ACCESS_BALANCE:12.50"
        if text.startswith("ACCESS_BALANCE:"):
            balance = float(text.split(":")[1])
            logger.debug("SmsActivate balance: %.2f", balance)
            return balance
        raise RuntimeError(f"SmsActivate getBalance error: {text}")

    async def rent_number(self, service: str, country_code: int = 7) -> VirtualNumber:
        """
        GET .../handler_api.php?api_key=&action=getNumber&service=&country=
        Returns: ACCESS_NUMBER:<id>:<phone>
        """
        params = {
            "api_key": self._key,
            "action": "getNumber",
            "service": service,
            "country": country_code,
        }
        async with self._s.get(self.BASE, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            text = await r.text()

        if not text.startswith("ACCESS_NUMBER:"):
            raise RuntimeError(f"SmsActivate getNumber error: {text}")

        parts = text.split(":")
        activation_id = parts[1]
        number = _normalize_phone(parts[2], country_code)
        logger.info("SmsActivate rented #%s for service=%s country=%s", activation_id, service, country_code)
        return VirtualNumber(
            number=number,
            provider=SmsProvider.SMS_ACTIVATE,
            activation_id=activation_id,
            service=service,
            country_code=country_code,
        )

    async def get_sms(self, activation_id: str) -> Optional[str]:
        """
        GET .../handler_api.php?api_key=&action=getStatus&id=
        STATUS_OK:<code> → code received
        STATUS_WAIT_CODE → still waiting
        """
        params = {"api_key": self._key, "action": "getStatus", "id": activation_id}
        async with self._s.get(self.BASE, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            text = await r.text()

        if text.startswith("STATUS_OK:"):
            return text.split(":")[1].strip()
        return None  # STATUS_WAIT_CODE or other

    async def release_number(self, activation_id: str) -> bool:
        """Set status=8 (cancel)."""
        params = {"api_key": self._key, "action": "setStatus", "id": activation_id, "status": 8}
        async with self._s.get(self.BASE, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            text = await r.text()
        ok = "ACCESS_CANCEL" in text
        logger.info("SmsActivate release #%s -> %s (%s)", activation_id, ok, text)
        return ok


class _GrizzlySmsAdapter:
    """
    GrizzlySms (grizzlysms.com) adapter.

    Docs: https://grizzlysms.com/ru/developers
    """

    BASE = "https://grizzlysms.com/stubs/handler_api.php"

    def __init__(self, api_key: str, session: "aiohttp.ClientSession"):
        self._key = api_key
        self._s = session

    async def get_balance(self) -> float:
        params = {"api_key": self._key, "action": "getBalance"}
        async with self._s.get(self.BASE, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            text = await r.text()
        if text.startswith("ACCESS_BALANCE:"):
            balance = float(text.split(":")[1])
            logger.debug("GrizzlySms balance: %.2f", balance)
            return balance
        raise RuntimeError(f"GrizzlySms getBalance error: {text}")

    async def rent_number(self, service: str, country_code: int = 7) -> VirtualNumber:
        """
        POST https://grizzlysms.com/stubs/handler_api.php
        body: api_key, action=getNumber, service, country
        Returns: ACCESS_NUMBER:<id>:<phone>
        """
        data = {
            "api_key": self._key,
            "action": "getNumber",
            "service": service,
            "country": country_code,
        }
        async with self._s.post(self.BASE, data=data, timeout=aiohttp.ClientTimeout(total=15)) as r:
            text = await r.text()

        if not text.startswith("ACCESS_NUMBER:"):
            raise RuntimeError(f"GrizzlySms getNumber error: {text}")

        parts = text.split(":")
        activation_id = parts[1]
        number = _normalize_phone(parts[2], country_code)
        logger.info("GrizzlySms rented #%s for service=%s country=%s", activation_id, service, country_code)
        return VirtualNumber(
            number=number,
            provider=SmsProvider.GRIZZLY_SMS,
            activation_id=activation_id,
            service=service,
            country_code=country_code,
        )

    async def get_sms(self, activation_id: str) -> Optional[str]:
        params = {"api_key": self._key, "action": "getStatus", "id": activation_id}
        async with self._s.get(self.BASE, params=params, timeout=aiohttp.ClientTimeout(total=15)) as r:
            text = await r.text()
        if text.startswith("STATUS_OK:"):
            return text.split(":")[1].strip()
        return None

    async def release_number(self, activation_id: str) -> bool:
        data = {"api_key": self._key, "action": "setStatus", "id": activation_id, "status": 8}
        async with self._s.post(self.BASE, data=data, timeout=aiohttp.ClientTimeout(total=15)) as r:
            text = await r.text()
        ok = "ACCESS_CANCEL" in text
        logger.info("GrizzlySms release #%s -> %s (%s)", activation_id, ok, text)
        return ok


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------


def _normalize_phone(raw: str, country_code: int) -> str:
    """Converts a raw digit string to E.164 format."""
    digits = re.sub(r"\D", "", raw)
    cc = str(country_code)
    if digits.startswith(cc):
        return f"+{digits}"
    return f"+{cc}{digits}"


def _mock_code(seed: str) -> str:
    """Deterministic 6-digit mock OTP derived from seed (phone / activation_id)."""
    h = int(hashlib.md5(seed.encode()).hexdigest()[:6], 16)
    return str(100000 + (h % 900000))


# ---------------------------------------------------------------------------
# Main service
# ---------------------------------------------------------------------------


class SmsGatewayService:
    """
    Multi-provider SMS gateway with virtual number rental and OTP polling.

    Usage:
        async with SmsGatewayService() as gw:
            num = await gw.rent_number("google", country_code=7)
            code = await gw.wait_for_sms(num, pattern=r"G-\\d+")
            await gw.release_number(num)
    """

    def __init__(self):
        self._mock_mode: bool = SMS_MOCK_MODE
        self._session: Optional["aiohttp.ClientSession"] = None
        # Adapter instances (lazily built in __aenter__ / _ensure_session)
        self._adapters: Dict[SmsProvider, object] = {}

    # ------------------------------------------------------------------
    # Async context manager
    # ------------------------------------------------------------------

    async def __aenter__(self) -> "SmsGatewayService":
        await self._ensure_session()
        return self

    async def __aexit__(self, *_) -> None:
        await self.close()

    async def close(self) -> None:
        if self._session and not self._session.closed:
            await self._session.close()

    # ------------------------------------------------------------------
    # Internal helpers
    # ------------------------------------------------------------------

    async def _ensure_session(self) -> None:
        if self._mock_mode:
            return
        if aiohttp is None:
            raise ImportError("aiohttp is required for live SMS providers. Install it or set SMS_MOCK_MODE=true.")
        if self._session is None or self._session.closed:
            self._session = aiohttp.ClientSession()
            self._build_adapters()

    def _build_adapters(self) -> None:
        s = self._session
        if ONLINESIM_KEY:
            self._adapters[SmsProvider.ONLINESIM] = _OnlineSimAdapter(ONLINESIM_KEY, s)
        if SMS_ACTIVATE_KEY:
            self._adapters[SmsProvider.SMS_ACTIVATE] = _SmsActivateAdapter(SMS_ACTIVATE_KEY, s)
        if GRIZZLY_SMS_KEY:
            self._adapters[SmsProvider.GRIZZLY_SMS] = _GrizzlySmsAdapter(GRIZZLY_SMS_KEY, s)

    def _provider_order(self, preferred: Optional[SmsProvider]) -> List[SmsProvider]:
        """Returns the ordered list of providers to try (preferred first)."""
        order = [SmsProvider.ONLINESIM, SmsProvider.SMS_ACTIVATE, SmsProvider.GRIZZLY_SMS]
        if preferred and preferred in order:
            order = [preferred] + [p for p in order if p != preferred]
        return order

    def _adapter(self, provider: SmsProvider):
        adapter = self._adapters.get(provider)
        if adapter is None:
            raise RuntimeError(
                f"Provider {provider.value} is not configured. "
                f"Set the corresponding SMS_PROVIDER_*_KEY environment variable."
            )
        return adapter

    # ------------------------------------------------------------------
    # Public API
    # ------------------------------------------------------------------

    async def rent_number(
        self,
        service: str,
        country_code: int = 7,
        preferred_provider: Optional[SmsProvider] = None,
    ) -> VirtualNumber:
        """
        Rents a virtual phone number for the specified service.

        Tries providers in order (preferred → fallback chain) until one
        succeeds or all fail.

        Args:
            service:            Provider-specific service code, e.g. "go" (Google),
                                "rg" (Reddit), "ig" (Instagram), "tg" (Telegram).
            country_code:       ITU-T country code. 7=RU, 1=US, 31=NL.
            preferred_provider: Hint which provider to try first.

        Returns:
            VirtualNumber with E.164 phone, provider tag and activation_id.
        """
        await self._ensure_session()

        if self._mock_mode:
            mock_id = f"mock-{service}-{country_code}-{int(time.time())}"
            number = _normalize_phone(f"9{abs(hash(mock_id)) % 10**9:09d}", country_code)
            logger.info("Mock mode: rented %s for service=%s", number, service)
            return VirtualNumber(
                number=number,
                provider=SmsProvider.MOCK,
                activation_id=mock_id,
                service=service,
                country_code=country_code,
            )

        last_error: Optional[Exception] = None
        for provider in self._provider_order(preferred_provider):
            if provider not in self._adapters:
                continue
            try:
                balance = await self._adapter(provider).get_balance()
                if balance < MIN_BALANCE:
                    logger.warning("Provider %s has low balance (%.2f), skipping", provider.value, balance)
                    continue
                vnum = await self._adapter(provider).rent_number(service, country_code)
                logger.info(
                    "Rented number %s via %s (activation_id=%s)",
                    vnum.number, provider.value, vnum.activation_id,
                )
                return vnum
            except Exception as exc:
                logger.warning("Provider %s failed to rent number: %s", provider.value, exc)
                last_error = exc

        raise RuntimeError(
            f"All SMS providers failed to rent a number for service={service} country={country_code}. "
            f"Last error: {last_error}"
        )

    async def wait_for_sms(
        self,
        number: VirtualNumber,
        pattern: str = r"\d{4,6}",
        timeout: int = 120,
    ) -> Optional[str]:
        """
        Polls the provider until an SMS code matching *pattern* arrives
        or *timeout* seconds elapse.

        Args:
            number:   VirtualNumber returned by rent_number().
            pattern:  Regex pattern to match against the received SMS body.
            timeout:  Maximum wait time in seconds (default: 120).

        Returns:
            The matched code string, or None on timeout.
        """
        await self._ensure_session()

        if number.provider == SmsProvider.MOCK:
            code = _mock_code(number.activation_id)
            logger.info("Mock mode: returning code %s for %s", code, number.number)
            return code

        rx = re.compile(pattern)
        deadline = time.monotonic() + timeout

        logger.info(
            "Waiting for SMS on %s (provider=%s, activation_id=%s, timeout=%ds) ...",
            number.number, number.provider.value, number.activation_id, timeout,
        )

        while time.monotonic() < deadline:
            try:
                raw = await self._adapter(number.provider).get_sms(number.activation_id)
                if raw:
                    match = rx.search(raw)
                    if match:
                        code = match.group(0)
                        logger.info("SMS code received for %s: %s", number.number, code)
                        return code
                    logger.debug("SMS received but pattern not matched: %r", raw)
            except Exception as exc:
                logger.warning("Error polling SMS for %s: %s", number.number, exc)

            remaining = deadline - time.monotonic()
            if remaining <= 0:
                break
            await asyncio.sleep(min(POLL_INTERVAL, remaining))

        logger.warning("Timeout waiting for SMS on %s", number.number)
        return None

    async def release_number(self, number: VirtualNumber) -> bool:
        """
        Releases / cancels the rented virtual number back to the provider.

        Args:
            number: VirtualNumber to release.

        Returns:
            True if the provider accepted the release, False otherwise.
        """
        await self._ensure_session()

        if number.provider == SmsProvider.MOCK:
            logger.info("Mock mode: releasing %s (noop)", number.number)
            return True

        try:
            ok = await self._adapter(number.provider).release_number(number.activation_id)
            return ok
        except Exception as exc:
            logger.error("Failed to release number %s: %s", number.number, exc)
            return False

    async def get_balance(self, provider: SmsProvider) -> float:
        """
        Returns the current balance for a specific provider.

        Args:
            provider: SmsProvider enum value (not MOCK).

        Returns:
            Balance as a float.
        """
        await self._ensure_session()

        if provider == SmsProvider.MOCK:
            return 999.0

        return await self._adapter(provider).get_balance()

    async def get_all_balances(self) -> Dict[str, float]:
        """Returns balances for all configured providers."""
        await self._ensure_session()
        result: Dict[str, float] = {}
        for provider in [SmsProvider.ONLINESIM, SmsProvider.SMS_ACTIVATE, SmsProvider.GRIZZLY_SMS]:
            if provider in self._adapters:
                try:
                    result[provider.value] = await self.get_balance(provider)
                except Exception as exc:
                    logger.warning("Could not fetch balance for %s: %s", provider.value, exc)
                    result[provider.value] = -1.0
            else:
                result[provider.value] = None  # not configured
        return result
