import logging
import os
from typing import Optional, Dict, Any, List
from fastapi import FastAPI, HTTPException, Query, Body
from pydantic import BaseModel, Field

from app.totp_service import TotpService
from app.imap_service import ImapInboxService
from app.storage import AccountStorage

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("auth_inbox_gateway")

app = FastAPI(
    title="SmartBet Auth & 2FA Inbox Gateway",
    version="1.0.0",
    description="Centralized 2FA code generation (TOTP) and IMAP inbox email verification code extractor for SMM agents."
)

storage = AccountStorage()

class AccountRegisterDTO(BaseModel):
    account_id: str = Field(..., description="Unique persona / account identifier, e.g. smm_threads_en_01")
    platform: str = Field(..., description="Target platform: threads, instagram, reddit, twitter, etc.")
    username: str
    password: Optional[str] = None
    totp_secret: Optional[str] = Field(None, description="Base32 TOTP secret key for Authenticator 2FA")
    email_address: Optional[str] = None
    email_password: Optional[str] = None
    imap_host: Optional[str] = "imap.gmail.com"
    imap_port: Optional[int] = 993
    language: Optional[str] = "en"
    persona_bio: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None

@app.get("/healthz")
@app.get("/actuator/health")
def health():
    redis_ok = storage.ping()
    return {
        "status": "UP" if redis_ok else "DEGRADED",
        "redis": "UP" if redis_ok else "DOWN"
    }

# ── TOTP Endpoints ─────────────────────────────────────────────────────────────

@app.get("/api/v1/totp/generate")
def generate_totp_from_secret(secret: str = Query(..., description="Base32 TOTP secret")):
    """Generates current 6-digit TOTP code directly from provided secret string."""
    try:
        return TotpService.generate_current_code(secret)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Invalid TOTP secret: {str(e)}")

@app.get("/api/v1/accounts/{account_id}/totp")
def get_account_totp(account_id: str):
    """Generates current TOTP code for a stored account."""
    acc = storage.get_account(account_id)
    if not acc:
        raise HTTPException(status_code=404, detail=f"Account {account_id} not found")
    
    totp_secret = acc.get("totp_secret")
    if not totp_secret:
        raise HTTPException(status_code=400, detail=f"No TOTP secret configured for {account_id}")

    try:
        res = TotpService.generate_current_code(totp_secret)
        res["account_id"] = account_id
        res["platform"] = acc.get("platform")
        return res
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating TOTP: {str(e)}")

# ── Email 2FA Endpoints ────────────────────────────────────────────────────────

@app.get("/api/v1/accounts/{account_id}/email-code")
def extract_email_code_for_account(
    account_id: str,
    service: Optional[str] = None,
    timeout_sec: int = Query(45, ge=5, le=120)
):
    """Polls email inbox for account and extracts 2FA code."""
    acc = storage.get_account(account_id)
    if not acc:
        raise HTTPException(status_code=404, detail=f"Account {account_id} not found")

    email_addr = acc.get("email_address")
    email_pwd = acc.get("email_password")
    imap_host = acc.get("imap_host", "imap.gmail.com")
    imap_port = acc.get("imap_port", 993)
    target_service = service or acc.get("platform", "default")

    if not email_addr or not email_pwd:
        raise HTTPException(status_code=400, detail=f"Email credentials not configured for {account_id}")

    code_data = ImapInboxService.get_latest_code(
        host=imap_host,
        user=email_addr,
        password=email_pwd,
        service_name=target_service,
        port=imap_port,
        timeout_seconds=timeout_sec
    )

    if not code_data:
        raise HTTPException(status_code=408, detail=f"Timeout waiting for 2FA email code for {email_addr}")

    code_data["account_id"] = account_id
    return code_data

# ── Account Management & Cookies ───────────────────────────────────────────────

@app.post("/api/v1/accounts/register")
def register_account(payload: AccountRegisterDTO):
    """Registers or updates account metadata in the centralized vault."""
    storage.save_account(payload.account_id, payload.model_dump())
    return {"status": "success", "account_id": payload.account_id}

@app.get("/api/v1/accounts")
def list_accounts():
    """Returns list of registered SMM accounts."""
    accounts = storage.list_accounts()
    # Mask passwords
    for a in accounts:
        if a.get("password"):
            a["password"] = "***"
        if a.get("email_password"):
            a["email_password"] = "***"
        if a.get("totp_secret"):
            a["has_totp"] = True
            a["totp_secret"] = "***"
        else:
            a["has_totp"] = False
    return {"count": len(accounts), "accounts": accounts}

@app.post("/api/v1/accounts/{account_id}/cookies")
def save_account_cookies(account_id: str, cookies: Any = Body(...)):
    """Saves browser cookies and storage state for persistent sessions."""
    storage.save_cookies(account_id, cookies)
    return {"status": "saved", "account_id": account_id}

@app.get("/api/v1/accounts/{account_id}/cookies")
def get_account_cookies(account_id: str):
    """Retrieves saved cookies and session for authenticated launch."""
    cookies = storage.get_cookies(account_id)
    if cookies is None:
        raise HTTPException(status_code=404, detail="No cookies saved for this account")
    return {"account_id": account_id, "cookies": cookies}
