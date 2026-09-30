#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — 2FA & Auth Inbox Gateway Client
Task #55 [auto-reg] (IMAP OTP retrieval, SMS OTP poller, TOTP Generator)

Features:
1. IMAP email OTP code poller via auth-inbox-gateway:8000 (/api/v1/inbox/code).
2. SMS 2FA code poller via auth-inbox-gateway:8000 (/api/v1/inbox/sms).
3. Standard TOTP (RFC 6238) token generator with pure-python fallback.
4. Embedded mock mode for local testing and isolated environments.
"""

import base64
import hashlib
import hmac
import json
import logging
import os
import re
import struct
import time
from typing import Dict, List, Optional

try:
    import requests
except ImportError:
    requests = None

try:
    import pyotp
except ImportError:
    pyotp = None

logger = logging.getLogger("AuthInboxClient")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")

DEFAULT_GATEWAY_URL = os.getenv("AUTH_INBOX_GATEWAY_URL", "http://auth-inbox-gateway:8000")


class AuthInboxGatewayClient:
    """
    Client for auth-inbox-gateway to retrieve 2FA email/SMS verification codes
    and generate TOTP tokens for sportsbooks, affiliate networks, and social platforms.
    """

    def __init__(
        self,
        base_url: str = DEFAULT_GATEWAY_URL,
        mock_mode: bool = False
    ):
        self.base_url = base_url.rstrip("/")
        self.mock_mode = mock_mode
        self._mock_codes: Dict[str, str] = {}
        if requests is not None:
            self.session = requests.Session()
        else:
            self.session = None

    def set_mock_code(self, identifier: str, code: str) -> None:
        """Stores a mock code for testing email or phone."""
        self._mock_codes[identifier] = code

    def clear_mock_codes(self) -> None:
        self._mock_codes.clear()

    def is_gateway_healthy(self, timeout: float = 3.0) -> bool:
        """Checks if auth-inbox-gateway is UP and reachable."""
        if self.mock_mode:
            return True
        if self.session is None:
            return False
        try:
            url = f"{self.base_url}/healthz"
            resp = self.session.get(url, timeout=timeout)
            return resp.status_code == 200
        except Exception:
            try:
                # Alternate actuator / status endpoint
                url = f"{self.base_url}/api/v1/health"
                resp = self.session.get(url, timeout=timeout)
                return resp.status_code == 200
            except Exception:
                return False

    def fetch_email_code(
        self,
        email: str,
        pattern: str = r"\b\d{4,6}\b",
        sender_filter: Optional[str] = None,
        max_retries: int = 5,
        retry_delay: float = 2.0
    ) -> Optional[str]:
        """
        Polls auth-inbox-gateway for incoming verification OTP sent to email.
        """
        # Check mock store first or if mock mode enabled
        if email in self._mock_codes:
            code = self._mock_codes.pop(email)
            logger.info("Found mock 2FA code for %s: %s", email, code)
            return code

        if self.mock_mode or self.session is None:
            # Fallback generated test code for mock execution
            logger.info("Mock mode active: generating deterministic verification code for %s", email)
            hash_int = int(hashlib.md5(email.encode("utf-8")).hexdigest()[:6], 16)
            return str(100000 + (hash_int % 900000))

        url = f"{self.base_url}/api/v1/inbox/code"
        params = {"email": email, "pattern": pattern}
        if sender_filter:
            params["sender"] = sender_filter

        for attempt in range(1, max_retries + 1):
            try:
                logger.info("Polling 2FA code for %s (attempt %d/%d)...", email, attempt, max_retries)
                resp = self.session.get(url, params=params, timeout=10)
                if resp.status_code == 200:
                    data = resp.json()
                    code = data.get("code")
                    if code:
                        logger.info("Successfully received 2FA email code for %s: %s", email, code)
                        return str(code)
            except Exception as e:
                logger.warning("Gateway poll attempt %d failed: %s", attempt, e)

            if attempt < max_retries:
                time.sleep(retry_delay)

        logger.warning("Timeout waiting for 2FA code on %s", email)
        return None

    def fetch_sms_code(
        self,
        phone: str,
        pattern: str = r"\b\d{4,6}\b",
        max_retries: int = 5,
        retry_delay: float = 2.0
    ) -> Optional[str]:
        """
        Polls auth-inbox-gateway for incoming SMS verification OTP sent to phone.
        """
        clean_phone = re.sub(r"[^\d+]", "", phone)
        if clean_phone in self._mock_codes:
            code = self._mock_codes.pop(clean_phone)
            logger.info("Found mock SMS code for %s: %s", clean_phone, code)
            return code

        if self.mock_mode or self.session is None:
            hash_int = int(hashlib.md5(clean_phone.encode("utf-8")).hexdigest()[:6], 16)
            return str(100000 + (hash_int % 900000))

        url = f"{self.base_url}/api/v1/inbox/sms"
        params = {"phone": clean_phone, "pattern": pattern}

        for attempt in range(1, max_retries + 1):
            try:
                logger.info("Polling SMS 2FA code for %s (attempt %d/%d)...", clean_phone, attempt, max_retries)
                resp = self.session.get(url, params=params, timeout=10)
                if resp.status_code == 200:
                    data = resp.json()
                    code = data.get("code")
                    if code:
                        logger.info("Successfully received SMS 2FA code for %s: %s", clean_phone, code)
                        return str(code)
            except Exception as e:
                logger.warning("Gateway SMS poll attempt %d failed: %s", attempt, e)

            if attempt < max_retries:
                time.sleep(retry_delay)

        logger.warning("Timeout waiting for SMS 2FA code on %s", clean_phone)
        return None

    def generate_totp_code(
        self,
        totp_secret: str,
        interval: int = 30,
        digits: int = 6,
        timestamp: Optional[int] = None
    ) -> str:
        """
        Generates standard RFC 6238 TOTP token from base32 secret.
        Works with pyotp or pure-python standard library fallback.
        """
        if pyotp is not None and timestamp is None:
            return pyotp.TOTP(totp_secret, interval=interval, digits=digits).now()

        # Pure standard library implementation of RFC 6238 (HMAC-SHA1 TOTP)
        secret_clean = totp_secret.replace(" ", "").upper()
        # Add proper base32 padding if missing
        missing_padding = len(secret_clean) % 8
        if missing_padding:
            secret_clean += "=" * (8 - missing_padding)

        key = base64.b32decode(secret_clean, casefold=True)
        current_time = timestamp if timestamp is not None else int(time.time())
        counter = int(current_time // interval)
        msg = struct.pack(">Q", counter)
        digest = hmac.new(key, msg, hashlib.sha1).digest()
        offset = digest[-1] & 0x0F
        code = (struct.unpack(">I", digest[offset:offset + 4])[0] & 0x7FFFFFFF) % (10 ** digits)
        return str(code).zfill(digits)
