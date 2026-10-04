#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — SMS Gateway API Router
plane-4a454c05 / Task 2: SMS routes for auth-inbox-gateway

Include in main.py with:
    from sms_routes import router as sms_router
    app.include_router(sms_router)
"""

import logging
from typing import Optional

from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel, Field

from sms_gateway import SmsGatewayService, SmsProvider, VirtualNumber

logger = logging.getLogger("sms_routes")

router = APIRouter(prefix="/api/v1/sms", tags=["sms"])

# ---------------------------------------------------------------------------
# Request / Response schemas
# ---------------------------------------------------------------------------


class RentRequest(BaseModel):
    service: str = Field(..., description="Provider-specific service code, e.g. 'go' (Google), 'rg' (Reddit)")
    country_code: int = Field(7, description="ITU-T country code: 7=RU, 1=US, 31=NL")
    preferred_provider: Optional[str] = Field(
        None,
        description="Hint: 'onlinesim' | 'sms_activate' | 'grizzly_sms'"
    )


class RentResponse(BaseModel):
    number: str
    activation_id: str
    provider: str
    service: str
    country_code: int
    expires_at: str


class WaitResponse(BaseModel):
    code: Optional[str] = None
    status: str  # "ready" | "waiting" | "timeout"


class ReleaseResponse(BaseModel):
    released: bool


class BalanceResponse(BaseModel):
    onlinesim: Optional[float] = None
    sms_activate: Optional[float] = None
    grizzly_sms: Optional[float] = None


# ---------------------------------------------------------------------------
# In-memory registry: activation_id → VirtualNumber
# Needed so /wait and /release can look up the full object.
# ---------------------------------------------------------------------------

_registry: dict[str, VirtualNumber] = {}


def _resolve_provider(name: Optional[str]) -> Optional[SmsProvider]:
    if not name:
        return None
    for p in SmsProvider:
        if p.value == name:
            return p
    return None


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------


@router.post("/rent", response_model=RentResponse, summary="Rent a virtual phone number")
async def rent_number(body: RentRequest):
    """
    Rents a virtual phone number from the best available SMS provider.

    - Tries providers in order: preferred → fallback chain (OnlineSim → SmsActivate → GrizzlySms).
    - In mock mode (`SMS_MOCK_MODE=true`) returns a deterministic fake number.
    """
    preferred = _resolve_provider(body.preferred_provider)
    try:
        async with SmsGatewayService() as gw:
            vnum = await gw.rent_number(
                service=body.service,
                country_code=body.country_code,
                preferred_provider=preferred,
            )
    except RuntimeError as exc:
        logger.error("rent_number failed: %s", exc)
        raise HTTPException(status_code=503, detail=str(exc))

    _registry[vnum.activation_id] = vnum
    return RentResponse(
        number=vnum.number,
        activation_id=vnum.activation_id,
        provider=vnum.provider.value,
        service=vnum.service,
        country_code=vnum.country_code,
        expires_at=vnum.expires_at.isoformat(),
    )


@router.get("/wait", summary="Poll for an SMS OTP code")
async def wait_for_sms(
    activation_id: str = Query(..., description="activation_id returned by /rent"),
    provider: str = Query(..., description="Provider name returned by /rent"),
    pattern: str = Query(r"\d{4,6}", description="Regex pattern to match in the SMS body"),
    timeout: int = Query(30, ge=5, le=300, description="Polling timeout in seconds (max 300)"),
):
    """
    Polls the provider for an incoming SMS code.

    - Returns `200 {"code": "12345", "status": "ready"}` when a code arrives.
    - Returns `202 {"status": "waiting"}` if still waiting (client should retry).
    - Returns `200 {"status": "timeout"}` if the timeout elapsed without a code.
    """
    vnum = _registry.get(activation_id)
    if vnum is None:
        # Reconstruct minimal VirtualNumber from query params for one-shot polling
        provider_enum = _resolve_provider(provider)
        if provider_enum is None:
            raise HTTPException(status_code=400, detail=f"Unknown provider: {provider}")
        from datetime import datetime, timezone
        vnum = VirtualNumber(
            number="",
            provider=provider_enum,
            activation_id=activation_id,
            service="",
            country_code=7,
            expires_at=datetime.now(timezone.utc),
        )

    try:
        async with SmsGatewayService() as gw:
            code = await gw.wait_for_sms(vnum, pattern=pattern, timeout=timeout)
    except Exception as exc:
        logger.error("wait_for_sms failed: %s", exc)
        raise HTTPException(status_code=503, detail=str(exc))

    if code:
        return WaitResponse(code=code, status="ready")

    # Return 202 Accepted so the client knows to retry
    from fastapi.responses import JSONResponse
    return JSONResponse(status_code=202, content={"code": None, "status": "waiting"})


@router.delete("/release", response_model=ReleaseResponse, summary="Release / cancel a virtual number")
async def release_number(
    activation_id: str = Query(..., description="activation_id returned by /rent"),
    provider: str = Query(..., description="Provider name returned by /rent"),
):
    """
    Cancels and releases the rented virtual number back to the provider.
    """
    vnum = _registry.pop(activation_id, None)
    if vnum is None:
        provider_enum = _resolve_provider(provider)
        if provider_enum is None:
            raise HTTPException(status_code=400, detail=f"Unknown provider: {provider}")
        from datetime import datetime, timezone
        vnum = VirtualNumber(
            number="",
            provider=provider_enum,
            activation_id=activation_id,
            service="",
            country_code=7,
            expires_at=datetime.now(timezone.utc),
        )

    try:
        async with SmsGatewayService() as gw:
            released = await gw.release_number(vnum)
    except Exception as exc:
        logger.error("release_number failed: %s", exc)
        raise HTTPException(status_code=503, detail=str(exc))

    return ReleaseResponse(released=released)


@router.get("/balance", response_model=BalanceResponse, summary="Get balances for all SMS providers")
async def get_balance():
    """
    Returns the current account balance for each configured SMS provider.
    Providers without API keys are reported as `null`.
    """
    try:
        async with SmsGatewayService() as gw:
            balances = await gw.get_all_balances()
    except Exception as exc:
        logger.error("get_balance failed: %s", exc)
        raise HTTPException(status_code=503, detail=str(exc))

    return BalanceResponse(
        onlinesim=balances.get("onlinesim"),
        sms_activate=balances.get("sms_activate"),
        grizzly_sms=balances.get("grizzly_sms"),
    )
