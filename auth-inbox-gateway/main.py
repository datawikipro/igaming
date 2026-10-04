#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Auth Inbox Gateway
FastAPI microservice for catch-all IMAP email OTP retrieval and SMS code polling.

Endpoints:
  GET  /healthz                          → {"status": "ok"}
  GET  /api/v1/inbox/code?email=&pattern=&sender= → {"code": "123456"} or 404
  GET  /api/v1/inbox/sms?phone=&pattern=          → {"code": "123456"} or 404
  POST /api/v1/inbox/domains             → add catch-all IMAP domain
  GET  /api/v1/inbox/domains             → list configured domains
"""

import asyncio
import email as email_lib
import json
import logging
import os
import re
import time
from concurrent.futures import ThreadPoolExecutor
from typing import Dict, List, Optional, Tuple

from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
import imapclient

# ---------------------------------------------------------------------------
# Logging
# ---------------------------------------------------------------------------
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger("auth-inbox-gateway")

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
CATCHALL_DOMAINS_JSON: str = os.getenv("CATCHALL_DOMAINS_JSON", "[]")
POLL_INTERVAL_SEC: float = float(os.getenv("POLL_INTERVAL_SEC", "3"))
POLL_TIMEOUT_SEC: float = float(os.getenv("POLL_TIMEOUT_SEC", "60"))
CODE_CACHE_TTL_SEC: float = float(os.getenv("CODE_CACHE_TTL_SEC", "300"))  # 5 minutes


# ---------------------------------------------------------------------------
# Models
# ---------------------------------------------------------------------------
class DomainConfig(BaseModel):
    domain: str = Field(..., description="Catch-all domain, e.g. smartbet.guru")
    imap_host: str = Field(..., description="IMAP server hostname")
    imap_port: int = Field(993, description="IMAP port (993 = SSL, 143 = STARTTLS)")
    username: str = Field(..., description="IMAP login username")
    password: str = Field(..., description="IMAP login password")
    use_ssl: bool = Field(True, description="Use SSL/TLS for IMAP connection")


class DomainListResponse(BaseModel):
    domains: List[str]


class CodeResponse(BaseModel):
    code: str


# ---------------------------------------------------------------------------
# In-memory code cache:  key → (code, expires_at)
# ---------------------------------------------------------------------------
_code_cache: Dict[str, Tuple[str, float]] = {}


def _cache_put(key: str, code: str) -> None:
    _code_cache[key] = (code, time.monotonic() + CODE_CACHE_TTL_SEC)
    logger.info("Cache PUT key=%s code=%s ttl=%ss", key, code, CODE_CACHE_TTL_SEC)


def _cache_get(key: str) -> Optional[str]:
    entry = _code_cache.get(key)
    if entry is None:
        return None
    code, expires_at = entry
    if time.monotonic() > expires_at:
        del _code_cache[key]
        logger.debug("Cache EXPIRED key=%s", key)
        return None
    return code


def _cache_delete(key: str) -> None:
    _code_cache.pop(key, None)


# ---------------------------------------------------------------------------
# CatchAllEmailService
# ---------------------------------------------------------------------------
class CatchAllEmailService:
    """
    Manages multiple IMAP connections (one per domain).
    Scans UNSEEN messages addressed to a target email (catch-all inbox),
    extracts OTP codes via regex, marks messages as seen, and caches results.
    """

    def __init__(self) -> None:
        self._domains: Dict[str, DomainConfig] = {}  # domain → config
        self._executor = ThreadPoolExecutor(max_workers=8, thread_name_prefix="imap")
        self._load_from_env()

    def _load_from_env(self) -> None:
        try:
            raw = json.loads(CATCHALL_DOMAINS_JSON)
            for item in raw:
                cfg = DomainConfig(**item)
                self._domains[cfg.domain] = cfg
                logger.info("Loaded IMAP domain from env: %s @ %s:%d", cfg.domain, cfg.imap_host, cfg.imap_port)
        except Exception as exc:
            logger.warning("Failed to parse CATCHALL_DOMAINS_JSON: %s", exc)

    def add_domain(self, cfg: DomainConfig) -> None:
        self._domains[cfg.domain] = cfg
        logger.info("Added IMAP domain: %s @ %s:%d", cfg.domain, cfg.imap_host, cfg.imap_port)

    def list_domains(self) -> List[str]:
        return list(self._domains.keys())

    def _find_domain_for_email(self, email_addr: str) -> Optional[DomainConfig]:
        """Returns the first DomainConfig whose domain suffix matches the email."""
        email_lower = email_addr.lower()
        for domain, cfg in self._domains.items():
            if email_lower.endswith(f"@{domain.lower()}"):
                return cfg
        return None

    # ------------------------------------------------------------------
    # Blocking IMAP helpers (run in executor)
    # ------------------------------------------------------------------

    def _imap_fetch_code(
        self,
        cfg: DomainConfig,
        target_email: str,
        pattern: str,
        sender_filter: Optional[str],
    ) -> Optional[str]:
        """
        Connect to IMAP, search for UNSEEN messages to `target_email`,
        apply regex pattern to extract code, mark message as SEEN.
        Returns the first matching code or None.
        """
        try:
            server = imapclient.IMAPClient(
                cfg.imap_host,
                port=cfg.imap_port,
                ssl=cfg.use_ssl,
                use_uid=True,
            )
            server.login(cfg.username, cfg.password)
            logger.debug("IMAP connected to %s as %s", cfg.imap_host, cfg.username)
        except Exception as exc:
            logger.error("IMAP login failed for %s: %s", cfg.imap_host, exc)
            return None

        try:
            server.select_folder("INBOX")
            criteria = ["UNSEEN", "TO", target_email]
            if sender_filter:
                criteria += ["FROM", sender_filter]

            uids = server.search(criteria)
            if not uids:
                logger.debug("No UNSEEN messages for %s on %s", target_email, cfg.imap_host)
                return None

            logger.info("Found %d UNSEEN message(s) for %s", len(uids), target_email)
            messages = server.fetch(uids, ["RFC822"])

            compiled = re.compile(pattern)
            for uid, msg_data in messages.items():
                raw = msg_data.get(b"RFC822", b"")
                msg = email_lib.message_from_bytes(raw)
                body = _extract_body(msg)

                match = compiled.search(body)
                if match:
                    code = match.group()
                    logger.info("OTP extracted for %s: %s (uid=%s)", target_email, code, uid)
                    # Mark as read
                    try:
                        server.add_flags([uid], [imapclient.SEEN])
                    except Exception as flag_err:
                        logger.warning("Could not mark uid=%s as SEEN: %s", uid, flag_err)
                    return code

            return None

        except Exception as exc:
            logger.error("IMAP fetch error for %s: %s", target_email, exc)
            return None
        finally:
            try:
                server.logout()
            except Exception:
                pass

    # ------------------------------------------------------------------
    # Async polling
    # ------------------------------------------------------------------

    async def poll_email_code(
        self,
        email_addr: str,
        pattern: str,
        sender_filter: Optional[str],
    ) -> Optional[str]:
        """
        Poll IMAP every POLL_INTERVAL_SEC up to POLL_TIMEOUT_SEC.
        Returns the code or None on timeout.
        """
        cache_key = f"email:{email_addr}"
        cached = _cache_get(cache_key)
        if cached:
            logger.info("Cache HIT for %s → %s", cache_key, cached)
            _cache_delete(cache_key)
            return cached

        cfg = self._find_domain_for_email(email_addr)
        if cfg is None:
            logger.warning("No IMAP domain configured for email: %s", email_addr)
            return None

        loop = asyncio.get_event_loop()
        deadline = time.monotonic() + POLL_TIMEOUT_SEC

        while time.monotonic() < deadline:
            code = await loop.run_in_executor(
                self._executor,
                self._imap_fetch_code,
                cfg,
                email_addr,
                pattern,
                sender_filter,
            )
            if code:
                _cache_put(cache_key, code)
                return code

            remaining = deadline - time.monotonic()
            if remaining <= 0:
                break
            await asyncio.sleep(min(POLL_INTERVAL_SEC, remaining))

        logger.warning("Timeout (%ss) waiting for OTP email to %s", POLL_TIMEOUT_SEC, email_addr)
        return None


def _extract_body(msg: email_lib.message.Message) -> str:
    """Extract plain text body from an email.message.Message."""
    parts: List[str] = []
    if msg.is_multipart():
        for part in msg.walk():
            ct = part.get_content_type()
            disp = str(part.get("Content-Disposition", ""))
            if ct in ("text/plain", "text/html") and "attachment" not in disp:
                try:
                    charset = part.get_content_charset() or "utf-8"
                    parts.append(part.get_payload(decode=True).decode(charset, errors="replace"))
                except Exception:
                    pass
    else:
        try:
            charset = msg.get_content_charset() or "utf-8"
            parts.append(msg.get_payload(decode=True).decode(charset, errors="replace"))
        except Exception:
            pass
    return " ".join(parts)


# ---------------------------------------------------------------------------
# SMS stub (placeholder — will be wired to SMS gateway in Task 2)
# ---------------------------------------------------------------------------

# In-memory SMS code store: phone → (code, expires_at)
_sms_store: Dict[str, Tuple[str, float]] = {}


def sms_push_code(phone: str, code: str) -> None:
    """Called by the SMS webhook handler to store an incoming code."""
    _sms_store[phone] = (code, time.monotonic() + CODE_CACHE_TTL_SEC)
    logger.info("SMS code stored for %s: %s", phone, code)


async def poll_sms_code(phone: str, pattern: str) -> Optional[str]:
    """
    Poll the in-memory SMS store every POLL_INTERVAL_SEC up to POLL_TIMEOUT_SEC.
    Returns the code matching pattern, or None on timeout.
    """
    compiled = re.compile(pattern)
    deadline = time.monotonic() + POLL_TIMEOUT_SEC

    while time.monotonic() < deadline:
        entry = _sms_store.get(phone)
        if entry:
            code_candidate, expires_at = entry
            if time.monotonic() <= expires_at:
                m = compiled.search(code_candidate)
                if m:
                    code = m.group()
                    del _sms_store[phone]
                    logger.info("SMS code retrieved for %s: %s", phone, code)
                    return code

        remaining = deadline - time.monotonic()
        if remaining <= 0:
            break
        await asyncio.sleep(min(POLL_INTERVAL_SEC, remaining))

    logger.warning("Timeout (%ss) waiting for SMS OTP for %s", POLL_TIMEOUT_SEC, phone)
    return None


# ---------------------------------------------------------------------------
# FastAPI Application
# ---------------------------------------------------------------------------
app = FastAPI(
    title="Auth Inbox Gateway",
    description="Catch-all IMAP email OTP retrieval, SMS code poller and virtual number gateway for SmartBet.guru",
    version="1.1.0",
)

try:
    from sms_routes import router as sms_router
    app.include_router(sms_router)
    logger.info("SMS gateway routes registered: /api/v1/sms/*")
except ImportError as _e:
    logger.warning("sms_routes not available, SMS gateway endpoints disabled: %s", _e)

email_service = CatchAllEmailService()


@app.get("/healthz", tags=["health"])
async def healthz() -> JSONResponse:
    return JSONResponse({"status": "ok"})


@app.get("/api/v1/inbox/code", response_model=CodeResponse, tags=["inbox"])
async def get_email_code(
    email: str = Query(..., description="Target email address to check"),
    pattern: str = Query(r"\b\d{4,8}\b", description="Regex pattern to extract OTP"),
    sender: Optional[str] = Query(None, description="Optional sender email filter"),
) -> JSONResponse:
    """
    Poll the catch-all IMAP inbox for an OTP sent to `email`.
    Retries every 3s for up to 60s. Returns 404 on timeout.
    """
    logger.info("GET /api/v1/inbox/code email=%s pattern=%s sender=%s", email, pattern, sender)
    code = await email_service.poll_email_code(email, pattern, sender)
    if code is None:
        raise HTTPException(status_code=404, detail=f"No OTP found for {email} within timeout")
    return JSONResponse({"code": code})


@app.get("/api/v1/inbox/sms", response_model=CodeResponse, tags=["inbox"])
async def get_sms_code(
    phone: str = Query(..., description="Target phone number (E.164 preferred)"),
    pattern: str = Query(r"\b\d{4,8}\b", description="Regex pattern to extract OTP"),
) -> JSONResponse:
    """
    Poll the in-memory SMS store for an OTP received on `phone`.
    Retries every 3s for up to 60s. Returns 404 on timeout.
    """
    clean_phone = re.sub(r"[^\d+]", "", phone)
    logger.info("GET /api/v1/inbox/sms phone=%s pattern=%s", clean_phone, pattern)
    code = await poll_sms_code(clean_phone, pattern)
    if code is None:
        raise HTTPException(status_code=404, detail=f"No SMS OTP found for {clean_phone} within timeout")
    return JSONResponse({"code": code})


@app.post("/api/v1/inbox/domains", tags=["domains"])
async def add_domain(cfg: DomainConfig) -> JSONResponse:
    """Add or update a catch-all IMAP domain configuration."""
    email_service.add_domain(cfg)
    return JSONResponse({"status": "added", "domain": cfg.domain})


@app.get("/api/v1/inbox/domains", response_model=DomainListResponse, tags=["domains"])
async def list_domains() -> JSONResponse:
    """List all configured catch-all IMAP domains."""
    return JSONResponse({"domains": email_service.list_domains()})


@app.post("/api/v1/inbox/sms/push", include_in_schema=True, tags=["sms-webhook"])
async def push_sms(
    phone: str = Query(..., description="Phone number that received the SMS"),
    message: str = Query(..., description="Full SMS message text"),
) -> JSONResponse:
    """
    Webhook endpoint for SMS gateway to push incoming messages.
    Stores the full message text; pattern matching happens on poll.
    """
    clean_phone = re.sub(r"[^\d+]", "", phone)
    sms_push_code(clean_phone, message)
    return JSONResponse({"status": "stored", "phone": clean_phone})
