#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — SMS Gateway Client (smm-agent side)
plane-4a454c05 / Task 2: SmsGatewayClient

Thin async HTTP client that calls auth-inbox-gateway SMS endpoints:
  POST /api/v1/sms/rent
  GET  /api/v1/sms/wait
  DEL  /api/v1/sms/release
  GET  /api/v1/sms/balance

Environment variables:
  SMS_GATEWAY_URL  — Base URL of auth-inbox-gateway (default: http://auth-inbox-gateway:8000)
  SMS_MOCK_MODE    — "true" to skip real HTTP calls and return mock codes
"""

import asyncio
import hashlib
import logging
import os
import time
from typing import Dict, Optional

try:
    import aiohttp
except ImportError:
    aiohttp = None

logger = logging.getLogger("SmsGatewayClient")

SMS_GATEWAY_URL: str = os.getenv("SMS_GATEWAY_URL", "http://auth-inbox-gateway:8000")
SMS_MOCK_MODE: bool = os.getenv("SMS_MOCK_MODE", "false").lower() == "true"

_BASE = SMS_GATEWAY_URL.rstrip("/") + "/api/v1/sms"


def _mock_code(seed: str) -> str:
    """Deterministic 6-digit OTP from seed (same algorithm as sms_gateway.py)."""
    h = int(hashlib.md5(seed.encode()).hexdigest()[:6], 16)
    return str(100000 + (h % 900000))


class SmsGatewayClient:
    """
    Async HTTP client for the auth-inbox-gateway SMS virtual number service.

    Typical flow:
        async with SmsGatewayClient() as client:
            info = await client.rent_number("go", country_code=7)
            code = await client.wait_for_code(
                info["activation_id"], info["provider"], max_wait=120
            )
            await client.release_number(info["activation_id"], info["provider"])

    Mock mode (SMS_MOCK_MODE=true):
        No HTTP calls are made; rent_number returns a fake object,
        wait_for_code returns a deterministic code, release_number returns True.
    """

    def __init__(
        self,
        base_url: str = SMS_GATEWAY_URL,
        mock_mode: bool = SMS_MOCK_MODE,
    ):
        self._base = base_url.rstrip("/") + "/api/v1/sms"
        self._mock_mode = mock_mode
        self._session: Optional["aiohttp.ClientSession"] = None

    # ------------------------------------------------------------------
    # Async context manager
    # ------------------------------------------------------------------

    async def __aenter__(self) -> "SmsGatewayClient":
        if not self._mock_mode:
            if aiohttp is None:
                raise ImportError("aiohttp is required. Install it or set SMS_MOCK_MODE=true.")
            self._session = aiohttp.ClientSession()
        return self

    async def __aexit__(self, *_) -> None:
        await self.close()

    async def close(self) -> None:
        if self._session and not self._session.closed:
            await self._session.close()

    # ------------------------------------------------------------------
    # Internal helpers
    # ------------------------------------------------------------------

    async def _get(self, path: str, params: dict) -> dict:
        url = f"{self._base}{path}"
        async with self._session.get(
            url, params=params, timeout=aiohttp.ClientTimeout(total=30)
        ) as r:
            r.raise_for_status()
            return await r.json()

    async def _post(self, path: str, json_body: dict) -> dict:
        url = f"{self._base}{path}"
        async with self._session.post(
            url, json=json_body, timeout=aiohttp.ClientTimeout(total=30)
        ) as r:
            r.raise_for_status()
            return await r.json()

    async def _delete(self, path: str, params: dict) -> dict:
        url = f"{self._base}{path}"
        async with self._session.delete(
            url, params=params, timeout=aiohttp.ClientTimeout(total=30)
        ) as r:
            r.raise_for_status()
            return await r.json()

    # ------------------------------------------------------------------
    # Public API
    # ------------------------------------------------------------------

    async def rent_number(
        self,
        service: str,
        country_code: int = 7,
        preferred_provider: Optional[str] = None,
    ) -> Dict:
        """
        Rents a virtual phone number via auth-inbox-gateway.

        Args:
            service:            Service code (e.g. "go" for Google, "rg" for Reddit).
            country_code:       ITU-T country code (7=RU, 1=US, 31=NL).
            preferred_provider: Optional provider hint ("onlinesim" | "sms_activate" | "grizzly_sms").

        Returns:
            dict with keys: number, activation_id, provider, service, country_code, expires_at
        """
        if self._mock_mode:
            mock_id = f"mock-{service}-{country_code}-{int(time.time())}"
            number = f"+7{'9' + str(abs(hash(mock_id)) % 10**9):0>10}"[:12]
            logger.info("Mock mode: rented %s for service=%s", number, service)
            return {
                "number": number,
                "activation_id": mock_id,
                "provider": "mock",
                "service": service,
                "country_code": country_code,
                "expires_at": None,
            }

        body: Dict = {"service": service, "country_code": country_code}
        if preferred_provider:
            body["preferred_provider"] = preferred_provider

        logger.info("Renting number via gateway: service=%s country=%d", service, country_code)
        data = await self._post("/rent", body)
        logger.info("Rented %s (activation_id=%s via %s)", data.get("number"), data.get("activation_id"), data.get("provider"))
        return data

    async def wait_for_code(
        self,
        activation_id: str,
        provider: str,
        pattern: str = r"\d{4,6}",
        max_wait: int = 120,
        poll_interval: int = 10,
    ) -> Optional[str]:
        """
        Polls auth-inbox-gateway until an SMS code arrives or max_wait expires.

        The gateway uses short per-request timeouts (30 s), so this method
        re-polls every *poll_interval* seconds until *max_wait* total seconds pass.

        Args:
            activation_id:  activation_id returned by rent_number().
            provider:       Provider name returned by rent_number().
            pattern:        Regex to match in the SMS body.
            max_wait:       Total maximum wait time in seconds.
            poll_interval:  Seconds between /wait calls.

        Returns:
            The matched code string, or None on timeout.
        """
        if self._mock_mode:
            code = _mock_code(activation_id)
            logger.info("Mock mode: returning code %s for activation_id=%s", code, activation_id)
            return code

        deadline = time.monotonic() + max_wait
        while time.monotonic() < deadline:
            remaining = int(deadline - time.monotonic())
            fetch_timeout = min(poll_interval, remaining, 30)

            try:
                logger.debug(
                    "Polling SMS for activation_id=%s (remaining=%.0fs) ...",
                    activation_id, remaining,
                )
                params = {
                    "activation_id": activation_id,
                    "provider": provider,
                    "pattern": pattern,
                    "timeout": fetch_timeout,
                }
                url = f"{self._base}/wait"
                async with self._session.get(
                    url, params=params, timeout=aiohttp.ClientTimeout(total=fetch_timeout + 5)
                ) as r:
                    if r.status == 200:
                        data = await r.json()
                        if data.get("status") == "ready" and data.get("code"):
                            logger.info("SMS code received: %s", data["code"])
                            return data["code"]
                    elif r.status == 202:
                        logger.debug("Still waiting for SMS (202 Accepted)...")
                    else:
                        text = await r.text()
                        logger.warning("Unexpected /wait response %d: %s", r.status, text)
            except asyncio.TimeoutError:
                logger.debug("Poll timeout, retrying...")
            except Exception as exc:
                logger.warning("Error polling SMS: %s", exc)

            await asyncio.sleep(min(poll_interval, max(0, deadline - time.monotonic())))

        logger.warning("Timeout waiting for SMS code for activation_id=%s", activation_id)
        return None

    async def release_number(self, activation_id: str, provider: str) -> bool:
        """
        Releases the rented virtual number back to the provider.

        Args:
            activation_id: activation_id returned by rent_number().
            provider:      Provider name returned by rent_number().

        Returns:
            True if successfully released, False otherwise.
        """
        if self._mock_mode:
            logger.info("Mock mode: releasing activation_id=%s (noop)", activation_id)
            return True

        try:
            data = await self._delete("/release", {"activation_id": activation_id, "provider": provider})
            released = bool(data.get("released", False))
            logger.info("Released number activation_id=%s -> %s", activation_id, released)
            return released
        except Exception as exc:
            logger.error("Failed to release activation_id=%s: %s", activation_id, exc)
            return False

    async def get_balance(self) -> Dict[str, Optional[float]]:
        """
        Returns balances for all configured SMS providers.

        Returns:
            dict: {"onlinesim": 12.5, "sms_activate": 8.0, "grizzly_sms": 5.5}
                  Unconfigured providers have None.
        """
        if self._mock_mode:
            return {"onlinesim": 999.0, "sms_activate": 999.0, "grizzly_sms": 999.0}

        data = await self._get("/balance", {})
        return data
